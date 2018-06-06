package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.Box2DUtils;
import a.b.c.d.tslrm.data.SegmentUtils;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.StopWatch;


public class RegionSearchBefore {
    private Point2D[] point2Ds;

    public RegionSearchBefore(Point2D[] point2Ds) {
        this.point2Ds = point2Ds;
    }
    
    public int finalLength = 0;
    public double slope;
    public double intercept;
    public int maxLength = 0;    

    public int period = 1000;
    public int count = 0;
    public double baseUpBound = 0;
    public double finalError = 0;
    public StringBuilder builder1 = new StringBuilder("");
    public StringBuilder builder2 = new StringBuilder("");
    public double errorBound;

    public int calcUpperBound(List<PLASegment> segmentList, PLASegment startExcludeSegment) {
        int ret = 0;

        if (segmentList.size() <= 0)
            return ret;

        PLASegment lastSegment = startExcludeSegment;


        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment currentSegment = segmentList.get(i);


            if (currentSegment.getStart() > lastSegment.getEnd()) {
                ret = ret + currentSegment.getLength();
                lastSegment = currentSegment;
            } else {


                if (currentSegment.getEnd() > lastSegment.getEnd()) {
                    ret = ret + currentSegment.getEnd() - lastSegment.getEnd();
                    lastSegment = currentSegment;
                } else {



                }
            }
        }

        return ret;
    }

    public Point2D searchByBox2D(List<PLASegment> segmentList, double error) {
        Point2D ret = null;
        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        item.upperBound = calcUpperBoundByBox2D(segmentList, item.box2D);
        ret = new Point2D(item.k, item.b);
        
        queue.add(item);

        maxLength = item.lowerBound;
        int maxUpBound = item.upperBound;
        boolean needPrune = false;
        while (queue.size() > 0) {
        	maxUpBound = queue.get(0).upperBound;
        	double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            QueueItem remove = queue.remove(0);
            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                k = childBox.getMinX() + childBox.getWidth() / 2;
                b = childBox.getMinY() + childBox.getHeight() / 2;

                childItem.k = k;
                childItem.b = b;
                childItem.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);

                if(childItem.lowerBound > maxLength){
                	maxLength = childItem.lowerBound;
                	ret = new Point2D(k, b);
                	needPrune = true;
                }

                if (childItem.upperBound > maxLength)
                    queue.add(childItem);
            }

            if(needPrune){
            	for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
            	needPrune = false;
            }

            Collections.sort(queue);
            
        }

        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2DWithAccuracy(List<PLASegment> segmentList, double error, int piod, double bUpBound){
    	period = piod;
    	Point2D ret = null;
        baseUpBound = bUpBound;
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
			public void run() {
				double e = (baseUpBound * 1.0 - maxLength) / baseUpBound;
				double accuracy = 1 - e;
				int tempTime = count * period / 1000;
				builder1.append(tempTime + "\t");
				builder2.append(accuracy + "\t");
				count ++;
			}
        	
        }, 0, period);
        
        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        item.upperBound = calcUpperBoundByBox2D(segmentList, item.box2D);
        
        queue.add(item);

        maxLength = item.lowerBound;
        int maxUpBound = item.upperBound;
        boolean needPrune = false;
        while (queue.size() > 0) {
        	maxUpBound = queue.get(0).upperBound;
        	double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
            	finalError = 1 - e;
                break;
            }

            QueueItem remove = queue.remove(0);
            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                k = childBox.getMinX() + childBox.getWidth() / 2;
                b = childBox.getMinY() + childBox.getHeight() / 2;

                childItem.k = k;
                childItem.b = b;
                childItem.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);

                if(childItem.lowerBound > maxLength){
                	maxLength = childItem.lowerBound;
                	ret = new Point2D(k, b);
                	needPrune = true;
                }

                if (childItem.upperBound > maxLength)
                    queue.add(childItem);
            }

            if(needPrune){
            	for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
            	needPrune = false;
            }

            Collections.sort(queue);           
        }
        timer.cancel();

        finalLength = maxLength;
        return ret;
    }
    
    public Point2D searchByBox2DPerLength(List<PLASegment> segmentList, double error, String fileName, int perLength) throws IOException{
    	PrintWriter pw = new PrintWriter(new FileWriter(new File(fileName)));
    	StringBuilder lengthLine = new StringBuilder("");
    	StringBuilder upBoundTimeLine = new StringBuilder("");
    	
    	Point2D ret = null;
        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        item.upperBound = calcUpperBoundByBox2D(segmentList, item.box2D);
        
        queue.add(item);
        
        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        
        int calcUpperBoundCount = 0;
        
        maxLength = item.lowerBound;
        int maxUpBound = item.upperBound;
        boolean needPrune = false;
        while (queue.size() > 0) {
        	maxUpBound = queue.get(0).upperBound;
        	double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            QueueItem remove = queue.remove(0);
            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                k = childBox.getMinX() + childBox.getWidth() / 2;
                b = childBox.getMinY() + childBox.getHeight() / 2;

                childItem.k = k;
                childItem.b = b;
                verifyWatch.resume();
                childItem.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                verifyWatch.suspend();
                
                calcUpperBoundWatch.resume();
                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount ++;

                if(childItem.lowerBound > maxLength){
                	maxLength = childItem.lowerBound;
                	ret = new Point2D(k, b);
                	needPrune = true;
                }

                if (childItem.upperBound > maxLength)
                    queue.add(childItem);
                
                if(calcUpperBoundCount % perLength == 0){                	
                	long tempUpTime = calcUpperBoundWatch.getTime() / 1000;
                	lengthLine.append(calcUpperBoundCount + "\t");   
                	upBoundTimeLine.append(tempUpTime + "\t");
                	verifyWatch.reset();  
                	verifyWatch.start();
                	verifyWatch.suspend();
                	calcUpperBoundWatch.reset();
                	calcUpperBoundWatch.start();
                	calcUpperBoundWatch.suspend();
                }
            }

            if(needPrune){
            	for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
            	needPrune = false;
            }

            Collections.sort(queue);
            
        }

        finalLength = maxLength;
        lengthLine.append(calcUpperBoundCount);
        upBoundTimeLine.append(calcUpperBoundWatch.getTime() / 1000);
        pw.println(lengthLine);
        pw.println(upBoundTimeLine);
        pw.close();
        return ret;
    }
    
    public int calcUpperBoundByBox2D(List<PLASegment> segmentList, Box2D box2D) {
        int ret = 0;

        List<PLASegment> list = new ArrayList<PLASegment>();
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            Polygon2D p = new SimplePolygon2D(new double[]{box2D.getMinX(),box2D.getMaxX(),box2D.getMaxX(),box2D.getMinX()},new double[]{box2D.getMinY(),box2D.getMinY(),box2D.getMaxY(),box2D.getMaxY()});
            Polygon2D intersection = Polygon2DUtils.intersection(p, segment.getPolygonKB());
            if (intersection.getVertexNumber() > 0) {
                list.add(segment);
            }
        }

        if (list.size() > 0) {
            ret = list.get(0).getLength() + calcUpperBound(list, list.get(0));
        }
        return ret;
    }
}
