package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Hoglin;

@Environment(EnvType.CLIENT)
public class HoglinRenderer extends MobRenderer<Hoglin, HoglinModel> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/hoglin/hoglin.png");

	public HoglinRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new HoglinModel(), 0.7F);
	}

	public ResourceLocation getTextureLocation(Hoglin hoglin) {
		return TEXTURE_LOCATION;
	}
}
