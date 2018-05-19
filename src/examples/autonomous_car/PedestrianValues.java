package autonomous_car;

public class PedestrianValues {
	
	private int x;
	private int y;
	private boolean goingUp = false;
	private boolean goingDown = false;
	private boolean waitingToGoDown = false;
	private boolean waitingToGoUp = true;
	
	public PedestrianValues(int x, int y, boolean goingUp, boolean goingDown, boolean waitingToGoUp, boolean waitingToGoDown) {
		this.x = x;
		this.y = y;
		this.goingUp = goingUp;
		this.goingDown = goingDown;
		this.waitingToGoUp = waitingToGoUp;
		this.waitingToGoUp = waitingToGoUp;
	}

	public synchronized int getY() {
		return this.y;
	}
	
	public synchronized void setY(int y) {
		this.y = y;
	}

	public synchronized int getX() {
		return x;
	}

	public synchronized void setX(int x) {
		this.x = x;
	}

	public synchronized boolean isGoingUp() {
		return goingUp;
	}

	public synchronized void setGoingUp(boolean goingUp) {
		this.goingUp = goingUp;
	}

	public synchronized boolean isGoingDown() {
		return goingDown;
	}

	public synchronized void setGoingDown(boolean goingDown) {
		this.goingDown = goingDown;
	}

	public synchronized boolean isWaitingToGoDown() {
		return waitingToGoDown;
	}

	public synchronized void setWaitingToGoDown(boolean waitingToGoDown) {
		this.waitingToGoDown = waitingToGoDown;
	}

	public synchronized boolean isWaitingToGoUp() {
		return waitingToGoUp;
	}

	public synchronized void setWaitingToGoUp(boolean waitingToGoUp) {
		this.waitingToGoUp = waitingToGoUp;
	}
	
	
}
