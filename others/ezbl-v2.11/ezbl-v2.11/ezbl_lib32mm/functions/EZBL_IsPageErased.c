/**
 * File:   EZBL_IsPageErased.c
 *
 * Reads all bits on a specified flash page and checks if they are '1's
 * indicating the erased state.
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

#include <cp0defs.h>

enum EZBL_CP0_REGSEL
{
    // Common Name = ((MIPS CP0 Register Num)<<16) | (Select Num)<<11;
    EZBL_CP0_BadVAddr  =  (8<<16) | (0<<11),    // Address for the most recent address-related exception
    EZBL_CP0_BadInstr  =  (8<<16) | (1<<11),    // Instruction that caused the most recent exception
    EZBL_CP0_BadInstrP =  (8<<16) | (2<<11),    // Reports the branch instruction if a delay slot caused the most recent exception
    EZBL_CP0_Count     =  (9<<16) | (0<<11),    // Core Timer (processor cycle count/2)
    EZBL_CP0_Compare   = (11<<16) | (0<<11),    // Core Timer period/compare interrupt value
    EZBL_CP0_Status    = (12<<16) | (0<<11),    // Processor Status and control
    EZBL_CP0_IntCtrl   = (12<<16) | (1<<11),    // Interrupt system status and control
    EZBL_CP0_SRSCtl    = (12<<16) | (2<<11),    // Shadow Register Sets status and control
    EZBL_CP0_SRSMap1   = (12<<16) | (3<<11),    // Shadow set IPL mapping
    EZBL_CP0_View_IPL  = (12<<16) | (4<<11),    // Contiguous view of IM and IPL fields
    EZBL_CP0_SRSMap2   = (12<<16) | (5<<11),    // Shadow set IPL mapping
    EZBL_CP0_Cause     = (13<<16) | (0<<11),    // Cause of last exception
    EZBL_CP0_View_RIPL = (13<<16) | (4<<11),
    EZBL_CP0_NestedExc = (13<<16) | (5<<11),
    EZBL_CP0_EPC       = (14<<16) | (0<<11),    // Program counter at last exception
    EZBL_CP0_NestedEPC = (14<<16) | (2<<11),    // Program counter at last exception
    EZBL_CP0_PRId      = (15<<16) | (0<<11),    // Processor identification and revision
    EZBL_CP0_EBase     = (15<<16) | (1<<11),    // Exception Base Address Register
    EZBL_CP0_PerfCtl0  = (25<<16) | (0<<11),    // Performance Counter 0 Control
    EZBL_CP0_PerfCnt0  = (25<<16) | (1<<11),    // Performance Counter 0
    EZBL_CP0_PerfCtl1  = (25<<16) | (2<<11),    // Performance Counter 1 Control
    EZBL_CP0_PerfCnt1  = (25<<16) | (3<<11),    // Performance Counter 1
    EZBL_CP0_ErrorEPC  = (30<<16) | (0<<11),    // Program counter at last error
};
static inline unsigned int __attribute__((always_inline, optimize(1))) EZBL_MFC0(const enum EZBL_CP0_REGSEL cp0RegSel)
{
    unsigned int i;
    __asm__("mfc0   %0, $%1, %2" : "=d"(i) : "O"(cp0RegSel>>16), "O"((cp0RegSel>>11) & 0x7));
    return i;
}

#define FLASH_BYTES_PER_PAGE            2048u   // Bytes in a Page



/**
 * Blank checks a given Flash Page. A page is considered erased if it reads back
 * as all '1's in every location.
 *
 * An exception is made for the last page of Boot Flash containing the device
 * Configuration Words (0xBFC01700). On this page, two reserved bits will
 * normally read back as '0', even when freshly erased. Therefore, when
 * <i>address</i> specifies this same page, a don't care exception is made for
 * these bits.
 *
 * NOTE: If the page contains significantly corrupt Flash data, for example, due
 * to a power failure in the middle of erase or programming, then this function
 * can trigger a general exception trap. This will occur whenever the ECC bits
 * for the target read location indicate an uncorrectable error and will
 * manifest as a Bus Error Exception (load) in CP0 CAUSE&lt;EXECCODE&gt;.
 *
 * @param addressOnPage
 *          Target address to check for erased status. The address can lie
 *          anywhere on the page with the function internally starting at the
 *          first location on the page and terminating on the last location in
 *          the page, or the first non-blank word, whichever is found first.
 *
 *          addressOnPage can be a virtual address or physical address. The
 *          blank check reads will occur against a kseg1 (uncached) pointer.
 *
 * @return <ul><li>0 - Page is NOT blank</li>
 *             <li>1 - Page is blank</li></ul>
 */
int EZBL_IsPageErased(unsigned long addressOnPage)
{
    unsigned long *readPtr;
    unsigned long i;
    unsigned long readData;

    readPtr = (unsigned long*)((addressOnPage & ~(FLASH_BYTES_PER_PAGE-1u)) | 0xA0000000u);  // Force kseg1 address
    for(i = 0; i < FLASH_BYTES_PER_PAGE; i += sizeof(*readPtr))
    {
        readData = *readPtr++;

        // First check if the prior read triggered a Bus Error Exception (0x7).
        // This would be likely due to an uncorrectable ECC error (i.e. someone
        // programmed the same Flash dword twice), so we must force an erase to
        // ensure we don't brick ourselves by being unable to erase an address
        // that triggers ECC bus errors.
        if((EZBL_MFC0(EZBL_CP0_Cause) & _CP0_CAUSE_EXCCODE_MASK) == (0x7u<<_CP0_CAUSE_EXCCODE_POSITION))
            return 0;

        if(readData != 0xFFFFFFFFul)
        {
            if(readData == 0x7FFFFFFFu) // Make an exception for the reserved '0' bit in Flash Config or Alternate Config (0xBFC017E0, 0xBFC01760, 0x9FC017E0, or 0x9FC01760 bit 15) since this bit will typically erase to a zero (assuming the device isn't Code Protected and a Page Erase is performed)
            {
                if((((unsigned long)(readPtr - 1u)) & 0xDFFFFF7Fu) == 0xBFC01760u)
                    continue;
            }
            return 0;
        }
    }

    return 1;
}
