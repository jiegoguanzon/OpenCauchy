/*
 * File:   motorControllerTest.c
 * Author: jiego
 *
 * Created on September 12, 2019, 12:34 PM
 */

#define FCY 4000000

#include "xc.h"
#include "libpic30.h"

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

int main(void) {

    AD1PCFG = 0xFFFF;
    TRISB = 0xFF00;

    uint8_t state = 0;

    while (1) {

        switch (state) {
            case 0:
                INHA = 1;
                INLA = 0;
                INHB = 0;
                INLB = 1;
                INHC = 0;
                INLC = 0;
                break;
            case 1:
                INHA = 1;
                INLA = 0;
                INHB = 0;
                INLB = 0;
                INHC = 0;
                INLC = 1;
                break;
            case 2:
                INHA = 0;
                INLA = 0;
                INHB = 1;
                INLB = 0;
                INHC = 0;
                INLC = 1;
                break;
            case 3:
                INHA = 0;
                INLA = 1;
                INHB = 1;
                INLB = 0;
                INHC = 0;
                INLC = 0;
                break;
            case 4:
                INHA = 0;
                INLA = 1;
                INHB = 0;
                INLB = 0;
                INHC = 1;
                INLC = 0;
                break;
            case 5:
                INHA = 0;
                INLA = 0;
                INHB = 0;
                INLB = 1;
                INHC = 1;
                INLC = 0;
                break;
        }

        if (++state == 6)
            state = 0;

        __delay_ms(500);

    }

    return 0;

}
