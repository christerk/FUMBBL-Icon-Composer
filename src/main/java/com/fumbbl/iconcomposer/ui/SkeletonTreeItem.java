package com.fumbbl.iconcomposer.ui;

import com.fumbbl.iconcomposer.model.types.NamedItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;

public class SkeletonTreeItem extends TreeItem<NamedItem> {
    private Class itemType;

    public SkeletonTreeItem(NamedItem value) {
        super(value);

        addChildrenListener(value);
    }

    public Class itemType() {
        return itemType;
    }

    private void addChildrenListener(NamedItem value) {
        if (value == null) {
            return;
        }
        ObservableMap<String, ? extends NamedItem> children = value.getChildren();

        children.values().forEach(child -> {
            SkeletonTreeItem.this.getChildren().add(
                    new SkeletonTreeItem(child)
            );
        });

        children.addListener((MapChangeListener<? super String, ? super NamedItem> )change -> {
            if (change.wasAdded()) {
                SkeletonTreeItem.this.getChildren().add(new SkeletonTreeItem(change.getValueAdded()));
            }

            if (change.wasRemoved()) {
               SkeletonTreeItem.this.getChildren().remove(new SkeletonTreeItem(change.getValueRemoved()));
            }
        });
    }
}
