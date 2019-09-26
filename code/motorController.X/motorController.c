/*
 * File:   motorController.c
 * Author: jiego
 *
 * Created on September 25, 2019, 6:05 PM
 */

#define FCY 4000000

#include "xc.h"
#include "libpic30.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "headers/spi.h"
#include "headers/lcd_i2c.h"

#pragma config FWDTEN = OFF
#pragma config JTAGEN = OFF
#pragma config POSCMOD = NONE
#pragma config OSCIOFNC = ON
#pragma config FCKSM = CSDCMD
#pragma config FNOSC = FRCPLL
#pragma config PLL96MHZ = OFF
#pragma config PLLDIV = NODIV
#pragma config SOSCSEL = IO

#define INHA    _LATB5
#define INLA    _LATB4
#define INHB    _LATB3
#define INLB    _LATB2
#define INHC    _LATB1
#define INLC    _LATB0

uint8_t timer;

void __attribute__((interrupt, no_auto_psv)) _T1Interrupt (void) {
    _T1IE = 0;
    _T1IF = 0;
    
    timer++;
    
    if (timer >= 1){
        _LATB15 = !_RB15;
        timer = 0;
    }
    
    _T1IE = 1;
    _T1IF = 0;
}

void T1_init(){
    T1CON = 0x8000;
    _T1IF = 0;
    _T1IP = 6;
    PR1 = 80;
    _LATB15 = 0;
    timer = 0;
    _T1IE = 1;
    return;
}

int main (void) {

    TRISA = 0xFFFF;
    TRISB = 0xFFFF;
    AD1PCFG = 0xFFFF;

    uint16_t data = 0;
    uint16_t frame = 0;
    char str[16];

    LCD_init();

    _TRISB0 = 0;
    _TRISB1 = 0;
    _TRISB2 = 0;
    _TRISB3 = 0;
    _TRISB4 = 0; 
    _TRISB15 = 0;
    T1_init();

    spi_init();

    INHA = INHB = INHC = 0;
    INLA = INLB = INLC = 0;

    INLC = 1;

    spi_change_hs_peak_source_gate_current(11);
    spi_change_hs_peak_sink_gate_current(11);
    spi_change_ls_peak_source_gate_current(11);
    spi_change_ls_peak_sink_gate_current(11);
    spi_change_pwm_mode(2);

    frame = spi_generate_frame(SPI_READ, 0x2, 0x0);
    data = spi_transfer(frame);

    sprintf(str, "Frame: 0x%04x", frame);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "Data: 0x%04x", data);
    LCD_send_string(str);

    LCD_cursor_fourth_line();
    LCD_send_string("Starting motor...");
    __delay_ms(2000);
    INLA = INHB = INLB = 1;
    __delay_ms(500);

    uint8_t state = 1;
    
    while (1) {

        switch (state) {

            case 1:
                INLA = 1;
                INHB = 1;
                INLB = 0;
                break;
            case 2:
                INLA = 1;
                INHB = 0;
                INLB = 0;
                break;
            case 3:
                INLA = 1;
                INHB = 0;
                INLB = 1;
                break;
            case 4:
                INLA = 0;
                INHB = 0;
                INLB = 1;
                break;
            case 5:
                INLA = 0;
                INHB = 1;
                INLB = 1;
                break;
            case 6:
                INLA = 0;
                INHB = 1;
                INLB = 0;
                break;
                
        }

        if (++state == 7)
            state = 1;

        //__delay_us(600);
        __delay_ms(5);

    }

    while (1);

    return 0;

}
