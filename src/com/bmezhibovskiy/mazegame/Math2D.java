package com.bmezhibovskiy.mazegame;

import android.graphics.PointF;

public class Math2D {
	
	private Math2D(){}
	
	//Collision detection methods
	public static boolean pointInCircle(float px, float py, PointF c, float r) {
		return Math.sqrt((c.y-py)*(c.y-py) + (c.x-px)*(c.x-px)) < r;	
	}
	
	public static boolean circleIntersection(PointF c1, float r1, PointF c2, float r2) {
		return Math.sqrt((c2.y-c1.y)*(c2.y-c1.y) + (c2.x-c1.x)*(c2.x-c1.x)) < r1 + r2;
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
	public static PointF project(PointF a, PointF b) { //Project a onto b
		float dotProduct = dot(a,b);
		return scale(b, dotProduct/lengthSquared(b));
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
		if(Float.isInfinite(angle)) {
			throw new IllegalArgumentException("Angle is infinite.");
		}
		if(Float.isNaN(angle)) {
			throw new IllegalArgumentException("Angle is not a number.");
		}		
		return new PointF((float)(v.x*Math.cos(angle) - v.y*Math.sin(angle)), (float)(v.x*Math.sin(angle) + v.y*Math.cos(angle)));
	}
}
