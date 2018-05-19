package autonomous_car;

public class Pedestrian extends Coordinate implements Runnable{
	
	private Thread thread;
	private boolean goingUp = false;
	private boolean goingDown = false;
	private boolean waitingToGoDown = false;
	private boolean waitingToGoUp = true;

	public Pedestrian(int length, int width, int x, int y) {
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		this.thread = new Thread();
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
	
	public void start() {
		thread.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				if(isWaitingToGoUp()) {
					Thread.sleep(1000);
					this.goingUp = true;
					this.goingDown = false;
					this.waitingToGoUp = false;
					this.waitingToGoDown = false;
				} else if(isGoingUp()) {
					Thread.sleep(4000);
					this.goingUp = false;
					this.goingDown = false;
					this.waitingToGoUp = false;
					this.waitingToGoDown = true;
				} else if(isWaitingToGoDown()) {
					Thread.sleep(1000);
					this.goingUp = false;
					this.goingDown = true;
					this.waitingToGoUp = false;
					this.waitingToGoDown = false;
				} else if(isGoingDown()) {
					Thread.sleep(4000);
					this.goingUp = false;
					this.goingDown = false;
					this.waitingToGoUp = true;
					this.waitingToGoDown = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
