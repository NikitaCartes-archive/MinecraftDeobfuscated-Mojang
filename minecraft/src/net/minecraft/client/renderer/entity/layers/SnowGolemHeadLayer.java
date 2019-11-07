package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class SnowGolemHeadLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
	public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, SnowGolem snowGolem, float f, float g, float h, float j, float k, float l) {
		if (!snowGolem.isInvisible() && snowGolem.hasPumpkin()) {
			poseStack.pushPose();
			this.getParentModel().getHead().translateAndRotate(poseStack);
			float m = 0.625F;
			poseStack.translate(0.0, -0.34375, 0.0);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			poseStack.scale(0.625F, -0.625F, -0.625F);
			ItemStack itemStack = new ItemStack(Blocks.CARVED_PUMPKIN);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(
					snowGolem,
					itemStack,
					ItemTransforms.TransformType.HEAD,
					false,
					poseStack,
					multiBufferSource,
					snowGolem.level,
					i,
					LivingEntityRenderer.getOverlayCoords(snowGolem, 0.0F)
				);
			poseStack.popPose();
		}
	}
}
