#define SPI_WRITE       0
#define SPI_READ        1

void spi_init (void) {

        _SPIEN = 0;

        SPI1STAT = 0x0000;
        SPI1CON = 0x0000;

        _TRISD2 = 0;
        _LATD2 = 1;

        _MODE16 = 1;            // Word-wide (16 bits) communication
        _SMP = 1;               // Input data sampled at end of data output time
        _MSTEN = 1;             // Master mode

        /*---------- 16:1 Primary Prescaler ----------*/
        _PPRE0 = 1;
        _PPRE1 = 0;

        /*---------- 1:1 Secondary Prescaler ----------*/
        _SPRE0 = 0;
        _SPRE1 = 1;
        _SPRE2 = 1;

        _SPIROV = 0;            // Clear overflow flag

        _SPI1IF = 0;            //Clear the SPI1 Interrupt Flag
        _SPI1IE = 0;            //SPI1 ISR processing is not enabled.

        _SPIEN = 1;             //Turn on the SPI1 module

        __delay_ms(5);

}

uint16_t spi_generate_frame (uint8_t op, uint8_t addr, uint16_t data) {
        return (op << 15) + (addr << 11) + data;
}

uint16_t spi_transfer (uint16_t frame) {

        __delay_ms(1);
        _LATD2 = 0;
        __delay_us(500);

        _SPIROV = 0;

        while (_SPITBF);
        SPI1BUF = frame;
        while (!_SPIRBF);

        __delay_us(500);
        _LATD2 = 1;
        __delay_ms(1);

        return SPI1BUF;

}