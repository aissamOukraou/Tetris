
package tetris.Models.game;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import tetris.Models.pieces.Tetrimino;

/**
 * Simple FIFO Queue which is always back-filled with elements from a given Bag.
 */
public class PiecesSuivante {

	private List<Tetrimino> _queue;
	private Hold _hold; // the bag object to refill the queue
	private int _length; // the length of the queue

	/**
	 * Creates a queue with length elements which is back-filled from the given Bag
	 * @param hold
	 * @param length
	 */
	public PiecesSuivante(Hold hold, int length) {
		this._hold = hold;
		this._length = length;
		_queue = new ArrayList<>(_length);
		fillQueue();
	}
	
	/**
	 * Retrieves the next Tetrimino from the queue and back-fills the queue from the give Bag.
	 * @return the next Tetrimino
	 */
	public Tetrimino getNext() {
		Tetrimino next = _queue.remove(0);
		fillQueue();
		return next;
	}
	
	/**
	 * Retrieves the Tetrimino at position i 
	 * @return the next Tetrimino
	 */
	public Tetrimino get(int i) {
		return _queue.get(i);
	}
	
	/**
	 * Iterator over all elements in correct order.
	 * @return the ListIterator for the queued elements
	 */
	public ListIterator<Tetrimino> getListIterator() {
		return _queue.listIterator();
	}

	/*
	 *	back-fills the queue from the Bag until it is full 
	 */
	private void fillQueue() {
		while (_queue.size() < _length) {
			_queue.add(_hold.getNext());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		_queue.forEach((q) -> sb.append(q.toString()).append(" "));
		return sb.toString();
	}
	
	

}
