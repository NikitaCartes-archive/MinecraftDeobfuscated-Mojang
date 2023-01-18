/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenDirection;

@Environment(value=EnvType.CLIENT)
public enum ScreenAxis {
    HORIZONTAL,
    VERTICAL;


    public ScreenAxis orthogonal() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case HORIZONTAL -> VERTICAL;
            case VERTICAL -> HORIZONTAL;
        };
    }

    public ScreenDirection getPositive() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case HORIZONTAL -> ScreenDirection.RIGHT;
            case VERTICAL -> ScreenDirection.DOWN;
        };
    }

    public ScreenDirection getNegative() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case HORIZONTAL -> ScreenDirection.LEFT;
            case VERTICAL -> ScreenDirection.UP;
        };
    }

    public ScreenDirection getDirection(boolean bl) {
        return bl ? this.getPositive() : this.getNegative();
    }
}

