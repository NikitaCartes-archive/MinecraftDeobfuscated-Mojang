package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

@Environment(EnvType.CLIENT)
public class SquidRenderer extends MobRenderer<Squid, SquidModel<Squid>> {
	private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

	public SquidRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new SquidModel<>(), 0.7F);
	}

	protected ResourceLocation getTextureLocation(Squid squid) {
		return SQUID_LOCATION;
	}

	protected void setupRotations(Squid squid, float f, float g, float h) {
		float i = Mth.lerp(h, squid.xBodyRotO, squid.xBodyRot);
		float j = Mth.lerp(h, squid.zBodyRotO, squid.zBodyRot);
		GlStateManager.translatef(0.0F, 0.5F, 0.0F);
		GlStateManager.rotatef(180.0F - g, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(i, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotatef(j, 0.0F, 1.0F, 0.0F);
		GlStateManager.translatef(0.0F, -1.2F, 0.0F);
	}

	protected float getBob(Squid squid, float f) {
		return Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
	}
}
