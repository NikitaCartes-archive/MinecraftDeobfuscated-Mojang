package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;

@Environment(EnvType.CLIENT)
public class SalmonRenderer extends MobRenderer<Salmon, SalmonModel<Salmon>> {
	private static final ResourceLocation SALMON_LOCATION = new ResourceLocation("textures/entity/fish/salmon.png");

	public SalmonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new SalmonModel<>(), 0.4F);
	}

	@Nullable
	protected ResourceLocation getTextureLocation(Salmon salmon) {
		return SALMON_LOCATION;
	}

	protected void setupRotations(Salmon salmon, float f, float g, float h) {
		super.setupRotations(salmon, f, g, h);
		float i = 1.0F;
		float j = 1.0F;
		if (!salmon.isInWater()) {
			i = 1.3F;
			j = 1.7F;
		}

		float k = i * 4.3F * Mth.sin(j * 0.6F * f);
		RenderSystem.rotatef(k, 0.0F, 1.0F, 0.0F);
		RenderSystem.translatef(0.0F, 0.0F, -0.4F);
		if (!salmon.isInWater()) {
			RenderSystem.translatef(0.2F, 0.1F, 0.0F);
			RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
		}
	}
}
