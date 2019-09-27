package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
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

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Shulker shulker, float f, float g, float h, float j, float k, float l, float m
	) {
		poseStack.pushPose();
		switch (shulker.getAttachFace()) {
			case DOWN:
			default:
				break;
			case EAST:
				poseStack.mulPose(Vector3f.ZP.rotation(90.0F, true));
				poseStack.mulPose(Vector3f.XP.rotation(90.0F, true));
				poseStack.translate(1.0, -1.0, 0.0);
				poseStack.mulPose(Vector3f.YP.rotation(180.0F, true));
				break;
			case WEST:
				poseStack.mulPose(Vector3f.ZP.rotation(-90.0F, true));
				poseStack.mulPose(Vector3f.XP.rotation(90.0F, true));
				poseStack.translate(-1.0, -1.0, 0.0);
				poseStack.mulPose(Vector3f.YP.rotation(180.0F, true));
				break;
			case NORTH:
				poseStack.mulPose(Vector3f.XP.rotation(90.0F, true));
				poseStack.translate(0.0, -1.0, -1.0);
				break;
			case SOUTH:
				poseStack.mulPose(Vector3f.ZP.rotation(180.0F, true));
				poseStack.mulPose(Vector3f.XP.rotation(90.0F, true));
				poseStack.translate(0.0, -1.0, 1.0);
				break;
			case UP:
				poseStack.mulPose(Vector3f.XP.rotation(180.0F, true));
				poseStack.translate(0.0, -2.0, 0.0);
		}

		ModelPart modelPart = this.getParentModel().getHead();
		modelPart.yRot = k * (float) (Math.PI / 180.0);
		modelPart.xRot = l * (float) (Math.PI / 180.0);
		DyeColor dyeColor = shulker.getColor();
		ResourceLocation resourceLocation;
		if (dyeColor == null) {
			resourceLocation = ShulkerRenderer.DEFAULT_TEXTURE_LOCATION;
		} else {
			resourceLocation = ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()];
		}

		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(resourceLocation));
		LivingEntityRenderer.setOverlayCoords(shulker, vertexConsumer, 0.0F);
		modelPart.render(poseStack, vertexConsumer, m, i, null);
		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
	}
}
