/*******************************************************************************
  Easy Bootloader Library for PIC24/dsPIC33/PIC32MM Library API Source File

  Summary:
    Decodes the provided 'cause' MIPS CPU register (obtained in a trap handler)
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
 * MIPS cause CPU register to stdout.
 *
 * @param status MIPS cause CPU register value to decode. This integer is
 *               obtained as a parameter from a trap handler function.
 */
void EZBL_DumpDecodedCause(unsigned int cause)
{
    unsigned int exceptionCode = (cause>>2) & 0x1Fu;

    EZBL_printf("Cause:\n"
                "+--+--+-----+--+---+--+--+--+--+----+-----+-------------+-------+-+--------+---+\n"
                "|BD|TI|   CE|DC|PCI|IC|AP|IV|WP|FDCI|    0|RIPL17.RIPL10|IP1.IP0|0|Exc Code|  0|\n"
                "|31|30|29 28|27| 26|25|24|23|22|  21|20 18|17         10|9     8|7|6      2|1 0|\n"
                "+--+--+-----+--+---+--+--+--+--+----+-----+-------------+-------+-+--------+---+\n"
                "BD[31]      =  %X: Exception in Branch Delay slot\n",  (cause>>31) & 0x1);
    EZBL_printf("TI[30]      =  %X: Core Timer Interrupt flag\n",                                       (cause>>30) & 0x1);
    EZBL_printf("CE[29:28]   =  %X: Coprocessor referenced when a COP Unusable exception taken\n",      (cause>>28) & 0x3);
    EZBL_printf("DC[27]      =  %X: Disable Count register\n",                                          (cause>>27) & 0x1);
    EZBL_printf("PCI[26]     =  %X: Performance Counter Interrupt flag\n",                              (cause>>26) & 0x1);
    EZBL_printf("IC[25]      =  %X: Interrupt Chaining occurred on last IRET\n",                        (cause>>25) & 0x1);
    EZBL_printf("AP[24]      =  %X: Exception occurred during Interrupt Auto-Prologue\n",               (cause>>24) & 0x1);
    EZBL_printf("IV[23]      =  %X: 0 = Use general exception vector; 1 = special interrupt vector\n",  (cause>>23) & 0x1);
    EZBL_printf("WP[22]      =  %X: Watch exception was deferred\n",                                    (cause>>22) & 0x1);
    EZBL_printf("FDCI[21]    =  %X: Fast Debug Channel Interrupt flag\n",                               (cause>>21) & 0x1);
    EZBL_printf("RIPL[17:10] = %02X: Requested Interrupt Priority Level (read-only)\n",                 (cause>>10) & 0xFF);
    EZBL_printf("IP1[9]      =  %X: Request Software Interrupt 1\n",                                    (cause>>9) & 0x1);
    EZBL_printf("IP0[8]      =  %X: Request Software Interrupt 0\n",                                    (cause>>8) & 0x1);
    EZBL_printf("ExcCode[6:2]= %s%02X%s: Exception code\n"
                "    00 = Interrupt\n"
                "    04 = Address error exception (load or instruction fetch)\n"
                "    05 = Address error exception (store)\n"
                "    06 = Bus error exception (instruction fetch)\n"
                "    07 = Bus error exception (data reference: load or store)\n"
                "    08 = Syscall exception\n"
                "    09 = Breakpoint exception\n"
                "    0A = Reserved instruction exception\n"
                "    0B = Coprocessor Unusable exception\n"
                "    0C = Arithmetic Overflow exception\n"
                "    0D = Trap exception\n"
                "    1D = MPU Exception\n",
                exceptionCode ? "\x1B[31m" : "",    // Change exception code text color to ANSI red text if non-zero (i.e.: not an interrupt)
                exceptionCode,
                "\x0F\x1B[0m");                     // Return to default text color
}
