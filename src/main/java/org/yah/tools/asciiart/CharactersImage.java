package org.yah.tools.asciiart;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class CharactersImage {
    private static final String LS = System.lineSeparator();
    private final char[][] characters;
    private final String margin;

    public CharactersImage(char[][] characters, int margin) {
        this.characters = Objects.requireNonNull(characters, "characters is null");
        this.margin = createMargin(margin);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        int height = characters.length;
        int width = characters[0].length;
        //noinspection ForLoopReplaceableByForEach
        for (int y = 0; y < height; y++) {
            sb.append(margin);
            for (int x = 0; x < width; x++) {
                sb.append(characters[y][x]);
            }
            sb.append(LS);
        }
        return sb.toString();
    }

    public void toFile(String output) throws IOException {
        final String string = toString();
        try (OutputStream os = new FileOutputStream(output)) {
            final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
            os.write(bytes);
        }
    }

    private static String createMargin(int margin) {
        if (margin > 0) {
            char[] chars = new char[margin];
            Arrays.fill(chars, ' ');
            return new String(chars);
        }
        return "";
    }
}
