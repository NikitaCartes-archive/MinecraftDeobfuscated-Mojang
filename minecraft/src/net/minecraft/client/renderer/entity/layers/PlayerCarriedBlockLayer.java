package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class PlayerCarriedBlockLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlayerCarriedBlockLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		AbstractClientPlayer abstractClientPlayer,
		float f,
		float g,
		float h,
		float j,
		float k,
		float l
	) {
		BlockState blockState = abstractClientPlayer.getCarriedBlock();
		if (blockState != null) {
			poseStack.pushPose();
			Item item = GenericItemBlock.itemFromGenericBlock(blockState);
			if (item != null) {
				ItemStack itemStack = item.getDefaultInstance();
				Minecraft.getInstance()
					.getItemInHandRenderer()
					.renderItem(
						abstractClientPlayer, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource, OverlayTexture.NO_OVERLAY
					);
			} else {
				poseStack.translate(0.0, 0.6875, -0.75);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
				poseStack.translate(0.125, 0.25, 0.5);
				float m = 0.625F;
				poseStack.scale(-0.625F, -0.625F, 0.625F);
				poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			}

			poseStack.popPose();
		}
	}
}
