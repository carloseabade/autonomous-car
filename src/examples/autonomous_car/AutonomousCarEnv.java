package autonomous_car;

import java.util.concurrent.TimeUnit;
import ail.mas.DefaultEnvironment;
import ail.mas.MAS;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.util.AILexception;
import autonomous_car_2.Coordinate;

public class AutonomousCarEnv extends DefaultEnvironment{
	
	
	private Coordinate car = new Coordinate(0, 0); //Current coordinates of the agent/vehicle
	private int velocity; //Agent/vehicle velocity
	private int sensor; //Sensor range
	private int nObstacles; // Total of static obstacles
	private Coordinate obstacle1 = new Coordinate(10,0);
	private Coordinate obstacle2 = new Coordinate(10,1);
	private boolean simulate; // Determines if the environment should send message to the simulator
	private int waitTimeLocation; // Wait time to send first message
	
	// Environment setup
	@Override
	public void setMAS(MAS m) {
		super.setMAS(m);
			
this.velocity = 1;
		
		this.sensor = 9;
		
		this.nObstacles = 2;
		
		this.simulate = true;
		
		this.waitTimeLocation = 300; 	

		initObstacle(); // Obstacles (Coordinates)
		
		Predicate start = new Predicate("start");
		addPercept("car", start);
		
		Predicate lane0 = new Predicate("lane0");
		addPercept("car", lane0);
		
	}
	
	/*
	 * Set 'nObstacles' in the environment randomly.
	 */
	private void initObstacle() {
		
		int x;
		int y;
		
		if (nObstacles > 0) {
			do {
				x = obstacle1.getX() + (int)(Math.random() * 31);
				y = (int) (Math.random() * 2);
			}while (x == 0 || x < obstacle1.getX() || (x == obstacle1.getX() && y == obstacle1.getY() && x == obstacle2.getX() && y == obstacle2.getY()));

			obstacle1.setX(x);
			obstacle1.setY(y);

		}
		
		nObstacles--;

		if (nObstacles > 0) {
			do {					
				x = obstacle2.getX() + (int)(Math.random() * 31);
				y = (int) (Math.random() * 2);
			}while (x == 0 || x < obstacle2.getX() || (x == obstacle2.getX() && y == obstacle2.getY() && x == obstacle1.getX() && y == obstacle1.getY()));

				obstacle2.setX(x);
				obstacle2.setY(y);

		}
		
		// Simulator Setup
		if(simulate) {
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obstacle1.getX()), String.valueOf(obstacle1.getY())} ) );
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obstacle2.getX()), String.valueOf(obstacle2.getY())} ) );
			    
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch(Exception e) {
			    System.err.println(e);
			}
		}
		
		System.out.println("Obstacle1 X: " + obstacle1.getX());
		System.out.println("Obstacle1 Y: " + obstacle1.getY());
		System.out.println("Obstacle2 X: " + obstacle2.getX());
		System.out.println("Obstacle2 Y: " + obstacle2.getY());
		
	}

	
	// Identifies agents' actions
	// Identifies agents' actions
		public Unifier executeAction(String agName, Action act) throws AILexception {
			
			Unifier u = new Unifier();
			
			if(act.getFunctor().equals("run")) {
				
				Predicate old_position = new Predicate("at");
				old_position.addTerm(new NumberTermImpl(car.getX()));
				old_position.addTerm(new NumberTermImpl(car.getY()));
				
				car.setX(car.getX() + velocity);
				
				Predicate at = new Predicate("at");
				at.addTerm(new NumberTermImpl(car.getX()));
				at.addTerm(new NumberTermImpl(car.getY()));
				
				System.err.println("MOVING " + car.getX() + " " + car.getY());
				
				removePercept(agName, old_position); //remove old position
				addPercept(agName, at); //inform new position to the agent

				Predicate going_forward = new Predicate("going_forward");
				addPercept(agName, going_forward);

				
			}
			else if (act.getFunctor().equals("check_env")) {

				if(car.getX()+sensor >= obstacle1.getX() && car.getX()+sensor >= obstacle2.getX() && car.getX() < obstacle1.getX() && car.getX() < obstacle2.getX()) {
					Predicate obs1 = new Predicate("obs1");
					
					addPercept(agName, obs1);
					
					Predicate obs2 = new Predicate("obs2");
					
					addPercept(agName, obs2);
					
				} else if(car.getX()+sensor == obstacle2.getX() && car.getY() == obstacle2.getY()) {
					Predicate obs2 = new Predicate("obs2");
					
					Predicate same_lane = new Predicate("same_lane");
					
					addPercept(agName, same_lane);
					addPercept(agName, obs2);
					
				} else if(car.getX()+sensor == obstacle1.getX() && car.getY() == obstacle1.getY()){
					Predicate obs1 = new Predicate("obs1");
					
					Predicate same_lane = new Predicate("same_lane");
					
					addPercept(agName, same_lane);
					addPercept(agName, obs1);	
				}

			}
			else if(act.getFunctor().equals("go_right")) {
				
				Predicate old_position = new Predicate("at");
				old_position.addTerm(new NumberTermImpl(car.getX()));
				old_position.addTerm(new NumberTermImpl(car.getY()));
				
				if(car.getX()+sensor == obstacle2.getX() && car.getY() == obstacle2.getY()) {
					Predicate obs2 = new Predicate("obs2");
					
					Predicate same_lane = new Predicate("same_lane");
					
					removePercept(agName, same_lane);
					removePercept(agName, obs2);
				} else if(car.getX()+sensor == obstacle1.getX() && car.getY() == obstacle1.getY()) {
					Predicate obs1 = new Predicate("obs1");
					
					Predicate same_lane = new Predicate("same_lane");
					
					removePercept(agName, same_lane);
					removePercept(agName, obs1);	
				}
				
				car.setX(car.getX() + velocity);
				car.setY(car.getY() + 1); 
				
				Predicate at = new Predicate("at");
				at.addTerm(new NumberTermImpl(car.getX()));
				at.addTerm(new NumberTermImpl(car.getY()));
				
				System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());
				
				Predicate lane1 = new Predicate("lane1");
				Predicate lane0 = new Predicate("lane0");
				
				addPercept(agName, lane1);
				removePercept(agName, lane0);
				removePercept(agName, old_position); //remove old position
				addPercept(agName, at); //inform new position to the agent

				
				Predicate going_forward = new Predicate("going_forward");
				addPercept(agName, going_forward);
			}
			else if(act.getFunctor().equals("go_left")) {
				
				Predicate old_position = new Predicate("at");
				old_position.addTerm(new NumberTermImpl(car.getX()));
				old_position.addTerm(new NumberTermImpl(car.getY()));
				
				if(car.getX()+sensor == obstacle2.getX() && car.getY() == obstacle2.getY()) {
					Predicate obs2 = new Predicate("obs2");
					
					Predicate same_lane = new Predicate("same_lane");
					
					removePercept(agName, same_lane);
					removePercept(agName, obs2);
				} else if(car.getX()+sensor == obstacle1.getX() && car.getY() == obstacle1.getY()) {
					Predicate obs1 = new Predicate("obs1");
					
					Predicate same_lane = new Predicate("same_lane");
					
					removePercept(agName, same_lane);
					removePercept(agName, obs1);				
				}

				car.setX(car.getX() + velocity);
				car.setY(car.getY() - 1); 
				
				Predicate at = new Predicate("at");
				at.addTerm(new NumberTermImpl(car.getX()));
				at.addTerm(new NumberTermImpl(car.getY()));
				
				System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());
				
				Predicate lane1 = new Predicate("lane1");
				Predicate lane0 = new Predicate("lane0");
				
				addPercept(agName, lane0);
				removePercept(agName, lane1);
				
				removePercept(agName, old_position); //remove old position
				addPercept(agName, at); //inform new position to the agent
				
				Predicate going_forward = new Predicate("going_forward");
				addPercept(agName, going_forward);
			}
			else if(act.getFunctor().equals("stop")) {
				Predicate stopped = new Predicate("stopped");
				addPercept(agName, stopped);
				
				Predicate start = new Predicate("start");
				removePercept(agName, start);
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

