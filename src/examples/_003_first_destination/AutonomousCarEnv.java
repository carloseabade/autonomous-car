package _003_first_destination;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import ail.mas.DefaultEnvironment;
import ail.mas.DefaultEnvironmentwRandomness;
import ail.mas.MAS;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.util.choice.Choice;

public class AutonomousCarEnv extends DefaultEnvironmentwRandomness {
	
	// Agent/Vehicle
	private Coordinate car; // Current coordinates of the agent/vehicle
	private String currentDirection; // Direction that the vehicle is heading towards (Used by the simulator)
	
	private int nObstacles; // Total of static obstacles	
	
	// Simulator
	//Class client - Class responsible to create a conexion environment-simulator. Uses static methods
	private boolean simulate; // Determines if the environment should send message to the simulator
	private int waitTimeLocation; // Wait time to send first message

	/*
	 * Note:
	 * In order to be able to visualize agent's movements correctly, wait times (waitTimeDefault and waitTimeLocation) are used.
	 * Any part of the code 'if(simulate)' verifies if the simulator is active to send messages to the simulation.
	 */
	
	// Environment setup
	@Override
	public void setMAS(MAS m) {
		super.setMAS(m);
		
		// Info about agent/vehicle - Default Values
		this.car = new Coordinate(0, 0);
		this.currentDirection = "north"; // (Used by simulation)
		
		// Info about environment 
		this.nObstacles = 5;		 
		
		/*
		 *  Initialize damageLevel, variable responsible to determine the damage caused by each obstacle in unavoidable collision situation.
		 *  Levels:
		 *  1 - Low.
		 *  2 - Moderate.
		 *  3 - High.
		 *  
		 *  The distinction between such levels is made by the agent, which will process each situation differently.
		 *  There are equal chances for classification to each level.
		 */
		
		// Simulator
		this.simulate = false;
		this.waitTimeLocation = 300; 

		
		// Setup Methods
		initObstacles(); // Obstacles (Coordinates)
	
	}
	

	/*
	 * Set 'nObstacles' in the environment randomly.
	 * Method checks if the coordinate (x,y) doesn't have a obstacle already have an obstacle.
	 * Guarantees that there isn't an obstacle in the agent's initial position and in the depot's location.
	 */
	private void initObstacles() {
		
		int x, y;
		for(int i = 0; i < this.nObstacles; i++) {
		    	do {
		    	    x = (int) (Math.random());
		    	    y = (int) (Math.random() * 3);
		    	}while( this.environmentGrid.get(GridCell.getIndex(x, y)).hasObstacle() ||
		    			( x == this.car.getX() && y == this.car.getY() )
		    			);
		    
			environmentGrid.get(GridCell.getIndex(x, y)).setObstacle(true);
		}
	}

	
	/*
	 * Identifies possible agent's actions and properly invoke a method to handle each one of them
	 * */
	public Unifier executeAction(String agName, Action act) throws AILexception {

		String actionName = act.getFunctor();
	
		Coordinate from = new Coordinate(0,0);
		Coordinate currentPosition = new Coordinate(0,0);
		Coordinate destination = new Coordinate(0,0);
		String direction;
		
		switch(actionName) {
		case "drive": 
			from.setX( Util.getIntTerm(act.getTerm(0)) );
			from.setY( Util.getIntTerm(act.getTerm(1)) );

			direction = act.getTerm(2).getFunctor();
			
			destination.setX(Util.getIntTerm(act.getTerm(3)));
			destination.setY(Util.getIntTerm(act.getTerm(4)));
			
			
			drive(agName, from, direction, destination);
			break;
		case "compass": 
			currentPosition.setX( Util.getIntTerm(act.getTerm(0)) );
			currentPosition.setY( Util.getIntTerm(act.getTerm(1)) );
			compass(agName, currentPosition);
			break;
		case "localize": 
			localize(agName);
			break;
		case "no_further_from": 
			from.setX( Util.getIntTerm(act.getTerm(0)) );
			from.setY( Util.getIntTerm(act.getTerm(1)) );

			currentPosition.setX( Util.getIntTerm(act.getTerm(2)) );
			currentPosition.setY( Util.getIntTerm(act.getTerm(3)) );
			
			destination.setX(Util.getIntTerm(act.getTerm(4)));
			destination.setY(Util.getIntTerm(act.getTerm(5)));
			
			noFurtherFrom(agName, from, currentPosition, destination);
			
			break;
			default:
		}

		return super.executeAction(agName, act);

	}

	
	/*
	 * Input:
	 * 		String agName: Agent's Name
	 *  		Coordinate from: Agent's current coordinate/position
	 *  		String direction: Direction in which the agent is moving towards 
	 *  		Coordinate destination: Position where the agent is trying to move to - Current Route Destination 
	 *  
	 *  Process:
	 *  		Based in which direction the agent wants to move, this method invokes 'getDirectionCoordinate' to calculate the new direction.
	 *  		After that, the agent 'percepts' its movement as the following beliefs 'adapt_from_to' and 'moved_from_to' are added to 
	 *  			its belief base.
	 *  		In order to update its location, 'updateLocation' method is called.
	 *  
	 *  Output:
	 *  		Agent moves from one position to the other.
	 * 
	 */
	private void drive(String agName, Coordinate from, String direction, Coordinate destination) {
		
		this.currentDirection = direction;
		
		Coordinate newPosition = getDirectionCoordinate(direction, car);

		Predicate adaptedFromTo = new Predicate("adapt_from_to");
		Predicate movedFromTo = new Predicate("moved_from_to");
		
		addFromTo(agName, adaptedFromTo, from, car, direction, destination);
		addFromTo(agName, movedFromTo, from, newPosition, direction, destination);
		
		System.err.println(String.format("Moving %s from (%d,%d) to (%d,%d)", direction, car.getX(), car.getY(), newPosition.getX(), newPosition.getY()));

		updateLocation(agName, car, newPosition);
	}
	
	
	/*
	 * Input:
	 * 		String agName: Agent's Name
	 *  		Coordinate currentPosition: Agent's current coordinate/position
	 *  		Coordinate newPosition: Position where the agent is going to move to
	 *  
	 *  Process:
	 *  		Updates agent's location.
	 *  		Invoke 'scanSurroundings' to scan all coordinates surrounding the agent.
	 *  
	 *  Output: 
	 *  		Update agent's perception about its current location.
	 * 
	 */
	private void updateLocation(String agName, Coordinate currentPosition, Coordinate newPosition) {	
	    
		Predicate oldLocation = new Predicate("at");
		oldLocation.addTerm(new NumberTermImpl(currentPosition.getX()));
		oldLocation.addTerm(new NumberTermImpl(currentPosition.getY()));

		Predicate at = new Predicate("at");
		at.addTerm(new NumberTermImpl(newPosition.getX()));
		at.addTerm(new NumberTermImpl(newPosition.getY()));

		car.setX(newPosition.getX());
		car.setY(newPosition.getY());
		
		if(simulate) {
		    
		    try {
			TimeUnit.MILLISECONDS.sleep(waitTimeLocation);
		} catch(Exception e) {
			System.err.println(e);
		}
			Client.sendMessage( Client.convertArray2String( new String[] 
				{"carLocation", String.valueOf(newPosition.getX()), String.valueOf(newPosition.getY()), this.currentDirection} ) );
			
		}

		scanSurroundings(agName, newPosition);
		

		removePercept(agName, oldLocation); 
		addPercept(agName, at);
	}

	
	/*
	 * Input:
	 * 		String agName: Agent's Name
	 *  		Coordinate currentPosition: Agent's current coordinate/position
	 *  
	 *  Process:
	 *  		Verify the existence of obstacles in all directions surrounding the agent. 
	 *  		Since we consider a static environment, all beliefs that refers to an obstacle keep stored in the agent's belief base.
	 *  		
	 *  		This method verifies the existence of a precondition for an unavoidable collision situation.
	 *  			- If there are (at least) three obstacles surrounding the agent.
	 *  			- The existence of an obstacle is given by the output of 'verifyObstacle'
	 *  			- Finally, if the result given by 'collisionChance' is true, then a collision will occur in the agent's next movement.
	 *  				- In this case, any direction that previously did not have an obstacle will be considered as having a 'temporary obstacle'.
	 *  				- The method 'addObstacleDamage' is called to classify all obstacles.
	 *  				- Agent is notify that its next movement will be a collision.
	 *  
	 *  		The variable 'obstacleDamageRelated' is used for environment control.
	 *  
	 *  
	 *  Output: 
	 *  		Agent's surroundings scanned.
	 *  		
	 */
	private void scanSurroundings(String agName, Coordinate currentPosition) {
		
		
	}
	
	
	
	
	/*
	 * Input:
	 * 		String agName: Agent's Name
	 * 		String direction: Direction that 'currentPosition' is related to the agent's current location.
	 *  		Coordinate currentPosition: Coordinates surrounding the agent's current location.
	 * 
	 * Process:
	 * 		Verifies if 'currentPosition' is inside the environment's grid and if there's an obstacle in it.
	 * 
	 * Output:
	 * 		Return the classification of 'currentPosition'
	 * 			'N' - No obstacle 
	 * 			'O' - There is an obstacle
	 * 			'F' - There is an obstacle, but 'currentPosition' isn't a valid position for the environment's grid.
	 */
	
	private char verifyObstacle(String agName, String direction, Coordinate currentPosition) {
		
		Coordinate surroundingPosition = getDirectionCoordinate(direction, currentPosition);
		
		char typeObstacle = 'N';
		boolean hasObstacle = false;
		
			if (environmentGrid.get(GridCell.getIndex(surroundingPosition.getX(), surroundingPosition.getY())).hasObstacle()) {
				hasObstacle = true;
				typeObstacle = 'O';
				
				if(simulate) {
					Client.sendMessage( new String[] {"obstacle", 
							String.valueOf(surroundingPosition.getX()), String.valueOf(surroundingPosition.getY())}  );
				}
			}
		
		if(hasObstacle) {
			addPercept(agName, addObstacle(direction, currentPosition));
			addPercept(agName, addObstacle("center", surroundingPosition));
		}
		
		return typeObstacle;
	}


	/*
	 * Adds an new obstacle perception.
	 */
	private Predicate addObstacle(String direction, Coordinate coordinate) {
		Predicate obstacle = new Predicate("obstacle");
		obstacle.addTerm(new Predicate(direction));
		obstacle.addTerm(new NumberTermImpl(coordinate.getX()));
		obstacle.addTerm(new NumberTermImpl(coordinate.getY()));
		
		return obstacle;
	}

	
	/*
	 * Adds adapt_from_to or moved_from_to perceptions
	 * */
	private void addFromTo(String agName, Predicate predicate, Coordinate from, Coordinate current, String direction, Coordinate destination) {
		predicate.addTerm(new NumberTermImpl(from.getX()));
		predicate.addTerm(new NumberTermImpl(from.getY()));
		predicate.addTerm(new NumberTermImpl(current.getX()));
		predicate.addTerm(new NumberTermImpl(current.getY()));
		predicate.addTerm(new Predicate(direction));
		predicate.addTerm(new NumberTermImpl(destination.getX()));
		predicate.addTerm(new NumberTermImpl(destination.getY()));
		
		addPercept(agName, predicate);
	}

	/*
	 * Adds adapt_from_to or moved_from_to perceptions
	 * */
	private void addAdaptAndMovedFromTo(
			String agName, Coordinate from, Coordinate currentPosition, 
			Coordinate destination, String moved, String adapt) {
		
		Predicate adaptFromTo = new Predicate("adapt_from_to");
		Predicate movedFromTo = new Predicate("moved_from_to");
		
		addFromTo(agName, adaptFromTo, from, getDirectionCoordinate(moved, currentPosition), adapt, destination);
		addFromTo(agName, movedFromTo, from, getDirectionCoordinate(moved, currentPosition), moved, destination);
		
		
	}
	
	
	/*
	 * Adds no_further perception
	 * */
	private void noFurtherFrom(String agName, Coordinate from, Coordinate currentPosition, Coordinate destination) {			
		
		System.err.println(
				"Can't come here: at("+currentPosition.getX()+","+currentPosition.getY()+") "
				+ "to get to ("+destination.getX()+","+destination.getY()+")");
				

		addAdaptAndMovedFromTo(agName, from, currentPosition, destination, "north", "south");
		addAdaptAndMovedFromTo(agName, from, currentPosition, destination, "south", "north");
		addAdaptAndMovedFromTo(agName, from, currentPosition, destination, "east", "west");
		addAdaptAndMovedFromTo(agName, from, currentPosition, destination, "west", "east");
		
		
		Predicate noFurther = new Predicate("no_further");
		noFurther.addTerm(new NumberTermImpl(from.getX()));
		noFurther.addTerm(new NumberTermImpl(from.getY()));
		noFurther.addTerm(new NumberTermImpl(currentPosition.getX()));
		noFurther.addTerm(new NumberTermImpl(currentPosition.getY()));
		noFurther.addTerm(new NumberTermImpl(destination.getX()));
		noFurther.addTerm(new NumberTermImpl(destination.getY()));
		addPercept(agName, noFurther);

	}
	
	
	/*
	 * Adds in which direction(s) the agent  has to move to achieve the end of its current route.
	 */
	private void compass(String agName, Coordinate coordinate) {
		//System.err.println( String.format("Verifying direction for (%d,%d)", x,y) );

		removeDirection(agName, "north");
		removeDirection(agName, "south");
		removeDirection(agName, "east");
		removeDirection(agName, "west");

		if (coordinate.getY() > car.getY())
			addDirection(agName, "north");
		if (coordinate.getY() < car.getY())
			addDirection(agName, "south");
		if (coordinate.getX() > car.getX())
			addDirection(agName, "east");
		if (coordinate.getX() < car.getX())
			addDirection(agName, "west");

		Predicate receiveDirection = new Predicate("receive_direction");
		addPercept(agName, receiveDirection);

	}

	// Helper to 'compass'
	private void addDirection(String agName, String direction) {
		Predicate go = new Predicate(direction);
		addPercept(agName, go);

	}
	// Helper to 'compass'
	private void removeDirection(String agName, String direction) {

		Predicate go = new Predicate(direction);
		removePercept(agName, go);

	}


	/*
	 * Provides the agent initial location
	 */
	private void localize(String agName) {

		System.err.println("Initializing GPS");
		System.err.println(String.format("Agent %s is at (%d,%d)", agName, car.getX(), car.getY()));

		updateLocation(agName, new Coordinate(0, 0), car);

	}
	
	/*
	 * Return a new coordinate given a position and a direction.
	 * */
	private Coordinate getDirectionCoordinate(String direction, Coordinate coordinate) {
		Coordinate newCoordinate;
		
		switch (direction) {
			case "north": 
				newCoordinate = new Coordinate(coordinate.getX(), coordinate.getY()+1);
				break;
			case "south":
				newCoordinate = new Coordinate(coordinate.getX(), coordinate.getY()-1);
				break;
			case "east":
				newCoordinate = new Coordinate(coordinate.getX()+1, coordinate.getY());
				break;
			case "west":
				newCoordinate = new Coordinate(coordinate.getX()-1, coordinate.getY());
				break;
			default:
				newCoordinate = new Coordinate(coordinate.getX(), coordinate.getY());
				break;
		}
		
		return newCoordinate;
	}
}
