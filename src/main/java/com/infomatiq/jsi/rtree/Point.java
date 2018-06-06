

















package com.infomatiq.jsi.rtree;


public class Point {

  public double x, y;
  

  public Point(double x, double y) {
    this.x = x; 
    this.y = y;
  }
  

  public void set(Point other) {
    x = other.x;
    y = other.y;
  }
  

  public String toString() {
    return "(" + x + ", " + y + ")";
  }
  

  public int xInt() {
    return (int) Math.round(x);
  }
  

  public int yInt() {
    return (int) Math.round(y);
  }
}
