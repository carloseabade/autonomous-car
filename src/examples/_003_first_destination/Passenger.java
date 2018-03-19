package _003_first_destination;

public class Passenger {
	
	private String name;

	private Coordinate pickUp;
	private Coordinate dropOff;
	
	public Passenger(Coordinate pickUp, Coordinate dropOff) {
		this.pickUp = pickUp;
		this.dropOff = dropOff;
	}
	
	public Passenger(String name, Coordinate pickUp, Coordinate dropOff) {
		this.name = name;

		this.pickUp = pickUp;
		this.dropOff = dropOff;
	}
	
	public String getName() {
		return name;
	}

	public Coordinate getPickUp() {
		return pickUp;
	}

	public Coordinate getDropOff() {
		return dropOff;
	}

}
