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
		return Math2D.circleIntersection(center, radius, coinCenter, coinRadius);
	}
	
	public boolean detectAndResolveBombCollision(PointF bombCenter, float bombRadius) {
		if(Math2D.circleIntersection(center, radius, bombCenter, bombRadius)) {
			++bombsAvailable;
			return true;
		}
		return false;		
	}
	
	public boolean detectFinishCollision(PointF finishLocation) {
		return Math2D.pointInCircle(finishLocation.x,finishLocation.y, center, radius);		
	}
	
	public void draw(android.graphics.Canvas canvas) {
		int animationFrameIndex = 0;
		//TODO: Idle animation
		if(Math.abs(velocity.x) > minSpeed || Math.abs(velocity.y) > minSpeed) {
			animationFrameIndex = (drawCounter/moveAnimationDrawsPerFrame) % moveAnimationNumFrames;
		}
		else {
			animationFrameIndex = (drawCounter/idleAnimationDrawsPerFrame) % idleAnimationNumFrames;
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
		
		if(inputVector == null || inputVector.a == null) {
			if(velocity.length() > minSpeed) {
				PointF brakeForce = Math2D.scale(Math2D.normalize(velocity),-brakeForceMagnitude);
				velocity = Math2D.add(velocity, brakeForce);
			}
			else {
				velocity.set(0,0);
			}
			inputVector = new LineSegment2D(0,0,0,0);
		}
		{
			PointF acceleration = Math2D.scale(Math2D.subtract(inputVector.b, inputVector.a), accelerationScale);			
			velocity = Math2D.add(velocity, acceleration);
			
			if(velocity.length() > 0.0f) {
				float dragMagnitude = 0.5f*Math2D.lengthSquared(velocity)*dragCoefficient;
				PointF drag = Math2D.scale(Math2D.normalize(velocity),-dragMagnitude);
				velocity = Math2D.add(velocity, drag);
			}
			
			
			PointF directionVector = Math2D.subtract(inputVector.b, inputVector.a);
			if(directionVector.length() > 0.0f) {
				rotationInDegrees = (float) (Math.atan2(directionVector.y, directionVector.x) * 180/Math.PI);
			}
		}

		center = Math2D.add(center,velocity);
	}
	
	Bitmap spriteSheet;
	private float rotationInDegrees = 0.0f;
	private int drawCounter = 0;
	private final int moveAnimationNumFrames = 8;
	private final int idleAnimationNumFrames = 2;
	private final int moveAnimationDrawsPerFrame = 2;
	private final int idleAnimationDrawsPerFrame = 14;
	private final int spriteWidth = 128;
	private final int spriteHeight = 128;
	
	private final float dragCoefficient = 0.05f;
	
	private Paint heroPaint = new Paint();

	private PointF center = new PointF();
	private PointF velocity = new PointF();
	private final float accelerationScale = 0.002f;
	private final float maxSpeed = 4.0f;
	private final float minSpeed = 0.05f;
	private final float brakeForceMagnitude = 0.1f;

	private int bombsAvailable = 1;

	private float radius = 25;

}
