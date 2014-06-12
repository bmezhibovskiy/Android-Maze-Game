package com.bmezhibovskiy.mazegame;

import android.graphics.PointF;
import android.graphics.RectF;

public class Math2D {
	
	private Math2D(){}
	
	//Collision detection methods
	public static boolean pointInCircle(float px, float py, PointF c, float r) {
		return Math.sqrt((c.y-py)*(c.y-py) + (c.x-px)*(c.x-px)) < r;	
	}
	
	public static boolean circleIntersection(PointF c1, float r1, PointF c2, float r2) {
		return Math.sqrt((c2.y-c1.y)*(c2.y-c1.y) + (c2.x-c1.x)*(c2.x-c1.x)) < r1 + r2;
	}
	
	public static boolean pointInRect(PointF p, RectF r) {
		return r.contains(p.x, p.y);
	}
	
	public static boolean circleIntersectsRect(PointF center, float radius, RectF rect) {
		if(pointInRect(center, rect)) {
			return true;
		}
		LineSegment2D top = new LineSegment2D(rect.left, rect.top, rect.right, rect.top);
		LineSegment2D left = new LineSegment2D(rect.left, rect.top, rect.left, rect.bottom);
		LineSegment2D right = new LineSegment2D(rect.right, rect.top, rect.right, rect.bottom);
		LineSegment2D bottom = new LineSegment2D(rect.left, rect.bottom, rect.right, rect.bottom);
		return top.intersectsCircle(center, radius) || left.intersectsCircle(center, radius) || right.intersectsCircle(center, radius) || bottom.intersectsCircle(center, radius);
	}
	
	//Vector math methods
	public static PointF add(PointF a, PointF b) {
		return new PointF(a.x+b.x,a.y+b.y);
	}
	public static PointF subtract(PointF a, PointF b) {
		return new PointF(a.x-b.x,a.y-b.y);
	}
	public static PointF scale(PointF v, float s) {
		return new PointF(v.x*s, v.y*s);
	}
	public static PointF normalize(PointF v) throws IllegalArgumentException {
		float length = v.length();
		if(length <= 0.0f) {
			throw new IllegalArgumentException("Vector length is zero.");
		}
		return new PointF(v.x/length, v.y/length);
	}
	public static float dot(PointF a, PointF b) {
		return a.x*b.x + a.y*b.y;
	}
	public static float lengthSquared(PointF a) {
		return a.x*a.x + a.y*a.y;
	}
	public static PointF project(PointF a, PointF b) throws IllegalArgumentException { //Project a onto b
		float lengthSquaredB = lengthSquared(b);
		if(lengthSquaredB <= 0.0f) {
			throw new IllegalArgumentException("Vector length of 'b' is zero.");
		}
		float dotProduct = dot(a,b);
		return scale(b, dotProduct/lengthSquaredB);
	}
	public static float angle(PointF a, PointF b) throws IllegalArgumentException { //In radians, between -pi and pi
		float aLength = a.length();
		float bLength = b.length();
		if(aLength <= 0.0f) {
			throw new IllegalArgumentException("Vector 'a' length is zero.");
		}
		if(bLength <= 0.0f) {
			throw new IllegalArgumentException("Vector 'b' length is zero.");
		}
		double cosAngle = Math.max(-1.0, Math.min(1.0, dot(a,b)/(aLength*bLength)));
		double angle = Math.acos(cosAngle);
		
		PointF perpendicularToB = new PointF(b.y,-b.x);
		if(dot(a,perpendicularToB) < 0) {
			angle = -angle;
		}
		return (float)angle;
	}
	public static PointF rotate(PointF v, float angle) throws IllegalArgumentException { //Angle is in radians				
		return new PointF((float)(v.x*Math.cos(angle) - v.y*Math.sin(angle)), (float)(v.x*Math.sin(angle) + v.y*Math.cos(angle)));
	}
}
