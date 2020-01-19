/*******************************************************************************
Copyright 2019 Microchip Technology Inc. (www.microchip.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*******************************************************************************/

/******************************************************************************
 Getting Started
 ------------------------------------------------------------------------------
 There is an attached readme.txt file attached to this project (and located in
 the root directory of the project) that describes how to run this demo. 
 ******************************************************************************/

#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>

#include "system.h"

#include "led1.h"
#include "led2.h"

#include "button_s1.h"
#include "button_s2.h"
#include "button_s3.h"

#include "led3_rgb.h"

#include "adc.h"
#include "motor_control_types.h"
#include "motorControl.h"

#include "timer_1ms.h"

static void PrintHeader(void);
static void PrintData(void);

static uint16_t potentiometer;
static bool button_s1_is_pressed;
static bool button_s2_is_pressed;
static bool button_s3_is_pressed;
static uint8_t selected_color_3bpp;
    
static char button_strings[2][12] = {
    { "Not Pressed" },
    { "Pressed    " }
};

static char color_strings[8][8] = {
    { "Black  " },
    { "Blue   " },
    { "Lime   " },
    { "Cyan   " },
    { "Red    " },
    { "Magenta" },
    { "Yellow " },
    { "White  " }
};

MC_SINCOS_T sineCosTheta;

int main(void)
{       
    SYSTEM_Initialize();
            
    //Configure the LEDs off by default
    LED1_Off();
    LED2_Off();
    
    //Enable and configure the ADC so it can sample the potentiometer.
    ADC_SetConfiguration(ADC_CONFIGURATION_DEFAULT);
    ADC_ChannelEnable(ADC_CHANNEL_POTENTIOMETER);
    
    PrintHeader();

    TIMER_SetConfiguration(TIMER_CONFIGURATION_1S);
    
    /*
    while(1)
    {        
        potentiometer = ADC_Read(ADC_CHANNEL_POTENTIOMETER);
        
        button_s1_is_pressed = BUTTON_S1_IsPressed();
        button_s2_is_pressed = BUTTON_S2_IsPressed();
        button_s3_is_pressed = BUTTON_S3_IsPressed();
        
        if(button_s1_is_pressed == true)
        {
            LED1_On();
        }
        else
        {
            LED1_Off();  
        }
        
        if(button_s2_is_pressed == true)
        {
            LED2_On();
        }
        else
        {
            LED2_Off();  
        }
        
        if(button_s3_is_pressed == true)
        {
            //Set RGB LED color to white if S3 is pressed
            selected_color_3bpp = 0b111;
        }
        else
        {
            //Otherwise use the potentiometer to select the RGB LED color.
            
            //16-bit potentiometer reduced down to 3 bits for 3bpp
            selected_color_3bpp = potentiometer >> 13;
        }
        
        LED3_RGB_SetColor3bpp(selected_color_3bpp);

        uint8_t temp = MC_CalculateSineCosine_InlineC_Ram(0x1FFF, &sineCosTheta);
        
        PrintData();
    }
    */

    while (1) ;

}

/*******************************************************************************
 Required baud rate settings:
 - Baud Rate: 38400
 - Data: 8 bit
 - Parity: none
 - Stop: 1 stop bit
 - Flow Control: none
 ******************************************************************************/
static void PrintHeader(void)
{
    printf("\033[2J");      //Clear screen
    printf("\033[0;0f");    //return cursor to 0,0
    printf("\033[?25l");    //disable cursor
        
    printf("------------------------------------------------------------------\r\n");
    printf("dsPIC33CK Curiosity Development Board Demo                        \r\n");
    printf("------------------------------------------------------------------\r\n");
    printf("S1 - controls LED1\r\n");
    printf("S2 - controls LED2\r\n");
    printf("Potentiometer - controls active RGB color\r\n");
    printf("\r\n");
}

static void PrintData(void)
{
    printf("\033[8;0f");    //move cursor to row 8, column 0 (after header)

    printf("Potentiometer: %i/4095    \r\n", potentiometer>>4);
    printf("Button S1: %s\r\n", button_strings[button_s1_is_pressed]);
    printf("Button S2: %s\r\n", button_strings[button_s2_is_pressed]);
    printf("Button S3: %s\r\n", button_strings[button_s3_is_pressed]);
    printf("Current Color: %s\r\n", color_strings[selected_color_3bpp]);
    printf("Sine: %d\r\n", sineCosTheta.sin);
    printf("Cos: %d\r\n", sineCosTheta.cos);
}