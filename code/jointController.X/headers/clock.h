#define FOSC        180000000UL
#define FOSC_MHZ    180U
#define FCY         90000000UL
#define FCY_MHZ     90U

void clock_init () {

    CLKDIVbits.PLLPRE = 1;
    PLLFBDbits.PLLFBDIV = 225;
    PLLDIVbits.POST1DIV = 5;
    PLLDIVbits.POST2DIV = 1;
    __builtin_write_OSCCONH (0x01);
    __builtin_write_OSCCONL (OSCCON | 0x01);
    while (OSCCONbits.OSWEN != 0);

}