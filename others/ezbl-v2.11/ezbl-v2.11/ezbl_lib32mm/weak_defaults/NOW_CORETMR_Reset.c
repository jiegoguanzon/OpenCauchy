/*******************************************************************************
 *
 * Time Counting and Measurement Function entry point for NOW APIs in Easy
 * Bootloader Library (EZBL)
 * 
 * Includes routines for measuring absolute and relative time differences
 *
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

// Use Polling mode to capture Core Timer roll over events to update 
// NOW_internalCount. The PIC32MM Core Timer takes 343.597 seconds to roll over
// at 25 MHz. This is a long time, so instead of using Interrupts, this option
// is implemented for self-polling use where any NOW_16(), NOW_32(), or NOW_64()
// call will automatically update the upper 32-bits as needed.
#define POLLING_MODE



// This vector number is the same on PIC32MM0064GPL036 family and
// PIC32MM0256GPM064 family devices.
#define _CORE_TIMER_VECTOR                       0


extern volatile unsigned int __attribute__((section("sfr"))) IEC0;
extern volatile unsigned int __attribute__((section("sfr"))) IFS0;
extern volatile unsigned int __attribute__((section("sfr"))) IPC0;


extern int __attribute__((weak, alias("NOW_IntEnable")))  NOW_EnableInterrupts(void);     // Generate NOW_EnableInterrupts() alias for old EZBL version compatibility
extern int __attribute__((weak, alias("NOW_IntDisable"))) NOW_DisableInterrupts(void);    // Generate NOW_DisableInterrupts() alias for old EZBL version compatibility


/**
 * NOW_internalCount - Internal 64-bit NOW timer variable. Use the NOW_16(),
 * NOW_32(), NOW_64() APIs to read this instead of referencing this directly.
 * While this variable is 64-bits, it may not include the actual low order bits
 * of the timer, which get read from the hardware directly.
 */
static union
{
    volatile unsigned char      u8[8];
    volatile unsigned short     u16[4];
    volatile unsigned long      u32[2]; // Low order 32-bits stores a snapshot of CP0_COUNT value in polling mode. In Interrupt mode, low order 32-bits is not used.
    volatile unsigned long long u64;
} NOW_internalCount __attribute__((persistent));


/**
 * Initializes a Timer for time keeping with the NOW_* API. Timing is
 * implemented relative to the timer's selected input clock frequency. This
 * function uses this value to set the values stored in NOW_sec,
 * NOW_ms and NOW_us for direct use in calling code to
 * translate run-time adjustable NOW counts to physical seconds and other human
 * meaningful times.
 *
 * This function defaults to enabling the Timer/CCP Interrupt at priority
 * level 4 on 16-bit devices and no interrupt on PIC32MM devices which use the
 * MIPS Core Timer. On 16-bit devices, the interrupt will fire every 65536
 * instructions and need around ~15 or 30 cycles per interrupt event on 16-bit
 * processors. i.e. less than 0.05% CPU use.
 *
 * Most of the NOW APIs should only be called at IPL levels below this ISR
 * priority if accurate and monotonic NOW counts are needed. If you wish to use
 * these APIs in higher priority ISRs, increase the timer ISR priority level
 * to one higher than the highest caller.
 *
 * @param peripheralClock
 *          Number of input timer clock cycles per second of real time. For
 *          example, use 70000000 if your device is operating at 70 MIPS and you
 *          are using the system instruction clock to clock the timer. LPRC's
 *          will need ~31000 for 31kHz, and of course 32.768kHz SOSC crystals
 *          are also normally used with 32768.
 *
 *          However, if you know your crystal or clock input is biased above or
 *          below the optimal crystal spec, you can deliberately provide a value
 *          slightly faster or slower to automatically correct for this
 *          oscillation frequency error in software computations. At room
 *          temperature, crystals will oscillate slower than intended when you
 *          have oversized crystal loading capacitors (ex: more parasitic pin
 *          capacitance than you calculated for), and similarly, run fast when
 *          there is below optimal capacitance.
 */
void __attribute__((weak)) NOW_CORETMR_ResetCalc(unsigned long peripheralClock)
{
    EZBL_BCC0(EZBL_CP0_Cause, _CP0_CAUSE_DC_MASK);   // Ensure Disable Count bit is clear in CP0 Cause register
    EZBL_MTC0(EZBL_CP0_Compare, 0xFFFFFFFF);       // Set Core Timer Compare (period) register

    NOW_Fcy = peripheralClock;
    NOW_sec = peripheralClock>>1;
    NOW_ms = (peripheralClock+250u)/500u;
    NOW_us = (peripheralClock+250000u)/500000u;
    NOW_IntEnable();
}


/**
 * Optimized implementation of NOW_ResetCalc() intended for having division
 * steps performed at compile time when the timer input frequency is a
 * compile-time constant.
 *
 * Do not call this function directly. Instead call the NOW_Reset() macro which
 * tests the input and calls NOW_ResetCalc() or NOW_ResetOpt() as appropriate.
 *
 * @param systemClock   System clock frequency, in Hertz. Ex: 24000000 for 24
 *                      MIPS. This sets the NOW_Fcy global variable.
 * @param second        Number of NOW_64()/NOW_32()/NOW_16() ticks in a physical
 *                      second. This sets the NOW_sec global variable.
 * @param millisecond   Number of NOW_64()/NOW_32()/NOW_16() ticks in a physical
 *                      millisecond. This sets the NOW_ms global
 *                      variable.
 * @param microsecond   Number of NOW_64()/NOW_32()/NOW_16() ticks in a physical
 *                      microsecond. This sets the NOW_us global
 *                      variable.
 */
void __attribute__((weak)) NOW_CORETMR_ResetConst(void)
{
    EZBL_BCC0(EZBL_CP0_Cause, _CP0_CAUSE_DC_MASK);  // Ensure Disable Count bit is clear in CP0 Cause register
    EZBL_MTC0(EZBL_CP0_Compare, 0xFFFFFFFF);        // Set Core Timer Compare (period) register
    NOW_IntEnable();
}


/**
 * Returns the current state of the Interrupt Enable bit (ex: IFSy<TxIE> or
 * IFSy<CCTxIE>) used by the NOW timing module and scheduler.
 *
 * @return 0 if the interrupt is disabled
 *         1 if the interrupt is enabled
 *
 *         If the NOW_Reset() macro or a NOW_TMRx_ResetCalc(),
 *         NOW_TMRx_ResetConst(), NOW_CCPx_ResetCalc(), or NOW_CCPx_ResetConst()
 *         function is never called anywhere in the project (or EZBL bootloader
 *         project), this function will effectively resolve to a series of
 *         no-operations without causing a linking error. I.e. No SFR writes
 *         will take place and no harm will be done, with a mere 0 always being
 *         returned.
 */
int __attribute__((weak)) NOW_RdIntEn(void)
{
#if defined(POLLING_MODE)
    return 0;
#else
    return EZBL_RdIntFlag(_CORE_TIMER_VECTOR);
#endif
}


/**
 * Enables the MIPS Core Timer Interrupt by setting the IECxbits.CTIE bit.
 *
 * If polling mode is used for the NOW API, this function does nothing.
 */
int __attribute__((weak))  NOW_IntEnable(void)
{
#if defined(POLLING_MODE)
    return 0;   // In polling mode, no interrupt is needed
#else
    return EZBL_SetIntEn(_CORE_TIMER_VECTOR);
#endif
}


/**
 * Disables the MIPS Core Timer Interrupt by clearing the IECxbits.CTIE bit.
 *
 * If polling mode is used for the NOW API, this function does nothing.
 *
 * @return Prior IEC<0> Core Timer interrupt enable state (constant 0 for
 *         polling mode)
 */
int __attribute__((weak)) NOW_IntDisable(void)
{
#if defined(POLLING_MODE)
    return 0;
#else
    return EZBL_ClrIntEn(_CORE_TIMER_VECTOR);
#endif
}


/**
 * Atomically returns the least significant 16 bits of the current NOW counter
 * value. This function is safe to be called from all ISRs (and main context),
 * and at any time. This function is reentrant safe.
 *
 * @return 16-bit NOW counter value, where 1 NOW count is equal to 1 tick of the
 *         reference timer. Use the NOW_sec, NOW_ms, NOW_us
 *         variable contents, or the NOW_Diff_to_ms() and NOW_Diff_to_us()
 *         functions in order to make physical sense of how long a NOW count is
 *         at run time.
 *
 *         Because this return value is only 16-bits wide and could increment as
 *         at up to the system device frequency, some clock sources will
 *         overflow 16-bits very easily. Ex: at 70MIPS and the CPU clock used
 *         for the NOW timer, the longest interval you could correctly measure
 *         with this function is 936us. Use the NOW_32() or NOW_64() functions
 *         if longer durations are being measured.
 *
 *         On PIC32MM devices, the MIPS Core Timer is used, so a 16-bit value
 *         rolls over in 5.24288 milliseconds at 25 MIPS.
 */
unsigned short __attribute__((weak)) NOW_16(void)
{
#if defined(POLLING_MODE)
    return (unsigned short)NOW_64();                    // For polling mode
#else
    return (unsigned short)EZBL_MFC0(EZBL_CP0_Count);   // For interrupt mode
#endif
}


/**
 * Atomically returns the least significant 32 bits of the current NOW counter
 * value. This function is safe to be called from the main contexts and ISRs
 * of lower priority than the TMR1 ISR priority (IPL=1,2 or 3 by default).
 * Calling from a higher or equal ISR priority context will succeed and cause
 * no harm, but the value returned may be off by up to 0x10000 counts if the
 * lower 16 bit count rolls over.
 *
 * Execution time is 12 or 20 cycles best (and normal) case, depending on device
 * family, which includes the call and return branches. If you are very unlucky
 * and the lower 16-bits rolls over during the read (less than 0.013% chance if
 * no interrupt occurs), the final return value will still be atomic, but
 * execution time will approximately double.
 *
 * @return 32-bit NOW counter value, where 1 NOW count is equal to 1 instruction
 *         cycle. Use the NOW_sec, NOW_ms, NOW_us
 *         variable contents, or the NOW_Diff_to_ms() and NOW_Diff_to_us()
 *         functions in order to make physical sense of how long a NOW count is.
 *
 *         Because this return value is only 32-bits wide and increments at the
 *         system device frequency, it is subject to overflow when working with
 *         longer intervals . Ex: at 70MIPS, the longest interval you could
 *         correctly measure with this function is 61.356 seconds. Use the
 *         NOW_64() function if longer durations are being measured.
 */
#if defined(POLLING_MODE)
extern unsigned long __attribute__((weak, alias("NOW_64"))) NOW_32(void);
#else
unsigned long __attribute__((weak)) NOW_32(void)
{
    return (unsigned long)EZBL_MFC0(EZBL_CP0_Count); // For interrupt mode
}
#endif


/**
 * Atomically returns the entire 64 bits of the current NOW counter value
 * value. This function is safe to be called from the main context and ISRs
 * of lower priority than the TMRx ISR priority (IPL=1,2 or 3 by default).
 * Calling from a higher or equal ISR priority context will succeed and cause
 * no harm, but the value returned may be off by up to 0x100000000 counts if the
 * lower 32 bit count rolls over.
 *
 * Execution time is 20 or 32 cycles best (and normal) case, depending on device
 * family, which includes the call and return branches. If you are very unlucky
 * and the lower 16-bits rolls over during the read (less than 0.013% chance if 
 * no interrupt occurs), the final return value will still be atomic, but
 * execution time will approximately double.
 *
 * @return 64-bit NOW counter value, where 1 NOW count is equal to 1 instruction
 *         cycle. Use the NOW_sec, NOW_ms, NOW_us
 *         variable contents, or the NOW_Diff_to_ms() and NOW_Diff_to_us()
 *         functions in order to make physical sense of how long a NOW count is.
 *
 *         Because this return value is a whopping 64-bits, the returned value
 *         will be virtually immune to overflow. Ex: at 1GHz (I want that PIC!),
 *         overflow won't occur for over 584 years. As a result, this NOW count
 *         can also be used for absolute time measurement. Do do so, just
 *         initialize NOW_internalCount to the present time (relative to
 *         whatever date/time you consider Time T=0) before calling NOW_init().
 *         If you do not initialize NOW_internalCount, the default will be
 *         0x0000000000000000, so NOW_64() will still be usable as an absolute
 *         timestamp, but values returned will be relative to the last device
 *         reset.
 */
unsigned long long __attribute__((weak)) NOW_64(void)
{
#if defined(POLLING_MODE)
    unsigned long cacheH;
    unsigned long i;
    
    i = EZBL_MFC0(EZBL_CP0_Count);                                      // Get low 32 bits from Coprocessor 0 Count register (MIPS Core Timer)
    cacheH = NOW_internalCount.u32[1] + (i < NOW_internalCount.u32[0]); // Get high 32-bits and adjust by adding 1 if low 32-bits has rolled over since last time it was captured
    NOW_internalCount.u32[0] = i;                                       // Update capture value for lower 32-bits rollover tracking
    NOW_internalCount.u32[1] = cacheH;                                  // Update upper 32-bits in case if a rollover happened

    return i | ((unsigned long long)cacheH)<<32;                        // Return all 64-bits. This is more efficient than returning NOW_internalCount.u64 because NOW_internalCount is volatile and i and cacheH are already in CPU registers
#else
    unsigned long i, j;
    unsigned long iH, jH;

    do
    {
        i = EZBL_MFC0(EZBL_CP0_Count);          // Get low 32 bits from Coprocessor 0 Count register (MIPS Core Timer)
        iH = NOW_internalCount.u32[1];          // Get high 32-bits
        j = EZBL_MFC0(EZBL_CP0_Count);          // Reget low 32-bits to check for asynchronous interrupt roll over event
        jH = NOW_internalCount.u32[1];
    } while((iH != jH) || (i > j));


    return j | ((unsigned long long)jH)<<32;    // Return all 64-bits. This is more efficient than returning NOW_internalCount.u64 because NOW_internalCount is volatile and i and iH are already in CPU registers.
#endif
}


/**
 * Waits the specified number of NOW counts in a blocking loop doing nothing.
 *
 * @param waitTime Number of NOW counts to wait. Use any fraction or multiple
 *                 of NOW_sec, NOW_ms, NOW_us variables to
 *                 specify absolute human time frames.
 *
 * @return NOW_32() return value (the present time, now stale by ~13
 *         instruction cycles).
 */
unsigned long __attribute__((weak)) NOW_Wait(unsigned long waitTime)
{
    unsigned long startTime = NOW_32();
    unsigned long now;

    do
    {
        now = NOW_32();
    } while(now - startTime < waitTime);

    return now;
}


// PIC32MM Core Timer takes at least 357.91 seconds to roll over at 24 MHz. This
// is a long time, so instead of using Interrupts, this file can implement
// self-polling where any NOW_16(), NOW_32(), or NOW_64() call will
// automatically update the upper 32-bits as needed.
#if !defined(POLLING_MODE)
void __attribute__((interrupt(IPL4SOFT), vector(_CORE_TIMER_VECTOR), weak, keep)) CoreTimerInterrupt(void)
{
    NOW_internalCount.u32[1]++;
    EZBL_ClrIntFlag(_CORE_TIMER_VECTOR);
}
#endif
