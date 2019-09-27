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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;

@Environment(value=EnvType.CLIENT)
public class StructureBlockRenderer
extends BlockEntityRenderer<StructureBlockEntity> {
    public StructureBlockRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(StructureBlockEntity structureBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        double r;
        double q;
        double p;
        double o;
        double n;
        double m;
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
        double h = blockPos.getX();
        double j = blockPos.getZ();
        double k = blockPos.getY();
        double l = k + (double)blockPos2.getY();
        switch (structureBlockEntity.getMirror()) {
            case LEFT_RIGHT: {
                m = blockPos2.getX();
                n = -blockPos2.getZ();
                break;
            }
            case FRONT_BACK: {
                m = -blockPos2.getX();
                n = blockPos2.getZ();
                break;
            }
            default: {
                m = blockPos2.getX();
                n = blockPos2.getZ();
            }
        }
        switch (structureBlockEntity.getRotation()) {
            case CLOCKWISE_90: {
                o = n < 0.0 ? h : h + 1.0;
                p = m < 0.0 ? j + 1.0 : j;
                q = o - n;
                r = p + m;
                break;
            }
            case CLOCKWISE_180: {
                o = m < 0.0 ? h : h + 1.0;
                p = n < 0.0 ? j : j + 1.0;
                q = o - m;
                r = p - n;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                o = n < 0.0 ? h + 1.0 : h;
                p = m < 0.0 ? j : j + 1.0;
                q = o + n;
                r = p - m;
                break;
            }
            default: {
                o = m < 0.0 ? h + 1.0 : h;
                p = n < 0.0 ? j + 1.0 : j;
                q = o + m;
                r = p + n;
            }
        }
        float s = 1.0f;
        float t = 0.9f;
        float u = 0.5f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LINES);
        if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, o, k, p, q, l, r, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
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
            boolean bl3;
            BlockState blockState = blockGetter.getBlockState(blockPos4);
            boolean bl2 = blockState.isAir();
            boolean bl4 = bl3 = blockState.getBlock() == Blocks.STRUCTURE_VOID;
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

