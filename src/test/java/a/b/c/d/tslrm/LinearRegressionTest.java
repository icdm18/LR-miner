package a.b.c.d.tslrm;



import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;

import org.apache.log4j.Logger;

import java.io.*;

public class LinearRegressionTest {
    private Logger logger = Logger.getLogger(LinearRegressionTest.class);

    @Test
    public void testLinearRegression() {
        SimpleRegression simpleRegression = new SimpleRegression();







        double[][] xy = new double[][]{
                {208, 21.6},
                {152, 15.5},
                {113, 10.4},
                {227, 31.0},
                {137, 13.0},
                {238, 32.4},
                {178, 19.0},
                {104, 10.4},
                {191, 19.0},
                {130, 11.8}
        };

        simpleRegression.addData(xy);















        double intercept = simpleRegression.getIntercept();
        System.out.println("intercept = " + intercept);
        double slope = simpleRegression.getSlope();
        System.out.println("slope = " + slope);

        double meanSquareError = simpleRegression.getMeanSquareError();
        System.out.println("meanSquareError = " + meanSquareError);
        System.out.println("meanSquareError*(xy.length-2) = " + meanSquareError*(xy.length-2));

        double regressionSumSquares = simpleRegression.getRegressionSumSquares();
        System.out.println("regressionSumSquares = " + regressionSumSquares);

        double sumSquaredErrors = simpleRegression.getSumSquaredErrors();
        System.out.println("sumSquaredErrors = " + sumSquaredErrors);

        simpleRegression.getSignificance();

        double mse = 0;
        for (int i = 0; i < xy.length; i++) {
            double x = xy[i][0];
            double y = xy[i][1];

            double ey = x * slope + intercept;



            mse += (ey-y)*(ey-y);
        }
        System.out.println("mse = " + mse);





        double mse1 = 0;
        for (int i = 0; i < xy.length; i++) {
            double x = xy[i][0];
            double y = xy[i][1];

            double ex = (y-intercept) / slope;



            mse1 += (ex-x)*(ex-x);
        }
        System.out.println("mse1 = " + mse1);

        System.out.println("sqrt(mse/mse1) = " + Math.sqrt(mse/mse1));


    }


    @Test
    public void test2()
    {
        SimpleRegression reg = new SimpleRegression();
        reg.addData(10,19);
        reg.addData(9,19);
        reg.addData(6,12);

        double slope = reg.getSlope();
        System.out.println("slope = " + slope);
        double intercept = reg.getIntercept();
        System.out.println("intercept = " + intercept);
        double v = reg.getSumSquaredErrors() / reg.getN();
        System.out.println("v = " + v);

        System.out.println("reg.getN() = " + reg.getN());

    }

    @Test
    public void test3() throws IOException, UnsupportedEncodingException {
        String s = "123中国";
        File file = new File("1.txt");
        FileOutputStream fos = new FileOutputStream(file);
        System.out.println(file.getAbsolutePath() );
        byte[] bytes = s.getBytes("utf-8");
        for (int i = 0; i < bytes.length; i++) {
            byte aByte = bytes[i];
            System.out.println("aByte = " + aByte);
        }
        fos.write(bytes);
        fos.flush();
        fos.close();
    }
}
