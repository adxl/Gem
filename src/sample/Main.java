package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("vue.fxml"));
        primaryStage.setScene(new Scene(root,800,600));
//        primaryStage.setMinHeight(200);
//        primaryStage.setMinWidth(400);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void setMainStageTitle(String title)
    {
        mainStage.setTitle(title);
    }

    public static void main(String[] args) {
        launch(args);
    }


}
