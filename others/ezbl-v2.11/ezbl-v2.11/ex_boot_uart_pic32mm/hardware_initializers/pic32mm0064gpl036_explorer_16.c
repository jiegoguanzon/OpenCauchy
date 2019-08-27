/*
 * File:   pic32mm0064gpl036_explorer_16.c
 *
 * Created on 2016 November 28
 *
 * Initializes the device Configuration words, system clock frequency, UARTs, 
 * LEDs, buttons, NOW timer module on the PIC32 MIPS Core Timer, and exposes
 * some common APIs for reading buttons and manipulating LEDs.
 * 
 * This file is intended to be used on the PIC32MM0064GPL036 PIM for the
 * Explorer 16/32 (and Explorer 16) hardware, but is also the recommended
 * starting basis for any project implementing any of the PIC32MM0064GPL036
 * family devices.
 *
 * Reference (CTRL + Click on link):
 *     PIC32MM0064GPL036 Family data sheet
*      http://www.microchip.com/wwwproducts/productds/PIC32MM0064GPL036
 *
 *     PIC32MM0064GPL036 General Purpose PIM (Plug-in-Module)
 *     http://www.microchip.com/MA320020
 *     http://ww1.microchip.com/downloads/en/DeviceDoc/50002513a.pdf (Info Sheet)
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
#if defined(__PIC32MM) && (__PIC32_FEATURE_SET0 == 'G') && (__PIC32_FEATURE_SET1 == 'P') && (__PIC32_PRODUCT_GROUP == 'L')   // PIC32MM0064GPL036 Family devices

#include "../ezbl_integration/ezbl.h"


// PIC32MM0064GPL036 Configuration bit settings
// FDEVOPT
#pragma config SOSCHP = ON              // Secondary Oscillator High Power mode bit (OFF = SOSC crystal oscillator, if enabled, drives crystal with minimum power; ON = SOSC crystal oscillator, if enabled, drives crystal with stronger bias for higher reliability in humid or electrically noisy environments)
// FICD
#pragma config JTAGEN = OFF             // JTAG Enable bit (OFF = No JTAG support, no TMS pull-up, cannot be enabled in software, must use "2-wire" ICSP on PGECx/PGEDx pair for program and debug; ON = JTAG Enabled on reset, weak pull-up on TMS/RB9, can be run-time disabled by setting CFGCON[JTAGEN] = 0 when not debugging)
#pragma config ICS = PGx1               // ICE/ICD Communication Channel Selection bits (Communicate on PGEC1/AN3/C1INC/C2INA/RP15/RB1 & PGED1/AN2/C1IND/C2INB/RP14/RB0)
// FPOR
#pragma config BOREN = BOR2             // Brown-out Reset mode bits (BOR3 = BOR enabled, SBOREN bit does nothing; BOR2 = BOR enabled, but not in sleep mode; BOR1 = BOR controlled by SBOREN; BOR0 = BOR disabled, SBOREN does nothing)
#pragma config RETVR = ON               // Retention Voltage Regulator Enable bit (ON = ~1.2V Retention regulator is enabled and controlled by RETEN bit during sleep; OFF = Sleep mode always uses higher power 1.8V standby regulator)
#pragma config LPBOREN = ON             // Downside Voltage Protection bit (ON = Low Power BOR used when main BOR is off; OFF = always use main BOR)
// FWDT
#pragma config SWDTPS = PS1024          // Sleep Mode Watchdog Timer Postscale Selection bits (1:1024)
#pragma config FWDTWINSZ = PS75_0       // Watchdog Timer Window Size bits (PS75_0 = window size is 75% with most freedom; PS50_0 = less freedom; PS37_5 = less freedom; PS25_0 = windows size is 25% with least freedom)
#pragma config WINDIS = OFF             // Windowed Watchdog Timer mode bit (OFF = classic, non-window mode; ON = Windowed mode)
#pragma config RWDTPS = PS512           // Run Mode Watchdog Timer Postscale Selection bits (1:512 ~= 500 ms)
#pragma config RCLKSEL = LPRC           // Run Mode Watchdog Timer Clock Source Selection bits (Clock source is LPRC (same as for sleep mode))
#pragma config FWDTEN = OFF             // Watchdog Timer Enable bit (WDT is disabled, use software to enable it)
// FOSCSEL
#pragma config FNOSC = PLL              // Oscillator Selection bits (FRCDIV; PLL; PRI; DCO; SOSC; LPRC)
#pragma config PLLSRC = FRC             // System PLL Input Clock Selection bit (FRC oscillator is selected as PLL reference input on device reset)
#pragma config SOSCEN = ON              // Secondary Oscillator Enable bit (ON = Secondary crystal oscillator circuit enabled, RB4/RA4 function as SOSCI/SOSCO (no GPIO); OFF = Secondary oscillator disabled, GPIO and other functions available on RB4/RA4)
#pragma config IESO = ON                // Two Speed Startup Enable bit (OFF = Two speed startup is disabled; ON = Enabled, POR/BOR/Wake from sleep with FRC, then switch to FNOSC clock after it has warmed up)
#pragma config POSCMOD = XT             // Primary Oscillator Selection bit (OFF = Primary oscillator disabled; EC = External digital clock input on OSC1; XT = Moderate cystal drive strength, up to at least 8.0MHz; HS = High Speed cystal)
#pragma config OSCIOFNC = OFF           // System Clock on CLKO Pin Enable bit (OFF = OSCO pin operates at GPIO; ON = OSCO pin operates as CLKO SYSCLK output)
#pragma config SOSCSEL = OFF            // SOSC/CLKI External Clock Input bit (OFF = 32.768K crystal oscillator mode, ON = CLKI or GPIO mode)
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

    // Initialize NOW module (using PIC32 Core Timer without interrupts)
    NOW_Reset(CORETMR, FCY);    // Using 8.0MHz FRC with 3xPLL


    // Function     Explorer 16/32              PIC32MM0064GPL036 Device Pins
    // LED          PIM#, Net                   PIC#, name
    // D3 (LSb)       17, P17_LED3                27, PGED2/TDO/RP17/RB10 (P17_LED3_P100_LCDD4)
    // D4             38, P38_LED4                28, PGEC2/TDI/RP18/RB11 (P38_LED4_P3_LCDD5)
    // D5             58, P58_LED5                --, (N/C)
    // D6             59, P59_LED6                --, (N/C)
    // D7             60, P60_LED7                --, (N/C)
    // D8             61, P61_LED8                --, (N/C)
    // D9             91, P91_LED9_P96_VBUSON     29, AN7/LVDIN/RP12/RB12 (P91_LED9_P4_LCDD6)
    // D10 (MSb)      92, P92_S5_LED10            30, AN8/RP13/RB13 (P92_LED10_P5_LCDD7)   <- Pin function is muxed with button S5; we will use it as an LED output only
    EZBL_DefineLEDMap(RB13, RB12, RB11, RB10);  // Use LEDSet()/LEDOn()/LEDOff()/LEDToggle() to access this bit mapping
    LATBCLR   = (1u<<10) | (1u<<11) | (1u<<12) | (1u<<13);
    TRISBCLR  = (1u<<10) | (1u<<11) | (1u<<12) | (1u<<13);
    ANSELBCLR = (1u<<10) | (1u<<11) | (1u<<12) | (1u<<13);

    
    // Function     Explorer 16/32 PIM Header   PIC32MM0064GPL036 Device Pins
    // Push Button  PIM#, PICtail#, name        PIC#, name
    // S4 (LSb)     80,        106, RD13          17, RP11/RB7 (P80_S4)
    // S5           92,         74, RA7           30, AN8/RP13/RB13 (P92_LED10_P5_LCDD7)    <- Pin function is muxed with LED D10; will use LED output only
    // S6           84,        100, RD7           21, RC8 (P19_RSTB_P84_S6)
    // S3 (MSb)     83,         99, RD6           22, RP19/RC9 (P18_INTB_P83_S3)
    EZBL_DefineButtonMap(RC9, RC8, RB7);        // Use ButtonRead()/ButtonPeek()/ButtonsPushed/ButtonsReleased/ButtonsToggled, etc. to read this bit mapping
    TRISBSET = (1<<7);
    TRISCSET = (1<<8) | (1<<9);


    // Function     Explorer 16/32              PIC32MM0064GPL036 Device Pins
    // UART2        PIM#, Net                   PIC#, Pin Functions
    // U2TX (PIC out) 50, P50_TXB                 37, VREF-/AN1/RP2/OCM1F/RA1
    // U2RX (PIC in)  49, P49_RXB                 36, VREF+/AN0/RP1/OCM1E/INT3/RA0
    ANSELACLR        = (1<<1) | (1<<0);
    RPOUT(2)         = _RPOUT_U2TX; // RP2 = U2TX
    RPINR9bits.U2RXR = 1;           // RP1 = U2RX
    CNPUASET = _CNPUA_CNPUA0_MASK;  // Turn on weak pull up on U2RX so the signal stays idle if nobody is plugged in
    if(EZBL_COMBaud <= 0)           // If auto-baud enabled, delay our UART initialization so MCP2221A POR timer and init
    {                               // is complete before we start listening. POR timer max spec is 140ms, so MCP2221A TX
        NOW_Wait(140u*NOW_ms);      // pin glitching could occur long after we have enabled our UART without this forced delay.
    }
    EZBL_COMBootIF = UART_Reset(2, FCY, EZBL_COMBaud, 1);   // Use EZBL_FIFORead*()/EZBL_FIFOPeek*()/EZBL_FIFOWrite*() and &UART2_RxFifo/&UART2_TxFifo to read/write. printf()/EZBL_printf()/EZBL_STDOUT/EZBL_STDIN will also map to this when useForStdio == 1.


//    // Initialize UART 1 for stdio and debugging (using air-wires to mikroBus B pins)
//    //      Fixed pins: TX = Explorer 16/32 P54_MISOB = PIC pin 31 = CDAC1/AN9/RP9/RTCC/U1TX/SDI1/C1OUT/INT1/RB14
//    //                  RX = Explorer 16/32 P78_PWMB  = PIC pin 32 =  AN10/REFCLKO/RP10/U1RX/SS1/FSYNC1/INT0/RB15
//    CNPUBSET = 1<<15;
//    ANSELBCLR = 0x3<<14;
//    UART_Reset(1, FCY, 460800, 1);                        // Use EZBL_FIFORead*()/EZBL_FIFOPeek*()/EZBL_FIFOWrite*() and &UART1_RxFifo/&UART1_TxFifo to read/write. printf()/EZBL_printf()/EZBL_STDOUT/EZBL_STDIN will also map to this when useForStdio == 1.
//    EZBL_ConsoleReset();
//    EZBL_printf("\n\n\nHello World!"
//            "\n  SYS_CLK = %lu"
//            "\n  U1BRG = 0x%04X"
//            "\n  U2BRG = 0x%04X"
//            "\n", NOW_Fcy, U1BRG, U2BRG);

    return FCY;
}

#endif  // defined(__PIC32MM) && (__PIC32_FEATURE_SET0 == 'G') && (__PIC32_FEATURE_SET1 == 'P') && (__PIC32_PRODUCT_GROUP == 'L')   // PIC32MM0064GPL036 Family devices
