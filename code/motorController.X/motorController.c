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
uint8_t state;

uint16_t time_elapsed = 0;
uint16_t time_limit = 1000;
uint16_t time_limit_min = 50;
uint8_t time_elapsed_delta_v = 0;
uint8_t time_limit_delta_v = 200;

uint8_t handoff = 0;

void __attribute__((interrupt, no_auto_psv)) _T1Interrupt (void) {
    _T1IE = 0;
    _T1IF = 0;
    
    time_elapsed++;
    time_elapsed_delta_v++;

    if (time_elapsed_delta_v >= time_limit_delta_v) {
        if (--time_limit <= time_limit_min) {
            time_limit = time_limit_min;
            handoff = 1;
        }
        time_elapsed_delta_v = 0;
    }

    if (handoff) {

        timer++;
        
        if (timer >= time_limit_min / 4){

            switch (state) {

                case 1:
                    INLA = !_RB4;
                    INLB = !_RB2;
                    INLC = 0;
                    break;
                case 2:
                    INLA = !_RB4;
                    INLB = 0;
                    INLC = !_RB0;
                    break;
                case 3:
                    INLA = 0;
                    INLB = !_RB2;
                    INLC = !_RB0;
                    break;
                case 4:
                    INLA = !_RB4;
                    INLB = !_RB2;
                    INLC = 0;
                    break;
                case 5:
                    INLA = !_RB4;
                    INLB = 0;
                    INLC = !_RB0;
                    break;
                case 6:
                    INLA = 0;
                    INLB = !_RB2;
                    INLC = !_RB0;
                    break;

            }

        //_LATB15 = !_RB15;

            timer = 0;
        }

    }
    
    _T1IE = 1;
    _T1IF = 0;
}

void T1_init(){

    T1CON = 0x8000;
    _T1IF = 0;
    _T1IP = 6;

    PR1 = 200;

    //_TRISB15 = 0;
    //_LATB15 = 0;
    timer = 0;

    _T1IE = 1;

    return;
}

int main (void) {

    TRISA = 0xFFFF;
    TRISB = 0xFFC0;
    AD1PCFG = 0xFFFF;

    uint16_t data = 0;
    uint16_t frame = 0;
    char str[16];

    LCD_init();
    spi_init();

    spi_change_hs_peak_source_gate_current(3);
    spi_change_hs_peak_sink_gate_current(3);
    spi_change_ls_peak_source_gate_current(3);
    spi_change_ls_peak_sink_gate_current(3);
    //spi_change_pwm_mode(1);

    INHA = INHB = INHC = 0;
    INLA = INLB = INLC = 0;

    frame = spi_generate_frame(SPI_READ, 0x2, 0x0);
    data = spi_transfer(frame);

    sprintf(str, "Frame: 0x%04x", frame);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "Data: 0x%04x", data);
    LCD_send_string(str);

    state = 1;
    _TRISB15 = 1;
    //_LATB15 = 0;

    uint8_t go = 0;

    while (1) {

        while (go == 0) {
            if (_RB15)
                go++;
        }

        if (go == 1) {
            go++;
            LCD_cursor_third_line();
            LCD_send_string("Starting motor.");
            __delay_ms(1000);
            LCD_send_character('.');
            __delay_ms(1000);
            LCD_send_character('.');
            __delay_ms(1000);
            LCD_cursor_fourth_line();
            LCD_send_string("Motor started.");
            T1_init();
        }

        /*
        if (_RB15) {
            INLA = 0;
            INHA = 0;
            INLB = 0;
            INHB = 0;
            INLC = 0;
            INHC = 0;
            break;
        }
        */


        if (time_elapsed >= time_limit) {
            time_elapsed = 0;

            switch (state) {

                /*
                case 1:
                    INLA = 1;
                    INHA = 1;
                    INLB = 1;
                    INHB = 0;
                    INLC = 0;
                    INHC = 0;
                    break;
                case 2:
                    INLA = 1;
                    INHA = 1;
                    INLB = 0;
                    INHB = 0;
                    INLC = 1;
                    INHC = 0;
                    break;
                case 3:
                    INLA = 0;
                    INHA = 0;
                    INLB = 1;
                    INHB = 1;
                    INLC = 1;
                    INHC = 0;
                    break;
                case 4:
                    INLA = 1;
                    INHA = 0;
                    INLB = 1;
                    INHB = 1;
                    INLC = 0;
                    INHC = 0;
                    break;
                case 5:
                    INLA = 1;
                    INHA = 0;
                    INLB = 0;
                    INHB = 0;
                    INLC = 1;
                    INHC = 1;
                    break;
                case 6:
                    INLA = 0;
                    INHA = 0;
                    INLB = 1;
                    INHB = 0;
                    INLC = 1;
                    INHC = 1;
                    break;
                */
                
                case 1:
                    INHA = 1;
                    INLA = 0;
                    INHB = 0;
                    INLB = 1;
                    INHC = 0;
                    INLC = 0;
                    break;
                case 2:
                    INHA = 1;
                    INLA = 0;
                    INHB = 0;
                    INLB = 0;
                    INHC = 0;
                    INLC = 1;
                    break;
                case 3:
                    INHA = 0;
                    INLA = 0;
                    INHB = 1;
                    INLB = 0;
                    INHC = 0;
                    INLC = 1;
                    break;
                case 4:
                    INHA = 0;
                    INLA = 1;
                    INHB = 1;
                    INLB = 0;
                    INHC = 0;
                    INLC = 0;
                    break;
                case 5:
                    INHA = 0;
                    INLA = 1;
                    INHB = 0;
                    INLB = 0;
                    INHC = 1;
                    INLC = 0;
                    break;
                case 6:
                    INHA = 0;
                    INLA = 0;
                    INHB = 0;
                    INLB = 1;
                    INHC = 1;
                    INLC = 0;
                    break;
                    
            }

            if (++state == 7)
                state = 1;

            //_LATB15 = !_RB15;

        }

    }

    __delay_ms(1000);
    while (!_RB15);

    return 0;

}
