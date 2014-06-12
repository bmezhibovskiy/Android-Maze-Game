package com.bmezhibovskiy.mazegame;

import android.graphics.PointF;
import android.graphics.RectF;

public class RotatedRect {
	
	private PointF a = new PointF();
	private PointF b = new PointF();
	private PointF c = new PointF();
	private PointF d = new PointF();
	private PointF pivot = new PointF();
	private float currentRotation = 0;
	private RectF unrotatedRect = null;
	
	public PointF getA() { return new PointF(a.x,a.y); }
	public PointF getB() { return new PointF(b.x,b.y); }
	public PointF getC() { return new PointF(c.x,c.y); }
	public PointF getD() { return new PointF(d.x,d.y); }
	
	private RotatedRect(PointF a, PointF b, PointF c, PointF d, PointF pivot) {
		this.a.set(a);
		this.b.set(b);
		this.c.set(c);
		this.d.set(d);
		this.pivot.set(pivot);
	}
	public RotatedRect(RectF r, PointF pivot) {
		this(new PointF(r.left,r.top), new PointF(r.left,r.bottom), new PointF(r.right,r.top), new PointF(r.right,r.bottom), pivot);
		unrotatedRect = new RectF(r);
	}
	
	public RotatedRect(RectF r, PointF pivot, float angle) {		
		this(r, pivot);
		rotate(angle);
	}
	
	public void rotate(float angle) {
		if(currentRotation+angle < 0.0) {
			currentRotation = (2.0f*(float)Math.PI) - (Math.abs(currentRotation+angle) % (2.0f*(float)Math.PI));
		}
		else {
			currentRotation = (currentRotation+angle) % (2.0f*(float)Math.PI);
		}
		a = Math2D.add(Math2D.rotate(Math2D.subtract(a,pivot), angle), pivot);
		b = Math2D.add(Math2D.rotate(Math2D.subtract(b,pivot), angle), pivot);
		c = Math2D.add(Math2D.rotate(Math2D.subtract(c,pivot), angle), pivot);
		d = Math2D.add(Math2D.rotate(Math2D.subtract(d,pivot), angle), pivot);
	}
	
	public boolean intersectsCircle(PointF center, float radius) {
		if(containsPoint(center)) {
			return true;
		}
		LineSegment2D AB = new LineSegment2D(a,b);
		LineSegment2D BC = new LineSegment2D(b,c);
		LineSegment2D CD = new LineSegment2D(c,d);
		LineSegment2D AD = new LineSegment2D(a,d);
		return AB.intersectsCircle(center, radius) || BC.intersectsCircle(center, radius) || CD.intersectsCircle(center, radius) || AD.intersectsCircle(center, radius);
	}
	
	public boolean containsPoint(PointF p) {
		if(unrotatedRect == null) {
			throw new NullPointerException("RotatedRect must be based on a RectF, so the original unrotated RectF must exist.");
		}
		PointF unrotatedPoint = Math2D.add(Math2D.rotate(Math2D.subtract(p,pivot), -currentRotation), pivot);
		return unrotatedRect.contains(unrotatedPoint.x, unrotatedPoint.y);
	}
	
}
