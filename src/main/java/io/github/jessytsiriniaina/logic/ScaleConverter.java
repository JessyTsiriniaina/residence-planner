package io.github.jessytsiriniaina.logic;

import java.util.IllegalFormatCodePointException;

public class ScaleConverter {
    private int pixelsPerMeter;

    public ScaleConverter(int pixelsPerMeter) throws IllegalArgumentException {
        if(pixelsPerMeter <= 0) {
            throw new IllegalArgumentException("L'echelle doit être positive");
        }
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public int toPixels(double meters) {
        return (int) Math.round(meters * pixelsPerMeter);
    }

    public double toMeters(int pixels) {
        return pixels / pixelsPerMeter;
    }
}
