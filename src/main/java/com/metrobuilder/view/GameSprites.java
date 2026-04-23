package com.metrobuilder.view;

import javafx.scene.image.WritableImage;

/**
 * Named sprite definitions for the game.
 *
 * Sprite sheet layouts (all sprites are 512×512 px):
 *
 * rail.png (1024×1536 → 2 cols × 3 rows):
 *   (0,0) horizontal rail   (1,0) —
 *   (0,1) vertical rail     (1,1) —
 *   (0,2) —                 (1,2) —
 *
 * train_down.png (1024×1536 → 2 cols × 3 rows):
 *   rows: blue(0), orange(1), red(2)
 *   cols: front(0), rear(1)
 *
 * train_left.png (1536×1024 → 3 cols × 2 rows):
 *   rows: blue(0), orange(1)
 *   cols: front(0), wagon(1), rear(2)
 */
public class GameSprites {

    private final SpriteSheet railSheet;
    private final SpriteSheet trainDownSheet;
    private final SpriteSheet trainLeftSheet;

    public GameSprites() {
        railSheet      = new SpriteSheet("/assets/images/rail.png",       512, 512);
        trainDownSheet = new SpriteSheet("/assets/images/train_down.png", 512, 512);
        trainLeftSheet = new SpriteSheet("/assets/images/train_left.png", 512, 512);
    }

    // --- Rails ---
    public WritableImage railHorizontal() { return railSheet.getSprite(0, 0); }
    public WritableImage railVertical()   { return railSheet.getSprite(0, 1); }

    // --- Train facing down (front = locomotive, rear = tail end) ---
    public WritableImage trainDownBlueFront()   { return trainDownSheet.getSprite(0, 0); }
    public WritableImage trainDownBlueRear()    { return trainDownSheet.getSprite(1, 0); }
    public WritableImage trainDownOrangeFront() { return trainDownSheet.getSprite(0, 1); }
    public WritableImage trainDownOrangeRear()  { return trainDownSheet.getSprite(1, 1); }
    public WritableImage trainDownRedFront()    { return trainDownSheet.getSprite(0, 2); }
    public WritableImage trainDownRedRear()     { return trainDownSheet.getSprite(1, 2); }

    // --- Train facing left (front → wagon → rear) ---
    public WritableImage trainLeftBlueFront()   { return trainLeftSheet.getSprite(0, 0); }
    public WritableImage trainLeftBlueWagon()   { return trainLeftSheet.getSprite(1, 0); }
    public WritableImage trainLeftBlueRear()    { return trainLeftSheet.getSprite(2, 0); }
    public WritableImage trainLeftOrangeFront() { return trainLeftSheet.getSprite(0, 1); }
    public WritableImage trainLeftOrangeWagon() { return trainLeftSheet.getSprite(1, 1); }
    public WritableImage trainLeftOrangeRear()  { return trainLeftSheet.getSprite(2, 1); }
}
