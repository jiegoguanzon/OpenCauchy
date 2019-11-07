#define PWM_FREQ    20000L
#define PWM_PERIOD  1
#define PRESCALER   1
#define PTPER_VAL   (FCY / (PWM_FREQ * PRESCALER * 2)) - 1

void mcpwm_init (void) {

    PTCON = 0x0000;
    PTMR = 0x0000;
    SEVTCMP = 0x0000;
    PWMCON1 = 0x0000;
    PWMCON2 = 0x0000;
    DTCON1 = 0x0000;

    _PTMOD = 2;                 // Center-aligned PWM Mode

    _PMOD1 = 0;                 // Use high side PWM only
    _PEN1H = 1;                 // Enable high side PWM output
    _PEN1L = 1;                 // Enable high side PWM output
    _PMOD2 = 0;                 // Use high side PWM only
    _PEN2H = 1;                 // Enable high side PWM output
    _PEN2L = 1;                 // Enable high side PWM output
    _PMOD3 = 0;                 // Use high side PWM only
    _PEN3H = 1;                 // Enable high side PWM output
    _PEN3L = 1;                 // Enable high side PWM output

    _PTPER = PTPER_VAL;
    PDC1 = PTPER_VAL / 2;
    PDC2 = PTPER_VAL / 2;
    PDC3 = PTPER_VAL / 2;

    _PWMIF = 0;
    _PWMIE = 1;

    _PTEN = 1;

}

float angle = 0;
float angle_delta = 0.5;

void __attribute__ ((interrupt, no_auto_psv)) _PWMInterrupt (void) {

    angle += angle_delta;

    if (angle >= 360)
        angle = 0;

    _PWMIF = 0;

}