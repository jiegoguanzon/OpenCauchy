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
#pragma config FWDTEN = OFF
#pragma config ICS = PGx2
#pragma config GWRP = OFF
#pragma config GCP = OFF
#pragma config JTAGEN = OFF

#pragma config POSCMOD = XT
#pragma config IOL1WAY = OFF
#pragma config OSCIOFNC = ON
#pragma config FCKSM = CSECME
#pragma config FNOSC = PRIPLL
#pragma config PLL96MHZ = ON
#pragma config PLLDIV = DIV2
#pragma config IESO = OFF
 
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
    EZBL_DefineLEDMap(RA7, RB5);
    LEDSet(0x00);   // Start extinguished
    _TRISA7 = 0;
    _TRISB5 = 0;
    _ANSA7 = 0;
    _ANSB5 = 0;

    // Define push buttons as GPIO inputs for ButtonRead()/ButtonPeek()/ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
    EZBL_DefineButtonMap(RE9, RG8);
    _TRISE9 = 1;
    _TRISG8 = 1;
    _ANSE9  = 0;
    _ANSG8  = 0; 
    ButtonRead();   // Get initial values for ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
}


/* Interrupt handler for USB host. */
void __attribute__((interrupt,auto_psv)) _USB1Interrupt()
{
    USB_HostInterruptHandler();
}
