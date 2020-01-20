#define PWM_FREQ_REG    (((50 * 200) / 2) - 1)

void PWM_init () {

    /* Master clock source is selected as High speed PLL clock */
    PCLKCONbits.MCLKSEL = 0b10;
    /* Set PWM Phase Register - No phase shift */
    MPHASE = 0;
    /* Set PWM Period */
    MPER = PWM_FREQ_REG;
    /* Set PWM Duty Cycles */
    //MDC = pwm_gen / 2;
    PG1DC = PWM_FREQ_REG / 2;
    PG2DC = PWM_FREQ_REG / 2;
    PG3DC = PWM_FREQ_REG / 2;
    /* Set Dead Time Registers */
    PG1DTL = 0;
    PG1DTH = 0;
    PG2DTL = 0;
    PG2DTH = 0;
    PG3DTL = 0;
    PG3DTH = 0;

    /* Select PWM Generator Duty Cycle Register as PG1DC */
    /* Select PWM Generator Period Register as MPER */
    /* Select PWM Generator Phase Register as MPHASE */
    /* PWM Generator broadcasts software set of UPDREQ */
    /* control bit and EOC signal to other PWM generators. */
    /* PWM Buffer Update Mode is at start of next PWM cycle if UPDREQ = 1 */
    /* PWM generator operates in Single Trigger Mode */
    /* Start of Cycle is Local EOC */
    PG1CONH = 0x6800;
    /* PWM Generator is disabled */
    /* PWM Generator uses Master Clock selected by
    the PCLKCONbits.MCLKSEL bits */
    /* PWM Generator operates in Center-Aligned mode*/
    PG1CONL = 0x000C;
    /* PWM Generator Output operates in Complementary Mode */
    /* PWM Generator controls the PWMxH output pin */
    /* PWM Generator controls the PWMxL output pin */
    PG1IOCONH = 0x000C;
    /* PGxTRIGA register compare event is enabled as trigger source for
    ADC Trigger 1 */
    /* A write of the PGxDC register automatically sets the UPDREQ bit */
    /* PWM generator trigger output is EOC*/
    PG1EVTL = 0x0008;
    /* Select PWM Generator Duty Cycle Register as PG2DC */
    /* Select PWM Generator Period Register as MPER */
    /* Select PWM Generator Phase Register as MPHASE */
    /* PWM generator does not broadcast UPDATE status bit or EOC signal
    to other PWM generators */
    /* PWM Buffer Update Mode is slaved immediate*/
    /* PWM generator operates in Single Trigger Mode */
    /* Start of Cycle is PG1 trigger output selected by
    PG1EVTbits.PGTRGSEL<2:0> bits */
    PG2CONH = 0x6301;
    /* PWM Generator is enabled */
    /* PWM Generator uses Master Clock selected by
    the PCLKCONbits.MCLKSEL bits */
    /* PWM Generator operates in Center-Aligned mode */
    PG2CONL = 0x800C;
    /* PWM Generator output operates in Complementary Mode */
    /* PWM Generator controls the PWMxH output pin */
    /* PWM Generator controls the PWMxL output pin */
    PG2IOCONH = 0x000C;
    /* Select PWM Generator Duty Cycle Register as PG3DC */
    /* Select PWM Generator Period Register as MPER */
    /* Select PWM Generator Phase Register as MPHASE */
    /* PWM generator does not broadcast UPDATE status bit or EOC signal to
    other PWM generators */
    /* PWM Buffer Update Mode is slaved immediate*/
    /* PWM generator operates in Single Trigger Mode */
    /* Start of Cycle is PG1 trigger output selected by
    PG1EVTbits.PGTRGSEL<2:0> bits */
    PG3CONH = 0x6301;
    /* PWM Generator is enabled */
    /* PWM Generator uses Master Clock selected by the PCLKCONbits.MCLKSEL bits */
    /* PWM Generator operates in Center-Aligned mode */
    PG3CONL = 0x800C;
    /* PWM Generator output operates in Complementary Mode */
    /* PWM Generator controls the PWMxH output pin */
    /* PWM Generator controls the PWMxL output pin */
    PG3IOCONH = 0x000C;
    /* Turning ON the PWM Generator 1;
    Thus starting all the PWM modules in unison */
    PG1CONLbits.ON = 1;

}