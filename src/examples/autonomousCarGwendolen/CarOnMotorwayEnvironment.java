package autonomousCarGwendolen;

import java.io.IOException;
import java.net.SocketException;

import ail.mas.DefaultEnvironment;
import ail.mas.scheduling.NActionScheduler;
import ail.syntax.Literal;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.syntax.Action;
import ail.util.AILSocketClient;
import ail.util.AILexception;
import ajpf.MCAPLJobber;
import ajpf.MCAPLScheduler;
import ajpf.util.AJPFLogger;

public class CarOnMotorwayEnvironment extends DefaultEnvironment implements MCAPLJobber {
	
	String logname = "autonomousCarGwendolen";
		
	/**
	 * Socket that connects to the Simulator.
	 */
	protected AILSocketClient socket;
	
	/**
	 * Has the environment concluded?
	 */
	private boolean finished = false;
	
	private int started;
	private int lane;
	private int x;
	private int y;
	
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

				x = socket.readInt();
				y = socket.readInt();
				System.out.println("1 Valor de :"+y);socket.readInt();
				socket.readInt();
				started = socket.readInt();
				lane = socket.readInt();
								
				try {
					while (socket.pendingInput()) {
						x = socket.readInt();
						y = socket.readInt();
						System.out.println("2 Valor de :"+y);socket.readInt();
						socket.readInt();
						started = socket.readInt();
						lane = socket.readInt();		
					}
				} catch (Exception e) {
					AJPFLogger.warning(logname, e.getMessage());
				} 
				
								
				if (started > 0) {
					addPercept(new Literal("started"));
//					
//					Literal lane1 = new Literal("lane1");
//					lane1.addTerm(new NumberTermImpl(lane));
//					addPercept("lane1", lane1);
//
//					Literal lane2 = new Literal("lane2");
//					lane2.addTerm(new NumberTermImpl(lane*3));
//					addPercept("lane2", lane2);
				}
				
				//System.out.println("Certo:" + y);
				
				if(y < 100) {
					removePercept("car", new Literal("obs"));
				} else {
					addPercept("car", new Literal("obs"));
				}
				
//				Literal xpos = new Literal("xpos");
//				xpos.addTerm(new NumberTermImpl(x));
//				addPercept("xpos", xpos);
//				
//				Literal ypos = new Literal("ypos");
//				ypos.addTerm(new NumberTermImpl(y));
//				addPercept("ypos", ypos);
//				
//				//System.out.println(ypos);

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
		} else if (act.getFunctor().equals("left")) {
			socket.writeInt(1);
			socket.writeInt(2);
			removePercept("car", new Literal("left"));
		} else if (act.getFunctor().equals("right")) {
			socket.writeInt(-1);
			socket.writeInt(2);
		}else if (act.getFunctor().equals("stay_in_lane")) {
			
			socket.writeInt(0);
			socket.writeInt(2);
		}else if (act.getFunctor().equals("stop")) {
			socket.writeInt(0);
			socket.writeInt(0);
		}

		u.compose(super.executeAction(agName, act));
		
		return u;
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
	
	public void finalize() {
		socket.close();
	}

}
