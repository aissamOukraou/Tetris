package tetris;

import tetris.Models.game.MeilleurScore;
import tetris.Models.game.OrdonnanceurSimple;
import tetris.Models.game.TetrisSettings;
import tetris.VueControleur.TetrisGUI;

/**
 * Main class for Tetris app. Starts up the JavaFX VueControleur and exits.
 */
public class Tetris {

	// VERSION
	//public static final String VERSION = "1.1";
	
	// pre-load the high score data and share it through the class as static
	public static final MeilleurScore __MEILLEUR_SCORE = MeilleurScore.getInstance();
	
	// pre-load setting
	public static final TetrisSettings _tetrisSettings = TetrisSettings.getInstance();

	/**
	 * The handle to the user interface class
	 */
	public static TetrisGUI _ui;
	
	/**
	 * The handle to the model
	 */
	public static OrdonnanceurSimple _tetrisModel;
	
	/**
	 * Main creates the UI object (JavaFX Application) and waits for the UI to show. After that the thread exits as 
	 * JavaFX runs in a separate thread. 
	 * @param args - not yet used
	 */
	public static void main(String[] args) {
		_ui = new TetrisGUI();
	}

    /**
     * Clean up and exit the application
     */
    public static void exitTetris() {
    	exitTetris(0);
    }

    /**
     * Clean up and exit the application
     */
    private static void exitTetris(int returnCode) {
        // nothing to clean up yet
        System.exit(returnCode);
    }
    
    /**
     * Called when there is an unexpected unrecoverable error.<br/>
     * Prints a stack trace together with a provided message.<br/>
     * Terminates with <tt>exit(1)</tt>.
     * @param message to be displayed with the exception message
     */
    public static void fatalError(String message) {
        Exception e = new Exception(message);
        e.printStackTrace();
        exitTetris(1);
    }

    /**
     * Called when there is an unexpected but recoverable error.<br/>
     * Prints a stack trace together with a provided message.<br/>
     * @param message to be displayed with the exception message
     */
    public static void criticalError(String message) {
        Exception e = new Exception(message);
        e.printStackTrace();
    }
    
    /**
     * Called when there is an unexpected minor error.<br/>
     * Prints a provided message.<br/>
     * @param message to be displayed
     */
    public static void minorError(String message) {
        System.err.println(message);
    }
	

}
