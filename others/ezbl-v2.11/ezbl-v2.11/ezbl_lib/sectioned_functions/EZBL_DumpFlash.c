/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM

  File Name:
    EZBL_DumpFlash.c

  Summary:
    Implements the EZBL_DumpFlash() API, useful for displaying bulk
    flash/program space contents on a UART or other stdout destination -
    typically for debugging purposes.

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

int EZBL_printf(const char *format, ...);

// Structure for reading consecutive program space instruction words with
// post-inc indirect addressing and 0x00 byte padded 32-bit return values.
// Use with EZBL_ReadUnpackedInd() API.
typedef struct
{
    unsigned long readPgmAddr;  // Instruction word aligned read start address; Self increments by 0x2 addresses per instruction word read
    unsigned long *dest;        // Aligned RAM pointer to write data to. Null valid only when destLen is <= 4.
    unsigned int  destLen;      // Number of bytes of space at *dest to fill with program word data. A non-multiple of 4 will be processed only up to the last complete multiple of 4.
} EZBL_UNPACKED_READER;

unsigned long EZBL_ReadUnpackedInd(EZBL_UNPACKED_READER *params);

#if defined(__XC16__)
unsigned long EZBL_ReadFlash(unsigned long address);
#elif defined(__XC32__) || defined(__PIC32__)
#define EZBL_ReadFlash(address)      (*((unsigned long*)((address) | 0x80000000ul)))
#endif

/**
 * Prints a given flash/program space range in human readable hexadecimal words
 * to stdout.
 *
 * Use the UART_Reset() or I2C_Reset() macros to initialize the _write handler
 * for stdout. Also, for UARTs, ensure the TX I/O pin has a PPS assignment for
 * it, assuming it needs one. Ex:
 *         // Output on U2TX pin assigned to RP45 at 460800 baud, 8N1, with a
 *         // system clock of 70MIPS, and set this UART interface as the stdio
 *         // target.
 *         _RP45R = _RPOUT_U1TX;
 *         UART_Reset(2, 70000000, 460800, 1);
 *
 * @param startAddr Starting memory address to begin reading and printing. This
 *                  value should be even since program memory addresses always
 *                  increment by 0x2 addresses.
 * 
 *                  Must be a legal memory address for the device (address
 *                  beyond the end of flash up to 0x7FFFFF will typically cause
 *                  an Address Error Trap).
 *
 * @param endAddr   End memory address to stop printing when reached. This
 *                  address is NOT printed (i.e. set this to one location after
 *                  the memory of interest).
 */
void EZBL_DumpFlash(unsigned long startAddr, unsigned long endAddr)
{
    unsigned int i;
    unsigned long word;
    unsigned long limitAddr;
    unsigned long words[8];
    EZBL_UNPACKED_READER rd;


    if(endAddr == startAddr)
        return;

    if(startAddr & 0x00000Fu)
    {
        EZBL_printf("\n  %06lX  ", startAddr & 0xFFFFF0u);
        i = startAddr & 0x000Eu;
        while(i)
        {
            EZBL_printf("       ");
            i -= 2u;
        }
        if(startAddr & 0x1u)
        {
            word = EZBL_ReadFlash(startAddr);
            EZBL_printf("%04X   ", (unsigned int)(word>>8));
            startAddr++;
        }
    }

    limitAddr = endAddr & 0xFFFFF0u;
    rd.readPgmAddr = startAddr;
    rd.dest = words;
    rd.destLen = sizeof(words);
    while(rd.readPgmAddr < limitAddr)
    {
        EZBL_printf("\n  %06lX  ", rd.readPgmAddr);
        EZBL_ReadUnpackedInd(&rd);
        EZBL_printf("%06lX %06lX %06lX %06lX %06lX %06lX %06lX %06lX", words[0], words[1], words[2], words[3], words[4], words[5], words[6], words[7]);
    }

    limitAddr = endAddr & 0xFFFFFEu;
    rd.destLen = sizeof(words[0]);
    while(rd.readPgmAddr < limitAddr)
    {
        if((((unsigned int)rd.readPgmAddr) & 0xEu) == 0u)    // Start of new line?
        {
            EZBL_printf("\n  %06lX  ", rd.readPgmAddr & 0xFFFFF0u);
        }
        EZBL_ReadUnpackedInd(&rd);
        EZBL_printf("%06lX ", words[0]);
    }

    if(endAddr & 0x1)
    {
        words[0] = EZBL_ReadFlash(endAddr);
        EZBL_printf("  %04X", (unsigned int)words[0]);
    }
    EZBL_printf("\n");
}
