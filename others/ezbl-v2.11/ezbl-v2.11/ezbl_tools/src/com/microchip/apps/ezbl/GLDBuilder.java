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
import java.util.*;
import java.util.regex.*;


/**
 *
 * @author C12128
 */
public class GLDBuilder
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        String gldContents;
        TextBlock deviceGLD = null;
        String dynamicSectionsHeader = "", dynamicSections = "", dynamicSectionsFooter = "";

        String equDefineSectionTagsRegexStart = "AUTOMATIC EQU/DEFINE FILL SECTION[^\n]*?\n";
        String equDefineSectionTagsRegexEnd = "(?<=\n)[ \t]*?/[*][^E\n]*END OF EQU/DEFINE FILL SECTION";
        String deviceSectionFillSectionTagsRegexStart = "AUTOMATIC DEVICE SECTION FILL SECTION[^\n]*?\n";
        String deviceSectionFillSectionTagsRegexEnd = "(?<=\n)[ \t]*?/[*][^E\n]*END OF DEVICE SECTION FILL SECTION";

        // Obtain the given command line options and all prior ezbl_tools invocation state information
        String stateSavePath = CommandAndBuildState.GetStateFilePath(args);
        EZBLState state = EZBLState.ReadFromFile(stateSavePath);        // Load up the preexisting state file from disk, if one exists
        state = CommandAndBuildState.ReadArgs(state, args);             // Parse any new command line options and save them

        if(state.undecodedOptions.contains("-make_non_ezbl_gld"))
        {
            System.exit(CreateNonEZBLLinkerScripts(args));
        }

        // If nothing is specified upon execution or a pasing error occured, write usage information to STDOUT
        if(!state.parseOkay)
        {
            System.out.print("\r\n"
                             + Multifunction.FormatHelpText(79, 0 * 3, "Usage:")
                             + Multifunction.FormatHelpText(79, 1 * 3, "java -jar ezbl_tools.jar --gldbuilder -mcpu=PIC [-options] \"path to XC16 bins\" \"linker_script.gld\"")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 1 * 3, "Inputs:")
                             + Multifunction.FormatHelpText(79, 2 * 3, "Target PIC part number: ex: -mcpu=33EP64MC506, -mcpu=24FJ128GA010,-mcpu=33FJ256GP710A")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "Path to the XC16 bin installation folder for device .gld searching. Ex: \"C:\\Program Files (x86)\\Microchip\\xc16\\v1.33\\bin\"")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "Preexisting linker script (ex: ezbl_build_standalone.gld) to insert parsed device specific linker script information into.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 1 * 3, "Options:")
                             + Multifunction.FormatHelpText(79, 2 * 3, "-ignore=0x002000,0x3000")
                             + Multifunction.FormatHelpText(79, 3 * 3, "Address range to drop from the device Flash MEMORY regions in the linker script. Typically, this is set to a bootloader start address for the first paramter and a bootloader end address for the second end parameter. The second parameter is exclusive, or 1 address greater than the actual ignore range. This option may be repeated if multiple address ranges should be ignored.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 1 * 3, "Outputs:")
                             + Multifunction.FormatHelpText(79, 2 * 3, "Appropriately tagged sections in the preexisting ezbl .gld linker script are updated to reflect the information in the current device .gld file.")
                             + "\r\n"
            );

            return;
        }

        // Validate input parameters
        if((state.partNumber == null) || (!state.MCU16Mode && (state.linkScriptPath == null)) || (state.MCU16Mode && (state.compilerLinkScriptPath == null) && (state.linkScriptPath == null)))
        {
            System.err.println("ezbl_tools: Missing required input parameter: need a device part number, compiler linker script to start from, and output linker script file specified.");
            System.exit(-1);
        }

        // Read in the project's .gld/.ld file that we will be reading and likely editing
        gldContents = Multifunction.ReadFile(state.linkScriptPath, true);
        if(gldContents == null)
        {
            System.err.println("ezbl_tools: Unable to open \"" + state.linkScriptPath + "\". Ensure project contains valid EZBL .gld or .ld linker script.");
            System.exit(-2);
        }

        //if(state.MCU16Mode)
        //{
        //    makeMCU16LinkerScript(state);
        //    return;
        //}
        // Convert .gld/.ld intput byte stream into a parsable TextBlock
        TextBlock outGLD = new TextBlock(gldContents);

        // Read the compiler's device-specific linker script contents into the deviceGLD TextBlock class
        if(state.compilerLinkScriptPath != null)
        {
            gldContents = Multifunction.ReadFile(state.compilerLinkScriptPath, true);
            if(gldContents == null)
            {
                System.err.println("ezbl_tools: Unable to read \"" + state.compilerLinkScriptPath + "\"");
                System.exit(-3);
            }
            deviceGLD = new TextBlock(gldContents);
            state.compilerGLDContents = gldContents; // Also save a copy in the app state so we don't have to read it again in a later invocation
        }

        if(deviceGLD.Find("#define __EZBL_TOOLS_COMPUTED_TABLE_SIZE_CORRECTION_FILLER[^\n]{2,}?[\n]"))
        {
            deviceGLD.ReplaceOuter("");
        }

        // Process all SECTIONS regions
        while((deviceGLD != null) && deviceGLD.Find("SECTIONS[^{]*", "\\{", "\\}", "\n"))
        {
            if(dynamicSectionsHeader.isEmpty())
            {
                dynamicSectionsHeader = deviceGLD.GetOuterLeft(false);
                dynamicSectionsFooter = deviceGLD.GetOuterRight(false);
            }
            TextBlock sectionBlock = new TextBlock(deviceGLD.GetInner(true));

            // Find and delete .text.EZBLTableSizeCorrectionFiller section, if it exists. We don't want it in first pass linking as it might lead to progressively more data being generated to stabilize the project's size
            if(sectionBlock.Find("\\s[.]text[.]EZBLTableSizeCorrectionFiller[^:]*:[^{]*", "\\{", "\\}", "\n"))
            {
                sectionBlock.ReplaceOuter("\n");
            }

            // Build a new .ivt Interrupt Vector Table section that points to Application Interrupt Goto Table (IGT) entries, or through a Bootloader ISR muxing stub if the Bootloader needs the interrupt vector
            state.erasableIGT = "";
            if(state.remapISRThroughIGT && state.MCU16Mode)
            {
                if(sectionBlock.Find("\\s[.]ivt[^:]*:[^{]*", "\\{", "\\}", "\n"))
                {
                    List<String> ivtLines = new ArrayList<>();
                    List<String> igtLines = new ArrayList<>();
                    igtLines.add("\n      #define GOTO(address)        LONG(0x040000 | (ABSOLUTE(address) & 0x00FFFF)); LONG(0x000000 | ((ABSOLUTE(address) & 0x7F0000)>>16))");
                    igtLines.add("\n      #define BRA(short_address)   LONG(0x370000 | (((ABSOLUTE(short_address) - ABSOLUTE(. + 2))>>1) & 0x00FFFF))");

                    // Find longest strings for padding purposes
                    int longestVectorName = 0;
                    int longestVectorDesc = 0;
                    for(int i = 0; i < state.ivtVectors.size(); i++)
                    {
                        InterruptVector vector = state.ivtVectors.get(i);
                        if(vector.name.length() > longestVectorName)
                            longestVectorName = vector.name.length();
                        if(vector.desc.length() > longestVectorDesc)
                            longestVectorDesc = vector.desc.length();
                    }
                    if(longestVectorName > 31)  // Max at 32 total characters so we don't go overboard when someone else went overboard naming the vector
                        longestVectorName = 31;
                    if(longestVectorDesc > 40)
                        longestVectorDesc = 40;
                    String vecNamePad = String.valueOf(longestVectorName);
                    String vecDescPad = String.valueOf(longestVectorDesc);

                    // Create the new IVT and IGT, skipping IGT entry generation when the hardware doesn't implement any interrupt for the IVT entry
                    int outputIndex = 0;
                    for(int i = 0; i < state.ivtVectors.size(); i++)
                    {
                        InterruptVector vector = state.ivtVectors.get(i);
                        String formattedVectorName = String.format("_%1$-" + vecNamePad + "s", vector.name);
                        String vecComment = String.format("/* IRQ %2$3d, Vector %3$3d: %1$-" + vecDescPad + "s */", vector.desc, i - 8, i);
                        if(i < 8)
                            vecComment = String.format("/* Trap %2$2d, Vector %3$3d: %1$-" + vecDescPad + "s */", vector.desc, i, i);

                        if(!vector.implemented)
                        {
                            String s = String.format("\n      LONG(ABSOLUTE(_EZBL_IGT_BASE) + 0x%1$04X);", state.ivtVectorsImplemented * state.igtSpacing);
                            ivtLines.add(String.format("%1$-" + String.valueOf(longestVectorName * 2 + 100) + "s  %2$s", s, vecComment));
                        }
                        else
                        {   // Generate IGT entry for Application
                            igtLines.add(String.format("\n      %1$s(DEFINED(_%2$s) ? ABSOLUTE(_%2$s) : ABSOLUTE(__DefaultInterrupt));", state.igtSpacing == 0x2 ? "BRA" : "GOTO", formattedVectorName));

                            // Generate IVT line for Bootloader
                            ivtLines.add(String.format("\n      LONG(DEFINED(EZBL_Dispatch%1$s) ? ABSOLUTE(EZBL_Dispatch%1$s) : ABSOLUTE(_EZBL_IGT_BASE) + 0x%2$04X);  %3$s", formattedVectorName, outputIndex++ * state.igtSpacing, vecComment));
                        }
                    }

                    // Create the last IGT goto entry for unimplemented hardware ivtVectors that just points to the __DefaultInterrupt (in case if ever a vector is ever missed in the device files for a new device)
                    igtLines.add(String.format("\n      %1$s(ABSOLUTE(__DefaultInterrupt));", state.igtSpacing == 0x2 ? "BRA" : "GOTO"));

                    // Format .ivt and retain other text near it
                    String newIVT = sectionBlock.GetOuterLeft(true).replaceAll("\n(?! )", "\n  ") + "{" + CatStringList(ivtLines) + "\n  " + sectionBlock.GetOuterRight(false);

                    // Update .ivt to point to bootloader space or delete it
                    sectionBlock.ReplaceOuter(newIVT);
                    state.erasableIGT = CatStringList(igtLines);
                }
            }

            if(state.deleteResetSection)
            {
                // Find and delete .reset section. We will be using our own.
                if(sectionBlock.Find("\\s[.]reset[^:]*:[^{]*", "\\{", "\\}", "\n"))
                {
                    sectionBlock.ReplaceOuter("\n");
                }
            }
            dynamicSections += sectionBlock.GetFullBlock();
            deviceGLD.DeleteOuter();
        }

        // If an encryption password was supplied, save it in the project so it
        // can be used at run time to decode .blobs encrypted with the same
        // password
        if(state.encryptionKey != null)
        {
            String saltString = "";
            for(int i = 0; i < 16; i++)
            {
                saltString += String.format("%1$02X", state.encryptionSalt[i]);
            }

            dynamicSections += String.format("\n  /* Encryption password: %1$s */ ", state.encryptionPassword)
                               + "\n  /* Encryption salt: " + saltString + " */"
                               + "\n  EZBL_CryptKey :"
                               + "\n  {"
                               + "\n      _EZBL_CryptKey = ABSOLUTE(.);"
                               + "\n      ";
            for(int i = 0; i < 15; i += 3)
            {
                dynamicSections += String.format("LONG(0x%3$02X%2$02X%1$02X); ", state.encryptionKey[i], state.encryptionKey[i + 1], state.encryptionKey[i + 2]);
            }
            dynamicSections += String.format("LONG(0x%3$02X%2$02X%1$02X); ", state.encryptionKey[15], 0x00, 0x00);
            dynamicSections += "\n  } >" + state.mainFlashRegion.name
                               + "\n";
        }

        // Append header and footer closing braces and extras
        dynamicSections = dynamicSectionsHeader + dynamicSections + dynamicSectionsFooter;

        // AUTOMATIC EQU/DEFINE FILL SECTION
        if(state.MCU16Mode && outGLD.Find(equDefineSectionTagsRegexStart, equDefineSectionTagsRegexEnd))
        {
            List<String> deviceParameters = new ArrayList<>();
            deviceParameters.add(String.format("_EZBL_ADDRESSES_PER_SECTOR      = 0x%1$06X;\n", state.getEraseBlockSizeAddresses()));
            deviceParameters.add(String.format("_EZBL_MAIN_FLASH_BASE           = 0x%1$06X;\n", state.mainFlashRegion.startAddr));
            deviceParameters.add(String.format("_EZBL_MAIN_FLASH_END_ADDRESS    = 0x%1$06X;\n", state.mainFlashRegion.endAddr));
            deviceParameters.add(String.format("_EZBL_CONFIG_BASE               = 0x%1$06X;\n", state.configWordsRegion == null ? 0 : state.configWordsRegion.startAddr));
            deviceParameters.add(String.format("_EZBL_CONFIG_END_ADDRESS        = 0x%1$06X;\n", state.configWordsRegion == null ? 0 : state.configWordsRegion.endAddr));
            deviceParameters.add(String.format("_EZBL_DEVID_ADDRESS             = 0x%1$06X;\n", state.devIDAddr));
            deviceParameters.add(String.format("_EZBL_DEVID_MASK                = 0x%1$06X;\n", state.devIDMask));
            deviceParameters.add(String.format("_EZBL_DEVID_VALUE               = 0x%1$06X;\n", state.devIDValue));
            deviceParameters.add(String.format("_EZBL_REVID_ADDRESS             = 0x%1$06X;\n", state.devRevAddr));
            deviceParameters.add(String.format("_EZBL_REVID_MASK                = 0x%1$06X;\n", state.devRevMask));
            deviceParameters.add(String.format("_EZBL_RESERVED_BIT_ADDRESS      = 0x%1$06X;   %2$s\n", state.devSpecialConf.reservedBitAddr, state.devSpecialConf.reserveBitConfigName == null ? "" : "/* " + state.devSpecialConf.reserveBitConfigName + " */"));
            deviceParameters.add(String.format("_EZBL_RESERVED_BIT_MASK         = 0x%1$06X;\n", state.devSpecialConf.reservedBitMask));
            deviceParameters.add(String.format("_EZBL_CODE_PROTECT_ADDRESS      = 0x%1$06X;   %2$s\n", state.devSpecialConf.codeProtectAddr, "/* " + state.devSpecialConf.codeProtectConfigName + " */"));
            deviceParameters.add(String.format("_EZBL_CODE_PROTECT_MASK         = 0x%1$06X;\n", state.devSpecialConf.codeProtectMask));
            deviceParameters.add(String.format("_EZBL_BACKBUG_ADDRESS           = 0x%1$06X;   %2$s\n", state.devSpecialConf.BACKBUGAddr, "/* " + state.devSpecialConf.BACKBUGConfigName + " */"));
            deviceParameters.add(String.format("_EZBL_BACKBUG_MASK              = 0x%1$06X;\n", state.devSpecialConf.BACKBUGMask));

            for(MemoryRegion p : state.devConfigWordsByAddr.values())
            {
                deviceParameters.add(String.format("_EZBL_%1$-25s = 0x%2$06X;\n", p.name, p.startAddr));
            }

            // Commented out things that aren't used anymore
            outGLD.ReplaceInner(
                    deviceGLD.GetFullBlock()
                    + "\n"
                    + "\n"
                    + Multifunction.CatStringList(deviceParameters)
                    + "\n"
                    + String.format("#define __EZBL_PROGRAM_BASE       0x%1$06X\n", state.mainFlashRegion.startAddr)
                    + String.format("#define __EZBL_PROGRAM_LENGTH     0x%1$06X\n", state.mainFlashRegion.endAddr - state.mainFlashRegion.startAddr)
                    + String.format("#define __EZBL_PROGRAM_ERASE_SIZE 0x%1$06X\n", state.eraseBlockSizeAddresses)
                    + String.format("#define __EZBL_BASE_ADDRESS       0x%1$06X\n", state.baseAddress)
                    + String.format("#define __EZBL_IGT_ADDRESSES      0x%1$06X", state.remapISRThroughIGT ? 0x4 * (state.ivtVectorsImplemented + 1) : 0)
                    + " /* Number of Flash addresses needed to store all Interrupt Goto Table entries, including one for the __DefaultInterrupt */\n"
                    + "\n");
        }
        else if(state.MCU16Mode)
        {
            // If we don't find anything, reset the search locations so that subsequent sections can still be found
            outGLD.ResetFind();
            System.err.println("ezbl_tools: Unable to find AUTOMATIC EQU/DEFINE FILL SECTION in " + state.linkScriptPath + "; skipping insertion of this section. Ensure correct linker script in use and marker lines are present.");
        }

        // Don't ResetFind() - section area must always be at the end of the file, so we need not search all the prior text
        // AUTOMATIC DEVICE SECTION FILL SECTION
        if(state.MCU16Mode && outGLD.Find(deviceSectionFillSectionTagsRegexStart, deviceSectionFillSectionTagsRegexEnd))
        {
            outGLD.ReplaceInner(dynamicSections);
        }
        else if(state.MCU16Mode)
        {
            // If we don't find anything, reset the search locations so that subsequent sections can still be found
            outGLD.ResetFind();
            System.err.println("ezbl_tools: Unable to find AUTOMATIC DEVICE SECTION FILL SECTION in " + state.linkScriptPath + "; skipping insertion of this section. Ensure correct linker script in use and marker lines are present.");
        }

        // Modify the requested output.gld file with our new linker information
        if(Multifunction.UpdateFileIfDataDifferent(state.linkScriptPath, outGLD.GetFullBlock(), true) < 0)
        {
            System.err.println("ezbl_tools: failed to write to \"" + state.linkScriptPath + "\"");
        }

        state.SaveToFile();
    }

    public static String makeMCU16LinkerScript(EZBLState state)
    {
        String compilerGLD;
        String ret;
        List<String> ivtLines = new ArrayList<>();
        List<String> igtLines = new ArrayList<>();
        List<String> equates = new ArrayList<>();
        List<String> macros = new ArrayList<>();

        // Validate input parameters
        if((state.partNumber == null) || (!state.MCU16Mode && (state.linkScriptPath == null)) || (state.MCU16Mode && (state.compilerLinkScriptPath == null) && (state.linkScriptPath == null)))
        {
            System.err.println("ezbl_tools: Missing required input parameter: need a device part number, compiler linker script to start from, and output linker script file specified.");
            System.exit(-1);
        }

        // Read in the project's .gld/.ld file that we will be reading and likely editing
        ret = Multifunction.ReadFile(state.linkScriptPath, true);
        if(ret == null)
        {
            System.err.println("ezbl_tools: Unable to open \"" + state.linkScriptPath + "\". Ensure project contains valid EZBL linker script.");
            System.exit(-2);
        }

        // Update project .gld to match needed data from compiler .gld and add our special items
        compilerGLD = Multifunction.ReadFile(state.compilerLinkScriptPath, true);
        if(compilerGLD == null)
        {
            System.err.println("ezbl_tools: Unable to read \"" + state.compilerLinkScriptPath + "\"");
            System.exit(-3);
        }

        // Generate a new .ivt and application .igt section 
        ivtLines.add("  /* EZBL Bootloader project: redirect interrupt vectors to Application Interrupt Goto Table */\n"
                     + "  .ivt __IVT_BASE : \n"
                     + "  {");
        igtLines.add("#else\n"
                     + "  /* EZBL Application project: receive interrupts from Bootloader and branch to ISR */\n"
                     + "  .igt _EZBL_IGT_BASE : \n"
                     + "  {");

        // Find longest strings for padding purposes
        int longestVectorName = 0;
        int longestVectorDesc = 0;
        for(int i = 0; i < state.ivtVectors.size(); i++)
        {
            InterruptVector vector = state.ivtVectors.get(i);
            if(vector.name.length() > longestVectorName)
                longestVectorName = vector.name.length();
            if(vector.desc.length() > longestVectorDesc)
                longestVectorDesc = vector.desc.length();
        }
        if(longestVectorName > 31)  // Max at 32 total characters so we don't go overboard when someone else went overboard naming the vector
            longestVectorName = 31;
        if(longestVectorDesc > 40)
            longestVectorDesc = 40;
        String vecNamePad = String.valueOf(longestVectorName);
        String vecDescPad = String.valueOf(longestVectorDesc);

        // Create the new IVT and IGT, skipping IGT entry generation when the hardware doesn't implement any interrupt for the IVT entry
        int outputIndex = 0;
        for(int i = 0; i < state.ivtVectors.size(); i++)
        {
            InterruptVector vector = state.ivtVectors.get(i);
            String formattedVectorName = String.format("_%-" + vecNamePad + "s", vector.name);
            String vecComment = String.format("/* IRQ %2$3d, Vector %3$3d: %1$-" + vecDescPad + "s */", vector.desc, i - 8, i);
            if(i < 8)
                vecComment = String.format("/* Trap %2$2d, Vector %3$3d: %1$-" + vecDescPad + "s */", vector.desc, i, i);

            if(!vector.implemented)
            {
                String s = String.format("    LONG(ABSOLUTE(_EZBL_IGT_BASE) + 0x%04X);", state.ivtVectorsImplemented * state.igtSpacing);
                ivtLines.add(String.format("%-" + String.valueOf(longestVectorName * 2 + 98) + "s %s", s, vecComment));
            }
            else
            {   // Generate IGT entry for Application
                igtLines.add(String.format("    %1$s(DEFINED(_%2$s) ? ABSOLUTE(_%2$s) : ABSOLUTE(__DefaultInterrupt));", state.igtSpacing == 0x2 ? "BRA" : "GOTO", formattedVectorName));

                // Generate IVT line for Bootloader
                ivtLines.add(String.format("    LONG(DEFINED(EZBL_Dispatch%1$s) ? ABSOLUTE(EZBL_Dispatch%1$s) : ABSOLUTE(_EZBL_IGT_BASE) + 0x%2$04X);  %3$s", formattedVectorName, outputIndex++ * state.igtSpacing, vecComment));
            }
        }
        ivtLines.add("  } > ivt");

        // Create the last IGT goto entry for unimplemented hardware ivtVectors that just points to the __DefaultInterrupt (in case if ever a vector is ever missed in the device files for a new device)
        igtLines.add(String.format("    %1$s(ABSOLUTE(__DefaultInterrupt));", state.igtSpacing == 0x2 ? "BRA" : "GOTO"));
        igtLines.add("  } > program\n"
                     + "#endif");

        // Update .ivt to point to bootloader space or delete it
        state.erasableIGT = CatStringList(igtLines);

        macros.add("/**\n"
                   + " * EZBL Generated Information\n"
                   + " */");
        macros.add("#define GOTO(address)             LONG(0x040000 | (ABSOLUTE(address) & 0x00FFFF)); LONG(0x000000 | ((ABSOLUTE(address) & 0x7F0000)>>16))");
        macros.add("#define BRA(short_addr)           LONG(0x370000 | (((ABSOLUTE(short_addr) - ABSOLUTE(. + 2))>>1) & 0x00FFFF))");
        macros.add("#define BTSC(address, bit)        LONG(0xAF0000 | (ABSOLUTE(address) & 0x1FFE) | ((ABSOLUTE(bit) & 0x7)<<13) | ((ABSOLUTE(bit) & 0x8)>>3))");
        macros.add("\n\n\n");
        macros.add(String.format("#define __EZBL_PROGRAM_BASE       0x%06X", state.mainFlashRegion.startAddr));
        macros.add(String.format("#define __EZBL_PROGRAM_LENGTH     0x%06X", state.mainFlashRegion.endAddr - state.mainFlashRegion.startAddr));
        macros.add(String.format("#define __EZBL_PROGRAM_ERASE_SIZE 0x%06X", state.eraseBlockSizeAddresses));
        macros.add(String.format("#define __EZBL_BASE_ADDRESS       0x%06X", state.baseAddress));
        macros.add(String.format("#define __EZBL_IGT_ADDRESSES      0x%06X", state.remapISRThroughIGT ? 0x4 * (state.ivtVectorsImplemented + 1) : 0));

        // AUTOMATIC EQU/DEFINE FILL SECTION
        equates.add(String.format("_EZBL_appBootloadState          = 0x%06X;", state.baseAddressOfAppErasable));
        equates.add(String.format("_EZBL_APP_RESET_BASE            = 0x%06X;", state.baseAddressOfGotoReset));
        equates.add(String.format("_EZBL_IGT_BASE                  = 0x%06X;", state.baseAddressOfIGT));
        equates.add(String.format("_EZBL_ADDRESSES_PER_SECTOR      = 0x%06X;", state.getEraseBlockSizeAddresses()));
        equates.add(String.format("_EZBL_MAIN_FLASH_BASE           = 0x%06X;", state.mainFlashRegion.startAddr));
        equates.add(String.format("_EZBL_MAIN_FLASH_END_ADDRESS    = 0x%06X;", state.mainFlashRegion.endAddr));
        if(state.configWordsRegion != null)
        {
            equates.add(String.format("_EZBL_CONFIG_BASE               = 0x%06X;", state.configWordsRegion.startAddr));
            equates.add(String.format("_EZBL_CONFIG_END_ADDRESS        = 0x%06X;", state.configWordsRegion.endAddr));
        }
        equates.add(String.format("_EZBL_DEVID_ADDRESS             = 0x%06X;", state.devIDAddr));
        equates.add(String.format("_EZBL_DEVID_MASK                = 0x%06X;", state.devIDMask));
        equates.add(String.format("_EZBL_DEVID_VALUE               = 0x%06X;", state.devIDValue));
        equates.add(String.format("_EZBL_REVID_ADDRESS             = 0x%06X;", state.devRevAddr));
        equates.add(String.format("_EZBL_REVID_MASK                = 0x%06X;", state.devRevMask));
        if(state.devSpecialConf.reserveBitConfigName != null)
        {
            equates.add(String.format("_EZBL_RESERVED_BIT_ADDRESS      = 0x%1$06X;   /* %2$s */", state.devSpecialConf.reservedBitAddr, state.devSpecialConf.reserveBitConfigName));
            equates.add(String.format("_EZBL_RESERVED_BIT_MASK         = 0x%1$06X;", state.devSpecialConf.reservedBitMask));
        }
        equates.add(String.format("_EZBL_CODE_PROTECT_ADDRESS      = 0x%1$06X;   /* %2$s */", state.devSpecialConf.codeProtectAddr, state.devSpecialConf.codeProtectConfigName));
        equates.add(String.format("_EZBL_CODE_PROTECT_MASK         = 0x%1$06X;", state.devSpecialConf.codeProtectMask));
        equates.add(String.format("_EZBL_BACKBUG_ADDRESS           = 0x%1$06X;   /* %2$s */", state.devSpecialConf.BACKBUGAddr, state.devSpecialConf.BACKBUGConfigName));
        equates.add(String.format("_EZBL_BACKBUG_MASK              = 0x%1$06X;", state.devSpecialConf.BACKBUGMask));
        for(MemoryRegion p : state.devConfigWordsByAddr.values())
        {
            equates.add(String.format("_EZBL_%1$-25s = 0x%2$06X;", p.name, p.startAddr));
        }

        ret = compilerGLD;

        // Find the position of the first SECTIONS { } declaration block and insert our macros, equates, and other global stuff there
        Pattern p = Pattern.compile("(?m-is)^[\\s]*?SECTIONS[^{}]*?\\{");
        Matcher m = p.matcher(ret);
        if(!m.find())
        {
            System.err.printf("ezbl_tools.jar: unable to locate SECTIONS {...} in '%s'\n", state.linkScriptPath);
            System.exit(-4);
        }
        ret = ret.substring(0, m.start())
              + "\n\n"
              + CatStringList(macros, "\n")
              + "\n\n"
              + CatStringList(equates, "\n")
              + "\n\n\n"
              + ret.substring(m.start());

        // Remove .ivt and .aivt sections. We shall use our own.
        PairWithText secMapping = findOutputSectionMapping(ret, ".ivt");
        if(secMapping == null)
            System.out.printf("ezbl_tools.jar: did not locate .ivt section in '%s'\n", state.linkScriptPath);
        else
            ret = ret.substring(0, (int)secMapping.first) + ret.substring((int)secMapping.second);

        secMapping = findOutputSectionMapping(ret, ".aivt");
        if(secMapping != null)
            ret = ret.substring(0, (int)secMapping.first) + ret.substring((int)secMapping.second);

        // Qualify the .reset section inclusion based on if this is a Bootloader project or an Application Project
        secMapping = findOutputSectionMapping(ret, ".reset");
        if(secMapping == null)
        {
            System.err.printf("ezbl_tools.jar: unable to locate .reset section in '%s'\n", state.linkScriptPath);
            System.exit(-5);
        }
        ret = ret.substring(0, (int)secMapping.first)
              + "#if defined(EZBL_BOOT_PROJECT)"
              + ret.substring((int)secMapping.first, (int)secMapping.second).replaceAll("(?m)^#", "  #").replaceFirst("(?m)(?<=\\{)[^}]+(?=\\})",
                                                                                                                      "\n    GOTO(__reset)"
                                                                                                                      + "\n  ")
              + "\n"
              + CatStringList(ivtLines, "\n") + "\n"
              + CatStringList(igtLines, "\n") + "\n"
              + ret.substring((int)secMapping.second);

        state.SaveToFile();

        // Modify the requested output.gld file with our new linker information
        if(Multifunction.UpdateFileIfDataDifferent(state.linkScriptPath, ret, false) < 0)
        {
            System.err.println("ezbl_tools: failed to write to \"" + state.linkScriptPath + "\"");
            System.exit(-6);
        }

        return ret;
    }

    /**
     * Obtains the start index, end index, and matched text for the specified
     * output section text in a .gld/.ld linker script, including any preceeding
     * or following comments or preprocessor statements out to the first \n\n
     * pair in either direction (but only one such pair is returned)
     *
     * @param searchText
     * @param outputSectionName
     *
     * @return
     */
    public static PairWithText findOutputSectionMapping(String searchText, String outputSectionName)
    {
        if(!searchText.contains(outputSectionName))
            return null;

        Pattern p;
        Matcher m;
        char firstLetter = outputSectionName.charAt(0);
        String secName = "\\Q" + outputSectionName + "\\E";

        p = Pattern.compile("(?m-is)(?<=(\n\n))([^\\" + firstLetter + "{}]+?" + secName + "[^:]*?:[^{]*\\{[^}]*\\}[^>]*>[^\n]+\n([^\n}]+\n)*)");
        m = p.matcher(searchText);

        if(m.find())
        {
            return new PairWithText((long)m.start(), (long)m.end(), m.group(2));
        }

        p = Pattern.compile("(?m-is)(?:(([\\s]*?\\{[^\n]*?\n(?!\n))|(\n\n)))([^\\" + firstLetter + "{}]+?" + secName + "[^:]*?:[^{]*\\{[^}]*\\}[^>]*>[^\n]+\n([^\n}]+\n)*)");
        m = p.matcher(searchText);
        if(m.find())
        {
            return new PairWithText((long)m.start(), (long)m.end(), m.group(4));
        }

        return null;
    }

    /**
     * Generates an absolute assembly file containing the specified
     * application/bootloader bl2 encoded as literal, post-linked text for
     * encapsulation in another project. A suitable .gld linker script is also
     * generated.
     *
     * @param state State of the build process. Must be created in one of the
     *              other modules (ex: MakeEditor + GLDBuilder + DumpParser +
     *              Blobber chain)
     * @param bl2   Bl2b class to extract program data from
     *
     * @return New linker script text in index 0, plus asm (.sec) file text in
     *         index 1. Array length is always 2 for success, or null return for
     *         failure.
     */
    public static String[] CreateMergeScript(EZBLState state, Bl2b bl2)
    {
        // Items from the bl2
        TreeMap<String, Symbol> romSymbols = new TreeMap<>();
        List<Symbol> ramSymbols = new ArrayList<>();
        List<Symbol> exportableSymbols = new ArrayList<>();
        List<Symbol> symbolsConsumed = new ArrayList<>();

        String gldContents;

        List<PairWithText> gldPairs = new ArrayList<>();
        List<PairWithText> gldRAMSectionAllocations = new ArrayList<>();
        String gldROMSectionAllocation;
        String gldRAMSectionAllocation;

        // Items for the output .gld and .S files
        List<String> symbolTable = new ArrayList<>();
        List<String> exportTable = new ArrayList<>();
        List<String> asmMerge = new ArrayList<>();
        MemoryRegion mrEZBL_ForwardBootloaderISR = null;
        List<Section> romNoLoadSections = new ArrayList<>();
        List<MemoryRegion> romUseRegions;

        String[] retGLDASM = new String[2];

        if(state.elfDump == null || (state.romUseRegions == null))
        {
            // Validate input parameters
            if(state.partNumber == null || ((state.compilerGLDContents == null) && (state.compilerLinkScriptPath == null) && state.MCU16Mode))
            {
                if(!state.silent)
                {
                    System.err.println("ezbl_tools: Missing required input parameter. Device part number and compiler linker script path required.");
                }
                System.exit(-10);
            }
        }

        // Undo alignment padding if it results in use of the last instruction word of flash. We can't define 0xFFFFFF padding here without making the application project fail to link due to linker complaint when the last address is occupied.
        romUseRegions = state.romUseRegions;
        if(state.MCU16Mode)
        {
            MemoryRegion lastInstrWord = state.romUseRegions.get(0).clone();
            lastInstrWord.startAddr = state.mainExecutionRegion.endAddr - 0x2;
            lastInstrWord.endAddr = state.mainExecutionRegion.endAddr;
            romUseRegions = MemoryRegion.SubtractRegions(state.romUseRegions, lastInstrWord, false);
        }
        {
            // Expand bl2 ROM ranges to include all unused addresses that are
            // within bootloader space (so that they can't be reused by anything
            // during merge linking.
            for(MemoryRegion mr : romUseRegions)
            {
                bl2.FillData(mr, (byte)0xFF);
            }

            // Uncoalesce all records so that they fit in a named MEMORY region
            for(int i = 0; i < bl2.records.size(); i++)
            {
                DataRecord d = bl2.records.get(i);
                d.normalizePIC32Addresses();

                // Map each bl2 mr to a edc:ProgramSpace definition, splitting the bl2 section if needed, so that linking is possible
                Section dSec = new Section();
                dSec.loadMemoryAddress = d.address;
                dSec.size = d.data.length;
                dSec.isROM = true;
                dSec.data = d;
                dSec.flags.CODE = true;
                MemoryRegion mr = dSec.mapToDeviceRegion(state.devProgramSpaces, Partition.values()[state.targetPartition]);
                mr.alignToProgSize();
                d.comment = mr.comment;
                if((d.address >= mr.startAddr) && (d.address < mr.endAddr))
                {   // Found a matching ProgramSpace region that this section belongs in
                    if(d.getEndAddress() > mr.endAddr)
                    {
                        // Needs a split
                        DataRecord splitRecord = d.SplitAtAddress(mr.endAddr);
                        bl2.records.add(i + 1, splitRecord);
                    }
                    d.assignedMemory = mr.name;
                }
                else if((d.getEndAddress() > mr.startAddr) && (d.address <= mr.startAddr))
                {// Found opposite case where a region appears to be inside or overlap the data
                    // Needs a split
                    DataRecord splitRecord = d.SplitAtAddress(mr.startAddr);
                    bl2.records.add(i + 1, splitRecord);
                }

                // Throw away regions that only have 0xFFFFFF data in them (we
                // likely generated these in the first place as page keepout).
                byte sum = (byte)0xFF;
                for(byte dByte : d.data)
                {
                    sum &= dByte;
                }
                if(sum == (byte)0xFF)
                {
                    bl2.records.remove(d);
                    i--;    // Removed an element, don't increment loop counter this time
                    continue;
                }
            }

            // Find all global equates in each ROM or RAM section (no debugging sections)
            if(state.elfDump != null)
            {
                if((state.coreType == CPUClass.mm))
                {
                    state.elfDump.normalizePIC32Addresses();
                    Symbol gp = state.elfDump.symbolsByName.get("_gp");
                    if(gp == null)
                        System.err.println("ezbl_tools.jar: could not determine Bootloader's Global Pointer (_gp) address. Application projects will not be able to safely call Bootloader functions.");
                    else
                        exportableSymbols.add(gp);
                }

                // Throw away .text.EZBL_AppReservedHole* sections. These 
                // are used in Bootloader projects to force linking away from certain 
                // addresses so they are available when building the Application
                state.elfDump.removeSections("[.]text[.]EZBL_AppReserved.*");

                // Collect absolute _EZBL_* equates for application use. Ex: _EZBL_FORWARD_ISR_* definitions.
                for(Symbol s : state.elfDump.symbols)
                {
                    if(!s.isExportSuitable())
                        continue;

                    exportableSymbols.add(s);
                }

                // Built-in symbols and symbol prefixes that we shall exclude from export to the Application project via the .merge.S file
                List<String> noSymExportNames = Arrays.asList(
                        "__Size_block",
                        "__Aldata",
                        "__reset",
                        "__resetPRI",
                        "__psv_init");
                List<String> noSymExportNameStarts = Arrays.asList(
                        "__crt_start_mode",
                        "__data_init",
                        "_persist_",
                        "_sbss_",
                        "_sdata_",
                        "__pic32_data_init",
                        "_dinit",
                        "_on_reset",
                        "_on_bootstrap",
                        "_nmi_handler",
                        "_reset",
                        "_startup",
                        "__preinit_array",
                        "__init_array",
                        "__fini_array");

                // Remove project specified symbols for export via EZBL_NoExportSYM() macro calls
                List<String> noSymExportRegEx = new ArrayList<>();
                Section noSymExports = state.elfDump.sectionsByName.get(".info.EZBL_NoExportSYM");
                if((noSymExports != null) && (noSymExports.data != null) && (noSymExports.data.data.length != 0))
                {
                    noSymExportRegEx = Arrays.asList(new String(noSymExports.data.data).split("\0"));
                    for(int i = 0; i < noSymExportRegEx.size(); i++)
                    {
                        String noExport = noSymExportRegEx.get(i);
                        try
                        {
                            for(int j = 0; j < exportableSymbols.size(); j++)
                            {
                                Symbol s = exportableSymbols.get(j);
                                if(s.name.matches(noExport))
                                {
                                    if(state.verbose)
                                        System.out.printf("ezbl_tools.jar: suppressing export of '%s' due to .info.EZBL_NoExportSYM(\"%s\");\n", s.name, noExport);
                                    exportableSymbols.remove(s);
                                    j--;
                                }
                            }
                        }
                        catch(PatternSyntaxException ex)
                        {
                            System.err.printf("ezbl_tools.jar: found unrecognized regular expression '%s' in .info.EZBL_NoExportSYM, ignoring\n", noExport);
                            noSymExportRegEx.remove(i--);
                        }
                    }
                }

                // Remove project specified sections for export via EZBL_NoExportSection() macro calls
                Section noSectionExports = state.elfDump.sectionsByName.get(".info.EZBL_NoExportSection");
                List<String> noSectionExportRegEx = new ArrayList<>();
                if((noSectionExports != null) && (noSectionExports.data != null) && (noSectionExports.data.data.length != 0))
                {
                    noSectionExportRegEx = Arrays.asList(new String(noSectionExports.data.data).split("\0"));
                    for(int i = 0; i < noSectionExportRegEx.size(); i++)
                    {
                        String noSectionExport = noSectionExportRegEx.get(i);
                        try
                        {
                            if("".matches(noSectionExport))
                            {
                                // Just testing for exception - nothing to do
                            }
                        }
                        catch(PatternSyntaxException ex)
                        {
                            System.err.printf("ezbl_tools.jar: found unrecognized regular expression '%s' in .info.EZBL_NoExportSection, ignoring\n", noSectionExport);
                            noSectionExportRegEx.remove(i--);
                        }
                    }
                }

                // Collect RAM and ROM equates by section
                for(Section sec : state.elfDump.sections)
                {
                    // Only spend time processing RAM and ROM sections (no debugging)
                    if(!sec.isRAM && !sec.isROM)
                        continue;

                    if(sec.nameMatchesRegEx(noSectionExportRegEx))
                        continue;

                    // Generate a list of rom noload/NEVER_LOAD ranges typically used for data EEPROM emulation data
                    if(sec.isROM && sec.flags.ALLOC && sec.flags.NEVER_LOAD)
                    {
                        if((sec.name.startsWith("reserve_") && ((sec.loadMemoryAddress == 0xBFC00490L) || (sec.loadMemoryAddress == 0x9FC00490L) || (sec.loadMemoryAddress == 0x1FC00490L)))
                           || (sec.name.startsWith("reserve_") && ((sec.loadMemoryAddress == 0x8000000L) || (sec.loadMemoryAddress == 0xA000000L) || (sec.loadMemoryAddress == 0x0000000L))))
                        {   // Ignore PIC32 ICD debugging sections - we have our own sections defined to always block these regions
                            continue;
                        }

                        romNoLoadSections.add(sec);
                    }

                    sec.LoadSymbols(exportableSymbols);
                    for(int i = 0; i < sec.symbols.size(); i++)
                    {
                        Symbol sym = sec.symbols.get(i);

                        if(sec.isRAM && sym.name.endsWith("EZBL_ForwardBootloaderISR"))
                            mrEZBL_ForwardBootloaderISR = sec.mapToDeviceRegion(state.devMemories, null);

                        if(sym.nameMatchesRegEx(noSymExportRegEx) || sym.nameMatches(noSymExportNames) || sym.nameStartsWith(noSymExportNameStarts))
                        {
                            exportableSymbols.remove(sym);
                            continue;
                        }

                        if(sym.section.startsWith("."))
                        {
                            if(sym.section.equals(".init")
                               || sym.section.equals(".fini")
                               || sym.section.equals(".preinit_array")
                               || sym.section.equals(".init_array")
                               || sym.section.equals(".fini_array")
                               || sym.section.equals(".ctors")
                               || sym.section.equals(".dtors"))
                            {
                                exportableSymbols.remove(sym);
                                continue;
                            }
                        }
                        else if(sym.name.startsWith("__vector_dispatch_"))
                            sym.name = sym.name.replace("__vector_dispatch_", "EZBL_BOOT_vector_dispatch_");
                        else if(sym.name.equals("_general_exception_context"))
                            sym.name = "EZBL_BOOT_general_exception_context";
                        else if(sym.name.equals("_general_exception_handler"))
                            sym.name = "EZBL_BOOT_general_exception_handler";
                        else if(sym.name.equals("EZBL_general_exception"))
                            sym.name = "EZBL_BOOT_general_exception";
                        else if(sym.name.equals("EZBL_TrapHandler"))
                            sym.name = "EZBL_BOOT_TrapHandler";
                        else if(sym.name.equals("malloc") || sym.name.equals("calloc") || sym.name.equals("realloc") || sym.name.equals("free"))
                            sym.name = "EZBL_BOOT_" + sym.name;
                        else if(sym.name.startsWith("__alloc"))
                            sym.name = "EZBL_BOOT" + sym.name;
                        else if(sym.name.equals("_bootstrap_exception_handler"))
                            sym.name = "EZBL_BOOT__bootstrap_exception_handler";

                        // Place symbol into correct bin
                        if(sec.isRAM)
                            ramSymbols.add(sym);
                        else if(sec.isROM)
                            romSymbols.put(sym.name, sym);
                        else if(!sym.name.startsWith("EZBL") && !sym.name.startsWith("_EZBL"))
                            exportableSymbols.remove(sym);
                    }
                }

                // Sort by Symbol name for easy display
                Collections.sort(ramSymbols, new com.microchip.apps.ezbl.SymbolNameComparator());
            }

            // Build bootloader code/constants as an absolute assembly file
            asmMerge.add("/**"
                         + "\n * EZBL Bootloader Code and RAM Allocation"
                         + "\n *"
                         + "\n * Automatically generated file - not intended for manual editing. If changes "
                         + "\n * are made here, they will normally be overwritten when you rebuild your "
                         + "\n * Bootloader. If necessary, maintain a backup copy and manually merge your "
                         + "\n * customizations back in."
                         + "\n * "
                         + "\n * Built for:"
                         + "\n *     " + state.fullPartNumber + (state.dualPartitionMode ? " (Dual Partition mode)" : "")
                         + "\n * From:"
                         + "\n *     " + state.artifactPath
                         + "\n * Using build configuration:"
                         + "\n *     " + state.conf
                         + "\n * "
                         + "\n * Tool paths:"
                         + "\n *     " + Multifunction.GetCanonicalPath(state.compilerFolder + "/..")
                         + "\n *     " + Multifunction.GetCanonicalPath(state.pathToIDEBin + "/../..")
                         + "\n */"
                         + "\n"
                         + "\n ; Validate matching target processors between Bootloader and Application projects."
                         + "\n ; If you get this error and wish to force the two dissimilar targets together anyway,"
                         + "\n ; you can comment out these .error statements. However, something will likely be"
                         + "\n ; broken at run time, so do so only if you are sure of what you are doing."
                         + (state.MCU16Mode
                            ? "\n    .ifndef __" + state.partNumber
                              + "\n    .error \"Bootloader's " + state.projectName + ".merge.S/.gld files were generated for a different target processor.\""
                              + "\n    .error \"Recompile this Application project or the " + state.projectName + " Bootloader project for the same hardware.\""
                              + "\n    .endif"
                            : "\n#if !defined(__" + state.partNumber + "__)"
                              + "\n    .error \"Bootloader's " + state.projectName + ".merge.S file was generated for a different target processor.\""
                              + "\n    .error \"Recompile this Application project or the " + state.projectName + " Bootloader project for the same hardware.\""
                              + "\n#endif")
                         + "\n"
                         + "\n"
            );

            asmMerge.add(String.format("\n;----Target Bootloader ID and this Application Version meta data for .bl2 file header----"
                                       + "\n    .pushsection    .info.EZBL_metaParameters, info, keep"
                                       + "\n    .weak   _BOOTID_HASH0"
                                       + "\n    .weak   _BOOTID_HASH1"
                                       + "\n    .weak   _BOOTID_HASH2"
                                       + "\n    .weak   _BOOTID_HASH3"
                                       + "\n    .weak   _APPID_VER_BUILD"
                                       + "\n    .weak   _APPID_VER_MINOR"
                                       + "\n    .weak   _APPID_VER_MAJOR"
                                       + "\n_BOOTID_HASH0 = 0x%1$08X"
                                       + "\n_BOOTID_HASH1 = 0x%2$08X"
                                       + "\n_BOOTID_HASH2 = 0x%3$08X"
                                       + "\n_BOOTID_HASH3 = 0x%4$08X"
                                       + "\nEZBL_metaAppIDVerBuild:"
                                       + "\n    .long   _APPID_VER_BUILD"
                                       + "\nEZBL_metaAppIDVerMinor:"
                                       + "\n    .short  _APPID_VER_MINOR"
                                       + "\nEZBL_metaAppIDVerMajor:"
                                       + "\n    .short  _APPID_VER_MAJOR"
                                       + "\n    .popsection"
                                       + "\n", bl2.bootIDHash[0], bl2.bootIDHash[1], bl2.bootIDHash[2], bl2.bootIDHash[3]));

            // Reserve all the needed RAM ranges
            if(mrEZBL_ForwardBootloaderISR != null)
            {
                gldPairs.add(new PairWithText(mrEZBL_ForwardBootloaderISR.startAddr, mrEZBL_ForwardBootloaderISR.endAddr,
                                              String.format("\n#if defined(EZBL_HIDE_BOOT_SYMBOLS) /* EZBL_ForwardBootloaderISR exists here and cannot be hidden */"
                                                            + "\n  EZBL_RAM_AT_0x%1$04X 0x%1$04X :"
                                                            + "\n  {"
                                                            + "\n    *(EZBL_RAM_AT_0x%1$04X); /* [0x%1$04X, 0x%2$04X), contains %3$d bytes */"
                                                            + "\n  } >%4$s"
                                                            + "\n#endif"
                                                            + "\n", mrEZBL_ForwardBootloaderISR.startAddr, mrEZBL_ForwardBootloaderISR.endAddr, mrEZBL_ForwardBootloaderISR.endAddr - mrEZBL_ForwardBootloaderISR.startAddr, mrEZBL_ForwardBootloaderISR.name)));

                asmMerge.add("\n;----Bootloader reserved static RAM----"
                             + "\n#if defined(EZBL_HIDE_BOOT_SYMBOLS)"
                             + String.format("\n    ; Bootloader RAM block intended for %1$s region"
                                             + "\n    ; 0x%2$04X to 0x%3$04X, length 0x0004 (4 bytes)"
                                             + "\n    .pushsection    EZBL_RAM_AT_0x%2$04X, address(0x%2$04X), persist, keep", mrEZBL_ForwardBootloaderISR.name, mrEZBL_ForwardBootloaderISR.startAddr, mrEZBL_ForwardBootloaderISR.endAddr - mrEZBL_ForwardBootloaderISR.startAddr)
                             + "\n    .global     _EZBL_ForwardBootloaderISR"
                             + "\n    .type       _EZBL_ForwardBootloaderISR, @object"
                             + "\n_EZBL_ForwardBootloaderISR: ; This variable cannot be hidden since ISR dispatch code uses it"
                             + "\n    .space      0x4"
                             + "\n    .size       _EZBL_ForwardBootloaderISR, . - _EZBL_ForwardBootloaderISR"
                             + "\n    .popsection"
                             + "\n#endif"
                             + "\n");
            }
            Collections.sort(state.ramUseRegions);

            if(state.MCU16Mode && state.linkedAsDebugImage)
            {
                MemoryRegion mr = state.ramUseRegions.get(0);
                if(mr.startAddr + 0x50 != mr.endAddr)
                {
                    mr = mr.clone();
                    mr.startAddr -= 0x50;
                    mr.endAddr = mr.startAddr + 0x50;
                    state.ramUseRegions.add(0, mr);
                }
            }

            asmMerge.add("\n#if !defined(EZBL_HIDE_BOOT_SYMBOLS)");
            for(MemoryRegion mr : state.ramUseRegions)
            {
                asmMerge.add(mr.toASMString());
                gldRAMSectionAllocations.add(new PairWithText(mr.startAddr, mr.endAddr, mr.toLinkerString()));

                // Print the exported equates that are in this address mr
                for(Symbol s : ramSymbols)
                {
                    if((s.address >= mr.startAddr) && (s.address < mr.endAddr))
                    {
                        symbolsConsumed.add(s);
                        symbolTable.add(String.format("\n    .equ        %1$s, 0x%2$06X", s.name, s.address));
                        if(s.name.equals("_EZBL_ForwardBootloaderISR"))    // Do not weaken this always present/required symbol
                        {
                            exportTable.add(String.format("\n    .global     %1$s", s.name));
                        }
                        else
                        {
                            exportTable.add(String.format("\n    .weak       %1$s", s.name));
                        }
                        if(s.flags.function)
                        {
                            exportTable.add(String.format("\n    .type       %1$s, @function", s.name));
                        }
                        else if(s.flags.object)
                        {
                            exportTable.add(String.format("\n    .type       %1$s, @object", s.name));
                        }
                    }
                }
            }
            asmMerge.add("\n#endif");

            // Finally, locate the appBootloadState, hash and user flags, app reset vector, and leave space for IGT (created by linker script)
            if(state.MCU16Mode)
            {
                asmMerge.add("\n\n;----App erasable items that the Bootloader knows about----");
                asmMerge.add(String.format(""
                                           + "\n    .pushsection    EZBL_AppErasable, address(0x%1$06X), code, keep"
                                           + "\n    ; EZBL_appBootloadState - Base address of EZBL_INSTALLED_FLAGS structure (18 bytes/6 instruction words)"
                                           + "\n    .pword      0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0x12CDFF"
                                           + "\n"
                                           + "\n    ; _EZBL_APP_RESET_BASE"
                                           + "\n    .extern     __reset"
                                           + "\n    goto        __reset"
                                           + "\n"
                                           + "\n    ; Interrupt Goto Table follows for 0x%2$X addresses (0x%3$06X to 0x%4$06X)"
                                           + "\n    ; The .igt section is defined in the linker script since this is an application"
                                           + "\n    ; erasable/updatable structure."
                                           + "\n    .popsection",
                                           state.baseAddressOfAppErasable, state.sizeOfIGT, state.baseAddressOfIGT, state.baseAddressOfIGT + state.sizeOfIGT
                ));

                gldPairs.add(new PairWithText(state.baseAddressOfAppErasable, state.baseAddressOfIGT,
                                              String.format("\n  EZBL_AppErasable 0x%1$06X :"
                                                            + "\n  {"
                                                            + "\n    *(EZBL_AppErasable); /* [0x%1$06X, 0x%2$06X), contains %3$d bytes */"
                                                            + "\n  } >program"
                                                            + "\n", state.baseAddressOfAppErasable, state.baseAddressOfAppErasable + state.sizeOfAppErasable, state.sizeOfAppErasable / 2 * 3)));

                // Add .igt section to our list of GLD section mappings
                if((state.erasableIGT != null) && (!state.erasableIGT.isEmpty()))
                {
                    gldPairs.add(new PairWithText(state.baseAddressOfIGT, state.baseAddressOfIGT + state.sizeOfIGT,
                                                  String.format(
                                                          "\n  /* Application's erasable Interrupt Goto Table */"
                                                          + "\n  .igt 0x%1$06X :"
                                                          + "\n  { /* [0x%1$06X, 0x%3$06X), contains %4$d bytes */"
                                                          + "%2$s"
                                                          + "\n  } >program"
                                                          + "\n", state.baseAddressOfIGT, state.erasableIGT, state.baseAddressOfIGT + state.sizeOfIGT, state.sizeOfIGT / 2 * 3)));
                }
            }
            else    // PIC32MM
            {
                Symbol sym = state.elfDump.symbolsByName.get("EZBL_BOOTLOADER_SIZE");
                if(sym != null)
                {
                    asmMerge.add(String.format("\n\n;----Bootloader reserved flash space propagated from the bootloader project----"
                                               + "\n    .global EZBL_BOOTLOADER_SIZE"
                                               + "\nEZBL_BOOTLOADER_SIZE = 0x%08X"
                                               + "\n"
                                               + "\n", sym.address));
                }

                sym = state.elfDump.symbolsByName.get("EZBL_appBootloadState");
                asmMerge.add(String.format("\n\n;----App erasable items that the Bootloader knows about----"
                                           + "\n    .pushsection    .EZBL_appBootloadState, address(0x%08X), code, keep"
                                           + "\n    ; EZBL_appBootloadState - Base address of EZBL_INSTALLED_FLAGS structure (24 bytes/6 instruction words)"
                                           + "\n    .global EZBL_appBootloadState"
                                           + "\n    .global EZBL_appIDVer"
                                           + "\n    .type   EZBL_appBootloadState, @object"
                                           + "\n    .type   EZBL_appIDVer, @object"
                                           + "\nEZBL_appBootloadState:"
                                           + "\nEZBL_appIDVer:"
                                           + "\n    .long   _APPID_VER_BUILD    ; EZBL_appIDVer.appIDVerBuild"
                                           + "\n    .short  _APPID_VER_MINOR    ; EZBL_appIDVer.appIDVerMinor"
                                           + "\n    .short  _APPID_VER_MAJOR    ; EZBL_appIDVer.appIDVerMajor"
                                           + "\n    .size   EZBL_appIDVer, . - EZBL_appIDVer"
                                           + "\n    .long   0xFFFFFFFF          ; hash32 - not defined at compile time, set when run-time programmed"
                                           + "\n    .long   0xFFFFFFFF          ; CRC32  - not defined at compile time, set when run-time programmed"
                                           + "\n    .long   0xFFFF12CD          ; appInstalled == EZBL_APP_INSTALLED (0x12CD)"
                                           + "\n    .long   0xFFFFFFFF          ; padding to fit 3x flash double words"
                                           + "\n    .size   EZBL_appBootloadState, . - EZBL_appBootloadState"
                                           + "\n    .popsection"
                                           + "\n", sym.address));

                gldPairs.add(new PairWithText(state.baseAddressOfAppErasable, state.baseAddressOfIGT,
                                              String.format("\n  EZBL_appBootloadState 0x%1$08X :"
                                                            + "\n  {"
                                                            + "\n    KEEP(*(.EZBL_appBootloadState*)); /* [0x%1$08X, 0x%2$08X), contains %3$d bytes */"
                                                            + "\n  } >kseg0_program_mem"
                                                            + "\n", state.baseAddressOfAppErasable, state.baseAddressOfAppErasable + state.sizeOfAppErasable, state.sizeOfAppErasable)));
            }

            // Find the bootloader's .const section, assuming one exists
            if(state.MCU16Mode && (state.elfDump != null))
            {
                Section constSect = state.elfDump.romSectionMapByName.get(".const");
                // If bootloader had a .const section, we need to explicitly
                // generate a .const section consumer in the linker script for the
                // application that will match the same PSV page that the bootloader
                // used so const pointers can be passed between each other during
                // function calls. Otherwise, bad run-time bugs would happen due 
                // to PSVPAG/DSRPAG mismatch.
                if(constSect != null)
                {
                    MemoryRegion constRegion = new MemoryRegion(state.mainExecutionRegion.startAddr, (state.mainExecutionRegion.startAddr & ~0x7FFFL) + 0x8000L);
                    constRegion.type = MemType.ROM;
                    constRegion.startAddr = constSect.loadMemoryAddress & ~0x7FFFL;
                    constRegion.endAddr = constRegion.startAddr + 0x8000L;
                    constRegion.copyMetaData(constSect.mapToDeviceRegion(state.devProgramSpaces, Partition.values()[state.targetPartition]));
                    if(constRegion.endAddr > state.mainExecutionRegion.endAddr)
                        constRegion.endAddr = state.mainExecutionRegion.endAddr;

                    List<MemoryRegion> usedSpaces = new ArrayList<>();

                    // First search the bootloader .bl2 data so we know what
                    // addresses are already occupied on the PSV page. We ultimately
                    // need to find the biggest free block on the same PSV page 
                    // to put the application .const section.
                    for(DataRecord dr : bl2.records)
                    {
                        usedSpaces.add(dr.mapToDeviceRegion(state.devProgramSpaces, Partition.values()[state.targetPartition]));
                    }
                    usedSpaces = MemoryRegion.alignToEraseSize(usedSpaces);
                    usedSpaces = MemoryRegion.coalesce(usedSpaces, 0, 0, false);
                    MemoryRegion appErasableRegion = new MemoryRegion(state.baseAddressOfAppErasable, state.baseAddressOfAppErasable + state.sizeOfAppErasable);
                    appErasableRegion.type = MemType.ROM;
                    usedSpaces.add(appErasableRegion);
                    List<MemoryRegion> constPageHoles = MemoryRegion.SubtractRegions(constRegion.getAsList(), usedSpaces, false);
                    // Now locate biggest free hole on the 32KB/32K address PSV page
                    TreeMap<Long, MemoryRegion> holesMappedByWidth = new TreeMap<>();
                    for(MemoryRegion constHole : constPageHoles)
                    {
                        holesMappedByWidth.put(constHole.endAddr - constHole.startAddr, constHole);
                    }
                    MemoryRegion biggestConstHole = constRegion.clone();
                    biggestConstHole.endAddr = biggestConstHole.startAddr;
                    if(holesMappedByWidth.lastKey() != null)
                        biggestConstHole = holesMappedByWidth.lastEntry().getValue();
                    long constPageBiggestHoleSize = biggestConstHole.endAddr - biggestConstHole.startAddr;

                    gldPairs.add(new PairWithText(biggestConstHole.startAddr, biggestConstHole.endAddr,
                                                  String.format("\n#if !defined(EZBL_HIDE_BOOT_SYMBOLS)"
                                                                + "\n  /*"
                                                                + "\n  ** .const compatibility with Bootloader .const section"
                                                                + "\n  ** "
                                                                + "\n  ** Explicitly locate the .const section so the largest possible PSV "
                                                                + "\n  ** window is available for the Application project without using a "
                                                                + "\n  ** different PSVPAG/DSRPAG value than the Bootloader project requires "
                                                                + "\n  ** for API compatibility. "
                                                                + "\n  */"
                                                                + "\n  .const 0x%1$06X : AT(0x%2$06X)"
                                                                + "\n  {"
                                                                + "\n    LONG(0xFFFFFF);/* Dummy word to avoid empty .const section and prevent linker de-initializing CORCON<PSV>/DSRPAG when App starts */"
                                                                + "\n    *(.const);     /* 0x%3$06X (%3$d) available addresses/bytes for auto PSV constants */"
                                                                + "\n  } >program"
                                                                + "\n#endif"
                                                                + "\n", 0x8000 | (biggestConstHole.startAddr & 0x7FFF), biggestConstHole.startAddr, constPageBiggestHoleSize
                                                  )));
                    if(((constPageBiggestHoleSize < state.minFreePSVSpace) || (constPageBiggestHoleSize < state.warnFreePSVSpace)))
                    {
                        if(!state.silent)
                        {
                            long spaceNeeded = Math.max(Math.max(state.warnFreePSVSpace, state.minFreePSVSpace), state.eraseBlockSizeAddresses);
                            long alignedAppReservedEnd = constSect.loadMemoryAddress + (0x8000 - (constSect.loadMemoryAddress % 0x8000));
                            long alignedAppReservedStart = constSect.loadMemoryAddress + constSect.size;
                            if((alignedAppReservedStart % state.eraseBlockSizeAddresses) != 0)
                                alignedAppReservedStart += state.eraseBlockSizeAddresses - (alignedAppReservedStart % state.eraseBlockSizeAddresses);
                            for(MemoryRegion mr : state.devProgramSpaces)
                            {
                                if((constSect.loadMemoryAddress >= mr.startAddr) && (constSect.loadMemoryAddress + constSect.size <= mr.endAddr) && (alignedAppReservedEnd > mr.endAddr))
                                {
                                    alignedAppReservedEnd = mr.endAddr - 0x2;
                                    break;
                                }
                            }
                            long spaceMakable = alignedAppReservedEnd - alignedAppReservedStart;
                            while(spaceMakable > spaceNeeded + 0x1000)
                            {
                                alignedAppReservedStart += state.eraseBlockSizeAddresses;
                                spaceMakable = alignedAppReservedEnd - alignedAppReservedStart;
                            }

                            if(spaceMakable < spaceNeeded)
                            {   // Couldn't fullfil minimum requirements. See if we can if we start from the beginning of the PSV page, before the .const section
                                long alignedAppReservedEnd2 = constSect.loadMemoryAddress & ~state.eraseBlockSizeAddresses;
                                long alignedAppReservedStart2 = constSect.loadMemoryAddress & ~0x8000L;
                                if(alignedAppReservedStart2 < state.eraseBlockSizeAddresses)
                                    alignedAppReservedStart2 = state.eraseBlockSizeAddresses;
                                long spaceMakable2 = alignedAppReservedEnd2 - alignedAppReservedStart2;
                                if(spaceMakable2 > spaceMakable)
                                {
                                    while(spaceMakable2 > spaceNeeded + 0x1000)
                                    {
                                        alignedAppReservedEnd2 -= state.eraseBlockSizeAddresses;
                                        spaceMakable2 = alignedAppReservedEnd2 - alignedAppReservedStart2;
                                        if(spaceMakable2 > spaceNeeded + 0x1000)
                                        {
                                            alignedAppReservedStart2 += state.eraseBlockSizeAddresses;
                                            spaceMakable2 = alignedAppReservedEnd2 - alignedAppReservedStart2;
                                        }
                                    }
                                    alignedAppReservedStart = alignedAppReservedStart2;
                                    alignedAppReservedEnd = alignedAppReservedEnd2;
                                    spaceMakable = spaceMakable2;
                                }
                            }

                            if(spaceMakable < spaceNeeded)
                            {   // Can't fit either before or after the .const section, try to come up with something that straddles two PSV regions and occupies the .const region to force the linker to move .const onto the next PSV page
                                alignedAppReservedEnd = constSect.loadMemoryAddress + (0x8000 - (constSect.loadMemoryAddress % 0x8000)) + 0x8000;
                                alignedAppReservedStart = (constSect.loadMemoryAddress + constSect.size - 0x2);
                                alignedAppReservedStart -= alignedAppReservedStart % state.eraseBlockSizeAddresses;
                                while(alignedAppReservedEnd - alignedAppReservedStart > spaceNeeded + 0x1000)
                                {
                                    alignedAppReservedEnd -= state.eraseBlockSizeAddresses;
                                }
                                for(MemoryRegion mr : state.devProgramSpaces)
                                {
                                    if((constSect.loadMemoryAddress >= mr.startAddr) && (constSect.loadMemoryAddress + constSect.size <= mr.endAddr) && (alignedAppReservedEnd > mr.endAddr))
                                    {
                                        alignedAppReservedEnd = mr.endAddr - 0x2;
                                        break;
                                    }
                                }
                                spaceMakable = alignedAppReservedEnd - alignedAppReservedStart;
                            }

                            if(spaceMakable < spaceNeeded)
                            {   // Still can't find a solution. A solution may not exist, so just generate the min required. Device has too little flash or too many constants in the Bootloader to accomodate the check boundary.
                                alignedAppReservedStart = state.eraseBlockSizeAddresses;
                                alignedAppReservedEnd = alignedAppReservedStart + ((spaceNeeded % state.eraseBlockSizeAddresses == 0) ? spaceNeeded : spaceNeeded + (state.eraseBlockSizeAddresses - (spaceNeeded % state.eraseBlockSizeAddresses)));
                                for(MemoryRegion mr : state.devProgramSpaces)
                                {
                                    if((constSect.loadMemoryAddress >= mr.startAddr) && (constSect.loadMemoryAddress + constSect.size <= mr.endAddr) && (alignedAppReservedEnd > mr.endAddr))
                                    {
                                        alignedAppReservedEnd = mr.endAddr - 0x2;
                                        break;
                                    }
                                }
                            }

                            System.err.print("EZBL check " + ((constPageBiggestHoleSize < state.minFreePSVSpace) ? "error:\n" : "warning:\n")
                                             + Multifunction.FormatHelpText(
                                            String.format(".const found at 0x%1$06X; %2$d bytes free on PSV/EDS page\n"
                                                          + "\n"
                                                          + "If calling Bootloader APIs from an Application project, all constants must reside on the same PSV/EDS page. To avoid running out of PSV addresses in future Application projects, "
                                                          + "recompile this Bootloader project with space explicitly reserved for the Application using the EZBL_SetAppReservedHole() macro, ex:"
                                                          + "\n    EZBL_SetAppReservedHole(0x%3$06X, 0x%4$06X);"
                                                          + "\nTo change the checking thresholds for this diagnostic, specify the -min_free_psv=x and/or -warn_free_psv=y command line options.",
                                                          constSect.loadMemoryAddress, constPageBiggestHoleSize, alignedAppReservedStart, alignedAppReservedEnd), 110, 4, 0, false));
                            System.err.flush(); // Flush text now since we are going to cause a failure and don't want subsequent Make errors to print between our lines.
                        }
                        if(constPageBiggestHoleSize < state.minFreePSVSpace)
                        {
                            System.exit(-10);
                        }
                    }
                }
            }

            if(!romNoLoadSections.isEmpty())
            {
                asmMerge.add("\n\n\n;----Bootloader defined noload holes in program memory----");
                for(Section sec : romNoLoadSections)
                {
                    long byteLen = sec.size;
                    MemoryRegion mr = sec.mapToDeviceRegion(state.devProgramSpaces, Partition.values()[state.targetPartition]);
                    if(state.MCU16Mode)
                    {
                        byteLen = byteLen / 2 * 3;
                    }
                    asmMerge.add(String.format("\n"
                                               + "\n    ; Bootloader noload attributed hole intended for program region '%5$s'"
                                               + "\n    ; 0x%1$06X to 0x%2$06X, length 0x%3$06X (%4$d bytes; needs %6$d pages)"
                                               + "\n    .pushsection    %7$s, address(0x%1$06X), code, keep, noload"
                                               + "\n    .skip 0x%3$06X"
                                               + "\n    .popsection",
                                               sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size, sec.size, byteLen, mr.name, (byteLen - 1) / state.eraseBlockSizeBytes, sec.name));
                    gldPairs.add(new PairWithText(sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size, mr.toLinkerString("(NOLOAD)")));

                    // Print the exported equates that are in this address mr
                    for(Symbol s : sec.symbols)
                    {
                        symbolsConsumed.add(s);
                        symbolTable.add(String.format("\n    .equ        %1$s, 0x%2$06X", s.name, s.address));
                        exportTable.add(String.format("\n    .weak       %1$s", s.name));
                        if(s.flags.function)
                            exportTable.add(String.format("\n    .type       %1$s, @function", s.name));
                        else if(s.flags.object)
                            exportTable.add(String.format("\n    .type       %1$s, @object", s.name));
                    }
                }
            }

            asmMerge.add("\n\n\n;----Bootloader reserved program memory----");
            for(DataRecord d : bl2.records)
            {
                d.normalizePIC32Addresses();
                MemoryRegion mr = d.mapToDeviceRegion(state.devProgramSpaces, Partition.values()[state.targetPartition]);
                String secName = String.format("EZBL_ROM_AT_0x%06X", mr.startAddr);
                if(mr.type.equals(MemType.BYTEFUSE) || mr.type.equals(MemType.FLASHFUSE))
                    secName = "EZBL_BTLDR_CONFIG_WORD_" + mr.name;
                asmMerge.add(String.format("\n"
                                           + "\n    ; Bootloader code block intended for program region '%5$s'"
                                           + "\n    ; 0x%1$06X to 0x%2$06X, length 0x%3$06X (%4$d bytes; needs %6$d pages)"
                                           + "\n    .pushsection    " + secName + ", address(0x%1$06X), code, keep",
                                           d.address, d.getEndAddress(), d.getEndAddress() - d.address, d.data.length, d.assignedMemory, (d.data.length - 1) / state.eraseBlockSizeBytes));

                gldPairs.add(new PairWithText(d.address, d.getEndAddress(), mr.toLinkerString()));

                // Print the exported equates that are in this address mr
                List<Symbol> functionsHere = new ArrayList<>();
                for(Symbol s : romSymbols.values())
                {
                    if((s.address >= d.address) && (s.address < d.getEndAddress()))
                    {
                        symbolsConsumed.add(s);
                        if(s.flags.function)
                        {
                            if(state.MCU16Mode)
                            {
                                symbolTable.add(String.format("\n    .equ        %s, 0x%06X", s.name, s.address));
                                exportTable.add(String.format("\n    .type       %s, @function", s.name));
                            }
                            functionsHere.add(s);
                        }
                        else
                        {
                            symbolTable.add(String.format("\n    .equ        %s, 0x%06X", s.name, s.address));
                            if(s.flags.object)
                                exportTable.add(String.format("\n    .type       %s, @object", s.name));
                        }
                        exportTable.add(String.format("\n    .weak       %s", s.name));
                    }
                }

                // Print the code and constants in this memory range
                asmMerge.add("\n" + d.getDataAsASMCode(functionsHere));
                asmMerge.add("\n    .popsection");
            }

            // Print other absolute and EZBL equates
            exportableSymbols.removeAll(symbolsConsumed);
            Collections.sort(exportableSymbols, new com.microchip.apps.ezbl.SymbolNameComparator());
            for(Symbol s : exportableSymbols)
            {
                symbolTable.add(String.format("\n    .equ        %1$s, 0x%2$06X", s.name, s.address));
                exportTable.add(String.format("\n    .weak       %1$s", s.name));
                if(s.flags.function)
                {
                    exportTable.add(String.format("\n    .type       %1$s, @function", s.name));
                }
                else if(s.flags.object)
                {
                    exportTable.add(String.format("\n    .type       %1$s, @object", s.name));
                }
            }

            asmMerge.add("\n\n\n#if !defined(EZBL_HIDE_BOOT_SYMBOLS)");
            if(!exportTable.isEmpty())  // Print the export tables
            {
                asmMerge.add("\n\n;----Bootloader symbol export table----"
                             + CatStringList(exportTable)
                             + "\n");
            }
            if(!symbolTable.isEmpty())  // Print the symbol table
            {
                asmMerge.add("\n\n\n;----Bootloader symbol addresses----"
                             + CatStringList(symbolTable)
                             + "\n");
            }
            asmMerge.add("\n#endif");
            asmMerge.add("\n");

            // Read the given device .gld contents into the deviceGLD TextBlock class (note: this is the project .ld file for PIC32MM projects)
            gldContents = state.compilerGLDContents;
            if(gldContents == null)
            {
                String referenceLinkerFile = state.MCU16Mode ? state.compilerLinkScriptPath : state.linkScriptPath;
                gldContents = Multifunction.ReadFile(referenceLinkerFile, true);
                if(gldContents == null)
                {
                    if(!state.silent)
                    {
                        System.err.println("ezbl_tools: Unable to read \"" + referenceLinkerFile + "\"");
                    }
                    System.exit(-2);
                }
            }
            // Convert .gld intput byte stream into a parsable TextBlock
            TextBlock outGLD = new TextBlock(gldContents);
            String dynamicSectionsHeader = "", dynamicSections = "", dynamicSectionsFooter = "";

            // Sort all GLD section mappings by address for nice logical
            // appearance in the .gld output file and output as a single
            // string.
            if(!state.MCU16Mode)
                Collections.sort(gldPairs, new PairWithText.PIC32MMBootFirstAddrComparator());
            gldROMSectionAllocation = "";
            gldRAMSectionAllocation = "";
            for(PairWithText p : gldRAMSectionAllocations)
            {
                gldRAMSectionAllocation += p.text;
            }
            for(PairWithText p : gldPairs)
            {
                // Explicitly map all Bootloader sections before any
                // Application sections are placed
                gldROMSectionAllocation += p.text;
            }

            // Convert .gld intput byte stream into a parsable TextBlock
            // Search through all SECTIONS regions for things that need to be
            // deleted and to find the correct place to add things
            while(outGLD.Find("SECTIONS[^{]*?", "\\{", "\\}", "\n"))
            {
                if(dynamicSectionsHeader.isEmpty())
                {
                    dynamicSectionsHeader = outGLD.GetOuterLeft(false);
                    dynamicSectionsFooter = outGLD.GetOuterRight(false);
                }
                TextBlock sectionBlock = new TextBlock(outGLD.GetInner(true));

                if(state.MCU16Mode)
                {
                    // Find and delete .reset section. We will be using our own. This is
                    // also a good place to insert all our bootloader sections because
                    // it is the first thing the compiler's default linker scripts place
                    // (it is address 0x000000 afterall). Included here are
                    // EZBL_AppErasable, EZBL_ROM_AT_0xXXXXXX, .igt, and possibly other
                    // blocks.
                    if(sectionBlock.Find("\\s[.]reset[^:]*?:[^{]*?", "\\{", "\\}", "\n"))
                    {
                        sectionBlock.ReplaceOuter(gldRAMSectionAllocation + "\n" + gldROMSectionAllocation + "\n");
                    }

                    // Find and vaporize .ivt and .aivt sections since we are using our .igt generated section instead for App projects
                    if(sectionBlock.Find("\\s[.]ivt[^:]*?:[^{]*?", "\\{", "\\}", "\n"))
                        sectionBlock.ReplaceOuter("\n");
                    if(sectionBlock.Find("\\s[.]aivt[^:]*?:[^{]*?", "\\{", "\\}", "\n"))
                        sectionBlock.ReplaceOuter("\n");
                }
                else    // PIC32MM
                {
                    if(sectionBlock.Find("\\s\\.reset[^:]*?:[^{]*?", "\\{", "\\}", "\n"))
                        sectionBlock.InsertInner(gldROMSectionAllocation + "\n", true, true);
                    if(sectionBlock.Find("\\s\\.dbg_data[^:]*?:[^{]*?", "\\{", "\\}", "\n"))
                        sectionBlock.InsertInner(gldRAMSectionAllocation + "\n", false, false);
                }

                dynamicSections += sectionBlock.GetFullBlock();
                outGLD.DeleteOuter();
            }

            // Remove junk "/*\n** Interrupt Vector Table\n*/" comment from compiler .gld file if it is sitting around. We have our own IVT in the Bootloader and an IGT for this Application.
            dynamicSections = dynamicSections.replace("/*\n** Interrupt Vector Table\n*/\n", "");

            // Append header braces and extras
            dynamicSections = dynamicSectionsHeader + dynamicSections + dynamicSectionsFooter;
            retGLDASM[0] = outGLD.GetFullBlock().replaceFirst("(?dmus)(/\\*.*?Generic linker script.*?\\*/\\s+?)", "$1\n\n"
                                                                                                                   + "#if !defined(__" + state.partNumber + "__)\n"
                                                                                                                   + "#define __" + state.partNumber + "__  1\n"
                                                                                                                   + "#endif\n")
                           + "\n\n" + dynamicSections;

            // Done making the merge.S file contents
            retGLDASM[1] = CatStringList(asmMerge);
            if(!state.MCU16Mode)    // ASM code comment character is '#' instead of ';' for XC32
                retGLDASM[1] = retGLDASM[1].replace(';', '#').replaceAll("bytes\\#", Matcher.quoteReplacement("bytes;"));

            return retGLDASM;
        }
    }

    /**
     * Creates PIC24/dsPIC linker scripts with remapping IGT facilities when NOT
     * building an EZBL Bootloader or Application. This is executed from
     * Main.java using a command line such as:
     * <p>
     * </p>
     * <code>
     * java -jar ezbl_tools.jar --gldbuilder -make_non_ezbl_gld -mcpu=24FJ1024GB610 -compiler_folder=\"C:\Program Files (x86)\Microchip\xc16\v1.35\bin"
     * </code>
     */
    public static int CreateNonEZBLLinkerScripts(String[] args)
    {
        EZBLState state = CommandAndBuildState.ReadArgs(null, args);            // Parse command line options into instance specific state
        List<String> deviceParameters = new ArrayList<>();
        List<String> commonSections = new ArrayList<>();
        List<String> bootSections = new ArrayList<>();
        List<String> appSections = new ArrayList<>();
        List<String> appIGT = new ArrayList<>();
        List<String> ivt = new ArrayList<>();
        List<String> bootISRDispatchers = new ArrayList<>();
        int implementedVectorCount;
        int appIGTSpacing = 0x4;            // 0x4 used in IGT for GOTOs, or 0x2 for BRA on smaller devices <= 96KB. Must use GOTO on bigger devices since the reprogrammable App defines where the branch targets will lie
        String gldContents;
        TextBlock deviceGLD;
        String gldOriginalSectionsHeader = "", gldOriginalSections = "", gldOriginalSectionsFooter = "";

        // If nothing is specified upon execution or a pasing error occured, write usage information to STDOUT
        if(!state.parseOkay)
        {
            System.out.print("\r\n"
                             + Multifunction.FormatHelpText(79, 0 * 3, "Usage:")
                             + Multifunction.FormatHelpText(79, 1 * 3, "java -jar ezbl_tools.jar --gldbuilder -make_non_ezbl_glds -mcpu=PIC [-options] -compiler_folder=\"path to XC16 bins\"")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 1 * 3, "Inputs:")
                             + Multifunction.FormatHelpText(79, 2 * 3, "Target PIC part number: ex: -mcpu=33EP64MC506, -mcpu=24FJ128GA010,-mcpu=33FJ256GP710A")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "Path to the XC16 bin installation folder for device .gld searching. Ex: \"C:\\Program Files (x86)\\Microchip\\xc16\\v1.xx\\bin\"")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 1 * 3, "Outputs:")
                             + Multifunction.FormatHelpText(79, 2 * 3, "Generated .gld linker script file for two App project with interrupt multiplexing printed to stdout.")
                             + "\r\n"
            );

            return -1;
        }

        // Validate input parameters
        if(state.partNumber == null || state.compilerLinkScriptPath == null)
        {
            System.err.println("ezbl_tools: Missing required input parameter: need a device part number, compiler linker script to start from, and output linker script file specified.");
            return -2;
        }

        // Read the compiler's device .gld contents into the deviceGLD TextBlock class
        gldContents = Multifunction.ReadFile(state.compilerLinkScriptPath, true);
        if(gldContents == null)
        {
            System.err.println("ezbl_tools: Unable to read \"" + state.compilerLinkScriptPath + "\"");
            return -3;
        }

        deviceGLD = new TextBlock(gldContents);
        // Process all SECTIONS regions
        while(deviceGLD.Find("SECTIONS[^{]*", "\\{", "\\}", "\n"))
        {
            if(gldOriginalSectionsHeader.isEmpty())
            {
                gldOriginalSectionsHeader = deviceGLD.GetOuterLeft(false);
                gldOriginalSectionsFooter = deviceGLD.GetOuterRight(false);
            }
            TextBlock sectionBlock = new TextBlock(deviceGLD.GetInner(true));

            // Process any .ivt sections we find. These need to be remapped into
            // application space. Simultaneously we want to generate an
            // "Interrupt Goto Table", or .igt section. This code could be
            // modified to support an Interrupt Branch Table instead on devices
            // with small enough memory for branching to any address.
            if(sectionBlock.Find("((?ms)\\s/[*].*[*]/\\s\\.ivt)|((?-ms)\\s\\.ivt)[^:]*:[^{]*[^:]*:[^{]*", "\\{", "\\}", "\n"))
            //if(sectionBlock.Find("\\s[.]ivt[^:]*:[^{]*", "\\{", "\\}", "\n"))
            {
                String gldParse = sectionBlock.GetInner(true);

                gldParse = gldParse.replaceAll("__DefaultInterrupt", "");
                gldParse = gldParse.replaceAll("LONG[\\s]*?[(]", "");
                gldParse = gldParse.replaceAll("ABSOLUTE[\\s]*?[(]", "");
                gldParse = gldParse.replaceAll(":[\\s\\S]*?;", "");
                gldParse = gldParse.replaceAll("DEFINED[\\s]*?[(][\\s\\S]*?[?]", "");
                gldParse = gldParse.replaceAll("[)]", "");
                gldParse = gldParse.replaceAll("[\\s]+", "\n");
                gldParse = gldParse.trim();

                // Save a list of possible ivtVectors for the DumpParser to determine which are in use
                //state.ivtVectors = gldParse.split("\n");
                // First, count out how many true interrupts are implemented
                // on this device 
                implementedVectorCount = 0;
                for(int i = 0; i < state.ivtVectors.size(); i++)
                {
                    if(state.ivtVectors.get(i).name.startsWith("__Interrupt"))
                    {   // No physical hardware interrupt present: coalesce all IVT targets to single _DefaultInterrupt() App IGT entry
                        continue;
                    }
                    implementedVectorCount++;
                }

                // Create the IVT and IGT tables
                int outputIndex = 0;
                String igtCode;
                String formattedVectorName;
                for(int i = 0; i < state.ivtVectors.size(); i++)
                {
                    if(state.ivtVectors.get(i).name.startsWith("__Interrupt"))
                    {   // No physical hardware interrupt present: coalesce all IVT targets to single _DefaultInterrupt() App IGT entry
                        ivt.add(String.format("%1$-54s/* Vector at 0x%2$06X, IRQ %3$3d, Vector %4$3d, target _APP_RESET_VECTOR + 0x%5$03X, %6$s coalesces to _DefaultInterrupt() */", "LONG(ABSOLUTE(_Dispatch__DefaultInterrupt));", 0x4 + 0x2 * i, i, i + 8, 0x4 + implementedVectorCount * appIGTSpacing, state.ivtVectors.get(i).name));
                        continue;
                    }

                    formattedVectorName = String.format("%1$-18s", state.ivtVectors.get(i).name);
                    igtCode = "GOTO(DEFINED(" + formattedVectorName + ") ? " + formattedVectorName + " : __DefaultInterrupt);";
                    appIGT.add(String.format("%1$-84s/* IRQ %2$3d, Vector %3$3d, _APP_RESET_VECTOR + 0x%4$03X */", igtCode, i, i + 8, 0x4 + outputIndex * appIGTSpacing));
                    ivt.add(String.format("%1$-54s/* Vector at 0x%2$06X, IRQ %3$3d, Vector %4$3d, target _APP_RESET_VECTOR + 0x%5$03X */", "LONG(ABSOLUTE(_Dispatch" + state.ivtVectors.get(i).name + "));", 0x4 + 0x2 * i, i, i + 8, 0x4 + outputIndex * appIGTSpacing));
                    bootISRDispatchers.add(String.format("_Dispatch%1$18s /* IRQ %2$3d, Vector %3$3d */", state.ivtVectors.get(i).name + " = ABSOLUTE(.);", i, i + 8));
                    bootISRDispatchers.add("    BTSC(_IVTMuxTarget, 0);");
                    bootISRDispatchers.add("    BRA(_APP1_RESET_VECTOR + 0x" + String.format("%1$04X);", 0x04 + (outputIndex * appIGTSpacing)));
                    bootISRDispatchers.add("    BRA(_APP2_RESET_VECTOR + 0x" + String.format("%1$04X);", 0x04 + (outputIndex++ * appIGTSpacing)));
                }

                // Create the last IGT goto entry for unimplemented hardware ivtVectors that just points to the __DefaultInterrupt (in case if ever a new vector is added because it was missing originally in the compiler .gld)
                appIGT.add(String.format("%1$-84s/* _APP_RESET_VECTOR + 0x%2$03X, Unimplemented/coalesced IRQs */", "GOTO(__DefaultInterrupt);", 0x4 + implementedVectorCount * appIGTSpacing));
                bootISRDispatchers.add(String.format("_Dispatch%1$18s /* Unimplemented/coalesced IRQs */", "__DefaultInterrupt = ABSOLUTE(.);"));
                bootISRDispatchers.add("    BTSC(_IVTMuxTarget, 0);");
                bootISRDispatchers.add("    BRA(_APP1_RESET_VECTOR + 0x" + String.format("%1$04X);", 0x04 + (implementedVectorCount * appIGTSpacing)));
                bootISRDispatchers.add("    BRA(_APP2_RESET_VECTOR + 0x" + String.format("%1$04X);", 0x04 + (implementedVectorCount * appIGTSpacing)));
            }

            // Delete .ivt section mapping since we generated our own
            sectionBlock.ReplaceOuter("");

            // Delete .reset section since we will be generating our own
            //if(sectionBlock.Find("(?ms)(/[*]).*?([*]/)\\s*?\\.reset[^:]*:[^{]*", "\\{", "\\}", "\n\\s*?\\#endif\n"))  // Nukes more than it should, such as Program Memory comment before the section
            if(sectionBlock.Find("\\s[.]reset[^:]*:[^{]*", "\\{", "\\}", "\n"))
            {
                sectionBlock.ReplaceOuter("");// /* removed, see above .reset mappings */");
            }

            gldOriginalSections += sectionBlock.GetFullBlock();
            deviceGLD.DeleteOuter();
        }

        // Append header and footer closing braces and extras
        gldOriginalSections = gldOriginalSectionsHeader + gldOriginalSections + gldOriginalSectionsFooter;

        // Add all stuff in the original .gld file, minus the stuff we took out
        deviceParameters.add(deviceGLD.GetFullBlock());

        // Add our symbol constants obtained from the Essential Device Characteristics database
        deviceParameters.add("\n\n");
        deviceParameters.add(String.format("\n_EZBL_ADDRESSES_PER_SECTOR      = 0x%1$06X;", state.eraseBlockSizeAddresses));
        deviceParameters.add(String.format("\n_EZBL_MAIN_FLASH_BASE           = 0x%1$06X;", state.mainFlashRegion.startAddr | (state.dualPartitionMode ? 0x400000 : 0)));
        deviceParameters.add(String.format("\n_EZBL_MAIN_FLASH_END_ADDRESS    = 0x%1$06X;", state.mainFlashRegion.endAddr | (state.dualPartitionMode ? 0x400000 : 0)));
        deviceParameters.add(String.format("\n_EZBL_CONFIG_BASE               = 0x%1$06X;", state.configWordsRegion == null ? 0 : state.configWordsRegion.startAddr | (state.dualPartitionMode ? 0x400000 : 0)));
        deviceParameters.add(String.format("\n_EZBL_CONFIG_END_ADDRESS        = 0x%1$06X;", state.configWordsRegion == null ? 0 : state.configWordsRegion.endAddr | (state.dualPartitionMode ? 0x400000 : 0)));
        deviceParameters.add(String.format("\n_EZBL_DEVID_ADDRESS             = 0x%1$06X;", state.devIDAddr));
        deviceParameters.add(String.format("\n_EZBL_DEVID_MASK                = 0x%1$06X;", state.devIDMask));
        deviceParameters.add(String.format("\n_EZBL_DEVID_VALUE               = 0x%1$06X;", state.devIDValue));
        deviceParameters.add(String.format("\n_EZBL_REVID_ADDRESS             = 0x%1$06X;", state.devRevAddr));
        deviceParameters.add(String.format("\n_EZBL_REVID_MASK                = 0x%1$06X;", state.devRevMask));
        deviceParameters.add(String.format("\n_EZBL_RESERVED_BIT_ADDRESS      = 0x%1$06X;   %2$s", state.devSpecialConf.reservedBitAddr | (state.dualPartitionMode ? 0x400000 : 0), "/* " + state.devSpecialConf.reserveBitConfigName + " */"));
        deviceParameters.add(String.format("\n_EZBL_RESERVED_BIT_MASK         = 0x%1$06X;", state.devSpecialConf.reservedBitMask));
        deviceParameters.add(String.format("\n_EZBL_CODE_PROTECT_ADDRESS      = 0x%1$06X;   %2$s", state.devSpecialConf.codeProtectAddr | (state.dualPartitionMode ? 0x400000 : 0), "/* " + state.devSpecialConf.codeProtectConfigName + " */"));
        deviceParameters.add(String.format("\n_EZBL_CODE_PROTECT_MASK         = 0x%1$06X;", state.devSpecialConf.codeProtectMask));
        deviceParameters.add(String.format("\n_EZBL_BACKBUG_ADDRESS           = 0x%1$06X;   %2$s", state.devSpecialConf.BACKBUGAddr | (state.dualPartitionMode ? 0x400000 : 0), "/* " + state.devSpecialConf.BACKBUGConfigName + " */"));
        deviceParameters.add(String.format("\n_EZBL_BACKBUG_MASK              = 0x%1$06X;", state.devSpecialConf.BACKBUGMask));
        deviceParameters.add("\n"
                             + "\n/* Define macros for generating Reset Vector, IVT, IGT and run-time multiplexing dispatch code */"
                             + "\n#define BTSC(address, bit)   LONG(0xAF0000 | (ABSOLUTE(address) & 0x1FFE) | ((ABSOLUTE(bit) & 0x7)<<13) | ((ABSOLUTE(bit) & 0x8)>>3))"
                             + "\n#define BRA(short_address)   LONG(0x370000 | (((ABSOLUTE(short_address) - ABSOLUTE(. + 2))>>1) & 0x00FFFF))"
                             + "\n#define GOTO(address)        LONG(0x040000 | (ABSOLUTE(address) & 0x00FFFF)); LONG(0x000000 | ((ABSOLUTE(address) & 0x7F0000)>>16))"
                             + "\n"
                             + "\n");

        commonSections.add(
                "\nSECTIONS"
                + "\n{"
                + "\n  /* INTRecipient global RAM variable allocated in all projects to select ISR to execute when HW Interrupt occurs */"
                + "\n  .data.IVTMuxTarget (__DATA_BASE + 0x50) (NOLOAD) :"
                + "\n  {"
                + "\n    _IVTMuxTarget = .;"
                + "\n    . += 2;"
                + "\n  } >data"
                + "\n");

        bootSections.add("\n"
                         + "\n/* Flash contents for Bootloader or other project that owns the hardware vectors */"
                         + "\n#if !defined(APP_RESET_VECTOR)    "
                         + "\n  #if !defined(__CORESIDENT) || defined(__DEFINE_RESET)"
                         + "\n  .reset :"
                         + "\n  {"
                         + "\n    GOTO(__reset);"
                         + "\n  } >reset"
                         + "\n  #endif"
                         + "\n"
                         + "\n"
                         + "\n  .ivt __IVT_BASE :"
                         + "\n  {"
                         + "\n    " + CatStringList(ivt, "\n    ")
                         + "\n  } >ivt"
                         + "\n"
                         + "\n"
                         + "\n  /**"
                         + "\n   * Program code to run time select which ISR executes on hardware interrupt."
                         + "\n   *   IVTMuxTarget<0> == '0' means App 1 ISRs execute."
                         + "\n   *   IVTMuxTarget<0> == '1' means App 2 ISRs execute."
                         + "\n   * Access the IVTMuxTarget variable using this declaration:"
                         + "\n   *   extern volatile unsigned int __attribute__((near, keep, noload)) IVTMuxTarget;"
                         + "\n   */"
                         + "\n  .text.ISRDispatchers :"
                         + "\n  {"
                         + "\n    " + CatStringList(bootISRDispatchers, "\n    ")
                         + "\n  } >program"
                         + "\n");

        appSections.add("\n#else    /* APP_RESET_VECTOR macro defined in project and set to the base program address for the Application project */"
                        + "\n"
                        + "\n  _APP_RESET_VECTOR = ABSOLUTE(APP_RESET_VECTOR);"
                        + "\n"
                        + "\n  .reset (ABSOLUTE(_APP_RESET_VECTOR) + 0x0) :"
                        + "\n  {"
                        + "\n    GOTO(__reset);"
                        + "\n  } >program"
                        + "\n"
                        + "\n"
                        + "\n  .igt (ABSOLUTE(_APP_RESET_VECTOR) + 0x4) :"
                        + "\n  {"
                        + "\n    " + CatStringList(appIGT, "\n    ")
                        + "\n  } >program"
                        + "\n#endif"
                        + "\n}"
                        + "\n"
                        + "\n"
                        + "\n");

        System.out.print(CatStringList(deviceParameters) + CatStringList(commonSections) + CatStringList(bootSections) + CatStringList(appSections) + gldOriginalSections);
        return 0;
    }
}
