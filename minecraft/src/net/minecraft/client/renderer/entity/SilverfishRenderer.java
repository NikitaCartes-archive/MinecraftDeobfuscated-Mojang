package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Silverfish;

@Environment(EnvType.CLIENT)
public class SilverfishRenderer extends MobRenderer<Silverfish, SilverfishModel<Silverfish>> {
	private static final ResourceLocation SILVERFISH_LOCATION = new ResourceLocation("textures/entity/silverfish.png");

	public SilverfishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new SilverfishModel<>(), 0.3F);
	}

	protected float getFlipDegrees(Silverfish silverfish) {
		return 180.0F;
	}

	protected ResourceLocation getTextureLocation(Silverfish silverfish) {
		return SILVERFISH_LOCATION;
	}
}
