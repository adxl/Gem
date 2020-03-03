package sample.controllers;

import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.bind.v2.TODO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import sample.Main;

import java.io.*;
import java.util.*;

public class Controller {
	@FXML
	private AnchorPane appRoot;
	@FXML
	private AnchorPane paletteContainer;
	@FXML
	private GridPane paletteGrid;
	private ColorPicker[] colorPickers=new ColorPicker[4];

	@FXML
	private AnchorPane promptAnchorPane;
	@FXML
	private TabPane tabPane;
	@FXML
	public SplitPane splitPane;

	private int untitledIdCounter;

	//private TextArea currentTextArea; //Deprecated
	private CodeArea currentCodeArea;

	//	private TextArea currentLinesCounter; //Deprecated

	//old textArea related fields : Deprecated
	/*private ScrollBar currentTextScrollBar;
	private ScrollBar currentLinesCounterScrollBar;*/

	//	private boolean isListened=false;
	//	private boolean isReady=false;

	@FXML
	private Slider fontSizeSlider;
	@FXML
	private Label fontSizeSliderIcon;

	@FXML
	private VBox openFilesList;
	private HashMap<String,File> openTabsFiles=new HashMap<>();

	//	private HashMap<TextArea,Boolean> currentFiles=new HashMap<>();
	private HashMap<CodeArea,Boolean> currentFilesModifiedState=new HashMap<>();


	private HashMap<String,HashMap<Character,Integer>> currentPalette=new HashMap<>();

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

		for(int i=0;i<4;i++)
		{
			colorPickers[i]=(ColorPicker)(paletteGrid.getChildren().get(i+4));
			colorPickers[i].valueProperty().addListener(event->setCustomTheme());
		}

		//TODO : disabled font size modifications for a moment
		fontSizeSlider.setVisible(false);
		fontSizeSliderIcon.setVisible(false);
		/*fontSizeSlider.visibleProperty().bind(isTabPaneEmpty.not());
		fontSizeSliderIcon.visibleProperty().bind(isTabPaneEmpty.not());*/
		SplitPane.setResizableWithParent(splitPane,false);
		SplitPane.setResizableWithParent(splitPane.getItems().get(0),false);
		//		quickInit();
		setDefaultTheme();
	}

	//quick init for test
	private void quickInit() throws IOException {
		//		openExistingFile("Gem.iml");
		//		openExistingFile("README.md");
		//		openExistingFile("todo.md");
		//		createFile();
		//		tabPane.getSelectionModel().select(0);
	}

	private void tabSwitchListener(Tab tab) {
		tab.setOnSelectionChanged(event->
		{
			if(tab.isSelected())
			{
				currentCodeAreaListener();
				System.out.println(tab.getText()/*+"--"+currentCodeArea.getText()*/);

				int index=tabPane.getTabs().indexOf(tab);
				if(tabPane.getTabs().size()==1 && openFilesList.getChildren().size()==2)
					openFilesList.getChildren().get(1).setStyle("-fx-text-fill:_TEXT");
				else
					for(Node l : openFilesList.getChildren()) // Label (l) upcasting
						l.setStyle("-fx-text-fill:_DETAILS");

				(openFilesList.getChildren().get(index)).setStyle("-fx-text-fill:_TEXT"); //#39ea49 green

				fileType.setText(getType(tab.getText()));

				if(openTabsFiles.get(tab.getText())!=null)
					filePath.setText(String.valueOf(openTabsFiles.get(tab.getText())));
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
		//		currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		//		currentLinesCounter.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		//		currentTextArea.requestFocus();
	}

	private void currentCodeAreaListener() {
		//TODO: isListened=false;
		//TODO: isReady=false;

		//currentTextArea=(TextArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(1);
		currentCodeArea=(CodeArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(0);

		if(!currentFilesModifiedState.containsKey(currentCodeArea))
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

		//currentLinesCounter=(TextArea)((AnchorPane)tabPane.getSelectionModel().getSelectedItem().getContent()).getChildren().get(0);

		//currentTextArea.textProperty().removeListener(textChangeListener);
		//currentTextArea.textProperty().addListener(textChangeListener);
		//currentTextArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));

		currentCodeArea.textProperty().removeListener(textChangeListener);
		currentCodeArea.textProperty().addListener(textChangeListener);
		//TODO set text fontsize

		//currentLinesCounter.setFont(Font.font("Arial",fontSizeSlider.getValue()));
		//countLines();

		//TODO Platform.runLater(currentTextArea::requestFocus);
	}

	private void textAreaChanged(ObservableValue<? extends String> observableValue,String p,String c) {
		//		currentTextArea.setOnScroll(null);
		//		int textLines=c.split("\r\n|\r|\n",-1).length;
		//		int counterLines=currentLinesCounter.getText().split("\n").length;
		//		if(counterLines<textLines) //add row(s)
		//		{
		//			int linesDiff=textLines-counterLines;
		//			for(int i=0;i<linesDiff;i++)
		//				currentLinesCounter.appendText("\n"+(++counterLines));
		//		} else if(counterLines>textLines) //remove row(s)
		//		{
		//			char[] chars=currentLinesCounter.getText().toCharArray();
		//			int linesDiff=counterLines-textLines;
		//			int diffCounter=0;
		//			int deleteStartIndex=-1;
		//			for(int i=currentLinesCounter.getLength()-1;i >= 0;i--)
		//			{
		//				deleteStartIndex=i;
		//				if(chars[i]=='\n')
		//					diffCounter++;
		//				if(diffCounter==linesDiff)
		//					break;
		//			}
		//			currentLinesCounter.deleteText(deleteStartIndex,currentLinesCounter.getLength());
		//		}
		//		currentTextScrollBar=(ScrollBar)currentTextArea.lookup(".scroll-bar:vertical");
		//		currentLinesCounterScrollBar=(ScrollBar)currentLinesCounter.lookup(".scroll-bar:vertical");
		//		if(!isListened)
		//		{
		//			currentLinesCounterScrollBar.valueProperty().bindBidirectional(currentTextScrollBar.valueProperty());
		//			isListened=true;
		//		}
		setModified(true);
		setLabelModified(true);
	}

	private Tab generateEditorTab(String title,@Nullable String text) throws IOException {
		AnchorPane root=FXMLLoader.load(getClass().getResource("/sample/views/editor_tab.fxml"));
		CodeArea codeArea;

		if(text==null)
			codeArea=new CodeArea();
		else
			codeArea=new CodeArea(text);

		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

		AnchorPane.setTopAnchor(codeArea,0.0);
		AnchorPane.setRightAnchor(codeArea,0.0);
		AnchorPane.setBottomAnchor(codeArea,0.0);
		AnchorPane.setLeftAnchor(codeArea,0.0);

		root.getChildren().add(codeArea);

		return new Tab(title,root);
	}

	@FXML
	private void createFile() throws IOException {
		Tab tab=generateEditorTab("Untitled"+untitledIdCounter++,null);

		addFileToOpenFilesList(tab);

		openTabsFiles.put(tab.getText(),null);

		tabSwitchListener(tab);
		tab.setOnCloseRequest(event->closeFile(tab.getText()));

		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);

		currentCodeArea=(CodeArea)((AnchorPane)(tab.getContent())).getChildren().get(0);
		currentCodeArea.requestFocus();
	}

	private void createPrefFile(String title,String text) throws IOException {
		//		Tab tab=new Tab(title,FXMLLoader.load(getClass().getResource("/sample/views/tab.fxml")));
		Tab tab=generateEditorTab(title,text);

		addFileToOpenFilesList(tab);

		tabSwitchListener(tab);
		tab.setOnCloseRequest(event->closeFile(tab.getText()));

		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);

		currentCodeArea=(CodeArea)((AnchorPane)(tab.getContent())).getChildren().get(0);
		currentCodeArea.requestFocus();

		//((AnchorPane)tab.getContent()).getChildren().get(1).requestFocus();

		//TODO might be unnecessary
		/*currentTextArea.setOnMouseEntered(event->
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
		});*/
	}

	@FXML
	private void openFile() throws IOException {
		Stage stage=new Stage();
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Open File");
		File selectedFile=fileChooser.showOpenDialog(stage);
		if(selectedFile!=null)
		{
			if(!openTabsFiles.containsKey(selectedFile.getName()))
			{
				FileReader fileReader=new FileReader(selectedFile.getAbsolutePath());
				//TODO possible duplicated code : openExistingFile()
				BufferedReader bufferedReader=new BufferedReader(fileReader);
				StringBuilder stringBuilder=new StringBuilder();
				String text;
				while((text=bufferedReader.readLine())!=null)
				{
					stringBuilder.append(text).append("\n");
				}
				System.out.println("<"+stringBuilder.charAt(stringBuilder.length()-1)+">");
				stringBuilder.deleteCharAt(stringBuilder.length()-1);
				openTabsFiles.put(selectedFile.getName(),selectedFile);
				createPrefFile(selectedFile.getName(),stringBuilder.toString());
			} else
			{
				for(Tab t : tabPane.getTabs())
				{
					if(t.getText().equals(selectedFile.getName()))
					{
						tabPane.getSelectionModel().select(t);
						return;
					}
				}
			}
		}
	}

	private void openExistingFile(String path) throws IOException {
		File file=new File(path);
		FileReader fileReader=new FileReader(path);

		//TODO possible duplicated code : openFile()
		BufferedReader bufferedReader=new BufferedReader(fileReader);
		StringBuilder stringBuilder=new StringBuilder();
		String text;
		while((text=bufferedReader.readLine())!=null)
		{
			stringBuilder.append(text).append("\n");
		}
		openTabsFiles.put(file.getName(),file);
		createPrefFile(file.getName(),stringBuilder.toString());
	}

	private void closeFile(String title) {
		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		boolean wasModified=isModified();

		currentFilesModifiedState.remove(currentCodeArea);
		tabPane.getTabs().remove(tab);

		if(wasModified)
			removeFileFromOpenFilesList(title+"⚫");
		else
			removeFileFromOpenFilesList(title);

		openTabsFiles.remove(title);
	}

	@FXML
	private void closeFileRequest() {
		Tab selectedTab=tabPane.getSelectionModel().getSelectedItem();
		String title=selectedTab.getText();
		closeFile(title);
	}

	private void addFileToOpenFilesList(Tab tab) {
		Label label=new Label(tab.getText());
		label.setOnMouseClicked(event->tabPane.getSelectionModel().select(tab));
		openFilesList.getChildren().add(label);
	}

	private void removeFileFromOpenFilesList(String title) {
		for(Label l : openFilesList.getChildren().toArray(new Label[0]))
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
		if(openTabsFiles.get(fileName)==null) //saving an untitled file
		{
			saveFileAs();
		} else //saving an existing file
		{
			PrintWriter writer=new PrintWriter(openTabsFiles.get(fileName));
			//writer.println(currentTextArea.getText());
			writer.println(currentCodeArea.getText());
			writer.close();

			setModified(false);
			setLabelModified(false);
		}
	}

	@FXML
	private void saveFileAs() throws IOException {
		//		if(!currentTextArea.getText().isEmpty())
		if(!currentCodeArea.getText().isEmpty()) //don't save an empty file
		{
			Stage stage=new Stage();
			FileChooser fileChooser=new FileChooser();
			File file=fileChooser.showSaveDialog(stage);
			if(file!=null)
			{
				PrintWriter writer=new PrintWriter(file);
				//				writer.println(currentTextArea.getText());
				writer.println(currentCodeArea.getText());
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
			return name.substring(name.lastIndexOf(".")+1).toUpperCase();
		return "";
	}

	private void countLines() {
		//		int textLines=currentTextArea.getText().split("\r\n|\r|\n",-1).length;
		//		int counterLines=currentLinesCounter.getText().split("\n").length;
		//		if(textLines!=counterLines)
		//		{
		//			StringBuilder lines=new StringBuilder();
		//			lines.append(1);
		//			for(int i=2;i<=textLines;i++)
		//			{
		//				lines.append("\n").append(i);
		//			}
		//			currentLinesCounter.setText(lines.toString());
		//		}
	}

	// independent UI related methods :

	@FXML
	private void showPalette() {
		paletteContainer.setVisible(true);
	}

	private void hidePalette() {
		paletteContainer.setVisible(false);
	}

	@FXML
	private void setLightTheme() {
		clearCurrentPalette();

		currentPalette.put("primary",colorToRGBA("F2F2F2FF"));
		currentPalette.put("secondary",colorToRGBA("DBDBDBFF"));
		currentPalette.put("text",colorToRGBA("343A40FF"));
		currentPalette.put("details",colorToRGBA("343A408C"));

		String style="_PRIMARY:"+colorToHex(currentPalette.get("primary"))+";"+"_SECONDARY:"+colorToHex(currentPalette.get("secondary"))+";"+"_TEXT:"+colorToHex(currentPalette.get("text"))+";"+"_DETAILS:"+colorToHex(currentPalette.get("details"))+";";
		appRoot.setStyle(style);

		updatePalette();
	}

	@FXML
	private void setDarkTheme() {
		clearCurrentPalette();

		currentPalette.put("primary",colorToRGBA("15151EFF"));
		currentPalette.put("secondary",colorToRGBA("080810FF"));
		currentPalette.put("text",colorToRGBA("AEAEAEFF"));
		currentPalette.put("details",colorToRGBA("6D6D6D99"));

		String style="_PRIMARY:"+colorToHex(currentPalette.get("primary"))+";"+"_SECONDARY:"+colorToHex(currentPalette.get("secondary"))+";"+"_TEXT:"+colorToHex(currentPalette.get("text"))+";"+"_DETAILS:"+colorToHex(currentPalette.get("details"))+";";
		appRoot.setStyle(style);

		updatePalette();
	}

	@FXML
	private void setDefaultTheme() {
		clearCurrentPalette();

		currentPalette.put("primary",colorToRGBA("2A363BFF"));
		currentPalette.put("secondary",colorToRGBA("242C32FF"));
		currentPalette.put("text",colorToRGBA("CDCDCDFF"));
		currentPalette.put("details",colorToRGBA("878787FF"));

		String style="_PRIMARY:"+colorToHex(currentPalette.get("primary"))+";"+"_SECONDARY:"+colorToHex(currentPalette.get("secondary"))+";"+"_TEXT:"+colorToHex(currentPalette.get("text"))+";"+"_DETAILS:"+colorToHex(currentPalette.get("details"))+";";
		appRoot.setStyle(style);

		updatePalette();
	}

	@FXML
	private void confirmTheme() {
		clearCurrentPalette();
		String[] colors=new String[4];
		for(int i=0;i<4;i++)
		{
			colors[i]=((ColorPicker)paletteGrid.getChildren().get(i+4)).getValue().toString().substring(2,10).toUpperCase();
		}
		currentPalette.put("primary",colorToRGBA(colors[0]));
		currentPalette.put("secondary",colorToRGBA(colors[1]));
		currentPalette.put("text",colorToRGBA(colors[2]));
		currentPalette.put("details",colorToRGBA(colors[3]));

		String style="_PRIMARY:"+colorToHex(currentPalette.get("primary"))+";"+"_SECONDARY:"+colorToHex(currentPalette.get("secondary"))+";"+"_TEXT:"+colorToHex(currentPalette.get("text"))+";"+"_DETAILS:"+colorToHex(currentPalette.get("details"))+";";
		appRoot.setStyle(style);

		hidePalette();
	}

	@FXML
	private void setCustomTheme() {
		String[] colors=new String[4];
		for(int i=0;i<4;i++)
		{
			colors[i]="#"+((ColorPicker)paletteGrid.getChildren().get(i+4)).getValue().toString().substring(2,10).toUpperCase();
		}
		String style="_PRIMARY:"+colors[0]+";"+"_SECONDARY:"+colors[1]+";"+"_TEXT:"+colors[2]+";"+"_DETAILS:"+colors[3]+";";
		appRoot.setStyle(style);
	}

	@FXML
	private void closeAndResetTheme() {
		String style="_PRIMARY:"+colorToHex(currentPalette.get("primary"))+";"+"_SECONDARY:"+colorToHex(currentPalette.get("secondary"))+";"+"_TEXT:"+colorToHex(currentPalette.get("text"))+";"+"_DETAILS:"+colorToHex(currentPalette.get("details"))+";";
		appRoot.setStyle(style);
		hidePalette();
	}

	private void updatePalette() {
		String primaryColor=colorToHex(currentPalette.get("primary")).substring(0,7);
		float primaryAlpha=(float)(currentPalette.get("primary")).get('a')/255;

		String secondaryColor=colorToHex(currentPalette.get("secondary")).substring(0,7);
		float secondaryAlpha=(float)(currentPalette.get("secondary")).get('a')/255;

		String textColor=colorToHex(currentPalette.get("text")).substring(0,7);
		float textAlpha=(float)(currentPalette.get("text")).get('a')/255;

		String detailsColor=colorToHex(currentPalette.get("details")).substring(0,7);
		float detailsAlpha=(float)(currentPalette.get("details")).get('a')/255;

		((ColorPicker)paletteGrid.getChildren().get(4)).setValue(Color.web(primaryColor,primaryAlpha));
		((ColorPicker)paletteGrid.getChildren().get(5)).setValue(Color.web(secondaryColor,secondaryAlpha));
		((ColorPicker)paletteGrid.getChildren().get(6)).setValue(Color.web(textColor,textAlpha));
		((ColorPicker)paletteGrid.getChildren().get(7)).setValue(Color.web(detailsColor,detailsAlpha));
	}

	private String colorToHex(HashMap<Character,Integer> rgba) {
		return String.format("#%02X%02X%02X%02X",rgba.get('r'),rgba.get('g'),rgba.get('b'),rgba.get('a'));
	}

	//param hex -> hexadecimal representation of rgba without "#"
	private HashMap<Character,Integer> colorToRGBA(String hex) {
		HashMap<Character,Integer> rgba=new HashMap<>();
		rgba.put('r',Integer.parseInt(hex.substring(0,2),16));
		rgba.put('g',Integer.parseInt(hex.substring(2,4),16));
		rgba.put('b',Integer.parseInt(hex.substring(4,6),16));
		rgba.put('a',Integer.parseInt(hex.substring(6,8),16));
		return rgba;
	}

	public void clearCurrentPalette() {
		currentPalette.clear();
	}

	//end of UI methods


	private boolean isModified() {
		return currentFilesModifiedState.get(currentCodeArea);
	}

	private void setModified(boolean b) {
		Tab tab=tabPane.getSelectionModel().getSelectedItem();
		if(b) //set modified
		{
			tabPane.setStyle(circleSVGPathProperty);
			tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-font-style:italic;");
			Main.setMainStageTitle(tab.getText()+"   (modified)");
			//			currentFilesModifiedState.put(currentTextArea,true);
			currentFilesModifiedState.put(currentCodeArea,true);
		} else //set original (unmodified)
		{
			tabPane.setStyle(defaultSVGPathProperty);
			tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-font-style:normal;");
			Main.setMainStageTitle(tab.getText());
			//			currentFilesModifiedState.put(currentTextArea,false);
			currentFilesModifiedState.put(currentCodeArea,false);
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

