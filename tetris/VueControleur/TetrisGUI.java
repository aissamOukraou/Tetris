
package tetris.VueControleur;

import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The JavaFX Application class
 */
public class TetrisGUI extends Application {

	/**
	 * The main controller for this JavaFX application
	 */
	public static TetrisGUI_Controller _controller;

	private static TetrisGUI _instance = null; 	// The singleton instance of this class
	private static Stage _primaryStage; 		// The primary stage
	private BorderPane _root;					// the root pane for the gui

	/**
	 * Creates the JavaFX UI and starts the JavaFX application and waits until the Stage is shown.<br/>
	 * It is not possible to instantiate this more than once - throws RunTime Exception.
	 */
	public TetrisGUI() {
		if (_instance != null)
			throw new RuntimeException("It is not possible to instantiate the GUI more than once!");

		// Startup the JavaFX platform
		Platform.setImplicitExit(false);

		Locale.setDefault(Locale.FRANCE);

		Platform.startup(() -> {
			_primaryStage = new Stage();
			_primaryStage.setTitle("Tetris ");
			start(_primaryStage);
		});

		TetrisGUI._instance = this;
		waitForUI();
	}

	/**
	 * Standard way to start a JavaFX application. Is called in the constructor.
	 */
	@Override
	public void start(Stage primaryStage) {

		TetrisGUI._primaryStage = primaryStage;

		try {

			// read FXML file and setup UI
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TetrisGUI.fxml"));
			_root = (BorderPane)fxmlLoader.load();
			_controller = fxmlLoader.getController();

			// Create the Sceen based on the FXML root pane
			Scene scene = new Scene(_root,_root.getPrefWidth(),_root.getPrefHeight());
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			_primaryStage.setScene(scene);

			// set the minimum size
			_primaryStage.setMinWidth(785);
			_primaryStage.setMinHeight(795);
			_primaryStage.setMaxWidth(785);
			_primaryStage.setMaxHeight(795);


            // get last window position and size from window state file
            double windowLocX = Double.parseDouble(
                    TetrisGUI_Controller.getWindowState().getProperty("windowLocationX", "100"));
            double windowLocY = Double.parseDouble(
            		TetrisGUI_Controller.getWindowState().getProperty("windowLocationY", "200"));
            double windowSizeX = Double.parseDouble(
            		TetrisGUI_Controller.getWindowState().getProperty("windowSizeX", "785"));
            double windowSizeY = Double.parseDouble(
            		TetrisGUI_Controller.getWindowState().getProperty("windowSizeY", "795"));

			// position and resize the window
			_primaryStage.setX(windowLocX);
			_primaryStage.setY(windowLocY);
			_primaryStage.setWidth(windowSizeX);
			_primaryStage.setHeight(windowSizeY);

			// add key handler
			_controller.addKeyEventHandler();

			// now show the window
			_primaryStage.show();

			// closeAction - close through close action
			scene.getWindow().setOnCloseRequest(event -> {
				_controller.close_action(event);
				event.consume();
			});

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits for the UI to show
	 */
	public void waitForUI() {
		// wait for the UI to show before returning
		Platform.runLater(() -> {
			do {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// handle interruption if needed
				}
			} while (_primaryStage == null || !_primaryStage.isShowing());
		});
	}


	/**
	 * @return the primary stage which has been stored as a static field
	 */
	public static Stage getPrimaryStage() {
		return _primaryStage;
	}


}
