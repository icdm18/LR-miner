package a.b.c.d.expriment;

import a.b.c.d.tslrm.PLARegionSearch;
import a.b.c.d.tslrm.PLASegment;
import a.b.c.d.tslrm.RegionSearchBefore;
import a.b.c.d.tslrm.TSPLAPointBoundKBMiner;
import a.b.c.d.tslrm.data.ClimateDATAUtils;
import math.geom2d.Point2D;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class AccuracyVSTimeExperiment {
	

	public void PLARegionSearchTest(String fileName, double error, int baseBound, int length, int period) throws IOException{		    	
		String resultFile = "D:\\data\\experiments\\accuracyVstime_Original(length=";
    	resultFile += length + ",error=" + error + ").txt";
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);                

        RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracy(segs, error, period, baseBound);       
        stopWatch.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
                
        System.out.println("stopWatch2.getTime() = " + stopWatch.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
        System.out.println("point2Ds1.getY() = " + point2Ds1.getY());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	

	public void PLARegionSearchWithListTest(String fileName, double error, int baseBound, int length, int period) throws IOException{
		String resultFile = "D:\\data\\experiments\\accuracyVstime_List(length=";
    	resultFile += length + ",error=" + error + ").txt";
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);                

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracy(segs, error, period, baseBound);       
        stopWatch.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
                
        System.out.println("stopWatch.getTime() = " + stopWatch.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
        System.out.println("point2Ds1.getY() = " + point2Ds1.getY());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	


	public void PLARegionSearchWithListInsideTest(String fileName, double error, int baseBound, int length, int period) throws IOException{
		String resultFile = "D:\\data\\experiments\\accuracyVstime_ListInsides(length=";
    	resultFile += length + ",error=" + error + ").txt";
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);                

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracyInsides(segs, error, period, baseBound);       
        stopWatch.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
        
        System.out.println("FinalUpperBound = " + plaRegionSearch.maxUpBound);
        System.out.println("stopWatch.getTime() = " + stopWatch.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
        System.out.println("point2Ds1.getY() = " + point2Ds1.getY());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	
	public static void main(String[] args) throws IOException{
		String fileName = "D:\\data\\experiments\\Experiment_Tem_Record_1960-2012(length=100000).txt";
		double error = 0.01;
		int baseBound = 22225;
		int length = 80000;
		int period = 40000;
		AccuracyVSTimeExperiment expt = new AccuracyVSTimeExperiment();	
		expt.PLARegionSearchWithListInsideTest(fileName, error, baseBound, length, period);
		System.out.println("****************************************");
		expt.PLARegionSearchWithListTest(fileName, error, baseBound, length, period);
		System.out.println("****************************************");
		expt.PLARegionSearchTest(fileName, error, baseBound, length, period);
	}

}
