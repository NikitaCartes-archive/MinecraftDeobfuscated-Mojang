/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(value=EnvType.CLIENT)
public class BelowOrAboveWidgetTooltipPositioner
implements ClientTooltipPositioner {
    private final AbstractWidget widget;

    public BelowOrAboveWidgetTooltipPositioner(AbstractWidget abstractWidget) {
        this.widget = abstractWidget;
    }

    @Override
    public Vector2ic positionTooltip(Screen screen, int i, int j, int k, int l) {
        Vector2i vector2i = new Vector2i();
        vector2i.x = this.widget.getX() + 3;
        vector2i.y = this.widget.getY() + this.widget.getHeight() + 3 + 1;
        if (vector2i.y + l + 3 > screen.height) {
            vector2i.y = this.widget.getY() - l - 3 - 1;
        }
        if (vector2i.x + k > screen.width) {
            vector2i.x = Math.max(this.widget.getX() + this.widget.getWidth() - k - 3, 4);
        }
        return vector2i;
    }
}

