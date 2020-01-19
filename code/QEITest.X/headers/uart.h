#include <xc.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#ifdef __cplusplus  // Provide C++ Compatibility

    extern "C" {

#endif
        
/**
  Section: Data Types
*/

/** UART1 Driver Hardware Flags

  @Summary
    Specifies the status of the hardware receive or transmit

  @Description
    This type specifies the status of the hardware receive or transmit.
    More than one of these values may be OR'd together to create a complete
    status value.  To test a value of this type, the bit of interest must be
    AND'ed with value and checked to see if the result is non-zero.
*/

typedef enum
{
   
    /* Indicates that Receive buffer has overflowed */
    UART1_RX_OVERRUN_ERROR
        /*DOM-IGNORE-BEGIN*/  = (1 << 1) /*DOM-IGNORE-END*/,

    /* Indicates that Receive break interrupt was received */
    UART1_RX_BREAK_INT_FLAG
        /*DOM-IGNORE-BEGIN*/  = (1 << 2) /*DOM-IGNORE-END*/,
        
    /* Indicates that Framing error has been detected for the current character */
    UART1_FRAMING_ERROR
        /*DOM-IGNORE-BEGIN*/  = (1 << 3) /*DOM-IGNORE-END*/,
    
    /* Indicates that Auto-Baud rate acquisition interrupt was received */
    UART1_AUTOBAUD_RATE_ACQ_INT_FLAG
        /*DOM-IGNORE-BEGIN*/  = (1 << 5) /*DOM-IGNORE-END*/,

    /* Indicates that Parity error has been detected for the current character */
    UART1_PARITY_ERROR
        /*DOM-IGNORE-BEGIN*/  = (1 << 6) /*DOM-IGNORE-END*/,

    /* Indicates that Transmit shifter empty interrupt was received */
    UART1_TX_SHIFTER_EMPTY_INT_FLAG
        /*DOM-IGNORE-BEGIN*/  = (1 << 7) /*DOM-IGNORE-END*/,
     
    /* Indicates that Receive buffer is full */
    UART1_RX_BUFFER_FULL
        /*DOM-IGNORE-BEGIN*/  = (1UL << 16) /*DOM-IGNORE-END*/,
    
    /* Indicates that Receive buffer is empty */
    UART1_RX_BUFFER_EMPTY
        /*DOM-IGNORE-BEGIN*/  = (1UL << 17) /*DOM-IGNORE-END*/,
        
    /* Indicates that Receiver is Idle */
    UART1_RECEIVER_IDLE
        /*DOM-IGNORE-BEGIN*/  = (1UL << 19) /*DOM-IGNORE-END*/,

    /* Indicates that Transmit buffer is full */
    UART1_TX_BUFFER_FULL
        /*DOM-IGNORE-BEGIN*/  = (1UL << 20) /*DOM-IGNORE-END*/,
    
    /* Indicates that Transmit buffer is empty */
    UART1_TX_BUFFER_EMPTY
        /*DOM-IGNORE-BEGIN*/  = (1UL << 21) /*DOM-IGNORE-END*/,
        
    /* Indicates that Stop bit detection mode is set */
    UART1_STOP_DETECT_MODE
        /*DOM-IGNORE-BEGIN*/  = (1UL << 22) /*DOM-IGNORE-END*/,
        
    /* Indicates that TX Write transmit error has been detected for the current character */
    UART1_TX_WRITE_TX_ERROR
        /*DOM-IGNORE-BEGIN*/  = (1UL << 23) /*DOM-IGNORE-END*/,

}UART1_STATUS;

/** UART1 Driver Transfer Flags

  @Summary
    Specifies the status of the receive or transmit

  @Description
    This type specifies the status of the receive or transmit operation.
    More than one of these values may be OR'd together to create a complete
    status value.  To test a value of this type, the bit of interest must be
    AND'ed with value and checked to see if the result is non-zero.
*/

typedef enum
{
    /* Indicates that the core driver buffer is full */
    UART1_TRANSFER_STATUS_RX_FULL
        /*DOM-IGNORE-BEGIN*/  = (1 << 0) /*DOM-IGNORE-END*/,

    /* Indicates that at least one byte of Data has been received */
    UART1_TRANSFER_STATUS_RX_DATA_PRESENT
        /*DOM-IGNORE-BEGIN*/  = (1 << 1) /*DOM-IGNORE-END*/,

    /* Indicates that the core driver receiver buffer is empty */
    UART1_TRANSFER_STATUS_RX_EMPTY
        /*DOM-IGNORE-BEGIN*/  = (1 << 2) /*DOM-IGNORE-END*/,

    /* Indicates that the core driver transmitter buffer is full */
    UART1_TRANSFER_STATUS_TX_FULL
        /*DOM-IGNORE-BEGIN*/  = (1 << 3) /*DOM-IGNORE-END*/,

    /* Indicates that the core driver transmitter buffer is empty */
    UART1_TRANSFER_STATUS_TX_EMPTY
        /*DOM-IGNORE-BEGIN*/  = (1 << 4) /*DOM-IGNORE-END*/

} UART1_TRANSFER_STATUS;

typedef union
{
    struct
    {
            uint8_t full:1;
            uint8_t empty:1;
            uint8_t reserved:6;
    }s;
    uint8_t status;
}

UART_BYTEQ_STATUS;

/** UART Driver Hardware Instance Object

  @Summary
    Defines the object required for the maintenance of the hardware instance.

*/

typedef struct
{
    /* RX Byte Q */
    uint8_t                                      *rxTail ;

    uint8_t                                      *rxHead ;

    /* TX Byte Q */
    uint8_t                                      *txTail ;

    uint8_t                                      *txHead ;

    UART_BYTEQ_STATUS                        rxStatus ;

    UART_BYTEQ_STATUS                        txStatus ;

} UART_OBJECT ;

static volatile UART_OBJECT uart1_obj ;

/** UART Driver Queue Length

  @Summary
    Defines the length of the Transmit and Receive Buffers

*/

#define UART1_CONFIG_TX_BYTEQ_LENGTH 8
#define UART1_CONFIG_RX_BYTEQ_LENGTH 8

/** UART Driver Queue

  @Summary
    Defines the Transmit and Receive Buffers

*/

static uint8_t uart1_txByteQ[UART1_CONFIG_TX_BYTEQ_LENGTH] ;
static uint8_t uart1_rxByteQ[UART1_CONFIG_RX_BYTEQ_LENGTH] ;

/**
  Section: Driver Interface
*/

void UART1_Enable(void)
{
    U1MODEbits.UARTEN = 1;
    U1MODEbits.UTXEN = 1; 
    U1MODEbits.URXEN = 1;
}

void UART1_Initialize(void)
{
    // URXEN disabled; RXBIMD RXBKIF flag when Break makes low-to-high transition after being low for at least 23/11 bit periods; UARTEN enabled; MOD Asynchronous 8-bit UART; UTXBRK disabled; BRKOVR TX line driven by shifter; UTXEN disabled; USIDL disabled; WAKE disabled; ABAUD disabled; BRGH enabled; 
    // Data Bits = 8; Parity = None; Stop Bits = 1 Stop bit sent, 1 checked at RX;
    U1MODE = (0x8080 & ~(1<<15));  // disabling UARTEN bit
    // STSEL 1 Stop bit sent, 1 checked at RX; BCLKMOD disabled; SLPEN disabled; FLO Off; BCLKSEL FOSC/2; C0EN disabled; RUNOVF disabled; UTXINV disabled; URXINV disabled; HALFDPLX disabled; 
    U1MODEH = 0x00;
    // OERIE disabled; RXBKIF disabled; RXBKIE disabled; ABDOVF disabled; OERR disabled; TXCIE disabled; TXCIF disabled; FERIE disabled; TXMTIE disabled; ABDOVE disabled; CERIE disabled; CERIF disabled; PERIE disabled; 
    U1STA = 0x00;
    // URXISEL RX_ONE_WORD; UTXBE enabled; UTXISEL TX_BUF_EMPTY; URXBE enabled; STPMD disabled; TXWRE disabled; 
    U1STAH = 0x22;
	// BaudRate = 38400; Frequency = 4000000 Hz; BRG 25; 
    U1BRG = 0x19;
    // BRG 0; 
    U1BRGH = 0x00;
    // P1 0; 
    U1P1 = 0x00;
    // P2 0; 
    U1P2 = 0x00;
    // P3 0; 
    U1P3 = 0x00;
    // P3H 0; 
    U1P3H = 0x00;
    // TXCHK 0; 
    U1TXCHK = 0x00;
    // RXCHK 0; 
    U1RXCHK = 0x00;
    // T0PD 1 ETU; PTRCL disabled; TXRPT Retransmit the error byte once; CONV Direct logic; 
    U1SCCON = 0x00;
    // TXRPTIF disabled; TXRPTIE disabled; WTCIF disabled; WTCIE disabled; BTCIE disabled; BTCIF disabled; GTCIF disabled; GTCIE disabled; RXRPTIE disabled; RXRPTIF disabled; 
    U1SCINT = 0x00;
    // ABDIF disabled; WUIF disabled; ABDIE disabled; 
    U1INT = 0x00;

    IEC0bits.U1RXIE = 1;
    
     //Make sure to set LAT bit corresponding to TxPin as high before UART initialization
    UART1_Enable();  // enabling UARTEN bit
    
    uart1_obj.txHead = uart1_txByteQ;
    uart1_obj.txTail = uart1_txByteQ;
    uart1_obj.rxHead = uart1_rxByteQ;
    uart1_obj.rxTail = uart1_rxByteQ;
    uart1_obj.rxStatus.s.empty = true;
    uart1_obj.txStatus.s.empty = true;
    uart1_obj.txStatus.s.full = false;
    uart1_obj.rxStatus.s.full = false;
}

/**
    Maintains the driver's transmitter state machine and implements its ISR
*/

void __attribute__ ( ( interrupt, no_auto_psv ) ) _U1TXInterrupt ( void )
{ 
    if((uart1_obj.txHead == uart1_obj.txTail) && (uart1_obj.txStatus.s.full == false))
    {
        while(U1STAbits.TRMT == 0){}
        
        uart1_obj.txStatus.s.empty = true;
        IEC0bits.U1TXIE = 0;
        return;
    }

    IFS0bits.U1TXIF = 0;

    while(!(U1STAHbits.UTXBF == 1))
    {
        U1TXREG = *uart1_obj.txHead;

        uart1_obj.txHead++;

        if(uart1_obj.txHead == (uart1_txByteQ + UART1_CONFIG_TX_BYTEQ_LENGTH))
        {
            uart1_obj.txHead = uart1_txByteQ;
        }

        uart1_obj.txStatus.s.full = false;

        if(uart1_obj.txHead == uart1_obj.txTail)
        {
            break;
        }
    }
}

void __attribute__ ( ( interrupt, no_auto_psv ) ) _U1RXInterrupt( void )
{
    while(!(U1STAHbits.URXBE == 1))    //Check for the RX Buffer not empty
    {
        *uart1_obj.rxTail = U1RXREG;

        uart1_obj.rxTail++;

        if(uart1_obj.rxTail == (uart1_rxByteQ + UART1_CONFIG_RX_BYTEQ_LENGTH))
        {
            uart1_obj.rxTail = uart1_rxByteQ;
        }

        uart1_obj.rxStatus.s.empty = false;
        
        if(uart1_obj.rxTail == uart1_obj.rxHead)
        {
            //Sets the flag RX full
            uart1_obj.rxStatus.s.full = true;
            break;
        }   
    }

    IFS0bits.U1RXIF = false;
   
}

void __attribute__ ( ( interrupt, no_auto_psv ) ) _U1EInterrupt( void )
{
    if ((U1STAbits.OERR == 1))
    {
        U1STAbits.OERR = 0;
    }
	
    IFS3bits.U1EIF = false;
}

/* ISR for UART Event Interrupt */
void __attribute__ ( ( interrupt, no_auto_psv ) ) _U1EVTInterrupt ( void )
{
    /* Add handling for UART events here */

    IFS11bits.U1EVTIF = false;
}

/**
  Section: UART Driver Client Routines
*/

uint8_t UART1_Read( void)
{
    uint8_t data = 0;

    data = *uart1_obj.rxHead;

    uart1_obj.rxHead++;

    if (uart1_obj.rxHead == (uart1_rxByteQ + UART1_CONFIG_RX_BYTEQ_LENGTH))
    {
        uart1_obj.rxHead = uart1_rxByteQ;
    }

    if (uart1_obj.rxHead == uart1_obj.rxTail)
    {
        uart1_obj.rxStatus.s.empty = true;
    }

    uart1_obj.rxStatus.s.full = false;

    return data;
}

unsigned int UART1_ReadBuffer( uint8_t *buffer, const unsigned int bufLen)
{
    unsigned int numBytesRead = 0 ;
    while ( numBytesRead < ( bufLen ))
    {
        if( uart1_obj.rxStatus.s.empty)
        {
            break;
        }
        else
        {
            buffer[numBytesRead++] = UART1_Read () ;
        }
    }

    return numBytesRead ;
}

void UART1_Write( const uint8_t byte)
{
    IEC0bits.U1TXIE = 0;
    
    *uart1_obj.txTail = byte;

    uart1_obj.txTail++;
    
    if (uart1_obj.txTail == (uart1_txByteQ + UART1_CONFIG_TX_BYTEQ_LENGTH))
    {
        uart1_obj.txTail = uart1_txByteQ;
    }

    uart1_obj.txStatus.s.empty = false;

    if (uart1_obj.txHead == uart1_obj.txTail)
    {
        uart1_obj.txStatus.s.full = true;
    }

    IEC0bits.U1TXIE = 1 ;
}

unsigned int UART1_WriteBuffer( const uint8_t *buffer , const unsigned int bufLen )
{
    unsigned int numBytesWritten = 0 ;

    while ( numBytesWritten < ( bufLen ))
    {
        if((uart1_obj.txStatus.s.full))
        {
            break;
        }
        else
        {
            UART1_Write (buffer[numBytesWritten++] ) ;
        }
    }

    return numBytesWritten ;
}

UART1_TRANSFER_STATUS UART1_TransferStatusGet (void )
{
    UART1_TRANSFER_STATUS status = 0;

    /* The TX empty must be checked before the full in order to prevent a race
     * condition where a TX transmission could start between these two checks
     * resulting in both full and empty set at the same time.
     */
    if(uart1_obj.txStatus.s.empty)
    {
        status |= UART1_TRANSFER_STATUS_TX_EMPTY;
    }

    if(uart1_obj.txStatus.s.full)
    {
        status |= UART1_TRANSFER_STATUS_TX_FULL;
    }

    /* The RX full must be checked before the empty in order to prevent a race
     * condition where a RX reception could start between these two checks
     * resulting in both empty and full set at the same time.
     */
    if(uart1_obj.rxStatus.s.full)
    {
        status |= UART1_TRANSFER_STATUS_RX_FULL;
    }

    if(uart1_obj.rxStatus.s.empty)
    {
        status |= UART1_TRANSFER_STATUS_RX_EMPTY;
    }
    else
    {
        status |= UART1_TRANSFER_STATUS_RX_DATA_PRESENT;
    }
    return status;
}

/*
    Uart Peek function returns the character in the read sequence with
    the provided offset, without extracting it.
*/
uint8_t UART1_Peek(uint16_t offset)
{
    if( (uart1_obj.rxHead + offset) >= (uart1_rxByteQ + UART1_CONFIG_RX_BYTEQ_LENGTH))
    {
      return uart1_rxByteQ[offset - (uart1_rxByteQ + UART1_CONFIG_RX_BYTEQ_LENGTH - uart1_obj.rxHead)];
    }
    else
    {
      return *(uart1_obj.rxHead + offset);
    }
}

/*
    Uart PeekSafe function validates all the possible conditions and get the character  
    in the read sequence with the provided offset, without extracting it.
*/
bool UART1_PeekSafe(uint8_t *dataByte, uint16_t offset)
{
    uint16_t index = 0;
    bool status = true;
    
    if((offset >= UART1_CONFIG_RX_BYTEQ_LENGTH) || (uart1_obj.rxStatus.s.empty) || (!dataByte))
    {
        status = false;
    }
    else
    {    
        //Compute the offset buffer overflow range
        index = ((uart1_obj.rxHead - uart1_rxByteQ) + offset) % UART1_CONFIG_RX_BYTEQ_LENGTH;
    
        /**
         * Check for offset input value range is valid or invalid. If the range 
         * is invalid, then status set to false else true.
         */
        if(uart1_obj.rxHead < uart1_obj.rxTail) 
        {
            if((uart1_obj.rxHead + offset) > (uart1_obj.rxTail - 1))
            {
                status = false;
            }
        }
        else if(uart1_obj.rxHead > uart1_obj.rxTail)
        {
            if((uart1_rxByteQ + index) < uart1_obj.rxHead)
            {
                if( (uart1_rxByteQ + index) >= uart1_obj.rxTail )
                {
                    status = false;
                }
            } 
        }

        if(status == true)
        {
            *dataByte = UART1_Peek(offset);
        }
    }
    return status;
}

unsigned int UART1_ReceiveBufferSizeGet(void)
{
    if(!uart1_obj.rxStatus.s.full)
    {
        if(uart1_obj.rxHead > uart1_obj.rxTail)
        {
            return(uart1_obj.rxHead - uart1_obj.rxTail);
        }
        else
        {
            return(UART1_CONFIG_RX_BYTEQ_LENGTH - (uart1_obj.rxTail - uart1_obj.rxHead));
        } 
    }
    return 0;
}

unsigned int UART1_TransmitBufferSizeGet(void)
{
    if(!uart1_obj.txStatus.s.full)
    { 
        if(uart1_obj.txHead > uart1_obj.txTail)
        {
            return(uart1_obj.txHead - uart1_obj.txTail);
        }
        else
        {
            return(UART1_CONFIG_TX_BYTEQ_LENGTH - (uart1_obj.txTail - uart1_obj.txHead));
        }
    }
    return 0;
}

bool UART1_ReceiveBufferIsEmpty (void)
{
    return((bool) uart1_obj.rxStatus.s.empty);
}

bool UART1_TransmitBufferIsFull(void)
{
    return((bool) uart1_obj.txStatus.s.full);
}

uint32_t UART1_StatusGet (void)
{
    uint32_t statusReg = U1STAH;
    return ((statusReg << 16 ) | U1STA);
}

void UART1_Disable(void)
{
    U1MODEbits.UARTEN = 0;
    U1MODEbits.UTXEN = 0; 
    U1MODEbits.URXEN = 0;
}

int __attribute__((__section__(".libc.write"))) write(int handle, void *buffer, unsigned int len) {
    while(U1STAbits.TRMT == 0);
    return UART1_WriteBuffer((uint8_t *) buffer, len);
}
