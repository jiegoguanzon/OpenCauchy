/*
 *******************************************************************************
 * Copyright (c) 2016 Microchip Technology Inc. All rights reserved.
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
import static com.microchip.apps.ezbl.Multifunction.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;


/**
 *
 * @author C12128
 */
public class EZBLState implements Serializable, Cloneable
{
    static final long serialVersionUID = 211L;      // EZBLState() class definition version. Should be set to (Integer.decode(EZBLState.getEZBLToolsVersion)*100)
    public String ezblStateSavePath = null;
    public int pass = 0;                           // -pass option: A pass counter for parsing .elf data - not related to -password
    public boolean parseOkay = false;
    public boolean MCU16Mode = true;               // PIC24 or dsPIC targetted (16-bit device)


    public enum CPUClass
    {
        other, // Unknown
        f, // PIC24F, dsPIC30F, dsPIC33F, PIC24H (16-bit)
        e, // dsPIC33E, PIC24E (16-bit)
        c, // dsPIC33C (16-bit)
        b, // Reserved (16-bit)
        a, // Reserved (32-bit)
        mm // PIC32MM (32-bit)
    };
    public CPUClass coreType = CPUClass.other;
    public boolean verbose = false;                // Flag for printing more debug messages
    public boolean silent = false;                 // Flag for printing no status messages
    public boolean linkedAsDebugImage = false;     // Flag to setting BKBUG to match what the ICD requires when the project is compiled for Debug mode
    public int flashWordSize = 0x4;                // Number of program addresses that a minimally sized Flash word occupies. Used for padding to minimum Flash write size requirements.
    public int hexAlign = 0;                       // Minimum address alignment for Flash padding/alignment operations (Blobber). 0 is reserved to mean option not set. If you don't want alignment, use hexAlign = 1.
    public boolean remapISRThroughIGT = true;      // true: Generate an Interrupt Goto Table to remap all Interrupt Vectors onto an erasable Flash page; false: Assume interrupts as defined by the compiler are fine (ex: Dual Partition ommits any need to forward interrupts).
    public boolean saveTemps = false;              // Configuration option of saving ezbl_tools temporary files during the build process, including elf dumps
    public boolean allowLastPageErase = true;      // Configuration Option
    public boolean allowFirstPageErase = false;    // Configuration Option
    public boolean deleteResetSection = false;     // Determins if the GLDBuilder should delete the .reset section that pre-exists in XC16 .gld linker scripts or not. May be desired if explicitly specified on the command line.
    public boolean dualPartitionMode = false;      // false = Single Partition view; true = Partition 1 (Active Partition/requires Dual Partition supporting device); 2 = Partition 2 (Inactive Partition/requires Dual Partition supporting device)
    public String[] argsFromMakeEditor = null;     // Array of original command line arguments passed in when the MakeEditor class (--make_editor) was first invoked. Used to rebuild a new state file at the first GLDBuilder stage if the target processor has changed.
    public String conf = null;                     // ${CONF}, name of MPLAB X build configuration, ex:"default"
    public String makefilePath = null;             // Path to MPLAB X build configuration makefile, ex: "nbproject/Makefile-${CONF}.mk"
    public String artifactPath = null;             // Location of MPLAB X .hex or .elf build output
    public String projectName = null;              // Makefile ${PROJECTNAME}, ex: ex_boot_uart
    public String linkScriptPath = null;           // MPLAB X project linker script file path
    public String compilerFolder = null;           // Makefile ${MP_CC_DIR}, ex: C:\Program Files (x86)\Microchip\xc16\v1.25\bin, set via -compiler_folder="C:\..."
    public String compilerLinkScriptPath = null;   // XC16, etc. compiler linker script for the selected project processor
    public String saveTempsFile = null;            // -save_temps= file path for communications logging or other single file outputs
    public String temporariesPath = null;          // -temp-folder= command line option, with a trailing '/' added. If no such option is specified, the folder that this ezbl_tools.jar file is located is used.
    public String partNumber = null;               // Makefile ${MP_PROCESSOR_OPTION}, ex: "33EP512GM710", "24FJ128GA010"
    public String fullPartNumber = null;           // Derived from partNumber, ex: "dsPIC33EP512GM710", "PIC24FJ128GA010"
    public String javaPath = null;                 // Makefile ${MP_JAVA_PATH}, ex: "C:\Program Files (x86)\Microchip\MPLABX\sys\java\jre1.7.0_25-windows-x64\java-windows/bin/"
    public String pathToIDEBin = null;             // Makefile ${PATH_TO_IDE_BIN}, ex: "C:/Program Files (x86)/Microchip/MPLABX/DefaultMPLAB/mplab_ide/platform/../mplab_ide/modules/../../bin/"
    public String hexPath = null;                  // Makefile ${CND_ARTIFACT_PATH_${CONF}}, ex: "dist/default/production/ex_boot_uart_icsp_prog.production.hex" or "dist/default/debug/ex_boot_uart_icsp_prog.debug.elf"
    public String elfPath = null;                  // Path to .elf output artifact/temporary Pass 1 linking
    public String undecodedOptions = "";
    public String passThroughOptions = "";
    public String comPort = null;                  // Serial COM port name, ex: COM8, I2C, I2C0, I2C1, etc.
    public String baudRate = null;                 // Serial COM port or I2C baud rate, ex: 115200, 460800, 100000, 400000, etc.
    public int autoBaudRetryLimit = 4;             // Maximum number of non-timeout communications auto-baud echo failures to attempt before giving up if echo data is coming back corrupt. Every two attempts represents a halfing of baud rate used (70.71% multipler per attempt).
    public boolean hardwareFlowControl = false;    // Boolean indicating if communications implement hardware flow control or if the software protocol has to implement it.
    public String commandPrefix = "";              // Communicator command transmit prefix to apply for transmission (to avoid interfering with other nodes that are using a shared communications bus)
    public boolean skipComHandshake = false;       // Communicator should try to communicate without doing auto-baud or echo testing
    public int slaveAddress = 0x0;                 // Slave target address on the I2C bus (only applicable to I2C coms)
    public int milliTimeout = 1100;                // Default communications timeout (time since last RX activity, not absolute time for operations). Should be set > ping time.
    public String javaLaunchString = null;         // Command execution string for makefiles including full ClassPath for crownking.edc.jar, crownking.jar/crownking.common.jar and jna.jar from MPLAB X.
    public String encryptionPassword = null;       // AES128 raw .blob encryption password as entered by the user
    public String encryptionSaltString = null;     // Not implemented
    public byte encryptionSalt[] = null;       // Custom password salt (does not really need to be kept secret as it only prevents using pre-computed existing hash tables. Any unique value could work.)
    public byte encryptionKey[] = null;        // Padded (self-concatenated) and salted Password info (must be kept private)
    //public TreeMap<Integer, Integer> generatedDataSizeByPass = new TreeMap<>();   // <Link pass number (starting at 1), Program space addresses used by generated EZBL table and IVT data>
    public long baseAddress = 0x000000;        // Base address of the bootloader, not including GOTO @ 0x000000, IVT entries, or config words. 0x000000 means no special handling and just let the linker choose where to place things.
    public long endAddressOfBootloader = 0;    // First address after a bootloader sector
    public long baseAddressOfAppErasable;      // Base address of EZBL_appBootloadState, EZBL_APP_RESET_BASE, and Interrupt Goto Table
    public long baseAddressOfGotoReset;        // Base address of app's goto __reset instruction
    public long baseAddressOfIGT;              // Base address of the Interrupt Goto Table (application erasable)
    public int sizeOfIGT;                      // Number of addresses used by the Interrupt Goto Table (in the Application project)
    public int sizeOfAppErasable;              // Number of addresses used by the whole App Erasable section. Includes EZBL_appBootloadState structure, reset vector, and IGT.

    // Block of data decoded from EDC .PIC file
    public boolean hasFlashConfigWords = false;                 // Specifies if Config Words are in Flash or in real Fuses
    public boolean edcInfoLoaded = false;                       // Flag indicating the following variables have been found and successfully read for the given device
    public List<MemoryRegion> devNVGeometry = null;             // SQLitePartDatabase.MemoryTable.BootloadableRanges table data: array describing the non-volatile memory addresses on the device. Excludes FBOOT.
    public List<MemoryRegion> devMemories = new ArrayList<>();  // SQLitePartDatabase.MemoryTable.DeviceMemories table data: array describing all recognized memories on the target device, including RAM, test space, flash, otp, EEPROM, etc.
    public List<MemoryRegion> devProgramSpaces = new ArrayList<>(); // SQLitePartDatabase.MemoryTable.GLDMemories table data (SQLitePartDatabase.MemoryTable.DeviceMemories for PIC32MM): array describing the physical part's non-volatile geometry. Split into separate regions and contains MEMORY names as text for linker assignment. Includes all Config words (including FBOOT) split by name.
    public TreeMap<Long, MemoryRegion> devConfigWordsByAddr = new TreeMap<>();  // SQLitePartDatabase.MemoryTable.DecodedConfigMemories table data: list of all Config words by name, their address and their size for the selected device
    public TreeMap<String, MemoryRegion> devConfigWordsByName = new TreeMap<>();  // SQLitePartDatabase.MemoryTable.DecodedConfigMemories table data: list of all Config words, their address and their size for the selected device
    public MemoryRegion mainExecutionRegion = null;             // MemoryRegion of ordinary execution Flash, not including Reset Vector/IVT/Flash Config Words addresses. I.e. __CODE_BASE to __CODE_BASE + __CODE_LENGTH on devices with Aux Flash or secondary partitions.
    public MemoryRegion mainFlashRegion = null;                 // MemoryRegion of main Flash array (ex: starting at address 0x000000 with Reset Vector and extending to the end of flash, inclusive any flash config words if present)
    public MemoryRegion configWordsRegion = null;               // SQLitePartDatabase.MemoryTable.DeviceMemories derived MemoryRegion where all of the normal Flash Configuration words lie between on the device (not including FBOOT, if it exists
    protected int eraseBlockSize = 512;                         // Count of instructions for the minimum erase block size
    protected int eraseBlockSizeBytes = eraseBlockSize * 3;     // Count of bytes for the minimum erase block size
    protected int eraseBlockSizeAddresses = eraseBlockSize * 2; // Count of addresses for the minimum erase block size
    public long devIDAddr = 0;                                  // Location of DEVID value in program memory
    public long devIDMask = 0;                                  // Mask for important bits in the DEVID register (not part of revision)
    public long devIDValue = 0;                                 // Value that should corresponds to the selected PIC, but formatted as stored in DDT (not masked yet)
    public long devRevAddr = 0;                                 // Location of DEVREV value in program memory; This seems to be stored as one range in edc, so it is called an end address.
    public long devRevMask = 0;                                 // Mask for important bits in the DEVREV register (or DEVID register on PIC32MM)


    public static class SpecialConfigFields implements Serializable    // Special Config word bitfields
    {
        long BACKBUGAddr = 0;               // Address of the Config Word containing BACKBUG ("BKBUG") background debugger config bit
        long BACKBUGMask = 0;               // Bit position mask in the Config Word containing BACKBUG bit
        String BACKBUGConfigName = null;
        long codeProtectAddr = 0;           // Ex: address of FGS Flash Config Word
        long codeProtectMask = 0;           // Ex: 0x000002, meaning clearing bit 1 will turn on Code Protect. Some devices with multiple levels of Code Guard security will have more than one Code Protect Bit
        String codeProtectConfigName = null;
        long reservedBitAddr = 0;           // Flash address of the Flash Reserved Bit (should only be applicable if Config Words are in Flash)
        long reservedBitMask = 0;           // Bit position mask info in the reservedBitAddr program location for the Reserved Bit; this bit must be programmed as '0' if it doesn't already have that value in it.
        String reserveBitConfigName = null; // Name of the Flash Config Word that contains the reserved bit
        public int FBOOTAddr = -1;          // Address of FBOOT (could be 0x801000 or 0x801800, depending on device). If the device does not have an FBOOT Config word, then this field is set to -1.
        public int FBOOTValue = 0xFFFFFF;   // Value to program into the FBOOT config
    };
    public SpecialConfigFields devSpecialConf = new SpecialConfigFields();
    public ELFDumpParser elfDump = null;    // Parsed symbol and section list
    public GLDMemories gldMemories = null;  // Structure of all of the memories found in the Project linker script
    public SortedMap<Integer, InterruptVector> ivtVectors = null;
    public int ivtVectorsImplemented = 0;              // Count of how many true interrupt ivtVectors the selected processor supports (i.e. all reserved vector entries in the table are thrown away when counting)
    public String ivtSectionRemapped = null;           // .ivt section, but changed to point to IGT
    public String erasableIGT = null;                  // Remapped interrupt vector table (Interrupt Goto Table)
    public int igtSpacing = 0x4;
    public String compilerGLDContents = null;          // Processor .gld file contents read from compiler folder. This field should not be modified except when loading the compiler's gld for the first time.
    public List<MemoryRegion> romUseRegions = new ArrayList<>();    // Sector padded (except possibly 1st sector and config words sector) list of addresses used by this project
    public List<MemoryRegion> ramUseRegions = new ArrayList<>();    // Static/global RAM address range used by this project
    public List<Section> ramUseSections = new ArrayList<>();        // Static/global RAM address range used by this project
    public List<MemoryRegion> ignoreROMRegions = new ArrayList<>(); // -ignore= ranges specified on the command line and internal ones created by EZBL (ex: EZBL_RESERVE_CONST_FOR_APP() macro in bootloader)
    public List<MemoryRegion> noEraseRegions = null;
    public List<MemoryRegion> noProgramRegions = null;
    public List<MemoryRegion> noVerifyRegions = null;

    public int minFreePSVSpace = 0;                     // Minimum number of PSV address space bytes (and program addresses) that a standalone mode bootloader must keep free for future application use without triggering a build failure
    public int warnFreePSVSpace = 0;                    // Minimum number of PSV address space bytes (and program addresses) that a standalone mode bootloader must keep free for future application use without triggering a warning message

    public List<Section> ramSections = null;            // RAM use section list (includes decoded symbols and section data contents)
    public List<Section> romSections = null;            // ROM use section list (includes decoded symbols and section data contents)
    public List<Section> otherSections = null;          // Debug and other section list (includes decoded symbols and section data contents)

    // Live-Update items
    public int targetPartition = 0;                    // Specify 0 for non-dual boot capable device or to enable Single boot mode, specify 1 for Dual Boot mode with the code targetting the Active Partition or Partition 1, 2 for Daul Boot mode with the code targetting the Inactive Partition or Partition 2

    public static final String nullFile()
    {
        String os = System.getProperty("os.name");
        if((os == null) || os.startsWith("Windows"))
            return "NUL";
        else
            return "/dev/null";
    }

    public static final String ezblToolsExecPath()  // Not including any trailing slashes
    {
        String execPath = null;
        try
        {
            CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            execPath = jarFile.getAbsolutePath();
        }
        catch(URISyntaxException ex)
        {
            execPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        }
        try
        {
            File f = new File(execPath).getParentFile();
            if(f != null)
                execPath = f.getCanonicalPath();
        }
        catch(IOException ex)
        {
            execPath = "";
        }
        if(execPath == null)
            return "";

        return execPath;
    }

    public static final String ourJarPath()  // Inclusive of ezbl_tools.jar executable name or classpath string
    {
        try
        {
            CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            return jarFile.getAbsolutePath();
        }
        catch(URISyntaxException ex)
        {
            return Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        }
    }

    public EZBLState()
    {
        javaLaunchString = "$(MP_JAVA_PATH)java -cp \"" + ourJarPath() + "\" com.microchip.apps.ezbl.Main";

        // NOTE: all of these may resolve to null if not launched by MPLAB X IDE or make. These are reading makefile variables.
        compilerFolder = GetCanonicalPath(FixSlashes(TrimQuotes(System.getenv("MP_CC_DIR"))));
        projectName = FixSlashes(TrimQuotes(System.getenv("PROJECTNAME")));
        conf = FixSlashes(System.getenv("CONF"));
        javaPath = FixSlashes(TrimQuotes(System.getenv("MP_JAVA_PATH")));
        if(projectName != null)
        {
            elfPath = FixSlashes(TrimQuotes(System.getenv("DISTDIR"))) + File.separator + projectName + "." + TrimQuotes(System.getenv("IMAGE_TYPE")) + ".elf";
            hexPath = FixSlashes(TrimQuotes(System.getenv("DISTDIR"))) + File.separator + projectName + "." + TrimQuotes(System.getenv("IMAGE_TYPE")) + ".hex";
        }
        if(elfPath != null)
            artifactPath = elfPath;
        pathToIDEBin = GetCanonicalPath(FixSlashes(TrimQuotes(System.getenv("PATH_TO_IDE_BIN"))));
        partNumber = TrimQuotes(System.getenv("MP_PROCESSOR_OPTION"));
        if(partNumber != null)

            fullPartNumber = partNumber.startsWith("33") ? "dsPIC" + partNumber.toUpperCase() : "PIC" + partNumber.toUpperCase();
        linkScriptPath = FixSlashes(TrimQuotes(System.getenv("MP_LINKER_FILE_OPTION")));
        if(linkScriptPath != null)
            linkScriptPath = GetCanonicalPath(FixSlashes(TrimQuotes(linkScriptPath.replace(",--script=", ""))));  // Trim junk from Makefile string
        verbose = Boolean.getBoolean(TrimQuotes(System.getenv("EZBL_VERBOSE")));
        if(verbose)
        {
            System.out.printf("    javaLaunchString = %s\n", javaLaunchString);
            System.out.printf("    compilerFolder = %s\n", compilerFolder);
            System.out.printf("    projectName = %s\n", projectName);
            System.out.printf("    conf = %s\n", conf);
            System.out.printf("    javaPath = %s\n", javaPath);
            System.out.printf("    elfPath = %s\n", elfPath);
            System.out.printf("    hexPath = %s\n", hexPath);
            System.out.printf("    artifactPath = %s\n", artifactPath);
            System.out.printf("    pathToIDEBin = %s\n", pathToIDEBin);
            System.out.printf("    partNumber = %s\n", partNumber);
            System.out.printf("    fullPartNumber = %s\n", fullPartNumber);
            System.out.printf("    linkScriptPath = %s\n", linkScriptPath);
        }
    }

    @Override
    public EZBLState clone()
    {
        try
        {
            EZBLState stateClone = (EZBLState)(super.clone());
            // TODO: implement deep clone of mutable members? Don't really need this right now.
            return stateClone;
        }
        catch(CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    void SaveToFile()
    {
        // Remove non-persistent options so we don't try to use them later
        this.undecodedOptions = this.undecodedOptions.replaceAll("-dump[\\s]*?", "");                   // Used by Blobber only
        this.undecodedOptions = this.undecodedOptions.replaceAll("-generate_merge[\\s]*?", "");         // Used by Blobber only

        // Remove tons of section and symbol data that take forever to load. These will be regenerated when needed (with the symbol table being reparsed from one large symbol dump string).
        if(this.elfDump != null)
            this.elfDump.symbols = null;
        if(this.pass < 2)
        {
            this.elfDump = null;
            this.romUseRegions = null;
            this.ramUseRegions = null;
            this.ramUseSections = null;
            this.otherSections = null;
            this.ramSections = null;
            this.romSections = null;
        }
        this.gldMemories = null;
        this.devConfigWordsByName = null;

        File f = new File(this.ezblStateSavePath);
        File parentFolder = f.getParentFile();
        if(parentFolder != null)
        {
            if(!parentFolder.exists())
            {
                parentFolder.mkdirs();
            }
        }

        // Write everything to a binary file for later use in another stage
        ObjectOutputStream objectOut;
        FileOutputStream fileOut;
        try
        {
            fileOut = new FileOutputStream(this.ezblStateSavePath, false);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(this);
            objectOut.close();
            fileOut.close();
        }
        catch(IOException ex)
        {
            if(!this.silent)
                System.err.printf("ezbl_tools: Unable to save state information\n    %1$s\n", ex.getMessage());
        }
    }

    public static EZBLState ReadFromFile(String filename)
    {
        // Ordinary binary file saving/loading
        if(!(new File(filename).exists()))
        {
            return new EZBLState();
        }

        // Read everything from file
        ObjectInputStream objectIn;
        FileInputStream in;
        try
        {
            in = new FileInputStream(filename);
            objectIn = new ObjectInputStream(in);
            EZBLState ret = (EZBLState)objectIn.readObject();
            objectIn.close();
            in.close();

            // Repopulate the elfDump symbol table if it was saved only as unparsed string data (done to speed up loading, since class is complex to read, but quick to reparse)
            if((ret.elfDump != null) && (ret.elfDump.symbols == null) && (ret.elfDump.symbolTableAsString != null))
            {
                ret.elfDump.symbols = new ArrayList<>();
                int lastIndex = -1;
                int curIndex;
                while((curIndex = ret.elfDump.symbolTableAsString.indexOf('\n', lastIndex + 1)) >= 0)
                {
                    ret.elfDump.symbols.add(new Symbol(ret.elfDump.symbolTableAsString.substring(lastIndex + 1, curIndex)));
                    lastIndex = curIndex;
                }
            }

            if(ret.devConfigWordsByAddr != null)
            {
                ret.devConfigWordsByName = new TreeMap<>();
                for(MemoryRegion mr : ret.devConfigWordsByAddr.values())
                {
                    ret.devConfigWordsByName.put(mr.name, mr);
                }
            }

//            if(ret.generatedDataSizeByPass == null)
//                ret.generatedDataSizeByPass = new TreeMap<>();

            return ret;
        }
        catch(InvalidClassException ex)
        {
            System.out.println("ezbl_tools: " + filename + " contains incompatible state data; using new state data.");
            return new EZBLState();
        }
        catch(IOException | ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean LoadEDCData()
    {
        // Read edc (Essential Device Characteristics) information for the given
        // device from the pre-parsed EZBL device properties database 
        // (SQLitePartDatabase class + ezbl_tools.jar resource .csv files)
        if(this.partNumber != null)
        {
            SQLitePartDatabase db = new SQLitePartDatabase();
            EDCProperties dev = db.getDeviceProperties(this.fullPartNumber);
            if(dev == null)
                return false;

            this.hasFlashConfigWords = dev.hasFlashConfigWords;
            this.flashWordSize = dev.programBlockSize;
            this.setEraseBlockSizeAddresses(dev.eraseBlockSize);
            this.devIDAddr = this.MCU16Mode ? dev.devIDAddr : Multifunction.normalizePIC32Addr(dev.devIDAddr);
            this.devIDValue = dev.devID;
            this.devIDMask = dev.devIDMask;
            this.devRevAddr = this.MCU16Mode ? dev.revIDAddr : Multifunction.normalizePIC32Addr(dev.revIDAddr);
            this.devRevMask = dev.revIDMask;
            this.ivtVectors = db.getInterrupts(dev.partNumber);
            this.ivtVectorsImplemented = InterruptVector.GetImplementedVectorCount(this.ivtVectors.values());
            this.devSpecialConf.BACKBUGAddr = this.MCU16Mode ? dev.BACKBUGAddr : Multifunction.normalizePIC32Addr(dev.BACKBUGAddr);
            this.devSpecialConf.BACKBUGMask = dev.BACKBUGMask;
            this.devSpecialConf.BACKBUGConfigName = dev.BACKBUGRegName;
            this.devSpecialConf.reservedBitAddr = this.MCU16Mode ? dev.ReservedBitAddr : Multifunction.normalizePIC32Addr(dev.ReservedBitAddr);
            this.devSpecialConf.reservedBitMask = dev.ReservedBitAddr == 0 ? 0 : 1L << dev.ReservedBitPos;
            this.devSpecialConf.reserveBitConfigName = dev.ReservedBitRegName;
            this.devSpecialConf.codeProtectAddr = this.MCU16Mode ? dev.CodeProtectAddr : Multifunction.normalizePIC32Addr(dev.CodeProtectAddr);
            this.devSpecialConf.codeProtectMask = dev.CodeProtectMask;
            this.devSpecialConf.codeProtectConfigName = dev.CodeProtectRegName;

            this.devProgramSpaces = new ArrayList<>();
            this.devMemories = new ArrayList<>();
            this.devConfigWordsByAddr = new TreeMap<>();
            this.devConfigWordsByName = new TreeMap<>();
            this.devNVGeometry = new ArrayList<>();
            this.mainExecutionRegion = null;
            this.configWordsRegion = null;

            List<MemoryRegion> gldRegions = db.getMemories(SQLitePartDatabase.MemoryTable.GLDMemories, dev.partNumber, null, MemoryRegion.Partition.single);
            for(MemoryRegion mr : gldRegions)
            {
                if(mr.name.equals("FBOOT"))
                {
                    this.devSpecialConf.FBOOTAddr = (int)mr.startAddr;
                    this.devProgramSpaces.add(mr.clone());
                }
            }

            if(this.coreType != CPUClass.mm)
            {
                gldRegions = db.getMemories(SQLitePartDatabase.MemoryTable.GLDMemories, dev.partNumber, null, MemoryRegion.Partition.values()[this.targetPartition]);
                for(MemoryRegion mr : gldRegions)
                {
                    this.devProgramSpaces.add(mr.clone());
                }
            }
            List<MemoryRegion> configRanges = db.getMemories(SQLitePartDatabase.MemoryTable.DecodedConfigMemories, dev.partNumber, null, MemoryRegion.Partition.values()[this.targetPartition]);
            for(MemoryRegion mr : configRanges)
            {
                if((this.coreType == CPUClass.mm))
                    mr.normalizePIC32Addresses();
                this.devConfigWordsByAddr.put(mr.startAddr, mr);
                this.devConfigWordsByName.put(mr.name, mr);
            }
            List<MemoryRegion> bootloadableRanges = db.getMemories(SQLitePartDatabase.MemoryTable.BootloadableRanges, dev.partNumber, null, MemoryRegion.Partition.values()[this.targetPartition]);
            for(MemoryRegion mr : bootloadableRanges)
            {
                if((mr.type == MemType.OTP) || (mr.type == MemType.TEST) || mr.name.equalsIgnoreCase("bootcfg") || mr.name.equalsIgnoreCase("customerotp"))
                    continue;
                if((this.coreType == CPUClass.mm))
                    mr.normalizePIC32Addresses();

                if((mr.name.equals("program") || mr.name.equals("code")) && (mr.type == MemoryRegion.MemType.ROM))
                    this.mainFlashRegion = mr.clone();
                this.devNVGeometry.add(mr.clone());
            }
            List<MemoryRegion> specialRanges = db.getMemories(SQLitePartDatabase.MemoryTable.DeviceMemories, dev.partNumber, null, MemoryRegion.Partition.values()[this.targetPartition]);
            for(MemoryRegion mr : specialRanges)
            {
                devMemories.add(mr);
                if((this.coreType == CPUClass.mm))
                {
                    mr.normalizePIC32Addresses();
                    if(mr.name.equals("code"))
                        mr.name = "kseg0_program_mem";
                    if(mr.name.equals("bootconfig"))
                        mr.name = "kseg1_boot_mem";
                    if(mr.type == MemoryRegion.MemType.FLASHFUSE)
                        mr.name = "configsfrs";
                    this.devProgramSpaces.add(mr.clone());
                }

                // Find the boundaries of the Config words as flash words or test space fuses, excluding FBOOT, which is named "bootcfg" on 16-bit devices
                if(!mr.name.equals("bootcfg") && ((mr.type == MemoryRegion.MemType.BYTEFUSE) || (mr.type == MemoryRegion.MemType.FLASHFUSE) || mr.name.equals("cfgmem") || mr.name.equals("configsfrs") || mr.name.equals("config") || mr.name.equals("altconfig")))
                {
                    if(this.configWordsRegion == null)
                        this.configWordsRegion = mr.clone().alignToProgSize();
                    else
                    {
                        if(mr.startAddr < this.configWordsRegion.startAddr)
                            this.configWordsRegion.startAddr = mr.startAddr;
                        if(mr.endAddr > this.configWordsRegion.endAddr)
                            this.configWordsRegion.endAddr = mr.endAddr;
                    }
                }
                if((mr.name.equals("program") || mr.name.equals("kseg0_program_mem")) && (mr.type == MemoryRegion.MemType.ROM))
                {
                    this.mainExecutionRegion = mr.clone();
                }
            }

            // Fix up special config word addresses when a target partition has been selected
            if(this.targetPartition != 0)
            {
                if(this.devConfigWordsByName.containsKey(dev.BACKBUGRegName))
                    this.devSpecialConf.BACKBUGAddr = this.devConfigWordsByName.get(dev.BACKBUGRegName).startAddr;
                if(this.devConfigWordsByName.containsKey(dev.ReservedBitRegName))
                    this.devSpecialConf.reservedBitAddr = this.devConfigWordsByName.get(dev.ReservedBitRegName).startAddr;
                if(this.devConfigWordsByName.containsKey(dev.CodeProtectRegName))
                    this.devSpecialConf.codeProtectAddr = this.devConfigWordsByName.get(dev.CodeProtectRegName).startAddr;
            }

            if(!this.hasFlashConfigWords)
                this.allowLastPageErase = true;
            this.igtSpacing = (this.mainExecutionRegion.endAddr < 0x10000L) ? 0x2 : 0x4;
            this.edcInfoLoaded = true;

            // Set or validate the warnFreePSVSpace/minFreePSVSpace values to be 1/2
            // the maximum possible size and 1/4 the maximum possible size,
            // respectively, assuming not explicitly set on the command line.
            long maxPossiblePSVSpace = (this.mainFlashRegion != null) ? this.mainFlashRegion.endAddr - this.mainFlashRegion.startAddr : 0x8000L;
            if(maxPossiblePSVSpace > 0x8000L)
            {
                maxPossiblePSVSpace = 0x8000;
            }
            int reasonablePSVMin = (int)(maxPossiblePSVSpace - 0x1800) / 4;
            if(reasonablePSVMin < this.eraseBlockSizeAddresses)
                reasonablePSVMin = this.eraseBlockSizeAddresses;
            if(this.minFreePSVSpace == 0)
            {
                this.minFreePSVSpace = reasonablePSVMin;
            }
            if(this.warnFreePSVSpace == 0)
            {
                this.minFreePSVSpace = reasonablePSVMin * 2;
            }
            if(this.minFreePSVSpace > (int)(maxPossiblePSVSpace - 0x1800))    // Limit checking when device doesn't have much flash to begin with. There has to be at least some space for the Bootloader code.
                this.minFreePSVSpace = (int)(maxPossiblePSVSpace - 0x1800);
            if(this.warnFreePSVSpace > (int)(maxPossiblePSVSpace - 0x1000))
                this.warnFreePSVSpace = (int)(maxPossiblePSVSpace - 0x1000);
            this.edcInfoLoaded = true;
            return true;
        }

        return false;
    }

    /**
     * Creates a sorted and reduced List of simple AddressRanges that correspond
     * to all addresses contained in the given list of sections, . Abutting
     * sections result in a one larger AddressRange covering both.
     *
     * None of the existing lists are modified or sorted by this operation. The
     * ranges returned are new copies.
     *
     * @param sections      List of Sections to include in the returned ranges.
     *                      The sections must have the .loadMemoryAddress and
     *                      .size parameters set appropriately. The .size field
     *                      is in addresses. If not needed, specify null or an
     *                      empty List.
     *
     * @param dataRecords   List of DataRecords to include in the returned
     *                      ranges. The DataRecords must have the .address and
     *                      .getEndAddress() returning appropriate values (i.e.
     *                      .architecture16bit must be set right) . If not
     *                      needed specify null or an empty List.
     *
     * @param addressRanges List of AddressRanges to also include in the
     *                      returned ranges. If not needed specify null or an
     *                      empty List.
     *
     * @return Numerically sorted list of Addresses occupied by all elements in
     *         the lists.
     */
    public static List<AddressRange> computeSimpleUnion(List<Section> sections, List<DataRecord> dataRecords, List<AddressRange> addressRanges)
    {
        List<AddressRange> unionRanges = new ArrayList<>();

        if(sections != null)
        {
            for(Section sec : sections)
            {
                unionRanges.add(new AddressRange(sec.loadMemoryAddress, sec.loadMemoryAddress + sec.size));
            }
        }
        if(dataRecords != null)
        {
            for(DataRecord rec : dataRecords)
            {
                unionRanges.add(new AddressRange(rec.address, rec.getEndAddress()));
            }
        }
        if(addressRanges != null)
        {
            for(AddressRange ar : addressRanges)
            {
                unionRanges.add(new AddressRange(ar.startAddr, ar.endAddr));
            }
        }

        AddressRange.SortAndReduce(unionRanges);

        return unionRanges;
    }

    public int getEraseBlockSize()
    {
        return eraseBlockSize;
    }
    public void setEraseBlockSize(int instructionsPerEraseBlock)
    {
        this.eraseBlockSize = instructionsPerEraseBlock;
        this.eraseBlockSizeBytes = this.MCU16Mode ? instructionsPerEraseBlock * 3 : instructionsPerEraseBlock * 4;
        this.eraseBlockSizeAddresses = this.MCU16Mode ? this.eraseBlockSizeBytes / 3 * 2 : this.eraseBlockSizeBytes;
    }
    public int getEraseBlockSizeBytes()
    {
        return eraseBlockSizeBytes;
    }
    public void setEraseBlockSizeBytes(int eraseBlockSizeBytes)
    {
        this.eraseBlockSizeBytes = eraseBlockSizeBytes;
        this.eraseBlockSizeAddresses = this.MCU16Mode ? eraseBlockSizeBytes / 3 * 2 : eraseBlockSizeBytes;
        this.eraseBlockSize = this.MCU16Mode ? eraseBlockSizeBytes / 3 : eraseBlockSizeBytes / 4;
    }
    public int getEraseBlockSizeAddresses()
    {
        return eraseBlockSizeAddresses;
    }
    public void setEraseBlockSizeAddresses(int eraseBlockSizeAddresses)
    {
        this.eraseBlockSizeAddresses = eraseBlockSizeAddresses;
        this.eraseBlockSizeBytes = this.MCU16Mode ? eraseBlockSizeAddresses / 2 * 3 : eraseBlockSizeAddresses;
        this.eraseBlockSize = this.MCU16Mode ? this.eraseBlockSizeBytes / 3 : this.eraseBlockSizeBytes / 4;
    }

    public int getAppBootloadStateSize()
    {
        if(this.MCU16Mode)
            return 0xC; // 3x 48-bit flash words (18 bytes)
        return 0x18;    // 3x 64-bit flash words (24 bytes)
    }

    /**
     * Generates human readable string from the pass number, such as:
     * "1st", "2nd", "3rd", "4th", etc.
     *
     * @return pass.toString() + {"st", "nd", "rd", "th"}, whichever is
     *         appropriate
     */
    public String getPassNumStr()
    {
        return getPassNumStr(pass);
    }
    public String getPassNumStr(int passNumber)
    {
        return String.format("%d%s", passNumber, passNumber == 1 ? "st" : passNumber == 2 ? "nd" : passNumber == 3 ? "rd" : "th");
    }
}
