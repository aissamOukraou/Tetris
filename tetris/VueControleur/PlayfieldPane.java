
package  tetris.VueControleur;

import tetris.Models.game.Grille;
import  tetris.Models.game.TetrisColor;
import  tetris.Models.pieces.Tetrimino;
import  tetris.Models.util.Coordinates;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class PlayfieldPane extends Pane {

	private static final double HEIGHT = 600;
	private static final double WIDTH = 300;

	private static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final Color GRID_COLOR = Color.LIGHTGRAY;
	private static final Color FRAME_COLOR = Color.LIGHTGRAY;

	private Grille _playField; // handle to the playField to draw

	private TetrisGUI_Controller _controller; // handle to the UI controller to access options

	// helper for an efficient draw()
	private Line[] _hlines = new Line[Grille.SKYLINE];
	private Line[] _vlines = new Line[Grille.MATRIX_WIDTH];
	private Rectangle[] _block = new Rectangle[(Grille.BUFFERZONE+ Grille.SKYLINE)* Grille.MATRIX_WIDTH];
	private Rectangle[] _tblock = new Rectangle[16];
	private Rectangle[] _gblock = new Rectangle[16];

	/**
	 * Initialize the playfieldPanel
	 * @param tetrisGUI_Controller 
	 */
	public PlayfieldPane(TetrisGUI_Controller tetrisGUI_Controller) {
		super();

		this._controller = tetrisGUI_Controller;

		// set up the pane
		this.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR,null,null)));
		// set size
		this.setMinWidth(WIDTH);
		this.setMinHeight(HEIGHT);
		this.setMaxWidth(WIDTH);
		this.setMaxHeight(HEIGHT);


		// prepare some elements and keep them to reference them
		for (int i = 0; i< Grille.SKYLINE; i++) {
			_hlines[i] = new Line();
		}
		for (int i = 0; i< Grille.MATRIX_WIDTH; i++) {
			_vlines[i] = new Line();
		}
		for (int i = 0; i<(Grille.BUFFERZONE+ Grille.SKYLINE)* Grille.MATRIX_WIDTH; i++) {
			_block[i] = new Rectangle();
		}
		for (int i=0; i<16; i++) {
			_tblock[i] = new Rectangle();
			_gblock[i] = new Rectangle();
		}

		// draw initial board
		draw();

	}

	/**
	 * @param _playField the _playField to set
	 */
	public void setPlayField(Grille _playField) {
		// ToDo: Maybe a copy is needed
		this._playField = _playField;
	}

	/**
	 * Draws all elements in the panel. Lines and Tetriminos
	 */
	public void draw() {
		if (_playField == null) _playField=new Grille(); // draw default field if no other playField is defined
		draw(_playField);
	}

	/**
	 * @param playField
	 */
	private void draw(Grille playField) {

		// clear the node to redraw everything
		this.getChildren().clear();

		// draw frame
		Rectangle rectangle = new Rectangle();
		rectangle.setStroke(FRAME_COLOR);
		// here we position the rectangle (this depends on pane size as well)
		rectangle.setX(0);
		rectangle.setY(0);
		// here we bind rectangle size to pane size
		rectangle.setHeight(HEIGHT);
		rectangle.setWidth(WIDTH);
		this.getChildren().add(rectangle);

		// draw lines
		for (int c = 1; c< Grille.MATRIX_WIDTH; c++) {
			// vertical lines
			double w = (WIDTH/ Grille.MATRIX_WIDTH)*c;
			Line v_line =_vlines[c-1];
			v_line.setStroke(GRID_COLOR);
			v_line.setStartX(w);
			v_line.setStartY(0);
			v_line.setEndX(w);
			v_line.setEndY(HEIGHT);
			this.getChildren().add(v_line);	
		}
		for (int r = 1; r< Grille.SKYLINE; r++) {
			// horizontal lines
			double h = (HEIGHT/ Grille.SKYLINE)*r;
			Line h_line =_hlines[r-1];
			h_line.setStroke(GRID_COLOR);
			h_line.setStartX(0);
			h_line.setStartY(h);
			h_line.setEndX(WIDTH);
			h_line.setEndY(h);
			this.getChildren().add(h_line);
		}

		final double h = (HEIGHT/ Grille.SKYLINE);
		final double w = (WIDTH/ Grille.MATRIX_WIDTH);

		int cr = 0; // counter for the prepared drawing objects
		
		// draw background cells
		// iterate through all cells a initialize with zero

		for (int yi = 0; yi < Grille.SKYLINE+1; yi++) { // we only draw the visible part therefore only to SKYLINE
			for (int xi = 0; xi < Grille.MATRIX_WIDTH; xi++) {
				final TetrisColor bc = _playField.getBackgroundColor(xi,yi);
				Color color = bc.toColor();
				if (bc != TetrisColor.EMPTY) {
					color = bc.toColor(); 
				} 
				final double offset_h = HEIGHT -(h*(yi+1)); // height is measured top down were as our playField is buttom up 
				final double offset_w = w * xi;
				Rectangle block = _block[cr++];
				block.setFill(color);
				block.setX(offset_w+1); // +1 to not overdraw the lines
				block.setY(offset_h+1);
				block.setWidth(w-1); // -1 to not overdraw the lines
				block.setHeight(h-1);
				this.getChildren().add(block);
			}
		}        

		// draw current Tetrimino
		// will draw over background so collision check needs to be done in model
		final Tetrimino t = playField.getCurrentTetrimino();
		
		if (t!=null) { // if no game is running there are no Tetriminos
			
			// set the max height we want to see Tetriminos
			int visibleHeight = Grille.SKYLINE+1;
			//if (!_controller.peekOption.isSelected()) {
			//	visibleHeight = Matrix.SKYLINE;
			//}

			Coordinates c;
			cr = 0;  

			// draw ghost tetrimino
			/*if (_controller.ghostPieceOption.isSelected()) {
				// copy the real tetrimino as we will change the position of the ghost
				final Tetrimino ghost = t.clone(); 

				// drop the ghost as far as possible
				while (!_playField.moveDown(ghost)) {}

				final int[][] gMatrix = ghost.getMatrix(ghost.getCurrentOrientation());

				c = ghost.getCurrentPosition(); // Convenience 

				cr = 0; // counter for the p

				// loop through the Tetrimino matrix
				for (int yi = 0; yi < gMatrix.length; yi++) {
					for (int xi = 0; xi < gMatrix[yi].length; xi++) {
						if (gMatrix[yi][xi] == 1) { // only draw when 1
							int bx = c.x + xi;
							int by = c.y - yi;
							// if not visible skip drawing
							if (by > visibleHeight) break;
							double offset_h = HEIGHT -(h*by); // height is measured top down were as our playField is buttom up 
							double offset_w = w * bx;
							Rectangle block = _gblock[cr++];
							block.setFill(BACKGROUND_COLOR);
							block.setStroke(ghost.getColor().toColor());
							block.setStrokeType(StrokeType.OUTSIDE);
							block.setStrokeWidth(2.0);
							Bloom bloom = new Bloom();
							bloom.setThreshold(0.1);
							block.setEffect(bloom);
							//block.setOpacity(0.5);
							block.setArcHeight(5.0);
							block.setArcWidth(5.0);
							block.setX(offset_w+1); // +1 to not overdraw the lines
							block.setY(offset_h+1);
							block.setWidth(w-1); // -1 to not overdraw the lines
							block.setHeight(h-1);
							this.getChildren().add(block);
						}
					}
				}
			}*/

			cr = 0; 
			
			final int[][] tMatrix = t.getMatrix(t.getCurrentOrientation());
			c = t.getCurrentPosition();
			
			// draw the real Tetrimino
			for (int yi = 0; yi < tMatrix.length; yi++) {
				for (int xi = 0; xi < tMatrix[yi].length; xi++) {
					if (tMatrix[yi][xi] == 1) { // only draw when 1
						int bx = c.x + xi;
						int by = c.y - yi;
						// if not visible skip drawing
						if (by > visibleHeight) break;
						double offset_h = HEIGHT -(h*by); // height is measured top down were as our playField is buttom up 
						double offset_w = w * bx;
						Rectangle block = _tblock[cr++];
						block.setFill(t.getColor().toColor());
						block.setArcHeight(5.0);
						block.setArcWidth(5.0);
						block.setX(offset_w+1); // +1 to not overdraw the lines
						block.setY(offset_h+1);
						block.setWidth(w-1); // -1 to not overdraw the lines
						block.setHeight(h-1);
						this.getChildren().add(block);
					}
				}
			}


		}
	}
}
