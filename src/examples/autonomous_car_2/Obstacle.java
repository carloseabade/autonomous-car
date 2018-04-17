package autonomous_car_2;

public class Obstacle extends Coordinate{
	
	private Boolean found = false;
	private Boolean analized = false;
	private Boolean overPast = false;
	
	public Obstacle(int length, int width, int x, int y) {
		
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		
	}

	public Boolean getFound() {
		return found;
	}

	public void setFound(Boolean found) {
		this.found = found;
	}

	public Boolean getOverPast() {
		return overPast;
	}

	public void setOverPast(Boolean overPast) {
		this.overPast = overPast;
	}

	public Boolean getAnalized() {
		return analized;
	}

	public void setAnalized(Boolean analized) {
		this.analized = analized;
	}
}
