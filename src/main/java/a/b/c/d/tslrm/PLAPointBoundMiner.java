package a.b.c.d.tslrm;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import java.util.*;


public class PLAPointBoundMiner {
    public PLAPointBoundMiner(Point2D[] point2Ds, double errorBound) {
        this.points = point2Ds;
        this.pointErrorBound = errorBound;
    }

    public static void main(String[] args) {
        double x[] = {1, 2, 3, 6, 14,1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6,  8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6,1, 2, 3, 4,1, 2, 3, 4,};
        double y[] = {4, 3, 8, 13, 9,1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
        System.out.println("x.length = " + x.length);
        System.out.println("y.length = " + y.length);

        double errorBound = 2;
        PLAPointBoundMiner miner = new PLAPointBoundMiner(x, y, errorBound);

        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
            System.out.println("segment = " + segment);
        }

        boolean verify = PLASegment.verify(miner.points, miner.plaSegmentList, errorBound);
        System.out.println("verify = " + verify);

        findMaxIntersection(miner.plaSegmentList);
    }


    public static void sortSegmentList(List<PLASegment> segments) {

        Collections.sort(segments, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o1.getLength() - o2.getLength();
            }
        });
    }

    public static void findMaxIntersection(List<PLASegment> segments) {
        List<PLASegment> list = new ArrayList<PLASegment>();

        for (PLASegment next : segments) {
            next.children.add(next);

            if (next.getLength() > 2)
            {
                list.add(next);
            }
        }


        sortSegmentList(list);

        System.out.println("==================");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list.get(i) = " + list.get(i));
        }


        while (list.size() > 1) {
            PLASegment remove = list.remove(0);
            Polygon2D removePolygon = remove.getPolygonYM();

            for (int i = 0; i < list.size(); i++) {
                PLASegment seg = list.get(i);

                Polygon2D segPolygonYM = seg.getPolygonYM();
                Polygon2D intersection = Polygon2DUtils.intersection(removePolygon, segPolygonYM);

                if (intersection.getVertexNumber() > 0)
                {
                    PLASegment newSeg = new PLASegment();

                    newSeg.setPolygonYM(intersection);
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
            System.out.println("==================");
            for (int i = 0; i < list.size(); i++) {
                System.out.println("list.get(i) = " + list.get(i));
            }
        }

        PLASegment plaSegment = list.get(0);
        System.out.println("plaSegment = " + plaSegment);
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
        plaSegment.setPolygonYM(polygonYMOfPoint(points[i]));

        boolean verify = plaSegment.verify(points, pointErrorBound);

        while (j < points.length) {
            Polygon2D p1 = polygonYMOf2Points(points[i], points[j]);
            Polygon2D p = Polygon2DUtils.intersection(plaSegment.getPolygonYM(), p1);

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
                plaSegment.setPolygonYM(polygonYMOfPoint(points[i]));
            } else {
                plaSegment.setEnd(j);
                plaSegment.setPolygonYM(p);

                verify = plaSegment.verify(points, pointErrorBound);

                j = j + 1;
            }
        }

        plaSegmentList.add(plaSegment);
    }

    public final double D_MAX = Long.MAX_VALUE/2;
    public final double D_MIN = Long.MIN_VALUE/2;

    public final double[] X_INF = {D_MIN, D_MAX, D_MAX, D_MIN};
    public final double[] Y_INF = {D_MIN, D_MIN, D_MAX, D_MAX};

    private Polygon2D polygonYMOfPoint(Point2D point) {
        double y[] = {point.getY() - pointErrorBound, point.getY() + pointErrorBound, point.getY() + pointErrorBound, point.getY() - pointErrorBound};
        double m[] = Y_INF;
        return new SimplePolygon2D(y, m);
    }

    private Polygon2D polygonYMOf2Points(Point2D point1, Point2D point2) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        double y[] = {y1 - pointErrorBound, y1 + pointErrorBound, y1 + pointErrorBound, y1 - pointErrorBound};
        if (x1 == x2) {
            Interval interval1 = new Interval(y1 - pointErrorBound, y1 + pointErrorBound);
            Interval interval2 = new Interval(y2 - pointErrorBound, y2 + pointErrorBound);

            Interval interval3 = intersection(interval1, interval2);
            if (interval3 != null) {
                y[0] = interval3.getLower();
                y[1] = interval3.getUpper();
                y[2] = interval3.getUpper();
                y[3] = interval3.getLower();
                return new SimplePolygon2D(y, Y_INF);
            } else {
                return new SimplePolygon2D();
            }
        } else if (x1 < x2) {




            double m0 = (y2 - y1) / (x2 - x1);

            double m1 = (y2 - y1 - 2 * pointErrorBound) / (x2 - x1);

            double m2 = (y2 - y1) / (x2 - x1);

            double m3 = (y2 - y1 + 2 * pointErrorBound) / (x2 - x1);

            double m[] = {m0, m1, m2, m3};
            return new SimplePolygon2D(y, m);
        } else {




            double m0 = (y2 - y1 + 2 * pointErrorBound) / (x2 - x1);

            double m1 = (y2 - y1) / (x2 - x1);

            double m2 = (y2 - y1 - 2 * pointErrorBound) / (x2 - x1);

            double m3 = (y2 - y1) / (x2 - x1);

            double m[] = {m0, m1, m2, m3};
            return new SimplePolygon2D(y, m);
        }
    }

    double[] x;
    double[] y;

    double pointErrorBound;

    public PLAPointBoundMiner(double[] x, double[] y, double pointErrorBound) {
        this.x = x;
        this.y = y;
        this.pointErrorBound = pointErrorBound;

        points = new Point2D[x.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point2D(x[i], y[i]);
        }
    }

    public Interval intersection(Interval interval1, Interval interval2) {
        double lower = Math.max(interval1.getLower(), interval2.getLower());
        double upper = Math.min(interval1.getUpper(), interval2.getUpper());

        if (upper >= lower) {
            return new Interval(lower, upper);
        } else {
            return null;
        }
    }
}
