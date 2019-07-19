package net.minecraft.client.renderer.entity;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;

@Environment(EnvType.CLIENT)
public class AreaEffectCloudRenderer extends EntityRenderer<AreaEffectCloud> {
	public AreaEffectCloudRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	@Nullable
	protected ResourceLocation getTextureLocation(AreaEffectCloud areaEffectCloud) {
		return null;
	}
}
