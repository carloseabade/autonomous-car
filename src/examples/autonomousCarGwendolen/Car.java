package autonomousCarGwendolen;

import ail.util.AILSocketServer;

public class Car {
	private int x, y, xrel, yrel;
	private int xdot = 0, ydot = 2;
	private int xaccel = 0;
	
	private int width = 2;
	private int length = 5;
	
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
	 * @param carnum
	 */
	public Car (int xi, int yi, int bw, int bh, boolean externalcontrol) {
		x = xi-((width*5)/2);
		y = yi;
		xrel = xi-((width*5)/2);
		yrel = yi;
		INITIAL_X = xi-((width*5)/2);
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
	
	/**
	 * Getter for x coordinate of car.
	 * @return
	 */
	public int getX() {
		return xrel;
	}
	
	/**
	 * Getter for y coordinate of car.
	 * @return
	 */
	public int getY() {
		return yrel;
	}
	
	/**
	 * Getter for the x coordinate relative to the top of the GUI.
	 * @return
	 */
	public double getXRel() {
		return xrel;
	}
	
	/**
	 * Getter for the y coordinate relative to the top of the GUI.
	 * @return
	 */
	public double getYRel() {
		return yrel;
	}
	
	/**
	 * Getter for the speed in the x direction
	 * @return
	 */
	public double getXDot() {
		return xdot;
	}
	
	/**
	 * Getter for speed in the y direction.
	 * @return
	 */
	public int getYDot() {
		return ydot;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getLength() {
		return length;
	}
	
	/**
	 * Setter for the y speed.
	 * @param speed
	 */
	public void setYDot(int speed) {
		ydot = speed;
	}
	
	/**
	 * Getter for total distance in the y direction.
	 * @return
	 */
	public double getYTot() {
		return y;
	}
	
	/**
	 * Getter for total distance in the x direction.
	 * @return
	 */
	public double getXTot() {
		return x;
	}
	
	/**
	 * Setter for the acceleration in the x direction.
	 * @param a
	 */
	public void setXAccel(int a) {
		xaccel = a;
	}
	

	/**
	 * Calculate cars new position.
	 */
	public void calculatePos() {
		xdot = xaccel;
		
		if (xdot == 0) {
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
				xaccel = socketserver.readInt();
				ydot = socketserver.readInt();
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
		if (controlled) {
			try {
				if (include_total_distance) {
					socketserver.writeInt(x);
					socketserver.writeInt(y);
				}
				socketserver.writeInt(xrel);
				socketserver.writeInt(yrel+length*5);
				socketserver.writeInt(xdot);
				socketserver.writeInt(ydot);
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
	
	/**
	 * Is the car being controlled externally to the simulator?
	 * @return
	 */
	public boolean isControlled() {
		return controlled;
	}
	
	/**
	 * Calculate total distances travelled not just relative to the top of the GUI.
	 * @return
	 */
	public boolean include_total_distance() {
		return include_total_distance;
	}
	
	/**
	 * Has the car started?
	 * @return
	 */
	public int started() {
		return started;
	}
}
