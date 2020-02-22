#define MODE 1
#define MODE_2 MODE
#define DRV_ENABLE _LATC0

void set_idrive_hs () {

    uint16_t frame = (0 << 15) + (3 << 11) + (3 << 8) + (MODE << 4) + (MODE_2);
    spi_transfer(frame);

}

void set_idrive_ls () {

    uint16_t frame = (0 << 15) + (4 << 11) + (1 << 10) + (3 << 8) + (MODE << 4) + (MODE_2);
    spi_transfer(frame);

}

uint16_t set_ocp () {

    uint16_t frame = (0 << 15) + (5 << 11) + (0 << 10) + (1 << 8) + (1 << 6) + (1 << 4) + (9);
    return spi_transfer(frame);

}

uint16_t read_ocp () {

    uint16_t frame = (1 << 15) + (5 << 11);
    return spi_transfer(frame);

}

uint16_t read_gdls () {

    uint16_t frame = (1 << 15) + (4 << 11);
    return spi_transfer(frame);

}

uint16_t read_gdhs () {

    uint16_t frame = (1 << 15) + (3 << 11);
    return spi_transfer(frame);

}

uint16_t read_fs1 () {

    uint16_t frame = (1 << 15) + (0 << 11);
    return spi_transfer(frame);

}

uint16_t read_fs2 () {

    uint16_t frame = (1 << 15) + (1 << 11);
    return spi_transfer(frame);

}

uint16_t clear_fault () {

    uint16_t frame = (1 << 15) + (2 << 11);
    uint16_t data = spi_transfer(frame);
    frame = (1 << 15) + (2 << 11) + (data & 0xFFFE) + (1);
    return spi_transfer(frame);

}