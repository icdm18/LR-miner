package rtree;

import java.util.*;

import math.geom2d.Box2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;

import a.b.c.d.tslrm.PLASegment;


public class RTree {
    public final String version = "1.003";
    public final String date = "December 7th 1999";


    public PageFile file = null;


    public static final int NIL = -1;


    public static final int RTREE_LINEAR = 0;
    public static final int RTREE_QUADRATIC = 1;
    public static final int RTREE_EXPONENTIAL = 2;
    public static final int RSTAR = 3;
    
    private List<PLASegment> segmentList;
    private PLASegment currentSeg;
    private Polygon2D currentPoly;







    public RTree(int dimension, double fillFactor, int capacity, PageFile file, int type, List<PLASegment> segmentList) {
        if (dimension <= 1) {
            throw new IllegalArgumentException("Dimension must be larger than 1.");
        }

        if (fillFactor < 0 || fillFactor > 0.5) {
            throw new IllegalArgumentException("Fill factor must be between 0 and 0.5.");
        }

        if (capacity <= 1) {
            throw new IllegalArgumentException("Capacity must be larger than 1.");
        }

        if (type != RTREE_QUADRATIC ) {
            throw new IllegalArgumentException("Invalid tree type.");
        }

        if (file.tree != null) {
            throw new IllegalArgumentException("PageFile already in use by another rtree instance.");
        }

        file.initialize(this, dimension, fillFactor, capacity, type);
        this.file = file;

        this.segmentList = segmentList;
        Leaf root = new Leaf(this, NIL, 0);
        file.writeNode(root);
    }


    public RTree(PageFile file) {
        if (file.tree != null) {
            throw new IllegalArgumentException("PageFile already in use by another rtree instance.");
        }

        if (file.treeType == -1) {
            throw new IllegalArgumentException("PageFile is empty. Use some other RTree constructor.");
        }

        file.tree = this;
        this.file = file;
    }
    
    public void buildRTree(){
    	int length = segmentList.size();
    	for(int i = 0; i < length; i++){
    		PLASegment tempSeg = segmentList.get(i);
    		Polygon2D tempPoly = tempSeg.getPolygonKB();
    		Box2D box = tempPoly.getBoundingBox();
    		Point p1 = new Point(new double[]{box.getMinX(), box.getMinY(), i});
    		Point p2 = new Point(new double[]{box.getMaxX(), box.getMaxY(), i});
    		HyperCube h = new HyperCube(p1, p2);
    		this.insert(h, i);
    	}
    }


    public int getNodeCapacity() {
        return file.nodeCapacity;
    }


    public double getFillFactor() {
        return file.fillFactor;
    }


    public int getDimension() {
        return file.dimension;
    }


    public int getPageSize() {
        return file.pageSize;
    }


    public int getTreeLevel() {
        return file.readNode(0).getLevel();
    }


    public int getTreeType() {
        return file.treeType;
    }


    public int insert(HyperCube h, int page) {
        if (h == null) {
            throw new IllegalArgumentException("HyperCube cannot be null.");
        }

        if (h.getDimension() != file.dimension) {
            throw new IllegalArgumentException("HyperCube dimension different than RTree dimension.");
        }

        AbstractNode root = file.readNode(0);

        Leaf l = root.chooseLeaf(h);
        return l.insert(h, page);
    }


    public int delete(HyperCube h, int page) {
        if (h == null) {
            throw new IllegalArgumentException("HyperCube cannot be null.");
        }

        if (h.getDimension() != file.dimension) {
            throw new IllegalArgumentException("HyperCube dimension different than RTree dimension.");
        }

        AbstractNode root = file.readNode(0);

        Leaf l = root.findLeaf(h, page);
        if (l != null) {
            return l.delete(h, page);
        }
        return NIL;
    }


    public Vector traverseByLevel(AbstractNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }

        Vector ret = new Vector();
        Vector v = traversePostOrder(root);

        for (int i = 0; i <= getTreeLevel(); i++) {
            Vector a = new Vector();
            for (int j = 0; j < v.size(); j++) {
                Node n = (Node) v.elementAt(j);
                if (n.getLevel() == i) {
                    a.addElement(n);
                }
            }
            for (int j = 0; j < a.size(); j++) {
                ret.addElement(a.elementAt(j));
            }
        }

        return ret;
    }


    public Enumeration traverseByLevel() {
        class ByLevelEnum implements Enumeration {

            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public ByLevelEnum() {
                AbstractNode root = file.readNode(0);
                nodes = traverseByLevel(root);
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("traverseByLevel");
                }

                Object n = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return n;
            }
        }
        ;

        return new ByLevelEnum();
    }


    public Vector traversePostOrder(AbstractNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }

        Vector v = new Vector();
        v.addElement(root);

        if (root.isLeaf()) {
        } else {
            for (int i = 0; i < root.usedSpace; i++) {
                Vector a = traversePostOrder(((Index) root).getChild(i));
                for (int j = 0; j < a.size(); j++) {
                    v.addElement(a.elementAt(j));
                }
            }
        }
        return v;
    }


    public Enumeration traversePostOrder() {
        class PostOrderEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public PostOrderEnum() {
                AbstractNode root = file.readNode(0);
                nodes = traversePostOrder(root);
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("traversePostOrder");
                }

                Object n = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return n;
            }
        }
        ;

        return new PostOrderEnum();
    }


    public Vector traversePreOrder(AbstractNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }

        Vector v = new Vector();

        if (root.isLeaf()) {
            v.addElement(root);
        } else {
            for (int i = 0; i < root.usedSpace; i++) {
                Vector a = traversePreOrder(((Index) root).getChild(i));
                for (int j = 0; j < a.size(); j++) {
                    v.addElement(a.elementAt(j));
                }
            }
            v.addElement(root);
        }
        return v;
    }


    public Enumeration traversePreOrder() {
        class PreOrderEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public PreOrderEnum() {
                AbstractNode root = file.readNode(0);
                nodes = traversePreOrder(root);
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("traversePreOrder");
                }

                Object n = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return n;
            }
        }
        ;

        return new PreOrderEnum();
    }


    public Vector intersection(HyperCube h, AbstractNode root) {
        if (h == null || root == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        if (h.getDimension() != file.dimension) {
            throw new IllegalArgumentException("HyperCube dimension different than RTree dimension.");
        }

        Vector v = new Vector();

        if (root.getNodeMbb().intersection(h)) {
            v.addElement(root);

            if (!root.isLeaf()) {
                for (int i = 0; i < root.usedSpace; i++) {
                    if (root.data[i].intersection(h)) {
                        Vector a = intersection(h, ((Index) root).getChild(i));
                        for (int j = 0; j < a.size(); j++) {
                            v.addElement(a.elementAt(j));
                        }
                    }
                }
            }
        }
        return v;
    }


    public void initMatrix(boolean[][] matrix){
    	int length = segmentList.size();
    	for(int i = 0; i < length; i++){
    		matrix[i][i] = true;
    		PLASegment curSegment = segmentList.get(i);
    		Polygon2D probePoly = curSegment.getPolygonKB();
  		  	Box2D box = probePoly.getBoundingBox();
  		  	Point p1 = new Point(new double[]{box.getMinX(), box.getMinY(), -Double.MAX_VALUE});
  		  	Point p2 = new Point(new double[]{box.getMaxX(), box.getMaxY(), Double.MAX_VALUE});
  		  	HyperCube h = new HyperCube(p1, p2);
  		  	initMatrixInterSection(file.readNode(0), matrix, probePoly, h, i);
    	}
    	;
    }
    
    public void initMatrixInterSection(AbstractNode n, boolean[][] matrix, Polygon2D probePoly, HyperCube h, int start){
    		if(n.isLeaf()){
    			for(int i = 0; i < n.usedSpace; i++){
    				if(n.branches[i] > start && n.data[i].intersection(h)){
    					PLASegment tempSeg = segmentList.get(n.branches[i]);
    					Polygon2D tempPoly = tempSeg.getPolygonKB();
    					Polygon2D intersection = Polygon2DUtils.intersection(probePoly, tempPoly);
    					 if(intersection.getVertexNumber() > 0){
    						  matrix[start][tempSeg.idx] = true;
    						  matrix[tempSeg.idx][start] = true;
    					  }
    				}
    			}
    		}else{
    			for (int i = 0; i < n.usedSpace; i++) {
                    if (n.data[i].intersection(h)) {
                    	initMatrixInterSection(((Index) n).getChild(i), matrix, probePoly, h, start);                    	
                    }
                }
    		}
    }
    

    
    public int calUpperBound(PLASegment probeSeg, int currentIdx){
    	this.currentSeg = probeSeg;
  	  	this.currentPoly = probeSeg.getPolygonKB();
  	  	Box2D box = currentPoly.getBoundingBox();
  	  	Point p1 = new Point(new double[] {box.getMinX(), box.getMinY(), currentIdx});
  	  	Point p2 = new Point(new double[] {box.getMaxX(), box.getMaxY(), Double.MAX_VALUE});
  	  	HyperCube h = new HyperCube(p1, p2);
  	  	AbstractNode node = file.readNode(0);
  	  	return upperBoundInterSection(node, h);
    }
    
    public int upperBoundInterSection(AbstractNode n, HyperCube h){
    	int upperBound = 0;
    	if(n.isLeaf()){
    		for(int i = 0; i < n.usedSpace; i++){
    			if(n.data[i].intersection(h)){
    				PLASegment tempSeg = segmentList.get(n.branches[i]);
    				Polygon2D tempPoly = tempSeg.getPolygonKB();
    				Polygon2D intersection = Polygon2DUtils.intersection(this.currentPoly, tempPoly);
    				if(intersection.getVertexNumber() > 0){
				 		if (tempSeg.getStart() > currentSeg.getEnd()) {
				 			upperBound += tempSeg.getLength();
			            }else{
			            	upperBound += tempSeg.getEnd() - currentSeg.getEnd();
			            }
				 	}
    			}
    		}
    	}else{
    		for(int i = 0; i < n.usedSpace; i++) {
                if (n.data[i].intersection(h)) {
                	upperBound += upperBoundInterSection(((Index) n).getChild(i), h);                    	
                }
            }
    	}
    	return upperBound;
    }
    

    public Enumeration intersection(HyperCube h) {
        class IntersectionEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public IntersectionEnum(HyperCube hh) {
                nodes = intersection(hh, file.readNode(0));
                if (nodes.isEmpty()) {
                    hasNext = false;
                }
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("intersection");
                }

                Object c = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return c;
            }
        }
        ;

        return new IntersectionEnum(h);
    }


    public Vector enclosure(HyperCube h, AbstractNode root) {
        if (h == null || root == null) throw new
                IllegalArgumentException("Arguments cannot be null.");

        if (h.getDimension() != file.dimension) throw new
                IllegalArgumentException("HyperCube dimension different than RTree dimension.");

        Vector v = new Vector();

        if (root.getNodeMbb().enclosure(h)) {
            v.addElement(root);

            if (!root.isLeaf()) {
                for (int i = 0; i < root.usedSpace; i++) {
                    if (root.data[i].enclosure(h)) {
                        Vector a = enclosure(h, ((Index) root).getChild(i));
                        for (int j = 0; j < a.size(); j++) {
                            v.addElement(a.elementAt(j));
                        }
                    }
                }
            }
        }
        return v;
    }


    public Enumeration enclosure(HyperCube h) {
        class ContainEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector cubes;

            private int index = 0;

            public ContainEnum(HyperCube hh) {
                cubes = enclosure(hh, file.readNode(0));
                if (cubes.isEmpty()) {
                    hasNext = false;
                }
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) throw new
                        NoSuchElementException("enclosure");

                Object c = cubes.elementAt(index);
                index++;
                if (index == cubes.size()) {
                    hasNext = false;
                }
                return c;
            }
        }
        ;

        return new ContainEnum(h);
    }


    public Vector enclosure(Point p, AbstractNode root) {
        return enclosure(new HyperCube(p, p), root);
    }


    public Enumeration enclosure(Point p) {
        return enclosure(new HyperCube(p, p));
    }


    public Vector nearestNeighbor(Point p) {
        return nearestNeighborSearch(file.readNode(0), p, Double.POSITIVE_INFINITY);
    }


    protected Vector nearestNeighborSearch(AbstractNode n, Point p, double nearest) {
        Vector ret = new Vector();
        HyperCube h;

        if (n.isLeaf()) {
            for (int i = 0; i < n.usedSpace; i++) {
                double dist = n.data[i].getMinDist(p);
                if (dist < nearest) {
                    h = n.data[i];
                    nearest = dist;
                    ret.addElement(new Data(h, n.branches[i], n.pageNumber, i));
                }
            }
            return ret;
        } else {

            class ABLNode {
                AbstractNode node;
                double minDist;

                public ABLNode(AbstractNode node, double minDist) {
                    this.node = node;
                    this.minDist = minDist;
                }
            }

            ABLNode[] abl = new ABLNode[n.usedSpace];

            for (int i = 0; i < n.usedSpace; i++) {
                AbstractNode ch = ((Index) n).getChild(i);
                abl[i] = new ABLNode(ch, ch.getNodeMbb().getMinDist(p));
            }


            Sort.mergeSort(
                    abl,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            double f = ((ABLNode) o1).minDist -
                                    ((ABLNode) o2).minDist;


                            if (f > 0) return 1;
                            else if (f < 0) return -1;
                            else return 0;
                        }
                    }
            );


            for (int i = 0; i < abl.length; i++) {
                if (abl[i].minDist <= nearest) {

                    ret.addElement(abl[i].node);

                    Vector v = nearestNeighborSearch(abl[i].node, p, nearest);



                    try {
                        Object o = v.lastElement();
                        if (o instanceof AbstractNode) {
                            for (int j = 0; j < v.size(); j++) {
                                ret.addElement(v.elementAt(j));
                            }
                            AbstractNode an = (AbstractNode) o;
                            h = (HyperCube) an.getNodeMbb();
                            nearest = h.getMinDist(p);
                        } else if (o instanceof Data) {


                            h = ((Data) o).mbb;
                            nearest = h.getMinDist(p);
                            ret.addElement(o);
                        }
                    } catch (NoSuchElementException e) {


                    }
                }
            }

            return ret;
        }
    }

}
