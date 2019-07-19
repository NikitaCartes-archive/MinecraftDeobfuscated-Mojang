/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.events;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ContainerEventHandler
extends GuiEventListener {
    public List<? extends GuiEventListener> children();

    default public Optional<GuiEventListener> getChildAt(double d, double e) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.isMouseOver(d, e)) continue;
            return Optional.of(guiEventListener);
        }
        return Optional.empty();
    }

    @Override
    default public boolean mouseClicked(double d, double e, int i) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.mouseClicked(d, e, i)) continue;
            this.setFocused(guiEventListener);
            if (i == 0) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    default public boolean mouseReleased(double d, double e, int i) {
        this.setDragging(false);
        return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseReleased(d, e, i)).isPresent();
    }

    @Override
    default public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.getFocused() != null && this.isDragging() && i == 0) {
            return this.getFocused().mouseDragged(d, e, i, f, g);
        }
        return false;
    }

    public boolean isDragging();

    public void setDragging(boolean var1);

    @Override
    default public boolean mouseScrolled(double d, double e, double f) {
        return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseScrolled(d, e, f)).isPresent();
    }

    @Override
    default public boolean keyPressed(int i, int j, int k) {
        return this.getFocused() != null && this.getFocused().keyPressed(i, j, k);
    }

    @Override
    default public boolean keyReleased(int i, int j, int k) {
        return this.getFocused() != null && this.getFocused().keyReleased(i, j, k);
    }

    @Override
    default public boolean charTyped(char c, int i) {
        return this.getFocused() != null && this.getFocused().charTyped(c, i);
    }

    @Nullable
    public GuiEventListener getFocused();

    public void setFocused(@Nullable GuiEventListener var1);

    default public void setInitialFocus(@Nullable GuiEventListener guiEventListener) {
        this.setFocused(guiEventListener);
    }

    default public void magicalSpecialHackyFocus(@Nullable GuiEventListener guiEventListener) {
        this.setFocused(guiEventListener);
    }

    @Override
    default public boolean changeFocus(boolean bl) {
        Supplier<GuiEventListener> supplier;
        BooleanSupplier booleanSupplier;
        boolean bl2;
        GuiEventListener guiEventListener = this.getFocused();
        boolean bl3 = bl2 = guiEventListener != null;
        if (bl2 && guiEventListener.changeFocus(bl)) {
            return true;
        }
        List<? extends GuiEventListener> list = this.children();
        int i = list.indexOf(guiEventListener);
        int j = bl2 && i >= 0 ? i + (bl ? 1 : 0) : (bl ? 0 : list.size());
        ListIterator<? extends GuiEventListener> listIterator = list.listIterator(j);
        BooleanSupplier booleanSupplier2 = bl ? listIterator::hasNext : (booleanSupplier = listIterator::hasPrevious);
        Supplier<GuiEventListener> supplier2 = bl ? listIterator::next : (supplier = listIterator::previous);
        while (booleanSupplier.getAsBoolean()) {
            GuiEventListener guiEventListener2 = supplier.get();
            if (!guiEventListener2.changeFocus(bl)) continue;
            this.setFocused(guiEventListener2);
            return true;
        }
        this.setFocused(null);
        return false;
    }
}

