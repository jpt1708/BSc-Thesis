package tom.utils;


import org.apache.commons.math3.random.MersenneTwister;

public class NumpyRandom extends MersenneTwister {

    private boolean has_gauss = false;
    private double gauss = 0;

    public NumpyRandom(int i) {
        super(i);
    }

    public double nextDouble() {
        int a = next(32) >>> 5;
        int b = next(32) >>> 6;
        return (a * 67108864.0 + b) / 9007199254740992.0;
    }

    public double staticNextDoubleFromTo(int x, int y) {
        return nextDouble();
    }

    public double nextGaussian() {
        if (has_gauss) {
            double tmp = gauss;
            gauss = 0;
            has_gauss = false;
            return tmp;
        } else {
            double x1, x2, s;

            do {
                x1 = 2.0 * nextDouble() - 1.0;
                x2 = 2.0 * nextDouble() - 1.0;
                s = x1 * x1 + x2 * x2;
            } while (s >= 1.0 || s == 0.0);

            double f = Math.sqrt(-2.0 * Math.log(s) / s);
            gauss = f * x1;
            has_gauss = true;
            return f * x2;
        }
    }

}

