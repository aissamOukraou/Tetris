
package  tetris.VueControleur;

import  tetris.Models.pieces.Tetrimino;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class HoldPane extends Pane {
	private static final Color BACKGROUND_COLOR = Color.DARKGRAY;
	
	private Tetrimino _holdTetrimino; // handle to Tetrimino object

	private double _offSetY;
	private double _minoHeight;
	
	/**
	 * Initialize the nextQueuedPanel
	 */
	public HoldPane() {
		super();
		
		// set up the pane
        this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR,null,null)));

        // draw initial hold
        draw();
	}


	public void setHoldTetrimino(Tetrimino hold) {
		this._holdTetrimino = hold;
	}

	/**
	 * 
	 */
	public void draw() {
		if (_holdTetrimino == null) {
			// clear the node to redraw everything
			this.getChildren().clear();
			return;
		}
		draw(_holdTetrimino);
	}

	/**
	 * Draw a single Tetrimino at specified position
	 * @param next
	 */
	private void draw(Tetrimino next) {

		// clear the node to redraw everything
		this.getChildren().clear();
		
		_minoHeight = (this.getWidth()/6); // height and width of cells based on pane width
		_offSetY = _minoHeight; // start with one mino height below the top
		
		int[][] tMatrix = next.getMatrix(Tetrimino.Facing.NORTH); // get north facing matrix

		Color color = next.getColor().toColor();

		// determine were to draw horizontally
		double start_x;
		switch (next.toString()) {
		case "O": {
			start_x = this.getWidth()/2 - _minoHeight; // 1 mino left of middle
			break;
		}
		case "I": {
			start_x = this.getWidth()/2 - (2*_minoHeight); // 2 minos left of middle
			break;
		}
		default: {
			start_x = this.getWidth()/2 - (_minoHeight+(_minoHeight/2)); // 1.5 minos left of middle
			break;
		}
		}
		
		for(int y=0; y<tMatrix.length;y++) {
			boolean hadMino = false;
			for(int x=0; x<tMatrix[y].length;x++) {
				if (tMatrix[y][x] == 1 && _offSetY < this.getHeight() - _minoHeight) {
					hadMino=true;
					// at least one mino in this row
					double relX = x*_minoHeight;
					Rectangle block = new Rectangle();
					block.setFill(color);
					block.setArcHeight(5.0);
					block.setArcWidth(5.0);
					block.setX(start_x+relX);
					block.setY(_offSetY);
					block.setWidth(_minoHeight); 
					block.setHeight(_minoHeight);
					this.getChildren().add(block);
				}
			}
			if (hadMino) {
				_offSetY += _minoHeight; // remember position for next Tetrimino
			}
		}
	}
}
