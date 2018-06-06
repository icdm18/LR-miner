

















package com.infomatiq.jsi.rtree;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;


public class PriorityQueue {
  public static final boolean SORT_ORDER_ASCENDING = true;
  public static final boolean SORT_ORDER_DESCENDING = false;

  private TIntArrayList values = null;
  private TDoubleArrayList priorities = null;
  private boolean sortOrder = SORT_ORDER_ASCENDING;

  private static boolean INTERNAL_CONSISTENCY_CHECKING = false;

  public PriorityQueue(boolean sortOrder) {
    this(sortOrder, 10);
  }

  public PriorityQueue(boolean sortOrder, int initialCapacity) {
    this.sortOrder = sortOrder;
    values = new TIntArrayList(initialCapacity);
    priorities = new TDoubleArrayList(initialCapacity);
  }


  private boolean sortsEarlierThan(double p1, double p2) {
    if (sortOrder == SORT_ORDER_ASCENDING) {
      return p1 < p2;
    }
    return p2 < p1;
  }



  public void insert(int value, double priority) {
    values.add(value);
    priorities.add(priority);

    promote(values.size() - 1, value, priority);
  }

  private void promote(int index, int value, double priority) {




    while (index > 0) {
      int parentIndex = (index - 1) / 2;
      double parentPriority = priorities.get(parentIndex);

      if (sortsEarlierThan(parentPriority, priority)) {
        break;
      }


      values.set(index, values.get(parentIndex));
      priorities.set(index, parentPriority);
      index = parentIndex;
    }

    values.set(index, value);
    priorities.set(index, priority);

    if (INTERNAL_CONSISTENCY_CHECKING) {
      check();
    }
  }

  public int size() {
    return values.size();
  }

  public void clear() {
    values.clear();
    priorities.clear();
  }

  public void reset() {
    values.reset();
    priorities.reset();
  }

  public int getValue() {
    return values.get(0);
  }

  public double getPriority() {
    return priorities.get(0);
  }

  private void demote(int index, int value, double priority) {
    int childIndex = (index * 2) + 1;

    while (childIndex < values.size()) {
      double childPriority = priorities.get(childIndex);

      if (childIndex + 1 < values.size()) {
        double rightPriority = priorities.get(childIndex + 1);
        if (sortsEarlierThan(rightPriority, childPriority)) {
          childPriority = rightPriority;
          childIndex++;
        }
      }

      if (sortsEarlierThan(childPriority, priority)) {
        priorities.set(index, childPriority);
        values.set(index, values.get(childIndex));
        index = childIndex;
        childIndex = (index * 2) + 1;
      } else {
        break;
      }
    }

    values.set(index, value);
    priorities.set(index, priority);
  }






  public int pop() {
    int ret = values.get(0);


    int lastIndex = values.size() - 1;
    int tempValue = values.get(lastIndex);
    double tempPriority = priorities.get(lastIndex);

    values.remove(lastIndex);
    priorities.remove(lastIndex);

    if (lastIndex > 0) {
      demote(0, tempValue, tempPriority);
    }

    if (INTERNAL_CONSISTENCY_CHECKING) {
      check();
    }

    return ret;
  }

  public void setSortOrder(boolean sortOrder) {
    if (this.sortOrder != sortOrder) {
      this.sortOrder = sortOrder;

      for (int i = (values.size() / 2) - 1; i >= 0; i--) {
        demote(i, values.get(i), priorities.get(i));
      }
    }
    if (INTERNAL_CONSISTENCY_CHECKING) {
      check();
    }
  }

  private void check() {


    int lastIndex = values.size() - 1;

    for (int i = 0; i < values.size() / 2; i++) {
      double currentPriority = priorities.get(i);

      int leftIndex = (i * 2) + 1;
      if (leftIndex <= lastIndex) {
        double leftPriority = priorities.get(leftIndex);
        if (sortsEarlierThan(leftPriority, currentPriority)) {
          System.err.println("Internal error in PriorityQueue");
        }
      }

      int rightIndex = (i * 2) + 2;
      if (rightIndex <= lastIndex) {
        double rightPriority = priorities.get(rightIndex);
        if (sortsEarlierThan(rightPriority, currentPriority)) {
          System.err.println("Internal error in PriorityQueue");
        }
      }
    }
  }
}
