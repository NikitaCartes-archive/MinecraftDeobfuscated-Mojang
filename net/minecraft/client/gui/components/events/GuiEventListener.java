/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface GuiEventListener {
    public static final long DOUBLE_CLICK_THRESHOLD_MS = 250L;

    default public void mouseMoved(double d, double e) {
    }

    default public boolean mouseClicked(double d, double e, int i) {
        return false;
    }

    default public boolean mouseReleased(double d, double e, int i) {
        return false;
    }

    default public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return false;
    }

    default public boolean mouseScrolled(double d, double e, double f) {
        return false;
    }

    default public boolean keyPressed(int i, int j, int k) {
        return false;
    }

    default public boolean keyReleased(int i, int j, int k) {
        return false;
    }

    default public boolean charTyped(char c, int i) {
        return false;
    }

    @Nullable
    default public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }

    default public boolean isMouseOver(double d, double e) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();

    @Nullable
    default public ComponentPath getCurrentFocusPath() {
        if (this.isFocused()) {
            return ComponentPath.leaf(this);
        }
        return null;
    }

    default public ScreenRectangle getRectangle() {
        return ScreenRectangle.empty();
    }
}

