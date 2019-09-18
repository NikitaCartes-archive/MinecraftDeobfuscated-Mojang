/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(value=EnvType.CLIENT)
public abstract class BatchedBlockEntityRenderer<T extends BlockEntity>
extends BlockEntityRenderer<T> {
    @Override
    public final void setupAndRender(T blockEntity, double d, double e, double f, float g, int i, BufferBuilder bufferBuilder, RenderType renderType, BlockPos blockPos) {
        this.renderToBuffer(blockEntity, (double)blockPos.getX() - BlockEntityRenderDispatcher.xOff, (double)blockPos.getY() - BlockEntityRenderDispatcher.yOff, (double)blockPos.getZ() - BlockEntityRenderDispatcher.zOff, g, i, renderType, bufferBuilder);
    }

    private void renderToBuffer(T blockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder) {
        int l;
        int k;
        Level level = ((BlockEntity)blockEntity).getLevel();
        if (level != null) {
            int j = level.getLightColor(((BlockEntity)blockEntity).getBlockPos());
            k = j >> 16;
            l = j & 0xFFFF;
        } else {
            k = 240;
            l = 240;
        }
        bufferBuilder.offset(d, e, f);
        this.renderToBuffer(blockEntity, d, e, f, g, i, renderType, bufferBuilder, k, l);
        bufferBuilder.offset(0.0, 0.0, 0.0);
    }

    @Override
    public final void render(T blockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
        this.renderToBuffer(blockEntity, d, e, f, g, i, renderType, bufferBuilder);
        bufferBuilder.offset(0.0, 0.0, 0.0);
        renderType.setupRenderState();
        tesselator.end();
        renderType.clearRenderState();
    }

    protected abstract void renderToBuffer(T var1, double var2, double var4, double var6, float var8, int var9, RenderType var10, BufferBuilder var11, int var12, int var13);

    protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getTextureAtlas().getSprite(resourceLocation);
    }
}

