package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class BlockRenderDispatcher implements ResourceManagerReloadListener {
	private final BlockModelShaper blockModelShaper;
	private final ModelBlockRenderer modelRenderer;
	private final AnimatedEntityBlockRenderer entityBlockRenderer = new AnimatedEntityBlockRenderer();
	private final LiquidBlockRenderer liquidBlockRenderer;
	private final Random random = new Random();

	public BlockRenderDispatcher(BlockModelShaper blockModelShaper, BlockColors blockColors) {
		this.blockModelShaper = blockModelShaper;
		this.modelRenderer = new ModelBlockRenderer(blockColors);
		this.liquidBlockRenderer = new LiquidBlockRenderer();
	}

	public BlockModelShaper getBlockModelShaper() {
		return this.blockModelShaper;
	}

	public void renderBreakingTexture(BlockState blockState, BlockPos blockPos, TextureAtlasSprite textureAtlasSprite, BlockAndBiomeGetter blockAndBiomeGetter) {
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			BakedModel bakedModel = this.blockModelShaper.getBlockModel(blockState);
			long l = blockState.getSeed(blockPos);
			BakedModel bakedModel2 = new SimpleBakedModel.Builder(blockState, bakedModel, textureAtlasSprite, this.random, l).build();
			this.modelRenderer.tesselateBlock(blockAndBiomeGetter, bakedModel2, blockState, blockPos, Tesselator.getInstance().getBuilder(), true, this.random, l);
		}
	}

	public boolean renderBatched(BlockState blockState, BlockPos blockPos, BlockAndBiomeGetter blockAndBiomeGetter, BufferBuilder bufferBuilder, Random random) {
		try {
			RenderShape renderShape = blockState.getRenderShape();
			if (renderShape == RenderShape.INVISIBLE) {
				return false;
			} else {
				switch (renderShape) {
					case MODEL:
						return this.modelRenderer
							.tesselateBlock(blockAndBiomeGetter, this.getBlockModel(blockState), blockState, blockPos, bufferBuilder, true, random, blockState.getSeed(blockPos));
					case ENTITYBLOCK_ANIMATED:
						return false;
					default:
						return false;
				}
			}
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Tesselating block in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
			throw new ReportedException(crashReport);
		}
	}

	public boolean renderLiquid(BlockPos blockPos, BlockAndBiomeGetter blockAndBiomeGetter, BufferBuilder bufferBuilder, FluidState fluidState) {
		try {
			return this.liquidBlockRenderer.tesselate(blockAndBiomeGetter, blockPos, bufferBuilder, fluidState);
		} catch (Throwable var8) {
			CrashReport crashReport = CrashReport.forThrowable(var8, "Tesselating liquid in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, null);
			throw new ReportedException(crashReport);
		}
	}

	public ModelBlockRenderer getModelRenderer() {
		return this.modelRenderer;
	}

	public BakedModel getBlockModel(BlockState blockState) {
		return this.blockModelShaper.getBlockModel(blockState);
	}

	public void renderSingleBlock(BlockState blockState, float f) {
		RenderShape renderShape = blockState.getRenderShape();
		if (renderShape != RenderShape.INVISIBLE) {
			switch (renderShape) {
				case MODEL:
					BakedModel bakedModel = this.getBlockModel(blockState);
					this.modelRenderer.renderSingleBlock(bakedModel, blockState, f, true);
					break;
				case ENTITYBLOCK_ANIMATED:
					this.entityBlockRenderer.renderSingleBlock(blockState.getBlock(), f);
			}
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.liquidBlockRenderer.setupSprites();
	}
}
