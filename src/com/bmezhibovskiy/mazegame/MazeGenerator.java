package com.bmezhibovskiy.mazegame;

import java.util.Set;

import android.graphics.PointF;

public interface MazeGenerator {
	public void generate(float mazeWidth, float mazeHeight, float cellWidth, float cellHeight, MazeGeneratorDelegate delegate);
	public PointF getStartLocation();
	public PointF getFinishLocation();
	public Set<LineSegment2D> getWalls();
	public Set<PointF> getRandomRoomLocations(int numLocations, boolean exclusive); //exclusive means it's never been returned before, and not a start or finish location
}
