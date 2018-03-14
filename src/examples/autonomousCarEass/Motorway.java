package autonomousCarEass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JPanel;

public class Motorway extends JPanel implements Runnable {
	
	static final long serialVersionUID = 0;
	
	private final int B_WIDTH = 500;
	private final int B_HEIGHT = 600;
	private final int DELAY = 25;
	
	private Thread animator;
	private Car car1;
	private boolean car1control = false;
	private Lane lane = new Lane();
	private final int INITIAL_X1 = lane.getWidth()*5/2;
	private final int INITIAL_Y1 = 0;
	
	private boolean started = false;
	private boolean running = true;
	
	/**
	 * Constructor.
	 * @param args
	 */
	public Motorway(String control) {
		initMotorway(control);
	}
		
	/**
	 * Initialization.
	 * @param args
	 */
	private void initMotorway(String control) {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
		setDoubleBuffered(true);
		
		if (control.equals("agent")) {
			car1control = true;
		}
		
		car1 = new Car(INITIAL_X1, INITIAL_Y1, B_WIDTH, B_HEIGHT, car1control);
		repaint();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#addNotify()
	 */
	public void addNotify() {
		super.addNotify();
		
		animator = new Thread(this);
		animator.start();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		drawMotorway(g);
	}
	
	/**
	 * Draw the current position of the cars on the motorway.
	 * @param g
	 */
	private void drawMotorway(Graphics g) {
		int d1 = car1.getX();
		int d2 = car1.getY();
		
		g.drawRect(d1, d2, car1.getWidth()*5, car1.getLength()*5);
		g.drawLine(lane.getWidth()*5, 0, lane.getWidth()*5, B_HEIGHT);
		g.drawLine(lane.getWidth()*5*2, 0, lane.getWidth()*5*2, B_HEIGHT);
		
		//g.fillRect(rubble1.getX(), rubble1.getY(), 10, 10);
		
		int ydot = car1.getYDot();
		
		g.drawString("Speed Car 1: " + ydot, 150, 20);
		g.drawString("Distance Car 1: " + d2, 150, 50);
		
		Toolkit.getDefaultToolkit().sync();
	}
	
	/**
	 * Update the car positions and information from/to sockets.
	 */
	private void cycle() {
		
		car1.calculatePos();
		car1.updateParameters();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		long beforeTime, timeDiff, sleep;
		
		beforeTime = System.currentTimeMillis();
		repaint();
		
		while (running) {
			
			if (started) {
				cycle();
				repaint();
			}
			
			timeDiff = System.currentTimeMillis() - beforeTime;
			sleep = DELAY - timeDiff;
			
			if (sleep < 0) {
				sleep = 2;
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.out.println("Interrupted: " + e.getMessage());
			}
			
			beforeTime = System.currentTimeMillis();
		} 
	}
	
	/**
	 * Called by the GUI when the user starts the simulation.
	 */
	public void start() {
		started = true;
		car1.start();
		
	}
	
	public boolean started() {
		return started;
	}
	
	/**
	 * Stop the loop in run().  Mostly  used for test cases.
	 */
	public void stop() {
		running = false;
		car1.close();
	}
	
	/**
	 * Return car1 object.  Again mostly used for control in tests.
	 * @return
	 */
	public Car getCar1() {
		return car1;
	}
	
	
	/**
	 * Configure the motorway for this simulation.
	 * @param config
	 */
	public void configure(MotorwayConfig config) {
		car1.configure(config);
		
	}
	
	

}
