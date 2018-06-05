package autonomous_car_simulacao;

public class TrafficLight extends Obstacle{

	private boolean red, yellow, green;
	private int time;
	
	public TrafficLight() {}

	public TrafficLight(int length, int width, int x, int y) {
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		this.setRed(true);
		this.setYellow(false);
		this.setGreen(false);
		this.time = 0;
	}
	
	public boolean isRed() {
		return this.red;
	}
	
	public boolean isYellow() {
		return this.yellow;
	}
	
	public boolean isGreen() {
		return this.green;
	}
	
	private void setRed(boolean red) {
		this.red = red;
	}
	
	private void setYellow(boolean yellow) {
		this.yellow = yellow;
	}
	
	private void setGreen(boolean green) {
		this.green = green;
	}
	
	public void addTime() {
		this.time += 1;
		if(this.time == 800) {
			this.time = 0;
		}
	}
	
	public void checkState() {
		if (this.time > -1 && this.time < 401) {
			this.setRed(true);
			this.setGreen(false);
			this.setYellow(false);
		} else if (this.time > 400 && this.time < 601) {
			this.setRed(false);
			this.setGreen(true);
			this.setYellow(false);
		} else if (this.time > 600 && this.time < 801) {
			this.setRed(false);
			this.setGreen(false);
			this.setYellow(true);
		}
	}
}
