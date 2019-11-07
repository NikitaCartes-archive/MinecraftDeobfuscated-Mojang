/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(value=EnvType.CLIENT)
public class SolidFaceRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public SolidFaceRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, long l) {
        Level blockGetter = this.minecraft.player.level;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        BlockPos blockPos = new BlockPos(d, e, f);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-6, -6, -6), blockPos.offset(6, 6, 6))) {
            BlockState blockState = blockGetter.getBlockState(blockPos2);
            if (blockState.getBlock() == Blocks.AIR) continue;
            VoxelShape voxelShape = blockState.getShape(blockGetter, blockPos2);
            for (AABB aABB : voxelShape.toAabbs()) {
                BufferBuilder bufferBuilder;
                Tesselator tesselator;
                AABB aABB2 = aABB.move(blockPos2).inflate(0.002).move(-d, -e, -f);
                double g = aABB2.minX;
                double h = aABB2.minY;
                double i = aABB2.minZ;
                double j = aABB2.maxX;
                double k = aABB2.maxY;
                double m = aABB2.maxZ;
                float n = 1.0f;
                float o = 0.0f;
                float p = 0.0f;
                float q = 0.5f;
                if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.WEST)) {
                    tesselator = Tesselator.getInstance();
                    bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.vertex(g, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, h, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, k, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, k, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    tesselator.end();
                }
                if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.SOUTH)) {
                    tesselator = Tesselator.getInstance();
                    bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.vertex(g, k, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, h, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, k, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, h, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    tesselator.end();
                }
                if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.EAST)) {
                    tesselator = Tesselator.getInstance();
                    bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.vertex(j, h, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, k, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, k, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    tesselator.end();
                }
                if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.NORTH)) {
                    tesselator = Tesselator.getInstance();
                    bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.vertex(j, k, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, k, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    tesselator.end();
                }
                if (blockState.isFaceSturdy(blockGetter, blockPos2, Direction.DOWN)) {
                    tesselator = Tesselator.getInstance();
                    bufferBuilder = tesselator.getBuilder();
                    bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.vertex(g, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, h, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(g, h, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    bufferBuilder.vertex(j, h, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                    tesselator.end();
                }
                if (!blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP)) continue;
                tesselator = Tesselator.getInstance();
                bufferBuilder = tesselator.getBuilder();
                bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.vertex(g, k, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(g, k, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(j, k, i).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(j, k, m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                tesselator.end();
            }
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}

