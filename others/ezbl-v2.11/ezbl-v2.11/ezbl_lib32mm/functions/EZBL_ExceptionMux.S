    #include <xc.h>



/**
                        //    BEV = Bootstrap exception vectors, R/W-1; XC32 sets to 0 to use EBASE relative vectors
                                  //    EXL = EXception Level, R/W-x; 0 = Normal, 1 = Exception (+kernel mode/Interrupts disabled)
                                            //   IV = Interrupt Exception use General Exception or Special Vector, R/W-x; 0 = Interrupts always go to General Exception Vector (0xBFC0_0181), 1 = Interrupts use the Interrupt Vector Table (EBASE + 0x201 + (Vector Spacing)*IRQ)
                                                    // EJTAGProbEn = Hardware debugger probe (REAL ICE/ICD/PICkit/eJTAG) attached
 * Exception            StatusBEV StatusEXL CauseIV Hardware Vector
 * - Reset and NMI      x         x         x       0xBFC0_0001
 * - EJTAG Debug        x         x         x       0xBFC0_0481 (EJTAGProbEn==0)   or 0xFF20_0201 (EJTAGProbEn==1, dseg1 address off chip)
 * - Interrupts         0         0         0       EBASE + 0x181                           (Normal Single Interrupt vector mode - note: This is the General Exception Vector too)
 *                      0         0         1       EBASE + 0x201 + (Vector Spacing)*IRQ    (Normal Multivector Interrupt mode)
 *                      1         0         0       0xBFC0_0381                             (Bootstrap Single Interrupt vector mode - note: This is the General Exception vector too)
 *                      1         0         1       0xBFC0_0401                             (Bootstrap Special Single Interrupt vector mode)

 // These can be ignored since they are not applicable or duplicated for general interrupts
 * - General Exception  0         x         x       EBASE + 0x181                           (Normal Everything else)
 *                      1         x         x       0xBFC0_0381                             (Bootstrap Everything else)
 * - SRAM Parity Error  1         x         x       0xBFC0_0301 (Bootstrap) (NOTE: Not a possible vector in PIC32MM parts)
                        0                           EBASE|0x101 (Normal)    (NOTE: Not a possible vector in PIC32MM parts)
 */
// Assumes IntCtlVS = 0x00 (Interrupt Vector Spacing == 0x00, applicable to single vector interrupt mode)

EZBL_BEV_EXCPT_EBASE_OFFSET  = 0x0140;
EZBL_DBG_EXCPT_EBASE_OFFSET  = 0x0160;
EZBL_RESET_EBASE_OFFSET      = 0x0800;


    // Reset/NMI Exception Vector (must be located @ 0xBFC00000 when compiled as a Bootloader)
    .pushsection .EZBL_VectorDispatch0000, code, address(0xBFC00000), keep
    .extern _RESET_ADDR
    .extern EZBL_RESET_EBASE_OFFSET
    .global EZBL_VectorDispatch0000
    .type   EZBL_VectorDispatch0000, @function
EZBL_VectorDispatch0000:
    mfc0    k0, _CP0_STATUS // Get Status as handler will need to decode EXL bit to determine NMI versus reset
    ext     k0, k0, _CP0_STATUS_NMI_POSITION, 1
    bnezc   k0, 1f
    lui     k0, %hi(_RESET_ADDR)                // For device Reset, always jump to _RESET_ADDR, which is where the XC32 crt for the Bootloader is in Boot Flash
    ori     k0, %lo(_RESET_ADDR) + 0x1
    jrc     k0
1:  mfc0    k0, _CP0_EBASE                      // For NMIs, jump to (Ebase + 0x800), which can be either in the Bootloader or the Application, depending on what Ebase has been set to in CP0
    addiu   k0, EZBL_RESET_EBASE_OFFSET + 0x1   // Add 0x1 to select microMIPS ISA target
    ins     k0, k0, 25, 1   // Set bit 25 of k0 to force the address into an equivalent kseg1 uncached range (if not already a kseg1 number)
    jr      k0
    mfc0    k0, _CP0_STATUS // Get Status as handler will need to decode EXL bit to determine NMI versus reset
    .size   EZBL_VectorDispatch0000, . - EZBL_VectorDispatch0000
    .popsection

//    // SRAM Parity Error (Cache) Exception Vector (not applicable on PIC32MM products; will never happen)
//    .pushsection .ezbl_VectorDispatch0100, code, address(0xBFC00100), keep
//    .extern EZBL_BEV_EXCPT_EBASE_OFFSET
//    .global EZBL_VectorDispatch0100
//EZBL_VectorDispatch0100:
//    mfc0    k0, _CP0_EBASE
//    addiu   k0, EZBL_BEV_EXCPT_EBASE_OFFSET + 0x1  // Add 0x1 to select microMIPS ISA target
//    ins     k0, k0, 25, 1   // Set bit 25 of k0 to force the address into an equivalent kseg1 uncached range (if not already a kseg1 number)
//    jr      k0
//    mfc0    k0, _CP0_STATUS // Get Status as handler will need to decode EXL bit to determine NMI versus reset
//    .popsection



    // Bootstrap General Exception and Single Interrupt vector (used when BEV = 1, EXL = 0, CauseIV = 0)
    // Needs to be forwarded to the Application if the Application also wants a Bootstrap mode
    .pushsection .EZBL_VectorDispatch0380, code, address(0xBFC00380), keep
    .extern EZBL_BEV_EXCPT_EBASE_OFFSET
    .global EZBL_VectorDispatch0380
    .type   EZBL_VectorDispatch0380, @function
EZBL_VectorDispatch0380:
    mfc0    k0, _CP0_EBASE
    addiu   k0, EZBL_BEV_EXCPT_EBASE_OFFSET + 0x1  // Add 0x1 to select microMIPS ISA target
    ins     k0, k0, 25, 1   // Set bit 25 of k0 to force the address into an equivalent kseg1 uncached range (if not already a kseg1 number)
    jr      k0
    mfc0    k0, _CP0_CAUSE
    .popsection

    // Bootstrap Special Interrupt Vector - probably still Single Interrupt mode and not using IRQ offsets
    // Needs to be forwarded to the Application if the Application also wants a Bootstrap mode
    .pushsection .EZBL_VectorDispatch0400, code, address(0xBFC00400), keep
    .extern EZBL_BOOTSTRAP_SPECIAL_VECTOR_EBASE_OFFSET
    .global EZBL_VectorDispatch0400
    .type   EZBL_VectorDispatch0400, @function
EZBL_VectorDispatch0400:
    mfc0    k0, _CP0_EBASE
    addiu   k0, _MVEC_INT_BASE_ADDR + 0x1   // Add 0x1 to select microMIPS ISA target
    ins     k0, k0, 25, 1                   // Set bit 25 of k0 to force the address into an equivalent kseg1 uncached range (if not already a kseg1 number)
    jr      k0
    mfc0    k0, _CP0_CAUSE
    .popsection


    // EJTAG Debug Exception Vector (not EBASE relative: located @ 0xBFC00480, can't be moved except via EJTAGProbEn, which puts it at 0xFF200200 in desg1 space off chip)
    .pushsection .EZBL_VectorDispatch0480, code, address(0xBFC00480), keep
    .extern EZBL_DBG_EXCPT_EBASE_OFFSET
    .global EZBL_VectorDispatch0480
    .type   EZBL_VectorDispatch0480, @function
EZBL_VectorDispatch0480:
    mtc0    k1, _CP0_DESAVE                         // Save $k1 register in DeSave CP0 register
    mfc0    k1, _CP0_EBASE
    addiu   k1, EZBL_DBG_EXCPT_EBASE_OFFSET + 0x1   // Add 0x1 to select microMIPS ISA target
    jr      k1
    mfc0    k1, _CP0_DESAVE                         // Restore $k1
    .popsection
