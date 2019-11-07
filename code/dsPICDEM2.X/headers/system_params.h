#define XTFREQ          7372800                 // On-board Crystal frequency (Hz)
#define PLLMODE         16                      // On-chip PLL setting
#define FCY             XTFREQ * PLLMODE / 4    // Instruction Cycle Frequency (Hz)



#define V_DS            16          // DC Link Voltage

#define V_REF           3.3         // Voltage Reference for Current Sense in Volts
#define SENSE_GAIN      20          // Current Sense Gain in Volts per Volts
#define R_SENSE         0.001       // Sense Resistor Value in Ohms

#define PI              3.141592654
#define PI_OVER_TWO     1.570796327
#define ONE_OVER_SQRT3  0.5773502692
#define TWO_OVER_SQRT3  1.154700538
#define SQRT3_OVER_TWO  0.8660254038