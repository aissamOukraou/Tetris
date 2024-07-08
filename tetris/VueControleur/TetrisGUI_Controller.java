
package tetris.VueControleur;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import tetris.Models.game.MeilleurScore;
import   tetris.Models.game.TetrisControlEvents;
import tetris.Models.game.OrdonnanceurSimple;
import   tetris.Models.game.TetrisSettings;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


public class TetrisGUI_Controller implements Observer {

	private static final WindowStateFX windowState = WindowStateFX.getInstance(); // to save and restore the last position of our window
	private static final TetrisSettings settings = TetrisSettings.getInstance();
	private Stage _primaryStage; // handle to primary stage
	private OrdonnanceurSimple _ordonnanceurSimple; // holds a running tetrisGame
	private PlayfieldPane _playfieldPane; // handle to PlayfieldPane
	private PiecesSuivantePane _piecesSuivantePane; // handle to NextQueuePane
	private HoldPane _holdPane; // handle to NextQueuePane

	// to use for scheduled updates of VueControleur properties - e.g. mem status label
	private final ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

	private String _oldPlayerName;

	/**
	 * This method is called by the FXMLLoader when initialization is complete
	 */
	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {

		_primaryStage = TetrisGUI.getPrimaryStage(); // set convenience reference to primary stage

		assertFXML(); // FXML auto checks

		statusbar_copyright_text.setText("Tetris(c) GAME");

		addPlayfieldPane(); // add the playfield pane
		//addNextQueuePane(); // add the next queue pane
		addHoldPane(); // add the hold Tetrimino pane
		addHowToText();
		updateHighScoreText();
		updateStatus();

		// change the startLevelLabel when the slider changes
		startLevelLabel.textProperty().bind(
				Bindings.format(
						"%.0f",
						startLevelSlider.valueProperty()
						)
				);

		readSettings();
		

	}


	/**
	 * Handles keyboard events - call from Main gui class
	 */
	protected void addKeyEventHandler() {
		// only when game is available and not a bot playing
		_primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (_ordonnanceurSimple == null ) return;

				switch (event.getCode()) {
				case ESCAPE: _ordonnanceurSimple.setPaused(_ordonnanceurSimple.isPaused() ? false : true); break;
				case LEFT:	_ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.LEFT); break;
				case RIGHT:	_ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.RIGHT); break;
				case X:
				case UP:	_ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.RTURN); break;
				case Y: // HACK_ in case of different keyboard layout
				case Z:		
				case CONTROL: _ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.LTURN); break;
				case DOWN:	_ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.SOFTDOWN); break;
				case SPACE:	_ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.HARDDOWN); break;
				case SHIFT:
				case C:		_ordonnanceurSimple.controlQueueAdd(TetrisControlEvents.HOLD); break;
				default:
				}
			}
		}); 
	}

	/**
	 * Creates a PlayfieldPane and adds it to the root panel  
	 */
	private void addPlayfieldPane() {
		_playfieldPane = new PlayfieldPane(this);
		rootPanel.setCenter(_playfieldPane);
	}

	/**
	 * Creates a NextQueuePane and adds it to the root panel  
	 */
	private void addNextQueuePane() {
		_piecesSuivantePane = new PiecesSuivantePane();
		AnchorPane.setTopAnchor(_piecesSuivantePane, 0.0);
		AnchorPane.setBottomAnchor(_piecesSuivantePane, 0.0);
		AnchorPane.setLeftAnchor(_piecesSuivantePane, 0.0);
		AnchorPane.setRightAnchor(_piecesSuivantePane, 0.0);
	}

	/**
	 * Creates a NextQueuePane and adds it to the root panel  
	 */
	private void addHoldPane() {
		_holdPane = new HoldPane();
		AnchorPane.setTopAnchor(_holdPane, 0.0);
		AnchorPane.setBottomAnchor(_holdPane, 0.0);
		AnchorPane.setLeftAnchor(_holdPane, 0.0);
		AnchorPane.setRightAnchor(_holdPane, 0.0);
		holdBox.getChildren().add(_holdPane);
	}

	/**
	 * Add HotToText to pane 
	 */
	private void addHowToText() {
		// howto Text
		Text howTo = new Text(String.format(
				"\nL déplacer à gauche.%n"
						+ "R déplacer à droite.%n"
						+ "DOWN bas pour chute douce.%n"
						+ "SPACE pour chute rapide.%n"
						+ "A tourner à gauche.%n"
						+ "S tourner à droite.%n"
						+ "UP pour tourner à droite.%n"
						+ "C pour Holder la piece.)"));
		howTo.setStyle("-fx-font-family: Comic Sans MS Bold; -fx-fill: red; -fx-font-size: 8pt");
		howtoText.getChildren().add(howTo);
	}


	/**
	 * This is called by model explicitly whenever the model changes
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {


		if (_ordonnanceurSimple != null && _ordonnanceurSimple.isRunning()) { // game is running
			_playfieldPane.setPlayField(_ordonnanceurSimple.getMatrix());
			_holdPane.setHoldTetrimino(_ordonnanceurSimple.getHoldTetrimino());
			PlatformUtil.platformRunAndWait(() -> setUItoGameRunning()); // setup VueControleur
			PlatformUtil.platformRunAndWait(() -> draw()); // draw panes
		} else { // no game 
			// if we just played a game continue to show the playfield after game over or game stopped
			if (_ordonnanceurSimple != null) _playfieldPane.setPlayField(_ordonnanceurSimple.getMatrix());
			else _playfieldPane.setPlayField(null);
			_holdPane.setHoldTetrimino(null);
			PlatformUtil.platformRunAndWait(() -> setUItoGameNotRunning()); // setup VueControleur
			PlatformUtil.platformRunAndWait(() -> draw()); // draw panes
		}
	}

	/**
	 * calls draw for all panes  
	 */
	private void draw() {
		_playfieldPane.draw();
		_holdPane.draw();
		updateScoreDraw();
		updateHighScoreText();
		updateStatus();


	}

	/**
	 * Updates the status bar info 
	 */
	private void updateStatus() {
		if (_ordonnanceurSimple ==null) {
			statusbar_status_text.setText("No Game started");
			return;
		}
		switch (_ordonnanceurSimple.getPhaseState()) {
		case NOTSTARTED: statusbar_status_text.setText("No game started!"); break;
		case GENERATION: statusbar_status_text.setText("Spawn Tetrimino!"); break;
		case FALLING: statusbar_status_text.setText("Tetrimino falling!"); break;
		case LOCK: statusbar_status_text.setText("Tetrimino locking!"); break;
		case PATTERN:
		case ITERATE:
		case ANIMATE:
		case ELIMINATE:
		case COMPLETION: statusbar_status_text.setText("Game running!"); break;
		case GAMEOVER: statusbar_status_text.setText("JEU TERMINER!"); break;
		}
		if (_ordonnanceurSimple.isPaused()) statusbar_status_text.setText("Game paused.");
	}

	/**
	 * Updates all info fields. E.g. score, etc. 
	 */
	private void updateScoreDraw() {
		if (_ordonnanceurSimple == null) {
			scoreLabel.setText("0");
			levelLabel.setText("1");
			linecountLabel.setText("0");
			tetrisCountLabel.setText("0");
			startLevelLabel.setText("not yet implemented"); // this is kept in UI as a property to menu or so
		} else {
			scoreLabel.setText(String.format("%,d", _ordonnanceurSimple.getScore()));
			levelLabel.setText(Integer.toString(_ordonnanceurSimple.getCurrentLevel()));
			linecountLabel.setText(Integer.toString(_ordonnanceurSimple.getLineCount()));
			//tetrisCountLabel.setText(Integer.toString(_ordonnanceurSimple.getTetrisesCount()));
		}
	}

	/**
	 *	print the highscore list 
	 */
	private void updateHighScoreText() {

		highScorePane.getChildren().clear();
		highScorePane.getHeight();

		List<Text> textlines = new ArrayList<>(15);

		final Font font = Font.font(
				"Courier New", 
				FontWeight.NORMAL, 
				FontPosture.REGULAR , 
				10);

		// highscore Text
		Text highScoreText = new Text(String.format(
				"HIGHSCORE %n"
						+ "===========%n"));
		highScoreText.setFont(font);
		highScoreText.setFill(Color.BLACK);

		textlines.add(highScoreText);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");

		List<MeilleurScore.HighScoreEntry> list = MeilleurScore.getInstance().getList();
		list.stream().limit(15).forEach((e) -> {
			final String txt = String.format("%-12.12s (%5s): %,5d %,11d%n", e.name, e.date.format(formatter),  e.lines, e.score );
			final Text tmp = new Text(txt);
			tmp.setFont(font);
			tmp.setFill(Color.BLACK);
			textlines.add(tmp);
		});

		TextFlow flow = new TextFlow();	
		flow.getChildren().addAll(textlines);

		highScorePane.getChildren().add(flow);

	}

	/**
	 * Setup controls (menu, buttons, etc.) for running game 
	 */
	private void setUItoGameRunning() {
		// -- set possible actions (menu) --
		newGame_menu.setDisable(true);
		newGame_button.setDisable(true);
		stopGame_menu.setDisable(false);
		stopGame_button.setDisable(false);
		if (_ordonnanceurSimple.isPaused()) {
			pauseGame_menu.setDisable(true);
			pauseGame_button.setDisable(true);
			resumeGame_menu.setDisable(false);
			resumeGame_button.setDisable(false);
		} else {
			pauseGame_menu.setDisable(false);
			pauseGame_button.setDisable(false);
			resumeGame_menu.setDisable(true);
			resumeGame_button.setDisable(true);
		}
		close_menu.setDisable(false);
		about_menu.setDisable(false);
		statusbar_status_text.setText("Game running");
	}

	/*
	 * Setup controls (menu, buttons, etc.) for game not running
	 */
	private void setUItoGameNotRunning() {
		// -- set possible actions (menu) --
		newGame_menu.setDisable(false);
		newGame_button.setDisable(false);
		stopGame_menu.setDisable(true);
		stopGame_button.setDisable(true);
		pauseGame_menu.setDisable(true);
		pauseGame_button.setDisable(true);
		resumeGame_menu.setDisable(true);
		resumeGame_button.setDisable(true);
		close_menu.setDisable(false);
		about_menu.setDisable(false);
		statusbar_status_text.setText("No Game");
	}

	/**
	 * Return the window state object.
	 * @return the window state
	 */
	public static WindowStateFX getWindowState() {
		return windowState;
	}

	/*
	 * Save the current sizes and coordinates of all windows to restore them
	 * when starting up the next time.
	 */
	private void saveWindowStates() {
		windowState.setProperty("windowLocationX", String.valueOf(this._primaryStage.getX()));
		windowState.setProperty("windowLocationY", String.valueOf(this._primaryStage.getY()));
		windowState.setProperty("windowSizeX", String.valueOf(this._primaryStage.getWidth()));
		windowState.setProperty("windowSizeY", String.valueOf(this._primaryStage.getHeight()));
		windowState.save();
	}

	/*
	 * Save settings to file 
	 */
	private void saveSettings() {
		settings.setProperty("player_name", playerNameField.getText());
		settings.save();
	}

	/**
	 * Reads the settings and sets the controls
	 */
	private void readSettings() {
		// read in settings
		playerNameField.setText(settings.getProperty("player_name", "Unknown Player"));
		_oldPlayerName = playerNameField.getText();
	}

	// #######################################################################
	// Actions
	// IMPORTANT: Watch out for deadlocks - JavaFX app thread calling model
	// 			  is always dangerous.	
	// #######################################################################

	/**
	 * Called when window is closed
	 * @param event
	 */
	@FXML
	void close_action(WindowEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(_primaryStage);
		alert.setTitle("Quitter");
		alert.setHeaderText("Quitter le Tetris");
		alert.setContentText("Vous voulez vraiment quitter le Tetris ?");

		// Créer un bouton "Cancel" sans texte
		ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
		//cancelButton.("Cancel");

		alert.getButtonTypes().setAll(ButtonType.OK, cancelButton);

		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			saveWindowStates();
			saveSettings();
			// Fermer la fenêtre directement plutôt que d'appeler Tetris.exitTetris()
			_primaryStage.close();
		} else {
			// ... l'utilisateur a choisi ANNULER ou a fermé la boîte de dialogue
			event.consume(); // Annuler la fermeture de la fenêtre si l'utilisateur a choisi "Annuler"
		}
	}



	@FXML
	void newGame_Action(ActionEvent event) {
		//_playfieldPane.requestFocus();
		_ordonnanceurSimple = new OrdonnanceurSimple((int)startLevelSlider.getValue());
		_ordonnanceurSimple.setPlayerName(playerNameField.getText());
		_ordonnanceurSimple.addObserver(this);
		_ordonnanceurSimple.startTetrisGame();

	}

	@FXML
	void stopGame_action(ActionEvent event) {
		_ordonnanceurSimple.stopTetrisGame();

	}

	@FXML
	void pauseGame_action(ActionEvent event) {
		_ordonnanceurSimple.setPaused(true);
	}

	@FXML
	void resumeGame_action(ActionEvent event) {
		_ordonnanceurSimple.setPaused(false);
	}

	@FXML
	void restoreFocusAction(ActionEvent event) {
		rootPanel.requestFocus();
	}

	@FXML
	void aboutDialogOpen_action(ActionEvent event) {
		AboutDialog aboutDialogStage = new AboutDialog();
		aboutDialogStage.showAndWait();
	}

	@FXML
	void playerNameChangeAction(ActionEvent event) {
		if (_ordonnanceurSimple != null) {
			_ordonnanceurSimple.setPlayerName(playerNameField.getText());
		}
		rootPanel.requestFocus();
	}



	// #######################################################
	// FXML Setup
	// #######################################################

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="newGame_button"
	private Button newGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="newGame_menu"
	private MenuItem newGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="playfieldPane"
	private Pane playfieldPane; // Value injected by FXMLLoader

	@FXML // fx:id="statusbar_copyright_test"
	private Label statusbar_copyright_text; // Value injected by FXMLLoader

	@FXML // fx:id="linecountLabel"
	private Label linecountLabel; // Value injected by FXMLLoader

	@FXML // fx:id="about_menu"
	private MenuItem about_menu; // Value injected by FXMLLoader

	@FXML // fx:id="scoreLabel"
	private Label scoreLabel; // Value injected by FXMLLoader

	@FXML // fx:id="levelLabel"
	private Label levelLabel; // Value injected by FXMLLoader

	@FXML // fx:id="startLevelLabel"
	private Label startLevelLabel; // Value injected by FXMLLoader

	@FXML // fx:id="menu_help"
	private Menu menu_help; // Value injected by FXMLLoader

	@FXML // fx:id="stopGame_button"
	private Button stopGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="pauseGame_button"
	private Button pauseGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="pauseGame_menu"
	private MenuItem pauseGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="menu_level"
	private Menu menu_level; // Value injected by FXMLLoader

	@FXML // fx:id="close_menu"
	private MenuItem close_menu; // Value injected by FXMLLoader

	@FXML // fx:id="holdBox"
	private Pane holdBox; // Value injected by FXMLLoader

	@FXML // fx:id="statusbar_mem_text"
	private Label statusbar_mem_text; // Value injected by FXMLLoader

	@FXML // fx:id="resumeGame_menu"
	private MenuItem resumeGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="menu_game"
	private Menu menu_game; // Value injected by FXMLLoader

	@FXML // fx:id="stopGame_menu"
	private MenuItem stopGame_menu; // Value injected by FXMLLoader

	@FXML // fx:id="statusbar_status_text"
	private Label statusbar_status_text; // Value injected by FXMLLoader

	@FXML // fx:id="rootPanel"
	private BorderPane rootPanel; // Value injected by FXMLLoader

	@FXML // fx:id="tetrisCountLabel"
	private Label tetrisCountLabel; // Value injected by FXMLLoader

	@FXML // fx:id="resumeGame_button"
	private Button resumeGame_button; // Value injected by FXMLLoader

	@FXML // fx:id="startLevelSlider"
	private Slider startLevelSlider; // Value injected by FXMLLoader

	@FXML // fx:id="nextQueueBox"
	private Pane nextQueueBox; // Value injected by FXMLLoader

	@FXML // fx:id="howtoText"
	private Pane howtoText; // Value injected by FXMLLoader

	@FXML // fx:id="highScorePane"
	private Pane highScorePane; // Value injected by FXMLLoader

	@FXML // fx:id="playerNameField"
	private TextField playerNameField; // Value injected by FXMLLoader



	/*
	 * FXML checks
	 */
	private void assertFXML() {
		assert newGame_button != null : "fx:id=\"newGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert newGame_menu != null : "fx:id=\"newGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert playfieldPane != null : "fx:id=\"playfieldPane\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert statusbar_copyright_text != null : "fx:id=\"statusbar_copyright_test\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert linecountLabel != null : "fx:id=\"linecountLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert about_menu != null : "fx:id=\"about_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert scoreLabel != null : "fx:id=\"scoreLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert levelLabel != null : "fx:id=\"levelLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert startLevelLabel != null : "fx:id=\"startLevelLabel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert menu_help != null : "fx:id=\"menu_help\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert stopGame_button != null : "fx:id=\"stopGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert pauseGame_button != null : "fx:id=\"pauseGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert pauseGame_menu != null : "fx:id=\"pauseGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert menu_level != null : "fx:id=\"menu_level\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert close_menu != null : "fx:id=\"close_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert holdBox != null : "fx:id=\"holdBox\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert resumeGame_menu != null : "fx:id=\"resumeGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert menu_game != null : "fx:id=\"menu_game\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert stopGame_menu != null : "fx:id=\"stopGame_menu\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert statusbar_status_text != null : "fx:id=\"statusbar_status_text\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert rootPanel != null : "fx:id=\"rootPanel\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert resumeGame_button != null : "fx:id=\"resumeGame_button\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert startLevelSlider != null : "fx:id=\"startLevelSlider\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert howtoText != null : "fx:id=\"howtoText\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert highScorePane != null : "fx:id=\"highScorePane\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
		assert playerNameField != null : "fx:id=\"playerNameField\" was not injected: check your FXML file 'TetrisGUI.fxml'.";
	}
}
