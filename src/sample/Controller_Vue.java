package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class Controller_Vue
{
	@FXML
	Label motd;

	@FXML
	TextArea textArea;

	@FXML
	public void createFile()
	{
		textArea.clear();
		textArea.setVisible(true);
	}

	@FXML
	public void openFile() throws IOException
	{
		Stage stage=new Stage();
		FileChooser fileChooser=new FileChooser();
		fileChooser.setTitle("Open File");
		File selectedFile=fileChooser.showOpenDialog(stage);

		if (selectedFile != null)
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
		}
	}

	@FXML
	public void closeFile()
	{
		textArea.clear();
		textArea.setVisible(false);

	}

	@FXML
	public void saveFileAs() throws IOException
	{
		if (!textArea.getText().isEmpty())
		{
			Stage stage = new Stage();
			FileChooser fileChooser = new FileChooser();
			File file = fileChooser.showSaveDialog(stage);
			if (file != null)
			{
				PrintWriter writer;
				writer = new PrintWriter(file);
				writer.println(textArea.getText());
				writer.close();
			}
		}
	}
}
