void set_idrive_hs () {

    uint16_t frame = (0 << 15) + (3 << 11) + (3 << 8) + (1 << 4) + (1);
    printf("Frame: 0x%X\n", frame);
    spi_transfer(frame);

}

void set_idrive_ls () {

    uint16_t frame = (0 << 15) + (4 << 11) + (1 << 10) + (3 << 8) + (1 << 4) + (1);
    printf("Frame: 0x%X\n", frame);
    spi_transfer(frame);

}

uint16_t read_ocp () {

    uint16_t frame = 0xA800;
    printf("Frame: 0x%X\n", frame);
    return spi_transfer(frame);

}