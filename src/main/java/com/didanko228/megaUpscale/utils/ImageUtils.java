package com.didanko228.megaUpscale.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.Iterator;

public class ImageUtils {

    public record ImageSize(int width, int height) {}

    public static ImageSize getImageSize(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                throw new IllegalArgumentException("Not a readable file");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException("File is not an image");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                return new ImageSize(width, height);
            } finally {
                reader.dispose();
            }
        }
    }
}
