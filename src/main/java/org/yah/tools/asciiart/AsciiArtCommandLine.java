package org.yah.tools.asciiart;

import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AsciiArtCommandLine {

    public static final String DEFAULT_SYMBOLS = " .:-=+*#%@";

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            printHelp();
            return;
        }

        if (commandLine.hasOption(HELP)) {
            printHelp();
            return;
        }

        final List<File> files = getInputFiles(commandLine);
        final File output = getOutputDirectory(commandLine);
        final String symbols = getSymbols(commandLine);
        String lineSeparator = getLinseSeparator(commandLine);
        int flags = getFlags(commandLine);
        int targetWidth = getInt(WIDTH, commandLine);
        int targetHeight = getInt(HEIGHT, commandLine);

        AsciiArtGenerator generator = new AsciiArtGenerator(symbols);
        for (File inputFile : files) {
            final BufferedImage image;
            try {
                image = ImageIO.read(inputFile);
            } catch (IOException e) {
                System.err.println("Error reading image " + inputFile + ": " + e.getMessage());
                System.exit(1);
                return;
            }
            final CharactersImage generated = generator.generate(image, targetWidth, targetHeight, flags);
            String name = inputFile.getName();
            // change extension
            final int index = name.lastIndexOf('.');
            if (index > 0)
                name = name.substring(0, index);
            name += ".txt";
            File outputFile = new File(output, name);
            try {
                generated.toFile(lineSeparator, outputFile);
            } catch (IOException e) {
                System.err.println("Error writing output " + outputFile + ": " + e.getMessage());
                System.exit(1);
                return;
            }
        }
    }

    private static final String OUTPUT = "o";
    private static final String SYMBOLS = "s";
    private static final String HELP = "help";
    private static final String LINESEP = "ls";
    private static final String INVERT = "i";
    private static final String WIDTH = "w";
    private static final String HEIGHT = "h";

    private static final Options options = createOptions();

    private static int getInt(String opt, CommandLine commandLine) {
        if (commandLine.hasOption(opt)) {
            final String value = commandLine.getOptionValue(opt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Invalid " + options.getOption(opt).getLongOpt() + ": " + value);
                System.exit(1);
            }
        }
        return -1;
    }

    private static String getLinseSeparator(CommandLine commandLine) {
        if (commandLine.hasOption(LINESEP)) {
            String plarform = commandLine.getOptionValue(LINESEP);
            if (plarform.equals("WINDOWS"))
                return "\r\n";
            if (plarform.equals("LINUX"))
                return "\n";
            System.err.println("Invalid platform " + plarform);
            System.exit(1);
        }
        return System.lineSeparator();
    }

    private static int getFlags(CommandLine commandLine) {
        int flags = AsciiArtGenerator.NORMALIZE;
        if (commandLine.hasOption(INVERT))
            flags |= AsciiArtGenerator.INVERT;
        return flags;
    }

    private static String getSymbols(CommandLine commandLine) {
        if (commandLine.hasOption(SYMBOLS))
            return commandLine.getOptionValue(SYMBOLS);
        return DEFAULT_SYMBOLS;
    }

    private static File getOutputDirectory(CommandLine commandLine) {
        if (commandLine.hasOption(OUTPUT))
            return new File(commandLine.getOptionValue(OUTPUT));
        return new File(".");
    }

    private static List<File> getInputFiles(CommandLine commandLine) {
        final List<String> argList = commandLine.getArgList();
        if (argList.isEmpty()) {
            System.err.println("At least one input image file is required");
            System.exit(1);
        }
        final List<File> files = argList.stream()
                .map(File::new)
                .collect(Collectors.toList());
        for (File file : files) {
            if (!file.exists()) {
                System.err.println("file " + file + " was not found");
                System.exit(1);
            }
        }
        return files;
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ascii-art [OPTIONS] <files>...", options);
    }

    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(Option.builder(OUTPUT)
                .longOpt("output")
                .argName("directory")
                .numberOfArgs(1)
                .desc("Output directory. Default to current directory")
                .build());

        options.addOption(Option.builder(SYMBOLS)
                .longOpt("symbols")
                .argName("symbols")
                .numberOfArgs(1)
                .desc("Symbols used to geneate the ascii file.")
                .build());

        options.addOption(Option.builder(LINESEP)
                .argName("platform")
                .numberOfArgs(1)
                .desc("Line separator: WINDOW or LINUX, default to platform")
                .build());

        options.addOption(Option.builder(INVERT)
                .longOpt("invert")
                .desc("Invert the image grayscale before generating ascii art")
                .build());

        options.addOption(Option.builder(HELP)
                .desc("Print some help")
                .build());

        options.addOption(Option.builder(WIDTH)
                .longOpt("width")
                .argName("width")
                .numberOfArgs(1)
                .desc("Output width in characters. Default to input image width")
                .type(Integer.class)
                .build());

        options.addOption(Option.builder(HEIGHT)
                .longOpt("height")
                .argName("height")
                .numberOfArgs(1)
                .desc("Output height in characters.")
                .type(Integer.class)
                .build());

        return options;
    }
}
