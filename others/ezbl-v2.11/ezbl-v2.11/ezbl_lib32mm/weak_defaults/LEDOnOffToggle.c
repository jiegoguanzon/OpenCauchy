/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM Library API Source File

  Summary:
    Implements the LEDSet(), LEDOn(), LEDOff() and LEDToggle() functions against
    the EZBL_LEDMap[] array created with the EZBL_DefineLEDMap() macro.
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

extern const unsigned int EZBL_LEDMap[];
unsigned int EZBL_MapWrite(unsigned int writeBitValues, const unsigned int *mappingArray);
unsigned int EZBL_MapClr(unsigned int clearBitMask, const unsigned int *mappingArray);
unsigned int EZBL_MapSet(unsigned int setBitMask, const unsigned int *mappingArray);
unsigned int EZBL_MapInv(unsigned int toggleBitMask, const unsigned int *mappingArray);


/**
 * Turns on or off all LEDs according to the supplied bitfield. Use the
 * EZBL_DefineLEDMap() macro to define which ports/port bits each LED is located
 * at.
 *
 * @param ledBitField Bitmask representing an array of LEDs that should be
 *                    turned on and off. Each '1' bit in the bit field turns
 *                    the corresponding LED on while each '0' turns the LED
 *                    off.
 *
 * @return Bitmask representing the LEDs that were previously on before setting
 *         the new value. Bits for LEDs that do not exist are returned as '0'
 *         values in the bitmask.
 */
unsigned int LEDSet(unsigned int writeBitMask)
{
    return EZBL_MapWrite(writeBitMask, EZBL_LEDMap);
}


/**
 * Turns off an array of LEDs using a logical NOT-AND of the ledBitField
 * provided. I.e. only '1' bits turn LEDs off, '0' bits have no effect.
 *
 * @param extinguishBitMask Bitmask representing an array of LEDs that should be
 *                    turned off. Each '1' bit in the bit field turns the
 *                    corresponding LED off.
 *
 * @return Bitmask representing the LEDs that were previously off before
 *         NOT-ANDing in the ledBitField value. Bits for LEDs that do not exist
 *         are returned as '1' values in the bitmask.
 */
unsigned int LEDOff(unsigned int extinguishBitMask)
{
    return ~EZBL_MapClr(extinguishBitMask, EZBL_LEDMap);
}


/**
 * Turns on an array of LEDs using a logical OR of the ledBitField provided.
 * I.e. only '1' bits turn on LEDs, '0' bits have no effect.
 *
 * @param illuminateBitMask Bitmask representing an array of LEDs that should be
 *                    turned on. Each '1' bit in the bit field turns the
 *                    corresponding LED on.
 *
 * @return Bitmask representing the LEDs that were previously on before ORing
 *         in the ledBitField value. Bits for LEDs that do not exist are
 *         returned as '0' values in the bitmask.
 */
unsigned int LEDOn(unsigned int illuminateBitMask)
{
    return EZBL_MapSet(illuminateBitMask, EZBL_LEDMap);
}


/**
 * Toggles the state of an array of LEDs using a logical XOR of the ledBitField
 * provided. '1' inverts present LED state from off to illuminated or
 * illuminated to off, '0' retains existing LED state.
 *
 * @param toggleBitField Bitmask of LEDs that should be toggled.
 *
 * @return Bitmask representing the LEDs that were previously illuminated
 *         before calling LEDToggle(). Bits for LEDs that do not exist are
 *         returned as '0'.
 */
unsigned int LEDToggle(unsigned int toggleBitMask)
{
    return EZBL_MapInv(toggleBitMask, EZBL_LEDMap);
}
