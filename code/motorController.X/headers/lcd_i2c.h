#define FCY 4000000

#include "xc.h"
#include "i2c.h"
#include "libpic30.h"

#define no_of_cols 5
#define LCD_address 0x4E

#define INST_REG 0
#define DATA_REG 1

uint8_t data_0;
uint8_t data_1;
uint8_t data_2;
uint8_t select;
uint8_t cursor_position = 0;

void LCD_send8bits(unsigned char data, uint8_t Rs){
	unsigned char data_l, data_u;
	data_l = ((data<<4) & 0xF0) | Rs;  //select lower nibble by moving it to the upper nibble position
	data_u = (data&0xf0) | Rs;  //select upper nibble
    
	I2C_start();

	I2C_write (LCD_address);
	
    I2C_write(data_u);
    I2C_write(data_u|0x0C);
    I2C_write(data_u|0x08);  
 
    I2C_write(data_l);
	I2C_write(data_l|0x0C);
	I2C_write(data_l|0x08);  
 
	I2C_stop();
    //__delay_us(100);
    __delay_ms(5)
}

void LCD_init(void){

    I2C_init();

    __delay_ms(15);
    LCD_send8bits(0x02, 0);
    __delay_ms(5);
    LCD_send8bits(0x28, 0);
    __delay_ms(5);
    LCD_send8bits(0x01, 0);
    __delay_ms(5);
    LCD_send8bits(0x06, 0);
    __delay_ms(5);
    LCD_send8bits(0x0F, 0);
    __delay_ms(5);

}

void LCD_send_character (char c) {
    LCD_send8bits(c, DATA_REG);
}

void LCD_send_string(char *str){
	while(*str){
        //LCD_send8bits(*str++, 1);
        LCD_send_character(*str++);
    }
}

void LCD_clear(void){
    LCD_send8bits(0x01, 0);
    __delay_ms(10);
}

void LCD_cursor_first_line () {
    LCD_send8bits(0x80, INST_REG);
}

void LCD_cursor_second_line () {
    LCD_send8bits(0xC0, INST_REG);
}

void LCD_cursor_third_line () {
    LCD_send8bits(0x94, INST_REG);
}

void LCD_cursor_fourth_line () {
    LCD_send8bits(0xD4, INST_REG);
}

void LCD_hide_cursor () {
    LCD_send8bits (0xC, INST_REG);
}

void LCD_show_cursor () {
    LCD_send8bits (0xF, INST_REG);
}

void LCD_position_cursor (uint8_t col, uint8_t row) {

    // 0x80 = 128 -> row = 1
    // 0xC0 = 192 -> row = 2
    // 0x94 = 148 -> row = 3
    // 0xD4 = 212 -> row = 4
    // col is from 1 to 20

    LCD_send8bits(((row * 128) - ((row - 1) * 64) - (row / 3) * (108)) + col - 1, INST_REG);

}

void LCD_store_character (uint8_t cgram_addr, char custom_char[]) {
    LCD_send8bits(0x40 + cgram_addr, INST_REG);
    int i = 0;
    for (i = 0; i < 8; i++) {
        LCD_send8bits(custom_char[i], DATA_REG);
    }
    LCD_send8bits(0x02, INST_REG);

}