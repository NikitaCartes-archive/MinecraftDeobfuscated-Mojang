package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;

@Environment(EnvType.CLIENT)
public class ElderGuardianRenderer extends GuardianRenderer {
	public static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

	public ElderGuardianRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, 1.2F);
	}

	protected void scale(Guardian guardian, PoseStack poseStack, float f) {
		poseStack.scale(ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE);
	}

	@Override
	public ResourceLocation getTextureLocation(Guardian guardian) {
		return GUARDIAN_ELDER_LOCATION;
	}
}
