package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.ClimateDATAUtils;
import a.b.c.d.tslrm.data.DataGenerator;
import a.b.c.d.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class RegionSearchBeforeTest {
    @Test
    public void testRegionSearch() throws IOException {
        String fileName = "D:\\data\\TEM-Record-1960-2012(Length = 20000).txt";



        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
        System.out.println("PointNum = " + point2Ds.length);
        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());





        long time = System.currentTimeMillis();
        List<PLASegment> segmentList = miner.buildSpecificSegments(3);
        time = System.currentTimeMillis() - time;
        System.out.println("Build Segment Time = " + time);
        System.out.println("all segmentList.size() = " + segmentList.size());

        RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
        plaRegionSearch.errorBound = errorBound;


        System.out.println("segs.size() = " + segmentList.size());

        time = System.currentTimeMillis();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segmentList, 0.05);
        time = System.currentTimeMillis() - time;
        System.out.println("Region Search Time = " + time);
        System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
        System.out.println("point2Ds1.getY() = " + point2Ds1.getY());
        int realLength = SegmentUtils.verifyTrueLength(point2Ds, point2Ds1.getX(), point2Ds1.getY(), errorBound, 3);
        System.out.println("RealLength = " + realLength);


















    }
    
    public void testRegionSearchWithSpecific() throws IOException {
        String fileName = "D:\\data\\TEM-1960-2012.txt";
        String fileResult = "D:\\data\\result_length-running.txt";
        PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
        String line1 = "0";
        String line2 = "0";
        String line3 = "0";


        int length = 2000;
        for(int i = 0; i < 10; i++){
        	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
            System.out.println("PointNum = " + point2Ds.length);
            double errorBound = 1;

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
            miner.process();
            System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());





            long time1 = System.currentTimeMillis();
            List<PLASegment> segmentList = miner.buildSpecificSegments(3);
            time1 = System.currentTimeMillis() - time1;
            System.out.println("Build Segment Time = " + time1);
            System.out.println("all segmentList.size() = " + segmentList.size());

            RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
            plaRegionSearch.errorBound = errorBound;


            System.out.println("segs.size() = " + segmentList.size());

            long time2 = System.currentTimeMillis();
            Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segmentList, 0.05);
            time2 = System.currentTimeMillis() - time2;
            System.out.println("Region Search Time = " + time2);
            System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
            System.out.println("point2Ds1.getY() = " + point2Ds1.getY());
            int realLength = SegmentUtils.verifyTrueLength(point2Ds, point2Ds1.getX(), point2Ds1.getY(), errorBound, 3);
            System.out.println("RealLength = " + realLength);
            
            line1 += " " + length;
            line2 += " " + time1;
            line3 += " " + time2;
            length += 2000;
        }
        
        pw.println(line1);
        pw.println(line2);
        pw.println(line3);
        


















    }


}
