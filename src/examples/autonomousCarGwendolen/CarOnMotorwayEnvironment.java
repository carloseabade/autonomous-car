package autonomousCarGwendolen;

import java.io.IOException;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.NActionScheduler;
import ail.syntax.Literal;
import ail.syntax.Unifier;
import ail.syntax.Action;
import ail.util.AILSocketClient;
import ail.util.AILexception;
import ajpf.MCAPLJobber;
import ajpf.MCAPLScheduler;
import ajpf.util.AJPFLogger;

public class CarOnMotorwayEnvironment extends DefaultEnvironment implements MCAPLJobber {
	
	String logname = "gwendolen.verifiableautonomoussystems.chapter5.CarOnMotorwayEnvironment";
		
	/**
	 * Socket that connects to the Simulator.
	 */
	protected AILSocketClient socket;
	
	/**
	 * Has the environment concluded?
	 */
	private boolean finished = false;
	
	private int speed_limit = 5;

	/**
	 * Constructor.
	 */
	public CarOnMotorwayEnvironment() {
		super();
		MCAPLScheduler s = new NActionScheduler(100);
		s.addJobber(this);
		setScheduler(s);
		addPerceptListener(s);
		AJPFLogger.info(logname, "Waiting Connection");
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
	@Override
	public void do_job() {
		if (socket.allok()) {
			readPredicatesfromSocket();
		}	else {
			System.err.println("something wrong with socket");
		}
	}

	/**
	 * Reading the values from the sockets and turning them into perceptions.
	 */
	public void readPredicatesfromSocket() {
		
		try {
			if (socket.pendingInput()) {

				socket.readDouble();
				socket.readDouble();
				@SuppressWarnings("unused")
				double c1_xdot = socket.readDouble();
				double c1_ydot = socket.readDouble();
				int c1_started = socket.readInt();
				
				socket.readDouble();
				socket.readDouble();
				@SuppressWarnings("unused")
				double c2_xdot = socket.readDouble();
				double c2_ydot = socket.readDouble();
				int c2_started = socket.readInt();
				
				
				try {
					while (socket.pendingInput()) {
						socket.readDouble();
						socket.readDouble();
						c1_xdot = socket.readDouble();
						c1_ydot = socket.readDouble();
						c1_started = socket.readInt();			
						socket.readDouble();
						socket.readDouble();
						c2_xdot = socket.readDouble();
						c2_ydot = socket.readDouble();
						c2_started = socket.readInt();			
					}
				} catch (Exception e) {
					AJPFLogger.warning(logname, e.getMessage());
				} 
				
								
				if (c1_started > 0) {
					addPercept("car1", new Literal("started"));
				}
				
				if (c2_started > 0) {
					addPercept("car2", new Literal("started"));
				}

				if (c1_ydot < speed_limit) {
					removePercept("car1", new Literal("at_speed_limit"));
				} else {
					addPercept("car1", new Literal("at_speed_limit"));
				}
				
				if (c2_ydot < speed_limit) {
					removePercept("car2", new Literal("at_speed_limit"));
				} else {
					addPercept("car2", new Literal("at_speed_limit"));
				}

			}
		} catch (Exception e) {
			AJPFLogger.warning(logname, e.getMessage());
		}
	}
	
	
	double car1_xaccel = 0.0;
	double car1_yaccel = 0.0;
	double car2_xaccel = 0.0;
	double car2_yaccel = 0.0;
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#executeAction(java.lang.String, ail.syntax.Action)
	 */
	public Unifier executeAction(String agName, Action act) throws AILexception {

		if (act.getFunctor().equals("accelerate")) {
			if (agName.equals("car1")) {
				car1_yaccel = 0.01;
			} else {
				car2_yaccel = 0.01;
			}
			socket.writeDouble(car1_xaccel);
			socket.writeDouble(car1_yaccel);
			socket.writeDouble(car2_xaccel);
			socket.writeDouble(car2_yaccel);
		} else if (act.getFunctor().equals("decelerate")) {
			if (agName.equals("car1")) {
				car1_yaccel = -0.1;
			} else {
				car2_yaccel = -0.1;
			}
			socket.writeDouble(car1_xaccel);
			socket.writeDouble(car1_yaccel);
			socket.writeDouble(car2_xaccel);
			socket.writeDouble(car2_yaccel);
		} else if (act.getFunctor().equals("maintain_speed")) {
			if (agName.equals("car1")) {
				car1_yaccel = 0.0;
			} else {
				car2_yaccel = 0.0;
			}
			socket.writeDouble(car1_xaccel);
			socket.writeDouble(car1_yaccel);
			socket.writeDouble(car2_xaccel);
			socket.writeDouble(car2_yaccel);
		} else if (act.getFunctor().equals("finished")) {
				finished = true;
		}
		
		return super.executeAction(agName, act);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ail.mas.DefaultEnvironment#cleanup()
	 */
	@Override
	public void cleanup() {
		socket.close();
	}

	/*
	 * (non-Javadoc)
	 * @see ail.others.DefaultEnvironment#done()
	 */
	@Override
	public boolean done() {
		if (finished) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MCAPLJobber o) {
		return o.getName().compareTo(getName());
	}

	/*
	 * (non-Javadoc)
	 * @see ajpf.MCAPLJobber#getName()
	 */
	@Override
	public String getName() {
		return "CarOnMotorwayEnvironment";
	}

}
