package com.spreadtrum.sgps;

interface SatelliteDataProvider {
    // !!== Enlarge maxSatellites from 15 to 24 for AGPS usage ==
    // !!== Enlarge maxSatellites from 24 to max for multi-GNSS ==
    int MAX_SATELLITES_NUMBER = 352;
    int SATELLITES_MASK_SIZE = 11;
    int SATELLITES_MASK_BIT_WIDTH = 32;

    void setSatelliteStatus(int svCount, int[] prns, float[] snrs,
            float[] elevations, float[] azimuths, int ephemerisMask,
            int almanacMask, int[] usedInFixMask);

    int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
            float[] azimuths, int ephemerisMask, int almanacMask,
            int[] usedInFixMask);
}
