package _003_first_destination;

public class GridCell {
	
	private Coordinate coordinate;

	private boolean hasObstacle;


	public GridCell(Coordinate coordinate) {
		this.coordinate = coordinate;
	}
	
	public GridCell(int gridX, int gridY) {
		this.coordinate = new Coordinate(gridX, gridY);
	}
	
	public GridCell(Coordinate coordinate, boolean hasObstacle) {
		this.coordinate = coordinate;
		this.hasObstacle = hasObstacle;
	}

	public GridCell(int gridX, int gridY, boolean hasObstacle) {
		this.coordinate = new Coordinate(gridX, gridY);
		this.hasObstacle = hasObstacle;
	}
	

	public Coordinate getCoordinate() {
		return this.coordinate;
	}

	public void setObstacle(boolean hasObstacle) {
		this.hasObstacle = hasObstacle;
	}
	public boolean hasObstacle() {
		return this.hasObstacle;
	}

	public static String getIndex (int x, int y) {
		return String.format("%d,%d", x, y);
	}
	
}
