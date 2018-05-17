package autonomous_car_semTravar;

public class Road {
	
	private int laneWidth;
	private int lane1Pos;
	private int lane2Pos;
	private int lane3Pos;
	private int lane4Pos;
	
	public Road() {}
	
	public Road(int laneWidth, int lane1Pos, int lane2Pos, int lane3Pos, int lane4Pos) {
		this.setLaneWidth(laneWidth);
		this.setLane1Pos(lane1Pos);
		this.setLane2Pos(lane2Pos);
		this.setLane3Pos(lane3Pos);
		this.setLane4Pos(lane4Pos);
	}

	public int getLane1Pos() {
		return lane1Pos;
	}

	public void setLane1Pos(int lane1Pos) {
		this.lane1Pos = lane1Pos;
	}

	public int getLane2Pos() {
		return lane2Pos;
	}

	public void setLane2Pos(int lane2Pos) {
		this.lane2Pos = lane2Pos;
	}

	public int getLane3Pos() {
		return lane3Pos;
	}

	public void setLane3Pos(int lane3Pos) {
		this.lane3Pos = lane3Pos;
	}

	public int getLane4Pos() {
		return lane4Pos;
	}

	public void setLane4Pos(int lane4Pos) {
		this.lane4Pos = lane4Pos;
	}

	public int getLaneWidth() {
		return laneWidth;
	}

	public void setLaneWidth(int laneWidth) {
		this.laneWidth = laneWidth;
	}
	
	public int getLane4Top() {
		return 0;
	}
	
	public int getLane4Bottom() {
		return getLaneWidth();
	}
	
	public int getLane3Top() {
		return getLaneWidth();
	}
	
	public int getLane3Bottom() {
		return getLaneWidth()*2;
	}
	
	public int getLane2Top() {
		return getLaneWidth()*2;
	}
	
	public int getLane2Bottom() {
		return getLaneWidth()*3;
	}

	public int getLane1Top() {
		return getLaneWidth()*3;
	}
	
	public int getLane1Bottom() {
		return getLaneWidth()*4;
	}
}
