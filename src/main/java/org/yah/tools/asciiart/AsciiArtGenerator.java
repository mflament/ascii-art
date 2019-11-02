package org.yah.tools.asciiart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class AsciiArtGenerator {

	private final char[] symbols;

	private final float step;

	/**
	 * default symbols to <code>.,:;i1tfLCG08@</code>
	 */
	public AsciiArtGenerator() {
		this(" .,:;i1tfLCG08@");
	}

	/**
	 * @param symbols
	 *            the ASCII character to use as result
	 */
	public AsciiArtGenerator(String symbols) {
		this.symbols = sortChars(symbols);
		step = 1f / symbols.length();
	}

	public char[][] generate(File imageFile, int targetWidth, int targetHeight) throws IOException {
		return generate(ImageIO.read(imageFile), targetWidth, targetHeight);
	}

	public char[][] generate(BufferedImage image, int targetWidth, int targetHeight) {
		float ar = image.getWidth() / (float) image.getHeight();
		if (targetWidth < 0 && targetHeight < 0) {
			targetWidth = image.getWidth();
			targetHeight = image.getHeight();
		}

		if (targetWidth < 0) {
			targetWidth = (int) (targetHeight * ar);
		}

		if (targetHeight < 0) {
			targetHeight = (int) (targetWidth * ar);
		}

		float[][] contrast = contrast(luminescence(image));
		contrast = resize(contrast, targetWidth, targetHeight);
		int height = contrast.length;
		int width = contrast[0].length;
		char[][] res = new char[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float v = contrast[y][x];
				int index = (int) (v / step);
				res[y][x] = symbols[index];
			}
		}
		return res;
	}

	private static float[][] luminescence(BufferedImage image) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		int[] pixels = new int[imageWidth * imageHeight];
		image.getRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);

		float[][] res = new float[imageHeight][imageWidth];
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				int pixel = pixels[y * imageWidth + x];
				res[y][x] = average(red(pixel), green(pixel), blue(pixel));
			}
		}
		return res;
	}

	private static float[][] contrast(float[][] values) {
		MinMax minMax = MinMax.from(values);
		return transform(values, v -> minMax.lerp(v));
	}

	private static class MinMax {
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

	private static float[][] transform(float[][] values, FloatFunction function) {
		int height = values.length;
		int width = values[0].length;
		float[][] res = new float[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float v = values[y][x];
				res[y][x] = function.apply(v);
			}
		}
		return res;
	}

	private interface FloatFunction {
		float apply(float v);
	}

	private static float[][] resize(float[][] values, int targetWidth, int targetHeight) {
		int sourceHeight = values.length;
		int sourceWidth = values[0].length;
		float tileWidth = sourceWidth / (float) targetWidth;
		float tileHeight = sourceHeight / (float) targetHeight;
		float[][] target = new float[targetHeight][targetWidth];
		for (int ty = 0; ty < targetHeight; ty++) {
			for (int tx = 0; tx < targetWidth; tx++) {
				int sx = (int) (tileWidth * tx);
				int sy = (int) (tileHeight * ty);
				target[ty][tx] = average(values, sx, sy,
						(int) (tileWidth * (tx + 1)),
						(int) (tileHeight * (ty + 1)));
			}
		}
		return target;
	}

	private static float average(float[][] values, int x, int y, int maxX, int maxY) {
		float res = 0;
		int count = 0;
		for (int dy = y; dy < maxY; dy++) {
			for (int dx = x; dx < maxX; dx++) {
				res += values[dy][dx];
				count++;
			}
		}
		return res / count;
	}

	/**
	 * used for debug
	 */
	@SuppressWarnings("unused")
	private static BufferedImage toImage(float[][] grayscale) {
		int height = grayscale.length;
		int width = grayscale[0].length;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = image.getRaster();
		int[] pixel = new int[1];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixel[0] = (int) (grayscale[y][x] * 255f);
				raster.setPixel(x, y, pixel);
			}
		}
		return image;
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

	public char[] sortChars(String s) {
		Font font = Font.decode(Font.MONOSPACED);
		Rectangle2D bounds = charBounds(font);

		// create an image containing all the symbols in black (foreground) and white
		// (background)
		BufferedImage img = new BufferedImage((int) bounds.getWidth() * s.length(),
				(int) bounds.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			g.setColor(Color.BLACK);
			g.setFont(font);
			g.drawString(s, 0, (int) -bounds.getY());
		} finally {
			g.dispose();
		}

		// create the luminescence of tall the symbols
		float[][] values = luminescence(img);

		// average each symbol luminescence
		WeightedChar[] weightedChars = new WeightedChar[s.length()];
		for (int i = 0; i < s.length(); i++) {
			int x = (int) (i * bounds.getWidth());
			weightedChars[i] = new WeightedChar(s.charAt(i),
					average(values, x, 0, (int) ((i + 1) * bounds.getWidth()), (int) bounds.getHeight()));
		}

		// sort symbols per luminescence
		Arrays.sort(weightedChars);

		// map to chars
		char[] res = new char[s.length()];
		for (int i = 0; i < res.length; i++) {
			res[i] = weightedChars[i].c;
		}
		return res;
	}

	private Rectangle2D charBounds(Font font) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = img.createGraphics();
		try {
			FontRenderContext fontRenderContext = graphics.getFontRenderContext();
			return font.getStringBounds(" ", fontRenderContext);
		} finally {
			graphics.dispose();
		}
	}

	private static class WeightedChar implements Comparable<WeightedChar> {
		private final char c;
		private final float w;

		public WeightedChar(char c, float w) {
			this.c = c;
			this.w = w;
		}

		@Override
		public int compareTo(WeightedChar o) {
			return Float.compare(w, o.w);
		}
	}

	public static void printToFile(char[][] chars, File output) throws IOException {
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(output)))) {
			int height = chars.length;
			int width = chars[0].length;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					ps.append(chars[y][x]);
				}
				ps.println();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		AsciiArtGenerator analyzer = new AsciiArtGenerator();
		printToFile(analyzer.generate(new File("images/pinguin.png"), 120, -1), new File("target/art.txt"));
	}

}
