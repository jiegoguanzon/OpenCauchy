#ifndef _MOTOR_CONTROL_TYPES_H_
#define _MOTOR_CONTROL_TYPES_H_

#include <stdint.h>

#ifdef __cplusplus
    extern "C" {
#endif

// *****************************************************************************
// *****************************************************************************
// Section: Data Types
// *****************************************************************************
// *****************************************************************************

// *****************************************************************************
/* Alpha-Beta reference frame data type

  Description:
    This structure will host parameters related to Alpha-Beta reference frame.
*/
typedef struct
{
    // Alpha component
    int16_t alpha;
    
    // Beta component
    int16_t beta;
} MC_ALPHABETA_T;

// *****************************************************************************
/* Sine-Cosine data type

  Description:
    This structure will host parameters related to Sine and Cosine components of the motor angle.
*/
typedef struct
{
    // Cosine component
    int16_t cos;
    
    // Sine component
    int16_t sin;
} MC_SINCOS_T;

// *****************************************************************************
/* D-Q reference frame data type

  Description:
    This structure will host parameters related to D-Q reference frame.
*/
typedef struct
{
    // D-axis component
    int16_t d;
    
    // Q-axis component
    int16_t q;
} MC_DQ_T;

// *****************************************************************************
/* Duty-cycle data type

  Description:
    This structure will host parameters related to PWM module Duty Cycle values.
*/
typedef struct
{
    // Duty cycle for phase #1
    uint16_t dutycycle1;
    
    // Duty cycle for phase #2
    uint16_t dutycycle2;
    
    // Duty cycle for phase #3
    uint16_t dutycycle3;
} MC_DUTYCYCLEOUT_T;

// *****************************************************************************
/* ABC reference frame data type

  Description:
    This structure will host parameters related to ABC reference frame.
*/
typedef struct
{
    // Phase A component 
    int16_t a;
    
    // Phase B component
    int16_t b;
    
    // Phase C component
    int16_t c;
} MC_ABC_T;

// *****************************************************************************
/* PI Controller State data type

  Description:
    This structure will host parameters related to the PI Controller state.
*/
typedef struct {

    // Integrator sum
    int32_t integrator;
    
    // Proportional gain co-efficient term
    int16_t kp;
    
    // Integral gain co-efficient term
    int16_t ki;
    
    // Excess gain co-efficient term
    int16_t kc;

    // Maximum output limit
    int16_t outMax;
    
    // Minimum output limit
    int16_t outMin;

} MC_PISTATE_T;

// *****************************************************************************
/* PI Controller Input data type

  Summary:
    PI Controller input type define

  Description:
    This structure will host parameters related to the PI Controller input. PI
    controller state is a part of the PI Controller input.
*/
typedef struct {

    // PI state as input parameter to the PI controller
    MC_PISTATE_T piState;
    
    // Input reference to the PI controller
    int16_t inReference;
    
    // Input measured value
    int16_t inMeasure;

} MC_PIPARMIN_T;

// *****************************************************************************
/* PI Controller Output data type

  Description:
    This structure will host parameters related to the PI Controller output.
*/
typedef struct {

    int16_t out;        // Output of the PI controller

} MC_PIPARMOUT_T;

typedef struct {

    int16_t q_ka;
    int16_t q_kb;
    int16_t q_ia;
    int16_t q_ib;
    int16_t a_offset;
    int16_t b_offset;

} MEAS_CURR_PARM_T;

#ifdef __cplusplus  // Provide C++ Compatibility
    }
#endif
#endif // _MOTOR_CONTROL_TYPES_H_
