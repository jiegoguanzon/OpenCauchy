/*
 * File:   motorController.c
 * Author: jiego
 *
 * Created on September 25, 2019, 6:05 PM
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

    TRISA = 0xFFFF;
    TRISB = 0xFFFF;
    AD1PCFG = 0xFFFF;

    return 0;

}
