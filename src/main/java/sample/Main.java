package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

	private static String pathArg;
	private static Stage mainStage;

	public static void main(String[] args) {
		if(args.length!=0)
			pathArg=args[0];
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/vue.fxml"));
		mainStage=primaryStage;
		mainStage.setScene(new Scene(loader.load()));
		mainStage.setTitle("Gem");
		mainStage.setMaximized(true);
		mainStage.setMinHeight(200);
		mainStage.setMinWidth(400);
		mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
		mainStage.show();
		checkPassedArg();
	}

	//TEST
	//	@Override
	//	public void start(Stage primaryStage) throws Exception {
	//		Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/test.fxml")););
	//		primaryStage.setScene(scene);
	//		primaryStage.show();
	//	}

	public static Stage getMainStage() {
		return mainStage;
	}

	private void checkPassedArg() {
		File file=getPassedFile();
		if(file!=null)
			setMainStageTitle(file.getName());
	}

	public static void setMainStageTitle(String title) {
		mainStage.setTitle(title);
	}

	public static File getPassedFile() {
		try
		{
			return new File(pathArg);
		} catch(Exception e)
		{
			return null;
		}
	}
}
