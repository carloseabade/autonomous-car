package eass.tutorials.motorwaysim;

import ail.util.AILSocketServer;

/**
 * A class for a lane on a simple motorway simulation. In this class are the lane measurements.
 * @author lad
 *
 */
public class Lane {
	
	private int width = 14; //Width in meters

	public Lane(int width) {
		this.width = width;
	}
	
	public Lane() {}

	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

}
