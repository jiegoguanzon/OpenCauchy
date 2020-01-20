#include "xc.h"
#include "libq.h"
#include "headers/clock.h"
#include "libpic30.h"
#include "headers/system.h"
#include "headers/pwm.h"
#include "headers/adc.h"
#include "headers/foc.h"
#include "headers/timer1.h"

MEAS_CURR_PARM_T meas_curr_parm;

static void PrintHeader(void);
static void PrintData(void);

//static uint16_t potentiometer;

int main(void) {

    CLKDIVbits.PLLPRE = 1;
    PLLFBDbits.PLLFBDIV = 225;
    PLLDIVbits.POST1DIV = 5;
    PLLDIVbits.POST2DIV = 1;
    __builtin_write_OSCCONH (0x01);
    __builtin_write_OSCCONL (OSCCON | 0x01);
    while (OSCCONbits.OSWEN != 0);

    _TRISE6 = 0;
    _LATE6 = 0;

    //CLOCK_Initialize();
    SYSTEM_Initialize();
    //timer1_init(TIMER_CONFIG_250NS);

    //ADC_SetConfiguration(ADC_CONFIGURATION_DEFAULT);
    //ADC_ChannelEnable(ADC_CHANNEL_POTENTIOMETER);
    //ADC_init();
    //PWM_init();
    
    //PrintHeader();

    float y = 0.7;
    _Q15 x;
    x = _Q15ftoi(y);

    printf("x: %d", x);

    while (1) {

        //_LATE6 = !_RE6;
        //__delay_ms(1);

    }

    while (1) {

        //potentiometer = ADC_Read(ADC_CHANNEL_POTENTIOMETER);
        //potentiometer = ADCBUF0;
        //PrintData();

    } 

    while (1);

    return 0;

}

static void PrintHeader (void) {

    printf("\033[2J");      //Clear screen
    printf("\033[0;0f");    //return cursor to 0,0
    printf("\033[?25l");    //disable cursor
        
    //printf("Measured Current Offset: %d\r\n", 1);
    //printf("Measured Currents:\nPhase A: %d\tPhase B: %d\tPhase C: %d\r\n", 1, 2, 3);

}

static void PrintData (void) {

    printf("\033[5;0f");
    printf("Potentiometer: %d/4095    \r\n", potentiometer >> 4);

}
