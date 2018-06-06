

















package com.infomatiq.jsi.rtree;

import gnu.trove.TIntProcedure;

import java.util.List;
import java.util.Properties;


public interface SpatialIndex {
  

  public void init(Properties props);
  

  public void add(Rectangle r, int id);
  

  public boolean delete(Rectangle r, int id);
   

  public void nearest(Point p, TIntProcedure v, double furthestDistance);
  

  public void nearestN(Point p, TIntProcedure v, int n, double distance);
  

  public void nearestNUnsorted(Point p, TIntProcedure v, int n, double distance);
  

  public List<Node> intersects(Rectangle r);  


  public void contains(Rectangle r, TIntProcedure ip); 
  

  public int size();
  
  

  public Rectangle getBounds();
  

  public String getVersion();
  
}
