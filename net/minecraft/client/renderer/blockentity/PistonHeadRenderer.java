/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

@Environment(value=EnvType.CLIENT)
public class PistonHeadRenderer
extends BlockEntityRenderer<PistonMovingBlockEntity> {
    private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

    @Override
    public void render(PistonMovingBlockEntity pistonMovingBlockEntity, double d, double e, double f, float g, int i) {
        BlockPos blockPos = pistonMovingBlockEntity.getBlockPos().relative(pistonMovingBlockEntity.getMovementDirection().getOpposite());
        BlockState blockState = pistonMovingBlockEntity.getMovedState();
        if (blockState.isAir() || pistonMovingBlockEntity.getProgress(g) >= 1.0f) {
            return;
        }
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        Lighting.turnOff();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (Minecraft.useAmbientOcclusion()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }
        ModelBlockRenderer.enableCaching();
        bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
        bufferBuilder.offset(d - (double)blockPos.getX() + (double)pistonMovingBlockEntity.getXOff(g), e - (double)blockPos.getY() + (double)pistonMovingBlockEntity.getYOff(g), f - (double)blockPos.getZ() + (double)pistonMovingBlockEntity.getZOff(g));
        Level level = this.getLevel();
        if (blockState.getBlock() == Blocks.PISTON_HEAD && pistonMovingBlockEntity.getProgress(g) <= 4.0f) {
            blockState = (BlockState)blockState.setValue(PistonHeadBlock.SHORT, true);
            this.renderBlock(blockPos, blockState, bufferBuilder, level, false);
        } else if (pistonMovingBlockEntity.isSourcePiston() && !pistonMovingBlockEntity.isExtending()) {
            PistonType pistonType = blockState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockState2 = (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistonType)).setValue(PistonHeadBlock.FACING, blockState.getValue(PistonBaseBlock.FACING));
            blockState2 = (BlockState)blockState2.setValue(PistonHeadBlock.SHORT, pistonMovingBlockEntity.getProgress(g) >= 0.5f);
            this.renderBlock(blockPos, blockState2, bufferBuilder, level, false);
            BlockPos blockPos2 = blockPos.relative(pistonMovingBlockEntity.getMovementDirection());
            bufferBuilder.offset(d - (double)blockPos2.getX(), e - (double)blockPos2.getY(), f - (double)blockPos2.getZ());
            blockState = (BlockState)blockState.setValue(PistonBaseBlock.EXTENDED, true);
            this.renderBlock(blockPos2, blockState, bufferBuilder, level, true);
        } else {
            this.renderBlock(blockPos, blockState, bufferBuilder, level, false);
        }
        bufferBuilder.offset(0.0, 0.0, 0.0);
        tesselator.end();
        ModelBlockRenderer.clearCache();
        Lighting.turnOn();
    }

    private boolean renderBlock(BlockPos blockPos, BlockState blockState, BufferBuilder bufferBuilder, Level level, boolean bl) {
        return this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, bufferBuilder, bl, new Random(), blockState.getSeed(blockPos));
    }
}

