package com.fumbbl.iconcomposer.ui;

import javafx.fxml.FXML;

import java.io.IOException;

public class RenameStage extends BaseStage {
    public RenameStage() throws IOException {
        super("Rename");
        setFxml("/ui/Rename.fxml");
    }

    @FXML
    private void receiveData(Object o) {

    }
}