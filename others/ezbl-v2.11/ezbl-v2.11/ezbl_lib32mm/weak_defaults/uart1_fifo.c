/*
 * File:   uart1_fifo.c
 *
 * An easily ported interrupt based UART TX and RX buffering implementation for
 * PIC32MMxxxxGPLxxx family devices. Implements independent software FIFOs for
 * TX and RX channels.
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

#if defined(EZBL_LIB)
#include "../ezbl.h"
#else
#include "ezbl_integration/ezbl.h"
#endif


// Configuration parameters
#define UART_MODULE_NUM         1       // Hardware UART module # to apply this
                                        // software FIFO to. 1 means UART1, 2
                                        // means UART2, etc.
#define UART_TX_FIFO_SIZE       32u     // Bytes for software TX FIFO buffering
#define UART_RX_FIFO_SIZE       96u     // Bytes for software RX FIFO buffering

#define UART_ISR_PRIORITY       1       // CPU IPL (Interrupt Priority Level) to
                                        // assign to the RX and TX ISRs. 0 means
                                        // main() context, which is the lowest
                                        // priority and 7 is the highest,
                                        // time-critical, priority.
                                        
                                        

// Exact match RX character sequence to trigger assignment of EZBL_COM_RX to
// this UART. Comment this line out to disable auto-bootloader wake up on this UART.
#define BOOTLOADER_WAKE_KEY     {'M','C','U','P','H','C','M','E'}

// NOTE: UxTX and UxRX I/O pin PPS settings need to be configured outside of
// this module if the selected UART does not have fixed device I/O pins.



#define UxMD                CAT3(U,UART_MODULE_NUM,MD)
#define UxMD_CLR            CAT3(U,UART_MODULE_NUM,MD_CLR)
#define UxMD_SET            CAT3(U,UART_MODULE_NUM,MD_SET)
#define UxMD_INV            CAT3(U,UART_MODULE_NUM,MD_INV)
#define IPLRXTXSOFT         CAT3(IPL,UART_ISR_PRIORITY,SOFT)


typedef struct PMDBITS
{
    unsigned ADCMD:1;       // PMD1
    unsigned :11;
    unsigned VREFMD:1;
    unsigned :7;
    unsigned HLVDMD:1;
    unsigned :11;
    unsigned ADCMD_CLR:1;   // PMD1CLR
    unsigned :11;
    unsigned VREFMD_CLR:1;
    unsigned :7;
    unsigned HLVDMD_CLR:1;
    unsigned :11;
    unsigned ADCMD_SET:1;   // PMD1SET
    unsigned :11;
    unsigned VREFMD_SET:1;
    unsigned :7;
    unsigned HLVDMD_SET:1;
    unsigned :11;
    unsigned ADCMD_INV:1;   // PMD1INV
    unsigned :11;
    unsigned VREFMD_INV:1;
    unsigned :7;
    unsigned HLVDMD_INV:1;
    unsigned :11;
    unsigned CMP1MD:1;      // PMD2
    unsigned CMP2MD:1;
    unsigned CMP3MD:1;          // PIC32MM0256GPM064 Family only
    unsigned :21;
    unsigned CLC1MD:1;
    unsigned CLC2MD:1;
    unsigned CLC3MD:1;          // PIC32MM0256GPM064 Family only
    unsigned CLC4MD:1;          // PIC32MM0256GPM064 Family only
    unsigned :4;
    unsigned CMP1MD_CLR:1;  // PMD2CLR
    unsigned CMP2MD_CLR:1;
    unsigned CMP3MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned :21;
    unsigned CLC1MD_CLR:1;
    unsigned CLC2MD_CLR:1;
    unsigned CLC3MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned CLC4MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned :4;
    unsigned CMP1MD_SET:1;  // PMD2SET
    unsigned CMP2MD_SET:1;
    unsigned CMP3MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned :21;
    unsigned CLC1MD_SET:1;
    unsigned CLC2MD_SET:1;
    unsigned CLC3MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned CLC4MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned :4;
    unsigned CMP1MD_INV:1;  // PMD2INV
    unsigned CMP2MD_INV:1;
    unsigned CMP3MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned :21;
    unsigned CLC1MD_INV:1;
    unsigned CLC2MD_INV:1;
    unsigned CLC3MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned CLC4MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned :4;
    unsigned :8;            // PMD3
    unsigned CCP1MD:1;
    unsigned CCP2MD:1;
    unsigned CCP3MD:1;
    unsigned CCP4MD:1;          // PIC32MM0256GPM064 Family only
    unsigned CC5MD:1;           // PIC32MM0256GPM064 Family only
    unsigned CC6MD:1;           // PIC32MM0256GPM064 Family only
    unsigned CC7MD:1;           // PIC32MM0256GPM064 Family only
    unsigned CC8MD:1;           // PIC32MM0256GPM064 Family only
    unsigned CC9MD:1;           // PIC32MM0256GPM064 Family only
    unsigned :15;
    unsigned :8;            // PMD3CLR
    unsigned CCP1MD_CLR:1;
    unsigned CCP2MD_CLR:1;
    unsigned CCP3MD_CLR:1;
    unsigned CCP4MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned CC5MD_CLR:1;       // PIC32MM0256GPM064 Family only
    unsigned CC6MD_CLR:1;       // PIC32MM0256GPM064 Family only
    unsigned CC7MD_CLR:1;       // PIC32MM0256GPM064 Family only
    unsigned CC8MD_CLR:1;       // PIC32MM0256GPM064 Family only
    unsigned CC9MD_CLR:1;       // PIC32MM0256GPM064 Family only
    unsigned :15;
    unsigned :8;            // PMD3SET
    unsigned CCP1MD_SET:1;
    unsigned CCP2MD_SET:1;
    unsigned CCP3MD_SET:1;
    unsigned CCP4MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned CC5MD_SET:1;       // PIC32MM0256GPM064 Family only
    unsigned CC6MD_SET:1;       // PIC32MM0256GPM064 Family only
    unsigned CC7MD_SET:1;       // PIC32MM0256GPM064 Family only
    unsigned CC8MD_SET:1;       // PIC32MM0256GPM064 Family only
    unsigned CC9MD_SET:1;       // PIC32MM0256GPM064 Family only
    unsigned :15;
    unsigned :8;            // PMD3INV
    unsigned CCP1MD_INV:1;
    unsigned CCP2MD_INV:1;
    unsigned CCP3MD_INV:1;
    unsigned CCP4MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned CC5MD_INV:1;       // PIC32MM0256GPM064 Family only
    unsigned CC6MD_INV:1;       // PIC32MM0256GPM064 Family only
    unsigned CC7MD_INV:1;       // PIC32MM0256GPM064 Family only
    unsigned CC8MD_INV:1;       // PIC32MM0256GPM064 Family only
    unsigned CC9MD_INV:1;       // PIC32MM0256GPM064 Family only
    unsigned :15;
    unsigned T1MD:1;        // PMD4
    unsigned T2MD:1;            // PIC32MM0256GPM064 Family only
    unsigned T3MD:1;            // PIC32MM0256GPM064 Family only
    unsigned :29;
    unsigned T1MD_CLR:1;    // PMD4CLR
    unsigned T2MD_CLR:1;        // PIC32MM0256GPM064 Family only
    unsigned T3MD_CLR:1;        // PIC32MM0256GPM064 Family only
    unsigned :29;
    unsigned T1MD_SET:1;    // PMD4SET
    unsigned T2MD_SET:1;        // PIC32MM0256GPM064 Family only
    unsigned T3MD_SET:1;        // PIC32MM0256GPM064 Family only
    unsigned :29;
    unsigned T1MD_INV:1;    // PMD4INV
    unsigned T2MD_INV:1;        // PIC32MM0256GPM064 Family only
    unsigned T3MD_INV:1;        // PIC32MM0256GPM064 Family only
    unsigned :29;
    unsigned U1MD:1;        // PMD5
    unsigned U2MD:1;
    unsigned U3MD:1;            // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned SPI1MD:1;
    unsigned SPI2MD:1;
    unsigned SPI3MD:1;          // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned I2C1MD:1;          // PIC32MM0256GPM064 Family only
    unsigned I2C2MD:1;          // PIC32MM0256GPM064 Family only
    unsigned I2C3MD:1;          // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned USBMD:1;           // PIC32MM0256GPM064 Family only
    unsigned :7;
    unsigned U1MD_CLR:1;    // PMD5CLR
    unsigned U2MD_CLR:1;
    unsigned U3MD_CLR:1;        // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned SPI1MD_CLR:1;
    unsigned SPI2MD_CLR:1;
    unsigned SPI3MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned I2C1MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned I2C2MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned I2C3MD_CLR:1;      // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned USBMD_CLR:1;       // PIC32MM0256GPM064 Family only
    unsigned :7;
    unsigned U1MD_SET:1;    // PMD5SET
    unsigned U2MD_SET:1;
    unsigned U3MD_SET:1;        // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned SPI1MD_SET:1;
    unsigned SPI2MD_SET:1;
    unsigned SPI3MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned I2C1MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned I2C2MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned I2C3MD_SET:1;      // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned USBMD_SET:1;       // PIC32MM0256GPM064 Family only
    unsigned :7;
    unsigned U1MD_INV:1;    // PMD5INV
    unsigned U2MD_INV:1;
    unsigned U3MD_INV:1;        // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned SPI1MD_INV:1;
    unsigned SPI2MD_INV:1;
    unsigned SPI3MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned I2C1MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned I2C2MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned I2C3MD_INV:1;      // PIC32MM0256GPM064 Family only
    unsigned :5;
    unsigned USBMD_INV:1;       // PIC32MM0256GPM064 Family only
    unsigned :7;
    unsigned RTCCMD:1;      // PMD6
    unsigned :7;
    unsigned REFOMD:1;
    unsigned :23;
    unsigned RTCCMD_CLR:1;  // PMD6CLR
    unsigned :7;
    unsigned REFOMD_CLR:1;
    unsigned :23;
    unsigned RTCCMD_SET:1;  // PMD6SET
    unsigned :7;
    unsigned REFOMD_SET:1;
    unsigned :23;
    unsigned RTCCMD_INV:1;  // PMD6INV
    unsigned :7;
    unsigned REFOMD_INV:1;
    unsigned :23;
    unsigned :3;            // PMD7
    unsigned CRCMD:1;           // PIC32MM0064GPL036 Family only
    unsigned DMAMD:1;
    unsigned :27;
    unsigned :3;            // PMD7CLR
    unsigned CRCMD_CLR:1;       // PIC32MM0064GPL036 Family only
    unsigned DMAMD_CLR:1;
    unsigned :27;
    unsigned :3;            // PMD7SET
    unsigned CRCMD_SET:1;       // PIC32MM0064GPL036 Family only
    unsigned DMAMD_SET:1;
    unsigned :27;
    unsigned :3;            // PMD7INV
    unsigned CRCMD_INV:1;       // PIC32MM0064GPL036 Family only
    unsigned DMAMD_INV:1;
    unsigned :27;
} PMDBITS;




typedef struct
{
    // UxMODE
    unsigned STSEL:1;   // STSEL: Stop Bit Selection bit <br>1 = Two Stop bits <br>0 = One Stop bit
    unsigned PDSEL:2;   // PDSEL<1:0>: Parity and Data Selection bits <br>0x3 = 9-bit data, no parity <br>0x2 = 8-bit data, odd parity <br>0x1 = 8-bit data, even parity <br>0x0 = 8-bit data, no parity
    unsigned BRGH:1;    // BRGH: High Baud Rate Enable bit <br>1 = BRG generates 4 clocks per bit period (4x baud clock, High-Speed mode) <br>0 = BRG generates 16 clocks per bit period (16x baud clock, Standard mode)
    unsigned URXINV:1;  // URXINV: Receive Polarity Inversion bit (AKA 'RXINV') <br>1 = UxRX Idle state is ‘0’ <br>0 = UxRX Idle state is ‘1’
    unsigned ABAUD:1;   // ABAUD: Auto-Baud Enable bit <br>1 = Enables baud rate measurement on the next character – requires reception of a Sync field (55h) before other data; cleared in hardware upon completion <br>0 = Baud rate measurement is disabled or completed
    unsigned LPBACK:1;  // LPBACK: UARTx Loopback Mode Select bit <br>1 = Enables Loopback mode <br>0 = Loopback mode is disabled
    unsigned WAKE:1;    // WAKE: Wake-up on Start bit Detect During Sleep Mode Enable bit <br>1 = UARTx continues to sample the UxRX pin, interrupt is generated on the falling edge; bit is cleared in hardware on the following rising edge <br>0 = No wake-up is enabled
    unsigned UEN:2;     // UEN<1:0>: UARTx Enable bits <br>0x3 = UxTX, UxRX and BCLKx pins are enabled and used; UxCTS pin is controlled by PORT latches <br>0x2 = UxTX, UxRX, UxCTS and UxRTS pins are enabled and used <br>0x1 = UxTX, UxRX and UxRTS pins are enabled and used; UxCTS pin is controlled by PORT latches <br>0x0 = UxTX and UxRX pins are enabled and used; UxCTS and UxRTS/BCLKx pins are controlled by PORT latches
    unsigned :1;        // Unimplemented: Read as ‘0’
    unsigned RTSMD:1;   // RTSMD: Mode Selection for UxRTS Pin bit <br>1 = UxRTS pin is in Simplex mode <br>0 = UxRTS pin is in Flow Control mode
    unsigned IREN:1;    // IREN: IrDA® Encoder and Decoder Enable bit <br>1 = IrDA encoder and decoder are enabled <br>0 = IrDA encoder and decoder are disabled
    unsigned SIDL:1;    // SIDL: Stop in Idle Mode bit <br>1 = Discontinues module operation when device enters Idle mode <br>0 = Continues module operation in Idle mode
    unsigned FREEZE:1;  // Unimplemented: Read as ‘0’, OR in Debug Mode: Freeze peripheral during debug halt
    unsigned UARTEN:1;  // UARTEN: UARTx Enable bit (AKA 'ON') <br>1 = UARTx is enabled; all UARTx pins are controlled by UARTx as defined by UEN<1:0> <br>0 = UARTx is disabled; all UARTx pins are controlled by PORT latches; UARTx power consumption is minimal

#if defined(__XC32__)
             unsigned OVFDIS:1;          // OVFDIS: Run During Overflow Condition Mode bit <br>1 = When an Overflow Error (OERR) condition is detected, the Shift register continues to run to remain synchronized <br>0 = When an Overflow Error (OERR) condition is detected, the Shift register stops accepting new data (Legacy mode)
             unsigned CLKSEL:2;          // CLKSEL<1:0>: UARTx Clock Selection bits <br>0x3 = The UARTx clock is the Reference Clock Output (REFCLKO) <br>0x2 = The UARTx clock is the FRC oscillator clock <br>0x1 = The UARTx clock is the SYSCLK <br>0x0 = The UARTx clock is the PBCLK (1:1 with SYSCLK)
             unsigned :3;
    volatile unsigned ACTIVE:1; // ACTIVE: UARTx Running Status bit <br>1 = UARTx is active (UxMODE register shouldn't be updated) <br>0 = UARTx is not active (UxMODE register can be updated)
             unsigned SLPEN:1;           // SLPEN: Run During Sleep Enable bit <br>1 = UARTx clock runs during Sleep <br>0 = UARTx clock is turned off during Sleep
             unsigned :8;
#endif
} EZBL_UxMODEBITS;


typedef struct
{
    unsigned RXDA:1;      // bit 0 RXDA: Receive Buffer Data Available bit (read-only) <br>1 = Receive buffer has data, at least one more character can be read <br>0 = Receive buffer is empty
    unsigned OERR:1;      // bit 1 OERR: Receive Buffer Overrun Error Status bit (read/clear only) <br>1 = Receive buffer has overflowed <br>0 = Receive buffer has not overflowed. Clearing a previously set OERR bit (1 → 0 transition) resets the receiver buffer and the UxRSR to the empty state.
    unsigned FERR:1;      // bit 2 FERR: Framing Error Status bit (read-only) <br>1 = Framing error has been detected for the current character (character at the top of the receive FIFO) <br>0 = Framing error has not been detected
    unsigned PERR:1;      // bit 3 PERR: Parity Error Status bit (read-only) <br>1 = Parity error has been detected for the current character (character at the top of the receive FIFO) <br>0 = Parity error has not been detected
    unsigned RIDLE:1;     // bit 4 RIDLE: Receiver Idle bit (read-only) <br>1 = Receiver is Idle <br>0 = Receiver is active
    unsigned ADDEN:1;     // bit 5 ADDEN: Address Character Detect bit (bit 8 of received data = 1) <br>1 = Address Detect mode enabled. If 9-bit mode is not selected, this does not take effect <br>0 = Address Detect mode disabled
    unsigned URXISEL:2;   // bit 7-6 URXISEL<1:0>: Receive Interrupt Mode Selection bits <br>0x3 = Interrupt is set on UxRSR transfer making the receive buffer full (i.e., has 4 data characters) <br>0x2 = Interrupt is set on UxRSR transfer making the receive buffer 3/4 full (i.e., has 3 data characters) <br>0x0 or 0x1 = Interrupt is set when any character is received and transferred from the UxRSR to the receive buffer. Receive buffer has one or more characters.
    unsigned TRMT:1;      // bit 8 TRMT: Transmit Shift Register Empty bit (read-only) <br>1 = Transmit Shift Register is empty and transmit buffer is empty (the last transmission has completed) <br>0 = Transmit Shift Register is not empty, a transmission is in progress or queued
    unsigned UTXBF:1;     // bit 9 UTXBF: Transmit Buffer Full Status bit (read-only) <br>1 = Transmit buffer is full <br>0 = Transmit buffer is not full, at least one more character can be written
    unsigned UTXEN:1;     // bit 10 UTXEN: Transmit Enable bit <br>1 = Transmit enabled, UxTX pin controlled by UARTx <br>0 = Transmit disabled, any pending transmission is aborted and buffer is reset. UxTX pin controlled by port
    unsigned UTXBRK:1;    // bit 11 UTXBRK: Transmit Break bit <br>1 = Send Sync Break on next transmission – Start bit, followed by twelve ‘0’ bits, followed by Stop bit; cleared by hardware upon completion <br>0 = Sync Break transmission disabled or completed
    unsigned URXEN:1;     // bit 12 Varies by device: Unimplemented: Read as ‘0’, OR, URXEN: UARTx Receiver Enable bit <br>1 = UARTx receiver is enabled, UxRX pin is controlled by UARTx (if ON = 1) <br>0 = UARTx receiver is disabled, UxRX pin is ignored by the UARTx module
#if defined(__XC16__)   // NOTE: compiler check only is probably overly generic. Must update for your device!
    // dsPIC33EP64GS506 data
    unsigned UTXISEL0:1;  // bit 15,13 UTXISEL<1:0>: Transmission Interrupt Mode Selection bits <br>0x3 = Reserved; do not use <br>0x2 = Interrupt when a character is transferred to the Transmit Shift register, and as a result, the transmit buffer becomes empty <br>0x1 = Interrupt when the last character is shifted out of the Transmit Shift register; all transmit operations are completed <br>0x0 = Interrupt when a character is transferred to the Transmit Shift register (this implies there is at least one character open in the transmit buffer)
    unsigned UTXINV:1;    // bit 14 UTXINV: Transmit Polarity Inversion bit <br>If IREN = 0: <br>1 = UxTX Idle state is ‘0’ <br>0 = UxTX Idle state is ‘1’ <br>If IREN = 1: <br>1 = IrDA® encoded UxTX Idle state is ‘1’ <br>0 = IrDA® encoded UxTX Idle state is ‘0’
    unsigned UTXISEL1:1;  // bit 15,13 UTXISEL<1:0>: Transmission Interrupt Mode Selection bits <br>0x3 = Reserved; do not use <br>0x2 = Interrupt when a character is transferred to the Transmit Shift register, and as a result, the transmit buffer becomes empty <br>0x1 = Interrupt when the last character is shifted out of the Transmit Shift register; all transmit operations are completed <br>0x0 = Interrupt when a character is transferred to the Transmit Shift register (this implies there is at least one character open in the transmit buffer)
#else
    // PIC32MM0064GPL036 data
    unsigned UTXINV:1;    // bit 14 UTXINV: Transmit Polarity Inversion bit <br>If IREN = 0: <br>1 = UxTX Idle state is ‘0’ <br>0 = UxTX Idle state is ‘1’ <br>If IREN = 1: <br>1 = IrDA® encoded UxTX Idle state is ‘1’ <br>0 = IrDA® encoded UxTX Idle state is ‘0’
    unsigned UTXISEL0:1;  // bit 15-14 UTXISEL<1:0>: Transmission Interrupt Mode Selection bits <br>0x3 = Reserved; do not use <br>0x2 = Interrupt when a character is transferred to the Transmit Shift register, and as a result, the transmit buffer becomes empty <br>0x1 = Interrupt when the last character is shifted out of the Transmit Shift register; all transmit operations are completed <br>0x0 = Interrupt when a character is transferred to the Transmit Shift register (this implies there is at least one character open in the transmit buffer)
    unsigned UTXISEL1:1;  // bit 15-14 UTXISEL<1:0>: Transmission Interrupt Mode Selection bits <br>0x3 = Reserved; do not use <br>0x2 = Interrupt when a character is transferred to the Transmit Shift register, and as a result, the transmit buffer becomes empty <br>0x1 = Interrupt when the last character is shifted out of the Transmit Shift register; all transmit operations are completed <br>0x0 = Interrupt when a character is transferred to the Transmit Shift register (this implies there is at least one character open in the transmit buffer)
#endif
} EZBL_UxSTABITS;

#define _UxSTA_OERR_MASK        0x00000002
#define _UxSTA_FERR_MASK        0x00000004
#define _UxSTA_PERR_MASK        0x00000008
#define _UxMODE_ABAUD_MASK      0x00000020
#define _UxMODE_WAKE_MASK       0x00000080


typedef struct
{
    // UxMODE
    union
    {
        EZBL_UxMODEBITS MODEbits;
        unsigned int MODE;
    };
    union
    {
        EZBL_UxMODEBITS MODECLRbits;
        unsigned int MODECLR;
    };
    union
    {
        EZBL_UxMODEBITS MODESETbits;
        unsigned int MODESET;
    };
    union
    {
        EZBL_UxMODEBITS MODEINVbits;
        unsigned int MODEINV;
    };


    // UxSTA
    union
    {
        EZBL_UxSTABITS STAbits;
        unsigned int STA;
    };
    union
    {
        EZBL_UxSTABITS STACLRbits;
        unsigned int STACLR;
    };
    union
    {
        EZBL_UxSTABITS STASETbits;
        unsigned int STASET;
    };
    union
    {
        EZBL_UxSTABITS STAINVbits;
        unsigned int STAINV;
    };

    // UxTXREG
    unsigned int TXREG;
    unsigned int TXREGCLR;
    unsigned int TXREGSET;
    unsigned int TXREGINV;


    // UxRXREG
    unsigned int RXREG;
    unsigned int RXREGCLR;
    unsigned int RXREGSET;
    unsigned int RXREGINV;

    // UxBRG
    unsigned int BRG;
    unsigned int BRGCLR;
    unsigned int BRGSET;
    unsigned int BRGINV;
} UART_SFRS;


// Rename and change datatype for external SFRs that the processor target will define the address of
static volatile PMDBITS      XC16_NEAR          PMDbits     __attribute__((weakref("PMD1"), section("sfrs")));
static volatile unsigned int XC16_NEAR          UxMODEReg   __attribute__((weakref("U" STRINGIFY(UART_MODULE_NUM) "MODE"), section("sfrs")));
extern volatile unsigned int XC16_NEAR          U1MODE      __attribute__((section("sfrs")));
extern volatile unsigned int XC16_NEAR          U2MODE      __attribute__((weak, section("sfrs")));
extern volatile unsigned int XC16_NEAR          U3MODE      __attribute__((weak, section("sfrs")));



// Allocate RAM for this file using local symbol names. Their public ones are exported further down using aliases.
static volatile UART_SFRS * const XC16_NEAR __attribute__((persistent)) UARTRegs = (volatile UART_SFRS * const)&UxMODEReg;
static EZBL_FIFO __attribute__((persistent))            TxFifo;
static EZBL_FIFO __attribute__((persistent))            RxFifo;
static unsigned char __attribute__((persistent, section(".bss.UART1_TxFifoBuffer"))) TxFifoBuffer[UART_TX_FIFO_SIZE];
static unsigned char __attribute__((persistent, section(".bss.UART1_RxFifoBuffer"))) RxFifoBuffer[UART_RX_FIFO_SIZE];
static unsigned char                                    wakeKeyIndex = 0;      // Bootloader wake up key decoder position
const char __attribute__((weak))                        EZBL_bootloaderWakeKey[] = BOOTLOADER_WAKE_KEY;  // An exact-match RX string to allow EZBL Bootloader code to come alive and start Bootloader command processing when the Application is running.

// Our local prototypes
static void ResetBRG(int UxBRGVal);
static void FIFO_EnableInterrupts(void);
static void FIFO_DisableInterrupts(void);
static void FIFO_ArmAutoBaud(void);
static void TX_FIFO_WaitUntilFlushed(void);
static unsigned int FIFO_QueryAutoBaud(void);
static unsigned int TX_FIFO_OnWrite(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO);
static unsigned int RX_FIFO_OnRead(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *readFIFO);
static unsigned int TX_FIFO_Flush(EZBL_FIFO *fifo, unsigned long *startTime, unsigned long *timeout);
static void RXInterruptFunc(void);
static void TXInterruptFunc(void);
static void __attribute__((interrupt(IPLRXTXSOFT), keep)) RXTXInterrupt(void);


// Object variable symbols for export
extern EZBL_FIFO __attribute__((weak, persistent, alias("RxFifo")))             UART1_RxFifo;
extern EZBL_FIFO __attribute__((weak, persistent, alias("TxFifo")))             UART1_TxFifo;
extern unsigned char __attribute__((weak, persistent, section(".bss.UART1_RxFifoBuffer"), alias("RxFifoBuffer"))) UART1_RxFifoBuffer[UART_RX_FIFO_SIZE];
extern unsigned char __attribute__((weak, persistent, section(".bss.UART1_TxFifoBuffer"), alias("TxFifoBuffer"))) UART1_TxFifoBuffer[UART_TX_FIFO_SIZE];

// Function symbols for export
void __attribute__((weak, alias("ResetBRG")))                   UART1_ResetBRG(int UxBRGVal);
void __attribute__((weak, alias("FIFO_EnableInterrupts")))      UART1_FIFO_EnableInterrupts(void);
void __attribute__((weak, alias("FIFO_DisableInterrupts")))     UART1_FIFO_DisableInterrupts(void);
void __attribute__((weak, alias("FIFO_ArmAutoBaud")))           UART1_FIFO_ArmAutoBaud(void);
void __attribute__((weak, alias("TX_FIFO_WaitUntilFlushed")))   UART1_TX_FIFO_WaitUntilFlushed(void);
unsigned int __attribute__((weak, alias("FIFO_QueryAutoBaud"))) UART1_FIFO_QueryAutoBaud(void);
unsigned int __attribute__((weak, alias("TX_FIFO_OnWrite")))    UART1_TX_FIFO_OnWrite(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO);
unsigned int __attribute__((weak, alias("RX_FIFO_OnRead")))     UART1_RX_FIFO_OnRead(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *readFIFO);
unsigned int __attribute__((weak, alias("TX_FIFO_Flush")))      UART1_TX_FIFO_Flush(EZBL_FIFO *fifo, unsigned long *startTime, unsigned long *timeout);
void __attribute__((weak, alias("RXTXInterrupt")))              UART1_RXTXInterrupt(void);
void __attribute__((weak, alias("RXInterruptFunc")))            UART1_RXInterruptFunc(void);
void __attribute__((weak, alias("TXInterruptFunc")))            UART1_TXInterruptFunc(void);



void ResetBRG(int UxBRGVal)
{
    EZBL_UxMODEBITS newMode =
    {
        .PDSEL  = 0x0,
        .BRGH   = 1,        // Set BRGH for better baud rate resolution/less error at high baud rates.  Should/must be done before setting UTXEN on PIC32MM?
        .ABAUD  = (UxBRGVal <= 0),
        .UARTEN = 1,        // Set UARTEN to turn on the UART.  Should/must be done before setting UTXEN on PIC32MM?
#if defined(__XC32__)
        .CLKSEL = 0x0,      // 0x0 = PBCLK/SYSCLK; 0x1 = Reserved; 0x2 = 2x PBCLK/SYSCLK; 0x3 = 4x PBCLK/SYSCLK - NOTE: Device dependent, check UART in data sheet
        .OVFDIS = 1,        // 1 = After RX Overflow Error (OERR), keep RX running; 0 = On OERR, halt RX until the OERR bit is cleared to acknowledge the RX data stream discontinuity
#endif
    };
    EZBL_UxSTABITS newStat =
    {
        .URXISEL  = 0x0,    // 0x0 = Interrupt continuously when RX hardware has 1+ characters FIFOed; 0x1 = Interrupt continuously while RX hardware half full (4+ characters on PIC32); 0x2 = Interrupt continuously while RX hardware 3/4 full (6+ characters on PIC32); 0x3 = Reserved
        .UTXISEL0 = 0,      // 0x0 = Interrupt continuously when TX hardware has 1+ free FIFO space; 0x1 = Interrupt once when TX FIFO goes empty (8 FIFO free space); 0x2 = Interrupt continuously when TX FIFO is completely empty (8 FIFO spaces free); 0x3 = Reserved
        .UTXISEL1 = 1,
        .UTXEN    = 1,      // Enable TX
        .URXEN    = 1       // Enable RX (device dependent function)
    };


    PMDbits.UxMD_SET = 1;       // Disable the module using the Peripheral Module Disable bit. This clears all internal state and SFRs in the peripheral. When un-disabled, they come back with the reset default values.

    // Start assuming PIC32MM0064GPL036 device (U1MODE @ 0xBF800600, U2MODE @ 0xBF800680)
    RxFifo.irqNum = 23;
    if((const unsigned int)UARTRegs == (const unsigned int)&U2MODE)
        RxFifo.irqNum = 40;

    if(((const unsigned short)(const unsigned int)&U2MODE) == 0x1900u)  // PIC32MM0256GPM064 family device (U1MODE @ 0xBF801800, U2MODE @ 0xBF801900, U3MODE @ 0xBF802000)
    {
        RxFifo.irqNum = 59;
        if(((const unsigned int)UARTRegs) == ((const unsigned int)&U2MODE))
            RxFifo.irqNum = 56;
        else
            RxFifo.irqNum = 53;
    }

    TxFifo.irqNum = RxFifo.irqNum+1;
    EZBL_FIFOReset(&TxFifo, TxFifoBuffer, sizeof(TxFifoBuffer), TX_FIFO_OnWrite, 0);
    EZBL_FIFOReset(&RxFifo, RxFifoBuffer, sizeof(RxFifoBuffer), 0, RX_FIFO_OnRead);
    TxFifo.flushFunction = TX_FIFO_Flush;
    PMDbits.UxMD_CLR = 1;       // Restore module existence
    RxFifo.activity.any = EZBL_COM_ACTIVITY_OTHER_MASK;   // Indicate something happened since we just reset everything and clear the other flags
    TxFifo.activity.any = EZBL_COM_ACTIVITY_OTHER_MASK;   // Indicate something happened since we just reset everything and clear the other flags

    // Write UxMODE, UxSTA, UxBRG registers
    UARTRegs->BRG = UxBRGVal < 0 ? -UxBRGVal : UxBRGVal;
    UARTRegs->MODEbits = newMode;
    UARTRegs->STAbits = newStat;

    EZBL_FIFOSetIntPri(&RxFifo, UART_ISR_PRIORITY);
    EZBL_FIFOSetIntPri(&TxFifo, UART_ISR_PRIORITY);
    EZBL_FIFOIntEnable(&RxFifo);
    EZBL_FIFOIntEnable(&TxFifo);
}


unsigned int TX_FIFO_Flush(EZBL_FIFO *fifo, unsigned long *startTime, unsigned long *timeout)
{
    while(fifo->dataCount)              // Wait for software TX FIFO to empty
    {
        if(UARTRegs->STAbits.TRMT)
        {
            EZBL_FIFOIntEnable(fifo);
            EZBL_FIFOIntRaise(fifo);
        }
        if(UARTRegs->STAbits.TRMT && fifo->dataCount)
            TXInterruptFunc();
        if(NOW_32() - *startTime > *timeout)
            return 0;
    }
    while(!UARTRegs->STAbits.TRMT)         // Wait for TX hardware FIFO to empty
    {
        if(NOW_32() - *startTime > *timeout)
            return 0;
    }
    return 1;
}

/**
 * Blocks execution until everything pending is finished being physically
 * transmitted. The software TX FIFO and the hardware UART TX FIFO are drained
 * to 0 bytes before returning.
 */
void TX_FIFO_WaitUntilFlushed(void)
{
    unsigned long timeout = 0xFFFFFFFF;

    TX_FIFO_Flush(&TxFifo, &timeout, &timeout);
}


/**
 * Enables interrupts used by the UART FIFO TX and RX routines by setting the
 * associated IECx bits.
 */
void FIFO_EnableInterrupts(void)
{
    EZBL_FIFOIntEnable(&RxFifo);
    EZBL_FIFOIntEnable(&TxFifo);
}


/**
 * Disables all interrupts used by the UART FIFO routines by clearing the
 * associated IECx bits.
 */
void FIFO_DisableInterrupts(void)
{
    EZBL_FIFOIntDisable(&RxFifo);
    EZBL_FIFOIntDisable(&TxFifo);
}


/**
 * Turns on hardware Auto-baud rate detection. Next character received must be 
 * 0x55 ('U'), after which UART RX will begin operating normally. UART TX rate
 * can change if transmitting while the auto-baud character is received. The
 * 0x55 character used for baud rate detection is not returned in the software
 * RX FIFO (nor hardware RX FIFO either).
 */
void FIFO_ArmAutoBaud(void)
{
    // Clear framing and other error accumulator if going to auto-baud mode
    RxFifo.activity.any &= ~(EZBL_COM_ACTIVITY_FRAMINGERROR_MASK | EZBL_COM_ACTIVITY_PARITYERROR_MASK | EZBL_COM_ACTIVITY_RXOVERFLOW_MASK);
    RxFifo.activity.other = 1;   // This is a state change worth notification over

    // Enable hardware Auto-baud mode
    UARTRegs->MODESET = _UxMODE_ABAUD_MASK;
}


/**
 * Tests if the RX hardware is armed for Auto-baud detection. If armed, the next
 * RX character is used to set the baud-rate and therefore must be the value 
 * 0x55 ('U'). This character does not get inserted into the RX stream when
 * it is used for auto-baud.
 *
 * @return 1 if auto-baud mode is set (FIFO_ArmAutoBaud() has been called
 *         and a timing character has not been received yet). 0 if auto-baud has
 *         completed or has never been armed.
 */
unsigned int FIFO_QueryAutoBaud(void)
{
    return UARTRegs->MODEbits.ABAUD;
}


/**
 * Callback function automatically invoked anytime a write is made against the
 * UART TX FIFO (with or without any data actually getting written).
 * 
 * This function is called after the write has taken place and is 
 * a convenient place to trigger necessary hardware actions, spy on the data 
 * passing through, possibly teeing it to another communications interface, or 
 * implement additional FIFO features, such as a blocking/almost guaranteed 
 * write with timeout mechanism to simplify other communications code.
 * 
 * This particular implementation triggers the UART TX ISR as needed to ensure 
 * the data doesn't just pile up in the FIFO. Additionally, a blocking construct 
 * with timeout is implemented to make all write calls succeed for all bytes 
 * requested unless physically impossible (ex: UART ISR disabled). The default 
 * timeout is set to 250ms (2,880 bytes @ 115200 baud, 480 bytes @ 19200), but
 * can be changed in the callback code.
 * 
 * @param bytesPushed Number of bytes that actually got written to the FIFO 
 *                    before this callback was invoked.
 * @param *writeSrc Pointer to the memory originally used when the FIFO write 
 *                  call was made. As no other code has executed yet, ordinary 
 *                  RAM data sources can be re-read here to tee the data.
 * @param regWriteLen Number of bytes that were requested to be written when the 
 *                    FIFO write call was made. Generally this will match the 
 *                    bytesPushed value unless the TX FIFO is completely full.
 * @param *destFIFO Pointer to the EZBL_FIFO that called this callback function.
 * 
 * @return Number of bytes that you want to return back to the EZBL_FIFOWrite() 
 *         caller as reportedly being successfully written. Generally you should
 *         return bytesPushed unless you've taken some action to transfer more 
 *         data (or perhaps stolen some back out of the FIFO).
 */
unsigned int TX_FIFO_OnWrite(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO)
{
    unsigned long startTime;
    unsigned int (*onWriteCallbackSave)(unsigned int bytesPushed, void *writeSrc, unsigned int reqWriteLen, EZBL_FIFO *destFIFO);

    // Trigger the TX ISR if we have TX hardware FIFO space to stuff this 
    // new data into
    if(!UARTRegs->STAbits.UTXBF)
    {
        EZBL_FIFOIntRaise(destFIFO);
        EZBL_FIFOIntEnable(destFIFO);
    }
    
    // If the number of bytes written doesn't match what was requested, the FIFO 
    // is full. For simple and efficient procedural flow, we shall block on 
    // UART TX until all bytes are always written.
    if(bytesPushed != reqWriteLen)
    {
        startTime = NOW_32();
        
        // Nullify the onWriteCallback pointer to avoid recursion
        onWriteCallbackSave = destFIFO->onWriteCallback;
        destFIFO->onWriteCallback = (void*)0;

        // Try to write some more residual bytes requested, but not previously 
        // possible. Give up after 250ms.
        do
        {
            bytesPushed += EZBL_FIFOWrite(destFIFO, (void*)((unsigned int)writeSrc + bytesPushed), reqWriteLen - bytesPushed);
            if(!UARTRegs->STAbits.UTXBF)
            {
                EZBL_FIFOIntEnable(destFIFO);
                EZBL_FIFOIntRaise(destFIFO);
                if(EZBL_FIFOGetInt(destFIFO)) // If ISR flag is staying set, this means we are executing from within an ISR @ CPU IPL >= _UxTXIP or the Interrupt is disabled, so we need to fall-back to polling. This could cause coherency bugs, if you reorder the clearing of _UxTXIF in the ISR, so be very careful if doing so!
                {
                    TXInterruptFunc();
                }
            }
        } while((bytesPushed != reqWriteLen) && (NOW_32() - startTime < NOW_sec/4u));

        // Restore onWriteCallback pointer
        destFIFO->onWriteCallback = onWriteCallbackSave;
    }

    TxFifo.activity.softTx = 1;
    return bytesPushed;
}


/**
 * Callback function automatically invoked anytime a software read attempt is
 * made against the UART RX FIFO, but before the underlying EZBL_FIFORead()
 * call returns data to the caller. Provides an early opportunity to block for
 * more data being available, spy on the data passing from the UART hardware
 * into software, auto-generate flow control/status information for the remote
 * node, or potentially allows manipulation of returned data.
 *
 * This function is called at the same IPL context as the caller for
 * EZBL_FIFORead().
 *
 * @param bytesPulled Number of bytes extracted from the FIFO and being
 *                    presently returned to the caller of EZBL_FIFORead(). This
 *                    is the number of valid bytes present at *readDest.
 *
 * @param readDest Pointer to a caller allocated buffer where read data has been
 *                 written for the original call to EZBL_FIFORead(). This
 *                 pointer may be null if the caller is discarding data from the
 *                 FIFO without actually reading it.
 *
 * @param reqReadLen Number of bytes requested to be read when EZBL_FIFORead()
 *                   was called. This number will be < bytesPulled if the FIFO
 *                   did not have enough data to satisfy the original
 *                   EZBL_FIFORead() request. This value may be zero.
 *
 * @param readFIFO Pointer to the EZBL_FIFO that EZBL_FIFORead() was attempted
 *                 against. As the same callback handler could potentially be
 *                 used for multiple FIFOs, this pointer provides a context
 *                 specific handle back to the FIFO that called this callback
 *                 function.
 *
 * @return Specify the number of bytes of data now available at *readDest for
 *         the caller to consume. If you do not add or subtract any data from
 *         the stream within this callback, always return bytesPulled,
 *         unchanged.
 *
 *         This example callback implementation blocks for up to 1 second if a
 *         read for data is made when insufficient data exists in the FIFO to
 *         fulfill the read request. While waiting, any just received data is
 *         copied to the *readDest buffer to allow the original reqReadLen value
 *         to be met (and when met, blocking ceases to ensure smooth program
 *         flow).
 */
unsigned int RX_FIFO_OnRead(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *readFIFO)
{
    unsigned long startTime;
    unsigned int (*onReadCallbackSave)(unsigned int bytesPulled, void *readDest, unsigned int reqReadLen, EZBL_FIFO *srcFIFO);

    // Check if number of bytes read doesn't match what was requested -- i.e.
    // FIFO empty. For simple and efficient procedural flow, we shall block for
    // up to 1 second waiting for the requested data to show up.
    if(bytesPulled != reqReadLen)
    {
        startTime = NOW_32();
        
        // Nullify the onReadCallback pointer to avoid recursion
        onReadCallbackSave = readFIFO->onReadCallback;
        readFIFO->onReadCallback = (void*)0;
        
        // Try to read some more residual bytes, but not previously possible. 
        // Give up after 1 second.
        do
        {
            bytesPulled += EZBL_FIFORead((void*)((unsigned int)readDest + bytesPulled), readFIFO, reqReadLen - bytesPulled);
            if(EZBL_FIFOGetInt(readFIFO))   // If ISR flag is staying set, this means we are executing from within an ISR @ CPU IPL >= _UxRXIP or the Interrupt is disabled, so we need to fall-back to polling. This could cause coherency bugs, if you reorder the clearing of _UxRXIF in the ISR, so be very careful if doing so!
            {
                RXInterruptFunc();
            }
        } while((reqReadLen != bytesPulled) && (NOW_32() - startTime < NOW_sec));

        // Restore onReadCallback pointer
        readFIFO->onReadCallback = onReadCallbackSave;
    }

    RxFifo.activity.softRx = 1;
    return bytesPulled;
}


/*********************************************************************
 * Function:        void RXInterruptFunc(void);
 *                  NOTE: This is not an ISR function. UxRXInterrupt() 
 *                        is the actual ISR.
 *
 * PreCondition:    None
 *
 * Input:           None
 *
 * Output:          Records hardware overflow/framing/etc errors
 *                  into UARTx_RxFifo->activity.
 *
 * Side Effects:    None
 *
 * Overview:        Receives a physical RX byte from the UART and
 *                  places it in a local RAM FIFO for software to
 *                  read it at its leisure.
 *
 * Note:            None
 ********************************************************************/
void RXInterruptFunc(void)
{
    unsigned int i;
    unsigned int status;
    unsigned int newFlags = 0;
    
    // Clear the interrupt flag so we don't keep entering this ISR
    EZBL_FIFOIntClear(&RxFifo);
    
    // Check if this interrupt is due to a wake up from sleep with UxRX pin 
    // start bit assertion edge (pin falling edge)
    if(UARTRegs->MODEbits.WAKE)
    {
        // Yes we just woke up. We can't have this data byte though since no 
        // clocks were running initially. Therefore, just turn off the wake-up
        // feature, clear the Interrupt flag, and return.
        UARTRegs->MODECLR = _UxMODE_WAKE_MASK;
        RxFifo.activity.other = 1;
        return;
    }
    
    // Read all available bytes. This is checked before reading anything because
    // in the event of an auto-baud 0x55 reception, we'll get this RX interrupt,
    // but won't actually have any data to put into the software RX FIFO.
    while(UARTRegs->STAbits.RXDA)
    {
        // Get the byte
        i = UARTRegs->RXREG;
        
        // Collect any Parity (not used, so never should be set), Framing, 
        // and Overrun errors. Parity/Framing errors persist only while the 
        // applicable byte is at the top of the hardware RX FIFO, so we need to 
        // collect it for every byte.
        status = UARTRegs->STA;
        newFlags |= EZBL_COM_ACTIVITY_RX_MASK;
        if(status & _UxSTA_FERR_MASK)
            newFlags |= EZBL_COM_ACTIVITY_FRAMINGERROR_MASK;
        if(status & _UxSTA_PERR_MASK)
            newFlags |= EZBL_COM_ACTIVITY_PARITYERROR_MASK;
        if(status & _UxSTA_OERR_MASK)
        {
            UARTRegs->STACLR = _UxSTA_OERR_MASK;                     // Clear overflow flag if it it has become set. This bit prevents future reception so it must be cleared.
            newFlags |= EZBL_COM_ACTIVITY_RXOVERFLOW_MASK;
        }

        // Check if Bootloader needs waking up
        wakeKeyIndex &= 0x0007;                         // Restrict index to 0-7 so we don't have to bounds check if no crt data initialization is performed (saves perhaps 200 bytes of flash to disable this linker option)
        if(((char)i) == EZBL_bootloaderWakeKey[wakeKeyIndex])
        {   // Might be external node trying to contact Bootloader, so decode the "MCUPHCME" first few bytes to decide if we should activate Bootloader command processing or not
            if(++wakeKeyIndex >= sizeof(EZBL_bootloaderWakeKey))
            {   // All 8 bytes matched exactly, enable the EZBL command processor task
                newFlags |= EZBL_COM_ACTIVITY_BOOTKEYRX_MASK;
                EZBL_COM_RX = &RxFifo;
                EZBL_COM_TX = &TxFifo;
            }
        }
        else
        {
            wakeKeyIndex = 0;
            if(((char)i) == EZBL_bootloaderWakeKey[0]);
                wakeKeyIndex = 1;
        }

        // Throw this byte away if it would cause overflow
        if(RxFifo.dataCount >= RxFifo.fifoSize)
        {
            newFlags |= EZBL_COM_ACTIVITY_RXOVERFLOW_MASK | EZBL_COM_ACTIVITY_OTHER_MASK;    // Flag for our software RX FIFO overflow
            continue;
        }

        // Copy the byte into the local FIFO
        // NOTE: The FIFO internal data structures are being accessed directly
        // here rather than calling the RX_FIFO_Write*() functions because
        // any function call in an ISR will trigger a whole lot of compiler
        // context saving overhead. The compiler has no way of knowing what
        // registers any given function will clobber, so it has to save them
        // all. For efficiency, the needed write-one-byte code is duplicated
        // here.
        *RxFifo.headPtr++ = (unsigned char)i;
        if(RxFifo.headPtr >= RxFifo.fifoRAM + RxFifo.fifoSize)
        {
            RxFifo.headPtr = RxFifo.fifoRAM;
        }
        EZBL_ATOMIC_ADD(RxFifo.dataCount, 1);
    }

    RxFifo.activity.any |= newFlags;      // Write-back gained activity bits
}


/*********************************************************************
 * Function:        void TXInterruptFunc(void);
 *                  NOTE: This is not an ISR function. UxTXInterrupt() 
 *                        is the actual ISR.
 *
 * PreCondition:    None
 *
 * Input:           Zero or more bytes are removed from the software TX FIFO
 *
 * Output:          Bytes removed from the software TX FIFO are written to the 
 *                  hardware UART TX FIFO. On return, the data has not 
 *                  physically been transmitted onto the wire yet.
 *
 * Side Effects:    None
 *
 * Overview:        Transmits physical TX FIFOed bytes out of the UART
 *
 * Note:            None
 ********************************************************************/
void TXInterruptFunc(void)
{
    // Clear the TX interrupt flag before transmitting again
    EZBL_FIFOIntClear(&TxFifo);

    // Transmit a byte, if possible, if pending
    // NOTE: The FIFO internal data structures are being accessed directly
    // here rather than calling the TX_FIFO_Read*() functions because
    // any function call in an ISR will trigger a whole lot of compiler 
    // context saving overhead. The compiler has no way of knowing what 
    // registers any given function will clobber, so it has to save them 
    // all. For efficiency, the needed read-one-byte code is duplicated here.
    while(!UARTRegs->STAbits.UTXBF)
    {
        // Stop loading UART TX REG if there is nothing left to transmit
        if(TxFifo.dataCount == 0u)
        {
            // Switch off the interrupt so we don't keep entering this ISR
            EZBL_FIFOIntDisable(&TxFifo);
            return;
        }

        // Get a byte from the TX buffer
        UARTRegs->TXREG = (unsigned int)*TxFifo.tailPtr++;
        if(TxFifo.tailPtr >= TxFifo.fifoRAM + TxFifo.fifoSize)
            TxFifo.tailPtr = TxFifo.fifoRAM;
        EZBL_ATOMIC_SUB(TxFifo.dataCount, 1);
        TxFifo.activity.tx = 1;
    }
}


/**
 * Wrapper ISR function that calls RXInterruptFunc() or TXInterruptFunc(). When
 * the hardware interrupts are triggered, this is a common entry point for both
 * interrupt handlers. These are merged because the interrupt prologue/epilog
 * code generated by the compiler is big and we don't really need to process
 * these at real-time rates. Merging the two saves ~160 bytes of flash.
 * When software needs to call the interrupt handler, it
 * must call RXInterruptFunc() or TXInterruptFunc() directly and not use this
 * ISR wrapper.
 *
 * Getting both RX and TX interrupt vectors to branch to this single function is
 * done in the UART_Reset() macro, defined in ezbl.h. Only one target function
 * is given for two __attribute__((vector)) options.
 */
extern volatile unsigned int INTSTAT __attribute__((section("sfrs")));
void __attribute__((interrupt(IPLRXTXSOFT), keep)) RXTXInterrupt(void)
{
    unsigned char lastIRQ = (unsigned char)INTSTAT;

    if(lastIRQ == RxFifo.irqNum)
        RXInterruptFunc();
    if(lastIRQ == TxFifo.irqNum)
        TXInterruptFunc();
}
