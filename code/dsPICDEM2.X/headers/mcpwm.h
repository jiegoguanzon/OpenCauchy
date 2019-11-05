#define PWM_FREQ    20000L
#define PRESCALER   1
#define PTPER_VAL   (FCY / (PWM_FREQ * PRESCALER * 2)) - 1

void mcpwm_init (void) {

    PTCON = 0x0000;
    PTMR = 0x0000;
    SEVTCMP = 0x0000;
    PWMCON1 = 0x0000;
    PWMCON2 = 0x0000;
    DTCON1 = 0x0000;

    _PTPER = PTPER_VAL;

}