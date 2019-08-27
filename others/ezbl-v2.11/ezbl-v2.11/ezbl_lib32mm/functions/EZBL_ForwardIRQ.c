/*******************************************************************************
  Easy Bootloader Library for PIC32MM API Source File

  Summary:
    Implements the EZBL_ForwardIRQ() and EZBL_ForwardAllIRQ() APIs called by the
    EZBL_ForwardIRQToApp(), EZBL_ForwardIRQToBoot(), EZBL_ForwardAllIRQToApp(),
    and EZBL_ForwardAllIRQToBoot() macros.

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

#include <cp0defs.h>

#if defined(EZBL_LIB)
#include "../ezbl.h"
#else
#include "ezbl_integration/ezbl.h"
#endif



/**
 * Array of Global Pointer accessible RAM pointers to flash (or RAM) Interrupt
 * Service Routines. As common RAM variables shared between Bootloader and
 * Application projects, these pointers cause a jump to an ISR in the Bootloader
 * or an ISR in the Application, depending on their contents at run-time.
 * Use the EZBL_ForwardIRQTo[App/Boot]() or EZBL_ForwardAllIRQTo[App/Boot]()
 * macros to modify these pointers.
 */
extern volatile unsigned int __attribute__((section(".EZBL_ISRPointers"), persistent)) EZBL_ISRPointers[];


/**
 * Dynamically redirects project ownership of the specified hardware interrupt
 * vector between Bootloader and Application projects.
 *
 * @param toAppProject Sets the destination project which should receive the
 *                     specified interrupt. 0 means the Bootloader project will
 *                     receive the interrupt, non-zero means the Application
 *                     project will receive it instead.
 *
 *                     For convenience, the EZBL_ForwardIRQToApp() and
 *                     EZBL_ForwardIRQToBoot() macros may be used to ignore
 *                     instead.

 * @param vectorNumber Interrupt vector number (IRQ #) that you wish to change
 *                     the hardware vectoring destination for. Use the macros
 *                     defined in the <xc.h> to specify this parameter, ex:
 *                     _UART2_RX_VECTOR, _TIMER_1_VECTOR, etc.
 *
 *                     If the Bootloader project does not implement an ISR for
 *                     the specified vector number, this function will do
 *                     nothing (hardware vectoring will branch to whatever
 *                     project is executing/defined by EBase).
 *
 * @return Last ISR function address which was programmed for the vector number.
 *         If no Bootloader ISR was defined for the vector number, 0 is
 *         returned.
 *
 *         If an interrupt is forwarded to the Application and no Application
 *         exists in flash, this function has no current or future effect and
 *         will return 0.
 */
unsigned int EZBL_ForwardIRQ(int toAppProject, int vectorNumber)
{
    // Create a dummy 0 item section in case if there are no sections named .EZBL_ISRPointers in the .ld linker script. This takes no space.
    __asm__("\n    .pushsection    .EZBL_ISRPointers, persist, keep"
            "\n    .popsection");

    volatile unsigned int *isrPtrs = EZBL_ISRPointers;
    unsigned int endAddr = __builtin_section_end(".EZBL_ISRPointers");
    unsigned int op = EZBL_SYM(_vector_spacing)<<1;
    int ret = 0;
    const unsigned int *originalCompilerVectors = (const unsigned int*)EZBL_SYM32(EZBL_BOOT_ebase_address + 0x18);

    if(toAppProject)
        originalCompilerVectors = (const unsigned int*)EZBL_SYM32(EZBL_APP_ebase_address + 0x18);

    while(((unsigned int)*originalCompilerVectors++) != vectorNumber)
    {
        originalCompilerVectors += op;
        if((unsigned int)++isrPtrs >= endAddr)
            return ret;
    }
    op = *originalCompilerVectors++;                                            // Read a J (jump) microMIPS32 instruction at the EBASE + vector offset address in flash
    ret = *isrPtrs;
    *isrPtrs = ((((op>>16) | (op<<16)) & 0x03FFFFFFu)<<1) | 0x9D000001u;        // Pull the lower 26 bits jump target information out of the J opcode and add the missing high bits. 0x1 is needed to stay in microMIPS ISA mode.
    return ret;
}

/**
 * Forwards all hardware interrupts to a single project (either Bootloader or
 * Application). This is a wrapper for EZBL_ForwardIRQ() that self-enumerates
 * all of the valid vector numbers which can be dynamically forwarded.
 */
void EZBL_ForwardAllIRQ(int toAppProject)
{
    // Create a dummy 0 item section in case if there are no sections named .EZBL_ISRPointers in the .ld linker script. This takes no space.
    __asm__("\n    .pushsection    .EZBL_ISRPointers, persist, keep"
            "\n    .popsection");

    const unsigned int *vecReadPtr = (const unsigned int*)EZBL_SYM32(EZBL_BOOT_ebase_address + 0x18);
    unsigned int i = __builtin_section_size(".EZBL_ISRPointers")/4u;

    while(i--)
    {
        EZBL_ForwardIRQ(toAppProject, *vecReadPtr++);
        vecReadPtr += EZBL_SYM(_vector_spacing)<<1;
    }
}
