/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(value=EnvType.CLIENT)
public class BlockRenderDispatcher
implements ResourceManagerReloadListener {
    private final BlockModelShaper blockModelShaper;
    private final ModelBlockRenderer modelRenderer;
    private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final RandomSource random = RandomSource.create();
    private final BlockColors blockColors;

    public BlockRenderDispatcher(BlockModelShaper blockModelShaper, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer, BlockColors blockColors) {
        this.blockModelShaper = blockModelShaper;
        this.blockEntityRenderer = blockEntityWithoutLevelRenderer;
        this.blockColors = blockColors;
        this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer) {
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        BakedModel bakedModel = this.blockModelShaper.getBlockModel(blockState);
        long l = blockState.getSeed(blockPos);
        this.modelRenderer.tesselateBlock(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, true, this.random, l, OverlayTexture.NO_OVERLAY);
    }

    public void renderBatched(BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource) {
        try {
            RenderShape renderShape = blockState.getRenderShape();
            if (renderShape == RenderShape.MODEL) {
                this.modelRenderer.tesselateBlock(blockAndTintGetter, this.getBlockModel(blockState), blockState, blockPos, poseStack, vertexConsumer, bl, randomSource, blockState.getSeed(blockPos), OverlayTexture.NO_OVERLAY);
            }
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            throw new ReportedException(crashReport);
        }
    }

    public void renderLiquid(BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        try {
            this.liquidBlockRenderer.tesselate(blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating liquid in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, null);
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
        if (renderShape == RenderShape.INVISIBLE) {
            return;
        }
        switch (renderShape) {
            case MODEL: {
                BakedModel bakedModel = this.getBlockModel(blockState);
                int k = this.blockColors.getColor(blockState, null, null, 0);
                float f = (float)(k >> 16 & 0xFF) / 255.0f;
                float g = (float)(k >> 8 & 0xFF) / 255.0f;
                float h = (float)(k & 0xFF) / 255.0f;
                this.modelRenderer.renderModel(poseStack.last(), multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState, false)), blockState, bakedModel, f, g, h, i, j);
                break;
            }
            case ENTITYBLOCK_ANIMATED: {
                this.blockEntityRenderer.renderByItem(new ItemStack(blockState.getBlock()), ItemDisplayContext.NONE, poseStack, multiBufferSource, i, j);
            }
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.liquidBlockRenderer.setupSprites();
    }
}

