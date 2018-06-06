package a.b.c.d.expriment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import math.geom2d.Point2D;

import a.b.c.d.tslrm.PLARegionSearch;
import a.b.c.d.tslrm.PLASegment;
import a.b.c.d.tslrm.RegionSearchBefore;
import a.b.c.d.tslrm.TSPLAPointBoundKBMiner;
import a.b.c.d.tslrm.data.ClimateDATAUtils;

public class RealLengthVsSegmentLengthExperiment {
	

	public void PLARegionSearchTest(String fileName, int[] segLength, double error, int length) throws IOException{
		  String fileResult = "D:\\data\\experiments\\result_RealLengthVsSegLength_Origional(error=" + error + ",length=" + length + ").txt";	      
	      String line1 = "";
	      String line2 = "";
	      double errorBound = 1;
	      PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
	      
	      for(int k = 0; k < segLength.length; k++){
	    	  Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
	          TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	          miner.process();          	        
	          List<PLASegment> segs = miner.buildSpecificSegments(segLength[k]);          
	          RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
	          plaRegionSearch.errorBound = errorBound;
	          
	          for(int i = segs.size() - 1; i >= 0; i--){
	          	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	          		segs.remove(i);
	          		System.out.println("Remove at " + i);
	          	}
	          }

	          Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segs, error);
	          if(k == segLength.length - 1){
	        	  line1 += segLength[k];
	        	  line2 += plaRegionSearch.finalLength;
	          }else{
	        	  line1 += segLength[k] + "\t";
	        	  line2 += plaRegionSearch.finalLength + "\t";
	          }
	      }
	      pw.println(line1);
	      pw.println(line2);
	      pw.close();
	}
	

	public void PLARegionSearchWithListTest(String fileName, int[] segLength, double error, int length) throws IOException{
		  String fileResult = "D:\\data\\experiments\\result_RealLengthVsSegLength_List(error=" + error + ",length=" + length + ").txt";	      
	      String line1 = "";
	      String line2 = "";
	      double errorBound = 1;
	      PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
	      
	      for(int k = 0; k < segLength.length; k++){
	    	  Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
	          TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	          miner.process();          	        
	          List<PLASegment> segs = miner.buildSpecificSegments(segLength[k]);          
	          PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	          plaRegionSearch.errorBound = errorBound;
	          
	          for(int i = segs.size() - 1; i >= 0; i--){
	          	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	          		segs.remove(i);
	          		System.out.println("Remove at " + i);
	          	}
	          }

	          Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segs, error);
	          if(k == segLength.length - 1){
	        	  line1 += segLength[k];
	        	  line2 += plaRegionSearch.finalLength;
	          }else{
	        	  line1 += segLength[k] + "\t";
	        	  line2 += plaRegionSearch.finalLength + "\t";
	          }
	      }
	      pw.println(line1);
	      pw.println(line2);
	      pw.close();
	}
	


	public void PLARegionSearchWithListInsideTest(String fileName, int[] segLength, double error, int length) throws IOException{
		  String fileResult = "D:\\data\\experiments\\result_RealLengthVsSegLength_ListInsides(error=" + error + ",length=" + length + ").txt";	      
	      String line1 = "";
	      String line2 = "";
	      double errorBound = 1;
	      PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
	      
	      for(int k = 0; k < segLength.length; k++){
	    	  Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
	          TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	          miner.process();          	        
	          List<PLASegment> segs = miner.buildSpecificSegments(segLength[k]);          
	          PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	          plaRegionSearch.errorBound = errorBound;
	          
	          for(int i = segs.size() - 1; i >= 0; i--){
	          	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	          		segs.remove(i);
	          		System.out.println("Remove at " + i);
	          	}
	          }

	          Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
	          if(k == segLength.length - 1){
	        	  line1 += segLength[k];
	        	  line2 += plaRegionSearch.finalLength;
	          }else{
	        	  line1 += segLength[k] + "\t";
	        	  line2 += plaRegionSearch.finalLength + "\t";
	          }
	      }
	      pw.println(line1);
	      pw.println(line2);
	      pw.close();
	}
	
	public static void main(String[] args) throws IOException{
		String fileName = "D:\\data\\experiments\\Experiment_Tem_Record_1960-2012(length=100000).txt";
		double error = 0.05;		
		int length = 40000;		
		int[] segLength = {3, 5, 8, 10, 12, 15};
		
		RealLengthVsSegmentLengthExperiment expt = new RealLengthVsSegmentLengthExperiment();
		expt.PLARegionSearchWithListInsideTest(fileName, segLength, error, length);
		System.out.println("*************************************");
		expt.PLARegionSearchWithListTest(fileName, segLength, error, length);
		System.out.println("*************************************");
		expt.PLARegionSearchTest(fileName, segLength, error, length);
	}
}
