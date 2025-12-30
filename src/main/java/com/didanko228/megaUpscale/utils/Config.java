package com.didanko228.megaUpscale.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    public static String KEY = dotenv.get("KEY");
}
