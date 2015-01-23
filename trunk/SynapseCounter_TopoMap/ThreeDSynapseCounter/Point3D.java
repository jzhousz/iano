
//helper structure to facilitate manipulation of 3D points
class Point3D
{
	int x, y, z;
	Point3D(int x, int y, int z) {this.x=x; this.y=y; this.z=z;}
	//Return tree if 2 points are close enough
	boolean equals(Point3D p2)
	{ 
		int distx, disty, distz;
		if (x>p2.x) distx = x-p2.x; else distx = p2.x -x;
		if (y>p2.y) disty = y-p2.y; else disty = p2.y -y;
		if (z>p2.z) distz = z-p2.z; else distz = p2.z -z;
		//distance on xy plane should be smaller than 2, on z should be smaller than 1. - Due to different resolution, don't say distx+disty+distz < 3
		if((distx<2) && (disty<=2) && distz <=1) return true;
		else return false; 
	}

	boolean closesTo(Point3D p2, int dx, int dy, int dz)
	{ 
		int distx, disty, distz;
		if (x>p2.x) distx = x-p2.x; else distx = p2.x -x;
		if (y>p2.y) disty = y-p2.y; else disty = p2.y -y;
		if (z>p2.z) distz = z-p2.z; else distz = p2.z -z;
		//distance on xy plane should be smaller than 2, on z should be smaller than 1. - Due to different resolution, don't say distx+disty+distz < 3
		if((distx <= dx) && (disty <= dy) && distz <= dz) return true;
		else return false; 
	}

	String convertToString() {
	
		return ("("+x+","+y+","+z+")");
	
	}
	
	void offsetPoint(int offX, int offY, int offZ) {
		x += offX;
		y += offY;
		z += offZ;
	}
}

