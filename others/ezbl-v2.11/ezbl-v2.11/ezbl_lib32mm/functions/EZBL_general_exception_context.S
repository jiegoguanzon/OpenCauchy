/*******************************************************************************
  Easy Bootloader Library for PIC32MM only

  Summary:
    Implements _general_exception_context symbol and replacement
    EZBL_general_exception_context() function which saves all MIPS CPU and CP0
    registers and then redispatches the exception to an ezbl_lib defined 
    EZBL_TrapHandler(), a user defined EZBL_general_exception() function, and/or
    the user defined _general_exception_handler() function.

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

    .equ    zero, $0
    .equ    at, $1
    .equ    v0, $2
    .equ    v1, $3
    .equ    a0, $4
    .equ    a1, $5
    .equ    a2, $6
    .equ    a3, $7
    .equ    t0, $8
    .equ    t1, $9
    .equ    t2, $10
    .equ    t3, $11
    .equ    t4, $12
    .equ    t5, $13
    .equ    t6, $14
    .equ    t7, $15
    .equ    s0, $16
    .equ    s1, $17
    .equ    s2, $18
    .equ    s3, $19
    .equ    s4, $20
    .equ    s5, $21
    .equ    s6, $22
    .equ    s7, $23
    .equ    t8, $24
    .equ    t9, $25
    .equ    k0, $26
    .equ    k1, $27
    .equ    gp, $28
    .equ    sp, $29
    .equ    fp, $30
    .equ    ra, $31


//typedef struct
//{
//    unsigned int v[2];        // Offset 0x00: Return Values
//    unsigned int a[4];        // Offset 0x08: Arguments
//    unsigned int t[10];       // Offset 0x18: Temporaries
//    unsigned int s[8];        // Offset 0x40: Saved
//    unsigned int k[2];        // Offset 0x60: Kernel temporaries
//    unsigned int gp;          // Offset 0x68: Global Pointer to RAM small data
//    unsigned int sp;          // Offset 0x6C: Stack Pointer
//    unsigned int fp;          // Offset 0x70: Frame Pointer
//    unsigned int ra;          // Offset 0x74: Return Address
//    unsigned long long hilo;  // Offset 0x78: HI/LO multiply/divide 64-bit register
//    unsigned int at;          // Offset 0x80: Assembler temporary
//} EZBL_CPU_REGS;

    .equ    CPU_REGS_SIZE, 0x84 // 132 bytes

//typedef struct
//{
//    unsigned int BadVAddr;          // Reports the address for the most recent address-related exception
//    unsigned int BadInstr;          // Reports the instruction that caused the most recent exception
//    unsigned int BadInstrP;         // Reports the branch instruction if a delay slot caused the most recent exception
//    unsigned int Count;             // Core Timer (processor cycle count/2)
//    unsigned int Compare;           // Core Timer period/compare interrupt value
//    EZBL_MIPS_STATUS Status;        // Processor Status and control
//    unsigned int IntCtl;            // Interrupt system status and control
//    unsigned int SRSCtl;            // Shadow Register Sets status and control
//
//    unsigned int SRSMap1;           // Shadow set IPL mapping
//    unsigned int View_IPL;          // Contiguous view of IM and IPL fields
//    unsigned int SRSMap2;           // Shadow set IPL mapping
//    EZBL_MIPS_CAUSE Cause;          // Cause of last exception
//    unsigned int View_RIPL;
//    unsigned int NestedExc;
//    unsigned int EPC;               // Program counter at last exception
//    unsigned int NestedEPc;         // Program counter at last exception
//
//    unsigned int PRId;              // Processor identification and revision
//    unsigned int EBase;             // Exception base address
//    unsigned int CDMMBase;          // Common Device Memory Map Base register
//    unsigned int Config;            // Configuration register 0
//    unsigned int Config1;           // Configuration register 1
//    unsigned int Config2;           // Configuration register 2
//    unsigned int Config3;           // Configuration register 3
//    unsigned int Config4;           // Configuration register 4
//
//    unsigned int Config5;           // Configuration register 5
//    unsigned int Config7;           // Configuration register 7
//    unsigned int LLAddr;            // Load Linked Address
//    unsigned int WatchLo;           // Watchpoint Address low order bits
//    unsigned int WatchHi;           // Watchpoint Address high order bits
//    unsigned int Debug;             // EJTAG Debug register
//    unsigned int Debug2;            // EJTAG Debug register 2
//    unsigned int DEPC;              // Program counter at last debug exception
//
//    unsigned int PerfCtl0;          // Performance Counter 0 Control
//    unsigned int PerfCnt0;          // Performance Counter 0
//    unsigned int PerfCtl1;          // Performance Counter 1 Control
//    unsigned int PerfCnt1;          // Performance Counter 1
//    unsigned int ErrorEPC;          // Program counter at last error
//    unsigned int DeSAVE;            // Debug handler scratchpad register
//    unsigned int KScratch1;         // Scratch Register 1 for Kernel Mode
//    unsigned int KScratch2;         // Scratch Register 2 for Kernel Mode
//} EZBL_CP0_REGS;



    .equ   CP0_REGS_SIZE, 0xA0      // 160 bytes (ommits the commented out fields that are useless or N/A on PIC32MM products)




/**
 * EZBL wrapper function that reimplements the _general_exception_context()
 * function, records all of the CPU and CP0 registers on the stack (as they were
 * when the exception took place), and then calls the EZBL_TrapHandler() 
 * ezbl_lib function (if present), user defined EZBL_general_exception() handler
 * function (if present), and  _general_exception_handler() function (if
 * present). This context save/restore function receives all of the
 * pre-exception CPU + CP0 register states for debug analysis.
 *
 * To use this context saving replacement, either use:
     EZBL_KeepSYM(EZBL_TrapHandler)
 * or, directly save this function and implement an exception handler using:
 *   EZBL_KeepSYM(EZBL_general_exception_context);
 *   void EZBL_general_exception(unsigned int cause, unsigned int status, const EZBL_CPU_REGS *cpuRegsDest, const EZBL_CP0_REGS *cp0RegsDest)
 *   {
 *      ...
 *   }
 */

    .pushsection    .gen_handler, code, keep
    .global         EZBL_dispatch_general_exception_context
    .type           EZBL_dispatch_general_exception_context, @function
    .set            noat
    .set            micromips
EZBL_dispatch_general_exception_context:
    la  k0, EZBL_general_exception_context
    jrc k0
    .size   EZBL_dispatch_general_exception_context, . - EZBL_dispatch_general_exception_context
    .popsection

// void EZBL_general_exception_context(void);
    .pushsection    .text.EZBL_general_exception_context, code
    .global         EZBL_general_exception_context
    .type           EZBL_general_exception_context, @function
    .weak           _general_exception_context
    .type           _general_exception_context, @function
    .weak           EZBL_TrapHandler
    .weak           EZBL_general_exception
    .weak           _general_exception_handler
    .set            noreorder
    .set            micromips
    .set            noat
    .align          0x2
    .equ            CPU_REGS_v, 0x00
    .equ            CPU_REGS_a, 0x08
    .equ            CPU_REGS_t, 0x18
    .equ            CPU_REGS_s, 0x40
    .equ            CPU_REGS_k, 0x60
    .equ            CPU_REGS_gp, 0x68
    .equ            CPU_REGS_sp, 0x6C
    .equ            CPU_REGS_fp, 0x70
    .equ            CPU_REGS_ra, 0x74
    .equ            CPU_REGS_hilo, 0x78
    .equ            CPU_REGS_at, 0x80
    .equ            STACK_OFFSET, (CPU_REGS_SIZE + CP0_REGS_SIZE + 0x4)    # 0x128 or 296 bytes total; + 0x4 maintains required 0x8 stack alignment
EZBL_general_exception_context:
#_general_exception_context:    # Uncommenting this line causes xc32-ld to preferentially find and choose this longer handler instead of the crt's default one, even if EZBL_general_exception_context isn't undefined by anyone.

    addiu	sp,sp,-STACK_OFFSET
    swp     v0, CPU_REGS_v+(0*0x4)(sp)
    swp     a0, CPU_REGS_a+(0*0x4)(sp)
    swp     a2, CPU_REGS_a+(2*0x4)(sp)
    swp     t0, CPU_REGS_t+(0*0x4)(sp)
    swp     t2, CPU_REGS_t+(2*0x4)(sp)
    swp     t4, CPU_REGS_t+(4*0x4)(sp)
    swp     t6, CPU_REGS_t+(6*0x4)(sp)
    swp     t8, CPU_REGS_t+(8*0x4)(sp)
    swm     s0, s1, s2, s3, s4, s5, s6, s7, CPU_REGS_s(sp)
    swp     k0, CPU_REGS_k(sp)
    sw      gp, CPU_REGS_gp(sp)
    addiu   v0, sp, STACK_OFFSET
    sw      v0, CPU_REGS_sp(sp)
    sw      fp, CPU_REGS_fp(sp)
    sw      ra, CPU_REGS_ra(sp)
    mflo    v0
    mfhi    v1
    swp     v0, CPU_REGS_ra(sp)
    sw      at, CPU_REGS_at(sp)

    addiu   a3, sp, CPU_REGS_SIZE   // a3 = *cp0Regs

    # Get CP0 register contents
    mfc0    s0, _CP0_BADVADDR
    mfc0    s1, $8, 1           # BadInstr
    mfc0    s2, $8, 2           # BadInstrP
    mfc0    s3, _CP0_COUNT
    mfc0    s4, _CP0_COMPARE
    mfc0    s5, _CP0_STATUS
    move    a1, s5              // a1 = Status (for later calling EZBL_TrapHandler()/EZBL_general_exception()/_general_exception_handler())
    mfc0    s6, _CP0_INTCTL
    mfc0    s7, _CP0_SRSCTL
    swm     s0, s1, s2, s3, s4, s5, s6, s7, (0*32)(a3)
    mfc0    s0, _CP0_SRSMAP
    mfc0    s1, _CP0_VIEW_IPL
    mfc0    s2, _CP0_SRSMAP2
    mfc0    s3, _CP0_CAUSE
    move    a0, s3              // a0 = Cause (for later calling EZBL_TrapHandler()/EZBL_general_exception()/_general_exception_handler())
    mfc0    s4, _CP0_VIEW_RIPL
    mfc0    s5, _CP0_NESTEDEXC
    mfc0    s6, _CP0_EPC
    mfc0    s7, _CP0_NESTEDEPC
    swm     s0, s1, s2, s3, s4, s5, s6, s7, (1*32)(a3)
    mfc0    s0, _CP0_PRID
    mfc0    s1, _CP0_EBASE
    mfc0    s2, _CP0_CDMMBASE
    mfc0    s3, _CP0_CONFIG
    mfc0    s4, _CP0_CONFIG1
    mfc0    s5, _CP0_CONFIG2
    mfc0    s6, _CP0_CONFIG3
    mfc0    s7, _CP0_CONFIG4
    swm     s0, s1, s2, s3, s4, s5, s6, s7, (2*32)(a3)
    mfc0    s0, _CP0_CONFIG5
    mfc0    s1, _CP0_CONFIG7
    mfc0    s2, _CP0_LLADDR
    mfc0    s3, _CP0_WATCHLO
    mfc0    s4, _CP0_WATCHHI
    mfc0    s5, _CP0_DEBUG
    mfc0    s6, _CP0_DEBUG2
    mfc0    s7, _CP0_DEPC
    swm     s0, s1, s2, s3, s4, s5, s6, s7, (3*32)(a3)
    mfc0    s0, _CP0_PERFCNT0_CONTROL
    mfc0    s1, _CP0_PERFCNT0_COUNT
    mfc0    s2, _CP0_PERFCNT1_CONTROL
    mfc0    s3, _CP0_PERFCNT1_COUNT
    mfc0    s4, _CP0_ERROREPC
    mfc0    s5, _CP0_DESAVE
    mfc0    s6, $31, 2          # KScratch1
    mfc0    s7, $31, 3          # KScratch2
    swm     s0, s1, s2, s3, s4, s5, s6, s7, (4*32)(a3)

    lwm32   s0, s1, s2, s3, s4, s5, s6, s7, CPU_REGS_s(sp)

    la      a2, EZBL_TrapHandler
    beqzc   a2, 1f
    jalrs16 a2          // void EZBL_TrapHandler(a0 = cause, a1 = status, a2 = *cpuRegs, a3 = *cp0Regs)
    move16  a2, sp

1:  la      a2, EZBL_general_exception
    beqz    a2, 1f
    addiu32 a3, sp, CPU_REGS_SIZE
    lw      a0, CP0_REGS_SIZE + 44(sp)  # Read CP0_REGS.Cause
    lw      a1, CP0_REGS_SIZE + 20(sp)  # Read CP0_REGS.Status
    jalrs16 a2
    move16  a2, sp


1:  la      a2, _general_exception_handler
    beqz    a2, 1f
    lw      a0, CP0_REGS_SIZE + 44(sp)  # Read CP0_REGS.Cause
    jalr    a2
    lw32    a1, CP0_REGS_SIZE + 20(sp)  # Read CP0_REGS.Status


1:  lwp     v0, CPU_REGS_hilo(sp)
    mtlo    v0
    mthi    v1
    lwp     v0, CPU_REGS_v+(0*0x4)(sp)
    lwp     a0, CPU_REGS_a+(0*0x4)(sp)
    lwp     a2, CPU_REGS_a+(2*0x4)(sp)
    lwp     t0, CPU_REGS_t+(0*0x4)(sp)
    lwp     t2, CPU_REGS_t+(2*0x4)(sp)
    lwp     t4, CPU_REGS_t+(4*0x4)(sp)
    lwp     t6, CPU_REGS_t+(6*0x4)(sp)
    lwp     t8, CPU_REGS_t+(8*0x4)(sp)
    lw      ra, CPU_REGS_ra(sp)
    lw      at, CPU_REGS_at(sp)
    addiu	sp,sp,STACK_OFFSET
    ehb
    eret
    
    .size   EZBL_general_exception_context, . - EZBL_general_exception_context
    .popsection
    