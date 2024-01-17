package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ShulkerRenderer extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
	private static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation(
		"textures/" + Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().getPath() + ".png"
	);
	private static final ResourceLocation[] TEXTURE_LOCATION = (ResourceLocation[])Sheets.SHULKER_TEXTURE_LOCATION
		.stream()
		.map(material -> new ResourceLocation("textures/" + material.texture().getPath() + ".png"))
		.toArray(ResourceLocation[]::new);

	public ShulkerRenderer(EntityRendererProvider.Context context) {
		super(context, new ShulkerModel<>(context.bakeLayer(ModelLayers.SHULKER)), 0.0F);
		this.addLayer(new ShulkerHeadLayer(this));
	}

	public Vec3 getRenderOffset(Shulker shulker, float f) {
		return ((Vec3)shulker.getRenderPosition(f).orElse(super.getRenderOffset(shulker, f))).scale((double)shulker.getScale());
	}

	public boolean shouldRender(Shulker shulker, Frustum frustum, double d, double e, double f) {
		return super.shouldRender(shulker, frustum, d, e, f) ? true : shulker.getRenderPosition(0.0F).filter(vec3 -> {
			EntityType<?> entityType = shulker.getType();
			float fx = entityType.getHeight() / 2.0F;
			float g = entityType.getWidth() / 2.0F;
			Vec3 vec32 = Vec3.atBottomCenterOf(shulker.blockPosition());
			return frustum.isVisible(new AABB(vec3.x, vec3.y + (double)fx, vec3.z, vec32.x, vec32.y + (double)fx, vec32.z).inflate((double)g, (double)fx, (double)g));
		}).isPresent();
	}

	public ResourceLocation getTextureLocation(Shulker shulker) {
		return getTextureLocation(shulker.getColor());
	}

	public static ResourceLocation getTextureLocation(@Nullable DyeColor dyeColor) {
		return dyeColor == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[dyeColor.getId()];
	}

	protected void setupRotations(Shulker shulker, PoseStack poseStack, float f, float g, float h, float i) {
		super.setupRotations(shulker, poseStack, f, g + 180.0F, h, i);
		poseStack.rotateAround(shulker.getAttachFace().getOpposite().getRotation(), 0.0F, 0.5F, 0.0F);
	}
}
