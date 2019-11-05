#define BAUDRATE        9600                            // Desired Baud Rate
#define BRGVAL          ((FCY / BAUDRATE) / 16) - 1     // Formula for U1BRG register

unsigned char *uart_char_ptr;

void uart_init (void) {

        U1MODE = 0x0000;        // Clear UART2 registers
        U1STA = 0x0000;

        _ALTIO = 1;             // Enable U1ATX and U1ARX instead of U1TX and U1RX

        U1BRG = BRGVAL;         // Load UART1 Baud Rate Generator
        _UARTEN = 1;            // Enable UART1 module

        _U1RXIF = 0;            // Clear UART1 Receiver Interrupt Flag
        _U1TXIF = 0;            // Clear UART1 Transmitter Interrupt Flag
        _U1RXIE = 0;            // Disable UART1 Receiver ISR
        _U1TXIE = 1;            // Enable UART1 Transmitter ISR

        _UTXISEL = 1;           // Interrupt when the transmit buffer becomes empty 

        _UTXEN = 1;             // Enable UART1 transmitter

}

void write_uart_to_rs232 (unsigned char *str) {

    uart_char_ptr = &str[0];            // Re-Initialize UART display buffer pointer to point to the first character
    U1TXREG = *uart_char_ptr++;         // Load the UART transmit register with first character

}

void __attribute__((interrupt, no_auto_psv)) _U1TXInterrupt (void) {

        int i = 0;

        while ((*uart_char_ptr != '\0') && (i < 4)) {
                U1TXREG = *uart_char_ptr++;
                i++;
        }

        IFS0bits.U1TXIF = 0;    // Clear the UART1 transmitter interrupt flag

}