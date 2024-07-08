
package tetris.Models.pieces;

import tetris.Models.game.TetrisColor;
import tetris.Models.util.Coordinates;

/**
 * 
 */
public class S_Tetrimino extends Tetrimino {
	
	static int [][][] tMatrix = new int[][][] {
		{ //NORTH
			{0, 1, 1},
			{1, 1, 0},
			{0, 0, 0}
		},
		{ // EAST
			{0, 1, 0},
			{0, 1, 1},
			{0, 0, 1}
		},
		{ // SOUTH
			{0, 0, 0},
			{0, 1, 1},
			{1, 1, 0}
		},
		{ // WEST
			{1, 0, 0},
			{1, 1, 0},
			{0, 1, 0}
		}
	};
	
	public S_Tetrimino() {
		super ("S", TetrisColor.GREEN, tMatrix, new Coordinates(3,22));
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#getShape()
	 */
	@Override
	public TetriminoShape getShape() {
		return TetriminoShape.S;
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#clone()
	 */
	@Override
	public Tetrimino clone() {
		Tetrimino tnew = new S_Tetrimino();
		// this field can be changed from public
		tnew._currentOrientation = this._currentOrientation;
		tnew._currentPosition = this._currentPosition.clone();
		return tnew;
	}

}
