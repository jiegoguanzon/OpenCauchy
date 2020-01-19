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

#ifndef BUTTON_S3_H
#define BUTTON_S3_H

#include <xc.h>
#include <stdbool.h>

/*** Button Definitions *********************************************/
#define BUTTON_S3_PORT  PORTEbits.RE9
#define BUTTON_S3_TRIS  TRISEbits.TRISE9

#define BUTTON_S3_PRESSED 0

static inline bool BUTTON_S3_IsPressed ( void )
{
    BUTTON_S3_TRIS = 1; //input pin
    return ( ( BUTTON_S3_PORT == BUTTON_S3_PRESSED ) ? true : false ) ;
}

#endif

