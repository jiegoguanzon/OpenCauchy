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
#include <math.h>
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

#define CSA     _RA2
#define CSB     _RA2
#define CSC     _RA2

uint8_t timer;
uint8_t state;

uint16_t time_elapsed = 0;
uint16_t time_limit = 1000;
uint16_t time_limit_min = 50;
uint8_t time_elapsed_delta_v = 0;
uint8_t time_limit_delta_v = 200;

uint8_t handoff = 0;

float rotor_theta = 0;

float i_a = 0;
float i_b = 0;
float i_c = 0;
float i_alpha = 0;
float i_beta = 0;
float i_d = 0;
float i_q = 0;

float v_a = 0;
float v_b = 0;
float v_c = 0;
float v_alpha = 0;
float v_beta = 0;
float v_d = 0;
float v_q = 0;

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

void clark_park_transform() {

    i_a = CSA;
    i_b = CSB;
    i_c = CSC;

    // Clark Trasform 
    i_alpha = i_a;
    i_beta = (i_a + 2 * i_b) / sqrt(3);

    i_d = i_alpha * cos(rotor_theta) + i_beta * sin(rotor_theta);
    i_q = -i_alpha * sin(rotor_theta) + i_beta * cos(rotor_theta);

}

void inv_clark_park_trasform() {

    v_alpha = v_d * cos(rotor_theta) - v_q * sin(rotor_theta);
    v_beta = v_d * sin(rotor_theta) + v_q * cos(rotor_theta);

    v_a = v_beta;
    v_b = (-v_beta + sqrt(3) * v_alpha) / 2;
    v_c = (-v_beta - sqrt(3) * v_alpha) / 2;

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

    INHA = INHB = INHC = 0;
    INLA = INLB = INLC = 0;

    spi_change_hs_peak_source_gate_current(3);
    spi_change_hs_peak_sink_gate_current(3);
    spi_change_ls_peak_source_gate_current(3);
    spi_change_ls_peak_sink_gate_current(3);
    spi_change_pwm_mode(1);

    frame = spi_generate_frame(SPI_READ, 0x0, 0x0);
    data = spi_transfer(frame);

    sprintf(str, "Frame: 0x%04x", frame);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "Data: 0x%04x", data);
    LCD_send_string(str);

    frame = spi_generate_frame(SPI_READ, 0x1, 0x0);
    data = spi_transfer(frame);

    LCD_cursor_third_line();
    sprintf(str, "Frame: 0x%04x", frame);
    LCD_send_string(str);
    LCD_cursor_fourth_line();
    sprintf(str, "Data: 0x%04x", data);
    LCD_send_string(str);

    state = 1;
    _TRISB14 = 1;
    _TRISB15 = 1;

    uint8_t go = 0;

    T1_init();

    while (1) {

        if (!_RB14) {

            __delay_ms(3000);

            frame = spi_generate_frame(SPI_READ, 0x0, 0x0);
            data = spi_transfer(frame);

            LCD_cursor_first_line();
            sprintf(str, "Frame: 0x%04x", frame);
            LCD_send_string(str);
            LCD_cursor_second_line();
            sprintf(str, "Data: 0x%04x", data);
            LCD_send_string(str);

            frame = spi_generate_frame(SPI_READ, 0x1, 0x0);
            data = spi_transfer(frame);

            LCD_cursor_third_line();
            sprintf(str, "Frame: 0x%04x", frame);
            LCD_send_string(str);
            LCD_cursor_fourth_line();
            sprintf(str, "Data: 0x%04x", data);
            LCD_send_string(str);

            break;

        }

        if (time_elapsed >= time_limit) {
            time_elapsed = 0;

            switch (state) {

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

            }

            if (++state == 7)
                state = 1;

            //_LATB15 = !_RB15;

        }

    }

    while (1);

    return 0;

}
