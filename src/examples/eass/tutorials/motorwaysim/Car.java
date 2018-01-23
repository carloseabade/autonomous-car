package eass.tutorials.motorwaysim;

import ail.util.AILSocketServer;

/**
 * A class for a car on a simple motorway simulation that may be controlled by an agent.
 * @author lad
 *
 */
public class Car {
	private double x, y, xrel, yrel;
	private double xdot = 0, ydot = 5;
	private double xaccel = 0;
	
	/**
	 * width & length in meters.
	 */
	private Double width = 2.19;
	private Double length = 4.98;
	
	private int INITIAL_X, INITIAL_Y, B_WIDTH, B_HEIGHT;
	
	private boolean controlled;
	private boolean include_total_distance;
	private int started = 0;

	/**
	 * Socket that connects to the agent.
	 */
	protected AILSocketServer socketserver;

	public Car() {}
	
	/**
	 * Constructor.
	 * @param xi
	 * @param yi
	 * @param bw
	 * @param bh
	 * @param externalcontrol
	 */
	public Car (int xi, int yi, int bw, int bh, boolean externalcontrol) {
		x = xi-((width.intValue()*5)/2);
		y = yi;
		xrel = xi-((width.intValue()*5)/2);
		yrel = yi;
		INITIAL_X = xi-((width.intValue()*5)/2);
		INITIAL_Y = yi;
		B_WIDTH = bw;
		B_HEIGHT = bh;
		controlled = externalcontrol;
		
		if (controlled) {
			System.err.println("Motorway Sim waiting Socket Connection");
			socketserver = new AILSocketServer();
			System.err.println("Got Socket Connection");
		}

	}
	
	public Double getWidth() {
		return width;
	}
	
	public Double getLength() {
		return length;
	}
	
	/**
	 * Getter for x coordinate of car.
	 * @return
	 */
	public double getX() {
		return xrel;
	}
	
	/**
	 * Getter for y coordinate of car.
	 * @return
	 */
	public double getY() {
		return yrel;
	}
	
	/**
	 * Getter for speed in the y direction.
	 * @return
	 */
	public double getYDot() {
		return ydot;
	}
	
	/**
	 * Setter for the y speed.
	 * @param speed
	 */
	public void setYDot(double speed) {
		ydot = speed;
	}
	
	/**
	 * Getter for speed in the x direction.
	 * @return
	 */
	public double getYTot() {
		return y;
	}
	
	/**
	 * Calculate cars new position.
	 */
	public void calculatePos() {
		xdot += xaccel;
				
		if (xdot < 0) {
			xdot = 0;
		}
		
		if (ydot < 0) {
			ydot = 0;
		}
		
		x += xdot;
		y += ydot;
		xrel += xdot;
		yrel += ydot;

		if (yrel > B_HEIGHT) {
			yrel = INITIAL_Y;
		}
		
		if (xrel > B_WIDTH) {
			xrel = INITIAL_X;
		}

	}
	
	/**
	 * Read in new accelerations from the socket and write current position and speed to the socket.
	 */
	public void updateParameters() {
		if (controlled) {
			if (socketserver.allok()) {
				try {
					if (socketserver.pendingInput()) {
						// System.err.println("reading values");
						readValues();
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				writeValues();
			} else {
				System.err.println("something wrong with socket server");
			}
			
		}

	}
	
	/**
	 * Read in values from socket.
	 */
	private void readValues() {
		if (controlled) {
			try {
				xaccel = socketserver.readDouble();
			} catch (Exception e) {
				System.err.println("READ ERROR: Closing socket");
				close();
			}
		}

	}
	
	/**
	 * Write values to socket.
	 */
	public void writeValues() {
		if ( controlled ) {
			try {
				if (include_total_distance) {
					socketserver.writeDouble(x);
					socketserver.writeDouble(y);
				}
				socketserver.writeDouble(xrel);
				socketserver.writeDouble(yrel);
				socketserver.writeDouble(xdot);
				socketserver.writeDouble(ydot);
				socketserver.writeInt(started);
				socketserver.writeInt(INITIAL_X);
			}  catch (Exception e) {
				System.err.println("READ ERROR: Closing socket");
				close();
			}
		}
	}
	
	/**
	 * Close up the socket server.
	 */
	public void close() {
		if (controlled) {
			socketserver.close();
		}
	}
	
	/**
	 * Called to tell the car that the simulation has started.
	 */
	public void start() {
		started = 1;
	}
	
	/** 
	 * Configure the car.
	 * @param config
	 */
	public void configure(MotorwayConfig config) {
		if (config.containsKey("data.include_total_distance")) {
			if (config.getProperty("data.include_total_distance").equals("true")) {
				include_total_distance = true;
			}
		}
	}
}
