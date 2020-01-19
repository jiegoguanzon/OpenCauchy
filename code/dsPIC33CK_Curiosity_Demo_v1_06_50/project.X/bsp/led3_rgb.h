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

#ifndef LED3_RBG_H
#define LED3_RBG_H

void LED3_RGB_On(void);
void LED3_RGB_Off(void);
void LED3_RGB_Toggle(void);
void LED3_RGB_SetColor(uint16_t red, uint16_t green, uint16_t blue);
/* 3bpp = one bit for red, one bit for green, one bit for blue. 
 * so uint8_t input is = 0b-----rgb */
void LED3_RGB_SetColor3bpp(uint8_t color);

void LED3_RED_On(void);
void LED3_RED_Off(void);
void LED3_RED_Toggle(void);
void LED3_RED_SetIntensity(uint16_t intensity);

void LED3_GREEN_On(void);
void LED3_GREEN_Off(void);
void LED3_GREEN_Toggle(void);
void LED3_GREEND_SetIntensity(uint16_t intensity);

void LED3_BLUE_On(void);
void LED3_BLUE_Off(void);
void LED3_BLUE_Toggle(void);
void LED3_BLUE_SetIntensity(uint16_t intensity);

#endif
