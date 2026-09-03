package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.color.ColorTables;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public record Palette(String id, Component name, int[] colors) {
    public static final int MAX = 16;

    public Palette {
        if (colors.length > MAX) {
            int[] trimmed = new int[MAX];
            System.arraycopy(colors, 0, trimmed, 0, MAX);
            colors = trimmed;
        }
    }

    public static Palette of(String id, Component name, int... colors) {
        return new Palette(id, name, colors);
    }

    public static Palette chat() {
        int[] colors = new int[16];
        for (int i = 0; i < 16; i++) {
            colors[i] = ColorTables.chat(i).rgba();
        }
        return new Palette("chat", Component.translatable("saelibvie.palette.chat"), colors);
    }

    public static Palette dye() {
        DyeColor[] dyes = DyeColor.values();
        int[] colors = new int[Math.min(16, dyes.length)];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = 0xFF000000 | dyes[i].getTextColor();
        }
        return new Palette("dye", Component.translatable("saelibvie.palette.dye"), colors);
    }

    public static Palette nord() {
        return new Palette("nord", Component.translatable("saelibvie.palette.nord"), new int[]{
                0xFF2E3440, 0xFF3B4252, 0xFF434C5E, 0xFF4C566A, 0xFFD8DEE9, 0xFFE5E9F0, 0xFFECEFF4, 0xFF8FBCBB,
                0xFF88C0D0, 0xFF81A1C1, 0xFF5E81AC, 0xFFBF616A, 0xFFD08770, 0xFFEBCB8B, 0xFFA3BE8C, 0xFFB48EAD});
    }

    public static Palette reds() {
        return new Palette("reds", Component.translatable("saelibvie.palette.reds"), new int[]{
                0xFFFFCDD2, 0xFFEF9A9A, 0xFFE57373, 0xFFEF5350, 0xFFD32F2F, 0xFFB71C1C});
    }

    public static Palette greens() {
        return new Palette("greens", Component.translatable("saelibvie.palette.greens"), new int[]{
                0xFFC8E6C9, 0xFFA5D6A7, 0xFF81C784, 0xFF66BB6A, 0xFF388E3C, 0xFF1B5E20});
    }

    public static Palette blues() {
        return new Palette("blues", Component.translatable("saelibvie.palette.blues"), new int[]{
                0xFFBBDEFB, 0xFF90CAF9, 0xFF64B5F6, 0xFF42A5F5, 0xFF1976D2, 0xFF0D47A1});
    }

    public static Palette recent(int[] recents) {
        return new Palette("recent", Component.translatable("saelibvie.palette.recent"), recents);
    }

    public static List<Palette> defaults() {
        List<Palette> list = new ArrayList<>();
        list.add(chat());
        list.add(dye());
        list.add(nord());
        list.add(reds());
        list.add(greens());
        list.add(blues());
        return list;
    }
}
