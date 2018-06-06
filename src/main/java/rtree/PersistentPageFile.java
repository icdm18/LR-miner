package rtree;

import java.io.*;
import java.util.*;


public class PersistentPageFile extends PageFile {

    private RandomAccessFile file;
    private String fileName;

    private Stack emptyPages = new Stack();


    private int headerSize = 24;

    public static final int EMPTY_PAGE = -2;

    public PersistentPageFile() {
	this(null);
    }

    public PersistentPageFile(String fileName) {
	try {
	    if (fileName == null) {
		File f = File.createTempFile("rtree", ".dat");
		this.fileName = f.getCanonicalPath();
		f.deleteOnExit();
		file = new RandomAccessFile(f, "rw");
	    } else {
		file = new RandomAccessFile(fileName, "rw");
		this.fileName = fileName;

		file.seek(0);
		byte[] header = new byte[headerSize];
		if (headerSize == file.read(header)) {
		    DataInputStream ds = new DataInputStream(new ByteArrayInputStream(header));
		    dimension = ds.readInt();
		    fillFactor = ds.readDouble();
		    nodeCapacity = ds.readInt();
		    pageSize = ds.readInt();
		    treeType = ds.readInt();


		    int i = 0;
		    try {
			while (true) {
			    if (EMPTY_PAGE == file.readInt()) {
				emptyPages.push(new Integer(i));
			    }
			    i++;
			    file.seek(headerSize + i * pageSize);
			}
		    } catch (IOException e) {
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    protected void initialize(RTree tree, int dimension, double fillFactor, int capacity, int treeType) {
	super.initialize(tree, dimension, fillFactor, capacity, treeType);
	emptyPages.clear();
       
	try {

	    file.setLength(0);

	    file.seek(0);
	    file.writeInt(dimension);
	    file.writeDouble(fillFactor);
	    file.writeInt(nodeCapacity);
	    file.writeInt(pageSize);
	    file.writeInt(treeType);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    protected void finalize() throws Throwable {
	try {
	    file.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	super.finalize();
    }



    public AbstractNode readNode(int page) throws PageFaultError {
	if (page < 0) {
	    throw new IllegalArgumentException("Page number cannot be negative.");
	}

	try {
	    file.seek(headerSize + page * pageSize);

	    byte[] b = new byte[pageSize];
	    int l = file.read(b);
	    if (-1 == l) {
		throw new PageFaultError("EOF found while trying to read page " + page + ".");
	    }

	    DataInputStream ds = new DataInputStream(new ByteArrayInputStream(b));

	    int parent = ds.readInt();
	    if (parent == EMPTY_PAGE) {
		throw new PageFaultError("Page " + page + " is empty.");
	    }

	    int level = ds.readInt();
	    int usedSpace = ds.readInt();

	    AbstractNode n;
	    if (level != 0) {
		n = new Index(tree, parent, page, level);
	    } else {
		n = new Leaf(tree, parent, page);
	    }

	    n.parent = parent;
	    n.level = level;
	    n.usedSpace = usedSpace;

	    double[] p1 = new double[dimension];
	    double[] p2 = new double[dimension];

	    for (int i = 0; i < usedSpace; i++) {
		for (int j = 0; j < dimension; j++) {
		    p1[j] = ds.readDouble();
		    p2[j] = ds.readDouble();
		}

		n.data[i] = new HyperCube(new Point(p1), new Point(p2));
		n.branches[i] = ds.readInt();
	    }

	    return n;
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    protected int writeNode(AbstractNode n) throws PageFaultError {
	if (n == null) {
	    throw new IllegalArgumentException("Node cannot be null.");
	}

	try {
	    int page;
	    if (n.pageNumber < 0) {
		if (emptyPages.empty()) {
		    page = (int) ((file.length() - headerSize) / pageSize);
		} else {
		    page = ((Integer) emptyPages.pop()).intValue();
		}
		n.pageNumber = page;
	    } else {
		page = n.pageNumber;
	    }

	    ByteArrayOutputStream bs = new ByteArrayOutputStream(pageSize);
	    DataOutputStream ds = new DataOutputStream(bs);
	    ds.writeInt(n.parent);
	    ds.writeInt(n.level);
	    ds.writeInt(n.usedSpace);

	    for (int i = 0; i < tree.getNodeCapacity(); i++) {
		for (int j = 0; j < tree.getDimension(); j++) {
		    if (n.data[i] == null) {
			ds.writeDouble(Double.NaN);
			ds.writeDouble(Double.NaN);
		    } else {
			ds.writeDouble(n.data[i].getP1().getDoubleCoordinate(j));
			ds.writeDouble(n.data[i].getP2().getDoubleCoordinate(j));
		    }
		}
		ds.writeInt(n.branches[i]);
	    }
	    ds.flush();
	    bs.flush();

	    file.seek(headerSize + page * pageSize);
	    file.write(bs.toByteArray());

	    return page;
	} catch (IOException e) {
	    e.printStackTrace();
	    return -1;
	}
    }

    protected AbstractNode deletePage(int page) throws PageFaultError {
	try {
	    if (page < 0 || page > (file.length() - headerSize) / pageSize) {
		return null;
	    } else {
		AbstractNode n = readNode(page);
		file.seek(headerSize + page * pageSize);
		file.writeInt(EMPTY_PAGE);
		emptyPages.push(new Integer(page));
		return n;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }

}
