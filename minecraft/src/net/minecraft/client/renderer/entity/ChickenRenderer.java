package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

@Environment(EnvType.CLIENT)
public class ChickenRenderer extends MobRenderer<Chicken, ChickenModel<Chicken>> {
	private static final ResourceLocation CHICKEN_LOCATION = new ResourceLocation("textures/entity/chicken.png");

	public ChickenRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new ChickenModel<>(), 0.3F);
	}

	protected ResourceLocation getTextureLocation(Chicken chicken) {
		return CHICKEN_LOCATION;
	}

	protected float getBob(Chicken chicken, float f) {
		float g = Mth.lerp(f, chicken.oFlap, chicken.flap);
		float h = Mth.lerp(f, chicken.oFlapSpeed, chicken.flapSpeed);
		return (Mth.sin(g) + 1.0F) * h;
	}
}
