package com.didanko228.megaUpscale;

import com.didanko228.megaUpscale.utils.Logger;
import com.didanko228.megaUpscale.utils.os.OperatingSystem;
import com.didanko228.megaUpscale.utils.os.OperatingSystemDetector;

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

public class RuntimeSetup {

    /** Распаковка одного файла из InputStream */
    public static void extractResource(InputStream in, Path target) throws IOException {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Основной метод распаковки с прогрессом */
    public static Map<String, Path> setupRuntimeWithProgress() throws IOException, URISyntaxException {
        OperatingSystem os = OperatingSystemDetector.detectOS();

        Path baseDir = null;

        if (os == OperatingSystem.WINDOWS) baseDir = Paths.get(System.getenv("APPDATA"), Main.PROJECT_NAME);
        else if (os == OperatingSystem.MAC) baseDir = Paths.get(System.getProperty("user.home"), "Applications", Main.PROJECT_NAME);
        else baseDir = Paths.get(System.getProperty("user.home"), ".config", Main.PROJECT_NAME);

        Path binDir = baseDir.resolve("bin");
        Path modelsDir = baseDir.resolve("models");
        Files.createDirectories(binDir);
        Files.createDirectories(modelsDir);

        // Backend
        Path backendBin;
        if (os == OperatingSystem.WINDOWS) {
            backendBin = binDir.resolve("realesrgan.exe");
            extractFromJar("/native/windows/realesrgan-ncnn-vulkan.exe", backendBin);
            extractFromJar("/native/windows/vcomp140.dll", binDir.resolve("vcomp140.dll"));
        } else if (os == OperatingSystem.MAC) {
            backendBin = binDir.resolve("realesrgan");
            extractFromJar("/native/macos/realesrgan-ncnn-vulkan", backendBin);
            backendBin.toFile().setExecutable(true);
        } else {
            backendBin = binDir.resolve("realesrgan");
            extractFromJar("/native/linux/realesrgan-ncnn-vulkan", backendBin);
            backendBin.toFile().setExecutable(true);
        }

        // Models
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
        paths.put("backend", backendBin);
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
