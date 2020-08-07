package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class ShulkerHeadLayer extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
	public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Shulker shulker, float f, float g, float h, float j, float k, float l) {
		poseStack.pushPose();
		poseStack.translate(0.0, 1.0, 0.0);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		Quaternion quaternion = shulker.getAttachFace().getOpposite().getRotation();
		quaternion.conj();
		poseStack.mulPose(quaternion);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0, -1.0, 0.0);
		DyeColor dyeColor = shulker.getColor();
		ResourceLocation resourceLocation = dyeColor == null ? ShulkerRenderer.DEFAULT_TEXTURE_LOCATION : ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()];
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(resourceLocation));
		this.getParentModel().getHead().render(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(shulker, 0.0F));
		poseStack.popPose();
	}
}
