package com.bmezhibovskiy.mazegame;

import android.graphics.PointF;

public class LineSegment2D {
	public PointF a = null;
	public PointF b = null;
	
	public LineSegment2D(float x1, float y1, float x2, float y2) {
		a = new PointF(x1,y1);
		b = new PointF(x2,y2);
	}
	
	public LineSegment2D(PointF a, PointF b) {
		if(a != null) {
			this.a = new PointF(a.x,a.y);
		}
		if(b != null) {
			this.b = new PointF(b.x,b.y);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		LineSegment2D other = (LineSegment2D)o;
		return (int)(other.a.x) == (int)(a.x) &&
				(int)(other.a.y) == (int)(a.y) &&
				(int)(other.b.x) == (int)(b.x) &&
				(int)(other.b.y) == (int)(b.y);
	}

	// http://doswa.com/2009/07/13/circle-segment-intersectioncollision.html
	public PointF closestPointToCircle(PointF c, float r) {

		PointF seg_v = new PointF(b.x-a.x,b.y-a.y);
		PointF pt_v = new PointF(c.x-a.x,c.y-a.y);
		float seg_v_length = seg_v.length();
		PointF seg_v_normalized = new PointF(seg_v.x/seg_v_length, seg_v.y/seg_v_length);
		float proj_v_length = pt_v.x*seg_v_normalized.x + pt_v.y*seg_v_normalized.y;
		PointF closest = new PointF();
		if(proj_v_length < 0) {
			closest.set(a);
		}
		else if(proj_v_length > seg_v_length) {
			closest.set(b);
		}
		else  {
			PointF proj_v = new PointF(seg_v_normalized.x*proj_v_length, seg_v_normalized.y*proj_v_length);
			closest = new PointF(a.x+proj_v.x, a.y+proj_v.y);
		}

		return closest;			
	}

	public PointF circleIntersectionResolutionOffset(PointF c, float r, float lineThickness) {
		PointF closest = closestPointToCircle(c, r);
		PointF dist_v = new PointF(c.x-closest.x, c.y-closest.y);
		if(dist_v.length() < r+lineThickness/2) {
			float dist_v_length = dist_v.length();
			float multiplier = ((r-dist_v_length+lineThickness/2)/dist_v_length);
			PointF offset = new PointF(dist_v.x*multiplier, dist_v.y*multiplier);
			return offset;
		}
		return null;
	}
}
