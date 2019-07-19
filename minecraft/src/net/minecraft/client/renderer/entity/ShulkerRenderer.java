package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ShulkerRenderer extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
	public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/shulker.png");
	public static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{
		new ResourceLocation("textures/entity/shulker/shulker_white.png"),
		new ResourceLocation("textures/entity/shulker/shulker_orange.png"),
		new ResourceLocation("textures/entity/shulker/shulker_magenta.png"),
		new ResourceLocation("textures/entity/shulker/shulker_light_blue.png"),
		new ResourceLocation("textures/entity/shulker/shulker_yellow.png"),
		new ResourceLocation("textures/entity/shulker/shulker_lime.png"),
		new ResourceLocation("textures/entity/shulker/shulker_pink.png"),
		new ResourceLocation("textures/entity/shulker/shulker_gray.png"),
		new ResourceLocation("textures/entity/shulker/shulker_light_gray.png"),
		new ResourceLocation("textures/entity/shulker/shulker_cyan.png"),
		new ResourceLocation("textures/entity/shulker/shulker_purple.png"),
		new ResourceLocation("textures/entity/shulker/shulker_blue.png"),
		new ResourceLocation("textures/entity/shulker/shulker_brown.png"),
		new ResourceLocation("textures/entity/shulker/shulker_green.png"),
		new ResourceLocation("textures/entity/shulker/shulker_red.png"),
		new ResourceLocation("textures/entity/shulker/shulker_black.png")
	};

	public ShulkerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new ShulkerModel<>(), 0.0F);
		this.addLayer(new ShulkerHeadLayer(this));
	}

	public void render(Shulker shulker, double d, double e, double f, float g, float h) {
		int i = shulker.getClientSideTeleportInterpolation();
		if (i > 0 && shulker.hasValidInterpolationPositions()) {
			BlockPos blockPos = shulker.getAttachPosition();
			BlockPos blockPos2 = shulker.getOldAttachPosition();
			double j = (double)((float)i - h) / 6.0;
			j *= j;
			double k = (double)(blockPos.getX() - blockPos2.getX()) * j;
			double l = (double)(blockPos.getY() - blockPos2.getY()) * j;
			double m = (double)(blockPos.getZ() - blockPos2.getZ()) * j;
			super.render(shulker, d - k, e - l, f - m, g, h);
		} else {
			super.render(shulker, d, e, f, g, h);
		}
	}

	public boolean shouldRender(Shulker shulker, Culler culler, double d, double e, double f) {
		if (super.shouldRender(shulker, culler, d, e, f)) {
			return true;
		} else {
			if (shulker.getClientSideTeleportInterpolation() > 0 && shulker.hasValidInterpolationPositions()) {
				BlockPos blockPos = shulker.getOldAttachPosition();
				BlockPos blockPos2 = shulker.getAttachPosition();
				Vec3 vec3 = new Vec3((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
				Vec3 vec32 = new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
				if (culler.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z))) {
					return true;
				}
			}

			return false;
		}
	}

	protected ResourceLocation getTextureLocation(Shulker shulker) {
		return shulker.getColor() == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[shulker.getColor().getId()];
	}

	protected void setupRotations(Shulker shulker, float f, float g, float h) {
		super.setupRotations(shulker, f, g, h);
		switch (shulker.getAttachFace()) {
			case DOWN:
			default:
				break;
			case EAST:
				GlStateManager.translatef(0.5F, 0.5F, 0.0F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
				break;
			case WEST:
				GlStateManager.translatef(-0.5F, 0.5F, 0.0F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				break;
			case NORTH:
				GlStateManager.translatef(0.0F, 0.5F, -0.5F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				break;
			case SOUTH:
				GlStateManager.translatef(0.0F, 0.5F, 0.5F);
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
				break;
			case UP:
				GlStateManager.translatef(0.0F, 1.0F, 0.0F);
				GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
		}
	}

	protected void scale(Shulker shulker, float f) {
		float g = 0.999F;
		GlStateManager.scalef(0.999F, 0.999F, 0.999F);
	}
}
