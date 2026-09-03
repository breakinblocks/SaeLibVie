package com.breakinblocks.saelibvie.nbtedit;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.client.ClientUtil;
import com.breakinblocks.saelibvie.ui.core.EditSession;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.PositionedIngredient;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Insets;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.screen.FormScreen;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.util.UiSounds;
import com.breakinblocks.saelibvie.ui.widget.Button;
import com.breakinblocks.saelibvie.ui.widget.FormWindow;
import com.breakinblocks.saelibvie.ui.widget.Label;
import com.breakinblocks.saelibvie.ui.widget.TextField;
import com.breakinblocks.saelibvie.ui.widget.TextOverlay;
import com.breakinblocks.saelibvie.ui.widget.Toasts;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NbtEditScreen extends FormScreen {
    public static final int ROW_HEIGHT = 10;
    public static final int INDENT = 10;
    public static final int TOP_HEIGHT = 20;

    private final CompoundTag info;
    private final Node rootNode;
    private final BiConsumer<Boolean, CompoundTag> callback;
    private boolean accepted;
    private boolean finished;
    @Nullable
    private Node selected;
    private Panel tree;
    private Panel toolbar;

    public static void open(CompoundTag info, CompoundTag tag, @Nullable Screen parent, BiConsumer<Boolean, CompoundTag> callback) {
        Minecraft.getInstance().setScreen(new NbtEditScreen(info, tag, parent, callback));
    }

    public NbtEditScreen(CompoundTag info, CompoundTag tag, @Nullable Screen parent, BiConsumer<Boolean, CompoundTag> callback) {
        super(Component.literal(NbtEditInfo.rootLabel(info)), parent);
        this.info = info;
        this.callback = callback;
        this.rootNode = new Node(null, NbtEditInfo.rootLabel(info), tag.copy());
        this.rootNode.build();
        this.rootNode.collapseAll();
        this.rootNode.collapsed = false;
        this.selected = rootNode;
        pauses(true);
    }

    @Override
    protected Rect formBounds() {
        int w = Math.round(width * 0.75f);
        int h = Math.round(height * 0.9f);
        return new Rect((width - w) / 2, (height - h) / 2, w, h);
    }

    @Override
    protected void buildForm(FormWindow form) {
        form.topHeight(TOP_HEIGHT);
        form.titleLabel().setVisible(false);
        toolbar = new Panel().layout(LinearLayout.horizontal(4).crossAlign(Align.CENTER)).padding(Insets.symmetric(2, 2));
        form.top().add(toolbar);
        Panel right = new Panel().layout(LinearLayout.horizontal(4).crossAlign(Align.CENTER)).padding(Insets.symmetric(2, 2));
        right.add(new Button(Component.empty(), this::copySelected).itemIcon(new ItemStack(Items.PAPER)).size(16, 16)
                .tooltip(List.of(Component.literal("Copy"), TextUtil.hotkey("Ctrl+C"))));
        right.add(new Button(Component.literal("-"), () -> {
            rootNode.collapseAll();
            rebuildTree();
        }).size(16, 16).tooltip(List.of(Component.translatable("gui.collapse_all"), TextUtil.hotkey("-"))));
        right.add(new Button(Component.literal("+"), () -> {
            rootNode.expandAll();
            rebuildTree();
        }).size(16, 16).tooltip(List.of(Component.translatable("gui.expand_all"), TextUtil.hotkey("="), TextUtil.hotkey("+"))));
        right.packToContent();
        form.top().add(right.id("right"));
        tree = new Panel().layout(LinearLayout.vertical(0)).padding(2);
        form.main().add(tree, LayoutData.filled());
        rebuildToolbar();
        rebuildTree();
    }

    @Override
    protected void build(UiRoot root) {
        super.build(root);
        Widget right = form.top().find("right");
        if (right != null) {
            right.setBounds(new Rect(form.width() - right.width() - 4, 0, right.width(), TOP_HEIGHT));
        }
        toolbar.setBounds(new Rect(2, 0, form.width() - (right != null ? right.width() : 0) - 8, TOP_HEIGHT));
    }

    private void rebuildToolbar() {
        toolbar.clear();
        Node node = selected;
        toolbar.add(new Button(Component.literal("Del"), this::deleteSelected).enabledWhen(() -> selected != null && selected != rootNode).size(28, 16)
                .tooltip(List.of(Component.translatable("selectServer.delete"))));
        toolbar.add(new Button(Component.literal("Ren"), this::renameSelected).enabledWhen(() -> selected != null && selected.parent != null && selected.parent.tag instanceof CompoundTag).size(28, 16)
                .tooltip(List.of(Component.literal("Rename"))));
        toolbar.add(new Button(Component.literal("Edit"), this::editSelected).enabledWhen(() -> selected != null && selected.isLeaf()).size(30, 16)
                .tooltip(List.of(Component.literal("Edit value"))));
        if (node != null) {
            List<Integer> types = node.acceptedTypes();
            if (!types.isEmpty()) {
                toolbar.add(new Label(Component.literal("Add")).dim().size(20, 10));
                for (int type : types) {
                    int tagType = type;
                    toolbar.add(new Button(Component.empty(), () -> addChild(tagType)).icon(spriteFor(tagType, false)).iconSize(8).size(14, 14)
                            .tooltip(List.of(Component.literal(typeName(tagType)))));
                }
            }
        }
        toolbar.requestLayout();
    }

    private void rebuildTree() {
        tree.clear();
        List<Node> visible = new ArrayList<>();
        rootNode.collectVisible(visible);
        for (Node node : visible) {
            tree.add(new Row(node), LayoutData.filled().height(ROW_HEIGHT));
        }
        tree.requestLayout();
    }

    private void select(@Nullable Node node) {
        selected = node;
        rebuildToolbar();
    }

    public static ResourceLocation spriteFor(int type, boolean open) {
        String name = switch (type) {
            case Tag.TAG_BYTE -> "byte";
            case Tag.TAG_SHORT -> "short";
            case Tag.TAG_INT -> "int";
            case Tag.TAG_LONG -> "long";
            case Tag.TAG_FLOAT -> "float";
            case Tag.TAG_DOUBLE -> "double";
            case Tag.TAG_STRING -> "string";
            case Tag.TAG_LIST -> open ? "list_open" : "list";
            case Tag.TAG_COMPOUND -> open ? "compound_open" : "compound";
            case Tag.TAG_BYTE_ARRAY -> open ? "byte_array_open" : "byte_array";
            case Tag.TAG_INT_ARRAY -> open ? "int_array_open" : "int_array";
            default -> "double";
        };
        return SaeLibVie.id("nbt/" + name);
    }

    public static String typeName(int type) {
        return switch (type) {
            case Tag.TAG_BYTE -> "Byte";
            case Tag.TAG_SHORT -> "Short";
            case Tag.TAG_INT -> "Int";
            case Tag.TAG_LONG -> "Long";
            case Tag.TAG_FLOAT -> "Float";
            case Tag.TAG_DOUBLE -> "Double";
            case Tag.TAG_STRING -> "String";
            case Tag.TAG_LIST -> "List";
            case Tag.TAG_COMPOUND -> "Compound";
            case Tag.TAG_BYTE_ARRAY -> "Byte Array";
            case Tag.TAG_INT_ARRAY -> "Int Array";
            case Tag.TAG_LONG_ARRAY -> "Long Array";
            default -> "Tag";
        };
    }

    private static Tag defaultTag(int type) {
        return switch (type) {
            case Tag.TAG_BYTE -> ByteTag.valueOf((byte) 0);
            case Tag.TAG_SHORT -> ShortTag.valueOf((short) 0);
            case Tag.TAG_INT -> IntTag.valueOf(0);
            case Tag.TAG_LONG -> LongTag.valueOf(0L);
            case Tag.TAG_FLOAT -> FloatTag.valueOf(0F);
            case Tag.TAG_DOUBLE -> DoubleTag.valueOf(0D);
            case Tag.TAG_STRING -> StringTag.valueOf("");
            case Tag.TAG_LIST -> new ListTag();
            case Tag.TAG_COMPOUND -> new CompoundTag();
            case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(new byte[0]);
            case Tag.TAG_INT_ARRAY -> new IntArrayTag(new int[0]);
            default -> new CompoundTag();
        };
    }

    private void deleteSelected() {
        Node node = selected;
        if (node == null || node == rootNode || node.parent == null) return;
        UiSounds.click();
        Node parent = node.parent;
        parent.removeChild(node);
        select(parent);
        rebuildTree();
    }

    private void renameSelected() {
        Node node = selected;
        if (node == null || node.parent == null || !(node.parent.tag instanceof CompoundTag)) return;
        Node parent = node.parent;
        TextField field = new TextField().maxLength(256).validator(s -> !s.isEmpty());
        TextOverlay overlay = new TextOverlay(Component.literal("New name"), field, node.key);
        overlay.onAccept(name -> {
            if (name.isEmpty()) return;
            CompoundTag compound = (CompoundTag) parent.tag;
            Tag value = compound.get(node.key);
            compound.remove(node.key);
            if (value != null) compound.put(name, value);
            parent.build();
            select(parent.child(name));
            rebuildTree();
        });
        Widget anchor = toolbar.children().size() > 1 ? toolbar.children().get(1) : toolbar;
        overlay.open(root, anchor, 0, anchor.height(), EditSession.of(a -> {
        }));
    }

    private void editSelected() {
        Node node = selected;
        if (node == null || !node.isLeaf()) return;
        editLeaf(node);
    }

    private void editLeaf(Node node) {
        Tag tag = node.tag;
        TextField field = new TextField().maxLength(1024);
        String initial;
        Function<String, Object> parser;
        if (tag instanceof StringTag) {
            initial = tag.getAsString();
            parser = text -> text;
        } else if (tag instanceof LongTag) {
            initial = Long.toString(((NumericTag) tag).getAsLong());
            parser = NbtEditScreen::parseLong;
            field.validator(text -> parseLong(text) != null);
        } else if (tag instanceof FloatTag || tag instanceof DoubleTag) {
            initial = Double.toString(((NumericTag) tag).getAsDouble());
            parser = NbtEditScreen::parseDouble;
            field.validator(text -> parseDouble(text) != null);
        } else if (tag instanceof NumericTag numeric) {
            initial = Integer.toString(numeric.getAsInt());
            parser = NbtEditScreen::parseLong;
            field.validator(text -> parseLong(text) != null);
        } else {
            return;
        }
        TextOverlay overlay = new TextOverlay(null, field, initial);
        overlay.onAccept(text -> {
            Tag written = rewrite(tag, parser.apply(text));
            if (written != null && node.parent != null) {
                node.parent.setChild(node, written);
                rebuildTree();
            }
        });
        overlay.openAtMouse(root, EditSession.of(a -> {
        }));
    }

    @Nullable
    private static Long parseLong(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.equals("-") || trimmed.equals("+")) return 0L;
        try {
            return Long.decode(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private static Double parseDouble(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.equals("-") || trimmed.equals("+")) return 0D;
        if (trimmed.equals("+Inf")) return Double.POSITIVE_INFINITY;
        if (trimmed.equals("-Inf")) return Double.NEGATIVE_INFINITY;
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private static Tag rewrite(Tag original, @Nullable Object parsed) {
        if (parsed == null) return null;
        if (original instanceof StringTag) return StringTag.valueOf(String.valueOf(parsed));
        if (parsed instanceof Number number) {
            if (original instanceof ByteTag) return ByteTag.valueOf((byte) clamp(number.longValue(), Byte.MIN_VALUE, Byte.MAX_VALUE));
            if (original instanceof ShortTag) return ShortTag.valueOf((short) clamp(number.longValue(), Short.MIN_VALUE, Short.MAX_VALUE));
            if (original instanceof IntTag) return IntTag.valueOf((int) clamp(number.longValue(), Integer.MIN_VALUE, Integer.MAX_VALUE));
            if (original instanceof LongTag) return LongTag.valueOf(number.longValue());
            if (original instanceof FloatTag) return FloatTag.valueOf((float) number.doubleValue());
            if (original instanceof DoubleTag) return DoubleTag.valueOf(number.doubleValue());
        }
        return null;
    }

    private static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(max, v));
    }

    private void addChild(int type) {
        Node node = selected;
        if (node == null) return;
        UiSounds.click();
        if (node.tag instanceof CompoundTag compound) {
            TextField field = new TextField().maxLength(256).validator(s -> !s.isEmpty());
            TextOverlay overlay = new TextOverlay(Component.literal("New name"), field, "");
            overlay.onAccept(name -> {
                if (name.isEmpty()) return;
                compound.put(name, defaultTag(type));
                node.build();
                node.collapsed = false;
                select(node.child(name));
                rebuildTree();
            });
            overlay.open(root, toolbar, 0, toolbar.height(), EditSession.of(a -> {
            }));
            return;
        }
        node.append(defaultTag(type));
        node.collapsed = false;
        rebuildTree();
    }

    private void copySelected() {
        Node node = selected;
        if (node == null) return;
        Tag tag = node.tag.copy();
        if (node == rootNode && tag instanceof CompoundTag compound) {
            List<Component> lines = NbtEditInfo.readLines(info);
            if (!lines.isEmpty()) {
                ListTag list = new ListTag();
                for (Component line : lines) list.add(StringTag.valueOf(line.getString()));
                compound.put("_", list);
            }
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(tag.toString());
        Toasts.info(Component.literal("NBT copied to clipboard"), Component.empty());
    }

    @Override
    protected void onAccept() {
        accepted = true;
        close();
    }

    @Override
    protected void onCancel() {
        close();
    }

    @Override
    public void close() {
        if (!finished) {
            finished = true;
            callback.accept(accepted, (CompoundTag) rootNode.tag);
        }
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (root.focused() != null) return false;
        if (keyCode == InputConstants.KEY_EQUALS || keyCode == InputConstants.KEY_ADD) {
            rootNode.expandAll();
            rebuildTree();
            return true;
        }
        if (keyCode == InputConstants.KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            rootNode.collapseAll();
            rebuildTree();
            return true;
        }
        if (Screen.isCopy(keyCode)) {
            copySelected();
            return true;
        }
        return false;
    }

    final class Node {
        @Nullable
        final Node parent;
        String key;
        Tag tag;
        final List<Node> children = new ArrayList<>();
        boolean collapsed;
        @Nullable
        ItemStack itemStack;

        Node(@Nullable Node parent, String key, Tag tag) {
            this.parent = parent;
            this.key = key;
            this.tag = tag;
        }

        boolean isLeaf() {
            return !(tag instanceof CompoundTag) && !(tag instanceof CollectionTag<?>);
        }

        int depth() {
            int d = 0;
            Node n = parent;
            while (n != null) {
                d++;
                n = n.parent;
            }
            return d;
        }

        void build() {
            children.clear();
            itemStack = null;
            if (tag instanceof CompoundTag compound) {
                List<String> keys = new ArrayList<>(compound.getAllKeys());
                keys.sort(String.CASE_INSENSITIVE_ORDER);
                for (String k : keys) {
                    Node child = new Node(this, k, compound.get(k));
                    child.build();
                    children.add(child);
                }
                if (compound.contains("id", Tag.TAG_STRING) && compound.contains("count", Tag.TAG_ANY_NUMERIC)) {
                    try {
                        Optional<ItemStack> parsed = ItemStack.parse(ClientUtil.registryAccess(), compound);
                        parsed.ifPresent(stack -> {
                            if (!stack.isEmpty()) {
                                itemStack = stack;
                                collapsed = true;
                            }
                        });
                    } catch (Exception ignored) {
                    }
                }
            } else if (tag instanceof ListTag list) {
                for (int i = 0; i < list.size(); i++) {
                    Node child = new Node(this, Integer.toString(i), list.get(i));
                    child.build();
                    children.add(child);
                }
            } else if (tag instanceof ByteArrayTag bytes) {
                byte[] array = bytes.getAsByteArray();
                for (int i = 0; i < array.length; i++) {
                    children.add(new Node(this, Integer.toString(i), ByteTag.valueOf(array[i])));
                }
            } else if (tag instanceof IntArrayTag ints) {
                int[] array = ints.getAsIntArray();
                for (int i = 0; i < array.length; i++) {
                    children.add(new Node(this, Integer.toString(i), IntTag.valueOf(array[i])));
                }
            }
        }

        @Nullable
        Node child(String childKey) {
            for (Node child : children) {
                if (child.key.equals(childKey)) return child;
            }
            return null;
        }

        void collectVisible(List<Node> out) {
            out.add(this);
            if (collapsed) return;
            for (Node child : children) {
                child.collectVisible(out);
            }
        }

        void collapseAll() {
            if (!isLeaf()) collapsed = true;
            for (Node child : children) child.collapseAll();
        }

        void expandAll() {
            collapsed = false;
            for (Node child : children) child.expandAll();
        }

        List<Integer> acceptedTypes() {
            List<Integer> all = List.of((int) Tag.TAG_COMPOUND, (int) Tag.TAG_LIST, (int) Tag.TAG_STRING, (int) Tag.TAG_BYTE, (int) Tag.TAG_SHORT,
                    (int) Tag.TAG_INT, (int) Tag.TAG_LONG, (int) Tag.TAG_FLOAT, (int) Tag.TAG_DOUBLE, (int) Tag.TAG_BYTE_ARRAY, (int) Tag.TAG_INT_ARRAY);
            if (tag instanceof CompoundTag) {
                return all;
            }
            if (tag instanceof ListTag list) {
                if (list.isEmpty()) {
                    return all;
                }
                return List.of((int) list.getElementType());
            }
            if (tag instanceof ByteArrayTag) return List.of((int) Tag.TAG_BYTE);
            if (tag instanceof IntArrayTag) return List.of((int) Tag.TAG_INT);
            return List.of();
        }

        void propagate() {
            if (parent != null) {
                parent.setChildTag(key, tag);
            }
        }

        void setChildTag(String childKey, Tag value) {
            if (tag instanceof CompoundTag compound) {
                compound.put(childKey, value);
            } else if (tag instanceof ListTag list) {
                int index = Integer.parseInt(childKey);
                if (index >= 0 && index < list.size()) list.set(index, value);
            } else if (tag instanceof ByteArrayTag bytes) {
                byte[] array = bytes.getAsByteArray().clone();
                int index = Integer.parseInt(childKey);
                if (index >= 0 && index < array.length && value instanceof NumericTag n) array[index] = n.getAsByte();
                tag = new ByteArrayTag(array);
            } else if (tag instanceof IntArrayTag ints) {
                int[] array = ints.getAsIntArray().clone();
                int index = Integer.parseInt(childKey);
                if (index >= 0 && index < array.length && value instanceof NumericTag n) array[index] = n.getAsInt();
                tag = new IntArrayTag(array);
            }
            propagate();
        }

        void setChild(Node child, Tag value) {
            child.tag = value;
            setChildTag(child.key, value);
        }

        void removeChild(Node child) {
            if (tag instanceof CompoundTag compound) {
                compound.remove(child.key);
            } else if (tag instanceof ListTag list) {
                int index = Integer.parseInt(child.key);
                if (index >= 0 && index < list.size()) list.remove(index);
            } else if (tag instanceof ByteArrayTag bytes) {
                byte[] array = bytes.getAsByteArray();
                int index = Integer.parseInt(child.key);
                byte[] next = new byte[Math.max(0, array.length - 1)];
                for (int i = 0, j = 0; i < array.length; i++) {
                    if (i != index) next[j++] = array[i];
                }
                tag = new ByteArrayTag(next);
            } else if (tag instanceof IntArrayTag ints) {
                int[] array = ints.getAsIntArray();
                int index = Integer.parseInt(child.key);
                int[] next = new int[Math.max(0, array.length - 1)];
                for (int i = 0, j = 0; i < array.length; i++) {
                    if (i != index) next[j++] = array[i];
                }
                tag = new IntArrayTag(next);
            }
            propagate();
            build();
        }

        void append(Tag value) {
            if (tag instanceof ListTag list) {
                list.add(value);
            } else if (tag instanceof ByteArrayTag bytes) {
                byte[] array = bytes.getAsByteArray();
                byte[] next = Arrays.copyOf(array, array.length + 1);
                next[array.length] = value instanceof NumericTag n ? n.getAsByte() : 0;
                tag = new ByteArrayTag(next);
            } else if (tag instanceof IntArrayTag ints) {
                int[] array = ints.getAsIntArray();
                int[] next = Arrays.copyOf(array, array.length + 1);
                next[array.length] = value instanceof NumericTag n ? n.getAsInt() : 0;
                tag = new IntArrayTag(next);
            }
            propagate();
            build();
        }

        String valueText() {
            if (tag instanceof ByteTag || tag instanceof ShortTag || tag instanceof IntTag) return Integer.toString(((NumericTag) tag).getAsInt());
            if (tag instanceof LongTag) return Long.toString(((NumericTag) tag).getAsLong());
            if (tag instanceof FloatTag || tag instanceof DoubleTag) return Double.toString(((NumericTag) tag).getAsDouble());
            return tag.getAsString();
        }
    }

    final class Row extends Widget {
        private final Node node;

        Row(Node node) {
            this.node = node;
            setSize(100, ROW_HEIGHT);
            if (node == rootNode) {
                List<Component> lines = NbtEditInfo.readLines(info);
                if (!lines.isEmpty()) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.literal("Info:").withStyle(ChatFormatting.AQUA));
                    tooltip.addAll(lines);
                    tooltip(tooltip);
                }
            }
        }

        private int indent() {
            return node.depth() * INDENT;
        }

        @Override
        public Optional<PositionedIngredient> ingredientUnderMouse() {
            if (node.itemStack == null) return Optional.empty();
            return PositionedIngredient.optional(node.itemStack, this);
        }

        @Override
        protected void paint(UiGraphics g) {
            int x = indent();
            boolean isSelected = node == selected;
            if (isSelected) {
                g.fill(x, 0, width(), height(), 0x40FFFFFF);
            }
            g.fill(x + 1, 1, x + 9, 9, 0x60808080);
            g.sprite(spriteFor(node.tag.getId(), !node.collapsed), x + 1, 1, 8, 8);
            int textX = x + 11;
            int textY = 1;
            if (node.tag instanceof CompoundTag) {
                int color = isSelected ? 0xFF55FF55 : 0xFF00AA00;
                g.text(node.key, textX, textY, color, false);
                if (node.itemStack != null) {
                    int ix = textX + g.textWidth(node.key) + 2;
                    g.pushTransform(ix, 1, 0.5f);
                    g.item(node.itemStack, 0, 0);
                    g.popTransform();
                }
            } else if (node.tag instanceof ListTag) {
                g.text(node.key, textX, textY, isSelected ? 0xFFFFFF55 : 0xFFFFAA00, false);
            } else if (node.tag instanceof ByteArrayTag || node.tag instanceof IntArrayTag) {
                g.text(node.key, textX, textY, isSelected ? 0xFFFFFF55 : 0xFFFFAA00, false);
            } else {
                int keyColor = isSelected ? 0xFFFFFFFF : 0xFFAAAAAA;
                int valueColor = isSelected ? 0xFF55FFFF : 0xFF00AAAA;
                g.text(node.key + ": ", textX, textY, keyColor, false);
                g.text(node.valueText(), textX + g.textWidth(node.key + ": "), textY, valueColor, false);
            }
        }

        private void toggle() {
            node.collapsed = !node.collapsed;
            rebuildTree();
        }

        @Override
        protected boolean onMouseClicked(double lx, double ly, int button) {
            if (!node.isLeaf()) {
                if (lx >= indent() && lx < indent() + ROW_HEIGHT) {
                    toggle();
                } else {
                    select(node);
                }
                return true;
            }
            select(node);
            if (button == 1) {
                editLeaf(node);
            }
            return true;
        }

        @Override
        protected boolean onMouseDoubleClicked(double lx, double ly, int button) {
            if (!node.isLeaf()) {
                toggle();
            } else {
                select(node);
                editLeaf(node);
            }
            return true;
        }
    }
}
