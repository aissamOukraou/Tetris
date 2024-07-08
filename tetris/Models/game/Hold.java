
package tetris.Models.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import  tetris.Models.pieces.I_Tetrimino;
import  tetris.Models.pieces.J_Tetrimino;
import  tetris.Models.pieces.L_Tetrimino;
import  tetris.Models.pieces.O_Tetrimino;
import  tetris.Models.pieces.S_Tetrimino;
import  tetris.Models.pieces.T_Tetrimino;
import  tetris.Models.pieces.Tetrimino;
import  tetris.Models.pieces.Z_Tetrimino;

public class Hold {
	
	private List<Tetrimino> _elements;
	
	/**
	 * Creates a bag with all 7 elements
	 */
	public Hold() {
		_elements = new ArrayList<>(7);
		fillAndShuffle();
	}

	public Tetrimino getNext() {
		if (_elements.isEmpty()) {
			fillAndShuffle();
		}
		return _elements.remove(0);
	}

	/*
	 * fills the bag with all 7 Tetriminos and shuffles the order 
	 */
	private void fillAndShuffle() {
		_elements.add(new O_Tetrimino());
		_elements.add(new I_Tetrimino());
		_elements.add(new T_Tetrimino());
		_elements.add(new L_Tetrimino());
		_elements.add(new J_Tetrimino());
		_elements.add(new S_Tetrimino());
		_elements.add(new Z_Tetrimino());
		Collections.shuffle(_elements);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		_elements.forEach((q) -> sb.append(q.toString()).append(" "));
		return sb.toString();
	}
	
	
	
}
