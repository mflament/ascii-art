package org.yah.tools.asciiart;

import java.awt.image.BufferedImage;

public class AsciiArtGenerator {


    public static final int NORMALIZE = 1;
    public static final int INVERT = 2;

    private final CharacterRamp characterRamp;

    /**
     * @param symbols the ASCII character to use as result
     */
    public AsciiArtGenerator(String symbols) {
        this.characterRamp = CharacterRamp.create(symbols);
    }

    public CharactersImage generate(BufferedImage image,
                                    int targetWidth,
                                    int targetHeight,
                                    int flags) {
        if (targetWidth == 0 || targetHeight == 0)
            throw new IllegalArgumentException("maxWidth or maxHeight can not be 0");

        if (targetWidth < 0 && targetHeight < 0)
            targetWidth = image.getWidth();

        final float charAspectRatio = new CharacterBounds(' ').getAspectRatio();
        final float imageAspectRatio = image.getWidth() / (float) image.getHeight();
        final float aspectRatio = charAspectRatio * imageAspectRatio;

        int width, height, margin = 0;
        if (targetWidth < 0) {
            // target height set, no target width
            // compute width from target height and aspect ratio
            height = targetHeight;
            width = (int) (targetHeight / aspectRatio);
        } else if (targetHeight < 0) {
            // target width but no target height
            // compute height from width and aspect ratio
            width = targetWidth;
            height = (int) (targetWidth * aspectRatio);
        } else {
            // target height and target width
            // respect target height, and use target width to center if needed
            height = targetHeight;
            width = (int) (targetHeight / aspectRatio);
            if (width < targetWidth) {
                // center
                margin = (targetWidth - width) / 2;
            }
        }

        char[][] chars = new char[height][width];
        float stepx = Math.max(1, image.getWidth() / (float) width);
        float stepy = Math.max(1, image.getHeight() / (float) height);

        GrayscaleImage grayscaleImage = GrayscaleImage.from(image);
        if ((flags & NORMALIZE) != 0)
            grayscaleImage.normalize();
        if ((flags & INVERT) != 0)
            grayscaleImage.invert();

        for (int y = 0; y < height; y++) {
            float yoffset = y * stepy;
            float maxy = yoffset + stepy;
            for (int x = 0; x < width; x++) {
                float xoffset = x * stepx;
                float maxx = xoffset + stepx;
                final float average = grayscaleImage.average((int) xoffset, (int) yoffset, (int) maxx, (int) maxy);
                chars[y][x] = characterRamp.get(average);
            }
        }
        return new CharactersImage(chars, margin);
    }
}
