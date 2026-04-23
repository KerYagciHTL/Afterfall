package com.metrobuilder.view;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.Objects;

/**
 * Slices individual sprites from a sprite sheet image.
 * Sprites are arranged in a uniform grid of fixed width/height.
 */
public class SpriteSheet {

    private final Image source;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int cols;
    private final int rows;

    public SpriteSheet(String resourcePath, int spriteWidth, int spriteHeight) {
        this.source = new Image(
            Objects.requireNonNull(
                SpriteSheet.class.getResourceAsStream(resourcePath),
                "Sprite sheet not found on classpath: " + resourcePath
            )
        );
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.cols = (int) (source.getWidth() / spriteWidth);
        this.rows = (int) (source.getHeight() / spriteHeight);
    }

    /**
     * Extracts the sprite at the given grid position.
     *
     * @param col zero-based column index (x axis)
     * @param row zero-based row index (y axis)
     */
    public WritableImage getSprite(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) {
            throw new IllegalArgumentException(
                String.format("Sprite (%d,%d) out of bounds — sheet is %dx%d", col, row, cols, rows)
            );
        }
        WritableImage sprite = new WritableImage(spriteWidth, spriteHeight);
        sprite.getPixelWriter().setPixels(
            0, 0, spriteWidth, spriteHeight,
            source.getPixelReader(),
            col * spriteWidth, row * spriteHeight
        );
        return sprite;
    }

    public int getCols()        { return cols; }
    public int getRows()        { return rows; }
    public int getSpriteWidth() { return spriteWidth; }
    public int getSpriteHeight(){ return spriteHeight; }
}
