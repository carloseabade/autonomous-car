package autonomous_car_2;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import ail.mas.DefaultEnvironmentwRandomness;
import ail.mas.MAS;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.util.AILexception;

public final class AutonomousCarEnv extends DefaultEnvironmentwRandomness{
	
	
	private Coordinate car = new Coordinate(0, 0); // Current coordinates of the agent/vehicle
	private int velocity;
	private int sensor; // Sensor range
	
	private int nObstacles; // Total of static obstacles
	
	private static ArrayList<Coordinate> obstacles = new ArrayList<Coordinate>(); 
	
	private boolean simulate; // Determines if the environment should send message to the simulator
	private int waitTimeLocation; // Wait time to send first message
	
	// 1st obstacle
	private int obs1_x;
	private int obs1_y;
	
	//2nd obstacle
	private int obs2_x;
	private int obs2_y;
	
	//3rd obstacle
	private int obs3_x;
	private int obs3_y;

	//4th obstacle
	private int obs4_x;
	private int obs4_y;

	
	// Environment setup
	@Override
	public void setMAS(MAS m) {
		super.setMAS(m);
			
		// Info about agent/vehicle - Default Values
		
		this.velocity = 1;
		
		this.sensor = 6;
		
		this.nObstacles = 5;
		
		this.simulate = true;
		
		this.waitTimeLocation = 300; 	

		// 1st obstacle
		this.obs1_x = 6;
		this.obs1_y = 0;
		
		//2nd obstacle
		this.obs2_x = 20;
		this.obs2_y = 1;
		
		//3rd obstacle
		this.obs3_x = 28;
		this.obs3_y = 1;

		//4th obstacle
		this.obs4_x = 40;
		this.obs4_y = 0;

		initObstacles(); // Obstacles (Coordinates)
		
		Predicate start = new Predicate("start");
		addPercept("car", start);


	}
	
	public static ArrayList<Coordinate> getObstacles(){
		return obstacles;
	}
	
	/*
	 * Set 'nObstacles' in the environment randomly.
	 * Method checks if the coordinate (x,y) doesn't have a obstacle already have an obstacle.
	 * Guarantees that there isn't an obstacle in the agent's initial position and in the depot's location.
	 */
	private void initObstacles() {
		
		Coordinate c = new Coordinate(0, 0);
		int x = 6;
		int y = 0;
		
		c.setX(x);
	    c.setY(y);
	    
	    obstacles.add(c);
		
		/*for(int i = 0; i < this.nObstacles; i++) {
		    	    x = (int) (Math.random() * 100 + car.getX());
		    	    y = (int) (Math.random() * 4);
		    	    
		    	    c.setX(x);
		    	    c.setY(y);
		    	    
		    	    obstacles.add(c);
		}*/
	}

	
	// Identifies agents' actions
	public Unifier executeAction(String agName, Action act) throws AILexception {
		
		Unifier u = new Unifier();
		
		if(act.getFunctor().equals("run")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car.getX()));
			old_position.addTerm(new NumberTermImpl(car.getY()));
			
			// car_x is not altered
			car.setX(car.getX() + velocity); // increment one in the X axis
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car.getX()));
			at.addTerm(new NumberTermImpl(car.getY()));
			
			System.err.println("MOVING " + car.getX() + " " + car.getY());
			
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent
			
			if(car.getX()+sensor == obs1_x && car.getY() == obs1_y) {
				Predicate go_right = new Predicate("go_right");
				addPercept(agName, go_right);
			} 
			else if(car.getX()+sensor == obs2_x && car.getY() == obs2_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car.getX()+sensor == obs3_x && car.getY() == obs3_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car.getX()+sensor == obs4_x && car.getY() == obs4_y) {
				Predicate go_left = new Predicate("go_right");
				addPercept(agName, go_left);
			}

			Predicate going_forward = new Predicate("going_forward");
			addPercept(agName, going_forward);
		}
		else if (act.getFunctor().equals("sensor_enable")) {

			if(car.getX()+sensor == obs1_x && (car.getX()+sensor == obs2_x || car.getX()+sensor == obs3_x || car.getX()+sensor == obs4_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car.getX()+sensor == obs2_x && (car.getX()+sensor == obs1_x || car.getX()+sensor == obs3_x || car.getX()+sensor == obs4_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car.getX()+sensor == obs3_x && (car.getX()+sensor == obs1_x || car.getX()+sensor == obs2_x || car.getX()+sensor == obs4_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car.getX()+sensor == obs4_x && (car.getX()+sensor == obs1_x || car.getX()+sensor == obs2_x || car.getX()+sensor == obs3_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car.getX()+sensor == obs1_x && car.getX() == obs1_y) {
				Predicate go_right = new Predicate("go_right");
				addPercept(agName, go_right);
			} 
			else if(car.getX()+sensor == obs2_x && car.getX() == obs2_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car.getX()+sensor == obs3_x && car.getX() == obs3_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car.getX()+sensor == obs4_x && car.getX() == obs4_y) {
				Predicate go_left = new Predicate("go_right");
				addPercept(agName, go_left);
			}

		}
		else if(act.getFunctor().equals("right")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car.getX()));
			old_position.addTerm(new NumberTermImpl(car.getY()));
			
			car.setX(car.getX() + velocity);
			car.setY(car.getY() + velocity); 
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car.getX()));
			at.addTerm(new NumberTermImpl(car.getY()));
			
			System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent

			Predicate go_right = new Predicate("go_right");
			removePercept(agName, go_right);
			
			Predicate going_forward = new Predicate("going_forward");
			addPercept(agName, going_forward);
		}
		else if(act.getFunctor().equals("left")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car.getX()));
			old_position.addTerm(new NumberTermImpl(car.getY()));
			

			car.setX(car.getX() + velocity);
			car.setY(car.getY() + velocity); 
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car.getX()));
			at.addTerm(new NumberTermImpl(car.getY()));
			
			System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent

			Predicate go_left = new Predicate("go_left");
			removePercept(agName, go_left);
			
			Predicate going_forward = new Predicate("going_forward");
			addPercept(agName, going_forward);
		}
		else if(act.getFunctor().equals("stop")) {
			Predicate stopped = new Predicate("stopped");
			addPercept(agName, stopped);
		}
		
		super.executeAction(agName, act);
		
		if(simulate) {
		    
		    try {
			TimeUnit.MILLISECONDS.sleep(waitTimeLocation);
		} catch(Exception e) {
			System.err.println(e);
		}
			Client.sendMessage( Client.convertArray2String( new String[] 
				{"carLocation", String.valueOf(car.getX()), String.valueOf(car.getY())} ) );
			
		}
		
		return u;
		
	}

}

