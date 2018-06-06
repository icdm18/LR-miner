package a.b.c.d.tslrm;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PLAPointBoundKBMiner {
    public PLAPointBoundKBMiner(Point2D[] point2Ds, double errorBound) {
        this.points = point2Ds;
        this.pointErrorBound = errorBound;
    }

    public static void main(String[] args) {
        double x[] = {1, 2, 3, 6, 14, 1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6, 1, 2, 3, 4, 1, 2, 3, 4,};
        double y[] = {4, 3, 8, 13, 9, 1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
        System.out.println("x.length = " + x.length);
        System.out.println("y.length = " + y.length);

        double errorBound = 1;
        PLAPointBoundKBMiner miner = new PLAPointBoundKBMiner(x, y, errorBound);

        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
            System.out.println("segment = " + segment.toStringKB());
        }




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

            if (next.getLength() > 2) {
                list.add(next);
            }
        }


        sortSegmentList(list);

        System.out.println("==================");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list.get(i) = " + list.get(i).toStringKB());
        }


        while (list.size() > 1) {
            PLASegment remove = list.remove(0);
            Polygon2D removePolygon = remove.getPolygonKB();

            for (int i = 0; i < list.size(); i++) {
                PLASegment seg = list.get(i);

                Polygon2D segPolygonKB = seg.getPolygonKB();
                Polygon2D intersection = Polygon2DUtils.intersection(removePolygon, segPolygonKB);

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
            System.out.println("==================");
            for (int i = 0; i < list.size(); i++) {
                System.out.println("list.get(i) = " + list.get(i).toStringKB());
            }
        }

        PLASegment plaSegment = list.get(0);
        System.out.println("plaSegment = " + plaSegment.toStringKB());
        for(PLASegment child : plaSegment.children)
        {
            System.out.println("child = " + child.toStringKB());
        }
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

    public final double D_MAX = 100000;

    public final double D_MIN = -D_MAX;


    public final double[] X_INF = {D_MIN, D_MAX, D_MAX, D_MIN};
    public final double[] Y_INF = {D_MIN, D_MIN, D_MAX, D_MAX};



    double[] x;
    double[] y;

    double pointErrorBound;

    public PLAPointBoundKBMiner(double[] x, double[] y, double pointErrorBound) {
        this.x = x;
        this.y = y;
        this.pointErrorBound = pointErrorBound;

        points = new Point2D[x.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point2D(x[i], y[i]);
        }
    }


}
