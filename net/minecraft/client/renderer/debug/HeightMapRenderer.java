/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

@Environment(value=EnvType.CLIENT)
public class HeightMapRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public HeightMapRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(long l) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        MultiPlayerLevel levelAccessor = this.minecraft.level;
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double f = camera.getPosition().z;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        BlockPos blockPos = new BlockPos(camera.getPosition().x, 0.0, camera.getPosition().z);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-40, 0, -40), blockPos.offset(40, 0, 40))) {
            int i;
            if (levelAccessor.getBlockState(blockPos2.offset(0, i = levelAccessor.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockPos2.getX(), blockPos2.getZ()), 0).below()).isAir()) {
                LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)blockPos2.getX() + 0.25f) - d, (double)i - e, (double)((float)blockPos2.getZ() + 0.25f) - f, (double)((float)blockPos2.getX() + 0.75f) - d, (double)i + 0.09375 - e, (double)((float)blockPos2.getZ() + 0.75f) - f, 0.0f, 0.0f, 1.0f, 0.5f);
                continue;
            }
            LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)blockPos2.getX() + 0.25f) - d, (double)i - e, (double)((float)blockPos2.getZ() + 0.25f) - f, (double)((float)blockPos2.getX() + 0.75f) - d, (double)i + 0.09375 - e, (double)((float)blockPos2.getZ() + 0.75f) - f, 0.0f, 1.0f, 0.0f, 0.5f);
        }
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}

