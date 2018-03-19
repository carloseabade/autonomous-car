package _003_first_destination;

import java.util.ArrayList;
import java.util.HashMap;
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
	private Coordinate depotLocation; // Depot coordinates (Position where the agent goes after crashing)
	private String currentDirection; // Direction that the vehicle is heading towards (Used by the simulator)
	
	// Environment 
	private Map<String, GridCell> environmentGrid; // Maps all available positions of the environment
	private int nObstacles; // Total of static obstacles
	private int maxGridSize; // Environment's grid/matrix size (maxGridSize X maxGridSize). Starts at 0


	// Passengers
	private ArrayList<Passenger> passengers; // Passenger List
	private int nPassengers; // Total of Passengers (Randomly Generated)
	private Passenger currentPassenger; // Current Passenger inside the Agent/Vehicle
	
	
	// Helpers
	private ArrayList<Predicate> obstacleDamageRelated = new ArrayList<>(); // Store info about predicates related to unavoidable collisions.
	private Choice<Boolean> collisionChance; // Determines the chance of unavoidable collisions given the existence of a precondition
	private Choice<String> damageLevel; // Determines the chance of obstacle damage level classification 
	
	// Simulator
	//Class client - Class responsible to create a conexion environment-simulator. Uses static methods
	private boolean simulate; // Determines if the environment should send message to the simulator
	private int waitTimeDefault; // Wait time between messages
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
		this.depotLocation = new Coordinate(0, 0); 
		this.currentDirection = "north"; // (Used by simulation)
		
		// Info about environment 
		this.nObstacles = 0;
		this.maxGridSize = 10;
		 
		// Passengers
		this.passengers = new ArrayList<Passenger>();
		this.nPassengers = 5;
		this.currentPassenger = new Passenger(new Coordinate(1, 1), new Coordinate(2, 1));  // (Used by simulation)
		 
		 // Helpers
		this.obstacleDamageRelated = new ArrayList<>();
		
		/*
		 * Initialize collisionChance, variable responsible to determine if in a given situation the obstacle avoidance can occur.
		 * As precondition, the agent must be surrounded by three static obstacles.
		 * In this scenario, there is a 10% chance that the agent won't be able to avoid a possible collision.
		 */
		
		this.collisionChance = new Choice<Boolean>(m.getController());
		this.collisionChance.addChoice(0.9, false); // Will avoid
		this.collisionChance.addChoice(0.1, true);  // Won't avoid
		
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
		
		this.damageLevel = new Choice<String>(m.getController());
		this.damageLevel.addChoice(0.33, "high");
		this.damageLevel.addChoice(0.33, "moderate");
		this.damageLevel.addChoice(0.34, "low");
		 
		// Simulator
		this.simulate = true;
		this.waitTimeDefault = 750; 
		this.waitTimeLocation = 300; 

		
		// Setup Methods
		initGridInformation(); // Environment Grid
		initPassengerList(); // Random Passenger List
		initObstacles(); // Obstacles (Coordinates)
		
		
		// Simulator Setup
		if(simulate) {
		    Client.sendMessage( new String[] {"clear", String.valueOf(this.maxGridSize)} );
		    
		    Client.sendMessage( 
			new String[] {"pickUp", String.valueOf(this.currentPassenger.getPickUp().getX()), 
					String.valueOf(this.currentPassenger.getPickUp().getY())} );
		    Client.sendMessage( 
			new String[] {"dropOff", String.valueOf(this.currentPassenger.getDropOff().getX()), 
					String.valueOf(this.currentPassenger.getDropOff().getY())});
		    
		    try {
		    		TimeUnit.MILLISECONDS.sleep(100);
		    } catch(Exception e) {
		    		System.err.println(e);
		    }
		}

		// Shows current passenger list
		showAllPassengerList();
	}

	/*
	 * Shows current passenger list
	 */
	private void showAllPassengerList() {
		for (Passenger passenger : passengers) {
			System.err.println(String.format("Passenger %s: PickUp(%d,%d) - Drop Off (%d,%d)", passenger.getName(),
					passenger.getPickUp().getX(), passenger.getPickUp().getY(), 
					passenger.getDropOff().getX(), passenger.getDropOff().getY()
					));
		}
	}

	/*
	 * Maps all positions available in the environment using a HashMap.
	 * Where:
	 * String - ID for a specific cell
	 * GridCell - Cell Data
	 * 
	 */
	private void initGridInformation() {

	    	this.environmentGrid = new HashMap<String, GridCell>();
		
		for (int x = 0; x < this.maxGridSize; x++) {
			for (int y = 0; y < this.maxGridSize; y++) {

				String cellName = GridCell.getIndex(x, y);
				this.environmentGrid.put(cellName, new GridCell(x, y, false));
			}
		}
	}

	/*
	 * Create a passenger list with 'nPassengers' passengers
	 */
	private void initPassengerList() {

			for (int i = 0; i < this.nPassengers; i++) {
				int pickUpX = (int) (Math.random() * (this.maxGridSize));
				int pickUpY = (int) (Math.random() * (this.maxGridSize));
				int dropOffX, dropOffY;
				do {
				    dropOffX = (int) (Math.random() * (this.maxGridSize));
				    dropOffY = (int) (Math.random() * (this.maxGridSize));
				}while(pickUpX == dropOffX && pickUpY == dropOffY);
				
				
				this.passengers.add(new Passenger("" + i, new Coordinate(pickUpX, pickUpY), new Coordinate(dropOffX, dropOffY)));
				
			}
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
		    	    x = (int) (Math.random() * maxGridSize);
		    	    y = (int) (Math.random() * maxGridSize);
		    	}while( this.environmentGrid.get(GridCell.getIndex(x, y)).hasObstacle() || 
		    			( x == this.depotLocation.getX() && y == this.depotLocation.getY()) ||
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
		case "get_ride": 
			getRide(agName);
			break;
		case "refuse_ride": 
			String refuseType = act.getTerm(0).getFunctor();
			
			refuseRide(agName, refuseType);
			break;
		case "park": 
			String parkType = act.getTerm(0).getFunctor();
			
			park(agName, parkType);
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
		case "call_emergency": 
			currentPosition.setX( Util.getIntTerm(act.getTerm(0)) );
			currentPosition.setY( Util.getIntTerm(act.getTerm(1)) );

			callEmergency(agName, currentPosition);
			break;
		case "get_assistance": 
			currentPosition.setX( Util.getIntTerm(act.getTerm(0)) );
			currentPosition.setY( Util.getIntTerm(act.getTerm(1)) );

			getAssistance(agName, currentPosition);
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
		
		char north = verifyObstacle(agName, "north", currentPosition);
		char south = verifyObstacle(agName, "south", currentPosition);
		char east =  verifyObstacle(agName, "east",  currentPosition);
		char west =  verifyObstacle(agName, "west",  currentPosition);
		
		int obstacleAround = 0;

		if(north != 'N') 
			obstacleAround++;
		if(south != 'N') 
			obstacleAround++;
		if(east != 'N') 
			obstacleAround++;
		if(west != 'N') 
			obstacleAround++;
		
		if(obstacleAround == 3 && collisionChance.get_choice()) {

			if(simulate) {
				try {
					TimeUnit.MILLISECONDS.sleep(waitTimeDefault);
				} catch(Exception e) {
					System.err.println(e);
				}
			}
			
		    	addObstacleDamage(agName, north, "north", currentPosition);
			addObstacleDamage(agName, south, "south", currentPosition);
			addObstacleDamage(agName, east, "east", currentPosition);
			addObstacleDamage(agName, west, "west", currentPosition);
				
			Predicate unavoidableCollision = new Predicate("unavoidable_collision");
			unavoidableCollision.addTerm(new NumberTermImpl(currentPosition.getX()));
			unavoidableCollision.addTerm(new NumberTermImpl(currentPosition.getY()));

			obstacleDamageRelated.add(unavoidableCollision);
				
			addPercept(agName, unavoidableCollision);
				
			if(simulate) {
				try {
					TimeUnit.MILLISECONDS.sleep(waitTimeDefault);
				} catch(Exception e) {
					System.err.println(e);
				}
			}
			
		}
	}
	
	
	/*
	 * Input:
	 * 		String agName: Agent's Name
	 * 		char typeObstacle: Identifies the type of obstacle in 'currentPosition'
	 * 			'N' - No obstacle 
	 * 			'O' - There is an obstacle
	 * 			'F' - There is an obstacle, but 'currentPosition' isn't a valid position for the environment's grid.
	 * 		String direction: Direction that 'currentPosition' is related to the agent's current location.
	 *  		Coordinate currentPosition: Coordinates surrounding the agent's current location.
	 *  
	 *  Process/Output:
	 *  		Classifies the damage level caused by an obstacle inside the environment and the agent perpects it.
	 * 
	 */
	
	private void addObstacleDamage(String agName, char typeObstacle, String direction, Coordinate currentPosition) {
		String currentDamageLevel = " ";
		
		if(typeObstacle != 'F') {
			currentDamageLevel = this.damageLevel.get_choice();
			
			Predicate obstacleDamage = new Predicate("obstacle_damage");
			obstacleDamage.addTerm(new NumberTermImpl(currentPosition.getX()));
			obstacleDamage.addTerm(new NumberTermImpl(currentPosition.getY()));
			obstacleDamage.addTerm(new Predicate(direction));
			
			if (typeObstacle == 'N') {
				Coordinate directionPosition = getDirectionCoordinate(direction, currentPosition);
				
				Predicate newObstacle = addObstacle("center", directionPosition);
				addPercept(agName, newObstacle);
				
				Predicate tempObstacle = new Predicate("temp_obstacle");
				tempObstacle.addTerm(new NumberTermImpl(directionPosition.getX()));
				tempObstacle.addTerm(new NumberTermImpl(directionPosition.getY()));
				addPercept(agName, tempObstacle);

				obstacleDamageRelated.add(newObstacle);
				obstacleDamageRelated.add(tempObstacle);
				
			}

			obstacleDamage.addTerm(new Predicate( currentDamageLevel ));
			
			System.err.println("Damage obstacle: " + obstacleDamage);

			if(simulate) {
				Coordinate directionPosition = getDirectionCoordinate(direction, currentPosition) ;
				
				Client.sendMessage( new String[] {"obstacleDamage", 
						String.valueOf(directionPosition.getX()),
						String.valueOf(directionPosition.getY()),
						currentDamageLevel
				} );
			}
			obstacleDamageRelated.add(obstacleDamage);
			addPercept(agName, obstacleDamage);
		}
		
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
		

		if (	surroundingPosition.getX() < 0 || surroundingPosition.getX() >= maxGridSize || 
				surroundingPosition.getY() < 0 || surroundingPosition.getY() >= maxGridSize) {
			hasObstacle = true;
			typeObstacle = 'F';
		} else {
			if (environmentGrid.get(GridCell.getIndex(surroundingPosition.getX(), surroundingPosition.getY())).hasObstacle()) {
				hasObstacle = true;
				typeObstacle = 'O';
				
				if(simulate) {
					Client.sendMessage( new String[] {"obstacle", 
							String.valueOf(surroundingPosition.getX()), String.valueOf(surroundingPosition.getY())}  );
				}
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
	 * Process agent's refusals to complete a ride, where 'refuseType' is the reason why.
	 */
	private void refuseRide(String agName, String refuseType) {

	    	if(simulate)
		{
			try {
				TimeUnit.MILLISECONDS.sleep(this.waitTimeDefault);
			} catch(Exception e) {
				System.err.println(e);
			}
		}
	    
		String type = "";
		
		switch (refuseType) {
		case "pick_up":
			type = "Pick up";
			break;
		case "drop_off":
			type = "Drop off";
			break;
		case "car_unavailable":
			type = "Car Unavailable - Total Loss";
			break;
		default:
			break;
		}
		
		if(simulate)
		{	

		    Client.sendMessage( 
				new String[] {"refuseRide", String.valueOf(car.getX()), String.valueOf(car.getY()), refuseType} );
		    try {
			TimeUnit.MILLISECONDS.sleep(this.waitTimeDefault);
		    } catch(Exception e) {
			System.err.println(e);
		    }
		}
		
		
		System.err.println(String.format("%s: %s can't finish ride for %s", type, agName, currentPassenger.getName()));
	}

	/*
	 * Agent park action
	 */
	private void park(String agName, String parkType) {


		switch (parkType) {
		case "pick_up":
			System.err.println(String.format("Pick Up Passanger %s in (%d,%d)", currentPassenger.getName(), car.getX(),
					car.getY()));
			break;
		case "drop_off":
			System.err.println(String.format("Drop Off Passanger %s in (%d,%d)\n", currentPassenger.getName(),
					car.getX(), car.getY()));
			break;
		case "depot":
			if(car.getX() == depotLocation.getX() && car.getY() == depotLocation.getY())
				System.err.println(String.format("%s is back to the depot.", agName));
			break;
		default:
			break;

		}

	}

	/*
	 * Provides the agent initial location
	 */
	private void localize(String agName) {

		System.err.println("Initializing GPS");
		System.err.println(String.format("Agent %s is at (%d,%d)", agName, car.getX(), car.getY()));

		addDepot(agName);

		updateLocation(agName, new Coordinate(0, 0), car);

	}

	/*
	 * Add depot location
	 */
	private void addDepot(String agName) {
		
		if(simulate)
		{
			Client.sendMessage(new String[] {"depot", String.valueOf(depotLocation.getX()), String.valueOf(depotLocation.getY())});
			
		}

		Predicate depot = new Predicate("depot");
		depot.addTerm(new NumberTermImpl(depotLocation.getX()));
		depot.addTerm(new NumberTermImpl(depotLocation.getY()));

		addPercept(agName, depot);

	}

	/*
	 * O método getRide recebe o seguintes argumento:
	 * String agName: O nome do agente.
	 * 
	 * A função remove as percepções referentes ao ponto de pegada, pick_up, e o ponto de destino, drop_off,do último passageiro.
	 * Na sequência, é verificado se ainda há algum passageiro disponível, onde tal informação é armazenada na array interna do ambiente, passengers.
	 * 
	 * Caso não haja nenhum outro passageiro, o ambiente adiciona uma percepção para o agente 
	 * informando que não há mais nenhuma corrida disponível, no_possible_new_ride.
	 * Caso exista alguma corrida disponível, o ambiente insere percepções referentes a tal corrida no agente agName, 
	 * e atualiza a variável de controle da lista de passageiros.
	 * 
	 * Em ambos os casos, o ambiente informa ao agente por meio da percepção ride_info que os dados sobre uma possível corrida foram atualizados. 
	 * 
	 */
	
	/*
	 * Provides a new ride to the agent and removes any current belief to previous passengers.
	 * If the passenger list is empty,  a belief 'no_possible_new_ride' and the agent stop its process.
	 */
	private void getRide(String agName) {
		
		Predicate pickUpLast = new Predicate("pick_up");
		pickUpLast.addTerm(new NumberTermImpl(currentPassenger.getPickUp().getX()));
		pickUpLast.addTerm(new NumberTermImpl(currentPassenger.getPickUp().getY()));

		Predicate dropOffLast = new Predicate("drop_off");
		dropOffLast.addTerm(new NumberTermImpl(currentPassenger.getDropOff().getX()));
		dropOffLast.addTerm(new NumberTermImpl(currentPassenger.getDropOff().getY()));

		removePercept(agName, pickUpLast);
		removePercept(agName, dropOffLast);

		if (passengers.isEmpty()) {

			Predicate noPossibleRide = new Predicate("no_possible_new_ride");
			addPercept(agName, noPossibleRide);

			System.err.println("No more available rides!");
		} else {
			currentPassenger = passengers.get(0);
			passengers.remove(0);

			int pickUpX = currentPassenger.getPickUp().getX();
			int pickUpY = currentPassenger.getPickUp().getY();

			int dropOffX = currentPassenger.getDropOff().getX();
			int dropOffY = currentPassenger.getDropOff().getY();
			
			if(simulate)
			{
				Client.sendMessage( 
					new String[] {"pickUp", String.valueOf(pickUpX), String.valueOf(pickUpY)} );
				Client.sendMessage( 
					new String[] {"dropOff", String.valueOf(dropOffX), String.valueOf(dropOffY)});
			}

			
			System.err.println(String.format("\n%s going to pick up  %s in (%d,%d)", agName, currentPassenger.getName(),
					pickUpX, pickUpY));
			System.err.println(String.format("%s going to drop off %s in (%d,%d)", agName, currentPassenger.getName(),
					dropOffX, dropOffY));

			Predicate pickUp = new Predicate("pick_up");
			pickUp.addTerm(new NumberTermImpl(pickUpX));
			pickUp.addTerm(new NumberTermImpl(pickUpY));

			Predicate dropOff = new Predicate("drop_off");
			dropOff.addTerm(new NumberTermImpl(dropOffX));
			dropOff.addTerm(new NumberTermImpl(dropOffY));

			addPercept(agName, pickUp);
			addPercept(agName, dropOff);
		}

		Predicate rideInfo = new Predicate("ride_info");
		addPercept(agName, rideInfo); 

	}
	
	
	/*
	 * O método callEmergency recebe os seguintes argumentos:
	 * String agName:
	 * Coordinate currentPosition: Coordenadas da posição de onde o agente está requisitando ajuda da emergência.
	 * 
	 * A funcão adiciona uma percepção para o agente de que a emergência das coordenadas currentPosition está sendo atendida.
	 * 
	 * Para controle interno do ambiente, o predicado emergency é inserido em um ArrayList obstacleDamageRelated 
	 * para que possa ser removido futuramente quando necessário.
	 */
	
	/*
	 * 'Call emergency' to handle crashes.
	 */
	private void callEmergency(String agName, Coordinate currentPosition) {

		System.err.println(String.format("%s crashed in (%d,%d). Calling Emergency.", agName, currentPosition.getX(), currentPosition.getY()));

		Predicate emergency = new Predicate("emergency");
		emergency.addTerm(new NumberTermImpl(currentPosition.getX()));
		emergency.addTerm(new NumberTermImpl(currentPosition.getY()));

		this.obstacleDamageRelated.add(emergency);
		
		addPercept(agName, emergency);

	}
	

	/*
	 * Waits for 'emergency' assistance after a crash.
	 */
	private void getAssistance(String agName, Coordinate currentPosition) {
		
		for(Predicate obstacleDamage : this.obstacleDamageRelated) {
			removePercept(agName, obstacleDamage);
		}
		this.obstacleDamageRelated.clear();
		
		if(simulate) {
			try {
				TimeUnit.MILLISECONDS.sleep(this.waitTimeDefault);
			} catch(Exception e) {
				System.err.println(e);
			}
		}
		
		if(simulate) {
			Client.sendMessage( new String[] {"removeObstacleDamage"} );
		}
		
		Predicate assisted = new Predicate("assisted");
		assisted.addTerm(new NumberTermImpl(currentPosition.getX()));
		assisted.addTerm(new NumberTermImpl(currentPosition.getY()));
		addPercept(agName, assisted);
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
