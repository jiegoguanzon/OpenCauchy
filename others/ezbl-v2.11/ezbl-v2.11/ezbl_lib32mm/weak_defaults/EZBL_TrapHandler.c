/*******************************************************************************
  Easy Bootloader Library for PIC32MM Library API Source File

  Summary:
    Implements the EZBL_TrapHandler() API on PIC32MM devices. This replaces
    _general_exception_handler() with a great deal of system state information
    printed in human readable form to stdout or EZBL_STDOUT.

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
#if defined(EZBL_LIB)
#include "../ezbl.h"
#else
#include "ezbl_integration/ezbl.h"
#endif

extern volatile unsigned int __attribute__((section("sfrs"))) RCON;

#define __SFRS_BASE                 0xBF800000
#define __KSEG0_DATA_MEM_BASE       0x80000000
#define __KSEG1_BOOT_MEM_BASE       0xBFC00000
#define __KSEG1_BOOT_MEM_LENGTH     0x1800
#define __KSEG0_PROGRAM_MEM_BASE    0x9D000000
#define __KSEG0_PROGRAM_MEM_LENGTH  0x00010000  // Only 64KB... some parts have more, but printing so much data could take a while and such data is of questionable value


// Reference the EZBL_general_exception_context symbol, which will take
// over/implement _general_exception_context() and call this
// EZBL_TrapHandler() function after collecting CPU and CP0 register states.
// EZBL_general_exception() will be called upon return, if present, then
// _general_exception_handler(), if present.
EZBL_KeepSYM(EZBL_general_exception_context);


/**
 * EZBL general exception/trap handler for PIC32MM devices which will collect
 * all CPU + CP0 registers at the point of the exception and dump them, along
 * with various other RAM, Flash and SFR state information in human readable
 * text form to stdout/EZBL_STDOUT.
 *
 * This function forces inclusion of the EZBL_general_exception_context()
 * function, not the default _general_exception_context function provided by the
 * XC32 toolchain.
 * 
 * To use this trap handler, place this statement in a C source file at
 * file-scope:
 *      EZBL_KeepSYM(EZBL_TrapHandler);
 * EZBL_general_exception(), if present, will be called after executing this
 * trap handler. After EZBL_general_exception() returns, assuming it exists,
 * _general_exception_handler(), will be called, if present. If
 * EZBL_general_exception() does not exist, _general_exception_handler() will be
 * called, if present after returning from this EZBL_TrapHandler() function.
 *
 * @param cause Coprocessor 0 Cause register, in an easily evaluated bitfield
 *              structure. Access cause.val to retrieve the undecoded value
 *              normally available in a _general_exception_handler().
 *
 * @param status Coprocessor 0 Status register, in an easily evaluated bitfield
 *              structure. Access status.val to retrieve the undecoded value
 *              normally available in a _general_exception_handler().
 *
 * @param *cpuRegs Pointer to an EZBL_CPU_REGS structure allocated temporarily
 *                 on the stack that contains all of the MIPS CPU register
 *                 contents when the exception occurred. When this function
 *                 returns, the contents of this structure is deallocated and
 *                 non-saved/temporary register values (t0-t9, v0-v1, a0-a3, a0,
 *                 k0-k1)will be written back into the CPU registers.
 *
 * @param *cp0Regs Pointer to an EZBL_CP0_REGS structure allocated temporarily
 *                 on the stack that contains all of the Coprocessor 0 register
 *                 contents when the exception occurred. When this function
 *                 returns, the contents of this structure is deallocated and
 *                 any values written into it will also be discarded (i.e. they
 *                 are not written back to CP0 at exception return).
 */
void EZBL_TrapHandler(EZBL_MIPS_CAUSE cause, EZBL_MIPS_STATUS status, EZBL_CPU_REGS *cpuRegs, EZBL_CP0_REGS *cp0Regs)
{
    EZBL_printf("\n\nException: 0x%02X, EPC = 0x%08X\n\n", cause.ExceptionCode, cp0Regs->EPC & 0xFFFFFFFEu);

    EZBL_DumpDecodedCause(cause.val);
    EZBL_DumpDecodedStatus(status.val);

    EZBL_printf("\n\nCPU Registers:"
                "\n  v0 = 0x%08X"
                "\n  v1 = 0x%08X"
                "\n  a0 = 0x%08X"
                "\n  a1 = 0x%08X"
                "\n  a2 = 0x%08X"
                "\n  a3 = 0x%08X"
                "\n  t0 = 0x%08X"
                "\n  t1 = 0x%08X"
                "\n  t2 = 0x%08X"
                "\n  t3 = 0x%08X"
                "\n  t4 = 0x%08X"
                "\n  t5 = 0x%08X"
                "\n  t6 = 0x%08X"
                "\n  t7 = 0x%08X"
                "\n  t8 = 0x%08X"
                "\n  t9 = 0x%08X"
                "\n  s0 = 0x%08X"
                "\n  s1 = 0x%08X"
                "\n  s2 = 0x%08X"
                "\n  s3 = 0x%08X"
                "\n  s4 = 0x%08X"
                "\n  s5 = 0x%08X"
                "\n  s6 = 0x%08X"
                "\n  s7 = 0x%08X"
                "\n  k0 = 0x%08X"
                "\n  k1 = 0x%08X"
                "\n  gp = 0x%08X"
                "\n  sp = 0x%08X"
                "\n  fp = 0x%08X"
                "\n  ra = 0x%08X"
                "\n  hilo = 0x%016llX"
                "\n  at = 0x%08X",
                cpuRegs->v[0], cpuRegs->v[1],
                cpuRegs->a[0], cpuRegs->a[1], cpuRegs->a[2], cpuRegs->a[3],
                cpuRegs->t[0], cpuRegs->t[1], cpuRegs->t[2], cpuRegs->t[3], cpuRegs->t[4], cpuRegs->t[5], cpuRegs->t[6], cpuRegs->t[7], cpuRegs->t[8], cpuRegs->t[9],
                cpuRegs->s[0], cpuRegs->s[1], cpuRegs->s[2], cpuRegs->s[3], cpuRegs->s[4], cpuRegs->s[5], cpuRegs->s[6], cpuRegs->s[7],
                cpuRegs->k[0], cpuRegs->k[1],
                cpuRegs->gp,
                cpuRegs->sp,
                cpuRegs->fp,
                cpuRegs->ra,
                cpuRegs->hilo,
                cpuRegs->at);

    EZBL_printf("\n\nCP0 Registers:"
                "\n  BadVAddr  = 0x%08X"
                "\n  BadInstr  = 0x%08X"
                "\n  BadInstrP = 0x%08X"
                "\n  Count     = 0x%08X"
                "\n  Compare   = 0x%08X"
                "\n  Status    = 0x%08X"
                "\n  IntCtl    = 0x%08X"
                "\n  SRSCtl    = 0x%08X"
                "\n  SRSMap1   = 0x%08X"
                "\n  View_IPL  = 0x%08X"
                "\n  SRSMap2   = 0x%08X"
                "\n  Cause     = 0x%08X"
                "\n  View_RIPL = 0x%08X"
                "\n  NestedExc = 0x%08X"
                "\n  EPC       = 0x%08X"
                "\n  NestedEPc = 0x%08X",
                        ((unsigned int*)cp0Regs)[0], ((unsigned int*)cp0Regs)[1], ((unsigned int*)cp0Regs)[2], ((unsigned int*)cp0Regs)[3],
                        ((unsigned int*)cp0Regs)[4], ((unsigned int*)cp0Regs)[5], ((unsigned int*)cp0Regs)[6], ((unsigned int*)cp0Regs)[7],
                        ((unsigned int*)cp0Regs)[8], ((unsigned int*)cp0Regs)[9], ((unsigned int*)cp0Regs)[10], ((unsigned int*)cp0Regs)[11],
                        ((unsigned int*)cp0Regs)[12], ((unsigned int*)cp0Regs)[13], ((unsigned int*)cp0Regs)[14], ((unsigned int*)cp0Regs)[15]);
    EZBL_printf("\n  PRId      = 0x%08X"
                "\n  EBase     = 0x%08X"
                "\n  CDMMBase  = 0x%08X"
                "\n  Config    = 0x%08X"
                "\n  Config1   = 0x%08X"
                "\n  Config2   = 0x%08X"
                "\n  Config3   = 0x%08X"
                "\n  Config4   = 0x%08X"
                "\n  Config5   = 0x%08X"
                "\n  Config7   = 0x%08X"
                "\n  LLAddr    = 0x%08X"
                "\n  WatchLo   = 0x%08X"
                "\n  WatchHi   = 0x%08X"
                "\n  Debug     = 0x%08X"
                "\n  Debug2    = 0x%08X"
                "\n  DEPC      = 0x%08X",
                        ((unsigned int*)cp0Regs)[16], ((unsigned int*)cp0Regs)[17], ((unsigned int*)cp0Regs)[18], ((unsigned int*)cp0Regs)[19],
                        ((unsigned int*)cp0Regs)[20], ((unsigned int*)cp0Regs)[21], ((unsigned int*)cp0Regs)[22], ((unsigned int*)cp0Regs)[23],
                        ((unsigned int*)cp0Regs)[24], ((unsigned int*)cp0Regs)[25], ((unsigned int*)cp0Regs)[26], ((unsigned int*)cp0Regs)[27],
                        ((unsigned int*)cp0Regs)[28], ((unsigned int*)cp0Regs)[29], ((unsigned int*)cp0Regs)[30], ((unsigned int*)cp0Regs)[31]);
    EZBL_printf("\n  PerfCtl0  = 0x%08X"
                "\n  PerfCnt0  = 0x%08X"
                "\n  PerfCtl1  = 0x%08X"
                "\n  PerfCnt1  = 0x%08X"
                "\n  ErrorEPC  = 0x%08X"
                "\n  DeSAVE    = 0x%08X"
                "\n  KScratch1 = 0x%08X"
                "\n  KScratch2 = 0x%08X", 
                        ((unsigned int*)cp0Regs)[32], ((unsigned int*)cp0Regs)[33], ((unsigned int*)cp0Regs)[34], ((unsigned int*)cp0Regs)[35],
                        ((unsigned int*)cp0Regs)[36], ((unsigned int*)cp0Regs)[37], ((unsigned int*)cp0Regs)[38], ((unsigned int*)cp0Regs)[39]);

    EZBL_printf("\n\nRCON = 0x%08X", RCON);

    EZBL_printf("\n\nSFRs:");
    EZBL_DumpRAM((const void*)__SFRS_BASE, 0x4000u/4u);
    EZBL_DumpRAM((const void*)(__SFRS_BASE+0x8000u), 0x72D0u/4u);

    EZBL_printf("\n\nRAM:");
    EZBL_DumpRAM((const void*)__KSEG0_DATA_MEM_BASE, (EZBL_SYM(_stack) | 0x7FFu) + 0x1);

    EZBL_printf("\n\nBoot Flash:");
    EZBL_DumpFlash(__KSEG1_BOOT_MEM_BASE, __KSEG1_BOOT_MEM_BASE + 0x1800u);

    EZBL_printf("\n\nExecution Flash (first 4KB):");
    EZBL_DumpFlash(__KSEG0_PROGRAM_MEM_BASE, __KSEG0_PROGRAM_MEM_BASE + 4096u);
    
    EZBL_printf("\n\n");
    EZBL_FIFOFlush(EZBL_STDOUT, NOW_sec);
}