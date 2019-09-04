package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

@Environment(EnvType.CLIENT)
public class StructureBlockRenderer extends BlockEntityRenderer<StructureBlockEntity> {
	public void render(StructureBlockEntity structureBlockEntity, double d, double e, double f, float g, int i) {
		if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
			super.render(structureBlockEntity, d, e, f, g, i);
			BlockPos blockPos = structureBlockEntity.getStructurePos();
			BlockPos blockPos2 = structureBlockEntity.getStructureSize();
			if (blockPos2.getX() >= 1 && blockPos2.getY() >= 1 && blockPos2.getZ() >= 1) {
				if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getMode() == StructureMode.LOAD) {
					double h = 0.01;
					double j = (double)blockPos.getX();
					double k = (double)blockPos.getZ();
					double l = e + (double)blockPos.getY() - 0.01;
					double m = l + (double)blockPos2.getY() + 0.02;
					double n;
					double o;
					switch (structureBlockEntity.getMirror()) {
						case LEFT_RIGHT:
							n = (double)blockPos2.getX() + 0.02;
							o = -((double)blockPos2.getZ() + 0.02);
							break;
						case FRONT_BACK:
							n = -((double)blockPos2.getX() + 0.02);
							o = (double)blockPos2.getZ() + 0.02;
							break;
						default:
							n = (double)blockPos2.getX() + 0.02;
							o = (double)blockPos2.getZ() + 0.02;
					}

					double p;
					double q;
					double r;
					double s;
					switch (structureBlockEntity.getRotation()) {
						case CLOCKWISE_90:
							p = d + (o < 0.0 ? j - 0.01 : j + 1.0 + 0.01);
							q = f + (n < 0.0 ? k + 1.0 + 0.01 : k - 0.01);
							r = p - o;
							s = q + n;
							break;
						case CLOCKWISE_180:
							p = d + (n < 0.0 ? j - 0.01 : j + 1.0 + 0.01);
							q = f + (o < 0.0 ? k - 0.01 : k + 1.0 + 0.01);
							r = p - n;
							s = q - o;
							break;
						case COUNTERCLOCKWISE_90:
							p = d + (o < 0.0 ? j + 1.0 + 0.01 : j - 0.01);
							q = f + (n < 0.0 ? k - 0.01 : k + 1.0 + 0.01);
							r = p + o;
							s = q - n;
							break;
						default:
							p = d + (n < 0.0 ? j + 1.0 + 0.01 : j - 0.01);
							q = f + (o < 0.0 ? k + 1.0 + 0.01 : k - 0.01);
							r = p + n;
							s = q + o;
					}

					int t = 255;
					int u = 223;
					int v = 127;
					Tesselator tesselator = Tesselator.getInstance();
					BufferBuilder bufferBuilder = tesselator.getBuilder();
					RenderSystem.disableFog();
					RenderSystem.disableLighting();
					RenderSystem.disableTexture();
					RenderSystem.enableBlend();
					RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
					);
					this.setOverlayRenderState(true);
					if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
						this.renderBox(tesselator, bufferBuilder, p, l, q, r, m, s, 255, 223, 127);
					}

					if (structureBlockEntity.getMode() == StructureMode.SAVE && structureBlockEntity.getShowAir()) {
						this.renderInvisibleBlocks(structureBlockEntity, d, e, f, blockPos, tesselator, bufferBuilder, true);
						this.renderInvisibleBlocks(structureBlockEntity, d, e, f, blockPos, tesselator, bufferBuilder, false);
					}

					this.setOverlayRenderState(false);
					RenderSystem.lineWidth(1.0F);
					RenderSystem.enableLighting();
					RenderSystem.enableTexture();
					RenderSystem.enableDepthTest();
					RenderSystem.depthMask(true);
					RenderSystem.enableFog();
				}
			}
		}
	}

	private void renderInvisibleBlocks(
		StructureBlockEntity structureBlockEntity, double d, double e, double f, BlockPos blockPos, Tesselator tesselator, BufferBuilder bufferBuilder, boolean bl
	) {
		RenderSystem.lineWidth(bl ? 3.0F : 1.0F);
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
		BlockGetter blockGetter = structureBlockEntity.getLevel();
		BlockPos blockPos2 = structureBlockEntity.getBlockPos();
		BlockPos blockPos3 = blockPos2.offset(blockPos);

		for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos3, blockPos3.offset(structureBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
			BlockState blockState = blockGetter.getBlockState(blockPos4);
			boolean bl2 = blockState.isAir();
			boolean bl3 = blockState.getBlock() == Blocks.STRUCTURE_VOID;
			if (bl2 || bl3) {
				float g = bl2 ? 0.05F : 0.0F;
				double h = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.45F) + d - (double)g;
				double i = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.45F) + e - (double)g;
				double j = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45F) + f - (double)g;
				double k = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.55F) + d + (double)g;
				double l = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.55F) + e + (double)g;
				double m = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55F) + f + (double)g;
				if (bl) {
					LevelRenderer.addChainedLineBoxVertices(bufferBuilder, h, i, j, k, l, m, 0.0F, 0.0F, 0.0F, 1.0F);
				} else if (bl2) {
					LevelRenderer.addChainedLineBoxVertices(bufferBuilder, h, i, j, k, l, m, 0.5F, 0.5F, 1.0F, 1.0F);
				} else {
					LevelRenderer.addChainedLineBoxVertices(bufferBuilder, h, i, j, k, l, m, 1.0F, 0.25F, 0.25F, 1.0F);
				}
			}
		}

		tesselator.end();
	}

	private void renderBox(Tesselator tesselator, BufferBuilder bufferBuilder, double d, double e, double f, double g, double h, double i, int j, int k, int l) {
		RenderSystem.lineWidth(2.0F);
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(d, e, f).color((float)k, (float)k, (float)k, 0.0F).endVertex();
		bufferBuilder.vertex(d, e, f).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, e, f).color(k, l, l, j).endVertex();
		bufferBuilder.vertex(g, e, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(d, e, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(d, e, f).color(l, l, k, j).endVertex();
		bufferBuilder.vertex(d, h, f).color(l, k, l, j).endVertex();
		bufferBuilder.vertex(g, h, f).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, h, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(d, h, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(d, h, f).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(d, h, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(d, e, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, e, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, h, i).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, h, f).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, e, f).color(k, k, k, j).endVertex();
		bufferBuilder.vertex(g, e, f).color((float)k, (float)k, (float)k, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.lineWidth(1.0F);
	}

	public boolean shouldRenderOffScreen(StructureBlockEntity structureBlockEntity) {
		return true;
	}
}
