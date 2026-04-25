package com.didanko228.megaUpscale.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Getter
public class ModelRegistry {
    private final List<ModelInfo> models;

    public ModelRegistry(Path baseDir) throws Exception {
        Path jsonPath = baseDir.resolve("models.json");

        String json = Files.readString(jsonPath);

        this.models = new Gson().fromJson(
                json,
                new TypeToken<List<ModelInfo>>(){}.getType()
        );
    }

    public ModelInfo getByName(String name) {
        return models.stream()
                .filter(m -> m.name.equals(name))
                .findFirst()
                .orElseThrow();
    }
}
