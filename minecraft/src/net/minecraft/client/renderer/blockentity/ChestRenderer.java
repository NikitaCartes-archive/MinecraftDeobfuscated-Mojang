package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Calendar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
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
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> extends BlockEntityRenderer<T> {
	private final ModelPart lid;
	private final ModelPart bottom;
	private final ModelPart lock;
	private final ModelPart doubleLeftLid;
	private final ModelPart doubleLeftBottom;
	private final ModelPart doubleLeftLock;
	private final ModelPart doubleRightLid;
	private final ModelPart doubleRightBottom;
	private final ModelPart doubleRightLock;
	private boolean xmasTextures;

	public ChestRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			this.xmasTextures = true;
		}

		this.bottom = new ModelPart(64, 64, 0, 19);
		this.bottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
		this.lid = new ModelPart(64, 64, 0, 0);
		this.lid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
		this.lid.y = 9.0F;
		this.lid.z = 1.0F;
		this.lock = new ModelPart(64, 64, 0, 0);
		this.lock.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
		this.lock.y = 8.0F;
		this.doubleLeftBottom = new ModelPart(64, 64, 0, 19);
		this.doubleLeftBottom.addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
		this.doubleLeftLid = new ModelPart(64, 64, 0, 0);
		this.doubleLeftLid.addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
		this.doubleLeftLid.y = 9.0F;
		this.doubleLeftLid.z = 1.0F;
		this.doubleLeftLock = new ModelPart(64, 64, 0, 0);
		this.doubleLeftLock.addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
		this.doubleLeftLock.y = 8.0F;
		this.doubleRightBottom = new ModelPart(64, 64, 0, 19);
		this.doubleRightBottom.addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
		this.doubleRightLid = new ModelPart(64, 64, 0, 0);
		this.doubleRightLid.addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
		this.doubleRightLid.y = 9.0F;
		this.doubleRightLid.z = 1.0F;
		this.doubleRightLock = new ModelPart(64, 64, 0, 0);
		this.doubleRightLock.addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
		this.doubleRightLock.y = 8.0F;
	}

	@Override
	public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Level level = blockEntity.getLevel();
		boolean bl = level != null;
		BlockState blockState = bl ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
		ChestType chestType = blockState.hasProperty((Property<T>)ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
		Block block = blockState.getBlock();
		if (block instanceof AbstractChestBlock) {
			AbstractChestBlock<?> abstractChestBlock = (AbstractChestBlock<?>)block;
			boolean bl2 = chestType != ChestType.SINGLE;
			poseStack.pushPose();
			float g = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
			poseStack.translate(-0.5, -0.5, -0.5);
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
					this.render(poseStack, vertexConsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, h, k, j);
				} else {
					this.render(poseStack, vertexConsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, h, k, j);
				}
			} else {
				this.render(poseStack, vertexConsumer, this.lid, this.lock, this.bottom, h, k, j);
			}

			poseStack.popPose();
		}
	}

	private void render(PoseStack poseStack, VertexConsumer vertexConsumer, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, float f, int i, int j) {
		modelPart.xRot = -(f * (float) (Math.PI / 2));
		modelPart2.xRot = modelPart.xRot;
		modelPart.render(poseStack, vertexConsumer, i, j);
		modelPart2.render(poseStack, vertexConsumer, i, j);
		modelPart3.render(poseStack, vertexConsumer, i, j);
	}
}
