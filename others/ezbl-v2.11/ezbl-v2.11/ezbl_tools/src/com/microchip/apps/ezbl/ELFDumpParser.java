/*
 *******************************************************************************
 * Copyright (c) 2018 Microchip Technology Inc. All rights reserved.
 *
 * Microchip licenses to you the right to use, modify, copy and distribute
 * Software only when embedded on a Microchip microcontroller or digital signal
 * controller that is integrated into your product or third party product
 * (pursuant to the sublicense terms in the accompanying license agreement).
 *
 * You should refer to the license agreement accompanying this Software for
 * additional information regarding your rights and obligations.
 *
 * SOFTWARE AND DOCUMENTATION ARE PROVIDED AS IS WITHOUT WARRANTY OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
 * MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR
 * PURPOSE. IN NO EVENT SHALL MICROCHIP OR ITS LICENSORS BE LIABLE OR OBLIGATED
 * UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF
 * WARRANTY, OR OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR
 * EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF
 * PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD
 * PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR
 * COSTS.
 *******************************************************************************
 */
package com.microchip.apps.ezbl;

import static com.microchip.apps.ezbl.Multifunction.CatStringList;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;


/**
 *
 * @author C12128
 */
// Parses a text dump file generated by passing a .elf file to the xc16-objdump
// compiler utility.
public class ELFDumpParser implements Serializable
{
    private enum PiecewiseParseStates
    {
        PROCESSING_SECTION_HEADERS,
        PROCESSING_SYMBOL_TABLE,
        PROCESSING_SECTION_CONTENTS,
        PROCESSING_DISASSEMBLY,
        PROCESSING_OTHER,
    };

    public List<Section> sections = new ArrayList<>();
    public List<Symbol> symbols = new ArrayList<>();
    public TreeMap<Long, String> symMap = new TreeMap<>();                  // Symbol address, Symbol name
    public TreeMap<String, Symbol> symbolsByName = new TreeMap<>();         // Symbol name, Symbol class
    public TreeMap<String, Symbol> functionSymsByName = new TreeMap<>();    // Symbol name, Symbol class
    public TreeMap<Long, Symbol> functionSymsByAddr = new TreeMap<>();      // Symbol address, Symbol class
    public TreeMap<Long, Section> romSectionMapByAddr = new TreeMap<>();    // Program address, Section class reference
    public Map<String, Section> romSectionMapByName = new HashMap<>();      // ROM sec name, Section class reference
    public Map<String, Section> sectionsByName = new HashMap<>();           // ROM sec name, Section class reference
    public TreeMap<Integer, String> disassemblyMap = new TreeMap<>();       // Program address, xc16-objdump disassembly listing line contents
    private String piecewiseSecName = null;
    private List<DataRecord> piecewiseSecData = null;
    private List<Process> parserProcesses = new ArrayList<>();
    private List<InputStream> parserOutput = new ArrayList<>();
    private String piecewiseResidual = "";
    private PiecewiseParseStates parser = PiecewiseParseStates.PROCESSING_OTHER;
    public String symbolTableAsString = null;
    public List<String> symbolTableAsStrings = null;

    public ELFDumpParser()
    {
    }

    /**
     * This function is deprecated since it is slower. Use startObjDump()
     * followed by parseAllObjOutput() instead.
     *
     * Parses a text dump file generated by passing a .elf file to the
     * xc16-objdump compiler utility.
     *
     * @param dumpContents The output of the xc16-objdump execution. This can be
     *                     generated and conveniently stored in a text file
     *                     using a command line, such as: "C:\Program Files
     *                     (x86)\Microchip\xc16\vx.xx\bin\xc16-objdump -x -t -r
     *                     BuiltApplication.elf > "ELFDump.txt"
     */
    @Deprecated
    public ELFDumpParser(String dumpContents)
    {
        Pattern p;
        Matcher m;
        String parseLines[];

        symbols = new ArrayList<>();
        sections = new ArrayList<>();

        // Replace \r\n sequences (if any) with just \n
        dumpContents = dumpContents.replaceAll("\r", "");

        // Extract the sec table (when done, table is in m.group(1))
        p = Pattern.compile("\nSections:[\\s]*?\n[^\\n]*?\n(([\\s[0-9]]*?[^\\n]*?\n[^\\n]*?\n)*?)(?=[^\\s0-9])", Pattern.DOTALL);
        m = p.matcher(dumpContents);
        if(!m.find())
        {
            System.err.println("EZBL: Could not locate Sections table. Ensure \"Symbols info: Strip all\" option was not selected when building.");
            return;
        }
        // Decode all sections
        parseLines = m.group(1).split("\n");
        for(int i = 0; i < parseLines.length; i += 2)
        {
            Section sec = new Section(parseLines[i] + "\n" + parseLines[i + 1]);
            sections.add(sec);
            sectionsByName.put(sec.name, sec);
        }

        // Extract the symbol table (when done, table is in m.group(1))
        p = Pattern.compile("\nSYMBOL TABLE:[^\n]*?\n(.*)\n{2}", Pattern.DOTALL);
        m = p.matcher(dumpContents);
        if(!m.find())
        {
            System.err.println("EZBL: Could not locate SYMBOL TABLE. Ensure \"Symbols info: Strip all\" option was not selected when building.");
            return;
        }

        // Decode all symbols
        parseLines = m.group(1).split("\n");
        String lastFilenameFound = "";

        for(String symbolLine : parseLines)
        {
            Symbol symbol = new Symbol(symbolLine);
            symbol.probableFile = lastFilenameFound;
            if(symbol.flags.file)
            {
                lastFilenameFound = symbol.name;
            }
            else if(symbol.section.equals("__c30_signature"))
            {
                lastFilenameFound = "*";
            }

            symbols.add(symbol);
        }
    }

    // Call this to asynchronously launch xc16-objdump.exe, then follow up with
    // parseAllObjOutput(), passing the returned process index to block until
    // dump/parse complete.
    public int startObjDump(String... commandAndOptions)
    {
        ProcessBuilder proc = new ProcessBuilder(commandAndOptions);
        return startObjDump(proc);
    }

    public int startObjDump(List<String> commandAndOptions)
    {
        ProcessBuilder proc = new ProcessBuilder(commandAndOptions);
        return startObjDump(proc);
    }

    public int startObjDump(ProcessBuilder proc)
    {
        try
        {
            proc.redirectErrorStream(true);
            Process ps = proc.start();
            parserProcesses.add(ps);
            parserOutput.add(ps.getInputStream());

            return parserProcesses.size() - 1;
        }
        catch(IOException e)
        {
            System.err.println("ezbl_tools: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Reads and parses output from the xc16-objdump process until EOF is
     * reached. Because parsing can be done while xc16-objdump is still
     * executing, this function takes advantage of multiple CPU cores.
     *
     * @param processIndex
     *
     * @return
     */
    public String parseAllObjOutput(int processIndex)
    {
        // Do nothing if the index isn't valid since XC16 needs two operations and XC32 only needs one -> makes it easier to just call this twice
        if(processIndex >= parserProcesses.size())
        {
            return "";
        }

        List<String> objDumpText = new ArrayList<>();
        boolean lastIteration = false;
        Charset charset = Charset.availableCharsets().get("ISO-8859-1");

        InputStream obj = parserOutput.get(processIndex);
        byte b[] = new byte[8192];

        while(true) // Loop so we piecewise parse all data from a single process first before moving to the next. We do not support parallel parsing right now.
        {
            try
            {
                int readBytes = obj.read(b);
                if(readBytes <= 0)
                {
                    parserProcesses.remove(processIndex);
                    parserOutput.remove(processIndex);
                    parsePiecewise((String)null);       // Flag to indicate EOF reached and final processing needs to be applied if any
                    return CatStringList(objDumpText);
                }

                String piece = new String(b, 0, readBytes, charset);
                objDumpText.add(piece);
                parsePiecewise(piece);
            }
            catch(IOException e)
            {   // Process likely ended, but maybe it hasn't started yet, so confirm that it is ended
                try
                {
                    int retCode = parserProcesses.get(processIndex).waitFor();  // Go to sleep until the process is really terminated
                    if(retCode == 0 && !lastIteration)
                    {
                        lastIteration = true;
                        continue;               // Try once more to read any last data
                    }
                }
                catch(InterruptedException ex)
                {
                    // Nothing special - terminate
                }
                b = null;
                parserProcesses.remove(processIndex);
                parserOutput.remove(processIndex);
                parsePiecewise((String)null);           // Flag to indicate EOF reached and final processing needs to be applied if any
                return CatStringList(objDumpText);
            }
        }
    }

    private void endPiecewiseSectionDataParsing()
    {
        if((symbolTableAsStrings != null) && (!symbolTableAsStrings.isEmpty()))
        {
            symbolTableAsString = CatStringList(symbolTableAsStrings);
            symbolTableAsStrings = null;
        }

        if(piecewiseSecName == null)
        {
            return;
        }

        // Find matching sec from already known Section header sections
        boolean matchFound = false;
        DataRecord.CoalesceRecords(piecewiseSecData, true);

        Section existingSec = sectionsByName.get(piecewiseSecName);
        if((existingSec != null))
        {
            if(piecewiseSecData.size() > 0)
            {
                if(piecewiseSecData.get(0).address != existingSec.loadMemoryAddress)
                {
                    if(piecewiseSecData.get(0).address == existingSec.virtualMemoryAddress)
                        piecewiseSecData.get(0).address = existingSec.loadMemoryAddress;
                }
                existingSec.data = piecewiseSecData.get(0);
                if(piecewiseSecData.size() > 1)
                {
                    // This should never happen; if it does, we might need to fix something
                    System.err.println("ezbl_tools: More section fragments than expected.");
                }
            }
            matchFound = true;
        }
        if(!matchFound)
        {
            for(Section s : sections)
            {
                if(s.name.equals(piecewiseSecName) && ((s.loadMemoryAddress == piecewiseSecData.get(0).address) || (s.virtualMemoryAddress == piecewiseSecData.get(0).address)))
                {
                    if(piecewiseSecData.size() > 0)
                    {
                        s.data = piecewiseSecData.get(0);
                        if(piecewiseSecData.size() > 1)
                        {
                            // This should never happen; if it does, we might need to fix something
                            System.err.println("ezbl_tools: More section fragments than expected.");
                        }
                    }
                    matchFound = true;
                    break;
                }
            }
        }

        if(!matchFound)
        {   // Decoded a previously unknown Section (i.e. never looked at the Section headers table). Save it as a new sec.
            Section s = new Section();
            s.name = piecewiseSecName;
            if(piecewiseSecData.size() > 0)
            {
                s.data = piecewiseSecData.get(0);
                if(piecewiseSecData.size() > 1)
                {
                    // This should never happen; if it does, we might need to fix something
                    System.err.println("ezbl_tools: More section fragments than expected.");
                }
                s.loadMemoryAddress = s.data.address;
                s.virtualMemoryAddress = s.data.address;
                s.size = s.data.architecture16Bit ? (s.data.data.length + 2) / 3 * 2 : s.data.data.length;
                s.isDebug = true;
            }
            sections.add(s);
            sectionsByName.put(s.name, s);
            if(s.isROM)
            {
                romSectionMapByName.put(s.name, s);
                for(long addr = s.loadMemoryAddress; addr < s.loadMemoryAddress + s.size; addr += 0x2)
                {
                    romSectionMapByAddr.put(addr, s);
                }
            }
        }
        piecewiseSecName = null;
        piecewiseSecData = null;
    }

    public void parsePiecewise(String dumpText)
    {
        int startIndex;
        int newLineIndex = -1;
        boolean elementChange = false;

        if(dumpText == null)    // null string indicates final EOF call
        {
            endPiecewiseSectionDataParsing();
            return;
        }

        piecewiseResidual += dumpText;
        piecewiseResidual = piecewiseResidual.replace("\r", "");

        while(true)
        {
            startIndex = newLineIndex + 1;

            // Find the first newline
            newLineIndex = piecewiseResidual.indexOf('\n', startIndex);
            if(newLineIndex < 0)
            {   // Didn't find one, so we will have to hold onto this data as residual info until we do get a newline
                break;
            }

            // See if we are done with the last parse element and could be comming upon a new element to decode
            if(piecewiseResidual.startsWith("Sections:", startIndex))
            {
                newLineIndex = piecewiseResidual.indexOf('\n', newLineIndex + 1);
                if(newLineIndex < 0)
                {
                    break;
                }
                parser = PiecewiseParseStates.PROCESSING_SECTION_HEADERS;
                elementChange = true;
            }
            else if(piecewiseResidual.startsWith("SYMBOL TABLE:", startIndex))
            {
                parser = PiecewiseParseStates.PROCESSING_SYMBOL_TABLE;
                symbolTableAsStrings = new ArrayList<>();
                symbolTableAsString = null;
                elementChange = true;
            }
            else if(piecewiseResidual.startsWith("Contents of section ", startIndex))
            {
                // Example .elf dump lines with -s passed to xc16-objdump
                //
                //Contents of sec .reset:
                // 0000 240204 000000                $.....
                //Contents of sec .text:
                // 0224 6f3f21 2efe22 0e0188 000000  o?!.."......
                // 022c 000120 200288 0c0007 004122  ..  ......A"
                //Contents of sec .config_GSSK:
                // f80004 cfffff                       ...
                parser = PiecewiseParseStates.PROCESSING_SECTION_CONTENTS;
                elementChange = true;
            }
            else if(piecewiseResidual.startsWith("Disassembly of section ", startIndex))
            {
                // Example .elf dump lines with --disassemble passed to xc16-objdump
                //
                // 108:   b4 af 02
                // 10a:   00 b0 02        .pword 0x02b000
                //Disassembly of sec .text.EZBLNoProgramRanges:
                //
                //0000010c <.text.EZBLNoProgramRanges>:
                // 10c:   00 00 00        nop
                // 10e:   00 1c 00        nop
                // 110:   80 af 02        call      0xaf84af80 <__FDEVOPT1+0xaf81ffd0>
                // 112:   84 af 02
                // 114:   98 af 02        call      0xafb4af98 <__FDEVOPT1+0xafb1ffe8>
                // 116:   b4 af 02
                //Disassembly of sec .text.EZBLNoEraseRanges:
                //
                //00000118 <.text.EZBLNoEraseRanges>:
                // 118:   00 00 00        nop
                // 11a:   00 1c 00        nop               ...
                parser = PiecewiseParseStates.PROCESSING_DISASSEMBLY;
                elementChange = true;
            }
            else if((piecewiseResidual.charAt(startIndex) == '\n') && (parser != PiecewiseParseStates.PROCESSING_DISASSEMBLY))
            {
                parser = PiecewiseParseStates.PROCESSING_OTHER;
                elementChange = true;
            }

            // See if we are done parsing a block of Section contents
            if(elementChange)
            {
                endPiecewiseSectionDataParsing();
                if(parser == PiecewiseParseStates.PROCESSING_SECTION_CONTENTS)
                {
                    //Contents of sec ???:
                    piecewiseSecName = piecewiseResidual.substring(startIndex + "Contents of section ".length(), newLineIndex - 1); // -1 to remove the colon
                    piecewiseSecData = new ArrayList<>();
                }
                elementChange = false;
                continue;
            }

            switch(parser)
            {
                case PROCESSING_SECTION_HEADERS:
                    // Find a second newline
                    newLineIndex = piecewiseResidual.indexOf('\n', newLineIndex + 1);
                    if(newLineIndex < 0)
                    {
                        piecewiseResidual = piecewiseResidual.substring(startIndex);
                        return;
                    }
                    Section sec = new Section(piecewiseResidual.substring(startIndex, newLineIndex));

                    if(sec.name == null)
                    {
                        parser = PiecewiseParseStates.PROCESSING_OTHER;
                        elementChange = true;
                    }
                    else
                    {
                        if(sec.isROM)
                        {
                            romSectionMapByName.put(sec.name, sec);
                            for(long addr = sec.loadMemoryAddress; addr < sec.loadMemoryAddress + sec.size; addr += 0x2)
                            {
                                romSectionMapByAddr.put(addr, sec);
                            }
                        }
                        sections.add(sec);
                        sectionsByName.put(sec.name, sec);
                    }
                    break;

                case PROCESSING_SYMBOL_TABLE:
                    String sub = piecewiseResidual.substring(startIndex, newLineIndex);
                    Symbol s = new Symbol(sub);
                    if(s.name == null)
                    {
                        parser = PiecewiseParseStates.PROCESSING_OTHER;
                        symbolTableAsString = CatStringList(symbolTableAsStrings);
                        symbolTableAsStrings = null;
                        elementChange = true;
                    }
                    else
                    {
                        symbolTableAsStrings.add(piecewiseResidual.substring(startIndex, newLineIndex + 1));
                        symbols.add(s);
                        symMap.put(s.address, s.name);
                        symbolsByName.put(s.name, s);
                        if(s.flags.function && !s.flags.local)
                        {
                            functionSymsByAddr.put(s.address, s);
                            functionSymsByName.put(s.name, s);
                        }
                    }
                    break;

                case PROCESSING_SECTION_CONTENTS:
                    DataRecord dr = new DataRecord(piecewiseResidual.substring(startIndex, newLineIndex + 1));    // DataRecord() constructor requires the trailing '\n' character
                    if(dr.address < 0)
                    {
                        parser = PiecewiseParseStates.PROCESSING_OTHER;
                        elementChange = true;
                    }
                    else
                    {
                        if(piecewiseSecData.size() > 0) // After the first line, maintain the architecture16Bit flag. This ensures correct flagging for the final line, which could be shorter and have 3 bytes (ex: 32-bit debug fields), or 2 bytes (ex: Flash when it looks like RAM).
                        {
                            dr.architecture16Bit = piecewiseSecData.get(0).architecture16Bit;
                        }
                        piecewiseSecData.add(dr);
                    }
                    break;

                case PROCESSING_DISASSEMBLY:
                    String line = piecewiseResidual.substring(startIndex, newLineIndex);
                    if(line.matches("^[ ]*[0-9a-zA-Z]+[\\:]\\t[^$]*"))
                    {
                        int end = line.indexOf(':');
                        int start;
                        int instrStart = 0;

                        for(start = end - 1; start >= 0; start--)
                        {
                            if(line.charAt(start) == ' ')
                            {
                                start++;
                                break;
                            }
                        }
                        if((start < end) && (end >= 1) && (start >= 0))
                        {
                            instrStart = line.indexOf('\t');
                            if(instrStart > 0)
                            {
                                instrStart = line.indexOf('\t', instrStart + 1);
                            }
                            if(instrStart > 0)
                            {
                                Integer addr = Integer.decode("0x" + line.substring(start, end));
                                String disassembledOp = line.substring(instrStart + 1).replace('', '?').replace('', '?').trim();
                                disassemblyMap.put(addr, disassembledOp);
                            }
                        }
                    }
                    break;
                case PROCESSING_OTHER:  // Ignore other stuff we don't need, like DYNAMIC SYMBOL TABLE:, blank newlines, or generalized messages
                    break;
            }
        }
        piecewiseResidual = piecewiseResidual.substring(startIndex);

    }

    public void parsePiecewise(byte dumpBytes[])
    {
        if(dumpBytes == null)
        {
            parsePiecewise((String)null);
        }
        else
        {
            parsePiecewise(new String(dumpBytes, Charset.availableCharsets().get("ISO-8859-1")));
        }
    }

    public void parsePiecewise(byte dumpBytes[], int offset, int length)
    {
        parsePiecewise(new String(dumpBytes, offset, length, Charset.availableCharsets().get("ISO-8859-1")));
    }

    /**
     * Converts PIC32 virtual kseg0, virtual kseg1, and physical addresses into
     * a known address range according to the following ranges: kseg1 RAM -->
     * kseg0_data_mem (0x8xxxxxxx) flash physical or kseg1 --> kseg0_flash_mem
     * (0x9Dxxxxxx) boot flash physical or kseg0 --> kseg1_flash_mem
     * (0xBFCxxxxx)
     *
     * in kseg1_data_mem, kseg0_flash_mem, or kseg1_flash_mem, as appropriate.
     * Physical addresses and addresses in a non-prefered kseg are converted.
     */
    public static long NormalizePIC32Addr(long address)
    {
        if((address & 0xFFC00000L) == 0xA0000000L)    // Make kseg1_data_mem addresses kseg0_data_mem addresses
            address ^= 0x20000000L;
        if((address & 0x7FC00000L) == 0x1D000000L)    // Make main flash physical addresses kseg0_flash_mem addresses
            address |= 0x80000000L;
        if((address & 0x7FC00000L) == 0x1FC00000L)    // Make boot flash/Config word physical addresses kseg1_flash_mem addresses
            address |= 0xA0000000L;
        return address;
    }

    /**
     * Makes loadMemoryAddress and any Symbol addresses within the section fit
     * in kseg1_data_mem, kseg0_flash_mem, or kseg1_flash_mem, as appropriate.
     * Physical addresses and addresses in a non-prefered kseg are converted.
     */
    public void normalizePIC32Addresses()
    {
        if(this.symbols != null)
        {
            for(Symbol s : this.symbols)
            {
                s.address = NormalizePIC32Addr(s.address);
            }
        }
        if(this.symMap != null)
        {
            for(Long addr : symMap.keySet())
            {
                addr = NormalizePIC32Addr(addr);
            }
        }

        if(this.sections != null)
        {
            for(Section s : this.sections)
            {
                s.loadMemoryAddress = NormalizePIC32Addr(s.loadMemoryAddress);
                if(s.symbols != null)
                {
                    for(Long addr : s.symbolsByAddr.keySet())
                    {
                        addr = NormalizePIC32Addr(addr);
                    }
                }
            }
        }

        if(romSectionMapByAddr != null)
        {
            for(Long addr : this.romSectionMapByAddr.keySet())
            {
                addr = NormalizePIC32Addr(addr);
            }
        }

        if(functionSymsByAddr != null)
        {
            for(Long addr : this.functionSymsByAddr.keySet())
            {
                addr = NormalizePIC32Addr(addr);
            }
        }
    }

    public List<Section> removeSections(String sectionNameRegex)
    {
        List<Section> removed = new ArrayList<>();

        for(int secIndex = 0; secIndex < sections.size(); secIndex++)
        {
            Section sec = sections.get(secIndex);

            // Throw away .text.EZBL_AppReservedHole.* ROM sections. These 
            // are used in Bootloader projects to force certain addresses as 
            // available when building the Application.
            if(sec.name.matches(sectionNameRegex))
            {
                removed.add(sec);
                sections.remove(sec);
                sectionsByName.remove(sec.name);
                romSectionMapByName.remove(sec.name);
                for(long addr = sec.loadMemoryAddress; addr < sec.loadMemoryAddress + sec.size; addr += 0x2)
                {
                    romSectionMapByAddr.remove(addr);
                }
                secIndex--;
                continue;
            }
        }

        return removed;
    }

    public void addSection(Section sec)
    {
        if(!sections.contains(sec))
            sections.add(sec);
        sectionsByName.put(sec.name, sec);
    }

    public void addSections(Collection<Section> sections)
    {
        for(Section sec : sections)
        {
            addSection(sec);
        }
    }

    public void addSymbol(Symbol sym)
    {
        if(!symbols.contains(sym))
            symbols.add(sym);
        symbolsByName.put(sym.name, sym);
        symMap.put(sym.address, sym.name);
    }
    public void addSymbols(Collection<Symbol> symbols)
    {
        for(Symbol sym : symbols)
        {
            addSymbol(sym);
        }
    }

    public String generateHTMLReport()
    {
        String elfFile = "";
        String devName = "";
        int devID = 0;
        int devREV = 0;
        String dumpToolVer = "";
        String dumpToolFile = "";
        String rep;
        String htmlHeader;
        String htmlBody;
        String htmlSectionTable;
        String htmlSymbolTable = "";
        String htmlFooter;
        long lastEndAddr;

        // Generate HTML header and styles
        htmlHeader = "\n"
                     + "\n<html>"
                     + "\n<head>"
                     + "\n  <style>"
                     + "\n  table {"
                     + "\n    border: 2px solid black;"
                     + "\n    border-collapse: collapse;"
                     + "\n    table-layout: fixed;"
                     + "\n    vertical-align: middle;"
                     + "\n    text-align: right;"
                     + "\n  }"
                     + "\n  tr:hover {"
                     + "\n    background-color: #F5F5F5;"
                     + "\n  }"
                     + "\n  td {"
                     + "\n    border: 1px solid #D0D0D0;"
                     + "\n    min-width: 3em;"
                     + "\n    padding: 5px;"
                     + "\n    vertical-align: middle;"
                     + "\n    font-family: \"Courier New\", \"Lucida Console\";"
                     + "\n    text-align: right;"
                     + "\n  }"
                     + "\n  td.text {"
                     + "\n    text-align: left;"
                     + "\n  }"
                     + "\n  td.num {"
                     + "\n    text-align: right;"
                     + "\n  }"
                     + "\n  td.dimNum {"
                     + "\n    color: #E0E0E0;"
                     + "\n    text-align: right;"
                     + "\n  }"
                     + "\n  td.boldNum {"
                     + "\n    font-weight: bold;"
                     + "\n    text-align: right;"
                     + "\n  }"
                     + "\n  th.vert {"
                     + "\n    background: #D0D0D0;"
                     + "\n    border: 1px solid black;"
                     + "\n    min-height: 4em;"
                     + "\n  }"
                     + "\n  th.horiz {"
                     + "\n    min-width: 7em;"
                     + "\n  }"
                     + "\n  </style>"
                     + "\n</head>";

        // Add body tag and some data specifying what this report is for
        htmlBody = "\n"
                   + String.format("\n<body>")
                   + String.format("\n<h1>%1$s report</h1>", elfFile)
                   + String.format("\n<h2>%1$s</h2>", devName)
                   + String.format("\n<li>Dump tool: %1$s</li>", dumpToolFile)
                   + String.format("\n<li>Dump tool version: %1$s</li>", dumpToolVer)
                   + String.format("\n<li>DEVID: 0x%1$06X, DEVREV: 0x%2$06X</li>", devID, devREV)
                   + "\n<br/>";

        // Generate RAM Section list/table
        lastEndAddr = 0x1000;
        htmlSectionTable = "\n"
                           + String.format("\n<p><strong>Number of sections:</strong> %1$d</p>", sections.size())
                           + "\n<p><strong>RAM Sections</strong></p>"
                           + "\n<table>"
                           + "\n<tr>"
                           + "\n  <th class='vert' id='SecLMA'>Load Addr</th>"
                           + "\n  <th class='vert' id='SecByteLen'>Bytes</th>"
                           + "\n  <th class='vert' id='SecName'>Name</th>"
                           + "\n  <th class='vert' id='SecFlags'>Flags</th>"
                           + "\n  <th class='vert' id='SecFlags'>Alignment</th>"
                           + "\n  <th class='vert' id='SecVMA'>Virtual Addr</th>"
                           + "\n  <th class='vert' id='SecID'>ID</th>"
                           + "\n  <th class='vert' id='SecFlags'>Offset</th>"
                           + "</tr>";
        for(Section sec : sections)
        {
            if(!sec.isRAM)
            {
                continue;
            }
            htmlSectionTable += "\n<tr>"
                                + String.format("\n  <td class='%1$s'>%2$04X:%3$04X</td>", ((sec.loadMemoryAddress != lastEndAddr) ? "boldNum" : "num"), sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.size)
                                + String.format("\n  <td class='text'>%1$s</td>", sec.name)
                                + String.format("\n  <td class='text'>%1$s</td>", sec.flags.wholeString)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.alignment)
                                + String.format("\n  <td class='%1$s'>%2$04X:%3$04X</td>", ((sec.loadMemoryAddress == sec.virtualMemoryAddress) ? "dimNum" : "num"), sec.virtualMemoryAddress, sec.virtualMemoryAddress + sec.size)
                                + String.format("\n  <td class='num'>%1$3d</td>", sec.id)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.fileOffset)
                                + "\n</tr>";
            lastEndAddr = sec.loadMemoryAddress + sec.size;
        }
        htmlSectionTable += "\n</table>";

        lastEndAddr = 0x000000;
        htmlSectionTable += "\n"
                            + "\n<p><strong>ROM Sections</strong></p>"
                            + "\n<table>"
                            + "\n<tr>"
                            + "\n  <th class='vert' id='SecLMA'>Load Addr</th>"
                            + "\n  <th class='vert' id='SecByteLen'>Size (bytes)</th>"
                            + "\n  <th class='vert' id='SecName'>Name</th>"
                            + "\n  <th class='vert' id='SecFlags'>Flags</th>"
                            + "\n  <th class='vert' id='SecFlags'>Alignment</th>"
                            + "\n  <th class='vert' id='SecVMA'>Virtual Addr</th>"
                            + "\n  <th class='vert' id='SecID'>ID</th>"
                            + "\n  <th class='vert' id='SecFlags'>Offset</th>"
                            + "</tr>";
        for(Section sec : sections)
        {
            if(!sec.isROM)
            {
                continue;
            }
            htmlSectionTable += "\n<tr>"
                                + String.format("\n  <td class='%1$s'>%2$06X:%3$06X</td>", ((sec.loadMemoryAddress != lastEndAddr) ? "boldNum" : "num"), sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.size)
                                + String.format("\n  <td class='text'>%1$s</td>", sec.name)
                                + String.format("\n  <td class='text'>%1$s</td>", sec.flags.wholeString)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.alignment)
                                + String.format("\n  <td class='%1$s'>%2$06X:%3$06X</td>", ((sec.loadMemoryAddress == sec.virtualMemoryAddress) ? "dimNum" : "num"), sec.virtualMemoryAddress, sec.virtualMemoryAddress + sec.size)
                                + String.format("\n  <td class='num'>%1$3d</td>", sec.id)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.fileOffset)
                                + "\n</tr>";
            lastEndAddr = sec.loadMemoryAddress + sec.size;
        }
        htmlSectionTable += "\n</table>";

        lastEndAddr = 0x00000000;
        htmlSectionTable += "\n"
                            + "\n<p><strong>Debugging and other sections</strong></p>"
                            + "\n<table>"
                            + "\n<tr>"
                            + "\n  <th class='vert' id='SecLMA'>Load Addr</th>"
                            + "\n  <th class='vert' id='SecByteLen'>Size (bytes)</th>"
                            + "\n  <th class='vert' id='SecName'>Name</th>"
                            + "\n  <th class='vert' id='SecFlags'>Flags</th>"
                            + "\n  <th class='vert' id='SecFlags'>Alignment</th>"
                            + "\n  <th class='vert' id='SecVMA'>Virtual Addr</th>"
                            + "\n  <th class='vert' id='SecID'>ID</th>"
                            + "\n  <th class='vert' id='SecFlags'>Offset</th>"
                            + "</tr>";
        for(Section sec : sections)
        {
            if(!sec.isDebug)
            {
                continue;
            }
            htmlSectionTable += "\n<tr>"
                                + String.format("\n  <td class='%1$s'>%2$08X:%3$08X</td>", ((sec.loadMemoryAddress != lastEndAddr) ? "boldNum" : "num"), sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.size)
                                + String.format("\n  <td class='text'>%1$s</td>", sec.name)
                                + String.format("\n  <td class='text'>%1$s</td>", sec.flags.wholeString)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.alignment)
                                + String.format("\n  <td class='%1$s'>%2$08X:%3$08X</td>", ((sec.loadMemoryAddress == sec.virtualMemoryAddress) ? "dimNum" : "num"), sec.virtualMemoryAddress, sec.virtualMemoryAddress + sec.size)
                                + String.format("\n  <td class='num'>%1$3d</td>", sec.id)
                                + String.format("\n  <td class='num'>%1$d</td>", sec.fileOffset)
                                + "\n</tr>";
            lastEndAddr = sec.loadMemoryAddress + sec.size;
        }
        htmlSectionTable += "\n</table>";

//        // Generate Symbol table
//        htmlSymbolTable = "\n"
//                          + String.format("\n<p><strong>Number of symbols:</strong> %1$d</p>", symbols.size())
//                          + "\n<table>"
//                          + "\n<tr>"
//                          + "\n  <th class='vert' id='SymAddr'>Value (Address)</th>"
//                          + "\n  <th class='vert' id='SymSec'>Section</th>"
//                          + "\n  <th class='vert' id='SymName'>Name</th>"
//                          + "\n  <th class='vert' id='SymFlags'>Flags</th>"
//                          + "</tr>";
//        for(Symbol s : symbols)
//        {
//            htmlSymbolTable += "\n<tr>"
//                               + String.format("\n  <td class='num' id='SymAddr_%1$s'>%2$04X</td>", s.name, s.address)
//                               + String.format("\n  <td class='num' id='SymAddr_%1$s'>(%2$d)</td>", s.name, s.address)
//                               + String.format("\n  <th class='text' id='SymSec_%1$s'>%1$s</th>", s.sec)
//                               + String.format("\n  <td class='text' id='SymName_%1$s'>%2$s</td>", s.name, s.name)
//                               + String.format("\n  <td class='text' id='SymFlags_%1$s'>%2$s</td>", s.name, "TODO")
//                               + "\n</tr>";
//        }
//        htmlSymbolTable += "\n</table>";
        // Generate footer
        htmlFooter = "\n</body>"
                     + "\n</html>";

        rep = htmlHeader + htmlBody + htmlSectionTable + htmlSymbolTable + htmlFooter;

        return rep;
    }

    public List<Symbol> getSymbols()
    {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols)
    {
        this.symbols = symbols;
    }

    public List<Section> getSections()
    {
        return sections;
    }

    public void setSections(List<Section> sections)
    {
        this.sections = sections;
    }
}
