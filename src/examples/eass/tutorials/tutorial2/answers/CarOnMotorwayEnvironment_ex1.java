package eass.tutorials.tutorial2.answers;

import java.io.IOException;

import eass.mas.DefaultEASSEnvironment;
import ail.mas.scheduling.NActionScheduler;
import ail.syntax.Literal;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.syntax.Action;
import ail.util.AILSocketClient;
import ail.util.AILexception;
import ajpf.util.AJPFLogger;

/**
 * This is an environment for connecting with the simple Java Motorway Simulation for tutorial purposes.
 * @author louiseadennis
 *
 */
public class CarOnMotorwayEnvironment_ex1 extends DefaultEASSEnvironment {
	
	String logname = "eass.tutorials.tutorial1.CarOnMotorwayEnvironment";
		
	/**
	 * Socket that connects to the Physical Engine.
	 */
	protected AILSocketClient socket;

	/**
	 * Has the environment finished?
	 */
	private boolean finished = false;
	
	private boolean in_lane_1 = true;
	private boolean in_lane_2 = false;
	private boolean in_transit = false;

	/**
	 * Constructor.
	 */
	public CarOnMotorwayEnvironment_ex1() {
		super();
		super.scheduler_setup(this,  new NActionScheduler(100));
		AJPFLogger.info("eass.mas", "Waiting Connection");
		try {
			socket = new AILSocketClient();
		} catch (IOException e) {
			AJPFLogger.severe(logname, e.getMessage());
			System.exit(0);
		}
		AJPFLogger.info(logname, "Connected to Socket");
	}
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#do_job()
	 */
	public void do_job() {
		try {
			if (socket.allok()) {
				readPredicatesfromSocket();
			}
		} catch (Exception e) {
			AJPFLogger.warning(logname, e.getMessage());
		}
	}

	int lane;
	/**
	 * Reading the values from the sockets and turning them into perceptions.
	 */
	public void readPredicatesfromSocket() {
		
		try {
			if (socket.pendingInput()) {

				double x = socket.readDouble();
				socket.readDouble();
				double xdot = socket.readDouble();
				double ydot = socket.readDouble();
				int started = socket.readInt();
				lane = socket.readInt();
				try {
					while (socket.pendingInput()) {
						x = socket.readDouble();
						socket.readDouble();
						xdot = socket.readDouble();
						ydot = socket.readDouble();
						started = socket.readInt();	
						lane = socket.readInt();
					}
				} catch (Exception e) {
					AJPFLogger.warning(logname, e.getMessage());
				} 
				
				Literal xpos = new Literal("xpos");
				xpos.addTerm(new NumberTermImpl(x));
				
				if (started > 0) {
					addPercept(new Literal("started"));
				}
									
				addUniquePercept("xpos", xpos);

			}
		} catch (Exception e) {
			AJPFLogger.warning(logname, e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#executeAction(java.lang.String, ail.syntax.Action)
	 */
	public Unifier executeAction(String agName, Action act) throws AILexception {
		Unifier u = new Unifier();
		
		if (act.getFunctor().equals("maintain_speed")) {
			socket.writeDouble(0.0);
		} else if (act.getFunctor().equals("finished")) {
			finished = true;
		} else if (act.getFunctor().equals("change_lane1")) {
			if(in_lane_2) {
				removePercept(agName, new Predicate("in_lane_2"));
				in_transit = true;
			}
			socket.writeDouble(-0.2);
		} else if (act.getFunctor().equals("change_lane2")) {
			if(in_lane_1) {
				removePercept(agName, new Predicate("in_lane_1"));
				in_transit = true;
			}
			socket.writeDouble(0.2);
			
			Literal lane2 = new Literal("lane2");
			lane2.addTerm(new NumberTermImpl(lane*4));
			addUniquePercept("lane2", lane2);
			
		} else if (act.getFunctor().equals("stay_in_lane")) {
			socket.writeDouble(-0.1);
			if(in_lane_1 && in_transit) {
				in_lane_1 = false;
				in_lane_2 = true;
				
				
			}
			if(in_lane_2 && in_transit) {
				in_lane_2 = false;
				in_lane_1 = true;
				
				Literal lane1 = new Literal("lane1");
				lane1.addTerm(new NumberTermImpl(lane*4));
				addUniquePercept("lane1", lane1);

			}
			in_transit = false;
		}
		
		u.compose(super.executeAction(agName, act));
		return u;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see ail.mas.DefaultEnvironment#finalize()
	 */
	public void finalize() {
		socket.close();
	}

	/*
	 * (non-Javadoc)
	 * @see ail.others.DefaultEnvironment#done()
	 */
	public boolean done() {
		if (finished) {
			return true;
		}
		return false;
	}

}
