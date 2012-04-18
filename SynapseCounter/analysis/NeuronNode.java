package annotool.analysis;

public class NeuronNode{
	protected double x, y, z;
	protected int num;
	protected double radius;
	protected int parent;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param num
	 * @param radius
	 * @param parent
	 */
	public NeuronNode(double x, double y, double z, int num, double radius, int parent) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.num = num;
		this.radius = radius;
		this.parent = parent;
	}
	
	/**
	 * Returns the distance between this node and n.
	 * @param n the other node
	 * @return the distance 
	 */
	public final double distance(NeuronNode n) {
		double dx, dy, dz;
	 
	    dx = this.x - n.x;
	    dy = this.y - n.y;
	    dz = this.z - n.z;
	      
	    return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	/**
	 * Returns the distance between this node (x, y, z).
	 * @param x
	 * @param y
	 * @param z
	 * @return the distance
	 */
	public final double distance(double x, double y, double z) {
		double dx, dy, dz;
	 
	    dx = this.x - x;
	    dy = this.y - y;
	    dz = this.z - z;
	      
	    return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
}
