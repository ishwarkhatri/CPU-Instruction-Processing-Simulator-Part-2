package edu.binghamton.my.common;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.binghamton.my.model.Instruction;

public final class CircularQueue extends AbstractCollection<Instruction> {

  private static final int MAX_CAPACITY = 16;
  private static final int DEFAULT_CAPACITY = 16;

  private int size          = 0;
  private int producerIndex = 0;
  private int consumerIndex = 0;

  // capacity must be a power of 2 at all times
  private int capacity;
  private int maxCapacity;

  // we mask with capacity -1.  This variable caches that values
  private int bitmask; 

  private Instruction[] q;

  public CircularQueue() {
    this(DEFAULT_CAPACITY);
  }

  // Construct a queue which has at least the specified capacity.  If
  // the value specified is a power of two then the queue will be
  // exactly the specified size.  Otherwise the queue will be the
  // first power of two which is greater than the specified value.
  public CircularQueue(int c) {
    this(c, MAX_CAPACITY);
  }

  public CircularQueue(int c, int mc) {
    if (c > mc) {
      throw new IllegalArgumentException("Capacity greater than maximum");
    }

    if (mc > MAX_CAPACITY) {
      throw new IllegalArgumentException("Maximum capacity greater than " +
        "allowed");
    }

    for (capacity = 1; capacity < c; capacity <<= 1) ;
    for (maxCapacity = 1; maxCapacity < mc; maxCapacity <<= 1) ;

    bitmask = capacity - 1;
    q = new Instruction[capacity];
  }

  public int getNextSlotIndex() {
	  return producerIndex;
  }

  public boolean add(Instruction obj) {
    if (size == capacity) {
      // no room
      return false;
    }

    size++;
    q[producerIndex] = obj;

    producerIndex = (producerIndex + 1) & bitmask;

    return true;
  }

  public Object remove() {
    Object obj;
    
    if (size == 0) return null;
    
    size--;
    obj = q[consumerIndex];
    q[consumerIndex] = null; // allow gc to collect
    
    consumerIndex = (consumerIndex + 1) & bitmask;

    return obj;
  }

  public boolean isEmpty() { return size == 0; }

  public int size() { return size; }

  public int capacity() { return capacity; }

  public Object peek() {
    if (size == 0) return null;
    return q[consumerIndex];
  }

  public void clear() {
    Arrays.fill(q, null);
    size = 0;
    producerIndex = 0;
    consumerIndex = 0;
  }

  public String toString() {
    StringBuffer s = new StringBuffer(super.toString() + " - capacity: '" +
      capacity() + "' size: '" + size() + "'");

    if (size > 0) {
      s.append(" elements:");
      for (int i = 0; i < size; ++i) {
        s.append('\n');
        s.append('\t');
        s.append(q[consumerIndex + i & bitmask].toString());
      }      
    }

    return s.toString();
  }

  public Iterator<Instruction> iterator() {
    return new Iterator<Instruction>() {
      private final int ci = consumerIndex;
      private final int pi = producerIndex;
      private int s = size;
      private int i = ci;

      public boolean hasNext() {
        checkForModification();
        return s > 0;
      }

      public Instruction next() {
        checkForModification();
        if (s == 0) throw new NoSuchElementException();
    
        s--;
        Instruction r = q[i];
        i = (i + 1) & bitmask;

        return r;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void checkForModification() {
        if (ci != consumerIndex) throw new ConcurrentModificationException();
        if (pi != producerIndex) throw new ConcurrentModificationException();
      }
    };
  }
}