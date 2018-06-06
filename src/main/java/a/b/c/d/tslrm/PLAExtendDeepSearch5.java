package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.SegmentUtils;
import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.rtree.Rectangle;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.*;


public class PLAExtendDeepSearch5 {
    private Point2D[] point2Ds;
    private RTree rTree;
    public double errorBound;
    private List<PLASegment> segmentList;
    private List<PLASegment> lowBoundSegmentList;
    private boolean[][] matrix;
    
    public PLAExtendDeepSearch5(Point2D[] point2Ds, double errorBound){
		this.point2Ds = point2Ds;
		this.errorBound = errorBound;
	}
    public PLAExtendDeepSearch5(){
    	
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


        buildTree();
        rTree.initMatrix2(matrix);
        

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
                int currentUpBound = 0;
                try{
                currentUpBound = calUpBound(nextSegment.currentPolygon);
                }catch (Exception ex){
                	Polygon2D curPoly = nextSegment.currentPolygon;
                	int vetexNum = curPoly.getVertexNumber();
                	System.out.println("vetex Num:" + vetexNum);
                	for(int k = 0; k < vetexNum; k++){
                		System.out.println("X=" + curPoly.getVertex(k).getX() + "   Y=" +curPoly.getVertex(k).getY());
                	}
                	System.out.println("Exception when" + stack.size());
                }
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
            	deleteSegment(tempSeg);
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


  	public int calUpBound(Polygon2D probePoly){
  		int upBound = 0;
  		List<PLASegment> segments = rTree.calUpBound(probePoly);
  		if(segments.size() <=0){
  			return upBound;
  		}
  		if(segments.size() == 1){
  			return segments.get(0).getLength();
  		}
  		sortSegments(segments);
  		PLASegment lastSeg = segments.get(0);
  		upBound = lastSeg.getLength();
  		for(int i = 1; i < segments.size(); i++){
  			PLASegment curSeg = segments.get(i);
  			if(curSeg.getStart() > lastSeg.getEnd()){
  				upBound += curSeg.getLength();
  			}else{
  				upBound += curSeg.getEnd() - lastSeg.getEnd();
  			}
  			lastSeg = curSeg;
  		}
  		return upBound;
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

 	private void deleteSegment(PLASegment plaSegment){
 		Polygon2D currentPoly = plaSegment.getPolygonKB();
 		Box2D box = currentPoly.getBoundingBox();
 		Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
 		rTree.delete(probeRect, plaSegment.index);
 	}
 		
 	private void buildTree(){
 			
 		Properties p = new Properties();
 		p.setProperty("MinNodeEntries", "16");
 		p.setProperty("MaxNodeEntries", "32");
 			
 	    rTree = new RTree(segmentList); 
 	    rTree.init(p);
 	    rTree.buildRTree();
 	}
}
