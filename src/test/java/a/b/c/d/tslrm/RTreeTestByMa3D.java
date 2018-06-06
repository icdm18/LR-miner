package a.b.c.d.tslrm;

import java.io.File;
import java.util.List;

import org.junit.Test;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2DUtils;
import rtree.MemoryPageFile;
import rtree.RTree;
import a.b.c.d.tslrm.data.DataGenerator;

public class RTreeTestByMa3D {
	
	public void textMatrixWithRTree3D() throws Exception{
		String fileName = "data\\test_10000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();







        
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            segment.idx = i;
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        
        boolean[][] matrix1 = new boolean[segmentList.size()][segmentList.size()];
        boolean[][] matrix2 = new boolean[segmentList.size()][segmentList.size()];
        

        System.out.println("Cost time of init matrix using nomal method");
        long nomalInitTime = System.currentTimeMillis();
        for (int i = 0; i < segmentList.size(); i++) {
          PLASegment segment = segmentList.get(i);
          for (int j = i; j < segmentList.size(); j++) {
              PLASegment plaSegment = segmentList.get(j);

              if (Polygon2DUtils.intersection(segment.getPolygonKB(), plaSegment.getPolygonKB()).getVertexNumber() > 0) {
                  matrix1[i][j] = true;
                  matrix1[j][i] = true;
              	}
          	}
        }
        nomalInitTime = System.currentTimeMillis() - nomalInitTime;
        System.out.println(nomalInitTime + "ms");
        
        long buildTreeTime = System.currentTimeMillis();
        MemoryPageFile file = new MemoryPageFile();

        RTree rTree = new RTree(3, 0.5f, 16, file, RTree.RTREE_QUADRATIC, segmentList);
        rTree.buildRTree();
        buildTreeTime = System.currentTimeMillis() - buildTreeTime;       
        System.out.println("Tree build Time" + buildTreeTime + "ms");
        
        System.out.println("Cost time of init matrix using RTree 3D");
     	long RTreeInitTime = System.currentTimeMillis();
     	rTree.initMatrix(matrix2);
     	RTreeInitTime = System.currentTimeMillis() - RTreeInitTime;
     	System.out.println(RTreeInitTime + "ms");
        
     	int nonSame = 0;
     	for(int i =0; i < segmentList.size(); i++){
     		for(int j = 0; j < segmentList.size(); j++){
     			if(matrix1[i][j] != matrix2[i][j]){
     				nonSame++;
     			}
     		}
     	}
     	
     	System.out.println("nonSame :" + nonSame);
     	int[] ubs1 = new int[segmentList.size()];
     	int[] ubs2 = new int[segmentList.size()];
        
     	for (int i = 0; i < ubs1.length; i++) {
            ubs1[i] = 0;
            PLASegment lastSegment = null;
            for (int j = 0; j < ubs1.length; j++) {
                if (matrix1[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                        ubs1[i] = ubs1[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {



                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                            ubs1[i] = ubs1[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        } else {


                            if (currentSegment.getEnd() > lastSegment.getEnd()) {
                                ubs1[i] = ubs1[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            } else {



                            }
                        }
                    }
                }
            }
        }
     	
     	for (int i = 0; i < ubs2.length; i++) {
            ubs2[i] = 0;
            PLASegment lastSegment = null;
            for (int j = 0; j < ubs2.length; j++) {
                if (matrix2[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                        ubs2[i] = ubs2[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {



                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                            ubs2[i] = ubs2[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        } else {


                            if (currentSegment.getEnd() > lastSegment.getEnd()) {
                                ubs2[i] = ubs2[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            } else {



                            }
                        }
                    }
                }
            }
        }
     	int nonEqual = 0;
     	for(int i = 0; i < segmentList.size(); i++){

     		if(ubs1[i] != ubs2[i])
     			nonEqual++;
     	}
     	
     	System.out.println("nonEqual is" + nonEqual);
        
	}
	@Test
	public void testDeepSearch() throws Exception{
		String fileName = "data\\test_1000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();

        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() != 5){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        long timeTotal = System.currentTimeMillis();
        PLAExtendDeepSearch deepSearch = new PLAExtendDeepSearch();
        int maxLength = deepSearch.search(segmentList, 100, 0.01);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed without RTree is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	@Test
	public void testDeepSearchRTree3D() throws Exception{
		String fileName = "data\\test_1000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();

        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() != 5){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        
        long timeTotal = System.currentTimeMillis();
        PLAExtendDeepSearchWithRTree3D deepSearch = new PLAExtendDeepSearchWithRTree3D();
        
        int maxLength = deepSearch.search(segmentList, 100, 0.01);
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed with RTree3D is " + "   " + maxLength);
        
        System.out.println("Total Cost Time is " + timeTotal);
	}
}
