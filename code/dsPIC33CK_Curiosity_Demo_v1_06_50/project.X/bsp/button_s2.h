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

#ifndef BUTTON_S2_H
#define BUTTON_S2_H

#include <xc.h>
#include <stdbool.h>

/*** Button Definitions *********************************************/
#define BUTTON_S2_PORT  PORTEbits.RE8
#define BUTTON_S2_TRIS  TRISEbits.TRISE8

#define BUTTON_S2_PRESSED 0

static inline bool BUTTON_S2_IsPressed ( void )
{
    BUTTON_S2_TRIS = 1; //input pin
    return ( ( BUTTON_S2_PORT == BUTTON_S2_PRESSED ) ? true : false ) ;
}

#endif

