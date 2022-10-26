/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public interface ClientTooltipComponent {
    public static ClientTooltipComponent create(FormattedCharSequence formattedCharSequence) {
        return new ClientTextTooltip(formattedCharSequence);
    }

    public static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
        if (tooltipComponent instanceof BundleTooltip) {
            return new ClientBundleTooltip((BundleTooltip)tooltipComponent);
        }
        throw new IllegalArgumentException("Unknown TooltipComponent");
    }

    public int getHeight();

    public int getWidth(Font var1);

    default public void renderText(Font font, int i, int j, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
    }

    default public void renderImage(Font font, int i, int j, PoseStack poseStack, ItemRenderer itemRenderer, int k) {
    }
}

