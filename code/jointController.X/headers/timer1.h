#define TIMER_PRESCALE_1    0b00
#define TIMER_PRESCALE_8    0b01
#define TIMER_PRESCALE_64   0b10
#define TIMER_PRESCALE_256  0b11

typedef enum {
    TIMER_CONFIG_500NS,
    TIMER_CONFIG_1US,
    TIMER_CONFIG_1MS,
    TIMER_CONFIG_1S,
    TIMER_CONFIG_OFF
} TIMER_CONFIGS;

bool timer1_init (TIMER_CONFIGS configuration) {

    switch (configuration) {
        
        case TIMER_CONFIG_500NS:

            _TON = 0;

            _T1IE = 0;
            _T1IF = 0;
            _T1IP = 4;

            TMR1 = 0;
            PR1 = 90;

            _TECS = 0b10;       // Use FOSC
            _TCS = 1;
            _TSYNC = 0;
            _TCKPS = TIMER_PRESCALE_1;      // Input clock prescale 1:8
            _TON = 1;

            _T1IE = 1;

            return true;

        case TIMER_CONFIG_1US:
        
            _TON = 0;

            _T1IE = 0;
            _T1IF = 0;
            _T1IP = 4;

            TMR1 = 0;
            PR1 = 180;

            _TECS = 0b10;       // Use FOSC
            _TCS = 1;
            _TSYNC = 0;
            _TCKPS = TIMER_PRESCALE_1;      // Input clock prescale 1:8
            _TON = 1;

            _T1IE = 1;

            return true;

        case TIMER_CONFIG_1MS:
        
            _TON = 0;

            _T1IE = 0;
            _T1IF = 0;
            _T1IP = 4;

            TMR1 = 0;
            PR1 = 1000;

            _TECS = 0b11;       // Use FRC
            _TCS = 1;
            _TSYNC = 0;
            _TCKPS = TIMER_PRESCALE_8;      // Input clock prescale 1:8
            _TON = 1;

            _T1IE = 1;

            return true;
        
        case TIMER_CONFIG_1S:
        
            _TON = 0;

            _T1IE = 0;
            _T1IF = 0;
            _T1IP = 4;

            TMR1 = 0;
            PR1 = 31250;

            _TECS = 0b11;       // Use FRC
            _TCS = 1;
            _TSYNC = 0;
            _TCKPS = TIMER_PRESCALE_256;      // Input clock prescale 1:256
            _TON = 1;

            _T1IE = 1;

            return true;

        case TIMER_CONFIG_OFF:
            _T1IE = 0;
            return true;

    }

    return false;

}

void __attribute__((__interrupt__, auto_psv)) _T1Interrupt (void) {

    _T1IF = 0;

}