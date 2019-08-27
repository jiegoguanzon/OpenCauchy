;/*******************************************************************************
;  Easy Bootloader for PIC24/dsPIC33 and PIC32MM source file (ezbl_lib32)
;
;  Summary:
;    Assembly language optimized routines in EZBL
;
;  Description:
;    Assembly language optimized routines in EZBL
;
;*******************************************************************************/
;
;/*******************************************************************************
;  Copyright (C) 2017 Microchip Technology Inc.
;
;  MICROCHIP SOFTWARE NOTICE AND DISCLAIMER:  You may use this software, and any
;  derivatives created by any person or entity by or on your behalf, exclusively
;  with Microchip's products.  Microchip and its licensors retain all ownership
;  and intellectual property rights in the accompanying software and in all
;  derivatives here to.
;
;  This software and any accompanying information is for suggestion only.  It
;  does not modify Microchip's standard warranty for its products.  You agree
;  that you are solely responsible for testing the software and determining its
;  suitability.  Microchip has no obligation to modify, test, certify, or
;  support the software.
;
;  THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS".  NO WARRANTIES, WHETHER
;  EXPRESS, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED
;  WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR
;  PURPOSE APPLY TO THIS SOFTWARE, ITS INTERACTION WITH MICROCHIP'S PRODUCTS,
;  COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION.
;
;  IN NO EVENT, WILL MICROCHIP BE LIABLE, WHETHER IN CONTRACT, WARRANTY, TORT
;  (INCLUDING NEGLIGENCE OR BREACH OF STATUTORY DUTY), STRICT LIABILITY,
;  INDEMNITY, CONTRIBUTION, OR OTHERWISE, FOR ANY INDIRECT, SPECIAL, PUNITIVE,
;  EXEMPLARY, INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE, FOR COST OR EXPENSE OF
;  ANY KIND WHATSOEVER RELATED TO THE SOFTWARE, HOWSOEVER CAUSED, EVEN IF
;  MICROCHIP HAS BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE.
;  TO THE FULLEST EXTENT ALLOWABLE BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL
;  CLAIMS IN ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF
;  FEES, IF ANY, THAT YOU HAVE PAID DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.
;
;  MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE
;  TERMS.
;*******************************************************************************/


/**
 * void EZBL_RestoreInterrupts(unsigned int enabledState);
 *
 * Sets the CP0 Status<IE> register to the specified interrupt enable state.
 * This function is named for compatibility with the EZBL_RestoreInterrupts()
 * function used in the 16-bit version of ezbl_lib.a, but is capable of directly
 * enabling/disabling interrupts without internal decoding.
 *
 * @param enabledState Flag indicating if interrupts shall be enabled (1) or
 *                     disabled (0) upon function return.
 *
 * @return Status<IE> set or cleared to match enabledState
 */
    .pushsection    .text.EZBL_RestoreInterrupts, code
    .global         EZBL_RestoreInterrupts
    .type           EZBL_RestoreInterrupts, @function
EZBL_RestoreInterrupts:
    .set            noreorder
    beqz            $a0, 1f
    mfc0            $v0, $12, 0     /* $12 == _CP0_STATUS, 0 == _CP0_STATUS_SELECT */
    ei
1:  jr.hb           $ra
    ext32           $v0, $v0, 0, 1  /* Return only bit 0 (Status<IE>) Interrupt Enable bit. This must be a 32-bit instruction for the jr.hb delay slot. */
    .set            reorder
    .size           EZBL_RestoreInterrupts, . - EZBL_RestoreInterrupts
    .popsection
    