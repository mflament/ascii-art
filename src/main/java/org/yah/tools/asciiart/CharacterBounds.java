package org.yah.tools.asciiart;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

class CharacterBounds {
    final int width;
    final int heigth;
    final int basline;

    public CharacterBounds(char character) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();
        try {
            FontRenderContext fontRenderContext = graphics.getFontRenderContext();
            final Rectangle2D bounds = CharacterRamp.FONT.getStringBounds(Character.toString(character), fontRenderContext);
            width = (int) bounds.getWidth();
            heigth = (int) bounds.getHeight();
            basline = (int) bounds.getY();
        } finally {
            graphics.dispose();
        }
    }

    float getAspectRatio() {
        return width / (float) heigth;
    }
}
