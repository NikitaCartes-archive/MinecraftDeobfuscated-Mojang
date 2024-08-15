package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class NoopRenderer<T extends Entity> extends EntityRenderer<T, EntityRenderState> {
	public NoopRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityRenderState entityRenderState) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}
}
