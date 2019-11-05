#define SPI_WRITE   0
#define SPI_READ    1

void spi_init () {

    _SPIEN = 0;

    _TRISB13 = 0;                               // Set Slave Select (SS) pin as output
    DRV_SS = 1;                                 // Disable SS

    SPI1STAT = 0;                               // Clear SPI1 Status Register
    SPI1CON1 = 0;                               // Clear SPI1 Control Register 1
    SPI1CON2 = 0;                               // Clear SPI1 Control Register 2

    _SPI1IF = 0;                                // Clear SPI1 Interrupt Flag
    _SPI1IE = 1;                                // Enable SPI1 Interrupt

    SPI1CON1bits.MODE16 = 1;                    // Word-wide (16 bits) communication
    _SMP = 1;                                   // Input data sampled at end of data output time
    _MSTEN = 1;                                 // Master mode
    
    /*---------- 16:1 Primary Prescaler ----------*/
    _PPRE0 = 1;
    _PPRE1 = 0;

    /*---------- 4:1 Primary Prescaler ----------*/
    _SPRE2 = 1;
    _SPRE1 = 1;
    _SPRE0 = 1;

    _SPIROV = 0;                                // Clear overflow flag

    /*
    __builtin_write_OSCCONL(OSCCON & 0xBF);
    _RP7R = 8;                                  // SPI1 Clock Output to RB7/RP7
    _SDI1R = 5;                                 // SPI1 Data Input to RA0/RP5
    _RP6R = 7;                                  // SPI1 Data Output to RA1/RP6
    __builtin_write_OSCCONL(OSCCON | 0x40);
    */

    _SPIEN = 1;                                 // Enable SPI1 module

    __delay_ms(5);

}

uint16_t spi_generate_frame (uint8_t op, uint8_t addr, uint16_t data) {
    return (op << 15) + (addr << 11) + data;
}

uint16_t spi_transfer (uint16_t frame) {

    __delay_ms(1);
    DRV_SS = 0;
    __delay_us(500)

    while (_SPITBF);
    SPI1BUF = frame;
    while (!_SPIRBF);

    __delay_us(500)
    DRV_SS = 1;
    __delay_ms(1);

    return SPI1BUF;

}

void __attribute__((interrupt, no_auto_psv)) _SPI1Interrupt (void) {
    _SPI1IF = 0;
}