package org.yah.tools.asciiart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class CharacterRamp {

    public static final Font FONT = Font.decode(Font.MONOSPACED);

    public static CharacterRamp create(String symbols) {
        return new CharacterRamp(sort(symbols));
    }

    private final String sortedSymbols;

    private CharacterRamp(String sortedSymbols) {
        this.sortedSymbols = sortedSymbols;
    }

    public char get(float luminence) {
        int index = (int) (luminence * (sortedSymbols.length() - 1));
        return sortedSymbols.charAt(index);
    }

    private static String sort(String symbols) {
        if (symbols.length() == 0)
            throw new IllegalArgumentException("need symbols");

        CharacterBounds bounds = new CharacterBounds(symbols.charAt(0));

        GrayscaleImage characterImage = createImage(symbols, bounds);
        CharacterLuminescence[] characterLuminences = new CharacterLuminescence[symbols.length()];
        int xOffset = 0;
        // average each symbol luminescence
        for (int i = 0; i < symbols.length(); i++) {
            float luminence = characterImage.average(xOffset, 0, xOffset + bounds.width, bounds.heigth);
            characterLuminences[i] = new CharacterLuminescence(symbols.charAt(i), luminence);
        }
        // sort by luminescence
        Arrays.sort(characterLuminences);
        // create a string form sorted characters
        StringBuilder sb = new StringBuilder(symbols.length());
        for (int i = 0; i < characterLuminences.length; i++) {
            sb.append(characterLuminences[i].character);
        }
        return sb.toString();
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
