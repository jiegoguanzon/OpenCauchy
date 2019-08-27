/**
 * Easy Bootloader Library for PIC24/dsPIC33/PIC32MM
 *
 * Created 2017 April 05
 *
 * Implements EZBL_RAMCopy() API for copying an array in memory - synonymous 
 * with the memcpy() C-stdlib function.
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
 * Copies RAM efficiently, just like memcpy() but without dependence on a C
 * library that could go in application space or get optimized to inline code.
 *
 * @param *dest RAM pointer to write the copied data to. Address can be byte
 *              aligned. This pointer cannot be NULL (0x0000) except when
 *              <i>length</i> is 0. This pointer may overlap with the *source
 *              pointer memory addresses, in which case, the copy operation will
 *              proceed starting at the first dest/source byte and proceeding to
 *              the end with each byte being written before the next is read.
 * @param *source RAM or PSV pointer to read data from. Address can be byte
 *                aligned. This pointer cannot be null except when
 *                <i>length</i> is 0.
 * @param length Number of bytes to copy. 0 is allowed, resulting in no
 *               operation.
 *
 * @return *dest pointer, unchanged
 */
void * EZBL_RAMCopy(void *dest, const void *source, unsigned int length)
{
    const unsigned int *rdPtr32 = (const unsigned int*)source;
    unsigned int *wrPtr32       = (unsigned int*)dest;
    const unsigned char *rdPtr;
    unsigned char *wrPtr;

    // See if source and dest are 32-bit aligned for a fast copy
    if((((unsigned int)dest | (unsigned int)source) & 0x3u) == 0u)
    {
        while(length & 0xFFFFFFFCu)
        {
            *wrPtr32++ = *rdPtr32++;
            length -= sizeof(unsigned int);
        }
    }

    // Revert to byte copy for residual 1-3 bytes and for unaligned source/dest data
    rdPtr = (const unsigned char*)rdPtr32;
    wrPtr = (unsigned char*)wrPtr32;

    while(length--)
    {
        *wrPtr++ = *rdPtr++;
    }
    
    return dest;
}


/**
 * Copies RAM efficiently, just like memcpy() but without dependence on a C
 * library that could go in application space or get optimized to inline code.
 *
 * @param *dest RAM pointer to write the copied data to. Address can be byte
 *              aligned. This pointer cannot be NULL (0x0000) except when
 *              <i>length</i> is 0. This pointer may overlap with the *source
 *              pointer memory addresses, in which case, the copy operation will
 *              proceed starting at the first dest/source byte and proceeding to
 *              the end with each byte being written before the next is read.
 * @param *source RAM or PSV pointer to read data from. Address can be byte
 *                aligned. This pointer cannot be null except when
 *                <i>length</i> is 0.
 * @param length Number of bytes to copy. 0 is allowed, resulting in no
 *               operation.
 *
 * @return *dest pointer, incremented by length bytes
 */
void * EZBL_RAMCopyEx(void *dest, const void *source, unsigned int length)
{
    return (void*)(((unsigned int)EZBL_RAMCopy(dest, source, length)) + length);
}
