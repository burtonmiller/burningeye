package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;

public class Obstacle extends GameObject {
    private Bitmap mBitmap;

    public Obstacle(int radius, Bitmap bitmap) {
        this.mRadius = radius;
        this.mBitmap = bitmap;
        this.mState = 0;
        this.mSpeed = 0.0f;
        this.mHP = 1000.0f;
    }

    public void update() {
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }
}
