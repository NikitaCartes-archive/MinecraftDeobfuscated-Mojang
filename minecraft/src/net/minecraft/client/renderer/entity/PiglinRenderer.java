package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.layers.PiglinArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(EnvType.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<Piglin, PiglinModel<Piglin>> {
	private static final ResourceLocation PIGLIN_LOCATION = new ResourceLocation("textures/entity/piglin/piglin.png");

	public PiglinRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new PiglinModel<>(0.0F, 128, 64), 0.5F);
		this.addLayer(new PiglinArmorLayer<>(this, new HumanoidModel(0.5F), new HumanoidModel(1.0F), makeHelmetHeadModel()));
	}

	private static <T extends Piglin> PiglinModel<T> makeHelmetHeadModel() {
		PiglinModel<T> piglinModel = new PiglinModel<>(1.0F, 64, 16);
		piglinModel.earLeft.visible = false;
		piglinModel.earRight.visible = false;
		return piglinModel;
	}

	public ResourceLocation getTextureLocation(Piglin piglin) {
		return PIGLIN_LOCATION;
	}

	protected void setupRotations(Piglin piglin, PoseStack poseStack, float f, float g, float h) {
		if (piglin.isConverting()) {
			g += (float)(Math.cos((double)piglin.tickCount * 3.25) * Math.PI * 0.5);
		}

		super.setupRotations(piglin, poseStack, f, g, h);
	}
}
