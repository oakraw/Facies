package com.oakraw.facies.custom;

/**
 * Created by Rawipol on 12/20/14 AD.
 */
public class Coordinates {
    private float x;
    private float y;
    private float width;
    private float height;

    public Coordinates(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
