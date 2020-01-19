/*******************************************************************************
 System Initialization File

  File Name:
    system.h

  Summary:
    System Initialization.

  Description:
    This file contains source code necessary to initialize the system.
 *******************************************************************************/

/*******************************************************************************
Copyright (c) 2013 released Microchip Technology Inc.  All rights reserved.

Microchip licenses to you the right to use, modify, copy and distribute
Software only when embedded on a Microchip microcontroller or digital signal
controller that is integrated into your product or third party product
(pursuant to the sublicense terms in the accompanying license agreement).

You should refer to the license agreement accompanying this Software for
additional information regarding your rights and obligations.

SOFTWARE AND DOCUMENTATION ARE PROVIDED AS IS WITHOUT WARRANTY OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
IN NO EVENT SHALL MICROCHIP OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER
CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR
OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR
CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF
SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
(INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *******************************************************************************/

 #ifndef SYSTEM_H
 #define SYSTEM_H
 
// ****************************************************************************
// ****************************************************************************
// Section: System Initialization
// ****************************************************************************
// ****************************************************************************
/*******************************************************************************
  Function:
    void SYS_Initialize ( void )

  Summary:
    Initializes the Explorer 16 board peripherals

  Description:
    This routine initializes the Explorer 16 board peripherals for the demo.
    In a bare-metal environment (where no OS is supported), this routine should
    be called almost immediately after entering the "main" routine.

  Precondition:
    The C-language run-time environment and stack must have been initialized.

  Parameters:
    None.

  Returns:
    None.

  Example:
    <code>
    SYS_INT_Initialize(NULL);
    </code>

  Remarks:
    Basic System Initialization Sequence:

    1.  Initialize minimal board services and processor-specific items
        (enough to use the board to initialize drivers and services)
    2.  Initialize all supported system services
    3.  Initialize all supported modules
        (libraries, drivers, middle-ware, and application-level modules)
    4.  Initialize the main (static) application, if present.

    The order in which services and modules are initialized and started may be
    important.

 */

void SYSTEM_Initialize ( void );

#endif
