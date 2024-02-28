package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.WitherSkeleton;

@Environment(EnvType.CLIENT)
public class WitherSkeletonRenderer extends SkeletonRenderer<WitherSkeleton> {
	private static final ResourceLocation WITHER_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

	public WitherSkeletonRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.WITHER_SKELETON, ModelLayers.WITHER_SKELETON_INNER_ARMOR, ModelLayers.WITHER_SKELETON_OUTER_ARMOR);
	}

	public ResourceLocation getTextureLocation(WitherSkeleton witherSkeleton) {
		return WITHER_SKELETON_LOCATION;
	}

	protected void scale(WitherSkeleton witherSkeleton, PoseStack poseStack, float f) {
		poseStack.scale(1.2F, 1.2F, 1.2F);
	}
}
