package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

@Environment(EnvType.CLIENT)
public class RavagerRenderer extends MobRenderer<Ravager, RavagerModel> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/ravager.png");

	public RavagerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new RavagerModel(), 1.1F);
	}

	protected ResourceLocation getTextureLocation(Ravager ravager) {
		return TEXTURE_LOCATION;
	}
}
