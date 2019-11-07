void adc_init (void) {

        _SSRC = 3;
        _ASAM = 1;

        _SMPI = 1;
        _CSCNA = 1;

        _SAMC = 31;
        _ADCS = 40;

        ADCHS = 0x0000;
        ADCSSL = 0x000C;

        _PCFG2 = 0;
        _PCFG3 = 0;

        _ADIF = 0;
        _ADIE = 0;

        _ADON = 1;

}

int pot = 0;
int temp = 0;

void __attribute__((interrupt, no_auto_psv)) _ADCInterrupt(void) {

    _LATB4 = !_RB4;

    pot = ADCBUF0;
    temp = ADCBUF1;

    IFS0bits.ADIF = 0;

}