#define XTFREQ          7372800                 // On-board Crystal frequency (Hz)
#define PLLMODE         8                       // On-chip PLL setting
#define FCY             XTFREQ * PLLMODE / 4    // Instruction Cycle Frequency (Hz)