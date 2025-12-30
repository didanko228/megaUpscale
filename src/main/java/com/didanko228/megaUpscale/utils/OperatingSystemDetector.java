package com.didanko228.megaUpscale.utils;

public class OperatingSystemDetector {
    public enum OS {
        WINDOWS, LINUX, MAC
    }

    public static OS detectOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OS.WINDOWS;
        if (os.contains("mac")) return OS.MAC;
        return OS.LINUX;
    }
}
