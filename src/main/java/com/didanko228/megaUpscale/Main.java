package com.didanko228.megaUpscale;

import com.didanko228.megaUpscale.utils.Config;
import com.didanko228.megaUpscale.utils.Logger;
import com.didanko228.megaUpscale.utils.OperatingSystemDetector;
import com.didanko228.megaUpscale.utils.TranslationManager;

public class Main {
    public static String PROJECT_NAME = "megaUpscale";
    public static String PROJECT_ID = PROJECT_NAME.toLowerCase();

    static void main() {
        TranslationManager.loadTranslations();

        Logger.info("KEY: " + Config.KEY);
        Logger.info(TranslationManager.translate("en_us", "helloworld"));
        Logger.info(TranslationManager.translate("ru_ru", "helloworld"));
        Logger.info("OS: " + OperatingSystemDetector.detectOS());
    }
}
