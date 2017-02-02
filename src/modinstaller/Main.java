package modinstaller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


import static modinstaller_logic.Paths.loadModsPath;
import static modinstaller_logic.Paths.setWindow;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        setWindow(primaryStage);
        if (!loadModsPath(false)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No installation directory");
            alert.setContentText("You have to select a installation directory to use the modinstaller.");
            alert.showAndWait();
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            primaryStage.setTitle("Minetest Modinstaller");

            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
