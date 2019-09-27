package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

@Environment(EnvType.CLIENT)
public class StructureBlockRenderer extends BlockEntityRenderer<StructureBlockEntity> {
	public StructureBlockRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(
		StructureBlockEntity structureBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
			BlockPos blockPos = structureBlockEntity.getStructurePos();
			BlockPos blockPos2 = structureBlockEntity.getStructureSize();
			if (blockPos2.getX() >= 1 && blockPos2.getY() >= 1 && blockPos2.getZ() >= 1) {
				if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getMode() == StructureMode.LOAD) {
					double h = (double)blockPos.getX();
					double j = (double)blockPos.getZ();
					double k = (double)blockPos.getY();
					double l = k + (double)blockPos2.getY();
					double m;
					double n;
					switch (structureBlockEntity.getMirror()) {
						case LEFT_RIGHT:
							m = (double)blockPos2.getX();
							n = (double)(-blockPos2.getZ());
							break;
						case FRONT_BACK:
							m = (double)(-blockPos2.getX());
							n = (double)blockPos2.getZ();
							break;
						default:
							m = (double)blockPos2.getX();
							n = (double)blockPos2.getZ();
					}

					double o;
					double p;
					double q;
					double r;
					switch (structureBlockEntity.getRotation()) {
						case CLOCKWISE_90:
							o = n < 0.0 ? h : h + 1.0;
							p = m < 0.0 ? j + 1.0 : j;
							q = o - n;
							r = p + m;
							break;
						case CLOCKWISE_180:
							o = m < 0.0 ? h : h + 1.0;
							p = n < 0.0 ? j : j + 1.0;
							q = o - m;
							r = p - n;
							break;
						case COUNTERCLOCKWISE_90:
							o = n < 0.0 ? h + 1.0 : h;
							p = m < 0.0 ? j : j + 1.0;
							q = o + n;
							r = p - m;
							break;
						default:
							o = m < 0.0 ? h + 1.0 : h;
							p = n < 0.0 ? j + 1.0 : j;
							q = o + m;
							r = p + n;
					}

					float s = 1.0F;
					float t = 0.9F;
					float u = 0.5F;
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LINES);
					if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
						LevelRenderer.renderLineBox(poseStack, vertexConsumer, o, k, p, q, l, r, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
					}

					if (structureBlockEntity.getMode() == StructureMode.SAVE && structureBlockEntity.getShowAir()) {
						this.renderInvisibleBlocks(structureBlockEntity, vertexConsumer, blockPos, true, poseStack);
						this.renderInvisibleBlocks(structureBlockEntity, vertexConsumer, blockPos, false, poseStack);
					}
				}
			}
		}
	}

	private void renderInvisibleBlocks(
		StructureBlockEntity structureBlockEntity, VertexConsumer vertexConsumer, BlockPos blockPos, boolean bl, PoseStack poseStack
	) {
		BlockGetter blockGetter = structureBlockEntity.getLevel();
		BlockPos blockPos2 = structureBlockEntity.getBlockPos();
		BlockPos blockPos3 = blockPos2.offset(blockPos);

		for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos3, blockPos3.offset(structureBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
			BlockState blockState = blockGetter.getBlockState(blockPos4);
			boolean bl2 = blockState.isAir();
			boolean bl3 = blockState.getBlock() == Blocks.STRUCTURE_VOID;
			if (bl2 || bl3) {
				float f = bl2 ? 0.05F : 0.0F;
				double d = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.45F - f);
				double e = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.45F - f);
				double g = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45F - f);
				double h = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.55F + f);
				double i = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.55F + f);
				double j = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55F + f);
				if (bl) {
					LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F);
				} else if (bl2) {
					LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
				} else {
					LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 1.0F, 0.25F, 0.25F, 1.0F, 1.0F, 0.25F, 0.25F);
				}
			}
		}
	}

	public boolean shouldRenderOffScreen(StructureBlockEntity structureBlockEntity) {
		return true;
	}
}
