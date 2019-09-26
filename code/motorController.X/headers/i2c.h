#define FCY 4000000

#include "xc.h"
#include "libpic30.h"

// Initialize I2C module
void I2C_init(){	
    I2C1BRG = 4;       
    I2C1CON = 0x2000;
    //_MI2C1IE = 1;
    _I2CEN = 1;
}

// Send start bit sequence
void I2C_start(){
    _SEN = 1;
    while(!_S) ;
    Nop();
    //printstring("1");
    // _MI2C1IF = 0;
}

// Send repeated start bit sequence
void I2C_restart(){
    _RSEN = 1;
    while(!_S);
    Nop();
    //printstring("0");
   // _MI2C1IF = 0;
}

// Send stop bit sequence
void I2C_stop(){
    _PEN = 1;
    while(!_P);
    //printstring("2");
    //_MI2C1IF = 0;
}

// Send ACK bit sequence
void I2C_ack(){
    _ACKDT = 0;
    _ACKEN = 1;
    Nop();
    //printstring("3");
}

// Send NACK bit sequence
void I2C_nack(){
    _ACKDT = 1;
    _ACKEN = 1;
    __delay_ms(1);
    //printstring("4");
}

// Transfer one byte sequence
int I2C_write(unsigned char Byte){
    I2C1TRN = Byte;
    while(_TRSTAT );
    //_MI2C1IF = 0;
    //printstring("5");
    return _ACKSTAT;
}

// Receive one byte sequence
unsigned I2C_read(){
    _RCEN = 1;
    while(!_RBF);
   // _MI2C1IF = 0;
    __delay_ms(1);
    return I2C1RCV;
}