
package tetris.Models.pieces;

import tetris.Models.game.TetrisColor;
import tetris.Models.util.Coordinates;

/**
 * 
 */
public class Z_Tetrimino extends Tetrimino {
	
	static int [][][] tMatrix = new int[][][] {
		{ //NORTH
			{1, 1, 0},
			{0, 1, 1},
			{0, 0, 0}
		},
		{ // EAST
			{0, 0, 1},
			{0, 1, 1},
			{0, 1, 0}
		},
		{ // SOUTH
			{0, 0, 0},
			{1, 1, 0},
			{0, 1, 1}
		},
		{ // WEST
			{0, 1, 0},
			{1, 1, 0},
			{1, 0, 0}
		}
	};
	
	public Z_Tetrimino() {
		super ("Z", TetrisColor.RED, tMatrix, new Coordinates(3,22));
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#getShape()
	 */
	@Override
	public TetriminoShape getShape() {
		return TetriminoShape.Z;
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#clone()
	 */
	@Override
	public Tetrimino clone() {
		Tetrimino tnew = new Z_Tetrimino();
		// this field can be changed from public
		tnew._currentOrientation = this._currentOrientation;
		tnew._currentPosition = this._currentPosition.clone();
		return tnew;
	}
	
	

}
