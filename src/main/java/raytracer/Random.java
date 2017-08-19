package raytracer;

public class Random {
    private static final int MERS_N = 624;
    private static final int MERS_M = 397;
    private static final int MERS_R = 31;
    private static final int MERS_U = 11;
    private static final int MERS_S = 7;
    private static final int MERS_T = 15;
    private static final int MERS_L = 18;
    private static final int MERS_A = 0x9908B0DF;
    private static final int MERS_B = 0x9D2C5680;
    private static final int MERS_C = 0xEFC60000;

    int mt[];                   // state vector
    int mti;                             // index into mt

    public Random() {
        this(2);
    }

    public Random(int seed) {
        // re-seed generator
        init(seed);
    }

    private void init(int seed) {
        mt = new int[MERS_N];
        mt[0] = seed;
        for (mti = 1; mti < MERS_N; mti++) {
            mt[mti] = (1812433253 * (mt[mti - 1] ^ (mt[mti - 1] >> 30)) + mti);
        }
    }


    private void initByArray(int seeds[], int length) {
        // seed by more than 32 bits
        int i, j, k;
        init(19650218);
        if (length <= 0) return;
        i = 1;
        j = 0;
        k = (MERS_N > length ? MERS_N : length);
        for (; k > 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >> 30)) * 1664525))+seeds[j] + j;
            i++;
            j++;
            if (i >= MERS_N) {
                mt[0] = mt[MERS_N - 1];
                i = 1;
            }
            if (j >= length) j = 0;
        }
        for (k = MERS_N - 1; k > 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >> 30)) * 1566083941))-i;
            if (++i >= MERS_N) {
                mt[0] = mt[MERS_N - 1];
                i = 1;
            }
        }
        mt[0] = 0x80000000; // MSB is 1; assuring non-zero initial array
    }

    int rand() {
        // generate 32 random bits
        int y;

        if (mti >= MERS_N) {
            // generate MERS_N words at one time
            int LOWER_MASK = (1 << MERS_R)-1; // lower MERS_R bits
            int UPPER_MASK = -1 << MERS_R;      // upper (32 - MERS_R) bits
            int mag01[] = {0, MERS_A} ;

            int kk;
            for (kk = 0; kk < MERS_N - MERS_M; kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + MERS_M] ^ (y >> 1) ^ mag01[y & 1];
            }

            for (; kk < MERS_N - 1; kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (MERS_M - MERS_N)] ^ (y >> 1) ^ mag01[y & 1];
            }

            y = (mt[MERS_N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[MERS_N - 1] = mt[MERS_M - 1] ^ (y >> 1) ^ mag01[y & 1];
            mti = 0;
        }

        y = mt[mti++];

        // Tempering (May be omitted):
        y ^= y >> MERS_U;
        y ^= (y << MERS_S) & MERS_B;
        y ^= (y << MERS_T) & MERS_C;
        y ^= y >> MERS_L;
        return y;
    }


    float random1() {
        int r = ((1 << 23) - 1) & rand();
        return Float.intBitsToFloat((0x7F << 23) | r) - 1.0f;
    }

    float random2() {
        int r = ((1 << 23) - 1) & rand();
        return Float.intBitsToFloat((0x80 << 23) | r) - 3.0f;
    }

    int rand(int min, int max) {
        // output random integer in the interval min <= x <= max
        int r;
        r = ((max - min) + 1) * rand() + min; // mtiply interval with random and truncate
        if (r > max) r = max;
        if (max < min) return 0x80000000;
        return r;
    }

}
