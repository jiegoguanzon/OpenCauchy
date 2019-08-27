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

import com.microchip.apps.ezbl.MemoryRegion.MemType;
import com.microchip.apps.ezbl.MemoryRegion.Partition;
import static com.microchip.apps.ezbl.Multifunction.CatStringList;
import java.io.*;
import java.security.*;
import java.util.*;


/**
 *
 * @author C12128
 */
public class Section implements Serializable, Cloneable, Comparable<Section>
{
    static final long serialVersionUID = 1L;
    public int id = 0;
    public String name = "";
    public String combinedName = "";
    public String sourceFile = "";
    public long size = -1;                  // In linker units. This is bytes for RAM or PC units for anything that is stored in program space (including PSV constants).
    public long virtualMemoryAddress = -1;
    public long loadMemoryAddress = -1;
    public long fileOffset = 0;
    public int alignment = 2;
    public SectionFlags flags = new SectionFlags();
    public List<Symbol> symbols = null;
    public Map<Long, Symbol> symbolsByAddr = null;
    public Map<String, Symbol> symbolsByName = null;
    public DataRecord data = null;
    public boolean isROM = false;
    public boolean isRAM = false;
    public boolean isDebug = false;
    public MemoryRegion mappedMemoryRegion = null;

    public Section Clone()
    {
        Section ret = new Section();

        ret.alignment = this.alignment;
        ret.combinedName = this.combinedName;
        if(this.data != null)
        {
            ret.data = this.data.Clone();
        }
        ret.fileOffset = this.fileOffset;
        ret.flags = this.flags.Clone();
        ret.id = this.id;
        ret.isDebug = this.isDebug;
        ret.isRAM = this.isRAM;
        ret.isROM = this.isROM;
        ret.loadMemoryAddress = this.loadMemoryAddress;
        ret.name = this.name;
        ret.size = this.size;
        ret.sourceFile = this.sourceFile;
        if(this.symbols != null)
        {
            ret.symbols = new ArrayList<>(this.symbols.size());
            ret.symbolsByAddr = new HashMap<>(this.symbols.size());
            ret.symbolsByName = new HashMap<>(this.symbols.size());
            for(int i = 0; i < this.symbols.size(); i++)
            {
                Symbol s = this.symbols.get(i).Clone();
                ret.symbolsByAddr.put(s.address, s);
                ret.symbolsByName.put(s.name, s);
                ret.symbols.add(s);
            }
        }

        ret.virtualMemoryAddress = this.virtualMemoryAddress;
        ret.mappedMemoryRegion = (this.mappedMemoryRegion == null) ? null : this.mappedMemoryRegion.clone();

        return ret;
    }

    public Section()
    {
    }

    public Section(String obj_dump_line)
    {
        //Sections:
        //Idx Name          Size      VMA       LMA       File off  Algn
        //  0 EZBL       00003600  00000200  00000200  0000035c  2**1
        //                  CONTENTS, ALLOC, LOAD, CODE
        try
        {
            String[] dump = obj_dump_line.split("[\\s]+?");
            List<String> allFlags = new ArrayList<>();
            int i = 0;
            for(String s : dump)
            {
                if(s.length() == 0)
                    continue;

                switch(i++)
                {
                    case 0: // Idx
                        id = Integer.decode(s);
                        break;
                    case 1: // Section name
                        name = s;
                        break;
                    case 2: // Size
                        size = Long.parseLong(s, 16);
                        break;
                    case 3: // VMA
                        virtualMemoryAddress = Long.parseLong(s, 16);
                        break;
                    case 4: // LMA
                        loadMemoryAddress = Long.parseLong(s, 16);
                        break;
                    case 5: // File offset
                        fileOffset = Long.parseLong(s, 16);
                        break;
                    case 6: // Alignment
                        alignment = 2 ^ Integer.parseInt(s.substring(s.lastIndexOf('*') + 1));
                        break;
                    default: // 7+ Flags
                        s = s.toUpperCase();
                        allFlags.add(s + " ");
                        if(s.endsWith(","))
                        {
                            s = s.substring(0, s.length() - 1);
                        }
                        switch(s)
                        {
                            case "CONTENTS":
                                flags.CONTENTS = true;
                                break;
                            case "ALLOC":
                                flags.ALLOC = true;
                                break;
                            case "LOAD":
                                flags.LOAD = true;
                                break;
                            case "CODE":
                                flags.CODE = true;
                                break;
                            case "ABSOLUTE":
                                flags.ABSOLUTE = true;
                                break;
                            case "DEBUGGING":
                                flags.DEBUGGING = true;
                                break;
                            case "DATA":
                                flags.DATA = true;
                                break;
                            case "NEVER_LOAD":
                                flags.NEVER_LOAD = true;
                                break;
                            case "PERSIST":
                                flags.PERSIST = true;
                                break;
                            case "PSV":     // XC16 only
                                flags.PSV = true;
                                break;
                            case "PAGE":    // XC16 only
                                flags.PAGE = true;
                                break;
                            case "READONLY":
                                flags.READONLY = true;
                                break;
                            case "EDS":     // XC16 only
                                flags.EDS = true;
                                break;
                            case "RELOC":
                                flags.RELOC = true;
                                break;
                            case "NEAR":    // ??? only?
                                flags.NEAR = true;
                                break;
                            case "REVERSE": // ??? only?
                                flags.REVERSE = true;
                                break;
                            case "SECURE":  // XC16 only?
                                flags.SECURE = true;
                                break;
                            case "XMEMORY": // XC16 only
                                flags.XMEMORY = true;
                                break;
                            case "YMEMORY": // XC16 only
                                flags.YMEMORY = true;
                                break;
                            case "MEMORY":  // XC16 only?   // External memory
                                flags.MEMORY = true;
                                break;
                            case "PACKEDFLASH":     // __pack_upper_byte
                                flags.PACKEDFLASH = true;
                                break;
                            case "PRESERVED":   // XC16 only?
                                flags.PRESERVED = true;
                                break;
                            case "UPDATE":      // XC16 only?
                                flags.UPDATE = true;
                                break;
                            case "LINK_ONCE_SAME_SIZE":      // XC32 only
                                flags.LINK_ONCE_SAME_SIZE = true;
                                break;
                            default:
                                flags.unknown += "," + s;
                                break;
                        }
                        break;
                }
            }
            if(!flags.unknown.isEmpty())   // Prune off initial comma if there are any unknown flags.
            {
                flags.unknown = flags.unknown.substring(1);
            }
            flags.wholeString = CatStringList(allFlags);
            if(flags.wholeString.endsWith(" "))
            {
                flags.wholeString = flags.wholeString.substring(0, flags.wholeString.length() - 1);
            }

            isDebug = this.flags.DEBUGGING
                      || ((this.loadMemoryAddress == 0) && (this.virtualMemoryAddress == 0) && (this.size == 0)) // .EZBL_ISRPointers dummy section on PIC32MM
                      || this.name.equals(".mdebug.abi32")
                      || this.name.equals(".gnu.attributes")
                      || this.name.equals(".reginfo")
                      || this.name.equals(".comment")
                      || this.name.equals(".info.EZBL_KeepSYM");
            isROM = !this.flags.DEBUGGING
                    && ((this.flags.CODE && this.flags.READONLY)
                        || this.flags.PSV
                        || this.flags.PAGE
                        || this.flags.PACKEDFLASH
                        || ((this.loadMemoryAddress & 0xFFC00000L) == 0x1D000000L)
                        || ((this.loadMemoryAddress & 0xFFC00000L) == 0x1FC00000L)
                        || ((this.loadMemoryAddress & 0xFFC00000L) == 0x9D000000L)
                        || ((this.loadMemoryAddress & 0xFFC00000L) == 0x9FC00000L));
            isRAM = !isROM
                    && (this.flags.DATA
                        || this.flags.NEAR
                        || this.flags.EDS
                        || this.flags.MEMORY
                        || this.flags.PERSIST
                        || this.flags.XMEMORY
                        || this.flags.YMEMORY
                        || this.flags.REVERSE
                        || ((this.loadMemoryAddress & 0xFFC00000L) == 0x80000000L)
                        || ((this.loadMemoryAddress & 0xFFC00000L) == 0xA0000000L));
            if(!isDebug && !isROM && !isRAM && (this.flags.CODE || this.name.startsWith(".text") || this.name.startsWith(".ivt") || this.name.startsWith(".const") || this.name.startsWith("reserve_boot_")))
                isROM = true;
            if(!isDebug && !isROM && !isRAM && (this.name.startsWith(".bss") || this.name.startsWith(".pbss") || this.name.startsWith(".nbss") || this.name.startsWith(".data") || this.name.startsWith("reserve_data_") || (this.flags.ALLOC && !this.flags.CONTENTS)))
                isRAM = true;
            if(!isDebug && !isROM && !isRAM)    // If we don't know how to handle this, we have no choice but to treat it as a debug section and not use it
                isDebug = true;

            // Fix XC32 v1.44 tagging stuff in flash/boot flash as data when compiled for debug
            if(this.flags.READONLY && (((this.loadMemoryAddress & 0x1FC00000L) == 0x1FC00000L) || ((this.loadMemoryAddress & 0x1FC00000L) == 0x1D000000L)))
            {
                isRAM = false;
                isROM = true;
                this.flags.DATA = false;
                this.flags.CODE = true;
            }
        }
        catch(NumberFormatException ex)
        {
            name = null;    // Tag that an error occured durring parsing
        }
    }

    /**
     * Compares the 'loadMemoryAddress' element as a signed numerical value.
     *
     * @param y Section to compare against
     *
     * @return 0 if both .loadMemoryAddress elements are equal, -1 if
     *         y.loadMemoryAddress is less than this' loadMemoryAddress, or +1
     *         otherwise.
     */
    @Override
    public int compareTo(Section y) // Needed for calling Collections.sort()
    {
        return this.loadMemoryAddress < y.loadMemoryAddress ? -1 : this.loadMemoryAddress == y.loadMemoryAddress ? 0 : 1;
    }

    /**
     * Create a new MemoryRegion to represent the section addresses and attempts
     * to locate the proper device memory region from the list that this section
     * lives in. If found, additional metadata from the reference deviceRegion
     * is copied to retain information like the erase and programming alignment.
     *
     * @param deviceRegions Reference region list for the various memory types
     *                      on the target device.
     *
     * @return A new MemoryRegion, containing start and end addresses to match
     *         the section load memory address range and updated with metadata
     *         from the matching deviceRegion, if found. If no match is found,
     *         the MemoryRegion will have less meta data set in it derived soley
     *         from the section attributes and properties.
     */
    public MemoryRegion mapToDeviceRegion(List<MemoryRegion> deviceRegions, Partition sectionPartition)
    {
        MemoryRegion ret = new MemoryRegion(this.loadMemoryAddress, this.loadMemoryAddress + this.size);
        ret.partition = sectionPartition;
        if(this.data != null)
        {
            ret.name = this.data.assignedMemory;
            ret.endAddr = this.data.getEndAddress();
        }
        if(this.isDebug || this.isRAM || (sectionPartition == null))
            sectionPartition = Partition.single;

        if((ret.startAddr & 0xFFC00000L) == 0xA0000000L)    // Make kseg1_data_mem addresses kseg0_data_mem addresses
        {
            ret.startAddr ^= 0x20000000L;
            ret.endAddr ^= 0x20000000L;
        }
        if((ret.startAddr & 0xFFC00000L) == 0x1D000000L)    // Make main flash physical addresses kseg0_flash_mem addresses
        {
            ret.startAddr |= 0x80000000L;
            ret.endAddr |= 0x80000000L;
        }
        if(((ret.startAddr & 0x7FC00000L) == 0x1FC00000L) // Make boot flash/Config word physical or kseg0 addresses kseg1 uncached addresses
           || ((ret.startAddr & 0x7FC00000L) == 0x1F800000L)) // Make SFR physical or kseg0 addresses kseg1 uncached addresses
        {
            ret.startAddr |= 0xA0000000L;
            ret.endAddr |= 0xA0000000L;
        }

        Partition p = sectionPartition;
        while(true)
        {
            for(MemoryRegion mr : deviceRegions)
            {
                if((ret.startAddr >= mr.endAddr) || (ret.endAddr <= mr.startAddr) || (mr.partition != p))   // Skip if no overlap at all
                    continue;

                if((this.isDebug && mr.isDebugSpace()) || (this.isRAM && mr.isDataSpace()) || (this.isROM && mr.isProgSpace()))
                {
                    ret.copyMetaData(mr);
                    if((this.data != null) && (this.data.getEndAddress() > mr.endAddr)) // Flag truncated section end address if it spills into a different device memory region
                        ret.endAddr = mr.endAddr;
                    if(ret.startAddr < mr.startAddr)    // Keep looking if we a start outside a known range but end within a range, now truncated to the end of the range
                        continue;
                    return ret;
                }
            }

            // Didn't find a good match for start address and type, so look again and match against the closest deviceRegion that preceeds the section
            for(MemoryRegion mr : deviceRegions)
            {
                if(mr.partition != p)
                    continue;

                if((this.isDebug && mr.isDebugSpace()) || (this.isRAM && mr.isDataSpace()) || (this.isROM && mr.isProgSpace()))
                {
                    if((ret.endAddr > mr.startAddr) && (ret.startAddr < mr.endAddr))
                    {   // Device region found fully contained within the section data, so flag section for end address truncation
                        ret.endAddr = mr.startAddr;
                    }
                    if(ret.startAddr == mr.endAddr)
                    {
                        ret.copyMetaData(mr);
                        ret.comment = "no-device-mem-defined";
                    }
                }
            }

            if(this.isDebug)
                ret.type = MemType.DEBUG;
            if(this.isRAM && !ret.isDataSpace())
            {
                ret.type = MemType.RAM;
                ret.eraseAlign = 1;
                ret.programAlign = 1;
            }
            if(this.isROM && !ret.isProgSpace())
                ret.type = MemType.ROM;

            if(p == Partition.single)
                break;
            p = Partition.single;   // Try searching again using partition 1 as the critera since some things are global and not in the partition 1/partition 2 view
        }
        return ret;
    }

    /**
     * Makes loadMemoryAddress and any Symbol addresses within the section fit
     * in kseg1_data_mem, kseg0_flash_mem, or kseg1_flash_mem, as appropriate.
     * Physical addresses and addresses in a non-prefered kseg are converted.
     */
    public void normalizePIC32Addresses()
    {
        if((this.loadMemoryAddress & 0xFFC00000L) == 0xA0000000L)    // Make kseg1_data_mem addresses kseg0_data_mem addresses
        {
            this.loadMemoryAddress ^= 0x20000000L;
        }
        if((this.loadMemoryAddress & 0x7FC00000L) == 0x1D000000L)    // Make main flash physical addresses kseg0_flash_mem addresses
        {
            this.loadMemoryAddress |= 0x80000000L;
        }
        if((this.loadMemoryAddress & 0x7FC00000L) == 0x1FC00000L)    // Make boot flash/Config word physical addresses kseg1_flash_mem addresses
        {
            this.loadMemoryAddress |= 0xA0000000L;
        }
        if(this.symbols != null)
        {
            for(Symbol s : this.symbols)
            {
                if((s.address & 0xFFC00000L) == 0xA0000000L)    // Make kseg1_data_mem addresses kseg0_data_mem addresses
                {
                    s.address ^= 0x20000000L;
                }
                if((s.address & 0x7FC00000L) == 0x1D000000L)    // Make main flash physical addresses kseg0_flash_mem addresses
                {
                    s.address |= 0x80000000L;
                }
                if((s.address & 0x7FC00000L) == 0x1FC00000L)    // Make boot flash/Config word physical addresses kseg1_flash_mem addresses
                {
                    s.address |= 0xA0000000L;
                }
            }
            for(Symbol s : this.symbolsByAddr.values())
            {
                this.symbolsByAddr.put(s.address, s);
            }
        }
    }

    public boolean nameMatchesRegEx(List<String> regExpressions)
    {
        if(regExpressions == null)
            return false;

        for(String regEx : regExpressions)
        {
            if(this.name.matches(regEx))
                return true;
        }

        return false;
    }

    /**
     * Returns all used addresses by the given list of sections. The section's
     * loadMemoryAddress and size parameters are used to determine the addresses
     * (not section data).
     *
     * @param sections List of sections to determine the addresses from.
     *
     * @return List of AddressRange's corresponding to the given sections.
     */
    static public List<AddressRange> convertToAddressRanges(List<Section> sections)
    {
        List<AddressRange> ret = new ArrayList<>();
        for(Section sec : sections)
        {
            ret.add(new AddressRange(sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size));
        }
        return ret;
    }

    /**
     * Returns all DataRecords contained in the given list of sections.
     *
     * If a section contains a null data field, a new DataRecord is created for
     * the return list based on the section's loadMemoryAddress and size fields
     * and the data is set to all '1's (0xFF bytes). Additionally, to try and
     * maintain consistent downstream handling, any created DataRecord's
     * architecture16Bit field is set to the logical OR of all non-null
     * DataRecords' architecture16Bit value. This adjusts the DataRecord's size
     * for sections that have isROM==true AND architecture16Bit==true.
     *
     * @param sections List of sections to determine the data from.
     *
     * @return List of DataRecord's corresponding to the given sections.
     */
    static public List<DataRecord> convertToDataRecords(List<Section> sections)
    {
        List<DataRecord> ret = new ArrayList<>();
        boolean architectureIs16Bit = false;

        // First pass, decide if this is a 16-bit device or not by searching for
        // any true sec.data.architecture16Bit field. This will be needed if we
        // need to create any DataRecords.
        for(Section sec : sections)
        {
            if(sec.data != null)
            {
                if(sec.data.architecture16Bit)
                {
                    architectureIs16Bit = true;
                    break;
                }
            }
        }

        // Second pass, actually copy DataRecord references or create DataRecords
        for(Section sec : sections)
        {
            DataRecord dr;

            dr = sec.data;
            if(dr == null)  // When null, need to create a DataRecord with all '1's set
            {
                dr = new DataRecord();
                dr.address = sec.loadMemoryAddress;
                dr.architecture16Bit = architectureIs16Bit;
                dr.data = new byte[(int)((sec.isROM && dr.architecture16Bit) ? sec.size / 2 * 3 : sec.size)];
                Arrays.fill(dr.data, (byte)0xFF);
            }

            ret.add(dr);
        }

        return ret;
    }

    /**
     * @return the loadMemoryAddress
     */
    public long getLoadMemoryAddress()
    {
        return loadMemoryAddress;
    }

    /**
     * @param loadMemoryAddress the loadMemoryAddress to set
     */
    public void setLoadMemoryAddress(long loadMemoryAddress)
    {
        this.loadMemoryAddress = loadMemoryAddress;
    }

    /**
     * @return the data
     */
    public DataRecord getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(DataRecord data)
    {
        this.data = data;
    }

    /**
     * Splits the section into separate Section classes based on the section's
     * loadMemoryAddress and size.
     *
     * Symbols within the section are retained unchanged in both the original
     * and split off portion. The DataRecords associated with the section are,
     * however, adjusted.
     *
     * @return The section corresponding to the right-side of the split. The
     *         left side is updated in place. If the splitAddress does not fall
     *         in the section, null is returned.
     */
    public Section Split(long splitAddress)
    {
        // Handle right
        if(this.loadMemoryAddress + this.size > splitAddress)
        {
            Section newRight = this.Clone();

            if(this.data != null)
            {
                newRight.data = this.data.SplitAtAddress(splitAddress);
            }
            newRight.size = (this.loadMemoryAddress + this.size) - splitAddress;
            this.size -= newRight.size;
            newRight.loadMemoryAddress += this.size;
            newRight.virtualMemoryAddress += this.size;
            return newRight;
        }

        return null;
    }

    void LoadSymbols(List<Symbol> symbols)
    {
        this.symbols = new ArrayList<>();
        this.symbolsByAddr = new TreeMap<>();
        this.symbolsByName = new TreeMap<>();
        for(Symbol sym : symbols)
        {
            if(((data != null) && (!this.data.architecture16Bit)) || ((sym.address & 0xFF000000L) != 0))
                sym.normalizePIC32Addresses();

            if(sym.section.equals(this.name))
            {
                if(sym.name.matches("[_A-Za-z0-9]*$"))
                {
                    this.symbolsByAddr.put(sym.address, sym);
                    this.symbolsByName.put(sym.name, sym);
                    this.symbols.add(sym);
                }
            }
            if(sym.flags.debugging)
            {
                if(this.sourceFile == null)
                {
                    if(sym.address == this.virtualMemoryAddress)
                    {
                        this.sourceFile = sym.probableFile;
                    }
                }
            }
        }
    }

    void addSymbol(Symbol sym)
    {
        if(!this.symbols.contains(sym))
            this.symbols.add(sym);
        this.symbolsByAddr.put(sym.address, sym);
        this.symbolsByName.put(sym.name, sym);
    }

    void addAllSymbols(Collection<Symbol> symbols)
    {
        for(Symbol s : symbols)
        {
            addSymbol(s);
        }
    }

    /**
     * Given a elf object section data dump created with the command
     * "xc16-objdump -s buildArtifact.elf > elfSectionDump.txt", extracts the
     * data values stored at the same address range occupied by this section.
     *
     * @param elfSectionDump Text file contents of the dump created by the
     *                       xc16-objdump compiler tool.
     *
     * @return true if the text file contained data applicable to this section
     *         and was loaded. false if no data records were created.
     */
    boolean LoadSectionContents(String elfSectionDump)
    {
        int possibleSectionStartIndex;
        int endOffset;
        String sectionContents;
        int wordByteCount;
        List<DataRecord> lines = new ArrayList<DataRecord>();

        //Contents of section .data.LCD_str1:
        // 1390 4275636b 20302e30 30562030 2e303041  Buck 0.00V 0.00A
        //Contents of section .data.Vin_str1:
        // 13a0 565f496e 70757420 3d202020 20202056  V_Input =      V
        //Contents of section .reset:
        // 0000 240204 000000                $.....
        //Contents of section .text:
        // 0224 6f3f21 2efe22 0e0188 000000  o?!.þ"......
        // 022c 000120 200288 0c0007 004122  ..  ......A"
        //Contents of section .config_GSSK:
        // f80004 cfffff                       
        //Contents of section .const:
        // 8000 00000000 00000000 00000000 00000000  ................
        // 8010 00000000 00000000 00000000 00000000  ................
        // Find the named section block start index
        //Pattern p = Pattern.compile("(?m)^Contents of section ([^\n]*)\n");
        //Matcher m = p.matcher(elfSectionDump);
        //while(m.find())    // Loop over all sections that have this same name (they don't have to be uniquely named)
        String searchString = "Contents of section " + this.name + ":\n";
        possibleSectionStartIndex = 0;
        while(true)
        {
            possibleSectionStartIndex = elfSectionDump.indexOf(searchString, possibleSectionStartIndex);
            if(possibleSectionStartIndex < 0)
            {
                break;
            }
            possibleSectionStartIndex += searchString.length();

            // Find the section block end index
            endOffset = elfSectionDump.indexOf("\nC", possibleSectionStartIndex);
            if(endOffset < 0)
            {
                endOffset = elfSectionDump.length();
            }

            // Look at only this section data
            sectionContents = elfSectionDump.substring(possibleSectionStartIndex, endOffset);

            // Decode section addresses and if in range, save the data as data records
            int startIndex = 1;
            int eolIndex = 0;
            int asciiPrintIndex;
            int dataSeperatorIndex;
            while(eolIndex >= 0)
            {
                dataSeperatorIndex = sectionContents.indexOf(' ', startIndex + 1);  // +1 is for unneeded starting space on each line
                if(dataSeperatorIndex < 0)
                {
                    break;
                }
                asciiPrintIndex = sectionContents.indexOf("  ", dataSeperatorIndex + 1);  // +1 is for size of the data seperator space
                if(asciiPrintIndex < 0)
                {
                    break;
                }
                wordByteCount = ((sectionContents.indexOf(' ', dataSeperatorIndex + 1) - dataSeperatorIndex) / 2);
                eolIndex = sectionContents.indexOf('\n', asciiPrintIndex + 2);
                long address = Integer.decode("0x" + sectionContents.substring(startIndex, dataSeperatorIndex));
                startIndex = eolIndex + 2;  // +2 is for unneeded \n and space characters

                // Skip parsing this line/section if the content addresses are outside the section (must be an identically named section)
                if((this.loadMemoryAddress > address) || ((this.loadMemoryAddress + this.size) < address))
                {
                    break;
                }

                // Decode the data on this line and add it to the section as a data record
                String encodedData = sectionContents.substring(dataSeperatorIndex + 1, asciiPrintIndex);
                encodedData = encodedData.replaceAll(" ", "");
                int dataCount = encodedData.length() / 2;
                byte data[] = new byte[dataCount];
                for(int i = 0; i < dataCount; i++)
                {
                    data[i] = (byte)((Integer.decode("0x" + encodedData.substring(i * 2, i * 2 + 2))) & 0xFF);
                }

                DataRecord dr = new DataRecord(address, data, wordByteCount == 3);
                lines.add(dr);
            }
        } // Coalesce all data since we created a record for each line of dump file
        DataRecord.CoalesceRecords(lines);
        if(lines.isEmpty())
        {
            return false;
        }
        this.data = lines.get(0);
        return lines.size() == 1;
    }

    /**
     * Returns a 32 byte SHA-256 hash of this Section class. This hash
     * represents everything that is unique to the Section, including section
     * addresses, name, contents, flags, and alignment. Unimportant or derived
     * information, such as the id, fileOffset, sourceFile, and combinedName
     * fields are ignored/not included in the hash.
     */
    byte[] GetHash()
    {
        Section cleanSection = this.Clone();
        cleanSection.id = 0;
        cleanSection.fileOffset = 0;
        cleanSection.sourceFile = null;
        cleanSection.combinedName = null;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutput objectOut = null;
        try
        {
            objectOut = new ObjectOutputStream(byteStream);
            objectOut.writeObject(this);
            byte[] thisClassBytes = byteStream.toByteArray();
            objectOut.close();

            // Compute SHA-256 hash
            try
            {
                MessageDigest hashComputer = MessageDigest.getInstance("SHA-256");
                return hashComputer.digest(thisClassBytes);
            }
            catch(NoSuchAlgorithmException ex)
            {
                System.err.println("    EZBL: ERROR! Can't find 'SHA-256' hash algorithm. Make sure your JRE includes SHA-256 support.");
                return null;
            }
        }
        catch(IOException ex)
        {
            return null;
        }
    }

    static public List<MemoryRegion> getMappedMemoryRegions(List<Section> sectionList)
    {
        List<MemoryRegion> ret = new ArrayList<>();
        for(Section s : sectionList)
        {
            ret.add(s.mappedMemoryRegion);
        }
        return ret;
    }

    public AddressRange GetLoadAddressRange()
    {
        return new AddressRange(this.loadMemoryAddress, this.loadMemoryAddress + this.getSize());
    }

    public AddressRange GetVirtualAddressRange()
    {
        return new AddressRange(this.virtualMemoryAddress, this.virtualMemoryAddress + this.getSize());
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCombinedName()
    {
        return combinedName;
    }

    public void setCombinedName(String combinedName)
    {
        this.combinedName = combinedName;
    }

    public String getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getVirtualMemoryAddress()
    {
        return virtualMemoryAddress;
    }

    public void setVirtualMemoryAddress(long virtualMemoryAddress)
    {
        this.virtualMemoryAddress = virtualMemoryAddress;
    }

    public long getloadMemoryAddress()
    {
        return loadMemoryAddress;
    }

    public void setloadMemoryAddress(long loadMemoryAddress)
    {
        this.loadMemoryAddress = loadMemoryAddress;
    }

    public long getFileOffset()
    {
        return fileOffset;
    }

    public void setFileOffset(long fileOffset)
    {
        this.fileOffset = fileOffset;
    }

    public int getAlignment()
    {
        return alignment;
    }

    public void setAlignment(int alignment)
    {
        this.alignment = alignment;
    }

    public SectionFlags getFlags()
    {
        return flags;
    }

    public void setFlags(SectionFlags flags)
    {
        this.flags = flags;
    }

    public List<Symbol> getSymbols()
    {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols)
    {
        this.symbols = symbols;
    }

    public boolean isIsROM()
    {
        return isROM;
    }

    public void setIsROM(boolean isROM)
    {
        this.isROM = isROM;
    }

    public boolean isIsRAM()
    {
        return isRAM;
    }

    public void setIsRAM(boolean isRAM)
    {
        this.isRAM = isRAM;
    }

    public boolean isIsDebug()
    {
        return isDebug;
    }

    public void setIsDebug(boolean isDebug)
    {
        this.isDebug = isDebug;
    }
}
