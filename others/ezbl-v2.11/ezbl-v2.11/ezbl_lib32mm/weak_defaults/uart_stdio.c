/*******************************************************************************
  Easy Bootloader for PIC24/dsPIC and PIC32MM (ezbl_lib)

  Summary:
    Supplies the read()/write() (or _mon_putc()/_mon_getc()) function
    implementations to redirect stdout/stderr/stdin when the UART_stdio link
    symbol is referenced.
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

#include <stdio.h>


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

unsigned int EZBL_FIFOWrite(EZBL_FIFO *destFIFO, const void *source, unsigned int dataLen);
unsigned int EZBL_FIFORead(void *dest, EZBL_FIFO *srcFIFO, unsigned int dataLen);
static inline unsigned int __attribute__((always_inline)) EZBL_FIFOWrite8(EZBL_FIFO *dest, unsigned char writeData)
{
    return EZBL_FIFOWrite(dest, &writeData, 1);
}
static inline unsigned char __attribute__((always_inline, optimize(1))) EZBL_FIFORead8(EZBL_FIFO *source)
{
    unsigned char readData;
    EZBL_FIFORead(&readData, source, 1);
    return readData;
}


extern EZBL_FIFO __attribute__((persistent)) UART_TxFifo;
extern EZBL_FIFO __attribute__((persistent)) UART_RxFifo;



/**
 * Generic write()/read() function for printf()/scanf() and other C stdlib file
 * I/O operations. This function overrides the internal write()/read() function
 * of the library so whenever a higher layer printf() or similar call is made,
 * these functions get called automatically. This allows stdout/stdin to map to
 * these software FIFO functions instead of directly to a UART peripheral.
 *
 * For further information and info on how these functions interact, with C
 * stdlib file read/write IO, see the XC16 C library user's guide (DS50002071),
 * ex:
 * C:\Program Files (x86)\Microchip\xc16\v1.30\docs\MPLAB_XC16_C_Compiler_Users_Guide.pdf
 *
 * @param handle File handle specifying where the write is intended to go (ex:
 *               stdout, stderr, file, etc.) or read comes from (ex: stdin).
 *               handle is not checked here, so this function will wrap ALL
 *               writes to stdout, stderr, and all other file handles. This
 *               function should be modified or replaced if needing other handle
 *               destinations besides this UART module.
 *
 * @param *buffer Pointer to source data being written to the handle, or for
 *                read() pointer to the destination RAM buffer to write the read
 *                data to.
 *
 * @param len Number of bytes that need to be copied from *buffer to the output
 *            handle, or for read(), number of bytes read from the handle.
 *
 * @return int Number of bytes copied from *buffer to the UART TX FIFO (or from
 *         the UART RX FIFO). This function blocks if there is insufficient
 *         space directly in the software TX FIFO until space is freed up.
 *         However, it returns immediately after all bytes have been buffered
 *         allowing efficient asynchronous and pipelined transmission.
 */
#if defined(__XC16__)
int __attribute__((section(".libc.write"))) write(int handle, void *buffer, unsigned int len)
{
    __asm__("\n"
            "\n     .global _UART_stdio"
            "\n_UART_stdio = _read");
    return EZBL_FIFOWrite(&UART_TxFifo, buffer, len);
}


int __attribute__((section(".libc.read"))) read(int handle, void *buffer, unsigned int len)
{
    return EZBL_FIFORead(buffer, &UART_RxFifo, len);
}


#elif defined(__XC32__)
void _mon_putc(char c)
{
    __asm__(".global UART_stdio"
            "\nUART_stdio = _mon_getc");
    EZBL_FIFOWrite8(&UART_TxFifo, c);
}

int _mon_getc(int canblock)
{
    if(canblock == 0)
    {
        if(UART_RxFifo.dataCount == 0u)
            return -1;
    }
    return (int)(unsigned int)EZBL_FIFORead8(&UART_RxFifo);
}
#endif
