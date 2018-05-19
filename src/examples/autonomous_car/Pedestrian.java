package autonomous_car;

public class Pedestrian extends Coordinate{
	
	private PedestrianValues pedestrianValues;
	public boolean check_pedestrian = false;
	private boolean goingUp = false;
	private boolean goingDown = false;
	private boolean waitingToGoDown = false;
	private boolean waitingToGoUp = true;

	public Pedestrian(int length, int width, int x, int y, PedestrianValues pedestrianValues) {
		super("Thread do pedestre");
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		this.pedestrianValues = pedestrianValues;
		goingUp = pedestrianValues.isGoingUp();
		goingDown = pedestrianValues.isGoingDown();
		waitingToGoUp = pedestrianValues.isWaitingToGoUp();
		waitingToGoDown = pedestrianValues.isWaitingToGoDown();
	}
	
	public boolean isGoingUp() {
		return goingUp;
	}
	
	public boolean isGoingDown() {
		return goingDown;
	}
	
	public boolean isWaitingToGoDown() {
		return waitingToGoDown;
	}
	
	public boolean isWaitingToGoUp() {
		return waitingToGoUp;
	}
	
	public void check_pedestrian() {
		try {
			if(isWaitingToGoUp()) {
//				Thread.sleep(2000);
				this.goingUp = true;
				this.goingDown = false;
				this.waitingToGoUp = false;
				this.waitingToGoDown = false;
			} else if(isGoingUp()) {
				if(this.getY()<60) {
					this.goingUp = false;
					this.goingDown = false;
					this.waitingToGoUp = false;
					this.waitingToGoDown = true;
				}
			} else if(isWaitingToGoDown()) {
//				Thread.sleep(2000);
				this.goingUp = false;
				this.goingDown = true;
				this.waitingToGoUp = false;
				this.waitingToGoDown = false;
			} else if(isGoingDown()) {
				if(this.getY()>156) {
					this.goingUp = false;
					this.goingDown = false;
					this.waitingToGoUp = true;
					this.waitingToGoDown = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		this.getThread().start();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				if(pedestrianValues.isWaitingToGoUp()) {
					Thread.sleep(2000);
					pedestrianValues.setGoingUp(true);
					pedestrianValues.setGoingDown(false);
					pedestrianValues.setWaitingToGoUp(false);
					pedestrianValues.setWaitingToGoDown(false);
				} else if(pedestrianValues.isGoingUp()) {
					if(pedestrianValues.getY()<70) {
						pedestrianValues.setGoingUp(false);
						pedestrianValues.setGoingDown(false);
						pedestrianValues.setWaitingToGoUp(false);
						pedestrianValues.setWaitingToGoDown(true);
					}
				} else if(pedestrianValues.isWaitingToGoDown()) {
					Thread.sleep(2000);
					pedestrianValues.setGoingUp(false);
					pedestrianValues.setGoingDown(true);
					pedestrianValues.setWaitingToGoUp(false);
					pedestrianValues.setWaitingToGoDown(false);
				} else if(pedestrianValues.isGoingDown()) {
					if(pedestrianValues.getY()>146) {
						pedestrianValues.setGoingUp(false);
						pedestrianValues.setGoingDown(false);
						pedestrianValues.setWaitingToGoUp(true);
						pedestrianValues.setWaitingToGoDown(false);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
