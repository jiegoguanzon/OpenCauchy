#define DRV_ADDR_FS     0x0         // Fault Status 1 Register
#define DRV_ADDR_VGSS   0x1         // VGS Status 2 Register
#define DRV_ADDR_DC     0x2         // Driver Control Register
#define DRV_ADDR_GDHS   0x3         // Gate Drive HS Register
#define DRV_ADDR_GDLS   0x4         // Gate Drive LS Register
#define DRV_ADDR_OCPC   0x5         // OCP Control Register
#define DRV_ADDR_CSA    0x6         // CSA Control Register

void drv_display_faults() {

    uint16_t data = 0;
    uint16_t frame = 0;
    char str[16] = {};

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_FS, 0x0);
    data = spi_transfer(frame);

    LCD_clear();
    LCD_send_string("Fault Status 1:");
    LCD_cursor_second_line();
    sprintf(str, "0x%04x", data);
    LCD_send_string(str);

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_VGSS, 0x0);
    data = spi_transfer(frame);

    LCD_cursor_third_line();
    LCD_send_string("VGS Status 2:");
    LCD_cursor_fourth_line();
    sprintf(str, "0x%04x", data);
    LCD_send_string(str);
    

}

void drv_change_pwm_mode (uint8_t mode) {

    /*
        Modes:
        00b - 6x PWM Mode (Default)
        01b - 3x PWM Mode
        10b - 1x PWM Mode
        11b - Independent PWM Mode
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_DC, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_DC, (data & 0xFF9F) + (mode << 5));
    spi_transfer(frame);

}

void drv_lock_registers () {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_DC, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_DC, (data & 0xF8FF) + (6 << 8));
    spi_transfer(frame);

}

void drv_unlock_registers () {

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_DC, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_DC, (data & 0xF8FF) + (3 << 8));
    spi_transfer(frame);

}

void drv_change_hs_peak_source_gate_current (uint8_t mode) {

    /*
        0000b = 10 mA
        0001b = 30 mA
        0010b = 60 mA
        0011b = 80 mA
        0100b = 120 mA
        0101b = 140 mA
        0110b = 170 mA
        0111b = 190 mA
        1000b = 260 mA
        1001b = 330 mA
        1010b = 370 mA
        1011b = 440 mA
        1100b = 570 mA
        1101b = 680 mA
        1110b = 820 mA
        1111b = 1000 mA (Default)
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_GDHS, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_GDHS, (data & 0xFF0F) + (mode << 4));
    spi_transfer(frame);

}

void drv_change_hs_peak_sink_gate_current (uint8_t mode) {

    /*
        0000b = 20 mA
        0001b = 60 mA
        0010b = 120 mA
        0011b = 160 mA
        0100b = 240 mA
        0101b = 280 mA
        0110b = 340 mA
        0111b = 380 mA
        1000b = 520 mA
        1001b = 660 mA
        1010b = 740 mA
        1011b = 880 mA
        1100b = 1140 mA
        1101b = 1360 mA
        1110b = 1640 mA
        1111b = 2000 mA (Default)
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_GDHS, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_GDHS, (data & 0xFFF0) + mode);
    spi_transfer(frame);

}

void drv_change_ls_peak_source_gate_current (uint8_t mode) {

    /*
        0000b = 10 mA
        0001b = 30 mA
        0010b = 60 mA
        0011b = 80 mA
        0100b = 120 mA
        0101b = 140 mA
        0110b = 170 mA
        0111b = 190 mA
        1000b = 260 mA
        1001b = 330 mA
        1010b = 370 mA
        1011b = 440 mA
        1100b = 570 mA
        1101b = 680 mA
        1110b = 820 mA
        1111b = 1000 mA (Default)
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_GDLS, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_GDLS, (data & 0xFF0F) + (mode << 4));
    spi_transfer(frame);

}

void drv_change_ls_peak_sink_gate_current (uint8_t mode) {

    /*
        0000b = 20 mA
        0001b = 60 mA
        0010b = 120 mA
        0011b = 160 mA
        0100b = 240 mA
        0101b = 280 mA
        0110b = 340 mA
        0111b = 380 mA
        1000b = 520 mA
        1001b = 660 mA
        1010b = 740 mA
        1011b = 880 mA
        1100b = 1140 mA
        1101b = 1360 mA
        1110b = 1640 mA
        1111b = 2000 mA (Default)
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_GDLS, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_GDLS, (data & 0xFFF0) + mode);
    spi_transfer(frame);

}

void drv_change_tdrive (uint8_t mode) {

    /*
        00b = 500 ns peak gate current drive time
        01b = 1000 ns peak gate current drive time
        10b = 2000 ns peak gate current drive time
        11b = 4000 ns peak gate current drive time (Default)
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_GDLS, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_GDLS, (data & 0xFFF0) + (mode << 8));
    spi_transfer(frame);

}

void drv_change_vds (uint8_t mode) {

    /*
        0000b = 0.06 V
        0001b = 0.13 V
        0010b = 0.20 V
        0011b = 0.26 V
        0100b = 0.31 V
        0101b = 0.45 V
        0110b = 0.53 V
        0111b = 0.60 V
        1000b = 0.68 V
        1001b = 0.75 V (Default)
        1010b = 0.94 V
        1011b = 1.13 V
        1100b = 1.30 V
        1101b = 1.50 V
        1110b = 1.70 V
        1111b = 1.88 V
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_GDLS, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_GDLS, (data & 0xFFF0) + mode);
    spi_transfer(frame);

}


void drv_change_deadtime (uint8_t mode) {

    /*
        00b = 50 ns dead time
        01b = 100 ns dead time (Default)
        10b = 200 ns dead time
        11b = 400 ns dead time
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_OCPC, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_OCPC, (data & 0xFFF0) + (mode << 8));
    spi_transfer(frame);

}

void drv_change_csa_gain (uint8_t mode) {

    /*
        00b = 5 V/V current sense amplifier gain
        01b = 10 V/V current sense amplifier gain
        10b = 20 V/V current sense amplifier gain (Default)
        11b = 40 V/V current sense amplifier gain
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_CSA, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_CSA, (data & 0xFFF0) + (mode << 6));
    spi_transfer(frame);

}

void drv_change_sense_lvl (uint8_t mode) {

    /*
        00b = Sense OCP 0.25 V
        01b = Sense OCP 0.50 V
        10b = Sense OCP 0.75 V
        11b = Sense OCP 1.00 V (Default)
    */

    uint16_t data = 0;
    uint16_t frame = 0;

    frame = spi_generate_frame(SPI_READ, DRV_ADDR_CSA, 0x0);
    data = spi_transfer(frame);
    frame = spi_generate_frame(SPI_WRITE, DRV_ADDR_CSA, (data & 0xFFF0) + (mode));
    spi_transfer(frame);

}
