package eass.tutorials.motorwaysim;

import ail.util.AILSocketServer;

/**
 * A class for a lane on a simple motorway simulation. In this class are the lane measurements.
 * @author lad
 *
 */
public class Lane {
	
	private Double width = 14.0; //Width in meters

	public Lane(Double width) {
		this.width = width;
	}
	
	public Lane() {}

	public Double getWidth() {
		return width;
	}
	
	public void setWidth(Double width) {
		this.width = width;
	}

}
