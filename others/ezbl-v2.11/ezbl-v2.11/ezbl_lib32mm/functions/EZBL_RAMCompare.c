/**
 * Easy Bootloader Library for PIC24/dsPIC33/PIC32MM
 *
 * Created 2017 April 05
 *
 * Implements EZBL_RAMCompare() API for testing equality of two arrays in 
 * memory. On PIC32MM platforms, flash memory can also be compared with this 
 * function, despite the name (16-bit backwards compatibility).
 */

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


/**
 * Compares <i>length</i> bytes from <i>*source1</i> with <i>*source2</i>. On
 * exact match 0 is returned. If different, 0x1 is returned.
 *
 * @param *source1 RAM or PSV pointer to data to compare against. Address can be
 *                 byte aligned. This pointer cannot be NULL (0x0000) except
 *                 when <i>length</i> is 0.
 * @param *source2 RAM or PSV pointer to data to compare. Address can be
 *                 byte aligned. This pointer cannot be NULL (0x0000) except
 *                 when <i>length</i> is 0.
 * @param length Number of bytes to compare. 0 is allowed, resulting in no
 *               operation.
 */
int EZBL_RAMCompare(const void *source1, const void *source2, unsigned int length)
{
    unsigned char *rdPtr1 = (unsigned char*)source1;
    unsigned char *rdPtr2 = (unsigned char*)source2;


    while(length--)
    {
        if(*rdPtr1++ != *rdPtr2++)
        {
            return *(--rdPtr1) - *(--rdPtr2);
        }
    }

    return 0;
}
