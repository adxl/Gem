package sample.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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
import sample.Main;

import java.io.*;
import java.util.*;

public class Controller_Vue {
	@FXML
	private AnchorPane promptAnchorPane;
	@FXML
	private TabPane tabPane;

	private int untitledIdCounter;
	private TextArea currentTextArea;
	private TextArea currentLinesCounter;
	private ScrollBar currentTextScrollBar;
	private ScrollBar currentLinesCounterScrollBar;

	private boolean isListened=false;
	private boolean isReady=false;

	@FXML
	private Slider fontSizeSlider;
	@FXML
	private Label fontSizeSliderIcon;

	@FXML
	private VBox openFilesList;
	private HashMap<String,File> openFiles=new HashMap<>();
	private HashMap<TextArea,Boolean> currentFiles=new HashMap<>();

	@FXML
	private Label fileType;
	@FXML
	private Label filePath;

	private ChangeListener<String> textChangeListener;

	private String defaultSVGPathProperty="shape:\"M 0,0 H1 L 4,3 7,0 H8 V1 L 5,4 8,7 V8 H7 L 4,5 1,8 H0 V7 L 3,4 0,1 Z\";";
	private String circleSVGPathProperty="shape:\"M 500 300 A 50 50 0 1 1 700 300 A 50 50 0 1 1 500 300 Z\";";

	@FXML
	private void initialize() throws IOException {
		untitledIdCounter=1;
		fontSizeSlider.setMin(10);
		fontSizeSlider.setMax(20);
		fontSizeSlider.setValue(13);
		fontSizeSlider.valueProperty().addListener(this::fontSizeSliderListener);
		textChangeListener=this::textAreaChanged;
		if(Main.getPassedFile()!=null)
		{
			openExistingFile(Main.getPassedFile().getAbsolutePath());
		}
		final BooleanBinding isTabPaneEmpty=Bindings.isEmpty(tabPane.getTabs());
		promptAnchorPane.visibleProperty().bind(isTabPaneEmpty);
		fontSizeSlider.visibleProperty().bind(isTabPaneEmpty.not());
		fontSizeSliderIcon.visibleProperty().bind(isTabPaneEmpty.not());
	}

	private void tabSwitchListener(Tab tab) {
		tab.setOnSelectionChanged(event->
		{
			if(tab.isSelected())
			{
				currentTextAreaListener();
				int index=tabPane.getTabs().indexOf(tab);
				if(tabPane.getTabs().size()==1 && openFilesList.getChildren().size()==2)
					openFilesList.getChildren().get(1).setStyle("-fx-text-fill:#cdcdcd");
				else
					for(Node l : openFilesList.getChildren())
						l.setStyle("-fx-text-fill:#6D7678");
				(openFilesList.getChildren().get(index)).setStyle("-fx-text-fill:#cdcdcd"); //#39ea49 green
				fileType.setText(getType(tab.getText()));
				if(openFiles.get(tab.getText())!=null)
					filePath.setText(String.valueOf(openFiles.get(tab.getText())));
				else
					filePath.setText("");
				try
				{
					Main.setMainStageTitle(tab.getText());
				} catch(NullPointerException ignored)
				{
				}
			} else
			{
				Main.setMainStageTitle("Gem");
				fileType.setText("");
				filePath.setText("");
			}
		});
	}

	private void fontSizeSliderListener(ObservableValue<? extends Number> observableValue,Number previousValue,Number currentValue) {
		currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		currentLinesCounter.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		currentTextArea.requestFocus();
	}

	private void currentTextAreaListener() {
		isListened=false;
		isReady=false;
		currentTextArea=(TextArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(1);
		if(!currentFiles.containsKey(currentTextArea))
		{
			setModified(false);
		} else
		{
			if(isModified())
			{
				setModified(true);
				setLabelModified(true);
			} else
			{
				setModified(false);
				setLabelModified(false);
			}
		}
		currentLinesCounter=(TextArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(0);
		currentTextArea.textProperty().removeListener(textChangeListener);
		currentTextArea.textProperty().addListener(textChangeListener);
		currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		currentLinesCounter.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		countLines();
	}

	private void textAreaChanged(ObservableValue<? extends String> observableValue,String p,String c) {
		currentTextArea.setOnScroll(null);
		int textLines=c.split("\r\n|\r|\n",-1).length;
		int counterLines=currentLinesCounter.getText().split("\n").length;
		if(counterLines<textLines) //add row(s)
		{
			int linesDiff=textLines-counterLines;
			for(int i=0;i<linesDiff;i++)
				currentLinesCounter.appendText("\n"+(++counterLines));
		} else if(counterLines>textLines) //remove row(s)
		{
			char[] chars=currentLinesCounter.getText().toCharArray();
			int linesDiff=counterLines-textLines;
			int diffCounter=0;
			int deleteStartIndex=-1;
			for(int i=currentLinesCounter.getLength()-1;i >= 0;i--)
			{
				deleteStartIndex=i;
				if(chars[i]=='\n')
					diffCounter++;
				if(diffCounter==linesDiff)
					break;
			}
			currentLinesCounter.deleteText(deleteStartIndex,currentLinesCounter.getLength());
		}
		currentTextScrollBar=(ScrollBar)currentTextArea.lookup(".scroll-bar:vertical");
		currentLinesCounterScrollBar=(ScrollBar)currentLinesCounter.lookup(".scroll-bar:vertical");
		if(!isListened)
		{
			currentLinesCounterScrollBar.valueProperty().bindBidirectional(currentTextScrollBar.valueProperty());
			isListened=true;
		}
		setModified(true);
		setLabelModified(true);
	}

	@FXML
	private void createFile() throws IOException, InterruptedException {
		Tab tab=new Tab("Untitled "+untitledIdCounter++,FXMLLoader.load(getClass().getResource("../views/tab.fxml")));
		addFileToOpenFilesList(tab);
		openFiles.put(tab.getText(),null);
		tabSwitchListener(tab);
		tab.setOnCloseRequest(event->closeFile(tab.getText()));
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		currentTextArea=(TextArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(1);
		((AnchorPane)tab.getContent()).getChildren().get(1).requestFocus();
	}

	private void createPrefFile(String title,String text) throws IOException {
		Tab tab=new Tab(title,FXMLLoader.load(getClass().getResource("../views/tab.fxml")));
		addFileToOpenFilesList(tab);
		tabSwitchListener(tab);
		tab.setOnCloseRequest(event->closeFile(tab.getText()));
		((TextArea)((AnchorPane)tab.getContent()).getChildren().get(1)).setText(text);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		((AnchorPane)tab.getContent()).getChildren().get(1).requestFocus();
		if(!isReady)
		{
			currentTextArea.setOnScroll(event->
			{
				int length=currentTextArea.getLength();
				currentTextArea.appendText(" ");
				currentTextArea.deleteText(length-1,length);
			});
			isReady=true;
		}
	}

	@FXML
	private void openFile() throws IOException {
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
				openFiles.put(selectedFile.getName(),selectedFile);
				createPrefFile(selectedFile.getName(),stringBuilder.toString());
			} else
				for(Tab t : tabPane.getTabs())
					if(t.getText().equals(selectedFile.getName()))
					{
						tabPane.getSelectionModel().select(t);
						return;
					}
		}
	}

	private void openExistingFile(String path) throws IOException {
		File file=new File(path);
		FileReader fileReader=new FileReader(path);
		BufferedReader bufferedReader=new BufferedReader(fileReader);
		StringBuilder stringBuilder=new StringBuilder();
		String text;
		while((text=bufferedReader.readLine())!=null)
		{
			stringBuilder.append(text).append("\n");
		}
		openFiles.put(file.getName(),file);
		createPrefFile(file.getName(),stringBuilder.toString());
	}

	private void closeFile(String title) {
		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		boolean wasModified=isModified();
		currentFiles.remove(currentTextArea);
		tabPane.getTabs().remove(tab);
		if(wasModified)
			removeFileFromOpenFilesList(title+"*");
		else
			removeFileFromOpenFilesList(title);
		openFiles.remove(title);
	}

	@FXML
	private void closeFileRequest() {
		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		String title=tab.getText();
		closeFile(title);
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

	@FXML
	private void saveFile() throws IOException {
		String fileName=tabPane.getSelectionModel().getSelectedItem().getText();
		if(openFiles.get(fileName)==null) //saving an untitled file
		{
			saveFileAs();
		} else //saving an existing file
		{
			PrintWriter writer=new PrintWriter(openFiles.get(fileName));
			writer.println(currentTextArea.getText());
			writer.close();
			setModified(false);
		}
	}

	@FXML
	private void saveFileAs() throws IOException {
		//		String fileName=tabPane.getSelectionModel().getSelectedItem().getText();
		if(!currentTextArea.getText().isEmpty())
		{
			Stage stage=new Stage();
			FileChooser fileChooser=new FileChooser();
			File file=fileChooser.showSaveDialog(stage);
			if(file!=null)
			{
				PrintWriter writer=new PrintWriter(file);
				writer.println(currentTextArea.getText());
				writer.close();
				closeFileRequest();
				openExistingFile(file.getAbsolutePath());
				setModified(false);
				setLabelModified(false);
			}
		}
	}

	private String getType(String name) {
		if(name.matches(".*[.].*"))
		{
			return name.substring(name.lastIndexOf(".")+1).toUpperCase();
		}
		return "";
	}

	private void countLines() {
		int textLines=currentTextArea.getText().split("\r\n|\r|\n",-1).length;
		int counterLines=currentLinesCounter.getText().split("\n").length;
		if(textLines!=counterLines)
		{
			StringBuilder lines=new StringBuilder();
			lines.append(1);
			for(int i=2;i<=textLines;i++)
			{
				lines.append("\n").append(i);
			}
			currentLinesCounter.setText(lines.toString());
		}
	}

	private boolean isModified() {
		return currentFiles.get(currentTextArea);
	}

	private void setModified(boolean b) {
		//		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		if(b) //set modified
		{
			tabPane.setStyle(circleSVGPathProperty);
			tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-font-style:italic;");
			currentFiles.put(currentTextArea,true);
		} else //set original
		{
			tabPane.setStyle(defaultSVGPathProperty);
			tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-font-style:normal;");
			currentFiles.put(currentTextArea,false);
		}
	}

	private void setLabelModified(boolean b) {
		String tabName=tabPane.getSelectionModel().getSelectedItem().getText();
		if(b) //set modified
		{
			for(Label l : openFilesList.getChildren().toArray(new Label[0]))
			{
				if(l.getText().equals(tabName) && l.getText().charAt(l.getText().length()-1)!='*')
					l.setText(tabName+"*");
			}
		} else
		{
			for(Label l : openFilesList.getChildren().toArray(new Label[0]))
			{
				if(l.getText().equals(tabName) && l.getText().charAt(l.getText().length()-1)=='*')
					l.setText(tabName.substring(tabName.length()-1));
			}
		}
	}

	@FXML
	private void exitApplication() {
		System.exit(0);
	}
	//
}

