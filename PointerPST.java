/******************************************************************************
*                       Copyright (c) 2011 - 2012 by                          *
*                               Simon Pratt                                   *
*                         (All rights reserved)                               *
*******************************************************************************
*                                                                             *
* FILE:    PointerPST.java                                                    *
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

public class PointerPST implements PrioritySearchTree {
    private PointerPSTNode root;
    
    public PointerPST(ArrayList<PSTPoint> points) {
	if(points == null) return;
	Collections.sort(points); // Sort by y-coordinate in increasing order
	this.root = buildTree(points);
    }
/******************************************************************************
* Given a list of valid points P ordered by y-coordinate in increasing        *
* order, determines a median which bisects the remaining points, then         *
* builds:                                                                     *
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

    // Assumes all points are valid (e.g. not null)
    private PointerPSTNode buildTree(ArrayList<PSTPoint> points) {
	if(points == null || points.size() < 1) return null;
	// Find point with lowest Y value
	PSTPoint rootPoint = points.remove(0);
	// Find median X value
	double sum = 0.0d;
	for(PSTPoint p : points)
	    sum += p.getX();
	double medianX = sum/points.size();
	// Make upper and lower point array
	ArrayList<PSTPoint> upperPoints = new ArrayList<PSTPoint>();
	ArrayList<PSTPoint> lowerPoints = new ArrayList<PSTPoint>();
	for(PSTPoint p : points) {
	    if(p.getX() <= medianX) lowerPoints.add(p);
	    else upperPoints.add(p);
	}
	// Make tree
	PointerPSTNode root = new PointerPSTNode(rootPoint);
	if(lowerPoints.size() > 0)
	    root.setLeftChild(buildTree(lowerPoints));
	if(upperPoints.size() > 0)
	    root.setRightChild(buildTree(upperPoints));
	return root;
    }
/******************************************************************************
*                                                                             *
* Find all points within the region bounded by (minX,minY) and (maxX,maxY)    *
*                                                                             *
*              +--------+ (maxX,maxY)                                         *
*              |        |                                                     *
*              |        |                                                     *
*              |        |                                                     *
*  (minX,minY) +--------+                                                     *
*                                                                             *
* Assumes maxX > minX and maxY > minY.                                        *
* Choose minX,minY,maxX,maxY appropriately.                                   *
*                                                                             *
******************************************************************************/
    public ArrayList<PSTPoint> findAllPointsWithin(double minX, 
						   double maxX, double maxY)
	throws EmptyTreeException {
	return findAllPointsWithin(minX,maxX,maxY,
				   new ArrayList<PSTPoint>(),root);
    }
    // Note that as maxY and maxX approach positive infinity and
    // minX approaches negative infinity, this search visits more nodes.
    // In the worst case, all nodes are visited.
    private ArrayList<PSTPoint> findAllPointsWithin(double minX,
						    double maxX, double maxY,
						    ArrayList<PSTPoint> list,
						    PointerPSTNode node)
	throws EmptyTreeException {
	if(node == null) return list;
	if(node.getY() <= maxY) {
	    double nodeX = node.getX();
	    if(nodeX >= minX && nodeX <= maxX) { 
		list.add(node.getPoint());
	    }
	    PointerPSTNode leftChild = node.getLeftChild();
	    if(leftChild != null) {
		double nodeR = maxX(leftChild);
		// nodeR >= points in left tree >= minX
		if(nodeR >= minX)
		    findAllPointsWithin(minX,maxX,maxY,list,
					leftChild);
		// nodeR < points in right tree <= maxX
		if(nodeR < maxX) 
		    findAllPointsWithin(minX,maxX,maxY,list,
					node.getRightChild());
	    }
	}
	return list;
    }
/******************************************************************************
* Other query functions                                                       *
******************************************************************************/
    public double minYinRange(double minX, double maxX, double maxY)
	throws NoPointsInRangeException {
	double min = minYinRange(minX,maxX,maxY,root);
	if(min < Double.POSITIVE_INFINITY) return min;
	throw new NoPointsInRangeException();
    }
    private double minYinRange(double minX, double maxX, double maxY,
			       PointerPSTNode node) {
	if(node == null || node.getY() > maxY) return Double.POSITIVE_INFINITY;
	double nodeX = node.getX();
	if(nodeX >= minX && nodeX <= maxX) return node.getY();
	PointerPSTNode leftChild = node.getLeftChild();
	if(leftChild != null) {
	    double nodeR = maxX(leftChild);
	    // nodeR >= points in left tree >= minX
	    if(nodeR >= minX && nodeR < maxX) {
		double minLeft = minYinRange(minX,maxX,maxY,leftChild);
		double minRight = minYinRange(minX,maxX,maxY,node.getRightChild());
		return (minLeft < minRight ? minLeft : minRight);
	    } else if(nodeR >= minX) {
		return minYinRange(minX,maxX,maxY,node.getLeftChild());
	    } else if(nodeR < maxX) {
		return minYinRange(minX,maxX,maxY,node.getRightChild());
	    }
	}
	return Double.POSITIVE_INFINITY;
    }
    public double minXinRange(double minX, double maxX, double maxY)
	throws NoPointsInRangeException {
	double min = minXinRange(minX,maxX,maxY,root);
	if(min < Double.POSITIVE_INFINITY) return min;
	throw new NoPointsInRangeException();
    }
    private double minXinRange(double minX, double maxX, double maxY,
			       PointerPSTNode node) {
	if(node == null || node.getY() > maxY)
	    return Double.POSITIVE_INFINITY;
	double min = Double.POSITIVE_INFINITY;
	double nodeX = node.getX();
	if(minX <= nodeX && nodeX <= maxX)
	    min = nodeX;
	PointerPSTNode leftChild = node.getLeftChild();
	if(leftChild != null) {
	    double nodeR = maxX(leftChild);
	    if(nodeR >= minX) {
		double minLeft = minXinRange(minX,maxX,maxY,leftChild);
		if(minLeft < min) min = minLeft;
	    }
	    if(nodeR < maxX) {
		double minRight = minXinRange(minX,maxX,maxY,node.getRightChild());
		if(minRight < min) min = minRight;
	    }
	}
	return min;
    }
    public double maxXinRange(double minX, double maxX, double maxY)
	throws NoPointsInRangeException {
	double max = maxXinRange(minX,maxX,maxY,root);
	if(max > Double.NEGATIVE_INFINITY) return max;
	throw new NoPointsInRangeException();
    }
    private double maxXinRange(double minX, double maxX, double maxY,
			       PointerPSTNode node) {
	if(node == null || node.getY() > maxY)
	    return Double.NEGATIVE_INFINITY;
	double max = Double.NEGATIVE_INFINITY;
	double nodeX = node.getX();
	if(minX <= nodeX && nodeX <= maxX)
	    max = nodeX;
	PointerPSTNode leftChild = node.getLeftChild();
	if(leftChild != null) {
	    double nodeR = maxX(leftChild);
	    if(nodeR >= minX) {
		double maxLeft = maxXinRange(minX,maxX,maxY,leftChild);
		if(maxLeft > max) max = maxLeft;
	    }
	    if(nodeR < maxX) {
		double maxRight = maxXinRange(minX,maxX,maxY,node.getRightChild());
		if(maxRight > max) max = maxRight;
	    }
	}
	return max;
    }
    public double maxYinRange(double minX, double maxX, double maxY)
	throws NoPointsInRangeException {
	double max = maxYinRange(minX,maxX,maxY,root);
	if(max > Double.NEGATIVE_INFINITY) return max;
	throw new NoPointsInRangeException();
    }
    private double maxYinRange(double minX, double maxX, double maxY,
			       PointerPSTNode node) {
	if(node == null || node.getY() > maxY)
	    return Double.NEGATIVE_INFINITY;
	double max = Double.NEGATIVE_INFINITY;
	double nodeX = node.getX();
	if(minX <= nodeX && nodeX <= maxX)
	    max = node.getY();
	PointerPSTNode leftChild = node.getLeftChild();
	if(leftChild != null) {
	    double nodeR = maxX(leftChild);
	    if(nodeR >= minX) {
		double maxLeft = maxYinRange(minX,maxX,maxY,leftChild);
		if(maxLeft > max) max = maxLeft;
	    }
	    if(nodeR < maxX) {
		double maxRight = maxYinRange(minX,maxX,maxY,node.getRightChild());
		if(maxRight > max) max = maxRight;
	    }
	}
	return max;
    }
	
/******************************************************************************
* Whole-tree query functions                                                  *
******************************************************************************/
    public double maxX() {
	return maxX(root);
    }
    private double maxX(PointerPSTNode node) {
	double max = node.getX();
	PointerPSTNode child = node.getRightChild();
	while(child != null) {
	    node = child;
	    child = node.getRightChild();
	    if(node.getX() > max)
		max = node.getX();
	}
	// Since a leaf without a sibling is always left
	// we have to check the last left child just in case
	child = node.getLeftChild();
	if(child != null && child.getX() > max)
	    max = child.getX();
	return max;
    }
/******************************************************************************
* Utility Functions                                                           *
******************************************************************************/
    private static void printList(ArrayList<PSTPoint> points) {
	for(PSTPoint p : points) System.out.print(p + " ");
	System.out.println();
    }
    private static int doubleToInt(double d) {
	return (new Double(d)).intValue();
    }
}