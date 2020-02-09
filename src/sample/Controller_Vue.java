package sample;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller_Vue implements Cloneable {
	@FXML
	private TabPane tabPane; //tabs container
	private int newTabsCounter;
	private TextArea currentTextArea;
	private VBox currentRowCounter;
	private int linesCounter;

	@FXML
	private VBox openFilesList;

	@FXML
	private Label filePath;
	@FXML
	private Label fileType;
	@FXML
	private Slider fontSizeSlider;

	//	File currentFile;
	//	Map<String,File> openFiles=new HashMap<>();
	ChangeListener<String> changeListener;

	@FXML
	public void initialize() {
		linesCounter=1;
		newTabsCounter=1;
		fontSizeSlider.setMin(10);
		fontSizeSlider.setMax(20);
		fontSizeSlider.setValue(13);
		fontSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observableValue,Number previousValue,Number currentValue) {
				currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
				currentTextArea.setText(currentTextArea.getText());
				currentTextArea.requestFocus();
				for(Label l : currentRowCounter.getChildren().toArray(new Label[currentRowCounter.getChildren().size()]))
					l.setFont(Font.font("Arial",fontSizeSlider.getValue()));
			}
		});
		changeListener=this::textAreaChanged;
	}

	private void currentTextAreaListener() {
		currentRowCounter=(VBox)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(0);
		currentTextArea=(TextArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(1);
		currentTextArea.textProperty().removeListener(changeListener);
		currentTextArea.textProperty().addListener(changeListener);
		currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		for(Label l : currentRowCounter.getChildren().toArray(new Label[currentRowCounter.getChildren().size()]))
			l.setFont(Font.font("Arial",fontSizeSlider.getValue()));
	}

	private void textAreaChanged(ObservableValue<? extends String> observableValue,String p,String c) {
		int lines=c.split("\r\n|\r|\n",-1).length;
		if(linesCounter!=lines)
		{
			linesCounter=lines;
			currentRowCounter.getChildren().remove(0,currentRowCounter.getChildren().size());
			for(int i=1;i<=lines;i++)
			{
				Label tempLabel=new Label(String.valueOf(i));
				tempLabel.setFont(Font.font(fontSizeSlider.getValue()));
				currentRowCounter.getChildren().add(tempLabel);
			}
		}
	}

	private void addFileToOpenFilesList(String title) {
		openFilesList.getChildren().add(new Label(title));
	}

	private void removeFileFromOpenFilesList(String title) {
		for(Label l : openFilesList.getChildren().toArray(new Label[openFilesList.getChildren().size()]))
		{
			if(l.getText().equals(title))
			{
				openFilesList.getChildren().remove(l);
				break;
			}
		}
	}

	@FXML
	public void createFile() throws IOException {
		Tab tab=new Tab("Untitled "+newTabsCounter++,FXMLLoader.load(getClass().getResource("newEditorTab.fxml")));
		tab.setOnSelectionChanged(event->
		{
			if(tab.isSelected())
				currentTextAreaListener();
		});
		tab.setOnClosed(event->closeFile(tab.getText()));
		tabPane.getTabs().add(tab);
		addFileToOpenFilesList(tab.getText());
	}

	@FXML
	public void openFile() throws IOException {
		//		Stage stage=new Stage();
		//		FileChooser fileChooser=new FileChooser();
		//		fileChooser.setTitle("Open File");
		//		File selectedFile=fileChooser.showOpenDialog(stage);
		//		if(selectedFile!=null)
		//		{
		//			FileReader fileReader=new FileReader(selectedFile.getAbsolutePath().toString());
		//			BufferedReader bufferedReader=new BufferedReader(fileReader);
		//			StringBuilder stringBuilder=new StringBuilder();
		//			String text="";
		//			while((text=bufferedReader.readLine())!=null)
		//			{
		//				stringBuilder.append(text).append("\n");
		//			}
		//			resetLines();
		//			rowCounter.setVisible(true);
		//			textArea.setVisible(true);
		//			textArea.setText(stringBuilder.toString());
		//			textArea.requestFocus();
		//			openFiles.put(selectedFile.getAbsolutePath(),selectedFile);
		//			Main.setMainStageTitle(selectedFile.getName());
		//			filePath.setText(selectedFile.getAbsolutePath());
		//			fileType.setText(getType(selectedFile));
		//		}
		//		Stage stage=new Stage();
		//		FileChooser fileChooser=new FileChooser();
		//		fileChooser.setTitle("Open File");
		//		File selectedFile=fileChooser.showOpenDialog(stage);
		//		if(selectedFile!=null)
		//		{
		//			FileReader fileReader=new FileReader(selectedFile.getAbsolutePath());
		//			BufferedReader bufferedReader=new BufferedReader(fileReader);
		//			StringBuilder stringBuilder=new StringBuilder();
		//			String text="";
		//			while((text=bufferedReader.readLine())!=null)
		//			{
		//				stringBuilder.append(text).append("\n");
		//			}
		//			tabs.add(tabs.get(0));
		////			resetLines();
		//			AnchorPane anchor = (AnchorPane)tabs.get(tabs.size()-1).getContent();
		//			currentTextArea=(TextArea)anchor.getChildren().get(1);
		//			currentRowCounter=(VBox)anchor.getChildren().get(0);
		//			currentRowCounter.setVisible(true);
		//			currentTextArea.setVisible(true);
		//			currentTextArea.setText(stringBuilder.toString());
		//			currentTextArea.requestFocus();
		//			openFiles.put(selectedFile.getAbsolutePath(),selectedFile);
		//			Main.setMainStageTitle(selectedFile.getName());
		//			filePath.setText(selectedFile.getAbsolutePath());
		//			fileType.setText(getType(selectedFile));
		//		}
	}

	@FXML
	public void closeFile(String title) {
		removeFileFromOpenFilesList(title);
	}

	@FXML
	public void saveFile() throws IOException {
		//		if(openFiles!=null)
		//		{
		//			PrintWriter writer;
		//			writer=new PrintWriter(openFiles);
		//			writer.println(textArea.getText());
		//			writer.close();
		//			Main.setMainStageTitle(openFiles.getName());
		//			filePath.setText(openFiles.getAbsolutePath());
		//			fileType.setText(getType());
		//		} else
		//		{
		//			saveFileAs();
		//		}
	}

	@FXML
	public void saveFileAs() throws IOException {
		//		if(!textArea.getText().isEmpty())
		//		{
		//			Stage stage=new Stage();
		//			FileChooser fileChooser=new FileChooser();
		//			File file=fileChooser.showSaveDialog(stage);
		//			if(file!=null)
		//			{
		//				PrintWriter writer;
		//				writer=new PrintWriter(file);
		//				writer.println(textArea.getText());
		//				writer.close();
		//				openFiles=file;
		//				Main.setMainStageTitle(openFiles.getName());
		//				filePath.setText(openFiles.getAbsolutePath());
		//				fileType.setText(getType());
		//			}
		//		}
	}

	private String getType(File f) {
		String type="";
		try
		{
			if(f!=null && f.exists())
			{
				type=f.getName().substring(f.getName().lastIndexOf(".")+1);
			}
		} catch(Exception e)
		{
			type="";
		}
		return type.toUpperCase();
	}

	private void resetLines() {
		linesCounter=1;
		currentRowCounter.getChildren().remove(0,currentRowCounter.getChildren().size());
	}

	public void exitApplication() {
		System.exit(0);
	}
	//
}
