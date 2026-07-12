package io.github.jessytsiriniaina.logic;

public class ScaleConverter {
    private int pixelsPerMeter = 0;

    public int toPixels(double meters) {
        return (int) Math.round(meters * pixelsPerMeter);
    }

    public double toMeters(int pixels) {
        return pixels / pixelsPerMeter;
    }

    public void setPixelsPerMeter(int pixelsPerMeter) {
        this.pixelsPerMeter = pixelsPerMeter;
    }
}
