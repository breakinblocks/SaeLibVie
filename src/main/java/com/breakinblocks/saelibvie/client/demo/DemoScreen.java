package com.breakinblocks.saelibvie.client.demo;

import com.breakinblocks.saelibvie.ui.color.ColorToken;
import com.breakinblocks.saelibvie.ui.color.Themes;
import com.breakinblocks.saelibvie.ui.core.LayoutData;
import com.breakinblocks.saelibvie.ui.core.Panel;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Align;
import com.breakinblocks.saelibvie.ui.geom.Anchor;
import com.breakinblocks.saelibvie.ui.geom.Axis;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.layout.AnchorLayout;
import com.breakinblocks.saelibvie.ui.layout.FlowLayout;
import com.breakinblocks.saelibvie.ui.layout.GridLayout;
import com.breakinblocks.saelibvie.ui.layout.LinearLayout;
import com.breakinblocks.saelibvie.ui.screen.SaeScreen;
import com.breakinblocks.saelibvie.ui.util.TextUtil;
import com.breakinblocks.saelibvie.ui.widget.Button;
import com.breakinblocks.saelibvie.ui.widget.Checkbox;
import com.breakinblocks.saelibvie.ui.widget.ConfirmButton;
import com.breakinblocks.saelibvie.ui.widget.CycleButton;
import com.breakinblocks.saelibvie.ui.widget.Dropdown;
import com.breakinblocks.saelibvie.ui.widget.EntityView;
import com.breakinblocks.saelibvie.ui.widget.FluidGauge;
import com.breakinblocks.saelibvie.ui.widget.Graph;
import com.breakinblocks.saelibvie.ui.widget.ItemView;
import com.breakinblocks.saelibvie.ui.widget.KeyValueRows;
import com.breakinblocks.saelibvie.ui.widget.Label;
import com.breakinblocks.saelibvie.ui.widget.ListView;
import com.breakinblocks.saelibvie.ui.widget.NumberStepper;
import com.breakinblocks.saelibvie.ui.widget.PagedGrid;
import com.breakinblocks.saelibvie.ui.widget.ProgressBar;
import com.breakinblocks.saelibvie.ui.widget.ScrollPanel;
import com.breakinblocks.saelibvie.ui.widget.SegmentedControl;
import com.breakinblocks.saelibvie.ui.widget.Separator;
import com.breakinblocks.saelibvie.ui.widget.Slider;
import com.breakinblocks.saelibvie.ui.widget.SlotDecor;
import com.breakinblocks.saelibvie.ui.widget.StatBar;
import com.breakinblocks.saelibvie.ui.widget.TabPanel;
import com.breakinblocks.saelibvie.ui.widget.TextBlock;
import com.breakinblocks.saelibvie.ui.widget.TextField;
import com.breakinblocks.saelibvie.ui.widget.ToggleButton;
import com.breakinblocks.saelibvie.ui.widget.Viewport3D;
import com.breakinblocks.saelibvie.ui.widget.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DemoScreen extends SaeScreen {
    private enum Mode {
        WHITELIST,
        BLACKLIST,
        IGNORE
    }

    private float progress;
    private int fluidAmount = 3500;
    private int windowCount;
    private boolean toggleState;
    private Mode mode = Mode.WHITELIST;
    private double sliderValue = 40;
    private int stepperValue = 16;
    private String typed = "";
    @Nullable
    private Window mainWindow;

    public DemoScreen(@Nullable Screen parent) {
        super(Component.translatable("gui.saelibvie.demo.title"), parent);
    }

    @Override
    protected void build(UiRoot root) {
        root.layout(AnchorLayout.INSTANCE);
        Window main = new Window(getTitle()).resizable(240, 160, 600, 400);
        main.setBounds(new Rect(0, 0, Math.min(width - 20, 340), Math.min(height - 20, 230)));
        mainWindow = main;

        TabPanel tabs = new TabPanel();
        tabs.addTab(Component.translatable("gui.saelibvie.demo.controls"), buildControls());
        tabs.addTab(Component.translatable("gui.saelibvie.demo.data"), buildData());
        tabs.addTab(Component.translatable("gui.saelibvie.demo.lists"), buildLists());
        tabs.addTab(Component.translatable("gui.saelibvie.demo.three_d"), build3d());
        tabs.addTab(Component.translatable("gui.saelibvie.demo.layout"), buildLayout());
        main.add(tabs, LayoutData.filled());
        openCentered(main);
    }

    private Panel buildControls() {
        Panel page = new Panel().layout(LinearLayout.vertical(4)).padding(4);
        Panel row1 = new Panel().layout(FlowLayout.create(4, 4));
        row1.add(new Button(Component.literal("Button"), () -> progress = 0f).size(60, 14));
        row1.add(new ToggleButton(Component.literal("On"), Component.literal("Off")).bind(() -> toggleState, v -> toggleState = v).size(50, 14));
        row1.add(CycleButton.ofEnum(Mode.class, m -> Component.literal(TextUtil.prettify(m.name().toLowerCase(Locale.ROOT))))
                .bind(() -> mode, m -> mode = m).size(80, 14));
        row1.add(new ConfirmButton(Component.literal("Delete"), Component.literal("Confirm?"), () -> stepperValue = 0).size(60, 14));
        row1.add(new Button(Component.translatable("gui.saelibvie.demo.new_window"), this::spawnWindow).size(80, 14));
        row1.add(new Button(Component.translatable("gui.saelibvie.demo.modal"), this::openModal).size(80, 14));
        row1.preferredSize(300, 36);
        page.add(row1, LayoutData.filled().height(36));

        Panel row2 = new Panel().layout(LinearLayout.horizontal(6));
        row2.add(new Checkbox(Component.literal("Checkbox"), true, v -> {
        }).size(80, 12));
        row2.add(SegmentedControl.ofEnum(Mode.class, m -> Component.literal(m.name().substring(0, 1) + m.name().substring(1).toLowerCase(Locale.ROOT)))
                .bind(() -> mode, m -> mode = m).size(150, 14));
        page.add(row2, LayoutData.filled().height(14));

        page.add(new Slider(0, 100, sliderValue, v -> sliderValue = v).step(1).label(Component.literal("Slider")).size(200, 12), LayoutData.create().width(200).height(12));
        page.add(new NumberStepper(0, 999, stepperValue, v -> stepperValue = v).steps(1, 10, 64).size(90, 14), LayoutData.create().width(90).height(14));

        Panel row3 = new Panel().layout(LinearLayout.horizontal(6));
        row3.add(new TextField().hint(Component.literal("Type here")).maxLength(32).onCommit(v -> typed = v).size(120, 14));
        row3.add(new Dropdown<>(Themes::ids, id -> Component.literal(id.getPath()))
                .bind(Themes::getDefaultId, id -> {
                    Themes.setDefault(id);
                    root.setTheme(Themes.getDefault());
                }).size(100, 14));
        page.add(row3, LayoutData.filled().height(14));

        page.add(new Label(() -> Component.literal("Committed: " + typed + "  mode=" + mode + "  slider=" + Math.round(sliderValue) + "  stepper=" + stepperValue)).dim(), LayoutData.filled().height(10));
        page.add(new Slider(0.2, 1.0, 1.0, v -> {
            if (mainWindow != null) mainWindow.alpha((float) v);
        }).step(0.05).label(Component.translatable("gui.saelibvie.demo.opacity")).formatter(v -> Component.literal(TextUtil.percent(v))).size(140, 12), LayoutData.create().width(140).height(12));
        return page;
    }

    private Panel buildData() {
        Panel page = new Panel().layout(LinearLayout.horizontal(6)).padding(4);
        Panel left = new Panel().layout(LinearLayout.vertical(4));
        left.add(new ProgressBar(() -> progress).size(120, 9));
        left.add(new ProgressBar(() -> progress).vertical().size(12, 40));
        left.add(new StatBar(() -> progress, 0xFF7FD46B).label(Component.literal("Experience")).size(120, 16));
        left.add(new StatBar(() -> 1f - progress, 0xFFE0556A).label(Component.literal("Health")).size(120, 16));
        left.add(new KeyValueRows()
                .row(Component.literal("Speed"), () -> Component.literal(TextUtil.decimal(progress * 120, 1) + " RPM"))
                .row(Component.literal("Torque"), () -> Component.literal(TextUtil.decimal(40 + progress * 10, 1) + " Nm"))
                .row(Component.literal("Status"), () -> Component.literal(progress > 0.5f ? "Running" : "Idle"), () -> progress > 0.5f ? root.theme().color(ColorToken.POSITIVE) : root.theme().color(ColorToken.NEGATIVE))
                .size(120, 27));
        page.add(left, LayoutData.weighted(1f).fill(true));

        Panel right = new Panel().layout(LinearLayout.vertical(4));
        right.add(new Graph(() -> progress * 100f).size(90, 32));
        right.add(new FluidGauge(() -> new FluidStack(Fluids.WATER, fluidAmount), 8000).size(16, 40));
        right.add(new Label(() -> Component.literal(TextUtil.abbreviate(fluidAmount * 137L))).dim());
        page.add(right, LayoutData.create().width(90).fill(true));
        return page;
    }

    private Panel buildLists() {
        Panel page = new Panel().layout(LinearLayout.horizontal(6)).padding(4);
        List<String> entries = new ArrayList<>();
        for (int i = 1; i <= 40; i++) entries.add("Entry " + i);
        ListView<String> list = ListView.simple(() -> entries, Component::literal)
                .rowHeight(12)
                .emptyText(Component.literal("Nothing here"))
                .rowTooltip(s -> List.of(Component.literal(s), Component.literal("Click to select").withStyle(ChatFormatting.GRAY)));
        page.add(list, LayoutData.weighted(1f).fill(true));

        List<ItemStack> stacks = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            if (stacks.size() < 120 && item != Items.AIR) stacks.add(new ItemStack(item));
        });
        page.add(PagedGrid.items(() -> stacks, 6).size(6 * 18 + 8, 18 * 5), LayoutData.create().width(6 * 18 + 8).fill(true));

        ScrollPanel scroll = new ScrollPanel().layout(LinearLayout.vertical(2)).padding(2);
        for (int i = 0; i < 12; i++) {
            int index = i;
            scroll.add(new Button(Component.literal("Scrolled " + i), () -> progress = index / 12f).size(70, 12));
        }
        page.add(scroll, LayoutData.create().width(84).fill(true));
        return page;
    }

    private Panel build3d() {
        Panel page = new Panel().layout(LinearLayout.horizontal(6)).padding(4);
        page.add(new EntityView(() -> Minecraft.getInstance().player).scale(30).size(70, 90), LayoutData.create().width(70).fill(true));
        page.add(new EntityView(() -> Minecraft.getInstance().player).scale(30).followMouse(false).spin(2f).size(70, 90), LayoutData.create().width(70).fill(true));
        page.add(Viewport3D.ofBlock(() -> Blocks.CRAFTING_TABLE.defaultBlockState()).baseScale(24).size(90, 90), LayoutData.create().width(90).fill(true));
        page.add(new ItemView(new ItemStack(Items.DIAMOND_PICKAXE)).slotBackground(true), LayoutData.create().size(18, 18));
        return page;
    }

    private Panel buildLayout() {
        Panel page = new Panel().layout(GridLayout.split(2, 2, 4, 4)).padding(4);
        Panel anchored = new Panel().chrome(Panel.Chrome.INSET).layout(AnchorLayout.INSTANCE);
        for (Anchor anchor : Anchor.values()) {
            anchored.add(new Label(Component.literal(anchor.name().substring(0, 2))).size(12, 8), LayoutData.anchored(anchor, 0, 0).margin(2));
        }
        page.add(anchored, LayoutData.filled());

        Panel flow = new Panel().chrome(Panel.Chrome.INSET).layout(FlowLayout.create(2, 2)).padding(2);
        for (int i = 0; i < 14; i++) {
            flow.add(new Button(Component.literal(Integer.toString(i))).size(18, 12));
        }
        page.add(flow, LayoutData.filled());

        Panel grid = new Panel().chrome(Panel.Chrome.INSET).layout(GridLayout.slots(4)).padding(2);
        grid.add(new SlotDecor(1, 1, 4, 2), LayoutData.create().span(4, 2));
        page.add(grid, LayoutData.filled());

        Panel text = new Panel().chrome(Panel.Chrome.PANEL).header(Component.literal("Text")).padding(3).layout(LinearLayout.vertical(2));
        text.add(new TextBlock(Component.literal("Wrapped text blocks reflow to the panel width, and panels can carry headers, insets and window chrome from the active theme.")).align(Align.START), LayoutData.filled());
        text.add(Separator.horizontal(), LayoutData.filled().height(3));
        text.add(new Label(Component.literal("Axis: " + Axis.HORIZONTAL)).dim());
        page.add(text, LayoutData.filled());
        return page;
    }

    private void spawnWindow() {
        windowCount++;
        Window window = new Window(Component.literal("Window " + windowCount)).resizable(80, 50, 300, 300).collapsible(true);
        window.setBounds(new Rect(20 + windowCount * 12, 20 + windowCount * 12, 140, 90));
        window.layout(LinearLayout.vertical(3));
        window.add(new Label(Component.literal("Drag the title bar")).dim());
        window.add(new Button(Component.literal("Close"), window::close).size(60, 12));
        openWindow(window);
    }

    private void openModal() {
        Panel modal = new Panel().chrome(Panel.Chrome.WINDOW).padding(8).layout(LinearLayout.vertical(6));
        modal.setSize(180, 70);
        modal.add(new TextBlock(Component.translatable("gui.saelibvie.demo.modal_body")), LayoutData.filled());
        modal.add(new Button(Component.translatable("gui.saelibvie.demo.close"), () -> root.popLayer()).size(60, 14), LayoutData.create().crossAlign(Align.CENTER));
        pushModal(modal);
    }

    @Override
    public void tick() {
        super.tick();
        progress = (progress + 0.01f) % 1f;
        fluidAmount = (fluidAmount + 37) % 8000;
    }
}
