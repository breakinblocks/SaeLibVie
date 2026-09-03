package com.breakinblocks.saelibvie.ui.geom;

public record Rect(int x, int y, int w, int h) {
    public static final Rect EMPTY = new Rect(0, 0, 0, 0);

    public static Rect of(int x, int y, int w, int h) {
        return new Rect(x, y, w, h);
    }

    public static Rect fromCorners(int x1, int y1, int x2, int y2) {
        return new Rect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public int right() {
        return x + w;
    }

    public int bottom() {
        return y + h;
    }

    public int centerX() {
        return x + w / 2;
    }

    public int centerY() {
        return y + h / 2;
    }

    public boolean isEmpty() {
        return w <= 0 || h <= 0;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    public boolean intersects(Rect other) {
        return other.x < right() && other.right() > x && other.y < bottom() && other.bottom() > y;
    }

    public Rect intersect(Rect other) {
        int nx = Math.max(x, other.x);
        int ny = Math.max(y, other.y);
        int nr = Math.min(right(), other.right());
        int nb = Math.min(bottom(), other.bottom());
        if (nr <= nx || nb <= ny) {
            return new Rect(nx, ny, 0, 0);
        }
        return new Rect(nx, ny, nr - nx, nb - ny);
    }

    public Rect union(Rect other) {
        if (isEmpty()) return other;
        if (other.isEmpty()) return this;
        return fromCorners(Math.min(x, other.x), Math.min(y, other.y), Math.max(right(), other.right()), Math.max(bottom(), other.bottom()));
    }

    public Rect offset(int dx, int dy) {
        return new Rect(x + dx, y + dy, w, h);
    }

    public Rect at(int nx, int ny) {
        return new Rect(nx, ny, w, h);
    }

    public Rect sized(int nw, int nh) {
        return new Rect(x, y, nw, nh);
    }

    public Rect inset(int amount) {
        return inset(amount, amount, amount, amount);
    }

    public Rect inset(Insets insets) {
        return inset(insets.left(), insets.top(), insets.right(), insets.bottom());
    }

    public Rect inset(int left, int top, int right, int bottom) {
        return new Rect(x + left, y + top, Math.max(0, w - left - right), Math.max(0, h - top - bottom));
    }

    public Rect grow(int amount) {
        return new Rect(x - amount, y - amount, w + amount * 2, h + amount * 2);
    }

    public Rect withX(int nx) {
        return new Rect(nx, y, w, h);
    }

    public Rect withY(int ny) {
        return new Rect(x, ny, w, h);
    }

    public Rect withW(int nw) {
        return new Rect(x, y, nw, h);
    }

    public Rect withH(int nh) {
        return new Rect(x, y, w, nh);
    }

    public Rect leftPart(int width) {
        return new Rect(x, y, Math.min(width, w), h);
    }

    public Rect rightPart(int width) {
        int nw = Math.min(width, w);
        return new Rect(right() - nw, y, nw, h);
    }

    public Rect topPart(int height) {
        return new Rect(x, y, w, Math.min(height, h));
    }

    public Rect bottomPart(int height) {
        int nh = Math.min(height, h);
        return new Rect(x, bottom() - nh, w, nh);
    }

    public Rect splitLeft(int width) {
        return new Rect(x + width, y, Math.max(0, w - width), h);
    }

    public Rect splitTop(int height) {
        return new Rect(x, y + height, w, Math.max(0, h - height));
    }

    public Rect align(int innerW, int innerH, Anchor anchor) {
        int ax = x + anchor.horizontal().offset(w, innerW);
        int ay = y + anchor.vertical().offset(h, innerH);
        return new Rect(ax, ay, innerW, innerH);
    }

    public Rect clampInside(Rect bounds) {
        int nx = Math.max(bounds.x, Math.min(x, bounds.right() - w));
        int ny = Math.max(bounds.y, Math.min(y, bounds.bottom() - h));
        return new Rect(nx, ny, w, h);
    }
}
