/**
 * File:   fileio_demo.c
 *
 * Example for USB Host MSD Application projects ("usb_msd" Build Configuration)
 *
 * Demonstrates FILEIO library APIs in Bootloader flash being reused by this
 * Application for file write/read/delete of "TEST.TXT" file on the USB MSD
 * media.
 */

/*******************************************************************************
  Copyright (C) 2018 Microchip Technology Inc.

  MICROCHIP SOFTWARE NOTICE AND DISCLAIMER:  You may use this software, and any
  derivatives created by any person or entity by or on your behalf, exclusively
  with Microchip's products.  Microchip and its licensors retain all ownership
  and intellectual property rights in the accompanying software and in all
  derivatives here to.

  This software and any accompanying information is for suggestion only.  It
  does not modify Microchip's standard warranty for its products.  You agree
  that you are solely responsible for testing the software and determining its
  suitability.  Microchip has no obligation to modify, test, certify, or
  support the software.

  THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS".  NO WARRANTIES, WHETHER
  EXPRESS, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED
  WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR
  PURPOSE APPLY TO THIS SOFTWARE, ITS INTERACTION WITH MICROCHIP'S PRODUCTS,
  COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION.

  IN NO EVENT, WILL MICROCHIP BE LIABLE, WHETHER IN CONTRACT, WARRANTY, TORT
  (INCLUDING NEGLIGENCE OR BREACH OF STATUTORY DUTY), STRICT LIABILITY,
  INDEMNITY, CONTRIBUTION, OR OTHERWISE, FOR ANY INDIRECT, SPECIAL, PUNITIVE,
  EXEMPLARY, INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE, FOR COST OR EXPENSE OF
  ANY KIND WHATSOEVER RELATED TO THE SOFTWARE, HOWSOEVER CAUSED, EVEN IF
  MICROCHIP HAS BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE.
  TO THE FULLEST EXTENT ALLOWABLE BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL
  CLAIMS IN ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF
  FEES, IF ANY, THAT YOU HAVE PAID DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.

  MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE
  TERMS.
*******************************************************************************/

#include <xc.h>
#include <stdint.h>
#include "../ezbl_integration/ezbl.h"
#include "fileio.h"



void USBHostTasks(void);                    // Function in Bootloader
void USBHostMSDTasks(void);                 // Function in Bootloader


extern const FILEIO_DRIVE_CONFIG gUSBDrive; // Declared in Bootloader to map FILEIO APIs to USB Host MSD physical layer
extern uint8_t deviceAddress;               // Declared in Bootloader to record/access USB media parameters

FILEIO_OBJECT file;


unsigned int fifoOnWrite(unsigned int __attribute__((unused)) bytesWritten, void *writeData, unsigned int dataLen, EZBL_FIFO *writeFIFO)
{
    if(FILEIO_Write(writeData, 1, dataLen, &file) == dataLen)
        LEDSet(0x01);       // Write success
    else
        LEDSet(0x00);       // Write failure
    writeFIFO->dataCount = 0;   // Throw everything in the FIFO away. We are just using it as a way to capture EZBL_DumpFlash() output data and redirect it to the current FILEIO file.
    return dataLen;
}

/**
 * Callback function exercising USB Host MSD Bootloader's FILEIO read, write
 * and delete functionally without duplicating flash/RAM requirements
 * already paid for in the ex_boot_usb_msd Bootloader project.
 *
 * Step 1: Insert USB thumb drive (FAT16/FAT32 formatted)
 * Step 2: Push a button on the PCB and observe return code on LEDs
 * Step 3: Look for "TEST.TXT" file modifications on thumb drive using PC
 */
int FILEIODemoProc(void)
{
    EZBL_FIFO   *stdoutSave;
    EZBL_FIFO   fileWriteFIFO;
    uint8_t     buf[64];


    ButtonRead();   // Populate ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled global bit mask variables
    if((ButtonsPushed & 0x7u) == 0x0u)
        return 0;

    LEDSet(0x7);                            // One or more buttons pushed
    if(FILEIO_MediaDetect(&gUSBDrive, &deviceAddress))
    {
        LEDSet(0x6);                        // Media detected
        if(FILEIO_DriveMount('A', &gUSBDrive, &deviceAddress) == FILEIO_ERROR_NONE)
        {
            LEDSet(0x05);                   // Media mounted

            // Write a "TEST.TXT" test file if Button bit 0 pushed (SW1 on dsPIC33E USB Starter Kit II/PIC24E USB Starter Kit)
            if(ButtonsPushed & 0x1u)
            {
                if(FILEIO_Open(&file, "TEST.TXT", FILEIO_OPEN_WRITE | FILEIO_OPEN_CREATE | FILEIO_OPEN_TRUNCATE) == FILEIO_RESULT_SUCCESS)
                {
                    LEDSet(0x04);           // File opened
                    if(FILEIO_Write("Hello World! I offer you my RAM:\r\n", 1, sizeof("Hello World! I offer you my RAM:\r\n")-1, &file) == sizeof("Hello World! I offer you my RAM:\r\n")-1)
                        LEDSet(0x01);       // Write success
                    else
                        LEDSet(0x00);       // Write failure

                    // Dump human readable hex RAM contents to TEST.TXT as well
                    stdoutSave = EZBL_STDOUT;
                    EZBL_FIFOReset(&fileWriteFIFO, buf, sizeof(buf), fifoOnWrite, 0);
                    EZBL_STDOUT = &fileWriteFIFO;
                    EZBL_DumpRAM((const unsigned int*)__KSEG0_DATA_MEM_BASE, __KSEG0_DATA_MEM_LENGTH);          // Print RAM contents
                    EZBL_printf("\r\n\r\n\r\nI seem to have Config words to share too:\r\n");
                    EZBL_DumpFlash(0xBFC01740u, 0xBFC01800u);     // Also print Config words row
                    EZBL_FIFOFlush(EZBL_STDOUT, 0);
                    EZBL_STDOUT = stdoutSave;

                    // All done writing, so clean up
                    FILEIO_Close(&file);
                }
            }

            // Read a "TEST.TXT" test file if Button bit 1 pushed (SW2 on dsPIC33E USB Starter Kit II/PIC24E USB Starter Kit)
            if(ButtonsPushed & 0x2u)
            {
                if(FILEIO_Open(&file, "TEST.TXT", FILEIO_OPEN_READ) == FILEIO_RESULT_SUCCESS)
                {
                    LEDSet(0x04);           // File opened
                    if(FILEIO_Read(buf, 1, sizeof("Hello World! I offer you my RAM:\r\n")-1, &file) == sizeof("Hello World! I offer you my RAM:\r\n")-1)
                    {
                        LEDSet(0x03);       // Read success
                        if(EZBL_RAMCompare(buf, "Hello World! I offer you my RAM:\r\n", sizeof("Hello World! I offer you my RAM:\r\n")-1) == 0)
                            LEDSet(0x01);   // Data comparison success
                    }
                    else
                        LEDSet(0x00);       // Read failure
                    FILEIO_Close(&file);
                }
            }

            // Delete a "TEST.TXT" test file if Button bit 2 pushed (SW3 on dsPIC33E USB Starter Kit II/PIC24E USB Starter Kit)
            if(ButtonsPushed & 0x4u)
            {
                if(FILEIO_Remove("TEST.TXT") == FILEIO_RESULT_SUCCESS)
                    LEDSet(0x01);           // File deletion success
                else
                    LEDSet(0x00);           // File deletion failure
            }

            FILEIO_DriveUnmount('A');
        }
    }

    // Block for a couple seconds so a human can view the LED result
    NOW_Wait(2u*NOW_sec);
    return LEDSet(0x00);
}
