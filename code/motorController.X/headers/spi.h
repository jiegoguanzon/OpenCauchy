#define FCY 4000000

#include "xc.h"
#include "libpic30.h"

#define SPI_SS      _LATB10

#define SPI_WRITE   0
#define SPI_READ    1

#define ADDR_FS     0x0
#define ADDR_VGSS   0x1
#define ADDR_DC     0x2
#define ADDR_GDHS   0x3
#define ADDR_GDLS   0x4
#define ADDR_OCPC   0x5

void spi_init () {

    _TRISB10 = 0;   // Set Slave Select (SS) pin as output
    SPI_SS = 1;    // Disable SS

    SPI1STAT = 0;   // Clear SPI1 Status Register
    SPI1CON1 = 0;   // Clear SPI1 Control Register 1
    SPI1CON2 = 0;   // Clear SPI1 Control Register 2

    _SPI1IF = 0;    // Clear SPI1 Interrupt Flag
    _SPI1IE = 1;    // Enable SPI1 Interrupt

    SPI1CON1bits.MODE16 = 1;    // Word-wide (16 bits) communication
    _SMP = 1;   // Input data sampled at end of data output time
    _MSTEN = 1; // Master mode
    
    // 16:1 Primary Prescaler
    _PPRE0 = 1;
    _PPRE1 = 0;

    // 4:1 Secondary Prescaler
    _SPRE2 = 1;
    _SPRE1 = 0;
    _SPRE0 = 0;

    _SPIROV = 0;    // Clear overflow flag

    __builtin_write_OSCCONL(OSCCON & 0xBF);
    _RP7R = 8;  // SPI1 Clock Output to RB7/RP7
    _SDI1R = 8; // SPI1 Data Input to RB8/RP8
    _RP9R = 7;  // SPI1 Data Output to RB9/RP9
    __builtin_write_OSCCONL(OSCCON | 0x40);

    _SPIEN = 1; // Enable SPI1 module

}

uint16_t spi_write (uint8_t addr, uint16_t data) {

    SPI_SS = 0;    
    SPI1BUF = 0 + (addr << 11) + (data & 0x7FF);
    while (!_SPI1RBF);
    SPI_SS = 1;
    
    return SPI1BUF;

}

uint16_t spi_read (uint8_t addr) {

    SPI_SS = 0;
    SPI1BUF = 1 + (addr << 14);
    while (!_SPI1RBF);
    SPI1BUF = 1 + (addr << 14);
    while (!_SPI1RBF);
    SPI_SS = 1;

    return SPI1BUF;

}