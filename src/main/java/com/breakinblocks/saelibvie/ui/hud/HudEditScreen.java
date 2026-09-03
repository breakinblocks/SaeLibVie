package com.breakinblocks.saelibvie.ui.hud;

import com.breakinblocks.saelibvie.ui.behavior.DragBehavior;
import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.core.Widget;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.AnchorLayout;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.render.UiGraphics;
import com.breakinblocks.saelibvie.ui.screen.SaeScreen;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.widget.Button;
import com.breakinblocks.saelibvie.ui.widget.Label;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HudEditScreen extends SaeScreen {
    private final Map<ResourceLocation, HudRegistry.Placement> staged = new HashMap<>();
    private final Map<ResourceLocation, Boolean> stagedEnabled = new HashMap<>();
    private boolean committed;

    public HudEditScreen(@Nullable Screen parent) {
        super(Component.translatable("gui.saelibvie.hud.title"), parent);
        dimBackground(false);
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new HudEditScreen(mc.screen));
    }

    @Override
    protected void init() {
        HudRegistry.setEditing(true);
        super.init();
    }

    @Override
    protected void build(UiRoot root) {
        root.layout(AnchorLayout.INSTANCE);
        root.add(new Label(getTitle()).title().shadow(true), LayoutData.anchored(Anchor.TOP_CENTER, 0, 8));
        root.add(new Label(Component.translatable("gui.saelibvie.hud.hint")).dim().shadow(true), LayoutData.anchored(Anchor.TOP_CENTER, 0, 20));

        for (HudRegistry.Entry entry : HudRegistry.entries()) {
            root.add(new EditProxy(entry));
        }

        Panel buttons = new Panel().layout(LinearLayout.horizontal(4));
        buttons.add(new Button(Component.translatable("gui.saelibvie.hud.default"), () -> {
            staged.clear();
            stagedEnabled.clear();
            for (HudRegistry.Entry entry : HudRegistry.entries()) {
                staged.put(entry.key(), entry.defaults());
            }
            root.requestLayout();
        }).size(70, 16));
        buttons.add(new Button(Component.translatable("gui.saelibvie.hud.save"), () -> {
            for (HudRegistry.Entry entry : HudRegistry.entries()) {
                HudRegistry.Placement placement = staged.getOrDefault(entry.key(), entry.placement());
                Boolean enabled = stagedEnabled.get(entry.key());
                if (enabled != null) placement = placement.withEnabled(enabled);
                entry.setPlacement(placement);
            }
            HudRegistry.save();
            committed = true;
            onClose();
        }).size(70, 16));
        buttons.add(new Button(Component.translatable("gui.saelibvie.hud.cancel"), this::onClose).size(70, 16));
        buttons.packToContent();
        root.add(buttons, LayoutData.anchored(Anchor.BOTTOM_CENTER, 0, -10));
    }

    private HudRegistry.Placement placementFor(HudRegistry.Entry entry) {
        return staged.getOrDefault(entry.key(), entry.placement());
    }

    private boolean enabledFor(HudRegistry.Entry entry) {
        Boolean value = stagedEnabled.get(entry.key());
        return value != null ? value : placementFor(entry).enabled();
    }

    @Override
    public void onClose() {
        HudRegistry.setEditing(false);
        for (HudRegistry.Entry entry : HudRegistry.entries()) {
            HudElement element = entry.element();
            if (element.parent() != null) {
                element.parent().remove(element);
            }
            HudRegistry.hudRoot().add(element);
        }
        super.onClose();
    }

    private final class EditProxy extends Panel {
        private final HudRegistry.Entry entry;

        EditProxy(HudRegistry.Entry entry) {
            this.entry = entry;
            HudElement element = entry.element();
            element.setVisible(true);
            add(element);
            element.setPos(0, 0);
            element.setHudScale(placementFor(entry).scale());
            behavior(DragBehavior.create().clampToParent(true).onEnd(w -> commitPosition()));
            tooltip(() -> List.of(
                    Component.literal(entry.key().toString()),
                    Component.translatable(enabledFor(entry) ? "gui.saelibvie.hud.enabled" : "gui.saelibvie.hud.disabled")
                            .withStyle(enabledFor(entry) ? ChatFormatting.GREEN : ChatFormatting.RED),
                    Component.translatable("gui.saelibvie.hud.toggle_hint").withStyle(ChatFormatting.GRAY)));
            requestLayout();
        }

        private void commitPosition() {
            UiRoot root = root();
            if (root == null) return;
            HudRegistry.Placement current = placementFor(entry);
            HudRegistry.Placement updated = HudRegistry.Placement.fromPixels(current.anchor(), x(), y(), width(), height(),
                    root.screenWidth(), root.screenHeight(), current.scale(), enabledFor(entry));
            staged.put(entry.key(), updated);
        }

        @Override
        protected void beforeRender(UiGraphics g) {
            super.beforeRender(g);
            syncFromPlacement();
        }

        private void syncFromPlacement() {
            UiRoot root = root();
            if (root == null) return;
            HudElement element = entry.element();
            HudRegistry.Placement placement = placementFor(entry);
            Rect placed = placement.resolve(root.screenWidth(), root.screenHeight(), element.width(), element.height());
            int w = Math.max(placed.w(), 8);
            int h = Math.max(placed.h(), 8);
            if (!isDragging()) {
                setBounds(new Rect(placed.x(), placed.y(), w, h));
            } else {
                setSize(w, h);
            }
            element.setHudScale(placement.scale());
        }

        private boolean isDragging() {
            for (var behavior : behaviors()) {
                if (behavior.isActive(this)) return true;
            }
            return false;
        }

        @Override
        protected void paintBackground(UiGraphics g) {
            Rect r = localRect();
            boolean enabled = enabledFor(entry);
            g.fill(r, enabled ? entry.editColor() : 0x60303030);
            g.outline(r, isHovered() ? g.color(ColorToken.ACCENT) : 0xFFFFFFFF);
            if (!enabled) {
                g.text("off", 2, 2, g.color(ColorToken.NEGATIVE), true);
            }
        }

        @Override
        protected void paintChildren(UiGraphics g) {
            if (!enabledFor(entry)) {
                g.pushAlpha(0.35f);
                super.paintChildren(g);
                g.popAlpha();
                return;
            }
            super.paintChildren(g);
        }

        @Override
        public boolean mouseClicked(double lx, double ly, int button) {
            if (!isVisible()) return false;
            if (button == 1) {
                stagedEnabled.put(entry.key(), !enabledFor(entry));
                return true;
            }
            for (var behavior : behaviors()) {
                if (behavior.mouseClicked(this, lx, ly, button)) return true;
            }
            return true;
        }

        @Override
        protected boolean onMouseScrolled(double lx, double ly, double scrollX, double scrollY) {
            HudRegistry.Placement current = placementFor(entry);
            float scale = Math.max(0.25f, Math.min(3f, current.scale() + (scrollY > 0 ? 0.25f : -0.25f)));
            staged.put(entry.key(), current.withScale(scale));
            return true;
        }

        @Override
        public boolean contains(double lx, double ly) {
            return localRect().contains(lx, ly);
        }
    }

    @Override
    protected void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int count = HudRegistry.entries().size();
        String status = TextUtil.grouped(count) + " " + Component.translatable("gui.saelibvie.hud.elements").getString();
        graphics.drawString(font, status, 4, height - 12, 0xFFAAAAAA, true);
    }
}
