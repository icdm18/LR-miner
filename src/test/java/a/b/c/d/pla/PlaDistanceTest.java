package a.b.c.d.pla;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;


public class PlaDistanceTest {

    @Test
    public void test() {
        System.out.println("PlaDistanceTest.test");

        long round = 0;
        while (true) {
            round ++;
            if (round % 1000000 == 0)
                System.out.println("round = " + round);

            int count = (int) (Math.random() * 10 + 3);
            double[] axis = new double[count];
            for (int i = 0; i < axis.length; i++) {
                axis[i] = i;
            }

            double max = 100;
            double min = -100;
            double[] x = generate(min, max, count);
            double[] y = generate(min, max, count);

            double dist = squareDistance(x, y);
            SimpleRegression rx = getSimpleRegression(axis,x);
            SimpleRegression ry = getSimpleRegression(axis,y);
            double plaDist = plaSquareDistance(rx.getSlope(), rx.getIntercept(), ry.getSlope(), ry.getIntercept(), count);

            double xsse = rx.getSumSquaredErrors();
            double ysse = ry.getSumSquaredErrors();

            double upper = xsse + ysse;
            double lower = Math.abs(xsse - ysse);

            double diff = dist - plaDist;
            double min1 = Math.min(xsse, ysse);
            double max1 = Math.max(xsse, ysse);

            if (diff > upper * 2 + 0.00001)

            {
            System.out.println("diff = " + diff);
            System.out.println("upper = " + upper);
            System.out.println("lower = " + lower);
            System.out.println("xsse = " + xsse);
            System.out.println("ysse = " + ysse);
                throw new RuntimeException("10");
            }

































        }
    }

    private void print(double[] y) {
        for (int i = 0; i < y.length; i++) {
            double v = y[i];
            System.out.println("v = " + v);
        }
    }

    public static SimpleRegression getSimpleRegression(double[] x, double[] y) {
        SimpleRegression ret = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            ret.addData(x[i], y[i]);
        }
        return ret;
    }


    public static double[] generate(double min, double max, int count) {
        double[] ret = new double[count];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Math.random() * (max - min) + min;
        }
        return ret;
    }

    @Test
    public void testGenerate() {
        double[] vs = generate(-100, 200, 13);
        for (int i = 0; i < vs.length; i++) {
            double v = vs[i];
            System.out.println("v = " + v);
        }
    }

    public static double squareDistance(double[] x, double[] y) {
        double ret = 0;
        for (int i = 0; i < x.length; i++) {
            ret = ret + (y[i] - x[i]) * (y[i] - x[i]);
        }
        return ret;
    }

    public static double plaSquareDistance(double a1, double b1, double a2, double b2, int count) {
        double ret = 0;
        double a3 = a1 - a2;
        double b3 = b1 - b2;
        for (int i = 0; i < count; i++) {
            ret = ret + (a3 * i + b3) * (a3 * i + b3);
        }
        return ret;
    }
}
