#include "xc.h"

void QEI_init () {

    QEI1CONbits.QEIEN = 0;
    QEI1CONbits.PIMOD = 5;
    /*
    QEI1CONbits.IMV = 0;
    QEI1CONbits.INTDIV = 1;
    QEI1CONbits.CNTPOL = 0;
    QEI1CONbits.GATEN = 0;
    QEI1CONbits.CCM = 0;

    QEI1IOCbits.QCAPEN = 0;
    QEI1IOCbits.FLTREN = 0;
    QEI1IOCbits.QFDIV = 6;
    QEI1IOCbits.OUTFNC = 0;
    QEI1IOCbits.SWPAB = 0;
    QEI1IOCbits.HOMPOL = 0;
    QEI1IOCbits.IDXPOL = 0;
    QEI1IOCbits.QEBPOL = 1;
    QEI1IOCbits.QEAPOL = 1;

    QEI1IOCHbits.HCAPEN = 0;
    */

    QEI1CONbits.QEIEN = 1;

}