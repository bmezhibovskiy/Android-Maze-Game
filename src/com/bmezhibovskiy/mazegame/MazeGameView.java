package com.bmezhibovskiy.mazegame;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.bmezhibovskiy.mazegame.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MazeGameView extends View {	

	public MazeGameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		wallPaint = new Paint();
		wallPaint.setStyle(Paint.Style.STROKE);
		wallPaint.setStrokeWidth(wallThickness);
		heroPaint = new Paint();
		floorTextPaint = new Paint();
		floorTextPaint.setTextSize(heroRadius*2.0f);
		inputIndicatorPaint = new Paint();
		inputIndicatorPaint.setARGB(100, 100, 0, 75);
		inputIndicatorPaint.setStyle(Paint.Style.STROKE);
		inputIndicatorPaint.setStrokeWidth(16);
		uiTextPaint = new Paint();
		uiTextPaint.setTextSize(24);
		uiTextPaint.setStyle(Paint.Style.FILL);
		uiTextPaint.setColor(Color.GREEN);
		uiTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		uiTextStrokePaint = new Paint();
		uiTextStrokePaint.setStyle(Paint.Style.STROKE);
		uiTextStrokePaint.setStrokeWidth(1.5f);
		uiTextStrokePaint.setTextSize(24);
		uiTextStrokePaint.setTypeface(Typeface.DEFAULT_BOLD);
		uiTextStrokePaint.setColor(Color.BLACK);
		coinPaint = new Paint();
		coinPaint.setARGB(255,180,190,0);
		bombPaint = new Paint();
		bombPaint.setColor(Color.GRAY);

		/** Boilerplate code for getting attributes out of the XML **/
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.MazeGameView,
				0, 0);
		try {
			//Get the attributes here and set them to member variables
			mTitle = a.getString(R.styleable.MazeGameView_testAttr);
		} finally {
			a.recycle();
		}
		/** Boilerplate End **/
	}	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_UP) {
			initialTapPoint = null;
		}
		else {
			currentTapPoint = new PointF(event.getX(), event.getY());
			
			if(initialTapPoint == null) {
				initialTapPoint = new PointF(currentTapPoint.x, currentTapPoint.y);	
			}
			
			circleAcceleration = new PointF(currentTapPoint.x - initialTapPoint.x, currentTapPoint.y - initialTapPoint.y);					
			return true; //true means this event was handled
		}
		return super.onTouchEvent(event);		
	}

	/** Protected **/

	@Override
	protected void onDraw(android.graphics.Canvas canvas) {
		super.onDraw(canvas);		
		for(LineSegment2D wall : walls) {
			//stretch the walls by half their thickness so the corners are nicer
			float x1Offset = 0, y1Offset = 0, x2Offset = 0, y2Offset = 0;
			if(wall.a.x < wall.b.x) {
				x1Offset = -wallThickness/2.0f;
				x2Offset = wallThickness/2.0f;
			}
			else if(wall.a.x > wall.b.x) {
				x1Offset = wallThickness/2.0f;
				x2Offset = -wallThickness/2.0f;				
			}
			else if(wall.a.y < wall.b.y) {
				y1Offset = -wallThickness/2.0f;
				y2Offset = wallThickness/2.0f;				
			}
			else if(wall.a.y > wall.b.y) {
				y1Offset = wallThickness/2.0f;
				y2Offset = -wallThickness/2.0f;				
			}
			canvas.drawLine(wall.a.x+x1Offset, wall.a.y+y1Offset, wall.b.x+x2Offset, wall.b.y+y2Offset, wallPaint);			
		}
		for(PointF coin : coins) {
			canvas.drawCircle(coin.x,coin.y,coinRadius,coinPaint);
		}
		for(PointF bomb : bombs) {
			canvas.drawCircle(bomb.x,bomb.y,bombRadius,bombPaint);
		}
		canvas.drawCircle(heroCenter.x, heroCenter.y, heroRadius, heroPaint);
		
		canvas.drawText("S", startLocation.x, startLocation.y, floorTextPaint);
		canvas.drawText("F", finishLocation.x, finishLocation.y, floorTextPaint);
		
		if(initialTapPoint != null && currentTapPoint != null) {
			canvas.drawLine(initialTapPoint.x, initialTapPoint.y, currentTapPoint.x, currentTapPoint.y, inputIndicatorPaint);
		}
		canvas.drawText("Score: "+Integer.toString(score),10,20,uiTextPaint);
		canvas.drawText("Score: "+Integer.toString(score),10,20,uiTextStrokePaint);
		canvas.drawText("Bombs: "+Integer.toString(heroBombsAvailable),260,20,uiTextPaint);
		canvas.drawText("Bombs: "+Integer.toString(heroBombsAvailable),260,20,uiTextStrokePaint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {		
		super.onSizeChanged(w, h, oldw, oldh);
		if(walls == null) { //only generate maze if one doesn't exist.
			generateMaze(w,h);
		}
	}

	/** Private **/

	private class UpdateTimerTask extends TimerTask {

		@Override
		public void run() {
			if(initialTapPoint == null) { //decelerate				
				circleVelocity.set(circleVelocity.x*deccelerationMultiplier,circleVelocity.y*deccelerationMultiplier);
				if(circleVelocity.length() < minSpeed) {
					circleVelocity.set(0,0);
				}
			}
			else {
				circleVelocity.offset(circleAcceleration.x,circleAcceleration.y);
				float circleVelocityLength = circleVelocity.length();
				if(circleVelocityLength > maxSpeed) {
					float multiplier = maxSpeed/circleVelocityLength;
					circleVelocity.set(circleVelocity.x*multiplier,circleVelocity.y*multiplier);
				}
			}

			heroCenter.x = heroCenter.x + circleVelocity.x;
			heroCenter.y = heroCenter.y + circleVelocity.y;
			
			for(Iterator<PointF> coinIterator = coins.iterator(); coinIterator.hasNext(); ) {
				if(Math2DUtilities.circleIntersection(heroCenter, heroRadius, coinIterator.next(), coinRadius)) {
					coinIterator.remove();
					++score;
					break;
				}
			}
			
			for(Iterator<PointF> bombIterator = bombs.iterator(); bombIterator.hasNext(); ) {
				if(Math2DUtilities.circleIntersection(heroCenter, heroRadius, bombIterator.next(), coinRadius)) {
					bombIterator.remove();
					++heroBombsAvailable;
					break;
				}
			}
			
			if(Math2DUtilities.pointInCircle(finishLocation.x,finishLocation.y, heroCenter, heroRadius)) {
				winLevel();
			}
			else {
				for(LineSegment2D wall : walls) {
					PointF currentOffset = wall.circleIntersectionResolutionOffset(heroCenter, heroRadius, wallThickness);
					if(currentOffset != null) {
						heroCenter.offset(currentOffset.x,currentOffset.y);
					}
				}
				postInvalidate();
			}
		}

	}

	@SuppressWarnings("unused")
	private String mTitle;
	private int score = 0;
	private Paint wallPaint;
	private Paint heroPaint;
	private Paint floorTextPaint;
	private Paint inputIndicatorPaint;
	private Paint uiTextPaint;
	private Paint uiTextStrokePaint;
	private Paint coinPaint;
	private Paint bombPaint;
	private final float wallThickness = 10.0f;
	private PointF heroCenter = new PointF();
	private PointF circleVelocity = new PointF();
	private PointF startLocation = new PointF();
	private PointF finishLocation = new PointF();
	private final float maxSpeed = 4.0f;
	private final float minSpeed = 0.05f;
	private PointF circleAcceleration = new PointF();
	private final float deccelerationMultiplier = 0.8f;
	private float heroRadius = 15;
	private int difficulty = 5;
	private float wiggleRoom = heroRadius+wallThickness/2-difficulty;
	private Set<LineSegment2D> walls;
	private Set<PointF> coins;
	private Set<PointF> bombs;
	private int heroBombsAvailable = 1;
	private float coinRadius = heroRadius/2.0f;
	private float bombRadius = heroRadius/1.5f;
	private PointF initialTapPoint;
	private PointF currentTapPoint;
	private Timer updateTimer = new Timer();
	private UpdateTimerTask updateTimerTask;
	
	private void generateMaze(float width, float height) {

		float cellSize = heroRadius * 2 + wiggleRoom * 2;
		
		DepthFirstSearchMazeGenerator mazeGenerator = new DepthFirstSearchMazeGenerator();
		mazeGenerator.generate(width, height, cellSize, cellSize, new MazeGeneratorDelegate() {			
			@Override
			public void mazeGenerationDidFinish(MazeGenerator generator) {
				heroCenter.set(generator.getStartLocation());
				startLocation.set(generator.getStartLocation());
				finishLocation.set(generator.getFinishLocation());
				walls = generator.getWalls();
				coins = generator.getRandomRoomLocations((int) (Math.random()*5.0+8.0), true);
				bombs = generator.getRandomRoomLocations((int) (Math.random()*3.0+1.0), true);
				updateTimerTask = new UpdateTimerTask();
				updateTimer.schedule(updateTimerTask, 0, 33);
			}
		});

		Log.d("Canvas", "Maze generation done.");
	}
	
	private void winLevel() {
		walls = null;
		coins = null;
		initialTapPoint = null;
		updateTimerTask.cancel();
		updateTimer.purge();
		updateTimerTask = null;
		generateMaze(getWidth(), getHeight());
	}
}
