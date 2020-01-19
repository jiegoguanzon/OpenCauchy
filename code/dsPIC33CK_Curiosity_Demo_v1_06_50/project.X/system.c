/*******************************************************************************
 System Initialization File

  File Name:
    sys_init.c

  Summary:
    System Initialization.

  Description:
    This file contains source code necessary to initialize the system.
 *******************************************************************************/

// DOM-IGNORE-BEGIN
/*******************************************************************************
Copyright (c) 2013 released Microchip Technology Inc.  All rights reserved.

Microchip licenses to you the right to use, modify, copy and distribute
Software only when embedded on a Microchip microcontroller or digital signal
controller that is integrated into your product or third party product
(pursuant to the sublicense terms in the accompanying license agreement).

You should refer to the license agreement accompanying this Software for
additional information regarding your rights and obligations.

SOFTWARE AND DOCUMENTATION ARE PROVIDED AS IS WITHOUT WARRANTY OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
IN NO EVENT SHALL MICROCHIP OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER
CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR
OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR
CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF
SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
(INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *******************************************************************************/
// DOM-IGNORE-END

// ****************************************************************************
// ****************************************************************************
// Section: Configuration Bits
// ****************************************************************************
// ****************************************************************************

// FSEC
#pragma config BWRP = OFF    //Boot Segment Write-Protect bit->Boot Segment may be written
#pragma config BSS = DISABLED    //Boot Segment Code-Protect Level bits->No Protection (other than BWRP)
#pragma config BSEN = OFF    //Boot Segment Control bit->No Boot Segment
#pragma config GWRP = OFF    //General Segment Write-Protect bit->General Segment may be written
#pragma config GSS = DISABLED    //General Segment Code-Protect Level bits->No Protection (other than GWRP)
#pragma config CWRP = OFF    //Configuration Segment Write-Protect bit->Configuration Segment may be written
#pragma config CSS = DISABLED    //Configuration Segment Code-Protect Level bits->No Protection (other than CWRP)
#pragma config AIVTDIS = OFF    //Alternate Interrupt Vector Table bit->Disabled AIVT

// FBSLIM
#pragma config BSLIM = 8191    //Boot Segment Flash Page Address Limit bits->8191

// FOSCSEL
#pragma config FNOSC = PRI    //Oscillator Source Selection->Primary Oscillator (XT, HS, EC)
#pragma config IESO = OFF    //Two-speed Oscillator Start-up Enable bit->Start up with user-selected oscillator source

// FOSC
#pragma config POSCMD = XT    //Primary Oscillator Mode Select bits->XT Crystal Oscillator Mode
#pragma config OSCIOFNC = ON    //OSC2 Pin Function bit->OSC2 is general purpose digital I/O pin
#pragma config FCKSM = CSDCMD    //Clock Switching Mode bits->Both Clock switching and Fail-safe Clock Monitor are disabled
#pragma config PLLKEN = ON    //PLL Lock Status Control->PLL lock signal will be used to disable PLL clock output if lock is lost
#pragma config XTCFG = G3    //XT Config->24-32 MHz crystals
#pragma config XTBST = ENABLE    //XT Boost->Boost the kick-start

// FWDT
#pragma config RCLKSEL = LPRC    //Watchdog Timer Clock Select bits->Always use LPRC
#pragma config WINDIS = OFF    //Watchdog Timer Window Enable bit->Watchdog Timer in Window mode
#pragma config WDTWIN = WIN25    //Watchdog Timer Window Select bits->WDT Window is 25% of WDT period
#pragma config FWDTEN = ON_SW    //Watchdog Timer Enable bit->WDT controlled via SW, use WDTCON.ON bit

// FPOR
#pragma config BISTDIS = DISABLED    //Memory BIST Feature Disable->mBIST on reset feature disabled

// FICD
#pragma config ICS = PGD3    //ICD Communication Channel Select bits->Communicate on PGC3 and PGD3
#pragma config JTAGEN = OFF    //JTAG Enable bit->JTAG is disabled
#pragma config NOBTSWP = DISABLED    //BOOTSWP instruction disable bit->BOOTSWP instruction is disabled

// FDMTIVTL
#pragma config DMTIVTL = 0    //Dead Man Timer Interval low word->0

// FDMTIVTH
#pragma config DMTIVTH = 0    //Dead Man Timer Interval high word->0

// FDMTCNTL
#pragma config DMTCNTL = 0    //Lower 16 bits of 32 bit DMT instruction count time-out value (0-0xFFFF)->0

// FDMTCNTH
#pragma config DMTCNTH = 0    //Upper 16 bits of 32 bit DMT instruction count time-out value (0-0xFFFF)->0

// FDMT
#pragma config DMTDIS = OFF    //Dead Man Timer Disable bit->Dead Man Timer is Disabled and can be enabled by software

// FDEVOPT
#pragma config ALTI2C1 = OFF    //Alternate I2C1 Pin bit->I2C1 mapped to SDA1/SCL1 pins
#pragma config ALTI2C2 = OFF    //Alternate I2C2 Pin bit->I2C2 mapped to SDA2/SCL2 pins
#pragma config ALTI2C3 = OFF    //Alternate I2C3 Pin bit->I2C3 mapped to SDA3/SCL3 pins
#pragma config SMBEN = SMBUS    //SM Bus Enable->SMBus input threshold is enabled
#pragma config SPI2PIN = PPS    //SPI2 Pin Select bit->SPI2 uses I/O remap (PPS) pins

// FALTREG
#pragma config CTXT1 = OFF    //Specifies Interrupt Priority Level (IPL) Associated to Alternate Working Register 1 bits->Not Assigned
#pragma config CTXT2 = OFF    //Specifies Interrupt Priority Level (IPL) Associated to Alternate Working Register 2 bits->Not Assigned
#pragma config CTXT3 = OFF    //Specifies Interrupt Priority Level (IPL) Associated to Alternate Working Register 3 bits->Not Assigned
#pragma config CTXT4 = OFF    //Specifies Interrupt Priority Level (IPL) Associated to Alternate Working Register 4 bits->Not Assigned

// FBTSEQ
#pragma config BSEQ = 4095    //Relative value defining which partition will be active after device Reset; the partition containing a lower boot number will be active->4095
#pragma config IBSEQ = 4095    //The one's complement of BSEQ; must be calculated by the user and written during device programming.->4095

// FBOOT
#pragma config BTMODE = SINGLE    //Device Boot Mode Configuration->Device is in Single Boot (legacy) mode

// #pragma config statements should precede project file includes.
// Use project enums instead of #define for ON and OFF.

#include <xc.h>
#include "uart1.h"

// ****************************************************************************
// ****************************************************************************
// Section: File Scope Functions
// ****************************************************************************
// ****************************************************************************

void SOSC_Configuration ( void ) ;

void __attribute__ ( ( __interrupt__ , auto_psv ) ) _OscillatorFail ( void ) ;
void __attribute__ ( ( __interrupt__ , auto_psv ) ) _AddressError ( void ) ;
void __attribute__ ( ( __interrupt__ , auto_psv ) ) _StackError ( void ) ;
void __attribute__ ( ( __interrupt__ , auto_psv ) ) _MathError ( void ) ;

// ****************************************************************************
// ****************************************************************************
// Section: System Initialization
// ****************************************************************************
// ****************************************************************************

void CLOCK_Initialize(void)
{
    // FRCDIV FRC/1; PLLPRE 1; DOZE 1:8; DOZEN disabled; ROI disabled; 
    CLKDIV = 0x3001;
    // PLLFBDIV 150; 
    PLLFBD = 0x96;
    // TUN Center frequency; 
    OSCTUN = 0x00;
    // POST1DIV 1:4; VCODIV FVCO/4; POST2DIV 1:1; 
    PLLDIV = 0x41;
    // APLLEN disabled; FRCSEL FRC; APLLPRE 1:1; 
    ACLKCON1 = 0x101;
    // APLLFBDIV 150; 
    APLLFBD1 = 0x96;
    // APOST1DIV 1:4; APOST2DIV 1:1; AVCODIV FVCO/4; 
    APLLDIV1 = 0x41;
    // CANCLKEN disabled; CANCLKSEL No Clock Selected; CANCLKDIV Divide by 1; 
    CANCLKCON = 0x00;
    // ROEN disabled; ROSWEN disabled; ROSLP disabled; ROSEL FOSC; ROOUT disabled; ROSIDL disabled; 
    REFOCONL = 0x00;
    // RODIV 0; 
    REFOCONH = 0x00;
    // IOLOCK disabled; 
    RPCON = 0x00;
    // PMDLOCK disabled; 
    PMDCON = 0x00;
    // ADC1MD enabled; T1MD enabled; U2MD enabled; U1MD enabled; SPI2MD enabled; SPI1MD enabled; QEIMD enabled; C1MD enabled; PWMMD enabled; I2C1MD enabled; 
    PMD1 = 0x00;
    // CCP2MD enabled; CCP1MD enabled; CCP4MD enabled; CCP3MD enabled; CCP7MD enabled; CCP8MD enabled; CCP5MD enabled; CCP6MD enabled; CCP9MD enabled; 
    PMD2 = 0x00;
    // I2C3MD enabled; PMPMD enabled; U3MD enabled; QEI2MD enabled; CRCMD enabled; I2C2MD enabled; 
    PMD3 = 0x00;
    // REFOMD enabled; 
    PMD4 = 0x00;
    // DMA1MD enabled; SPI3MD enabled; DMA2MD enabled; DMA3MD enabled; DMA0MD enabled; 
    PMD6 = 0x00;
    // CMP3MD enabled; PTGMD enabled; CMP1MD enabled; CMP2MD enabled; 
    PMD7 = 0x00;
    // DMTMD enabled; CLC3MD enabled; OPAMPMD enabled; BIASMD enabled; CLC4MD enabled; SENT2MD enabled; SENT1MD enabled; CLC1MD enabled; CLC2MD enabled; 
    PMD8 = 0x00;
    // CF no clock failure; NOSC PRI; CLKLOCK unlocked; OSWEN Switch is Complete; 
    __builtin_write_OSCCONH((uint8_t) (0x02));
    __builtin_write_OSCCONL((uint8_t) (0x00));
}

/*******************************************************************************
  Function:
    void SYS_Initialize ( void )

  Summary:
    Initializes the Explorer 16 board peripherals

  Description:
    This routine initializes the Explorer 16 board peripherals for the demo.
    In a bare-metal environment (where no OS is supported), this routine should
    be called almost immediately after entering the "main" routine.

  Precondition:
    The C-language run-time environment and stack must have been initialized.

  Parameters:
    None.

  Returns:
    None.

  Example:
    <code>
    SYS_INT_Initialize(NULL);
    </code>

  Remarks:
    Basic System Initialization Sequence:

    1.  Initialize minimal board services and processor-specific items
        (enough to use the board to initialize drivers and services)
    2.  Initialize all supported system services
    3.  Initialize all supported modules
        (libraries, drivers, middle-ware, and application-level modules)
    4.  Initialize the main (static) application, if present.

    The order in which services and modules are initialized and started may be
    important.

 */

void SYSTEM_Initialize ( void )
{    
    _RP68R = 1;    //UART PPS - RD4[RP68] = U1TX 
    UART1_Initialize();
}



// *****************************************************************************
// *****************************************************************************
// Section: Primary Exception Vector handlers
// *****************************************************************************
// *****************************************************************************

// *****************************************************************************
/*  void __attribute__((__interrupt__,auto_psv)) _OscillatorFail(void)

  Summary:
    Provides the required exception vector handlers for Oscillator trap

  Description:
   This routine is used if INTCON2bits.ALTIVT = 0 and it handles the oscillator
 trap.

  Remarks:
   All trap service routines in this file simply ensure that device
   continuously executes code within the trap service routine. Users
   may modify the basic framework provided here to suit to the needs
   of their application.
 */

void __attribute__ ( ( __interrupt__ , auto_psv ) ) _OscillatorFail ( void )
{
    INTCON1bits.OSCFAIL = 0 ;        //Clear the trap flag
    while (1) ;
}

// *****************************************************************************
/*  void __attribute__((__interrupt__,auto_psv)) _AddressError(void)

  Summary:
    Provides the required exception vector handlers for Address Error trap

  Description:
   This routine is used if INTCON2bits.ALTIVT = 0 and it handles the address
 error trap.

  Remarks:
   All trap service routines in this file simply ensure that device
   continuously executes code within the trap service routine. Users
   may modify the basic framework provided here to suit to the needs
   of their application.
 */

void __attribute__ ( ( __interrupt__ , auto_psv ) ) _AddressError ( void )
{
    INTCON1bits.ADDRERR = 0 ;        //Clear the trap flag
    while (1) ;
}

// *****************************************************************************
/*  void __attribute__((__interrupt__,auto_psv)) _StackError(void))

  Summary:
    Provides the required exception vector handlers for Stack Error trap

  Description:
   This routine is used if INTCON2bits.ALTIVT = 0 and it handles the stack
 error trap.

  Remarks:
   All trap service routines in this file simply ensure that device
   continuously executes code within the trap service routine. Users
   may modify the basic framework provided here to suit to the needs
   of their application.
 */

void __attribute__ ( ( __interrupt__ , auto_psv ) ) _StackError ( void )
{
    INTCON1bits.STKERR = 0 ;         //Clear the trap flag
    while (1) ;
}

// *****************************************************************************
/*  void __attribute__((__interrupt__,auto_psv)) _MathError(void))

  Summary:
    Provides the required exception vector handlers for Math Error trap

  Description:
   This routine is used if INTCON2bits.ALTIVT = 0 and it handles the math
 error trap.

  Remarks:
   All trap service routines in this file simply ensure that device
   continuously executes code within the trap service routine. Users
   may modify the basic framework provided here to suit to the needs
   of their application.
 */

void __attribute__ ( ( __interrupt__ , auto_psv ) ) _MathError ( void )
{
    INTCON1bits.MATHERR = 0 ;        //Clear the trap flag
    while (1) ;
}
