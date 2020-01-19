

extern uint16_t mcSineTableInRam[];

static inline uint16_t MC_CalculateSineCosine_InlineC_Ram(int16_t angle, MC_SINCOS_T *sincos)
{
    uint16_t remainder, index, y0, y1, delta, return_value;
    uint32_t result;

    return_value = 0;
    
    /* Index = (Angle*128)/65536 */
    result = __builtin_muluu(128,angle);
    index = (result & 0xFFFF0000)>>16;
    remainder = (uint16_t) result & 0xFFFF;

    /* Check if interpolation is required or not */
    if(remainder == 0)
    {
        /* No interpolation required, use index only */
        sincos->sin = mcSineTableInRam[index];
        index = index+32;
        if (index > 127)
        {
            index = index - 128;
        }    
        sincos->cos = mcSineTableInRam[index];
        return_value = 1;
    }
    else
    {
        /* Interpolation required. Determine the delta between indexed value
         * and the next value from the mcSineTableInRam and scale the remainder 
         * with delta to get the linear interpolated value. */

        y0 = mcSineTableInRam[index];
        index = index+1;
        if (index > 127)
        {
            index = index - 128;
        }
        y1 = mcSineTableInRam[index];
        delta = y1 - y0;
        result = __builtin_mulus(remainder,delta);
        sincos->sin = y0 + ((result & 0xFFFF0000)>>16);

        /* Increment by 32 for cosine index. Increment by 31 here
         * since index has already been incremented once. */
        index = index+31;
        if (index > 127)
        {
            index = index - 128;
        }
        
        y0 = mcSineTableInRam[index];
        index = index+1;
        if (index > 127)
        {
            index = index - 128;
        }
        y1 = mcSineTableInRam[index];
        delta = y1 - y0;
        result = __builtin_mulus(remainder,delta);
        sincos->cos = y0 + ((result & 0xFFFF0000)>>16);
        return_value = 2;
    }
    return(return_value);
}