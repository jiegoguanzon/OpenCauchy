/*
 * File:   main.c
 * Author: Jiego
 *
 * Created on November 5, 2019, 8:33 PM
 */


// FOSC
#pragma config FPR = XT_PLL16           // Primary Oscillator Mode (XT w/PLL 8x)
#pragma config FOS = PRI                // Oscillator Source (Internal Fast RC)
#pragma config FCKSMEN = CSW_FSCM_OFF   // Clock Switching and Monitor (Sw Disabled, Mon Disabled)

// FWDT
#pragma config FWPSB = WDTPSB_16        // WDT Prescaler B (1:16)
#pragma config FWPSA = WDTPSA_512       // WDT Prescaler A (1:512)
#pragma config WDT = WDT_OFF            // Watchdog Timer (Disabled)

// FBORPOR
#pragma config FPWRT = PWRT_OFF         // POR Timer Value (Timer Disabled)
#pragma config BODENV = BORV20          // Brown Out Voltage (Reserved)
#pragma config BOREN = PBOR_ON          // PBOR Enable (Enabled)
#pragma config LPOL = PWMxL_ACT_HI      // Low-side PWM Output Polarity (Active High)
#pragma config HPOL = PWMxH_ACT_HI      // High-side PWM Output Polarity (Active High)
#pragma config PWMPIN = RST_IOPIN       // PWM Output Pin Reset (Control with PORT/TRIS regs)
#pragma config MCLRE = MCLR_EN          // Master Clear Enable (Enabled)

// FGS
#pragma config GWRP = GWRP_OFF          // General Code Segment Write Protect (Disabled)
#pragma config GCP = CODE_PROT_OFF      // General Segment Code Protection (Disabled)

// FICD
#pragma config ICS = ICS_PGD            // Comm Channel Select (Use PGC/EMUC and PGD/EMUD)

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include "xc.h"
#include "headers/system_params.h"
#include "libpic30.h"
#include "headers/uart.h"
#include "headers/spi.h"
#include "headers/mcpwm.h"
#include "headers/foc.h"
#include "headers/adc.h"

char uart_str[256];

float phase_a_current, phase_b_current, phase_c_current;
float i_alpha, i_beta;
float i_d, i_q;
float v_alpha, v_beta;

int main (void) {

    ADPCFG = 0xFFFF;                    // Reset all ports to digital operation.

    __delay_ms(10);                    // Provide 500 ms delay for LCD to start-up.

    _TRISB4 = 0;
    _LATB4 = 0;

    uart_init();                        // Initialize UART1 module.
    spi_init();                         // Initialize SPI1 module.
    //mcpwm_init();                       // Initialize MCPWM module.
    //adc_init();

    uint16_t data = 0;
    uint16_t frame = 0;
    
    frame = spi_generate_frame(SPI_READ, 0x2, 0x0);
    data = spi_transfer(frame);
    sprintf(uart_str, "Frame: 0x%.4x\nData: 0x%.4x\n", frame, data);
    uart_send_string(uart_str);

    __delay_ms(100);

    frame = spi_generate_frame(SPI_WRITE, 0x2, (data & 0xFF9F) + (1 << 5));
    data = spi_transfer(frame);
    sprintf(uart_str, "Frame: 0x%.4x\nData: 0x%.4x\n", frame, data);
    uart_send_string(uart_str);

    while (1) {

        //foc_inv_park_transform(&v_alpha, &v_beta, 0, 0, angle * PI / 180);
        //foc_svpwm(v_alpha, v_beta);

    }


    while (1);

    return 0;

}
