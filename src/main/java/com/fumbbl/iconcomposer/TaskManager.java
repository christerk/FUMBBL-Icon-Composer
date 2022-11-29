package com.fumbbl.iconcomposer;

import com.fumbbl.iconcomposer.controllers.Controller;
import com.fumbbl.iconcomposer.controllers.TaskQueue;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TaskManager {
    private final TaskQueue taskQueue;

    public final SimpleStringProperty taskStateProperty;
    public final SimpleBooleanProperty taskRunningProperty;
    public final SimpleDoubleProperty taskPctProperty;

    public TaskManager() {
        taskQueue = new TaskQueue(this);
        taskStateProperty =  new SimpleStringProperty();
        taskRunningProperty = new SimpleBooleanProperty();
        taskPctProperty = new SimpleDoubleProperty();
    }

    public void runInBackground(Runnable task) {
        taskQueue.execute(task);
    }
    public void startProgress() {
        taskQueue.startProgress();
    }
    public void stopProgress() {
        taskQueue.stopProgress();
    }

    public void startBatch() {
        taskQueue.startBatch();
    }

    public void runBatch() {
        taskQueue.runBatch(() -> {
            Platform.runLater(() -> taskRunningProperty.set(false));
        });
    }

    public void shutdown() {
        taskQueue.shutdownNow();
        Platform.exit();
    }

    public void onProgressStart(String description) {
        Platform.runLater(() -> taskStateProperty.set(description));
    }

    public void onProgress(double pct, boolean b) {
        Platform.runLater(() -> {
            taskPctProperty.set(pct);
            taskRunningProperty.set(!b);
        });
    }
}
