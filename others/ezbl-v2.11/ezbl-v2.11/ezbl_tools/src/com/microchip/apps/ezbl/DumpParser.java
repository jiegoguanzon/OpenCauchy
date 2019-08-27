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

import com.microchip.apps.ezbl.EZBLState.CPUClass;
import com.microchip.apps.ezbl.MemoryRegion.MemType;
import com.microchip.apps.ezbl.MemoryRegion.Partition;
import static com.microchip.apps.ezbl.Multifunction.CatStringList;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;


/**
 *
 * @author C12128
 */
public class DumpParser
{
    // Zero or one time load variables - does not require regenartion when executing a 3rd linking pass and parsing
    static private EZBLState state = null;
    static private String stateSavePath = null;
    static private ProcessBuilder relinkProcBuilder = null;
    static private Process relinkProc = null;
    static private String linkCommandLine = null;   // Normal production use
    //static private String linkCommandLine = "\"C:\\Program Files (x86)\\Microchip\\xc16\\vDefault\\bin\\xc16-gcc.exe\"   -o dist/PIC24FJ1024GB610_PIM/production/exp16_pic24fj1024gb610_pim.x.production.elf  build/PIC24FJ1024GB610_PIM/production/_ext/1706142600/fileio.o build/PIC24FJ1024GB610_PIM/production/_ext/838585624/usb_host.o build/PIC24FJ1024GB610_PIM/production/_ext/838585624/usb_host_msd.o build/PIC24FJ1024GB610_PIM/production/_ext/838585624/usb_host_msd_scsi.o build/PIC24FJ1024GB610_PIM/production/system.o build/PIC24FJ1024GB610_PIM/production/_ext/300881143/main.o build/PIC24FJ1024GB610_PIM/production/_ext/300881143/usb_config.o build/PIC24FJ1024GB610_PIM/production/EZBL_InstallFILEIO2Flash.o    ..\\ezbl_integration\\ezbl_lib.a  -mcpu=24FJ1024GB610        -omf=elf -DEZBL_BOOT_PROJECT=usb_msd -save-temps=obj -DXPRJ_PIC24FJ1024GB610_PIM=PIC24FJ1024GB610_PIM  -legacy-libc    -Wl,--defsym=EZBL_BOOT_PROJECT=1,-DEZBL_BOOT_PROJECT=EZBL_BOOT_PROJECT,-D__24FJ1024GB610__=1,--local-stack,,--defsym=__MPLAB_BUILD=1,,--script=\"..\\ezbl_integration\\ezbl_build_standalone.gld\",--heap=2000,--stack=16,--check-sections,--data-init,--pack-data,--handles,--no-isr,--gc-sections,--fill-upper=0,--stackguard=16,--no-force-link,--smart-io,-Map=\"dist/PIC24FJ1024GB610_PIM/production/exp16_pic24fj1024gb610_pim.x.production.map\",--report-mem,--memorysummary,dist/PIC24FJ1024GB610_PIM/production/memoryfile.xml,--defsym=_BOOTID_HASH0=0x57088A08,--defsym=_BOOTID_HASH1=0x4E45A423,--defsym=_BOOTID_HASH2=0xC86D9E8E,--defsym=_BOOTID_HASH3=0x0CB0B5B6";   // Debugging only - no easy way to pipe data to stdin when executing in NetBeans debugger    
    static private String debuggableArtifactPath = null;
    static private String objDumpExecutable = null;
    static private int hexCharWidth = 8;    // 8 hex digits for 32-bit, 6 digits for 16-bit program space addresses
    static private String linkerFileData = null;

    static private final String firstPassResultsSectionRegexStart = "AUTOMATIC FIRST PASS RESULTS SECTION[^\n]*?\n";
    static private final String firstPassResultsSectionRegexEnd = "(?<=\n)[ \t]*?/[*][^E\n]*END OF FIRST PASS RESULTS SECTION";
    static private final String forwardBootloaderFlagsVariableName = "EZBL_ForwardBootloaderISR";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        List<DataRecord> eraseRestoreTable = new ArrayList<>();
        List<MemoryRegion> appSpaceGeometry;
        List<MemoryRegion> noAppSpaceRanges;
        MemoryRegion configWordPageFreespace;
        boolean bootContentOnConfigPage = false;
        String ivtDispatcher = "";
        String ivtMasks = "";
        String ivtVectorPointers = "";
        String ivtVectorSave = "";
        String bootReservedRAMMacro = "";
        int sizeOfAppGotoReset = 0x4; // Number of addresses used by the EZBL_APP_RESET_BASE
        List<String> gldOutput = new ArrayList<>();
        String outputValuesString;
        String elfDumpText;
        int genROMDataAddrSize = 0;
        int genISRDispatchStubs = 0;

        // Obtain the given command line options and all prior ezbl_tools invocation state information
        if(state == null)
        {
            stateSavePath = CommandAndBuildState.GetStateFilePath(args);
            state = EZBLState.ReadFromFile(stateSavePath);                      // Load up the preexisting state file from disk, if one exists
            state = CommandAndBuildState.ReadArgs(state, args);                 // Parse any new command line options and save them

            // If nothing is specified upon execution, write usage information to STDOUT
            if(!state.parseOkay)
            {
                System.out.print("\r\n"
                                 + Multifunction.FormatHelpText(79, 0 * 3, "Usage:")
                                 + Multifunction.FormatHelpText(79, 1 * 3, "java -jar ezbl_tools.jar --dump_parser -pass=1 -elf_artifact=input.elf -linkscript=,--script=\"boot_proj_linker_file.[gld/ld]\"")
                                 + "\r\n");

                return;
            }

            if(state.MCU16Mode)
                hexCharWidth = 6;

            if((state.artifactPath == null) || (state.linkScriptPath == null))
            {
                if(!state.silent)
                    System.err.println("ezbl_tools: Missing required input parameter.");
                System.exit(-1);
            }

            debuggableArtifactPath = state.elfPath != null ? state.elfPath : state.artifactPath;
            if(!debuggableArtifactPath.toLowerCase().endsWith(".elf"))
            {
                debuggableArtifactPath = debuggableArtifactPath.replaceFirst("\\.[hH][eE][xX]$", ".elf");
            }
            if(!new File(debuggableArtifactPath).exists())
            {
                System.err.printf("ezbl_tools: \"%s\" could not be found\n", debuggableArtifactPath);
                System.exit(-5);
            }

            objDumpExecutable = "xc16-objdump";
            if(ELFReader.Machine(debuggableArtifactPath) == ELFReader.e_machine.EM_MIPS)
                objDumpExecutable = "xc32-objdump";

            // Read the Bootloader project's .gld/.ld linker script that we will read-modify-write to
            linkerFileData = Multifunction.ReadFile(state.linkScriptPath, true);
            if(linkerFileData == null)
            {
                System.err.println("ezbl_tools: could not read \"" + state.linkScriptPath + "\"");
                System.exit(-5);
            }
        }

        // Start getting the Section headers, Symbol Table, and for XC32, the Section contents
        state.elfDump = new ELFDumpParser();
        List<String> cmdLine;
        if(objDumpExecutable.contains("xc32-objdump"))
        {
            cmdLine = Arrays.asList(state.compilerFolder + File.separator + objDumpExecutable, "--section-headers", "--syms", "--full-contents", debuggableArtifactPath);
        }
        else
        {
            cmdLine = Arrays.asList(state.compilerFolder + File.separator + objDumpExecutable, "--section-headers", "--syms", debuggableArtifactPath); // Do not add --debugging or --reloc for XC16 compatibility on Mac
            if(state.verbose)
                System.out.println("ezbl_tools: " + CatStringList(cmdLine, " "));
            if(state.elfDump.startObjDump(cmdLine) < 0)
            {
                System.err.printf("ezbl_tools: failed to execute '%1$s'\n", CatStringList(cmdLine, " "));
                System.exit(-5);
            }
            cmdLine = Arrays.asList(state.compilerFolder + File.separator + objDumpExecutable, "--full-contents", debuggableArtifactPath); // Do not add --debugging or --reloc for XC16 compatibility on Mac
        }
        if(state.verbose)
            System.out.println("ezbl_tools: " + CatStringList(cmdLine, " "));
        if(state.elfDump.startObjDump(cmdLine) < 0)
        {
            System.err.printf("ezbl_tools: failed to execute '%1$s'\n", CatStringList(cmdLine, " "));
            System.exit(-3);
        }

        // Clear last lists of used ranges and allocate new lists
        state.romUseRegions = new ArrayList<>();
        state.ramUseRegions = new ArrayList<>();
        state.ramUseSections = new ArrayList<>();
        state.otherSections = new ArrayList<>();
        state.ramSections = new ArrayList<>();
        state.romSections = new ArrayList<>();
        state.noProgramRegions = new ArrayList<>();
        state.noEraseRegions = new ArrayList<>();
        state.noVerifyRegions = new ArrayList<>();
        noAppSpaceRanges = new ArrayList<>();

        //if(state.MCU16Mode)
        //{
        //    firstPassResultsSectionRegexStart = "EZBL Generated Information([^*/]+\\*)+/\n";
        //    firstPassResultsSectionRegexEnd = "\n\n\n";
        //}
        // Things could have moved, so erase any previous end address
        // information derived from a prior DumpParser pass
        if((state.coreType == CPUClass.mm) && (state.baseAddress == 0x00000000L))
        {
            state.baseAddress = 0x9D000000L;
        }

        elfDumpText = state.elfDump.parseAllObjOutput(0);   // Wait for Section header table and Symbol Tables to be available
        elfDumpText += state.elfDump.parseAllObjOutput(0);  // Wait for Section contents to be decoded (if applicable; XC32 doesn't need this)

        boolean newDualPartitionMode = state.elfDump.symbolsByName.containsKey("__DUAL_PARTITION");
        if(state.dualPartitionMode != newDualPartitionMode)
        {
            state.LoadEDCData();    // Reload the EDC data since we changed Single/Dual Partition mode since prior operations
        }

        if((state.coreType == CPUClass.mm))
            state.elfDump.normalizePIC32Addresses();

        if(state.saveTemps)
            Multifunction.WriteFile(state.temporariesPath + "ezbl_objdump.txt", elfDumpText.getBytes(), false);
        else
            Multifunction.DeleteFile(state.temporariesPath + "ezbl_objdump.txt");

        // Throw away .text.EZBL_AppReservedHole* sections. These 
        // are used in Bootloader projects to force linking away from certain 
        // addresses so they are available when building the Application
        state.elfDump.removeSections("[.]text[.]EZBL_AppReserved.*");

        if(state.hasFlashConfigWords && (state.configWordsRegion != null))
        {
            configWordPageFreespace = state.configWordsRegion.clone().alignToEraseSize();
            MemoryRegion.SubtractRegion(configWordPageFreespace.getAsList(), state.configWordsRegion.clone().alignToProgSize());
            for(long addr = configWordPageFreespace.startAddr; addr < configWordPageFreespace.endAddr; addr += 0x2)
            {
                if(state.elfDump.romSectionMapByAddr.containsKey(addr))
                {
                    Section lastPageSec = state.elfDump.romSectionMapByAddr.get(addr);
                    if(lastPageSec.name.startsWith("reserve_") || lastPageSec.name.startsWith(".dbg_code") || (lastPageSec.loadMemoryAddress == 0xBFC00490L))
                        continue;
                    bootContentOnConfigPage = true;
                    break;
                }
            }
        }

        // Generate a .heap section for XC32/PIC32MM projects (ex: USB Host MSD bootloaders). These are implicitly created by xc32-ld using:
        //      _heap weak *ABS* symbol (base address)
        //      _eheap weak *ABS* symbol (end address of minimum requested heap size)
        //      _min_heap_size global *ABS* symbol (user project defined minimize heap size in bytes)
        //      __MIN_HEAP_SIZE (unknown origin, weak *ABS* copy of _min_heap_size?)
        if(!state.MCU16Mode && state.elfDump.symbolsByName.containsKey("_heap") && !state.elfDump.sectionsByName.containsKey(".heap"))
        {
            Section heap = new Section();
            heap.alignment = 0x4;
            heap.name = ".heap";
            heap.flags = new SectionFlags();
            heap.symbols = new ArrayList<>();
            heap.symbolsByAddr = new HashMap<>();
            heap.symbolsByName = new HashMap<>();
            heap.addSymbol(state.elfDump.symbolsByName.get("_heap"));
            heap.loadMemoryAddress = state.elfDump.symbolsByName.get("_heap").address;
            heap.virtualMemoryAddress = heap.loadMemoryAddress;
            heap.size = 0;
            if(state.elfDump.symbolsByName.containsKey("__MIN_HEAP_SIZE"))
            {
                heap.addSymbol(state.elfDump.symbolsByName.get("__MIN_HEAP_SIZE"));
                heap.size = state.elfDump.symbolsByName.get("__MIN_HEAP_SIZE").address;
            }
            if(state.elfDump.symbolsByName.containsKey("_min_heap_size"))
            {
                heap.addSymbol(state.elfDump.symbolsByName.get("_min_heap_size"));
                heap.size = state.elfDump.symbolsByName.get("_min_heap_size").address;
            }
            if(state.elfDump.symbolsByName.containsKey("_eheap"))
            {
                heap.size = state.elfDump.symbolsByName.get("_eheap").address - heap.virtualMemoryAddress;
                heap.addSymbol(state.elfDump.symbolsByName.get("_eheap"));
            }
            heap.isRAM = true;
            heap.flags.ALLOC = true;
            heap.flags.NEVER_LOAD = true;
            state.elfDump.addSection(heap);
        }

        // Find all the important data ("ram") and nonvolatile/Flash program
        // ("rom") sections for reporting in the RAM and ROM use tables. Flash 
        // Bootloader sections are expanded to cover a full erase block.
        for(int secIndex = 0; secIndex < state.elfDump.sections.size(); secIndex++)
        {
            Section sec = state.elfDump.sections.get(secIndex);

            // Capture the debug/production build state for setting BKBUG
            if(sec.name.equals(".icd") || sec.name.startsWith("reserve_data_") || sec.name.equals("reserve_boot_") || ((state.coreType == CPUClass.mm) && (sec.loadMemoryAddress == 0xBFC00490L)))
                state.linkedAsDebugImage = true;
            else if(sec.name.equals("EZBL_ICD_RAM_RESERVE"))
                state.linkedAsDebugImage = false;

            if(sec.isDebug)  // Ignore debugging sections
            {
                state.otherSections.add(sec);
                continue;
            }

            MemoryRegion mr = sec.mapToDeviceRegion(state.devMemories, state.dualPartitionMode ? Partition.partition1 : Partition.single);
            if(sec.data != null)
                sec.data.assignedMemory = mr.name;
            sec.mappedMemoryRegion = mr.clone();

            if(sec.isROM)   // Harvest all nonvolatile Flash sections (.text, .prog, .const, .psv, .ivt, Flash config words, etc.)
            {
                mr.alignToProgSize();

                if((state.devSpecialConf.FBOOTAddr > 0) && (sec.loadMemoryAddress == state.devSpecialConf.FBOOTAddr))  // Reserve FBOOT data as a no-verify range since MPLAB ICSP tools could program a different value from the .hex file
                {
                    state.noVerifyRegions.add(mr.clone());
                }

                // Otherwise add this ROM section contents to a list so we can regenerate the data as .s assembly output
                state.romSections.add(sec);

                // Compute start and end addresses for this section that fills
                // a complete sector if they don't already, aren't in test
                // memory (ex: real fuses outside Flash), or are a special
                // region we want to handle seperately
                // Only modify sections that are in Flash (i.e. not fuse based config words)
                // Do not modify Flash config word sections if we are allowed to erase the last page
                if(((!state.allowLastPageErase) || (state.configWordsRegion == null) || (state.allowLastPageErase && (mr.endAddr <= state.configWordsRegion.startAddr) && (mr.type != MemType.FLASHFUSE))) // !Flash Config words
                   || (state.MCU16Mode
                       && ((mr.startAddr & 0xFF800000L) == 0) // !Test memory
                       && ((!state.allowFirstPageErase) || (state.allowFirstPageErase && (mr.startAddr >= state.eraseBlockSizeAddresses) && (state.baseAddress >= state.eraseBlockSizeAddresses))))) // !Reset vector/IVT on PIC24/dsPIC33
                {
                    if(state.MCU16Mode)
                    {
                        if(mr.type == MemType.FLASHFUSE)
                        {
                            if(bootContentOnConfigPage || !state.allowLastPageErase)
                                mr.alignToEraseSize();
                            else
                                mr.alignToProgSize();
                        }
                        else
                            mr.alignToEraseSize();

                    }
                    else
                        mr.alignToEraseSizeWithoutOverlap(state.configWordsRegion.getAsList());
                }

                // Add the effective ROM use requirements range to a collection
                // of addresses that need to be non-erasable (i.e. belong to the
                // bootloader). This will be used to generate the
                // __EZBL_TOOLS_COMPUTED_NO_PROGRAM_RANGES/__EZBL_TOOLS_COMPUTED_NO_ERASE_RANGES.
                MemoryRegion regionClone = mr.clone();
                if(regionClone.comment == null)
                    regionClone.comment = "";
                regionClone.comment += String.format(" %s: [0x%08X, 0x%08X)", sec.name, sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size);

                if(sec.flags.NEVER_LOAD)
                    noAppSpaceRanges.add(regionClone);
                else
                    state.romUseRegions.add(regionClone);
                state.noProgramRegions.add(regionClone.clone());
                if((state.configWordsRegion == null) || bootContentOnConfigPage || (mr.endAddr <= state.configWordsRegion.startAddr) || (mr.startAddr >= state.configWordsRegion.endAddr))
                    state.noEraseRegions.add(regionClone.clone());
                continue;
            }

            // Harvest all RAM sections required for the bootloader(.bss, .nbss, .data, .ndata, .pbss, .xbss, .ybss, .npbss?, etc.)
            if(sec.isRAM)
            {
                state.ramSections.add(sec);

                // Do not mark RAM as Bootloader occupied if it contains "EZBL_AppReservedRAMHole" in the section name or is "EZBL_APP_RAM_RESERVE"/"EZBL_ICD_RAM_RESERVE". These are noload sections good for preventing the Bootloader from clobbering RAM locations, like all the near RAM on the device.
                if(sec.name.startsWith("EZBL_ICD_RAM_RESERVE") || sec.name.equals("EZBL_APP_RAM_RESERVE") || sec.name.contains("EZBL_AppReservedRAMHole") || sec.name.equals(".stack") || sec.name.equals(".dbg_data"))
                    continue;

                state.ramUseRegions.add(mr.clone());
                state.ramUseSections.add(sec);
                continue;
            }

            // If we get down here, it means this section isn't for debugging, known allocation in RAM, or known allocation in ROM. This ideally shouldn't happen. If it does, we should debug this...
            if(state.verbose)
            {
                System.err.println("ezbl_tools: Don't know what memory region the \"" + sec.name + "\" section belongs in, so bootloader handling may be incorrect. This EZBL version may not be compatible with your compiler version.");
            }
            state.otherSections.add(sec);
        }

        // Sort the sections by loadMemoryAddress
        Collections.sort(state.romSections);
        Collections.sort(state.ramSections);

        // Figure out how many addresses the Bootloader has reserved for it in main flash and where the first Application page is located
        List<MemoryRegion> bootloaderSpaces = new ArrayList<>();
        bootloaderSpaces.addAll(state.romUseRegions);
        bootloaderSpaces.addAll(noAppSpaceRanges);
        if(state.allowLastPageErase && !bootContentOnConfigPage)
            bootloaderSpaces = MemoryRegion.SubtractRegions(bootloaderSpaces, state.configWordsRegion, false);
        bootloaderSpaces = MemoryRegion.coalesce(bootloaderSpaces, bootloaderSpaces.get(0).eraseAlign, bootloaderSpaces.get(0).eraseAlign, false);
        List<MemoryRegion> appSpaceRegions = MemoryRegion.SubtractRegions(state.mainFlashRegion, bootloaderSpaces, false);
        long newAddressOfAppErasable = appSpaceRegions.get(0).startAddr;
        if(!state.MCU16Mode)    // PIC32MM
            bootloaderSpaces = MemoryRegion.SubtractRegions(state.mainFlashRegion, appSpaceRegions, false); // Remove everything in Boot Flash since EZBL_BOOTLOADER_SIZE is used in XC32 linker scripts to allocate EZBL_appErasableFlags, EZBL_APP_EBase, EZBL_APP_START, etc.
        long sizeOfBootloaderOccupiedSpace = 0;
        for(MemoryRegion mr : bootloaderSpaces)
        {
            sizeOfBootloaderOccupiedSpace += mr.endAddr - mr.startAddr;
        }

        // Compute the base addresses to place app erasable data, including the app's goto __reset and Interrupt Goto Table
//        if(state.verbose)
//        {
//            System.err.printf("\nezbl_tools.jar: prior base address of App = 0x%06X, new address = 0x%06X\n", state.baseAddressOfAppErasable, newAddressOfAppErasable);
//            Multifunction.WriteFile(String.format("build/ezbl_debug/pass%d_ezbl_commands.txt", state.pass), String.format("pushd %s\njava -cp %s %s\npopd\n", System.getProperty("user.dir"), System.getProperty("java.class.path"), System.getProperty("sun.java.command")).getBytes(), false);
//            Multifunction.CopyFile(state.linkScriptPath, String.format("build/ezbl_debug/pass%d_linker_script_input.gld", state.pass));
//            Multifunction.CopyFile(stateSavePath, String.format("build/ezbl_debug/pass%d_ezbl_state.bin", state.pass));
//            Multifunction.CopyFile(debuggableArtifactPath, String.format("build/ezbl_debug/pass%d_input.elf", state.pass));
//        }
        if((state.pass == 2) && (state.baseAddressOfAppErasable != newAddressOfAppErasable))    // If bootloader changed size in pass 2 linking due to generated data, try to correct by linking a 3rd time
        {
            try
            {
                if(System.in.available() != 0)
                {
                    InputStreamReader in = new InputStreamReader(System.in);
                    char linkCommandsChars[] = new char[System.in.available()];
                    in.read(linkCommandsChars);
                    linkCommandLine = String.valueOf(linkCommandsChars);
                }
                if((linkCommandLine != null) && !linkCommandLine.isEmpty())
                {
                    System.out.printf("\nezbl_tools.jar: Bootloader changed geometry between pass %d and pass %d linking. A %s linking step will be invoked.\n", state.pass - 1, state.pass, state.getPassNumStr(state.pass + 1));
                    System.out.println(linkCommandLine.trim());
                    relinkProcBuilder = new ProcessBuilder(Multifunction.ParseCommandLineArguments(linkCommandLine));
                    relinkProcBuilder.inheritIO();
                }
            }
            catch(IOException ex)
            {
                Logger.getLogger(DumpParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(relinkProcBuilder == null)
            {
                System.err.printf("\nezbl_tools.jar: Bootloader changed geometry between pass %d and pass %d linking. Linking a %s time is required.\n\n", state.pass - 1, state.pass, state.getPassNumStr(state.pass + 1));
                System.exit(-301);
            }
        }
        else if((state.pass >= 3) && (state.baseAddressOfAppErasable != newAddressOfAppErasable))
        {
            System.err.printf("\nezbl_tools.jar: Linking a %s time did not successfully resolve the geometry. Try adding or removing a few bytes of code/flash constants.\n\n", state.getPassNumStr());
            System.exit(-300);
        }
        state.sizeOfIGT = state.remapISRThroughIGT ? (state.ivtVectorsImplemented + 1) * state.igtSpacing : 0;
        state.sizeOfAppErasable = state.getAppBootloadStateSize() + sizeOfAppGotoReset + state.sizeOfIGT;
        state.baseAddressOfAppErasable = newAddressOfAppErasable;
        state.baseAddressOfGotoReset = state.baseAddressOfAppErasable + state.getAppBootloadStateSize();
        state.baseAddressOfIGT = state.baseAddressOfGotoReset + sizeOfAppGotoReset;

        // Add the appBootloadState as a noVerifyRange since we will skip it
        // during bootloading and only write it when everything is verified. We
        // can't abort with a verify error when the image file presents this
        // record.
        MemoryRegion appBootloadStateRange = new MemoryRegion(state.baseAddressOfAppErasable, state.baseAddressOfAppErasable + state.getAppBootloadStateSize());
        appBootloadStateRange.copyMetaData(state.mainExecutionRegion);
        appBootloadStateRange.eraseAlign = appBootloadStateRange.programAlign;       // Do not allow future address expansion of this record during coalescing
        state.noVerifyRegions.add(appBootloadStateRange);
        state.noProgramRegions.add(appBootloadStateRange);

        // Sort and coalesce the used address ranges
        state.ramUseRegions = MemoryRegion.coalesce(state.ramUseRegions, 1, 1, true);
        state.romUseRegions = MemoryRegion.coalesce(state.romUseRegions, true, state.configWordsRegion.getAsList(), false);

        if((state.coreType == CPUClass.mm))    // PIC32MM
        {
            for(int i = 0; i < 256; i++)
            {
                Symbol vecSym = state.elfDump.symbolsByName.get(String.format("__vector_dispatch_%d", i));
                if(vecSym == null)
                    continue;

                String vecDesc = state.ivtVectors.get(i) != null
                                 ? state.ivtVectors.get(i).desc != null
                                   ? "/* RAM pointer to " + state.ivtVectors.get(i).desc + " ISR */"
                                   : ""
                                 : "";

                long ISRAddr = 0;
                String ISRFunctionName = "???";
                Symbol ISRFunctionSym;
                Section vectorSection = state.elfDump.romSectionMapByAddr.get(vecSym.address);
                if(vectorSection != null)
                {
                    ISRAddr = vectorSection.data.GetIntDataAtAddress(vecSym.address);
                    ISRAddr = ((((((ISRAddr >> 16) & 0x0000FFFFL) | (ISRAddr << 16)) & 0x03FFFFFFL) << 1) | 0x9D000001L);  // Pull the lower 26 bits jump target information out of the J opcode and add the missing high bits. 0x1 is needed to stay in microMIPS ISA mode.
                    ISRFunctionSym = state.elfDump.functionSymsByAddr.get(ISRAddr - 1);
                    if(ISRFunctionSym != null)
                        ISRFunctionName = ISRFunctionSym.name;
                    if(ISRFunctionName.contains("_DefaultInterrupt"))   // ISR removed since last build; do not retain these.
                        continue;
                }

                ivtVectorPointers += String.format("\n    %-92s", String.format("EZBL_Vector%dISRPtr = .; %s", i, vecDesc));
                ivtVectorPointers += String.format("\n    %-92s", String.format(". += 4; /* = 0x%08X for Boot %s() ISR or App ISR address for IRQ %d */", ISRAddr, ISRFunctionName, i));

                ivtDispatcher += String.format("\n    %1$-92s", String.format(".vector_%1$d_EZBL_RAMRedirect _ebase_address + 0x200 + ((_vector_spacing << 3) * %1$d) :", i));
                ivtDispatcher += String.format("\n    %1$-92s", String.format("{"));
                ivtDispatcher += String.format("\n    %1$-92s", String.format("  LW32(26, 28, ABSOLUTE(EZBL_Vector%1$dISRPtr) - ABSOLUTE(_gp)); /* k0 = 26, gp = 28 */", i));
                ivtDispatcher += String.format("\n    %1$-92s", String.format("  JRC16(26);"));
                ivtDispatcher += String.format("\n    %1$-92s", String.format("  NOP16();"));
                ivtDispatcher += String.format("\n    %1$-92s", String.format("} > kseg0_program_mem"));

                ivtVectorSave += String.format("\n    %1$-92s", String.format("  LONG(%d);", i));
                ivtVectorSave += String.format("\n    %1$-92s", String.format("  EZBL_Vector%dOriginal = ABSOLUTE(.);", i));
                ivtVectorSave += String.format("\n    %1$-92s", String.format("  KEEP(*(.vector_%d));", i));
                genROMDataAddrSize += 0xC;
            }

            bootReservedRAMMacro = "";
            for(MemoryRegion r : state.ramUseRegions)
            {
                bootReservedRAMMacro += String.format("\n    %-92s", String.format("  EZBL_RAM_AT_0x%1$08X 0x%1$08X :", r.startAddr));
                bootReservedRAMMacro += String.format("\n    %-92s", String.format("  {"));
                bootReservedRAMMacro += String.format("\n    %-92s", String.format("    *(EZBL_RAM_AT_0x%1$08X); /* [0x%1$08X, 0x%3$08X), contains %2$d bytes */", r.startAddr, r.endAddr - r.startAddr, r.endAddr));
                bootReservedRAMMacro += String.format("\n    %-92s", String.format("  } > %s", r.name));
            }
        }
        else    // PIC24/dsPIC devices only
        {
            // Find/compute final IVT entry values
            if(state.remapISRThroughIGT)
            {
                int igtIndex = 0;

                for(int i = 0; i < state.ivtVectors.size(); i++)
                {
                    InterruptVector vector = state.ivtVectors.get(i);
                    Symbol sym = state.elfDump.symbolsByName.get("__" + vector.name);

                    int igtAddress = (int)state.baseAddressOfIGT + state.igtSpacing * igtIndex;
                    if(!vector.implemented)    // When unimplemented, send to extra __DefaultInterrupt IGT entry
                        igtAddress = (int)state.baseAddressOfIGT + state.igtSpacing * state.ivtVectorsImplemented;

                    if((sym == null) || (genISRDispatchStubs > 32))
                    {
                        if((sym != null) && (genISRDispatchStubs > 32))
                        {
                            System.err.print(Multifunction.FormatHelpText(110, 0, String.format("ezbl_tools: warning: There are more than 32 interrupts defined in your bootloader. EZBL only supports up to 32 run-time choosable ISRs in your Bootloader. Building will proceed by not hooking the Bootloader's %1$s() function up in the IVT and will instead always forward this Interrupt to the Application IGT.", vector.name)));
                        }
                    }
                    else
                    {
                        // Function defined in this build, map to a muxing stub so we
                        // can control if the bootloader or application receives the
                        // interrupt.

                        // Create code to forward implemented interrupts to the
                        // application when bootloader interrupt forwarding is turned off
                        String shortIntName = vector.name.replace("Interrupt", "").replaceFirst("_", "");
                        ivtMasks += String.format("\n%1$-32s = 0x%2$08X;"
                                                  + "\n%3$-32s = %4$d;"
                                                  + "\n%5$-32s = %6$d;",
                                                  "_EZBL_FORWARD_MASK_" + shortIntName, (1 << genISRDispatchStubs),
                                                  "_EZBL_FORWARD_POS_" + shortIntName, genISRDispatchStubs,
                                                  "_EZBL_FORWARD_IRQ_" + shortIntName, vector.irqNum);

                        ivtDispatcher += String.format("%1$-96s", "\n    EZBL_Dispatch_" + vector.name + " = ABSOLUTE(.);") + String.format("%1$-49s", " /* EZBL_Dispatch_" + vector.name + ":") + " */ "
                                         + String.format("%1$-96s", String.format("\n    BTSC(ABSOLUTE(_" + forwardBootloaderFlagsVariableName + ") + 0x%1$X, %2$d);", genISRDispatchStubs / 16 * 2, genISRDispatchStubs % 16)) + String.format("%1$-49s", String.format(" /*    btsc    _" + forwardBootloaderFlagsVariableName + ", #%1$d", genISRDispatchStubs)) + " */ "
                                         + String.format("%1$-96s", String.format("\n    %1$s(ABSOLUTE(0x%2$06X));", state.igtSpacing == 0x2 ? "BRA" : "GOTO", igtAddress)) + String.format("%1$-49s", String.format(" /*    %1$s    ApplicationIGT(__%2$s)", state.igtSpacing == 0x2 ? "bra" : "goto", vector.name)) + " */ "
                                         + String.format("%1$-96s", String.format("\n    %1$s(DEFINED(__" + vector.name + ") ? ABSOLUTE(__" + vector.name + ") : ABSOLUTE(0x%2$06X));", state.igtSpacing == 0x2 ? "BRA" : "GOTO", igtAddress)) + String.format("%1$-49s", String.format(" /*    %1$s    BootloaderISR(__%2$s)", state.igtSpacing == 0x2 ? "bra" : "goto", vector.name)) + " */ ";
                        genISRDispatchStubs++;
                    }

                    // Advance counter so we know what the next IGT entry address
                    // should be whenever we process an implemented vector
                    if(state.ivtVectors.get(i).implemented)
                        igtIndex++;
                }

                genROMDataAddrSize += (0x2 + 2 * state.igtSpacing) * genISRDispatchStubs;
            }
        }
        ivtDispatcher = ivtDispatcher.replaceAll("\n", Matcher.quoteReplacement("\\\n"));     // Add line continuation character to all preprocessor new-lines
        ivtVectorPointers = ivtVectorPointers.replaceAll("\n", Matcher.quoteReplacement("\\\n"));
        ivtVectorSave = ivtVectorSave.replaceAll("\n", Matcher.quoteReplacement("\\\n"));
        bootReservedRAMMacro = bootReservedRAMMacro.replaceAll("\n", Matcher.quoteReplacement("\\\n"));

        // Build eraseRestoreTable data restore records for erasable
        // bootloader elements.
        // Find items that aren't on an whole erase page by themself (ex: Config
        // Word Values, Reset vector, used interrupt vector list if allowing
        // Page 0 or Last Config Words page to be erased)
        // Enumerate everything in ROM, including test space i.e. address >= 0x800000
        for(Section sec : state.romSections)
        {
            // Check if section overlaps the first erase page and we allow erasing the first page
            if(state.MCU16Mode && state.allowFirstPageErase)
            {
                if(sec.loadMemoryAddress < 0x000000L + state.eraseBlockSizeAddresses)
                {
                    if(sec.data == null)
                    {
                        System.err.print("ezbl_tools: failed to get section data from object dump.\n");
                        System.exit(-4);
                    }
                    // Clone this data record, except the comment and assignedMemory String references since we are going to possibly make changes to the address and data
                    DataRecord restoreRecord = sec.data.Clone();
                    restoreRecord.comment = "";
                    restoreRecord.assignedMemory = "";

                    if(restoreRecord.getEndAddress() > 0x000000L + state.eraseBlockSizeAddresses)  // Filter out anything outside of the first erase page
                        restoreRecord = restoreRecord.SplitAtAddress(state.eraseBlockSizeAddresses);
                    eraseRestoreTable.add(restoreRecord);
                }
            }

            if(state.hasFlashConfigWords && state.allowLastPageErase && !bootContentOnConfigPage && (state.configWordsRegion != null))
            {
                // Check if this is a Flash Config value or other Bootloader data on last page, if so, save it to eraseRestoreTable
                if((sec.loadMemoryAddress + sec.size > state.configWordsRegion.startAddr) && (sec.loadMemoryAddress < state.configWordsRegion.endAddr))
                {
                    if(sec.data == null)
                    {
                        System.err.print("ezbl_tools: failed to get section data from object dump.\n");
                        System.exit(-4);
                    }
                    DataRecord restoreRecord = sec.data.Clone();
                    if(sec.data.address < state.configWordsRegion.startAddr)
                        restoreRecord = restoreRecord.SplitAtAddress(state.configWordsRegion.startAddr);
                    eraseRestoreTable.add(restoreRecord);
                    continue;
                }
            }

            // Add "real" config fuse values, if they exist, but not if the device also has Flash Config Words. FBOOT is excluded from the address check here since all devices with FBOOT have Flash Config Words.
            if(!state.hasFlashConfigWords && (sec.loadMemoryAddress >= 0x800000L))  // Implies MCU16Mode
            {
                if(sec.data == null)
                {
                    System.err.print("ezbl_tools: failed to get section data from object dump.\n");
                    System.exit(-4);
                }
                eraseRestoreTable.add(sec.data.Clone());   // These do not need address padding as they are byte based
                continue;
            }
        }

        // If built for debug mode, mask the BKBUG bit (or 2-bit DEBUG field for PIC32MM)
        if(state.linkedAsDebugImage && (state.devSpecialConf.BACKBUGAddr != 0))
        {
            MemoryRegion specialCfgWord = state.devConfigWordsByName.get(state.devSpecialConf.BACKBUGConfigName).clone().alignToProgSize();
            specialCfgWord.eraseAlign = specialCfgWord.programAlign;    // Do not expand addresses
            DataRecord cfgRecord = DataRecord.ExtractRange(eraseRestoreTable, specialCfgWord.startAddr, specialCfgWord.endAddr);
            if(cfgRecord == null)
            {   // No existing data on the min flash programmable word block
                cfgRecord = new DataRecord();
                cfgRecord.address = specialCfgWord.startAddr;
                cfgRecord.architecture16Bit = state.MCU16Mode;
                cfgRecord.assignedMemory = state.MCU16Mode ? state.devSpecialConf.BACKBUGConfigName : "configsfrs";
                cfgRecord.comment = "Generated section for debugging after bootloader erase of Config words flash page";
                cfgRecord.data = new byte[(int)(state.MCU16Mode ? (specialCfgWord.endAddr - specialCfgWord.startAddr) / 2 * 3 : (specialCfgWord.endAddr - specialCfgWord.startAddr))];
                Arrays.fill(cfgRecord.data, (byte)0xFF);   // Going to clear bit below
            }
            int i = (int)(state.devSpecialConf.BACKBUGAddr - specialCfgWord.startAddr);
            if(state.MCU16Mode)
                i = i / 2 * 3;
            cfgRecord.data[i + 0] &= (byte)((~state.devSpecialConf.BACKBUGMask) & 0xFFL);
            cfgRecord.data[i + 1] &= (byte)(((~state.devSpecialConf.BACKBUGMask) & 0xFF00L) >> 8);
            cfgRecord.data[i + 2] &= (byte)(((~state.devSpecialConf.BACKBUGMask) & 0xFF0000L) >> 16);
            if(!state.MCU16Mode)
                cfgRecord.data[i + 3] &= (byte)(((~state.devSpecialConf.BACKBUGMask) & 0xFF000000L) >> 24);
            if(!bootContentOnConfigPage)
                eraseRestoreTable.add(cfgRecord); // Put record back in the eraseRestoreTable list. DataRecord.ExtractRange() removes it from the list.
            state.noVerifyRegions.add(specialCfgWord);          // Do not try to verify the Config word address containing BKBUG since it may or may not have been manipulated by MPLAB ICSP programming
            state.romUseRegions.add(specialCfgWord);
            noAppSpaceRanges.add(specialCfgWord);
            if((state.coreType == CPUClass.mm))
            {
                state.noProgramRegions.add(specialCfgWord);
                state.noEraseRegions.add(specialCfgWord);
            }
        }

        // Optimize records and pad/align to minimum flash programming size
        DataRecord.CoalesceRecords(eraseRestoreTable, true, state.flashWordSize, state.flashWordSize);

        // Now move/create/force-mask the Reserved bit containing Config Word to the very end of the rom restore table
        if(state.devSpecialConf.reservedBitAddr != 0)
        {
            MemoryRegion specialCfgWord = state.devConfigWordsByName.get(state.devSpecialConf.reserveBitConfigName).clone().alignToProgSize();
            specialCfgWord.eraseAlign = specialCfgWord.programAlign;    // Do not expand addresses
            DataRecord cfgRecord = DataRecord.ExtractRange(eraseRestoreTable, specialCfgWord.startAddr, specialCfgWord.endAddr);
            if(cfgRecord == null)
            {   // No existing data on the min flash programmable word block
                cfgRecord = new DataRecord();
                cfgRecord.address = specialCfgWord.startAddr;
                cfgRecord.architecture16Bit = state.MCU16Mode;
                cfgRecord.assignedMemory = state.devSpecialConf.reserveBitConfigName;
                cfgRecord.comment = "Generated section for clearing Reserved Bit";
                cfgRecord.data = new byte[(int)(state.MCU16Mode ? (specialCfgWord.endAddr - specialCfgWord.startAddr) / 2 * 3 : (specialCfgWord.endAddr - specialCfgWord.startAddr))];
                Arrays.fill(cfgRecord.data, (byte)0xFF);   // Going to clear bit below
            }
            int i = (int)(state.devSpecialConf.reservedBitAddr - specialCfgWord.startAddr);
            if(state.MCU16Mode)
                i = i / 2 * 3;
            cfgRecord.data[i + 0] &= (byte)((~state.devSpecialConf.reservedBitMask) & 0xFFL);
            cfgRecord.data[i + 1] &= (byte)(((~state.devSpecialConf.reservedBitMask) & 0xFF00L) >> 8);
            cfgRecord.data[i + 2] &= (byte)(((~state.devSpecialConf.reservedBitMask) & 0xFF0000L) >> 16);
            if(!state.MCU16Mode)
                cfgRecord.data[i + 3] &= (byte)(((~state.devSpecialConf.reservedBitMask) & 0xFF000000L) >> 24);
            if(state.MCU16Mode)
            {
                if(!bootContentOnConfigPage)
                    eraseRestoreTable.add(cfgRecord); // Put record back in the eraseRestoreTable list. DataRecord.ExtractRange() removes it from the list.
                // DO NOT sort at this point since this reserved record needs to appear last
                DataRecord.CoalesceRecords(eraseRestoreTable, false);   // Do not sort, just coalesce the reservedBitRecord, if applicable
                state.romUseRegions.add(specialCfgWord);
            }
            state.noVerifyRegions.add(specialCfgWord);      // Do not try to verify the Config word address containing BKBUG or SIGN since it may or may not have been manipulated by MPLAB ICSP programming or hardware
            noAppSpaceRanges.add(specialCfgWord);
            if((state.coreType == CPUClass.mm))
            {
                state.noProgramRegions.add(specialCfgWord);
                state.noEraseRegions.add(specialCfgWord);
            }
        }

        // Don't verify any undefined Config words region if the Bootloader is not allowed to erase them. This is necessary because MPLAB ICSP tools currently (~MPLAB X IDE v4.20) may program unimplemented config words with data left over from row programming other rows, device dependent
        if(state.hasFlashConfigWords && (state.configWordsRegion != null) && (!state.allowLastPageErase || bootContentOnConfigPage))
        {
            state.noVerifyRegions.addAll(MemoryRegion.SubtractRegions(state.configWordsRegion.getAsList(), Section.getMappedMemoryRegions(state.romSections), false));
        }

        if(state.verbose)
        {
            List<MemoryRegion> paddingAlignmentRegions = MemoryRegion.SubtractRegions(state.romUseRegions, Section.getMappedMemoryRegions(state.romSections), false);
            long totalPad = 0;
            for(MemoryRegion mr : paddingAlignmentRegions)
            {
                System.err.printf("    Padding Range: [0x%06X, 0x%06X)\n", mr.startAddr, mr.endAddr);
                totalPad += mr.endAddr - mr.startAddr;
            }
            System.err.printf("    Total Padding/Alignment space: 0x%06X (%d bytes)\n", totalPad, totalPad / 2 * 3);
        }

        // Optimize generated table data
        noAppSpaceRanges = MemoryRegion.coalesce(noAppSpaceRanges, 0, 0, false);
        state.noProgramRegions = MemoryRegion.coalesce(state.noProgramRegions, true, state.allowLastPageErase && !bootContentOnConfigPage ? state.configWordsRegion.getAsList() : null, false);
        state.noEraseRegions = MemoryRegion.coalesce(state.noEraseRegions, true, state.allowLastPageErase && !bootContentOnConfigPage ? state.configWordsRegion.getAsList() : null, false);
        state.noVerifyRegions = MemoryRegion.coalesce(state.noVerifyRegions, true, state.allowLastPageErase && !bootContentOnConfigPage ? state.configWordsRegion.getAsList() : null, false);
        //state.noVerifyRegions = MemoryRegion.coalesce(state.noVerifyRegions, false, state.allowLastPageErase && !bootContentOnConfigPage ? state.configWordsRegion.getAsList() : null, false);

        appSpaceGeometry = MemoryRegion.SubtractRegions(state.devNVGeometry, state.romUseRegions, false);
        MemoryRegion.alignToEraseSize(noAppSpaceRanges, !bootContentOnConfigPage ? state.configWordsRegion.getAsList() : null);
        appSpaceGeometry = MemoryRegion.SubtractRegions(appSpaceGeometry, noAppSpaceRanges, false);    // Subtract off any bootloader noload/NEVER_LOAD attributed sections, typically used for data EEPROM emulation, which we don't want to erase as app space and don't want to restrict erase by the application.

        // Sum all the data we are generating and adding to the linker script within Bootloader space
        for(DataRecord dr : eraseRestoreTable)
        {
            genROMDataAddrSize += (state.MCU16Mode ? 0x4 : 0x8) + (dr.getEndAddress() - dr.address);  // 0x4 (or 0x8) is for start address and byte length record data
        }
        genROMDataAddrSize += (state.MCU16Mode ? 0x4 : 0x8) * (appSpaceGeometry.size()
                                                               + state.noProgramRegions.size()
                                                               + state.noEraseRegions.size()
                                                               + state.noVerifyRegions.size());
//        // Seems to work, but this case is probably never needed in practice. Commented out; in the event the problem occurs, user is notified at pass 3 link faulre to add or remove code from the project instead of trying to auto-handle this.
//        state.generatedDataSizeByPass.put(state.pass, genROMDataAddrSize);
//        String tableSizeCorrectionFiller = null;
//        if(state.pass >= 2)
//        {
//            int sizeDiff = state.generatedDataSizeByPass.get(state.pass - 1) - genROMDataAddrSize;
//            Section fillerSec = state.elfDump.sectionsByName.get(".text.EZBLTableSizeCorrectionFiller");
//            if(fillerSec != null)
//                sizeDiff = (int)fillerSec.size;
//            else if((relinkProcBuilder != null) && (sizeDiff > 0))
//                genROMDataAddrSize += sizeDiff;
//
//            if(sizeDiff > 0)
//            {
//                int dummyFiller[] = new int[state.MCU16Mode ? sizeDiff / 2 : sizeDiff / 4];
//                Arrays.fill(dummyFiller, state.MCU16Mode ? 0x00FFFFFF : 0xFFFFFFFF);
//                tableSizeCorrectionFiller = genLinkerMacro("__EZBL_TOOLS_COMPUTED_TABLE_SIZE_CORRECTION_FILLER", state.MCU16Mode, dummyFiller);     // Table: .text.EZBLTableSizeCorrectFiller            
//            }
//        }

        // Generate Pass 1+ linking edits to project .gld file for ordinary bootloaders
        gldOutput.add("/* EZBL bootloader data generated during historical project linking passes */\n"
                      + String.format("\n/* ezbl_tools generated rom size = 0x%06X */\n", genROMDataAddrSize));
        gldOutput.add(String.format("\n%-32s =   0x%08X;", (state.MCU16Mode ? "_" : "") + "EZBL_BOOTLOADER_SIZE", sizeOfBootloaderOccupiedSpace));

        if(state.MCU16Mode)
        {
            gldOutput.add(String.format("\n%-32s =   0x%08X;", "_EZBL_appBootloadState", state.baseAddressOfAppErasable)
                          + String.format("\n%-32s =   0x%08X;", "_EZBL_APP_RESET_BASE", state.baseAddressOfGotoReset)
                          + String.format("\n%-32s =   0x%08X;", "_EZBL_IGT_BASE", state.baseAddressOfIGT)
                          + "\n");
            if(state.remapISRThroughIGT)
            {
                gldOutput.add("\n/* Bit mask flags for " + forwardBootloaderFlagsVariableName + " */"
                              + ivtMasks + "\n"
                              + "\n");
            }
        }
        gldOutput.add("\n");

        if(state.MCU16Mode) // Write formatted bootloader ram use table FYI (EZBL v1.11+)
            gldOutput.add(genLinkerMacro("__EZBL_TOOLS_COMPUTED_BOOT_RAM_USE /* FYI - not stored/used */", state.ramUseRegions, hexCharWidth));
        gldOutput.add(genLinkerMacro("__EZBL_TOOLS_COMPUTED_APP_SPACE_GEOMETRY", appSpaceGeometry, hexCharWidth));      // Table: .text.EZBLAppSpaceGeometry
        gldOutput.add(genLinkerMacro("__EZBL_TOOLS_COMPUTED_NO_PROGRAM_RANGES", state.noProgramRegions, hexCharWidth)); // Table: .text.EZBLNoProgramRanges
        gldOutput.add(genLinkerMacro("__EZBL_TOOLS_COMPUTED_NO_ERASE_RANGES", state.noEraseRegions, hexCharWidth));     // Table: .text.EZBLNoEraseRanges 
        gldOutput.add(genLinkerMacro("__EZBL_TOOLS_COMPUTED_NO_VERIFY_RANGES", state.noVerifyRegions, hexCharWidth));   // Table: .text.EZBLNoVerifyRanges
        gldOutput.add(genLinkerMacro("__EZBL_TOOLS_COMPUTED_ERASE_RESTORE_TABLE", eraseRestoreTable));                  // Table: .text.EZBLEraseRestoreTable
//        if(tableSizeCorrectionFiller != null)
//            gldOutput.add(tableSizeCorrectionFiller);

        // Write EZBL_ForwardBootloaderISR mask symbols and formatted __EZBL_IVT_DISPATCH_CODE
        gldOutput.add(String.format("\n%-96s", "#define __EZBL_IVT_DISPATCH_CODE ")
                      + ivtDispatcher
                      + "\n");

        if(!state.MCU16Mode)
        {
            gldOutput.add(String.format("\n%-96s", "#define __EZBL_RAM_POINTERS_TO_ISRS ")
                          + ivtVectorPointers
                          + "\n");

            gldOutput.add(String.format("\n%-96s", "#define __EZBL_IVT_VECTOR_SAVE ")
                          + ivtVectorSave
                          + "\n");

            gldOutput.add(String.format("\n%-96s", "#define __EZBL_TOOLS_COMPUTED_BOOTLOADER_RESERVED_RAM ")
                          + bootReservedRAMMacro
                          + "\n");
        }

        // Find the proper FIRST PASS RESULTS SECTION and replace it with new linking results
        TextBlock outGLD = new TextBlock(linkerFileData);

        if(!outGLD.Find(firstPassResultsSectionRegexStart, firstPassResultsSectionRegexEnd))
        {
            System.err.println("ezbl_tools: could not find 'AUTOMATIC FIRST PASS RESULTS SECTION' to write first pass linking results in \"" + state.linkScriptPath + "\"");
            System.exit(-6);
        }

        outputValuesString = CatStringList(gldOutput);
        outGLD.ReplaceInner(outputValuesString);

        // Write computed data to output file
        if(Multifunction.UpdateFileIfDataDifferent(state.linkScriptPath, outGLD.GetFullBlock(), true) < 0)
        {
            System.err.println("ezbl_tools: failed to write to \"" + state.linkScriptPath + "\"");
            System.exit(-7);
        }

        // Repeat the whole parsing process if we launched the linker to fixup any addresses that might change the bootloader geometry in pass 2 and force a 3rd pass
        if(relinkProcBuilder != null)
        {
            int linkReturn = -302;
            try
            {
                relinkProc = relinkProcBuilder.start();
                linkReturn = relinkProc.waitFor();
                relinkProc = null;
                relinkProcBuilder = null;
                if(linkReturn == 0)
                {
                    state.pass++;
                    main(args);
                    return;
                }
            }
            catch(InterruptedException ex)
            {
                Logger.getLogger(DumpParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(IOException ex)
            {
                System.err.printf("\nezbl_tools.jar: failed to launch %s linking process.\n", state.getPassNumStr());
                System.exit(-301);
            }
            System.exit(linkReturn);
        }

        // Save any parsing work we've done for later use (ex: symbols in --blobber -CreateMergeScript .merge.S/.gld/.ld generation)
        state.SaveToFile();
    }

    private static String genLinkerMacro(String elementNameAndComment, List<MemoryRegion> regions, int longCharWidth)
    {
        int lineFormatWidth = 80;
        List<String> ret = new ArrayList<>();
        int indentWidth = 4;
        String numFormat = "0x%X";
        String lineFormat = "\n%-" + String.valueOf(lineFormatWidth) + "s";
        String indentedLineFormat = "\\\n    %-" + String.valueOf(lineFormatWidth - 4) + "s";

        if(longCharWidth > 0)
            numFormat = "0x%0" + String.valueOf(longCharWidth) + "X";
        if(elementNameAndComment.length() > lineFormatWidth)
        {
            lineFormatWidth = elementNameAndComment.length() + 16 - (elementNameAndComment.length() % 16);
            lineFormat = "\n%-" + String.valueOf(lineFormatWidth) + "s";
            indentedLineFormat = String.format("\\\n%" + String.valueOf(indentWidth) + "s", "") + "%-" + String.valueOf(lineFormatWidth - indentWidth) + "s";
        }

        ret.add(String.format(lineFormat, String.format("#define " + elementNameAndComment)));
        for(MemoryRegion r : regions)
        {
            ret.add(String.format(indentedLineFormat, String.format("LONG(" + numFormat + "); LONG(" + numFormat + ");", r.startAddr, r.endAddr)));
        }

        if(ret.size() == 1) // Remove line padding to full line and line continuation slash for the last line (NOTE: Can also be the first line for empty definitions)
            ret.set(ret.size() - 1, "\n" + ret.get(ret.size() - 1).trim());
        else
            ret.set(ret.size() - 1, ret.get(ret.size() - 1).trim());
        ret.add("\n");
        return CatStringList(ret);
    }

    private static String genLinkerMacro(String elementNameAndComment, List<DataRecord> records)
    {
        int lineFormatWidth = 96;                   // Total characters in line before creating a line continuiation
        List<String> ret = new ArrayList<>();
        int indentWidth = 4;
        String numFormat;
        String lineFormat = "\n%-" + String.valueOf(lineFormatWidth) + "s";
        String indentedLineFormat = "\\\n    %-" + String.valueOf(lineFormatWidth - 4) + "s";

        int longCharWidth = 4;  // Number of hex characters needed to print an address with prefixed 0 padding. Should be 4 for 16-bit data space addresses, 6 for 16-bit program space addresses, or 8 for 32-bit addresses
        if(!records.isEmpty())
            longCharWidth = records.get(0).architecture16Bit ? 6 : 8;
        numFormat = "0x%0" + String.valueOf(longCharWidth) + "X";

        if(elementNameAndComment.length() > lineFormatWidth)                    // Adjust line continuation position by tab-equivalents of 16 characters at a time when too many characters exist for the default 96 character line width
        {
            lineFormatWidth = elementNameAndComment.length() + 16 - (elementNameAndComment.length() % 16);
            lineFormat = "\n%-" + String.valueOf(lineFormatWidth) + "s";
            indentedLineFormat = String.format("\\\n%-" + String.valueOf(indentWidth) + "s", "") + "%-" + String.valueOf(lineFormatWidth) + "s";
        }

        ret.add(String.format(lineFormat, String.format("#define " + elementNameAndComment)));  // Ex: #define __EZBL_TOOLS_COMPUTED_ERASE_RESTORE_TABLE

        for(DataRecord r : records)
        {
            String restoreLine = String.format("LONG(" + numFormat + "); LONG(" + numFormat + "); /* Start address, byte length [" + numFormat + ", " + numFormat + ") */ ", r.address, r.data.length, r.address, r.getEndAddress());
            ret.add(String.format(indentedLineFormat, restoreLine));
            restoreLine = "";
            for(long addr = r.address; addr < r.getEndAddress(); addr += (r.architecture16Bit ? 2 : 4))
            {
                if(((addr - r.address) % (r.architecture16Bit ? 10 : 0x10) == 0) && !restoreLine.isEmpty())
                {
                    ret.add(String.format(indentedLineFormat, restoreLine));
                    restoreLine = "";
                }
                restoreLine += String.format("LONG(" + numFormat + "); ", r.GetIntDataAtAddress(addr));
            }
            if(!restoreLine.isEmpty())
                ret.add(String.format(indentedLineFormat, restoreLine));
        }

        if(ret.size() == 1) // Remove line padding to full line and line continuation slash for the last line (NOTE: Can also be the first line for empty definitions)
            ret.set(ret.size() - 1, "\n" + ret.get(ret.size() - 1).trim());
        else
            ret.set(ret.size() - 1, ret.get(ret.size() - 1).trim());
        ret.add("\n");
        return CatStringList(ret);
    }

    private static String genLinkerMacro(String elementNameAndComment, boolean architecture16Bit, int literalDataToStore[])
    {
        int longCharWidth = architecture16Bit ? 6 : 8;
        int lineFormatWidth = 80;
        List<String> ret = new ArrayList<>();
        int indentWidth = 4;
        String numFormat = "0x%0" + String.valueOf(longCharWidth) + "X";
        String lineFormat = "\n%-" + String.valueOf(lineFormatWidth) + "s";
        String indentedLineFormat = "\\\n    %-" + String.valueOf(lineFormatWidth - 4) + "s";
        int wordsPerLine = 4;

        if(elementNameAndComment.length() > lineFormatWidth)
        {
            lineFormatWidth = elementNameAndComment.length() + 16 - (elementNameAndComment.length() % 16);
            lineFormat = "\n%-" + String.valueOf(lineFormatWidth) + "s";
            indentedLineFormat = String.format("\\\n%" + String.valueOf(indentWidth) + "s", "") + "%-" + String.valueOf(lineFormatWidth - indentWidth) + "s";
        }

        ret.add(String.format(lineFormat, String.format("#define " + elementNameAndComment)));
        for(int i = 0; i < literalDataToStore.length; i += wordsPerLine)
        {
            String s = "";
            for(int j = 0; j < wordsPerLine; j++)
            {
                if(i + j >= literalDataToStore.length)
                    break;
                s += String.format("LONG(" + numFormat + "); ", literalDataToStore[i + j]);
            }
            ret.add(String.format(indentedLineFormat, s));
        }

        if(ret.size() == 1) // Remove line padding to full line and line continuation slash for the last line (NOTE: Can also be the first line for empty definitions)
            ret.set(ret.size() - 1, "\n" + ret.get(ret.size() - 1).trim());
        else
            ret.set(ret.size() - 1, ret.get(ret.size() - 1).trim());
        ret.add("\n");
        return CatStringList(ret);
    }
}
