package autonomous_car_semTravar;

public class PedestrianValues{
	
	private int lenght;
	private int width;
	private int x;
	private int y;
	private boolean goingUp = false;
	private boolean goingDown = false;
	private boolean waitingToGoDown = false;
	private boolean waitingToGoUp = true;
	
	public PedestrianValues(int lenght, int width, int x, int y, boolean goingUp, boolean goingDown, boolean waitingToGoUp, boolean waitingToGoDown) {
		this.setLenght(lenght);
		this.setWidth(width);
		this.x = x;
		this.y = y;
		this.goingUp = goingUp;
		this.goingDown = goingDown;
		this.waitingToGoUp = waitingToGoUp;
		this.waitingToGoDown = waitingToGoDown;
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

	public int getLenght() {
		return lenght;
	}

	public void setLenght(int lenght) {
		this.lenght = lenght;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}	
}
