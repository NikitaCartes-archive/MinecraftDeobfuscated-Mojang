package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;

@Environment(EnvType.CLIENT)
public class BreezeRenderer extends MobRenderer<Breeze, BreezeModel<Breeze>> {
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze.png");

	public BreezeRenderer(EntityRendererProvider.Context context) {
		super(context, new BreezeModel<>(context.bakeLayer(ModelLayers.BREEZE)), 0.5F);
		this.addLayer(new BreezeWindLayer(context, this));
		this.addLayer(new BreezeEyesLayer(this));
	}

	public void render(Breeze breeze, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		BreezeModel<Breeze> breezeModel = this.getModel();
		enable(breezeModel, breezeModel.head(), breezeModel.rods());
		super.render(breeze, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Breeze breeze) {
		return TEXTURE_LOCATION;
	}

	public static BreezeModel<Breeze> enable(BreezeModel<Breeze> breezeModel, ModelPart... modelParts) {
		breezeModel.head().visible = false;
		breezeModel.eyes().visible = false;
		breezeModel.rods().visible = false;
		breezeModel.wind().visible = false;

		for (ModelPart modelPart : modelParts) {
			modelPart.visible = true;
		}

		return breezeModel;
	}
}
