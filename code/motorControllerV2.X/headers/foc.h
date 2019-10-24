#define PI  3.14159265
#define one_over_sqrt3  0.5773502692
#define two_over_sqrt3  1.154700538
#define SQRT3_OVER_TWO  0.8660254038

void svpwm (double angle) {

    double v_dc = 16.0;
    double pwm_period = 1.0 / 10000.0;
    pwm_period = 100.0;

    double v_alpha = 7.0;
    double v_beta = 5.2;

    double v_s = sqrt(v_alpha * v_alpha + v_beta * v_beta);
    double phi = atan(v_beta / v_alpha);
    phi = 340 * PI / 180;
    phi = angle * PI / 180;

    int k = (phi / (PI / 3)) + 1;

    //double m = (1.732050808 * pwm_period * v_s) / v_dc;
    //double m = (1.732050808 * pwm_period * v_s) / v_dc;
    //double m = (1.224744871 * pwm_period * v_s) / v_dc;
    double m = 0.57;

    double period_b = SQRT3_OVER_TWO * m * pwm_period * sin(((PI * k) / 3) - phi);
    double period_c = SQRT3_OVER_TWO * m * pwm_period * sin(-((PI * (k - 1)) / 3) + phi);
    double period_a = pwm_period - period_b - period_c;

    double period_1;
    double period_2;
    double period_3;

    switch (k) {

        case 1:
            period_1 = period_b + period_c + (period_a / 2);
            period_2 = period_c + (period_a / 2);
            period_3 = period_a / 2;
            break;
        case 2:
            period_1 = period_b + (period_a / 2);
            period_2 = period_b + period_c + (period_a / 2);
            period_3 = period_a / 2;
            break;
        case 3:
            period_1 = period_a / 2;
            period_2 = period_b + period_c + (period_a / 2);
            period_3 = period_c + (period_a / 2);
            break;
        case 4:
            period_1 = period_a / 2;
            period_2 = period_b + (period_a / 2);
            period_3 = period_b + period_c + (period_a / 2);
            break;
        case 5:
            period_1 = period_c + (period_a / 2);
            period_2 = period_a / 2;
            period_3 = period_b + period_c + (period_a / 2);
            break;
        case 6:
            period_1 = period_b + period_c + (period_a / 2);
            period_2 = period_a / 2;
            period_3 = period_b + (period_a / 2);
            break;

    }

    OC1R = (uint16_t) (399 - (period_1 / pwm_period * 399));
    //OC1RS = PR1 - OC1R;
    OC2R = (uint16_t) (399 - (period_2 / pwm_period * 399));
    //OC2RS = PR2 - OC2R;

    char str[16] = {};

    /*
    LCD_clear();
    sprintf(str, "v_s: %.8f", v_s);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "phi: %.8f", phi);
    LCD_send_string(str);
    LCD_cursor_third_line();
    sprintf(str, "theta: %.8f", theta);
    LCD_send_string(str);
    __delay_ms(3000);

    LCD_clear();
    sprintf(str, "k: %d", k);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "m: %.8f", m);
    LCD_send_string(str);
    LCD_cursor_third_line();
    sprintf(str, "pwm_period: %.5f", pwm_period * 1000000);
    LCD_send_string(str);
    __delay_ms(3000);
    
    LCD_clear();
    sprintf(str, "period_a: %.5f", period_a * 1000000);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "period_b: %.5f", period_b * 1000000);
    LCD_send_string(str);
    LCD_cursor_third_line();
    sprintf(str, "period_c: %.5f", period_c * 1000000);
    LCD_send_string(str);
    __delay_ms(3000);

    LCD_clear();
    sprintf(str, "period_1: %.5f", period_1 * 1000000);
    LCD_send_string(str);
    LCD_cursor_second_line();
    sprintf(str, "period_2: %.5f", period_2 * 1000000);
    LCD_send_string(str);
    LCD_cursor_third_line();
    sprintf(str, "period_3: %.5f", period_3 * 1000000);
    LCD_send_string(str);
    __delay_ms(3000);

    LCD_clear();
    sprintf(str, "phi: %.5f", phi * 180 / PI);
    LCD_send_string(str);
    */
    

}