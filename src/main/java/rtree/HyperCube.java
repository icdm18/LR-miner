package rtree;


public class HyperCube implements Cloneable {
    private Point p1;

    private Point p2;

    public HyperCube(Point p1, Point p2) {
	if (p1 == null || p2 == null) throw new
	    IllegalArgumentException("Points cannot be null.");

	if (p1.getDimension() != p2.getDimension()) throw new
	    IllegalArgumentException("Points must be of the same dimension.");

	for (int i = 0; i < p1.getDimension(); i++) {
	    if (p1.getDoubleCoordinate(i) > p2.getDoubleCoordinate(i))
        {
            throw new IllegalArgumentException("Give lower left corner first and upper right corner afterwards.");

        }
	}

	this.p1 = (Point) p1.clone();
	this.p2 = (Point) p2.clone();
    }

    public int getDimension() {
	return p1.getDimension();
    }

    public Point getP1() {
	return (Point) p1.clone();
    }

    public Point getP2() {
	return (Point) p2.clone();
    }

    public boolean equals(HyperCube h) {
	if (p1.equals(h.getP1()) && p2.equals(h.getP2())) {
	    return true;
	} else {
	    return false;
	}
    }


    public boolean intersection(HyperCube h) {
	if (h == null) throw new
	    IllegalArgumentException("HyperCube cannot be null.");

	if (h.getDimension() != getDimension()) throw new
	    IllegalArgumentException("HyperCube dimension is different from current dimension.");

	boolean intersect = true;
	for (int i = 0; i < getDimension(); i++) {
	    if (p1.getDoubleCoordinate(i) > h.p2.getDoubleCoordinate(i) ||
		p2.getDoubleCoordinate(i) < h.p1.getDoubleCoordinate(i)) {
		intersect = false;
		break;
	    }
	}
	return intersect;
    }


    public boolean enclosure(HyperCube h) {
	if (h == null) throw new
	    IllegalArgumentException("HyperCube cannot be null.");

	if (h.getDimension() != getDimension()) throw new
	    IllegalArgumentException("HyperCube dimension is different from current dimension.");

	boolean inside = true;
	for (int i = 0; i < getDimension(); i++) {
	    if (p1.getDoubleCoordinate(i) > h.p1.getDoubleCoordinate(i) ||
		p2.getDoubleCoordinate(i) < h.p2.getDoubleCoordinate(i)) {
		inside = false;
		break;
	    }
	}

	return inside;
    }


    public boolean enclosure(Point p) {
	if (p == null) throw new
	    IllegalArgumentException("Point cannot be null.");

	if (p.getDimension() != getDimension()) throw new
	    IllegalArgumentException("Point dimension is different from HyperCube dimension.");

	return enclosure(new HyperCube(p, p));
    }


    public double intersectingArea(HyperCube h) {
	if (!intersection(h)) {
	    return 0;
	} else {
	    double ret = 1;
	    for (int i = 0; i < getDimension(); i++) {
		double l1 = p1.getDoubleCoordinate(i);
		double h1 = p2.getDoubleCoordinate(i);
		double l2 = h.p1.getDoubleCoordinate(i);
		double h2 = h.p2.getDoubleCoordinate(i);

		if (l1 <= l2 && h1 <= h2) {

		    ret *= (h1 - l1) - (l2 - l1);
		} else if (l2 <= l1 && h2 <= h1) {

		    ret *= (h2 - l2) - (l1 - l2);
		} else if (l2 <= l1 && h1 <= h2) {

		    ret *= h1 - l1;
		} else if (l1 <= l2 && h2 <= h1) {

		    ret *= h2 - l2;
		} else if (l1 <= l2 && h2 <= h1) {

		    ret *= h2 - l2;
		} else if (l2 <= l1 && h1 <= h2) {

		    ret *= h1 - l1;
		}
	    }
	    if (ret <= 0) throw new
		ArithmeticException("Intersecting area cannot be negative!");
	    return ret;
	}
    }


    public static HyperCube getUnionMbb(HyperCube[] a) {
	if (a == null || a.length == 0) throw new
	    IllegalArgumentException("HyperCube array is empty.");

	HyperCube h = (HyperCube) a[0].clone();

	for (int i = 1; i < a.length; i++) {
	    h = h.getUnionMbb(a[i]);
	}

	return h;
    }


    public HyperCube getUnionMbb(HyperCube h) {
	if (h == null) throw new
	    IllegalArgumentException("HyperCube cannot be null.");

	if (h.getDimension() != getDimension()) throw new
	    IllegalArgumentException("HyperCubes must be of the same dimension.");

	double[] min = new double[getDimension()];
	double[] max = new double[getDimension()];

	for (int i = 0; i < getDimension(); i++) {
	    min[i] = Math.min(p1.getDoubleCoordinate(i), h.p1.getDoubleCoordinate(i));
	    max[i] = Math.max(p2.getDoubleCoordinate(i), h.p2.getDoubleCoordinate(i));
	}

	return new HyperCube(new Point(min), new Point(max));
    }


    public double getArea() {
	double area = 1;

	for (int i = 0; i < getDimension(); i++) {
	    area *= p2.getDoubleCoordinate(i) - p1.getDoubleCoordinate(i);
	}

	return area;
    }


    public double getMinDist(Point p) {
	if (p == null) throw new
	    IllegalArgumentException("Point cannot be null.");

	if (p.getDimension() != getDimension()) throw new
	    IllegalArgumentException("Point dimension is different from HyperCube dimension.");

	double ret = 0;
	for (int i = 0; i < getDimension(); i++) {
	    double q = p.getDoubleCoordinate(i);
	    double s = p1.getDoubleCoordinate(i);
	    double t = p2.getDoubleCoordinate(i);
	    double r;

	    if (q < s) r = s;
	    else if (q > t) r = t;
	    else r = q;

	    ret += Math.pow(Math.abs(q - r), 2);
	}

	return ret;
    }

    public Object clone() {
	return new HyperCube((Point) p1.clone(), (Point) p2.clone());
    }

    public String toString() {
	return "P1" + p1.toString() + ":P2" + p2.toString();
    }

}
