
package tetris.Models.game;

import java.time.LocalDateTime;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.LinkedBlockingQueue;

import  tetris.Models.pieces.Tetrimino;

/**
 * This represents the state of a Tetris game. It holds all information necessary to represent a Tetris game at any 
 * point in time.
 */
public class OrdonnanceurSimple extends Observable implements Runnable, Observer {

	/**
	 * Sets how many Tetriminos are in the next queue. Not necessarily the same as how 
	 * many are shown in the VueControleur.
	 */
	public static final int NEXTQUEUE_SIZE = 7;

	// sounds are play at certain points ==> should this be in model or view??
	//private static final TetrisSounds _sounds = new TetrisSounds();

	// Tetris state
	private Grille _playfield;		// matrix with all cells
	private Hold _hold;			// bag with all 7 Tetriminos - randomly shuffled to the next queue
	private PiecesSuivante _piecesSuivante;		// holds a list the next Tetriminos
	private Tetrimino	_holdQueue; 		// holds one Tetrimino to be used later
	private int			_startLevel; 	// start level can be set differently by the UI
	private int			_currentLevel; 	// current level while playing
	private int			_score;			// current score
	private int			_lineCount;		// who many line have been eliminated since start
	private int			_tetrisesCount;	// number of Tetrises since start

	// application fields
	private Thread		_gameThread; 			// the thread where the Tetris game will run in
	private boolean 		_gameStopped = true; 	// flag to stop a running game
	private boolean 		_isPaused = false;		// flag to pause a running game

	private TetrisEtat _phaseState = TetrisEtat.NOTSTARTED; // the state/phase the engine is currently in
	// this determines what inputs and actions are allowed and 
	// which states can follow

	// Timers to control falling and locking time
	private TempsTetris _fallingTimer	= new TempsTetris(1000);
	private TempsTetris _lockTimer 		= new TempsTetris(500);

	// queues control inputs from the VueControleur
	private LinkedBlockingQueue<TetrisControlEvents> _controlQueue = new LinkedBlockingQueue<>();

	private boolean _holdAllowed = true; // using hold is only allowed once between LOCK phases

	// game statistics
	private int _lastClearedLinesCount = 0;
	private int _lastHardDropLineCount = 0;
	private int _lastSoftDropLineCount = 0;

	// Contains a List of high scores
	private MeilleurScore _meilleurScore;

	// current player's name
	private String _playerName = "Unknown Player";

	/**
	 * Creates a Tetris game with default values
	 */
	public OrdonnanceurSimple() {
		this(0);
	}

	/**
	 * Creates a Tetris game with a specified start level.
	 * @param startLevel
	 */
	public OrdonnanceurSimple(int startLevel) {
		_playfield 		= new Grille();
		_hold = new Hold();
		_piecesSuivante = new PiecesSuivante(_hold, NEXTQUEUE_SIZE);
		_holdQueue 		= null;
		_startLevel 	= startLevel;
		_currentLevel 	= startLevel;
		_score 			= 0;
		_lineCount 		= (startLevel-1) * 10; // if started with a higher level assume appropriate line count 
		_tetrisesCount 	= 0;

		_meilleurScore = MeilleurScore.getInstance();
	}

	/**
	 * Starts a new a new game of Tetris<br/>
	 * The thread then calls run() to actually do the work.
	 */
	public void startTetrisGame() {
		// Now start the thread
		if (_gameThread == null) {
			_gameThread = new Thread(this, "TetrisGame");
			_gameThread.start();
			_gameStopped = false;
		} else {
			throw new IllegalStateException("startTetrisGame(): Game thread already exists.");
		}
	}

	/**
	 * Stops the current game.
	 */
	public void stopTetrisGame() {
		if (_gameThread==null || !_gameThread.isAlive()) {
			throw new IllegalStateException("stopTetrisGame(): Game thread is not running");
		}
		// set a flag to stop the game
		_phaseState = TetrisEtat.GAMEOVER;
		_gameStopped = true;
		_gameThread.interrupt();
	}

	/**
	 * Implements the Runnable interface
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// -- tell the view that model has changed
		setChanged();
		notifyObservers("Game Thread started");
		//_sounds.playClip(Clips.GAME_START);

		// run completion phase one time to set level 
		// completion will the go to generation phase
		completionPhase();

		// clear control input queue
		_controlQueue.clear();

		do { // loop as long as game is running

			/* ******************************************************
			 * TETRIS STATE MACHINE
			 ********************************************************/
			switch (_phaseState) {
				case GENERATION:
					generationPhase();
					break;
				case FALLING:

					fallingPhase();
					break;
				case LOCK:

					lockPhase();
					break;
				case PATTERN:

					patternPhase();
					break;
				case ITERATE:

					_phaseState = TetrisEtat.ANIMATE;
					break;
				case ANIMATE:
					_phaseState = TetrisEtat.ELIMINATE; // not implemented
					break;
				case ELIMINATE:

					eliminatePhase();
					break;
				case COMPLETION:

					completionPhase();
					break;
				case GAMEOVER:
					_gameStopped=true;
					break;
				case NOTSTARTED:
					// do nothings
					break;
				default:
					break;
			}

			// -- tell the view that model has changed
			setChanged();
			notifyObservers("After PHASE loop");

			waitIfPaused();

		} while (_gameStopped == false);

		// game stopped

		// save highscore 
		_meilleurScore.addEntryAndSave(_playerName, _score, _currentLevel, _tetrisesCount, _lineCount, LocalDateTime.now());

		// -- tell the view that model has changed
		setChanged();
		notifyObservers("Game Thread stopped");
	}

	/**
	 * GENERATION phase 
	 */
	private void generationPhase() {
		// get next Tetrimino from nextQueue
		Tetrimino next = _piecesSuivante.getNext();
		// spawn it on the playfield
		if (_playfield.spawn(next)) {
			// collision detected - "BLOCK OUT" GAME OVER CONDITION
			_phaseState = TetrisEtat.GAMEOVER;
			// -- tell the view that model has changed
			setChanged();
			notifyObservers("Game Over");
			//_sounds.playClip(Clips.GAME_OVER);
		} else {
			// Immediately fall into visible area and check for collision
			if (_playfield.moveDown()) {
				// collision detected - LOCKING
				_phaseState = TetrisEtat.LOCK;
			}
			_phaseState = TetrisEtat.FALLING;
			// -- tell the view that model has changed
			setChanged();
			notifyObservers("Generation finished");
		}
	}

	/**
	 * FALLING phase
	 */
	private void fallingPhase() {

		// Start falling timer
		_fallingTimer.addObserver(this);
		_fallingTimer.setTimer(calculateFallingTime());
		_fallingTimer.start();

		// While timer is >0 allow movements
		// movement = inputs from keyboard (events)
		// we query a blocking queue and wait
		// timer will wake us if no event happens
		boolean breakFlag = false;
		do {
			// handle movement events
			// Take next control event or wait until available
			TetrisControlEvents event = TetrisControlEvents.NONE;
			try {
				event = _controlQueue.take(); // blocks until an event is available
			} catch (InterruptedException e) { /* empty*/ }

			waitIfPaused();

			// event handling
			switch(event) {
				case LEFT:
					if(_playfield.moveSideway(-1)) {
						//_sounds.playClip(Clips.TOUCH_LR);
					} else {
						//_sounds.playClip(Clips.MOVE_LR);
					};

					break;
				case RIGHT:
					if(_playfield.moveSideway(1)) {
						//_sounds.playClip(Clips.TOUCH_LR);
					} else {
						//_sounds.playClip(Clips.MOVE_LR);
					};
					break;
				case RTURN:
					if(_playfield.turnMove(1)) {
						//_sounds.playClip(Clips.ROTATE_FAIL);
					} else {
						//_sounds.playClip(Clips.ROTATE_LR);
					};
					break;
				case LTURN:
					if(_playfield.turnMove(-1)) {
						//	_sounds.playClip(Clips.ROTATE_FAIL);
					} else {
						//_sounds.playClip(Clips.ROTATE_LR);
					};
					break;
				case SOFTDOWN:
					_playfield.moveDown();	// ignored if no move possible
					_lastSoftDropLineCount+=1;
					//_sounds.playClip(Clips.SOFTDROP);
					break;
				case HARDDOWN:
					_lastHardDropLineCount = _playfield.drop();
					setChanged();
					notifyObservers("During FALLING after HARDOWN");
					//_sounds.playClip(Clips.HARDDROP);
					breakFlag = true;
					break;
				case HOLD:
					if (_holdAllowed) {
						Tetrimino toField = _holdQueue == null ? _piecesSuivante.getNext() : _holdQueue;
						try { // to reset start coordinates we use a new object - avoids a long switch statement :)
							_holdQueue = _playfield.getCurrentTetrimino().getClass().newInstance();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						_playfield.spawn(toField);
						//_sounds.playClip(Clips.HOLD);
					}
					_holdAllowed = false;
					break;
				case NONE:
					break;
			}

			// -- tell the view that model has changed
			setChanged();
			notifyObservers("During FALLING");

		} while (!breakFlag && _fallingTimer.getRemainingTime() > 0);

		// stop the timer just t make sure
		_fallingTimer.stop();
		if (_playfield.moveDown()) {
			// landed on surface
			_phaseState = TetrisEtat.LOCK;
			// -- tell the view that model has changed
			setChanged();
			notifyObservers("During FALLING");
			//_sounds.playClip(Clips.TOUCHDOWN);
		} else {
			//_sounds.playClip(Clips.FALLING);
		}
	}

	/**
	 * LOCK phase
	 * Implements the INFINITE PLACEMENT LOCK DOWN	
	 * TODO: Implement EXTENDED LOCK DOWN and CLASSIC LOCKDOWN
	 */
	private void lockPhase() {
		//System.out.println("Enter LOCK phase");

		// Start lock timer - lock time is always 500ms
		_lockTimer.addObserver(this);
		_lockTimer.restart();

		boolean breakFlag = false;
		do {
			// handle movement events
			// Take next control event or wait until available
			TetrisControlEvents event = TetrisControlEvents.NONE;
			try {
				event = _controlQueue.take();
			} catch (InterruptedException e) { /* empty*/ }

			waitIfPaused();

			// event handling
			switch(event) {
				case LEFT:
					if (!_playfield.moveSideway(-1)) { // if moved reset timer
						_lockTimer.restart();
					}
					break;
				case RIGHT:
					if (!_playfield.moveSideway(1)) { // if moved reset timer
						_lockTimer.restart();				}
					break;
				case RTURN:
					if (!_playfield.turnMove(1)) { // if moved reset timer
						_lockTimer.restart();				}
					break;
				case LTURN:
					if (!_playfield.turnMove(-1)) { // if moved reset timer
						_lockTimer.restart();				}
					break;
				case SOFTDOWN:
					// ignore in LOCK
					break;
				case HARDDOWN:
					while (!_playfield.moveDown()) {
						// -- tell the view that model has changed
						setChanged();
						notifyObservers("During LOCK after HARDDOWN");
					}
					breakFlag = true;
					break;
				case HOLD:
					// ignore in LOCK
					break;
				case NONE:
					break;
			}

			// check if Tetrimino can move down
			// if yes then go back to phase FALLING
			if (_playfield.canMoveDown()) {
				//System.out.println("LOCK CAN MOVE -> FALLING");
				breakFlag = true;
				_phaseState = TetrisEtat.FALLING;
			}

			// -- tell the view that model has changed
			setChanged();
			notifyObservers("During LOCK");
			//_sounds.playClip(Clips.LOCK);

		} while (!breakFlag && _lockTimer.getRemainingTime() > 0);

		// stop the timer just to make sure
		_lockTimer.stop();

		// allow new holds
		_holdAllowed = true;

		// merge Tetrimino into background
		if (_phaseState == TetrisEtat.LOCK) {// only merge if we are still in phase LOCK
			_playfield.merge();
			_phaseState = TetrisEtat.PATTERN; // go to next phase
		}

	}

	/**
	 * PATTERN phase
	 * This phase marks all lines for clearance in the ELIMINATE phase.
	 */
	private void patternPhase() {
		//System.out.println("Enter PATTERN phase");

		// TODO: check for game over - locked piece higher than skyline


		// look for LINE CLEAR
		_playfield.markLinesToBeCleared();

		// currently no other patterns to look for 

		_phaseState = TetrisEtat.ITERATE;
	}

	/**
	 * ELIMINATE phase
	 * This phase removes all lines from the playfield which were marked for clearance.<br/>
	 * Also handles game statistics like scoring, bonus scores, etc.
	 * TODO: BackToBack Bonus
	 * TODO: SPINS and SPIN Bonus
	 */
	private void eliminatePhase() {

		// clear lines
		_lastClearedLinesCount = _playfield.clearMarkedLines();

		// score
		_score += calculateLineClearScore(_lastClearedLinesCount);
		_score += _lastSoftDropLineCount; // soft drop points 1 x number of lines
		_score += _lastHardDropLineCount * 2; // hard drop points 2 x number of lines
		_lineCount += _lastClearedLinesCount;

		// other statistics
		if (_lastClearedLinesCount > 0) {
			//_sounds.playClip(Clips.LINECLEAR);
			if (_lastClearedLinesCount == 4) {
				_tetrisesCount++;
				// -- tell the view that model has changed
				setChanged();
				notifyObservers("TETRIS");
				//_sounds.playClip(Clips.TETRIS);
			}
		}

		// reset the counters
		_lastHardDropLineCount = 0;
		_lastSoftDropLineCount = 0;
		_lastClearedLinesCount = 0;

		_phaseState = TetrisEtat.COMPLETION;
	}

	/**
	 * COMPLETION phase
	 * This is where any updates to information fields on the Tetris playfield are updated, such as the Score and Time. 
	 * The Level Up condition is also checked to see if it is necessary to advance the game level.
	 */
	private void completionPhase() {
		// set level - FIXED GOAL SYSTEM
		// TODO: implement VARIABLE GOAL SYSTEM
		if (_lineCount > 0) {
			int old = _currentLevel;
			_currentLevel = _lineCount/10 +1;
			if (_currentLevel > 15) _currentLevel = 15;
			if (_currentLevel > old) {
				//_sounds.playClip(Clips.LEVELUP);
			}
		}
		_phaseState = TetrisEtat.GENERATION;
	}

	/**
	 * @param numberOfClearedLines
	 * @return score for the last placement
	 */
	private int calculateLineClearScore(int numberOfClearedLines) {
		int score = 0;
		switch (numberOfClearedLines) {
			case 0: break;
			case 1: score += 100 * _currentLevel;
			case 2: score += 300 * _currentLevel;
			case 3: score += 500 * _currentLevel;
			case 4: score += 800 * _currentLevel;
		}
		return score;
	}

	/**
	 * @return the falling time for the current level
	 */
	private long calculateFallingTime() {
		switch (_currentLevel) {
			case 1: return 1000;
			case 2: return 793;
			case 3: return 618;
			case 4: return 473;
			case 5: return 355;
			case 6: return 262;
			case 7: return 190;
			case 8: return 135;
			case 9: return 94;
			case 10: return 64;
			case 11: return 43;
			case 12: return 28;
			case 13: return 18;
			case 14: return 11;
			case 15: return 7;
			default: return 7;
		}
	}

	/**
	 * This is called from the VueControleur to add control events (e.g. key press) to our queue.
	 * @param e
	 */
	public void controlQueueAdd(TetrisControlEvents e) {
		_controlQueue.add(e);
	}

	/**
	 * This is called from the timer to wake us from waiting for a key event.
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		_controlQueue.add(TetrisControlEvents.NONE);
	}

	/*
	 * Checks if game is paused and waits until game is resumed
	 */
	private void waitIfPaused() {
		if (_isPaused) {
			while (_isPaused && _gameStopped == false) {
				//System.out.println("PAUSED "+LocalDateTime.now());
				try {
					synchronized (_gameThread) {
						_gameThread.wait();
					}
				} catch (InterruptedException e) { /* nothing */ }
			}
		}
	}

	/**
	 * Checks if the game is running
	 * @return true if game is running
	 */
	public boolean isRunning() {
		return !_gameStopped;
	}

	/**
	 * @return the _isPaused
	 */
	public boolean isPaused() {
		return _isPaused;
	}

	/**
	 * @param _isPaused the _isPaused to set
	 */
	public void setPaused(boolean _isPaused) {
		this._isPaused = _isPaused;
		synchronized (_gameThread) {
			_gameThread.notify();
		}
		// -- tell the view that model has changed
		setChanged();
		notifyObservers("Game paused: "+_isPaused);
	}

	/**
	 * @return the _playfield
	 */
	public Grille getMatrix() {
		return _playfield;
	}

	/**
	 * @return the _nextQueue
	 */
	public PiecesSuivante getNextQueue() {
		return _piecesSuivante;
	}

	/**
	 * @return the Tetrimino in hold
	 */
	public Tetrimino getHoldTetrimino() {
		return _holdQueue;
	}

	/**
	 * @return the _startLevel
	 */
	public int getStartLevel() {
		return _startLevel;
	}

	/**
	 * @param _startLevel the _startLevel to set
	 */
	public void setStartLevel(int _startLevel) {
		this._startLevel = _startLevel;
	}

	/**
	 * @return the _currentLevel
	 */
	public int getCurrentLevel() {
		return _currentLevel;
	}

	/**
	 * @return the _score
	 */
	public int getScore() {
		return _score;
	}

	/**
	 * @return the _nextQueue
	 */
	public MeilleurScore getHighScoreData() {
		return _meilleurScore;
	}

	/**
	 * @return the _lineCount
	 */
	public int getLineCount() {
		return _lineCount;
	}

	/**
	 * @return the _tetrisesCount
	 */
	public int getTetrisesCount() {
		return _tetrisesCount;
	}

	/**
	 * @return the _phaseState
	 */
	public TetrisEtat getPhaseState() {
		return _phaseState;
	}

	/**
	 * @param text
	 */
	public void setPlayerName(String text) {
		_playerName = text;
	}



}
