package a.b.c.d.tslrm;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.Collection;
import java.util.Iterator;


public class PolygonIntersectionTest {
    public static void main(String[] args) {
        Polygon2D polygon1 = new SimplePolygon2D(new double[]{1,2,2,1},new double[]{1,1,2,2});
        Polygon2D polygon2 = new SimplePolygon2D(new double[]{1.5,2.5,2.5,1.5},new double[]{1.5,1.5,2.5,2.5});
        polygon1.getBoundingBox();


         polygon1 = new SimplePolygon2D(new double[]{-1,-2,-2,-1},new double[]{1,1,2,2});
         polygon2 = new SimplePolygon2D(new double[]{1.5,2.5,2.5,1.5},new double[]{1.5,1.5,2.5,2.5});

        t1(polygon1, polygon2);



    }

    private static void t1(Polygon2D polygon1, Polygon2D polygon2) {
        Polygon2D polygon3 = Polygon2DUtils.intersection(polygon1, polygon2);
        Collection<Point2D> vertices = polygon3.getVertices();
        for (Point2D next : vertices) {
            System.out.println("next.getX() = " + next.getX());
            System.out.println("next.getY() = " + next.getY());

        }

        String s = polygon3.toString();
        System.out.println("s = " + s);
    }
}
