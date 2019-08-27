/*******************************************************************************
  Easy Bootloader Library for PIC24 and dsPIC33

  Summary:
    Implements the EZBL_IntCtl*() functions

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


/**
 * Clears the IECx<y> Interrupt Enable bit for the specified interrupt vector.
 *
 * IECx register bit change is done atomically.
 *
 * @param irqNum IRQ/Interrupt vector to manipulate the IECx bits for.
 *
 * @return Prior IECx Interrupt Enable state, not taking into account global
 *         interrupt enable/disable states, such as a DISI instruction or
 *         GIE == 0 condition.
 *          <ul>
 *              <li>0 = Interrupt Enable bit was previously clear</li>
 *              <li>1 = Interrupt Enable bit was previously set</li>
 *          </ul>
 */
# int EZBL_ClrIntEn(int irqNum);


/**
 * Clears the IFSx<y> Interrupt Flag bit for the specified interrupt vector.
 *
 * IFSx register bit change is done atomically.
 *
 * @param irqNum IRQ/Interrupt vector number to manipulate the IFSx bits for.
 *
 * @return Prior IFSx Interrupt Flag state
 *          <ul>
 *              <li>0 = No interrupt was pending</li>
 *              <li>1 = Interrupt was pending</li>
 *          </ul>
 */
# int EZBL_ClrIntFlag(int irqNum);


    .equ    zero, $0
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
    .equ    ra, $31

    .pushsection    .text.EZBL_IntCtrl, code
    .global         EZBL_RdIntEn
	.global         EZBL_WrIntEn
    .global         EZBL_ClrIntEn
    .global         EZBL_SetIntEn
	.global         EZBL_InvIntEn
    .global         EZBL_RdIntFlag
    .global         EZBL_WrIntFlag
    .global         EZBL_ClrIntFlag
    .global         EZBL_SetIntFlag
    .global         EZBL_InvIntFlag
    .global         EZBL_RdIntPri
    .global         EZBL_WrIntPri
    .global         EZBL_FIFOSetIntPri
    .global         EZBL_FIFOGetIntPri
    .global         EZBL_FIFOGetIntEn
    .global         EZBL_FIFOIntEnable
    .global         EZBL_FIFOIntDisable
    .global         EZBL_FIFOIntEnableSet
    .global         EZBL_FIFOGetInt
    .global         EZBL_FIFOIntRaise
    .global         EZBL_FIFOIntClear
    .global         EZBL_FIFOIntFlagSet
    .type           EZBL_RdIntEn, @function
    .type           EZBL_WrIntEn, @function
    .type           EZBL_ClrIntEn, @function
    .type           EZBL_SetIntEn, @function
    .type           EZBL_InvIntEn, @function
    .type           EZBL_RdIntFlag, @function
    .type           EZBL_WrIntFlag, @function
    .type           EZBL_ClrIntFlag, @function
    .type           EZBL_SetIntFlag, @function
    .type           EZBL_InvIntFlag, @function
    .type           EZBL_RdIntPri, @function
    .type           EZBL_WrIntPri, @function
    .type           EZBL_FIFOSetIntPri, @function
    .type           EZBL_FIFOGetIntPri, @function
    .type           EZBL_FIFOGetIntEn, @function
    .type           EZBL_FIFOIntEnable, @function
    .type           EZBL_FIFOIntDisable, @function
    .type           EZBL_FIFOIntEnableSet, @function
    .type           EZBL_FIFOGetInt, @function
    .type           EZBL_FIFOIntRaise, @function
    .type           EZBL_FIFOIntClear, @function
    .type           EZBL_FIFOIntFlagSet, @function
    .equ            irqNumOffset, 32    # Byte offset to irqNum in EZBL_FIFO structure
    .extern         IEC0
    .extern         IFS0
    .extern         IPC0
    .set            micromips
    .set            noreorder

EZBL_RdIntFlag:         # int EZBL_RdIntFlag(int irqNum);
    b       _readWriteIFS
    li      v1, 0x0
EZBL_InvIntFlag:        # int EZBL_InvIntFlag(int irqNum);
    b       _readWriteIFS
    li      v1, 0xC
EZBL_ClrIntFlag:        # int EZBL_ClrIntFlag(int irqNum);
    li      a1, 0
EZBL_WrIntFlag:         # int EZBL_WrIntFlag(int irqNum, int newIFSbitVal);
    beqz    a1, _readWriteIFS
    li      v1, 0x4
EZBL_SetIntFlag:        # int EZBL_SetIntFlag(int irqNum);
    li      v1, 0x8

_readWriteIFS:
    # Get existing IECx<y> value into v0, v1 = &IFS0, a3 = 0x1<<(irqNum&0x1F)
    la      v0, IFS0

_RegGet:
    ext     a2, a0, 5, 3            # a2 = high bits<7:5> for SFR addr
    sll     a2, a2, 4               # a2 = high bits<7:5> for SFR addr*0x10 so we skip past CLR/SET/INV regs
    addu    a2, v0, a2              # a2 = &IECx/&IFSx

    lw      v0, 0(a2)               # v0 = IECx/IFSx
    beqz    v1, _noWrite            # v1: Read/CLR/SET/INV offsets of 0x0, 0x4, 0x8, or 0xC
    srlv    v0, v0, a0

    addu    a2, a2, v1              # a2 = &IEC[fifo->irqNum]CLR/SET/INV
    li      a3, 0x1
    sllv    a3, a3, a0              # a3 = 0x1<<(fifo->irqNum & 0x1F) mask for CLR/SET/INV
    sw      a3, 0(a2)

_noWrite:
    jr      ra
    andi    v0, v0, 0x1


EZBL_RdIntEn:           # int EZBL_RdIntEn(int irqNum);
    b       _readWriteIEC
    li      v1, 0x0
EZBL_InvIntEn:          # int EZBL_InvIntEn(int irqNum);
    b       _readWriteIEC
    li      v1, 0xC
EZBL_ClrIntEn:          # int EZBL_ClrIntEn(int irqNum);
    li      a1, 0
EZBL_WrIntEn:           # int EZBL_WrIntEn(int irqNum, int newIECbitVal);
    beqz    a1, _readWriteIEC
    li      v1, 0x4
EZBL_SetIntEn:          # int EZBL_SetIntEn(int irqNum);
    li      v1, 0x8

_readWriteIEC:    # Get existing IECx<y> value into v0, v0 = &IECx, a3 = 0x1<<(irqNum&0x1F)
    lui     v0, %hi(IEC0)
    b       _RegGet
    addiu   v0, %lo(IEC0)

    # unsigned int EZBL_RdIntPri(int irqNum);
EZBL_RdIntPri:
    b       _readWriteIPC
    li      v1, 1

# unsigned int EZBL_WrIntPri(int irqNum, unsigned int newPriority);
EZBL_WrIntPri:
    li      v1, 0
_readWriteIPC:
    lui     a2, %hi(IPC0)
    srl     v0, a0, 2
    sll     v0, v0, 4
    andi    a0, a0, 0x3
    addu    a0, v0, a0
    addu    a2, a2, a0
    beqz    v1, 1f
    lbu     v1, %lo(IPC0)(a2)           # a3 = IPC[fifo->irqNum];
0:  jr      ra
    srl     v0, v1, 2                   # v0 = bits<4:2> for Priority (bits 1:0 are subpriority and we'll ignore those)
1:  ins     v1, a1, 2, 3
    b       0b
    sb      v1, %lo(IPC0)(a2)           # IPC[fifo->irqNum] = newPriority<<2 | existingSubPriority;

EZBL_FIFOGetIntPri:     # unsigned int EZBL_FIFOGetIntPri(EZBL_FIFO *fifo);
    b       1f
    li      v1, 1
EZBL_FIFOSetIntPri:     # unsigned int EZBL_FIFOSetIntPri(EZBL_FIFO *fifo, unsigned int newPriority);
    li      v1, 0
1:  lui     a3, %hi(_readWriteIPC)
    b       _testNullFifo
    addiu   a3, %lo(_readWriteIPC)

EZBL_FIFOGetInt:        # int EZBL_FIFOGetInt(EZBL_FIFO *fifo);
    b       _getIRQNumForFlag
    li      v1, 0x0
EZBL_FIFOIntClear:      # int EZBL_FIFOIntClear(EZBL_FIFO *fifo);
    li      a1, 0
EZBL_FIFOIntFlagSet:    # int EZBL_FIFOIntFlagSet(EZBL_FIFO *fifo, int newIFSbitVal);
    beqz    a1, _getIRQNumForFlag
    li      v1, 0x4
EZBL_FIFOIntRaise:      # int EZBL_FIFOIntRaise(EZBL_FIFO *fifo);
    li      v1, 0x8
_getIRQNumForFlag:
    la      a3, _readWriteIFS

_testNullFifo:
    beqz    a0, _noWrite            # if(fifo == 0) goto _noWrite
    li      v0, 0
    jr      a3
    lbu     a0, irqNumOffset(a0)    # a0 = fifo->irqNum;

EZBL_FIFOGetIntEn:      # int EZBL_FIFOGetIntEn(EZBL_FIFO *fifo);
    b       _getIRQNumForEn
    li      v1, 0x0
EZBL_FIFOIntDisable:    # int EZBL_FIFOIntDisable(EZBL_FIFO *fifo);
    li      a1, 0
EZBL_FIFOIntEnableSet:  # int EZBL_FIFOIntEnableSet(EZBL_FIFO *fifo, int newIECbitVal);
    beqz    a1, _getIRQNumForEn
    li      v1, 0x4
EZBL_FIFOIntEnable:     # int EZBL_FIFOIntEnable(EZBL_FIFO *fifo);
    li      v1, 0x8
_getIRQNumForEn:
    lui     a3, %hi(_readWriteIEC)
    b       _testNullFifo
    addiu   a3, %lo(_readWriteIEC)


    .size   EZBL_RdIntEn, . - EZBL_RdIntEn
    .size   EZBL_WrIntEn, . - EZBL_WrIntEn
    .size   EZBL_ClrIntEn, . - EZBL_ClrIntEn
    .size   EZBL_SetIntEn, . - EZBL_SetIntEn
    .size   EZBL_InvIntEn, . - EZBL_InvIntEn
    .size   EZBL_RdIntFlag, . - EZBL_RdIntFlag
	.size   EZBL_WrIntFlag, . - EZBL_WrIntFlag
	.size   EZBL_ClrIntFlag, . - EZBL_ClrIntFlag
    .size   EZBL_SetIntFlag, . - EZBL_SetIntFlag
	.size   EZBL_InvIntFlag, . - EZBL_InvIntFlag

    .size   EZBL_RdIntPri, . - EZBL_RdIntPri
    .size   EZBL_WrIntPri, . - EZBL_WrIntPri

    .size   EZBL_FIFOIntEnable, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOIntDisable, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOIntEnableSet, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOIntRaise, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOIntClear, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOIntFlagSet, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOGetIntEn, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOGetInt, . - EZBL_FIFOIntDisable
    .size   EZBL_FIFOGetIntPri, . - EZBL_FIFOGetIntPri
    .size   EZBL_FIFOSetIntPri, . - EZBL_FIFOSetIntPri
    .popsection
