#ifndef ADC_H
#define ADC_H

#include <xc.h>
#include <stdint.h>
#include <stdbool.h>

#ifndef FCY
#define FCY 180000000
#endif

/*** ADC Channel Definitions *****************************************/
#define ADC_CHANNEL_POTENTIOMETER ADC_CHANNEL_23

/* This defines number of current offset samples for averaging 
 * If the 2^n samples are considered specify n(in this case 2^7(= 128)=> 7*/
#define  CURRENT_OFFSET_SAMPLE_SCALER         7

#define ADC_RES     2       // 3: 12 bits  2: 10 bits  1: 8 bits   0: 6 bits
#define ADC_FORM    1       // 1: Fractional    0: Integer

#define ADCBUF_SPEED_REF_A      ADCBUF23
#define ADCBUF_INV_A_IPHASE1    ADCBUF0
#define ADCBUF_INV_A_IPHASE2    ADCBUF1
        
#define ClearADCIF()                _ADCAN23IF = 0
#define EnableADCInterrupt()        _ADCAN23IE = 1
#define DisableADCInterrupt()       _ADCAN23IE = 0
#define ClearADCIF_ReadADCBUF()     ADCBUF23

bool adc_init (void) {

    ADCON1L = 0;
    /* ADC Module is turned OFF before configuring it */
    /* ADC Enable bit 1 = ADC module is enabled 0 = ADC module is off         */
    ADCON1Lbits.ADON = 0;
    /* ADC Stop in Idle Mode bit 0 = Continues module operation in Idle mode  */
    ADCON1Lbits.ADSIDL = 0;

    ADCON1H = 0;
    /* Shared ADC Core Resolution Selection bits
       0b11 = 12-bit ; 0b10 = 10-bit ; 0b01 = 8-bit ; 0b00 = 6-bit resolution */
    ADCON1Hbits.SHRRES = ADC_RES;
    /* Fractional Data Output Format bit 1 = Fractional ; 0 = Integer         */
    ADCON1Hbits.FORM = ADC_FORM;

    ADCON2L = 0;
    /* Shared ADC Core Input Clock Divider bits
       These bits determine the number of TCORESRC (Source Clock Periods) 
       for one shared TADCORE (Core Clock Period).
       1111111 = 254 Source Clock Periods
        ???
       0000010 = 4 Source Clock Periods
       0000001 = 2 Source Clock Periods
       0000000 = 2 Source Clock Periods
     */
    ADCON2Lbits.SHRADCS = 0;
    /* EIEN: Early Interrupts Enable bit
       1 = The early interrupt feature is enabled for the input channel 
           interrupts ,when the EISTATx flag is set
       0 = The individual interrupts are generated when conversion is done ,
           when the ANxRDY flag is set*/
    ADCON2Lbits.EIEN = 0;

    ADCON2H = 0;
    /* Shared ADC Core Sample Time Selection bits 
       These bits specify the number of shared ADC Core Clock Periods (TADCORE) 
       for the shared ADC core sample time.
       Ranges from 2 to 1025 TADCORE
       if SHRSAMC = 15 ,then Sampling time is 17 TADCORE */
    ADCON2Hbits.SHRSAMC = 15;

    ADCON3L  = 0;
    /* ADC Reference Voltage Selection bits 
       0b000 = VREFH is AVDD and VREFL is  AVSS */
    ADCON3Lbits.REFSEL = 0;

    ADCON3H = 0;
    /* Dedicated ADC Core 0 Enable bit 
       1 = Dedicated ADC core is enabled 
       0 = Dedicated ADC core is disabled  */
    /* Dedicated ADC Core 0 is Disabled prior to configuration */
    ADCON3Hbits.C0EN      = 0 ;
    /* Dedicated ADC Core 1 Enable bit 
       1 = Dedicated ADC core is enabled 
       0 = Dedicated ADC core is disabled  */
    /* Dedicated ADC Core 1 is Disabled prior to configuration */
    ADCON3Hbits.C1EN      = 0 ;
    /* Shared ADC Core Enable bit 1 = Shared ADC core is enabled 
       0 = Shared ADC core is disabled  */
    /* Shared ADC Core is Disabled prior to configuration */
    ADCON3Hbits.SHREN = 0;
    /* ADC Module Clock Source Selection bits 
       0b11 = FVCO/4;0b10 = AFVCODIV ;0b01 = FOSC ; 0b00 = FP(Peripheral Clock) */
    ADCON3Hbits.CLKSEL = 0;
    /* ADC Module Clock Source Divider bits (1 to 64)
       The divider forms a TCORESRC clock used by all ADC cores (shared and 
       dedicated) from the TSRC ADC module clock selected by the CLKSEL<2:0> .
       000000 = 1 Source Clock Periods */
    ADCON3Hbits.CLKDIV = 0;
    
    /* Initialize ADC CONTROL REGISTER 4 LOW */
    ADCON4L      = 0x0000;
    /* Dedicated ADC Core 0 Conversion Delay Enable bit
       0 = After trigger, the sampling will be stopped immediately and 
           the conversion will be started on the next core clock cycle*/
    ADCON4Lbits.SAMC0EN = 0;
    /* Dedicated ADC Core 1 Conversion Delay Enable bit
       0 = After trigger, the sampling will be stopped immediately and 
           the conversion will be started on the next core clock cycle*/
    ADCON4Lbits.SAMC1EN = 0;
    
    /* Initialize ADC CONTROL REGISTER 4 HIGH */
    ADCON4H      = 0x0000;
    /* Dedicated ADC Core 0 Input Channel Selection bits
       01 = ANA0 00 = AN0 */
    ADCON4Hbits.C0CHS = 0;
    /* Dedicated ADC Core 1 Input Channel Selection bits
       01 = ANA1 00 = AN1 */
    ADCON4Hbits.C1CHS = 0;
    
    /* Initialize DEDICATED ADC CORE 0 CONTROL REGISTER LOW */
    ADCORE0L     = 0x0000;
    /* Dedicated ADC Core 0 Conversion Delay Selection bits 
       These bits determine the time between the trigger event and 
       the start of conversion in the number of the Core Clock Periods (TADCORE)
       Ranges from 2 to 1025 TADCORE
       if SHRSAMC = 8 ,then Sampling time is 10 TADCORE */
    ADCORE0Lbits.SAMC = 8;
    /* Initialize DEDICATED ADC CORE 0 CONTROL REGISTER HIGH */
    ADCORE0H     = 0x0000;
    /* Dedicated ADC Core 0 Input Clock Divider bits
       These bits determine the number of TCORESRC (Source Clock Periods) 
       for one shared TADCORE (Core Clock Period).
       1111111 = 254 Source Clock Periods
        ???
       0000010 = 4 Source Clock Periods
       0000001 = 2 Source Clock Periods
       0000000 = 2 Source Clock Periods
     */
    ADCORE0Hbits.ADCS = 0;
    /* Dedicated ADC Core 0 Resolution Selection bits
       0b11 = 12-bit ; 0b10 = 10-bit ; 0b01 = 8-bit ; 0b00 = 6-bit resolution */
    ADCORE0Hbits.RES = ADC_RES;
    
    /* Initialize DEDICATED ADC CORE 1 CONTROL REGISTER LOW */
    ADCORE1L     = 0x0000;
    /* Dedicated ADC Core 0 Conversion Delay Selection bits 
    These bits determine the time between the trigger event and 
    the start of conversion in the number of the Core Clock Periods (TADCORE)
    Ranges from 2 to 1025 TADCORE
    if SHRSAMC = 8 ,then Sampling time is 10 TADCORE */
    ADCORE1Lbits.SAMC = 8;
    /* Initialize DEDICATED ADC CORE 1 CONTROL REGISTER HIGH */
    ADCORE1H     = 0x0000;
    /* Dedicated ADC Core 1 Input Clock Divider bits
       These bits determine the number of TCORESRC (Source Clock Periods) 
       for one shared TADCORE (Core Clock Period).
       1111111 = 254 Source Clock Periods
        ???
       0000010 = 4 Source Clock Periods
       0000001 = 2 Source Clock Periods
       0000000 = 2 Source Clock Periods
     */
    ADCORE1Hbits.ADCS = 0;
    /* Dedicated ADC Core 1 Resolution Selection bits
       0b11 = 12-bit ; 0b10 = 10-bit ; 0b01 = 8-bit ; 0b00 = 6-bit resolution */
    ADCORE1Hbits.RES = ADC_RES;
    
    /* Configuring ADC INPUT MODE CONTROL REGISTER bits 
       Output Data Sign for Corresponding Analog Inputs bits
       1 = Channel output data is signed
       0 = Channel output data is unsigned    */
    /*ADMOD0L configures Output Data Sign for Analog inputs  AN0 to AN7 */
    ADMOD0L = 0x0000;
    ADMOD0Lbits.SIGN0 = 1;
    ADMOD0Lbits.SIGN1 = 1;
   
    /*ADMOD1L configures Output Data Sign for Analog inputs  AN16 to AN23 */
    ADMOD1L = 0;
    ADMOD1Lbits.SIGN23 = 1;
    
    /* Ensuring all interrupts are disabled and Status Flags are cleared */
    ADIEL = 0;
    ADIEH = 0;
    ADSTATL = 0;
    ADSTATH = 0;
    ADEIEL  = 0;
    ADEIEH  = 0;
    ADEISTATL = 0;
    ADEISTATH = 0;
 
    ADCON5H = 0;
    /* Shared ADC Core Ready Common Interrupt Enable bit
       0 = Common interrupt is disabled for an ADC core ready event*/
    ADCON5Hbits.SHRCIE = 0;
    /* ADC Dedicated Core x Power-up Delay bits
       These bits determine the power-up delay in the number of the 
       Core Source Clock Periods (TCORESRC) for all ADC cores.
       0b1011 = 2048 Source Clock Periods */
    ADCON5Hbits.WARMTIME  = 0b1111 ;                                         
    
    /* ADC Enable bit 1 = ADC module is enabled 0 = ADC module is off         */
    /* Turn on ADC Module */
    ADCON1Lbits.ADON      = 1 ;  
    
    ADCON5L = 0;
    /* Turn on analog power for dedicated core 0 */
    ADCON5Lbits.C0PWR     = 1 ;
    while(ADCON5Lbits.C0RDY == 0);
    /* Dedicated ADC Core 0 Enable bit 
       1 = Dedicated ADC core is enabled 
       0 = Dedicated ADC core is disabled  */
    ADCON3Hbits.C0EN      = 1 ;

    /* Turn on analog power for dedicated core 1 */
    ADCON5Lbits.C1PWR     = 1 ;
    while(ADCON5Lbits.C1RDY == 0);
    /* Dedicated ADC Core 1 Enable bit 
       1 = Dedicated ADC core is enabled 
       0 = Dedicated ADC core is disabled  */
    ADCON3Hbits.C1EN      = 1 ;

    /* Turn on analog power for shared core */
    ADCON5Lbits.SHRPWR    = 1 ;
    while(ADCON5Lbits.SHRRDY == 0);
    /* Shared ADC Core Enable bit 1 = Shared ADC core is enabled 
       0 = Shared ADC core is disabled  */
    /* Shared ADC Core is Enabled  */
    ADCON3Hbits.SHREN     = 1 ;
    
    /* Setup ADC Interrupts for reading and processing converted results */
    /* Common Interrupt Enable bits
       1 = Common and individual interrupts are enabled for analog channel
       0 = Common and individual interrupts are disabled for analog channel*/
    _IE23        = 1 ;
    /* Clear ADC interrupt flag */
    _ADCAN23IF    = 0 ;  
    /* Set ADC interrupt priority IPL 7  */ 
    _ADCAN23IP   = 7 ;  
    /* Disable the AN23 interrupt  */
    _ADCAN23IE    = 0 ;  
    
    _IE0        = 0 ;
    /* Clear ADC interrupt flag */
    _ADCAN0IF    = 0 ;  
    /* Set ADC interrupt priority IPL 7  */ 
    _ADCAN0IP   = 7 ;  
    /* Disable the AN4 interrupt  */
    _ADCAN0IE    = 0 ;  
    
    _IE1        = 0 ;
    /* Clear ADC interrupt flag */
    _ADCAN1IF    = 0 ;  
    /* Set ADC interrupt priority IPL 7  */ 
    _ADCAN1IP   = 7 ;  
    /* Disable the AN1 interrupt  */
    _ADCAN1IE    = 0 ;  
    
    /* Trigger Source Selection for Corresponding Analog Inputs bits 
        00100 = PMW1 Trigger 1
        00001 = Common software trigger
        00000 = No trigger is enabled  */
    
    /* Trigger Source for Analog Input #0  = 0b0100 */
    ADTRIG0Lbits.TRGSRC0 = 0x4;
    /* Trigger Source for Analog Input #1  = 0b0100 */
    ADTRIG0Lbits.TRGSRC1 = 0x4;
    /* Trigger Source for Analog Input #23  = 0b0100 */
    ADTRIG5Hbits.TRGSRC23 = 0x4;

    return true;

}

#endif