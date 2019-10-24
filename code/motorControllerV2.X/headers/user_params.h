#define FCY 4000000             // Instructions Per Second

#define DRV_INHA    _LATB5      // Phase A High Side MOSFET Gate Output Pin
#define DRV_INLA    _LATB4      // Phase A Low Side MOSFET Gate Output Pin
#define DRV_INHB    _LATB3      // Phase B High Side MOSFET Gate Output Pin
#define DRV_INLB    _LATB2      // Phase B Low Side MOSFET Gate Output Pin
#define DRV_INHC    _LATB1      // Phase C High Side MOSFET Gate Output Pin
#define DRV_INLC    _LATB0      // Phase C Low Side MOSFET Gate Output Pin

#define DRV_CSA     _RA2        // Phase A Current Sense Input Pin
#define DRV_CSB     _RA3        // Phase B Current Sense Input Pin
#define DRV_CSC     _RA4        // Phase C Current Sense Input Pin

#define DRV_ENABLE  _LATB6      // Enable Output Pin
#define DRV_SS      _LATB13     // Slave Select Output Pin
