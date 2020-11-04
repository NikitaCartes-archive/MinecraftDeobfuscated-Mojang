/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

@Environment(value=EnvType.CLIENT)
public class StructureBlockRenderer
implements BlockEntityRenderer<StructureBlockEntity> {
    public StructureBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StructureBlockEntity structureBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        double p;
        double o;
        double n;
        double m;
        double l;
        double k;
        if (!Minecraft.getInstance().player.canUseGameMasterBlocks() && !Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        BlockPos blockPos = structureBlockEntity.getStructurePos();
        BlockPos blockPos2 = structureBlockEntity.getStructureSize();
        if (blockPos2.getX() < 1 || blockPos2.getY() < 1 || blockPos2.getZ() < 1) {
            return;
        }
        if (structureBlockEntity.getMode() != StructureMode.SAVE && structureBlockEntity.getMode() != StructureMode.LOAD) {
            return;
        }
        double d = blockPos.getX();
        double e = blockPos.getZ();
        double g = blockPos.getY();
        double h = g + (double)blockPos2.getY();
        switch (structureBlockEntity.getMirror()) {
            case LEFT_RIGHT: {
                k = blockPos2.getX();
                l = -blockPos2.getZ();
                break;
            }
            case FRONT_BACK: {
                k = -blockPos2.getX();
                l = blockPos2.getZ();
                break;
            }
            default: {
                k = blockPos2.getX();
                l = blockPos2.getZ();
            }
        }
        switch (structureBlockEntity.getRotation()) {
            case CLOCKWISE_90: {
                m = l < 0.0 ? d : d + 1.0;
                n = k < 0.0 ? e + 1.0 : e;
                o = m - l;
                p = n + k;
                break;
            }
            case CLOCKWISE_180: {
                m = k < 0.0 ? d : d + 1.0;
                n = l < 0.0 ? e : e + 1.0;
                o = m - k;
                p = n - l;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                m = l < 0.0 ? d + 1.0 : d;
                n = k < 0.0 ? e : e + 1.0;
                o = m + l;
                p = n - k;
                break;
            }
            default: {
                m = k < 0.0 ? d + 1.0 : d;
                n = l < 0.0 ? e + 1.0 : e;
                o = m + k;
                p = n + l;
            }
        }
        float q = 1.0f;
        float r = 0.9f;
        float s = 0.5f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
        }
        if (structureBlockEntity.getMode() == StructureMode.SAVE && structureBlockEntity.getShowAir()) {
            this.renderInvisibleBlocks(structureBlockEntity, vertexConsumer, blockPos, true, poseStack);
            this.renderInvisibleBlocks(structureBlockEntity, vertexConsumer, blockPos, false, poseStack);
        }
    }

    private void renderInvisibleBlocks(StructureBlockEntity structureBlockEntity, VertexConsumer vertexConsumer, BlockPos blockPos, boolean bl, PoseStack poseStack) {
        Level blockGetter = structureBlockEntity.getLevel();
        BlockPos blockPos2 = structureBlockEntity.getBlockPos();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos3, blockPos3.offset(structureBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
            BlockState blockState = blockGetter.getBlockState(blockPos4);
            boolean bl2 = blockState.isAir();
            boolean bl3 = blockState.is(Blocks.STRUCTURE_VOID);
            if (!bl2 && !bl3) continue;
            float f = bl2 ? 0.05f : 0.0f;
            double d = (float)(blockPos4.getX() - blockPos2.getX()) + 0.45f - f;
            double e = (float)(blockPos4.getY() - blockPos2.getY()) + 0.45f - f;
            double g = (float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45f - f;
            double h = (float)(blockPos4.getX() - blockPos2.getX()) + 0.55f + f;
            double i = (float)(blockPos4.getY() - blockPos2.getY()) + 0.55f + f;
            double j = (float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55f + f;
            if (bl) {
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
                continue;
            }
            if (bl2) {
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
                continue;
            }
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, d, e, g, h, i, j, 1.0f, 0.25f, 0.25f, 1.0f, 1.0f, 0.25f, 0.25f);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(StructureBlockEntity structureBlockEntity) {
        return true;
    }
}

