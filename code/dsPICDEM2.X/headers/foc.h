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

    int k = (phi / (PI / 3)) + 1;

    if (v_s > V_DS)
        v_s = V_DS;

    //float m = v_s / V_DS;
    float m = 0.57;

    float period_b = SQRT3_OVER_TWO * m * sin(((PI * k) / 3) - phi);
    float period_c = SQRT3_OVER_TWO * m * sin(-((PI * (k - 1)) / 3) + phi);
    float period_a = 1 - period_b - period_c;

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

    PDC1 = (uint16_t) (2 * period_1 * PTPER_VAL);
    PDC2 = (uint16_t) (2 * period_2 * PTPER_VAL);
    PDC3 = (uint16_t) (2 * period_3 * PTPER_VAL);

}

void foc () {

    float vq_ref, vd_ref;
    float v_alpha, v_beta;
    float theta;

    // Measure 3-phase currents  
    // Clark Transform
    // Park Transform
    // Generate errors
    // PI Controllers
    // Estimate position and speed
    // Inverse Park Transform
    foc_inv_park_transform(&v_alpha, &v_beta, vq_ref, vd_ref, theta)
    // SVM
    foc_svpwm(v_alpha, v_beta);

}