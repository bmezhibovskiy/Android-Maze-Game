package com.bmezhibovskiy.mazegame;

import java.util.ArrayList;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class Hero extends AnimatedSprite {

	public Hero(PointF location, float size, AssetManager assets) {
		super(location, size);
		dragonSpriteSheet = bitmapFromAssetNamed("dragon_spritesheet.png", assets);
		flameSpriteSheet = bitmapFromAssetNamed("flame_spritesheet.png", assets);
	}	

	public float getBombsAvailable() {
		return bombsAvailable;
	}	

	public boolean detectCoinCollision(PointF coinCenter, float coinRadius) {
		return Math2D.circleIntersection(getCenter(), radius, coinCenter, coinRadius);
	}

	public boolean detectAndResolveBombCollision(PointF bombCenter, float bombRadius) {
		if(Math2D.circleIntersection(getCenter(), radius, bombCenter, bombRadius)) {
			++bombsAvailable;
			return true;
		}
		return false;		
	}

	public boolean detectFinishCollision(PointF finishLocation) {
		return Math2D.pointInCircle(finishLocation.x,finishLocation.y, getCenter(), radius);		
	}

	public void draw(android.graphics.Canvas canvas) {
		PointF currentCenter = getCenter();
		canvas.save();
		canvas.rotate(rotationInDegrees(), currentCenter.x, currentCenter.y);
		
		int animationFrameIndex = 0;
		if(fireSize() > 0) {
			animationFrameIndex = (drawCounter/fireAnimationDrawsPerFrame) % fireAnimationNumFrames;
			int srcLeft = fireSpriteWidth * animationFrameIndex;
			Rect src = new Rect(srcLeft,0,srcLeft+fireSpriteWidth,fireSpriteHeight);				
			canvas.drawBitmap(flameSpriteSheet, src, unrotatedFireRect(), heroPaint);
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
		canvas.drawBitmap(dragonSpriteSheet, src, unrotatedHeroRect(), heroPaint);
		
		canvas.restore();
		++drawCounter;
	}

	public void update(PointF inputVector1, PointF inputVector2) {
		
		fireInputLength = inputVector2 != null ? (inputVector2.length() - fireInputLengthOffset) : 0.0f;				
		bombsAvailable = Math.max(0.0f, bombsAvailable - fireSize() * 0.002f);
		
		if(velocity.length() > speed()) {
			velocity = Math2D.scale(velocity, speed()/velocity.length());
		}

		if(inputVector1 == null) {
			if(speed() > minSpeed) {
				PointF brakeForce = Math2D.scale(velocity,-brakeForceMagnitude/speed());
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
			inputVector1 = new PointF(0,0);
		}
		
		
		
		if(inputVector1.length() > 0) {
			float angleBetweenInputAndFacing = Math2D.angle(inputVector1,facing);
			float angularAcceleration = -angleBetweenInputAndFacing * angularAccelerationScale;
			angularVelocity += angularAcceleration;
		}
		
		if(Math.abs(angularVelocity) > 0.0f) {
			float angularDrag = -angularVelocity*angularDragConstant;
			angularVelocity += angularDrag;
		}

		facing = Math2D.rotate(facing, angularVelocity);
		
		PointF acceleration = Math2D.scale(facing, inputVector1.length()*accelerationScale);
		float accelerationLength = acceleration.length();
		if(accelerationLength > maxAccelerationLength) {
			acceleration = Math2D.scale(acceleration,maxAccelerationLength/accelerationLength);
		}

		velocity = Math2D.add(velocity, acceleration);
		
		for(PointF accel : temporaryAccelerations) {
			velocity = Math2D.add(velocity, accel);
		}
		temporaryAccelerations.removeAll(temporaryAccelerations);

		if(speed() > 0.0f) {
			float dragMagnitude = speed()*dragConstant;
			PointF drag = Math2D.scale(velocity,-dragMagnitude/speed());
			velocity = Math2D.add(velocity, drag);

		}
		setCenter(Math2D.add(getCenter(),velocity));
	}
	
	public RotatedRect vulnerableRect() {
		RectF unrotatedHeroRectF = new RectF(unrotatedHeroRect());
		return new RotatedRect(unrotatedHeroRectF, new PointF(unrotatedHeroRectF.centerX(),unrotatedHeroRectF.centerY()), (rotationInDegrees()) * ((float)Math.PI/180.0f));
	}
	
	public RotatedRect dangerousRect() {
		if(unrotatedFireRect() == null) {
			return null;
		}
		RectF unrotatedFireRectF = new RectF(unrotatedFireRect());
		RectF unrotatedHeroRectF = new RectF(unrotatedHeroRect());
		return new RotatedRect(unrotatedFireRectF, new PointF(unrotatedHeroRectF.centerX(),unrotatedHeroRectF.centerY()), (rotationInDegrees()) * ((float)Math.PI/180.0f));
	}
	
	public void getHit(AnimatedSprite other) {
		//TODO: disable fire while being hit?		
		if(!other.getCenter().equals(this.getCenter())) {
			temporaryAccelerations.add(Math2D.scale(Math2D.normalize(Math2D.subtract(getCenter(),other.getCenter())), speed()+2.2f));
		}
	}
	
	private Rect unrotatedHeroRect() {
		float sizeOffset = speed()*0.4f;
		float dragonHalfWidth = radius - sizeOffset;
		float dragonHalfHeight = radius + sizeOffset;
		PointF center = getCenter();
		return new Rect((int)(center.x-dragonHalfWidth),(int)(center.y-dragonHalfHeight), (int)(center.x+dragonHalfWidth), (int)(center.y+dragonHalfHeight));
	}
	
	private Rect unrotatedFireRect() {
		if(fireSize() > 0.0) {
			float fireHalfWidth = fireSize()*(fireWidth/2.0f);
			float fireHalfHeight = fireSize()*(fireHeight/2.0f);
			PointF center = getCenter();
			PointF dstCenter = new PointF(center.x, center.y-radius-fireHalfHeight);
			return new Rect((int)(dstCenter.x-fireHalfWidth),(int)(dstCenter.y-fireHalfHeight), (int)(dstCenter.x+fireHalfWidth), (int)(dstCenter.y+fireHalfHeight));
		}
		return null;
	}
	
	private float fireSize() {
		if(bombsAvailable <= 0.0f) {
			return 0.0f;
		}
		float rawFireSize = fireInputLength * fireInputScale;
		if(rawFireSize < minFireLength) {
			return 0.0f;
		}
		return Math.min(maxFireLength, rawFireSize);
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
	
	private final float accelerationScale = 0.012f;
	private final float maxAccelerationLength = 1.2f;
	private final float angularAccelerationScale = 0.05f;
	private final float minSpeed = 0.05f;
	private final float minAngularSpeed = 0.005f;
	private final float brakeForceMagnitude = 0.1f;
	private final float angularBrakeForceMagnitude = 0.006f;
	
	private ArrayList<PointF> temporaryAccelerations = new ArrayList<PointF>();

	private float bombsAvailable = 2.0f;
	
	private float fireWidth = radius*0.5f;
	private final float fireHeightStretchMultiplier = 2.4f;
	private float fireHeight = fireHeightStretchMultiplier * fireWidth * (float)fireSpriteHeight/(float)fireSpriteWidth;
	private float fireInputLength = 0.0f;
	private final float fireInputLengthOffset = 40.0f;
	private final float fireInputScale = 0.0105f;
	private final float maxFireLength = 2.2f;
	private final float minFireLength = 0.4f;

}
