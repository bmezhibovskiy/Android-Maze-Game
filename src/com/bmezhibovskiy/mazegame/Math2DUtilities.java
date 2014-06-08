package com.bmezhibovskiy.mazegame;

import android.graphics.PointF;

public class Math2DUtilities {
	
	private Math2DUtilities(){}
	
	public static boolean pointInCircle(float px, float py, PointF c, float r) {
		return Math.sqrt((c.y-py)*(c.y-py) + (c.x-px)*(c.x-px)) < r;	
	}
	
	public static boolean circleIntersection(PointF c1, float r1, PointF c2, float r2) {
		return Math.sqrt((c2.y-c1.y)*(c2.y-c1.y) + (c2.x-c1.x)*(c2.x-c1.x)) < r1 + r2;
	}
}
