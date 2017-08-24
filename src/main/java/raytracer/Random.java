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
    private java.util.Random r = new java.util.Random();

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

    static int x=123456789, y=362436069, z=521288629;

    int rand() {
        int t;
        x ^= x << 16;
        x ^= x >> 5;
        x ^= x << 1;

        t = x;
        x = y;
        y = z;
        z = t ^ x ^ y;

        return z;
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
