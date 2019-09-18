package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class LecternRenderer extends BatchedBlockEntityRenderer<LecternBlockEntity> {
	private final BookModel bookModel = new BookModel();

	protected void renderToBuffer(
		LecternBlockEntity lecternBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k
	) {
		BlockState blockState = lecternBlockEntity.getBlockState();
		if ((Boolean)blockState.getValue(LecternBlock.HAS_BOOK)) {
			bufferBuilder.pushPose();
			bufferBuilder.translate(0.5, 1.0625, 0.5);
			float h = ((Direction)blockState.getValue(LecternBlock.FACING)).getClockWise().toYRot();
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, -h, true));
			bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 67.5F, true));
			bufferBuilder.translate(0.0, -0.125, 0.0);
			this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
			this.bookModel.render(bufferBuilder, 0.0625F, j, k, this.getSprite(EnchantTableRenderer.BOOK_LOCATION));
			bufferBuilder.popPose();
		}
	}
}
