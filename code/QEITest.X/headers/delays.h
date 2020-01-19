#include "xc.h"

void delay_ms(uint32_t millis) {

    uint32_t i = 0;

    uint32_t count = millis * 250;

    while (i <= count)
        i++;

}

void delay_us (uint32_t micros) {

    uint32_t i = 0;

    uint32_t count = micros / 4;

    while (i <= count)
        i++;

}