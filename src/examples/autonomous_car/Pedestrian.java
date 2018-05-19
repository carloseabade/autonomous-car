package autonomous_car;

public class Pedestrian extends Coordinate{
	
	private PedestrianValues pedestrianValues;

	public Pedestrian(PedestrianValues pedestrianValues) {
		super("Thread do pedestre");
		this.pedestrianValues = pedestrianValues;
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
