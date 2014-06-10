package com.bmezhibovskiy.mazegame;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class Hero {

	public Hero(PointF location, AssetManager assets) {
		center.set(location);
		InputStream spriteSheetStream = null;
		try {
			spriteSheetStream = assets.open("dragon_spritesheet.png");
			spriteSheet = BitmapFactory.decodeStream(spriteSheetStream);
			spriteSheetStream.close();
		} catch (IOException e) {
			Log.wtf("Hero", e.getLocalizedMessage());
		}
	}
	
	public PointF getLocation() {
		return center;
	}
	
	public int getBombsAvailable() {
		return bombsAvailable;
	}
	
	public boolean detectAndResolveWallCollision(LineSegment2D wall, float wallThickness) {
		PointF currentOffset = wall.circleIntersectionResolutionOffset(center, radius, wallThickness);
		if(currentOffset != null) {
			center.offset(currentOffset.x,currentOffset.y);
			return true;
		}
		return false;
	}
	
	public boolean detectCoinCollision(PointF coinCenter, float coinRadius) {
		return Math2DUtilities.circleIntersection(center, radius, coinCenter, coinRadius);
	}
	
	public boolean detectAndResolveBombCollision(PointF bombCenter, float bombRadius) {
		if(Math2DUtilities.circleIntersection(center, radius, bombCenter, bombRadius)) {
			++bombsAvailable;
			return true;
		}
		return false;		
	}
	
	public boolean detectFinishCollision(PointF finishLocation) {
		return Math2DUtilities.pointInCircle(finishLocation.x,finishLocation.y, center, radius);		
	}
	
	public void draw(android.graphics.Canvas canvas) {
		int animationFrameIndex = 0;
		//TODO: Idle animation
		if(Math.abs(velocity.x) > minSpeed || Math.abs(velocity.y) > minSpeed) {
			animationFrameIndex = (drawCounter/drawsPerAnimationFrame) % moveAnimationNumFrames;
		}
		int srcLeft = spriteWidth * animationFrameIndex;
		Rect src = new Rect(srcLeft,0,srcLeft+spriteWidth,spriteHeight);
		Rect dst = new Rect((int)(center.x-radius),(int)(center.y-radius), (int)(center.x+radius), (int)(center.y+radius));
		canvas.save();
		canvas.rotate(rotationInDegrees+90, center.x, center.y);
		canvas.drawBitmap(spriteSheet, src, dst, heroPaint);
		canvas.restore();
		++drawCounter;
	}
	
	public void update(LineSegment2D inputVector) {
		
		if(inputVector == null || inputVector.a == null) { //decelerate				
			velocity.set(velocity.x*deccelerationMultiplier,velocity.y*deccelerationMultiplier);
			if(velocity.length() < minSpeed) {
				velocity.set(0,0);
			}
		}
		else {
			acceleration = new PointF(inputVector.b.x - inputVector.a.x, inputVector.b.y - inputVector.a.y);
			velocity.offset(acceleration.x,acceleration.y);
			float circleVelocityLength = velocity.length();
			if(circleVelocityLength > maxSpeed) {
				float multiplier = maxSpeed/circleVelocityLength;
				velocity.set(velocity.x*multiplier,velocity.y*multiplier);
			}
			
			PointF directionVector = new PointF(inputVector.b.x-inputVector.a.x, inputVector.b.y-inputVector.a.y);
			rotationInDegrees = (float) (Math.atan2(directionVector.y, directionVector.x) * 180/Math.PI);
		}

		center.x = center.x + velocity.x;
		center.y = center.y + velocity.y;
		
		
	}
	
	Bitmap spriteSheet;
	private float rotationInDegrees = 0.0f;
	private int drawCounter = 0;
	private final int moveAnimationNumFrames = 8;
	private final int drawsPerAnimationFrame = 2;
	private final int spriteWidth = 128;
	private final int spriteHeight = 128;
	
	private Paint heroPaint = new Paint();

	private PointF center = new PointF();
	private PointF velocity = new PointF();
	private PointF acceleration = new PointF();
	private final float deccelerationMultiplier = 0.8f;
	private final float maxSpeed = 4.0f;
	private final float minSpeed = 0.05f;

	private int bombsAvailable = 1;

	private float radius = 25;

}
