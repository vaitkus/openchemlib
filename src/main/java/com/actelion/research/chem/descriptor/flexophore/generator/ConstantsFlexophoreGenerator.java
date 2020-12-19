package com.actelion.research.chem.descriptor.flexophore.generator;

import com.actelion.research.chem.descriptor.flexophore.PPNode;

public class ConstantsFlexophoreGenerator {


    /**
     * Defines the resolution for the range.
     */
    public static final int BINS_HISTOGRAM = 80;

    public static final int INTERACTION_TYPE_NONE = -1;


    /**
     * Range histogram in Angstrom. As long as the molecule coordinates are in Angstrom.
     */
    public static final int RANGE_HISTOGRAM = 40;

    public static final int MAX_VAL_INTERACTION_TYPE = (int)Math.pow(2, Byte.SIZE * PPNode.NUM_BYTES_INTERACTION_TYPE)-1;


    public static final boolean OPTIMIZE_RIGID_FRAGS = false;


    /**
     *
     */
    public static final double [] FILTER01 = {1};
    public static final double [] FILTER03 = {0.25, 0.5, 0.25};
    public static final double [] FILTER05 = {0.125, 0.25,0.25,0.25, 0.125};
    public static final double [] FILTER07 = {0.125, 0.125, 0.125,0.25,0.125, 0.125, 0.125};
    public static final double [] FILTER09 = {0.06, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.06};

    public static final double [][] ARR_FILTER_FOR_BLURR = {
            FILTER03, FILTER03, FILTER05, FILTER07, FILTER07, FILTER07, FILTER09, FILTER09
    };

    /**
     * Filter for sliding window to blurr distance histograms
     * 07.04.2020
     */
    // public static final double [] FILTER = FILTER05;

    public static final double [] FILTER = FILTER07;

    public static double getResolution(){
        return RANGE_HISTOGRAM / (double) BINS_HISTOGRAM;
    }
}
