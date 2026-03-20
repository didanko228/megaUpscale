package com.didanko228.megaUpscale;

import com.didanko228.megaUpscale.ui.MainWindow;
import com.didanko228.megaUpscale.utils.Logger;
import com.didanko228.megaUpscale.utils.TranslationManager;

import java.nio.file.Path;
import java.util.Map;

public class Main {
    public static String PROJECT_NAME = "megaUpscale";
    public static String PROJECT_ID = PROJECT_NAME.toLowerCase();

    static void main(String[] args) throws Exception {
        TranslationManager.loadTranslations();

        // --- первичная загрузка ресурсов ---
        Map<String, Path> paths = RuntimeSetup.setupRuntimeWithProgress();

        Path backend = paths.get("backend");
        Path modelsDir = paths.get("models");

        Logger.info("Backend: " + backend);
        Logger.info("Models: " + modelsDir);

        MainWindow.launchUI(args);
    }
}
