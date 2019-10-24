/*
 * File:   main.c
 * Author: jiego
 *
 * Created on October 22, 2019, 1:15 PM
 */

#include <stdio.h>
#include <math.h>
#include "xc.h"
#include "headers/user_params.h"
#include "libpic30.h"
#include "headers/spi.h"
#include "headers/i2c.h"
#include "headers/lcd_i2c.h"
#include "headers/drv8323.h"
#include "headers/foc.h"

#pragma config FWDTEN = OFF
#pragma config JTAGEN = OFF
#pragma config POSCMOD = NONE
#pragma config OSCIOFNC = ON
#pragma config FCKSM = CSDCMD
#pragma config FNOSC = FRCPLL
#pragma config PLL96MHZ = OFF
#pragma config PLLDIV = NODIV
#pragma config SOSCSEL = IO

double angle = 0.0;

void __attribute__((interrupt, no_auto_psv)) _T1Interrupt (void) {

    _T1IE = 0;
    _T1IF = 0;

    _T1IE = 1;
    _T1IF = 0;

}

void __attribute__((interrupt, no_auto_psv)) _OC1Interrupt (void) {

    _OC1IE = 0;
    _OC1IF = 0;

    _OC1IF = 0;
    _OC1IE = 1;

}

void __attribute__((interrupt, no_auto_psv)) _OC2Interrupt (void) {

    _OC2IE = 0;
    _OC2IF = 0;

    _OC2IF = 0;
    _OC2IE = 1;

}

void t1_init () {

    T1CON = 0x8000;
    _T1IF = 0;
    _T1IP = 6;

    PR1 = 200;

    _T1IE = 1;

}

void oc1_init () {

    OC1CON1 = 0;
    OC1CON2 = 0;

    OC1CON1bits.OCTSEL = 0x07;
    OC1CON1bits.OCM = 0x07;
    OC1CON2bits.SYNCSEL = 0x1F;

    OC1R = 399 / 2;
    OC1RS = 399;

    OC2CON1 = 0;
    OC2CON2 = 0;

    OC2CON1bits.OCTSEL = 0x07;
    OC2CON1bits.OCM = 0x07;
    OC2CON2bits.SYNCSEL = 0x1F;

    OC2R = 399 / 2;
    OC2RS = 399;

    __builtin_write_OSCCONL(OSCCON & 0xBF);

    _RP1R = 18;
    _RP3R = 19;

    __builtin_write_OSCCONL(OSCCON | 0x40);

    _OC1IF = 0;
    _OC1IE = 1;

    _OC2IF = 0;
    _OC2IE = 1;

}

void oc2_init () {

    OC2CON1 = 0;
    OC2CON2 = 0;

    OC2CON1bits.OCTSEL = 0x07;
    OC2CON1bits.OCM = 0x07;
    OC2CON2bits.SYNCSEL = 0x1F;

    __builtin_write_OSCCONL(OSCCON & 0xBF);

    _RP3R = 19;

    __builtin_write_OSCCONL(OSCCON | 0x40);

    OC2R = 399 / 2;
    OC2RS = 399;

    _OC2IF = 0;
    _OC2IE = 1;

}

int main(void) {

    TRISA = 0xFFFF;
    TRISB = 0xFFC0;
    AD1PCFG = 0xFFFF;

    LCD_init();
    oc1_init();
    //oc2_init();

    double angle = 0;
    double angle_delta = 1;

    while (1) {

        svpwm(angle);

        angle += angle_delta;

        if(angle >= 360)
            angle = 0;

        __delay_ms(50);

    }

    return 0;

}
