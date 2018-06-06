package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.ForexData;
import a.b.c.d.tslrm.data.Point2DUtils;
import a.b.c.d.tslrm.data.SegmentUtils;
import a.b.c.d.tslrm.log.DefaultFileAppender;
import math.geom2d.Point2D;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ForexCorrelationTest {
    static Logger logger = Logger.getLogger(ForexDataReginSearchTest.class);

    @Test
    public void test() throws IOException {
        Logger.getRootLogger().addAppender(new DefaultFileAppender(this.getClass()));
        double errorBound = 0.1;
        double accuracy = 0.005;
        int consecutive = 3;
        logger.debug("errorBound = " + errorBound);
        logger.debug("accuracy = " + accuracy);
        logger.debug("consecutive = " + consecutive);

        File dir = new File("data/currency");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("60_");
            }
        });

        System.out.println("files.length = " + files.length);

        ForexData[] forexDatas = new ForexData[files.length];
        for (int i = 0; i < forexDatas.length; i++) {
            forexDatas[i] = ForexData.readFromFile(files[i].getAbsolutePath());
            logger.debug("read " + forexDatas[i].secondCurrency);
        }

        List<ForexCorrelation> forexCorrelations = new ArrayList<ForexCorrelation>();


        for (int i = 0; i < forexDatas.length; i++) {
            ForexData dataX = forexDatas[i];
            logger.debug("==========dataX.secondCurrency = " + dataX.secondCurrency);
            for (int j = 0; j < forexDatas.length; j++) {
                ForexData dataY = forexDatas[j];

                if (!dataX.secondCurrency.equals(dataY.secondCurrency))
                {
                    logger.debug("----------dataY.secondCurrency = " + dataY.secondCurrency);

                    ForexCorrelation forexCorrelation = regionSearch(dataX, dataY, errorBound, accuracy, consecutive);
                    logger.debug("forexCorrelation = " + forexCorrelation);
                    forexCorrelations.add(forexCorrelation);
                }
            }
        }

        logger.debug("forexCorrelations.size() = " + forexCorrelations.size());
        logger.debug("----------------do for cross relation ---------------");

        for (int i = 0; i < forexCorrelations.size(); i++) {
            ForexCorrelation forexCorrelation1 = forexCorrelations.get(i);

            logger.debug("forexCorrelation1.currencyX = " + forexCorrelation1.currencyX);
            for (int j = 0; j < forexCorrelations.size(); j++) {
                ForexCorrelation forexCorrelation2 = forexCorrelations.get(j);
                if (forexCorrelation1.currencyX.equals(forexCorrelation2.currencyX))
                {

                    logger.debug("forexCorrelation1.currencyY = " + forexCorrelation1.currencyY);
                    logger.debug("forexCorrelation2.currencyY = " + forexCorrelation2.currencyY);

                    double v = CorrelationUtil.calcCorrelation(forexCorrelation1.positions, forexCorrelation2.positions);
                    logger.debug("relative = " + v);
                }
            }
        }
    }

    public static ForexCorrelation regionSearch(ForexData dataX, ForexData dataY, double errorBound, double accuracy, int consecutive)
    {
        logger.debug("begin ...");
        Point2D[] point2Ds = Point2DUtils.genFromForexData(dataX, dataY, 1);
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);

        List<PLASegment> segs = miner.buildSpecificSegments(consecutive);
        logger.debug("all segmentList.size() = " + segs.size());
        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;

        for(int i = segs.size() - 1; i >= 0; i--){
            if(segs.get(i).getPolygonKB().getRings().size() > 1){
                segs.remove(i);
                logger.debug("Remove at " + i);
            }
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, accuracy);
        stopWatch2.stop();

        logger.debug("point2Ds1.getX() k= " + point2Ds1.getX());
        logger.debug("point2Ds1.getY() b= " + point2Ds1.getY());
        logger.debug("PartitionNum = " + plaRegionSearch.partitionNum);
        logger.debug("RealLength = " + plaRegionSearch.finalLength);
        logger.debug("CountInsides = " + plaRegionSearch.countInsides);
        logger.debug("stopWatchInsides = " + plaRegionSearch.stopWatchInsides.getTime());
        Set<Integer> positions = SegmentUtils.verifyTrueLengthReturnPoints(point2Ds,point2Ds1.getX(),point2Ds1.getY(),errorBound,consecutive);
        logger.debug("positions.size() = " + positions.size());
        ForexCorrelation ret = new ForexCorrelation(dataX.secondCurrency,dataY.secondCurrency,errorBound,point2Ds1.getX(),point2Ds1.getY(),plaRegionSearch.finalLength,dataX,dataY,positions);

        return ret;
    }

}


