/**
 * File:   main.c
 *
 * Example LED blinking application to demonstrate successful building of an 
 * application containing an EZBL Bootloader on PIC32MM devices.
 *
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
#include "ezbl_integration/ezbl.h"


// Optional App defined Bootloader callback to reject or accept w/safe App shutdown in response to a background external firmware update offer
int EZBL_BootloadRequest(EZBL_FIFO *rxFromRemote, EZBL_FIFO *txToRemote, unsigned long fileSize, EZBL_APPID_VER *appIDVer);


int main(void)
{
    unsigned long ledTimer;

    // Connect Bootloader's EZBL_AppPreInstall function pointer to our
    // EZBL_BootloadRequest function. If you delete this line, the Bootloader
    // will decide how to handle background Bootload requests (default: kill App
    // mid execution, accept firmware update and erase this App).
    EZBL_AppPreInstall = EZBL_BootloadRequest;

    // Use this if you wish to disable all background operations in the
    // Bootloader, such as communications interrupts/software FIFOing.
    // NOTE: effect is immediate, so make sure to disable the interrupts or have
    // an ISR that is ready to accept them in this Application project.
    //EZBL_ForwardAllIntToApp();

    // XC32 initializer disables global interrupts, so explicitly restore them
    __builtin_enable_interrupts();


//    // Optionally let the world know that we are not a brick! Whoohoo!
//    EZBL_FIFOSetBaud(EZBL_STDOUT, -230400);
//    EZBL_printf("\x0F\n\n\nApp v%u.%02u.%04lu starting\n", EZBL_appIDVer.appIDVerMajor, EZBL_appIDVer.appIDVerMinor, EZBL_appIDVer.appIDVerBuild); // \x0F is a SI "Shift In" control code so terminal emulators go back to the correct character set if they got changed while we were being programmed or reset.

    ledTimer = NOW_32();
    while(1)
    {
        ClrWdt();
        EZBL_BootloaderTask();         // Periodically call this to allow background detection of a Bootload request. To only allow bootloading only when the Bootloader is executing, comment this out or stop calling it periodically.
        
        // Every 500ms toggle an LED
        if(NOW_32() - ledTimer > NOW_sec/2u)
        {
            ledTimer += NOW_sec/2u;
            LEDToggle(0x01);
        }

        #if defined(XPRJ_usb_msd)
        // For USB Host MSD projects (MPLAB "usb_msd" Build Configuration),
        // periodically call the FILEIODemoProc() function in
        // usb_msd_fileio_demo/fileio_demo.c to show the USB Host Stack and FILEIO
        // library APIs in Bootloader flash being reused by this Application
        int FILEIODemoProc(void);   // Prototype for USB Host MSD FILEIO demo App code in fileio_demo.c
        FILEIODemoProc();
        #endif
    }
}


/**
 * Optional callback function invoked when the EZBL Bootloader wants to
 * terminate and erase this Application. Use this function to safely terminate
 * any ongoing hardware operations or reject the offered firmware image and
 * prevent this application from being erased.
 *
 * If last minute notification or rejection opportunity is not needed or
 * applicable for the bootloader in use, this function can be removed and the
 * EZBL_AppPreInstall pointer should be left unreferenced (i.e. leave it set to
 * null).
 *
 * @param *rxFromRemote Pointer to the RX EZBL_FIFO applicable to this offering.
 *
 *                      If the offering medium does not have an RX EZBL_FIFO
 *                      associated with it (Ex: FILEIO/USB thumb drive or local
 *                      memory type Bootloaders), then this pointer is null.
 *
 * @param *txToRemote Pointer to the TX EZBL_FIFO applicable to this offering.
 *
 *                    If the offering medium does not have a TX EZBL_FIFO
 *                    associated with it (Ex: FILEIO/USB thumb drive or local
 *                    memory type Bootloaders), then this pointer is null.
 *
 * @param fileSize Total file size of the .bl2 file being offered, including
 *                 the SYNC bytes and .bl2 header.
 *
 * @param *appIDVer Pointer to the EZBL_APPID_VER data contained in the offered
 *                  .bl2 file header.
 *
 *                  By the time this callback is called, the BOOTID_HASH has
 *                  already been confirmed as a match for the Bootloader.
 *                  Additionally, if the EZBL_NO_APP_DOWNGRADE feature is
 *                  enabled, the offered APPID_VER value has already been
 *                  confirmed acceptable before this callback occurs.
 *
 * @return Return 0 to reject the offered firmware image or 1 to accept the
 *         image and proceed with erase/programming steps.
 *
 *         NOTE: When this callback is executed, the remote node (if applicable)
 *         is left waiting and will timeout if a response takes too long to
 *         arrive. To ensure a communications timeout does not occur, limit code
 *         in this callback to less than the remote node's communications
 *         timeout (typically around 1 second, but configurable and not
 *         applicable for local memory bootloaders).
 */
int EZBL_BootloadRequest(EZBL_FIFO *rxFromRemote, EZBL_FIFO *txToRemote, unsigned long fileSize, EZBL_APPID_VER *appIDVer)
{
    // Implement any Application shutdown code here. The Bootloader is about to
    // disable all interrupts and start erasing the flash.

//    // Alternatively, we can complain and abort the shutdown/erase attempt
//    if(ButtonPeek() & 0x1u)   // Example triggers when EZBL_ButtonMap's LSb is held down
//    {
//        EZBL_FIFOWrite32(txToRemote, (((unsigned long)EZBL_ERROR_CUSTOM_MESSAGE)<<16) | 0x0000u);
//        EZBL_FIFOWriteStr(txToRemote, "I'm busy. Try reprogramming me some other time.");
//        return 0;
//    }

    return 1;   // 1 = Allow erase/programming, this App dies. 0 = Reject the firmware update, keeping this App running unharmed.
}


/**
 * Application General Exception Handler - optional, but useful for debugging.
 *
 * @param cause is the MIPS Coprocessor 0, Register 13, Select 0 value captured
 *              just after the exception occurred. The EXCCODE field @
 *              CAUSE<6:2> is likely a useful starting point towards identifying
 *              the offending operation.
 *
 * @param status is CP0, Register 12, Select 0 value captured just after the
 *               exception
 *
 * @param *cpuRegs Pointer to a EZBL_CPU_REGS structure containing all CPU
 *                 register contents at the time of the exception. Any
 *                 modification to v[0-1], t[0-9], a[0-3], k[0-1], at, or hilo
 *                 will get loaded back into the CPU upon return from this
 *                 exception handler.
 *
 *                 To receive this parameter, you must use the
 *                 EZBL_general_exception() function signature and keep the
 *                 EZBL_general_exception_context symbol.
 *
 * @param *cp0Regs Pointer to a EZBL_CP0_REGS structure containing all
 *                 Coprocessor 0 register contents at the time of the exception.
 *                 This structure is NOT written back to CP0 upon return.
 *
 *                 To receive this parameter, you must use the
 *                 EZBL_general_exception() function signature and keep the
 *                 EZBL_general_exception_context symbol.
 *
 * @return void
 *
 *         If the EZBL_general_exception() function is used,
 *         _general_exception_handler() will be called upon return (if
 *         implemented as well).
 */
//void _general_exception_handler(EZBL_MIPS_CAUSE cause, EZBL_MIPS_STATUS status)   // Standard XC32 handler, but with easier to decode data types for the cause and status.
//EZBL_KeepSYM(EZBL_TrapHandler);                                                   // Uncomment for advanced general_exception_handler that prints lots of debug data to EZBL_STDOUT. See also EZBL_general_exception().
//EZBL_KeepSYM(EZBL_general_exception_context);                                     // Uncomment if using EZBL_general_exception()
//void EZBL_general_exception(EZBL_MIPS_CAUSE cause, EZBL_MIPS_STATUS status, EZBL_CPU_REGS *cpuRegs, EZBL_CP0_REGS *cp0Regs)   // More advanced exception handler offered by EZBL with a snapshot of the CPU and Coprocessor 0 registers at the instant the exception occurred. To use this function, the EZBL_general_exception_context symbol must be kept.
//{
//    LEDOn(0xFFFF);    // Visually indicate an unhandled exception
//    EZBL_printf("\n\nException in Application. EPC is 0x%08x", __FUNCTION__, EZBL_MFC0(EZBL_CP0_EPC) & 0xFFFFFFFEu);
//    EZBL_FIFOFlush(EZBL_STDOUT, NOW_sec);
//    while(1);
//}
