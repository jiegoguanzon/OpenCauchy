#include <xc.h>
#include <libq.h>
#include <stdint.h>

/* scaling factor for pot */
#define KPOT        _Q15ftoi(0.5)
/* scaling factor for current phase A */
#define KCURRA      _Q15ftoi(0.5)
/* scaling factor for current phase B */
#define KCURRB      _Q15ftoi(0.5) 

/* Motor's number of pole pairs */
#define NOPOLEPAIRS 5
/* Nominal speed of the motor in RPM */
#define NOMINAL_SPEED_RPM    2500 
/* Maximum speed of the motor in RPM - given by the motor's manufacturer */
#define MAXIMUM_SPEED_RPM    3500 

/* The following values are given in the xls attached file */
#define NORM_CURRENT_CONST     0.000121
/* normalized ls/dt value */
#define NORM_LSDTBASE 1460
/* normalized rs value */
#define NORM_RS  12990
/* the calculation of Rs gives a value exceeding the Q15 range so,
 the normalized value is further divided by 2 to fit the 32768 limit
 this is taken care in the estim.c where the value is implied
 normalized inv kfi at base speed */
#define NORM_INVKFIBASE  7956
/* the calculation of InvKfi gives a value which not exceed the Q15 limit
   to assure that an increase of the term with 5 is possible in the lookup table
   for high flux weakening the normalized is initially divided by 2
   this is taken care in the estim.c where the value is implied
   normalized dt value */
#define NORM_DELTAT  1790

/* Limitation constants */
/* di = i(t1)-i(t2) limitation
 high speed limitation, for dt 50us 
 the value can be taken from attached xls file */
#define D_ILIMIT_HS 1092
/* low speed limitation, for dt 8*50us */
#define D_ILIMIT_LS 4369

/* Filters constants definitions  */
/* BEMF filter for d-q components @ low speeds */
#define KFILTER_ESDQ 1200
/* BEMF filter for d-q components @ high speed - Flux Weakening case */
#define KFILTER_ESDQ_FW 164
/* Estimated speed filter constatn */
#define KFILTER_VELESTIM 2*374

/* initial offset added to estimated value, 
 when transitioning from open loop to closed loop 
 the value represents 45deg and should satisfy both 
 open loop and closed loop functioning 
 normally this value should not be modified, but in 
 case of fine tuning of the transition, depending on 
 the load or the rotor moment of inertia */
#define INITOFFSET_TRANS_OPEN_CLSD 0x2000

/* current transformation macro, used below */
#define NORM_CURRENT(current_real) (_Q15ftoi(current_real/NORM_CURRENT_CONST/32768))

/* Open loop startup constants */

/* The following values depends on the PWM frequency,
 lock time is the time needed for motor's poles alignment 
before the open loop speed ramp up */
/* This number is: 20,000 is 1 second. */
#define LOCK_TIME 4000 
/* Open loop speed ramp up end value Value in RPM*/
#define END_SPEED_RPM 500 
/* Open loop acceleration */
#define OPENLOOP_RAMPSPEED_INCREASERATE 10
/* Open loop q current setup - */
#define Q_CURRENT_REF_OPENLOOP NORM_CURRENT(1.41)

/* Maximum motor speed converted into electrical speed */
#define MAXIMUMSPEED_ELECTR MAXIMUM_SPEED_RPM * NOPOLEPAIRS
/* Nominal motor speed converted into electrical speed */
#define NOMINALSPEED_ELECTR NOMINAL_SPEED_RPM*NOPOLEPAIRS

/* End speed converted to fit the startup ramp */
#define END_SPEED (END_SPEED_RPM * NOPOLEPAIRS * LOOPTIME_SEC * 65536 / 60.0)*1024
/* End speed of open loop ramp up converted into electrical speed */
#define ENDSPEED_ELECTR END_SPEED_RPM*NOPOLEPAIRS
    
/* In case of the potentiometer speed reference, a reference ramp
is needed for assuring the motor can follow the reference imposed /
minimum value accepted */
#define SPEEDREFRAMP   _Q15ftoi(0.00003)  
/* The Speed Control Loop Executes every  SPEEDREFRAMP_COUNT */
#define SPEEDREFRAMP_COUNT   3 

/* D Control Loop Coefficients */
#define D_CURRCNTR_PTERM       _Q15ftoi(0.02)
#define D_CURRCNTR_ITERM       _Q15ftoi(0.001)
#define D_CURRCNTR_CTERM       _Q15ftoi(0.999)
#define D_CURRCNTR_OUTMAX      0x7FFF

/* Q Control Loop Coefficients */
#define Q_CURRCNTR_PTERM       _Q15ftoi(0.02)
#define Q_CURRCNTR_ITERM       _Q15ftoi(0.001)
#define Q_CURRCNTR_CTERM       _Q15ftoi(0.999)
#define Q_CURRCNTR_OUTMAX      0x7FFF

/* Velocity Control Loop Coefficients */
#define SPEEDCNTR_PTERM        _Q15ftoi(0.5)
#define SPEEDCNTR_ITERM        _Q15ftoi(0.005)
#define SPEEDCNTR_CTERM        _Q15ftoi(0.999)
#define SPEEDCNTR_OUTMAX       0x5000