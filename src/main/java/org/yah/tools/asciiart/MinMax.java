package org.yah.tools.asciiart;

public class MinMax {
    private final float min, max;

    private MinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public static MinMax from(float[][] values) {
        int height = values.length;
        int width = values[0].length;
        float min = 1f, max = 0f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float v = values[y][x];
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }
        return new MinMax(min, max);
    }

    public float lerp(float v) {
        return (v - min) / (max - min);
    }
}
