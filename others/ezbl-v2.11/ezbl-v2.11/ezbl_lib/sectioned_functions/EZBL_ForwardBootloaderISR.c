/*******************************************************************************
  Easy Bootloader for PIC24 and dsPIC33 Library Source File

  Summary:
    EZBL_ForwardBootloaderISR variable definition

 *******************************************************************************/

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

#if defined(__XC16__)
/**
 * RAM bitfield controlling/indicating which interrupts that the Bootloader
 * implements should be passed to the Application's interrupt handler or 
 * processed by the Bootloader's interrupt handler.
 *
 * This is a 32-bit global variable where each bit represents one IRQ/Vector
 * each, but pruned to only contain bits for interrupts actually implemented in
 * the Bootloader project. Both Application and Bootloader projects can read and
 * write bits within this variable at run time. At Bootloader reset, the CRT
 * will zero all bits in this "virtual RAM register", and at Application launch,
 * the contents defined by the Bootloader will stay unchanged (i.e. the
 * Application's CRT execution will not reset the contents of this virtual
 * register).
 *
 * - A '1' bit indicates the interrupt should be passed untouched and with
 *   minimal delay to the Application project. The Bootloader's ISR for the same
 *   interrupt will be ignored.
 * - A '0' bit indicates the interrupt would be handled by the Bootloader
 *   project's ISR and the Application project will never see the interrupt.
 * - All unused bits are don't care values.
 * - Being defined as an unsigned long, only 32-total Bootloader ISRs can be
 *   controlled with this global variable. If more than 32 ISRs are defined in
 *   your Bootloader project, linking of the Bootloader project will trigger a
 *   build-time error (and you should reconsider having so much code in your
 *   Bootloader project).
 * - The Application project always has dedicated access to all hardware
 *   interrupts that are not run-time selected through this virtual register, so
 *   implementing interrupt handlers in your Bootloader will never preclude
 *   future use of the same hardware in Application projects.
 *
 * As the bit positions and their meanings depend on the ISRs implemented in
 * your Bootloader project, the mapping of bitfields to named interrupts is only
 * pseudo constant. Therefore this variable should not be written to directly.
 * Instead, use the EZBL_ForwardIntToApp(), EZBL_ForwardIntToBoot(),
 * EZBL_ForwardAllIntToApp() and EZBL_ForwardAllIntToBoot() macros to manipulate
 * this pseudo register. However, it is possible to save and restore the whole
 * value as an atomic unit.
 *
 * The parameter required by the EZBL_ForwardIntTo[App/Boot]() macros is the
 * XC16 ISR name of the interrupt, but without a leading underscore and without
 * the 'Interrupt' suffix on it. Ex: an _U2RXInterrupt() ISR is referenced in
 * these macros by the name "U2RX", without quotation marks.
 *
 * To forward the U2RX Interrupt to the Application, you may write
 * your "begin forwarding" code as:
 *      <code>
 *      #include "ezbl_integration/ezbl.h"
 *      ...
 *      EZBL_ForwardIntToApp(U2RX);
 *      </code>
 *
 * To terminate forwarding and let the Bootloader handle the U2RX interrupt,
 * you would atomically clear the same bit without affecting the others:
 *      <code>
 *      EZBL_ForwardIntToBoot(U2RX);
 *      </code>
 *
 * Changes to the bits in this virtual register have immediate effect, assuming
 * global interrupts and the corresponding interrupt enable flag in the IECx
 * registers is set.
 */
volatile unsigned long __attribute__((near, keep)) EZBL_ForwardBootloaderISR;


#elif defined(__XC32__) || defined(__PIC32__)
    // PIC32MM products should use the following macros/functions to control
    // interrupt vectoring between Bootloader and Application projects instead
    // of EZBL_ForwardBootloaderISR:
    //      EZBL_ForwardInt(), EZBL_ForwardIntToBoot(), EZBL_ForwardIntToApp(),
    //      EZBL_ForwardAllInt(), EZBL_ForwardAllIntToBoot(), EZBL_ForwardAllIntToApp()

#endif
