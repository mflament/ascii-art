package org.yah.tools.asciiart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GrayscaleImage {

    public static GrayscaleImage from(BufferedImage image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int[] colorPixels = new int[imageWidth * imageHeight];
        image.getRGB(0, 0, imageWidth, imageHeight, colorPixels, 0, imageWidth);

        float[][] grayPixels = new float[imageHeight][imageWidth];
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int colorPixel = colorPixels[y * imageWidth + x];
                grayPixels[y][x] = average(red(colorPixel), green(colorPixel), blue(colorPixel));
            }
        }
        return new GrayscaleImage(grayPixels);
    }

    /**
     * [y][x] gray scale value
     */
    private final float[][] pixels;
    private final int width;
    private final int height;

    public GrayscaleImage(float[][] pixels) {
        this.pixels = pixels;
        height = pixels.length;
        width = pixels[0].length;
    }

    public float average() {
        float sum = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sum += pixels[y][x];
            }
        }
        return sum / (width * height);
    }

    public float average(int x, int y, int maxx, int maxy) {
        maxx = Math.min(maxx, width);
        maxy = Math.min(maxy, height);
        float sum = 0;
        int pixelsCount = 0;
        for (int cy = y; cy < maxy; cy++) {
            for (int cx = x; cx < maxx; cx++) {
                sum += pixels[cy][cx];
                pixelsCount++;
            }
        }
        return sum / pixelsCount;
    }

    public void normalize() {
        final MinMax minMax = MinMax.from(pixels);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = minMax.lerp(pixels[y][x]);
            }
        }
    }

    public void invert() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = 1f - pixels[y][x];
            }
        }
    }

    public void save(String file) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, argb(pixels[y][x]));
            }
        }
        ImageIO.write(image, "png", new File(file));
    }

    private static float average(float... values) {
        float res = 0;
        for (int i = 0; i < values.length; i++) {
            res += values[i];
        }
        return res / values.length;
    }

    private static float red(int argb) {
        return ((argb & 0xFF0000) >> 16) / 255f;
    }

    private static float green(int argb) {
        return ((argb & 0xFF00) >> 8) / 255f;
    }

    private static float blue(int argb) {
        return (argb & 0xFF) / 255f;
    }

    private static int argb(float grayscale) {
        byte col = (byte) (255 * grayscale);
        int res = 0xFF000000;
        res |= (col << 16) & 0xFF0000;
        res |= (col << 8) & 0xFF00;
        res |= col & 0xFF;
        return res;
    }


}
