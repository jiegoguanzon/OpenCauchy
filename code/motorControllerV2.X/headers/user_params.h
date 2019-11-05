#define FCY             4000000             // Instructions Per Second

#define DRV_INHA        _LATB5      // Phase A High Side MOSFET Gate Output Pin
#define DRV_INLA        _LATB4      // Phase A Low Side MOSFET Gate Output Pin
#define DRV_INHB        _LATB3      // Phase B High Side MOSFET Gate Output Pin
#define DRV_INLB        _LATB2      // Phase B Low Side MOSFET Gate Output Pin
#define DRV_INHC        _LATB1      // Phase C High Side MOSFET Gate Output Pin
#define DRV_INLC        _LATB0      // Phase C Low Side MOSFET Gate Output Pin

#define DRV_CSA         _RA2        // Phase A Current Sense Input Pin
#define DRV_CSB         _RA3        // Phase B Current Sense Input Pin
#define DRV_CSC         _RA4        // Phase C Current Sense Input Pin

#define DRV_ENABLE      _LATB6      // Enable Output Pin
#define DRV_SS          _LATB13     // Slave Select Output Pin

#define V_DS            16          // DC Link Voltage
#define PWM_PERIOD      100         // Period of SVPWM in microseconds

#define V_REF           3.3         // Voltage Reference for Current Sense in Volts
#define SENSE_GAIN      20          // Current Sense Gain in Volts per Volts
#define R_SENSE         0.001       // Sense Resistor Value in Ohms

#define PI              3.141592654
#define PI_OVER_TWO     1.570796327
#define ONE_OVER_SQRT3  0.5773502692
#define TWO_OVER_SQRT3  1.154700538
#define SQRT3_OVER_TWO  0.8660254038