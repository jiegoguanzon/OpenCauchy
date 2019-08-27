/*******************************************************************************
  Easy Bootloader for PIC24/dsPIC and PIC32MM (ezbl_lib)

  Summary:
    Implements the EZBL_EraseAppSpace() ezbl_lib API

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


//#define EZBL_DEBUG     // Uncomment to allow debugging messages to be printed to stdout

#if !defined(EZBL_DEBUG)
#define EZBL_printf(...)        do{}while(0)
#define EZBL_FIFOFlush(...)     do{}while(0)
#endif


int EZBL_EraseAppSpace(unsigned int *eraseState)
{
    EZBL_UNION_64 limits;
    unsigned long endLimitAddr;
    unsigned long tblRdPtr;
    unsigned int i;
    unsigned int blankRepeats = 8u;
    unsigned short keySave;
#if defined(__XC16__)
    const unsigned int pageAddrSize = EZBL_SYM(EZBL_ADDRESSES_PER_SECTOR);
#elif defined(__XC32__) || defined(__PIC32__)
    const unsigned int pageAddrSize = 0x800u;
#endif

    keySave = EZBL_NVMKey;
    EZBL_NVMKey = 0;

    // Create a dummy 0 item section in case if there are no sections named .text.EZBLAppSpaceGeometry. This takes no space.
    __asm__("\n    .pushsection    .text.EZBLAppSpaceGeometry, code, keep" EZBL_PAGE_ATTR
            "\n    .popsection");

    tblRdPtr = (unsigned long)__builtin_section_begin(".text.EZBLAppSpaceGeometry");
    limits.u64 = EZBL_ReadTablePair(&tblRdPtr);
    i = 0;
    do
    {
        // Find the erase page address
        for(; i < *eraseState; i++)
        {
            if(limits.u8[2] >= 0xF8u)
            {
                EZBL_printf("\n    Reached Config bytes @ 0x%06lX, restoring and calling EZBL_EraseAppSpace() finished\n\n", limits.u32[0]);
                EZBL_RestoreAppErasable(1);
                return 0;
            }

            endLimitAddr = limits.u32[1];
            if(limits.u16[2] & (pageAddrSize-1u))
                endLimitAddr = (endLimitAddr | (pageAddrSize-1u))+1u;
            limits.u32[0] += pageAddrSize;
            if(limits.u32[0] >= endLimitAddr)        // Is start address + x*pageSize past the EZBLAppSpaceGeometry end address?
            {
                if(((unsigned int)tblRdPtr) >= (unsigned int)__builtin_section_end(".text.EZBLAppSpaceGeometry"))
                {
                    EZBL_printf("\n    All done erasing pages. Last tested address = 0x%06lX\n\n", limits.u32[0] - pageAddrSize);
                    return 0;
                }
                limits.u64 = EZBL_ReadTablePair(&tblRdPtr); // Increment to next EZBLAppSpaceGeometry
                if(limits.u32[0] < endLimitAddr)            // Still on the same erase page?
                    i--;
            }
        }
        *eraseState += 1u;

        EZBL_printf("\n0x%06lX  blank checking", limits.u32[0]);
        if(!EZBL_IsPageErased(limits.u32[0])) // Don't waste time or endurance if the page is already blank
        {
            if(EZBL_IsEraseRestricted(limits.u32[0]))  // Don't erase this address if it is in the .text.EZBLNoEraseRanges section, such as user pages reserved for emulated data EEPROM
            {
                EZBL_printf(": erase restricted; skipping");
                continue;
            }
            EZBL_printf(": not blank; erasing");
            EZBL_NVMKey = keySave - (0xFC21u - 0x03DFu);
            EZBL_ErasePage(limits.u32[0]);
            EZBL_RestoreAppErasable(0);         // If we've erased some flash Config words, restore their default values
            return 1;
        }
#if defined(EZBL_DEBUG)
        else
            EZBL_printf(": blank");
#endif
    } while(--blankRepeats);

    return 1;
}
