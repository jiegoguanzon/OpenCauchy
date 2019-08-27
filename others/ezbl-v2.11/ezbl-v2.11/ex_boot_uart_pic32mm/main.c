/**
 * Easy Bootloader for PIC32MM UART Bootloader example main file.
 * Interrupt Service Routines implemented in this project are:
 *   PIC32MM0064GPL036 PIM:
 *    - UART 2 RX @ IPL1 (defined in ezbl_lib32mm.a, see ezbl_lib32mm -> weak_defaults/uart2_fifo.c; can be a different UART instance, see UART_Reset() in hardware initializer)
 *    - UART 2 TX @ IPL1 (defined in ezbl_lib32mm.a, see ezbl_lib32mm -> weak_defaults/uart2_fifo.c; can be a different UART instance, see UART_Reset() in hardware initializer)
 *   PIC32MM0256GPM064 PIM:
 *    - UART 1 RX @ IPL1 (see ezbl_lib32mm -> weak_defaults/uart1_fifo.c)
 *    - UART 1 TX @ IPL1 (see ezbl_lib32mm -> weak_defaults/uart1_fifo.c)
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


// EZBL ezbl_lib32mm.a link-time options:
//EZBL_SetSYM(EZBL_NO_APP_DOWNGRADE, 1);    // Uncomment to disallow upload of App image with a version/build number that is < existing App version (assuming a valid App exists). Note: This can be circumvented if someone starts uploading a recent App image, they interrupt power or communications, then upload older firmware. With nowhere to store an offered Application but in an erased flash, the act of starting a valid upload results in prior knowledge of which older version(s) are disallowed.
//EZBL_SetSYM(EZBL_FLOW_THRESHOLD, 60);     // Optional water mark to adjust when outbound flow control advertisements are generated. Small values consume more TX bandwidth by generating more messages. See EZBL_Install2Flash() API documentation for additional information.


// General Bootloader timeout governing reset idle time to initial Application
// launch and bootload termination when communications are broken.
#define BOOTLOADER_TIMEOUT          (NOW_sec * 1u)


// Bootloader global variables and flags
EZBL_INSTALL_CTX EZBL_bootCtx __attribute__((persistent));  // State tracking structure for active bootload operations and timeout
int __attribute__((keep)) (*EZBL_AppPreInstall)(EZBL_FIFO *rxFromRemote, EZBL_FIFO *txToRemote, unsigned long fileSize, EZBL_APPID_VER *appIDVer) = 0;    // RAM function pointer to allow a running Application to accept/reject an incoming bootload request, and/or do shutdown tasks prior to being erased.



int main(void)
{
    unsigned long ledTimer;

    // Configure interrupt vectoring. The forwarding destination/addresses will
    // be undefined until this (or EZBL_ForwardAllIRQToApp()) is called at least
    // once globally as this macro sets up pointers in RAM to the applicable ISR
    // functions.
    EZBL_ForwardAllIRQToBoot();

    InitializeBoard();

    EZBL_bootCtx.state = SM_SYNC_INIT;
    EZBL_bootCtx.timeout = BOOTLOADER_TIMEOUT;
    EZBL_bootCtx.lastActivity = NOW_32();

    ledTimer = NOW_32();

    while(1)
    {
        ClrWdt();

        // Every 62.5ms toggle a heartbeat LED (8 Hz blink rate) indicating this Bootloader is executing
        if(NOW_32() - ledTimer > NOW_sec/16u)
        {
            ledTimer += NOW_sec/16u;
            LEDToggle(0x01);            // Toggles an LED pin according to EZBL_DefineLEDMap() in hardware initializer
        }

        // Check for new firmware offerings or continue an existing bootload session
        EZBL_BootloaderTask();
    }
}


/**
 * Function to periodically call in order to process incoming App firmware
 * update offerings. The EZBL_Install2Flash() function does nothing and returns
 * immediately if the communications RX ISR has not detected the Bootloader wake
 * up key and set the EZBL_COM_RX + EZBL_COM_TX global FIFO pointers to the
 * matched communications interface.
 *
 * Attributes, noinline and keep, are assigned to prevent the compiler from
 * inlining this function into main() when high optimization settings are enabled.
 * Retaining this function name allows subsequent Application projects to also
 * call this function and allow background receipt of firmware update offerings.
 *
 * If a valid firmware image built for this bootloader (i.e contains matching
 * BOOTID_HASH in the header, as generated from the
 * BOOTID_VENDOR/BOOTID_MODEL/BOOTID_NAME/BOOTID_OTHER strings in the
 * ezbl_boot.mk makefile, EZBL_Install2Flash() will call EZBL_PreInstall(), and
 * if allowed, proceed to erase the Application space flash and reprogram it.
 * Firmware update will be aborted without erasing anything if the
 * EZBL_PreInstall() callback returns 0.
 *
 * @return 0 - value not used.
 */
int __attribute__((noinline, keep)) EZBL_BootloaderTask(void)
{
    // Reset UART baud rate to auto-baud if a data error occurs. This allows
    // bootloading with auto-baud when the Application is running without
    // normally interfering with an Application defined constant baud rate for
    // the same UART.
    if((EZBL_COMBaud <= 0) && (EZBL_COMBootIF->activity.any & (EZBL_COM_ACTIVITY_FRAMINGERROR_MASK | EZBL_COM_ACTIVITY_PARITYERROR_MASK)))
        EZBL_FIFOSetBaud(EZBL_COMBootIF, EZBL_COMBaud);


    // Do bootloader processing when the communications RX ISR has detected the
    // BOOTLOADER_WAKE_KEY sequence ("MCUPHCME"). Reception of this string sets
    // the EZBL_COM_RX/EZBL_COM_TX global pointers to the FIFOs that we will
    // bootload with.
    if(EZBL_COM_RX)
    {
        // Run the EZBL state machine that reads from the communications port via
        // EZBL_COM_RX, responds via EZBL_COM_TX, handles flow control, erases flash
        // (only when valid firmware offering presented and EZBL_PreInstall()
        // callback returns 1), programs flash, validates with CRC and read-back,
        // then programs a App-valid flag to allow new Application execution. For
        // the first call, the EZBL_INSTALL_CTX.state, and timeout fields must be
        // pre-configured (done during main() initialization):
        //     EZBL_bootCtx.state = SM_SYNC_INIT;
        //     EZBL_bootCtx.timeout = BOOTLOADER_TIMEOUT;
        EZBL_Install2Flash(&EZBL_bootCtx);

        // Optionally display "Bootloader rejected firmware as incompatible" error
        // on host if a .bl2 file with BOOTID_HASH value is sent to us which doesn't
        // match our own BOOTID_HASH. I.e. firmware is intended for some other
        // bootloader.
        //if(EZBL_bootCtx.bootCode == EZBL_ERROR_BOOTID_HASH_MISMATCH)
        //{
        //    EZBL_FIFOWrite32(EZBL_COM_TX, (((unsigned long)EZBL_bootCtx.bootCode)<<16) | 0x0000u);
        //    EZBL_bootCtx.bootCode = 0;  // Prevent retransmission
        //}
        
        return 0;
    }


    // If the Bootloader Project is currently executing (not Application),
    // timeout and launch the Application after the BOOTLOADER_TIMEOUT delay
    // elapses with continuous communications silence.
    if(!EZBL_appIsRunning)
    {
        if(EZBL_COMBootIF->activity.any)
        {   // Restore full BOOTLOADER_TIMEOUT interval if something observed
            EZBL_bootCtx.lastActivity = NOW_32();
            EZBL_COMBootIF->activity.any = 0;
        }
        if(NOW_32() - EZBL_bootCtx.lastActivity > EZBL_bootCtx.timeout)
        {
            if(EZBL_COMBaud <= 0)
                EZBL_FIFOSetBaud(EZBL_COMBootIF, EZBL_COMBaud); // Sets EZBL_COMBootIF->activity.other, so next loop will update EZBL_bootCtx.lastActivity

            if(EZBL_IsAppPresent())
            {
                LEDOff(0xFF);
                // Example interrupt management calls for disabling Bootloader interrupts letting the Application project handle them instead
                //EZBL_ForwardAllIRQToApp();
                //EZBL_ClrIntEn(EZBL_STDOUT->irqNum);
                //EZBL_ClrIntEn(EZBL_STDIN->irqNum);
                //EZBL_STDOUT = 0;
                //EZBL_STDIN = 0;
                EZBL_StartAppIfPresent();
            }
        }
    }

    return 0;
}


/**
 * Callback function executed by EZBL_Install2Flash() just before the existing
 * Application (if any) is erased, permitting last minute rejection of a
 * firmware update or an opportunity to cleanly shut down any running operations
 * (especially if they might interfere with successful Bootloading). See
 * callback documentation for EZBL_PreInstall() in ezbl.h for more information.
 */
int EZBL_PreInstall(EZBL_FIFO *rxFromRemote, EZBL_FIFO *txToRemote, unsigned long fileSize, EZBL_APPID_VER *appIDVer)
{
    if(EZBL_appIsRunning && EZBL_AppPreInstall)
        return (*EZBL_AppPreInstall)(rxFromRemote, txToRemote, fileSize, appIDVer);

    return 1; // Accepts update offering if no App installed or running right now
}


/**
 * Bootloader General Exception Handler - optional, but useful for debugging.
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
//    EZBL_printf("\n\nException in Bootloader. EPC is 0x%08x", __FUNCTION__, EZBL_MFC0(EZBL_CP0_EPC) & 0xFFFFFFFEu);
//    EZBL_FIFOFlush(EZBL_STDOUT, NOW_sec);
//    while(1);
//}
