/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;

@Environment(value=EnvType.CLIENT)
public interface FocusNavigationEvent {
    public ScreenDirection getVerticalDirectionForInitialFocus();

    @Environment(value=EnvType.CLIENT)
    public record ArrowNavigation(ScreenDirection direction) implements FocusNavigationEvent
    {
        @Override
        public ScreenDirection getVerticalDirectionForInitialFocus() {
            return this.direction.getAxis() == ScreenAxis.VERTICAL ? this.direction : ScreenDirection.DOWN;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class InitialFocus
    implements FocusNavigationEvent {
        @Override
        public ScreenDirection getVerticalDirectionForInitialFocus() {
            return ScreenDirection.DOWN;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record TabNavigation(boolean forward) implements FocusNavigationEvent
    {
        @Override
        public ScreenDirection getVerticalDirectionForInitialFocus() {
            return this.forward ? ScreenDirection.DOWN : ScreenDirection.UP;
        }
    }
}

