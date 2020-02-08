package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.midi.SoundbankResource;
import java.io.*;
import java.util.List;

public class Controller_Vue {
	@FXML
	private TextArea textArea;
	@FXML
	private VBox rowCounter;
	private int linesCounter=1;
	private Label tempLabel;
	@FXML
	private Label filePath;
	@FXML
	private Label fileType;
	@FXML
	private Slider fontSizeSlider;

	File currentFile;

	@FXML
	public void initialize() {
		fontSizeSlider.setMin(10);
		fontSizeSlider.setMax(20);
		fontSizeSlider.setValue(13);
		fontSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observableValue,Number previousValue,Number currentValue) {
				textArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
				textArea.setText(textArea.getText());
				textArea.requestFocus();
				for(Label l : (Label[])rowCounter.getChildren().toArray(new Label[rowCounter.getChildren().size()]))
					l.setFont(Font.font("Arial",fontSizeSlider.getValue()));
			}
		});
		textArea.textProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observableValue,String p,String c) {
				int lines=c.split("\r\n|\r|\n",-1).length;
				if(linesCounter!=lines)
				{
					linesCounter=lines;
					rowCounter.getChildren().remove(0,rowCounter.getChildren().size());
					for(int i=1;i<=lines;i++)
					{
						tempLabel=new Label(String.valueOf(i));
						tempLabel.setFont(Font.font(fontSizeSlider.getValue()));
						rowCounter.getChildren().add(tempLabel);
					}
				}
			}
		});
		//TODO QUICK CREATION FOR TEST, REMOVE AFTER
		createFile();
	}

	@FXML
	public void createFile() {
		closeFile();
		resetLines();
		rowCounter.setVisible(true);
		textArea.setVisible(true);
		textArea.requestFocus();
		Main.setMainStageTitle("Unsaved document");
	}

	@FXML
	public void openFile() throws IOException {
		Stage stage=new Stage();
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Open File");
		File selectedFile=fileChooser.showOpenDialog(stage);
		if(selectedFile!=null)
		{
			FileReader fileReader=new FileReader(selectedFile.getAbsolutePath().toString());
			BufferedReader bufferedReader=new BufferedReader(fileReader);
			StringBuilder stringBuilder=new StringBuilder();
			String text="";
			while((text=bufferedReader.readLine())!=null)
			{
				stringBuilder.append(text).append("\n");
			}
			resetLines();
			rowCounter.setVisible(true);
			textArea.setVisible(true);
			textArea.setText(stringBuilder.toString());
			textArea.requestFocus();
			currentFile=selectedFile;
			Main.setMainStageTitle(currentFile.getName());
			filePath.setText(currentFile.getAbsolutePath());
			fileType.setText(getType());
		}
	}

	@FXML
	public void closeFile() {
		currentFile=null;
		Main.setMainStageTitle("Gem");
		filePath.setText("");
		fileType.setText("");
		resetLines();
		rowCounter.setVisible(false);
		textArea.clear();
		textArea.setVisible(false);
	}

	@FXML
	public void saveFile() throws IOException {
		if(currentFile!=null)
		{
			PrintWriter writer;
			writer=new PrintWriter(currentFile);
			writer.println(textArea.getText());
			writer.close();
			Main.setMainStageTitle(currentFile.getName());
			filePath.setText(currentFile.getAbsolutePath());
			fileType.setText(getType());
		} else
		{
			saveFileAs();
		}
	}

	@FXML
	public void saveFileAs() throws IOException {
		if(!textArea.getText().isEmpty())
		{
			Stage stage=new Stage();
			FileChooser fileChooser=new FileChooser();
			File file=fileChooser.showSaveDialog(stage);
			if(file!=null)
			{
				PrintWriter writer;
				writer=new PrintWriter(file);
				writer.println(textArea.getText());
				writer.close();
				currentFile=file;
				Main.setMainStageTitle(currentFile.getName());
				filePath.setText(currentFile.getAbsolutePath());
				fileType.setText(getType());
			}
		}
	}

	private String getType() {
		String type="";
		try
		{
			if(currentFile!=null && currentFile.exists())
			{
				type=currentFile.getName().substring(currentFile.getName().lastIndexOf(".")+1);
			}
		} catch(Exception e)
		{
			type="";
		}
		return type.toUpperCase();
	}

	private void resetLines() {
		linesCounter=1;
		rowCounter.getChildren().remove(0,rowCounter.getChildren().size());
	}

	public void exitApplication() {
		System.exit(0);
	}
	//
}
