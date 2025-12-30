package com.didanko228.megaUpscale.utils;

import com.didanko228.megaUpscale.Main;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unused", "StringConcatenationArgumentToLogCall"})
public class Logger {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Main.PROJECT_ID);
    private final static String prefix = "(" + Main.PROJECT_NAME + ") ";

    public static void info(String s) {
        LOGGER.info(prefix + s);
    }

    public static void info(String s, Throwable t) {
        LOGGER.info(prefix + s, t);
    }

    public static void info(String format, Object... args) {
        LOGGER.info(prefix + format, args);
    }

    public static void warn(String s) {
        LOGGER.warn(prefix + s);
    }

    public static void warn(String s, Throwable t) {
        LOGGER.warn(prefix + s, t);
    }

    public static void warn(String format, Object... args) {
        LOGGER.warn(prefix + format, args);
    }

    public static void error(String s) {
        LOGGER.error(prefix + s);
    }

    public static void error(String s, Throwable t) {
        LOGGER.error(prefix + s, t);
    }

    public static void error(String format, Object... args) {
        LOGGER.error(prefix + format, args);
    }

    public static void debug(String s) {
        LOGGER.debug(prefix + s);
    }

    public static void debug(String s, Throwable t) {
        LOGGER.debug(prefix + s, t);
    }

    public static void debug(String format, Object... args) {
        LOGGER.debug(prefix + format, args);
    }

    public static void trace(String s) {
        LOGGER.trace(prefix + s);
    }

    public static void trace(String s, Throwable t) {
        LOGGER.trace(prefix + s, t);
    }

    public static void trace(String format, Object... args) {
        LOGGER.trace(prefix + format, args);
    }
}
