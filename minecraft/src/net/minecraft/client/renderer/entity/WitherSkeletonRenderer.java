package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(EnvType.CLIENT)
public class WitherSkeletonRenderer extends SkeletonRenderer {
	private static final ResourceLocation WITHER_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

	public WitherSkeletonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	@Override
	protected ResourceLocation getTextureLocation(AbstractSkeleton abstractSkeleton) {
		return WITHER_SKELETON_LOCATION;
	}

	protected void scale(AbstractSkeleton abstractSkeleton, float f) {
		GlStateManager.scalef(1.2F, 1.2F, 1.2F);
	}
}
