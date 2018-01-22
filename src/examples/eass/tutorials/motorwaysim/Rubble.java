package eass.tutorials.motorwaysim;

import ail.util.AILSocketServer;

/**
 * A class for a car on a simple motorway simulation that may be controlled by an agent.
 * @author lad
 *
 */
public class Rubble {
	
	private int INITIAL_X, INITIAL_Y;
	
	public int getX() {
		return INITIAL_X;
	}
	
	public int getY() {
		return INITIAL_Y;
	}

	/**
	 * Socket that connects to the agent.
	 */
	protected AILSocketServer socketserver;

	/**
	 * Constructor.
	 * @param xi
	 * @param yi
	 * @param bw
	 * @param bh
	 * @param externalcontrol
	 */
	public Rubble (int xi, int yi) {
		INITIAL_X = xi;
		INITIAL_Y = yi;
	}
	
	
	/**
	 * Write values to socket.
	 */
	public void showPosition() {
		try {
			socketserver.writeDouble(INITIAL_X);
			socketserver.writeDouble(INITIAL_Y);
			}  catch (Exception e) {
				System.err.println("READ ERROR: Closing socket");
				close();
			}
	}
	
	/**
	 * Close up the socket server.
	 */
	public void close() {
		socketserver.close();
	}
	
}
