package com.fumbbl.iconcomposer.controllers;

import com.fumbbl.iconcomposer.Main;
import com.fumbbl.iconcomposer.model.types.NamedImage;
import com.fumbbl.iconcomposer.model.types.NamedItem;
import com.fumbbl.iconcomposer.ui.StageType;
import javafx.scene.control.TextField;

public class RenameController extends BaseController {

    public TextField nameField;
    private NamedItem renameObject;

    @Override
    public void onShow() {
        NamedItem item = (NamedItem)stage.getUserData();

        renameObject = item;
        nameField.setText(item.getName());
    }

    public void rename() {
        controller.getMainController().renameItem(renameObject, nameField.getText());
        controller.getStageManager().hide(StageType.rename);
    }
}
