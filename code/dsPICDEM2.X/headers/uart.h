#define BAUDRATE        9600L                            // Desired Baud Rate
#define BRGVAL          ((FCY / BAUDRATE) / 16) - 1     // Formula for U1BRG register

void uart_init (void) {

        U2MODE = 0x0000;        // Clear UART2 registers
        U2STA = 0x0000;

        U2BRG = BRGVAL;         // Load UART1 Baud Rate Generator
        U2MODEbits.UARTEN = 1;            // Enable UART1 module

        IFS0bits.U1RXIF = 0;            // Clear UART1 Receiver Interrupt Flag
        IFS0bits.U1TXIF = 0;            // Clear UART1 Transmitter Interrupt Flag
        IEC0bits.U1RXIE = 0;            // Disable UART1 Receiver ISR
        IEC0bits.U1TXIE = 0;            // Enable UART1 Transmitter ISR

        U2STAbits.UTXISEL = 1;           // Interrupt when the transmit buffer becomes empty 

        U2STAbits.UTXEN = 1;             // Enable UART1 transmitter

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
