package com.bmezhibovskiy.mazegame;

import java.io.IOException;
import java.io.InputStream;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class Hero {

	public Hero(PointF location, float gameSize, AssetManager assets) {
		center.set(location);
		radius = gameSize;
		InputStream spriteSheetStream = null;
		try {
			spriteSheetStream = assets.open("dragon_spritesheet.png");
			dragonSpriteSheet = BitmapFactory.decodeStream(spriteSheetStream);
			spriteSheetStream.close();
			spriteSheetStream = assets.open("flame_spritesheet.png");
			flameSpriteSheet = BitmapFactory.decodeStream(spriteSheetStream);
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
		canvas.save();
		canvas.rotate(rotationInDegrees()+90, center.x, center.y);
		
		int animationFrameIndex = 0;
		float currentFireSize = fireSize();
		if(currentFireSize > 0) {
			animationFrameIndex = (drawCounter/fireAnimationDrawsPerFrame) % fireAnimationNumFrames;
			int srcLeft = fireSpriteWidth * animationFrameIndex;
			Rect src = new Rect(srcLeft,0,srcLeft+fireSpriteWidth,fireSpriteHeight);
			float fireHalfWidth = currentFireSize*(fireWidth/2.0f);
			float fireHalfHeight = currentFireSize*(fireHeight/2.0f);
			PointF dstCenter = new PointF(center.x, center.y-radius-fireHalfHeight);
			Rect dst = new Rect((int)(dstCenter.x-fireHalfWidth),(int)(dstCenter.y-fireHalfHeight), (int)(dstCenter.x+fireHalfWidth), (int)(dstCenter.y+fireHalfHeight));	
			canvas.drawBitmap(flameSpriteSheet, src, dst, heroPaint);
		}
		
		animationFrameIndex = 0;
		if(Math.abs(velocity.x) > minSpeed || Math.abs(velocity.y) > minSpeed) {
			animationFrameIndex = (drawCounter/moveAnimationDrawsPerFrame) % moveAnimationNumFrames;
		}
		else {
			animationFrameIndex = (drawCounter/idleAnimationDrawsPerFrame) % idleAnimationNumFrames;
		}
		
		int srcLeft = dragonSpriteWidth * animationFrameIndex;
		Rect src = new Rect(srcLeft,0,srcLeft+dragonSpriteWidth,dragonSpriteHeight);
		float sizeOffset = velocity.length()*0.4f;
		float dragonHalfWidth = radius - sizeOffset;
		float dragonHalfHeight = radius + sizeOffset;
		Rect dst = new Rect((int)(center.x-dragonHalfWidth),(int)(center.y-dragonHalfHeight), (int)(center.x+dragonHalfWidth), (int)(center.y+dragonHalfHeight));		
		canvas.drawBitmap(dragonSpriteSheet, src, dst, heroPaint);
		
		canvas.restore();
		++drawCounter;
	}

	public void update(PointF inputVector) {	

		if(inputVector == null) {
			if(velocity.length() > minSpeed) {
				PointF brakeForce = Math2D.scale(Math2D.normalize(velocity),-brakeForceMagnitude);
				velocity = Math2D.add(velocity, brakeForce);
			}
			else {
				velocity.set(0,0);
			}
			if(Math.abs(angularVelocity) > minAngularSpeed) {
				float angularBrakeForce = (angularVelocity/Math.abs(angularVelocity)) * -angularBrakeForceMagnitude;
				angularVelocity += angularBrakeForce;
			}
			else {
				angularVelocity = 0.0f;
			}
			inputVector = new PointF(0,0);
		}
		
		
		
		if(inputVector.length() > 0) {
			float angleBetweenInputAndFacing = Math2D.angle(inputVector,facing);
			float angularAcceleration = -angleBetweenInputAndFacing * angularAccelerationScale;
			angularVelocity += angularAcceleration;
		}
		
		if(Math.abs(angularVelocity) > 0.0f) {
			float angularDrag = -angularVelocity*angularDragConstant;
			angularVelocity += angularDrag;
		}

		facing = Math2D.rotate(facing, angularVelocity);
		
		PointF acceleration = Math2D.scale(facing, inputVector.length()*accelerationScale);
		float accelerationLength = acceleration.length();
		if(accelerationLength > maxAccelerationLength) {
			acceleration = Math2D.scale(acceleration,maxAccelerationLength/accelerationLength);
		}

		velocity = Math2D.add(velocity, acceleration);

		if(velocity.length() > 0.0f) {
			float dragMagnitude = velocity.length()*dragConstant;
			PointF drag = Math2D.scale(Math2D.normalize(velocity),-dragMagnitude);
			velocity = Math2D.add(velocity, drag);

		}
		center = Math2D.add(center,velocity);
	}
	
	private float rotationInDegrees() {
		return (float) (Math.atan2(facing.y, facing.x) * 180/Math.PI);
	}
	
	private float fireSize() {
		float theFireSize = (velocity.length()-4.5f)/2.0f;
		return velocity.length() > 4.5f ? theFireSize : 0.0f;
	}

	Bitmap dragonSpriteSheet;
	Bitmap flameSpriteSheet;
	private int drawCounter = 0;
	private final int moveAnimationNumFrames = 8;
	private final int idleAnimationNumFrames = 2;
	private final int moveAnimationDrawsPerFrame = 2;
	private final int idleAnimationDrawsPerFrame = 14;
	private final int dragonSpriteWidth = 128;
	private final int dragonSpriteHeight = 128;
	private final int fireAnimationNumFrames = 16;
	private final int fireAnimationDrawsPerFrame = 1;
	private final int fireSpriteWidth = 28;
	private final int fireSpriteHeight = 51;	

	private final float dragConstant = 0.125f;
	private final float angularDragConstant = 0.2f;

	private Paint heroPaint = new Paint();

	private PointF center = new PointF();
	private PointF velocity = new PointF();
	private PointF facing = new PointF(1.0f,0.0f);
	private float angularVelocity = 0;
	private final float accelerationScale = 0.003f;
	private final float maxAccelerationLength = 1.2f;
	private final float angularAccelerationScale = 0.05f;
	private final float minSpeed = 0.05f;
	private final float minAngularSpeed = 0.005f;
	private final float brakeForceMagnitude = 0.1f;
	private final float angularBrakeForceMagnitude = 0.006f;

	private int bombsAvailable = 1;

	private float radius = 25;
	
	private float fireWidth = radius*0.5f;
	private final float fireHeightStretchMultiplier = 2.4f;
	private float fireHeight = fireHeightStretchMultiplier * fireWidth * (float)fireSpriteHeight/(float)fireSpriteWidth;

}
