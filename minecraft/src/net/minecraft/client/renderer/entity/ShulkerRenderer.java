package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ShulkerRenderer extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
	public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation(
		"textures/" + ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION.getPath() + ".png"
	);
	public static final ResourceLocation[] TEXTURE_LOCATION = (ResourceLocation[])ModelBakery.SHULKER_TEXTURE_LOCATION
		.stream()
		.map(resourceLocation -> new ResourceLocation("textures/" + resourceLocation.getPath() + ".png"))
		.toArray(ResourceLocation[]::new);

	public ShulkerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new ShulkerModel<>(), 0.0F);
		this.addLayer(new ShulkerHeadLayer(this));
	}

	public Vec3 getRenderOffset(Shulker shulker, double d, double e, double f, float g) {
		int i = shulker.getClientSideTeleportInterpolation();
		if (i > 0 && shulker.hasValidInterpolationPositions()) {
			BlockPos blockPos = shulker.getAttachPosition();
			BlockPos blockPos2 = shulker.getOldAttachPosition();
			double h = (double)((float)i - g) / 6.0;
			h *= h;
			double j = (double)(blockPos.getX() - blockPos2.getX()) * h;
			double k = (double)(blockPos.getY() - blockPos2.getY()) * h;
			double l = (double)(blockPos.getZ() - blockPos2.getZ()) * h;
			return new Vec3(-j, -k, -l);
		} else {
			return super.getRenderOffset(shulker, d, e, f, g);
		}
	}

	public boolean shouldRender(Shulker shulker, Frustum frustum, double d, double e, double f) {
		if (super.shouldRender(shulker, frustum, d, e, f)) {
			return true;
		} else {
			if (shulker.getClientSideTeleportInterpolation() > 0 && shulker.hasValidInterpolationPositions()) {
				BlockPos blockPos = shulker.getOldAttachPosition();
				BlockPos blockPos2 = shulker.getAttachPosition();
				Vec3 vec3 = new Vec3((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
				Vec3 vec32 = new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
				if (frustum.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z))) {
					return true;
				}
			}

			return false;
		}
	}

	public ResourceLocation getTextureLocation(Shulker shulker) {
		return shulker.getColor() == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[shulker.getColor().getId()];
	}

	protected void setupRotations(Shulker shulker, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(shulker, poseStack, f, g, h);
		poseStack.translate(0.0, 0.5, 0.0);
		poseStack.mulPose(shulker.getAttachFace().getOpposite().getRotation());
		poseStack.translate(0.0, -0.5, 0.0);
	}

	protected void scale(Shulker shulker, PoseStack poseStack, float f) {
		float g = 0.999F;
		poseStack.scale(0.999F, 0.999F, 0.999F);
	}
}
