package com.didanko228.megaUpscale.ui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class LoggerBridge {
    private final TextArea textArea;

    public LoggerBridge(TextArea textArea) {
        this.textArea = textArea;
    }

    public void log(String message) {
        Platform.runLater(() -> {
            textArea.appendText(message + "\n");
        });
    }
}
