package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

@Environment(EnvType.CLIENT)
public class PistonHeadRenderer extends BlockEntityRenderer<PistonMovingBlockEntity> {
	private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

	public void render(PistonMovingBlockEntity pistonMovingBlockEntity, double d, double e, double f, float g, int i) {
		BlockPos blockPos = pistonMovingBlockEntity.getBlockPos().relative(pistonMovingBlockEntity.getMovementDirection().getOpposite());
		BlockState blockState = pistonMovingBlockEntity.getMovedState();
		if (!blockState.isAir() && !(pistonMovingBlockEntity.getProgress(g) >= 1.0F)) {
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
			Lighting.turnOff();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableBlend();
			RenderSystem.disableCull();
			if (Minecraft.useAmbientOcclusion()) {
				RenderSystem.shadeModel(7425);
			} else {
				RenderSystem.shadeModel(7424);
			}

			ModelBlockRenderer.enableCaching();
			bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
			bufferBuilder.offset(
				d - (double)blockPos.getX() + (double)pistonMovingBlockEntity.getXOff(g),
				e - (double)blockPos.getY() + (double)pistonMovingBlockEntity.getYOff(g),
				f - (double)blockPos.getZ() + (double)pistonMovingBlockEntity.getZOff(g)
			);
			Level level = this.getLevel();
			if (blockState.getBlock() == Blocks.PISTON_HEAD && pistonMovingBlockEntity.getProgress(g) <= 4.0F) {
				blockState = blockState.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(true));
				this.renderBlock(blockPos, blockState, bufferBuilder, level, false);
			} else if (pistonMovingBlockEntity.isSourcePiston() && !pistonMovingBlockEntity.isExtending()) {
				PistonType pistonType = blockState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
				BlockState blockState2 = Blocks.PISTON_HEAD
					.defaultBlockState()
					.setValue(PistonHeadBlock.TYPE, pistonType)
					.setValue(PistonHeadBlock.FACING, blockState.getValue(PistonBaseBlock.FACING));
				blockState2 = blockState2.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pistonMovingBlockEntity.getProgress(g) >= 0.5F));
				this.renderBlock(blockPos, blockState2, bufferBuilder, level, false);
				BlockPos blockPos2 = blockPos.relative(pistonMovingBlockEntity.getMovementDirection());
				bufferBuilder.offset(d - (double)blockPos2.getX(), e - (double)blockPos2.getY(), f - (double)blockPos2.getZ());
				blockState = blockState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
				this.renderBlock(blockPos2, blockState, bufferBuilder, level, true);
			} else {
				this.renderBlock(blockPos, blockState, bufferBuilder, level, false);
			}

			bufferBuilder.offset(0.0, 0.0, 0.0);
			tesselator.end();
			ModelBlockRenderer.clearCache();
			Lighting.turnOn();
		}
	}

	private boolean renderBlock(BlockPos blockPos, BlockState blockState, BufferBuilder bufferBuilder, Level level, boolean bl) {
		return this.blockRenderer
			.getModelRenderer()
			.tesselateBlock(level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, bufferBuilder, bl, new Random(), blockState.getSeed(blockPos));
	}
}
