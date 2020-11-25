package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class LecternRenderer implements BlockEntityRenderer<LecternBlockEntity> {
	private final BookModel bookModel;

	public LecternRenderer(BlockEntityRendererProvider.Context context) {
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}

	public void render(LecternBlockEntity lecternBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		BlockState blockState = lecternBlockEntity.getBlockState();
		if ((Boolean)blockState.getValue(LecternBlock.HAS_BOOK)) {
			poseStack.pushPose();
			poseStack.translate(0.5, 1.0625, 0.5);
			float g = ((Direction)blockState.getValue(LecternBlock.FACING)).getClockWise().toYRot();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(67.5F));
			poseStack.translate(0.0, -0.125, 0.0);
			this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
			VertexConsumer vertexConsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
			this.bookModel.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
	}
}
