
package tetris.Models.game;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TempsTetris extends Observable {
	
	ScheduledExecutorService _remainingTimeTimer =
	        Executors.newSingleThreadScheduledExecutor();
	
	TetrisTask _task = new TetrisTask();
	
	private long _timerTime;	// the initial time for this timer
	private long _remaingTime;	// remaining time (e.g. after stop)
	
	private long _lastSystemtime; // save the system time
	
	private boolean _isStarted = false; // flag to see if timer is running or stopped
	
	/**
	 * Creates a reusable timer with 0ms countdown time.<br/>
	 * timer.start() to start or resume the timer.<br/>
	 * timer.stop() to stop/pause the timer.<br/>
	 * timer.reset() to reset timer to initial time.<br/>
	 * timer.addObserver() to be notified if timer runs out of time<br/>
	 */
	public TempsTetris() {
		this(0);
	}
	
	/**
	 * Creates a reusable timer with the specified countdown time.<br/>
	 * timer.start() to start or resume the timer.<br/>
	 * timer.stop() to stop/pause the timer.<br/>
	 * timer.reset() to reset timer to initial time.<br/>
	 * timer.addObserver() to be notified if timer runs out of time<br/>
	 */
	public TempsTetris(long t) {
		_timerTime = t;
		_remaingTime = t;
	}
	
	/**
	 * Sets the timer to the specified time in ms.</br>
	 * Timer must be stopped before this is called otherwise it throws a RuntimeException. 
	 * @param t - time delay for the timer
	 * @throws RuntimeException if Timer is running
	 */
	public void setTimer(long t) {
		if (_isStarted) 
			throw new RuntimeException("Resetting a running timer is not allowed!");
		_timerTime = t;
		_remaingTime = t;
	}
	
	/**
	 * Starts or re-starts a timer with the remaining time.
	 * Is ignored if already started  
	 */
	public synchronized void start() {
		if (_isStarted) return; // ignore
		if (_remaingTime <= 0) return;
		_lastSystemtime = System.currentTimeMillis();
		_remainingTimeTimer.schedule(_task, _remaingTime, TimeUnit.MILLISECONDS);
		_isStarted = true;
	}
	
	/**
	 * Stops a running timer. Can be restarted with the remaining time with start()<br/>
	 * Is ignored of timer is already stopped. 
	 */
	public synchronized void stop() {
		if (!_isStarted) return; // ignore
		_remaingTime -= System.currentTimeMillis() - _lastSystemtime;
		_remainingTimeTimer.shutdownNow(); // to cancel all waiting tasks
		_remainingTimeTimer = Executors.newSingleThreadScheduledExecutor();
		_isStarted = false;
	}
	
	/**
	 * Reset a stopped timer to its initial remaining time 
	 * @throws RuntimeException if Timer is running
	 */
	public synchronized void reset() {
		if (_isStarted) 
			throw new RuntimeException("Resetting a running timer is not allowed!");
		_remaingTime = _timerTime;
	}
	
	/**
	 * Starts the timer with its initial time delay.</br>
	 * Stops the timer if it is currently running.
	 */
	public void restart() {
		stop();
		reset();
		start();
	}
	
	/**
	 * Returns the remaining time of this timer.
	 * @return remaining time
	 */
	public synchronized long getRemainingTime() {
		if (_isStarted)
			return _remaingTime - (System.currentTimeMillis()-_lastSystemtime);
		else
			return _remaingTime;
	}
	
	/**
	 * Returns if the timer is currently running.
	 * @return true if timer is currently running, false otherwise
	 */
	public synchronized boolean isRunning() {
		return _isStarted; 
	}

	/**
	 * This is the Task the Timer runs when it is started
	 */
	private final class TetrisTask implements Runnable  {
		@Override
		public void run() {
			_isStarted = false;
			_remaingTime = 0;
		    setChanged();
		    notifyObservers();
		}
	}

}
