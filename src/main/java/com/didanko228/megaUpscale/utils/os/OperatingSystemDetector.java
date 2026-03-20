package com.didanko228.megaUpscale.utils.os;

public class OperatingSystemDetector {
    public static OperatingSystem detectOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OperatingSystem.WINDOWS;
        if (os.contains("mac")) return OperatingSystem.MAC;
        return OperatingSystem.LINUX;
    }
}
