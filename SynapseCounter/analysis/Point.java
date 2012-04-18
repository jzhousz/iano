package annotool.analysis;

public class Point {
	public int x;
	public int y;
	
	Point() {
		x = 0;
		y = 0;
	}
	
	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	public double getDistanceTo(int x2, int y2) {
		double distance = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
		return distance;		
	}
	
	public double getDistanceTo(Point p2) {
		return getDistanceTo(p2.x, p2.y);
	}
}
