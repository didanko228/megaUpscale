package com.didanko228.megaUpscale.config;

import com.didanko228.megaUpscale.utils.Logger;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(BigDecimal.valueOf(src).stripTrailingZeros().toPlainString()))
            .registerTypeAdapter(double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(BigDecimal.valueOf(src).stripTrailingZeros().toPlainString()))
            .create();

    public static Config loadConfig(File configFile) {
        Config defaultConfig = new Config();
        JsonElement defaultJson = GSON.toJsonTree(defaultConfig);

        if (!configFile.exists()) {
            saveConfig(defaultConfig, configFile);
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonElement loadedJson = JsonParser.parseReader(reader);

            if (loadedJson == null || loadedJson.isJsonNull()) {
                Logger.error("Config file empty or null, loading default config.");
                saveConfig(defaultConfig, configFile);
                return defaultConfig;
            }

            boolean changed = false;
            if (loadedJson.isJsonObject() && defaultJson.isJsonObject()) {
                changed = mergeJsonObjects(loadedJson.getAsJsonObject(), defaultJson.getAsJsonObject());
            }

            Config mergedConfig = GSON.fromJson(loadedJson, Config.class);

            saveConfig(mergedConfig, configFile);

            if (changed) {
                Logger.info("Config updated with default values for missing fields.");
            }

            return mergedConfig;
        } catch (IOException | JsonSyntaxException e) {
            Logger.error("Error loading config: " + e.getMessage());
            saveConfig(defaultConfig, configFile);
            return defaultConfig;
        }
    }

    public static void logConfigFields(Object config) {
        Class<?> clazz = config.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(config);
                Logger.info(field.getName() + " = " + value);
            } catch (IllegalAccessException e) {
                Logger.info("Error getting value of " + field.getName() + ": " + e);
            }
        }
    }

    public static void saveConfig(Config config, File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error save config", e);
        }
    }

    private static boolean mergeJsonObjects(JsonObject target, JsonObject defaults) {
        boolean changed = false;

        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            String key = entry.getKey();
            JsonElement defaultValue = entry.getValue();

            if (!target.has(key)) {
                target.add(key, defaultValue);
                changed = true;
            } else {
                JsonElement targetValue = target.get(key);
                if (defaultValue.isJsonObject() && targetValue.isJsonObject()) {
                    boolean childChanged = mergeJsonObjects(targetValue.getAsJsonObject(), defaultValue.getAsJsonObject());
                    if (childChanged) changed = true;
                }
            }
        }

        return changed;
    }
}
