package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.geom.Size;
import com.breakinblocks.saelibvie.ui.render.Painter;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;

public class SlotDecor extends Widget {
    private final int columns;
    private final int rows;
    private final int pitch;

    public SlotDecor(int slotX, int slotY, int columns, int rows) {
        this(slotX, slotY, columns, rows, 18);
    }

    public SlotDecor(int slotX, int slotY, int columns, int rows, int pitch) {
        super(new Rect(slotX - 1, slotY - 1, columns * pitch, rows * pitch));
        this.columns = columns;
        this.rows = rows;
        this.pitch = pitch;
    }

    public static SlotDecor playerInventory(int slotX, int slotY) {
        return new SlotDecor(slotX, slotY, 9, 3);
    }

    public static SlotDecor hotbar(int slotX, int slotY) {
        return new SlotDecor(slotX, slotY, 9, 1);
    }

    @Override
    protected Size measure() {
        return new Size(columns * pitch, rows * pitch);
    }

    @Override
    protected void paint(UiGraphics g) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                Painter.slot(g, col * pitch, row * pitch, pitch);
            }
        }
    }
}
