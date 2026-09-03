package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.AcceptsShiftEnter;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Insets;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FormWindow extends Panel implements AcceptsShiftEnter {
    public static final int BOTTOM_HEIGHT = 25;
    public static final int DEFAULT_TOP_HEIGHT = 20;

    private final Panel top;
    private final ScrollPanel main;
    private final Panel bottom;
    private final Button acceptButton;
    private final Button cancelButton;
    private final Button closeButton;
    private final SearchBox searchBox;
    private final Label titleLabel;
    private int topHeight = DEFAULT_TOP_HEIGHT;
    private boolean showBottom = true;
    private boolean showClose;
    private boolean showSearch;
    private Insets mainInset = Insets.NONE;
    @Nullable
    private Runnable onAccept;
    @Nullable
    private Runnable onCancel;

    public FormWindow(Component title) {
        chrome(Chrome.WINDOW);
        padding(0);
        top = new Panel();
        titleLabel = new Label(title).title().shadow(true);
        top.add(titleLabel);
        searchBox = new SearchBox();
        searchBox.setVisible(false);
        top.add(searchBox);
        closeButton = new Button(Component.literal("x"), this::cancel).flat(true)
                .tooltip(List.of(Component.translatable("gui.close"), TextUtil.hotkey("Esc")));
        closeButton.setVisible(false);
        top.add(closeButton);
        main = new ScrollPanel().reserveBarSpace(ScrollPanel.ReservePolicy.WHEN_SCROLLABLE);
        main.layout(LinearLayout.vertical(0));
        bottom = new Panel().layout(LinearLayout.horizontal(4).mainAlign(Align.END).crossAlign(Align.CENTER)).padding(Insets.symmetric(4, 0));
        acceptButton = new Button(Component.translatable("gui.accept"), this::accept)
                .tooltip(List.of(TextUtil.hotkey("Shift+Enter")));
        cancelButton = new Button(Component.translatable("gui.cancel"), this::cancel)
                .tooltip(List.of(TextUtil.hotkey("Esc")));
        bottom.add(acceptButton, LayoutData.create().size(60, 16));
        bottom.add(cancelButton, LayoutData.create().size(60, 16));
        add(top);
        add(main);
        add(bottom);
    }

    public Panel top() {
        return top;
    }

    public ScrollPanel main() {
        return main;
    }

    public Panel bottom() {
        return bottom;
    }

    public Button acceptButton() {
        return acceptButton;
    }

    public Button cancelButton() {
        return cancelButton;
    }

    public Label titleLabel() {
        return titleLabel;
    }

    public SearchBox searchBox() {
        return searchBox;
    }

    public FormWindow title(Component title) {
        titleLabel.text(title);
        return this;
    }

    public FormWindow topHeight(int height) {
        this.topHeight = height;
        requestLayout();
        return this;
    }

    public int topHeight() {
        return topHeight;
    }

    public FormWindow showBottom(boolean show) {
        this.showBottom = show;
        bottom.setVisible(show);
        requestLayout();
        return this;
    }

    public FormWindow showCloseButton(boolean show) {
        this.showClose = show;
        closeButton.setVisible(show);
        requestLayout();
        return this;
    }

    public FormWindow searchBox(boolean show) {
        this.showSearch = show;
        searchBox.setVisible(show);
        if (show) {
            searchBox.requestFocus();
        }
        requestLayout();
        return this;
    }

    public boolean hasSearchBox() {
        return showSearch;
    }

    public FormWindow mainInset(Insets inset) {
        this.mainInset = inset;
        requestLayout();
        return this;
    }

    public FormWindow onAccept(Runnable action) {
        this.onAccept = action;
        return this;
    }

    public FormWindow onCancel(Runnable action) {
        this.onCancel = action;
        return this;
    }

    public void accept() {
        if (onAccept != null) onAccept.run();
    }

    public void cancel() {
        if (onCancel != null) onCancel.run();
    }

    @Override
    public void onShiftEnter() {
        accept();
    }

    public int bottomHeight() {
        return showBottom ? BOTTOM_HEIGHT : 0;
    }

    public int effectiveTopHeight() {
        return topHeight + (showSearch ? 16 : 0);
    }

    @Override
    protected void onLayout() {
        Rect c = contentRect();
        int th = effectiveTopHeight();
        int bh = bottomHeight();
        top.setBounds(new Rect(c.x(), c.y(), c.w(), th));
        main.setBounds(new Rect(c.x() + mainInset.left(), c.y() + th + mainInset.top(),
                c.w() - mainInset.horizontal(), c.h() - th - bh - mainInset.vertical()));
        bottom.setBounds(new Rect(c.x(), c.bottom() - bh, c.w(), bh));
        int closeSize = 12;
        int titleY = (topHeight - 8) / 2;
        titleLabel.setBounds(new Rect(4, titleY, top.width() - 8 - (showClose ? closeSize + 2 : 0), 10));
        closeButton.setBounds(new Rect(top.width() - closeSize - 3, (topHeight - closeSize) / 2, closeSize, closeSize));
        searchBox.setBounds(new Rect(4, topHeight, top.width() - 8, 14));
        layoutTop(top);
    }

    protected void layoutTop(Panel top) {
    }

    @Override
    protected void onResized() {
        super.onResized();
        requestLayout();
    }

    @Override
    protected void paintBackground(UiGraphics g) {
        super.paintBackground(g);
        Rect c = contentRect();
        int th = effectiveTopHeight();
        g.fill(c.x(), c.y() + th - 1, c.right(), c.y() + th, g.color(ColorToken.BORDER_SOFT));
        if (showBottom) {
            int by = c.bottom() - bottomHeight();
            g.fill(c.x(), by, c.right(), by + 1, g.color(ColorToken.BORDER_SOFT));
        }
    }
}
