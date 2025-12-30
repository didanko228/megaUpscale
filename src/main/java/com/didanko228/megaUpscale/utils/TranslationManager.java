package com.didanko228.megaUpscale.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;

public class TranslationManager {
    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    public static final String DEFAULT_LANGUAGE = "en_us";

    public static void loadTranslations() {
        try {
            // Получаем URL папки lang
            URL url = TranslationManager.class.getClassLoader().getResource("lang");
            if (url == null) {
                Logger.error("[TranslationManager] lang directory not found!");
                return;
            }

            String protocol = url.getProtocol();
            if (protocol.equals("file")) {
                // IDE
                File folder = new File(url.toURI());
                for (File file : Objects.requireNonNull(folder.listFiles(f -> f.getName().endsWith(".json")))) {
                    loadLocale(file.getName(), new FileInputStream(file));
                }
            } else if (protocol.equals("jar")) {
                // JAR
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                    jar.stream()
                            .filter(entry -> entry.getName().startsWith("lang/") && entry.getName().endsWith(".json"))
                            .forEach(entry -> {
                                try (InputStream in = TranslationManager.class.getClassLoader()
                                        .getResourceAsStream(entry.getName())) {
                                    loadLocale(entry.getName().replace("lang/", ""), in);
                                } catch (IOException e) {
                                    Logger.error("[TranslationManager] Error loading " + entry.getName(), e);
                                }
                            });
                }
            }
        } catch (Exception e) {
            Logger.error("[TranslationManager] Failed to load translations", e);
        }
    }

    private static void loadLocale(String fileName, InputStream in) throws IOException {
        String locale = fileName.replace(".json", "");
        Map<String, String> map = new Gson().fromJson(
                new BufferedReader(new InputStreamReader(in)),
                new TypeToken<Map<String, String>>() {}.getType()
        );
        translations.put(locale, map);
        Logger.info("[TranslationManager] Loaded locale: " + locale);
    }

    public static String translate(String language, String key, Object... args) {
        Map<String, String> localeMap = translations.getOrDefault(language, translations.get(DEFAULT_LANGUAGE));
        String template = localeMap.getOrDefault(key, key);
        return String.format(template, args);
    }
}
