package com.didanko228.megaUpscale.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class UpscaleService {
    private final Path backend;
    private final Path modelsDir;

    public UpscaleService(Path backend, Path modelsDir) {
        this.backend = backend;
        this.modelsDir = modelsDir;
    }

    /**
     * upscale
     *
     * @param input     input photo
     * @param output    output photo
     * @param modelName model name (example, 4xLSDIR)
     * @param scale     scale (2, 3, 4)
     */
    public void upscale(Path input, Path output, String modelName, int scale) throws IOException, InterruptedException {

        if (!Files.exists(input)) {
            Logger.error("Input file not found: " + input);
            return;
        }

        // command
        ProcessBuilder pb = new ProcessBuilder(
                backend.toAbsolutePath().toString(),
                "-i", input.toString(),
                "-o", output.toString(),
                "-n", modelName,
                "-m", modelsDir.toString(),
                "-s", String.valueOf(scale)
        );

        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Logger.info("[Upscale] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Upscale failed with exit code " + exitCode);
        }

        Logger.info("[Upscale] Finished successfully: " + output);
    }
}
