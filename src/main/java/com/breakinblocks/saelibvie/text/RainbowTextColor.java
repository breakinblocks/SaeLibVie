package com.breakinblocks.saelibvie.text;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.color.Color;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLEnvironment;

public final class RainbowTextColor extends CustomTextColor {
    public static final String ID = SaeLibVie.MOD_ID + ":rainbow";
    public static final RainbowTextColor INSTANCE = new RainbowTextColor();
    private static int[] table;

    private RainbowTextColor() {
        super(ID);
    }

    public static int[] table() {
        if (table == null) {
            int[] values = new int[255];
            for (int i = 0; i < 255; i++) {
                values[i] = Color.HSBtoRGB(i / 255F, 0.8F, 1F) & 0xFFFFFF;
            }
            table = values;
        }
        return table;
    }

    public static int currentRgb() {
        if (!FMLEnvironment.dist.isClient()) return 0xFFFFFF;
        return ClientPhase.rgb();
    }

    @Override
    public int getValue() {
        return currentRgb();
    }

    private static final class ClientPhase {
        static int rgb() {
            Minecraft mc = Minecraft.getInstance();
            long tick = mc == null ? 0L : mc.clientTickCount;
            return table()[(int) Math.floorMod(tick * 2L, 255L)];
        }
    }
}
