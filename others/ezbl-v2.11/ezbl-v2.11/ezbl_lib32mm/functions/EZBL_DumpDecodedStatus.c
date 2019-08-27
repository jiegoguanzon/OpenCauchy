/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM Library API Source File

  Summary:
    Decodes the provided 'status' MIPS CPU register (obtained in a trap handler)
    and prints the individual bitfields in human readable form on stdout.

 *******************************************************************************/

/*******************************************************************************
  Copyright (C) 2018 Microchip Technology Inc.

  MICROCHIP SOFTWARE NOTICE AND DISCLAIMER:  You may use this software, and any
  derivatives created by any person or entity by or on your behalf, exclusively
  with Microchip's products.  Microchip and its licensors retain all ownership
  and intellectual property rights in the accompanying software and in all
  derivatives here to.

  This software and any accompanying information is for suggestion only.  It
  does not modify Microchip's standard warranty for its products.  You agree
  that you are solely responsible for testing the software and determining its
  suitability.  Microchip has no obligation to modify, test, certify, or
  support the software.

  THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS".  NO WARRANTIES, WHETHER
  EXPRESS, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED
  WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR
  PURPOSE APPLY TO THIS SOFTWARE, ITS INTERACTION WITH MICROCHIP'S PRODUCTS,
  COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION.

  IN NO EVENT, WILL MICROCHIP BE LIABLE, WHETHER IN CONTRACT, WARRANTY, TORT
  (INCLUDING NEGLIGENCE OR BREACH OF STATUTORY DUTY), STRICT LIABILITY,
  INDEMNITY, CONTRIBUTION, OR OTHERWISE, FOR ANY INDIRECT, SPECIAL, PUNITIVE,
  EXEMPLARY, INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE, FOR COST OR EXPENSE OF
  ANY KIND WHATSOEVER RELATED TO THE SOFTWARE, HOWSOEVER CAUSED, EVEN IF
  MICROCHIP HAS BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE.
  TO THE FULLEST EXTENT ALLOWABLE BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL
  CLAIMS IN ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF
  FEES, IF ANY, THAT YOU HAVE PAID DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.

  MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE
  TERMS.
*******************************************************************************/

int EZBL_printf(const char *format, ...);


/**
 * Prints the individually decoded value and meaning of each bitfield in the
 * MIPS Status CPU register to stdout.
 *
 * @param status MIPS Status CPU register value to decode. This integer is
 *               obtained as a parameter from a trap handler function.
 */
void EZBL_DumpDecodedStatus(unsigned int status)
{
    EZBL_printf("Status:\n"
                "+--------+--+--+--+--+--+---+--+--+---+---+---+---------+---+--+-+---+---+--+\n"
                "|CU3..CU0|RP|FR|RE|MX| R|BEV|TS|SR|NMI|IPL|CEE|      IPL|  R|UM|R|ERL|EXL|IE|\n"
                "|31    28|27|26|25|24|23| 22|21|20| 19| 18| 17|16 10 9 8|7 5| 4|3|  2|  1| 0|\n"
                "+--------+--+--+--+--+--+---+--+--+---+---+---+---------+---+--+-+---+---+--+\n"
                "      CU0[28] =  %X: Controls access to coprocessor 0 (1=allowed)\n",              (status>>28) & 0x1);
    EZBL_printf("       RP[27] =  %X: Enables reduced power mode\n",                                (status>>27) & 0x1);
    EZBL_printf("       FR[26] =  %X: Reserved (Floating Point Register mode)\n",                   (status>>26) & 0x1);
    EZBL_printf("       RE[25] =  %X: Reserved (Reverse Endianess in User mode)\n",                 (status>>25) & 0x1);
    EZBL_printf("       MX[24] =  %X: Reserved (MIPS Extension for DSP Module)\n",                  (status>>24) & 0x1);
    EZBL_printf("        R[23] =  %X: Reserved\n",                                                  (status>>23) & 0x1);
    EZBL_printf("      BEV[22] =  %X: Controls exception vector (0 = Normal; 1 = Bootstrap)\n",     (status>>22) & 0x1);
    EZBL_printf("       TS[21] =  %X: Reserved (TLB Shutdown)\n",                                   (status>>21) & 0x1);
    EZBL_printf("       SR[20] =  %X: Soft Reset occurred\n",                                       (status>>20) & 0x1);
    EZBL_printf("      NMI[19] =  %X: Non-Maskable Interrupt occurred\n",                           (status>>19) & 0x1);
    EZBL_printf("      IPL[18] =  %X: Current CPU Interrupt Priority Level, bit 7\n",               (status>>18) & 0x1);
    EZBL_printf("      CEE[17] =  %X: Reserved (CoreExtended Enable)\n",                            (status>>17) & 0x1);
    EZBL_printf("   IPL[16:10] = %02X: Current CPU Interrupt Priority Level, bits 0 though 6\n",    (status>>10) & 0x7F);
    EZBL_printf("       R[7:5] =  %X: Reserved\n",                                                  (status>>5) & 0x7);
    EZBL_printf("        UM[4] =  %X: Base operating mode (0 = Kernel mode; 1 = User mode)\n",      (status>>4) & 0x1);
    EZBL_printf("         R[3] =  %X: Reserved\n",                                                  (status>>3) & 0x1);
    EZBL_printf("       ERL[2] =  %X: Error Level (1 = Error level; 0 = Normal level)\n",           (status>>2) & 0x1);
    EZBL_printf("       EXL[1] =  %X: Exception Level (1 = Exception level; 0 = Normal level)\n",   (status>>1) & 0x1);
    EZBL_printf("        IE[0] =  %X: Interrupt Enable (master enable)\n",                          (status>>0) & 0x1);
}
