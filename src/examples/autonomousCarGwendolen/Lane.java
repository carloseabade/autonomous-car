package autonomousCarGwendolen;

import ail.util.AILSocketServer;

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