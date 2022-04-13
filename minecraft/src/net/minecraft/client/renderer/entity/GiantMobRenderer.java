package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;

@Environment(EnvType.CLIENT)
public class GiantMobRenderer extends MobRenderer<Giant, HumanoidModel<Giant>> {
	private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");
	private final float scale;

	public GiantMobRenderer(EntityRendererProvider.Context context, float f) {
		super(context, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT)), 0.5F * f);
		this.scale = f;
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		this.addLayer(
			new HumanoidArmorLayer<>(
				this, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)), new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR))
			)
		);
	}

	protected void scale(Giant giant, PoseStack poseStack, float f) {
		poseStack.scale(this.scale, this.scale, this.scale);
	}

	public ResourceLocation getTextureLocation(Giant giant) {
		return ZOMBIE_LOCATION;
	}
}
