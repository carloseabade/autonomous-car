package autonomous_car_semTravar;

public class Car extends Coordinate{

	/*Velocidade atual do carroem m/s*/
	private int velocity;
	
	/*Velocidade máxima do carro em m/s*/
	private int maxVelocity;
	
	/*Aceleração constante do carro*/
	private int acceleration;
	
	/*Sensor utilizado para detectar obstáculos próximos*/
	private int ultrasonicSensor; 
	
	/*Sensor que captura os semáforos*/
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
	
	public int getFront() {
		return getLength();
	}
}
