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
public class StructureBlockRenderer implements BlockEntityRenderer<StructureBlockEntity> {
	public StructureBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	public void render(StructureBlockEntity structureBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
			BlockPos blockPos = structureBlockEntity.getStructurePos();
			BlockPos blockPos2 = structureBlockEntity.getStructureSize();
			if (blockPos2.getX() >= 1 && blockPos2.getY() >= 1 && blockPos2.getZ() >= 1) {
				if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getMode() == StructureMode.LOAD) {
					double d = (double)blockPos.getX();
					double e = (double)blockPos.getZ();
					double g = (double)blockPos.getY();
					double h = g + (double)blockPos2.getY();
					double k;
					double l;
					switch (structureBlockEntity.getMirror()) {
						case LEFT_RIGHT:
							k = (double)blockPos2.getX();
							l = (double)(-blockPos2.getZ());
							break;
						case FRONT_BACK:
							k = (double)(-blockPos2.getX());
							l = (double)blockPos2.getZ();
							break;
						default:
							k = (double)blockPos2.getX();
							l = (double)blockPos2.getZ();
					}

					double m;
					double n;
					double o;
					double p;
					switch (structureBlockEntity.getRotation()) {
						case CLOCKWISE_90:
							m = l < 0.0 ? d : d + 1.0;
							n = k < 0.0 ? e + 1.0 : e;
							o = m - l;
							p = n + k;
							break;
						case CLOCKWISE_180:
							m = k < 0.0 ? d : d + 1.0;
							n = l < 0.0 ? e : e + 1.0;
							o = m - k;
							p = n - l;
							break;
						case COUNTERCLOCKWISE_90:
							m = l < 0.0 ? d + 1.0 : d;
							n = k < 0.0 ? e : e + 1.0;
							o = m + l;
							p = n - k;
							break;
						default:
							m = k < 0.0 ? d + 1.0 : d;
							n = l < 0.0 ? e + 1.0 : e;
							o = m + k;
							p = n + l;
					}

					float q = 1.0F;
					float r = 0.9F;
					float s = 0.5F;
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
					if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
						LevelRenderer.renderLineBox(poseStack, vertexConsumer, m, g, n, o, h, p, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
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
			boolean bl3 = blockState.is(Blocks.STRUCTURE_VOID);
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

	@Override
	public int getViewDistance() {
		return 96;
	}
}
