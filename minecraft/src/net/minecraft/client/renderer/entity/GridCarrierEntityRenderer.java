package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.grid.GridCarrier;

@Environment(EnvType.CLIENT)
public class GridCarrierEntityRenderer extends EntityRenderer<GridCarrier> {
	public GridCarrierEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(GridCarrier gridCarrier, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
	}

	public ResourceLocation getTextureLocation(GridCarrier gridCarrier) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
