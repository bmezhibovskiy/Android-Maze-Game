package com.bmezhibovskiy.mazegame;

import java.util.Set;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.PointF;

public class Enemy extends AnimatedSprite{

	public Enemy(PointF location, float size, AssetManager assets) {
		super(location, size*0.6f);
		setRandomVelocity();
	}
	
	public void draw(android.graphics.Canvas canvas) {
		canvas.drawCircle(getCenter().x, getCenter().y, radius, new Paint());
	}
	
	public void update(Set<LineSegment2D> nearbyWalls, float wallThickness) {		
		setCenter(Math2D.add(getCenter(),velocity));
		for (LineSegment2D wall : nearbyWalls) {
			if(detectAndResolveWallCollision(wall, wallThickness)) {
				setRandomVelocity();
			}
		}
	}
	
	public boolean detectAndResolveCollisionWithHero(Hero hero) {
		RotatedRect vulnerableRect = hero.vulnerableRect();
		if(vulnerableRect.intersectsCircle(getCenter(),radius)) {
			hero.getHit(this);
		}
		RotatedRect dangerousRect = hero.dangerousRect();
		if(dangerousRect != null && dangerousRect.intersectsCircle(getCenter(),radius)) {
			return true;
		}
		return false; 
	}
	
	public void setRandomVelocity() {
		velocity.set(1.0f,0.0f);
		velocity = Math2D.rotate(velocity,(float)(Math.random()*2.0*Math.PI));
	}

}
