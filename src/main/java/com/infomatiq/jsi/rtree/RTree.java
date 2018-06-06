


















package com.infomatiq.jsi.rtree;

import a.b.c.d.tslrm.PLASegment;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.TIntStack;
import math.geom2d.Box2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class RTree implements SpatialIndex {
  private static final Logger log = Logger.getLogger(RTree.class.getName());
  private static final Logger deleteLog = Logger.getLogger(RTree.class.getName() + "-delete");
  
  private static final String version = "1.0b8";
  

  private final static int DEFAULT_MAX_NODE_ENTRIES = 10;
  int maxNodeEntries;
  int minNodeEntries;
  



  private TIntObjectHashMap nodeMap = new TIntObjectHashMap();
  

  private final static boolean INTERNAL_CONSISTENCY_CHECKING = false;
  

  private final static int ENTRY_STATUS_ASSIGNED = 0;
  private final static int ENTRY_STATUS_UNASSIGNED = 1; 
  private byte[] entryStatus = null;
  private byte[] initialEntryStatus = null;
  



  private TIntStack parents = new TIntStack();
  private TIntStack parentsEntry = new TIntStack();
  

  private int treeHeight = 1;
  private int rootNodeId = 0;
  private int size = 0;
  

  private int highestUsedNodeId = rootNodeId; 
  



  private TIntStack deletedNodeIds = new TIntStack();
  


  private TIntArrayList nearestIds = new TIntArrayList();
  private TIntArrayList savedValues = new TIntArrayList();
  private double savedPriority = 0;


  private SortedList nearestNIds = new SortedList();
  

  private PriorityQueue distanceQueue = 
    new PriorityQueue(PriorityQueue.SORT_ORDER_ASCENDING);
  
  private List<PLASegment> segmentList;
  private PLASegment currentSeg = null;
  private Polygon2D currentPoly = null;
  

  public RTree(List<PLASegment> segmentList) {  
	this.segmentList = segmentList;
    return;
  }
  
  public RTree() {  
	    return;
	  }
  











  public void init(Properties props) {
    if (props == null) {

      maxNodeEntries = 50;
      minNodeEntries = 20;
    } else {
      maxNodeEntries = Integer.parseInt(props.getProperty("MaxNodeEntries", "0"));
      minNodeEntries = Integer.parseInt(props.getProperty("MinNodeEntries", "0"));
      



      if (maxNodeEntries < 2) { 

        maxNodeEntries = DEFAULT_MAX_NODE_ENTRIES;
      }
      

      if (minNodeEntries < 1 || minNodeEntries > maxNodeEntries / 2) {

        minNodeEntries = maxNodeEntries / 2;
      }
    }
    
    entryStatus = new byte[maxNodeEntries];  
    initialEntryStatus = new byte[maxNodeEntries];
    
    for (int i = 0; i < maxNodeEntries; i++) {
      initialEntryStatus[i] = ENTRY_STATUS_UNASSIGNED;
    }
    
    Node root = new Node(rootNodeId, 1, maxNodeEntries);
    nodeMap.put(rootNodeId, root);
    

  }
  

  public void add(Rectangle r, int id) {
    if (log.isDebugEnabled()) {

    }
    
    add(r.minX, r.minY, r.maxX, r.maxY, id, 1); 
    
    size++;
    
    if (INTERNAL_CONSISTENCY_CHECKING) {
      checkConsistency();
    }
  }
  

  public void buildRTree(){
	  int length = segmentList.size();
	  for(int i = 0; i < length; i++){
		  PLASegment tempSeg = segmentList.get(i);
		  Polygon2D tempPoly = tempSeg.getPolygonKB();
		  Box2D box = tempPoly.getBoundingBox();
		  Rectangle rect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
		  this.add(rect, i);
	  }
  }
  

  private void add(double minX, double minY, double maxX, double maxY, int id, int level) {


    Node n = chooseNode(minX, minY, maxX, maxY, level);
    Node newLeaf = null;
    



    if (n.entryCount < maxNodeEntries) {
      n.addEntry(minX, minY, maxX, maxY, id);
    } else {
      newLeaf = splitNode(n, minX, minY, maxX, maxY, id);  
    }
    


    Node newNode = adjustTree(n, newLeaf); 



    if (newNode != null) {
      int oldRootNodeId = rootNodeId;
      Node oldRoot = getNode(oldRootNodeId);
      
      rootNodeId = getNextNodeId();
      treeHeight++;
      Node root = new Node(rootNodeId, treeHeight, maxNodeEntries);
      root.addEntry(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY, newNode.nodeId);
      root.addEntry(oldRoot.mbrMinX, oldRoot.mbrMinY, oldRoot.mbrMaxX, oldRoot.mbrMaxY, oldRoot.nodeId);
      nodeMap.put(rootNodeId, root);
    }    
  } 
  

  public boolean delete(Rectangle r, int id) {






    




  	parents.reset();
  	parents.push(rootNodeId);
  	
  	parentsEntry.reset();
  	parentsEntry.push(-1);
  	Node n = null;
  	int foundIndex = -1;
  	
  	while (foundIndex == -1 && parents.size() > 0) {
  	  n = getNode(parents.peek());
  	  int startIndex = parentsEntry.peek() + 1;
      
      if (!n.isLeaf()) {

	  	  boolean contains = false;
        for (int i = startIndex; i < n.entryCount; i++) {
	  	    if (Rectangle.contains(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i],
                                 r.minX, r.minY, r.maxX, r.maxY)) { 
	  	      parents.push(n.ids[i]);
	  	      parentsEntry.pop();
	  	      parentsEntry.push(i);
	  	      parentsEntry.push(-1);
	  	      contains = true;
            break;
	  	    }
	  	  }
        if (contains) {
          continue;
        }
      } else {
        foundIndex = n.findEntry(r.minX, r.minY, r.maxX, r.maxY, id);        
      }
      
      parents.pop();
      parentsEntry.pop();
  	}
  	
  	if (foundIndex != -1) {
  	  n.deleteEntry(foundIndex);
      condenseTree(n);
      size--;
  	}
  	


    Node root = getNode(rootNodeId);
    while (root.entryCount == 1 && treeHeight > 1)
    {
        deletedNodeIds.push(rootNodeId);
        root.entryCount = 0;
        rootNodeId = root.ids[0];
        treeHeight--;
        root = getNode(rootNodeId);
    }
    



    if (size == 0) {
      root.mbrMinX = Double.MAX_VALUE;
      root.mbrMinY = Double.MAX_VALUE;
      root.mbrMaxX = -Double.MAX_VALUE;
      root.mbrMaxY = -Double.MAX_VALUE;
    }

    if (INTERNAL_CONSISTENCY_CHECKING) {
      checkConsistency();
    }
        
    return (foundIndex != -1);
  }
  

  public void nearest(Point p, TIntProcedure v, double furthestDistance) {
    Node rootNode = getNode(rootNodeId);
   
    double furthestDistanceSq = furthestDistance * furthestDistance;
    nearest(p, rootNode, furthestDistanceSq);
   
    nearestIds.forEach(v);
    nearestIds.reset();
  }
   
  private void createNearestNDistanceQueue(Point p, int count, double furthestDistance) {
    distanceQueue.reset();
    distanceQueue.setSortOrder(PriorityQueue.SORT_ORDER_DESCENDING);
    

    if (count <= 0) {
      return;
    }    
    
    parents.reset();
    parents.push(rootNodeId);
    
    parentsEntry.reset();
    parentsEntry.push(-1);
    


    
    double furthestDistanceSq = furthestDistance * furthestDistance;
    
    while (parents.size() > 0) {
      Node n = getNode(parents.peek());
      int startIndex = parentsEntry.peek() + 1;
      
      if (!n.isLeaf()) {



        boolean near = false;
        for (int i = startIndex; i < n.entryCount; i++) {
          if (Rectangle.distanceSq(n.entriesMinX[i], n.entriesMinY[i], 
                                 n.entriesMaxX[i], n.entriesMaxY[i], 
                                 p.x, p.y) <= furthestDistanceSq) {
            parents.push(n.ids[i]);
            parentsEntry.pop();
            parentsEntry.push(i);
            parentsEntry.push(-1);
            near = true;
            break;
          }
        }
        if (near) {
          continue;
        }
      } else {


        for (int i = 0; i < n.entryCount; i++) {
          double entryDistanceSq = Rectangle.distanceSq(n.entriesMinX[i], n.entriesMinY[i],
                                                   n.entriesMaxX[i], n.entriesMaxY[i],
                                                   p.x, p.y);
          int entryId = n.ids[i];
          
          if (entryDistanceSq <= furthestDistanceSq) {
            distanceQueue.insert(entryId, entryDistanceSq);
            
            while (distanceQueue.size() > count) {

              int value = distanceQueue.getValue();
              double distanceSq = distanceQueue.getPriority();
              distanceQueue.pop();
              

              if (distanceSq == distanceQueue.getPriority()) {
                savedValues.add(value);
                savedPriority = distanceSq;
              } else {
                savedValues.reset();
              }
            }
            


            if (savedValues.size() > 0 && savedPriority == distanceQueue.getPriority()) {
              for (int svi = 0; svi < savedValues.size(); svi++) {
                distanceQueue.insert(savedValues.get(svi), savedPriority);
              }
              savedValues.reset();
            }
            

            if (distanceQueue.getPriority() < furthestDistanceSq && distanceQueue.size() >= count) {
              furthestDistanceSq = distanceQueue.getPriority();  
            }
          } 
        }                       
      }
      parents.pop();
      parentsEntry.pop();  
    }
  }
  

  public void nearestNUnsorted(Point p, TIntProcedure v, int count, double furthestDistance) {











    createNearestNDistanceQueue(p, count, furthestDistance);
   
    while (distanceQueue.size() > 0) {
      v.execute(distanceQueue.getValue());
      distanceQueue.pop();
    }
  }
  

  public void nearestN(Point p, TIntProcedure v, int count, double furthestDistance) {
    createNearestNDistanceQueue(p, count, furthestDistance);
    
    distanceQueue.setSortOrder(PriorityQueue.SORT_ORDER_ASCENDING);
    
    while (distanceQueue.size() > 0) {
      v.execute(distanceQueue.getValue());
      distanceQueue.pop();
    }  
  }
    

  public void nearestN_orig(Point p, TIntProcedure v, int count, double furthestDistance) {

    if (count <= 0) {
      return;
    }
    
    parents.reset();
    parents.push(rootNodeId);
    
    parentsEntry.reset();
    parentsEntry.push(-1);
    
    nearestNIds.init(count);
    


    
    double furthestDistanceSq = furthestDistance * furthestDistance;
    
    while (parents.size() > 0) {
      Node n = getNode(parents.peek());
      int startIndex = parentsEntry.peek() + 1;
      
      if (!n.isLeaf()) {



        boolean near = false;
        for (int i = startIndex; i < n.entryCount; i++) {
          if (Rectangle.distanceSq(n.entriesMinX[i], n.entriesMinY[i], 
                                 n.entriesMaxX[i], n.entriesMaxY[i], 
                                 p.x, p.y) <= furthestDistanceSq) {
            parents.push(n.ids[i]);
            parentsEntry.pop();
            parentsEntry.push(i);
            parentsEntry.push(-1);
            near = true;
            break;
          }
        }
        if (near) {
          continue;
        }
      } else {


        for (int i = 0; i < n.entryCount; i++) {
          double entryDistanceSq = Rectangle.distanceSq(n.entriesMinX[i], n.entriesMinY[i],
                                                   n.entriesMaxX[i], n.entriesMaxY[i],
                                                   p.x, p.y);
          int entryId = n.ids[i];
          
          if (entryDistanceSq <= furthestDistanceSq) {

            nearestNIds.add(entryId, -entryDistanceSq);
            
            double tempFurthestDistanceSq = -nearestNIds.getLowestPriority();
            if (tempFurthestDistanceSq < furthestDistanceSq) {
              furthestDistanceSq = tempFurthestDistanceSq;  
            }
          } 
        }                       
      }
      parents.pop();
      parentsEntry.pop();  
    }
   
    nearestNIds.forEachId(v);
  }
   

  public List intersects(Rectangle r) {
    Node rootNode = getNode(rootNodeId);
    List leafs = new ArrayList();

    return leafs;
  }

  public int calUpperBound(PLASegment probeSeg, int currentIdx){
	  this.currentSeg = probeSeg;
	  this.currentPoly = probeSeg.currentPolygon;
	  Box2D box = currentPoly.getBoundingBox();
	  Node rootNode = getNode(rootNodeId);
	  Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
	  return intersects(probeRect, rootNode, currentIdx);
  }

  public List<PLASegment> calUpBound(Polygon2D currentPoly){
	  List<PLASegment> segments = new ArrayList<PLASegment>();
	  this.currentPoly = currentPoly;
	  Box2D box = currentPoly.getBoundingBox();
	  Node rootNode = getNode(rootNodeId);
	  Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
	  intersects(probeRect, rootNode, segments);
	  return segments;
  }
  
  public void intersects(Rectangle r, Node n, List<PLASegment> segments){
	  if(n.isLeaf()){
		  for(int i = 0; i < n.entryCount; i++){
			  PLASegment tempSeg = segmentList.get(n.ids[i]);
			  if(Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Polygon2D tempPoly = tempSeg.getPolygonKB();
				  Polygon2D intersection = Polygon2DUtils.intersection(tempPoly, this.currentPoly);
				  if(intersection.getVertexNumber() > 0){
					  segments.add(tempSeg);
				  }
			  }
		  }
	  }else{
		  for(int i = 0; i < n.entryCount; i++){
			  if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Node childNode = getNode(n.ids[i]);
				  intersects(r, childNode, segments);
			  }
		  }
	  }
  }

  public int initCalUpperBound(PLASegment probeSeg){
	  this.currentSeg = probeSeg;
	  this.currentPoly = probeSeg.getPolygonKB();
	  Box2D box = currentPoly.getBoundingBox();
	  Node rootNode = getNode(rootNodeId);
	  Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
	  return intersects(probeRect, rootNode);
  }

  public int intersects(Rectangle r, Node n, int currentIdx){
	  int upperBound = 0;
	  if (n.isLeaf()){
		  for(int i = 0; i < n.entryCount; i++){
			  PLASegment tempSeg = segmentList.get(n.ids[i]);
			 if((tempSeg.idx > currentIdx) && 
					 Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
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
		  for(int i = 0; i < n.entryCount; i++){
			  if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Node childNode = getNode(n.ids[i]);
				  upperBound += intersects(r, childNode, currentIdx);
			  }
		  }
	  }
	  
	  return upperBound;
  }

  public int intersects(Rectangle r, Node n){
	  int upperBound = 0;
	  if (n.isLeaf()){
		  for(int i = 0; i < n.entryCount; i++){
			  PLASegment tempSeg = segmentList.get(n.ids[i]);
			  if(Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Polygon2D tempPoly = tempSeg.getPolygonKB();
				 	Polygon2D intersection = Polygon2DUtils.intersection(this.currentPoly, tempPoly);
				 	if(intersection.getVertexNumber() > 0){
				 		if ((tempSeg.getStart() > currentSeg.getEnd()) && (tempSeg.getEnd() < currentSeg.getStart())) {
				 			upperBound += tempSeg.getLength();
			            }else{
			            	if(tempSeg.getEnd() > currentSeg.getEnd())
			            		upperBound += tempSeg.getEnd() - currentSeg.getEnd();
			            	else
			            		upperBound += currentSeg.getStart() - tempSeg.getStart();
			            }
				 	}
			  }
		  }
	  }else{
		  for(int i = 0; i < n.entryCount; i++){
			  if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Node childNode = getNode(n.ids[i]);
				  upperBound += intersects(r, childNode);
			  }
		  }
	  }
	  return upperBound;
  }

  public void initMatrix(boolean[][] matrix){
	  int length = this.segmentList.size();
	  for(int i = 0; i < length; i++){
		  matrix[i][i] = true;
		  PLASegment probeSegment = segmentList.get(i);
		  Polygon2D probePoly = probeSegment.getPolygonKB();
		  Box2D box = probePoly.getBoundingBox();
		  Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
		  Node rootNode = getNode(rootNodeId);
		  intersects(probeRect, probePoly, matrix, rootNode, i);
	  }
  }
  
  public void initMatrix2(boolean[][] matrix){
	  int length = this.segmentList.size();
	  for(int i = 0; i < length; i++){
		  matrix[i][i] = true;
		  PLASegment probeSegment = segmentList.get(i);
		  Polygon2D probePoly = probeSegment.getPolygonKB();
		  Box2D box = probePoly.getBoundingBox();
		  Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
		  Node rootNode = getNode(rootNodeId);
		  intersects2(probeRect, probePoly, matrix, rootNode, i);
	  }
  }

  public void contains(Rectangle r, TIntProcedure v) {


        
    parents.reset();
    parents.push(rootNodeId);
    
    parentsEntry.reset();
    parentsEntry.push(-1);
    


    
    while (parents.size() > 0) {
      Node n = getNode(parents.peek());
      int startIndex = parentsEntry.peek() + 1;
      
      if (!n.isLeaf()) {



        boolean intersects = false;
        for (int i = startIndex; i < n.entryCount; i++) {
          if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, 
                                   n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])) {
            parents.push(n.ids[i]);
            parentsEntry.pop();
            parentsEntry.push(i);
            parentsEntry.push(-1);
            intersects = true;
            break;
          }
        }
        if (intersects) {
          continue;
        }
      } else {


        for (int i = 0; i < n.entryCount; i++) {
          if (Rectangle.contains(r.minX, r.minY, r.maxX, r.maxY, 
                                 n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])) {
            if (!v.execute(n.ids[i])) {
              return;
            }
          } 
        }                       
      }
      parents.pop();
      parentsEntry.pop();  
    }
  }


  public int size() {
    return size;
  }


  public Rectangle getBounds() {
    Rectangle bounds = null;
    
    Node n = getNode(getRootNodeId());
    if (n != null && n.entryCount > 0) {
      bounds = new Rectangle();
      bounds.minX = n.mbrMinX;
      bounds.minY = n.mbrMinY;
      bounds.maxX = n.mbrMaxX;
      bounds.maxY = n.mbrMaxY;
    }
    return bounds;
  }
    

  public String getVersion() {
    return "RTree-" + version;
  }



  

  private int getNextNodeId() {
    int nextNodeId = 0;
    if (deletedNodeIds.size() > 0) {
      nextNodeId = deletedNodeIds.pop();
    } else {
      nextNodeId = 1 + highestUsedNodeId++;
    }
    return nextNodeId;
  }


  public Node getNode(int id) {
    return (Node) nodeMap.get(id);
  }


  public int getHighestUsedNodeId() {
    return highestUsedNodeId;
  }


  public int getRootNodeId() {
    return rootNodeId; 
  }
      

  private Node splitNode(Node n, double newRectMinX, double newRectMinY, double newRectMaxX, double newRectMaxY, int newId) {



    

    double initialArea = 0;
    if (log.isDebugEnabled()) {






    }
       
    System.arraycopy(initialEntryStatus, 0, entryStatus, 0, maxNodeEntries);
    
    Node newNode = null;
    newNode = new Node(getNextNodeId(), n.level, maxNodeEntries);
    nodeMap.put(newNode.nodeId, newNode);
    
    pickSeeds(n, newRectMinX, newRectMinY, newRectMaxX, newRectMaxY, newId, newNode);
    



    while (n.entryCount + newNode.entryCount < maxNodeEntries + 1) {
      if (maxNodeEntries + 1 - newNode.entryCount == minNodeEntries) {

        for (int i = 0; i < maxNodeEntries; i++) {
          if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
            entryStatus[i] = ENTRY_STATUS_ASSIGNED;
            
            if (n.entriesMinX[i] < n.mbrMinX) n.mbrMinX = n.entriesMinX[i];
            if (n.entriesMinY[i] < n.mbrMinY) n.mbrMinY = n.entriesMinY[i];
            if (n.entriesMaxX[i] > n.mbrMaxX) n.mbrMaxX = n.entriesMaxX[i];
            if (n.entriesMaxY[i] > n.mbrMaxY) n.mbrMaxY = n.entriesMaxY[i];
            
            n.entryCount++;
          }
        }
        break;
      }   
      if (maxNodeEntries + 1 - n.entryCount == minNodeEntries) {

        for (int i = 0; i < maxNodeEntries; i++) {
          if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
            entryStatus[i] = ENTRY_STATUS_ASSIGNED;
            newNode.addEntry(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i], n.ids[i]);
            n.ids[i] = -1;
          }
        }
        break;
      }
      





      pickNext(n, newNode);   
    }
      
    n.reorganize(this);
    

    if (INTERNAL_CONSISTENCY_CHECKING) {
      Rectangle nMBR = new Rectangle(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY);
      if (!nMBR.equals(calculateMBR(n))) {

      }
      Rectangle newNodeMBR = new Rectangle(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY);
      if (!newNodeMBR.equals(calculateMBR(newNode))) {

      }
    }
    

    if (log.isDebugEnabled()) {




    }
      
    return newNode;
  }
  

  private void pickSeeds(Node n, double newRectMinX, double newRectMinY, double newRectMaxX, double newRectMaxY, int newId, Node newNode) {



    double maxNormalizedSeparation = -1;
    int highestLowIndex = -1;
    int lowestHighIndex = -1;
    


    if (newRectMinX < n.mbrMinX) n.mbrMinX = newRectMinX;
    if (newRectMinY < n.mbrMinY) n.mbrMinY = newRectMinY;
    if (newRectMaxX > n.mbrMaxX) n.mbrMaxX = newRectMaxX;
    if (newRectMaxY > n.mbrMaxY) n.mbrMaxY = newRectMaxY;
    
    double mbrLenX = n.mbrMaxX - n.mbrMinX;
    double mbrLenY = n.mbrMaxY - n.mbrMinY;
    
    if (log.isDebugEnabled()) {

    }
    
    double tempHighestLow = newRectMinX;
    int tempHighestLowIndex = -1;
    
    double tempLowestHigh = newRectMaxX;
    int tempLowestHighIndex = -1;
    
    for (int i = 0; i < n.entryCount; i++) {
      double tempLow = n.entriesMinX[i];
      if (tempLow >= tempHighestLow) {
         tempHighestLow = tempLow;
         tempHighestLowIndex = i;
      } else {
        double tempHigh = n.entriesMaxX[i];
        if (tempHigh <= tempLowestHigh) {
          tempLowestHigh = tempHigh;
          tempLowestHighIndex = i;
        }
      }
      



      double normalizedSeparation = mbrLenX == 0 ? 1 : (tempHighestLow - tempLowestHigh) / mbrLenX;
      if (normalizedSeparation > 1 || normalizedSeparation < -1) {

      }
      
      if (log.isDebugEnabled()) {



      }
          




      if (normalizedSeparation >= maxNormalizedSeparation) {
        highestLowIndex = tempHighestLowIndex;
        lowestHighIndex = tempLowestHighIndex;
        maxNormalizedSeparation = normalizedSeparation;
      }
    }
    

    tempHighestLow = newRectMinY;
    tempHighestLowIndex = -1;
    
    tempLowestHigh = newRectMaxY;
    tempLowestHighIndex = -1;
    
    for (int i = 0; i < n.entryCount; i++) {
      double tempLow = n.entriesMinY[i];
      if (tempLow >= tempHighestLow) {
         tempHighestLow = tempLow;
         tempHighestLowIndex = i;
      } else {
        double tempHigh = n.entriesMaxY[i];
        if (tempHigh <= tempLowestHigh) {
          tempLowestHigh = tempHigh;
          tempLowestHighIndex = i;
        }
      }
      



      double normalizedSeparation = mbrLenY == 0 ? 1 : (tempHighestLow - tempLowestHigh) / mbrLenY;
      if (normalizedSeparation > 1 || normalizedSeparation < -1) {

      }
      
      if (log.isDebugEnabled()) {



      }
          




      if (normalizedSeparation >= maxNormalizedSeparation) {
        highestLowIndex = tempHighestLowIndex;
        lowestHighIndex = tempLowestHighIndex;
        maxNormalizedSeparation = normalizedSeparation;
      }
    }
    




    if (highestLowIndex == lowestHighIndex) { 
      highestLowIndex = -1;
      double tempMinY = newRectMinY;
      lowestHighIndex = 0;
      double tempMaxX = n.entriesMaxX[0];
      
      for (int i = 1; i < n.entryCount; i++) {
        if (n.entriesMinY[i] < tempMinY) {
          tempMinY = n.entriesMinY[i];
          highestLowIndex = i;
        }
        else if (n.entriesMaxX[i] > tempMaxX) {
          tempMaxX = n.entriesMaxX[i];
          lowestHighIndex = i;
        }
      }
    }
    

    if (highestLowIndex == -1) {
      newNode.addEntry(newRectMinX, newRectMinY, newRectMaxX, newRectMaxY, newId);
    } else {
      newNode.addEntry(n.entriesMinX[highestLowIndex], n.entriesMinY[highestLowIndex], 
                       n.entriesMaxX[highestLowIndex], n.entriesMaxY[highestLowIndex], 
                       n.ids[highestLowIndex]);
      n.ids[highestLowIndex] = -1;
      

      n.entriesMinX[highestLowIndex] = newRectMinX;
      n.entriesMinY[highestLowIndex] = newRectMinY;
      n.entriesMaxX[highestLowIndex] = newRectMaxX;
      n.entriesMaxY[highestLowIndex] = newRectMaxY;
      
      n.ids[highestLowIndex] = newId;
    }
    

    if (lowestHighIndex == -1) {
      lowestHighIndex = highestLowIndex;
    }
    
    entryStatus[lowestHighIndex] = ENTRY_STATUS_ASSIGNED;
    n.entryCount = 1;
    n.mbrMinX = n.entriesMinX[lowestHighIndex];
    n.mbrMinY = n.entriesMinY[lowestHighIndex];
    n.mbrMaxX = n.entriesMaxX[lowestHighIndex];
    n.mbrMaxY = n.entriesMaxY[lowestHighIndex];
  }


  private int pickNext(Node n, Node newNode) {
    double maxDifference = Double.NEGATIVE_INFINITY;
    int next = 0;
    int nextGroup = 0;
    
    maxDifference = Double.NEGATIVE_INFINITY;
   
    if (log.isDebugEnabled()) {

    }
   
    for (int i = 0; i < maxNodeEntries; i++) {
      if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED) {
        
        if (n.ids[i] == -1) {

        }
        
        double nIncrease = Rectangle.enlargement(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY, 
                                                n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);
        double newNodeIncrease = Rectangle.enlargement(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY,
                                                      n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]);

        double difference = Math.abs(nIncrease - newNodeIncrease);
         
        if (difference > maxDifference) {
          next = i;
          
          if (nIncrease < newNodeIncrease) {
            nextGroup = 0; 
          } else if (newNodeIncrease < nIncrease) {
            nextGroup = 1;
          } else if (Rectangle.area(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY) < Rectangle.area(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY)) {
            nextGroup = 0;
          } else if (Rectangle.area(newNode.mbrMinX, newNode.mbrMinY, newNode.mbrMaxX, newNode.mbrMaxY) < Rectangle.area(n.mbrMinX, n.mbrMinY, n.mbrMaxX, n.mbrMaxY)) {
            nextGroup = 1;
          } else if (newNode.entryCount < maxNodeEntries / 2) {
            nextGroup = 0;
          } else {
            nextGroup = 1;
          }
          maxDifference = difference; 
        }
        if (log.isDebugEnabled()) {


        }
      }
    }
    
    entryStatus[next] = ENTRY_STATUS_ASSIGNED;
      
    if (nextGroup == 0) {
      if (n.entriesMinX[next] < n.mbrMinX) n.mbrMinX = n.entriesMinX[next];
      if (n.entriesMinY[next] < n.mbrMinY) n.mbrMinY = n.entriesMinY[next];
      if (n.entriesMaxX[next] > n.mbrMaxX) n.mbrMaxX = n.entriesMaxX[next];
      if (n.entriesMaxY[next] > n.mbrMaxY) n.mbrMaxY = n.entriesMaxY[next];
      n.entryCount++;
    } else {

      newNode.addEntry(n.entriesMinX[next], n.entriesMinY[next], n.entriesMaxX[next], n.entriesMaxY[next], n.ids[next]);
      n.ids[next] = -1;
    }
    
    return next; 
  }


  private double nearest(Point p, Node n, double furthestDistanceSq) {
    for (int i = 0; i < n.entryCount; i++) {
      double tempDistanceSq = Rectangle.distanceSq(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i], p.x, p.y);
      if (n.isLeaf()) {
        if (tempDistanceSq < furthestDistanceSq) {
          furthestDistanceSq = tempDistanceSq;
          nearestIds.reset();
        }
        if (tempDistanceSq <= furthestDistanceSq) {
          nearestIds.add(n.ids[i]);
        }     
      } else {

         if (tempDistanceSq <= furthestDistanceSq) {

           furthestDistanceSq = nearest(p, getNode(n.ids[i]), furthestDistanceSq);
         }
      }
    }
    return furthestDistanceSq;
  }
  

  private void intersects(Rectangle r, Polygon2D currentPoly, boolean[][] matrix, Node n, int start) {
	  if(n.isLeaf()){
		  for(int i = 0; i < n.entryCount; i ++){
			  PLASegment tempSeg = segmentList.get(n.ids[i]);
			  if((tempSeg.idx > start) && 
					  Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Polygon2D tempPoly = tempSeg.getPolygonKB();
				  Polygon2D intersection = Polygon2DUtils.intersection(currentPoly, tempPoly);
				  if(intersection.getVertexNumber() > 0){
					  matrix[start][tempSeg.idx] = true;
					  matrix[tempSeg.idx][start] = true;
				  }
			  }			  
		  }
	  }else{
		  for(int i = 0; i < n.entryCount; i++){
			  if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Node childNode = getNode(n.ids[i]);
		          intersects(r, currentPoly, matrix, childNode, start);
			  }
		  }
	  }
  }
  
  private void intersects2(Rectangle r, Polygon2D currentPoly, boolean[][] matrix, Node n, int start) {
	  if(n.isLeaf()){
		  for(int i = 0; i < n.entryCount; i ++){
			  PLASegment tempSeg = segmentList.get(n.ids[i]);
			  if((tempSeg.index > start) && 
					  Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Polygon2D tempPoly = tempSeg.getPolygonKB();
				  Polygon2D intersection = Polygon2DUtils.intersection(currentPoly, tempPoly);
				  if(intersection.getVertexNumber() > 0){
					  matrix[start][tempSeg.index] = true;
					  matrix[tempSeg.index][start] = true;
				  }
			  }			  
		  }
	  }else{
		  for(int i = 0; i < n.entryCount; i++){
			  if (Rectangle.intersects(r.minX, r.minY, r.maxX, r.maxY, n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i])){
				  Node childNode = getNode(n.ids[i]);
		          intersects2(r, currentPoly, matrix, childNode, start);
			  }
		  }
	  }
  }


  private void condenseTree(Node l) {


    Node n = l;
    Node parent = null;
    int parentEntry = 0;
    
    TIntStack eliminatedNodeIds = new TIntStack();
  


    while (n.level != treeHeight) {
      parent = getNode(parents.pop());
      parentEntry = parentsEntry.pop();
      


      if (n.entryCount < minNodeEntries) {
        parent.deleteEntry(parentEntry);
        eliminatedNodeIds.push(n.nodeId);
      } else {


        if (n.mbrMinX != parent.entriesMinX[parentEntry] ||
            n.mbrMinY != parent.entriesMinY[parentEntry] ||
            n.mbrMaxX != parent.entriesMaxX[parentEntry] ||
            n.mbrMaxY != parent.entriesMaxY[parentEntry]) {
          double deletedMinX = parent.entriesMinX[parentEntry];
          double deletedMinY = parent.entriesMinY[parentEntry];
          double deletedMaxX = parent.entriesMaxX[parentEntry];
          double deletedMaxY = parent.entriesMaxY[parentEntry];
          parent.entriesMinX[parentEntry] = n.mbrMinX;
          parent.entriesMinY[parentEntry] = n.mbrMinY;
          parent.entriesMaxX[parentEntry] = n.mbrMaxX;
          parent.entriesMaxY[parentEntry] = n.mbrMaxY;
          parent.recalculateMBRIfInfluencedBy(deletedMinX, deletedMinY, deletedMaxX, deletedMaxY);
        }
      }

      n = parent;
    }
    





    while (eliminatedNodeIds.size() > 0) {
      Node e = getNode(eliminatedNodeIds.pop());
      for (int j = 0; j < e.entryCount; j++) {
        add(e.entriesMinX[j], e.entriesMinY[j], e.entriesMaxX[j], e.entriesMaxY[j], e.ids[j], e.level); 
        e.ids[j] = -1;
      }
      e.entryCount = 0;
      deletedNodeIds.push(e.nodeId);
    }
  }


  private Node chooseNode(double minX, double minY, double maxX, double maxY, int level) {

    Node n = getNode(rootNodeId);
    parents.reset();
    parentsEntry.reset();
     

    while (true) {
      if (n == null) {

      }
   
      if (n.level == level) {
        return n;
      }
      



      double leastEnlargement = Rectangle.enlargement(n.entriesMinX[0], n.entriesMinY[0], n.entriesMaxX[0], n.entriesMaxY[0],
                                                     minX, minY, maxX, maxY);
      int index = 0;
      for (int i = 1; i < n.entryCount; i++) {
        double tempMinX = n.entriesMinX[i];
        double tempMinY = n.entriesMinY[i];
        double tempMaxX = n.entriesMaxX[i];
        double tempMaxY = n.entriesMaxY[i];
        double tempEnlargement = Rectangle.enlargement(tempMinX, tempMinY, tempMaxX, tempMaxY, 
                                                      minX, minY, maxX, maxY);
        if ((tempEnlargement < leastEnlargement) ||
            ((tempEnlargement == leastEnlargement) && 
             (Rectangle.area(tempMinX, tempMinY, tempMaxX, tempMaxY) < 
              Rectangle.area(n.entriesMinX[index], n.entriesMinY[index], n.entriesMaxX[index], n.entriesMaxY[index])))) {
          index = i;
          leastEnlargement = tempEnlargement;
        }
      }
      
      parents.push(n.nodeId);
      parentsEntry.push(index);
    


      n = getNode(n.ids[index]);
    }
  }
  

  private Node adjustTree(Node n, Node nn) {


    

    while (n.level != treeHeight) {
    



      Node parent = getNode(parents.pop());
      int entry = parentsEntry.pop(); 
      
      if (parent.ids[entry] != n.nodeId) {



      }
   
      if (parent.entriesMinX[entry] != n.mbrMinX ||
          parent.entriesMinY[entry] != n.mbrMinY ||
          parent.entriesMaxX[entry] != n.mbrMaxX ||
          parent.entriesMaxY[entry] != n.mbrMaxY) {
   
        parent.entriesMinX[entry] = n.mbrMinX;
        parent.entriesMinY[entry] = n.mbrMinY;
        parent.entriesMaxX[entry] = n.mbrMaxX;
        parent.entriesMaxY[entry] = n.mbrMaxY;

        parent.recalculateMBR();
      }
      





      Node newNode = null;
      if (nn != null) {
        if (parent.entryCount < maxNodeEntries) {
          parent.addEntry(nn.mbrMinX, nn.mbrMinY, nn.mbrMaxX, nn.mbrMaxY, nn.nodeId);
        } else {
          newNode = splitNode(parent, nn.mbrMinX, nn.mbrMinY, nn.mbrMaxX, nn.mbrMaxY, nn.nodeId);
        }
      }
      


      n = parent;
      nn = newNode;
      
      parent = null;
      newNode = null;
    }
    
    return nn;
  }
  
  

  public boolean checkConsistency() {
    return checkConsistency(rootNodeId, treeHeight, null);
  }
  
  private boolean checkConsistency(int nodeId, int expectedLevel, Rectangle expectedMBR) {


    Node n = getNode(nodeId);
    
    if (n == null) {

      return false;
    }
    


    if (nodeId == rootNodeId && size() == 0) {
      if (n.level != 1) {

        return false;
      }
    }
    
    if (n.level != expectedLevel) {

      return false;
    }
    
    Rectangle calculatedMBR = calculateMBR(n);
    Rectangle actualMBR = new Rectangle();
    actualMBR.minX = n.mbrMinX;
    actualMBR.minY = n.mbrMinY;
    actualMBR.maxX = n.mbrMaxX;
    actualMBR.maxY = n.mbrMaxY;
    if (!actualMBR.equals(calculatedMBR)) {





      return false;
    }
    
    if (expectedMBR != null && !actualMBR.equals(expectedMBR)) {

      return false;
    }
    

    if (expectedMBR != null && actualMBR.sameObject(expectedMBR)) {

      return false;
    }
    
    for (int i = 0; i < n.entryCount; i++) {
      if (n.ids[i] == -1) {

        return false;
      }     
      
      if (n.level > 1) {
        if (!checkConsistency(n.ids[i], n.level - 1, new Rectangle(n.entriesMinX[i], n.entriesMinY[i], n.entriesMaxX[i], n.entriesMaxY[i]))) {
          return false;
        }
      }   
    }
    return true;
  }
  

  private Rectangle calculateMBR(Node n) {
    Rectangle mbr = new Rectangle();
   
    for (int i = 0; i < n.entryCount; i++) {
      if (n.entriesMinX[i] < mbr.minX) mbr.minX = n.entriesMinX[i];
      if (n.entriesMinY[i] < mbr.minY) mbr.minY = n.entriesMinY[i];
      if (n.entriesMaxX[i] > mbr.maxX) mbr.maxX = n.entriesMaxX[i];
      if (n.entriesMaxY[i] > mbr.maxY) mbr.maxY = n.entriesMaxY[i];
    }
    return mbr; 
  }
}
