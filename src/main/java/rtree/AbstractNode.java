package rtree;

import java.util.*;
import java.io.*;


public abstract class AbstractNode implements Node {

    protected int level;


    protected transient RTree tree;


    protected int parent;


    public int pageNumber;


    protected HyperCube[] data;


    public int[] branches;


    public int usedSpace;





    protected AbstractNode(RTree tree, int parent, int pageNumber, int level) {
	this.parent = parent;
	this.tree = tree;
	this.pageNumber = pageNumber;
	this.level = level;
	data = new HyperCube[tree.getNodeCapacity()+1];
	branches = new int[tree.getNodeCapacity()+1];
	usedSpace = 0;
    }






    public int getLevel() {
	return level;
    }


    public boolean isRoot() {
	return (parent == RTree.NIL);
    }


    public boolean isIndex() {
	return (level != 0);
    }


    public boolean isLeaf() {
	return (level == 0);
    }


    public HyperCube getNodeMbb() {
	if (usedSpace > 0) {
	    HyperCube[] h = new HyperCube[usedSpace];
	    System.arraycopy(data, 0, h, 0, usedSpace);
	    return HyperCube.getUnionMbb(h);
	} else {
	    return new HyperCube(new Point(new double[] {0, 0}), new Point(new double[] {0, 0}));
	}
    }




    public String getUniqueId() {
	return Integer.toString(pageNumber);
    }


    public AbstractNode getParent() {
	if (isRoot()) {
	    return null;
	} else {
	    return tree.file.readNode(parent);
	}
    }


    public HyperCube[] getHyperCubes() {
	HyperCube[] h = new HyperCube[usedSpace];

	for (int i = 0; i < usedSpace; i++) {
	    h[i] = (HyperCube) data[i].clone();
	}

	return h;
    }

    public String toString() {
	String s = "< Page: " + pageNumber + ", Level: " + level + ", UsedSpace: " + usedSpace + ", Parent: " + parent + " >\n";

	for (int i = 0; i < usedSpace; i++) {
	    s += "  " + (i+1) + ") " + data[i].toString() + " --> " + " page: " + branches[i] + "\n";
	}
	
	return s;
    }






    protected abstract Leaf chooseLeaf(HyperCube h);


    protected abstract Leaf findLeaf(HyperCube h, int page);






    protected void addData(AbstractNode n) {
	addData(n.getNodeMbb(), n.pageNumber);
    }


    protected void addData(HyperCube h, int page) {
	if (usedSpace == tree.getNodeCapacity()) {
	    throw new IllegalStateException("Node is full.");
	}

	data[usedSpace] = h;
	branches[usedSpace] = page;
	usedSpace++;
    }


    protected void deleteData(int i) {
	System.arraycopy(data, i+1, data, i, usedSpace-i-1);
	System.arraycopy(branches, i+1, branches, i, usedSpace-i-1);
	usedSpace--;
    }


    protected int[][] quadraticSplit(HyperCube h, int page) {
	if (h == null) {
	    throw new IllegalArgumentException("Hypercube cannot be null.");
	}



	data[usedSpace] = h;
	branches[usedSpace] = page;
	int total = usedSpace + 1;


	int[] mask = new int[total];
	for (int i = 0; i < total; i++) {
	    mask[i] = 1;
	}


	int c = total/2 + 1;

	int min = (int)Math.round(tree.getNodeCapacity() * tree.getFillFactor());

	if (min < 2) min = 2;

	int rem = total;


	int[] g1 = new int[c];
	int[] g2 = new int[c];

	int i1 = 0, i2 = 0;


	int[] seed = pickSeeds();
	g1[i1++] = seed[0];
	g2[i2++] = seed[1];
	rem -= 2;
	mask[g1[0]] = -1;
	mask[g2[0]] = -1;

	while (rem > 0) {
	    if (min - i1 == rem) {

		for (int i = 0; i < total; i++) {
		    if (mask[i] != -1) {
			g1[i1++] = i;
			mask[i] = -1;
			rem--;
		    }
		}
	    } else if (min - i2 == rem) {

		for (int i = 0; i < total; i++) {
		    if (mask[i] != -1) {
			g2[i2++] = i;
			mask[i] = -1;
			rem--;
		    }
		}
	    } else {

		HyperCube mbr1 = (HyperCube) data[g1[0]].clone();
		for (int i = 1; i < i1; i++) {
		    mbr1 = mbr1.getUnionMbb(data[g1[i]]);
		}
		HyperCube mbr2 = (HyperCube) data[g2[0]].clone();
		for (int i = 1; i < i2; i++) {
		    mbr2 = mbr2.getUnionMbb(data[g2[i]]);
		}



		double dif = Double.NEGATIVE_INFINITY;
		double d1 = 0, d2 = 0;
		int sel = -1;
		for (int i = 0; i < total; i++) {
		    if (mask[i] != -1) {
			HyperCube a = mbr1.getUnionMbb(data[i]);
			d1 = a.getArea() - mbr1.getArea();
			HyperCube b = mbr2.getUnionMbb(data[i]);
			d2 = b.getArea() - mbr2.getArea();
			if (Math.abs(d1 - d2) > dif) {
			    dif = Math.abs(d1 - d2);
			    sel = i;
			}
		    }
		}


		if (d1 < d2) {
		    g1[i1++] = sel;
		} else if (d2 < d1) {
		    g2[i2++] = sel;
		} else if (mbr1.getArea() < mbr2.getArea()) {
		    g1[i1++] = sel;
		} else if (mbr2.getArea() < mbr1.getArea()) {
		    g2[i2++] = sel;
		} else if (i1 < i2) {
		    g1[i1++] = sel;
		} else if (i2 < i1) {
		    g2[i2++] = sel;
		} else {
		    g1[i1++] = sel;
		}
		mask[sel] = -1;
		rem--;
	    }
	}


	int[][] ret = new int[2][];
	ret[0] = new int[i1];
	ret[1] = new int[i2];

	for (int i = 0; i < i1; i++) {
	    ret[0][i] = g1[i];
	}
	for (int i = 0; i < i2; i++) {
	    ret[1][i] = g2[i];
	}

	return ret;
    }




    protected int[] pickSeeds() {
	double inefficiency = Double.NEGATIVE_INFINITY;
	int i1 = 0, i2 = 0;
	

	for (int i = 0; i < usedSpace; i++) {
	    for (int j = i+1; j <= usedSpace; j++) {

		HyperCube h = data[i].getUnionMbb(data[j]);
		

		double d = h.getArea() - data[i].getArea() - data[j].getArea();
		
		if (d > inefficiency) {
		    inefficiency = d;
		    i1 = i;
		    i2 = j;
		}
	    }
	}
	
	return new int[] {i1, i2};
    }


    protected void condenseTree(Vector q) {
	if (isRoot()) {

	    if (! isLeaf() && usedSpace == 1) {
		AbstractNode n = tree.file.readNode(branches[0]);
		tree.file.deletePage(n.pageNumber);
		n.pageNumber = 0;
		n.parent = RTree.NIL;
		tree.file.writeNode(n);
		if (! n.isLeaf()) {
		    for (int i = 0; i < n.usedSpace; i++) {
			AbstractNode m = ((Index) n).getChild(i);
			m.parent = 0;
			tree.file.writeNode(m);
		    }
		}
	    }
	} else {

	    AbstractNode p = getParent();
	    int e;


	    for (e = 0; e < p.usedSpace; e++) {
		if (pageNumber == p.branches[e]) {
		    break;
		}
	    }

	    int min = (int)Math.round(tree.getNodeCapacity() * tree.getFillFactor());
	    if (usedSpace < min) {

		p.deleteData(e);
		q.addElement(this);
	    } else {

		p.data[e] = getNodeMbb();
	    }

	    tree.file.writeNode(p);
	    p.condenseTree(q);
	}
    }

}


