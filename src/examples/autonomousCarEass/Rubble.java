package autonomousCarEass;

import ail.util.AILSocketServer;

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
