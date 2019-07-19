package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
	public FallingBlockRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
	}

	public void render(FallingBlockEntity fallingBlockEntity, double d, double e, double f, float g, float h) {
		BlockState blockState = fallingBlockEntity.getBlockState();
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			Level level = fallingBlockEntity.getLevel();
			if (blockState != level.getBlockState(new BlockPos(fallingBlockEntity)) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
				this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
				GlStateManager.pushMatrix();
				GlStateManager.disableLighting();
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				if (this.solidRender) {
					GlStateManager.enableColorMaterial();
					GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(fallingBlockEntity));
				}

				bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
				BlockPos blockPos = new BlockPos(fallingBlockEntity.x, fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.z);
				GlStateManager.translatef((float)(d - (double)blockPos.getX() - 0.5), (float)(e - (double)blockPos.getY()), (float)(f - (double)blockPos.getZ() - 0.5));
				BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
				blockRenderDispatcher.getModelRenderer()
					.tesselateBlock(
						level,
						blockRenderDispatcher.getBlockModel(blockState),
						blockState,
						blockPos,
						bufferBuilder,
						false,
						new Random(),
						blockState.getSeed(fallingBlockEntity.getStartPos())
					);
				tesselator.end();
				if (this.solidRender) {
					GlStateManager.tearDownSolidRenderingTextureCombine();
					GlStateManager.disableColorMaterial();
				}

				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
				super.render(fallingBlockEntity, d, e, f, g, h);
			}
		}
	}

	protected ResourceLocation getTextureLocation(FallingBlockEntity fallingBlockEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
