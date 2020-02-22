#define DRV_SS  _LATD8

void spi_init () {

    _TRISD8 = 0;
    DRV_SS = 1;

    SPI1CON1L = 0x0000;
    SPI1CON1Lbits.SPIEN = 0;    // Turn off module
    SPI1CON1Lbits.DISSDO = 0;   // SDO1 pin is controlled by module
    SPI1CON1Lbits.MODE = 1;     // 16-bit communication
    SPI1CON1Lbits.SMP = 1;      // Input data sampled at end of data output time
    SPI1CON1Lbits.CKE = 0;      // Transmit happens on falling edge
    SPI1CON1Lbits.SSEN = 0;     // Slave Select Pin not used by module
    SPI1CON1Lbits.CKP = 0;      // Active state for clock is high level
    SPI1CON1Lbits.MSTEN = 1;    // Master Mode
    SPI1CON1Lbits.DISSDI = 0;   // SDI1 pin is controlled by module
    SPI1CON1Lbits.DISSCK = 0;   // SCLK1 pin is controlled by module

    SPI1CON1H = 0x0000;
    SPI1CON2L = 0x0000;

    SPI1BRGL = 4096;
    
    SPI1STATLbits.SPIROV = 0;

    _SPI1IF = 0;
    _SPI1IE = 1;

    SPI1CON1Lbits.SPIEN = 1;
    __delay_ms(5);

}

uint16_t spi_transfer (uint16_t frame) {

    __delay_ms(1);
    DRV_SS = 0;
    __delay_us(500);

    while(SPI1STATLbits.SPITBF);
    SPI1BUFL = frame;
    while (!SPI1STATLbits.SPIRBF);

    __delay_us(500);
    DRV_SS = 1;
    __delay_ms(1);

    return SPI1BUFL;

}

void __attribute__((interrupt, no_auto_psv)) _SPI1Interrupt (void) {
    _SPI1IF = 0;
}