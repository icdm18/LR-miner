package a.b.c.d.tslrm;

import a.b.c.d.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PLAExtendDeepSearch2 {
    private  Point2D[] point2Ds;
    public double errorBound;
    public PLAExtendDeepSearch2(Point2D[] point2Ds, double errorBound) {
        this.point2Ds = point2Ds;
        this.errorBound = errorBound;
    }
    public PLAExtendDeepSearch2(){
    	
    }

    public double slope;
    public double intercept;

    public int search(List<PLASegment> segmentList, int startLength, double error) {
        int maxUpbound;
        int maxLength = startLength;

        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            segment.idx = i;

            Point2D tempKB = segment.getPolygonKB().getCentroid();
            int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());
            segment.setLowerBound(lowerBound);
            if(maxLength < lowerBound)
            	maxLength = lowerBound;
        }
        System.out.println("init MaxLength is " + maxLength);




        boolean[][] matrix = new boolean[segmentList.size()][segmentList.size()];
        int[] ubs = new int[segmentList.size()];


        System.out.println("init matrix...");
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

        maxUpbound = calcUbs(segmentList, matrix, ubs);

        boolean b = adjustMatrix(matrix, ubs, startLength);
        while (b) {
            maxUpbound = calcUbs(segmentList, matrix, ubs);
            b = adjustMatrix(matrix, ubs, startLength);
        }

        List<PLASegment> stack = new ArrayList<PLASegment>();

        long maxStackSize = 0;
        long minStackSize = segmentList.size();

        Point2D maxKB = new Point2D(0, 0);
        long c = 0;
        
        PLASegment fakeSegment = new PLASegment();
        fakeSegment.idx = -1;
        fakeSegment.setStart(-10);
        fakeSegment.setEnd(-10);
        fakeSegment.setLength(0);
        fakeSegment.setPolygonKB(new SimplePolygon2D(TSPLAPointBoundKBMiner.X_INF, TSPLAPointBoundKBMiner.Y_INF));
        fakeSegment.totalLength = fakeSegment.getLength();
        fakeSegment.currentPolygon = fakeSegment.getPolygonKB();
        int currentLength = fakeSegment.getLength();
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

            PLASegment nextSegment = searchNext(stack, matrix, ubs, segmentList, startIdx);

            if (nextSegment != null) {
                PLASegment topSegment = stack.get(stack.size() - 1);
                stack.add(nextSegment);
                if (stack.size() > maxStackSize) {
                    maxStackSize = stack.size();

                }
                c++;
                if (c % 1000000 == 0) {
                    System.out.println("c = " + c);
                }



                if (nextSegment.getStart() > topSegment.getEnd()) {
                    currentLength = currentLength + nextSegment.getLength();
                } else if (nextSegment.getStart() <= topSegment.getEnd()) {


                    currentLength = currentLength + (nextSegment.getEnd() - topSegment.getEnd());
                }

                nextSegment.totalLength = currentLength;
                
				Point2D tempKB = stack.get(stack.size() - 1).currentPolygon.getCentroid();
                int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());
                if(lowerBound > maxLength){
                	maxKB = tempKB;
                	maxLength = lowerBound;

                	boolean needProne = adjustMatrix2(matrix, ubs, maxLength);
                	while (needProne) {
                        maxUpbound = calcUbs(segmentList, matrix, ubs);
                        needProne = adjustMatrix2(matrix, ubs, maxLength);
                    }

                }
                


                int upperBound = currentLength + checkUpperBound(nextSegment.currentPolygon, segmentList, nextSegment.idx, nextSegment.idx + 1, matrix);
                if (upperBound == maxLength) {



                	maxKB = tempKB;
                	maxLength = lowerBound;
                	boolean needProne = adjustMatrix(matrix, ubs, maxLength);
                	while (needProne) {
                        maxUpbound = calcUbs(segmentList, matrix, ubs);
                        needProne = adjustMatrix(matrix, ubs, maxLength);
                    }

                    for (int i = 1; i < stack.size(); i++) {
                        PLASegment segment = stack.get(i);
                        if (ubs[segment.idx] <= 0) {

                            for (int j = stack.size() - 1; j > i; j--) {
                                stack.remove(j);
                            }
                            break;
                        }
                    }
					startIdx = segmentList.size();
                } else if(upperBound < maxLength){

                    startIdx = segmentList.size();
                }else{
                	 startIdx = nextSegment.idx + 1;
                }
            } else {

                PLASegment removed = stack.remove(stack.size() - 1);
                if (stack.size() < minStackSize) {
                    minStackSize = stack.size();


                }
                if (stack.size() == 0) {
                	System.out.println("Statck Size is 0!!!!!!!!!!!!!!!");
                    break;
                } else {
                    PLASegment topSegment = stack.get(stack.size() - 1);
                    currentLength = topSegment.totalLength;

                    if ((topSegment.getStart() <= removed.getStart()) && (topSegment.getEnd() >= removed.getStart()))
                    {
                        startIdx = nextIdxOfStart(segmentList, topSegment.idx, topSegment.getEnd() + (topSegment.getLength() - 1));
                    } else {
                        startIdx = removed.idx + 1;
                    }
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

    private boolean adjustMatrix(boolean[][] matrix, int[] ubs, long maxLength) {
        boolean needProne = false;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] <= maxLength) {
                if (ubs[i] > 0) {

                    ubs[i] = 0;
                    needProne = true;
                    for (int j = 0; j < ubs.length; j++) {
                        matrix[i][j] = false;
                        matrix[j][i] = false;
                    }
                }
            }
        }
        return needProne;
    }
    
    private boolean adjustMatrix2(boolean[][] matrix, int[] ubs, long maxLength) {
    	boolean needProne = false;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] < maxLength) {
                if (ubs[i] > 0) {

                    ubs[i] = 0;
                    needProne = true;
                    for (int j = 0; j < ubs.length; j++) {
                        matrix[i][j] = false;
                        matrix[j][i] = false;
                    }
                }
            }
        }
        return needProne;
    }

    private int calcUbs(List<PLASegment> segmentList, boolean[][] matrix, int[] ubs) {

        PLASegment lastSegment;
        for (int i = 0; i < ubs.length; i++) {
            ubs[i] = 0;
            lastSegment = null;
            for (int j = 0; j < ubs.length; j++) {
                if (matrix[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                        ubs[i] = ubs[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {



                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                            ubs[i] = ubs[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        } else {


                            if (currentSegment.getEnd() > lastSegment.getEnd()) {
                                ubs[i] = ubs[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            } else {



                            }
                        }
                    }
                }
            }
        }

        long validCount = 0;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] > 0)

            if (ubs[i] > 0) {
                validCount++;
            }
        }


        int max = 0;
        for (int i = 0; i < ubs.length; i++) {
            int ub = ubs[i];
            if (ub > max) {
                max = ub;
            }
        }

        return max;
    }

    private int nextIdxOfStart(List<PLASegment> segmentList, int fromIdx, int start) {
        int ret = segmentList.size();
        for (int i = fromIdx; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if (segment.getStart() >= start) {
                ret = segment.idx;
                break;
            }
        }
        return ret;
    }

    private PLASegment searchNext(List<PLASegment> stack, boolean[][] matrix, int[] ubs, List<PLASegment> segmentList, int startIdx) {
        PLASegment topSegment = stack.get(stack.size() - 1);

        Polygon2D currentPolygon = topSegment.currentPolygon;
        for (int i = startIdx; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);

            if (ubs[i] <= 0)
                continue;

            if (topSegment.idx >= 0)
                if (false == matrix[topSegment.idx][i])
                    continue;

            Polygon2D intersection = Polygon2DUtils.intersection(currentPolygon, segment.getPolygonKB());
            if (intersection.getVertexNumber() >= 0) {
                segment.currentPolygon = intersection;
                return segment;
            }
        }

        return null;
    }

    private int checkUpperBound(Polygon2D currentPolygon, List<PLASegment> segmentList, int currentIdx, int startIdx, boolean[][] matrix) {
        List<PLASegment> list = new ArrayList<PLASegment>();
        for (int i = startIdx; i < segmentList.size(); i++) {
            if (currentIdx >= 0) {
                if (matrix[currentIdx][i] == false)
                    continue;
            }

            PLASegment segment = segmentList.get(i);
            if (Polygon2DUtils.intersection(currentPolygon, segment.getPolygonKB()).getVertexNumber() > 0) {
                list.add(segment);
            }
        }

        return calcUpperBound(list, segmentList.get(currentIdx));
    }

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
    

    public void sortSegmentList(List<PLASegment> segmentList){

        Collections.sort(segmentList, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o2.getLowerBound() - o1.getLowerBound();
            }
        });
    }
}
