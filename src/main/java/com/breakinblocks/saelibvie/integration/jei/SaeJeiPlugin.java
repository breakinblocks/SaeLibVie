package com.breakinblocks.saelibvie.integration.jei;

import com.breakinblocks.saelibvie.SaeLibVie;
import com.breakinblocks.saelibvie.ui.core.PositionedIngredient;
import com.breakinblocks.saelibvie.ui.core.UiRoot;
import com.breakinblocks.saelibvie.ui.geom.Rect;
import com.breakinblocks.saelibvie.ui.screen.SaeLibVieCursor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class SaeJeiPlugin implements IModPlugin {
    @Nullable
    public static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return SaeLibVie.id("jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
            @Override
            public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(double mouseX, double mouseY) {
                UiRoot root = SaeLibVieCursor.rootOf(Minecraft.getInstance().screen);
                if (root == null) return Optional.empty();
                return root.ingredientUnderMouse().flatMap(SaeJeiPlugin::toClickable);
            }
        });
        registration.addGhostIngredientHandler(Screen.class, new IGhostIngredientHandler<Screen>() {
            @Override
            public <I> List<Target<I>> getTargetsTyped(Screen screen, ITypedIngredient<I> ingredient, boolean doStart) {
                UiRoot root = SaeLibVieCursor.rootOf(screen);
                List<Target<I>> targets = new ArrayList<>();
                if (root == null) return targets;
                Object raw = ingredient.getIngredient();
                for (UiRoot.GhostTarget ghost : root.ghostTargets()) {
                    if (!ghost.target().acceptsGhost(raw)) continue;
                    Rect r = ghost.screenRect();
                    targets.add(new Target<>() {
                        @Override
                        public Rect2i getArea() {
                            return new Rect2i(r.x(), r.y(), r.w(), r.h());
                        }

                        @Override
                        public void accept(I accepted) {
                            ghost.target().acceptGhost(accepted);
                        }
                    });
                }
                return targets;
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private static Optional<IClickableIngredient<?>> toClickable(PositionedIngredient positioned) {
        if (runtime == null) return Optional.empty();
        Object ingredient = positioned.ingredient();
        Rect r = positioned.screenRect();
        Rect2i area = new Rect2i(r.x(), r.y(), r.w(), r.h());
        if (ingredient instanceof ItemStack stack && !stack.isEmpty()) {
            return typed(VanillaTypes.ITEM_STACK, stack, area);
        }
        if (ingredient instanceof FluidStack fluid && !fluid.isEmpty()) {
            return typed(NeoForgeTypes.FLUID_STACK, fluid, area);
        }
        return Optional.empty();
    }

    private static <T> Optional<IClickableIngredient<?>> typed(IIngredientType<T> type, T value, Rect2i area) {
        return runtime.getIngredientManager().createTypedIngredient(type, value).map(typed -> new IClickableIngredient<T>() {
            @Override
            public ITypedIngredient<T> getTypedIngredient() {
                return typed;
            }

            @Override
            public Rect2i getArea() {
                return area;
            }
        });
    }
}
