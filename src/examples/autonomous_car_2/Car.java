package autonomous_car_2;

public class Car extends Coordinate{

	/*Velocidade atual do carro*/
	private int velocity;
	
	/*Velocidade m�xima do carro*/
	private int maxVelocity;
	
	/*Sensor utilizado para detectar carros pr�ximos. 
	  Distancia maxima: 8m. Proporcional: 4*/
	private int ultrasonicSensor; 
	
	/*Sensor que captura os sem�foros, obst�culos que 
	  cortam o caminho da viagem e objetos. Para 
	  manobras urbanas de baixa velocidade. 
	  Distancia maxima: 60m. Proporcional 30*/
	private int wideSensor; 
	
	public Car(int length, int width, int velocity, int ultrasonicSensor, int wideSensor, int x, int y) {
		
		super.setX(x);
		super.setY(y);
		super.setLength(length);
		super.setWidth(width);
		this.velocity = velocity;
		this.ultrasonicSensor = ultrasonicSensor;
		this.wideSensor = wideSensor;
		
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
	
	
}
