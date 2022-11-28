package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.Model;
import javafx.fxml.FXML;

import java.io.IOException;

public class RenameStage extends BaseStage {
    public RenameStage(Model model) throws IOException {
        super(model,"Rename");
        setFxml("/ui/Rename.fxml");
    }
}