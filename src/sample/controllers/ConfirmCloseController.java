package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ConfirmCloseController {

	@FXML
	private AnchorPane root;
	private Stage stage;
	private Controller controller;

	@FXML
	private Label warningLabel;


	public void initialize() {
	}

	public void init(Controller controller,String fileName) {
		this.controller=controller;
		stage=(Stage)root.getScene().getWindow();
		warningLabel.setText(warningLabel.getText().replace("$name$",fileName));
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
