package rtree;


public class Data {
    public HyperCube mbb;
    public int dataPointer;
    public int parent;
    public int position;

    public Data(HyperCube h, int d, int p, int pos) {
	mbb = h;
	dataPointer = d;
	parent = p;
	position = pos;
    }
    
}
