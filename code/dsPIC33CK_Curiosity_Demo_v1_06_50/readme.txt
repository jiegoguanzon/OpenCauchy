================================================================================
dsPIC33CK Curiosity Development Board - Out of box demo
================================================================================

For more information about this board, please visit:

  www.microchip.com/dsPIC33CKcuriosity 

This site contains code, schematic, and documentation for this board as well as
useful links to other related topics.

Folder Structure
--------------------------------------------------------------------------------
/project.X
  The MPLAB X project folder that holds the source files and demo project for
  the out of box demo

/readme.txt
  This document.

Demo Overview
--------------------------------------------------------------------------------
This firmware demonstrates some of the basic hardware components on the board.  

When button S1 is pressed, LED LED1 will light up.
When button S2 is pressed, LED LED2 will light up.
When button S3 is pressed, the RGB LED will switch to white.

Turning the potentiometer will change the RGB LED color.  The RGB LED is not 
connected to pins where the PWMs can be mapped so the demo uses 
3 bits-per-pixel (3bpp) format for the LED color.  3bpp is one bit for each
color (1 for red, 1 for green, and one for blue) resulting in 8 possible colors
including "black" (all the colors off).

The UART-to-USB port on the board is used to send all of the current board
information to the terminal.  The UART required settings are:
 - Baud Rate: 38400
 - Data: 8 bit
 - Parity: none
 - Stop: 1 stop bit
 - Flow Control: none

The button status (S1, S2, S3) are printed, the potentiometer value, and the 
current RGB color are all printed to the window.



