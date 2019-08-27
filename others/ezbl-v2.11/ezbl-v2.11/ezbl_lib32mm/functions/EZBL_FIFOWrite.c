/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM API Source File

  Summary:
    Implements the EZBL_FIFOWrite() API on an XC32 platform.

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


/**
 * Writes data into a circular EZBL_FIFO RAM buffer from a normal data array,
 * updating the headPtr and dataCount in the FIFO structure. dataCount is
 * updated atomically without disabling interrupts.
 *
 * After the write is complete and the headPtr/dataCount FIFO parameters
 * updated, an optional destFIFO->onWriteCallback() function pointer is called.
 * This callback is invoked only when the pointer is non-null.
 *
 * The onWrite callback function must have type:
 *      unsigned int yourOnWriteFuncName(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO);
 * The return value of this callback is returned unchanged by EZBL_FIFOWrite(),
 * giving you an opportunity to manipulate the FIFO contents, return value,
 * and/or trigger other code to start reading the data out of the FIFO (ex: to a
 * UART TX or other communications ISR). The bytesPushed parameter specifies the
 * byte count actually added to the FIFO, *writeSrc is the original value of
 * *source, and reqWriteLen is the original value of dataLen.
 *
 * Note: the onWrite callback is called after the write has already taken place.
 * Therefore, any manipulation done to the writeSrc array will have no effect on
 * the FIFO. However, as writing to the FIFO does not destroy or manipulate the
 * source data, it is possible to re-read the source data from the original
 * pointer if you wish to save the data elsewhere (ex: a second FIFO connected
 * to a debug terminal or other "communications tee").
 *
 * @param *destFIFO EZBL_FIFO pointer to write the copied data to. FIFO overflow
 *                  is supressed. FIFO wraparound and write pointer update is
 *                  internally handled.
 *
 *                  If this pointer is null (0x0000), all processing is skipped
 *                  and no data is read from *source. However, EZBL_FIFOWrite()
 *                  returns dataLen, emulating an infinite bit-bucket.
 * @param *source RAM or PSV pointer to read data from. Address can be byte
 *                aligned. This pointer cannot be null except when <i>length</i>
 *                is 0.
 * @param dataLen Number of bytes to copy. This value must be <= 16384 on
 *                dsPIC30, PIC24F, PIC24H, or dsPIC33F families.
 *
 *                Zero is allowed, resulting in no write operation. However, the
 *                destFIFO->onWriteCallback() function will still be called, if
 *                not null.
 *
 * @return Value returned by destFIFO->onWriteCallback() or the number of bytes
 *         written to the FIFO. When unchanged by the callback, this parameter
 *         will be less than <i>length</i> only when the FIFO becomes completely
 *         full and the write is terminated early to avoid FIFO overflow.
 *
 *         If *destFIFO is null, dataLen is returned, emulating a successful
 *         write.
 */
unsigned int EZBL_FIFOWrite(EZBL_FIFO *destFIFO, const void *source, unsigned int dataLen)
{
    unsigned int bytesWritten;
    unsigned int chunkSize;
    unsigned int bytesToWrap;
    const unsigned char *srcPtr;

    if(destFIFO == 0)
        return dataLen;

    srcPtr = (const unsigned char*)source;

    bytesWritten = 0;
    chunkSize = destFIFO->fifoSize - destFIFO->dataCount;
    if(chunkSize > dataLen)
        chunkSize = dataLen;
    bytesWritten = chunkSize;

    bytesToWrap = destFIFO->fifoRAM + destFIFO->fifoSize - destFIFO->headPtr;
    if(chunkSize >= bytesToWrap)
    {
        EZBL_RAMCopyEx(destFIFO->headPtr, srcPtr, bytesToWrap);
        srcPtr += bytesToWrap;
        chunkSize -= bytesToWrap;
        destFIFO->headPtr = destFIFO->fifoRAM;
    }
    destFIFO->headPtr = EZBL_RAMCopyEx(destFIFO->headPtr, srcPtr, chunkSize);

    EZBL_ATOMIC_ADD(destFIFO->dataCount, bytesWritten);

    if(destFIFO->onWriteCallback)
        return destFIFO->onWriteCallback(bytesWritten, (void*)source, dataLen, destFIFO);   //unsigned int (*onWriteCallback)(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO);   // Pointer to a function to be called anytime the FIFO is written (or attempted to be written)

    return bytesWritten;
}
