package com.breakinblocks.saelibvie.ui.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class MenuItem {
    public enum Kind {
        TITLE,
        SEPARATOR,
        ACTION,
        SUBMENU
    }

    private final Kind kind;
    private final Component text;
    @Nullable
    private final Consumer<Integer> action;
    @Nullable
    private final List<MenuItem> subItems;
    @Nullable
    private Identifier spriteIcon;
    @Nullable
    private ItemStack itemIcon;
    private boolean enabled = true;
    @Nullable
    private List<Component> tooltip;
    @Nullable
    private Component confirmQuestion;
    private boolean keepOpen;

    private MenuItem(Kind kind, Component text, @Nullable Consumer<Integer> action, @Nullable List<MenuItem> subItems) {
        this.kind = kind;
        this.text = text;
        this.action = action;
        this.subItems = subItems;
    }

    public static MenuItem title(Component text) {
        return new MenuItem(Kind.TITLE, text, null, null);
    }

    public static MenuItem separator() {
        return new MenuItem(Kind.SEPARATOR, Component.empty(), null, null);
    }

    public static MenuItem of(Component text, Runnable action) {
        return new MenuItem(Kind.ACTION, text, button -> action.run(), null);
    }

    public static MenuItem of(Component text, Consumer<Integer> action) {
        return new MenuItem(Kind.ACTION, text, action, null);
    }

    public static MenuItem subMenu(Component text, List<MenuItem> items) {
        return new MenuItem(Kind.SUBMENU, text, null, List.copyOf(items));
    }

    public MenuItem icon(Identifier sprite) {
        this.spriteIcon = sprite;
        this.itemIcon = null;
        return this;
    }

    public MenuItem icon(ItemStack stack) {
        this.itemIcon = stack;
        this.spriteIcon = null;
        return this;
    }

    public MenuItem enabled(boolean value) {
        this.enabled = value;
        return this;
    }

    public MenuItem tooltip(List<Component> lines) {
        this.tooltip = List.copyOf(lines);
        return this;
    }

    public MenuItem tooltip(Component line) {
        return tooltip(List.of(line));
    }

    public MenuItem confirm(Component question) {
        this.confirmQuestion = question;
        return this;
    }

    public MenuItem keepOpen() {
        this.keepOpen = true;
        return this;
    }

    public Kind kind() {
        return kind;
    }

    public Component text() {
        return text;
    }

    @Nullable
    public Consumer<Integer> action() {
        return action;
    }

    @Nullable
    public List<MenuItem> subItems() {
        return subItems;
    }

    @Nullable
    public Identifier spriteIcon() {
        return spriteIcon;
    }

    @Nullable
    public ItemStack itemIcon() {
        return itemIcon;
    }

    public boolean hasIcon() {
        return spriteIcon != null || (itemIcon != null && !itemIcon.isEmpty());
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public List<Component> tooltip() {
        return tooltip;
    }

    @Nullable
    public Component confirmQuestion() {
        return confirmQuestion;
    }

    public boolean keepsOpen() {
        return keepOpen;
    }

    public boolean isClickable() {
        return kind == Kind.ACTION || kind == Kind.SUBMENU;
    }
}
