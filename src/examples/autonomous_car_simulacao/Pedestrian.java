package autonomous_car_simulacao;

public class Pedestrian extends Coordinate{

	private int time;
	private boolean goingUp = false;
	private boolean goingDown = false; 
	private boolean waitingToGoDown = false;
	private boolean waitingToGoUp = true;

	public Pedestrian(int length, int width, int x, int y) {
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
		this.setTime(0);
	}
	
	public void addTime() {
		if(isWaitingToGoDown() || isWaitingToGoUp()) {
			this.setTime(this.getTime() + 1);
			if(this.getTime() == 150) {
				this.setTime(0);
				if(isWaitingToGoDown()) {
					setGoingUp(false); 
					setGoingDown(true);
					setWaitingToGoUp(false);
					setWaitingToGoDown(false);
				} else if(isWaitingToGoUp()) {
					setGoingUp(true);
					setGoingDown(false);
					setWaitingToGoUp(false);
					setWaitingToGoDown(false);
				}
			}
		}
	}
	
	public void checkState() {
		if(isGoingUp()) {
			if(getY()<70) {
				setGoingUp(false); 
				setGoingDown(false);
				setWaitingToGoUp(false);
				setWaitingToGoDown(true);
			}
		} else if(isGoingDown()) {
			if(getY()>146) {
				setGoingUp(false);
				setGoingDown(false);
				setWaitingToGoUp(true);
				setWaitingToGoDown(false);
			}
		}
	}
	
	public boolean isGoingUp() {
		return goingUp;
	}

	public void setGoingUp(boolean goingUp) {
		this.goingUp = goingUp;
	}

	public boolean isGoingDown() {
		return goingDown;
	}

	public void setGoingDown(boolean goingDown) {
		this.goingDown = goingDown;
	}

	public boolean isWaitingToGoDown() {
		return waitingToGoDown;
	}

	public void setWaitingToGoDown(boolean waitingToGoDown) {
		this.waitingToGoDown = waitingToGoDown;
	}

	public boolean isWaitingToGoUp() {
		return waitingToGoUp;
	}

	public void setWaitingToGoUp(boolean waitingToGoUp) {
		this.waitingToGoUp = waitingToGoUp;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
}
