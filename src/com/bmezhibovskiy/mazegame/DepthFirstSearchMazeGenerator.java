package com.bmezhibovskiy.mazegame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.graphics.PointF;

public class DepthFirstSearchMazeGenerator implements MazeGenerator {
	
	// http://en.wikipedia.org/wiki/Maze_generation_algorithm
	@Override
	public void generate(float mazeWidth, float mazeHeight, float cellWidth,
			float cellHeight, MazeGeneratorDelegate delegate) {
		numCellsX = (int) (mazeWidth / cellWidth);
		numCellsY = (int) (mazeHeight / cellHeight);
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;

		mazeCells = new MazeCell[numCellsX][numCellsY];
		ArrayList<MazeCell> unvisitedCells = new ArrayList<MazeCell>();
		ArrayList<MazeCell> visitedCells = new ArrayList<MazeCell>();

		for(int i = 0; i < numCellsX; ++i) {
			for(int j = 0; j < numCellsY; ++j) {
				mazeCells[i][j] = new MazeCell(i,j);
				unvisitedCells.add(mazeCells[i][j]);
			}
		}

		int startX = (int) (Math.random()*numCellsX);
		int startY = (int) (Math.random()*numCellsY);
		paddingX = mazeWidth%cellWidth / 2.0f;
		paddingY = mazeHeight%cellHeight / 2.0f;
		startLocation = new PointF(startX*cellWidth+cellWidth/2+paddingX,startY*cellHeight+cellHeight/2+paddingY);
		ArrayList<MazeCell> cellStack = new ArrayList<MazeCell>();
		cellStack.add(mazeCells[startX][startY]);
		ArrayList<MazeCell> previousUnvisitedNeighbors = new ArrayList<MazeCell>();

		while(!cellStack.isEmpty()) {
			int lastIndex = cellStack.size() - 1;
			MazeCell currentCell = cellStack.get(lastIndex);
			cellStack.remove(lastIndex);
			currentCell.v = true;
			visitedCells.add(currentCell);
			unvisitedCells.remove(currentCell);
			previousUnvisitedNeighbors.remove(currentCell);

			ArrayList<MazeCell> unvisitedNeighbors = getNeighbors(currentCell, false);			
			if(unvisitedNeighbors.size() > 0)
			{
				int randNeighborIndex = (int) (Math.random()*unvisitedNeighbors.size());

				MazeCell randomNeighbor = unvisitedNeighbors.get(randNeighborIndex);
				unvisitedNeighbors.remove(randNeighborIndex);
				previousUnvisitedNeighbors.addAll(unvisitedNeighbors);
				
				//remove walls between current cell and random neighbor
				openWall(randomNeighbor, currentCell);
				cellStack.add(randomNeighbor);
			}
			
			if(cellStack.isEmpty()) {
				//For now, put the finish at the last cell before the cell stack goes empty for the first time.
				if(finishLocation == null) {
					finishLocation = new PointF(currentCell.x*cellWidth+cellWidth/2+paddingX,currentCell.y*cellHeight+cellHeight/2+paddingY);
				}
				
				if(!previousUnvisitedNeighbors.isEmpty()) {
					int randomUnvisitedIndex = (int) (Math.random()%previousUnvisitedNeighbors.size());
					MazeCell previousUnvistedNeighbor = previousUnvisitedNeighbors.get(randomUnvisitedIndex);
					cellStack.add(previousUnvistedNeighbor);
					//To avoid having areas of the maze completely inaccessible, we'll open a wall between this unvisited neighbor and a random visited neighbor.
					//Note: This causes unconnected walls. I think that's better than unreachable cells.
					ArrayList<MazeCell> visitedNeighbors = getNeighbors(previousUnvistedNeighbor, true);
					assert (visitedNeighbors.size() > 0) : "At this point there should be at least one adjacent visited neighbor";
					int randomVisitedIndex = (int) (Math.random()%visitedNeighbors.size());
					openWall(previousUnvistedNeighbor, visitedNeighbors.get(randomVisitedIndex));
				}
				else if(!unvisitedCells.isEmpty()) {
					int randomUnvisitedIndex = (int) (Math.random()%unvisitedCells.size());
					cellStack.add(unvisitedCells.get(randomUnvisitedIndex));
				}
			}
		}

		walls = new HashSet<LineSegment2D>();
		//generate walls based on cells
		for(MazeCell cell : visitedCells) {
			//cellX and cellY refer to the top-left of the cell, in view space
			float cellX = cell.x * cellWidth + paddingX;
			float cellY = cell.y * cellHeight + paddingY;
			if(cell.walls[0]) { //north wall
				walls.add(new LineSegment2D(cellX, cellY, cellX+cellWidth, cellY));
			}
			if(cell.walls[1]) { //south wall
				walls.add(new LineSegment2D(cellX, cellY+cellHeight, cellX+cellWidth, cellY+cellHeight));				
			}
			if(cell.walls[2]) { //east wall
				walls.add(new LineSegment2D(cellX+cellWidth, cellY, cellX+cellWidth, cellY+cellHeight));

			}
			if(cell.walls[3]) { //west wall
				walls.add(new LineSegment2D(cellX, cellY, cellX, cellY+cellHeight));				
			}
			
			neverReturnedLocations.add(new PointF(cellX + cellWidth/2.0f, cellY + cellHeight/2.0f));
		}
		
		delegate.mazeGenerationDidFinish(this);
	}

	@Override
	public PointF getStartLocation() {
		neverReturnedLocations.remove(startLocation);
		return startLocation;
	}

	@Override
	public PointF getFinishLocation() {
		neverReturnedLocations.remove(finishLocation);
		return finishLocation;
	}

	@Override
	public Set<LineSegment2D> getWalls() {
		return walls;
	}

	@Override
	public Set<PointF> getRandomRoomLocations(int numLocations, boolean exclusive) {
		//TODO: Fix floating point error with location comparison
		Set<PointF> locations = new HashSet<PointF>();
		if(exclusive && numLocations > neverReturnedLocations.size()) {
			locations.addAll(neverReturnedLocations);
			neverReturnedLocations.removeAll(neverReturnedLocations);
			return locations;
		}
		else {
			while(numLocations > 0) {
				PointF location;
				if(exclusive) {
					location = (PointF) neverReturnedLocations.toArray()[(int) (Math.random()*neverReturnedLocations.size())];
				}
				else {
					location = new PointF((float)Math.floor(Math.random()*numCellsX) * cellWidth + cellWidth/2.0f + paddingX,
										  (float)Math.floor(Math.random()*numCellsY) * cellWidth + cellHeight/2.0f + paddingY);
				}

				if(locations.add(location)) {
					boolean removed = neverReturnedLocations.remove(location);
					assert (removed) : "At this point location must be removed from neverReturnedLocations.";
					--numLocations;
				}
			}
		}
		return locations;
	}

	private class MazeCell {
		int x, y; //in grid space
		boolean[] walls; //n, s, e, w
		boolean v; //visited
		MazeCell(int x,int y) {
			this.x=x;
			this.y=y;
			walls = new boolean[4];
			for(int i = 0; i < 4; ++i) {
				walls[i] = true;
			}
			v = false;
		}
		
		@Override
		public boolean equals(Object o) {
			MazeCell m = (MazeCell)o;
			return m.x == x && m.y == y;
		}
	}
	
	private ArrayList<MazeCell> getNeighbors(MazeCell currentCell, boolean visited) {
		ArrayList<MazeCell> neighbors = new ArrayList<MazeCell>();
		for(int i = currentCell.x-1; i <= currentCell.x+1; ++i) {
			for(int j = currentCell.y-1; j <= currentCell.y+1; ++j) {
				//skip diagonals, out-of-bounds indices, and visited (or unvisited) cells
				if((i==currentCell.x || j==currentCell.y) && i>=0 && j>=0 && i<numCellsX && j<numCellsY && mazeCells[i][j].v == visited) {
					neighbors.add(mazeCells[i][j]);
				}
			}
		}
		return neighbors;
	}
	
	private void openWall(MazeCell a, MazeCell b) {
		if(a.x < b.x) {
			a.walls[2] = false;
			b.walls[3] = false;
		} else if(a.x > b.x) {
			a.walls[3] = false;
			b.walls[2] = false;
		} else if(a.y < b.y) {
			a.walls[1] = false;
			b.walls[0] = false;
		} else if(a.y > b.y) {
			a.walls[0] = false;
			b.walls[1] = false;
		}
	}
	
	private PointF startLocation;
	private PointF finishLocation;
	private float paddingX, paddingY;
	private Set<LineSegment2D> walls;
	private MazeCell[][] mazeCells;
	private int numCellsX, numCellsY;
	private float cellWidth, cellHeight;
	private Set<PointF> neverReturnedLocations = new HashSet<PointF>();

}
