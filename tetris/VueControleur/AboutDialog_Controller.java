package tetris.VueControleur;


import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public class AboutDialog_Controller {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="aboutDialog_OK_button"
    private Button aboutDialog_OK_button; // Value injected by FXMLLoader

    @FXML
    void aboutDialog_closeAction(ActionEvent event) {
        Stage stage = (Stage) aboutDialog_OK_button.getScene().getWindow();
        stage.close();
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert aboutDialog_OK_button != null : "fx:id=\"aboutDialog_OK_button\" was not injected: check your FXML file 'AboutDialog.fxml'.";
    }
}
