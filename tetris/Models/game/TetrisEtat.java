
package tetris.Models.game;

/**
 * All states a tetris engine can be in
 */
public enum TetrisEtat {
	
	NOTSTARTED,
	GENERATION,
	FALLING,
	LOCK,
	PATTERN,
	ITERATE,
	ANIMATE,
	ELIMINATE,
	COMPLETION,
	GAMEOVER;

}
