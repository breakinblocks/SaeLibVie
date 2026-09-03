package com.breakinblocks.saelibvie.ui.widget;

import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Function;

public class FilteredListPanel extends ScrollPanel {
    private Function<Widget, String> filterText = FilteredListPanel::defaultFilterText;
    private String query = "";

    public FilteredListPanel() {
        layout(LinearLayout.vertical(1));
        padding(1);
    }

    public static String defaultFilterText(Widget widget) {
        if (widget instanceof Button button) {
            Component label = button.currentLabel();
            return label == null ? "" : label.getString().toLowerCase(Locale.ROOT);
        }
        if (widget instanceof Label label) {
            return label.currentText().getString().toLowerCase(Locale.ROOT);
        }
        return "";
    }

    public FilteredListPanel filterText(Function<Widget, String> provider) {
        this.filterText = provider;
        applyFilter();
        return this;
    }

    public void setFilter(String text) {
        query = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        applyFilter();
        scrollToTop();
        requestLayout();
    }

    public String filter() {
        return query;
    }

    private void applyFilter() {
        for (Widget child : children) {
            child.setVisible(matches(child));
        }
    }

    private boolean matches(Widget child) {
        return query.isEmpty() || filterText.apply(child).contains(query);
    }

    @Override
    public <T extends Widget> T add(T child) {
        super.add(child);
        child.setVisible(matches(child));
        return child;
    }

    public int visibleCount() {
        int count = 0;
        for (Widget child : children) {
            if (child.isVisible()) count++;
        }
        return count;
    }

    public SearchBox connect(SearchBox box) {
        box.onSearch(this::setFilter);
        return box;
    }
}
