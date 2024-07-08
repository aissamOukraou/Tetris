
package tetris.Models.pieces;

import tetris.Models.game.TetrisColor;
import tetris.Models.util.Coordinates;

/**
 * 
 */
public class O_Tetrimino extends Tetrimino {
	
	static int [][][] tMatrix = new int[][][] {
		{ //NORTH
			{1, 1},
			{1, 1}
		},
		{ // EAST
			{1, 1},
			{1, 1}
		},
		{ // SOUTH
			{1, 1},
			{1, 1}
		},
		{ // WEST
			{1, 1},
			{1, 1}
		}
	};
	
	public O_Tetrimino() {
		super ("O", TetrisColor.YELLOW, tMatrix, new Coordinates(4,22));
	}
	
	/**
	 * @see fko.tetris.tetriminos.Tetrimino#getShape()
	 */
	@Override
	public TetriminoShape getShape() {
		return TetriminoShape.O;
	}

	/**
	 * @see fko.tetris.tetriminos.Tetrimino#clone()
	 */
	@Override
	public Tetrimino clone() {
		Tetrimino tnew = new O_Tetrimino();
		// this field can be changed from public
		tnew._currentOrientation = this._currentOrientation;
		tnew._currentPosition = this._currentPosition.clone();
		return tnew;
	}

}
