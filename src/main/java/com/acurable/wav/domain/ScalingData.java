package com.acurable.wav.domain;

public class ScalingData {

    private ScalingData(double floatScale, double floatOffset) {
        this.floatScale = floatScale;
        this.floatOffset = floatOffset;
    }

    // Scaling factor used for int <-> float conversion
    private final double floatScale;

    // Offset factor used for int <-> float conversion
    private final double floatOffset;

    public static ScalingData calculateScaling(int validBits) {


        double floatOffset;
        double floatScale;

        // Calculate the scaling factor for converting to a normalised double
        if (validBits > 8)
        {
            // If more than 8 validBits, data is signed
            // Conversion required dividing by magnitude of max negative value
            floatOffset = 0;
            floatScale = 1 << (validBits - 1);
        }
        else
        {
            // Else if 8 or less validBits, data is unsigned
            // Conversion required dividing by max positive value
            floatOffset = -1;
            floatScale = 0.5 * ((1 << validBits) - 1);
        }

        return new ScalingData(floatScale,floatOffset);
    }


    double scaleSample(double sample){
        return floatOffset + sample / floatScale;
    }



}
