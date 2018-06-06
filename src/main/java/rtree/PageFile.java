package rtree;


public abstract class PageFile {
    protected RTree tree = null;


    protected int dimension = -1;


    protected double fillFactor = -1;


    protected int nodeCapacity = -1;


    protected int pageSize = -1;


    protected int treeType = -1;









    public abstract AbstractNode readNode(int page) throws PageFaultError;


    protected abstract int writeNode(AbstractNode o) throws PageFaultError;


    protected abstract AbstractNode deletePage(int page) throws PageFaultError;

    protected void initialize(RTree tree, int dimension, double fillFactor, int capacity, int treeType) {
	this.dimension = dimension;
	this.fillFactor = fillFactor;
	this.nodeCapacity = capacity;
	this.treeType = treeType;
	this.tree = tree;

	this.pageSize = capacity * (16 * dimension + 4) + 12;
    }

    protected void finalize() throws Throwable {
	super.finalize();
    }

}
