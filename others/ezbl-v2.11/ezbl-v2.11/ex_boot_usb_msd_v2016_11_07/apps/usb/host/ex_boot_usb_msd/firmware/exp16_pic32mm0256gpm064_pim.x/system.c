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


/** CONFIGURATION Bits **********************************************/
// PIC32MM0256GPM064 Configuration Bit Settings
// FDEVOPT
#pragma config SOSCHP = ON              // Secondary Oscillator High Power Enable bit (SOSC oprerates in high power mode)
#pragma config ALTI2C = OFF             // Alternate I2C1 Pins Location Enable bit (Primary I2C1 pins are used)
#pragma config FUSBIDIO = ON            // USBID pin control (USBID pin is controlled by the port function)
#pragma config FVBUSIO = OFF            // VBUS Pin Control (VBUS pin is controlled by the USB module)
#pragma config USERID = 0xFFFF          // User ID bits (User ID bits)

// FICD
#pragma config JTAGEN = OFF             // JTAG Enable bit (JTAG is disabled)
#pragma config ICS = PGx2               // ICE/ICD Communication Channel Selection bits (Communicate on PGEC2/PGED2)

// FPOR
#pragma config BOREN = BOR2             // Brown-out Reset Enable bits (Brown-out Reset enabled only while device active and disabled in Sleep; SBOREN bit disabled)
#pragma config RETVR = ON               // Retention Voltage Regulator Enable bit (Retention regulator is enabled and controlled by RETEN bit during sleep)
#pragma config LPBOREN = ON             // Downside Voltage Protection Enable bit (Low power BOR is enabled, when main BOR is disabled)

// FWDT
#pragma config SWDTPS = PS1024          // Sleep Mode Watchdog Timer Postscale Selection bits (1:1024 ~= 1 second)
#pragma config FWDTWINSZ = PS75_0       // Watchdog Timer Window Size bits (Watchdog timer window size is 75%)
#pragma config WINDIS = OFF             // Windowed Watchdog Timer Disable bit (Watchdog timer is in non-window mode)
#pragma config RWDTPS = PS512           // Run Mode Watchdog Timer Postscale Selection bits (1:512 ~= 500 ms)
#pragma config RCLKSEL = LPRC           // Run Mode Watchdog Timer Clock Source Selection bits (Clock source is LPRC (same as for sleep mode))
#pragma config FWDTEN = OFF             // Watchdog Timer Enable bit (WDT is disabled, can be enabled in software)

// FOSCSEL
#pragma config FNOSC = PLL              // Oscillator Selection bits (Primary or FRC oscillator with PLL)
#pragma config PLLSRC = FRC             // System PLL Input Clock Selection bit (FRC oscillator is selected as PLL reference input on device reset)
#pragma config SOSCEN = ON              // Secondary Oscillator Enable bit (Secondary oscillator (SOSC) is enabled)
#pragma config IESO = ON                // Two Speed Startup Enable bit (Two speed startup is enabled)
#pragma config POSCMOD = HS             // Primary Oscillator Selection bit (HS oscillator mode is selected)
#pragma config OSCIOFNC = OFF           // System Clock on CLKO Pin Enable bit (OSCO pin operates as a normal I/O)
#pragma config SOSCSEL = OFF            // Secondary Oscillator External Clock Enable bit (Crystal is used (RA4 and RB4 are controlled by SOSC))
#pragma config FCKSM = CSECME           // Clock Switching and Fail-Safe Clock Monitor Enable bits (Clock switching is enabled; Fail-safe clock monitor is enabled)

// FSEC
#pragma config CP = OFF                 // Code Protection Enable bit (Code protection is disabled)

// #pragma config statements should precede project file includes.
// Use project enums instead of #define for ON and OFF.

#include <xc.h>
#include "usb.h"
#include <sys/attribs.h>
#include "../ezbl_integration/ezbl.h"


static void SYSTEM_SetOscToUSBCompatible(void);



/*********************************************************************
* Function: void SYS_Initialize( void )
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
    SYSTEM_SetOscToUSBCompatible();
    __builtin_enable_interrupts();


    // EZBL Bootloader items
    // Initialize NOW_*() time keeping API, including NOW_32() and NOW_TASK callbacks.
    NOW_Reset(CORETMR, 24000000);

    // Define LED pins as GPIO outputs for LEDSet()/LEDToggle()/etc APIs
    EZBL_DefineLEDMap(RC13, RB14, RD4, RD3, RD2, RD1, RD0); // Use LEDSet()/LEDOn()/LEDOff()/LEDToggle() to access this bit mapping
    LEDSet(0x00);   // Start extinguished
    EZBL_MapClrEx(0xFFFF, EZBL_LEDMap, (int)&TRISA - (int)&LATA);  // Write TRISxCLR = 1<<y; for all bits defined in the LED Map
    ANSELBCLR = 1u<<14;

    // Define push buttons as GPIO inputs for ButtonRead()/ButtonPeek()/ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled
    EZBL_DefineButtonMap(RB9, RC3, RC4);
    ButtonRead();   // Get initial values for ButtonsLastState/ButtonsPushed/ButtonsReleased/ButtonsToggled

    
//    // Function             Explorer 16/32          PIC32MM0256GPM064 Device Pins
//    // MCP2221A UART 1      PIM#, Net               PIC#, name
//    // U1TX (PIC out)         50, P50_TXB             40, U1TX/RC12
//    // U1RX (PIC in)          49, P49_RXB             10, AN19/U1RX/RA6
//    CNPUASET = _CNPUA_CNPUA6_MASK;
//    ANSELACLR = (1u<<6);
//    UART_Reset(1, NOW_Fcy, -230400, 1);   // Use EZBL_FIFORead*()/EZBL_FIFOPeek*()/EZBL_FIFOWrite*() and &UART1_RxFifo/&UART1_TxFifo to read/write. printf()/EZBL_printf()/EZBL_STDOUT/EZBL_STDIN will also map to this when useForStdio == 1.
}

/*********************************************************************
* Function: static void SYSTEM_SetOscToUSBCompatible(void)
*
* Overview: Configures the PIC32MM0256GPM064 PIM on the Explorer 16/32 to run 
*     in PRI+SPLL at 24MHz CPU, 48MHz USB module clock frequencies, both 
*     derived from the 8MHz crystal on the demo board.
*
* PreCondition: None
*
* Input:  None
*
* Output: None
*
********************************************************************/
static void SYSTEM_SetOscToUSBCompatible(void)
{
    // Configure REFO to request POSC
    REFO1CONbits.ROSEL = 2; // POSC = 2
    REFO1CONbits.OE = 0;    // Disable REFO pin output
    REFO1CONbits.ON = 1;    // Enable REFO module to begin warming up POSC

    // Wait for POSC to warm up and reach a stable clock amplitude
    // Required delay may vary depending on different application conditions
    // such as voltage, temperature, layout, XT or HS mode and components
    {
        unsigned int start = __builtin_mfc0(_CP0_COUNT, _CP0_COUNT_SELECT);   
        while((__builtin_mfc0(_CP0_COUNT, _CP0_COUNT_SELECT)) - start < (unsigned int)(0.009*8000000/2));   // Delay ~9ms
    }
    // Unlock OSCCON to clock switch to POSC + PLL
    SYSKEY = 0;
    SYSKEY = 0xAA996655;
    SYSKEY = 0x556699AA;
    
    // Configure the PLL to run from the POSC (8MHz on Explorer 16/32) and output 24MHz for the CPU + Peripheral Bus (and 48MHz for USB)
    SPLLCON = 0x02050000;       // PLLODIV = /4, PLLMULT = 12x, PLL source = POSC, so: 8MHz FRC * 12x / 4 = 24MHz CPU and peripheral bus frequency.
    
    // Switch to POSC = 2 (PLL)
    OSCCONCLR = _OSCCON_NOSC_MASK | _OSCCON_CLKLOCK_MASK | _OSCCON_OSWEN_MASK;
    OSCCONSET = (1<<_OSCCON_NOSC_POSITION) | _OSCCON_OSWEN_MASK;
    while(OSCCONbits.OSWEN);    // Wait for switch to complete (OSWEN self clears)
    
    // REFO module doesn't need to request POSC anymore since CPU is requesting it. However, keeping it enabled can keep the clock warm in sleep mode, so this is commented out.
    //REFO1CONbits.ON = 0;    // Enable REFO module to begin warming up POSC
}

/* Interrupt handler for USB host. */
void __ISR(_USB_VECTOR) _USB1Interrupt()
{
    USB_HostInterruptHandler();
}

