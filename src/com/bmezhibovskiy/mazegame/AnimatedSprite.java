package com.bmezhibovskiy.mazegame;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Log;

public abstract class AnimatedSprite {

	public AnimatedSprite(PointF location, float size) {
		center.set(location);
		prevCenter.set(location);
		radius = size;
	}
	
	public PointF getLocation() {
		return center;
	}
	
	public boolean detectAndResolveWallCollision(LineSegment2D wall, float wallThickness) {
		PointF currentOffset = wall.circleIntersectionResolutionOffset(center, radius, wallThickness);
		if(currentOffset != null) {
			center.offset(currentOffset.x,currentOffset.y);
			return true;
		}
		return false;
	}
	
	public abstract void draw(android.graphics.Canvas canvas);
	
	private PointF center = new PointF();
	protected PointF prevCenter = new PointF();
	protected PointF velocity = new PointF();
	
	protected PointF facing = new PointF(1.0f,0.0f);
	protected float angularVelocity = 0;
	
	protected float radius = 25;
	
	protected Bitmap bitmapFromAssetNamed(String assetFileName, AssetManager assets) {
		Bitmap spriteSheet = null;
		InputStream spriteSheetStream = null;
		try {
			spriteSheetStream = assets.open(assetFileName);
			spriteSheet = BitmapFactory.decodeStream(spriteSheetStream);
			spriteSheetStream.close();
		} catch (IOException e) {
			Log.wtf("AnimatedSprite", e.getLocalizedMessage());
		}
		return spriteSheet;
	}
	
	protected float rotationInDegrees() {
		return (float) (Math.atan2(facing.y, facing.x) * 180/Math.PI) + 90;
	}
	
	protected float speed() {
		return Math2D.subtract(center, prevCenter).length();
	}
	
	protected PointF getCenter() {
		return new PointF(center.x,center.y);
	}
	
	protected void setCenter(PointF p) {
		prevCenter.set(center);
		center.set(p);
	}
}
