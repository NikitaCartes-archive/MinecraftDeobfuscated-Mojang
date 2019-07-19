/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
    @Override
    public void render(StructureBlockEntity structureBlockEntity, double d, double e, double f, float g, int i) {
        double s;
        double r;
        double q;
        double p;
        double o;
        double n;
        if (!Minecraft.getInstance().player.canUseGameMasterBlocks() && !Minecraft.getInstance().player.isSpectator()) {
            return;
        }
        super.render(structureBlockEntity, d, e, f, g, i);
        BlockPos blockPos = structureBlockEntity.getStructurePos();
        BlockPos blockPos2 = structureBlockEntity.getStructureSize();
        if (blockPos2.getX() < 1 || blockPos2.getY() < 1 || blockPos2.getZ() < 1) {
            return;
        }
        if (structureBlockEntity.getMode() != StructureMode.SAVE && structureBlockEntity.getMode() != StructureMode.LOAD) {
            return;
        }
        double h = 0.01;
        double j = blockPos.getX();
        double k = blockPos.getZ();
        double l = e + (double)blockPos.getY() - 0.01;
        double m = l + (double)blockPos2.getY() + 0.02;
        switch (structureBlockEntity.getMirror()) {
            case LEFT_RIGHT: {
                n = (double)blockPos2.getX() + 0.02;
                o = -((double)blockPos2.getZ() + 0.02);
                break;
            }
            case FRONT_BACK: {
                n = -((double)blockPos2.getX() + 0.02);
                o = (double)blockPos2.getZ() + 0.02;
                break;
            }
            default: {
                n = (double)blockPos2.getX() + 0.02;
                o = (double)blockPos2.getZ() + 0.02;
            }
        }
        switch (structureBlockEntity.getRotation()) {
            case CLOCKWISE_90: {
                p = d + (o < 0.0 ? j - 0.01 : j + 1.0 + 0.01);
                q = f + (n < 0.0 ? k + 1.0 + 0.01 : k - 0.01);
                r = p - o;
                s = q + n;
                break;
            }
            case CLOCKWISE_180: {
                p = d + (n < 0.0 ? j - 0.01 : j + 1.0 + 0.01);
                q = f + (o < 0.0 ? k - 0.01 : k + 1.0 + 0.01);
                r = p - n;
                s = q - o;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                p = d + (o < 0.0 ? j + 1.0 + 0.01 : j - 0.01);
                q = f + (n < 0.0 ? k - 0.01 : k + 1.0 + 0.01);
                r = p + o;
                s = q - n;
                break;
            }
            default: {
                p = d + (n < 0.0 ? j + 1.0 + 0.01 : j - 0.01);
                q = f + (o < 0.0 ? k + 1.0 + 0.01 : k - 0.01);
                r = p + n;
                s = q + o;
            }
        }
        int t = 255;
        int u = 223;
        int v = 127;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        this.setOverlayRenderState(true);
        if (structureBlockEntity.getMode() == StructureMode.SAVE || structureBlockEntity.getShowBoundingBox()) {
            this.renderBox(tesselator, bufferBuilder, p, l, q, r, m, s, 255, 223, 127);
        }
        if (structureBlockEntity.getMode() == StructureMode.SAVE && structureBlockEntity.getShowAir()) {
            this.renderInvisibleBlocks(structureBlockEntity, d, e, f, blockPos, tesselator, bufferBuilder, true);
            this.renderInvisibleBlocks(structureBlockEntity, d, e, f, blockPos, tesselator, bufferBuilder, false);
        }
        this.setOverlayRenderState(false);
        GlStateManager.lineWidth(1.0f);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
        GlStateManager.enableFog();
    }

    private void renderInvisibleBlocks(StructureBlockEntity structureBlockEntity, double d, double e, double f, BlockPos blockPos, Tesselator tesselator, BufferBuilder bufferBuilder, boolean bl) {
        GlStateManager.lineWidth(bl ? 3.0f : 1.0f);
        bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
        Level blockGetter = structureBlockEntity.getLevel();
        BlockPos blockPos2 = structureBlockEntity.getBlockPos();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos3, blockPos3.offset(structureBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
            boolean bl3;
            BlockState blockState = blockGetter.getBlockState(blockPos4);
            boolean bl2 = blockState.isAir();
            boolean bl4 = bl3 = blockState.getBlock() == Blocks.STRUCTURE_VOID;
            if (!bl2 && !bl3) continue;
            float g = bl2 ? 0.05f : 0.0f;
            double h = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.45f) + d - (double)g;
            double i = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.45f) + e - (double)g;
            double j = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.45f) + f - (double)g;
            double k = (double)((float)(blockPos4.getX() - blockPos2.getX()) + 0.55f) + d + (double)g;
            double l = (double)((float)(blockPos4.getY() - blockPos2.getY()) + 0.55f) + e + (double)g;
            double m = (double)((float)(blockPos4.getZ() - blockPos2.getZ()) + 0.55f) + f + (double)g;
            if (bl) {
                LevelRenderer.addChainedLineBoxVertices(bufferBuilder, h, i, j, k, l, m, 0.0f, 0.0f, 0.0f, 1.0f);
                continue;
            }
            if (bl2) {
                LevelRenderer.addChainedLineBoxVertices(bufferBuilder, h, i, j, k, l, m, 0.5f, 0.5f, 1.0f, 1.0f);
                continue;
            }
            LevelRenderer.addChainedLineBoxVertices(bufferBuilder, h, i, j, k, l, m, 1.0f, 0.25f, 0.25f, 1.0f);
        }
        tesselator.end();
    }

    private void renderBox(Tesselator tesselator, BufferBuilder bufferBuilder, double d, double e, double f, double g, double h, double i, int j, int k, int l) {
        GlStateManager.lineWidth(2.0f);
        bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(d, e, f).color((float)k, (float)k, (float)k, 0.0f).endVertex();
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
        bufferBuilder.vertex(g, e, f).color((float)k, (float)k, (float)k, 0.0f).endVertex();
        tesselator.end();
        GlStateManager.lineWidth(1.0f);
    }

    @Override
    public boolean shouldRenderOffScreen(StructureBlockEntity structureBlockEntity) {
        return true;
    }
}

