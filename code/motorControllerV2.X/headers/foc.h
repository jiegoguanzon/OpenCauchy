void foc_get_phase_currents (float *phase_a_current, float *phase_b_current, float *phase_c_current) {

    *phase_a_current = ((V_REF / 2) - DRV_CSA) / (SENSE_GAIN * R_SENSE);
    *phase_b_current = ((V_REF / 2) - DRV_CSB) / (SENSE_GAIN * R_SENSE);
    *phase_c_current = ((V_REF / 2) - DRV_CSC) / (SENSE_GAIN * R_SENSE);

}

void foc_clark_transform (float *i_alpha, float *i_beta, float phase_a_current, float phase_b_current) {

    *i_alpha = phase_a_current;
    *i_beta = (ONE_OVER_SQRT3 * phase_a_current) + (TWO_OVER_SQRT3 * phase_b_current);

}

void foc_park_transform (float *i_d, float *i_q, float i_alpha, float i_beta, float theta) {

    *i_d = (i_alpha * cos(theta)) + (i_beta * sin(theta));
    *i_q = (i_beta * cos(theta)) - (i_alpha * sin(theta));

}

void foc_inv_park_transform (float *v_alpha, float *v_beta, float vq_ref, float vd_ref, float theta) {
    
    vd_ref = 9.0;
    vq_ref = 7.0;

    *v_alpha = (vd_ref * cos(theta)) - (vq_ref * sin(theta));
    *v_beta = (vq_ref * cos(theta)) + (vd_ref * sin(theta));

}

void foc_svpwm (float v_alpha, float v_beta) {

    float v_s = sqrt(v_alpha * v_alpha + v_beta * v_beta);
    float phi = atan2f(v_beta, v_alpha) + PI;
    //phi = angle * PI / 180;

    int k = (phi / (PI / 3)) + 1;

    if (v_s > V_DS)
        v_s = V_DS;

    //float m = v_s / V_DS;
    float m = 0.57;

    float period_b = SQRT3_OVER_TWO * m * PWM_PERIOD * sin(((PI * k) / 3) - phi);
    float period_c = SQRT3_OVER_TWO * m * PWM_PERIOD * sin(-((PI * (k - 1)) / 3) + phi);
    //float period_b = SQRT3_OVER_TWO * m * PWM_PERIOD * sine_lookup(((PI * k) / 3) - phi);
    //float period_c = SQRT3_OVER_TWO * m * PWM_PERIOD * sine_lookup(-((PI * (k - 1)) / 3) + phi);
    float period_a = PWM_PERIOD - period_b - period_c;

    float period_1;
    float period_2;
    float period_3;

    switch (k) {

        case 1:
            period_1 = period_b + period_c + (period_a / 2);
            period_2 = period_c + (period_a / 2);
            period_3 = period_a / 2;
            break;
        case 2:
            period_1 = period_b + (period_a / 2);
            period_2 = period_b + period_c + (period_a / 2);
            period_3 = period_a / 2;
            break;
        case 3:
            period_1 = period_a / 2;
            period_2 = period_b + period_c + (period_a / 2);
            period_3 = period_c + (period_a / 2);
            break;
        case 4:
            period_1 = period_a / 2;
            period_2 = period_b + (period_a / 2);
            period_3 = period_b + period_c + (period_a / 2);
            break;
        case 5:
            period_1 = period_c + (period_a / 2);
            period_2 = period_a / 2;
            period_3 = period_b + period_c + (period_a / 2);
            break;
        case 6:
            period_1 = period_b + period_c + (period_a / 2);
            period_2 = period_a / 2;
            period_3 = period_b + (period_a / 2);
            break;

    }

    OC1R = (uint16_t) (399 - (period_1 / PWM_PERIOD * 399));
    OC2R = (uint16_t) (399 - (period_2 / PWM_PERIOD * 399));
    OC3R = (uint16_t) (399 - (period_3 / PWM_PERIOD * 399));

}
