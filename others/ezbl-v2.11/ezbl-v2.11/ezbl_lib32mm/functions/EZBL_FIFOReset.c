/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM API Source File

  Summary:
    Implements generic EZBL_FIFOReset() API

 *******************************************************************************/


// DOM-IGNORE-BEGIN
/*******************************************************************************
  Copyright (C) 2017 Microchip Technology Inc.

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
// DOM-IGNORE-END


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


/**
 * Initializes or re-initializes an EZBL_FIFO structure with zero bytes of data,
 * and a specified backing memory.
 * 
 * @param *fifo pointer to an EZBL_FIFO structure to reset.
 * @param *buffer pointer to a RAM array to store the FIFO's data to.
 * @param bufferLen Number of bytes at the *buffer location to allocate to the 
 *                  FIFO.
 * @param *onWriteCallback() pointer to a callback function to call anytime 0 or 
 *        more bytes of data is written to the FIFO via public APIs 
 *        (EZBL_FIFOWrite(), EZBL_FIFOWrite8(), EZBL_FIFOprintf(), etc.). Set to 
 *        null if unneeded. This function is called after all data has been 
 *        written. Set to null ((void*)0) if you do not need onWrite 
 *        notification.
 * @param *onReadCallback() pointer to a callback function to call anytime 0 or 
 *        more bytes are read or peeked from the FIFO via public APIs. This 
 *        function is called after all data has been read into the callers 
 *        buffer (or discarded for a null read buffer output pointer).
 * 
 *        Set to null ((void*)0) if you do not need onRead/onPeek notification.
 * 
 * @return fifo's dataCount, fifoSize, fifoRAM, headPtr, and tailPtr elements 
 *         are all reset.
 * 
 *         Note, the memory contained in the physical buffer memory is not 
 *         zeroed, so any sensitive data that might be contained in them should 
 *         be zeroed externally.
 */
void EZBL_FIFOReset(EZBL_FIFO *fifo, void *buffer, unsigned int bufferLen, unsigned int (*onWriteCallback)(unsigned int bytesWritten, void *writeData, unsigned int dataLen, EZBL_FIFO *writeFIFO), unsigned int (*onReadCallback)(unsigned int bytesRead, void *readData, unsigned int dataLen, EZBL_FIFO *readFIFO))
{
    fifo->dataCount = 0;
    fifo->fifoSize  = bufferLen;
    fifo->fifoRAM   = buffer;
    fifo->headPtr   = buffer;
    fifo->tailPtr   = buffer;
    fifo->onWriteCallback = onWriteCallback;
    fifo->onReadCallback  = onReadCallback;
    fifo->flushFunction   = 0;
}
