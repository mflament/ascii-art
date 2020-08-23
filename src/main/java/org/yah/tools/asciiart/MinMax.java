package org.yah.tools.asciiart;

public class MinMax {

    public static Builder builder() {
        return new Builder();
    }

    private final float min, max;

    private MinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float lerp(float v) {
        return (v - min) / (max - min);
    }

    public static class Builder {
        private float min = Float.MAX_VALUE;
        private float max = Float.MIN_VALUE;

        private Builder() {

        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder add(float value) {
            if (value < min)
                min = value;
            if (value > max)
                max = value;
            return this;
        }

        public Builder add(float[][] values) {
            for (float[] row : values) {
                for (float value : row) {
                    add(value);
                }
            }
            return this;
        }

        public MinMax build() {
            return new MinMax(min, max);
        }
    }
}
