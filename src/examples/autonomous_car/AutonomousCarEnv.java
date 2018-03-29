package autonomous_car;

import java.util.concurrent.TimeUnit;
import ail.mas.DefaultEnvironment;
import ail.mas.MAS;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.util.AILexception;

public class AutonomousCarEnv extends DefaultEnvironment{
	
	
	// Initial position of the car
	private int car_x;
	private int car_y;
	
	private int velocity;
	
	private int sensor;
	
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

	private boolean simulate; // Determines if the environment should send message to the simulator
	private int waitTimeDefault; // Wait time between messages
	private int waitTimeLocation; // Wait time to send first message
	
	// Environment setup
	@Override
	public void setMAS(MAS m) {
		super.setMAS(m);
			
		// Info about agent/vehicle - Default Values
		this.car_x = 0;
		this.car_y = 0;
		this.velocity = 1;
		
		this.sensor = 9;
		
		// 1st obstacle
		this.obs1_x = 20;
		this.obs1_y = 0;
		
		//2nd obstacle
		this.obs2_x = 30;
		this.obs2_y = 1;
		
		//3rd obstacle
		this.obs3_x = 40;
		this.obs3_y = 1;

		//4th obstacle
		this.obs4_x = 50;
		this.obs4_y = 0;

		this.waitTimeDefault = 750; 
		this.waitTimeLocation = 300; 	

		this.simulate = true; 
	
		// Simulator Setup
		if(simulate) {
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obs1_x), String.valueOf(obs1_y)} ) );
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obs2_x), String.valueOf(obs2_y)} ) );
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obs3_x), String.valueOf(obs3_y)} ) );
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obs4_x), String.valueOf(obs4_y)} ) );
			    
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch(Exception e) {
			    System.err.println(e);
			}
		}
		
		Predicate start = new Predicate("start");
		addPercept("car", start);


	}
	
	// Identifies agents' actions
	public Unifier executeAction(String agName, Action act) throws AILexception {
		
		Unifier u = new Unifier();
		
		if(act.getFunctor().equals("run")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			// car_x is not altered
			car_x+=velocity; // increment one in the X axis
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car_x));
			at.addTerm(new NumberTermImpl(car_y));
			
			System.err.println("MOVING " + car_x + " " + car_y);
			
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent
			
			if(car_y == obs1_y && car_x < obs1_x && car_x+sensor >= obs1_x) {
				Predicate go_right = new Predicate("go_right");
				addPercept(agName, go_right);
			} 
			else if(car_y == obs2_y && car_x < obs2_x && car_x+sensor >= obs2_x) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car_y == obs3_y && car_x < obs3_x && car_x+sensor >= obs3_x) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car_y == obs4_y && car_x < obs4_x && car_x+sensor >= obs4_x) {
				Predicate go_left = new Predicate("go_right");
				addPercept(agName, go_left);
			}

			Predicate going_forward = new Predicate("going_forward");
			addPercept(agName, going_forward);
		}
		else if (act.getFunctor().equals("sensor_enable")) {

			if(car_x+sensor == obs1_x && (car_x+sensor == obs2_x || car_x+sensor == obs3_x || car_x+sensor == obs4_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car_x+sensor == obs2_x && (car_x+sensor == obs1_x || car_x+sensor == obs3_x || car_x+sensor == obs4_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car_x+sensor == obs3_x && (car_x+sensor == obs1_x || car_x+sensor == obs2_x || car_x+sensor == obs4_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car_x+sensor == obs4_x && (car_x+sensor == obs1_x || car_x+sensor == obs2_x || car_x+sensor == obs3_x)) {
				Predicate no_way = new Predicate("no_way");
				addPercept(agName, no_way);
			}
			else if(car_x+sensor == obs1_x && car_y == obs1_y) {
				Predicate go_right = new Predicate("go_right");
				addPercept(agName, go_right);
			} 
			else if(car_x+sensor == obs2_x && car_y == obs2_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car_x+sensor == obs3_x && car_y == obs3_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}
			else if(car_x+sensor == obs4_x && car_y == obs4_y) {
				Predicate go_left = new Predicate("go_right");
				addPercept(agName, go_left);
			}

		}
		else if(act.getFunctor().equals("right")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			// car_y is not altered
			car_x+=velocity;
			car_y++; // increment one in the Y axis
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car_x));
			at.addTerm(new NumberTermImpl(car_y));
			
			System.err.println("CHANGED LANE " + car_x + " " + car_y);
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent

			Predicate go_right = new Predicate("go_right");
			removePercept(agName, go_right);
			
			Predicate going_forward = new Predicate("going_forward");
			addPercept(agName, going_forward);
		}
		else if(act.getFunctor().equals("left")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			// car_y is not altered
			car_x+=velocity;
			car_y--; // increment one in the Y axis
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car_x));
			at.addTerm(new NumberTermImpl(car_y));
			
			System.err.println("CHANGED LANE " + car_x + " " + car_y);
			
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
				{"carLocation", String.valueOf(car_x), String.valueOf(car_y)} ) );
			
		}
		
		return u;
		
	}

}

