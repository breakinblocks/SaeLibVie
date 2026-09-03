package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.AcceptsShiftEnter;
import com.breakinblocks.saelibvie.ui.core.EditSession;
import com.breakinblocks.saelibvie.ui.core.LayerOptions;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TextAreaOverlay extends Panel implements AcceptsShiftEnter {
    public static final int BUTTON_SIZE = 16;

    private final TextArea area;
    private final ScrollPanel scroll;
    @Nullable
    private EditSession session;
    @Nullable
    private Consumer<String> onAccept;

    public TextAreaOverlay(String initialText) {
        chrome(Chrome.PANEL);
        padding(1);
        area = new TextArea().escapeCancels(true);
        area.background(false);
        scroll = new ScrollPanel().padding(1);
        scroll.layout(LinearLayout.vertical(0));
        scroll.add(area, LayoutData.filled());
        add(scroll);
        Button accept = new Button(Component.literal("✓"), this::accept)
                .tooltip(List.of(Component.translatable("gui.accept"), TextUtil.hotkey("Shift+Enter")));
        Button cancel = new Button(Component.literal("✗"), this::cancel)
                .tooltip(List.of(Component.translatable("gui.cancel"), TextUtil.hotkey("Esc")));
        add(accept.id("accept"));
        add(cancel.id("cancel"));
        setSize(200, defaultHeight());
        area.setValue(initialText);
    }

    public static int defaultHeight() {
        return (TextUtil.font().lineHeight + 1) * 4;
    }

    public TextAreaOverlay onAccept(Consumer<String> consumer) {
        this.onAccept = consumer;
        return this;
    }

    public TextArea area() {
        return area;
    }

    @Override
    protected void onLayout() {
        int inset = 2;
        Rect r = localRect();
        scroll.setBounds(new Rect(inset, inset, r.w() - inset * 2 - BUTTON_SIZE - 3, r.h() - inset * 2));
        area.setSize(scroll.contentRect().w(), area.preferredSize().h());
        find("accept").setBounds(new Rect(r.right() - inset - BUTTON_SIZE, inset, BUTTON_SIZE, BUTTON_SIZE));
        find("cancel").setBounds(new Rect(r.right() - inset - BUTTON_SIZE, inset + BUTTON_SIZE + 2, BUTTON_SIZE, BUTTON_SIZE));
    }

    @Override
    protected void onResized() {
        super.onResized();
        requestLayout();
    }

    public void open(UiRoot root, Rect bounds, EditSession session) {
        this.session = session;
        setBounds(bounds);
        root.pushLayer(this, LayerOptions.modal().scrim(false).session(session));
        area.requestFocus();
    }

    public void accept() {
        if (onAccept != null) onAccept.accept(area.value());
        if (session != null) session.accept();
        remove();
    }

    public void cancel() {
        if (session != null) session.cancel();
        remove();
    }

    private void remove() {
        UiRoot root = root();
        if (root != null) root.removeLayer(this);
    }

    @Override
    public void onShiftEnter() {
        accept();
    }
}
