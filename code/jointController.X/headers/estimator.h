#include <libq.h>
#include <stdint.h>
#include "mc/motor_control_declarations.h"
#include "user_parameters.h"
#include "control.h"

typedef struct {
    /* Integration constant */
    int16_t qDeltaT;
    /* angle of estimation */
    int16_t qRho;
    /* internal variable for angle */
    int32_t qRhoStateVar;
    /* primary speed estimation */
    int16_t qOmegaMr;
    /* last value for Ialpha */
    int16_t qLastIalpha;
    /* last value for Ibeta */
    int16_t qLastIbeta;
    /* difference Ialpha */
    int16_t qDIalpha;
    /* difference Ibeta */
    int16_t qDIbeta;
    /* BEMF alpha */
    int16_t qEsa;
    /* BEMF beta */
    int16_t qEsb;
    /* BEMF d */
    int16_t qEsd;
    /* BEMF q */
    int16_t qEsq;
    /* counter in Last DI tables */
    int16_t qDiCounter;
    /* dI*Ls/dt alpha */
    int16_t qVIndalpha;
    /* dI*Ls/dt beta */
    int16_t qVIndbeta;
    /* BEMF d filtered */
    int16_t qEsdf;
    /* state variable for BEMF d Filtered */
    int32_t qEsdStateVar;
    /* BEMF q filtered */
    int16_t qEsqf;
    /* state variable for BEMF q Filtered */
    int32_t qEsqStateVar;
    /* filter constant for d-q BEMF */
    int16_t qKfilterEsdq;
    /* Estimated speed */
    int16_t qVelEstim;
    /* Filter constant for Estimated speed */
    int16_t qVelEstimFilterK;
    /* State Variable for Estimated speed */
    int32_t qVelEstimStateVar;
    /* Value from last control step Ialpha */
    int16_t qLastValpha;
    /* Value from last control step Ibeta */
    int16_t qLastVbeta;
    /* dIalphabeta/dt */
    int16_t qDIlimitLS;
    /* dIalphabeta/dt */
    int16_t qDIlimitHS;
    /*  last  value for Ialpha */
    int16_t qLastIalphaHS[8];
    /* last  value for Ibeta */
    int16_t qLastIbetaHS[8];
    /* estimator angle initial offset */
    int16_t qRhoOffset;

} ESTIM_PARM_T;

typedef struct {
    /* Rs value - stator resistance */
    int16_t qRs;
    /* Ls/dt value - stator inductance / dt - variable with speed */
    int16_t qLsDt;
    /* Ls/dt value - stator inductance / dt for base speed (nominal) */
    int16_t qLsDtBase;
    /* InvKfi constant value ( InvKfi = Omega/BEMF ) */
    int16_t qInvKFi;
    /* InvKfi constant - base speed (nominal) value */
    int16_t qInvKFiBase;            
} MOTOR_ESTIM_PARM_T;

ESTIM_PARM_T estimator;
MOTOR_ESTIM_PARM_T motor_parameters;
MC_ALPHABETA_T bemf_alphabeta;
MC_DQ_T bemf_dq;
MC_SINCOS_T sincos_theta_estimator;

#define DECIMATE_NOMINAL_SPEED      NOMINAL_SPEED_RPM * NOPOLEPAIRS / 10
#define NOMINAL_ELECTRICAL_SPEED    NOMINAL_SPEED_RPM * NOPOLEPAIRS

void estimate_angle () {

    int32_t tempint;
    uint16_t index = (estimator.qDiCounter - 7) & 0x0007;

    if (_Q15abs(estimator.qVelEstim) < NOMINAL_ELECTRICAL_SPEED) {

        estimator.qDIalpha = (i_alphabeta.alpha - estimator.qLastIalphaHS[index]);

        /* The current difference can exceed the maximum value per 8 ADC ISR
           cycle .The following limitation assures a limitation per low speed -
           up to the nominal speed */
        if (estimator.qDIalpha > estimator.qDIlimitLS) 
            estimator.qDIalpha = estimator.qDIlimitLS;

        if (estimator.qDIalpha<-estimator.qDIlimitLS) 
            estimator.qDIalpha = -estimator.qDIlimitLS;

        estimator.qVIndalpha = (int16_t) (__builtin_mulss(motor_parameters.qLsDt, estimator.qDIalpha) >> 10);

        estimator.qDIbeta = (i_alphabeta.beta - estimator.qLastIbetaHS[index]);

        /* The current difference can exceed the maximum value per 8 ADC ISR cycle
           the following limitation assures a limitation per low speed - up to
           the nominal speed */
        if (estimator.qDIbeta > estimator.qDIlimitLS) 
            estimator.qDIbeta = estimator.qDIlimitLS;

        if (estimator.qDIbeta<-estimator.qDIlimitLS) 
            estimator.qDIbeta = -estimator.qDIlimitLS;

        estimator.qVIndbeta = (int16_t) (__builtin_mulss(motor_parameters.qLsDt, estimator.qDIbeta) >> 10);

    } else {

        estimator.qDIalpha = (i_alphabeta.alpha - estimator.qLastIalphaHS[(estimator.qDiCounter)]);

        /* The current difference can exceed the maximum value per 1 ADC ISR cycle
           the following limitation assures a limitation per high speed - up to
           the maximum speed */
        if (estimator.qDIalpha > estimator.qDIlimitHS) 
            estimator.qDIalpha = estimator.qDIlimitHS;

        if (estimator.qDIalpha < -estimator.qDIlimitHS) 
            estimator.qDIalpha = -estimator.qDIlimitHS;
            
        estimator.qVIndalpha = (int16_t) (__builtin_mulss(motor_parameters.qLsDt, estimator.qDIalpha) >> 7);

        estimator.qDIbeta = (i_alphabeta.beta - estimator.qLastIbetaHS[(estimator.qDiCounter)]);

        /* The current difference can exceed the maximum value per 1 ADC ISR cycle
           the following limitation assures a limitation per high speed - up to
           the maximum speed */
        if (estimator.qDIbeta > estimator.qDIlimitHS) 
            estimator.qDIbeta = estimator.qDIlimitHS;

        if (estimator.qDIbeta < -estimator.qDIlimitHS) 
            estimator.qDIbeta = -estimator.qDIlimitHS;

        estimator.qVIndbeta = (int16_t) (__builtin_mulss(motor_parameters.qLsDt, estimator.qDIbeta) >> 7);

    } 

    /* Update  LastIalpha and LastIbeta */
    estimator.qDiCounter = (estimator.qDiCounter + 1) & 0x0007;
    estimator.qLastIalphaHS[estimator.qDiCounter] = i_alphabeta.alpha;
    estimator.qLastIbetaHS[estimator.qDiCounter] = i_alphabeta.beta;

    /* Stator voltage equations
     Ualpha = Rs * Ialpha + Ls dIalpha/dt + BEMF
     BEMF = Ualpha - Rs Ialpha - Ls dIalpha/dt */
    bemf_alphabeta.alpha =  v_alphabeta.alpha - (int16_t) (__builtin_mulss(motor_parameters.qRs, i_alphabeta.alpha) >> 14) - estimator.qVIndalpha;
    /* The multiplication between the Rs and Ialpha was shifted by 14 instead
       of 15 because the Rs value normalized exceeded Q15 range, so it was
       divided by 2 immediately after the normalization - in userparms.h */

    /* Ubeta = Rs * Ibeta + Ls dIbeta/dt + BEMF
       BEMF = Ubeta - Rs Ibeta - Ls dIbeta/dt */
    bemf_alphabeta.beta =   v_alphabeta.beta - (int16_t) (__builtin_mulss(motor_parameters.qRs, i_alphabeta.beta) >> 14) - estimator.qVIndbeta;
    /* The multiplication between the Rs and Ibeta was shifted by 14 instead of 15
     because the Rs value normalized exceeded Q15 range, so it was divided by 2
     immediately after the normalization - in userparms.h */

    MC_CalculateSineCosine_Assembly_Ram((estimator.qRho + estimator.qRhoOffset), &sincos_theta_estimator);

    /*  Park_BEMF.d =  Clark_BEMF.alpha*cos(Angle) + Clark_BEMF.beta*sin(Rho)
       Park_BEMF.q = -Clark_BEMF.alpha*sin(Angle) + Clark_BEMF.beta*cos(Rho)*/
    MC_TransformPark_Assembly(&bemf_alphabeta, &sincos_theta_estimator, &bemf_dq);

    /* Filter first order for Esd and Esq
       EsdFilter = 1/TFilterd * Integral{ (Esd-EsdFilter).dt } */
    tempint = (int16_t) (bemf_dq.d - estimator.qEsdf);
    estimator.qEsdStateVar += __builtin_mulss(tempint, estimator.qKfilterEsdq);
    estimator.qEsdf = (int16_t) (estimator.qEsdStateVar >> 15);

    tempint = (int16_t) (bemf_dq.q - estimator.qEsqf);
    estimator.qEsqStateVar += __builtin_mulss(tempint, estimator.qKfilterEsdq);
    estimator.qEsqf = (int16_t) (estimator.qEsqStateVar >> 15);

    /* OmegaMr= InvKfi * (Esqf -sgn(Esqf) * Esdf)
       For stability the condition for low speed */
    if (_Q15abs(estimator.qVelEstim) > DECIMATE_NOMINAL_SPEED) {

        if (estimator.qEsqf > 0) {
            tempint = (int16_t) (estimator.qEsqf - estimator.qEsdf);
            estimator.qOmegaMr =  (int16_t) (__builtin_mulss(motor_parameters.qInvKFi, tempint) >> 15);
        } else {
            tempint = (int16_t) (estimator.qEsqf + estimator.qEsdf);
            estimator.qOmegaMr = (int16_t) (__builtin_mulss(motor_parameters.qInvKFi, tempint) >> 15);
        }

    } else { // if estimator speed < 10% => condition VelRef<>0

        if (estimator.qVelEstim > 0) {
            tempint = (int16_t) (estimator.qEsqf - estimator.qEsdf);
            estimator.qOmegaMr = (int16_t) (__builtin_mulss(motor_parameters.qInvKFi, tempint) >> 15);
        } else {
            tempint = (int16_t) (estimator.qEsqf + estimator.qEsdf);
            estimator.qOmegaMr = (int16_t) (__builtin_mulss(motor_parameters.qInvKFi, tempint) >> 15);
        }

    }

    /* The result of the calculation above is shifted left by one because
       initial value of InvKfi was shifted by 2 after normalizing -
       assuring that extended range of the variable is possible in the
       lookup table the initial value of InvKfi is defined in userparms.h */
    estimator.qOmegaMr = estimator.qOmegaMr << 1;

    /* the integral of the angle is the estimated angle */
    estimator.qRhoStateVar += __builtin_mulss(estimator.qOmegaMr,
                                estimator.qDeltaT);
    estimator.qRho = (int16_t) (estimator.qRhoStateVar >> 15);


    /* The estimated speed is a filter value of the above calculated OmegaMr.
       The filter implementation is the same as for BEMF d-q components
       filtering */
    tempint = (int16_t) (estimator.qOmegaMr - estimator.qVelEstim);
    estimator.qVelEstimStateVar += __builtin_mulss(tempint,
                                    estimator.qVelEstimFilterK);
    estimator.qVelEstim = (int16_t) (estimator.qVelEstimStateVar >> 15);

}

void init_estimator_parameters () {

    motor_parameters.qLsDtBase = NORM_LSDTBASE;
    motor_parameters.qLsDt = motor_parameters.qLsDtBase;
    motor_parameters.qRs = NORM_RS;

    motor_parameters.qInvKFiBase = NORM_INVKFIBASE;
    motor_parameters.qInvKFi  = motor_parameters.qInvKFiBase;

    estimator.qRhoStateVar = 0;
    estimator.qOmegaMr = 0;
    estimator.qDiCounter = 0;
    estimator.qEsdStateVar = 0;
    estimator.qEsqStateVar = 0;

    estimator.qDIlimitHS = D_ILIMIT_HS;
    estimator.qDIlimitLS = D_ILIMIT_LS;

    estimator.qKfilterEsdq = KFILTER_ESDQ;
    estimator.qVelEstimFilterK = KFILTER_VELESTIM;

    estimator.qDeltaT = NORM_DELTAT;
    estimator.qRhoOffset = INITOFFSET_TRANS_OPEN_CLSD;

}