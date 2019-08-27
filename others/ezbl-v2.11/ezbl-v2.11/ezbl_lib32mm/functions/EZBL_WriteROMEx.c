/**
 * File:   EZBL_WriteROMEx.c
 *
 * Implements Flash Erase/Write code for all PIC32MM devices.
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

#if defined(EZBL_LIB)
#include "../ezbl.h"
#else
#include "ezbl_integration/ezbl.h"
#endif


//#define EZBL_DEBUG     // Uncomment to allow debugging messages to be printed to stdout

#if defined(EZBL_DEBUG)
#define ALLOC_TO_BOOT_FLASH
#else
int __attribute((far)) EZBL_AddrInTable(unsigned long tableBaseAddr, unsigned int tablePgmAddrLen, unsigned long testAddress);
#define ALLOC_TO_BOOT_FLASH     __attribute__((far, section(".text.EZBL_BootFlashFunc")))
#define EZBL_printf(...)        do{}while(0)
#endif


// Flash parameters
#define FLASH_BYTES_PER_PAGE            2048u   // Bytes in a Page
#define FLASH_BYTES_PER_ROW             256u    // Bytes in a row
#define FLASH_MIN_PROGRAM_SIZE          8u      // Bytes in a "Double Word" programming operation, or other minimum ganularity wrote operation


#define NVMCON_OP_NOP                   (0x0u)  // Clears any WRERR/LVDERR status bits but does not erase or program anything
#define NVMCON_OP_PROG_FLASH_WORD       (0x2u)  // Program a "Double Word", or 8 bytes (one 64-bit Flash word, with ECC, if applicable), on Flash word aligned boundaries
#define NVMCON_OP_PROG_ROW              (0x3u)  // Program a full "Row", or 256 bytes at a time on row aligned boundaries
#define NVMCON_OP_ERASE_PAGE            (0x4u)  // Erase a Flash Page, or 2048 bytes at a time on page aligned boundaries
#define NVMCON_OP_ERASE_MAIN            (0x7u)  // Erase all main Program Flash Memory (does not include Boot Flash or Config Words, all pages must not be write protected)
#define NVMCON_OP_CHIP_ERASE            (0xEu)  // Erase everything, including Main Flash, Boot Flash and Config Words, all partitions (if applicable). This opcode can only be executed externally via the ICSP or JTAG programming interface pins.


extern volatile unsigned int __attribute__((section("sfrs"))) NVMCON;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMCONCLR;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMCONSET;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMKEY;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMADDR;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMSRCADDR;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMDATA0;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMDATA1;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMPWP;
extern volatile unsigned int __attribute__((section("sfrs"))) NVMBWP;
extern volatile unsigned int __attribute__((section("sfrs"))) RCON;

#define _NVMCON_WR_MASK                          0x00008000
#define _NVMCON_WREN_MASK                        0x00004000
#define _NVMCON_WRERR_MASK                       0x00002000


extern volatile unsigned int * __attribute__((weak))    EZBL_IntDisReqReg;
extern int __attribute__((weak))                        EZBL_IntDisReqFlagPos;

static int EZBL_DoNVMOp(unsigned long targetAddr, unsigned int nvmOp, unsigned int keyPrime, unsigned long sourceData1, unsigned long data2);


volatile unsigned int EZBL_NVMKey;  // Set to 0x03DF to enable a Flash writing API. NVMCON unlock keying should not depend solely on the adjacent instruction stream due to risky execution behavior with VDD contact bounce and BOR/POR induced restart of oscillator/PLL analog components.


/**
 * Internal function for handling all NVMCON<WR> bit operations. Do not call
 * this function directly. Instead use EZBL_EraseChip(), EZBL_WriteMemory()
 * and/or EZBL_ErasePage(), EZBL_ProgramLit64(), EZBL_Program64().
 *
 * keyPrime must be passed as 0x03DF and EZBL_NVMKey must also contain 0x03DF
 * for the operation to be completed. Any other values will cause NVMKEY
 * unlocking to fail, thus blanking the NVM operation.
 *
 * EZBL_NVMKey is not changed on return.
 */
static int ALLOC_TO_BOOT_FLASH EZBL_DoNVMOp(unsigned long targetAddr, unsigned int nvmOp, unsigned int keyPrime, unsigned long sourceData1, unsigned long data2)
{
    unsigned long tempAddr;

    targetAddr &= 0x1FFFFFFF;                   // Ensure we have a physical Flash address, not kseg0 or kseg1 address

    tempAddr = sourceData1 & 0x1FFFFFFC;        // RAM source address for Row writes must be a Physical address and 32-bit aligned
    while(NVMCON & _NVMCON_WR_MASK);            // Wait for any prior programming operation to complete

    if(NVMCON & _NVMCON_WRERR_MASK)             // WRERR is a sticky bit, and can get set when trying to erase/program write-protected Flash (ex: Bootloader addresses), so we need to execute a NOP NVMOp to clear the WRERR bit
    {
        if(nvmOp != NVMCON_OP_NOP)
        {
            EZBL_printf("\nEZBL_DoNVMOp(): NVMCON<WRERR> set"
                        "\n  NVMCON = 0x%04X, NVMADDR = 0x%08X, NVMSRCADDR = 0x%08X"
                        "\n  NVMDATA = 0x%08X_%08X"
                        "\n  NVMPWP = 0x%08X, NVMBWP = 0x%08X, RCON = 0x%08X", NVMCON, NVMADDR, NVMSRCADDR, NVMDATA1, NVMDATA0, NVMPWP, NVMBWP, RCON);
            EZBL_DoNVMOp(0x00000000, NVMCON_OP_NOP, 0x03DFu, 0, 0);
            while(NVMCON & _NVMCON_WR_MASK);    // Wait for NVM NOP to complete
        }
    }

    NVMCONCLR = _NVMCON_WREN_MASK;              // Ensure WREN bit is clear before we try to set NVMCON<NVMOP>. NVMOP is read only when WREN is set.
    NVMADDR = targetAddr;                       // Destination address
    NVMSRCADDR = tempAddr;                      // RAM source address (only used for row writes, unimportant otherwise)
    NVMDATA0 = sourceData1;                     // Low 32-bits for Double Word writes (unimportant otherwise)
    NVMDATA1 = data2;                           // High 32-bits for Double Word writes (unimportant otherwise)
    NVMCON = _NVMCON_WREN_MASK | nvmOp;         // Set NVMCON<WREN> and NVMOP
    //EZBL_printf("\n%08X  NVMCON = 0x%04X, NVMDATA = 0x%08X_%08X", NVMADDR, NVMCON, NVMDATA1, NVMDATA0);
    if(&EZBL_IntDisReqReg)
    {
        if(((unsigned int)EZBL_IntDisReqReg) & 0x0F800000u)     // SFR address
            EZBL_IntDisReqReg[2] = 1u<<((unsigned int)&EZBL_IntDisReqFlagPos);  // SET register
        else
            *EZBL_IntDisReqReg |= 1u<<((unsigned int)&EZBL_IntDisReqFlagPos);   // RAM, do read-modify-write
        while(*EZBL_IntDisReqReg & (1u<<((unsigned int)&EZBL_IntDisReqFlagPos)));
    }
    __builtin_disable_interrupts();             // Unlock NVM
    NVMKEY = 0x00000000;                        // Prep for future NVM unlock sequence. This ensures decoder is in the fully locked state.
    NVMKEY = (0xAA996655u - 0x000003DFu) + keyPrime;
    NVMKEY = (0x556699AAu + 0x000003DFu) - EZBL_NVMKey;
    NVMCONSET = _NVMCON_WR_MASK;                // Start NVMOP
    __builtin_enable_interrupts();



#if defined(EZBL_DEBUG)
    if(keyPrime != 0x3DFu)
        EZBL_printf("\n  keyPrime = 0x%04X", keyPrime);
    if(EZBL_NVMKey != 0x3DFu)
        EZBL_printf("\n  EZBL_NVMKey = 0x%04X", EZBL_NVMKey);
    while(NVMCON & _NVMCON_WR_MASK);            // Wait for NVM NOP to complete
    if(NVMCON & _NVMCON_WRERR_MASK)
        EZBL_printf("\n  WRERR set after NVM command: NVMCON = 0x%04X", NVMCON);
#endif

    return 1;
}



unsigned int EZBL_RestoreAppErasableInRAMPage(unsigned long pageDataTargetAddr, void *pageData)
{
    unsigned long *eraseRestoreTable;
    unsigned long *eraseRestoreTableEnd;
    unsigned long recordTarget;
    unsigned long recordSize;
    unsigned long pageBaseAddr = pageDataTargetAddr & 0x1FFFF800u;
    unsigned long chunkLen;
    unsigned int bytesRestored = 0;
    
    // Generate a dummy erase restore table section for linking in case if there
    // are no restorable items in this bootloader. This takes no space.
    __asm__("\n    .pushsection    .text.EZBLEraseRestoreTable, code, keep" EZBL_PAGE_ATTR
            "\n    .popsection");

    eraseRestoreTable = (unsigned long*)__builtin_section_begin(".text.EZBLEraseRestoreTable");
    eraseRestoreTableEnd = (unsigned long*)__builtin_section_end(".text.EZBLEraseRestoreTable");
    while((unsigned long)eraseRestoreTableEnd > (unsigned long)eraseRestoreTable)
    {
        recordTarget = *eraseRestoreTable++ & 0x1FFFFFFFu;
        recordSize = *eraseRestoreTable++;
        if((recordTarget & 0x1FFFF800u) != pageBaseAddr)
        {
            eraseRestoreTable += (recordSize+3u)/4u;
            continue;
        }
        chunkLen = recordSize;
        if(recordTarget - pageBaseAddr + chunkLen > FLASH_BYTES_PER_PAGE)
            chunkLen = pageBaseAddr + FLASH_BYTES_PER_PAGE - recordTarget;
        EZBL_RAMCopyEx(((char*)pageData) + (((unsigned short)recordTarget) & (FLASH_BYTES_PER_PAGE-1u)), eraseRestoreTable, chunkLen);
        bytesRestored += chunkLen;
        eraseRestoreTable += (recordSize+3u)/4u;
    }

    return bytesRestored;
}


/**
 * Erases one page of flash memory (2048 bytes or 0x800 addresses on PIC32MM
 * devices). Only Application flash pages can be targeted, with attempted
 * erasure of Bootloader pages being ignored.
 *
 * Set EZBL_NVMKey = 0x03DF before calling this function. The erase will be
 * skipped using any other value. EZBL_NVMKey will be cleared to 0x0 upon
 * return.
 *
 * This function does not do an internal pre-erase blank check, so calling this
 * function against an already erased page will waste time and flash endurance.
 * However, doing so will also ensure that any corrupt flash double words can be
 * cleared without triggering an ECC Double Error Detect bus load general
 * exception by pre-erase blank checking.
 *
 * @param pageAddresss Target flash address to erase. The address is checked to
 *                     ensure it is in not in the .text.EZBLNoEraseRanges table.
 *                     The address can lie anywhere on the target page.
 *
 * @return On apparent success, next sequential flash page address (i.e.
 *         pageAddress + 2048). Hardware write protect or an incorrect
 *         EZBL_NVMKey value can negate the erase sequence and return apparent
 *         success.
 *
 *         If the given erase address lies overlaps anything in the
 *         .text.EZBLNoEraseRanges table, this function will not
 *         erase anything and returns 0xFFFFFFFF.
 *
 *         EZBL_NVMKey is set to 0 in all cases.
 */
unsigned long EZBL_ErasePage(unsigned long pageAddress)
{
    int ret;
    unsigned long physicalAddr = pageAddress & 0x1FFFF800u;
    const unsigned long *kseg1Ptr = (const unsigned long*)(physicalAddr | 0xA0000000u);
    unsigned long pageData[FLASH_BYTES_PER_PAGE/4];
    unsigned int i;
    unsigned int preservedBytes = 0;


    EZBL_NVMKey += 0x1234u;
    ret = 0;
    preservedBytes = sizeof(pageData);
    for(i = 0; i < sizeof(pageData)/sizeof(pageData[0]); i++)
    {
        pageData[i] = *kseg1Ptr;
        if(!EZBL_IsEraseRestricted((unsigned long)kseg1Ptr++))
        {
            preservedBytes -= 4u;
            pageData[i] = 0xFFFFFFFF;
            ret = 0x1234;
        }
    }
    kseg1Ptr -= sizeof(pageData)/sizeof(pageData[0]);
    if(ret == 0x1234)
    {
        EZBL_NVMKey -= (unsigned int)ret;
        ret = EZBL_RestoreAppErasableInRAMPage(physicalAddr, pageData);
#if defined(EZBL_DEBUG)
        if(preservedBytes | ret)
            EZBL_printf("\nEZBL_ErasePage(0x%08lX): Restricted. Preserved %u bytes, restored %u bytes.", pageAddress, preservedBytes, ret);
#endif
        EZBL_DoNVMOp(physicalAddr, NVMCON_OP_ERASE_PAGE, EZBL_NVMKey, 0, 0);
        if(preservedBytes | ret)
            EZBL_WriteROMEx(physicalAddr, pageData, sizeof(pageData), 0, 0xDEAD, 0);
        EZBL_NVMKey = 0;
        return pageAddress + FLASH_BYTES_PER_PAGE;
    }

    EZBL_printf("\nEZBL_ErasePage(0x%08lX): Fully restricted", pageAddress);
    EZBL_NVMKey = 0;
    return -1;
}


/**
 * Programs an array of bytes into Main Flash, Boot Flash, Config Word, Inactive
 * Partition Flash at the specified Program Address. If the flash already
 * contains incompatible data, a read-modify-write will occur in which the whole
 * target page is read into RAM, modified within the specified target byte
 * range, then reprogrammed before returning. The target address and data length
 * do not have to be aligned or padded to match the native flash word size.
 *
 * Before calling this function, set <b>EZBL_NVMKey = 0x03DF</b> to permit
 * the flash write. Any other value in EZBL_NVMKey will cause all function
 * behavior to execute, but without actually writing anything to Flash.
 *
 *
 * The operation blocks for all but the last flash write block's write
 * operation. If this function is called while a prior non-blocking NVM flash
 * erase or write operation is already in progress, the function blocks until
 * the prior operation completes.
 *
 * Interrupts are disabled for a minimal time before starting each flash
 * programming operation and then enabled.
 *
 * In timing critical applications, an optional
 *     volatile unsigned int * EZBL_IntDisReqReg;
 * global variable should be declared and initialized to point to an IFSx
 * interrupt flags SFR address or RAM location to facilitate synchronization of
 * when precisely this function is allowed to disable interrupts and perform a
 * queued write operation. When enabled, bit 0 (or EZBL_IntDisReqFlagPos) of the
 * SFR/RAM pointer target will be set when EZBL_WriteROM[Ex]() needs to disable
 * interrupts, and then the code will block until something outside the
 * EZBL_WriteROM[Ex]() function clears the same bit.
 *
 * Generally, a time sensitive ISR would/should clear the bit each time it
 * finishes execution, allowing interrupts to be disabled for the NVM operation
 * only when an NVM write operation is least likely to interfere with concurrent
 * application functionality.
 *
 * To specify which bit in (*EZBL_IntDisReqReg) will be used for synchronization
 * (instead of the default of bit 0), at global scope, write:
 *     EZBL_SetSYM(EZBL_IntDisReqFlagPos, <i>i</i>);
 * where '<i>i</i>' is a link-time integer constant between 0 and 15.
 *
 *
 * In Bootloader or other applications where some of the device's program memory
 * map should never be programmed or altered, the applicable address range(s)
 * should be set by using the:
 *     EZBL_SetNoProgramRange(<i>startProgramAddr</i>, <i>endProgramAddr</i>);
 * macro declared at global scope. The start address is inclusive while the end
 * address is exclusive (i.e. the first address that IS programable).
 *
 * This macro will result in all EZBL_WriteROM[Ex]() calls being checked against
 * the given range and piecewise skipped if overlapping (and not overridden by
 * the extraNoProgRanges special 0xDEAD value).
 * EZBL_SetNoProgramRange() can be instanced multiple times to define
 * discontinuous address ranges that need to be write-protected.
 *
 * Skipping occurs based on the minimum flash write granularity applicable for
 * the device. For example, on devices that can program 48-bit single
 * instruction words, a no-program range of [0x000230, 0x000240), and a
 * EZBL_WriteROM[Ex]() call to program [0x000200, 0x000300) will result in data
 * being programmed to [0x000200, 0x000230) and [0x000240, 0x000300) with the
 * input source data for the no-program range being read but thrown away to
 * maintain logical stream synchronization. On PIC32MM devices, this quantity is
 * 8 bytes.
 *
 * @param destProgAddr
 *   Address in Program Memory to start writing to. This parameter must be
 *   a legal and implemented memory address or a bus error exception will occur
 *   on initial read checking. A physical, kseg0, or kseg1 address may be
 *   specified. This address can specify any byte within an NVM target word.
 *
 *   Pointers to flash should be type-casted to an (unsigned long) for this
 *   parameter.
 *
 * @param srcData
 *   Pointer to the source data in RAM to write to flash. Supports arbitrary
 *   byte alignment.
 *
 * @param byteCount
 *   Number of bytes to write. If the data length is not an integer multiple of
 *   the native flash word write size, the bytes in the last partial word are
 *   padded with 0xFF bytes and still programmed.
 *
 *   No error checking is done to ensure the destProgAddr + effective number
 *   of addresses needed for byteCount is physically present on the device.
 *   Writing to unimplemented target addresses will typically cause a bus error
 *   exception.
 *
 * @param flags
 *   Bitwise OR'd collection of flags to modify the writing behavior. Available
 *   options are:
 *   <ul>
 *      <li>EZBL_FLAG_CLRWDT (0x0400)           - Clear watchdog before each write</li>
 *   </ul>
 *
 * @param extraNoProgRanges
 *   Number of address pairs at noProgRanges to use for excluding specific
 *   addresses from being written.
 *
 *   Every 1 extraNoProgRanges implies 8 bytes of RAM at *noProgRanges will
 *   be read. Set to 0 if the default .text.EZBLNoProgramRanges section data
 *   should be used without anything extra.
 *
 *   The special value 0xDEAD (or (unsigned short)-8531) can be given to disable
 *   all no-program range checking, ignoring all contents in
 *   .text.EZBLNoProgramRanges and the *noProgRanges parameter. Use this with
 *   caution as it will allow overwriting of any device address, including this
 *   function or bootloader code, potentially bricking the device.
 *
 * @param *noProgRanges Pointer to array of 32-bit address pairs which should be
 *                      skipped during writing if overlapped with the dest
 *                      address write range. Checking is done on native flash
 *                      word boundary address, so low order range address bits
 *                      will be ignored (treated as '0').
 *
 *                      This parameter is ignored if extraNoProgRanges == 0 or
 *                      0xDEAD.
 *
 * @return
 *   Returns the next sequential flash word's memory address. In other words:
 *   destProgAddr + byteCount
 *
 *   The value contained in EZBL_NVMKey is cleared to 0x0000.
 *
 *   NVMCON<WR> may not be cleared upon return if executing from RAM or writing
 *   to a non-blocking flash target.
 */
unsigned long ALLOC_TO_BOOT_FLASH __EZBL_WriteROMEx(unsigned long destProgAddr, const void *srcData, unsigned int byteCount, unsigned int flags, int extraNoProgRanges, const unsigned long *noProgRanges)
{
    unsigned long alignedDestStartAddr;
    unsigned long alignedDestEndAddr;
    unsigned int keySave;
    unsigned int pageByteCount;
    unsigned long *flashRdPtr;
    unsigned long *noProgTable;
    unsigned int noProgTableSize;
    union
    {
        unsigned char u8[8];
        unsigned short u16[4];
        unsigned long u32[2];
        unsigned long long u64;
    } readData, writeData;
    int i;
    int j;
//    unsigned long pageData[FLASH_BYTES_PER_PAGE/sizeof(unsigned long)];
//    unsigned long pageAddr;
//    unsigned int bytesToUpdate;
//    unsigned int memWriteIndex;
//    unsigned long alignedKseg1;
//    unsigned char *srcDataReadPtr;

//    EZBL_printf("\n%08X", destProgAddr);


    keySave = EZBL_NVMKey + 1u;
    EZBL_NVMKey = 0;


    // Generate a list of addresses in RAM which we are not allowed to write to - typically Bootloader occupied space
    // Create a dummy 0 item section in case if there are no calls to .text.EZBLNoProgramRanges(). This takes no space.
    __asm__("\n    .pushsection    .text.EZBLNoProgramRanges, code, keep" EZBL_PAGE_ATTR
            "\n    .popsection");
    noProgTable = 0;
    noProgTableSize = 0;
    if(((unsigned short)extraNoProgRanges) != (unsigned short)0xDEADu)
    {
        noProgTableSize = extraNoProgRanges*8u + (unsigned int)__builtin_section_size(".text.EZBLNoProgramRanges");
        noProgTable = (unsigned long*)__builtin_alloca(noProgTableSize);
        if(noProgTable == 0)    // Can't actually happen - __builtin_alloca() always returns a pointer to the stack, but optimized away so who cares
        {
            noProgTable = (unsigned long*)__builtin_section_begin(".text.EZBLNoProgramRanges");
            noProgTableSize = __builtin_section_size(".text.EZBLNoProgramRanges");
        }
        else
        {   // Copy .text.EZBLNoProgramRanges section contents and noProgRanges[] arrays into a single contiguous array
            flashRdPtr = noProgTable;
            for(i = 0; i < __builtin_section_size(".text.EZBLNoProgramRanges")/4u; i++)
                *flashRdPtr++ = ((volatile unsigned long*)__builtin_section_begin(".text.EZBLNoProgramRanges"))[i];
            for(i = 0; i < extraNoProgRanges*2u; i++)
                *flashRdPtr++ = *noProgRanges++;
        }
    }

    while(byteCount)
    {
        pageByteCount = FLASH_BYTES_PER_PAGE - (destProgAddr & 0x7FFu);
        if(pageByteCount > byteCount)
            pageByteCount = byteCount;
        alignedDestStartAddr = (destProgAddr & 0xFFFFFFF8u) | 0x80000000u;
        alignedDestEndAddr = alignedDestStartAddr + pageByteCount;
        if(alignedDestEndAddr & 0x7)
            alignedDestEndAddr = (alignedDestEndAddr | 0x7u) + 0x1u;
        byteCount -= pageByteCount;

//        // Check if the target byte range (double word aligned and padded) is erased
//        for(flashRdPtr = (unsigned long*)alignedDestStartAddr; flashRdPtr != (unsigned long*)alignedDestEndAddr; /* no inc */)
//        {
//            pageData[0] = *flashRdPtr++;
//            if(pageData[0] != 0xFFFFFFFFu)
//            {
//                if(((((unsigned int)flashRdPtr-1) & 0xDFFFFF7Fu) != 0xBFC01760u) || (pageData[0] != 0x7FFFFFFFu)) // Ignore reserved bit that erases to '0'
//                    break;
//            }
//        }
//        if((unsigned long)flashRdPtr != alignedDestEndAddr)
//        {   // Destination subrange has overlapping stuff already on it, do read-modify-write of flash
//
//            pageAddr = alignedDestStartAddr & 0xFFFFF800;
//            EZBL_printf("\n%08X  Read-modify-write needed", (unsigned int)--flashRdPtr);
//            for(i = 0; i < sizeof(pageData)/sizeof(pageData[0]); i++)           // Read full page contents from flash into RAM
//                pageData[i] = ((volatile unsigned long*)pageAddr)[i];
//
//            //EZBL_DumpFlashDiff(0, 0, pageAddr, (const void*)pageData, 0, 0x800);
//
//            // Modify page contents in RAM, blocking the modification for restricted regions
//            bytesToUpdate = pageByteCount;
//            memWriteIndex = (destProgAddr & 0x7FF);
//            alignedKseg1 = alignedDestStartAddr | 0xA0000000u;
//            srcDataReadPtr = (unsigned char*)srcData;
//            unsigned char *memWritePtr = ((unsigned char*)pageData) + memWriteIndex;
//            unsigned int bytesChanged = 0;
//            i = 0;  // Flag to track if any changes were made
//            while(bytesToUpdate)
//            {
//                unsigned int chunkLen = FLASH_MIN_PROGRAM_SIZE - (memWriteIndex & 0x7);
//                if(chunkLen > bytesToUpdate)
//                    chunkLen = bytesToUpdate;
//                if(!EZBL_AddrInTable((unsigned long)noProgTable, noProgTableSize, alignedKseg1))
//                {
//                    for(i = 0; i < chunkLen; i++)
//                    {
//                        if(*srcDataReadPtr != *memWritePtr)
//                        {
//                            *memWritePtr = *srcDataReadPtr;
//                            bytesChanged++;
//                        }
//                        memWritePtr++;
//                    }
//                    //EZBL_RAMCopyEx(((char*)pageData) + memWriteIndex, srcDataReadPtr, chunkLen);  // Overwrite page data of interest
//                }
//                else
//                {
//                    //EZBL_printf("\n%08X  Page skip modify - noProgTable (srcDataReadPtr = 0x%08X, memWriteIndex = 0x%03X, chunkLen = %u)", alignedDestStartAddr, (unsigned int)srcDataReadPtr, memWriteIndex, chunkLen);
//                    EZBL_printf("\n%08X  Page skip modify - noProgTable [%u] = 0x%08X_%08X, attempt = 0x%08X, chunkLen = %u)", alignedDestStartAddr, memWriteIndex/4u, pageData[memWriteIndex/4u+1u], pageData[memWriteIndex/4u], *((unsigned long*)srcDataReadPtr), chunkLen);
//                }
//
//                srcDataReadPtr += chunkLen;
//                memWriteIndex += chunkLen;
//                bytesToUpdate -= chunkLen;
//                alignedKseg1 += FLASH_MIN_PROGRAM_SIZE;
//            }
//            //EZBL_DumpFlashDiff(0, 0, pageAddr, (const void*)pageData, 0, 0x800);
//
//            if(!bytesChanged)
//            {
//                    EZBL_printf("\n%08X  Read modify write abandoned, nothing changed", alignedDestStartAddr);
//            }
//            else
//            {
//                EZBL_NVMKey += keySave;
//                if(flags & EZBL_FLAG_CLRWDT)
//                    ClrWdt();
//                if(EZBL_ErasePage(pageAddr) != (unsigned long)-1)
//                {
//                    EZBL_NVMKey += keySave;
//                    for(i = sizeof(pageData) - FLASH_BYTES_PER_ROW; i >= 0; i -= FLASH_BYTES_PER_ROW)
//                    {
//                        if(flags & EZBL_FLAG_CLRWDT)
//                            ClrWdt();
//                        EZBL_DoNVMOp(pageAddr + (unsigned long)i, NVMCON_OP_PROG_ROW, EZBL_NVMKey, ((unsigned long)pageData) + (unsigned long)i, 0);
//                    }
//                    EZBL_NVMKey = 0;
//                }
//            }
//            destProgAddr += pageByteCount;
//            for(i = 0; i < sizeof(pageData)/sizeof(pageData[0]); i++)           // Clear page contents from RAM - likely contains executable code, so safer to prevent PC from jumping to it and executing it
//                pageData[i] = ((volatile unsigned long*)pageAddr)[i];
//            continue;
//        }

        // Destination subrange is erased, pad/align source data and sequentially write it
        while(pageByteCount)
        {
            writeData.u64 = 0xFFFFFFFFFFFFFFFFull;
            i = FLASH_MIN_PROGRAM_SIZE - (destProgAddr & 0x7);
            if(i > pageByteCount)
                i = pageByteCount;
            for(j = 0; j < i; j++)
            {
                writeData.u8[(destProgAddr & 0x7u) + j] = ((char*)srcData)[j];
            }

            do
            {
                // Skip writes with no data to write
                if(writeData.u64 == 0xFFFFFFFFFFFFFFFFull)
                {
                    EZBL_printf("\n%08X  Skip: all 1's", alignedDestStartAddr);
                    break;
                }
                else if(EZBL_AddrInTable((unsigned long)noProgTable, noProgTableSize, alignedDestStartAddr))    // Skip writes which lie on a no-programming range
                {
                    EZBL_printf("\n%08X  Skip: NoProgramRange", alignedDestStartAddr);
                    break;
                }

                // Skip writes with already matching data in Flash
                readData.u64 = *((unsigned long long*)(alignedDestStartAddr | 0xA0000000u));
                if(readData.u64 == writeData.u64)
                {
                    EZBL_printf("\n%08X  Skip: data match 0x%08lX_%08lX", alignedDestStartAddr, readData.u32[1], readData.u32[0]);
                    break;
                }
                else if(readData.u64 != 0xFFFFFFFFFFFFFFFFull)  // Skip writes if target range isn't in erased state
                {
                    EZBL_printf("\n%08X  Skip: data conflict: 0x%08lX_%08lX, need 0x%08lX_%08lX", alignedDestStartAddr, readData.u32[1], readData.u32[0], writeData.u32[1], writeData.u32[0]);
                    break;
                }

                EZBL_NVMKey += (keySave - 1u) + ((readData.u32[0] & writeData.u32[0]) ^ writeData.u32[0]);
                if(flags & EZBL_FLAG_CLRWDT)
                    ClrWdt();
                EZBL_DoNVMOp(alignedDestStartAddr, NVMCON_OP_PROG_FLASH_WORD, EZBL_NVMKey, writeData.u32[0], writeData.u32[1]);
            } while(0);
            EZBL_NVMKey = 0;

            alignedDestStartAddr = alignedDestStartAddr + FLASH_MIN_PROGRAM_SIZE;
            pageByteCount -= i;
            srcData = ((char*)srcData) + i;
            destProgAddr += i;
        }
    }
    return destProgAddr;
}


// Wrapper function to allow common code to call the far __EZBL_WriteROMEx()
// function allocated in Boot Flash
unsigned long EZBL_WriteROMEx(unsigned long destProgAddr, const void *srcData, unsigned int byteCount, unsigned int flags, int extraNoProgRanges, const unsigned long *noProgRanges)
{
    return __EZBL_WriteROMEx(destProgAddr, srcData, byteCount, flags, extraNoProgRanges, noProgRanges);
}



/**
 * Compares an array of RAM/data space bytes to flash/program space memory.
 *
 * @param pgmAddress Even program space address to start verification at.
 *
 * @param *compareData Pointer to RAM/data space byte array containing the data
 *                     that was attempted to be written to flash.
 *
 * @param length Maximum number of bytes to compare
 *
 * @param status [out] Optional pointer to a EZBL_MISMATCH_STATUS structure to
 *        receive extended failure data in the event of a mismatch.
 *
 * @return 0x0000 on successful match, which may or may not have required
 *         adjustment.
 *
 *         Negative return codes for uncorrectable verification mismatches.
 *         -1: General failure with no exception allowed. Mismatch may be due to:
 *             - Attempting to overwrite a Bootloader reserved address that
 *               EZBL_WriteROM[Ex]() blocked.
 *             - Incorrect value in EZBL_NVMKey. See EZBL_WriteROM() API requirements.
 *             - Hardware write protect/Code protect settings
 *             - RAM corruption from an asynchronous ISR that isn't saving state or
 *               is causing incorrect concurrent execution of EZBL functions that
 *               require the same hardware.
 *             - Improper/unimplemented address
 *             - Bad voltage or other electricals/environmentals?
 *
 *         Positive return codes for verification mismatches of lessor
 *         significance, such as an expected failed attempt to overwrite a
 *         Bootloader reserved location.
 */
int EZBL_VerifyROM(unsigned long pgmAddress, const void *compareData, unsigned int length, EZBL_MISMATCH_STATUS *status)
{
    const unsigned char *rdPtr1, *rdPtr2;
    unsigned char rd1, rd2;
    const unsigned long *noVerifyRanges, *endOfNoVerifyRanges;
    unsigned long noVerifyStartAddr, noVerifyEndAddr;
    int ignore;

    // Dummy section generation with no data in it. This is necessary so we can
    // use __builtin_section_begin()/__builtin_section_end() against the named
    // section and not get a link error if there are no EZBL_SetNoVerifyRange()
    // macro calls anywhere in the project.
    __asm__("\n     .pushsection    .text.EZBLNoVerifyRanges, code, keep"
            "\n     .popsection");


    pgmAddress = pgmAddress | 0x80000000u;  // Force evaluation in the 0x9xxxxxxx kseg0 flash (RAM and SFR) ranges, not physical

    if(EZBL_RAMCompare((const void*)pgmAddress, compareData, length) == 0)
        return 0;

    rdPtr1 = (const unsigned char*)pgmAddress;
    rdPtr2 = (const unsigned char*)compareData;

    while(length--)
    {
        rd1 = *rdPtr1++;
        rd2 = *rdPtr2++;
        if(rd1 != rd2)
        {
            ignore = 0;
            noVerifyRanges = (const unsigned long*)__builtin_section_begin(".text.EZBLNoVerifyRanges");         // In the 0x9Dxxxxxx/0x9FCxxxxx kseg0 flash ranges
            endOfNoVerifyRanges = (const unsigned long*)__builtin_section_end(".text.EZBLNoVerifyRanges");
            while(noVerifyRanges != endOfNoVerifyRanges)
            {
                noVerifyStartAddr = *noVerifyRanges++;
                noVerifyEndAddr   = *noVerifyRanges++;
                if(((unsigned long)rdPtr1 > noVerifyStartAddr) && ((unsigned long)rdPtr1 <= noVerifyEndAddr))
                {
                    ignore = 1;
                    break;
                }
            }
            if(ignore)
                continue;

            if(status)
            {
                status->pgmAddress = (unsigned long)--rdPtr1;
                status->pgmData = (unsigned long)rd1;
                status->dataAddress = (void*)--rdPtr2;
                status->compareData = (unsigned long)rd2;
            }
            return -1;
        }
    }

    return 0;
}
