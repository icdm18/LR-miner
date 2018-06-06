package a.b.c.d.tslrm;

import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.*;

import com.infomatiq.jsi.rtree.Node;
import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.rtree.Rectangle;


public class TSPLAPointBoundKBMiner {
    public TSPLAPointBoundKBMiner(Point2D[] point2Ds, double errorBound) {
        this.points = point2Ds;
        this.pointErrorBound = errorBound;
    }

    public static void main(String[] args) {
        double x[] = {1, 2, 3, 6, 14, 1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6, 1, 2, 3, 4, 1, 2, 3, 4,};
        double y[] = {4, 3, 8, 13, 9, 1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
        System.out.println("x.length = " + x.length);
        System.out.println("y.length = " + y.length);

        double errorBound = 1;
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(x, y, errorBound);


        List<PLASegment> allSegments = miner.buildAllSegments();
        System.out.println("allSegments.size() = " + allSegments.size());



        for (int i = 0; i < allSegments.size(); i++) {
            PLASegment segment = allSegments.get(i);
            System.out.println("segment = " + segment.toStringKB());
        }




        findMaxIntersection(allSegments);
    }


    public List<PLASegment> buildSpecificSegments(int length){
    	 List<PLASegment> ret = new ArrayList<PLASegment>();
    	 for(int i = 0; i < points.length; i++){
    		 Polygon2D p = polygonKBOfPoint(points[i]);
    		 Point2D lastPoint = points[i];
    		 for(int j = i + 1; j < points.length; j++){
    			 if(lastPoint.equals(points[j])){
    				 
    			 }else{
    				 Polygon2D p1 = polygonKBOfPoint(points[j]);
    				 lastPoint = points[j];
    				 p = Polygon2DUtils.intersection(p, p1);
    			 }


    			 if(p.getVertexNumber() > 0){
    				 PLASegment plaSegment = new PLASegment();
    				 plaSegment.setStart(i);
    				 plaSegment.setEnd(j);
                     plaSegment.setStartX(points[i].getX());
                     plaSegment.setStartY(points[i].getY());
                     plaSegment.setPolygonKB(p);
                     if (plaSegment.getLength() == length){
                    	 ret.add(plaSegment);
                    	 break;
                     }                	 
    			 }else{
    				 break;
    			 }
    		 }
    	 }
    	 return ret;
    }

    public List<PLASegment> buildAllSegments() {
        List<PLASegment> ret = new ArrayList<PLASegment>();
        for (int i = 0; i < points.length - 1; i++) {
            Polygon2D p = polygonKBOfPoint(points[i]);
            Point2D lastPoint = points[i];

            for (int j = i + 1; j < points.length; j++) {

                if(lastPoint.equals(points[j])){
                	
                }else{
                	Polygon2D p1 = polygonKBOfPoint(points[j]);
                	lastPoint = points[j];
                	p = Polygon2DUtils.intersection(p, p1);
                	              	
                }
                if (p.getVertexNumber() > 0)
                {
            		 PLASegment plaSegment = new PLASegment();
                     plaSegment.setStart(i);
                     plaSegment.setEnd(j);
                     plaSegment.setStartX(points[i].getX());
                     plaSegment.setStartY(points[i].getY());
                     plaSegment.setPolygonKB(p);
                     if (plaSegment.getLength() >= 3) ret.add(plaSegment);
                } else
                    break; 
            }
        }
        return ret;
    }


    public static void sortSegmentList(List<PLASegment> segments) {

        Collections.sort(segments, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o1.getLength() - o2.getLength();
            }
        });
    }

    public static void sortSegmentListByStartEnd(List<PLASegment> segments) {

        Collections.sort(segments, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                if (o1.getStart() != o2.getStart())
                    return o1.getStart() - o2.getStart();
                else
                    return o1.getEnd() - o2.getEnd();
            }
        });
    }

    public static void findMaxIntersection(List<PLASegment> segments) {
        List<PLASegment> list = new ArrayList<PLASegment>();

        for (PLASegment next : segments) {
            next.children.add(next);

            if (next.getLength() > 2) {
                list.add(next);
            }
        }


        sortSegmentList(list);







        while (list.size() > 1) {
            PLASegment remove = list.remove(0);
            System.out.println("remove.toString() = " + remove.toStringKB());
            Polygon2D removePolygon = remove.getPolygonKB();

            for (int i = 0; i < list.size(); i++) {
                PLASegment seg = list.get(i);

                Polygon2D segPolygonKB = seg.getPolygonKB();
                Polygon2D intersection = null;
                try {
                    intersection = Polygon2DUtils.intersection(removePolygon, segPolygonKB);
                    if (intersection.getRings().size() > 1) {
                        System.out.println(" = ");





                        for (Iterator<? extends LinearRing2D> iterator = intersection.getRings().iterator(); iterator.hasNext(); ) {
                            LinearRing2D next = iterator.next();
                            System.out.println("next.getVertexNumber() = " + next.getVertexNumber());
                            if (next.getVertexNumber() > 3) {
                                intersection = new SimplePolygon2D(intersection.getVertices());
                                break;
                            }
                        }

                    }
                } catch (RuntimeException e) {
                    System.out.println("segPolygonKB = " + segPolygonKB);
                    System.out.println("removePolygon = " + removePolygon);
                    throw e;
                }
                if (intersection.getVertexNumber() > 0)
                {
                    PLASegment newSeg = new PLASegment();

                    newSeg.setPolygonKB(intersection);
                    newSeg.children.addAll(seg.children);
                    newSeg.setLength(seg.getLength());


                    for (PLASegment child : remove.children) {
                        if (!newSeg.children.contains(child)) {
                            newSeg.setLength(newSeg.getLength() + child.getLength());
                            newSeg.children.add(child);
                        }
                    }
                    list.set(i, newSeg);
                }
            }

            sortSegmentList(list);




        }

        PLASegment plaSegment = list.get(0);
        _max_Length = plaSegment.getLength();
        System.out.println("plaSegment = " + plaSegment.toStringKB());
        List<PLASegment> plaSegments = new ArrayList<PLASegment>(plaSegment.children);
        sortSegmentListByStartEnd(plaSegments);

        plaSegments = mergeSegmentListByStartEnd(plaSegments);
        for (PLASegment child : plaSegments) {
            System.out.println("child = " + child.toStringKB());
        }
    }

    private static List<PLASegment> mergeSegmentListByStartEnd(List<PLASegment> plaSegments) {
        List<PLASegment> ret = new ArrayList<PLASegment>();

        PLASegment segment = null;
        for (int i = 0; i < plaSegments.size(); i++) {
            PLASegment nextSegment = plaSegments.get(i);
            if (segment == null) {
                segment = new PLASegment();
                segment.setStart( nextSegment.getStart());
                segment.setEnd(nextSegment.getEnd());
                segment.setPolygonKB(nextSegment.getPolygonKB());
                segment.setLength(nextSegment.getLength());
            } else {
                if (nextSegment.getStart() == (segment.getEnd() + 1)) {
                    segment.setEnd(nextSegment.getEnd());
                    segment.setLength(segment.getLength() + nextSegment.getLength());
                    segment.setPolygonKB(Polygon2DUtils.intersection(segment.getPolygonKB(), nextSegment.getPolygonKB()));
                } else {
                    ret.add(segment);
                    segment = new PLASegment();
                    segment.setStart( nextSegment.getStart());
                    segment.setEnd(nextSegment.getEnd());
                    segment.setPolygonKB(nextSegment.getPolygonKB());
                    segment.setLength(nextSegment.getLength());

                }
            }
        }

        ret.add(segment);

        return ret;
    }

    Point2D[] points;

    List<PLASegment> plaSegmentList;

    public void process() {

        plaSegmentList = new ArrayList<PLASegment>();


        int i = 0;
        int j = i + 1;
        PLASegment plaSegment = new PLASegment();
        plaSegment.setStart(i);
        plaSegment.setEnd(i);
        plaSegment.setStartX(points[i].getX());
        plaSegment.setStartY(points[i].getY());
        plaSegment.setPolygonKB(polygonKBOfPoint(points[i]));



        while (j < points.length) {
            Polygon2D p1 = polygonKBOfPoint(points[j]);
            Polygon2D p = Polygon2DUtils.intersection(plaSegment.getPolygonKB(), p1);

            if (p.getVertexNumber() <= 0)
            {
                plaSegmentList.add(plaSegment);

                i = j;
                j = j + 1;
                plaSegment = new PLASegment();
                plaSegment.setStart(i);
                plaSegment.setEnd(i);
                plaSegment.setStartX(points[i].getX());
                plaSegment.setStartY(points[i].getY());
                plaSegment.setPolygonKB(polygonKBOfPoint(points[i]));
            } else {
                plaSegment.setEnd(j);
                plaSegment.setPolygonKB(p);



                j = j + 1;
            }
        }

        plaSegmentList.add(plaSegment);
    }

    private Polygon2D polygonKBOfPoint(Point2D point) {
        double x = point.getX();
        double y = point.getY();
        double k[] = X_INF;
        double b[] = {(y - pointErrorBound - D_MIN * x), (y - pointErrorBound - D_MAX * x), (y + pointErrorBound - D_MAX * x), (y + pointErrorBound - D_MIN * x)};
        return new SimplePolygon2D(k, b);
    }

    public static final double D_MAX = 1000000;

    public static final double D_MIN = -D_MAX;


    public static final double[] X_INF = {D_MIN, D_MAX, D_MAX, D_MIN};
    public static final double[] Y_INF = {D_MIN, D_MIN, D_MAX, D_MAX};



    double[] x;
    double[] y;

    double pointErrorBound;

    public TSPLAPointBoundKBMiner(double[] x, double[] y, double pointErrorBound) {
        this.x = x;
        this.y = y;
        this.pointErrorBound = pointErrorBound;

        points = new Point2D[x.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point2D(x[i], y[i]);
        }
    }


    public static boolean hasIntersection(double lower1, double upper1, double lower2, double upper2) {
        double lower = Math.max(lower1, lower2);
        double upper = Math.min(upper1, upper2);
        return upper >= lower;
    }

    public static void findMaxIntersectionWithApriori(List<PLASegment> plaSegmentList) {
        PLASegment currentMaxSegment = null;
        int currentMaxLevel = -1;



        int currentLevel = 0;
        List<PLASegment> currentLevelList = new ArrayList<PLASegment>();
        PLASegment startSegment = new PLASegment();
        startSegment.setLength(0);
        startSegment.setPolygonKB(new SimplePolygon2D(X_INF, Y_INF));
        currentLevelList.add(startSegment);
        currentMaxSegment = startSegment;

        int nextLevel;
        while (true) {
            System.gc();
            System.out.println("==============");
            System.out.println("currentLevel = " + currentLevel);
            System.out.println("currentLevelList.size() = " + currentLevelList.size());
            System.out.println("currentMaxLevel = " + currentMaxLevel);
            System.out.println("currentMaxSegment.toStringKB() = " + currentMaxSegment.toStringKB());
            PLASegment[] children = currentMaxSegment.children.toArray(new PLASegment[0]);
            for (int i = 0; i < children.length; i++) {
                PLASegment child = children[i];
                System.out.println("child.toStringKB() = " + child.toStringKB());
            }

            nextLevel = currentLevel + 1;
            List<PLASegment> nextLevelList = new ArrayList<PLASegment>();

            for (int i = 0; i < currentLevelList.size(); i++) {
                PLASegment segment = currentLevelList.get(i);
                PLASegment[] childSegments = segment.children.toArray(new PLASegment[segment.children.size()]);

                Polygon2D p1 = segment.getPolygonKB();

                for (int j = 0; j < plaSegmentList.size(); j++) {
                    PLASegment extendSegment = plaSegmentList.get(j);


                    boolean checkPosition = true;
                    for (int k = 0; k < childSegments.length; k++) {
                        PLASegment childSegment = childSegments[k];

                        boolean  b = extendSegment.getStart() <= childSegment.getEnd();
                        if (b) {
                            checkPosition = false;
                            break;
                        }
                    }


                    if (!checkPosition)
                        continue;



                    Polygon2D p2 = extendSegment.getPolygonKB();

                    Polygon2D intersection = Polygon2DUtils.intersection(p1, p2);

                    if (intersection.getVertexNumber() > 0) {
                        PLASegment newSegment = new PLASegment();
                        newSegment.setLength(segment.getLength() + extendSegment.getLength());
                        newSegment.children.addAll(segment.children);
                        newSegment.children.add(extendSegment);
                        newSegment.setPolygonKB(intersection);
                        nextLevelList.add(newSegment);
                    }
                }
            }


            PLASegment maxSegment = null;

            for (int i = 0; i < nextLevelList.size(); i++) {
                PLASegment segment = nextLevelList.get(i);

                if ((maxSegment == null) || (maxSegment.getLength() < segment.getLength())) {
                    maxSegment = segment;
                }
            }

            if ((maxSegment != null) && ((currentMaxSegment == null) || (maxSegment.getLength() > currentMaxSegment.getLength()))) {
                currentMaxLevel = nextLevel;
                currentMaxSegment = maxSegment;
            }

            if (nextLevelList.size() > 0) {
                currentLevel = nextLevel;
                currentLevelList = nextLevelList;
            } else
                break;
        }
        System.out.println("currentMaxLevel = " + currentMaxLevel);
        System.out.println("currentMaxSegment = " + currentMaxSegment.toStringKB());
    }

    public static void findMaxIntersectionApproximate(List<PLASegment> segmentList) {
        List<PLASegment> reducedList = new ArrayList<PLASegment>(0);




        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if (i < segmentList.size() - 1) {
                segment.nextSegment = segmentList.get(i + 1);
            }

            for (int j = i + 1; j < segmentList.size(); j++) {
                PLASegment segment1 = segmentList.get(j);
                if (segment1.getStart() > segment.getEnd()) {
                    segment.nextDisjointSegment = segment1;
                    break;
                }
            }

            for (int j = i + 1; j < segmentList.size(); j++) {
                PLASegment segment1 = segmentList.get(j);
                if (segment1.getStart() > segment.getStart()) {
                    segment.nextStartSegment = segment1;
                    break;
                }
            }




        }

        PLASegment segment = segmentList.get(0);

        while (segment != null) {
            reducedList.add(segment);
            segment = segment.nextDisjointSegment;
        }

        System.out.println("reducedList.size() = " + reducedList.size());
        findMaxIntersection(reducedList);
    }

    public static int _max_Length = 0;

    public static void findMaxIntersectionWithStack(List<PLASegment> segmentList) {

        List<PLASegment> stack = new ArrayList<PLASegment>();



        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if (i < segmentList.size() - 1) {
                segment.nextSegment = segmentList.get(i + 1);
            }

            for (int j = i + 1; j < segmentList.size(); j++) {
                PLASegment segment1 = segmentList.get(j);
                if (segment1.getStart() > segment.getEnd()) {
                    segment.nextDisjointSegment = segment1;
                    break;
                }
            }

            for (int j = i + 1; j < segmentList.size(); j++) {
                PLASegment segment1 = segmentList.get(j);
                if (segment1.getStart() > segment.getStart()) {
                    segment.nextStartSegment = segment1;
                    break;
                }
            }




        }

        int[] startPositions = new int[segmentList.size()];
        Arrays.fill(startPositions, -1);

        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment seg = segmentList.get(i);
            int start = seg.getStart();
            if (startPositions[start] == -1)
                startPositions[start] = i;
        }


        int intersectionCount = 0;

        long maxLength = 0;
        PLASegment first = new PLASegment();
        first.setStart(-1);
        first.setEnd(-1);
        first.setLength(0);
        first.totalLength = 0;
        first.nextSegment = segmentList.get(0);
        stack.add(first);
        int currentLength = first.getLength();
        while (stack.size() > 0) {
            PLASegment popSegment = stack.remove(stack.size() - 1);
            PLASegment nextSegment = popSegment.nextSegment;
            while (nextSegment != null) {
                if ((maxLength - currentLength) >= (segmentList.get(segmentList.size() - 1).getEnd() - nextSegment.getStart() + 1)) {
                    break;
                }

                if (stack.size() == 0)
                {
                    nextSegment.currentPolygon = nextSegment.getPolygonKB();
                    nextSegment.totalLength = nextSegment.getLength();
                    stack.add(nextSegment);


                    currentLength = nextSegment.totalLength;
                    if (currentLength > maxLength) {
                        System.out.println("============================================");
                        System.out.println("currentLength = " + currentLength);
                        maxLength = currentLength;
                        for (int i = 0; i < stack.size(); i++) {
                            System.out.println("stack.get(i) = " + stack.get(i));
                        }
                    }

                    nextSegment = nextSegment.nextDisjointSegment;
                } else {

                    PLASegment topSegment = stack.get(stack.size() - 1);
                    Polygon2D topPolygon = topSegment.currentPolygon;
                    Polygon2D nextPolygon = nextSegment.getPolygonKB();
                    Polygon2D intersection = Polygon2DUtils.intersection(topPolygon, nextPolygon);

                    intersectionCount++;
                    if (intersectionCount % 1000000 == 0) {
                        System.out.println("intersectionCount = " + intersectionCount);
                    }


                    if (intersection.getVertexNumber() > 0) {
                        nextSegment.currentPolygon = intersection;
                        nextSegment.totalLength = topSegment.totalLength + nextSegment.getLength();
                        stack.add(nextSegment);

                        currentLength = nextSegment.totalLength;
                        if (currentLength > maxLength) {
                            System.out.println("============================================");
                            System.out.println("currentLength = " + currentLength);
                            maxLength = currentLength;
                            for (int i = 0; i < stack.size(); i++) {
                                System.out.println("stack.get(i) = " + stack.get(i));
                            }





                        }

                        nextSegment = nextSegment.nextDisjointSegment;
                    } else {
                        nextSegment = nextSegment.nextStartSegment;
                    }
                }
            }
        }












































    }

    public static void findMaxIntersectionWithStackWithMatrix(List<PLASegment> segmentList) {
        boolean[][] matrix = new boolean[segmentList.size()][segmentList.size()];
        int[] ubs = new int[segmentList.size()];



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


        PLASegment lastSegment;
        for (int i = 0; i < ubs.length; i++) {
            ubs[i] = 0;
            lastSegment = null;
            for (int j = 0; j < ubs.length; j++)
            {
                if (matrix[i][j])
                {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null)
                    {
                        ubs[i] = ubs[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    }
                    else
                    {



                        if (currentSegment.getStart() > lastSegment.getEnd())
                        {
                            ubs[i] = ubs[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        }
                        else
                        {


                            if (currentSegment.getEnd() > lastSegment.getEnd())
                            {
                                ubs[i] = ubs[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            }
                            else
                            {



                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < ubs.length; i++) {
            System.out.println("ub[" + i +"] = " + ubs[i]);
        }


        int maxUb = 0;
        int maxIdx = -1;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] > maxUb)
            {
                maxUb = ubs[i];
                maxIdx = i;
            }
        }

        findMaxIntersectionApproximate(segmentList);

        System.out.println("_max_Length = " + _max_Length);

        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] <= _max_Length)
            {
                for (int j = 0; j < ubs.length; j++) {
                    matrix[i][j] = false;
                    matrix[j][i] = false;
                }

                ubs[i] = 0;
            }
        }

        System.out.println("after prone");
        for (int i = 0; i < ubs.length; i++) {
            System.out.println("ub[" + i +"] = " + ubs[i]);
        }


        for (int i = 0; i < ubs.length; i++) {
            ubs[i] = 0;
            lastSegment = null;
            for (int j = 0; j < ubs.length; j++)
            {
                if (matrix[i][j])
                {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null)
                    {
                        ubs[i] = ubs[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    }
                    else
                    {



                        if (currentSegment.getStart() > lastSegment.getEnd())
                        {
                            ubs[i] = ubs[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        }
                        else
                        {


                            if (currentSegment.getEnd() > lastSegment.getEnd())
                            {
                                ubs[i] = ubs[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            }
                            else
                            {



                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < ubs.length; i++) {
            System.out.println("ub[" + i +"] = " + ubs[i]);
        }





        List<PLASegment> newList = new ArrayList<PLASegment>();

        for (int i = 0; i < ubs.length; i++) {
           if (matrix[maxIdx][i])
           {
               newList.add(segmentList.get(i));
           }
        }

        if (newList.size() < segmentList.size())
        {
            System.out.println("--------------------recursive --------------------------");
            findMaxIntersectionWithStackWithMatrix(newList);
        }

    }
    
    public static void initMatrixWithRTree(List<PLASegment> segmentList){
    	int length = segmentList.size();
    	boolean[][] matrix = new boolean[length][length];
    	int[] ups = new int[length];
    	PLASegment[] indexSegs = new PLASegment[length];

    	for(int i = 0; i < length; i++){
    		indexSegs[i] = segmentList.get(i);
    	}
    	System.out.println("Before prune:" + length);
    	long buildTreeTime = System.currentTimeMillis();
    	RTree rTree = builtTree(indexSegs);
    	buildTreeTime = System.currentTimeMillis() - buildTreeTime;
    	System.out.println("Tree build Time: " + buildTreeTime);
    	

    	for(int i = 0; i < length; i++){
    		matrix[i][i] = true;
    		PLASegment curSeg = segmentList.get(i);
    		Polygon2D curPoly = curSeg.getPolygonKB();
    		Rectangle probeRect = getRectangle(curSeg);
    		List possibles = rTree.intersects(probeRect);
    		for(int j = 0; j < possibles.size(); j++){
    			int tempIndex = (Integer) possibles.get(j);
    			if(tempIndex > i){
    				PLASegment tempSeg = indexSegs[tempIndex];
    				Polygon2D tempPoly = tempSeg.getPolygonKB();
    				if(Polygon2DUtils.intersection(curPoly, tempPoly).getVertexNumber() > 0){
						matrix[i][j] = true;
						matrix[j][i] = true;
					}
    			}
    		}
    	}   
    	
    }
    
    public static void initMatrix(List<PLASegment> segmentList){
    	 boolean[][] matrix = new boolean[segmentList.size()][segmentList.size()];
         int[] ubs = new int[segmentList.size()];



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
    }
    
    public static void vertifyMatrix(List<PLASegment> segmentList){
    	 boolean[][] matrix1 = new boolean[segmentList.size()][segmentList.size()];



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
         

        int length = segmentList.size();
     	boolean[][] matrix2 = new boolean[length][length];
     	int[] ups = new int[length];
     	PLASegment[] indexSegs = new PLASegment[length];

     	for(int i = 0; i < length; i++){
     		indexSegs[i] = segmentList.get(i);
     	}
     	System.out.println("Before prune:" + length);
     	long buildTreeTime = System.currentTimeMillis();
     	RTree rTree = builtTree(indexSegs);
     	buildTreeTime = System.currentTimeMillis() - buildTreeTime;
     	System.out.println("Tree build Time: " + buildTreeTime);

     	for(int i = 0; i < length; i++){
     		matrix2[i][i] = true;
     		PLASegment curSeg = segmentList.get(i);
     		Polygon2D curPoly = curSeg.getPolygonKB();
     		Rectangle probeRect = getRectangle(curSeg);
     		List possibles = rTree.intersects(probeRect);
     		for(int j = 0; j < possibles.size(); j++){
     			int tempIndex = (Integer) possibles.get(j);
     			if(tempIndex > i){
     				PLASegment tempSeg = indexSegs[tempIndex];
     				Polygon2D tempPoly = tempSeg.getPolygonKB();
     				if(Polygon2DUtils.intersection(curPoly, tempPoly).getVertexNumber() > 0){
     					matrix2[i][j] = true;
     					matrix2[j][i] = true;
 					}
     			}
     		}
     	}
     	int nonSame = 0;
     	for(int i =0; i < length; i++){
     		for(int j = 0; j < length; j++){
     			if(matrix1[i][j] != matrix2[i][j]){
     				nonSame++;
     			}
     		}
     	}
     	System.out.println("nonSame :" + nonSame);
    }
    
    public static void findMaxIntersectionWithAprioriWithRTree(List<PLASegment> segmentList){
    	int length = segmentList.size();
    	PLASegment[] indexSegs = new PLASegment[length];
    	
    	for(int i = 0; i < length; i++){
    		indexSegs[i] = segmentList.get(i);
    	}
    	System.out.println("Before prune:" + length);
    	long buildTreeTime = System.currentTimeMillis();
    	RTree rTree = builtTree(indexSegs);
    	buildTreeTime = System.currentTimeMillis() - buildTreeTime;
    	System.out.println("Tree build Time: " + buildTreeTime);
    	System.out.println("Tree size :" + rTree.size());
    	List<PLASegment> list = new ArrayList<PLASegment>();
    	List<PLASegment> tempList = null;
    	int kNum = 0;
    	int kLevel = 1;
    	
    	long TreeSearchTime = System.currentTimeMillis();
    	for(int i = 0; i < segmentList.size(); i++){
    		PLASegment next = segmentList.get(i);
    		Polygon2D nextPoly = next.getPolygonKB();
    		length = maxInsectLength(rTree, nextPoly, indexSegs);
    		System.out.println("Insect length:" + length);
    		if(length >= _max_Length){
    			next.children.add(next);
    			list.add(next);
    		}else{
    			Box2D box = nextPoly.getBoundingBox();
    	    	Rectangle nextRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
    	    	rTree.delete(nextRect, i);
    	    	System.out.println("delete 0ne");
    		}
    			
    	}
    	TreeSearchTime = System.currentTimeMillis() - TreeSearchTime;
    	System.out.println("TreeSearchTime" + TreeSearchTime);
    	
    	System.out.println("After prune:" + list.size());
    	PLASegment maxSeg = list.get(0);
    	while(true){
    		kNum = 0;
    		kLevel++;
    		System.out.println("KLevel:" + kLevel);
        	tempList = new ArrayList<PLASegment>();
        	while(list.size() > 0){
        		PLASegment curSegment = list.remove(0);
        		int lastKNum = 0;
        		Polygon2D curPolygon = curSegment.getPolygonKB();
        		List<PLASegment> children1 = curSegment.children;
        		for(int i = 0; i < list.size(); i++){
        			PLASegment testSegment = list.get(i);
    				List<PLASegment> children2 = testSegment.children;
    				boolean isSame = true;
    				for(int j = 0; j < kLevel - 2; j++){
    					if(children1.get(j) != children2.get(j)){
    						isSame = false;
    						break;
    					}
    				}
    				if(isSame){
    					Polygon2D testPolygon = testSegment.getPolygonKB();
    					Polygon2D intersection = Polygon2DUtils.intersection(curPolygon, testPolygon);
    					if(intersection.getVertexNumber() > 0){
    						int tempMax = maxInsectLength(rTree, intersection, indexSegs);
    						if(tempMax >= _max_Length){
    							PLASegment seg1 = children1.get(kLevel - 2);
            					PLASegment seg2 = children2.get(kLevel - 2);
    							if(seg1.getStart() > seg2.getEnd()){
            						PLASegment newSeg = new PLASegment();
                					newSeg.setPolygonKB(intersection);
            						newSeg.children.addAll(children2);
            						newSeg.children.add(seg1);
            						newSeg.setLength(curSegment.getLength() + seg1.getLength());
            						tempList.add(newSeg);
            						lastKNum++;
                					kNum ++;
            					}
            					if(seg1.getEnd() < seg2.getStart()){
            						PLASegment newSeg = new PLASegment();
                					newSeg.setPolygonKB(intersection);
            						newSeg.children.addAll(children1);
            						newSeg.children.add(seg2);
            						newSeg.setLength(curSegment.getLength() + seg2.getLength());
            						tempList.add(newSeg);
            						lastKNum++;
                					kNum ++;
            					}
    						}     					
    					}
    				}
        		}
        		if(lastKNum == 0){
        			if(maxSeg.getLength() < curSegment.getLength())
        				maxSeg = curSegment;
        		}
        	}
        	if(kNum == 0)
        		break;
        	list = tempList;
        	System.out.println("KNum:" + kNum);
    	}
    	System.out.println("==================");
        System.out.println("maxLength:" + maxSeg.toStringKB());
        List<PLASegment> children = maxSeg.children;
        for(PLASegment child : children){
        	System.out.println(child.toStringKB());
        }
    }
    
    public static Rectangle getRectangle(PLASegment seg){
    	Polygon2D poly = seg.getPolygonKB();
    	Box2D box = poly.getBoundingBox();
    	return new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
    }
    public static RTree builtTree(PLASegment[] allSegs){
    	
    	Properties p = new Properties();
		p.setProperty("MinNodeEntries", "16");
		p.setProperty("MaxNodeEntries", "32");
		
    	RTree rTree = new RTree(); 
    	rTree.init(p);
    	
    	for(int i = 0; i < allSegs.length; i++){
    		Rectangle rect = getRectangle(allSegs[i]);
    		rTree.add(rect, i);
    	}
    	
    	return rTree;
    }

    public static int maxInsectLength(RTree rTree, Polygon2D probePoly, PLASegment[] indexSegs){
    	Box2D box = probePoly.getBoundingBox();
    	Rectangle rect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
    	List<Node> leafs = rTree.intersects(rect);
    	
    	System.out.println("Leaf num: " + leafs.size());
    	int length = 0;
    	
    	long leafInsectionTime = System.currentTimeMillis();
    	for(Node leaf : leafs){
    		int tempNum = leaf.getEntryCount();
    		for(int i = 0; i < tempNum; i++){
    			int index = leaf.getId(i);
    			PLASegment tempSeg = indexSegs[index];
    			Polygon2D tempPoly = tempSeg.getPolygonKB();
    			
    			if(Polygon2DUtils.intersection(probePoly, tempPoly).getVertexNumber() > 0){
    				length += tempSeg.getLength();
    			}
    		}
    	}
    	leafInsectionTime = System.currentTimeMillis() - leafInsectionTime;

    	
    	return length;
    }
    
}
