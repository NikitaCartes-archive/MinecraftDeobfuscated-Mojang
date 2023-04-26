package net.minecraft.client.gui.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record GlyphRenderTypes(RenderType normal, RenderType seeThrough, RenderType polygonOffset) {
	public static GlyphRenderTypes createForIntensityTexture(ResourceLocation resourceLocation) {
		return new GlyphRenderTypes(
			RenderType.textIntensity(resourceLocation), RenderType.textIntensitySeeThrough(resourceLocation), RenderType.textIntensityPolygonOffset(resourceLocation)
		);
	}

	public static GlyphRenderTypes createForColorTexture(ResourceLocation resourceLocation) {
		return new GlyphRenderTypes(RenderType.text(resourceLocation), RenderType.textSeeThrough(resourceLocation), RenderType.textPolygonOffset(resourceLocation));
	}

	public RenderType select(Font.DisplayMode displayMode) {
		return switch (displayMode) {
			case NORMAL -> this.normal;
			case SEE_THROUGH -> this.seeThrough;
			case POLYGON_OFFSET -> this.polygonOffset;
		};
	}
}
