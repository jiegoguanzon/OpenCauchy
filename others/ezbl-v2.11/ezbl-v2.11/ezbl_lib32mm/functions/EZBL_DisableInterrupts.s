;/*******************************************************************************
;  Easy Bootloader for PIC24/dsPIC33 and PIC32MM source file (ezbl_lib32)
;
;  Summary:
;    Assembly language optimized routines for EZBL
;
;  Description:
;    Assembly language optimized routines for EZBL
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
 * unsigned int EZBL_DisableInterrupts(void);
 *
 * Disables all interrupts, including IPL7 ones by clearing the Status<IE> 
 * Coprocessor 0 register bit.
 *
 * The return value should be saved and subsequently passed to 
 * EZBL_RestoreInterrupts() to restore the original interrupt enable state.
 *
 * @return Prior Interrupt enable state where 1 indicates interrupts were
 *         globally enabled and 0 indicates they were already disabled.
 */
# unsigned int EZBL_DisableInterrupts(void);
    .pushsection    .text.EZBL_DisableInterrupts, code
    .global         EZBL_DisableInterrupts
    .type           EZBL_DisableInterrupts, @function
EZBL_DisableInterrupts:
    di      $v0
    jr.hb   $ra
    ext32   $v0, $v0, 0, 1  # Return only bit 0 (Status<IE>) Interrupt Enable bit. This must be a 32-bit instruction for the jr.hb delay slot.
    
    .size           EZBL_DisableInterrupts, . - EZBL_DisableInterrupts
    .popsection
