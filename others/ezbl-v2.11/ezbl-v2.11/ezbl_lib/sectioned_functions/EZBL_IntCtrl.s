;/*******************************************************************************
;  Easy Bootloader Library for PIC24 and dsPIC33
;
;  Summary:
;    Implements the EZBL_RdIntEn(), EZBL_WrIntEn(), EZBL_ClrIntEn(), EZBL_SetIntEn(), EZBL_InvIntEn(),
;    EZBL_RdIntFlag(), EZBL_WrIntFlag(), EZBL_ClrIntFlag(), EZBL_SetIntFlag(), EZBL_InvIntFlag(),
;    EZBL_RdIntPri() and EZBL_WrIntPri() functions
;
;*******************************************************************************/
;
;/*******************************************************************************
;  Copyright (C) 2018 Microchip Technology Inc.
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

	.equ	SR_C, 0x0	; SR<C> is bit 0
	.equ	SR_Z, 0x1	; SR<Z> is bit 1
	.equ	SR_OV, 0x2	; SR<OV> is bit 2
	.equ	SR_N, 0x3	; SR<N> is bit 3
    .macro  skip_next_instr
    btsc    w15, #0     ; Skips next instruction since w15 stack pointer can never be odd
    .endm


    .pushsection    .text.EZBL_IntCtrl, code
    .global         _EZBL_RdIntEn
    .global         _EZBL_WrIntEn
    .global         _EZBL_ClrIntEn
    .global         _EZBL_SetIntEn
    .global         _EZBL_InvIntEn
	.global         _EZBL_RdIntFlag
    .global         _EZBL_WrIntFlag
	.global         _EZBL_ClrIntFlag
	.global         _EZBL_SetIntFlag
    .global         _EZBL_InvIntFlag
    .global         _EZBL_RdIntPri
    .global         _EZBL_WrIntPri
    .type           _EZBL_RdIntEn, @function
    .type           _EZBL_WrIntEn, @function
    .type           _EZBL_ClrIntEn, @function
    .type           _EZBL_SetIntEn, @function
    .type           _EZBL_InvIntEn, @function
    .type           _EZBL_RdIntFlag, @function
    .type           _EZBL_WrIntFlag, @function
    .type           _EZBL_ClrIntFlag, @function
    .type           _EZBL_SetIntFlag, @function
    .type           _EZBL_InvIntFlag, @function
    .type           _EZBL_RdIntPri, @function
    .type           _EZBL_WrIntPri, @function
    .extern         _IEC0
    .extern         _IFS0
    .extern         _IPC0

; This code has not been tested

_EZBL_RdIntEn:      ; int EZBL_RdIntEn(int irqNum);
    mov     #_IEC0, w2
    skip_next_instr

_EZBL_RdIntFlag:    ; int EZBL_RdIntFlag(int irqNum);
    mov     #_IFS0, w2
    lsr     w0, #3, w7
    bclr    w7, #0
    add     w2, w7, w2
    btst.c  [w2], w0
    btsc    SR, #SR_C
    retlw   #1, w0
    retlw   #0, w0

_EZBL_SetIntFlag:   ; int EZBL_SetIntFlag(int irqNum);
    mov     #1, w1
    skip_next_instr

_EZBL_ClrIntFlag:   ; int EZBL_ClrIntFlag(int irqNum);
    mov     #0, w1

_EZBL_WrIntFlag:    ; int EZBL_WrIntFlag(int irqNum, int newIFSbitVal);
    and     w0, #0xF, w3
    rcall   _EZBL_RdIntFlag
commonWriteBit:
    cp0     w1
    bsw.z   [w2], w3
    return

_EZBL_InvIntFlag:   ; int EZBL_InvIntFlag(int irqNum);
    mov     #0x1, w3
    sl      w3, w0, w3
    rcall   _EZBL_RdIntFlag
    xor     w3, [w2], [w2]
    return


_EZBL_SetIntEn:     ; int EZBL_SetIntEn(int irqNum);
    mov     #1, w1
    skip_next_instr

_EZBL_ClrIntEn:     ; int EZBL_ClrIntEn(int irqNum);
    mov     #0, w1

_EZBL_WrIntEn:      ; int EZBL_WrIntEn(int irqNum, int newIECbitVal);
    and     w0, #0xF, w3
    rcall   _EZBL_RdIntEn
    bra     commonWriteBit

_EZBL_InvIntEn:     ; int EZBL_InvIntEn(int irqNum);
    mov     #0x1, w3
    sl      w3, w0, w3
    rcall   _EZBL_RdIntEn
    xor     w3, [w2], [w2]
    return

_EZBL_WrIntPri:     ; unsigned int EZBL_WrIntPri(int irqNum, unsigned int newPriority);
    mov     w0, w4
    rcall   _EZBL_RdIntPri
    xor     w0, w1, w1
    and     w1, #0xF, w1
    btsc    w4, #0
    swap.b  w1
    xor.b   w1, [w2], [w2]
    return

_EZBL_RdIntPri:    ; unsigned int EZBL_RdIntPri(int irqNum);
    lsr     w0, #2, w3
    mov     #_IPC0, w2
    add     w2, w3, w2
    mov.b   [w2], w3
    btsc    w0, #0
    swap.b  w3
    and     w3, #0xF, w0
    return



