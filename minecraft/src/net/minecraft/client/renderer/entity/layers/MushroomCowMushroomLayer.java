package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
	public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T mushroomCow, float f, float g, float h, float j, float k, float l) {
		if (!mushroomCow.isBaby() && !mushroomCow.isInvisible()) {
			BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
			BlockState blockState = mushroomCow.getMushroomType().getBlockState();
			int m = LivingEntityRenderer.getOverlayCoords(mushroomCow, 0.0F);
			poseStack.pushPose();
			poseStack.translate(0.2F, -0.35F, 0.5);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(-0.5, -0.5, -0.5);
			blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, m);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.translate(0.2F, -0.35F, 0.5);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(42.0F));
			poseStack.translate(0.1F, 0.0, -0.6F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(-0.5, -0.5, -0.5);
			blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, m);
			poseStack.popPose();
			poseStack.pushPose();
			this.getParentModel().getHead().translateAndRotate(poseStack);
			poseStack.translate(0.0, -0.7F, -0.2F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(-0.5, -0.5, -0.5);
			blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, m);
			poseStack.popPose();
		}
	}
}
