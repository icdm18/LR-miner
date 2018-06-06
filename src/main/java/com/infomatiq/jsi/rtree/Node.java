


















package com.infomatiq.jsi.rtree;


public class Node {
  int nodeId = 0;
  double mbrMinX = Double.MAX_VALUE;
  double mbrMinY = Double.MAX_VALUE;
  double mbrMaxX = -Double.MAX_VALUE;
  double mbrMaxY = -Double.MAX_VALUE;
  
  double[] entriesMinX = null;
  double[] entriesMinY = null;
  double[] entriesMaxX = null;
  double[] entriesMaxY = null;
  
  int[] ids = null;
  int level;
  int entryCount;

  Node(int nodeId, int level, int maxNodeEntries) {
    this.nodeId = nodeId;
    this.level = level;
    entriesMinX = new double[maxNodeEntries];
    entriesMinY = new double[maxNodeEntries];
    entriesMaxX = new double[maxNodeEntries];
    entriesMaxY = new double[maxNodeEntries];
    ids = new int[maxNodeEntries];
  }
   
  void addEntry(double minX, double minY, double maxX, double maxY, int id) {
    ids[entryCount] = id;
    entriesMinX[entryCount] = minX;
    entriesMinY[entryCount] = minY;
    entriesMaxX[entryCount] = maxX;
    entriesMaxY[entryCount] = maxY;
   
    if (minX < mbrMinX) mbrMinX = minX;
    if (minY < mbrMinY) mbrMinY = minY;
    if (maxX > mbrMaxX) mbrMaxX = maxX;
    if (maxY > mbrMaxY) mbrMaxY = maxY;
    
    entryCount++;
  }
  

  int findEntry(double minX, double minY, double maxX, double maxY, int id) {
    for (int i = 0; i < entryCount; i++) {
    	if (id == ids[i] && 
          entriesMinX[i] == minX && entriesMinY[i] == minY &&
          entriesMaxX[i] == maxX && entriesMaxY[i] == maxY) {
    	  return i;	
    	}
    }
    return -1;
  }
  

  void deleteEntry(int i) {
	  int lastIndex = entryCount - 1;
	double deletedMinX = entriesMinX[i];
    double deletedMinY = entriesMinY[i];
    double deletedMaxX = entriesMaxX[i];
    double deletedMaxY = entriesMaxY[i];
    
    if (i != lastIndex) {
      entriesMinX[i] = entriesMinX[lastIndex];
      entriesMinY[i] = entriesMinY[lastIndex];
      entriesMaxX[i] = entriesMaxX[lastIndex];
      entriesMaxY[i] = entriesMaxY[lastIndex];
    	ids[i] = ids[lastIndex];
	  }
    entryCount--;
    

    recalculateMBRIfInfluencedBy(deletedMinX, deletedMinY, deletedMaxX, deletedMaxY);
  } 
  


  void recalculateMBRIfInfluencedBy(double deletedMinX, double deletedMinY, double deletedMaxX, double deletedMaxY) {
    if (mbrMinX == deletedMinX || mbrMinY == deletedMinY || mbrMaxX == deletedMaxX || mbrMaxY == deletedMaxY) { 
      recalculateMBR();   
    }
  }
   
  void recalculateMBR() {
    mbrMinX = entriesMinX[0];
    mbrMinY = entriesMinY[0];
    mbrMaxX = entriesMaxX[0];
    mbrMaxY = entriesMaxY[0];

    for (int i = 1; i < entryCount; i++) {
      if (entriesMinX[i] < mbrMinX) mbrMinX = entriesMinX[i];
      if (entriesMinY[i] < mbrMinY) mbrMinY = entriesMinY[i];
      if (entriesMaxX[i] > mbrMaxX) mbrMaxX = entriesMaxX[i];
      if (entriesMaxY[i] > mbrMaxY) mbrMaxY = entriesMaxY[i];
    }
  }
    

  void reorganize(RTree rtree) {
    int countdownIndex = rtree.maxNodeEntries - 1; 
    for (int index = 0; index < entryCount; index++) {
      if (ids[index] == -1) {
         while (ids[countdownIndex] == -1 && countdownIndex > index) {
           countdownIndex--;
         }
         entriesMinX[index] = entriesMinX[countdownIndex];
         entriesMinY[index] = entriesMinY[countdownIndex];
         entriesMaxX[index] = entriesMaxX[countdownIndex];
         entriesMaxY[index] = entriesMaxY[countdownIndex];
         ids[index] = ids[countdownIndex];    
         ids[countdownIndex] = -1;
      }
    }
  }
  
  public int getEntryCount() {
    return entryCount;
  }
 
  public int getId(int index) {
    if (index < entryCount) {
      return ids[index];
    }
    return -1;
  }
  
  boolean isLeaf() {
    return (level == 1);
  }
  
  public int getLevel() {
    return level; 
  }
}
