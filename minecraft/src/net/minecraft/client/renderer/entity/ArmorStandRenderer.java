package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
	public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

	public ArmorStandRenderer(EntityRendererProvider.Context context) {
		super(context, new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);
		this.addLayer(
			new HumanoidArmorLayer<>(
				this,
				new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
				new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR))
			)
		);
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
	}

	public ResourceLocation getTextureLocation(ArmorStand armorStand) {
		return DEFAULT_SKIN_LOCATION;
	}

	protected void setupRotations(ArmorStand armorStand, PoseStack poseStack, float f, float g, float h) {
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - g));
		float i = (float)(armorStand.level.getGameTime() - armorStand.lastHit) + h;
		if (i < 5.0F) {
			poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(i / 1.5F * (float) Math.PI) * 3.0F));
		}
	}

	protected boolean shouldShowName(ArmorStand armorStand) {
		double d = this.entityRenderDispatcher.distanceToSqr(armorStand);
		float f = armorStand.isCrouching() ? 32.0F : 64.0F;
		return d >= (double)(f * f) ? false : armorStand.isCustomNameVisible();
	}

	@Nullable
	protected RenderType getRenderType(ArmorStand armorStand, boolean bl, boolean bl2, boolean bl3) {
		if (!armorStand.isMarker()) {
			return super.getRenderType(armorStand, bl, bl2, bl3);
		} else {
			ResourceLocation resourceLocation = this.getTextureLocation(armorStand);
			if (bl2) {
				return RenderType.entityTranslucent(resourceLocation, false);
			} else {
				return bl ? RenderType.entityCutoutNoCull(resourceLocation, false) : null;
			}
		}
	}
}
