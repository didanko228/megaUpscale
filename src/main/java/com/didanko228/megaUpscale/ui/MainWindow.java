package com.didanko228.megaUpscale.ui;

import com.didanko228.megaUpscale.Main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainWindow extends Application {

    @Override
    public void start(Stage stage) { // TODO use translations
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(10));

        // Input
        TextField inputField = new TextField();
        Button inputBtn = new Button("Browse...");
        HBox inputBox = new HBox(5, new Label("Input:"), inputField, inputBtn);

        inputBtn.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if (file != null) inputField.setText(file.getAbsolutePath());
        });

        // Output
        TextField outputField = new TextField();
        Button outputBtn = new Button("Save as...");
        HBox outputBox = new HBox(5, new Label("Output:"), outputField, outputBtn);

        outputBtn.setOnAction(e -> {
            File file = new FileChooser().showSaveDialog(stage);
            if (file != null) outputField.setText(file.getAbsolutePath());
        });

        // Model and Scale
        ComboBox<String> modelBox = new ComboBox<>();
        modelBox.getItems().addAll("4xLSDIR"); // TODO choose model
        modelBox.getSelectionModel().selectFirst();

        ComboBox<Integer> scaleBox = new ComboBox<>();
        scaleBox.getItems().addAll(2, 3, 4);
        scaleBox.getSelectionModel().selectFirst();

        HBox optionsBox = new HBox(10, new Label("Model:"), modelBox, new Label("Scale:"), scaleBox);

        // Start
        Button startBtn = new Button("Upscale");

        // Log
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(300);

        root.getChildren().addAll(inputBox, outputBox, optionsBox, startBtn, logArea);

        Scene scene = new Scene(root, 600, 450);
        stage.setTitle(Main.PROJECT_NAME);
        stage.setScene(scene);
        stage.show();
    }

    public static void launchUI(String[] args) {
        launch(args);
    }
}