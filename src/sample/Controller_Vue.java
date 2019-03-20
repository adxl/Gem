package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;

public class Controller_Vue
{
	File currentFile;

	@FXML
	private Label motd;

	@FXML
	private TextArea textArea;

	@FXML
	private Slider fontSizeSlider;

	@FXML
	public void initialize()
	{
		fontSizeSlider.setMin(14);
		fontSizeSlider.setMax(72);
		fontSizeSlider.setValue(14);

		fontSizeSlider.valueProperty().addListener(new ChangeListener<Number>()
		{
			public void changed(ObservableValue<? extends Number> observableValue, Number previousValue, Number currentValue)
			{
				textArea.setFont(Font.font("Arial",fontSizeSlider.getValue()));
				textArea.setText(textArea.getText());
				textArea.requestFocus();
			}
		});
	}

	@FXML
	public void createFile()
	{
		closeFile();

		textArea.setVisible(true);
		textArea.requestFocus();

		Main.setMainStageTitle("Unsaved document");
	}

	@FXML
	public void openFile() throws IOException
	{
		Stage stage=new Stage();
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Open File");
		File selectedFile=fileChooser.showOpenDialog(stage);

		if (selectedFile!=null)
		{
			FileReader fileReader=new FileReader(selectedFile.getAbsolutePath().toString());
			BufferedReader bufferedReader=new BufferedReader(fileReader);

			StringBuilder stringBuilder=new StringBuilder();

			String text="";

			while ((text=bufferedReader.readLine())!=null)
			{
				stringBuilder.append(text+"\n");
			}

			textArea.setVisible(true);
			textArea.setText(stringBuilder.toString());
			textArea.requestFocus();
			currentFile=selectedFile;

			Main.setMainStageTitle(currentFile.getName());
		}
	}

	@FXML
	public void closeFile()
	{
		currentFile=null;
		Main.setMainStageTitle("");
		textArea.clear();
		textArea.setVisible(false);
	}

	@FXML
	public void saveFile() throws IOException
	{
		if (currentFile!=null)
		{
			PrintWriter writer;
			writer=new PrintWriter(currentFile);
			writer.println(textArea.getText());
			writer.close();

			Main.setMainStageTitle(currentFile.getName());
		} else
		{
			saveFileAs();
		}
	}

	@FXML
	public void saveFileAs() throws IOException
	{
		if (!textArea.getText().isEmpty())
		{
			Stage stage=new Stage();
			FileChooser fileChooser=new FileChooser();
			File file=fileChooser.showSaveDialog(stage);
			if (file!=null)
			{
				PrintWriter writer;
				writer=new PrintWriter(file);
				writer.println(textArea.getText());
				writer.close();
				currentFile=file;
				Main.setMainStageTitle(currentFile.getName());
			}
		}
	}

	public void exitApplication()
	{
		System.exit(0);
	}

//
}
