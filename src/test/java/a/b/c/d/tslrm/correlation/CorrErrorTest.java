package a.b.c.d.tslrm.correlation;

import a.b.c.d.tslrm.ArrayUtil;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;


public class CorrErrorTest {
    @Test
    public void test() {
        double k = 3;
        double b = 2;
        double error = 100;
        int count = 10;

        double[] x = new double[count];
        double[] y = new double[count];

        for (int i = 0; i < y.length; i++) {
            x[i] = Math.random() * 100;
            y[i] = x[i] * k + b + Math.random() * error;
        }

        SimpleRegression sr = new SimpleRegression();
        for (int i = 0; i < y.length; i++) {
            sr.addData(x[i], y[i]);
        }
        double r = sr.getR();
        System.out.println("r = " + r);
        double slope = sr.getSlope();
        System.out.println("slope = " + slope);
        double intercept = sr.getIntercept();
        System.out.println("intercept = " + intercept);
    }


    MaxCorrBrute maxCorrBrute = new MaxCorrBrute();

    @Test
    public void test2Parallel() {
        int count = 18;
        double[] x1 = new double[count];
        double[] y1 = new double[count];

        double k = 2;
        double b1 = 1;
        double b2 = 3;
        double range = 1;
        DescriptiveStatistics xs = new DescriptiveStatistics();
        DescriptiveStatistics ys = new DescriptiveStatistics();
        for (int i = 0; i < x1.length; i++) {
            if (i % 2 == 0) {
                x1[i] = i / 2;
                y1[i] = k * x1[i] + b1;
            } else {
                x1[i] = i / 2;
                y1[i] = k * x1[i] + b2;
            }
            xs.addValue(x1[i]);
            ys.addValue(y1[i]);
            System.out.println("i = " + i);
            System.out.println("x1[i] = " + x1[i]);
            System.out.println("y1[i] = " + y1[i]);
        }
        System.out.println("xs = " + xs);
        System.out.println("ys = " + ys);

        SimpleRegression sr = ArrayUtil.getSimpleRegression(x1, y1);
        System.out.println("sr.getR() = " + sr.getR());
        System.out.println("sr.getSlope() = " + sr.getSlope());
        System.out.println("sr.getIntercept() = " + sr.getIntercept());


        for (int j = 1; j < 11; j++) {
            System.out.println("j = " + j);
            int[] idsByBruteForce = maxCorrBrute.getIdsByBruteForce(x1, y1, j);
            for (int i = 0; i < idsByBruteForce.length; i++) {
                int i1 = idsByBruteForce[i];
                System.out.println("i1 = " + i1 + " " + x1[i1] + "," + y1[i1]);
            }
        }


























    }

    @Test
    public void testAdd() {
        double[] x = new double[]{1, 2, 3, 4, 5};
        double[] y = new double[]{1, 3, 2, 3, 9};

        SimpleRegression simpleRegression = ArrayUtil.getSimpleRegression(x, y);
        System.out.println("simpleRegression.getR() = " + simpleRegression.getR());

        double[] x1 = new double[x.length + 1];
        double[] y1 = new double[x1.length];
        System.arraycopy(x,0,x1,0,x.length);
        System.arraycopy(y,0,y1,0,x.length);

        x1[x1.length-1] = -100;
        y1[y1.length-1] = 100;

        simpleRegression = ArrayUtil.getSimpleRegression(x1, y1);
        System.out.println("simpleRegression.getR() = " + simpleRegression.getR());

    }
}
