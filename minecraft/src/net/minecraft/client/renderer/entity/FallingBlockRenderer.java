package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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

	public void render(
		FallingBlockEntity fallingBlockEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource
	) {
		BlockState blockState = fallingBlockEntity.getBlockState();
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			Level level = fallingBlockEntity.getLevel();
			if (blockState != level.getBlockState(new BlockPos(fallingBlockEntity)) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
				poseStack.pushPose();
				BlockPos blockPos = new BlockPos(fallingBlockEntity.x, fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.z);
				poseStack.translate((double)(-(blockPos.getX() & 15)) - 0.5, (double)(-(blockPos.getY() & 15)), (double)(-(blockPos.getZ() & 15)) - 0.5);
				BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
				blockRenderDispatcher.getModelRenderer()
					.tesselateBlock(
						level,
						blockRenderDispatcher.getBlockModel(blockState),
						blockState,
						blockPos,
						poseStack,
						multiBufferSource.getBuffer(RenderType.getRenderLayer(blockState)),
						false,
						new Random(),
						blockState.getSeed(fallingBlockEntity.getStartPos())
					);
				poseStack.popPose();
				super.render(fallingBlockEntity, d, e, f, g, h, poseStack, multiBufferSource);
			}
		}
	}

	public ResourceLocation getTextureLocation(FallingBlockEntity fallingBlockEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
