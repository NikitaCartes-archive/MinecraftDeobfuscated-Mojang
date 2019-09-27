package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
	public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T mushroomCow, float f, float g, float h, float j, float k, float l, float m
	) {
		if (!mushroomCow.isBaby() && !mushroomCow.isInvisible()) {
			BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
			BlockState blockState = mushroomCow.getMushroomType().getBlockState();
			poseStack.pushPose();
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(-0.2F, 0.35F, 0.5);
			poseStack.mulPose(Vector3f.YP.rotation(-42.0F, true));
			int n = OverlayTexture.v(mushroomCow.hurtTime > 0 || mushroomCow.deathTime > 0);
			poseStack.pushPose();
			poseStack.translate(-0.5, -0.5, 0.5);
			blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, 0, n);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(-0.1F, 0.0, -0.6F);
			poseStack.mulPose(Vector3f.YP.rotation(-42.0F, true));
			poseStack.translate(-0.5, -0.5, 0.5);
			blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, 0, n);
			poseStack.popPose();
			poseStack.popPose();
			poseStack.pushPose();
			this.getParentModel().getHead().translateAndRotate(poseStack, 0.0625F);
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(0.0, 0.7F, -0.2F);
			poseStack.mulPose(Vector3f.YP.rotation(-12.0F, true));
			poseStack.translate(-0.5, -0.5, 0.5);
			blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, 0, n);
			poseStack.popPose();
		}
	}
}
