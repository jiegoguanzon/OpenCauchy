/**
 * NOW time counting/measurement/task scheduling functions of EZBL
 * Implements the NOW_16() ezbl_lib.a API
 *
 * @author C12128
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


/**
 * Atomically returns the low 16-bits of the NOW counter without disabling
 * interrupts, requiring execution at <= the Timer's IPL, or being unsafe for
 * rentrant execution at the same or different IPLs.
 *
 * Execution time on PIC24/dsPIC is 9 or 14 cycles (device specific), including
 * the call and return. Typically an inline implementation in ezbl.h will be
 * used instead of this library function, which needs fewer cycles (3 or 4).
 *
 * @return 16-bit NOW counter value, where 1 NOW count is normally equal
 *         to 1 instruction cycle. Use the NOW_sec, NOW_ms, NOW_us variable
 *         contents, or the NOW_Diff_to_ms() and NOW_Diff_to_us() functions in
 *         order to make physical sense of how long a NOW count or time span is.
 *
 *         NOTE: 16-bit NOW counts overflow in the NOW_32() function overflows
 *         in only 546us at 120 MIPS (4.096ms at 16 MIPS). Use NOW_32() or
 *         NOW_64() for larger time interval durations.
 */
    .pushsection .text.NOW_16, code
    .global _NOW_16
    .type   _NOW_16, @function
    .weak   _NOW_internalCount
    .equ    internalCountPing, 0
    .equ    internalCountPong, 8
    .equ    countPtr, 16
    .equ    timerSFRPtr, 18
    .equ    flags, 20
_NOW_16:    ; unsigned short NOW_16(void);
    mov     _NOW_timerSFRPtr, w0            ;// Get &TMRx
    mov     _NOW_timeOffset, w1             ;// Get NOW_timeOffset
    subr    w1, [w0], w0                    ;// w0 = TMRx SFR value - NOW_timeOffset
    return
    .size   _NOW_16, . - _NOW_16



