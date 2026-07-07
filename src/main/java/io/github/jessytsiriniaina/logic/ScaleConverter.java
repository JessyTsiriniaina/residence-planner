package io.github.jessytsiriniaina.logic;

public class ScaleConverter {
    private double pixelsPerMeter = 20.0;

    public int toPixels(double meters) {
        return (int) Math.round(meters * pixelsPerMeter);
    }

    public double toMeters(int pixels) {
        return pixels / pixelsPerMeter;
    }

    public void setPixelsPerMeter(double pixelsPerMeter) {
        this.pixelsPerMeter = pixelsPerMeter;
    }
}
