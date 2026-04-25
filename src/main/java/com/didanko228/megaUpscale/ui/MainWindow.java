package com.didanko228.megaUpscale.ui;

import com.didanko228.megaUpscale.Main;
import com.didanko228.megaUpscale.config.Config;
import com.didanko228.megaUpscale.config.ConfigManager;
import com.didanko228.megaUpscale.utils.*;
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
import java.util.List;

public class MainWindow extends Application {
    private static Path basePath;
    private static Path backendPath;
    private static Path modelsPath;
    private static Config config;

    public static void initRuntime(Path baseDir, Path backend, Path models, Config configuration) {
        basePath = baseDir;
        backendPath = backend;
        modelsPath = models;
        config = configuration;
    }

    @Override
    public void start(Stage stage) throws Exception {
        ModelRegistry registry = new ModelRegistry(basePath);

        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(10));

        Label imageSize = new Label(""); // for buttonHBox

        // Language
        List<String> languages = TranslationManager.getLanguages();
        ComboBox<String> languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(languages);
        languageComboBox.getSelectionModel().select(config.language);
        Label languageLabel = new Label();

        HBox languageBox = new HBox(5,
                new Label(TranslationManager.translate(languageComboBox.getValue(), "main.language.label")),
                languageComboBox,
                languageLabel
        );

        // Input
        TextField inputField = new TextField();
        Button inputBtn = new Button(TranslationManager.translate(languageComboBox.getValue(), "main.input.btn"));
        HBox inputBox = new HBox(5,
                new Label(TranslationManager.translate(languageComboBox.getValue(), "main.input.label")),
                inputField,
                inputBtn
        );
        HBox.setHgrow(inputField, Priority.ALWAYS);

        inputBtn.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if (file != null) inputField.setText(file.getAbsolutePath());
        });

        // Output
        TextField outputField = new TextField();
        Button outputBtn = new Button(TranslationManager.translate(languageComboBox.getValue(), "main.output.btn"));
        HBox outputBox = new HBox(5,
                new Label(TranslationManager.translate(languageComboBox.getValue(), "main.output.label")),
                outputField,
                outputBtn
        );
        HBox.setHgrow(outputField, Priority.ALWAYS);

        outputBtn.setOnAction(e -> {
            File file = new FileChooser().showSaveDialog(stage);
            if (file != null) outputField.setText(file.getAbsolutePath());
        });

        // Model and Scale
        ComboBox<String> modelBox = new ComboBox<>();

        for (ModelInfo model : registry.getModels()) {
            modelBox.getItems().add(model.name);
        }
        modelBox.getSelectionModel().selectFirst();

        ComboBox<Integer> scaleBox = new ComboBox<>();
        String nameFirstModel = modelBox.getItems().getFirst();
        ModelInfo firstModel = registry.getByName(nameFirstModel);

        addScaleOptions(scaleBox, firstModel, 5);

        scaleBox.getSelectionModel().selectFirst();

        HBox optionsBox = new HBox(10,
                new Label(TranslationManager.translate(languageComboBox.getValue(), "main.model.label")), modelBox,
                new Label(TranslationManager.translate(languageComboBox.getValue(), "main.scale.label")), scaleBox);

        // Log
        TextArea logArea = new TextArea();
        LoggerBridge loggerBridge = new LoggerBridge(logArea);
        logArea.setEditable(false);
        logArea.setPrefHeight(300);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        // Start
        Button startBtn = new Button(TranslationManager.translate(languageComboBox.getValue(), "main.start.btn"));
        startBtn.setOnAction(e -> {
            String inputPath = inputField.getText();
            String outputPath = outputField.getText();
            Path input = Paths.get(inputPath);
            Path output = Paths.get(outputPath);

            String model = modelBox.getValue();
            ModelInfo modelInfo = registry.getByName(model);
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

                        int countScale = 0;
                        int current = 1;

                        while (current < scale) {
                            current *= modelInfo.scale;
                            countScale++;
                        }

                        Path currentInput = input;

                        for (int i = 1; i <= countScale; i++) {
                            loggerBridge.log("Upscaling... " + i + " of " + countScale);

                            Path currentOutput;

                            if (i == countScale) {
                                currentOutput = output; // финальный файл
                            } else {
                                currentOutput = Files.createTempFile("upscale_step_" + i, ".png");
                            }

                            service.upscale(
                                    currentInput,
                                    currentOutput,
                                    model,
                                    modelInfo.scale
                            );

                            // удалить предыдущий временный файл
                            if (!currentInput.equals(input)) {
                                Files.deleteIfExists(currentInput);
                            }

                            currentInput = currentOutput;
                        }

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
        Button clearLogBtn = new Button(TranslationManager.translate(languageComboBox.getValue(), "main.clearlog.btn"));
        clearLogBtn.setOnAction(e -> logArea.clear());

        HBox buttonHBox = new HBox(5, startBtn, clearLogBtn, imageSize);

        root.getChildren().addAll(languageBox, inputBox, outputBox, optionsBox, buttonHBox, logArea);

        Scene scene = new Scene(root, 965, 470);
        stage.setTitle(Main.PROJECT_NAME);
        stage.setScene(scene);
        stage.show();

        stage.setMinWidth(965);
        stage.setMinHeight(470);

        // languageComboBox Listener
        languageComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            config.language = newValue;
            ConfigManager.saveConfig(config, new File(basePath.toFile(), "config.json"));
            languageLabel.setText(TranslationManager.translate(languageComboBox.getValue(), "main.language.set"));
        });

        // modelBox Listener
        modelBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            scaleBox.getItems().clear();

            ModelInfo newModel = registry.getByName(newValue);

            addScaleOptions(scaleBox, newModel, 5);

            scaleBox.getSelectionModel().selectFirst();
        });

        // scaleBox Listener
        scaleBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            ImageUtils.ImageSize size = null;
            try {
                size = ImageUtils.getImageSize(new File(inputField.getText()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            int width = size.width();
            int height = size.height();

            imageSize.setText(width + "x" + height + " -> " + (width * newValue) + "x" + (height * newValue));
        });

        // InputField Listener
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            Path path = Paths.get(newVal);

            if (Files.exists(path)) {
                try {
                    ImageUtils.ImageSize size = ImageUtils.getImageSize(new File(path.toString()));

                    int width = size.width();
                    int height = size.height();
                    int scale = scaleBox.getValue();

                    Path outPath = addSuffix(path, "_upscaled");
                    outputField.setText(outPath.toString());
                    imageSize.setText(width + "x" + height + " -> " + (width * scale) + "x" + (height * scale));
                } catch (Exception e) {
                    outputField.setText("");
                    imageSize.setText("");
                }
            } else {
                outputField.setText("");
                imageSize.setText("");
            }
        });
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

    private static void addScaleOptions(ComboBox<Integer> scaleBox, ModelInfo modelInfo, int countOptions) {
        int a = modelInfo.scale;

        for (int i = 1; i <= countOptions; i++) {
            scaleBox.getItems().add(a);
            a *= modelInfo.scale;
        }
    }
}