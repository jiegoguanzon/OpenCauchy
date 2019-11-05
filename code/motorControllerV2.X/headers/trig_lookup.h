#define TABLE_SIZE  512

float sine_table[TABLE_SIZE];
float cosine_table[TABLE_SIZE];

void create_trig_tables () {

    char str[16];
    int i;

    for (i = 0; i < TABLE_SIZE; i++)
        sine_table[i] = sin(PI_OVER_TWO * ((float) i / ((float) TABLE_SIZE - 1.0)));

    for (i = 0; i < TABLE_SIZE; i++)
        cosine_table[i] = cos(PI_OVER_TWO * ((float) i / ((float) TABLE_SIZE - 1.0)));
    

}

float sine_lookup (float rad) {

    int quadrant = (rad / PI_OVER_TWO) + 1;
    int index;
    float sign;

    switch (quadrant) {

        case 1:
            index = (int) ((rad / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = 1.0;
            break;
        case 2:
            index = (int) (((PI - rad) / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = 1.0;
            break;
        case 3:
            index = (int) (((PI - ((3 * PI_OVER_TWO) - rad)) / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = -1.0;
            break;
        case 4:
            index = (int) ((((2 * PI) - rad) / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = -1.0;
            break;

    }

    /*
    char str[16];

    LCD_clear();
    sprintf(str, "qdrnt: %d", quadrant);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "index: %d", index);
    LCD_send_string(str);
    LCD_cursor_third_line();
    sprintf(str, "sign: %.1f", sign);
    LCD_send_string(str);
    LCD_cursor_fourth_line();
    sprintf(str, "val: %.5f", sign * sine_table[index]);
    LCD_send_string(str);
    __delay_ms(3000);
    */

    return sign * sine_table[index];

}

float cosine_lookup (float rad) {

    int quadrant = (rad / PI_OVER_TWO) + 1;
    int index;
    float sign;

    switch (quadrant) {

        case 1:
            index = (int) ((rad / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = 1.0;
            break;
        case 2:
            index = (int) (((PI - rad) / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = -1.0;
            break;
        case 3:
            index = (int) (((PI - ((3 * PI_OVER_TWO) - rad)) / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = -1.0;
            break;
        case 4:
            index = (int) ((((2 * PI) - rad) / PI_OVER_TWO) * (TABLE_SIZE - 1));
            sign = 1.0;
            break;

    }

    return sign * cosine_table[index];

}