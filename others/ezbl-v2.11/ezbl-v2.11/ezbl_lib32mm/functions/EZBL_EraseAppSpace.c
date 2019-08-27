/**
 * File:   EZBL_EraseAppSpace.c
 *
 * Iterates over PIC32MM Application space flash pages, blank checks them, and
 * if not blank, starts an NVMCON flash page erase operation. Only one page is
 * erased per call to avoid a long blocking loop.
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


//#define EZBL_DEBUG     // Uncomment to allow debugging messages to be printed to stdout

#if defined(EZBL_DEBUG)
#if defined(EZBL_LIB)
#include "../ezbl.h"
#else
#include "ezbl_integration/ezbl.h"
#endif
#define EZBL_printf(...)    {EZBL_printf(__VA_ARGS__); EZBL_FIFOFlush(EZBL_STDOUT, NOW_sec);}
#else
#define EZBL_printf(...)        do{}while(0)
#define EZBL_FIFOFlush(...)     do{}while(0)
typedef union
{
    unsigned long long  u64;
    unsigned long       u32[2];
    unsigned short      u16[4];
    unsigned char       u8[8];
} EZBL_UNION_64;
#endif


#if defined(__XC16__)
#define EZBL_PAGE_ATTR                  ", page"
#elif defined(__XC32__) || defined(__PIC32__)
#define EZBL_PAGE_ATTR
#endif

#define FLASH_BYTES_PER_PAGE            2048u   // Bytes in a Page - true for all PIC32MM devices


extern volatile unsigned int EZBL_NVMKey;
int EZBL_IsPageErased(unsigned long addressOnPage);
unsigned long EZBL_ErasePage(unsigned long pageAddress);


/**
 * Erases all flash pages dedicated to the Application (address ranges contained
 * in the ".text.EZBLAppSpaceGeometry" section) in a piecewise fashion.
 *
 * Set <b>EZBL_NVMKey = 0xFC21</b> before each call to this function.
 *
 * Since erasing flash pages requires a long execution time, this function
 * returns between each page erase operation, allowing housekeeping and other
 * tasks to be serviced before starting another page erase operation.
 *
 * @param *eraseState pointer to an unsigned int to keep internal track of which
 *                    page was last erased/which page will be erased next.
 *
 *                    On first call, set this memory location to 0x0000. Ignore
 *                    the return value on subsequent calls, but return the same
 *                    value when continuing the erase sequence.
 * @return <ul>
 *              <li>1 - Erase sequence was initiated for a block of memory. One
 *                      or more calls to EZBL_EraseAppSpace() are required to
 *                      complete the erase.</li>
 *              <li>0 - Erase of all blocks in the App space geometry are
 *                      complete.</li>
 *         </ul>
 */
int EZBL_EraseAppSpace(unsigned int *eraseState)
{
    unsigned long addr;
    EZBL_UNION_64 *limits;
    unsigned int i;
    unsigned long endLimitAddr;


    // Create a dummy 0 item section in case if there are no sections named .text.EZBLAppSpaceGeometry. This takes no space.
    __asm__("\n    .pushsection    .text.EZBLAppSpaceGeometry, code, keep" EZBL_PAGE_ATTR
            "\n    .popsection");

    limits = (EZBL_UNION_64*)__builtin_section_begin(".text.EZBLAppSpaceGeometry");
    EZBL_printf("\nEntered EZBL_EraseAppSpace(); Table addr = 0x%08lX, *eraseState = %u", (unsigned long)limits, *eraseState);
    addr = limits->u32[0];              // Get EZBLAppSpaceGeometry start address
    for(i = 0; i < *eraseState; i++)
    {
        endLimitAddr = limits->u32[1];
        if(endLimitAddr & (FLASH_BYTES_PER_PAGE-1u))
            endLimitAddr = (endLimitAddr | (FLASH_BYTES_PER_PAGE-1u))+1u;
        addr += FLASH_BYTES_PER_PAGE;
        if(addr >= endLimitAddr)        // Is start address + x*pageSize past the EZBLAppSpaceGeometry end address?
        {
            limits++;                   // Increment to next EZBLAppSpaceGeometry
            if((unsigned long)limits >= (unsigned long)__builtin_section_end(".text.EZBLAppSpaceGeometry"))
            {
                EZBL_NVMKey = 0;
                return 0;
            }
            addr = limits->u32[0];
            if(addr < endLimitAddr)     // Still on the same erase page?
                i--;
        }
    }
    *eraseState += 1u;

    EZBL_printf("\n%08X  Testing EZBLAppSpaceGeometry", addr);
    if(!EZBL_IsPageErased(addr)) // Don't waste time or endurance if the page is already blank
    {
        EZBL_printf(": not blank; erasing");
        EZBL_NVMKey -= (0xFC21u - 0x03DFu);
        EZBL_ErasePage(addr);
        EZBL_printf(" done with page");
    }

    EZBL_NVMKey = 0;
    return 1;
}
