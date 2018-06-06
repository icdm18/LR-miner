package a.b.c.d.tslrm;

import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;
import com.seisw.util.geom.PolySimple;
import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.MultiPolygon2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.SimplePolygon2D;

import java.math.BigDecimal;

public class NewPolyUtils {
	

	
	public static boolean isSame(Polygon2D polygon1, Polygon2D polygon2){

    	Poly poly1 = convertToGpcjPolygon(polygon1);
    	Poly poly2 = convertToGpcjPolygon(polygon2);
    	int vertexNum = poly1.getNumPoints();
    	if(poly2.getNumPoints() != vertexNum){
    		return false;
    	}
    	for(int i = 0; i < vertexNum; i++){
    		double xDiff = Math.abs(poly1.getX(i) - poly2.getX(i));
    		double yDiff = Math.abs(poly1.getY(i) - poly2.getY(i));
    		if(xDiff + yDiff > 0.00000001){
    			return false;
    		}
    	}
    	return true;
	}

	public final static Polygon2D intersection(Polygon2D polygon1, Polygon2D polygon2) {

		if(isSame(polygon1, polygon2)){
			return polygon1;
		}
    	Poly poly1 = convertToGpcjPolygon(polygon1);
    	Poly poly2 = convertToGpcjPolygon(polygon2);

    	Poly result = poly1.intersection(poly2);


    	return convertFromGpcjPolygon(result);
    }
    private final static Poly convertToGpcjPolygon(Polygon2D polygon) {
    	PolyDefault result = new PolyDefault();
    	for (LinearRing2D ring : polygon.getRings())
    		result.add(convertToGpcjSimplePolygon(ring));
    	return result;
    }
    
    private final static PolySimple convertToGpcjSimplePolygon(
    		LinearRing2D ring) {
    	PolySimple poly = new PolySimple();
    	for (Point2D point : ring.getVertices())
    		poly.add(new com.seisw.util.geom.Point2D(point.getX(), point.getY()));
    	return poly;
    }
    
    private final static Polygon2D convertFromGpcjPolygon(Poly poly) {
    	int n = poly.getNumInnerPoly();
    	

    	if (n == 1) {
    		Point2D[] points = extractPolyVertices(poly.getInnerPoly(0));
    		return SimplePolygon2D.create(points);
    	}
    	

    	LinearRing2D[] rings = new LinearRing2D[n];
    	for (int i = 0; i < n; i++) 
    		rings[i] = convertFromGpcjSimplePolygon(poly.getInnerPoly(i));
    	

    	return MultiPolygon2D.create(rings);
    }
    
    private final static Point2D[] extractPolyVertices(Poly poly) {
    	int n = poly.getNumPoints();
    	Point2D[] points = new Point2D[n];
    	for (int i = 0; i < n; i++)
    		points[i] = Point2D.create(round(poly.getX(i)), round(poly.getY(i)));
    	return points;
    }
    
    private final static LinearRing2D convertFromGpcjSimplePolygon(
    		Poly poly) {
    	return LinearRing2D.create(extractPolyVertices(poly));
    }
    
    private static double round(double value) {  
        BigDecimal bd = new BigDecimal(value);  
        bd = bd.setScale(8, BigDecimal.ROUND_HALF_UP);  
        double d = bd.doubleValue();  
        bd = null;  
        return d;  
    }  

}
