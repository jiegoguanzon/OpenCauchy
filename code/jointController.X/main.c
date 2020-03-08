#include "xc.h"
#include <libq.h>
#include "headers/clock.h"
#include "libpic30.h"
#include "headers/system.h"
#include "headers/user_parameters.h"
#include "headers/mc/motor_control_types.h"
#include "headers/mc/motor_control_declarations.h"
#include "headers/meascurr.h"
#include "headers/control.h"
#include "headers/pwm.h"
#include "headers/adc.h"
#include "headers/estimator.h"
#include "headers/readadc.h"
#include "headers/spi.h"
#include "headers/drv8323.h"
#include "headers/can.h"
#include "headers/timer1.h"

volatile UGF_T uGF;

CTRL_PARM_T ctrl_parm;
MOTOR_STARTUP_DATA_T motor_startup_data;

MEAS_CURR_PARM_T meas_curr_parm;
READ_ADC_PARM_T read_adc_parm;

MC_ALPHABETA_T v_alphabeta, i_alphabeta;
MC_SINCOS_T sincos_theta;
MC_DQ_T v_dq, i_dq;
MC_ABC_T v_abc, i_abc;
MC_DUTYCYCLEOUT_T pwm_dutycycle;

MC_PIPARMIN_T pi_input_iq;
MC_PIPARMOUT_T pi_output_iq;
MC_PIPARMIN_T pi_input_id;
MC_PIPARMOUT_T pi_output_id;
MC_PIPARMIN_T pi_input_omega;
MC_PIPARMOUT_T pi_output_omega;

volatile int16_t theta = 0, theta_openloop = 0;

uint16_t pwm_period;

volatile uint16_t adcDataBuffer;
volatile uint16_t measCurrOffsetFlag = 0;

#define STARTUPRAMP_THETA_OPENLOOP_SCALER   10
#define MAX_VOLTAGE_VECTOR  0.98

void do_control(void);
void init_control_parameters(void);
void reset_parameters(void);
void calculate_park_angle(void);
void measure_curr_offset(int16_t *, int16_t *);
void print_diag(void);

int main(void) {

    _TRISC0 = 0;
    DRV_ENABLE = 0;
    _TRISE6 = 0;
    _LATE6 = 0;
    uGF.bits.RunMotor = 0;

    __delay_ms(100);
    DRV_ENABLE = 1;

    clock_init();
    system_init();
    pwm_init();
    adc_init();
    spi_init();
    can_init();

    //_LATE6 = 1;
    __delay_ms(1000);
    //read_ocp();
    set_idrive_hs();
    set_idrive_ls();
    //set_ocp();
    //printf("OCP: 0x%04X\n", read_ocp());
    //printf("GDLS: 0x%04X\n", read_gdls());
    //printf("GDHS: 0x%04X\n", read_gdhs());
    __delay_ms(1000);
    //_LATE6 = 0;

    measure_curr_offset(&meas_curr_parm.Offseta, &meas_curr_parm.Offsetb);

    CORCONbits.SATA = 0;

    reset_parameters();
    pwm_enable_outputs();

    print_diag();
    
    uGF.bits.RunMotor = 0;

    while (1) {

        print_diag();
        can_transmit();

        if (uGF.bits.OpenLoop)
            _LATE6 = 1;
        else
            _LATE6 = 0;

        __delay_ms(500);

    } 

    while (1);

    return 0;

}
void __attribute__((__interrupt__,no_auto_psv)) _ADCAN23Interrupt() {

    adcDataBuffer = ClearADCIF_ReadADCBUF();

    if (uGF.bits.RunMotor) {

        //_LATE6 = !_RE6;

        MeasCompCurr(ADCBUF_INV_A_IPHASE1, ADCBUF_INV_A_IPHASE2, &meas_curr_parm);
        i_abc.a = meas_curr_parm.qIa;
        i_abc.b = meas_curr_parm.qIb;

        MC_TransformClarke_Assembly(&i_abc, &i_alphabeta);
        MC_TransformPark_Assembly(&i_alphabeta, &sincos_theta, &i_dq);

        // Speed and field angle estimation
        estimate_angle();
        // Calculate control values
        do_control();
        // Calculate q angle
        calculate_park_angle();

        if (uGF.bits.OpenLoop)
            theta = theta_openloop;
        else
            theta = estimator.qRho;
        
        MC_CalculateSineCosine_Assembly_Ram(theta, &sincos_theta);
        MC_TransformParkInverse_Assembly(&v_dq, &sincos_theta, &v_alphabeta);
        MC_TransformClarkeInverseSwappedInput_Assembly(&v_alphabeta, &v_abc);
        MC_CalculateSpaceVectorPhaseShifted_Assembly(&v_abc, pwm_period, &pwm_dutycycle);

        PG3DC = pwm_dutycycle.dutycycle3;
        PG2DC = pwm_dutycycle.dutycycle2;
        PG1DC = pwm_dutycycle.dutycycle1;

        //_LATE6 = !_RE6;

    } else {

        PG3DC = PWM_MIN_DUTY;
        PG2DC = PWM_MIN_DUTY;
        PG1DC = PWM_MIN_DUTY;
        measCurrOffsetFlag = 1;

    }

    ClearADCIF();

}

void do_control () {

    volatile int16_t  temp_qref_pow;

    if (uGF.bits.OpenLoop) {

        if (uGF.bits.ChangeMode) {

            /* Just changed into open loop */
            uGF.bits.ChangeMode = 0;
            
            /* Synchronize angles */
            /* VqRef & VdRef not used */
            ctrl_parm.qVqRef = 0;
            ctrl_parm.qVdRef = 0;

            /* Reinitialize variables for initial speed ramp */
            motor_startup_data.startupLock  = 0;
            motor_startup_data.startupRamp  = 0;

        }

        /* Speed reference */
        ctrl_parm.qVelRef = Q_CURRENT_REF_OPENLOOP;
        /* q current reference is equal to the velocity reference 
         while d current reference is equal to 0
        for maximum startup torque, set the q current to maximum acceptable 
        value represents the maximum peak value */
        ctrl_parm.qVqRef = ctrl_parm.qVelRef;

        /* PI control for Q */
        pi_input_iq.inMeasure = i_dq.q;
        pi_input_iq.inReference = ctrl_parm.qVqRef;
        MC_ControllerPIUpdate_Assembly(pi_input_iq.inReference, pi_input_iq.inMeasure, &pi_input_iq.piState, &pi_output_iq.out);
        v_dq.q = pi_output_iq.out;

        /* PI control for D */
        pi_input_id.inMeasure = i_dq.d;
        pi_input_id.inReference = ctrl_parm.qVdRef;
        MC_ControllerPIUpdate_Assembly(pi_input_id.inReference, pi_input_id.inMeasure, &pi_input_id.piState, &pi_output_id.out);
        v_dq.d = pi_output_id.out;

    } else {

        if (uGF.bits.ChangeSpeed) {

            ReadADC0(ADCBUF_SPEED_REF_A, &read_adc_parm);
            read_adc_parm.qAnRef = (__builtin_muluu(read_adc_parm.qADValue, MAXIMUMSPEED_ELECTR - NOMINALSPEED_ELECTR) >> 15) + NOMINALSPEED_ELECTR;

            if (read_adc_parm.qAnRef < ENDSPEED_ELECTR)
                read_adc_parm.qAnRef = ENDSPEED_ELECTR;

        } else {

            ReadADC0(ADCBUF_SPEED_REF_A, &read_adc_parm);
            read_adc_parm.qAnRef = (__builtin_muluu(read_adc_parm.qADValue, NOMINALSPEED_ELECTR - ENDSPEED_ELECTR) >> 15) + ENDSPEED_ELECTR;

            if (read_adc_parm.qAnRef < ENDSPEED_ELECTR)
                read_adc_parm.qAnRef = ENDSPEED_ELECTR;

        }

        if (ctrl_parm.speedRampCount < SPEEDREFRAMP_COUNT)
            ctrl_parm.speedRampCount++;
        else {

            /* Ramp generator to limit the change of the speed reference
            the rate of change is defined by CtrlParm.qRefRamp */
            ctrl_parm.qDiff = ctrl_parm.qVelRef - read_adc_parm.qAnRef;

            /* Speed Ref Ramp */
            if (ctrl_parm.qDiff < 0)
                ctrl_parm.qVelRef = ctrl_parm.qVelRef + ctrl_parm.qRefRamp;
            else
                ctrl_parm.qVelRef = ctrl_parm.qVelRef - ctrl_parm.qRefRamp;

            /* If difference less than half of ref ramp, set reference
            directly from the pot */
            if (_Q15abs(ctrl_parm.qDiff) < (ctrl_parm.qRefRamp << 1))
                ctrl_parm.qVelRef = read_adc_parm.qAnRef;

            ctrl_parm.speedRampCount = 0;

        }

        if (uGF.bits.ChangeMode) {
            /* Just changed from open loop */
            uGF.bits.ChangeMode = 0;
            pi_input_omega.piState.integrator = (int32_t) ctrl_parm.qVqRef << 13;
        }

        /* If TORQUE MODE skip the speed controller */
        #ifndef	TORQUE_MODE

            /* Execute the velocity control loop */
            pi_input_omega.inMeasure = estimator.qVelEstim;
            pi_input_omega.inReference = ctrl_parm.qVelRef;

            MC_ControllerPIUpdate_Assembly(pi_input_omega.inReference, pi_input_omega.inMeasure, &pi_input_omega.piState, &pi_output_omega.out);

            ctrl_parm.qVqRef = pi_output_omega.out;

        #else

            ctrl_parm.qVqRef = ctrl_parm.qVelRef;

        #endif
        
        /* Flux weakening control - the actual speed is replaced 
        with the reference speed for stability 
        reference for d current component 
        adapt the estimator parameters in concordance with the speed */
        //ctrlParm.qVdRef=FieldWeakening(_Q15abs(ctrlParm.qVelRef));

        /* PI control for D */
        pi_input_id.inMeasure = i_dq.d;
        pi_input_id.inReference  = ctrl_parm.qVdRef;
        MC_ControllerPIUpdate_Assembly(pi_input_id.inReference,
                                       pi_input_id.inMeasure,
                                       &pi_input_id.piState,
                                       &pi_output_id.out);
        v_dq.d    = pi_output_id.out;

        /* Dynamic d-q adjustment
         with d component priority 
         vq=sqrt (vs^2 - vd^2) 
        limit vq maximum to the one resulting from the calculation above */
        temp_qref_pow = (int16_t)(__builtin_mulss(pi_output_id.out ,
                                                      pi_output_id.out) >> 15);
        temp_qref_pow = _Q15ftoi(MAX_VOLTAGE_VECTOR) - temp_qref_pow;
        pi_input_iq.piState.outMax = _Q15sqrt(temp_qref_pow);

        /* PI control for Q */
        pi_input_iq.inMeasure  = i_dq.q;
        pi_input_iq.inReference  = ctrl_parm.qVqRef;
        MC_ControllerPIUpdate_Assembly(pi_input_iq.inReference,
                                       pi_input_iq.inMeasure,
                                       &pi_input_iq.piState,
                                       &pi_output_iq.out);
        v_dq.q = pi_output_iq.out;

    }

}

void init_control_parameters () {

    read_adc_parm.qK = KPOT;
    meas_curr_parm.qKa = KCURRA;
    meas_curr_parm.qKb = KCURRB;

    ctrl_parm.qRefRamp = SPEEDREFRAMP;
    ctrl_parm.qRefRamp = SPEEDREFRAMP_COUNT;

    pwm_period = LOOPTIME_TCY;

    pi_input_id.piState.kp = D_CURRCNTR_PTERM;
    pi_input_id.piState.ki = D_CURRCNTR_ITERM;
    pi_input_id.piState.kc = D_CURRCNTR_CTERM;
    pi_input_id.piState.outMax = D_CURRCNTR_OUTMAX;
    pi_input_id.piState.outMin = -pi_input_id.piState.outMax;
    pi_input_id.piState.integrator = 0;
    pi_output_id.out = 0;

    pi_input_iq.piState.kp = Q_CURRCNTR_PTERM;
    pi_input_iq.piState.ki = Q_CURRCNTR_ITERM;
    pi_input_iq.piState.kc = Q_CURRCNTR_CTERM;
    pi_input_iq.piState.outMax = Q_CURRCNTR_OUTMAX;
    pi_input_iq.piState.outMin = -pi_input_iq.piState.outMax;
    pi_input_iq.piState.integrator = 0;
    pi_output_iq.out = 0;

    pi_input_omega.piState.kp = SPEEDCNTR_PTERM;
    pi_input_omega.piState.ki = SPEEDCNTR_ITERM;
    pi_input_omega.piState.kc = SPEEDCNTR_CTERM;
    pi_input_omega.piState.outMax = SPEEDCNTR_OUTMAX;
    pi_input_omega.piState.outMin = -pi_input_omega.piState.outMax;
    pi_input_omega.piState.integrator = 0;
    pi_output_omega.out = 0;

}

void reset_parameters () {

    DisableADCInterrupt();

    PG3DC = PWM_MIN_DUTY;
    PG2DC = PWM_MIN_DUTY;
    PG1DC = PWM_MIN_DUTY;

    pwm_disable_outputs();

    /* Stop the motor */
    uGF.bits.RunMotor = 0;
    /* Set the reference speed value to 0 */
    ctrl_parm.qVelRef = 0;
    /* Restart in open loop */
    uGF.bits.OpenLoop = 1;
    /* Change speed */
    uGF.bits.ChangeSpeed = 0;
    /* Change mode */
    uGF.bits.ChangeMode = 1;

    init_control_parameters();
    init_estimator_parameters();
    //init_fw_parameters();

    ClearADCIF();
    adcDataBuffer = ClearADCIF_ReadADCBUF();
    EnableADCInterrupt();

}

void calculate_park_angle () {

    if (uGF.bits.OpenLoop) {

        /* begin with the lock sequence, for field alignment */
        if (motor_startup_data.startupLock < LOCK_TIME)
            motor_startup_data.startupLock += 1;
        else if (motor_startup_data.startupRamp < END_SPEED) // Ramp up until end speed
            motor_startup_data.startupRamp += OPENLOOP_RAMPSPEED_INCREASERATE;
        else { // Switch to closed loop

            #ifndef OPEN_LOOP_FUNCTIONING
                uGF.bits.ChangeMode = 1;
                uGF.bits.OpenLoop = 0;
            #endif

        }

        /* The angle set depends on startup ramp */
        theta_openloop += (int16_t)(motor_startup_data.startupRamp >> STARTUPRAMP_THETA_OPENLOOP_SCALER);

    } else {

        if (estimator.qRhoOffset > 0)
            estimator.qRhoOffset--;

    }

}

void measure_curr_offset (int16_t *pOffseta,int16_t *pOffsetb) {    

    int32_t adcOffsetIa = 0, adcOffsetIb = 0;
    uint16_t i = 0;
                
    /* Enable ADC interrupt and begin main loop timing */
    ClearADCIF();
    adcDataBuffer = ClearADCIF_ReadADCBUF();
    EnableADCInterrupt();
    
    /* Taking multiple sample to measure voltage offset in all the channels */
    for (i = 0; i < (1 << CURRENT_OFFSET_SAMPLE_SCALER); i++) {

        measCurrOffsetFlag = 0;

        /* Wait for the conversion to complete */
        while (measCurrOffsetFlag != 1);

        /* Sum up the converted results */
        adcOffsetIa += ADCBUF_INV_A_IPHASE1;
        adcOffsetIb += ADCBUF_INV_A_IPHASE2;

    }

    /* Averaging to find current Ia offset */
    *pOffseta = (int16_t)(adcOffsetIa >> CURRENT_OFFSET_SAMPLE_SCALER);
    /* Averaging to find current Ib offset*/
    *pOffsetb = (int16_t)(adcOffsetIb >> CURRENT_OFFSET_SAMPLE_SCALER);
    measCurrOffsetFlag = 0;

    
    /* Make sure ADC does not generate interrupt while initializing parameters*/
    DisableADCInterrupt();
}

void print_diag () {

    printf("\033[2J");      //Clear screen
    printf("\033[0;0f");    //return cursor to 0,0
    printf("\033[?25l");    //disable cursor

    printf("Run Motor: %s\n", uGF.bits.RunMotor ? "Yes" : "No");
    printf("Run Mode: %s\n", uGF.bits.OpenLoop ? "Open Loop" : "Closed Loop");
    printf("\nADC Offset A: %d\tADC Offset B: %d\n", meas_curr_parm.Offseta, meas_curr_parm.Offsetb);
    printf("\nFS Register 1: 0x%04X\t FS Register 2: 0x%04X\n", read_fs1(), read_fs2());

}