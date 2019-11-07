#define BAUDRATE        9600L                            // Desired Baud Rate
#define BRGVAL          ((FCY / BAUDRATE) / 16) - 1     // Formula for U1BRG register

char *uart_char_ptr;

void uart_init (void) {

        U2MODE = 0x0000;        // Clear UART2 registers
        U2STA = 0x0000;

        //U2MODEbits.ALTIO = 1;             // Enable U1ATX and U1ARX instead of U1TX and U1RX

        U2BRG = BRGVAL;         // Load UART1 Baud Rate Generator
        U2MODEbits.UARTEN = 1;            // Enable UART1 module

        IFS0bits.U1RXIF = 0;            // Clear UART1 Receiver Interrupt Flag
        IFS0bits.U1TXIF = 0;            // Clear UART1 Transmitter Interrupt Flag
        IEC0bits.U1RXIE = 0;            // Disable UART1 Receiver ISR
        IEC0bits.U1TXIE = 0;            // Enable UART1 Transmitter ISR

        U2STAbits.UTXISEL = 1;           // Interrupt when the transmit buffer becomes empty 

        U2STAbits.UTXEN = 1;             // Enable UART1 transmitter

}

void write_uart_to_rs232 (char *str) {

        uart_char_ptr = &str[0];            // Re-Initialize UART display buffer pointer to point to the first character
        U2TXREG = *uart_char_ptr++;         // Load the UART transmit register with first character

}

void uart_send_byte (char d) {
        while (!U2STAbits.TRMT);
        U2TXREG = d;
}

void uart_send_string (char *str) {
        uint8_t i;
        for (i = 0; str[i] != '\0'; i++)
                uart_send_byte(str[i]);
}

/*
void __attribute__((interrupt, no_auto_psv)) _U1TXInterrupt (void) {

        int i = 0;

        while ((*uart_char_ptr != '\0') && (i < 4)) {
                U1TXREG = *uart_char_ptr++;
                i++;
        }

        IFS0bits.U1TXIF = 0;    // Clear the UART1 transmitter interrupt flag

}
*/
