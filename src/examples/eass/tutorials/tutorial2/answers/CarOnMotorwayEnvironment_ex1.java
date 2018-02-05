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

	int x = -1;

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

	int lane = -1;
	int started;
	/**
	 * Reading the values from the sockets and turning them into perceptions.
	 */
	public void readPredicatesfromSocket() {
		
		try {
			if (socket.pendingInput() && x == -1 && lane == -1){
				x = socket.readInt();
				socket.readInt();
				socket.readInt();
				socket.readInt();
				started = socket.readInt();
				lane = socket.readInt();
				
				if (started > 0) {
					addPercept(new Literal("started"));
				}
				
				
			} else if (socket.pendingInput() && x<(lane*2-2)) {
				try {
					while (socket.pendingInput()) {
						x = socket.readInt();
						socket.readInt();
						socket.readInt();
						socket.readInt();
						started = socket.readInt();	
						lane = socket.readInt();
					}
				} catch (Exception e) {
					AJPFLogger.warning(logname, e.getMessage());
				} 
				
				Literal xpos = new Literal("xpos");
				xpos.addTerm(new NumberTermImpl((int)x));
									
				addUniquePercept("xpos", xpos);
				
				System.out.println(lane*2-2);
				System.out.println("pos. carro: "+x);
				
			} else if(socket.pendingInput() && x==(lane*2-2)) {
				
				try {
					while (socket.pendingInput()) {
						socket.readInt();
						socket.readInt();
						socket.readInt();
						socket.readInt();
						started = socket.readInt();	
						socket.readInt();
					}
				} catch (Exception e) {
					AJPFLogger.warning(logname, e.getMessage());
				} 
				
				//System.out.println(lane*2-2);
				//System.out.println("pos. carro: "+x);
				
				
				Literal lane2 = new Literal("lane2");
				lane2.addTerm(new NumberTermImpl(lane*2-2));
				
				
				addUniquePercept("lane2", lane2);
				socket.writeInt(-1);
				
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
		
		if (act.getFunctor().equals("finished")) {
			finished = true;
		} else if (act.getFunctor().equals("change_lane")) {
			socket.writeInt(1);		
		} else if (act.getFunctor().equals("stay_in_lane")) {
			socket.writeInt(-1);
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
