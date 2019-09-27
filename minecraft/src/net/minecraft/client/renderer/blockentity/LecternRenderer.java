package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class LecternRenderer extends BlockEntityRenderer<LecternBlockEntity> {
	private final BookModel bookModel = new BookModel();

	public LecternRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(
		LecternBlockEntity lecternBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		BlockState blockState = lecternBlockEntity.getBlockState();
		if ((Boolean)blockState.getValue(LecternBlock.HAS_BOOK)) {
			poseStack.pushPose();
			poseStack.translate(0.5, 1.0625, 0.5);
			float h = ((Direction)blockState.getValue(LecternBlock.FACING)).getClockWise().toYRot();
			poseStack.mulPose(Vector3f.YP.rotation(-h, true));
			poseStack.mulPose(Vector3f.ZP.rotation(67.5F, true));
			poseStack.translate(0.0, -0.125, 0.0);
			this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.SOLID);
			this.bookModel.render(poseStack, vertexConsumer, 0.0625F, i, this.getSprite(EnchantTableRenderer.BOOK_LOCATION));
			poseStack.popPose();
		}
	}
}
