package rtree;


public interface Node {






    public AbstractNode getParent();

    public HyperCube[] getHyperCubes();

    public int getLevel();

    public HyperCube getNodeMbb();
    
    public String getUniqueId();

    public boolean isLeaf();

    public boolean isRoot();

    public boolean isIndex();

    public String toString();

}
