package com.didanko228.megaUpscale;

import com.didanko228.megaUpscale.config.Config;
import com.didanko228.megaUpscale.config.ConfigManager;
import com.didanko228.megaUpscale.ui.MainWindow;
import com.didanko228.megaUpscale.utils.Logger;
import com.didanko228.megaUpscale.utils.TranslationManager;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class Main {
    public static String PROJECT_NAME = "megaUpscale";
    public static String PROJECT_ID = PROJECT_NAME.toLowerCase();

    static void main(String[] args) throws Exception {
        TranslationManager.loadTranslations();

        // --- первичная загрузка ресурсов ---
        Map<String, Path> paths = RuntimeSetup.setupRuntimeWithProgress();

        Path baseDir = paths.get("baseDir");
        Path backend = paths.get("backend");
        Path modelsDir = paths.get("models");

        Logger.info("BaseDir: " + baseDir);
        Logger.info("Backend: " + backend);
        Logger.info("Models: " + modelsDir);

        // Config
        File configFile = new File(baseDir.toFile(), "config.json");
        Config config = ConfigManager.loadConfig(configFile);

        MainWindow.initRuntime(baseDir, backend, modelsDir, config);
        MainWindow.launchUI(args);
    }
}
