/*******************************************************************************
  Easy Bootloader Library

  Summary:
    Implements the EZBL_MapWrite() (and derived
    EZBL_MapRead()/EZBL_MapToggle()/EZBL_MapSet()/EZBL_MapClear()/etc.
    functions.
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

    .equ    zero, $0
    .equ    v0, $2
    .equ    v1, $3
    .equ    a0, $4
    .equ    a1, $5
    .equ    a2, $6
    .equ    a3, $7
    .equ    t2, $6
    .equ    t3, $7
    .equ    t0, $8
    .equ    t1, $9
    .equ    t2, $10
    .equ    t3, $11
    .equ    t4, $12
    .equ    t5, $13
    .equ    t6, $14
    .equ    t7, $15
    .equ    s0, $16
    .equ    s1, $17
    .equ    s2, $18
    .equ    s3, $19
    .equ    s4, $20
    .equ    s5, $21
    .equ    s6, $22
    .equ    s7, $23
    .equ    t8, $24
    .equ    t9, $25
    .equ    gp, $28
    .equ    sp, $29
    .equ    fp, $30
    .equ    ra, $31


/**
 * Writes bits from a bitfield to an array of mapped SFR/bit-offset
 * destinations.
 *
 * This function internally calls EZBL_MapInv() twice - first with 0x0000 as
 * the toggle value to read the existing state, then computes the needed toggle
 * values to set and clear all bits to match the supplied bitfield.
 *
 * @param writeBits Bitmask representing an array of IO states that should be
 *                  written to the IO mapping array, setting and clearing each
 *                  signal.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the bitValues,
 *                     index 2, is bit 1, etc.
 *
 * @param sfrAddrOffset Signed integer to add to each mappingArray SFR address.
 *                      Use this field to access related SFRs for the bitmap at
 *                      a known address offset while storing only one mapping
 *                      array for the hardware.
 *
 *                      For example, if the mappingArray was created against
 *                      LATx registers, and the TRISx register needs to be
 *                      read/toggled, specify sfrAddrOffset as:
 *                          (unsigned int)&TRISA - (unsigned int)&LATA
 *                      Note that TRISA comes before LATA on most device memory
 *                      maps, so the value computed will actually be a negative
 *                      number.
 *
 *                      Ensure the integral (unsigned int) cast is maintained
 *                      when referencing SFR pointers. Subtracting two pointers
 *                      directly yields a scaled quantity relative to the size
 *                      of the pointed to objects, which is not the address
 *                      offset.
 *
 * @return Bitmasked values for the IO map read before writing the new
 *         writeBits.
 */
# unsigned int EZBL_MapWriteEx(unsigned int writeBits, const unsigned int *mappingArray, int sfrAddrOffset);


/**
 * Writes bits from a bitfield to an array of mapped SFR/bit-offset
 * destinations.
 *
 * This function internally calls EZBL_MapInv() twice - first with 0x0000 as
 * the toggle value to read the existing state, then computes the needed toggle
 * values to set and clear all bits to match the supplied bitfield.
 *
 * @param writeBits Bitmask representing an array of IO states that should be
 *                  written to the IO mapping array, setting and clearing each
 *                  signal.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the bitValues,
 *                     index 2, is bit 1, etc.
 *
 * @return Bitmasked values for the IO map read before writing the new
 *         writeBits.
 */
# unsigned int EZBL_MapWrite(unsigned int writeBits, const unsigned int *mappingArray);


/**
 * Write's '0' values from an integer bitmask into a mapped array of SFR<bit>
 * destinations, leaving any SFR<bit> targets corresponding to '0' bitmask
 * values unchanged.
 *
 * This function internally calls EZBL_MapInv() twice - first with 0x0000 as
 * the toggle value to read the existing state, then computes the needed toggle
 * values to clr all '1' bits specified by the clrBits bitfield.
 *
 * @param clrBits Bitmasked bits that should be written as '0' to the mapping
 *                array, leaving mapping array bits corresponding to '0'
 *                bitfield bits unchanged.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the clrBits,
 *                     index 2, is bit 1, etc.
 *
 * @param sfrAddrOffset Signed integer to add to each mappingArray SFR address.
 *                      Use this field to access related SFRs for the bitmap at
 *                      a known address offset while storing only one mapping
 *                      array for the hardware.
 *
 *                      For example, if the mappingArray was created against
 *                      LATx registers, and the TRISx register needs to be
 *                      read/toggled, specify sfrAddrOffset as:
 *                          (unsigned int)&TRISA - (unsigned int)&LATA
 *                      Note that TRISA comes before LATA on most device memory
 *                      maps, so the value computed will actually be a negative
 *                      number.
 *
 *                      Ensure the integral (unsigned int) cast is maintained
 *                      when referencing SFR pointers. Subtracting two pointers
 *                      directly yields a scaled quantity relative to the size
 *                      of the pointed to objects, which is not the address
 *                      offset.
 *
 * @return Bitmasked values read from the mapped SFR<bit> registers before
 *         writing '0's according to clrBits.
 */
# unsigned int EZBL_MapClrEx(unsigned int clrBits, const unsigned int *mappingArray, int sfrAddrOffset);


/**
 * Write's '0' values from an integer bitmask into a mapped array of SFR<bit>
 * destinations, leaving any SFR<bit> targets corresponding to '0' bitmask
 * values unchanged.
 *
 * This function internally calls EZBL_MapInv() twice - first with 0x0000 as
 * the toggle value to read the existing state, then computes the needed toggle
 * values to clr all '1' bits specified by the clrBits bitfield.
 *
 * @param clrBits Bitmasked bits that should be written as '0' to the mapping
 *                array, leaving mapping array bits corresponding to '0'
 *                bitfield bits unchanged.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the clrBits,
 *                     index 2, is bit 1, etc.
 *
 * @return Bitmasked values read from the mapped SFR<bit> registers before
 *         writing '0's according to clrBits.
 */
# unsigned int EZBL_MapClr(unsigned int clrBits, const unsigned int *mappingArray);


/**
 * Write's '1' values from an integer bitmask into a mapped array of SFR<bit>
 * destinations, leaving any SFR<bit> targets corresponding to '0' bitmask
 * values unchanged.
 *
 * This function internally calls EZBL_MapInv() twice - first with 0x0000 as
 * the toggle value to read the existing state, then computes the needed toggle
 * values to set all '1' bits specified by the setBits bitfield.
 *
 * @param setBits Bitmasked bits that should be written as '1' to the mapping
 *                array, leaving mapping array bits corresponding to '0'
 *                bitfield bits unchanged.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the setBits,
 *                     index 2, is bit 1, etc.
 *
 * @param sfrAddrOffset Signed integer to add to each mappingArray SFR address.
 *                      Use this field to access related SFRs for the bitmap at
 *                      a known address offset while storing only one mapping
 *                      array for the hardware.
 *
 *                      For example, if the mappingArray was created against
 *                      LATx registers, and the TRISx register needs to be
 *                      read/toggled, specify sfrAddrOffset as:
 *                          (unsigned int)&TRISA - (unsigned int)&LATA
 *                      Note that TRISA comes before LATA on most device memory
 *                      maps, so the value computed will actually be a negative
 *                      number.
 *
 *                      Ensure the integral (unsigned int) cast is maintained
 *                      when referencing SFR pointers. Subtracting two pointers
 *                      directly yields a scaled quantity relative to the size
 *                      of the pointed to objects, which is not the address
 *                      offset.
 *
 * @return Bitmasked values read from the mapped SFR<bit> registers before
 *         writing '1's according to setBits.
 */
# unsigned int EZBL_MapSetEx(unsigned int setBits, const unsigned int *mappingArray, int sfrAddrOffset);


/**
 * Write's '1' values from an integer bitmask into a mapped array of SFR<bit>
 * destinations, leaving any SFR<bit> targets corresponding to '0' bitmask
 * values unchanged.
 *
 * This function internally calls EZBL_MapInv() twice - first with 0x0000 as
 * the toggle value to read the existing state, then computes the needed toggle
 * values to set all '1' bits specified by the setBits bitfield.
 *
 * @param setBits Bitmasked bits that should be written as '1' to the mapping
 *                array, leaving mapping array bits corresponding to '0'
 *                bitfield bits unchanged.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the setBits,
 *                     index 2, is bit 1, etc.
 *
 * @return Bitmasked values read from the mapped SFR<bit> registers before
 *         writing '1's according to setBits.
 */
# unsigned int EZBL_MapSet(unsigned int setBits, const unsigned int *mappingArray);


/**
 * Inverts (toggles) the state of an array of mapped SFR<bit> destinations using
 * a logical XOR of the invertMask provided. I.e. only '1' bits toggle a mapped
 * SFR state. '0' bits have no effect on the corresponding SFR<bit> value.
 *
 * This function is identical to EZBL_MapInv(), but includes an extra
 * sfrAddrOffset parameter for reading/writing to related SFR<bit> destinations
 * located at a fixed address offset from their original SFR address in the map.
 *
 * @param invertMask A bitmask representing an array of ordered bits who's state
 *                   should be toggled. Each '1' bit in the bit field toggles
 *                   the corresponding bit in the arbitrary SFR map. Unmapped
 *                   high order bits are ignored.
 *
 *                   Specify a invertMask of 0x0 to read the bit map without
 *                   writing/altering existing state.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the invertMask,
 *                     index 2, is bit 1, etc.
 *
 * @param sfrAddrOffset Signed integer to add to each mappingArray SFR address.
 *                      Use this field to access related SFRs for the bitmap at
 *                      a known address offset while storing only one mapping
 *                      array for the hardware.
 *
 *                      For example, if the mappingArray was created against
 *                      LATx registers, and the TRISx register needs to be
 *                      read/toggled, specify sfrAddrOffset as:
 *                          (unsigned int)&TRISA - (unsigned int)&LATA
 *                      Note that TRISA comes before LATA on most device memory
 *                      maps, so the value computed will actually be a negative
 *                      number.
 *
 *                      Ensure the integral (unsigned int) cast is maintained
 *                      when referencing SFR pointers. Subtracting two pointers
 *                      directly yields a scaled quantity relative to the size
 *                      of the pointed to objects, which is not the address
 *                      offset.
 *
 * @return Bitmask representing the mapped SFR bit values read before issuing
 *         any toggle write backs. Higher order bits that are not specified in
 *         the mapping array are returned as '0'.
 */
# unsigned int EZBL_MapInvEx(unsigned int invertMask, const unsigned int *mappingArray, int sfrAddrOffset);


/**
 * Inverts (toggles) the state of an array of mapped SFR<bit> destinations using
 * a logical XOR of the invertMask provided. I.e. only '1' bits toggle a mapped
 * SFR state. '0' bits have no effect on the corresponding SFR<bit> value.
 *
 * @param invertMask A bitmask representing an array of ordered bits who's state
 *                   should be toggled. Each '1' bit in the bit field toggles
 *                   the corresponding bit in the arbitrary SFR map. Unmapped
 *                   high order bits are ignored.
 *
 *                   Specify a invertMask of 0x0 to read the bit map without
 *                   writing/altering existing state.
 *
 * @param mappingArray Pointer to a (unsigned int) array. Index 0 must contain
 *                     a count of how many bit mappings follow in the array.
 *
 *                     Index 1 up to 16 shall encode SFR addresses and bit
 *                     positions to map to. Bits <15:12> encode the bit position
 *                     while bits <11:0> encode the SFR target address that the
 *                     bit applies to.
 *
 *                     Index 1 corresponds to bit 0 (LSbit) of the invertMask,
 *                     index 2, is bit 1, etc.
 *
 * @return Bitmask representing the mapped SFR bit values read before issuing
 *         any toggle write backs. Higher order bits that are not specified in
 *         the mapping array are returned as '0'.
 */
# unsigned int EZBL_MapInv(unsigned int invertMask, const unsigned int *mappingArray);




    .pushsection    .text.EZBL_Map, code
    .global         EZBL_MapWriteEx
    .global         EZBL_MapWrite
    .global         EZBL_MapClrEx
    .global         EZBL_MapClr
    .global         EZBL_MapSetEx
    .global         EZBL_MapSet
    .global         EZBL_MapInvEx
    .global         EZBL_MapInv
    .type           EZBL_MapWriteEx, @function
    .type           EZBL_MapWrite, @function
    .type           EZBL_MapClrEx, @function
    .type           EZBL_MapClr, @function
    .type           EZBL_MapSetEx, @function
    .type           EZBL_MapSet, @function
    .type           EZBL_MapInvEx, @function
    .type           EZBL_MapInv, @function
    .set            noreorder
    .set            micromips


EZBL_MapWriteEx:        # unsigned int EZBL_MapWriteEx(unsigned int writeBits, const unsigned int *mappingArray, int sfrAddrOffset);
    b       _mapXEx
    li      v1, 0x0
EZBL_MapWrite:          # unsigned int EZBL_MapWrite(unsigned int writeBits, const unsigned int *mappingArray);
    b       _mapX
    li      v1, 0x0
EZBL_MapClrEx:          # unsigned int EZBL_MapClrEx(unsigned int clrBits, const unsigned int *mappingArray, int sfrAddrOffset);
    b       _mapXEx
    li      v1, 0x4
EZBL_MapClr:            # unsigned int EZBL_MapClr(unsigned int clrBits, const unsigned int *mappingArray);
    b       _mapX
    li      v1, 0x4
EZBL_MapSetEx:          # unsigned int EZBL_MapSetEx(unsigned int setBits, const unsigned int *mappingArray, int sfrAddrOffset);
    b       _mapXEx
    li      v1, 0x8
EZBL_MapSet:            # unsigned int EZBL_MapSet(unsigned int setBits, const unsigned int *mappingArray);
    b       _mapX
    li      v1, 0x8
EZBL_MapInvEx:          # unsigned int EZBL_MapInvEx(unsigned int invertMask, const unsigned int *mappingArray, int sfrAddrOffset);
    b       _mapXEx
    li      v1, 0xC
EZBL_MapInv:            # unsigned int EZBL_MapInv(unsigned int invertMask, const unsigned int *mappingArray);
    li      v1, 0xC

_mapX:
    li      a2, 0

_mapXEx:                    # v0 = unsigned int _mapXEx(a0 = unsigned int write_clr_set_inv_bitmask, t7 = const unsigned int *mappingArray, v1 = write_clr_set_inv_offset_index, a2 = int sfrAddrOffset);
    beqz    a1, _done       # Abort if mappingArray == null, returning 0x00000000
    move    t3, zero        # t3 = read back return word accumulator = 0x00000000
    move    t7, a1
    lw      t0, 0(a1)       # t0 = mapLength
    move    t5, zero        # t5 = index counter
    b       2f
    li      t2, 0x1         # t2 = bit value to add to read result
1:  addiu   t5, t5, 1       # index++;
    lwxs    a3, t5(t7)      # a3 = *mappingArray
    ext     v0, a3, 17, 5   # v0 = (*mappingArray & 0x003E0000)>>17 = bitNum for SFR target
    ins     a3, zero, 17, 5 # a3 = (*mappingArray & 0xFFC1FFFF) = SFR target address
    addu    a3, a3, a2      # a3 += sfrAddrOffset
    lw      a1, 0(a3)       # read a1 = *((mappingArray[index] & 0xFFC1FFFFL) + sfrAddrOffset) = map target address read data
    sllv    v0, t2, v0      # Generate (1<<bitNum)
    and     a1, a1, v0      # a1 = (map read data & (1<<bitNum))
    beqz    a1, _writeBack  # read a '0', proceed to write stage
    and     t6, a0, t4      # t6 = write_clr_set_inv_bitmask & (0x1<<index); the write bit test value
    addu    t3, t3, t4      # record a '1' read

_writeBack:
    beqz    v1, _computeCLRorSETOffset  # if(v1 == 0) Doing all-bit write, so identify if the offset should target the CLR or the SET SFR
    addu    a3, a3, v1      # a3 = mapTargetAddr + [0x0/0x4/0x8/0xC]
    movz    v0, zero, t6    # v0 = (write_clr_set_inv_bitmask & (0x1<<index)) ? (1<<bitNum) : 0;
    sw      v0, 0(a3)

2:  bne     t5, t0, 1b      # if(index != mapLenth) repeat loop
    sllv    t4, t2, t5      # t4 = (1<<index)   ; position the read back output bitmask

 _done:
    jr      ra
    move    v0, t3

_computeCLRorSETOffset:
    li      a1, 4
    movz    a1, zero, t6    # a1 = (write_clr_set_inv_bitmask & (0x1<<index)) ? 4 : 0;
    addu    a3, a3, a1      # a3 = mapTargetAddr + ((write_clr_set_inv_bitmask & (0x1<<index)) ? 4 : 0);
    b       2b
    sw      v0, 4(a3)


    .size   EZBL_MapWriteEx, . - EZBL_MapWriteEx
    .size   EZBL_MapWrite, . - EZBL_MapWrite
    .size   EZBL_MapSetEx, . - EZBL_MapSetEx
    .size   EZBL_MapSet, . - EZBL_MapSet
    .size   EZBL_MapClrEx, . - EZBL_MapClrEx
    .size   EZBL_MapClr, . - EZBL_MapClr
    .size   EZBL_MapInvEx, . - EZBL_MapInvEx
    .size   EZBL_MapInv, . - EZBL_MapInv
    .popsection
