#include "headers/system.h"
#include "headers/pwm.h"
#include "headers/adc.h"
#include "headers/foc.h"

MEAS_CURR_PARM_T meas_curr_parm;

static void PrintHeader(void);
static void PrintData(void);

//static uint16_t potentiometer;

int main(void) {

    SYSTEM_Initialize();

    //ADC_SetConfiguration(ADC_CONFIGURATION_DEFAULT);
    //ADC_ChannelEnable(ADC_CHANNEL_POTENTIOMETER);
    //ADC_init();
    PWM_init();

    PrintHeader();

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
        
    printf("Measured Current Offset: %d\r\n", 1);
    printf("Measured Currents:\nPhase A: %d\tPhase B: %d\tPhase C: %d\r\n", 1, 2, 3);

}

static void PrintData (void) {

    printf("\033[5;0f");
    printf("Potentiometer: %d/4095    \r\n", potentiometer >> 4);

}
