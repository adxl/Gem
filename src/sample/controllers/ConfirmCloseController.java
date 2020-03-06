package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfirmCloseController {

	@FXML
	private AnchorPane root;
	private Stage stage;
	private Controller controller;

	@FXML
	private Label warningLabel;


	public void initialize() {
	}

	public void init(Controller controller,String style,String fileName) {
		this.controller=controller;
		stage=(Stage)root.getScene().getWindow();
		warningLabel.setText(warningLabel.getText().replace("$name$",fileName));
		root.setStyle(style);
	}

	@FXML
	private void requestSave() throws IOException {
		controller.saveFile();
		stage.close();
	}

	@FXML
	private void requestClose() {
		controller.closeFileRequest();
		stage.close();
	}
}
