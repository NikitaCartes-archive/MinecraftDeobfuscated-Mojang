package net.minecraft.client.gui.screens.inventory.tooltip;

import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TooltipRenderUtil {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/background");
	private static final ResourceLocation FRAME_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/frame");
	public static final int MOUSE_OFFSET = 12;
	private static final int PADDING = 3;
	public static final int PADDING_LEFT = 3;
	public static final int PADDING_RIGHT = 3;
	public static final int PADDING_TOP = 3;
	public static final int PADDING_BOTTOM = 3;
	private static final int MARGIN = 9;

	public static void renderTooltipBackground(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, @Nullable ResourceLocation resourceLocation) {
		int n = i - 3 - 9;
		int o = j - 3 - 9;
		int p = k + 3 + 3 + 18;
		int q = l + 3 + 3 + 18;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, (float)m);
		guiGraphics.blitSprite(RenderType::guiTextured, getBackgroundSprite(resourceLocation), n, o, p, q);
		guiGraphics.blitSprite(RenderType::guiTextured, getFrameSprite(resourceLocation), n, o, p, q);
		guiGraphics.pose().popPose();
	}

	private static ResourceLocation getBackgroundSprite(@Nullable ResourceLocation resourceLocation) {
		return resourceLocation == null ? BACKGROUND_SPRITE : resourceLocation.withPath((UnaryOperator<String>)(string -> "tooltip/" + string + "_background"));
	}

	private static ResourceLocation getFrameSprite(@Nullable ResourceLocation resourceLocation) {
		return resourceLocation == null ? FRAME_SPRITE : resourceLocation.withPath((UnaryOperator<String>)(string -> "tooltip/" + string + "_frame"));
	}
}
