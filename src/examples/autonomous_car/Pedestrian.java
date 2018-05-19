package autonomous_car;

public class Pedestrian extends Coordinate{
	
	public boolean check_pedestrian = false;
	private boolean goingUp = false;
	private boolean goingDown = false;
	private boolean waitingToGoDown = false;
	private boolean waitingToGoUp = true;

	public Pedestrian(int length, int width, int x, int y) {
		super("Thread do pedestre");
		super.setLength(length);
		super.setWidth(width);
		super.setX(x);
		super.setY(y);
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
				if(this.getY()<70) {
//					JOptionPane.showMessageDialog(null, "Deveria parar");
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
				if(this.getY()>146) {
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

}
