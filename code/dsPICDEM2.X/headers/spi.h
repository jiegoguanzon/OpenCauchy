void spi_init (void) {

        SPI1STAT = 0x0000;
        SPI1CON = 0x0000;

        _MODE16 = 1;            // Word-wide (16 bits) communication
        _SMP = 1;               // Input data sampled at end of data output time
        _MSTEN = 1;             // Master mode

        /*---------- 16:1 Primary Prescaler ----------*/
        _PPRE0 = 1;
        _PPRE1 = 0;

        /*---------- 1:1 Secondary Prescaler ----------*/
        _SPRE0 = 1;
        _SPRE1 = 1;
        _SPRE2 = 1;

        _SPIROV = 0;            // Clear overflow flag

        _SPI1IF = 0;            //Clear the SPI1 Interrupt Flag
        _SPI1IE = 0;            //SPI1 ISR processing is not enabled.

        _SPIEN = 1;             //Turn on the SPI1 module

}