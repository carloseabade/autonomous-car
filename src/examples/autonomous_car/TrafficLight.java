package autonomous_car;

public class TrafficLight extends Coordinate{

	private boolean red, yellow, green;
	private Boolean found = false;
	private Boolean analized = false;
	private Boolean sinalized = false;
	private Boolean overPast = false;

	public TrafficLight(int length, int width, int x, int y) {
		super("Thread do semaforo");
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
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
	
	public Boolean isFound() {
		return found;
	}

	public void setFound(Boolean found) {
		this.found = found;
	}

	public Boolean isOverPast() {
		return overPast;
	}

	public void setOverPast(Boolean overPast) {
		this.overPast = overPast;
	}

	public Boolean isAnalized() {
		return analized;
	}

	public void setAnalized(Boolean analized) {
		this.analized = analized;
	}

	public Boolean isSinalized() {
		return sinalized;
	}

	public void setSinalized(Boolean sinalized) {
		this.sinalized = sinalized;
	}

	public void start() {
		this.getThread().start();
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
