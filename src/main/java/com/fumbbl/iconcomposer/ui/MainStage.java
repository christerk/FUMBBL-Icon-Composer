package com.fumbbl.iconcomposer.ui;

import java.io.File;
import java.io.IOException;

import com.fumbbl.iconcomposer.model.Model;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.WindowEvent;

public class MainStage extends BaseStage {
	public MainStage(Model model) throws IOException {
		super(model,"FUMBBL Icon Composer");
		setFxml("/ui/Main.fxml");

		scene.setOnDragOver(event -> {
			Dragboard board = event.getDragboard();
			if (board.hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY);
			} else {
				event.consume();
			}
		});
		scene.setOnDragDropped(event -> {
			Dragboard board = event.getDragboard();
			boolean success = false;
			if (board.hasFiles()) {
				success = true;
				String path = null;
				for (File f : board.getFiles()) {
					path = f.getAbsolutePath();

					mainController.handleDroppedFile(path);
				}
			}
			event.setDropCompleted(success);
			event.consume();
		});
		
		stage.setOnCloseRequest(e -> mainController.shutdown());
	}
}
