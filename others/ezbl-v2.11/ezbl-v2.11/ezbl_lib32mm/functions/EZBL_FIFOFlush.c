/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM API Source File

  Summary:
    Implements EZBL_FIFOFlush() API that waits a specified amount of time for a
    EZBL_FIFO to empty all data.
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


#if defined(__XC16__)
#define XC16_NEAR            __attribute__((near))
#else
#define XC16_NEAR
#endif


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
    unsigned int (*flushFunction)(EZBL_FIFO *fifo, unsigned long *startTime, unsigned long *timeout); // Pointer to a function to be called anytime the EZBL_FIFOFlush() function is called
    unsigned int irqNum;                // IRQ Number to associate with this FIFO. This number is used to decode the correct IFSx/IECx/IPCx register/bit(s) to access when calling EZBL_FIFOEnableInt()/EZBL_FIFODisableInt()/EZBL_FIFOGetIntEn()/EZBL_FIFO_ClearInt()/EZBL_FIFOSetInt()/EZBL_FIFOGetInt()/EZBL_FIFOSetIntPri()/EZBL_FIFOGetIntPri() functions. IRQ numbers are zero-based where 0 indicates the first ordinary peripheral hardware interrupt (i.e. not a trap)
};


unsigned long NOW_32(void);
unsigned long NOW_Wait(unsigned long waitCount);


/**
 * Blocks until the FIFO holds 0 bytes, a timeout is reached, or an
 * implementation defined flush callback function returns.
 *
 * When the fifo->flushFunction pointer callback has been configured (set to a
 * non-null value), a timer is started, a TimeIsUp() function pointer is
 * generated, and execution is passed to the callback. If the callback chooses
 * to obey the timeout parameter, it can call the TimeIsUp() function to
 * determine when it should return.
 *
 * Only when fifo->flushFunction is zero, this function internally blocks until
 * the FIFO contents reach zero or the specified NOW timeout is reached.
 *
 * @param *fifo Pointer to the FIFO to "flush". If null, no operation is
 *              performed.
 *
 * @param timeout Maximum length of time the flush operation can block for,
 *                specified in NOW counts.
 *
 *                Use a multiple of NOW_second, NOW_millisecond, and/or
 *                NOW_microsecond to choose this timeout.
 *
 *                The special value (unsigned long)-1, or 0xFFFFFFFF, indicates
 *                an infinite wait time.
 *
 * @return flushFunction()'s callback return value, or, when null:
 *         <ul>
 *              <li>1 - All bytes in the FIFO were flushed (0 bytes remain) or
 *                      fifo was a null pointer</li>
 *              <li>0 - Timeout occurred</li>
 *         </ul>
 */
unsigned int EZBL_FIFOFlush(EZBL_FIFO *fifo, unsigned long timeout)
{
    unsigned long startTime;

    if(!fifo)
        return 1;

    startTime = NOW_32();
    if(fifo->flushFunction)
        return fifo->flushFunction(fifo, &startTime, &timeout);

    while(fifo->dataCount)
    {
        NOW_Wait(timeout>>16);
        if(NOW_32() - startTime > timeout)
            break;
    }
    
    return fifo->dataCount == 0u;
}
