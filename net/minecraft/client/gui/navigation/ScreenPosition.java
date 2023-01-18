/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;

@Environment(value=EnvType.CLIENT)
public record ScreenPosition(int x, int y) {
    public static ScreenPosition of(ScreenAxis screenAxis, int i, int j) {
        return switch (screenAxis) {
            default -> throw new IncompatibleClassChangeError();
            case ScreenAxis.HORIZONTAL -> new ScreenPosition(i, j);
            case ScreenAxis.VERTICAL -> new ScreenPosition(j, i);
        };
    }

    public ScreenPosition step(ScreenDirection screenDirection) {
        return switch (screenDirection) {
            default -> throw new IncompatibleClassChangeError();
            case ScreenDirection.DOWN -> new ScreenPosition(this.x, this.y + 1);
            case ScreenDirection.UP -> new ScreenPosition(this.x, this.y - 1);
            case ScreenDirection.LEFT -> new ScreenPosition(this.x - 1, this.y);
            case ScreenDirection.RIGHT -> new ScreenPosition(this.x + 1, this.y);
        };
    }

    public int getCoordinate(ScreenAxis screenAxis) {
        return switch (screenAxis) {
            default -> throw new IncompatibleClassChangeError();
            case ScreenAxis.HORIZONTAL -> this.x;
            case ScreenAxis.VERTICAL -> this.y;
        };
    }
}

