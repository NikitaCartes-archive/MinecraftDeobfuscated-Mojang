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
    public void render(StructureBlockEntity structureBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        double s;
        double r;
        double q;
        double p;
        double o;
        double n;
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
        double k = blockPos.getZ();
        double l = blockPos.getY();
        double m = l + (double)blockPos2.getY();
        switch (structureBlockEntity.getMirror()) {
            case LEFT_RIGHT: {
                n = blockPos2.getX();
                o = -blockPos2.getZ();
                break;
            }
            case FRONT_BACK: {
                n = -blockPos2.getX();
                o = blockPos2.getZ();
                break;
            }
            default: {
                n = blockPos2.getX();
                o = blockPos2.getZ();
            }
        }
        switch (structureBlockEntity.getRotation()) {
            case CLOCKWISE_90: {
                p = o < 0.0 ? h : h + 1.0;
                q = n < 0.0 ? k + 1.0 : k;
                r = p - o;
                s = q + n;
                break;
            }
            case CLOCKWISE_180: {
                p = n < 0.0 ? h : h + 1.0;
                q = o < 0.0 ? k : k + 1.0;
                r = p - n;
                s = q - o;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                p = o < 0.0 ? h + 1.0 : h;
                q = n < 0.0 ? k : k + 1.0;
                r = p + o;
                s = q - n;
                break;
            }
            default: {
                p = n < 0.0 ? h + 1.0 : h;
                q = o < 0.0 ? k + 1.0 : k;
                r = p + n;
                s = q + o;
            }
        }
        float t = 1.0f;
        float u = 0.9f;
        float v = 0.5f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, p, l, q, r, m, s, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
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

