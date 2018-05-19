package autonomous_car;

import autonomous_car.Coordinate;

public class Crosswalk extends Coordinate{
	
	private Boolean found = false;
	private Boolean analized = false;
	private Boolean sinalized = false;
	private Boolean overPast = false;
	private boolean hasPedestrian = false;
	private Pedestrian pedestrian = null;
	
	public Crosswalk(int length, int width, int x, int y, boolean hasPedestrian) {
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		this.hasPedestrian = hasPedestrian;
		if(hasPedestrian) {
			this.addPedestrian();
//			Thread pedestrianThread = new Thread(this.pedestrian);
//			pedestrianThread.start();
		}
	}
	
	private void addPedestrian() {
		pedestrian = new Pedestrian(9, 5, this.getX()+14, this.getY()+72);
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

	public Boolean getSinalized() {
		return sinalized;
	}

	public void setSinalized(Boolean sinalized) {
		this.sinalized = sinalized;
	}
	
	public Pedestrian getPedestrian() {
		return this.pedestrian;
	}
	
	public boolean hasPedestrian() {
		return hasPedestrian;
	}
	
}
