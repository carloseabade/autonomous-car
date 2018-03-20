// ----------------------------------------------------------------------------
// Copyright (C) 2014 Louise A. Dennis and Michael Fisher 
// 
// This file is part of Gwendolen
//
// Gwendolen is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// Gwendolen is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with Gwendolen if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//----------------------------------------------------------------------------

package autonomous_car;

import java.util.concurrent.TimeUnit;

import _003_first_destination.Client;
import ail.mas.DefaultEnvironment;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.util.AILexception;

public class AutonomousCarEnv extends DefaultEnvironment{
	
	
	// Initial position of the car
	private int car_x = 0;
	private int car_y = 0;
	
	// 1st obstacle
	private int obs1_x = 10;
	private int obs1_y = 0;
	
	// 2nd obstacle
	private int obs2_x = 35;
	private int obs2_y = 1;
	
	private boolean simulate = true; // Determines if the environment should send message to the simulator
	private int waitTimeDefault = 750; // Wait time between messages
	private int waitTimeLocation = 300; // Wait time to send first message
	
	// Identifies agents' actions
	public Unifier executeAction(String agName, Action act) throws AILexception {
		
		Unifier u = new Unifier();
		
		if(act.getFunctor().equals("run")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			// car_x is not altered
			car_x++; // increment one in the X axis
			
			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car_x));
			at.addTerm(new NumberTermImpl(car_y));
			
			System.err.println("MOVING " + car_x + " " + car_y);
			
			
			removePercept(agName, old_position); //remove old position
			addPercept(agName, at); //inform new position to the agent
						
			if(car_x == obs1_x-5 && car_y == obs1_y) {
				Predicate go_left = new Predicate("go_left");
				addPercept(agName, go_left);
			}

			if(car_x == obs2_x-5 && car_y == obs2_y) {
				Predicate go_right = new Predicate("go_right");
				addPercept(agName, go_right);
			}
			
			Predicate going_forward = new Predicate("going_forward");
			addPercept(agName, going_forward);
		}
		else if(act.getFunctor().equals("left")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			// car_y is not altered
			car_y++; // increment one in the Y axis
			
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
		else if(act.getFunctor().equals("right")) {
			
			Predicate old_position = new Predicate("at");
			old_position.addTerm(new NumberTermImpl(car_x));
			old_position.addTerm(new NumberTermImpl(car_y));
			
			// car_y is not altered
			car_y--; // increment one in the Y axis
			
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
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obs1_x), String.valueOf(obs1_y)} ) );
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"obsLocation", String.valueOf(obs2_x), String.valueOf(obs2_y)} ) );
		}
		
		return u;
		
	}

}

