package sample.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.Main;

import java.io.*;
import java.util.*;

public class Controller_Vue {
	@FXML
	private AnchorPane appRoot;
	@FXML
	private AnchorPane paletteContainer;
	@FXML
	private AnchorPane palette;
	@FXML
	private GridPane paletteGrid;
	@FXML
	private Button confirmPaletteButton;
	@FXML
	private Button cancelPaletteButton;

	@FXML
	private AnchorPane promptAnchorPane;
	@FXML
	private TabPane tabPane;
	@FXML
	public SplitPane splitPane;

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

	private ArrayList<HashMap<Character,Integer>> currentPalette=new ArrayList<>();

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
		//TODO : disabled font size modifications for a moment
		fontSizeSlider.setVisible(false);
		fontSizeSliderIcon.setVisible(false);
		/*fontSizeSlider.visibleProperty().bind(isTabPaneEmpty.not());
		fontSizeSliderIcon.visibleProperty().bind(isTabPaneEmpty.not());*/
		SplitPane.setResizableWithParent(splitPane,false);
		SplitPane.setResizableWithParent(splitPane.getItems().get(0),false);
		openExistingFile("Gem.iml");
		openExistingFile("README.md");
		openExistingFile("todo.md");
		createFile();
		tabPane.getSelectionModel().select(0);
		setDefaultTheme();
	}

	private void tabSwitchListener(Tab tab) {
		tab.setOnSelectionChanged(event->
		{
			if(tab.isSelected())
			{
				currentTextAreaListener();
				int index=tabPane.getTabs().indexOf(tab);
				if(tabPane.getTabs().size()==1 && openFilesList.getChildren().size()==2)
					openFilesList.getChildren().get(1).setStyle("-fx-text-fill:_TEXT");
				else
					for(Node l : openFilesList.getChildren())
						l.setStyle("-fx-text-fill:_DETAILS");
				(openFilesList.getChildren().get(index)).setStyle("-fx-text-fill:_TEXT"); //#39ea49 green
				fileType.setText(getType(tab.getText()));
				if(openFiles.get(tab.getText())!=null)
					filePath.setText(String.valueOf(openFiles.get(tab.getText())));
				else
					filePath.setText("");
				if(isModified())
				{
					Main.setMainStageTitle(tab.getText()+"   (modified)");
				} else
				{
					Main.setMainStageTitle(tab.getText());
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
		Platform.runLater(currentTextArea::requestFocus);
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
	private void createFile() throws IOException {
		Tab tab=new Tab("Untitled"+untitledIdCounter++,FXMLLoader.load(getClass().getResource("/sample/views/tab.fxml")));
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
		Tab tab=new Tab(title,FXMLLoader.load(getClass().getResource("/sample/views/tab.fxml")));
		addFileToOpenFilesList(tab);
		tabSwitchListener(tab);
		tab.setOnCloseRequest(event->closeFile(tab.getText()));
		((TextArea)((AnchorPane)tab.getContent()).getChildren().get(1)).setText(text);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		((AnchorPane)tab.getContent()).getChildren().get(1).requestFocus();
		currentTextArea.setOnMouseEntered(event->
		{
			if(!isReady && !isModified())
			{
				currentTextArea.appendText(" ");
				currentTextArea.deleteText(currentTextArea.getLength()-1,currentTextArea.getLength());
				currentTextArea.positionCaret(0);
				setModified(false);
				setLabelModified(false);
				isReady=true;
			}
		});
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
			removeFileFromOpenFilesList(title+"⚫");
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
			setLabelModified(false);
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
		if(name.matches(".+[.].⚫"))
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

	@FXML
	private void showPalette() {
		paletteContainer.setVisible(true);
	}

	private void hidePalette() {
		paletteContainer.setVisible(false);
	}

	@FXML
	private void confirmTheme() {
		String[] colors=new String[4];
		for(int i=0;i<4;i++)
		{
			colors[i]=((ColorPicker)paletteGrid.getChildren().get(i+4)).getValue().toString().substring(2,10).toUpperCase();
		}
		for(String color : colors)
		{
			currentPalette.add(colorToRGB(color));
		}
		String style="_PRIMARY:"+colorToHex(currentPalette.get(0))+";"+"_SECONDARY:"+colorToHex(currentPalette.get(1))+";"+"_TEXT:"+colorToHex(currentPalette.get(2))+";"+"_DETAILS:"+colorToHex(currentPalette.get(3))+";";
		appRoot.setStyle(style);
		hidePalette();
	}

	@FXML
	private void setLightTheme() {
//		currentPalette[0]="#f2f2f2";
//		currentPalette[1]="#DBDBDB";
//		currentPalette[2]="#343a40";
//		currentPalette[3]="rgba(52,58,64,0.55)";
//		String style="_PRIMARY:"+currentPalette[0]+";"+"_SECONDARY:"+currentPalette[1]+";"+"_TEXT:"+currentPalette[2]+";"+"_DETAILS:"+currentPalette[3]+";";
//		appRoot.setStyle(style);
	}

	@FXML
	private void setDarkTheme() {
//		currentPalette[0]="#15151E";
//		currentPalette[1]="#080810";
//		currentPalette[2]="#aeaeae";
//		currentPalette[3]="rgba(109,109,109,0.6)";
//		String style="_PRIMARY:"+currentPalette[0]+";"+"_SECONDARY:"+currentPalette[1]+";"+"_TEXT:"+currentPalette[2]+";"+"_DETAILS:"+currentPalette[3]+";";
//		appRoot.setStyle(style);
	}

	@FXML
	private void setDefaultTheme() {
//		currentPalette[0]="#2A363B";
//		currentPalette[1]="#242C32";
//		currentPalette[2]="#CDCDCD";
//		currentPalette[3]="#878787";
//		String style="_PRIMARY:"+currentPalette[0]+";"+"_SECONDARY:"+currentPalette[1]+";"+"_TEXT:"+currentPalette[2]+";"+"_DETAILS:"+currentPalette[3]+";";
//		appRoot.setStyle(style);
	}

	@FXML
	private void setCustomTheme() {
//		String[] colors=new String[4];
//		for(int i=0;i<4;i++)
//		{
//			colors[i]=((ColorPicker)paletteGrid.getChildren().get(i+4)).getValue().toString().substring(2,10).toUpperCase();
//		}
//		double r, g, b, a;
//		for(String color : colors)
//		{
//			r=Integer.parseInt(color.substring(0,2),16);
//			g=Integer.parseInt(color.substring(2,4),16);
//			b=Integer.parseInt(color.substring(4,6),16);
//			a=Integer.parseInt(color.substring(6,8),16);
//			System.out.println(r+","+g+","+b+","+a);
//		}
//		String style="_PRIMARY:"+colors[0]+";"+"_SECONDARY:"+colors[1]+";"+"_TEXT:"+colors[2]+";"+"_DETAILS:"+colors[3]+";";
//		appRoot.setStyle(style);
	}

	@FXML
	private void closeAndResetTheme() {
//		String style="_PRIMARY:"+currentPalette[0]+";"+"_SECONDARY:"+currentPalette[1]+";"+"_TEXT:"+currentPalette[2]+";"+"_DETAILS:"+currentPalette[3]+";";
//		appRoot.setStyle(style);
//		hidePalette();
	}

	private void updatePalette() {
//		int[] rgbaColors=new int[4];
//		for(int i=0;i<4;i++)
//		{
//			System.out.println(currentPalette[i]);
//			//			rgbaColors[0]=Integer.valueOf(currentPalette[i].substring(1,3),16);
//			//			rgbaColors[1]=Integer.valueOf(currentPalette[i].substring(3,5),16);
//			//			rgbaColors[2]=Integer.valueOf(currentPalette[i].substring(5,7),16);
//			//			rgbaColors[3]=Integer.valueOf(currentPalette[i].substring(7,9),16);
//			//			System.out.println(rgbaColors[0]+rgbaColors[1]+rgbaColors[2]);
//			//			((ColorPicker)paletteGrid.getChildren().get(i+4)).setValue(new Color(rgbaColors[0],rgbaColors[1],rgbaColors[2],rgbaColors[3]));
//			System.out.println("----------------------");
//		}
	}

	private String colorToHex(HashMap<Character,Integer> rgba) {
		StringBuilder colorString=new StringBuilder("#");
		colorString.append(rgba.get('r')).append(rgba.get('g')).append(rgba.get('b')).append(rgba.get('a'));
		System.out.println(colorString);
		return colorString.toString();
	}

	private HashMap<Character,Integer> colorToRGB(String string) {
		String hex=string.substring(1);
		HashMap<Character,Integer> rgba=new HashMap<>();
		rgba.put('r',Integer.parseInt(hex.substring(0,2),16));
		rgba.put('g',Integer.parseInt(hex.substring(2,4),16));
		rgba.put('b',Integer.parseInt(hex.substring(4,6),16));
		rgba.put('a',Integer.parseInt(hex.substring(6,8),16));
		return rgba;
	}

	private boolean isModified() {
		return currentFiles.get(currentTextArea);
	}

	private void setModified(boolean b) {
		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		if(b) //set modified
		{
			tabPane.setStyle(circleSVGPathProperty);
			tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-font-style:italic;");
			Main.setMainStageTitle(tab.getText()+"   (modified)");
			currentFiles.put(currentTextArea,true);
		} else //set original
		{
			tabPane.setStyle(defaultSVGPathProperty);
			tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-font-style:normal;");
			Main.setMainStageTitle(tab.getText());
			currentFiles.put(currentTextArea,false);
		}
	}

	private void setLabelModified(boolean b) {
		String tabName=tabPane.getSelectionModel().getSelectedItem().getText();
		if(b) //set modified
		{
			for(Label l : openFilesList.getChildren().toArray(new Label[0]))
			{
				if(l.getText().equals(tabName) && l.getText().charAt(l.getText().length()-1)!='⚫')
				{
					l.setText(tabName+"⚫");
					return;
				}
			}
		} else
		{
			for(Label l : openFilesList.getChildren().toArray(new Label[0]))
			{
				if(l.getText().equals(tabName+"⚫"))
				{
					l.setText(tabName);
					return;
				}
			}
		}
	}

	@FXML
	private void exitApplication() {
		System.exit(0);
	}
	//
}

