package a.b.c.d.expriment;

import a.b.c.d.tslrm.PLARegionSearch;
import a.b.c.d.tslrm.PLASegment;
import a.b.c.d.tslrm.TSPLAPointBoundKBMiner;
import a.b.c.d.tslrm.data.ForexData;
import a.b.c.d.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;


public class CurrencyTimeVsSegmentLength {

    private static Logger logger = Logger.getLogger(CurrencyTimeVsSegmentLength.class);

    private static final String[] currencies = {"CAD", "CHF", "CZK", "DKK", "HKD", "HUF", "JPY", "MXN", "NOK", "PLN", "SEK", "SGD", "TRY", "ZAR"};

    public static void main(String args[]) throws IOException {
        CurrencyTimeVsSegmentLength css = new CurrencyTimeVsSegmentLength();
        double error = 0.05;
        double errorBound = 0.1;



        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 8, true, 50000);



    }

    private void searchAllWithPointBasedOpti(String baseCurrency, double errorBound, double error, int segmentLength, boolean z_normalization, int length) throws IOException {



        double averageTime = 0, all = 0;

        for (String targetCurrency : currencies) {
            if (targetCurrency.equals(baseCurrency)) continue;
            logger.info(baseCurrency + "-" + targetCurrency);

            String targetCurrencyFilename = "data/currency/5_USD_" + targetCurrency + ".csv";
            ForexData targetCurrencyData = ForexData.readFromFile(targetCurrencyFilename);
            ForexData baseCurrencyData;
            if (baseCurrency.equals("USD")) {
                baseCurrencyData = ForexData.generateUSDFrom(targetCurrencyData);
            } else {
                String baseCurrencyFilename = "data/currency/5_USD_" + baseCurrency + ".csv";
                baseCurrencyData = ForexData.readFromFile(baseCurrencyFilename);
            }
            Point2D[] point2Ds = Point2DUtils.genFromForexData(baseCurrencyData, targetCurrencyData, z_normalization ? 1 : 0, length);






            long startTime = System.currentTimeMillis();

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
            miner.process();
            List<PLASegment> segs = miner.buildSpecificSegments(segmentLength);

            PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
            plaRegionSearch.errorBound = errorBound;
            for (int j = segs.size() - 1; j >= 0; j--) {
                if (segs.get(j).getPolygonKB().getRings().size() > 1) {
                    segs.remove(j);
                    System.out.println("Remove at " + j);
                }
            }

            Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
            double k = point2Ds1.getX();
            double b = point2Ds1.getY();

            long endTime = System.currentTimeMillis();

            all++;
            averageTime = averageTime + (endTime - startTime - averageTime) / all;

            System.out.println(averageTime);


        }
        System.out.println(segmentLength + "," + averageTime);

    }

}
