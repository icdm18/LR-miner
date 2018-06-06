package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;
import math.geom2d.polygon.MultiPolygon2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PLAExtendDeepSearch4 {
    private  Point2D[] point2Ds;
    public double errorBound;
    private List<PLASegment> segmentList;
    private List<PLASegment> lowBoundSegmentList;
    private boolean[][] matrix;
    
    public PLAExtendDeepSearch4(Point2D[] point2Ds, double errorBound){
		this.point2Ds = point2Ds;
		this.errorBound = errorBound;
	}
    public PLAExtendDeepSearch4(){
    	
    }

    public double slope;
    public double intercept;
    public int maxLength;
	public Point2D maxKB = null;
	
    public int search(List<PLASegment> segmentList, int startLength, double error) {
    	this.segmentList = segmentList;
    	lowBoundSegmentList = new ArrayList<PLASegment>();
        int maxUpbound = -1;
        maxLength = startLength;

        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            segment.index = i;
            segment.setDelete(false);

            Point2D tempKB = segment.getPolygonKB().getCentroid();
            int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());
            segment.setLowerBound(lowerBound);
            if(maxLength <= lowerBound){
            	maxLength = lowerBound;
            	maxKB = tempKB;
            }
            lowBoundSegmentList.add(segment);
            	
        }
        System.out.println("init MaxLength is " + maxLength);

        matrix = new boolean[segmentList.size()][segmentList.size()];


        for (int i = 0; i < segmentList.size(); i++) {

            PLASegment segment = segmentList.get(i);
            for (int j = i; j < segmentList.size(); j++) {
                PLASegment plaSegment = segmentList.get(j);
                if (Polygon2DUtils.intersection(segment.getPolygonKB(), plaSegment.getPolygonKB()).getVertexNumber() > 0) {
                    matrix[i][j] = true;
                    matrix[j][i] = true;
                }
            }
        }
        

        maxUpbound = calcUbs();

        boolean b = adjustMatrix();
        while (b) {
            maxUpbound = calcUbs();
            b = adjustMatrix();
        }

        sortSegmentsBasedOnUpBound(lowBoundSegmentList);
        for(int i = 0; i < lowBoundSegmentList.size(); i++){
        	lowBoundSegmentList.get(i).idx = i;
        }
        List<PLASegment> stack = new ArrayList<PLASegment>();
        long c = 0;
        System.out.println("Max UpBound = " + maxUpbound + "!!!!!!!!!!!!!!");
        PLASegment fakeSegment = new PLASegment();
        fakeSegment.idx = -1;
        fakeSegment.setStart(-10);
        fakeSegment.setEnd(-10);
        fakeSegment.setLength(0);
        fakeSegment.setPolygonKB(new SimplePolygon2D(TSPLAPointBoundKBMiner.X_INF, TSPLAPointBoundKBMiner.Y_INF));
        fakeSegment.totalLength = fakeSegment.getLength();
        fakeSegment.currentPolygon = fakeSegment.getPolygonKB();
        
        stack.add(fakeSegment);
        int startIdx = 0;
        
        while (stack.size() > 0) {
            double e = (maxUpbound * 1.0 - maxLength) / maxUpbound;
            if (e <= error) {
                System.out.println("maxUpbound = " + maxUpbound);
                System.out.println("maxLength = " + maxLength);
                System.out.println("e = " + e);
                break;
            }

            PLASegment nextSegment = searchNext(stack, startIdx);

            if (nextSegment != null) {
            	c++;
                if(c % 1000000 == 0)
                	System.out.println("C = " + c + "!!!!!!!");
                stack.add(nextSegment);
                
                Point2D tempKB = nextSegment.currentPolygon.getCentroid();
                int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());               
                if(lowerBound > maxLength){
                	maxKB = tempKB;
                	maxLength = lowerBound;
                	boolean needprune = adjustMatrix();
                    while (needprune) {
                        maxUpbound = calcUbs();
                        needprune = adjustMatrix();
                    }



                	int deleteIndex = -1;
                	for(int i = 1; i < stack.size(); i++){
                		if(stack.get(i).isDelete()){
                			deleteIndex = i;
                			break;
                		}
                	}
                	if((deleteIndex > -1) && (deleteIndex < stack.size())){
                		startIdx = stack.get(deleteIndex).idx + 1;
                		for(int i = stack.size() - 1; i >= deleteIndex; i--){
                			stack.remove(i);
                		}
                		continue;
                	}
                }
                int currentUpBound = checkUpperBound(nextSegment.currentPolygon, startIdx, stack);
                if(currentUpBound <= maxLength){

                	startIdx = segmentList.size();
                }else{
                	startIdx = nextSegment.idx + 1;
                }
                
            } else {

            	PLASegment removed = stack.remove(stack.size() - 1);
            	if(stack.size() == 0){
            		System.out.println("Statck Size is 0!!!!!!!!!!!!!!!");
                    break;
            	}else{
            		startIdx = removed.idx + 1;
            	}
            }
        }
        	System.out.println("maxLength = " + maxLength);
        	System.out.println("C = " + c + "!!!!!!!");
            
            if(maxKB != null){
            	System.out.println("k = " + maxKB.getX());
                System.out.println("b = " + maxKB.getY());
                int realLength = SegmentUtils.verifyTrueLength(point2Ds, maxKB.getX(), maxKB.getY(), errorBound, segmentList.get(0).getLength());
                System.out.println("RealLength with lowBound is " + realLength);
                this.slope = maxKB.getX();
                this.intercept = maxKB.getY();
            }
        return maxLength;
    }
    
    private boolean adjustMatrix() {
    	boolean needProne = false;
        for (int i = 0; i < segmentList.size(); i++) {
        	PLASegment tempSeg = segmentList.get(i);
        	if(tempSeg.isDelete())
        		continue;
            if (tempSeg.getUpBound() <= maxLength) {
            	needProne = true;
            	tempSeg.setUpBound(0);
            	tempSeg.setDelete(true);
            	for (int j = 0; j < segmentList.size(); j++) {
                    matrix[j][i] = false;
                }
            }
        }
        return needProne;
    }

    private int calcUbs() {

        PLASegment lastSegment = null;
        int maxUpBound = -1;
        for (int i = 0; i < segmentList.size(); i++) {
        	PLASegment curSeg = segmentList.get(i);
            if(curSeg.isDelete())
            	continue;
            int tempUpBound = 0;
            lastSegment = null;
            for (int j = 0; j < segmentList.size(); j++) {
                if (matrix[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                    	tempUpBound += currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {

                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                        	tempUpBound += currentSegment.getLength();
                        } else {
                        	tempUpBound += currentSegment.getEnd() - lastSegment.getEnd();
                        }
                        lastSegment = currentSegment;
                    }
                }
            }
            curSeg.setUpBound(tempUpBound);
            if(tempUpBound > maxUpBound)
            	maxUpBound = tempUpBound;
        }
        
        return maxUpBound;
    }

    private PLASegment searchNext(List<PLASegment> stack, int startIdx) {
        PLASegment topSegment = stack.get(stack.size() - 1);

        Polygon2D currentPolygon = topSegment.currentPolygon;
        for (int i = startIdx; i < lowBoundSegmentList.size(); i++) {
            PLASegment segment = lowBoundSegmentList.get(i);
            if (segment.isDelete())
                continue;
            if (topSegment.idx >= 0)
                if (!matrix[topSegment.index][segment.index])
                    continue;

            Polygon2D intersection = Polygon2DUtils.intersection(currentPolygon, segment.getPolygonKB());
            if (intersection.getVertexNumber() > 0) {
                segment.currentPolygon = intersection;
                return segment;
            }
        }

        return null;
    }

    private int checkUpperBound(Polygon2D currentPolygon, int currentIdx, List<PLASegment> stack) {
        List<PLASegment> list = new ArrayList<PLASegment>();
        PLASegment curSeg = lowBoundSegmentList.get(currentIdx);
        int startIdx = currentIdx + 1;
        for (int i = startIdx; i < lowBoundSegmentList.size(); i++) {       	
            PLASegment segment = lowBoundSegmentList.get(i);
            if(segment.isDelete())
            	continue;
            if(!matrix[curSeg.index][segment.index])
            	continue;
            try{
            if (Polygon2DUtils.intersection(currentPolygon, segment.getPolygonKB()).getVertexNumber() > 0) {
                list.add(segment);
            }
            }catch(Exception ex){
            	Polygon2D curPoly = currentPolygon;
            	int vetexNum = curPoly.getVertexNumber();
            	if(curPoly instanceof MultiPolygon2D){
            		System.out.println("curPoly is instanceof MultiPoly2D");
            	}
            	if(curPoly instanceof SimplePolygon2D){
            		System.out.println("curPoly is instanceof SimplePolygon2D");
            	}
            	System.out.println("vetex Num:" + vetexNum);
            	for(int k = 0; k < vetexNum; k++){
            		System.out.println("X=" + curPoly.getVertex(k).getX() + "   Y=" +curPoly.getVertex(k).getY());
            	}
            	System.out.println("******************");
            	vetexNum = segment.getPolygonKB().getVertexNumber();
            	Polygon2D segPoly = segment.getPolygonKB();
            	if(segPoly instanceof MultiPolygon2D){
            		System.out.println("segPoly is instanceof MultiPoly2D");
            	}
            	if(segPoly instanceof SimplePolygon2D){
            		System.out.println("segPoly is instanceof SimplePolygon2D");
            	}
            	System.out.println("vetex Num2:" + vetexNum);
            	for(int k = 0; k < vetexNum; k++){
            		System.out.println("X=" + segPoly.getVertex(k).getX() + "   Y=" +segPoly.getVertex(k).getY());
            	}
            	System.out.println("Exception when" + stack.size());
            }
        }

        for(int i = 1; i < stack.size(); i++)
        	list.add(stack.get(i));
        return calcUpperBound(list);
    }

    public int calcUpperBound(List<PLASegment> list) {
        int ret = 0;

        if (list.size() <= 0)
            return ret;
        sortSegments(list);
        PLASegment lastSegment = null;;


        for (int i = 0; i < list.size(); i++) {
            PLASegment currentSegment = list.get(i);


            if (lastSegment == null) {
            	ret += currentSegment.getLength();
                lastSegment = currentSegment;
            } else {

                if (currentSegment.getStart() > lastSegment.getEnd()) {
                	ret += currentSegment.getLength();
                } else {
                	ret += currentSegment.getEnd() - lastSegment.getEnd();
                }
                lastSegment = currentSegment;
            }
        }

        return ret;
    }
    

    public void sortSegmentsBasedOnLowBound(List<PLASegment> segmentList){

        Collections.sort(segmentList, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o2.getLowerBound() - o1.getLowerBound();
            }
        });
    }
    

    public void sortSegmentsBasedOnUpBound(List<PLASegment> segmentList){

        Collections.sort(segmentList, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o2.getUpBound() - o1.getUpBound();
            }
        });
    }
    

 	public void sortSegments(List<PLASegment> segments){
 		Collections.sort(segments, new Comparator<PLASegment>(){
 			public int compare(PLASegment p1, PLASegment p2){
 				return p1.getStart() - p2.getStart();
 			}
 		});
 	}
}
