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
#pragma config WDTPS = PS1
#pragma config FWPSA = PR32
#pragma config WINDIS = OFF
#pragma config FWDTEN = OFF
#pragma config ICS = PGx1
#pragma config GWRP = OFF
#pragma config GCP = OFF
#pragma config JTAGEN = OFF

#pragma config POSCMOD = XT
#pragma config I2C1SEL = PRI
#pragma config IOL1WAY = OFF
#pragma config OSCIOFNC = ON
#pragma config FCKSM = CSECME
#pragma config FNOSC = PRIPLL
#pragma config PLL96MHZ = ON
#pragma config PLLDIV = DIV2
#pragma config IESO = OFF

#pragma config WPFP = WPFP0
#pragma config SOSCSEL = SOSC
#pragma config WUTSEL = LEG
#pragma config WPDIS = WPDIS
#pragma config WPCFG = WPCFGDIS
#pragma config WPEND = WPENDMEM

#pragma config DSWDTPS = DSWDTPS3
#pragma config DSWDTOSC = LPRC
#pragma config RTCOSC = SOSC
#pragma config DSBOREN = OFF
#pragma config DSWDTEN = OFF
 
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
    //On the PIC24FJ64GB004 Family of USB microcontrollers, the PLL will not power up and be enabled
    //by default, even if a PLL enabled oscillator configuration is selected (such as HS+PLL).
    //This allows the device to power up at a lower initial operating frequency, which can be
    //advantageous when powered from a source which is not gauranteed to be adequate for 32MHz
    //operation.  On these devices, user firmware needs to manually set the CLKDIV<PLLEN> bit to
    //power up the PLL.
    {
        unsigned int pll_startup_counter = 600;
        CLKDIVbits.PLLEN = 1;
        while(pll_startup_counter--);
    }


    // EZBL Bootloader items
    // Initialize NOW_*() time keeping API, including NOW_32() and NOW_TASK callbacks.
    NOW_Reset(TMR1, 16000000);

    // Define LED pins as GPIO outputs for LEDSet()/LEDToggle()/etc APIs
    EZBL_DefineLEDMap(RC5, RA8, RB2, RB3);      // Define LED pins in EZBL_LEDMap (MSb on left to LSb on right). This map is with respect to the LATx registers.
    LEDSet(0x00);   // Start extinguished
    _TRISB3 = 0;
    _TRISB2 = 0;
    _TRISA8 = 0;
    _TRISC5 = 0;

    // Define push buttons as GPIO inputs for ButtonRead()/ButtonPeek()/ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
    EZBL_DefineButtonMap(RA10, RA9, RA7);
    _TRISA7  = 1;
    _TRISA9  = 1;
    _TRISA10 = 1;
    ButtonRead();   // Get initial values for ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
}


/* Interrupt handler for USB host. */
void __attribute__((interrupt,auto_psv)) _USB1Interrupt()
{
    USB_HostInterruptHandler();
}