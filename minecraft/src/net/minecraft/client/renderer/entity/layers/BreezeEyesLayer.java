package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;

@Environment(EnvType.CLIENT)
public class BreezeEyesLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
	private final ResourceLocation textureLoc;
	private final BreezeModel<Breeze> model;

	public BreezeEyesLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> renderLayerParent, EntityModelSet entityModelSet, ResourceLocation resourceLocation) {
		super(renderLayerParent);
		this.model = new BreezeModel<>(entityModelSet.bakeLayer(ModelLayers.BREEZE_EYES));
		this.textureLoc = resourceLocation;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Breeze breeze, float f, float g, float h, float j, float k, float l) {
		this.model.prepareMobModel(breeze, f, g, h);
		this.getParentModel().copyPropertiesTo(this.model);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.breezeEyes(this.textureLoc));
		this.model.setupAnim(breeze, f, g, j, k, l);
		this.model.root().render(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	protected ResourceLocation getTextureLocation(Breeze breeze) {
		return this.textureLoc;
	}
}
