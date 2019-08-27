/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM API Source File

  Summary:
    Implements generic EZBL_FIFO2FIFO() API that reads from one EZBL_FIFO and
    writes the specified bytes to another EZBL_FIFO.

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

// Circular FIFO buffer structure for use with EZBL_FIFOWrite()/EZBL_FIFORead() and related APIs
typedef struct EZBL_FIFO EZBL_FIFO;
struct EZBL_FIFO
{
    volatile unsigned int dataCount;    // Number of bytes that exist in the FIFO. i.e. headPtr - tailPtr, adjusted for wraparound and made atomically readable. You must use EZBL_ATOMIC_ADD() and EZBL_ATOMIC_SUB() if this value is to be modified.
    unsigned char *headPtr;             // Pointer to fifoRAM area for writing
    unsigned char *tailPtr;             // Pointer to fifoRAM area for reading
    unsigned int fifoSize;              // Number of bytes allocated to the fifoRAM
    unsigned char *fifoRAM;             // Main FIFO buffer RAM pointer (or const PSV pointer if only doing reading)
    unsigned int (*onWriteCallback)(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO);   // Pointer to a function to be called anytime the FIFO is written (or attempted to be written)
    unsigned int (*onReadCallback)(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *srcFIFO);      // Pointer to a function to be called anytime the FIFO is read (or attempted to be read)
    unsigned int (*flushFunction)(EZBL_FIFO *fifo, unsigned long timeout); // Pointer to a function to be called anytime the EZBL_FIFOFlush() function is called
    unsigned int irqNum;                // IRQ Number to associate with this FIFO. This number is used to decode the correct IFSx/IECx/IPCx register/bit(s) to access when calling EZBL_FIFOEnableInt()/EZBL_FIFODisableInt()/EZBL_FIFOGetIntEn()/EZBL_FIFO_ClearInt()/EZBL_FIFOSetInt()/EZBL_FIFOGetInt()/EZBL_FIFOSetIntPri()/EZBL_FIFOGetIntPri() functions. IRQ numbers are zero-based where 0 indicates the first ordinary peripheral hardware interrupt (i.e. not a trap)
};

unsigned int EZBL_FIFORead(void *dest, EZBL_FIFO *srcFIFO, unsigned int dataLen);
unsigned int EZBL_FIFOWrite(EZBL_FIFO *destFIFO, const void *source, unsigned int dataLen);


/**
 * Reads and removes data from one EZBL_FIFO and writes it to another EZBL_FIFO.
 *
 * This is a wrapper function that internally calls EZBL_FIFORead(), storing the
 * data linearly on the stack, then in turn calls EZBL_FIFOWrite(). Because the
 * stack is used to temporarily store all requested copy data, it is not
 * advisable to specify a very large dataLen unless you know that there is a
 * large amount of free space on the stack to safely complete the transfer.
 *
 * @param *destFIFO EZBL_FIFO pointer to write the copied data to. If the
 *                  srcFIFO underflows, the destFIFO will be written only with
 *                  the data actually returned during reading.
 *
 *                  Any applicable onWriteCallback will be triggered during one
 *                  or more write cycles.
 *
 *                  This pointer cannot be null (0x0000), even when
 *                  <i>dataLen</i> is 0.
 *
 * @param *srcFIFO EZBL_FIFO pointer to read from.
 *
 *                 Any applicable onReadCallback will be triggered during one
 *                 or more read cycles.
 *
 *                 This pointer cannot be null, even when <i>dataLen</i> is 0.
 *
 * @param copyLen Number of bytes to copy. This value must be smaller than the
 *                amount of free space on the stack, taking into account
 *                additional margin for the onRead and onWrite callbacks, plus
 *                any ISRs taking place.
 *
 *                Zero is allowed, resulting in no data transfer. However, the
 *                srcFIFO->onReadCallback() and destFIFO->onWriteCallback()
 *                function will still be called, if not null.
 *
 * @return Value returned by destFIFO->onWriteCallback() or the number of bytes
 *         written to the FIFO. When unchanged by the callback, this parameter
 *         will be less than <i>copyLen</i> when the source FIFO becomes empty
 *         or the destination FIFO becomes full.
 */
unsigned int EZBL_FIFO2FIFO(EZBL_FIFO *dest, EZBL_FIFO *source, unsigned int copyLen)
{
    unsigned char *buf;

    buf = __builtin_alloca(copyLen);    // Allocate temporary buffer on the stack for the whole copyLen to exist
    copyLen = EZBL_FIFORead(buf, source, copyLen);
    return EZBL_FIFOWrite(dest, buf, copyLen);
}
