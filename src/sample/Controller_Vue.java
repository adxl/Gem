package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Controller_Vue implements Cloneable {
	@FXML
	private TabPane tabPane; //tabs container
	private int untitledIdCounter;
	private TextArea currentTextArea;
	private VBox currentRowCounter;

	@FXML
	private VBox openFilesList;
	//	@FXML
	//	private Label filePath;
	@FXML
	private Label fileType;

	@FXML
	private Slider fontSizeSlider;

	//	File currentFile;
	Map<String,File> openFiles=new HashMap<>();
	ChangeListener<String> changeListener;

	@FXML
	public void initialize() {
		untitledIdCounter=1;
		fontSizeSlider.setMin(10);
		fontSizeSlider.setMax(20);
		fontSizeSlider.setValue(13);
		fontSizeSlider.valueProperty().addListener(this::fontSizeSliderListener);
		changeListener=this::textAreaChanged;
	}

	private void fontSizeSliderListener(ObservableValue<? extends Number> observableValue,Number previousValue,Number currentValue) {
		currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		currentTextArea.setText(currentTextArea.getText());
		currentTextArea.requestFocus();
		for(Label l : currentRowCounter.getChildren().toArray(new Label[currentRowCounter.getChildren().size()]))
			l.setFont(Font.font("Arial",fontSizeSlider.getValue()));
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
		if(currentRowCounter.getChildren().size()!=lines)
		{
			currentRowCounter.getChildren().remove(0,currentRowCounter.getChildren().size());
			for(int i=1;i<=lines;i++)
			{
				Label tempLabel=new Label(String.valueOf(i));
				tempLabel.setFont(Font.font(fontSizeSlider.getValue()));
				currentRowCounter.getChildren().add(tempLabel);
			}
		}
	}

	private void addFileToOpenFilesList(Tab tab) {
		Label label=new Label(tab.getText());
		label.setOnMouseClicked(event->tabPane.getSelectionModel().select(tab));
		openFilesList.getChildren().add(label);
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
		tabPane.getSelectionModel().selectLast();
	}

	@FXML //DONE
	public void createFile() throws IOException {
		Tab tab=new Tab("Untitled "+untitledIdCounter++,FXMLLoader.load(getClass().getResource("newEditorTab.fxml")));
		addFileToOpenFilesList(tab);
		tabSwitchListener(tab);
		tab.setOnClosed(event->closeFile(tab.getText()));
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		((AnchorPane)tab.getContent()).getChildren().get(1).requestFocus();
	}

	private void createPrefFile(String title,String text) throws IOException {
		Tab tab=new Tab(title,FXMLLoader.load(getClass().getResource("newEditorTab.fxml")));
		addFileToOpenFilesList(tab);
		tabSwitchListener(tab);
		tab.setOnClosed(event->closeFile(tab.getText()));
		((TextArea)((AnchorPane)tab.getContent()).getChildren().get(1)).setText(text);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		((AnchorPane)tab.getContent()).getChildren().get(1).requestFocus();
	}

	private void tabSwitchListener(Tab tab) {
		tab.setOnSelectionChanged(event->
		{
			if(tab.isSelected())
			{
				//				System.out.println(">"+tab.getText());
				currentTextAreaListener();
				int index=tabPane.getTabs().indexOf(tab);
				if(tabPane.getTabs().size()==1 && openFilesList.getChildren().size()==2)
					openFilesList.getChildren().get(1).setStyle("-fx-text-fill:#cdcdcd");
				else
					for(Node l : openFilesList.getChildren())
					{
						l.setStyle("-fx-text-fill:#6D7678");
					}
				(openFilesList.getChildren().get(index)).setStyle("-fx-text-fill:#cdcdcd"); //#39ea49 green
				fileType.setText(getType(tab.getText()));
				Main.setMainStageTitle(tab.getText());
			}
		});
	}

	@FXML ///DONE
	public void openFile() throws IOException {
		Stage stage=new Stage();
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Open File");
		File selectedFile=fileChooser.showOpenDialog(stage);
		if(selectedFile!=null)
		{
			if(!openFiles.containsKey(selectedFile.getName()))
			{
				FileReader fileReader=new FileReader(selectedFile.getAbsolutePath());
				BufferedReader bufferedReader=new BufferedReader(fileReader);
				StringBuilder stringBuilder=new StringBuilder();
				String text;
				while((text=bufferedReader.readLine())!=null)
				{
					stringBuilder.append(text).append("\n");
				}
				createPrefFile(selectedFile.getName(),stringBuilder.toString());
				openFiles.put(selectedFile.getName(),selectedFile);
				//			filePath.setText(selectedFile.getAbsolutePath());
			} else
				for(Tab t : tabPane.getTabs())
					if(t.getText().equals(selectedFile.getName()))
					{
						tabPane.getSelectionModel().select(t);
						return;
					}
		}
	}

	@FXML //DONE
	public void closeFileRequest() {
		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		String title=tab.getText();
		tabPane.getTabs().remove(tab);
		closeFile(title);
	}

	@FXML //DONE
	public void closeFile(String title) {
		removeFileFromOpenFilesList(title);
	}

	@FXML //TODO SAVE1
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

	@FXML //TODO SAVE2
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

	private String getType(String name) {
		if(name.matches(".*[.].*"))
		{
			return name.substring(name.lastIndexOf(".")+1).toUpperCase();
		}
		return "";
	}

	public void exitApplication() {
		System.exit(0);
	}
	//
}
