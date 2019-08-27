/*
 * File:   pic32mm0256gpl064_explorer_16.c
 *
 * Created on 2016 November 07
 *
 * Initializes the device Configuration words, system clock frequency, UARTs, 
 * LEDs, buttons, NOW timer module on the PIC32 MIPS Core Timer, and exposes
 * some common APIs for reading buttons and manipulating LEDs.
 * 
 * This file is intended to be used on the PIC32MM0256GPM064 PIM for the
 * Explorer 16 (and Explorer 16/32) hardware, but is also the recommended
 * starting basis for any project implementing any of the PIC32MM0256GPM064
 * family devices.
 *
 * Reference (CTRL + Click on link):
 *     PIC32MM0256GPM064 General Purpose PIM (Plug-in-Module)
 *     http://www.microchip.com/MA320023
 *     http://ww1.microchip.com/downloads/en/DeviceDoc/50002567a%20PIC32MM0256GPM064%20Plug-In%20Module%20(PIM)%20Information%20Sheet.pdf (Info Sheet)
 *
 *     Explorer 16/32 Development Board
 *     http://www.microchip.com/explorer1632
 *     http://ww1.microchip.com/downloads/en/DeviceDoc/Explorer_16_32_Schematics_R6_3.pdf (Schematic)
 *     http://ww1.microchip.com/downloads/en/DeviceDoc/Explorer_16_32_BillOfMaterials_R6_3.pdf (Bill of Materials)
 *     http://microchip.wikidot.com/boards:explorer1632 (User's Guide)
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

#include <xc.h>
#if defined(__PIC32MM) && (__PIC32_FEATURE_SET0 == 'G') && (__PIC32_FEATURE_SET1 == 'P') && (__PIC32_PRODUCT_GROUP == 'M')   // PIC32MM0256GPM064 Family devices

#include "../ezbl_integration/ezbl.h"


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
#pragma config POSCMOD = XT             // Primary Oscillator Selection bit (XT oscillator mode is selected)
#pragma config OSCIOFNC = OFF           // System Clock on CLKO Pin Enable bit (OSCO pin operates as a normal I/O)
#pragma config SOSCSEL = OFF            // Secondary Oscillator External Clock Enable bit (Crystal is used (RA4 and RB4 are controlled by SOSC))
#pragma config FCKSM = CSECME           // Clock Switching and Fail-Safe Clock Monitor Enable bits (Clock switching is enabled; Fail-safe clock monitor is enabled)

// FSEC
#pragma config CP = OFF                 // Code Protection Enable bit (Code protection is disabled)


// Device clock (SYSCLK, PBCLK/UPBCLK, all the same on PIC32MM devices)
#define FCY         24000000ul


EZBL_FIFO *EZBL_COMBootIF __attribute__((persistent));  // Pointer to RX FIFO to check activity on for bootloading
const long EZBL_COMBaud = -230400;                      // Communications baud rate: <= 0 means auto-baud with default value (for TX) set by the 2's complemented value; otherwise baud in bits/sec (ex: 460800)


/**
 * Initializes interrupt controller, NOW software timing module, UART and other 
 * I/O pins
 */
unsigned long InitializeBoard(void)
{
    __builtin_enable_interrupts();

    // Initialize NOW module (using PIC32 Core Timer)
    NOW_Reset(CORETMR, FCY);    // Using 8.0MHz FRC with 3xPLL

    // Initialize I/O pins/PPS config

    // Function         Explorer 16/32              PIC32MM0256GPM064 Device Pins
    // Push Button      PIM#, Net                   PIC#, name
    // S4 (LSb)           80, P80_S4                  36, OCM1E/INT3/RC4
    // S5                 92, P92_S5                  47, SCK1/RC13                 <- Pin function is muxed with LED D10; we will use it as an LED output only
    // S6                 84, P84_S6                  35, OCM2B/RC3
    // S3 (MSb)           83, P83_S3                  49, TMS/RP14/SDA1/INT2/RB9
    EZBL_DefineButtonMap(RB9, RC3, RC4);                    // Use ButtonRead()/ButtonPeek()/ButtonsPushed/ButtonsReleased/ButtonsToggled, etc. to read this bit mapping


    // Function         Explorer 16/32              PIC32MM0256GPM064 Device Pins
    // LED              PIM#, Net                   PIC#, name
    // D3 (LSb)           17, P17_LED3                34, REFCLKI/T1CK/T1G/U1RTS/U1BCLK/SDO1/RD0
    // D4                 38, P38_LED4                53, OCM1B/RD1
    // D5                 58, P58_LED5                32, SS1/FSYNC1/INT0/RD2
    // D6                 59, P59_LED6                33, OCM3B/RD3
    // D7                 60, P60_LED7                31, SDI1/INT1/RD4
    // D8                 61, P61_LED8                --, (N/C)
    // D9                 91, P91_LED9_P96_VBUSON      2, CVREF/AN9/C3INB/RP16/VBUSON/RB14
    // D10 (MSb)          92, P92_S5_LED10            47, SCK1/RC13                 <- Pin function is muxed with button S5; we will use it as an LED output only
    EZBL_DefineLEDMap(RC13, RB14, RD4, RD3, RD2, RD1, RD0); // Use LEDSet()/LEDOn()/LEDOff()/LEDToggle() to access this bit mapping
    LEDSet(0x00);                                           // Clear all the LATxy bits
    EZBL_MapClrEx(0xFFFF, EZBL_LEDMap, (int)&TRISA - (int)&LATA);  // Write TRISxCLR = 1<<y; for all bits defined in the LED Map
    ANSELBCLR = 1u<<14;


    // Function             Explorer 16/32          PIC32MM0256GPM064 Device Pins
    // MCP2221A UART 1      PIM#, Net               PIC#, name
    // U1TX (PIC out)         50, P50_TXB             40, U1TX/RC12
    // U1RX (PIC in)          49, P49_RXB             10, AN19/U1RX/RA6
    CNPUASET = _CNPUA_CNPUA6_MASK;
    ANSELACLR = (1u<<6);
    if(EZBL_COMBaud <= 0)           // If auto-baud enabled, delay our UART initialization so MCP2221A POR timer and init
    {                               // is complete before we start listening. POR timer max spec is 140ms, so MCP2221A TX
        NOW_Wait(140u*NOW_ms);      // pin glitching could occur long after we have enabled our UART without this forced delay.
    }
    EZBL_COMBootIF = UART_Reset(1, FCY, EZBL_COMBaud, 1);   // Use EZBL_FIFORead*()/EZBL_FIFOPeek*()/EZBL_FIFOWrite*() and &UART1_RxFifo/&UART1_TxFifo to read/write. printf()/EZBL_printf()/EZBL_STDOUT/EZBL_STDIN will also map to this when useForStdio == 1.


//    // stdio debugging only (requires air-wires to mikroBus A pins)
//    // Function             Explorer 16/32          PIC32MM0256GPM064 Device Pins
//    // mikroBUS A UART 2    PIM#, Net               PIC#, name
//    // U2TX (PIC out)         51, P51_TXA             50, RP23/RC6
//    // U2RX (PIC in)          52, P52_RXA             51, RP20/RC7
//    RPOUT(23) = _RPOUT_U2TX;
//    RPINR9bits.U2RXR = 20;
//    CNPUCSET = _CNPUC_CNPUC7_MASK;
//    if(EZBL_COMBaud <= 0)           // If auto-baud enabled, delay our UART initialization so MCP2221A POR timer and init
//    {                               // is complete before we start listening. POR timer max spec is 140ms, so MCP2221A TX
//        NOW_Wait(140u*NOW_ms);      // pin glitching could occur long after we have enabled our UART without this forced delay.
//    }
//    UART_Reset(2, FCY, 460800, 1);                          // Use EZBL_FIFORead*()/EZBL_FIFOPeek*()/EZBL_FIFOWrite*() and &UART2_RxFifo/&UART2_TxFifo to read/write. printf()/EZBL_printf()/EZBL_STDOUT/EZBL_STDIN will also map to this when useForStdio == 1.
//    EZBL_ConsoleReset();
//    EZBL_printf("\n\n\nHello World!"
//            "\n  SYS_CLK = %lu"
//            "\n  U1BRG = 0x%04X"
//            "\n  U2BRG = 0x%04X"
//            "\n", NOW_Fcy, U1BRG, U2BRG);


    // Function             Explorer 16/32          PIC32MM0256GPM064 Device Pins
    // LCD 4-bit Mode       PIM#, Net               PIC#, Pin Functions
    // E (Enable)             81, P81_LCDE            54, OCM3A/RA5
    // RS (Register Select)   44, P44_LCDRS           45, OCM3F/RC10
    // DATA<4>               100, P100_LCDD4          41, OCM3D/RC14
    // DATA<5>                 3, P3_LCDD5             6, AN16/U1CTS/RA13
    // DATA<6>                 4, P4_LCDD6            59, OCM3C/RA14
    // DATA<7>                 5, P5_LCDD7            58, RTCC/RA15
    LATACLR   = _LATA_LATA5_MASK | _LATA_LATA13_MASK | _LATA_LATA14_MASK | _LATA_LATA15_MASK;
    LATCCLR   = _LATC_LATC10_MASK | _LATC_LATC14_MASK;
    TRISACLR   = _TRISA_TRISA5_MASK | _TRISA_TRISA13_MASK | _TRISA_TRISA14_MASK | _TRISA_TRISA15_MASK;
    TRISCCLR   = _TRISC_TRISC10_MASK | _TRISC_TRISC14_MASK;
    ANSELACLR = _ANSELA_ANSA13_MASK;


    // Function             Explorer 16/32          PIC32MM0256GPM064 Device Pins
    // Analog Functions     PIM#, Net name          PIC#, Name, PIM Net name
    // 10K Pot Vdd Wiper      20, P20_POT             52, AN14/LVDI N/C2INC/RC8 (P20_POT__RC8)
    // TC1047A Temperature    21, P21_TEMP             8, AN18/RA11 (P21_TEMP__RA11)
    ANSELCSET = _ANSELC_ANSC8_MASK;
    ANSELASET = _ANSELA_ANSA11_MASK;


    return FCY;
}

#endif  // defined(__PIC32MM) && (__PIC32_FEATURE_SET0 == 'G') && (__PIC32_FEATURE_SET1 == 'P') && (__PIC32_PRODUCT_GROUP == 'M')   // PIC32MM0256GPM064 Family devices
