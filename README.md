# ASCII art generator.

Convert image to ascii art.

Use java image API and AWT to compute luminescence of each symbol used to draw the ascii image.

## Build
`mvn clean package`

Will create an executable jar in target directory. 
  
## Usage
`java -jar ascii-art.jar [OPTIONS] <files>`

Where options are:
```
 -h,--height <height>      Output height in characters.
 -help                     Print some help
 -i,--invert               Invert the image grayscale before generating
                           ascii art
 -ls <platform>            Line separator: WINDOW or LINUX, default to
                           platform
 -o,--output <directory>   Output directory. Default to current directory
 -s,--symbols <symbols>    Symbols used to geneate the ascii file.
 -w,--width <width>        Output width in characters. Default to input
                           image width
```

`files` are one or more path to images (png, jpg, bmp ... and other format supported by `javax.image.ImageIO`). 