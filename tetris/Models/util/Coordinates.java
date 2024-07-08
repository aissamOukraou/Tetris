
package tetris.Models.util;

/**
 * A simple class representing a pair of int for usage of x,y coordinates in a matrix
 */
public class Coordinates {

	public int x = 0;
	public int y = 0;

	/**
	 * @param x
	 * @param y
	 */
	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true; 
		if (obj instanceof Coordinates 
				&& ((Coordinates) obj).x == this.x
				&& ((Coordinates) obj).y == this.y)
			return true;
		return false;
	}


	@Override
	public Coordinates clone() {
		return new Coordinates(x, y);
	}

	@Override
	public String toString() {
		return "("+x+","+y+")";
	}
	
	
}
