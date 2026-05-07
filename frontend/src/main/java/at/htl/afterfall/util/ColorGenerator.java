package at.htl.afterfall.util;

import javafx.scene.paint.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ColorGenerator {
    private final Random     rng  = new Random();
    private final Set<String> used = new HashSet<>();

    public Color generateRouteColor() {
        Color c;
        do {
            double hue = rng.nextDouble() * 360;
            double sat = 0.5 + rng.nextDouble() * 0.5;
            double bri = 0.4 + rng.nextDouble() * 0.45;
            c = Color.hsb(hue, sat, bri);
        } while (used.contains(toHex(c)));
        used.add(toHex(c));
        return c;
    }

    public void markUsed(Color c) { used.add(toHex(c)); }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()   * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue()  * 255));
    }
}
