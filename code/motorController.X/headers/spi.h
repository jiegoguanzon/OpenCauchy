#define FCY 4000000

#include "xc.h"
#include "libpic30.h"

#define SPI_SS      _LATB13

#define SPI_WRITE   0
#define SPI_READ    1

#define ADDR_FS     0x0
#define ADDR_VGSS   0x1
#define ADDR_DC     0x2
#define ADDR_GDHS   0x3
#define ADDR_GDLS   0x4
#define ADDR_OCPC   0x5

void spi_init () {

    _SPIEN = 0;

    _TRISB13 = 0;   // Set Slave Select (SS) pin as output
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
    _SPRE1 = 1;
    _SPRE0 = 1;

    _SPIROV = 0;    // Clear overflow flag

    __builtin_write_OSCCONL(OSCCON & 0xBF);
    _RP7R = 8;  // SPI1 Clock Output to RB7/RP7
    _SDI1R = 5; // SPI1 Data Input to RA0/RP5
    _RP6R = 7;  // SPI1 Data Output to RA1/RP6
    __builtin_write_OSCCONL(OSCCON | 0x40);

    _SPIEN = 1; // Enable SPI1 module
    __delay_ms(5);

}

uint16_t spi_generate_frame (uint8_t op, uint8_t addr, uint16_t data) {
    return (op << 15) + (addr << 11) + data;
}

uint16_t spi_transfer (uint16_t frame) {

    __delay_ms(1);
    SPI_SS = 0;
    __delay_us(500)

    while (_SPITBF);
    SPI1BUF = frame;
    while (!_SPIRBF);

    __delay_us(500)
    SPI_SS = 1;
    __delay_ms(1);

    return SPI1BUF;

}

void spi_change_pwm_mode (uint8_t mode) {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x2, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x2, (data & 0xFF9F) + (mode << 5));
    spi_transfer(frame);

}

void spi_lock_registers () {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x2, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x2, (data & 0xF8FF) + (6 << 8));
    spi_transfer(frame);

}

void spi_unlock_registers () {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x2, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x2, (data & 0xF8FF) + (3 << 8));
    spi_transfer(frame);

}

void spi_change_hs_peak_source_gate_current (uint8_t mode) {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x3, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x3, (data & 0xFF0F) + (mode << 4));
    spi_transfer(frame);

}

void spi_change_hs_peak_sink_gate_current (uint8_t mode) {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x3, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x3, (data & 0xFFF0) + mode);
    spi_transfer(frame);

}

void spi_change_ls_peak_source_gate_current (uint8_t mode) {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x4, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x4, (data & 0xFF0F) + (mode << 4));
    spi_transfer(frame);

}

void spi_change_ls_peak_sink_gate_current (uint8_t mode) {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, 0x4, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, 0x4, (data & 0xFFF0) + mode);
    spi_transfer(frame);

}

void __attribute__((interrupt, no_auto_psv)) _SPI1Interrupt (void) {
    _SPI1IF = 0;
}