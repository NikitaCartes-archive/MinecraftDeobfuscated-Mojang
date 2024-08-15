package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(EnvType.CLIENT)
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
	private final ChestModel singleModel;
	private final ChestModel doubleLeftModel;
	private final ChestModel doubleRightModel;
	private boolean xmasTextures;

	public ChestRenderer(BlockEntityRendererProvider.Context context) {
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			this.xmasTextures = true;
		}

		this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
		this.doubleLeftModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
		this.doubleRightModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
	}

	@Override
	public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Level level = blockEntity.getLevel();
		boolean bl = level != null;
		BlockState blockState = bl ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestType chestType = blockState.hasProperty((Property<T>)ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		if (blockState.getBlock() instanceof AbstractChestBlock<?> abstractChestBlock) {
			boolean bl2 = chestType != ChestType.SINGLE;
			poseStack.pushPose();
			float g = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
			poseStack.translate(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Axis.YP.rotationDegrees(-g));
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborCombineResult;
			if (bl) {
				neighborCombineResult = abstractChestBlock.combine(blockState, level, blockEntity.getBlockPos(), true);
			} else {
				neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;
			}

			float h = neighborCombineResult.apply(ChestBlock.opennessCombiner(blockEntity)).get(f);
			h = 1.0F - h;
			h = 1.0F - h * h * h;
			int k = neighborCombineResult.apply(new BrightnessCombiner<>()).applyAsInt(i);
			Material material = Sheets.chooseMaterial(blockEntity, chestType, this.xmasTextures);
			VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entityCutout);
			if (bl2) {
				if (chestType == ChestType.LEFT) {
					this.render(poseStack, vertexConsumer, this.doubleLeftModel, h, k, j);
				} else {
					this.render(poseStack, vertexConsumer, this.doubleRightModel, h, k, j);
				}
			} else {
				this.render(poseStack, vertexConsumer, this.singleModel, h, k, j);
			}

			poseStack.popPose();
		}
	}

	private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ChestModel chestModel, float f, int i, int j) {
		chestModel.setupAnim(f);
		chestModel.renderToBuffer(poseStack, vertexConsumer, i, j);
	}
}
