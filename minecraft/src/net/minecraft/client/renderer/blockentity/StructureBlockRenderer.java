package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

@Environment(EnvType.CLIENT)
public class StructureBlockRenderer implements BlockEntityRenderer<StructureBlockEntity> {
	public StructureBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	public void render(StructureBlockEntity structureBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
			BlockPos blockPos = structureBlockEntity.getStructurePos();
			Vec3i vec3i = structureBlockEntity.getStructureSize();
			if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
				if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getMode() == StructureMode.LOAD) {
					double d = (double)blockPos.getX();
					double e = (double)blockPos.getZ();
					double g = (double)blockPos.getY();
					double h = g + (double)vec3i.getY();
					double k;
					double l;
					switch (structureBlockEntity.getMirror()) {
						case LEFT_RIGHT:
							k = (double)vec3i.getX();
							l = (double)(-vec3i.getZ());
							break;
						case FRONT_BACK:
							k = (double)(-vec3i.getX());
							l = (double)vec3i.getZ();
							break;
						default:
							k = (double)vec3i.getX();
							l = (double)vec3i.getZ();
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
					if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
						VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
						ShapeRenderer.renderLineBox(poseStack, vertexConsumer, m, g, n, o, h, p, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
					}

					if (structureBlockEntity.getMode() == StructureMode.SAVE && structureBlockEntity.getShowAir()) {
						this.renderInvisibleBlocks(structureBlockEntity, multiBufferSource, poseStack);
					}
				}
			}
		}
	}

	private void renderInvisibleBlocks(StructureBlockEntity structureBlockEntity, MultiBufferSource multiBufferSource, PoseStack poseStack) {
		BlockGetter blockGetter = structureBlockEntity.getLevel();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
		BlockPos blockPos = structureBlockEntity.getBlockPos();
		BlockPos blockPos2 = StructureUtils.getStructureOrigin(structureBlockEntity);

		for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos2, blockPos2.offset(structureBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
			BlockState blockState = blockGetter.getBlockState(blockPos3);
			boolean bl = blockState.isAir();
			boolean bl2 = blockState.is(Blocks.STRUCTURE_VOID);
			boolean bl3 = blockState.is(Blocks.BARRIER);
			boolean bl4 = blockState.is(Blocks.LIGHT);
			boolean bl5 = bl2 || bl3 || bl4;
			if (bl || bl5) {
				float f = bl ? 0.05F : 0.0F;
				double d = (double)((float)(blockPos3.getX() - blockPos.getX()) + 0.45F - f);
				double e = (double)((float)(blockPos3.getY() - blockPos.getY()) + 0.45F - f);
				double g = (double)((float)(blockPos3.getZ() - blockPos.getZ()) + 0.45F - f);
				double h = (double)((float)(blockPos3.getX() - blockPos.getX()) + 0.55F + f);
				double i = (double)((float)(blockPos3.getY() - blockPos.getY()) + 0.55F + f);
				double j = (double)((float)(blockPos3.getZ() - blockPos.getZ()) + 0.55F + f);
				if (bl) {
					ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
				} else if (bl2) {
					ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
				} else if (bl3) {
					ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
				} else if (bl4) {
					ShapeRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
				}
			}
		}
	}

	private void renderStructureVoids(StructureBlockEntity structureBlockEntity, VertexConsumer vertexConsumer, PoseStack poseStack) {
		BlockGetter blockGetter = structureBlockEntity.getLevel();
		if (blockGetter != null) {
			BlockPos blockPos = structureBlockEntity.getBlockPos();
			BlockPos blockPos2 = StructureUtils.getStructureOrigin(structureBlockEntity);
			Vec3i vec3i = structureBlockEntity.getStructureSize();
			DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(vec3i.getX(), vec3i.getY(), vec3i.getZ());

			for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos2, blockPos2.offset(vec3i).offset(-1, -1, -1))) {
				if (blockGetter.getBlockState(blockPos3).is(Blocks.STRUCTURE_VOID)) {
					discreteVoxelShape.fill(blockPos3.getX() - blockPos2.getX(), blockPos3.getY() - blockPos2.getY(), blockPos3.getZ() - blockPos2.getZ());
				}
			}

			discreteVoxelShape.forAllFaces((direction, i, j, k) -> {
				float f = 0.48F;
				float g = (float)(i + blockPos2.getX() - blockPos.getX()) + 0.5F - 0.48F;
				float h = (float)(j + blockPos2.getY() - blockPos.getY()) + 0.5F - 0.48F;
				float l = (float)(k + blockPos2.getZ() - blockPos.getZ()) + 0.5F - 0.48F;
				float m = (float)(i + blockPos2.getX() - blockPos.getX()) + 0.5F + 0.48F;
				float n = (float)(j + blockPos2.getY() - blockPos.getY()) + 0.5F + 0.48F;
				float o = (float)(k + blockPos2.getZ() - blockPos.getZ()) + 0.5F + 0.48F;
				ShapeRenderer.renderFace(poseStack, vertexConsumer, direction, g, h, l, m, n, o, 0.75F, 0.75F, 1.0F, 0.2F);
			});
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
