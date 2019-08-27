/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM Library API Source File

  Summary:
    Implements the EZBL_FIFORead() and EZBL_FIFOPeek() APIs on an XC32 platform.

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
 * Peeks at the data in a circular EZBL_FIFO, outputting to a normal RAM array.
 * FIFO state and contents are left unchanged, however, the onRead() callback
 * function for the FIFO, if set, is still called.
 *
 * @param *dest Pointer to write the output data to.
 *
 *              Null is allowed, but results in no operation occurring.
 *
 * @param *srcFIFO EZBL_FIFO structure to peek data from. Read underflow is
 *                 suppressed.
 *
 *                 If this pointer is null, the function returns immediately
 *                 with 0.
 *
 * @param dataLen Number of bytes to read.
 *
 *                0 is allowed, resulting in no operation.
 *
 * @return Number of bytes peeked from the FIFO. This parameter will be less
 *         than <i>length</i> only if the peeking pointer has reached the end of
 *         all available data.
 */
unsigned int EZBL_FIFOPeek(void *dest, EZBL_FIFO *srcFIFO, unsigned int dataLen)
{
    unsigned int bytesRead;
    unsigned int chunkSize;
    unsigned int bytesToWrap;
    unsigned char *writeDest;
    unsigned char *tailPtr;

    if((srcFIFO == 0) || (dest == 0))
        return 0;

    tailPtr = srcFIFO->tailPtr;
    writeDest = (unsigned char*)dest;


    bytesRead = 0;
    chunkSize = srcFIFO->dataCount;
    if(chunkSize > dataLen)
        chunkSize = dataLen;

    bytesToWrap = srcFIFO->fifoRAM + srcFIFO->fifoSize - tailPtr;
    if(chunkSize >= bytesToWrap)
    {
        writeDest = EZBL_RAMCopyEx(writeDest, tailPtr, bytesToWrap);
        chunkSize -= bytesToWrap;
        tailPtr = srcFIFO->fifoRAM;
        bytesRead += bytesToWrap;
    }
    if(chunkSize)
    {
        EZBL_RAMCopy(writeDest, tailPtr, chunkSize);
        tailPtr += chunkSize;
        bytesRead += chunkSize;
    }

    if(srcFIFO->onReadCallback)
        return srcFIFO->onReadCallback(bytesRead, dest, dataLen, srcFIFO);   //unsigned int (*onReadCallback)(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *srcFIFO);      // Pointer to a function to be called anytime the FIFO is read (or attempted to be read)

    return bytesRead;
}
