package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.SegmentUtils;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.rtree.Rectangle;


public class PLAExtendDeepSearch3 {
	private Point2D[] point2Ds;
	private RTree rTree;
	private double errorBound;
	private List<PLASegment> segmentList;
	public PLAExtendDeepSearch3(Point2D[] point2Ds, double errorBound){
		this.point2Ds = point2Ds;
		this.errorBound = errorBound;
	}

	public PLAExtendDeepSearch3(){}
	
	public double slope;
	public double intercept;
	public int maxLength;
	public Point2D maxKB = null;
	
	public int search(List<PLASegment> segmentList, int startLength, double error){
		this.segmentList = segmentList;
		maxLength = startLength;		
		int maxUpBound = initSegment();
		System.out.println("init MaxLength is " + maxLength);
    	List<PLASegment> stack = new ArrayList<PLASegment>();
    	PLASegment fakeSegment = new PLASegment();
        fakeSegment.idx = -1;
        fakeSegment.setStart(-10);
        fakeSegment.setEnd(-10);
        fakeSegment.setLength(0);
        fakeSegment.setDelete(false);
        fakeSegment.setPolygonKB(new SimplePolygon2D(TSPLAPointBoundKBMiner.X_INF, TSPLAPointBoundKBMiner.Y_INF));
        fakeSegment.totalLength = fakeSegment.getLength();
        fakeSegment.currentPolygon = fakeSegment.getPolygonKB();
        stack.add(fakeSegment);
        
        int c = 0;
        int startIdx = 0;
        while(stack.size() > 0){
        	double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                System.out.println("maxUpbound = " + maxUpBound);
                System.out.println("maxLength = " + maxLength);
                System.out.println("e = " + e);
                break;
            }
            
            PLASegment nextSegment = searchNext(stack, startIdx);

            if(nextSegment !=null){
            	c++;
                if(c % 1000000 == 0)
                	System.out.println("C = " + c + "!!!!!!!");
                stack.add(nextSegment);
                Point2D tempKB = nextSegment.currentPolygon.getCentroid();
                int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());
                if(lowerBound > maxLength){
                	maxKB = tempKB;
                	maxLength = lowerBound;
                	maxUpBound = adjustSegment(maxUpBound);

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
                
                int upperBound = calUpBound(nextSegment.currentPolygon);
                if(upperBound <= maxLength){

                	startIdx = segmentList.size();
                }else{
                	startIdx = nextSegment.idx + 1;
                }
            }else{

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
	
	private PLASegment searchNext(List<PLASegment> stack, int startIdx){
		PLASegment topSegment = stack.get(stack.size() - 1);
        Polygon2D currentPolygon = topSegment.currentPolygon;
        for (int i = startIdx; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if(segment.isDelete())
            	continue;
            Polygon2D intersection = Polygon2DUtils.intersection(currentPolygon, segment.getPolygonKB());
            if (intersection.getVertexNumber() > 0) {
            	segment.currentPolygon = intersection;
                return segment;
            }
        }

        return null;
	}


	public int initSegment(){

		int maxUpBound = -1;
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment tempSeg = segmentList.get(i);
			Point2D tempKB = tempSeg.getPolygonKB().getCentroid();
            int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());
            tempSeg.setLowerBound(lowerBound);
            if(lowerBound >= maxLength){
            	maxLength = lowerBound;
            	maxKB = tempKB;
            }
		}
		
		sortSegmentsBasedOnLowBound(segmentList);
		
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment tempSeg = segmentList.get(i);
			tempSeg.idx = i;
			tempSeg.setDelete(false);
		}
		
		buildTree();

		boolean needPrune = false;
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment tempSeg = segmentList.get(i);
			Polygon2D probePoly = tempSeg.getPolygonKB();
			int upBound = calUpBound(probePoly);
			tempSeg.setUpBound(upBound);
			if(upBound <= maxLength){
				needPrune = true;
			}
			if(upBound > maxUpBound)
				maxUpBound = upBound;
		}
		if(needPrune){
			maxUpBound = adjustSegment(maxUpBound);
		}
		
		return maxUpBound;
	}



	public int adjustSegment(int initUpBound){
		int loops = 0;
		int size = segmentList.size();
		int maxUpBound = initUpBound;
		boolean needPrune = true;
		while(needPrune){
			loops++;
			needPrune = false;
			for(int i = 0; i < segmentList.size(); i++){
				PLASegment tempSeg = segmentList.get(i);
				if((!tempSeg.isDelete()) && (tempSeg.getUpBound() <= maxLength)){
					tempSeg.setDelete(true);
					deleteSegment(tempSeg);
					size--;
					needPrune = true;
				}
			}

			if(needPrune){
				maxUpBound = -1;
				for(int i = 0; i < segmentList.size(); i++){
					PLASegment tempSeg = segmentList.get(i);
					Polygon2D probePoly = tempSeg.getPolygonKB();
					int tempUpBound = calUpBound(probePoly);
					tempSeg.setUpBound(tempUpBound);
					if(tempUpBound > maxUpBound)
						maxUpBound = tempUpBound;
				}
			}
		}
		return maxUpBound;
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
	

	public void sortSegments(List<PLASegment> segments){
		Collections.sort(segments, new Comparator<PLASegment>(){
			public int compare(PLASegment p1, PLASegment p2){
				return p1.getStart() - p2.getStart();
			}
		});
	}

	public void sortSegmentsBasedOnLowBound(List<PLASegment> segments){
		Collections.sort(segments, new Comparator<PLASegment>(){
			public int compare(PLASegment p1, PLASegment p2){
				return p2.getLowerBound() - p1.getLowerBound();
			}
		});
	}

	private void deleteSegment(PLASegment plaSegment){
		Polygon2D currentPoly = plaSegment.getPolygonKB();
		Box2D box = currentPoly.getBoundingBox();
		Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
		rTree.delete(probeRect, plaSegment.idx);
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
