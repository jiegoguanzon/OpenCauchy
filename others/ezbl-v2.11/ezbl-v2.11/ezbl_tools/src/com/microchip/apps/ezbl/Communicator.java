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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Howard Schlunder
 */
public class Communicator
{
    ReadCOMThread readCOMThread = null;
    WriteCOMThread writeCOMThread = null;
    volatile boolean timeToClose = false;
    volatile Process comTool = null;
    volatile FileInputStream in = null;
    volatile FileOutputStream out = null;
    final ConcurrentLinkedQueue<byte[]> binaryAppToCOMData = new ConcurrentLinkedQueue<byte[]>();
    final ConcurrentLinkedQueue<byte[]> binaryCOMToAppData = new ConcurrentLinkedQueue<byte[]>();
    byte[] mainThreadRXResidual = null;
    EZBLState state = null;
    String comPort = null;
    int baudRate = 115200;
    String logFilePath = null;
    boolean keepLog = false;
    boolean skipHandshake = false;  // Not supported in EZBL v2.xx protocol
    long milliTimeout;              // Base timeout, in milliseconds, for general communications operations that require a response or activity. Timeout is normally measured from the last generated or measured TX/RX activity.

    public static Integer doBootload(String ezblCommExecPath, String artifactFilePath, String comPort, int baudRate, int i2cSlaveAddr, long milliTimeout, String logFile) throws IOException
    {
        ProcessBuilder comPB;
        Process comProcess;
        Integer returnCode = null;

        if(!(new File(artifactFilePath).exists()))
        {
            artifactFilePath += ".bl2";
            if(!(new File(artifactFilePath).exists()))
                return null;
        }

        if(logFile == null)
        {
            File f = File.createTempFile("EZBLComLog", ".txt");
            String newTempFileName = f.getParent();
            f.delete();
            if(newTempFileName == null)
                newTempFileName = "";
            if(!newTempFileName.endsWith(File.separator))
                newTempFileName += File.separator;
            newTempFileName += "ezbl_comm_log.txt";
            f = new File(newTempFileName);
            logFile = f.getCanonicalPath();
        }

        if(ezblCommExecPath == null)
        {
            File file = new File(System.getProperty("java.class.path"));
            ezblCommExecPath = file.getParent();
            if(ezblCommExecPath == null)
                ezblCommExecPath = ".";
            ezblCommExecPath += File.separatorChar + "ezbl_comm.exe";
            if(!new File(ezblCommExecPath).exists())
            {
                if(ezblCommExecPath.contains(";"))
                    ezblCommExecPath = "ezbl_integration" + File.separatorChar + "ezbl_comm.exe";
            }
        }
        if(!new File(ezblCommExecPath).exists())
        {
            if(logFile != null)
                Multifunction.UpdateFileIfDataDifferent(logFile, "Could not find ezbl_comm.exe needed for UART/I2C communications\n", true);
            return null;
        }

        comPB = new ProcessBuilder(ezblCommExecPath,
                                   String.format("-artifact=%s", artifactFilePath),
                                   String.format("-com=%s", comPort),
                                   String.format("-baud=%s", baudRate),
                                   String.format("-slave_address=0x%02X", i2cSlaveAddr),
                                   String.format("-timeout=%d", milliTimeout),
                                   String.format("-log=%s", logFile));
        comPB.inheritIO();
        comProcess = comPB.start();

        long startTime = System.currentTimeMillis();
        while((returnCode == null) && (startTime - System.currentTimeMillis() < milliTimeout))
        {
            try
            {
                returnCode = comProcess.exitValue();
            }
            catch(IllegalThreadStateException ex)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch(InterruptedException ex1)
                {
                    // Nothing wrong with waking up
                }
            }
        }
        if(returnCode == null)
        {   // Timed out
            comProcess.destroy();
            returnCode = -30303;
        }

        System.err.println();
        return returnCode;
    }

    public static void main(String[] args)
    {
        COMResult ret = new COMResult();

        boolean errorReturn = true;         // Flag indicating if we should display the return code from ezbl_comm.exe
        boolean displayPostUploadFeedback = false;  // Flag indicating if we should keep RX channel open for a few milliseconds after uploading a firmware image and display any incomming text RX data to the Output window. Useful for debugging or custom messages generated by the target.
        boolean handshakeDone = false;
        boolean legacyProtocol = false;     // Default is .bl2 bus mode protocol for half-duplex compatibility and multi-target node reception. legacyProtocol mode is for older .blob uploading to EZBL v1.07b- type devices.
        int baud;
        String preCommands = null;          // Not supported in EZBL v2.xx protocol
        String readArtifact = null;         // Not supported in EZBL v2.xx protocol
        String writeArtifact = null;
        String readArtifact2 = null;        // Not supported in EZBL v2.xx protocol
        String postCommands = null;         // Not supported in EZBL v2.xx protocol
        String alternateArtifact = null;
        String comInPath = null;
        String comOutPath = null;
        int exitCode = 0;
        String artifactExts[] =
        {
            "", ".bl2", ".blob", ".hex"
        };

        // Obtain the given command line options and all prior ezbl_tools invocation state information
        EZBLState state = CommandAndBuildState.ReadArgs(null, args); // Parse command line options

        // If no file specified upon execution, write usage information to STDOUT
        if(args.length == 0)
        {
            System.out.print("\r\n"
                             + Multifunction.FormatHelpText(79, 0 * 3, "Uploads a .hex/.bl2/.blob firmware file to an EZBL Bootloader or EZBL ICSP Programmer.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 0 * 3, "Usage:")
                             + Multifunction.FormatHelpText(79, 1 * 3, "java -jar ezbl_tools.jar -communicator -artifact=\"app_firmware.bl2\" -com=COMx [-baud=x] [-i2c_address=x] [-timeout=x] [-options]")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 1 * 3, "Inputs:")
                             + Multifunction.FormatHelpText(79, 2 * 3, "-com=COMx")
                             + Multifunction.FormatHelpText(79, 3 * 3, "Required parameter where, COMx, is the communications port or OS specific file path that the bootloader or EZBL ICSP programmer is connected to. In the case of Windows COM ports, the specified COM port is internally prefixed as necessary with \"\\\\.\\\" to ensure the target represents a communications port rather than a file on the local filesystem.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 3 * 3, "If targeting an MCP2221 device acting as an I2C master, specify -com=I2C. If there is more than one MCP2221 connected to your system, you can specify different instances of the MCP2221 by appending a number (from 0 to n) onto the I2C token. For example, if there are two MCP2221 devices attached to the system, try, -com=I2C0, and if that does not work, instead try, -com=I2C1.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "[-timeout=x]")
                             + Multifunction.FormatHelpText(79, 3 * 3, "Optional parameter specifying the communications timeout to use. 'x' specifies how long to wait for a response before terminating, in milliseconds. For most operations, the internal timeout counter is reset anytime data is transmitted back to us, so the optimal timeout to specify depends primarily on the latency characteristics of the communications channel and target device. The default timeout when this parameter is not specified is 1000ms. When using a high latency communications link, such as a Bluetooth SPP channel, there can be appreciable connection initiation delay and retransmission latency for lost packets. A timeout of 10000 ms or higher may be needed for such hardware.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "[-baud=x]")
                             + Multifunction.FormatHelpText(79, 3 * 3, "Where, x, is the communications port baud rate that communications should be attempted at. For UARTs, auto-baud will attempt to set the target Bootloader or EZBL ICSP programmer to match this chosen buad rate. For COM ports, this parameter is optional and will default to 115200 baud if omitted. For an MCP2221 I2C interface, the default I2C baud rate is 400kHz.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "[-i2c_address=x]")
                             + Multifunction.FormatHelpText(79, 3 * 3, "When using I2C, this option is required. x is the 7-bit I2C target slave address to communicate with. For example, if the slave is at 0x08, you would specify, -slave_address=0x08.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "[-save_temps=com-log.txt]")
                             + Multifunction.FormatHelpText(79, 3 * 3, "Optional parameter specifying that all communications data should be logged to a file called 'com-log.txt'.")
                             //+ "\r\n" // Read-back not a valid command in EZBL v2.xx+
                             //+ Multifunction.FormatHelpText(79, 2 * 3, "[-read=pre-program.blob]")
                             //+ Multifunction.FormatHelpText(79, 3 * 3, "Optional parameter specifying the target should be sent a read-back command before issuing any erase or programming commands. If the target supports this function, a file will be outputted, generated by the target. Normally, the file will be a .blob formatted file with no hash and will contain the entire contents of the Flash memory + addressable Config Words. If the output filename is not specified, the data returned will be printed to stdout, converted from bytes to the locally configured character set.")
                             + "\r\n"
                             + Multifunction.FormatHelpText(79, 2 * 3, "[-artifact=app_firmware[.bl2|.blob|.hex]]")
                             + Multifunction.FormatHelpText(79, 3 * 3, "Where, app_firmware[.bl2|.blob|.hex], is the file to send to the target hardware for programming. If a file extension is excluded, an attempt is made to auto-detect the most optimal available file extension by searching the parent folder for a file with an exact name match (i.e. no extension), a file with the \".bl2\" extension, a file with the \".blob\" extension, or a file with the \".hex\" extension. .hex files are converted internally to a .blob format before COM transmission.")
                             //+ "\r\n" // Read-back not a valid command in EZBL v2.xx+
                             //+ Multifunction.FormatHelpText(79, 2 * 3, "[-read2=post-program.blob]")
                             //+ Multifunction.FormatHelpText(79, 3 * 3, "Optional parameter specifying the target should be sent a read-back command after finished with the erase and programming steps. If the target supports this function, a file will be outputted, generated by the target. Normally, the file will be a .blob formatted file with no hash and will contain the entire contents of the Flash memory + addressable Config Words. If the output filename is not specified, the data returned will be printed to stdout, converted from bytes to the locally configured character set.")
                             + "\r\n"
                             + "\r\n"
            );

            //PrintCOMPorts(System.out);  // On Windows only, display the available COM ports when displaying other help data. Commented out since it crashes the JRE when jna.jar isn't distributed (which we would like to avoid)
            exitCode = -1;
            return;
        }

        try
        {
            Communicator.doBootload(null, state.artifactPath, state.comPort, Integer.decode(state.baudRate), state.slaveAddress, state.milliTimeout, state.saveTempsFile);
            System.exit(0);
        }
        catch(IOException ex)
        {
            System.exit(-30304);
        }

        // Search for, and if found, extract and decode -auto_baud_retries=x, -command_prefix=x, etc. command line parameters
        List<String> argTokens = new ArrayList<>();

        argTokens.addAll(Arrays.asList(args));
        for(String token : argTokens)
        {
            String lowerToken = token.toLowerCase();

            if(lowerToken.startsWith("-auto_baud_retries="))    // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                state.autoBaudRetryLimit = Integer.decode(Multifunction.TrimQuotes(token.substring("-auto_baud_retries=".length())));
            }
            else if(lowerToken.startsWith("-com_in="))          // For future use
            {
                comInPath = Multifunction.TrimQuotes(token.substring("-com_in=".length()));
            }
            else if(lowerToken.startsWith("-com_out="))         // For future use
            {
                comOutPath = Multifunction.TrimQuotes(token.substring("-com_out=".length()));
            }
            else if(lowerToken.startsWith("-save-temps") || lowerToken.startsWith("-save_temps") || lowerToken.startsWith("-log"))   // Save temporary files generated by ezbl_tools.jar operations
            {
                state.saveTemps = true;
                if(lowerToken.startsWith("-save-temps=") || lowerToken.startsWith("-save_temps="))  // Use the given filename for log data, if file path specified
                {
                    state.saveTempsFile = Multifunction.TrimQuotes(token.substring("-save_temps=".length()));
                }
                else if(lowerToken.startsWith("-log="))                                             // Use the given filename for log data, if file path specified (alternate '-log=' name)
                {
                    state.saveTempsFile = Multifunction.TrimQuotes(token.substring("-log=".length()));
                }
            }
            else if(lowerToken.startsWith("-command_prefix="))  // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                state.commandPrefix = Multifunction.TrimQuotes(token.substring("-command_prefix=".length()));
            }
            else if(lowerToken.startsWith("-skip_handshake"))   // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                state.skipComHandshake = true;
            }
            else if(lowerToken.startsWith("-precommands="))     // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                preCommands = Multifunction.TrimQuotes(token.substring("-precommands=".length()));
                if(preCommands.isEmpty())
                {
                    preCommands = null;
                }
            }
            else if(lowerToken.startsWith("-read="))            // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                readArtifact = Multifunction.TrimQuotes(token.substring("-read=".length()));
                if(readArtifact.isEmpty())
                {
                    readArtifact = null;
                }
            }
            else if(lowerToken.startsWith("-artifact="))        // Main .bl2/.blob/.hex file to send to the target bootloader
            {
                writeArtifact = Multifunction.TrimQuotes(token.substring("-artifact=".length()));
            }
            else if(lowerToken.startsWith("-read2="))           // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                readArtifact2 = Multifunction.TrimQuotes(token.substring("-read2=".length()));
                if(readArtifact2.isEmpty())
                {
                    readArtifact2 = null;
                }
            }
            else if(lowerToken.startsWith("-postcommands="))    // Not supported in EZBL v2.xx protocol
            {
                legacyProtocol = true;
                postCommands = Multifunction.TrimQuotes(token.substring("-postcommands=".length()));
                if(postCommands.isEmpty())
                {
                    postCommands = null;
                }
            }
            else if(lowerToken.startsWith("-v1protocol"))
            {
                legacyProtocol = true;
            }
            else if(lowerToken.startsWith("-feedback")) // Post upload RX printing to Output window/stdout
            {
                displayPostUploadFeedback = true;
            }
            else if(!lowerToken.startsWith("-"))
            {
                File f = new File(token);
                if(f.canRead())
                {
                    alternateArtifact = token;
                }
            }
        }

        // If no -artifact= specified, but we found a readable filename without the prefix, assuming it is an artifact for upload.
        if((writeArtifact != null) && (writeArtifact.isEmpty()))
        {
            writeArtifact = null;
        }
        if((writeArtifact == null) && (alternateArtifact != null))
        {
            writeArtifact = alternateArtifact;
        }

        // Validate the -artifact= file name is a file, and if it doesn't exist,
        // try the list of known extensions to append in preferential order of ".bl2" -> ".blob" -> ".hex"
        if((writeArtifact != null) && (!writeArtifact.isEmpty()))
        {
            for(String ext : artifactExts)
            {
                File f = new File(writeArtifact + ext);
                if(f.canRead())
                {
                    writeArtifact += ext;
                    break;
                }
            }
        }
        state.artifactPath = writeArtifact;

        if(!legacyProtocol && ((writeArtifact == null) || writeArtifact.isEmpty()))
        {
            System.err.println("EZBL communicator error: missing required -artifact=[file.bl2|file.blob|file.hex] parameter");
            System.exit(-1);
            return;
        }

        // Write output messages to stderr (red text in MPLAB X IDE) instead of default stdout (black text with possible reordering)
        System.setOut(System.err);      // Permanent change; needed to maintain print order between stdout and stderr and allow MPLAB X to display status dots as they are created in the Output IDE window. Using stdout will display using default black text and not in order/immediately displayed.

        // Add the \\.\ prefix needed for Windows to recognize COM10+ as communications ports and not file names locally on the hard disk
        if((state.comPort != null) && (state.comPort.startsWith("COM") && (state.comPort.length() >= 5)))
        {
            state.comPort = "\\\\.\\" + state.comPort;
        }
        if((comInPath != null) && (comInPath.startsWith("COM") && (comInPath.length() >= 5)))
        {
            comInPath = "\\\\.\\" + comInPath;
        }
        if((comOutPath != null) && (comOutPath.startsWith("COM") && (comOutPath.length() >= 5)))
        {
            comOutPath = "\\\\.\\" + comOutPath;
        }

        Communicator comPort = null;

        try
        {
            if(state.comPort == null)
            {
                exitCode = -2;
                throw new IOException("missing required -com=[target] parameter");
            }

            if(state.baudRate != null)
                baud = Integer.decode(state.baudRate);
            else if(state.comPort.toLowerCase().contains("i2c"))
                baud = 400000;
            else
                baud = 115200;
            comPort = new Communicator(null, null, state.comPort, baud, state.slaveAddress, state.saveTempsFile);
            comPort.skipHandshake = state.skipComHandshake || !legacyProtocol;
            comPort.keepLog = state.saveTemps;
            comPort.milliTimeout = (long)state.milliTimeout;
            comPort.state = state;  // TODO: Remove this eventually when no more internal variables are needed

            if(comInPath != null)   // If communicator read path changed from default "\\\\.\\pipe\\ezbl_pipe_in_from_com", assign it
            {
                comPort.readCOMThread.fileIOPath = comInPath;
            }
            if(comOutPath != null)  // If communicator write path changed from default "\\\\.\\pipe\\ezbl_pipe_out_to_com", assign it
            {
                comPort.writeCOMThread.fileIOPath = comOutPath;
            }
            long threadStartNanoTime = System.nanoTime();
            comPort.readCOMThread.startNanoTimeRef = threadStartNanoTime;
            comPort.writeCOMThread.startNanoTimeRef = threadStartNanoTime;
            comPort.readCOMThread.start();
            comPort.writeCOMThread.start();

            if(legacyProtocol)  // EZBL v1.xx only using .blob files - not tested in v2.xx distributions as v2.xx protocol and .bl2 files used exclusively
            {
                handshakeDone = comPort.skipHandshake;
                if(!comPort.skipHandshake)
                {
                    ret = comPort.HandshakeWithTarget();
                    handshakeDone = !ret.error;
                    if(ret.error)
                    {
                        exitCode = -3;
                        throw new IOException(ret.responseText);
                    }
                    System.out.println(ret.responseText);
                }

                // Send any extra commands specified by the -precommands command option
                if(preCommands != null)
                {
                    ret = comPort.SendCommand(preCommands, 0, 0);
                    if(ret.error)
                    {
                        exitCode = -4;
                        throw new IOException(ret.responseText);
                    }
                }

                // Do a pre-bootload CRC to decide what is in this device first, if requested
                if(state.undecodedOptions.contains("-precrc"))
                {
                    ret = comPort.GetTargetCRC(0, handshakeDone | comPort.skipHandshake);
                    if(ret.error)
                    {
                        exitCode = -5;
                        throw new IOException(ret.responseText);
                    }
                    System.out.println("CRC32 returned from target: " + ret.responseText);
                }

                if(readArtifact != null)    // Do a device Flash read-back
                {
                    ret = comPort.ReadDeviceFlash(readArtifact, handshakeDone | comPort.skipHandshake);
                    if(ret.error)
                    {
                        exitCode = -6;
                        throw new IOException(ret.responseText);
                    }
                    handshakeDone = true;
                }
            }

            // Erase and program device (v1.xx and v2.xx)
            if(writeArtifact != null)
            {
                if(legacyProtocol)
                {
                    ret = comPort.Bootload(state.artifactPath, handshakeDone | comPort.skipHandshake);  // EZBL v1.xx
                }
                else
                {
                    ret = comPort.BootloadBL2(state.artifactPath, true);                                // EZBL v2.xx
                }
                if(ret.error)
                {
                    exitCode = -7;
                    throw new IOException(ret.responseText);
                }
                handshakeDone = false;
            }

            // EZBL v1.xx only using .blob files - not tested in v2.xx distributions as v2.xx protocol and .bl2 files used exclusively
            if(legacyProtocol)
            {
                if(readArtifact2 != null)   // Do a device Flash read-back
                {
                    ret = comPort.ReadDeviceFlash(readArtifact2, handshakeDone | comPort.skipHandshake);
                    if(ret.error)
                    {
                        exitCode = -8;
                        throw new IOException(ret.responseText);
                    }
                    handshakeDone = true;
                }

                // Send any extra commands specified by the -postcommands command option
                if(postCommands != null)
                {
                    ret = comPort.SendCommand(postCommands, 0, 0);
                    if(ret.error)
                    {
                        exitCode = -9;
                        throw new IOException(ret.responseText);
                    }
                }

                // Do a post CRC read back if requested (not needed since bootloader
                // does verification internally, but useful for tracking purposes)
                if(state.undecodedOptions.contains("-postcrc"))
                {
                    ret = comPort.GetTargetCRC(0, handshakeDone | comPort.skipHandshake);
                    if(ret.error)
                    {
                        exitCode = -10;
                        throw new IOException(ret.responseText);
                    }
                    System.out.println("CRC32 returned from target: " + ret.responseText);
                }
            }
        }
        catch(IOException ex)
        {
            errorReturn = !ret.fileError;   // Supress ezbl_comm.exe return code display if it was a local artifact path or pasing error
            System.err.print("EZBL communications error: " + ex.getMessage());
            if(!ex.getMessage().endsWith("\n"))
            {
                System.err.println();
            }
            if(exitCode == 0)
            {
                exitCode = -100;
            }
            if((comPort != null) && (comPort.logFilePath != null))
            {
                if(new File(comPort.logFilePath).canRead())
                {
                    System.err.printf("%1$-13sLog saved to: %2$s\n", " ", comPort.logFilePath);
                }
            }
        }

        // Dump any incoming data until no new newline characters are seen for
        // 100ms or a total time of milliTimeout (-timeout=x) elapses, whichever
        // occurs first
        //if((comPort != null) && (errorReturn == false) && displayPostUploadFeedback)
        if((comPort != null) && displayPostUploadFeedback)
        {
            long startTime = System.currentTimeMillis();
            long lastNewLine = System.currentTimeMillis();
            while((System.currentTimeMillis() - startTime < comPort.milliTimeout) && (System.currentTimeMillis() - lastNewLine < 100))
            {
                ret = comPort.ReadResponse(1, 100);
                if(ret.error || ret.asyncClose)
                {
                    //System.out.printf("\nClosing communications (code = %1$b, asyncClose = %2$b, fileError = %3$b, timedOut = %4$b, responseText = '%5$s')\n", ret.code, ret.asyncClose, ret.fileError, ret.timedOut, ret.responseText);
                    break;
                }
                if(ret.responseBinary[0] != 0x00)    // Don't print null string terminators
                {
                    System.out.print(String.valueOf((char)ret.responseBinary[0]));
                }
                if(ret.responseBinary[0] == '\n')
                {
                    lastNewLine = System.currentTimeMillis();
                }
            }
        }

        if(comPort != null)
        {
            comPort.Close(errorReturn);
        }
    }


    private class ReadCOMThread extends Thread
    {
        String fileIOPath = "\\\\.\\pipe\\ezbl_pipe_in_from_com";
        long startNanoTimeRef;

        @Override
        public void run()
        {
            long nanoTimeout = 1 * 1000 * 1000 * 1000;
            long startTime = System.nanoTime();
            byte[] data = new byte[512];

            // Debug logging
//            HashMap<Integer, byte[]> logData = new HashMap<Integer, byte[]>();
//            FileOutputStream log = null;
//            File f = new File("ezbl_ReadCOMThread_log.txt");
//            if(f.exists())
//            {
//                f.delete();
//            }
//            try
//            {
//                log = new FileOutputStream(f);
//                log.write(String.format("File Opened @ %1$d\n\n", startNanoTimeRef).getBytes());
//            }
//            catch(FileNotFoundException ex)
//            {
//            }
//            catch(IOException ex)
//            {
//            }
            // Try to open read pipe in Java
            // ezbl_comm.exe tool must have a chance to launch and open the pipes
            while(!timeToClose && (in == null))
            {
                try
                {
                    in = new FileInputStream(fileIOPath);
                }
                catch(FileNotFoundException ex)
                {
                    if(System.nanoTime() - startTime >= nanoTimeout)
                    {
                        timeToClose = true;
                        return;
                    }
                    try
                    {
                        // Test if the ezbl_comm.exe process has self terminated,
                        // if so, we should not wait for our own timeout.
                        // Obviously, no process means no pipe.
                        comTool.exitValue();

                        // If we get down here, indeed ezbl_comm.exe has self-terminated.
                        timeToClose = true;
                        return;
                    }
                    catch(IllegalThreadStateException okayEx)
                    {
                        // This is a good response. It means ezbl_comm.exe is
                        // still running, so we have hope for the pipe to open.
                    }
                }
                try
                {
                    Thread.sleep(2);
                }
                catch(InterruptedException ex)
                {
                }
            }

            try
            {
                while(!timeToClose)
                {
                    int len = in.available();
                    if(len <= 0)    // Go to sleep for a bit if nothing available right now
                    {
                        try
                        {
                            //Thread.sleep(0, 500000);    // 500us sleep duration
                            Thread.sleep(1);    // 1ms sleep duration
                        }
                        catch(InterruptedException ex)
                        {
                        }
                        continue;
                    }

                    // Debug logging
                    //int dataAvailableTime = (int)((System.nanoTime() - startNanoTimeRef) / 1000);
                    // Read the available bytes from the pipe
//                    int rByte = in.read();  // Should be a blocking read, but doesn't actually block on pipe read
//                    if(rByte < 0)
//                    {// EOF reached
//                        timeToClose = true;
//                        continue;
//                    }
//                    int len = 1 + in.available();    // Is there any more data available?
//                    byte[] data = new byte[len];
//                    data[0] = (byte)rByte;
//                    if(len > 1)
//                    {
//                        int readLen = in.read(data, 1, len - 1);
//                        if(readLen < 0)
//                        {
//                            timeToClose = true;
//                            continue;
//                        }
//                        if(readLen + 1 != data.length)  // Truncate the data if the read for some reason returned less data than we were expecting
//                        {
//                            data = Arrays.copyOf(data, readLen + 1);
//                        }
//                    }
                    if(len > data.length)
                    {
                        len = data.length;
                    }
                    len = in.read(data, 0, len);
                    if(len < 0) // Read failure
                    {
                        timeToClose = true;
                        break;
                    }

                    synchronized(binaryCOMToAppData)
                    {
                        binaryCOMToAppData.add(Arrays.copyOf(data, len));
                        binaryCOMToAppData.notifyAll();
                    }
                    // Debug logging
                    //logData.put(dataAvailableTime, data);
                }
            }
            catch(IOException ex)   // ezbl_comm.exe closed the pipe due to a com error
            {
                timeToClose = true; // Notify WriteCOMThread
            }

            // Debug logging
//            if(log != null)
//            {
//                try
//                {
//                    Set<Integer> times = logData.keySet();
//                    Integer[] times2 = times.toArray(new Integer[0]);
//                    Arrays.sort(times2);
//                    for(Integer t : times2)
//                    {
//                        byte[] data = logData.get(t);
//                        int seconds = (int)(t / 1000000);
//                        t -= seconds * 1000000;
//                        String dataOut = String.format("\n%1$d.%2$06d: RX %3$2d: ", seconds, t, data.length);
//                        log.write(dataOut.getBytes());
//                        log.write(Multifunction.bytesToHex(data).getBytes());
//                    }
//                    log.close();
//                }
//                catch(IOException ex)
//                {
//                }
//            }
        }
    }


    private class WriteCOMThread extends Thread
    {
        String fileIOPath = "\\\\.\\pipe\\ezbl_pipe_out_to_com";
        long startNanoTimeRef;

        @Override
        public void run()
        {
            long nanoTimeout = 1 * 1000 * 1000 * 1000;
            long startTime = System.nanoTime();

            // Try to open write pipe in Java
            // ezbl_comm.exe tool must have a chance to launch and open the pipes
            while(out == null)
            {
                try
                {
                    out = new FileOutputStream(fileIOPath);
                }
                catch(FileNotFoundException ex)    // Exception means ezbl_comm.exe has not launched yet and created the \\.\pipe\ezbl_pipe_out_to_com named pipe file yet. Being asynchronous and requiring OS task switching, this is normal for the first few hundred milliseconds or so, depending on system load.
                {
                    if(System.nanoTime() - startTime >= nanoTimeout)
                    {
                        timeToClose = true;
                        return;
                    }
                    try
                    {
                        // Test if the ezbl_comm.exe process has self terminated,
                        // if so, we should not wait for our own timeout.
                        // Obviously, no process means no pipe.
                        comTool.exitValue();

                        // If we get down here, indeed ezbl_comm.exe has self-terminated.
                        timeToClose = true;
                        return;
                    }
                    catch(IllegalThreadStateException okayEx)
                    {
                        // This is a good response. It means ezbl_comm.exe is
                        // still running, so we have hope for the pipe to open.
                    }
                }
                try
                {
                    Thread.sleep(2);
                }
                catch(InterruptedException ex)
                {
                }
                if(timeToClose)
                {
                    return;
                }
            }

//            // Debug logging
//            HashMap<Integer, byte[]> logData = new HashMap<Integer, byte[]>();
//            FileOutputStream log = null;
//            File f = new File("ezbl_WriteCOMThread_log.txt");
//            if(f.exists())
//            {
//                f.delete();
//            }
//            try
//            {
//                log = new FileOutputStream(f);
//                log.write(String.format("File Opened @ %1$d\n\n", startNanoTimeRef).getBytes());
//            }
//            catch(FileNotFoundException ex)
//            {
//            }
//            catch(IOException ex)
//            {
//            }
            // Loop writing data to the COM
            while(!timeToClose)
            {
                synchronized(binaryAppToCOMData)
                {
                    if(binaryAppToCOMData.isEmpty())
                    {
                        try
                        {
                            binaryAppToCOMData.wait();
                        }
                        catch(InterruptedException ex)
                        {
                            continue;
                        }
                    }
                }

                try
                {
                    byte[] writeData = binaryAppToCOMData.poll();
                    if(writeData != null)
                    {
                        //int t = (int)((System.nanoTime() - startNanoTimeRef) / 1000); // Debug logging
                        //logData.put(t, writeData);
                        out.write(writeData);
                    }
                }
                catch(IOException ex)
                {
                    timeToClose = true;
                }
            }

            // Debug logging
//            if(log != null)
//            {
//                try
//                {
//                    Set<Integer> times = logData.keySet();
//                    Integer[] times2 = times.toArray(new Integer[0]);
//                    Arrays.sort(times2);
//                    for(Integer t : times2)
//                    {
//                        byte[] data = logData.get(t);
//                        int seconds = (int)(t / 1000000);
//                        t -= seconds * 1000000;
//                        String dataOut = String.format("\n%1$d.%2$06d: TX %3$2d: ", seconds, t, data.length);
//                        log.write(dataOut.getBytes());
//                        log.write(Multifunction.bytesToHex(data).getBytes());
//                    }
//                    log.close();
//                }
//                catch(IOException ex)
//                {
//                }
//            }
        }
    }

    public static void PrintCOMPorts(PrintStream out)
    {
        try
        {
            List<String> comPorts = OSSpecific.EnumDosDevices();
            System.out.println("   QueryDosDevice() Windows API reports these COM ports:");
            Collections.sort(comPorts);
            for(int i = 0; i < comPorts.size(); i++)
            {
                String s = comPorts.get(i);
                if(!s.contains("COM"))
                {
                    comPorts.remove(i--);
                    continue;
                }
                out.print(Multifunction.FormatHelpText(79, 2 * 3, s));
            }
        }
        catch(RuntimeException e)
        {
            // OS might not be Windows - we don't implement enumerating other OS COM ports
        }
    }

    /**
     * Attempts to obtain a list of all valid COM ports reported on the system.
     *
     * This function presently only supports the Windows OS. Unsupported
     * platforms or in the event of an error, a null reference will be returned.
     *
     * NOTE: this function will cause a run-time crash if the lib\jna.jar file
     * isn't present in the ezbl_tools.jar distribution folder, so do not call
     * call this API if not distributing a copy of jna.jar. This error doesn't
     * appear trappable.
     *
     * @return List of Strings where each entry corresponds to one system COM
     *         port path. Note COM ports >= 10 require a "\\.\" to be prefixed
     *         on them for typical CreateFile() I/O against them.
     */
    public static List<String> EnumCOMPorts()
    {
        List<String> comPorts = null;
        try
        {
            comPorts = OSSpecific.EnumDosDevices();
            if(comPorts == null)
            {
                return null;
            }
            Collections.sort(comPorts);
            for(int i = 0; i < comPorts.size(); i++)
            {
                String s = comPorts.get(i);
                if(!s.contains("COM"))
                {
                    comPorts.remove(i--);
                    continue;
                }
            }
        }
        catch(RuntimeException e)
        {
            // OS might not be Windows - we don't implement enumerating other OS COM ports
        }

        return comPorts;
    }

    // EZBL v2.xx code. EZBL v1.xx code uses the below Communicator(EZBLState) function instead.
    public Communicator(String ezblCommExecPath, String artifactFilePath, String comPort, int baudRate, int i2cSlaveAddr, String logFile) throws IOException
    {
        this.comPort = comPort;
        this.baudRate = baudRate;

        this.logFilePath = logFile;
        if(this.logFilePath == null)
        {
            File f = File.createTempFile("EZBLComLog", ".txt");
            String newTempFileName = f.getParent();
            f.delete();
            if(newTempFileName == null)
            {
                newTempFileName = "";
            }
            if(!newTempFileName.endsWith(File.separator))
            {
                newTempFileName += File.separator;
            }
            newTempFileName += "ezbl_comm_log.txt";
            f = new File(newTempFileName);
            this.logFilePath = f.getCanonicalPath();
        }

        String execPath = ezblCommExecPath;
        if(ezblCommExecPath == null)
        {
            File file = new File(System.getProperty("java.class.path"));
            execPath = file.getParent();
            if(execPath == null)
            {
                execPath = ".";
            }
            execPath += File.separatorChar + "ezbl_comm.exe";
            if(!new File(execPath).exists())
            {
                if(execPath.contains(";"))
                {
                    execPath = "ezbl_integration" + File.separatorChar + "ezbl_comm.exe";
                }
            }
        }
        if(!new File(execPath).exists())
        {
            Multifunction.UpdateFileIfDataDifferent(logFilePath, "Could not find ezbl_comm.exe needed for UART/I2C communications\n", true);
            throw new IOException("Could not find ezbl_comm.exe needed for UART/I2C communications");
        }

        String cmdArgs[] = new String[2];
        cmdArgs[0] = execPath;
        cmdArgs[1] = ((artifactFilePath != null) ? "-artifact=" + artifactFilePath : "") + " -com=" + comPort + ((baudRate != 0) ? " -baud=" + String.valueOf(baudRate) : "") + " -slave_address=" + String.valueOf(i2cSlaveAddr) + " -log=\"" + this.logFilePath + "\"";
        comTool = Runtime.getRuntime().exec(cmdArgs[0] + " " + cmdArgs[1]);
        comTool.getErrorStream().close();   // Close all the STDIO streams to the process so its threads don't get suspended when we don't read from them.
        comTool.getInputStream().close();
        comTool.getOutputStream().close();

        readCOMThread = new ReadCOMThread();
        writeCOMThread = new WriteCOMThread();
        try
        {
            readCOMThread.setPriority(Thread.MAX_PRIORITY - 2);
            writeCOMThread.setPriority(Thread.MAX_PRIORITY - 2);
        }
        catch(Exception ex)
        {// Oh well, it's just performance/efficiency
        }
    }

    // EZBL v1.xx version. EZBL v2.xx code uses the above Communicator(String, String, String, int, int, String) function instead.
    @Deprecated
    public Communicator(EZBLState state) throws IOException
    {
        this.state = state;
        this.comPort = state.comPort;

        if(state.baudRate != null)
            this.baudRate = Integer.decode(state.baudRate);
        else if(this.comPort.toLowerCase().contains("i2c"))
            this.baudRate = 400000;
        else
            this.baudRate = 115200;

        this.logFilePath = state.saveTempsFile;
        if(this.logFilePath == null)
        {
            File f = File.createTempFile("EZBLComLog", ".txt");
            String newTempFileName = f.getParent();
            f.delete();
            if(newTempFileName == null)
            {
                newTempFileName = "";
            }
            if(!newTempFileName.endsWith(File.separator))
            {
                newTempFileName += File.separator;
            }
            newTempFileName += "ezbl_comm_log.txt";
            f = new File(newTempFileName);
            this.logFilePath = f.getCanonicalPath();
        }

        File file = new File(System.getProperty("java.class.path"));
        String execPath = file.getParent();
        if(execPath == null)
        {
            execPath = ".";
        }
        execPath += File.separatorChar + "ezbl_comm.exe";
        if(execPath.contains(";"))
        {
            execPath = "ezbl_integration" + File.separatorChar + "ezbl_comm.exe";
        }

        String cmdArgs[] = new String[2];
        cmdArgs[0] = execPath;
        cmdArgs[1] = "-artifact=" + state.artifactPath + " -com=" + state.comPort + ((state.baudRate != null) ? " -baud=" + state.baudRate : "") + " -slave_address=" + String.valueOf(state.slaveAddress) + " -log=\"" + this.logFilePath + "\"";
        comTool = Runtime.getRuntime().exec(cmdArgs[0] + " " + cmdArgs[1]);
        comTool.getErrorStream().close();   // Close all the STDIO streams to the process so its threads don't get suspended when we don't read from them.
        comTool.getInputStream().close();
        comTool.getOutputStream().close();

        readCOMThread = new ReadCOMThread();
        writeCOMThread = new WriteCOMThread();
        try
        {
            readCOMThread.setPriority(Thread.MAX_PRIORITY - 2);
            writeCOMThread.setPriority(Thread.MAX_PRIORITY - 2);
        }
        catch(Exception ex)
        {// Oh well, it's just performance/efficiency
        }
    }

    @Override
    public final void finalize() throws Throwable
    {
        Close(false);           // Be sure and close the ezbl_comm.exe process if it is open while we are terminating ourselves
        if(!this.keepLog && (this.logFilePath != null))   // Clean up temporary communications log
        {
            new File(this.logFilePath).deleteOnExit();
        }
        super.finalize();
    }

    /**
     * Closes the communications threads and terminates the ezbl_comm.exe
     * process. This function may block for up to 500ms as the ezbl_comm.exe
     * process closes or is killed.
     *
     * @return 3131, -3131, or the process return code from ezbl_comm.exe. 3131
     *         indicates the ezbl_comm.exe process was already closed. -3131
     *         indicates the ezbl_comm.exe process took longer than 500ms to
     *         self-close, so we decided to kill it instead of keep waiting.
     */
    public int Close(boolean printErrorCodes)
    {
        int ret = 0;

        timeToClose = true; // Flag to tell the read and write threads to terminate, which will also cause the ezbl_comm.exe process to terminate by seeing the pipes get disconnected.

        if(in != null)
        {
            try
            {
                readCOMThread.interrupt();
                in.getChannel().close();    // Trigger an AsynchronousCloseException to stop any blocking read in progress
                in.close();
            }
            catch(IOException ex)
            {
            }
        }
        if(out != null)
        {
            try
            {
                writeCOMThread.interrupt();
                out.getChannel().close();   // Trigger an AsynchronousCloseException to stop any blocking write in progress
                out.close();
            }
            catch(IOException ex)
            {
            }
        }
        if(comTool != null)
        {   // Wait for ezbl_comm.exe process to terminate and get return code. Do not use waitFor() since the function doesn't exist in Java 1.7
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - startTime < this.milliTimeout / 2)
            {
                try
                {
                    ret = comTool.exitValue();
                    comTool = null;
                    break;
                }
                catch(IllegalThreadStateException ex)
                {
                    // App not closed yet, wait for an additional 10ms and try again
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch(InterruptedException ie)
                    {
                        // Nothing to do, waking up is fine
                    }
                }
            }
            if(comTool != null)
            {
                // No luck having the process die on its own after
                // 500ms, let's kill it forcefully. This can happen if
                // the ezbl_comm.exe process is not responding right now
                // because it is busy with a blocking OS file I/O call,
                // the underlying com hardware driver isn't very
                // friendly, or the OS never gave sufficient CPU time to
                // the ezbl_comm.exe process. In such cases, we don't
                // want ourself to hang, nor do we want to leave the
                // headless ezbl_comm.exe process running in the
                // background, so we will just command the OS to kill
                // it.
                comTool.destroy();
                ret = -3131;
                comTool = null;
            }

            if(printErrorCodes && (ret != 0))
            {
                String errorReturnText = String.format("ezbl_comm.exe returned error code %1$d:\n", ret);
                switch(ret)
                {
                    case -1:
                    case -3:
                        errorReturnText += Multifunction.FormatHelpText(150, 4, "Prior instance of ezbl_comm.exe is already running.\n");
                        break;
                    case 1:
                        errorReturnText += Multifunction.FormatHelpText(150, 4, String.format("Could not open \"%1$s\". Ensure the communications port exists and is not already in use by another application.\n", this.comPort.replace("\\\\.\\COM", "COM")));
                        break;
                    case 2:
                        errorReturnText += Multifunction.FormatHelpText(150, 4, String.format("Specified I2Cx port not found (check -com=\"%1$s\" parameter)\n", this.comPort));
                        break;
                    case 3:
                    case 4:
                    case 5:
                        errorReturnText += Multifunction.FormatHelpText(150, 4, "MCP2221DLL API error return\n");
                        break;
                    case -3131:
                        errorReturnText = Multifunction.FormatHelpText(150, 4, "ezbl_comm.exe did not close normally. The selected COM port may be invalid or requires a longer timeout.");
                        break;
                    default:
                }
                System.err.printf(errorReturnText);
            }
        }
        in = null;
        out = null;
        try
        {
            readCOMThread.join(500);
        }
        catch(InterruptedException ex)
        {
        }
        try
        {
            writeCOMThread.join(500);
        }
        catch(InterruptedException ex)
        {
        }
        readCOMThread = null;
        writeCOMThread = null;
        binaryAppToCOMData.clear();  // Forget data buffers
        binaryCOMToAppData.clear();  // Forget data buffers
        return ret;
    }

    public COMResult SendCommand(String command, int responseBytesExpected, long milliTimeout)
    {
        return SendCommand(command, null, responseBytesExpected, milliTimeout);
    }

    public COMResult SendCommand(String command, String responseTerminator, long milliTimeout)
    {
        return SendCommand(command, responseTerminator, 0, milliTimeout);
    }

    public COMResult SendCommand(String command, String responseTerminator, int responseBytesExpected, long milliTimeout)
    {
        SendBinary((state.commandPrefix + command).getBytes());
        return ReadResponse(responseTerminator, responseBytesExpected, milliTimeout);
    }

    public void SendBinary(byte dataToTransmit[])
    {
        synchronized(binaryAppToCOMData)
        {
            binaryAppToCOMData.add(dataToTransmit);
            binaryAppToCOMData.notifyAll();
        }
    }

    public void SendBinary(byte dataToTransmit[], int startIndex, int length)
    {
        byte stageData[] = Arrays.copyOfRange(dataToTransmit, startIndex, startIndex + length);

        synchronized(binaryAppToCOMData)
        {
            binaryAppToCOMData.add(stageData);
            binaryAppToCOMData.notifyAll();
        }
    }

    public COMResult ReadLine(long milliTimeout)
    {
        return ReadResponse("\n", milliTimeout);
    }

    /**
     * Reads a specified number of bytes from the communications channel in a
     * blocking fashion, but with a maximum time to wait for the needed data to
     * arrive.
     *
     * @param responseByteCount Number of bytes to read
     * @param milliTimeout      Maximum number of milliseconds to block waiting
     *                          for the specified number of bytes to arrive. If
     *                          this timeout is exceeded, the read is aborted
     *                          and only the bytes available upon abort are
     *                          returned.
     *
     * @return COMReslt class containing the read data from the communications
     *         channel. On timeout or error, the returned number of bytes will
     *         not match the number of bytes requested by responseByteCount and
     *         can be as small as 0.
     */
    public COMResult ReadResponse(int responseByteCount, long milliTimeout)
    {
        return ReadResponse(null, responseByteCount, milliTimeout);
    }

    /**
     * Reads an arbitrary number of bytes from the communications channel in a
     * blocking fashion until the given string sequence is seen, but with a
     * maximum time to wait for the data and terminator to arrive.
     *
     * @param responseTerminator String to look for and terminate reading when
     *                           found. This string is included in the return
     *                           data, if found. This string is NOT treated as
     *                           null terminated. If you want to search for a
     *                           null terminated string, add the null terminator
     *                           as part of your string data.
     * @param milliTimeout       Maximum number of milliseconds to block waiting
     *                           for the responseTerminator string to arrive. If
     *                           this timeout is exceeded, the read is aborted
     *                           and only the bytes available upon abort are
     *                           returned.
     *
     * @return COMReslt class containing the data read from the communications
     *         channel, up to and including the responseTerminator. On timeout
     *         or error, the returned bytes will not end with the
     *         responseTerminator and can be as small as an empty string ("").
     */
    public COMResult ReadResponse(String responseTerminator, long milliTimeout)
    {
        return ReadResponse(responseTerminator, 0, milliTimeout);
    }

    public COMResult ReadResponse(String responseTerminator, int responseByteCount, long milliTimeout)
    {
        int writeIndex = 0;
        boolean newData;
        COMResult ret = new COMResult("", false, false, false);
        ret.responseBinary = new byte[responseByteCount];
        long startTime = System.currentTimeMillis();

        // Don't mess with the RX residual if there is nothing to be read
        if((responseByteCount == 0) && (responseTerminator == null))
        {
            return ret;
        }

        // Read first from any residual data that is already in our main()
        // thread context from the last call to ReadResponse()
        if(mainThreadRXResidual != null)
        {
            if(responseTerminator != null) // Text terminator read
            {
                ret.responseText = new String(mainThreadRXResidual);

                // When fresh data arrives, retry searching for the ending delimiter
                int findIndex = ret.responseText.indexOf(responseTerminator);
                if(findIndex >= 0)
                {
                    // Found the terminator, check if we read too much
                    findIndex += responseTerminator.length();
                    if(findIndex != ret.responseText.length())
                    {
                        // Read too much, place the extra characters back in the RX residual and truncate off of what we return
                        mainThreadRXResidual = Arrays.copyOfRange(mainThreadRXResidual, findIndex, mainThreadRXResidual.length);
                        ret.responseText = ret.responseText.substring(0, findIndex);
                    }
                    else
                    {
                        mainThreadRXResidual = null;
                    }

                    return ret;
                }
                mainThreadRXResidual = null;
            }
            else    // Binary byte count read
            {
                int copyLen = mainThreadRXResidual.length;
                if(writeIndex + mainThreadRXResidual.length > responseByteCount)    // Check if too much data would be copied
                {
                    copyLen = responseByteCount - writeIndex;
                }
                System.arraycopy(mainThreadRXResidual, 0, ret.responseBinary, writeIndex, copyLen);
                if(copyLen != mainThreadRXResidual.length)  // Remove only the data we used up.
                {   // Unused data exists, copy these unused bytes back into the residual
                    mainThreadRXResidual = Arrays.copyOfRange(mainThreadRXResidual, copyLen, mainThreadRXResidual.length);
                }
                else
                {
                    mainThreadRXResidual = null;
                }
                writeIndex += copyLen;

                // If we've reached the needed byte count end condition, stop reading data
                if(writeIndex == responseByteCount)
                {
                    return ret;
                }
            }
        }

        // Still need more data, so now do polling reads from the queue 
        // transfering from the ReadCOMThread to finish getting the requested data
        while(true)
        {
            newData = false;

            // Exit on timeout, assuming nothing is already waiting for us
            if(binaryCOMToAppData.isEmpty())
            {
                ret.timedOut = ((System.currentTimeMillis() - startTime) >= milliTimeout);
                if(ret.timedOut)
                {
                    break;
                }
            }

            // Exit if another thread says to close ourself
            if(timeToClose)
            {
                ret.asyncClose = true;
                break;
            }

            // Read fresh data
            while(!binaryCOMToAppData.isEmpty())
            {
                byte data[] = binaryCOMToAppData.poll();
                newData = true;

                if(responseTerminator != null) // Text terminator read
                {
                    ret.responseText += new String(data);

                    // When fresh data arrives, retry searching for the ending delimiter
                    int findIndex = ret.responseText.indexOf(responseTerminator);
                    if(findIndex >= 0)
                    {
                        // Found the terminator, check if we read too much
                        findIndex += responseTerminator.length();
                        if(findIndex != ret.responseText.length())
                        {
                            // Read too much, place the extra characters in the RX residual and truncate off of what we return
                            mainThreadRXResidual = Arrays.copyOfRange(data, ret.responseText.length() - findIndex, data.length);
                            ret.responseText = ret.responseText.substring(0, findIndex);
                        }

                        return ret;
                    }
                }
                else    // Binary byte count read
                {
                    int copyLen = data.length;
                    if(writeIndex + data.length > responseByteCount)    // Check if too much data would be copied
                    {
                        copyLen = responseByteCount - writeIndex;
                    }
                    System.arraycopy(data, 0, ret.responseBinary, writeIndex, copyLen);
                    if(copyLen != data.length)  // Save any residual we didn't use
                    {
                        mainThreadRXResidual = Arrays.copyOfRange(data, copyLen, data.length);
                    }
                    writeIndex += copyLen;

                    // If we've reached the needed byte count end condition, stop reading data
                    if(writeIndex == responseByteCount)
                    {
                        return ret;
                    }
                }
            }

            if(!newData)
            {
                // Nothing received: go to sleep until some more data
                // arrives. The RX thread will signal us and cause us to
                // wake early when some data is available.
                try
                {
                    synchronized(binaryCOMToAppData)
                    {
                        if(binaryCOMToAppData.isEmpty())
                        {
                            binaryCOMToAppData.wait(milliTimeout);
                        }
                    }
                }
                catch(InterruptedException ex)
                {
                }
            }
        }

        // Timeout or module asked to close; at least return what we have
        if(responseByteCount != writeIndex)
        {   // Truncate binary array length to match how many bytes we actually have
            ret.responseBinary = Arrays.copyOf(ret.responseBinary, writeIndex);
        }
        ret.error = true;
        return ret;
    }

    public static String FormatBinaryHelp(byte binaryData[], int indent, int bytesPerLine, int bytesPerWordGroup, int bytesPerAsciiGroup)
    {
        String decodedChars;
        byte c;
        int bytesLeft = binaryData.length;
        int i = 0;
        ArrayList<String> s = new ArrayList<String>();
        int spaceLeftInAsciiGroup;

        String indentString = String.format("\n%1$-" + String.valueOf(indent) + "s", "");
        while(bytesLeft > 0)
        {
            s.add(indentString);
            int lineBytesLeft = bytesPerLine > bytesLeft ? bytesLeft : bytesPerLine;
            int padLineBytes = bytesPerLine > bytesLeft ? bytesPerLine - bytesLeft : 0;
            decodedChars = "";
            while(lineBytesLeft > 0)
            {
                int wordBytesLeft = bytesPerWordGroup > lineBytesLeft ? lineBytesLeft : bytesPerWordGroup;
                spaceLeftInAsciiGroup = bytesPerAsciiGroup;
                while(wordBytesLeft > 0)
                {
                    c = binaryData[i++];
                    s.add(String.format("%1$02X", c));
                    if((c < 0x20) || (c >= 0x7E))
                    {
                        c = '.';
                    }

                    decodedChars += String.format("%1$c", (char)c);
                    bytesLeft--;
                    lineBytesLeft--;
                    wordBytesLeft--;
                    if(--spaceLeftInAsciiGroup == 0)
                    {
                        spaceLeftInAsciiGroup = bytesPerAsciiGroup;
                        decodedChars += " ";
                    }
                }
                s.add(" ");
            }
            int padChars = padLineBytes * 2 + ((padLineBytes + bytesPerWordGroup - 1) / bytesPerWordGroup) + 3;
            s.add(String.format("%1$-" + String.valueOf(padChars) + "s", "") + decodedChars);
        }

        return Multifunction.CatStringList(s);
    }

    public static String FormatBinaryHelp(byte binaryData[], int indent, int bytesPerLine, int bytesPerWordGroup)
    {
        return FormatBinaryHelp(binaryData, indent, bytesPerLine, bytesPerWordGroup, bytesPerWordGroup);
    }

    @Deprecated
    private COMResult HandshakeWithTarget()
    {
        return HandshakeWithTarget110();
    }

    @Deprecated
    private COMResult HandshakeWithTarget110()  // EZBL v1.10+ handshaking algorithm
    {
        long startTime, now, fullStartTime;
        long lastComActivity;
        COMResult ret = null;
        byte testTxData[] = new byte[3 + 1 + 16 + 1 + 1 + 16 + 1];
        byte[] txOutStream;
        int txOutStreamElements;
        byte[] receivedStream;
        int receivedStreamElements;
        byte[] expectStream;
        int expectStreamElements;
        long lastTxTime, nextTxTime;
        long epochMilliseconds;
        byte sec10, sec1, min10, min1, hr10, hr1, day10, day1, month10, month1, year10, year1, weekday1;
        long epochSec;

        // Skip all handshaking and auto-bauding if the command line parameter "-skip_handshake" is given
        if(this.skipHandshake)
        {
            return new COMResult("handshake skipped by command line", false, false, false);
        }

        // Ensure the input and output threads are opened before attempting to
        // send anything so we can have more accurate round-trip-time estimation
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < milliTimeout)
        {
            if((this.out != null) || (this.in != null) || !this.readCOMThread.isAlive() || !this.writeCOMThread.isAlive()) // Cross-thread accesses, but doesn't need synchronization because these are volatile fields and we need only read from them (with old or incoherent data still being a perfectly fine return value since it ultimately is just a boolean that we are using)
            {
                break;
            }
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException ex)
            {
                // Waking up is fine...
            }
        }
        if(((this.out == null) && (this.in == null)) || (!this.readCOMThread.isAlive() || !this.writeCOMThread.isAlive()))
        {
            String port = port = this.comPort.startsWith("\\\\.\\") ? this.comPort.substring(4) : this.comPort; // Trim off "\\.\" leading chars, if present
            ret = new COMResult(String.format("cannot open '%1$s'. Ensure the port is valid and not already in use by another application.", port), true, true, true);
// Using this code requires the lib\jna.jar file be distributed
//            List<String> portList = EnumCOMPorts();
//            if((portList != null) && !portList.isEmpty())
//            {
//                ret.responseText += "\nSystem port list contains: ";
//                for(String s : portList)
//                {
//                    ret.responseText += s + ", ";
//                }
//                ret.responseText = ret.responseText.substring(0, ret.responseText.length() - 2);
//            }
            ret.responseBinary = new byte[0];
            return ret;
        }

        // Purge any RX data so we are assured to start communications with
        // clean FIFOs
        ReadResponse(8192, 0);

        txOutStream = new byte[4096];
        txOutStreamElements = 0;
        receivedStream = new byte[4096];
        receivedStreamElements = 0;
        expectStream = new byte[4096];
        expectStreamElements = 0;
        Calendar rtcc = Calendar.getInstance();
        fullStartTime = System.currentTimeMillis();
        startTime = fullStartTime;
        int txGroupInterval = 50 + (int)Math.pow(milliTimeout, 1 / 2.1);// Milliseconds to next transmit, starting value (self grows)
        lastComActivity = System.currentTimeMillis() - txGroupInterval;
        int txGroupIntervalLimit = 50 + (int)milliTimeout / 2;         // Milliseconds delay between retransmits saturation limit
        while(System.currentTimeMillis() - startTime < milliTimeout)
        {
            now = System.currentTimeMillis();
            if(now - lastComActivity >= txGroupInterval)
            {
                if(expectStreamElements + 20 > expectStream.length)
                {
                    break;
                }
                if(txGroupInterval < txGroupIntervalLimit)
                {
                    txGroupInterval *= (1.05 + Math.random() * 0.25);   // Grow new TX delay by 5-30% longer than the last delay
                    if(txGroupInterval > txGroupIntervalLimit)          // But saturate once we get to about 325ms. We don't want to force the PIC side code to have long timeouts as well while also desiring not to flood an overloaded device with things it can't process and must drop.
                    {
                        txGroupInterval = txGroupIntervalLimit;
                    }
                }

                // Send UUUE{16 test bytes}UE{16 random test bytes} as a quick test to handshake and validate communications in one go
                testTxData[0] = 'U';
                expectStream[expectStreamElements++] = testTxData[0];
                testTxData[1] = 'U';
                expectStream[expectStreamElements++] = testTxData[1];
                testTxData[2] = 'U';
                expectStream[expectStreamElements++] = testTxData[2];
                testTxData[3] = 'M';
                testTxData[4] = 'C';
                testTxData[5] = 'U';
                expectStream[expectStreamElements++] = testTxData[5];
                testTxData[6] = 'P';
                testTxData[7] = 'H';
                testTxData[8] = 'C';
                testTxData[9] = 'M';
                testTxData[10] = (byte)'E';     // Echo request command code + 16 bytes of echo data
                if(this.baudRate != 0)          // Send the transmit baud rate, if known, for the Bootloader to use if it wants
                {
                    testTxData[11] = (byte)(this.baudRate & 0xFF);
                    testTxData[12] = (byte)((this.baudRate >> 8) & 0xFF);
                    testTxData[13] = (byte)((this.baudRate >> 16) & 0xFF);
                    testTxData[14] = (byte)((this.baudRate >> 24) & 0xFF);
                }
                epochMilliseconds = System.currentTimeMillis();

                sec10 = (byte)(rtcc.get(Calendar.SECOND) / 10);
                sec1 = (byte)(rtcc.get(Calendar.SECOND) % 10);
                min10 = (byte)(rtcc.get(Calendar.MINUTE) / 10);
                min1 = (byte)(rtcc.get(Calendar.MINUTE) % 10);
                hr10 = (byte)(rtcc.get(Calendar.HOUR_OF_DAY) / 10);
                hr1 = (byte)(rtcc.get(Calendar.HOUR_OF_DAY) % 10);
                day10 = (byte)(rtcc.get(Calendar.DAY_OF_MONTH) / 10);
                day1 = (byte)(rtcc.get(Calendar.DAY_OF_MONTH) % 10);
                month10 = (byte)(rtcc.get(Calendar.MONTH) / 10);
                month1 = (byte)(rtcc.get(Calendar.MONTH) % 10);
                year10 = (byte)((rtcc.get(Calendar.YEAR) % 100) / 10);
                year1 = (byte)((rtcc.get(Calendar.YEAR) % 100) % 10);
                weekday1 = (byte)(rtcc.get(Calendar.DAY_OF_WEEK) - 1);  // SUNDAY == 1, so change to define SUNDAY == 0, SATURDAY == 6

                epochSec = epochMilliseconds / 1000;

                testTxData[15] = (byte)((epochSec) & 0xFF);        // 32-bits representing unsigned Unix Epoch time (seconds since 1970 January 01, 12:00:00 AM UTC)
                testTxData[16] = (byte)((epochSec >> 8) & 0xFF);
                testTxData[17] = (byte)((epochSec >> 16) & 0xFF);
                testTxData[18] = (byte)((epochSec >> 24) & 0xFF);

                // Send RTCC fields in funny BCD nibble encoding
                testTxData[19] = (byte)(sec10 << 4 | sec1); // 0-59 seconds
                testTxData[20] = (byte)(min10 << 4 | min1); // 0-59 minutes
                testTxData[21] = (byte)(hr10 << 4 | hr1);   // 0-23 hours
                testTxData[22] = (byte)((((epochMilliseconds % 1000) & 0x3) << 4) | weekday1); // 0-6 day of week. +0-3 1ms counts by encoding 2 LSbits of millisecond count in upper nibble of this byte
                testTxData[23] = (byte)(day10 << 4 | day1); // 1-31(or 30, 29, or 28) day in month
                testTxData[24] = (byte)(month10 << 4 | month1); // 0-11 months
                testTxData[25] = (byte)(year10 << 4 | year1);   // 00-99 years (20 leading digits implicitly assumed)

                testTxData[26] = (byte)((epochMilliseconds % 1000) >> 2); // 0-249 4ms counts. Encode milliseconds within second / 4 (i.e. 1 LSb = 4ms, 996ms = 249, 250-255 not legal)
                testTxData[27] = 'U';

                SendBinary(testTxData, 0, 28);
                lastTxTime = epochMilliseconds;
                lastComActivity = lastTxTime;
                System.arraycopy(testTxData, 0, txOutStream, txOutStreamElements, 28);
                txOutStreamElements += 28;
                System.arraycopy(testTxData, 11, expectStream, expectStreamElements, 17);
                expectStreamElements += 17;
            }

            ret = ReadResponse(18, 1); // See what shows up in 1ms, if anything
            if(ret.asyncClose || ret.fileError)
            {
                break;
            }
            if(ret.responseBinary.length == 0)
            {
                continue;
            }
            lastComActivity = System.currentTimeMillis();
            if(receivedStreamElements + ret.responseBinary.length > receivedStream.length)
            {
                break;
            }
            System.arraycopy(ret.responseBinary, 0, receivedStream, receivedStreamElements, ret.responseBinary.length);
            receivedStreamElements += ret.responseBinary.length;

            if(receivedStreamElements < 18)
            {
                continue;
            }

            boolean matches = true;
            for(int i = 0; i < 18; i++)
            {
                if(receivedStream[receivedStreamElements - 1 - i] != expectStream[expectStreamElements - 1 - i])
                {
                    matches = false;
                    break;
                }
            }
            if(matches)
            {   // Successful match all the way to the end of the expected characters, extending at least 46 characters back for a whole transaction, less initial autobaud chars
                //now = System.currentTimeMillis();
                //long roundTripTime = now - lastTxTime;
                ret = SendCommand("#", 13, milliTimeout);
                //long roundTripTime2 = System.currentTimeMillis() - now;
                //System.err.printf("pingE = %1$dms, ping# = %2$dms\n", roundTripTime, roundTripTime2);   // DEBUG only code. Might be useful to print this someday though.
                if(ret.error || (ret.responseBinary == null) || (ret.responseBinary.length == 0) || (ret.responseBinary[0] != '+'))
                {
                    continue;
                }
                System.arraycopy(ret.responseBinary, 0, receivedStream, receivedStreamElements, ret.responseBinary.length);
                receivedStreamElements += ret.responseBinary.length;
                ret.responseBinary = new byte[receivedStreamElements];
                System.arraycopy(receivedStream, 0, ret.responseBinary, 0, receivedStreamElements);
                int devID = (((int)ret.responseBinary[receivedStreamElements - 12]) & 0xFF) | ((((int)ret.responseBinary[receivedStreamElements - 11]) & 0xFF) << 8) | ((((int)ret.responseBinary[receivedStreamElements - 10]) & 0xFF) << 16) | ((((int)ret.responseBinary[receivedStreamElements - 9]) & 0xFF) << 24);
                int devREV = (((int)ret.responseBinary[receivedStreamElements - 8]) & 0xFF) | ((((int)ret.responseBinary[receivedStreamElements - 7]) & 0xFF) << 8) | ((((int)ret.responseBinary[receivedStreamElements - 6]) & 0xFF) << 16) | ((((int)ret.responseBinary[receivedStreamElements - 5]) & 0xFF) << 24);
                int remoteWindow = (((int)ret.responseBinary[receivedStreamElements - 4]) & 0xFF) | ((((int)ret.responseBinary[receivedStreamElements - 3]) & 0xFF) << 8) | ((((int)ret.responseBinary[receivedStreamElements - 2]) & 0xFF) << 16) | ((((int)ret.responseBinary[receivedStreamElements - 1]) & 0xFF) << 24);
                ret.responseText = String.format("Connected: DEVID, DEVREV: %1$08X, %2$08X; Buffering: %3$d bytes", devID, devREV, remoteWindow);
                return ret;
            }
        }

        if(ret == null)
        {
            ret = new COMResult();
        }
        ret.timedOut |= (System.currentTimeMillis() - fullStartTime >= milliTimeout);
        ret.responseText = "handshake with target device " + (ret.timedOut ? "timed out" : "failed") + String.format(" (TX %1$d bytes, RX %2$d bytes)", txOutStreamElements, receivedStreamElements);
        ret.responseBinary = new byte[receivedStreamElements];
        System.arraycopy(receivedStream, 0, ret.responseBinary, 0, receivedStreamElements);
        return ret;
    }

    @Deprecated
    private COMResult HandshakeWithTarget109()  // EZBL v1.09 handshaking algorithm
    {
        COMResult ret;
        long startTime;
        int handshakeTxAttempts = 0;
        byte testTxData[] = new byte[3 + 1 + 16 + 1 + 1 + 16 + 1];

        // Skip all handshaking and auto-bauding if the command line parameter "-skip_handshake" is given
        if(this.skipHandshake)
        {
            return new COMResult("Handshake skipped by command line", false, false, false);
        }

        // Ensure the input and output threads are opened before attempting to
        // send anything so we can have more accurate round-trip-time estimation
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < milliTimeout)
        {
            if((this.out != null) && (this.in != null)) // Cross-thread accesses, but doesn't need synchronization because these are volatile fields and we need only read from them (with old or incoherent data still being a perfectly fine return value since it ultimately is just a boolean that we are using)
            {
                break;
            }
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException ex)
            {
                // Waking up is fine...
            }
        }

        // Purge any RX data so we are assured to start communications with
        // clean FIFOs
        ReadResponse(8192, 0);

        startTime = System.currentTimeMillis();
        while(handshakeTxAttempts++ < 3 || (System.currentTimeMillis() - startTime < milliTimeout))
        {
            // Send UUUE{16 test bytes}UE{16 random test bytes} as a quick test to handshake and validate communications in one go
            testTxData[0] = 'U';
            testTxData[1] = 'U';
            testTxData[2] = 'U';
            testTxData[3] = (byte)'E';      // Echo request command code + 16 bytes of echo data
            if(this.baudRate != 0)          // Send the transmit baud rate, if known, for the Bootloader to use if it wants
            {
                testTxData[4] = (byte)(this.baudRate & 0xFF);
                testTxData[5] = (byte)((this.baudRate >> 8) & 0xFF);
                testTxData[6] = (byte)((this.baudRate >> 16) & 0xFF);
                testTxData[7] = (byte)((this.baudRate >> 24) & 0xFF);
            }
            testTxData[8] = (byte)0x0F;
            testTxData[9] = (byte)0x1F;
            testTxData[10] = (byte)0x3F;
            testTxData[11] = (byte)0x7F;
            testTxData[12] = (byte)0xFF;
            testTxData[13] = (byte)0xA5;
            testTxData[14] = (byte)0x5A;
            testTxData[15] = (byte)0x55;
            testTxData[16] = (byte)0xAA;
            testTxData[17] = (byte)0x80;
            testTxData[18] = (byte)System.currentTimeMillis();
            testTxData[19] = (byte)(System.currentTimeMillis() >> 8);
            testTxData[20] = 'U';
            testTxData[21] = 'E';       // Echo request command code + 16 bytes of echo data
            if(this.baudRate != 0)      // Also start with baud rate for simple PIC decoding
            {
                testTxData[22] = (byte)(this.baudRate & 0xFF);
                testTxData[23] = (byte)((this.baudRate >> 8) & 0xFF);
                testTxData[24] = (byte)((this.baudRate >> 16) & 0xFF);
                testTxData[25] = (byte)((this.baudRate >> 24) & 0xFF);
            }
            for(int i = 26; i < 22 + 16; i += 4)    // 12 bytes of random data to ensure we synchronize to this echo request and not some prior one still floating in transit
            {
                long t = (long)(Math.random() * 0x00000000FFFFFFFFL);
                testTxData[i + 0] = (byte)(t);
                testTxData[i + 1] = (byte)(t >> 8);
                testTxData[i + 2] = (byte)(t >> 16);
                testTxData[i + 3] = (byte)(t >> 24);
            }
            testTxData[38] = (byte)'#';

            startTime = System.currentTimeMillis();
            SendBinary(testTxData);
            int expectIndex = 4;
            boolean commsLookingGood = false;
            int decodePhase = 0;
            int bytesGoodSoFar = 0;
            int attemptTimeout = (int)(milliTimeout * (0.1 + Math.random() * 0.4));
            for(int timeBlock = 0; (bytesGoodSoFar < 36) && (timeBlock < attemptTimeout); timeBlock++)
            {
                ret = ReadResponse(1, 1); // See what shows up in 1ms, if anything
                if(ret.asyncClose || ret.fileError)
                {
                    ret.responseText = "read error, ";
                    return ret;
                }

                if(ret.responseBinary.length != 0)
                {
                    switch(decodePhase)
                    {
                        case 0: // 'U' responses
                            if(ret.responseBinary[0] == 'U')
                            {
                                bytesGoodSoFar++;
                                decodePhase = 1;
                                continue;
                            }
                            break;
                        case 1: // Echo 16 bytes matching testTxData[4] to testTxData[19] corresponding to expectIndex
                            if(ret.responseBinary[0] == testTxData[expectIndex])
                            {
                                bytesGoodSoFar++;
                                expectIndex++;
                                if(expectIndex > 19)
                                {
                                    decodePhase = 2;
                                    continue;
                                }
                            }
                            break;

                        case 2: // 'U' response
                            if(ret.responseBinary[0] == 'U')
                            {
                                bytesGoodSoFar++;
                                decodePhase = 3;
                                expectIndex = 22;
                                continue;
                            }
                            break;

                        case 3: // 16 bytes matching testTxData[22] to testTxData[37])
                            if(ret.responseBinary[0] == testTxData[expectIndex])
                            {
                                bytesGoodSoFar++;
                                expectIndex++;
                                if(expectIndex > 37)
                                {
                                    commsLookingGood = true;
                                    break;
                                }
                            }
                            break;
                    }
                    if(commsLookingGood)
                    {
                        long fullEchoRoundTripTime = System.currentTimeMillis() - startTime;

                        ret = ReadResponse(13, milliTimeout / 4 + 2 * fullEchoRoundTripTime);       // Get the '#' device query command response (expecting '+' + 3x4 byte values)
                        if((ret.responseBinary.length == 13) && (ret.responseBinary[0] == '+'))
                        {
                            int devID = (((int)ret.responseBinary[1]) & 0xFF) | ((((int)ret.responseBinary[2]) & 0xFF) << 8) | ((((int)ret.responseBinary[3]) & 0xFF) << 16) | ((((int)ret.responseBinary[4]) & 0xFF) << 24);
                            int devREV = (((int)ret.responseBinary[5]) & 0xFF) | ((((int)ret.responseBinary[6]) & 0xFF) << 8) | ((((int)ret.responseBinary[7]) & 0xFF) << 16) | ((((int)ret.responseBinary[8]) & 0xFF) << 24);
                            int remoteWindow = (((int)ret.responseBinary[9]) & 0xFF) | ((((int)ret.responseBinary[10]) & 0xFF) << 8) | ((((int)ret.responseBinary[11]) & 0xFF) << 16) | ((((int)ret.responseBinary[12]) & 0xFF) << 24);
                            ret.responseText = String.format("Connected: DEVID, DEVREV: %1$08X, %2$08X; Buffering: %3$d bytes", devID, devREV, remoteWindow);
                            return ret;
                        }
                    }
                }
            }
        }

        // Echo data is correct, no extraneous data, we look good to go
        return new COMResult("Handshake failure", true, false, false);
    }

    @Deprecated
    private COMResult HandshakeWithTarget101()  // Original EZBL v1.01b handshaking algorithm
    {
        COMResult ret;
        long estimatedRoundTripTime;
        long startTime, lastTime, now;
        int autoBaudCharsEchoed;
        int autoBaudBackoff;
        int charactersReceived;

        // Skip all handshaking and auto-bauding if the command line parameter "-skip_handshake" is given
        if(this.skipHandshake)
        {
            return new COMResult("Handshake skipped by command line", false, false, false);
        }

        // Ensure the input and output threads are opened before attempting to
        // send anything so we can have more accurate round-trip-time estimation
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < milliTimeout)
        {
            if((this.out != null) && (this.in != null)) // Cross-thread accesses, but doesn't need synchronization because these are volatile fields and we need only read from them (with old or incoherent data still being a perfectly fine return value since it ultimately is just a boolean that we are using)
            {
                break;
            }
            try
            {
                Thread.sleep(1);
            }
            catch(InterruptedException ex)
            {
                // Waking up is fine...
            }
        }

        // Purge any RX data so we are assured to start communications with
        // clean FIFOs
        ReadResponse(8192, 10);

        // Send 'U' (0x55) auto-baud characters and a '#' character to get a
        // known informational response if successful. The extra 'U' auto-baud
        // characters are to try and force a sync up, assuming this baud rate is
        // possible to sync to.
        estimatedRoundTripTime = (milliTimeout / 20) < 10 ? 10 : (milliTimeout / 20);
        autoBaudCharsEchoed = 0;
        startTime = System.currentTimeMillis();
        lastTime = startTime;
        autoBaudBackoff = 3;    // Start with a 3ms back off period
        charactersReceived = 0; // Count how many characters were received so we can differentiate from completely dead communications links
        do
        {
            byte txData[] = new byte[1];
            txData[0] = (byte)'U';
            SendBinary(txData);                     // Transmit a 'U' auto-baud synch character
            ret = ReadResponse(1, autoBaudBackoff); // Check for a 'U' to be echoed back to us
            if(ret.responseBinary.length >= 1)
            {
                charactersReceived++;
                if(ret.responseBinary[0] == (byte)'U')
                {
                    autoBaudCharsEchoed++;

                    // Compute the worst case round trip time we've seen
                    now = System.currentTimeMillis();
                    if(now - lastTime > estimatedRoundTripTime)
                    {
                        estimatedRoundTripTime = now - lastTime;
                        lastTime = now;
                    }
                }
                else
                {
                    autoBaudCharsEchoed = 0;
                    ret.error = true;
                }
            }

            // For any timeout, increase our transmit delay up to a maximum of 100ms. We have this capped so we can still catch a bootloader that is not online, but will be as soon as the user power cycles it. Ex: set a large timeout, start bootloading command, wait for user to reset the device so the bootloader runs briefly. If the backoff grew continuously, the 1s typical power up bootloader window would often be missed.
            if(ret.timedOut && (autoBaudBackoff < 100))
            {
                autoBaudBackoff *= 1.34;                // Exponentially increase our transmission backoff
            }
        } while((autoBaudCharsEchoed < 3) && !ret.asyncClose && ((System.currentTimeMillis() - startTime) < milliTimeout));

        // Consume any in-transit 'U' responses that we may still receive
        do
        {
            ret = ReadResponse(10, (long)(estimatedRoundTripTime + autoBaudBackoff));
        } while((ret.responseBinary.length != 0) && !ret.asyncClose && ((System.currentTimeMillis() - startTime) < milliTimeout));

        // Transmit a '#' device query command
        ret = SendCommand("#", 1, (long)(estimatedRoundTripTime * 2));      // Transmit the '#' device query character and try to read 1 byte
        // Read characters until we get the '+' command acknowledgement character
        do
        {
            if(ret.responseBinary.length >= 1)
            {
                if(ret.responseBinary[0] == (byte)'U')
                {
                    // Found another auto-baud character, our estimatedRoundTripTime may be too short
                    estimatedRoundTripTime += autoBaudBackoff;
                }
                else if(ret.responseBinary[0] == (byte)'+')
                {
                    // Expected command acknowledgement
                    break;
                }
                else
                {
                    // Received something unexpected; Either the target is some other non-bootloader device (doesn't support our protocol), or the baud rate is too high to receive the expected '+' character.
                    ret.error = true;
                    ret.responseText = String.format("discovery/handshake stage, expecting 0x%1$02X ('%2$c') in response to '#' device query command, but received:", (byte)'+', '+');
                    ret.responseText += "\n{"
                                        + FormatBinaryHelp(ret.responseBinary, 4, 32, 1)
                                        + "\n}";
                    return ret;
                }
            }
            ret = ReadResponse(1, (long)(estimatedRoundTripTime * 2));      // Receive the expected '+' command acknowledgement and 12-byte additional response
        } while(ret.timedOut && !ret.asyncClose && ((System.currentTimeMillis() - startTime) < milliTimeout));

        if(ret.asyncClose)
        {
            //ret.responseText = String.format("discovery/handshake failure, asynchronous close while talking with ezbl_comm.exe");
            return ret;
        }
        if(ret.error)
        {
            if(charactersReceived == 0)
            {
                ret.responseText = Multifunction.FormatHelpText(100, 0, "discovery/handshake failure, did not receive any bytes from the communications channel. Check communications port, baud rate, physical connections and power.");
            }
            else
            {
                ret.responseText = String.format("discovery/handshake failure, did not receive command acknowledgement in response to '#' device query command before the specified timeout (%1$dms).", milliTimeout);
                if(ret.responseBinary.length != 0)
                {
                    ret.responseText += String.format("\nLast %1$d bytes received:", ret.responseBinary.length)
                                        + "\n{"
                                        + FormatBinaryHelp(ret.responseBinary, 4, 32, 1)
                                        + "\n}";

                }
                ret.responseText = Multifunction.FormatHelpText(100, 0, ret.responseText);
                return ret;
            }
        }

        ret = ReadResponse(12, estimatedRoundTripTime * 16 > milliTimeout ? milliTimeout : estimatedRoundTripTime * 16);      // Receive the expected 12-byte additional response data bytes
        if(ret.error)
        {
            ret.responseText = String.format("discovery/handshake stage, did not receive device info in response to '#' device query command (expecting 12 bytes)");
            if(ret.responseBinary.length != 0)
            {
                ret.responseText += String.format("\nLast %1$d bytes received:", ret.responseBinary.length)
                                    + "\n{"
                                    + FormatBinaryHelp(ret.responseBinary, 4, 32, 1)
                                    + "\n}";

            }
            ret.responseText = Multifunction.FormatHelpText(100, 0, ret.responseText);
            return ret;
        }

        // Test com reliability by issuing a 16-byte echo request
        byte testData[] = new byte[17];
        testData[0] = (byte)'E';    // Echo request command code
        testData[1] = (byte)0x00;   // 16-bytes of echo test data (this is just random data with a mix of consecutive 1's and 0 strings)
        testData[2] = (byte)0x01;
        testData[3] = (byte)0x03;
        testData[4] = (byte)0x07;
        testData[5] = (byte)0x0F;
        testData[6] = (byte)0x1F;
        testData[7] = (byte)0x3F;
        testData[8] = (byte)0x7F;
        testData[9] = (byte)0xFF;
        testData[10] = (byte)0xA5;
        testData[11] = (byte)0x5A;
        testData[12] = (byte)0x55;
        testData[13] = (byte)0xAA;
        testData[14] = (byte)0x80;
        testData[15] = (byte)System.currentTimeMillis();
        testData[16] = (byte)(System.currentTimeMillis() >> 8);
        SendBinary(testData);
        ret = ReadResponse(16, 10 + estimatedRoundTripTime * 16);
        if(ret.error)
        {
            ret.responseText = "handshake echo read error. Expecting 16 bytes. Check hardware and try a lower communications bit rate.";
            if(ret.responseBinary.length != 0)
            {
                ret.responseText += String.format("\nReceived %1$d bytes:", ret.responseBinary.length)
                                    + "\n{"
                                    + FormatBinaryHelp(ret.responseBinary, 4, 32, 1)
                                    + "\n}";

            }
            ret.responseText = Multifunction.FormatHelpText(100, 0, ret.responseText);
            return ret;
        }
        for(int i = 1; i < testData.length; i++)
        {
            if(ret.responseBinary[i - 1] != testData[i])
            {
                ret.error = true;
                ret.responseText = "handshake echo data wrong. Check hardware and try a different communications rate.";
                ret.responseText += String.format("\n    Received: %1$02X %2$02X %3$02X %4$02X %5$02X %6$02X %7$02X %8$02X %9$02X %10$02X %11$02X %12$02X %13$02X %14$02X %15$02X %16$02X", ret.responseBinary[0], ret.responseBinary[1], ret.responseBinary[2], ret.responseBinary[3], ret.responseBinary[4], ret.responseBinary[5], ret.responseBinary[6], ret.responseBinary[7], ret.responseBinary[8], ret.responseBinary[9], ret.responseBinary[10], ret.responseBinary[11], ret.responseBinary[12], ret.responseBinary[13], ret.responseBinary[14], ret.responseBinary[15]);
                ret.responseText += String.format("\n    Expected: %1$02X %2$02X %3$02X %4$02X %5$02X %6$02X %7$02X %8$02X %9$02X %10$02X %11$02X %12$02X %13$02X %14$02X %15$02X %16$02X", testData[1], testData[2], testData[3], testData[4], testData[5], testData[6], testData[7], testData[8], testData[9], testData[10], testData[11], testData[12], testData[13], testData[14], testData[15], testData[16]);
                return ret;
            }
        }

        // Check to make sure we don't receive any additional data bytes we aren't expecting
        COMResult test = ReadResponse(64, 1 + estimatedRoundTripTime);
        if(test.responseBinary.length != 0)
        {
            ret.error = true;
            ret.timedOut = false;
            ret.responseText = "unexpected extra data received during handshaking in response to '#' device query command.";
            if(test.responseBinary.length != 0)
            {
                ret.responseText += String.format("\nReceived %1$d extra bytes:", test.responseBinary.length)
                                    + "\n{"
                                    + FormatBinaryHelp(test.responseBinary, 4, 32, 1)
                                    + "\n}";

            }
            ret.responseText = Multifunction.FormatHelpText(100, 0, ret.responseText);
            return ret;
        }

        // Echo data is correct, no extraneous data, we look good to go
        return new COMResult("Handshake failure", true, false, false);
    }

    /**
     * Requests a CRC32 from the target Bootloader of the specified memory
     * region. This API is generally only supported for EZBL v1.01 bootloaders.
     * Newer targets may not support any of the c/d/e commands to save space.
     *
     * @param crcType           Integer specifying which region should be
     *                          checksumed.
     * <ul>
     * <li> 0 - Get CRC of all Nonvolatile memories</li>
     * <li> 1 - Get CRC of the Bootloader memory spaces</li>
     * <li> 2 - Get CRC of the Application memory spaces</li>
     * <li> Other values are reserved/undefined</li>
     * </ul>
     * @param suppressHandshake True if you have already completed handshaking
     *                          with the target bootloader or otherwise do not
     *                          require any pre- communications before issuing
     *                          the CRC request command.
     *
     * @return The requested CRC32 in COMResult.responseBinary[3:0], or
     *         COMResult.error on failure.
     */
    @Deprecated
    public COMResult GetTargetCRC(int crcType, boolean suppressHandshake)
    {
        // Read remote CRC32 of all non-volatile regions, all bootloader
        // regions, and all application space regions to determine what is
        // currently installed.
        long crc;
        COMResult ret;

        if((crcType < 0) || (crcType > 2))
        {
            return new COMResult("Incorrect crcType parameter", true, false, false);
        }
        if(!suppressHandshake)
        {
            ret = HandshakeWithTarget();
            if(ret.error)
            {
                return ret;
            }
        }

        ret = SendCommand((crcType == 0) ? "c" : (crcType == 1) ? "d" : "e", 5, this.milliTimeout * 2);
        if(ret.error)
        {
            return ret;
        }
        if(ret.responseBinary[0] != '+')
        {
            ret.responseText = String.format("CRC feature unavailable: 0x%1$02X\n", ret.responseBinary[0]);
            ret.error = true;
            return ret;
        }

        crc = (((long)ret.responseBinary[1]) & 0xFF) | ((((long)ret.responseBinary[2]) & 0xFF) << 8) | ((((long)ret.responseBinary[3]) & 0xFF) << 16) | ((((long)ret.responseBinary[4]) & 0xFF) << 24);
        ret.responseText = String.format("0x%1$08X", crc);
        return ret;
    }

    @Deprecated
    public COMResult ReadDeviceFlash(String outputArtifactPath, boolean suppressHandshake)
    {
        int blobSize;
        int bytesRemaining;
        byte buffer[];
        int bufferIndex;
        int fileIndex;
        long startTime;
        int remoteWindow;
        int devID;
        int devREV;
        COMResult ret;

        // For statistics keeping, let's get the current time of the JRE
        startTime = System.currentTimeMillis();

        // Auto-baud and handshake to ensure communications is synchronized with
        // the target bootloader
        if(!suppressHandshake)
        {
            ret = HandshakeWithTarget();
            if(ret.error)
            {
                return ret;
            }

            // Read remote DEVID (32-bits), DEVREV (32-bits), buffer size (32-bits)
            ret = SendCommand("#", 13, this.milliTimeout);
            if(ret.error)
            {
                ret.responseText = Multifunction.FormatHelpText(100, 0, "after handshaking, did not receive a valid response to the '#' device ID read command");
                return ret;
            }
            devID = (((int)ret.responseBinary[1]) & 0xFF) | ((((int)ret.responseBinary[2]) & 0xFF) << 8) | ((((int)ret.responseBinary[3]) & 0xFF) << 16) | ((((int)ret.responseBinary[4]) & 0xFF) << 24);
            devREV = (((int)ret.responseBinary[5]) & 0xFF) | ((((int)ret.responseBinary[6]) & 0xFF) << 8) | ((((int)ret.responseBinary[7]) & 0xFF) << 16) | ((((int)ret.responseBinary[8]) & 0xFF) << 24);
            remoteWindow = (((int)ret.responseBinary[9]) & 0xFF) | ((((int)ret.responseBinary[10]) & 0xFF) << 8) | ((((int)ret.responseBinary[11]) & 0xFF) << 16) | ((((int)ret.responseBinary[12]) & 0xFF) << 24);
            System.out.format("Connected: DEVID, DEVREV: %1$08X, %2$08X; Buffering: %3$d bytes\n", devID, devREV, remoteWindow);
        }

        // Send command to dump PIC Flash contents
        ret = SendCommand("F", 8, this.milliTimeout);
        if(ret.error)
        {
            ret.responseText = String.format("Did not receive image file length from target. Device read-back may not be enabled in the device firmware.\n");
            return ret;
        }
        if(ret.responseBinary[0] == '+')
        {
            // May be old EZBL v1.07b and earlier protocol with '+' command acknowledgment prefix
            if((ret.responseBinary[5] == 'B') && (ret.responseBinary[6] == 'L'))
            {
                // Yes off by one position, throw away the '+' character and decode with everything else shifted over
                byte trimmedData[] = new byte[ret.responseBinary.length - 1];
                System.arraycopy(ret.responseBinary, 1, trimmedData, 0, trimmedData.length);
                ret.responseBinary = trimmedData;
            }
        }

        blobSize = ((int)ret.responseBinary[0]) & 0x000000FF;
        blobSize |= (((int)ret.responseBinary[1]) & 0x000000FF) << 8;
        blobSize |= (((int)ret.responseBinary[2]) & 0x000000FF) << 16;
        blobSize |= (((int)ret.responseBinary[3]) & 0x000000FF) << 24;
        bytesRemaining = blobSize - ret.responseBinary.length;

        buffer = new byte[blobSize > 1024 * 1024 ? 1024 * 1024 : blobSize];            // Allocate memory for the whole file or 1MB, whichever is less
        System.arraycopy(ret.responseBinary, 0, buffer, 0, ret.responseBinary.length);
        bufferIndex = ret.responseBinary.length;
        fileIndex = 0;

        // Read in the rest of the .blob file data
        int chunkLen = 1024;     // Default to 1024 bytes per read/display status update option
        int lastPercentDone = 0;
        System.out.print("Read progress:   |0%         25%         50%         75%        100%|\n");
        System.out.print("                 |");
        System.out.flush();
        while(bytesRemaining != 0)
        {
            if(chunkLen > bytesRemaining)               // Limit to end of advirtised image file length
            {
                chunkLen = bytesRemaining;
            }
            if(chunkLen > buffer.length - bufferIndex)  // Limit to end of current buffer size
            {
                chunkLen = buffer.length - bufferIndex;
            }
            ret = ReadResponse(chunkLen, this.milliTimeout);
            if(ret.responseBinary != null)
            {
                System.arraycopy(ret.responseBinary, 0, buffer, bufferIndex, ret.responseBinary.length);
                bufferIndex += ret.responseBinary.length;
                if(bufferIndex >= buffer.length)
                {
                    Multifunction.WriteFile(outputArtifactPath, buffer, fileIndex != 0);    // Flush chunk to disk
                    fileIndex += bufferIndex;
                    bufferIndex = 0;
                }
            }
            if(ret.error)
            {
                // We are going to abort, so at least write out the partial file that we have
                if(bufferIndex != buffer.length)
                {
                    buffer = Arrays.copyOf(buffer, bufferIndex);
                }
                Multifunction.WriteFile(outputArtifactPath, buffer, fileIndex != 0);

                System.out.println();   // Clean up our partial status display
                ret.responseText = String.format("Error reading data from target at offset %1$d\n", fileIndex + bufferIndex);
                return ret;
            }
            bytesRemaining -= chunkLen;

            // Update display progress
            int percentDone = (blobSize - bytesRemaining) * 100 / blobSize;
            while(percentDone - lastPercentDone >= 2)
            {
                System.out.append('.');
                System.out.flush();
                lastPercentDone += 2;
            }
        }

        // All done reading, print transfer statistics
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.print(
                "|\n                  ");
        String status = String.format("%1$d bytes received in %2$1.3fs (%3$1.0f bytes/second)", fileIndex + bufferIndex, totalTime / 1000.0, fileIndex + bufferIndex / (totalTime / 1000.0 + 0.000001));   // Add 1 us to make division by 0 impossible without impacting the ret
        System.out.println(status);

        // Output final chunk to disk
        if(bufferIndex != buffer.length)
        {
            buffer = Arrays.copyOf(buffer, bufferIndex);
        }
        Multifunction.WriteFile(outputArtifactPath, buffer, fileIndex != 0);

        ret.error = false;
        ret.timedOut = false;
        ret.asyncClose = false;
        ret.responseText = "Read-back completed successfully";
        return ret;
    }

    static public String GetEZBLBootloaderErrorText(int errorCode, int EZBLReleaseVersion)
    {
        if(EZBLReleaseVersion >= 200)
        {
            switch(errorCode)
            {
                case 1:     // 0x0001 EZBL_ERROR_SUCCESS
                    return "Operation completed successfully.";
                case 2:     // 0x0002 EZBL_ERROR_SUCCESS_VER_GAP
                    return "Operation completed successfully, but the programmed image did not have an APPID_VER_MAJOR.APPID_VER_MINOR field that was +1 (minor code) from the existing application.";
                case 3:     // 0x0003 EZBL_ERROR_ALREADY_INSTALLED
                    return "Offered firmware image already matches the existing target firmware.";
                case -20:   // 0xFFEC EZBL_ERROR_COM_READ_TIMEOUT
                    return "Bootloader signaled communications timeout while waiting for image data.";
                case -21:   // 0xFFEB EZBL_ERROR_IMAGE_MALFORMED
                    return "Bootloader rejected firmware as malformed or of unsupported type. Possible communications error.";
                case -22:   // 0xFFEA EZBL_ERROR_BOOTID_HASH_MISMATCH
                    return "Bootloader rejected firmware as incompatible.";
                case -23:   // 0xFFE9 EZBL_ERROR_APPID_VER_MISMATCH
                    return "Bootloader rejected firmware as out of the required programming order.";
                case -24:   // 0xFFE8 EZBL_ERROR_HARD_VERIFY_ERROR
                    return "Bootloader read-back verification failure.";
                case -25:   // 0xFFE7 EZBL_ERROR_SOFT_VERIFY_ERROR
                    return "Bootloader read-back verification mismatch in reserved address range.";
                case -26:   // 0xFFE6 EZBL_ERROR_IMAGE_CRC
                    return "Bootloader computed CRC mismatch with CRC contained in firmware image. Probable communications error.";
                case -27:   // 0xFFE5 EZBL_ERROR_IMAGE_REJECTED
                    return "Bootloader or running application rejected the offered image.";

                default:
                    return null;
            }
        }

        if(EZBLReleaseVersion < 200)
        {
            switch(errorCode)
            {
                case 1:     // EZBL_ERROR_SUCCESS
                    return "Operation completed successfully.";
                // Older, less preferential error codes used in EZBL v1.xx. These are overly specific/wasteful generating on the Bootloader side.
                case -1:    // EZBL_ERROR_TIMEOUT_IN_BLOB_HEADER
                    return "Communications timeout attempting to read the first 4 bytes of the .blob file (where the file's length is contained).";
                case -2:    // EZBL_ERROR_TIMEOUT_IN_RECORD_HEADER
                    return "Communications timeout attempting to read a record header from the .blob file.";
                case -3:    // EZBL_ERROR_ILLEGAL_LENGTH
                    return "Communications corruption occurred or the .blob file contains an illegally long length field in a data record or the overall .blob header.";
                case -4:    // EZBL_ERROR_ILLEGAL_RECORD_ADDRESS
                    return "Communications corruption occurred or the .blob file contains an illegally high record address.";
                case -5:    // EZBL_ERROR_TIMEOUT_IN_RECORD_DATA
                    return "Communications timeout trying to read .blob record data.";
                case -6:    // EZBL_ERROR_TIMEOUT_IN_CRC
                    return "Communications timeout reading last 4 byte CRC field.";
                case -7:    // EZBL_ERROR_BLOB_CRC
                    return "CRC of received .blob contents mismatch with CRC contained in .blob. Probable communications corruption.";
                case -8:    // EZBL_ERROR_READ_BACK_VERIFICATION
                    return "Read-back verification mismatch. Probable configuration error or write protected memory.";
                case -9:    // EZBL_ERROR_BOOTLOADER_MISMATCH
                    return "Read-back verification mismatch. All programming completed, but data in the existing bootloader does not match the bootloader copy in the uploaded image. Make sure you transmitted a correct .hex/.blob file that exactly matches and was built for the installed bootloader. The Application must be compiled with _merge.s and _merge.gld files generated when the bootloader was originally built and deployed.";
                default:
                    return null;
            }
        }

        return null;
    }

    /**
     * Handshakes with the target bootloader, erases the application memory
     * space, and programs the specified .blob artifact into Flash memory.
     *
     * @param artifactPath      .blob/.bl2/.hex file to send to the bootloader
     * @param suppressHandshake true: no handshaking will be performed. The
     *                          connected DEVID/DEVREV/Window buffering size
     *                          data will not be printed to stderr. false:
     *                          ordinary handshaking with the bootloader will be
     *                          done before issuing any erase and programming
     *                          operations. The handshake allows detection of
     *                          certain types of communications corruption in
     *                          advance of transferring the .blob file.
     *
     * @return COMResult structure with the communications status and final
     *         return code from the bootloader.
     */
    @Deprecated
    public COMResult Bootload(String artifactPath, boolean suppressHandshake)
    {
        int bytesUploaded = 0;
        byte dataToTransmit[];
        Blob blob;
        byte blobData[];
        long startTime;
        int remoteWindow = 0x7FFFFFFF;
        int devID;
        int devREV;
        COMResult ret;

        // For statistics keeping, let's get the current time of the JRE
        startTime = System.currentTimeMillis();

        try
        {
            blob = null;
            if(artifactPath.toLowerCase().endsWith(".hex"))
            {
                String hexData = Multifunction.ReadFile(artifactPath, true);
                if(hexData == null)
                {
                    return new COMResult("could not read " + artifactPath, true, false, true);
                }
                blob = new Blob(hexData, true);    // Use string version of Blob constructor (takes a .hex data string)
                DataRecord.CoalesceRecords(blob.records, true, 0x2, 0x2);
                blob.ReorderForSequentialAccess();
                blobData = blob.GetBytes();
            }
            else    // Binary format of any kind (.blob/.bl2) format
            {
                blobData = Multifunction.ReadFileBinary(artifactPath);
                if(blobData == null)
                {
                    return new COMResult("could not read " + artifactPath, true, false, true);
                }
                // If we need to target dual-partition, we need to convert to an
                // ordinary blob first so we can append the FBOOT record before
                // transmission.
                if((state.targetPartition != 0) || (state.devSpecialConf.FBOOTValue != 0xFFFFFF))
                {
                    blob = new Blob(blobData, true);    // Use byte[] array version of Blob construction (takes .blob binary byte array)
                }
            }
        }
        catch(Exception ex)
        {
            return new COMResult("could not parse " + artifactPath + ". " + ex.getMessage(), true, false, true);
        }

        // If we are enabling a dual-partition mode via the command line, create the 0xFE FBOOT config value
        if((state.targetPartition != 0) || (state.devSpecialConf.FBOOTValue != 0xFFFFFF))
        {
            byte FBOOTbytes[] = new byte[3];

            FBOOTbytes[0] = (byte)(state.devSpecialConf.FBOOTValue & 0xFF);
            FBOOTbytes[1] = (byte)((state.devSpecialConf.FBOOTValue & 0xFF00) >> 8);
            FBOOTbytes[2] = (byte)((state.devSpecialConf.FBOOTValue & 0xFF0000) >> 16);

            if((state.targetPartition >= 1) && (state.devSpecialConf.FBOOTValue == 0xFFFFFF))
            {   // Ensure we program the device for a ordinary dual boot mode if the FBOOT value wasn't explicitly specified, but partition 1 or 2 was explicitly specified
                FBOOTbytes[0] = (byte)0xFE;
                FBOOTbytes[1] = (byte)0xFF;
                FBOOTbytes[2] = (byte)0xFF;
            }

            if(state.devSpecialConf.FBOOTAddr == 0)
            {   // If no FBOOT address specified, create records to program both possible cases
                // Note: These must be added to the beginning of the blob records, not at the end
                // like normal. They also should not be coalesced or sorted at this point. This
                // is ordered like this so that the ICSP programmer can see them first and program
                // FBOOT before trying to program any otherwise illegal addresses, like
                // 0x400000+ where the inactive partition should be, but wouldn't be until FBOOT is
                // programmed. FBOOT defaults to single boot mode after a chip erase.
                blob.records.add(0, new DataRecord(0x801000, FBOOTbytes, state.MCU16Mode)); // dsPIC33E address
                blob.records.add(0, new DataRecord(0x801800, FBOOTbytes, state.MCU16Mode)); // PIC24F address
            }
            else
            {   // Otherwise, just add the FBOOT record wherever it was specified
                blob.records.add(0, new DataRecord(state.devSpecialConf.FBOOTAddr, FBOOTbytes, state.MCU16Mode));
            }
            blobData = blob.GetBytes();
        }

        // Auto-baud and handshake to ensure communications is synchronized with
        // the target bootloader
        if(!suppressHandshake)
        {
            ret = HandshakeWithTarget();
            if(ret.error)
            {
                return ret;
            }

            // Read remote DEVID (32-bits), DEVREV (32-bits), buffer size (32-bits)
            ret = SendCommand("#", 13, this.milliTimeout);
            if(ret.error)
            {
                ret.responseText = "after handshaking, did not receive a valid response to the '#' device ID read command.";
                if(ret.responseBinary.length != 0)
                {
                    ret.responseText += String.format("\nReceived %1$d bytes:"
                                                      + "\n{"
                                                      + "\n    ", ret.responseBinary.length);

                    for(int i = 0; i < ret.responseBinary.length; i++)
                    {
                        if((i + 1) % 32 == 0)
                        {
                            ret.responseText += "\n    ";
                        }
                        ret.responseText += String.format("%1$02X ", ret.responseBinary[i]);
                    }
                    ret.responseText += "\n}";
                    ret.responseText = Multifunction.FormatHelpText(100, 0, ret.responseText);

                }
                return ret;
            }
            devID = (((int)ret.responseBinary[1]) & 0xFF) | ((((int)ret.responseBinary[2]) & 0xFF) << 8) | ((((int)ret.responseBinary[3]) & 0xFF) << 16) | ((((int)ret.responseBinary[4]) & 0xFF) << 24);
            devREV = (((int)ret.responseBinary[5]) & 0xFF) | ((((int)ret.responseBinary[6]) & 0xFF) << 8) | ((((int)ret.responseBinary[7]) & 0xFF) << 16) | ((((int)ret.responseBinary[8]) & 0xFF) << 24);
            remoteWindow = (((int)ret.responseBinary[9]) & 0xFF) | ((((int)ret.responseBinary[10]) & 0xFF) << 8) | ((((int)ret.responseBinary[11]) & 0xFF) << 16) | ((((int)ret.responseBinary[12]) & 0xFF) << 24);
            System.out.format("Connected: DEVID, DEVREV: %1$08X, %2$08X; Buffering: %3$d bytes\n", devID, devREV, remoteWindow);
        }

        // Send the bootload program opcode and wait for acknowledgement
        ret = SendCommand("MCHPb", 1, this.milliTimeout);
        if(ret.error || (ret.responseBinary[0] != '+'))
        {
            if(ret.error)
            {
                ret.responseText = "no ACK to bootload start command";
            }
            else
            {
                ret.error = true;
                ret.responseText = String.format("bad bootload request response 0x%1$02X ('" + String.valueOf((char)ret.responseBinary[0]) + "')", ret.responseBinary[0]);
            }
            return ret;
        }

        // First acknowledgement received, look for a second acknowledgement
        // indicating it is time to start sending data. The PIC will normally be
        // busy erasing itself between the last ACK and this second ACK. Erasing
        // can take several seconds. Ex: up to ~6.94s worst case on
        // dsPIC33EP512GM710 if all 342 pages (20.3ms/page erase spec) must be
        // erased.
        //
        // If the PIC chooses to send us status hashes or messages during this
        // time (that doesn't include a '+' or '-' character), we'll display it
        // verbatim.
        System.out.print("Erase");
        while(true)
        {
            ret = ReadResponse(1, this.milliTimeout);
            if(ret.error)
            {
                System.out.println();
                ret.responseText = "erase stage " + (ret.timedOut ? "timeout" : ""); //ret.responseText = "erase stage " + (ret.timedOut ? "timeout" : "") + " (asyncClose = " + String.valueOf(ret.asyncClose) + ", timedOut = " + String.valueOf(ret.timedOut) + ", fileError = " + String.valueOf(ret.fileError) + ", code = " + String.valueOf(ret.code) + ", binaryLen = " + (ret.responseBinary != null ? String.valueOf(ret.responseBinary.length) : "null") + ")";
                return ret;
            }
            if(ret.responseBinary[0] == '-')
            {
                System.out.println();
                ret.responseText = "erase stage (NACK)";
                ret.error = true;
                return ret;
            }
            if(ret.responseBinary[0] == '+')   // ACK received, time to send data
            {
                System.out.println();
                break;
            }
            System.out.append(String.valueOf((char)ret.responseBinary[0])); // No error so far; just print whatever we receive
            System.out.flush();
        }

        // Send out the entire .blob file, but with the remote device sending us
        // 16-bit flow control chunk requests so we don't overrun their RX
        // buffers while it is busy processing the prior data.
        int lastPercentDone = 0;
        System.out.print("Upload progress: |0%         25%         50%         75%        100%|\n");
        System.out.print("                 |");
        System.out.flush();

        int chunkLen = blobData.length / 100;   // Default to 1% chunks for hardware flow control protocols
        while(true)
        {
            if(!state.hardwareFlowControl)
            {
                ret = ReadResponse(2, this.milliTimeout);  // Get the length the recipient wants
                if(ret.error)
                {
                    System.out.println();
                    ret.responseText += String.format("did not receive file chunk request near offset %1$d (of %2$d)", bytesUploaded, blobData.length);
                    return ret;
                }

                // Convert two bytes into an integer length
                chunkLen = (((int)ret.responseBinary[0]) & 0xFF) | ((((int)ret.responseBinary[1]) << 8) & 0xFF00);
            }
            else
            {
                ret = ReadResponse(2, ((blobData.length - bytesUploaded == 0) ? this.milliTimeout : 0));  // Check if the recipient has aborted, but don't waste time if no message is already pending
                if(!ret.error && !ret.timedOut)
                {
                    // Convert two bytes into an integer length
                    chunkLen = (((int)ret.responseBinary[0]) & 0xFF) | ((((int)ret.responseBinary[1]) << 8) & 0xFF00);
                }
            }

            // Sanity check the return value so we can abort early if there is
            // communications corruption occuring.
            if((chunkLen > remoteWindow) && !state.hardwareFlowControl) // I2C/hardware flow control doesn't advirtise a chunk size
            {
                System.out.println();
                ret.error = true;
                ret.responseText += Multifunction.FormatHelpText(100, 0, String.format("remote node requested more data than their reported buffer size (%1$d requested, %2$d buffer size)", chunkLen, remoteWindow));
                return ret;
            }

            // 0x0000 request means we need to terminate or we are done
            if(chunkLen == 0)
            {
                break;
            }

            // Shrink the chunk if the recipient is asking for more data than we have
            if(chunkLen > blobData.length - bytesUploaded)
            {
                chunkLen = blobData.length - bytesUploaded;
            }

            // Send the data (asynchronous)
            dataToTransmit = new byte[chunkLen];
            System.arraycopy(blobData, bytesUploaded, dataToTransmit, 0, chunkLen);
            SendBinary(dataToTransmit);
            bytesUploaded += chunkLen;

            // Update display progress
            int percentDone = bytesUploaded * 100 / blobData.length;
            while(percentDone - lastPercentDone >= 2)
            {
                System.out.append('.');
                System.out.flush();
                lastPercentDone += 2;
            }
        }

        // All bytes uploaded or early abort, get final return code
        int errorCode;
        ret = ReadResponse(2, this.milliTimeout);
        if(ret.error)
        {
            System.out.println();
            ret.responseText = String.format("no return code from target near file offset %1$d (of %2$d)", bytesUploaded, blobData.length);
            return ret;
        }

        errorCode = ((int)ret.responseBinary[0]) & 0xFF;
        errorCode |= ((int)ret.responseBinary[1]) << 8;     // Does sign extension (this is desirable since all error codes are negative numbers)
        if((errorCode <= 0) || (bytesUploaded != blobData.length))
        {
            System.out.println();
            ret.responseText = String.format("remote node aborted with error %1$d around file offset %2$d (of %3$d)\n", errorCode, bytesUploaded, blobData.length);

            String errorString = GetEZBLBootloaderErrorText(errorCode, 100);
            if(errorString == null)
            {
                // Unrecognized error code, let the target try to tell us if it wants to. We shall abort waiting for a message after 100ms of not receiving any new lines.
                COMResult targetGeneratedErrorMessage = ReadLine(100);
                while(!targetGeneratedErrorMessage.responseText.isEmpty())
                {
                    ret.responseText += "\n       " + targetGeneratedErrorMessage.responseText;
                    targetGeneratedErrorMessage = ReadLine(100);
                }
            }
            else
                ret.responseText += Multifunction.FormatHelpText(110, 4, errorString);

            ret.error = true;
            return ret;
        }

        // Print transfer statistics
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.printf("|"
                          + "\n                  %1$d bytes sent in %2$1.3fs (%3$1.0f bytes/second)"
                          + "\n",
                          bytesUploaded, totalTime / 1000.0, bytesUploaded / (totalTime / 1000.0 + 0.000001));   // Add 1 us to make division by 0 impossible without impacting the ret

        // All done!
        ret.error = false;
        ret.timedOut = false;
        ret.asyncClose = false;
        ret.responseText = "Bootload complete";
        return ret;
    }

    /**
     * Alternate EZBL v2.xx+ Bootloading protocol for half-duplex, point to
     * multi-point bus oriented mediums, and applications not needing full
     * handshaking/extraneous round-trip tests.
     *
     * @param artifactPath    .bl2 or .hex file to send to the bootloader. If a
     *                        .hex file, it is converted to a .bl2 file first.
     *                        If any other file, it is read as a binary file and
     *                        simply sent to the remote node unprocessed.
     *
     * @param softFlowControl Indicates if the EZBL v2.xx+ software flow control
     *                        signaling should be enabled (as normally required)
     *                        or disabled.
     *
     * This should be false only if you know the underlying physical medium has
     * suitable flow control already in it, the Bootloader target has been
     * modified to not generate software flow control signaling, or the remote
     * node isn't an EZBL Bootloader at all and you just want to transmit a file
     * anyway.
     *
     * @return COMResult structure with the communications status and final
     *         return code from the bootloader.
     */
    public COMResult BootloadBL2(String artifactPath, boolean softFlowControl)
    {
        int bytesUploaded;
        byte[] imgBytes;
        long startTime;
        COMResult ret = new COMResult(new byte[2], true, false, false);
        int chunkLen;
        int lastPercentDone;
        boolean firstRead;
        boolean doneSending;
        int maxKeepAlives = 8192;   // Upper limit to signal babeling and not Bootloader traffic
        long allUploadedTime = 0;

        try
        {
            if(artifactPath.matches(".*?\\.[hH][eE][xX]$"))
            {
                String hexData = Multifunction.ReadFile(artifactPath, true);
                if(hexData == null)
                {
                    return new COMResult("could not read " + artifactPath, true, false, true);
                }
                Bl2b bl2b = new Bl2b(hexData);    // Use string version of Bl2b constructor (takes a .hex data string)
                bl2b.Coalesce(true, 0, 0);
                bl2b.ReorderForSequentialAccess();
                imgBytes = bl2b.GetBytes();
            }
            else    // Binary format of any kind (.bl2 format normally)
            {
                imgBytes = Multifunction.ReadFileBinary(artifactPath);
                if(imgBytes == null)
                {
                    return new COMResult("could not read " + artifactPath, true, false, true);
                }
            }
        }
        catch(Exception ex) // Could happen when converting .hex file to .bl2 if .hex file contains invalid data
        {
            return new COMResult("could not parse " + artifactPath + ". " + ex.getMessage(), true, false, true);
        }

        // Ensure the input and output threads are opened before attempting to
        // send anything so we can error out if there is no way to send anything
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < milliTimeout)
        {
            if(((this.out != null) && (this.in != null)) || !this.writeCOMThread.isAlive() || !this.readCOMThread.isAlive())
            {
                break;
            }
            try
            {
                Thread.sleep(2);
            }
            catch(InterruptedException ex)
            {
                // Waking up is fine...
            }
        }
        if((this.out == null) || !this.writeCOMThread.isAlive())
        {
            String port = port = this.comPort.startsWith("\\\\.\\") ? this.comPort.substring(4) : this.comPort; // Trim off "\\.\" leading chars, if present
            ret = new COMResult(String.format("cannot open '%1$s'. Ensure the port is valid, not already in use by another application and that ezbl_comm.exe is not blocked by antivirus software.", port), true, true, true);
// Using this code requires the lib\jna.jar file be distributed, which we aren't currently doing
//            List<String> portList = EnumCOMPorts();
//            if((portList != null) && !portList.isEmpty())
//            {
//                ret.responseText += "\nSystem port list contains: ";
//                for(String s : portList)
//                {
//                    ret.responseText += s + ", ";
//                }
//                ret.responseText = ret.responseText.substring(0, ret.responseText.length() - 2);
//            }
            ret.responseBinary = new byte[0];
            return ret;
        }

        // Purge anything in the RX buffers, if any. Generally, this should do 
        // nothing, but do this anyway in case if the lower level hardware 
        // drivers buffer some RX data until a consumer thread opens the port, 
        // we could have junk/application-specific UART data immediately 
        // presented to us.
        ReadResponse(65536, 1);

        // Send the .bl2 file header
        SendBinary(imgBytes, 0, (imgBytes.length >= 64 ? 64 : imgBytes.length));
        bytesUploaded = (imgBytes.length >= 64 ? 64 : imgBytes.length);
        if(softFlowControl)
        {
            ret = ReadResponse(2, milliTimeout);    // Get the first length the recipient wants/erase status notification/acknowledgement that they want this file
            if(ret.timedOut)
            {
                ret.responseText = "no target response";
                return ret;
            }
        }
        lastPercentDone = 0;
        System.out.print("Upload progress: |0%         25%         50%         75%        100%|\n");
        System.out.print("                 |");
        System.out.flush();
        firstRead = true;

        doneSending = false;
        while(true)
        {
            if(softFlowControl)
            {
                if(!firstRead)
                {
                    ret = ReadResponse(2, milliTimeout);  // Get the length the recipient wants
                    if(ret.error)
                    {
                        System.out.println();
                        ret.responseText += String.format("did not receive file chunk request near offset %1$d (of %2$d)", bytesUploaded, imgBytes.length);
                        return ret;
                    }
                }
                firstRead = false;
                chunkLen = (((int)ret.responseBinary[0]) & 0xFF) | (((int)ret.responseBinary[1]) << 8); // Convert two bytes into a signed integer length

                // 0x0000 request means we need to terminate or we are done
                if(chunkLen == 0)
                {
                    break;
                }

                if((chunkLen < 0) || (chunkLen == 0x1111) || (chunkLen == 0x1313))    // Erase phase generates negative values as keep alives, 0x1111 and 0x1313 reserved for XON and XOFF compatibility
                {
                    maxKeepAlives--;        // Abort if junk is being continuously received (probably not bootloader traffic taking place)
                    if(maxKeepAlives == 0)
                    {
                        break;
                    }
                    continue;
                }

                // Shrink the chunk if the recipient is advirtising buffer free space greater than the data we have left until EOF
                if(chunkLen > imgBytes.length - bytesUploaded)
                {
                    chunkLen = imgBytes.length - bytesUploaded;
                }
            }
            else
                chunkLen = imgBytes.length - bytesUploaded >= 1024 ? 1024 : imgBytes.length - bytesUploaded;

            // Send the data asynchronously
            SendBinary(imgBytes, bytesUploaded, chunkLen);
            bytesUploaded += chunkLen;
            if(!doneSending && (bytesUploaded == imgBytes.length))
            {
                doneSending = true;
                allUploadedTime = System.currentTimeMillis(); // Start a timer to terminate listening if we don't get a termination code from the remote node (to guard against babeling)
            }
            else if(doneSending)
            {
                if(System.currentTimeMillis() - allUploadedTime >= milliTimeout)  // Timeout if remote node doesn't signal completion for a long time
                {
                    break;
                }
            }

            // Update display progress
            int percentDone = bytesUploaded * 100 / imgBytes.length;
            while(percentDone - lastPercentDone >= 2)
            {
                System.out.append('.');
                System.out.flush();
                lastPercentDone += 2;
            }

            if(!this.writeCOMThread.isAlive())
            {
                break;
            }
        }

        // All bytes uploaded or early abort, get final return code
        int errorCode;
        ret = ReadResponse(2, this.milliTimeout);
        if(ret.error)
        {   // Bail if we could not read the return status code
            System.out.println();
            ret.responseText = String.format("no return code from target near file offset %1$d (of %2$d)", bytesUploaded, imgBytes.length);
            return ret;
        }

        errorCode = ((int)ret.responseBinary[0]) & 0xFF;
        errorCode |= ((int)ret.responseBinary[1]) << 8;     // Does sign extension (this is desirable since all error codes are negative numbers)
        if((errorCode <= 0) || (bytesUploaded != imgBytes.length))
        {
            System.out.println();
            ret.responseText = String.format("remote node aborted with error %1$d around file offset %2$d (of %3$d)\n", errorCode, bytesUploaded, imgBytes.length);

            String errorString = GetEZBLBootloaderErrorText(errorCode, 204);
            if(errorString == null)
            {
                // Unrecognized error code, let the target try to tell us if it wants to. We shall abort waiting for a message after 100ms of not receiving any new lines.
                COMResult targetGeneratedErrorMessage = ReadLine(100);
                if((targetGeneratedErrorMessage.responseText != null)
                   && !targetGeneratedErrorMessage.responseText.isEmpty()
                   && (targetGeneratedErrorMessage.responseText.matches("[\\x01-\\x7E]*$")))
                {
                    ret.responseText += "\n       " + targetGeneratedErrorMessage.responseText;
                }
                else
                {
                    ret.responseText += "\n       Invalid error code (communications corrupt or bootloader not executing)";
                }
            }
            else
                ret.responseText += Multifunction.FormatHelpText(110, 4, errorString);

            ret.error = true;
            return ret;
        }

        // Print transfer statistics
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.printf("|"
                          + "\n                  %1$d bytes sent in %2$1.3fs (%3$1.0f bytes/second)"
                          + "\n",
                          bytesUploaded, totalTime / 1000.0, bytesUploaded / (totalTime / 1000.0 + 0.000001));   // Add 1 us to make division by 0 impossible without impacting the ret

        // All done!
        ret.error = false;
        ret.timedOut = false;
        ret.asyncClose = false;
        ret.responseText = "Bootload complete";
        return ret;
    }
}


// A Class that can return a binary array, a binary string, plus a few booleans
// indicating what any particular read/write operation resulted in.
class COMResult
{
    String responseText = null;
    byte responseBinary[] = null;
    boolean timedOut = false;
    boolean error = false;
    boolean asyncClose = false;
    boolean fileError = false;
    int code = 0;

    public COMResult()
    {
    }

    public COMResult(String responseText, boolean error, boolean timedOut, boolean fileError)
    {
        this.responseText = responseText;
        this.error = error;
        this.timedOut = timedOut;
        this.fileError = fileError;
    }

    public COMResult(byte responseBinary[], boolean error, boolean timedOut, boolean fileError)
    {
        this.responseBinary = responseBinary;
        this.error = error;
        this.timedOut = timedOut;
        this.fileError = fileError;
    }
}
