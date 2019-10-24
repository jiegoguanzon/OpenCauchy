void I2C_init(){	
    I2C1BRG = 4;       
    I2C1CON = 0x2000;
    _I2CEN = 1;
}

void I2C_start(){
    _SEN = 1;
    while(!_S) ;
    Nop();
}

void I2C_restart(){
    _RSEN = 1;
    while(!_S);
    Nop();
}

void I2C_stop(){
    _PEN = 1;
    while(!_P);
}

void I2C_ack(){
    _ACKDT = 0;
    _ACKEN = 1;
    Nop();
}

void I2C_nack(){
    _ACKDT = 1;
    _ACKEN = 1;
    __delay_ms(1);
}

int I2C_write(unsigned char Byte){
    I2C1TRN = Byte;
    while(_TRSTAT );
    return _ACKSTAT;
}

unsigned I2C_read(){
    _RCEN = 1;
    while(!_RBF);
    __delay_ms(1);
    return I2C1RCV;
}