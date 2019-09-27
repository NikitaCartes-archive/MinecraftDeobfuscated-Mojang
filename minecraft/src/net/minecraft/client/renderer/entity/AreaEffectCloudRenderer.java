package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;

@Environment(EnvType.CLIENT)
public class AreaEffectCloudRenderer extends EntityRenderer<AreaEffectCloud> {
	public AreaEffectCloudRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public ResourceLocation getTextureLocation(AreaEffectCloud areaEffectCloud) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
