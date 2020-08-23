package org.yah.tools.asciiart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CharacterRamp {

    public static final Font FONT = Font.decode(Font.MONOSPACED);

    public static CharacterRamp create(String symbols) {
        return new CharacterRamp(sort(symbols));
    }

    private static final String LS = System.lineSeparator();
    private final CharacterLuminescence[] characters;

    private CharacterRamp(CharacterLuminescence[] characters) {
        this.characters = characters;
    }

    public char get(float luminence) {
        for (int i = 1; i < characters.length; i++) {
            if (characters[i].luminescence > luminence)
                return characters[i - 1].character;
        }
        return characters[characters.length - 1].character;
    }

    @Override
    public String toString() {
        return Arrays.stream(characters)
                .map(c -> Character.toString(c.character))
                .collect(Collectors.joining());
    }

    public String toDetailedString() {
        return Arrays.stream(characters)
                .map(c -> c.character + " : " + c.luminescence + LS)
                .collect(Collectors.joining());
    }

    private static CharacterLuminescence[] sort(String symbols) {
        final int length = symbols.length();
        if (length == 0)
            throw new IllegalArgumentException("need symbols");

        CharacterBounds bounds = new CharacterBounds(symbols.charAt(0));

        GrayscaleImage characterImage = createImage(symbols, bounds);
        CharacterLuminescence[] characterLuminescences = new CharacterLuminescence[length];
        int xOffset = 0;
        MinMax.Builder minMaxBuilder = MinMax.builder();
        float[] luminescences = new float[length];
        // average each symbol luminescence
        for (int i = 0; i < length; i++) {
            luminescences[i] = characterImage.average(xOffset, 0, xOffset + bounds.width, bounds.heigth);
            minMaxBuilder.add(luminescences[i]);
            xOffset += bounds.width;
        }
        final MinMax minMax = minMaxBuilder.build();
        for (int i = 0; i < length; i++) {
            characterLuminescences[i] = new CharacterLuminescence(symbols.charAt(i), minMax.lerp(luminescences[i]));
        }
        // sort by luminescence
        Arrays.sort(characterLuminescences);
        return characterLuminescences;
    }

    private static GrayscaleImage createImage(String symbols, CharacterBounds bounds) {
        BufferedImage image = new BufferedImage(bounds.width * symbols.length(),
                bounds.heigth,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(Color.BLACK);
            g.setFont(FONT);
            g.drawString(symbols, 0, -bounds.basline);
        } finally {
            g.dispose();
        }
        return GrayscaleImage.from(image);
    }

    private static class CharacterLuminescence implements Comparable<CharacterLuminescence> {
        private final char character;
        private final float luminescence;

        public CharacterLuminescence(char character, float luminescence) {
            this.character = character;
            this.luminescence = luminescence;
        }

        @Override
        public int compareTo(CharacterLuminescence o) {
            return Float.compare(luminescence, o.luminescence);
        }
    }

}
