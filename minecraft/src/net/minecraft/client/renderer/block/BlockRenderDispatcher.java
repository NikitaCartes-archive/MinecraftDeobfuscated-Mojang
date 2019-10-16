package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class BlockRenderDispatcher implements ResourceManagerReloadListener {
	private final BlockModelShaper blockModelShaper;
	private final ModelBlockRenderer modelRenderer;
	private final LiquidBlockRenderer liquidBlockRenderer;
	private final Random random = new Random();
	private final BlockColors blockColors;

	public BlockRenderDispatcher(BlockModelShaper blockModelShaper, BlockColors blockColors) {
		this.blockModelShaper = blockModelShaper;
		this.blockColors = blockColors;
		this.modelRenderer = new ModelBlockRenderer(this.blockColors);
		this.liquidBlockRenderer = new LiquidBlockRenderer();
	}

	public BlockModelShaper getBlockModelShaper() {
		return this.blockModelShaper;
	}

	public void renderBreakingTexture(
		BlockState blockState, BlockPos blockPos, BlockAndBiomeGetter blockAndBiomeGetter, PoseStack poseStack, VertexConsumer vertexConsumer
	) {
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			BakedModel bakedModel = this.blockModelShaper.getBlockModel(blockState);
			long l = blockState.getSeed(blockPos);
			this.modelRenderer
				.tesselateBlock(blockAndBiomeGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, true, this.random, l, OverlayTexture.NO_OVERLAY);
		}
	}

	public boolean renderBatched(
		BlockState blockState,
		BlockPos blockPos,
		BlockAndBiomeGetter blockAndBiomeGetter,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random
	) {
		try {
			RenderShape renderShape = blockState.getRenderShape();
			return renderShape != RenderShape.MODEL
				? false
				: this.modelRenderer
					.tesselateBlock(
						blockAndBiomeGetter,
						this.getBlockModel(blockState),
						blockState,
						blockPos,
						poseStack,
						vertexConsumer,
						bl,
						random,
						blockState.getSeed(blockPos),
						OverlayTexture.NO_OVERLAY
					);
		} catch (Throwable var11) {
			CrashReport crashReport = CrashReport.forThrowable(var11, "Tesselating block in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
			throw new ReportedException(crashReport);
		}
	}

	public boolean renderLiquid(BlockPos blockPos, BlockAndBiomeGetter blockAndBiomeGetter, VertexConsumer vertexConsumer, FluidState fluidState) {
		try {
			return this.liquidBlockRenderer.tesselate(blockAndBiomeGetter, blockPos, vertexConsumer, fluidState);
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

	public void renderSingleBlock(BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		RenderShape renderShape = blockState.getRenderShape();
		if (renderShape != RenderShape.INVISIBLE) {
			switch (renderShape) {
				case MODEL:
					BakedModel bakedModel = this.getBlockModel(blockState);
					poseStack.pushPose();
					poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
					int k = this.blockColors.getColor(blockState, null, null, 0);
					float f = (float)(k >> 16 & 0xFF) / 255.0F;
					float g = (float)(k >> 8 & 0xFF) / 255.0F;
					float h = (float)(k & 0xFF) / 255.0F;
					this.modelRenderer
						.renderModel(
							poseStack.getPose(),
							poseStack.getNormal(),
							multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState)),
							blockState,
							bakedModel,
							f,
							g,
							h,
							i,
							j
						);
					poseStack.popPose();
					break;
				case ENTITYBLOCK_ANIMATED:
					poseStack.pushPose();
					poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
					EntityBlockRenderer.instance.renderByItem(new ItemStack(blockState.getBlock()), poseStack, multiBufferSource, i, j);
					poseStack.popPose();
			}
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.liquidBlockRenderer.setupSprites();
	}
}
