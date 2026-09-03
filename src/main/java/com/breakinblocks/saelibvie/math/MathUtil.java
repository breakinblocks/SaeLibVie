package com.breakinblocks.saelibvie.math;

import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class MathUtil {
    public static final Random RANDOM = new Random();
    public static final int[] NORMALS_X = {0, 0, 0, 0, -1, 1};
    public static final int[] NORMALS_Y = {-1, 1, 0, 0, 0, 0};
    public static final int[] NORMALS_Z = {0, 0, -1, 1, 0, 0};
    public static final int[] ROTATION_X = {90, 270, 0, 0, 0, 0};
    public static final int[] ROTATION_Y = {0, 0, 180, 0, 90, 270};

    private static final int SPIRAL_CACHE_SIZE = 81;
    private static XZ[] spiralCache;

    private MathUtil() {
    }

    public static double sq(double v) {
        return v * v;
    }

    public static double sqrt(double v) {
        return v == 0D || v == 1D ? v : Math.sqrt(v);
    }

    public static double sqrt2sq(double x, double y) {
        return sqrt(sq(x) + sq(y));
    }

    public static double sqrt3sq(double x, double y, double z) {
        return sqrt(sq(x) + sq(y) + sq(z));
    }

    public static double distSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        if (x1 == x2 && y1 == y2 && z1 == z2) return 0D;
        return sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1);
    }

    public static double dist(double x1, double y1, double z1, double x2, double y2, double z2) {
        return sqrt(distSq(x1, y1, z1, x2, y2, z2));
    }

    public static double distSq(double x1, double y1, double x2, double y2) {
        return sq(x2 - x1) + sq(y2 - y1);
    }

    public static double dist(double x1, double y1, double x2, double y2) {
        return sqrt(distSq(x1, y1, x2, y2));
    }

    public static int chunk(int v) {
        return v >> 4;
    }

    public static int chunk(double v) {
        return chunk((int) Math.floor(v));
    }

    public static boolean canParseInt(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean canParseDouble(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static float lerp(float min, float max, float t) {
        return min + (max - min) * t;
    }

    public static double lerp(double min, double max, double t) {
        return min + (max - min) * t;
    }

    public static Vec3 lerp(double x1, double y1, double z1, double x2, double y2, double z2, double t) {
        return new Vec3(lerp(x1, x2, t), lerp(y1, y2, t), lerp(z1, z2, t));
    }

    public static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return lerp(a.x, a.y, a.z, b.x, b.y, b.z, t);
    }

    public static double map(double min1, double max1, double min2, double max2, double v) {
        return lerp(min2, max2, (v - min1) / (max1 - min1));
    }

    public static double mod(double v, double n) {
        double r = v % n;
        return r < 0 ? r + n : r;
    }

    public static int mod(int v, int n) {
        int r = v % n;
        return r < 0 ? r + n : r;
    }

    public static long clamp(long v, long lo, long hi) {
        return v < lo ? lo : Math.min(v, hi);
    }

    public static XZ getSpiralPoint(int index) {
        if (index < 0) index = 0;
        if (index < SPIRAL_CACHE_SIZE) {
            if (spiralCache == null) {
                XZ[] cache = new XZ[SPIRAL_CACHE_SIZE];
                for (int i = 0; i < SPIRAL_CACHE_SIZE; i++) {
                    cache[i] = getSpiralPoint0(i);
                }
                spiralCache = cache;
            }
            return spiralCache[index];
        }
        return getSpiralPoint0(index);
    }

    public static XZ getSpiralPoint0(int index) {
        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = 1;
        int segmentLength = 1;
        int segmentPassed = 0;
        for (int i = 0; i < index; i++) {
            x += dx;
            z += dz;
            segmentPassed++;
            if (segmentPassed == segmentLength) {
                segmentPassed = 0;
                int oldDx = dx;
                dx = dz;
                dz = -oldDx;
                if (dx == 0) {
                    segmentLength++;
                }
            }
        }
        return new XZ(x, z);
    }
}
