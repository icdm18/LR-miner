

















package com.infomatiq.jsi.rtree;




public class Rectangle {
  

  public double minX, minY, maxX, maxY;
  
  public Rectangle() {
    minX = Double.MAX_VALUE;
    minY = Double.MAX_VALUE;
    maxX = -Double.MAX_VALUE;
    maxY = -Double.MAX_VALUE;
  }
  

  public Rectangle(double x1, double y1, double x2, double y2) {
    set(x1, y1, x2, y2);
  }


  public void set(double x1, double y1, double x2, double y2) {
    minX = Math.min(x1, x2);
    maxX = Math.max(x1, x2);
    minY = Math.min(y1, y2);
    maxY = Math.max(y1, y2);
  }
  

  public void set(Rectangle r) {
    minX = r.minX;
    minY = r.minY;
    maxX = r.maxX;
    maxY = r.maxY;  
  }
   

  public Rectangle copy() {
    return new Rectangle(minX, minY, maxX, maxY); 
  }
  

  public boolean edgeOverlaps(Rectangle r) {
    return minX == r.minX || maxX == r.maxX || minY == r.minY || maxY == r.maxY;
  }
  

  public boolean intersects(Rectangle r) {
    return maxX >= r.minX && minX <= r.maxX && maxY >= r.minY && minY <= r.maxY;
  }
 

  static public boolean intersects(double r1MinX, double r1MinY, double r1MaxX, double r1MaxY,
                                 double r2MinX, double r2MinY, double r2MaxX, double r2MaxY) { 
    return r1MaxX >= r2MinX && r1MinX <= r2MaxX && r1MaxY >= r2MinY && r1MinY <= r2MaxY;                           
  }
  

  public boolean contains(Rectangle r) {
    return maxX >= r.maxX && minX <= r.minX && maxY >= r.maxY && minY <= r.minY;   
  }
  

  static public boolean contains(double r1MinX, double r1MinY, double r1MaxX, double r1MaxY,
                                 double r2MinX, double r2MinY, double r2MaxX, double r2MaxY) {
    return r1MaxX >= r2MaxX && r1MinX <= r2MinX && r1MaxY >= r2MaxY && r1MinY <= r2MinY;                              
  }
 

  public boolean containedBy(Rectangle r) {
    return r.maxX >= maxX && r.minX <= minX && r.maxY >= maxY && r.minY <= minY;   
  }
  

  public double distance(Point p) {
    double distanceSquared = 0;
    
    double temp = minX - p.x;
    if (temp < 0) {
      temp = p.x - maxX;
    }
    
    if (temp > 0) {
      distanceSquared += (temp * temp);
    }

    temp = minY - p.y;
    if (temp < 0) {
      temp = p.y - maxY;
    }

    if (temp > 0) {
      distanceSquared += (temp * temp);
    }
         
    return Math.sqrt(distanceSquared);
  }
  

  static public double distance(double minX, double minY, double maxX, double maxY, double pX, double pY) {
    return Math.sqrt(distanceSq(minX, minY, maxX, maxY, pX, pY));
  }
  
  static public double distanceSq(double minX, double minY, double maxX, double maxY, double pX, double pY) {
	  double distanceSqX = 0;
	  double distanceSqY = 0;
    
    if (minX > pX) {
      distanceSqX = minX - pX;
      distanceSqX *= distanceSqX;
    } else if (pX > maxX) {
      distanceSqX = pX - maxX;
      distanceSqX *= distanceSqX;
    }
   
    if (minY > pY) {
      distanceSqY = minY - pY;
      distanceSqY *= distanceSqY;
    } else if (pY > maxY) {
      distanceSqY = pY - maxY;
      distanceSqY *= distanceSqY;
    }
   
    return distanceSqX + distanceSqY;
  }
  


  public double distance(Rectangle r) {
	double distanceSquared = 0;
	double greatestMin = Math.max(minX, r.minX);
	double leastMax    = Math.min(maxX, r.maxX);
    if (greatestMin > leastMax) {
      distanceSquared += ((greatestMin - leastMax) * (greatestMin - leastMax)); 
    }
    greatestMin = Math.max(minY, r.minY);
    leastMax    = Math.min(maxY, r.maxY);
    if (greatestMin > leastMax) {
      distanceSquared += ((greatestMin - leastMax) * (greatestMin - leastMax)); 
    }
    return Math.sqrt(distanceSquared);
  }


  public double enlargement(Rectangle r) {
	  double enlargedArea = (Math.max(maxX, r.maxX) - Math.min(minX, r.minX)) *
                         (Math.max(maxY, r.maxY) - Math.min(minY, r.minY));
                         
    return enlargedArea - area();
  }
  

  static public double enlargement(double r1MinX, double r1MinY, double r1MaxX, double r1MaxY,
                                  double r2MinX, double r2MinY, double r2MaxX, double r2MaxY) { 
	  double r1Area = (r1MaxX - r1MinX) * (r1MaxY - r1MinY);                    
    
    if (r1Area == Double.POSITIVE_INFINITY) {
      return 0;
    }
    
    if (r2MinX < r1MinX) r1MinX = r2MinX;   
    if (r2MinY < r1MinY) r1MinY = r2MinY;   
    if (r2MaxX > r1MaxX) r1MaxX = r2MaxX;                               
    if (r2MaxY > r1MaxY) r1MaxY = r2MaxY;
    
    double r1r2UnionArea = (r1MaxX - r1MinX) * (r1MaxY - r1MinY);
          
    if (r1r2UnionArea == Double.POSITIVE_INFINITY) {


      return Double.POSITIVE_INFINITY;
    }
    return r1r2UnionArea - r1Area;                              
  }
  

  public double area() {
    return (maxX - minX) * (maxY - minY);
  }
  

  static public double area(double minX, double minY, double maxX, double maxY) {
    return (maxX - minX) * (maxY - minY);
  }
  

  public void add(Rectangle r) {
    if (r.minX < minX) minX = r.minX;
    if (r.maxX > maxX) maxX = r.maxX;
    if (r.minY < minY) minY = r.minY;
    if (r.maxY > maxY) maxY = r.maxY;
  }
  

  public void add(Point p) {
    if (p.x < minX) minX = p.x;
    if (p.x > maxX) maxX = p.x;
    if (p.y < minY) minY = p.y;
    if (p.y > maxY) maxY = p.y;
  }
  

  public Rectangle union(Rectangle r) {
    Rectangle union = this.copy();
    union.add(r);
    return union; 
  }
  

  public boolean equals(Object o) {
    boolean equals = false;
    if (o instanceof Rectangle) {
      Rectangle r = (Rectangle) o;
      if (minX == r.minX && minY == r.minY && maxX == r.maxX && maxY == r.maxY) {
        equals = true;
      }
    } 
    return equals;       
  }


  public boolean sameObject(Object o) {
    return super.equals(o); 
  }
  

  public String toString() {
    return "(" + minX + ", " + minY + "), (" + maxX + ", " + maxY + ")";
  }
  

  public double width() {
    return maxX - minX;
  }
  
  public double height() {
    return maxY - minY;
  }
  
  public double aspectRatio() {
    return width() / height();
  }
  
  public Point centre() {
    return new Point((minX + maxX) / 2, (minY + maxY) / 2);
  }
  
}
