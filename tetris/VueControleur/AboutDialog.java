
package  tetris.VueControleur;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutDialog extends Stage {

    /**
     * Creates a new window (stage) in form of the About Dialog.
     * This creates the window. Needs to be display with
     */
    public AboutDialog() {
        super();

        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TetrisGUI.class.getResource("AboutDialog.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            Scene scene = new Scene(pane);

            this.setTitle("About");
            this.initModality(Modality.WINDOW_MODAL);
            Stage stage = TetrisGUI.getPrimaryStage();
            this.initOwner(stage);
            this.setScene(scene);

            this.setOnShown((e) -> centerOnPrimaryStage());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Centers this dialog on the center of the primary stage.
     */
    private void centerOnPrimaryStage() {
        Stage stage = TetrisGUI.getPrimaryStage();
        this.setX(stage.getX() + stage.getWidth() / 2 - this.getWidth() / 2);
        this.setY(stage.getY() + stage.getHeight() / 2 - this.getHeight() / 2);
    }

}
