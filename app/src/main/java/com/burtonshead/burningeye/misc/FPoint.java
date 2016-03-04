package com.burtonshead.burningeye.misc;

public class FPoint {
    public float x;
    public float y;

    public FPoint()
    {

    }

    public FPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float distanceFrom(FPoint p) {
        return (float) Math.hypot((double) (p.x - this.x), (double) (p.y - this.y));
    }

    public static FPoint getPointOnLine(FPoint start, FPoint end, float distance, FPoint result) {
        float fraction = Math.min(1.0f, distance / ((float) Math.hypot((double) (end.x - start.x), (double) (end.y - start.y))));
        result.x = start.x + ((end.x - start.x) * fraction);
        result.y = start.y + ((end.y - start.y) * fraction);
        return result;
    }
}
