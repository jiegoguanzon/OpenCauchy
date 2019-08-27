/**
 * Easy Bootloader Library for PIC24/dsPIC33/PIC32MM
 *
 * Created 2017 April 05
 *
 * Implements EZBL_CRC32() API for computing CRC32 (IEEE 802.3 Ethernet, PKZIP, 
 * etc. algorithm).
 *
 */

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
 * Computes or continues a CRC-32 (IEEE 802.3 Ethernet version) over a stream of
 * bytes.
 *
 * The polynomial implemented is:
 * x32 + x26 + x23 + x22 + x16 + x12 + x11 + x10 + x8 + x7 + x5 + x4 + x2 + x + 1
 * Commonly this is represented as 0xEDB88320 (or 0x04C11DB7 for big endian).
 *
 * @param crcSeed Starting value to use in the shift register. Set to 0x00000000
 *                when starting a new CRC32 computation.
 * @param data Pointer to memory to start reading from. There are no alignment
 *             restrictions.
 * @param byteLength Count of how many bytes to include in this CRC computation
 *                   starting at the specified pointer address.
 *
 * @return 32-bit CRC computed. Pass this value back in for the crcSeed
 *         parameter when continuing a checksum with more data.
 */
unsigned long EZBL_CRC32(unsigned long crcSeed, const void *data, unsigned int byteLength)
{
    unsigned int k;
    unsigned int crcIn;
    unsigned int crcNext;
    const unsigned char *rdPtr = data;

    crcSeed = ~crcSeed;
    crcIn = 0x00000000u;

    // Read each chunk of data
    while(byteLength--)
    {
        // Shift in 8 bits
        crcIn = (unsigned int)(*rdPtr++);
        crcSeed ^= crcIn;
        for(k = 0; k < 8u; k++)
        {
            crcNext = (unsigned int)crcSeed;
            crcNext &= 0x01;
            crcSeed >>= 1;
            if(crcNext)
                crcSeed ^= 0xEDB88320;
        }
    }

    return ~crcSeed;
}
