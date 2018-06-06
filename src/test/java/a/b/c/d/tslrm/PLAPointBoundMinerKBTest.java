package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.DataGenerator;
import math.geom2d.Point2D;
import org.apache.log4j.Logger;
import org.junit.Test;


public class PLAPointBoundMinerKBTest {
    private Logger logger = Logger.getLogger(PLAPointBoundMinerKBTest.class);

    @Test
    public void testRandomWalk()
    {
        Point2D[] point2Ds = DataGenerator.randomWalk(1000*10, 1);

        double errorBound = 1;
        PLAPointBoundKBMiner miner = new PLAPointBoundKBMiner(point2Ds,errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
            System.out.println("segment = " + segment.toStringKB());
        }




        PLAPointBoundKBMiner.findMaxIntersection(miner.plaSegmentList);

    }
}
