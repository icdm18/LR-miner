package a.b.c.d.tslrm.data;

import org.junit.Test;


public class AccuracyTest {
    @Test
    public void test()
    {
        double[] values = DataGenerator.generateData(-100, 100, 1000, 18, 1, 0);

        int bitLength = DataGenerator.detectBitLength(values,-100,100);
        System.out.println("bitLength = " + bitLength);

    }
}
