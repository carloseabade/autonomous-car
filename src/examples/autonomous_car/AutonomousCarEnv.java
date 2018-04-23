package autonomous_car;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import ail.mas.DefaultEnvironment;
import ail.mas.MAS;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.util.AILexception;

public class AutonomousCarEnv extends DefaultEnvironment{
	
	private Simulator simulator = new Simulator();
	
	private Coordinate car = new Coordinate(0, 0); //Current coordinates of the agent/vehicle
	private int velocity; //Agent/vehicle velocity
	private int sensor; //Sensor range
	
	private int nObstacles; //Total of static obstacles
	private ArrayList<Coordinate> obstacles;
	private Coordinate obstacle1;
	private Coordinate obstacle2;
	
	private boolean simulate; //Determines if the environment should send message to the simulator
	private int waitTimeLocation; //Wait time to send first message
	private int controlObstacles = 0; 
	
	public AutonomousCarEnv() {
		new Thread(new Runnable() {
		     public void run() {
		          simulator.startAnimation();
		     }
		}).start();
	}
	
	// Environment setup
	@Override
	public void setMAS(MAS m) {
		
//		while(!simulator.getAnimate()) {}

		super.setMAS(m);
			
		this.velocity = 1;
		
		this.sensor = 9;
		
		this.nObstacles = 3;
		
		this.simulate = true;
		
		this.waitTimeLocation = 300;
		
		obstacles = new ArrayList<Coordinate>(nObstacles);

		initObstacle(); //Initiate Obstacles (Coordinates)
		
		setObstacles(); //Set Obstacles (Coordinates)
		
		Predicate start = new Predicate("start");
		addPercept("car", start);
		
	}
	
	/*
	 * Set 'nObstacles' in the environment randomly.
	 */
	private void initObstacle() {

		int i = 1;
		
		Coordinate aux = new Coordinate((9 + (int)(Math.random() * 31)),((int) (Math.random() * 2)));		
		
		obstacles.add(new Coordinate(aux.getX(), aux.getY()));
//		obstacles.add(new Coordinate(20,0));
//		obstacles.add(new Coordinate(33,1));
//		obstacles.add(new Coordinate(39,1));
		
		while (i < nObstacles) {
			do {
				aux.setX(obstacles.get(i-1).getX() + (int)(Math.random() * 31));
				aux.setY((int) (Math.random() * 2));
			} while (obstacles.get(i-1).getX() == aux.getX() && obstacles.get(i-1).getY() == aux.getY());
			
			obstacles.add(new Coordinate(aux.getX(), aux.getY()));
			
			i++;
		}
		
		i = 0;
		
		// Simulator Setup
		simulator.setObstacles(obstacles);
		
	}
	
	private void setObstacles() {
		
		obstacle1 = obstacles.get(controlObstacles);
		System.out.println("Obstacle1 X: " + obstacle1.getX());
		System.out.println("Obstacle1 Y: " + obstacle1.getY());
		
		if(controlObstacles+1 < obstacles.size()) {
			obstacle2 = obstacles.get(controlObstacles+1);
			System.out.println("Obstacle2 X: " + obstacle2.getX());
			System.out.println("Obstacle2 Y: " + obstacle2.getY());
		}
		
		controlObstacles += 2;
		
	}
	
	private void removeOldPosition(String agName) {
		
		Predicate old_position = new Predicate("at");
		old_position.addTerm(new NumberTermImpl(car.getX()));
		old_position.addTerm(new NumberTermImpl(car.getY()));
		
		removePercept(agName, old_position); //remove old position
	}
	
private void removeObstaclePredicate(String agName) {
		
	if(car.getX()+sensor == obstacle2.getX() && car.getY() == obstacle2.getY()) {
		Predicate obs2 = new Predicate("obs2");
		obs2.addTerm(new NumberTermImpl(obstacle2.getX()));
		obs2.addTerm(new NumberTermImpl(obstacle2.getY()));
		
		removePercept(agName, obs2);
	} else if(car.getX()+sensor == obstacle1.getX() && car.getY() == obstacle1.getY()) {
		Predicate obs1 = new Predicate("obs1");
		obs1.addTerm(new NumberTermImpl(obstacle1.getX()));
		obs1.addTerm(new NumberTermImpl(obstacle1.getY()));

		removePercept(agName, obs1);	
	}
}
	
	private void addNewPosition(String agName) {
		
		Predicate at = new Predicate("at");
		at.addTerm(new NumberTermImpl(car.getX()));
		at.addTerm(new NumberTermImpl(car.getY()));
		
		addPercept(agName, at); //inform new position to the agent

		Predicate going_forward = new Predicate("going_forward");
		
		addPercept(agName, going_forward);
	}
	
	private void sendMessageSimulator() {
		
		simulator.setCarLocation(car);
		
	}


	// Identifies agents' actions
		public Unifier executeAction(String agName, Action act) throws AILexception {
			
			Unifier u = new Unifier();
			
			if(car.getX() > obstacle1.getX() && car.getX() > obstacle2.getX() && controlObstacles < obstacles.size()) {
				setObstacles();
			}
			
			if(act.getFunctor().equals("run")) {
				
				removeOldPosition(agName);
				
				car.setX(car.getX() + velocity);
				
				System.err.println("MOVING " + car.getX() + " " + car.getY());
				
				addNewPosition(agName);
				
				sendMessageSimulator();
	
			}
			else if (act.getFunctor().equals("check_env")) {

				if(car.getX()+sensor >= obstacle1.getX() && car.getX()+sensor >= obstacle2.getX() && car.getX() < obstacle1.getX()+5 && car.getX() < obstacle2.getX()+5 && obstacle1.getY() != obstacle2.getY()) {
					Predicate obs1 = new Predicate("obs1");
					obs1.addTerm(new NumberTermImpl(obstacle1.getX()));
					obs1.addTerm(new NumberTermImpl(obstacle1.getY()));
					addPercept(agName, obs1);
					
					Predicate obs2 = new Predicate("obs2");
					obs2.addTerm(new NumberTermImpl(obstacle2.getX()));
					obs2.addTerm(new NumberTermImpl(obstacle2.getY()));
					addPercept(agName, obs2);
					
				} else if(car.getX()+sensor == obstacle2.getX() && car.getY() == obstacle2.getY()) {
					Predicate obs2 = new Predicate("obs2");
					obs2.addTerm(new NumberTermImpl(obstacle2.getX()));
					obs2.addTerm(new NumberTermImpl(obstacle2.getY()));
					
					addPercept(agName, obs2);
					
				} else if(car.getX()+sensor == obstacle1.getX() && car.getY() == obstacle1.getY()){
					Predicate obs1 = new Predicate("obs1");
					obs1.addTerm(new NumberTermImpl(obstacle1.getX()));
					obs1.addTerm(new NumberTermImpl(obstacle1.getY()));
					
					addPercept(agName, obs1);	
				} 

			}
			else if(act.getFunctor().equals("go_right")) {
				
				removeOldPosition(agName);
				
				removeObstaclePredicate(agName);
				
				car.setX(car.getX() + velocity);
				car.setY(car.getY() + 1); 
				
				System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());
				
				addNewPosition(agName);			    
				
				sendMessageSimulator();
				
			} 
			else if(act.getFunctor().equals("go_left")) {
				
				removeOldPosition(agName);

				removeObstaclePredicate(agName);
				
				car.setX(car.getX() + velocity);
				car.setY(car.getY() - 1); 
				
				System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());
				
				addNewPosition(agName);
				
				sendMessageSimulator();
				
			}
			else if(act.getFunctor().equals("stop")) {
				Predicate stopped = new Predicate("stopped");
				addPercept(agName, stopped);
				
				Predicate start = new Predicate("start");
				removePercept(agName, start);
				
				sendMessageSimulator();
				
			}
			
			super.executeAction(agName, act);
			
			return u;
			
		}

}

