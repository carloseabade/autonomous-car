package autonomous_car_semTravar;

public class Car extends Coordinate{

	private int velocity;
	private int maxVelocity;
	private int acceleration;
	private int ultrasonicSensor; 
	private int wideSensor; 
	
	public Car() {}
	
	public Car(int length, int width, int velocity, int x, int y) {
		super.setX(x);
		super.setY(y);
		super.setLength(length);
		super.setWidth(width);
		this.velocity = velocity;	
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public int getUltrasonicSensor() {
		return ultrasonicSensor;
	}

	public void setUltrasonicSensor(int ultrasonicSensor) {
		this.ultrasonicSensor = ultrasonicSensor;
	}

	public int getWideSensor() {
		return wideSensor;
	}

	public void setWideSensor(int wideSensor) {
		this.wideSensor = wideSensor;
	}

	public int getMaxVelocity() {
		return maxVelocity;
	}
	
	public void setMaxVelocity(int maxVelocity) {
		this.maxVelocity = maxVelocity;
	}

	public int getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(int acceleration) {
		this.acceleration = acceleration;
	}
	
}
