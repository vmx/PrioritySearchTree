/******************************************************************************
*                       Copyright (c) 2011 - 2012 by                          *
*                               Simon Pratt                                   *
*                         (All rights reserved)                               *
*******************************************************************************
*                                                                             *
* FILE:    PrioritySearchTree.java                                            *
*                                                                             *
* MODULE:  Priority Search Tree                                               *
*                                                                             *
* NOTES:   A priority search tree is a tree data structure which stores       *
*          a set of coordinates in sorted order.  The root element of         *
*          the tree is the point with highest Y value.  The rest of           *
*          the points are divided into two sets, one set having               *
*          smaller x values than the median, the other having higher.         *
*                                                                             *
*          See README for more information.                                   *
*                                                                             *
*          See LICENSE for license information.                               *
*                                                                             *
******************************************************************************/

import java.awt.geom.*;
import java.util.*;

public class PrioritySearchTree {
    PSTNode[] heap;

/******************************************************************************
* The worst case for space is when there are 2^m nodes, for some m.           *
* In which case, O(2^(logn) - 1) extra space is allocated.                    *
******************************************************************************/
    public PrioritySearchTree(ArrayList<PSTPoint> points) {
	if(points == null) return;
	Collections.sort(points); // Sort by y-coordinate in increasing order
	this.heap = new PSTNode[heapSize(treeHeight(points.size()))];
	buildTree(0,points);
    }
/******************************************************************************
* Given a root index and a list of valid points P ordered by                  *
* y-coordinate in increasing order, determines a median which bisects         *
* the remaining points, then builds:                                          *
*                                                                             *
*   root: point with lowest y-value                                           *
*   left child:  {p ∈ (P - root) | p.x <= medianX}                            *
*   right child: {p ∈ (P - root) | p.x >  medianX}                            *
*                                                                             *
* Note: points are also assumed to have distinct coordinates, i.e. no         *
*       two points have the same x coordinate and no two points have          *
*       the same y coordinate.                                                *
*                                                                             *
*       While this may seem unrealistic, we can convert any indistinct        *
*       coordinates by replacing all real coordinates with distinct           *
*       coordinates from the composite-number space without any loss          *
*       of generality.  See: Computational Geometry: Applications and         *
*       Algorithms, de Berg et al.  Section 5.5.                              *
*                                                                             *
******************************************************************************/
    private void buildTree(int rootIndex, ArrayList<PSTPoint> points) {
	if(points == null || points.size() < 1) return;
	// Since points are ordered by y increasing, smallest is first
	PSTPoint rootPoint = points.get(0);
	// Find median X value
	//  - uses average X value of non-root points
	double sumX = 0.0d;
	for(PSTPoint p : points) { 
	    sumX += p.getX();
	}
	sumX -= rootPoint.getX();
	double medianX = sumX/(points.size()-1);
	// Set the root node
	heap[rootIndex] = new PSTNode(rootPoint,medianX);
	// Bisect the non-root points into two arrays above and below the median
	ArrayList<PSTPoint> upperPoints = new ArrayList<PSTPoint>();
	ArrayList<PSTPoint> lowerPoints = new ArrayList<PSTPoint>();
	for(PSTPoint p : points) {
	    if(p == rootPoint) continue;
	    // note: if p.x is equal to median, it will be added to left child
	    else if(p.getX() <= medianX) lowerPoints.add(p);
	    else upperPoints.add(p);
	}
	if(lowerPoints.size() > 0) buildTree(indexOfLeftChild(rootIndex),lowerPoints);
	if(upperPoints.size() > 0) buildTree(indexOfRightChild(rootIndex),upperPoints);
    }
/******************************************************************************
*                                                                             *
* Find all points within the box given by (x1,y1) and (x2,y2)                 *
*                                                                             *
*          +--------+ (x2,y2)                                                 *
*          |        |                                                         *
*          |        |                                                         *
*          |        |                                                         *
*  (x1,y1) +--------+                                                         *
*                                                                             *
* Assumes x2 > x1 and y2 > y1.  Choose x1,y1,x2,y2 appropriately.             *
*                                                                             *
******************************************************************************/
    public ArrayList<PSTPoint> findAllPointsWithin(double x1, 
						   double x2, double y2) {
	return findAllPointsWithin(x1,x2,y2,new ArrayList<PSTPoint>(),0);
    }
    public ArrayList<PSTPoint> findAllPointsWithin(double x1, double y1,
						   double x2, double y2) {
	return findAllPointsWithin(x1,y1,x2,y2,new ArrayList<PSTPoint>(),0);
    }
    private ArrayList<PSTPoint> findAllPointsWithin(double x1, double y1,
						    double x2, double y2,
						    ArrayList<PSTPoint> list,
						    int rootIndex) {
	if(heap == null) return list;
	PSTNode node = heap[rootIndex];
	if(node == null) return list;
	if(node.getY() < y1) {
	    double nodeR = node.getMedianX();
	    // nodeR >= points in left tree >= x1
	    if(nodeR >= x1)
		findAllPointsWithin(x1,y1,x2,y2,list,indexOfLeftChild(rootIndex));
	    // nodeR < points in right tree <= x2
	    if(nodeR < x2) 
		findAllPointsWithin(x1,y1,x2,y2,list,indexOfRightChild(rootIndex));
	} else {
	    // Now that nodeY >= y1, we can do a 3 bounded search
	    findAllPointsWithin(x1,x2,y2,list,rootIndex);
	}
	return list;
    }
    // Note that as y2 and x2 approach positive infinity and
    // x1 approaches negative infinity, this search visits more nodes.
    // In the worst case, all nodes are visited.
    private ArrayList<PSTPoint> findAllPointsWithin(double x1,
						    double x2, double y2,
						    ArrayList<PSTPoint> list,
						    int rootIndex) {
	PSTNode node = heap[rootIndex];
	if(node == null) return list;
	double nodeX = node.getX();
	double nodeY = node.getY();
	double nodeR = node.getMedianX();
	if(nodeY <= y2) {
	    if(nodeX >= x1 && nodeX <= x2) { 
		list.add(node.getPoint());
	    }
	    // nodeR >= points in left tree >= x1
	    if(nodeR >= x1)
		findAllPointsWithin(x1,x2,y2,list,indexOfLeftChild(rootIndex));
	    // nodeR < points in right tree <= x2
	    if(nodeR < x2) 
		findAllPointsWithin(x1,x2,y2,list,indexOfRightChild(rootIndex));
	}
	return list;
    }
/******************************************************************************
* Other query functions                                                       *
******************************************************************************/
    public double minX() throws EmptyTreeException {
	int index = 0;
	if(heap[index] == null) throw new EmptyTreeException();
	double min = heap[index].getX();
	while(indexOfLeftChild(index) < heap.length &&
	      heap[indexOfLeftChild(index)] != null) {
	    index = indexOfLeftChild(index);
	    if(heap[index].getX() < min)
		min = heap[index].getX();
	}
	return min;
    }
    public double maxX() throws EmptyTreeException {
	int index = 0;
	if(heap[index] == null) throw new EmptyTreeException();
	double max = heap[index].getX();
	while(indexOfRightChild(index) < heap.length &&
	      heap[indexOfRightChild(index)] != null) {
	    index = indexOfRightChild(index);
	    if(heap[index].getX() > max)
		max = heap[index].getX();
	}
	// Since a leaf without a sibling is always left
	// we have to check the last left child just in case
	if(indexOfLeftChild(index) < heap.length &&
	   heap[indexOfLeftChild(index)] != null &&
	   heap[indexOfLeftChild(index)].getX() > max)
	    max = heap[indexOfLeftChild(index)].getX();
	return max;
    }    
    public double minY() throws EmptyTreeException {
	if(heap[0] == null) throw new EmptyTreeException();
	return heap[0].getY();
    }
    public double maxY() throws EmptyTreeException {
	if(heap[0] == null) throw new EmptyTreeException();
	return maxY(0);
    }
    private double maxY(int index) {
	double max = heap[index].getY();
	if(indexOfRightChild(index) < heap.length) {
	    if(heap[indexOfLeftChild(index)] == null &&
	       heap[indexOfRightChild(index)] != null) {
		max = maxY(indexOfRightChild(index));
	    } else if(heap[indexOfLeftChild(index)] != null &&
		      heap[indexOfRightChild(index)] == null) {
		max = maxY(indexOfLeftChild(index));
	    } else if(heap[indexOfLeftChild(index)] != null &&
		      heap[indexOfRightChild(index)] != null) {
		double maxLeft = maxY(indexOfLeftChild(index));
		double maxRight = maxY(indexOfRightChild(index));
		if(maxLeft > maxRight) max = maxLeft;
		else max = maxRight;
	    }
	}
	return max;
    }
/******************************************************************************
* Utility Functions                                                           *
******************************************************************************/
    // height of a balanced tree with n elements
    private static int treeHeight(int n) {
	return doubleToInt(Math.ceil(Math.log(n+1)/Math.log(2)));
    }
    // max number of heap nodes in a tree of given height
    private static int heapSize(int height) {
	return doubleToInt(Math.pow(2, height)-1);
    }
    // width of a tree at a given depth
    private static int width(int depth) {
	return doubleToInt(Math.pow(2,depth-1));
    }
    // amount of unused space allocated for a given number of nodes
    private static int waste(int n) {
	int height = treeHeight(n);
	return (width(height) - (n - heapSize(height-1)));
    }
    private static int indexOfLeftChild(int rootIndex) {
	return (2*rootIndex)+1;
    }
    private static int indexOfRightChild(int rootIndex) {
	return (2*rootIndex)+2;
    }
    private static int doubleToInt(double d) {
	return (new Double(d)).intValue();
    }
    private static void printList(ArrayList<PSTPoint> points) {
	for(PSTPoint p : points) System.out.print(p + " ");
	System.out.println();
    }
    private static void report(int n) {
	System.out.println("Nodes: " + n);
	int height = treeHeight(n);
	System.out.println("Tree depth: " + height);
	int heapSize = heapSize(height);
	System.out.println("Heap size: " + heapSize);
	System.out.println("Width at max depth: " + width(height));
	System.out.println("Unused nodes: " + (heapSize - n));
    }
/******************************************************************************
* Testing                                                                     *
******************************************************************************/  
    public static void main(String[] args) throws EmptyTreeException {
	ArrayList<PSTPoint> testPoints = new ArrayList<PSTPoint>();
	for(double i = 1.0d; i <= 50000; i++) {
	    testPoints.add(new PSTPoint(i,i));
	    testPoints.add(new PSTPoint(-i,-i));
	}
	System.out.print("Building tree...");
	PrioritySearchTree pst = new PrioritySearchTree(testPoints);
	System.out.println("done.");
	report(testPoints.size());

	System.out.println("MinY: " + pst.minY());
	System.out.println("MaxY: " + pst.maxY());
	System.out.println("MinX: " + pst.minX());
	System.out.println("MaxX: " + pst.maxX());
	System.out.println("All points within 4 bounds: ");
	printList(pst.findAllPointsWithin(-10.0d,-10.0d,10.0d,10.0d));
	System.out.println("All points within 3 bounds: ");
	printList(pst.findAllPointsWithin(-10.0d,10.0d,10.0d));
    }
/******************************************************************************
* Miscellaneous                                                               *
******************************************************************************/
    public class EmptyTreeException extends Exception {
	public EmptyTreeException() { super("Tree is empty"); }
    }
}