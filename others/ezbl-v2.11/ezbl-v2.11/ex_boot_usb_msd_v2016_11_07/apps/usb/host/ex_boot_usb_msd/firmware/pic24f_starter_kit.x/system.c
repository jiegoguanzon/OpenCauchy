/*******************************************************************************
Copyright 2016 Microchip Technology Inc. (www.microchip.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

To request to license the code under the MLA license (www.microchip.com/mla_license), 
please contact mla_licensing@microchip.com
*******************************************************************************/

#include <xc.h>
#include "usb.h"
#include "../ezbl_integration/ezbl.h"

/** CONFIGURATION Bits **********************************************/
#pragma config JTAGEN = OFF
#pragma config GCP = OFF
#pragma config GWRP = OFF
#pragma config FWDTEN = OFF
#pragma config ICS = PGx2

#pragma config PLL_96MHZ = ON
#pragma config IESO = OFF
#pragma config FCKSM = CSECME
#pragma config OSCIOFNC = ON
#pragma config POSCMOD = XT
#pragma config FNOSC = PRIPLL
#pragma config PLLDIV = DIV3
#pragma config IOL1WAY = OFF
 
/*********************************************************************
* Function: void SYS_Initialize(void)
*
* Overview: Initializes the system.
*
* PreCondition: None
*
* Input:  None
*
* Output: None
*
********************************************************************/
void SYS_Initialize()
{
    // EZBL Bootloader items
    // Initialize NOW_*() time keeping API, including NOW_32() and NOW_TASK callbacks.
    NOW_Reset(TMR1, 16000000);

    // Define LED pins as GPIO outputs for LEDSet()/LEDToggle()/etc APIs
    EZBL_DefineLEDMap(RF4, RG6, RG8);
    LEDSet(0x00);   // Start extinguished
    _TRISG8 = 0;
    _TRISG6 = 0;
    _TRISF4 = 0;

    // Define push buttons as GPIO inputs for ButtonRead()/ButtonPeek()/ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
    EZBL_DefineButtonMap(); // No buttons
    ButtonRead();           // Get initial values for ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
}


/* Interrupt handler for USB host. */
void __attribute__((interrupt,auto_psv)) _USB1Interrupt()
{
    USB_HostInterruptHandler();
}