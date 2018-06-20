package autonomous_car_simulacao;

public class TrafficLight extends Obstacle{

	private boolean red, yellow, green;
	private int time;
	private String state = "";
	
	public TrafficLight() {}

	public TrafficLight(int length, int width, int x, int y) {
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		this.setRed(true);
		this.setYellow(false);
		this.setGreen(false);
		this.setTime(0);
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
	 
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public String getState() {	
		return state;
	}
	
	public void addTime() {
		this.setTime(this.getTime() + 1);
		if(this.getTime() == 600) {
			this.setTime(0);
		}
	}
	
	public void checkState() {
		if (this.getTime() > -1 && this.getTime() < 201) {
			this.setRed(true);
			this.state = "red";
			this.setGreen(false);
			this.setYellow(false);
		} else if (this.getTime() > 200 && this.getTime() < 401) {
			this.setRed(false);
			this.setGreen(true);
			this.state = "green";
			this.setYellow(false);
		} else if (this.getTime() > 400 && this.getTime() < 601) {
			this.setRed(false);
			this.setGreen(false);
			this.setYellow(true);
			this.state = "yellow";
		}
	}
}