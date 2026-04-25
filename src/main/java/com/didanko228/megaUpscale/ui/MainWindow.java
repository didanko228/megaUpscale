package com.didanko228.megaUpscale.ui;

import com.didanko228.megaUpscale.Main;
import com.didanko228.megaUpscale.utils.UpscaleService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainWindow extends Application {
    private static Path backendPath;
    private static Path modelsPath;

    public static void initRuntime(Path backend, Path models) {
        backendPath = backend;
        modelsPath = models;
    }

    @Override
    public void start(Stage stage) { // TODO use translations
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(10));

        // Input
        TextField inputField = new TextField();
        Button inputBtn = new Button("Browse...");
        HBox inputBox = new HBox(5, new Label("Input:"), inputField, inputBtn);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        inputBtn.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if (file != null) inputField.setText(file.getAbsolutePath());
        });

        // Output
        TextField outputField = new TextField();
        Button outputBtn = new Button("Save as...");
        HBox outputBox = new HBox(5, new Label("Output:"), outputField, outputBtn);
        HBox.setHgrow(outputField, Priority.ALWAYS);

        outputBtn.setOnAction(e -> {
            File file = new FileChooser().showSaveDialog(stage);
            if (file != null) outputField.setText(file.getAbsolutePath());
        });

        // InputField Listener
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            Path path = Paths.get(newVal);

            if (Files.exists(path)) {
                Path outPath = addSuffix(path, "_upscaled");
                outputField.setText(outPath.toString());
            } else {
                outputField.setText("");
            }
        });

        // Model and Scale
        ComboBox<String> modelBox = new ComboBox<>();
        modelBox.getItems().addAll("4xLSDIR"); // TODO choose model
        modelBox.getSelectionModel().selectFirst();

        ComboBox<Integer> scaleBox = new ComboBox<>();
        scaleBox.getItems().addAll(2, 3, 4);
        scaleBox.getSelectionModel().selectFirst();

        HBox optionsBox = new HBox(10, new Label("Model:"), modelBox, new Label("Scale:"), scaleBox);

        // Log
        TextArea logArea = new TextArea();
        LoggerBridge loggerBridge = new LoggerBridge(logArea);
        logArea.setEditable(false);
        logArea.setPrefHeight(300);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        // Start
        Button startBtn = new Button("Upscale");
        startBtn.setOnAction(e -> {
            String inputPath = inputField.getText();
            String outputPath = outputField.getText();
            String model = modelBox.getValue();
            int scale = scaleBox.getValue();

            if (inputPath.isEmpty() || outputPath.isEmpty()) {
                loggerBridge.log("Input or output path is empty!");
                return;
            }

            // create Task
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        // create UpscaleService
                        UpscaleService service = new UpscaleService(backendPath, modelsPath);

                        // System.out -> loggerBridge
                        java.io.PrintStream originalOut = System.out;
                        System.setOut(new PrintStream(new OutputStream() {
                            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                            @Override
                            public void write(int b) {
                                if (b == '\n') {
                                    String line = buffer.toString(StandardCharsets.UTF_8);
                                    loggerBridge.log(line);
                                    buffer.reset();
                                } else {
                                    buffer.write(b);
                                }
                            }
                        }, true, StandardCharsets.UTF_8));

                        // upscale
                        service.upscale(Paths.get(inputPath),
                                Paths.get(outputPath),
                                model,
                                scale
                        );

                        System.setOut(originalOut);

                    } catch (Exception ex) {
                        loggerBridge.log("Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    return null;
                }
            };

            // Start Task in new thread
            new Thread(task).start();
        });

        // clearLog
        Button clearLogBtn = new Button("Clear Log");
        clearLogBtn.setOnAction(e -> logArea.clear());

        HBox buttonHBox = new HBox(5, startBtn, clearLogBtn);

        root.getChildren().addAll(inputBox, outputBox, optionsBox, buttonHBox, logArea);

        Scene scene = new Scene(root, 965, 470);
        stage.setTitle(Main.PROJECT_NAME);
        stage.setScene(scene);
        stage.show();

        stage.setMinWidth(965);
        stage.setMinHeight(470);
    }

    public static void launchUI(String[] args) {
        launch(args);
    }

    private static Path addSuffix(Path path, String suffix) {
        String fileName = path.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        String name = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String ext = (dotIndex == -1) ? "" : fileName.substring(dotIndex);

        return path.resolveSibling(name + suffix + ext);
    }
}