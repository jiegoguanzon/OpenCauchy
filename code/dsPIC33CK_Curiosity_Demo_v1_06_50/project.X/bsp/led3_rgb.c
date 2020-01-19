/*******************************************************************************
Copyright 2016 Microchip Technology Inc. (www.microchip.com)

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
#include <xc.h>
#include <stdint.h>

/* This module uses 3 dimmable LEDs (red, green, blue).  It uses them to
 * implement one color changing LED.
 */

#define LED3_RED_LAT     LATEbits.LATE15
#define LED3_RED_TRIS    TRISEbits.TRISE15

#define LED3_GREEN_LAT     LATEbits.LATE14
#define LED3_GREEN_TRIS    TRISEbits.TRISE14

#define LED3_BLUE_LAT     LATEbits.LATE13
#define LED3_BLUE_TRIS    TRISEbits.TRISE13

#define LED_ON  1
#define LED_OFF 0

#define INPUT  1
#define OUTPUT 0


void LED3_RED_On(void)
{
    LED3_RED_TRIS = OUTPUT;
    LED3_RED_LAT = LED_ON;
}

void LED3_RED_Off(void)
{
    LED3_RED_TRIS = OUTPUT;
    LED3_RED_LAT = LED_OFF;
}

void LED3_RED_Toggle(void)
{
	if(LED3_RED_LAT == 1)
    {
        LED3_RED_Off();
    }
    else
    {
        LED3_RED_On();
    }
}

void LED3_RED_SetIntensity(uint16_t new_intensity)
{  
    //Convert 16-bit to 1-bit (no PWMs hooked up to the RGB LED on this board)
    new_intensity >>= 15;
    
	if(new_intensity == 1)
    {
        LED3_RED_On();
    }
    else
    {
        LED3_RED_Off();
    }
}

void LED3_GREEN_On(void)
{
    LED3_GREEN_TRIS = OUTPUT;
    LED3_GREEN_LAT = LED_ON;
}

void LED3_GREEN_Off(void)
{
    LED3_GREEN_TRIS = OUTPUT;
    LED3_GREEN_LAT = LED_OFF;
}

void LED3_GREEN_Toggle(void)
{
	if(LED3_GREEN_LAT == 1)
    {
        LED3_GREEN_Off();
    }
    else
    {
        LED3_GREEN_On();
    }
}

void LED3_GREEN_SetIntensity(uint16_t new_intensity)
{  
    //Convert 16-bit to 1-bit (no PWMs hooked up to the RGB LED on this board)
    new_intensity >>= 15;
    
	if(new_intensity == 1)
    {
        LED3_GREEN_On();
    }
    else
    {
        LED3_GREEN_Off();
    }
}

void LED3_BLUE_On(void)
{
    LED3_BLUE_TRIS = OUTPUT;
    LED3_BLUE_LAT = LED_ON;
}

void LED3_BLUE_Off(void)
{
    LED3_BLUE_TRIS = OUTPUT;
    LED3_BLUE_LAT = LED_OFF;
}

void LED3_BLUE_Toggle(void)
{
	if(LED3_BLUE_LAT == 1)
    {
        LED3_BLUE_Off();
    }
    else
    {
        LED3_BLUE_On();
    }
}

void LED3_BLUE_SetIntensity(uint16_t new_intensity)
{  
    //Convert 16-bit to 1-bit (no PWMs hooked up to the RGB LED on this board)
    new_intensity >>= 15;
    
	if(new_intensity == 1)
    {
        LED3_BLUE_On();
    }
    else
    {
        LED3_BLUE_Off();
    }
}

void LED3_RGB_On(void)
{
    LED3_RED_On();
    LED3_BLUE_On();
    LED3_GREEN_On();  
}

void LED3_RGB_Off(void)
{
    LED3_RED_Off();
    LED3_BLUE_Off();
    LED3_GREEN_Off();  
}

void LED3_RGB_Toggle(void)
{
    LED3_RED_Toggle();
    LED3_BLUE_Toggle();
    LED3_GREEN_Toggle();  
}

void LED3_RGB_SetColor(uint16_t red, uint16_t green, uint16_t blue)
{
    LED3_RED_SetIntensity(red);
    LED3_GREEN_SetIntensity(green);
    LED3_BLUE_SetIntensity(blue);
}


void LED3_RGB_SetColor3bpp(uint8_t color)
{
    enum COLOR_MASK_3BPP {
        COLOR_MASK_3BPP_RED = 0x0004,
        COLOR_MASK_3BPP_GREEN = 0x0002,
        COLOR_MASK_3BPP_BLUE = 0x0001
    } ;
    
    if(color & COLOR_MASK_3BPP_RED)
    {
        LED3_RED_On();
    }
    else
    {
        LED3_RED_Off();
    }
    
    if(color & COLOR_MASK_3BPP_GREEN)
    {
        LED3_GREEN_On();
    }
    else
    {
        LED3_GREEN_Off();
    }
    
    if(color & COLOR_MASK_3BPP_BLUE)
    {
        LED3_BLUE_On();
    }
    else
    {
        LED3_BLUE_Off();
    }
}


