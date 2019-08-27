/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM Library API Source File

  Summary:
    Implements the EZBL_FIFORead() and EZBL_FIFOPeek() APIs.
    NOTE: ezbl_lib.a contains an ASM optimized implementation for PIC24/dsPIC
    devices, so this file is intended for use on PIC32 targets

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

#if defined(__XC16__) || defined(__PIC24__) || defined(__dsPIC__)
#define EZBL_ATOMIC_SUB(lval, value)    __asm__ __volatile__("subr %1, [%0], [%0]" : : "r"(&(lval)), "r"(value) : "memory", "cc")
#elif defined(__XC32__) || defined(__PIC32__)
#define EZBL_ATOMIC_SUB(lval, value)    do                                  \
                                        {                                   \
                                            __builtin_disable_interrupts(); \
                                            lval -= value;                  \
                                            __builtin_enable_interrupts();  \
                                        } while(0)
#endif


typedef union EZBL_COM_ACTIVITY COM_ACTIVITY_FLAGS; // Defined for backwards compatibility - use EZBL_COM_ACTIVITY instead.

// Bitfield structure for signaling communication FIFO state changes, especially
// for code operating in different asynchronous contexts.
typedef union
{
    struct
    {
        unsigned int tx:1;          // One or more bytes moved from the TX software FIFO to the TX hardware (ex: U2TX ISR wrote to U2TXREG)
        unsigned int rx:1;          // One or more bytes moved from RX hardware into the software RX FIFO (ex: _U2RXInterrupt() copied data out of U2RXREG)
        unsigned int softTx:1;      // Zero or more bytes moved into the TX software FIFO using a call to EZBL_FIFOWrite() or a wrapper function. Ex: UART2_TX_FIFO_OnWrite() callback executed.
        unsigned int softRx:1;      // Zero or more bytes peeked or read from the RX software FIFO as a result of a call to EZBL_FIFORead() or EZBL_FIFOPeek(). Ex: UART2_RX_FIFO_OnRead() callback executed.
        unsigned int rxOverflow:1;  // Hardware or software RX FIFO overflow/data lost
        unsigned int framingError:1;// UART RX Framing Error detected
        unsigned int parityError:1; // RX Parity Error detected
        unsigned int bootkeyRx:1;   // Bootloader synchronization/wakeup key detected (normally "MCUPHCME")
        unsigned int :7;
        unsigned int other:1;       // Status bit indicating other (ill-defined) hardware events or software calls have taken place that affect the state of the software TX/RX FIFOs or underlying hardware. Example captured status updates include: FIFO reset/initialized and software arming of auto-baud.
    };
    unsigned int any;           // Integral union form of the tx, rx, softTx, softRx, and other flags for testing/clearing multiple flags at once. Use with the EZBL_COM_ACTIVITY_*_MASK bit-masking constants.
} EZBL_COM_ACTIVITY;

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
    unsigned int irqNum;                // IRQ Number to associate with this FIFO. This number is used to decode the correct IFSx/IECx/IPCx register/bit(s) to access when calling EZBL_FIFOIntEnable()/EZBL_FIFOIntDisable()/EZBL_FIFOIntClear()/EZBL_FIFOIntRaise()/EZBL_FIFOIntEnableSet()/EZBL_FIFOIntFlagSet()/EZBL_FIFOGetIntEn()/EZBL_FIFOGetIntPri() functions. IRQ numbers are zero-based where 0 indicates the first ordinary peripheral hardware interrupt (i.e. not a trap)
    volatile EZBL_COM_ACTIVITY activity;// Status bit flags capturing various software and hardware state change events like RX interrupt byte, software RX FIFO read, TX interrupt byte, software TX FIFO write, RX bootloader wake key detection, HW/SW FIFO RX overflow and other activity
};  // Circular software FIFO buffer structure for use with EZBL_FIFOReset()/EZBL_FIFOWrite()/EZBL_FIFORead() and related APIs. FIFOs permit high speed non-blocking transfer of ordered data streams across asynchronous timing domains, such as a UART RX ISR and a bootloader consumer, or indirect printf() output to the UART TX ISR.


void * EZBL_RAMCopyEx(void *dest, const void *source, unsigned int length);



/**
 * Reads and removes or peeks at data in a circular EZBL_FIFO, outputting to a
 * normal RAM array. The tailPtr is incremented and dataCount atomically
 * reduced for reads.
 *
 * After the data has been read/peeked from the FIFO and copied to *dest, an
 * optional call to the source->onReadCallback() function is made. This call is
 * made only when *onReadCallback is not null.
 *
 * The onRead callback function must have type:
 *      unsigned int yourOnReadFuncName(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *srcFIFO);
 * The return value of this callback is returned unchanged by 
 * EZBL_FIFORead()/EZBL_FIFOPeek(), giving you an opportunity to manipulate the
 * read contents, return value, and/or trigger other code to refill any FIFO
 * free space generated by the read. The bytesPulled parameter specifies the
 * byte count removed from the FIFO, *readDest is the original value of *dest,
 * reqReadLen is the original value of dataLen, and *srcFIFO is the pointer to
 * the FIFO that is calling the callback (so one callback handler function could
 * be implemented to handle multiple different FIFOs simultaneously).
 *
 * Note: when implementing the onRead callback, *readDest can be null if *dest
 * was null when EZBL_FIFORead() was called. In such cases, the read data was
 * thrown away and cannot be reobtained. Technically the data is still in the
 * FIFO backing RAM and could be read for real through direct access, but since
 * the source->dataCount and tailPtr values have already been updated by the
 * time the callback is called, the data is subject to being overwritten by any
 * asynchronous events that may write to the FIFO. This ordering exists to allow
 * APIs that write to the FIFO to be called directly in the onReadCallback().
 *
 * @param *dest Pointer to write the output data to. If null, no read is
 *              performed but the tailPtr, dataCount, and return value will
 *              still be set as if a normal read took place, effectively
 *              throwing the buffer data away.
 *
 *              If source->onReadCallback() is non-null, it will still be called
 *              for null *dest values.
 *
 * @param *srcFIFO EZBL_FIFO structure to read data from. Read underflow is
 *                 suppressed.
 *
 *                 If this pointer is null, the function returns immediately
 *                 with 0.
 *
 * @param dataLen Number of bytes to read. This value must be <= 16384 on
 *                dsPIC30, PIC24F, PIC24H, or dsPIC33F families.
 *
 *                Zero length is allowed, resulting in no read operation, but
 *                the source->onReadCallback() callback is still called, if
 *                defined.
 *
 * @return Return value from the source->onReadCallback() function or the number
 *         of bytes read and removed from the FIFO when no callback is defined.
 *         Unless changed by the callback routine, this parameter will be less
 *         than <i>length</i> only if the FIFO has run out of data to read.
 */
static unsigned int EZBL_FIFOReadOrPeek(void *dest, EZBL_FIFO *srcFIFO, unsigned int dataLen, int peeking)
{
    unsigned int bytesRead;
    unsigned int chunkSize;
    unsigned int bytesToWrap;
    unsigned char *writeDest;
    unsigned char *tailPtr;

    if(srcFIFO == 0)
        return 0;

    writeDest = (unsigned char*)dest;

    chunkSize = srcFIFO->dataCount;
    if(chunkSize > dataLen)
        chunkSize = dataLen;
    bytesRead = chunkSize;

    tailPtr = srcFIFO->tailPtr;
    bytesToWrap = srcFIFO->fifoRAM + srcFIFO->fifoSize - tailPtr;
    if(chunkSize >= bytesToWrap)
    {
        if(writeDest)
            writeDest = EZBL_RAMCopyEx(writeDest, tailPtr, bytesToWrap);
        chunkSize -= bytesToWrap;
        tailPtr = srcFIFO->fifoRAM;
    }
    if(writeDest)
        EZBL_RAMCopyEx(writeDest, tailPtr, chunkSize);

    if(!peeking)
    {
        srcFIFO->tailPtr = tailPtr + chunkSize;
        EZBL_ATOMIC_SUB(srcFIFO->dataCount, bytesRead);
    }

    if(srcFIFO->onReadCallback)
        return srcFIFO->onReadCallback(bytesRead, dest, dataLen, srcFIFO);   //unsigned int (*onReadCallback)(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *srcFIFO);      // Pointer to a function to be called anytime the FIFO is read (or attempted to be read)

    return bytesRead;
}

unsigned int EZBL_FIFORead(void *dest, EZBL_FIFO *srcFIFO, unsigned int dataLen)
{
    return EZBL_FIFOReadOrPeek(dest, srcFIFO, dataLen, 0);
}

unsigned int EZBL_FIFOPeek(void *dest, EZBL_FIFO *srcFIFO, unsigned int dataLen)
{
    return EZBL_FIFOReadOrPeek(dest, srcFIFO, dataLen, 1);
}
