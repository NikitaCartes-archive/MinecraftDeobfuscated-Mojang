package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Environment(EnvType.CLIENT)
public interface ClientTooltipComponent {
	static ClientTooltipComponent create(FormattedCharSequence formattedCharSequence) {
		return new ClientTextTooltip(formattedCharSequence);
	}

	static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
		if (tooltipComponent instanceof BundleTooltip) {
			return new ClientBundleTooltip((BundleTooltip)tooltipComponent);
		} else {
			throw new IllegalArgumentException("Unknown TooltipComponent");
		}
	}

	int getHeight();

	int getWidth(Font font);

	default void renderText(Font font, int i, int j, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
	}

	default void renderImage(Font font, int i, int j, PoseStack poseStack, ItemRenderer itemRenderer, int k) {
	}
}
