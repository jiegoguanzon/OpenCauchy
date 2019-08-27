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
import static com.microchip.apps.ezbl.Multifunction.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;


/**
 *
 * @author C12128
 */
public class CommandAndBuildState implements Serializable
{
    public static final byte defaultEncryptionSalt[] =
    {
        (byte)0x95, (byte)0x0F, (byte)0xEB, (byte)0xD9, (byte)0xD7, (byte)0xBA, (byte)0x8E, (byte)0xCD, (byte)0xA4, (byte)0x88, (byte)0x53, (byte)0x60, (byte)0x7C, (byte)0xE4, (byte)0xF9, (byte)0x4B
    };

    /**
     * Returns a EZBLState class which is either a reference to the
     * stateTemplate that has been provided, updated by the args that have also
     * been provided, or a new EZBLState class, filled in with all values
     * specified by args. The stateTemplate is modified only when the "-mcpu="
     * argument is NOT provided, or "-mcpu=" arg is provided, but matches that
     * which was already stored in the stateTemplate.
     *
     * @param stateTemplate A EZBLState class to modify or replace. Can be null
     *                      if no prior state information is needed/wanted.
     * @param args          Array of Strings containing individual command line
     *                      arguments.
     *
     * @return EZBLState class reference to the updated stateTemplate, or a new
     *         EZBLClass (as needed).
     */
    public static EZBLState ReadArgs(EZBLState stateTemplate, String[] args)
    {
        String raw_mcpu = null;
        EZBLState originalState = null;
        EZBLState s = stateTemplate;

        // Create a new, default EZBLState as the stateTemplate if none is explicitly passed in
        if(s == null)
        {
            s = new EZBLState();
        }
        originalState = s.clone();

        for(int inIndex = 0; inIndex < args.length; inIndex++)
        {
            if((args[inIndex] == null) || (args[inIndex].length() == 0))    // Skip zero width args (ones we've eaten someplace on purpose so they don't propagate).
            {
                continue;
            }

            // Get argument token, normalize to lowercase and if it starts with '--', make it only have one '-'.
            if(args[inIndex].startsWith("--"))
            {
                args[inIndex] = args[inIndex].substring(1);
            }
            String token = args[inIndex].toLowerCase();
            String newOption = args[inIndex];
            int lastEqualIndex = newOption.lastIndexOf('=');
            if(lastEqualIndex >= 0)
            {   // When the option contains an equals, we would have lost a set of quotation marks after the equals. Re-add them.
                newOption = newOption.substring(0, lastEqualIndex + 1) + "\"" + newOption.substring(lastEqualIndex + 1) + "\"";
            }

            if(token.startsWith("-state="))
            {
                s.ezblStateSavePath = TrimQuotes(args[inIndex].substring("-state=".length()));
            }
            else if(token.matches("-16") || token.startsWith("-pic24") || token.startsWith("-dspic"))
            {
                s.MCU16Mode = true;
            }
            else if(token.matches("-32") || token.startsWith("-pic32"))
            {
                s.MCU16Mode = false;
            }
            else if(token.startsWith("-noivtremap"))
            {
                s.remapISRThroughIGT = false;
            }
            else if(token.startsWith("-ivtremap"))
            {
                s.remapISRThroughIGT = true;
            }
            else if(token.startsWith("-delete.reset="))    // Allows explicit true or false assigment of deleteResetSection
            {
                s.deleteResetSection = Boolean.parseBoolean(TrimQuotes(args[inIndex].substring("-delete.reset=".length())));
            }
            else if(token.startsWith("-delete.reset"))     // Allows implicit true assignment to deleteResetSection
            {
                s.deleteResetSection = true;
            }
            else if(token.startsWith("-timeout="))     // Timeout in milliseconds to use for communications command acknowledgements
            {
                s.milliTimeout = Integer.decode(TrimQuotes(args[inIndex].substring("-timeout=".length())));
            }
            else if(token.startsWith("-save-temps") || token.startsWith("-save_temps") || token.startsWith("-log"))   // Save temporary files generated by ezbl_tools.jar operations
            {
                s.saveTemps = true;
                if(token.startsWith("-save-temps=") || token.startsWith("-save_temps="))    // Also save the given filename, if given
                    s.saveTempsFile = TrimQuotes(args[inIndex].substring("-save-temps=".length()));
                if(token.startsWith("-log="))
                    s.saveTempsFile = TrimQuotes(args[inIndex].substring("-log=".length()));
            }
            else if(token.startsWith("-base="))        // Base address to build the bootloader for
            {
                s.baseAddress = Long.decode(TrimQuotes(args[inIndex].substring("-base=".length())));
            }
            else if(token.startsWith("-pass="))        // Pass count invoking this tool when doing multi-pass linking operations. Starts at 1.
            {
                s.pass = Integer.decode(TrimQuotes(args[inIndex].substring("-pass=".length())));
            }
            else if(token.startsWith("-ignore="))      // Address range to remove or ignore from the input/output (context sensitive)
            {
                s.ignoreROMRegions.add(new AddressRange(TrimQuotes(args[inIndex].substring("-ignore=".length()))).toMemoryRegion());
            }
            else if(token.startsWith("-temp-folder=") || token.startsWith("-temp_folder=")) // Location to write temporary files to
            {
                s.temporariesPath = TrimQuotes(args[inIndex].substring("-temp-folder=".length()));
            }
            else if(token.startsWith("-password="))    // Passphrase used for encryption/decryption
            {
                s.encryptionPassword = TrimQuotes(args[inIndex].substring("-password=".length()));
            }
            else if(token.startsWith("-salt="))        // Salt used for generating the encryption/decryption key from the passphrase/password
            {
                s.encryptionSaltString = TrimQuotes(args[inIndex].substring("-salt=".length()));
            }
            else if(token.startsWith("-conf="))        // MPLAB X ${CONF} make variable defining which configuration is targetted for the present build operation
            {
                s.conf = TrimQuotes(args[inIndex].substring("-conf=".length()));
            }
            else if(token.startsWith("-partition="))           // Target codePartition when building an application supporting Dual Partition functions (requires hardware support). Option can be 1 or 2.
            {
                s.targetPartition = Integer.decode(TrimQuotes(args[inIndex].substring("-partition=".length())));
                s.dualPartitionMode = (s.targetPartition != 0);
            }
            else if(token.startsWith("-fboot="))               // Value to program into the FBOOT configuration word (only needed if absent from the .hex/.blob file and targetting a Dual Partition configuration)
            {
                s.devSpecialConf.FBOOTValue = Integer.decode(TrimQuotes(args[inIndex].substring("-fboot=".length())));
            }
            else if(token.startsWith("-makefile="))            // Makefile-${CONF}.mk file used for the project build. This is automatically determined if -conf is specified, but specifying the -makefile option will take precedence.
            {
                s.makefilePath = TrimQuotes(args[inIndex].substring("-makefile=".length()));
            }
            else if(token.startsWith("-elf_artifact="))
            {
                s.elfPath = TrimQuotes(args[inIndex].substring("-elf_artifact=".length()));
            }
            else if(token.startsWith("-mcpu="))                // Main target CPU option (ex: "33ep512mu810")
            {
                raw_mcpu = args[inIndex];
                s.partNumber = TrimQuotes(args[inIndex].substring("-mcpu=".length()));
                s.partNumber = s.partNumber.replaceFirst(Matcher.quoteReplacement("([dD][sS][pP][iI][cC])|([pP][iI][cC])"), "");
                if((originalState.partNumber != null) && !s.partNumber.equals(originalState.partNumber))
                {
                    // A previous stateTemplate file was loaded, but this part number does not match the prior one.
                    // This can break various operations due to presaved other parameters that are no longer applicable.
                    // Therefore, let's delete all prior stateTemplate information and start over again parsing all
                    // parameters we were given in the MakeEditor stage plus all the parameters we have now.
                    if(s.verbose)
                    {
                        System.out.print("EZBL: Different -mcpu value provided; ignoring stale " + s.ezblStateSavePath + " state file contents.\n");
                    }
                    EZBLState ret = ReadArgs(null, args);   // Generate new state, and parse present args
                    ret.argsFromMakeEditor = args;          // Save args strings indicating what this state file is configured for
                    return ret;
                }
                if(s.partNumber.startsWith("33") || s.partNumber.startsWith("30") || s.partNumber.startsWith("24"))
                    s.MCU16Mode = true;
                else if(s.partNumber.startsWith("32"))
                    s.MCU16Mode = false;
                if(s.partNumber.startsWith("33") || s.partNumber.startsWith("30"))
                    s.fullPartNumber = "dsPIC" + s.partNumber;
                else
                    s.fullPartNumber = "PIC" + s.partNumber;
            }
            else if(token.startsWith("-artifact="))            // Path to a image file that will be manipulated (can be a .hex or .blob file)
            {
                s.artifactPath = TrimQuotes(args[inIndex].substring("-artifact=".length()));
                s.hexPath = s.artifactPath;
            }
            else if(token.startsWith("-project_name="))        // Makefile ${PROJECTNAME} value
            {
                s.projectName = TrimQuotes(args[inIndex].substring("-project_name=".length()));
            }
            else if(token.startsWith("-linkscript="))          // Path to the linker script file we shall be modifying for this build operation
            {
                s.linkScriptPath = GetCanonicalPath(FixSlashes(TrimQuotes(args[inIndex].substring("-linkscript=".length()))).replace(",--script=", ""));  // Trim junk from Makefile string
            }
            else if(token.startsWith("-compiler_folder="))     // Folder containing targetted MPLAB XC16/XC32 compiler installation binaries
            {
                s.compilerFolder = GetCanonicalPath(FixSlashes(TrimQuotes(args[inIndex].substring("-compiler_folder=".length()))));
            }
            else if(token.startsWith("-java="))                // Path to the Java JRE to use when self-executing subsequent commands that require Java. This can be used to ensure the same JRE is used between operations.
            {
                s.javaPath = GetCanonicalPath(TrimQuotes(args[inIndex].substring("-java=".length())));
            }
            else if(token.startsWith("-path_to_ide_bin="))                // Path to the Java JRE to use when self-executing subsequent commands that require Java. This can be used to ensure the same JRE is used between operations.
            {
                s.pathToIDEBin = GetCanonicalPath(TrimQuotes(args[inIndex].substring("-path_to_ide_bin=".length())));
            }
            else if(token.startsWith("-last_page_erasable"))   // Boolean specifying that the last Flash page should be allowed to be erased while bootloading. Many devices have Flash Configuration words located on this page.
            {
                s.allowLastPageErase = true;
            }
            else if(token.startsWith("-nolast_page_erasable"))   // Boolean specifying that the last Flash page should be allowed to be erased while bootloading. Many devices have Flash Configuration words located on this page.
            {
                s.allowLastPageErase = false;
            }
            else if(token.startsWith("-first_page_erasable"))  // Boolean specifying if the first Flash page should be allowed to be erased while bootloading. This page has the device reset vector and IVT on 16-bit devices.
            {
                s.allowFirstPageErase = true;
            }
            else if(token.startsWith("-version"))     // getImplementationTitle()/getImplementationVersion() returns null instead of manifest values for some reason...
            {
                System.out.printf("%s %s v%s\n", EZBLState.class.getPackage().getSpecificationVendor(), EZBLState.class.getPackage().getImplementationTitle(), EZBLState.class.getPackage().getImplementationVersion());
            }
            else if(token.startsWith("-verbose"))              // Verbose boolean
            {
                s.verbose = true;
            }
            else if(token.startsWith("-silent"))               // Silent boolean
            {
                s.silent = true;
            }
            else if(token.startsWith("-com="))                 // Communications port path to open when uploading a .blob or otherwise communicating with a running bootloader. Prefix with \\.\ when specifying COM10 and above on Windows.
            {
                s.comPort = TrimQuotes(args[inIndex].substring("-com=".length()));
                if(s.comPort.toLowerCase().startsWith("i2c"))  // If the communications port is an I2C interface, hardware flow control is implemented intrinsically by SCL clock stretching.
                    s.hardwareFlowControl = true;
            }
            else if(token.startsWith("-baud="))                // Communications baud rate when uploading a .blob or otherwise communicating with a running bootloader.
            {
                s.baudRate = TrimQuotes(args[inIndex].substring("-baud=".length()));
            }
            else if(token.startsWith("-slave_address="))       // I2C Slave address to target when communicating with an I2C bootloader -- Legacy, deprecated option
            {
                s.slaveAddress = Integer.decode(TrimQuotes(args[inIndex].substring("-slave_address=".length())));
            }
            else if(token.startsWith("-i2c_address="))       // I2C Slave address to target when communicating with an I2C bootloader
            {
                s.slaveAddress = Integer.decode(TrimQuotes(args[inIndex].substring("-i2c_address=".length())));
            }
            else if(token.startsWith("-auto_baud_retries="))
            {
                s.autoBaudRetryLimit = Integer.decode(TrimQuotes(args[inIndex].substring("-auto_baud_retries=".length())));
            }
            else if(token.startsWith("-command_prefix="))
            {
                s.commandPrefix = TrimQuotes(args[inIndex].substring("-command_prefix=".length()));
            }
            else if(token.startsWith("-skip_handshake"))
            {
                s.skipComHandshake = true;
            }
            else if(token.startsWith("-min_free_psv="))
            {
                s.minFreePSVSpace = Integer.decode(TrimQuotes(args[inIndex].substring("-min_free_psv=".length())));
            }
            else if(token.startsWith("-warn_free_psv="))
            {
                s.warnFreePSVSpace = Integer.decode(TrimQuotes(args[inIndex].substring("-warn_free_psv=".length())));
            }
            else if(token.startsWith("-align="))
            {
                s.hexAlign = Integer.decode(TrimQuotes(args[inIndex].substring("-align=".length())));
            }
            else if(token.startsWith("-"))
            {
                if(!s.undecodedOptions.contains(newOption))
                    s.undecodedOptions += " " + newOption + " ";
            }
            else
            {
                System.err.println("    EZBL: Command token \"" + args[inIndex] + "\" unrecognized. Ignoring.");
            }
        }

        // Choose default path for ezbl_build_state.bin file if all we have is a -conf= option
        if(s.ezblStateSavePath == null)
            s.ezblStateSavePath = GetStateFilePath(args);

        if(!s.silent && System.getProperty("java.class.path", "ezbl_tools.jar").contains("ezbl_tools\\build\\classes"))
            s.verbose = true;

        // Choose default makefile for the given configuration if -conf= specified, but no -makefile=
        if((s.makefilePath == null) && (s.conf != null))
            s.makefilePath = "nbproject" + File.separator + "Makefile-" + s.conf + ".mk";

        // Decode MCU16Mode, eCore, and the path to the compiler .gld linker
        // script file from -mcpu option and compiler path
        if((s.partNumber != null) && (s.fullPartNumber != null))
        {
            if(s.partNumber.startsWith("30") || s.partNumber.toUpperCase().startsWith("33F") || s.partNumber.toUpperCase().startsWith("24H") || s.partNumber.toUpperCase().startsWith("24F"))
                s.coreType = CPUClass.f;
            else if(s.partNumber.toUpperCase().startsWith("33E") || s.partNumber.toUpperCase().startsWith("24E"))
                s.coreType = CPUClass.e;
            else if(s.partNumber.toUpperCase().startsWith("33C"))
                s.coreType = CPUClass.c;
            else if(s.partNumber.toUpperCase().startsWith("33B"))
                s.coreType = CPUClass.b;
            else if(s.partNumber.toUpperCase().startsWith("33A"))
                s.coreType = CPUClass.a;
            else if(s.partNumber.toUpperCase().startsWith("32MM"))
                s.coreType = CPUClass.mm;
            if(s.coreType != CPUClass.other)
                s.MCU16Mode = (s.coreType == CPUClass.f) | (s.coreType == CPUClass.e) | (s.coreType == CPUClass.c) | (s.coreType == CPUClass.b);

            if((originalState.compilerFolder == null) || (s.compilerFolder == null) || !originalState.compilerFolder.equals(s.compilerFolder))  // Invalidate derived data if the compiler has changed
            {
                s.compilerLinkScriptPath = null;
                s.compilerGLDContents = null;
            }

            // Decode the target -mcpu= part number name to a device architecture
            if(s.MCU16Mode)
            {
                if(s.compilerLinkScriptPath == null)
                {
                    // Try ignoring compiler provided linker script if the current directory or ezbl_tools.jar directory contains a properly named "pPartNumber.gld" file, which we would use instead
                    File f = new File("p" + s.partNumber + ".gld");
                    if(f.exists())
                        s.compilerLinkScriptPath = f.getName();
                    else
                    {
                        f = new File(EZBLState.ezblToolsExecPath() + "/p" + s.partNumber + ".gld");
                        if(f.exists())
                            s.compilerLinkScriptPath = GetCanonicalPath(f.getPath());
                    }

                    if((s.compilerFolder != null) && (s.compilerLinkScriptPath == null))    // If local file didn't exist, try default XC16 path to processor .gld path given the part number
                    {
                        String familyPrefix = "PIC24F";
                        try
                        {   // Get "dsPIC3xx" or "PIC24x" family prefix string
                            familyPrefix = s.fullPartNumber.startsWith("ds") ? s.fullPartNumber.substring(0, 8) : s.fullPartNumber.substring(0, 6);
                        }
                        catch(IndexOutOfBoundsException ex)
                        {   // Do nothing - going to handle file loading problems later
                        }
                        s.compilerLinkScriptPath = FixSlashes(s.compilerFolder + "/../support/" + familyPrefix + "/gld/p" + s.partNumber + ".gld");
                        if(!(new File(s.compilerLinkScriptPath).exists()))
                        {
                            s.compilerLinkScriptPath = null;
                        }
                    }
                    if((s.compilerFolder != null) && (s.compilerLinkScriptPath == null))    // Still not found, try searching all folders recursively in support for the .gld file
                    {
                        List<File> gldMatches = FindFiles(FixSlashes(s.compilerFolder + "/../support/"), "p" + s.partNumber + ".gld", true);
                        if((gldMatches != null) && (gldMatches.size() >= 1))
                            s.compilerLinkScriptPath = GetCanonicalPath(gldMatches.get(0).getPath());
                    }
                    if((s.compilerFolder != null) && (s.compilerLinkScriptPath == null))    // Still not found, try searching all folders recursively in any compiler folder and with a wildcard prefix character instead of 'p' and case insensitive .gld extension
                    {
                        List<File> gldMatches = FindFilesRegEx(FixSlashes(s.compilerFolder + "/../"), "." + s.partNumber + "\\.[gG][lL][dD]", true);
                        if((gldMatches != null) && (gldMatches.size() >= 1))
                            s.compilerLinkScriptPath = GetCanonicalPath(gldMatches.get(0).getPath());
                    }
                    if((s.compilerFolder != null) && (s.compilerLinkScriptPath == null))    // Still not found, report an error and continue. Only when building a bootloader is a compiler provided linker script really needed.
                    {
                        System.err.println("EZBL: could not find .gld linker script for '" + raw_mcpu + "'. Ensure '-compiler_folder=' option is set to point to a valid XC16/XC32 bin folder and a valid target processor for the compiler is selected, this device is supported by EZBL and ezbl_tools.jar make/command parameters are correct.");
                    }
                }
            }
        }

        // Set default temporaries path to the same folder this .jar sits in if
        // the command line options didn't set it
        if(s.temporariesPath == null)
        {
            s.temporariesPath = CommandAndBuildState.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try
            {
                s.temporariesPath = URLDecoder.decode(s.temporariesPath, "UTF-8");
            }
            catch(UnsupportedEncodingException ex)
            {
                Logger.getLogger(CommandAndBuildState.class.getName()).log(Level.SEVERE, null, ex);
            }
            s.temporariesPath = new File(s.temporariesPath).getParent();

            // Check for null again in case if there is no parent directory
            if(s.temporariesPath == null)
            {
                s.temporariesPath = "";
            }
        }

        // Sanitize temporaries folder by adding directory seperator if it is
        // missing a trailing slash (we assume it always has one elsewhere)
        if(!s.temporariesPath.isEmpty())
        {
            // Add trailing slash if there isn't one already
            if(!s.temporariesPath.substring(s.temporariesPath.length() - 1).equals(File.separator))
            {
                s.temporariesPath += File.separator;
            }
        }

        // Decide if this is a slave CPU core or not. For Slave cores, lets auto-disable IVT remapping. Also disable for PIC32MM devices.
        if(((s.fullPartNumber != null) && (s.fullPartNumber.matches("[^S]*?S[0-9]$"))) || (s.coreType == CPUClass.mm))   // Ex: dsPIC33CH128MP508S1 for Slave Core 1
            s.remapISRThroughIGT = false;   // Looks like a Slave core to me; avoid making a regular Bootloader with ISR remapping

        s.LoadEDCData();

        // Expand/pad encryption password and salt it, if provided
        if(s.encryptionPassword != null)
        {
            int i;
            int chunkSize;
            byte passBytes[];

            s.encryptionKey = new byte[16];

            // Expand the password string to a byte array, concatenating onto itself
            // until 16 bytes exist
            try
            {
                passBytes = s.encryptionPassword.getBytes("UTF-8");
            }
            catch(UnsupportedEncodingException ex)
            {
                passBytes = s.encryptionPassword.getBytes();
            }
            i = 0;
            while(i < s.encryptionKey.length)
            {
                chunkSize = s.encryptionKey.length - i;
                if(chunkSize > passBytes.length)
                {
                    chunkSize = passBytes.length;
                }
                System.arraycopy(passBytes, 0, s.encryptionKey, i, chunkSize);
                i += chunkSize;
            }

            // Salt the password
            if(s.encryptionSaltString == null)
                s.encryptionSalt = defaultEncryptionSalt;
            for(i = 0; i < s.encryptionKey.length; i++)
            {
                s.encryptionKey[i] ^= s.encryptionSalt[i];
            }
        }

        s.parseOkay = true;
        return s;
    }

    public static String GetStateFilePath(String[] args)
    {
        String ret = "ezbl_integration" + File.separator + "ezbl_build_state.bin";  // Default, if nothing else given to key on

        if(args != null)
        {
            for(int inIndex = 0; inIndex < args.length; inIndex++)
            {
                // Get argument token, normalize to lowercase and if it starts with '--', make it only have one '-'.
                if(args[inIndex].startsWith("--"))
                {
                    args[inIndex] = args[inIndex].substring(1);
                }
                String token = args[inIndex].toLowerCase();

                if(token.startsWith("-state="))
                {
                    return TrimQuotes(args[inIndex].substring("-state=".length()));
                }
                else if(token.startsWith("-artifact="))
                {
                    String artifact = TrimQuotes(args[inIndex].substring("-artifact=".length()));
                    File f = new File(artifact);
                    String parentFolder = f.getParent();
                    if(parentFolder != null)
                    {
                        ret = parentFolder + File.separator + "ezbl_build_state.bin";
                    }
                }
            }
        }

        return ret;
    }
}
