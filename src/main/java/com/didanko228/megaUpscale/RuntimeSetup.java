package com.didanko228.megaUpscale;

import com.didanko228.megaUpscale.utils.Logger;
import com.didanko228.megaUpscale.utils.OperatingSystemDetector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.didanko228.megaUpscale.utils.OperatingSystemDetector.detectOS;

public class RuntimeSetup {
    public static Path jarDir() {
        try {
            return Path.of(RuntimeSetup.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).getParent();
        } catch (Exception e) {
            throw new RuntimeException("Cannot determine JAR directory", e);
        }
    }

    public static void ensureWritable(Path dir) throws IOException {
        Files.createDirectories(dir);
        Path test = Files.createTempFile(dir, "test", null);
        Files.delete(test);
    }

    /** Распаковка одного файла из InputStream */
    public static void extractResource(InputStream in, Path target) throws IOException {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Основной метод распаковки с прогрессом */
    public static Map<String, Path> setupRuntimeWithProgress() throws IOException, URISyntaxException {
        OperatingSystemDetector.OS os = detectOS();
        Path baseDir = jarDir();

        try {
            ensureWritable(baseDir);
        } catch (Exception e) {
            baseDir = Path.of(System.getProperty("user.home"), ".cache", "megaUpscale");
            Files.createDirectories(baseDir);
        }

        Path binDir = baseDir.resolve("bin");
        Path modelsDir = baseDir.resolve("models");
        Files.createDirectories(binDir);
        Files.createDirectories(modelsDir);

        // --- Backend ---
        Path backendExe;
        if (os == OperatingSystemDetector.OS.WINDOWS) {
            backendExe = binDir.resolve("realesrgan.exe");
            extractFromJar("/native/windows/realesrgan-ncnn-vulkan.exe", backendExe);
            extractFromJar("/native/windows/vcomp140.dll", binDir.resolve("vcomp140.dll"));
        } else if (os == OperatingSystemDetector.OS.MAC) {
            backendExe = binDir.resolve("realesrgan");
            extractFromJar("/native/macos/realesrgan-ncnn-vulkan", backendExe);
            backendExe.toFile().setExecutable(true);
        } else {
            backendExe = binDir.resolve("realesrgan");
            extractFromJar("/native/linux/realesrgan-ncnn-vulkan", backendExe);
            backendExe.toFile().setExecutable(true);
        }

        // --- Модели ---
        List<String> modelFiles = listResources("/models");
        int total = modelFiles.size();
        int done = 0;

        for (String modelPath : modelFiles) {
            String name = Paths.get(modelPath).getFileName().toString();
            Path target = modelsDir.resolve(name);
            if (!Files.exists(target)) {
                extractFromJar(modelPath, target);
                Logger.info("[Models] [" + ++done + "/" + total + "] " + name + " extracted");
            } else {
                Logger.info("[Models] [" + ++done + "/" + total + "] " + name + " already exists");
            }
        }

        Map<String, Path> paths = new HashMap<>();
        paths.put("backend", backendExe);
        paths.put("models", modelsDir);
        paths.put("baseDir", baseDir);
        return paths;
    }

    /** Извлечение из JAR */
    public static void extractFromJar(String resourcePath, Path target) throws IOException {
        try (InputStream in = RuntimeSetup.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
            extractResource(in, target);
        }
    }

    /** Динамически перечисляем файлы внутри JAR в /models */
    public static List<String> listResources(String pathInJar) throws IOException, URISyntaxException {
        List<String> files = new ArrayList<>();

        URL url = RuntimeSetup.class.getResource(pathInJar);
        if (url == null) return files;

        if (url.getProtocol().equals("jar")) {
            String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry e = entries.nextElement();
                    if (!e.isDirectory() && e.getName().startsWith(pathInJar.substring(1))) {
                        files.add("/" + e.getName());
                    }
                }
            }
        } else if (url.getProtocol().equals("file")) {
            Path folder = Paths.get(url.toURI());
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                for (Path p : stream) {
                    if (!Files.isDirectory(p)) files.add("/models/" + p.getFileName());
                }
            }
        }
        return files;
    }

}
