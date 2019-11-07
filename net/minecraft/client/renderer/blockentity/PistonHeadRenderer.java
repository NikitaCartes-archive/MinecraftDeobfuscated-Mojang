/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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

    public PistonHeadRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(PistonMovingBlockEntity pistonMovingBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        Level level = pistonMovingBlockEntity.getLevel();
        if (level == null) {
            return;
        }
        BlockPos blockPos = pistonMovingBlockEntity.getBlockPos().relative(pistonMovingBlockEntity.getMovementDirection().getOpposite());
        BlockState blockState = pistonMovingBlockEntity.getMovedState();
        if (blockState.isAir() || pistonMovingBlockEntity.getProgress(f) >= 1.0f) {
            return;
        }
        ModelBlockRenderer.enableCaching();
        poseStack.pushPose();
        poseStack.translate(pistonMovingBlockEntity.getXOff(f), pistonMovingBlockEntity.getYOff(f), pistonMovingBlockEntity.getZOff(f));
        if (blockState.getBlock() == Blocks.PISTON_HEAD && pistonMovingBlockEntity.getProgress(f) <= 4.0f) {
            blockState = (BlockState)blockState.setValue(PistonHeadBlock.SHORT, true);
            this.renderBlock(blockPos, blockState, poseStack, multiBufferSource, level, false, j);
        } else if (pistonMovingBlockEntity.isSourcePiston() && !pistonMovingBlockEntity.isExtending()) {
            PistonType pistonType = blockState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockState2 = (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistonType)).setValue(PistonHeadBlock.FACING, blockState.getValue(PistonBaseBlock.FACING));
            blockState2 = (BlockState)blockState2.setValue(PistonHeadBlock.SHORT, pistonMovingBlockEntity.getProgress(f) >= 0.5f);
            this.renderBlock(blockPos, blockState2, poseStack, multiBufferSource, level, false, j);
            BlockPos blockPos2 = blockPos.relative(pistonMovingBlockEntity.getMovementDirection());
            poseStack.popPose();
            poseStack.pushPose();
            blockState = (BlockState)blockState.setValue(PistonBaseBlock.EXTENDED, true);
            this.renderBlock(blockPos2, blockState, poseStack, multiBufferSource, level, true, j);
        } else {
            this.renderBlock(blockPos, blockState, poseStack, multiBufferSource, level, false, j);
        }
        poseStack.popPose();
        ModelBlockRenderer.clearCache();
    }

    private void renderBlock(BlockPos blockPos, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, Level level, boolean bl, int i) {
        RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
        this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, poseStack, vertexConsumer, bl, new Random(), blockState.getSeed(blockPos), i);
    }
}

