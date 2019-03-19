package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class Controller_Vue
{
	File currentFile;

	@FXML
	Label motd;

	@FXML
	TextArea textArea;

	@FXML
	public void createFile()
	{
		closeFile();
		textArea.setVisible(true);
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
			currentFile=selectedFile;

			Main.setMainStageTitle(currentFile.toString());

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
			Main.setMainStageTitle(currentFile.toString());
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
			}

			Main.setMainStageTitle(currentFile.toString());
		}


	}
}
