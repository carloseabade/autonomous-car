package autonomous_car;

public class TrafficLight extends Obstacle implements Runnable{
	
	private Thread thread;
	private boolean red, yellow, green;
	
	public TrafficLight() {}

	public TrafficLight(int length, int width, int x, int y) {
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		thread = new Thread();
		this.setRed(true);
		this.setYellow(false);
		this.setGreen(false);
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

	public void start() {
		thread.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				if(this.isRed()) {
					Thread.sleep(5000);
					this.setRed(false);
					this.setYellow(false);
					this.setGreen(true);
				} else if(this.isYellow()) {
					Thread.sleep(2000);
					this.setYellow(false);
					this.setGreen(false);
					this.setRed(true);
				} else if(this.isGreen()) {
					Thread.sleep(5000);
					this.setGreen(false);
					this.setRed(false);
					this.setYellow(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
