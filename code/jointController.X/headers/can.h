#define MAX_WORDS   100
unsigned int __attribute__((aligned(4))) CanTxBuffer[MAX_WORDS];

typedef unsigned long CANFD_MSG_TIMESTAMP;

typedef struct _CANFD_TX_MSGOBJ_CTRL {
    unsigned DLC:4;
    unsigned IDE:1;
    unsigned RTR:1;
    unsigned BRS:1;
    unsigned FDF:1;
    unsigned ESI:1;
    unsigned long SEQ:23;
    unsigned unimplemented1:16;
} CANFD_TX_MSGOBJ_CTRL;

typedef struct _CANFD_MSGOBJ_ID {
    unsigned SID:11;
    unsigned long EID:18;
    unsigned SID11:1;
    unsigned unimplemented1:2;
} CANFD_MSGOBJ_ID;

typedef union _CANFD_TX_MSGOBJ {
    struct {
        CANFD_MSGOBJ_ID id;
        CANFD_TX_MSGOBJ_CTRL ctrl;
        CANFD_MSG_TIMESTAMP timeStamp;
    } bF;
    unsigned int word[4];
    unsigned char byte[8];
} CANFD_TX_MSGOBJ;

void can_init () {

    //  CAN Clock Source = 360MHz   CAN Clock = 40MHz
    CANCLKCONbits.CANCLKEN = 1;
    CANCLKCONbits.CANCLKSEL = 2;
    CANCLKCONbits.CANCLKDIV = 8;

    // Enable CAN-FD module
    C1CONLbits.CON = 1;

    // Place CAN module in Configuration Mode
    C1CONHbits.REQOP = 4;
    while (C1CONHbits.OPMOD != 4);

    C1FIFOBAL = (unsigned int) &CanTxBuffer;

    C1NBTCFGH = 0x001E;
    C1NBTCFGL = 0x0707;
    C1DBTCFGH = 0x000E;
    C1DBTCFGL = 0x0303;
    C1TDCH = 0x0002;        //TDCMOD is Auto
    C1TDCL = 0x0F00;

    C1CONLbits.BRSDIS = 0x0;
    C1CONHbits.STEF = 0x0;
    C1CONHbits.TXQEN = 0x1; 

    C1TXQCONHbits.FSIZE = 0x0;
    C1TXQCONHbits.PLSIZE = 0x7;

    C1FIFOCON1Hbits.FSIZE = 0x1;
    C1FIFOCON1Hbits.PLSIZE = 0x2;
    C1FIFOCON1Lbits.TXEN = 0x1;

    C1CONHbits.REQOP = 0;
    //while (C1CONHbits.OPMOD != 0);
    __delay_ms(100);

}

void can_transmit () {

    CANFD_TX_MSGOBJ *txObj;

    txObj = (CANFD_TX_MSGOBJ *) C1TXQUAL;
    txObj->bF.id.SID = 0x100;
    txObj->bF.id.EID = 0x0000;
    txObj->bF.ctrl.BRS = 1;
    txObj->bF.ctrl.DLC = 0xF;
    txObj->bF.ctrl.FDF = 1;
    txObj->bF.ctrl.IDE = 0;

    int i;
    for (i = 0; i < 0x40; i++)
        txObj->byte[i + 8] = 0x5A;

    C1TXQCONLbits.UINC = 1;
    C1TXQCONLbits.TXREQ = 1;

    /* SID = 0x300 , 16 bytes of data */
    txObj = (CANFD_TX_MSGOBJ *)C1FIFOUA1L;
    txObj->bF.id.SID = 0x300;
    txObj->bF.id.EID = 0x0000;
    txObj->bF.ctrl.BRS = 1 ; //Switch bit rate
    txObj->bF.ctrl.DLC = 0xA; //16 bytes
    txObj->bF.ctrl.FDF = 1; // CANFD frame
    txObj->bF.ctrl.IDE = 0; //Standard frame

    for (i=0;i<0x10;i++ )
        txObj->byte[i+8] = 0xA5 ; // 16 bytes of 0xA5

    C1FIFOCON1Lbits.UINC = 1; // Set UINC bit
    C1FIFOCON1Lbits.TXREQ = 1; // Set TXREQ bit

}