/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

@Environment(value=EnvType.CLIENT)
public class CaveDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<BlockPos, BlockPos> tunnelsList = Maps.newHashMap();
    private final Map<BlockPos, Float> thicknessMap = Maps.newHashMap();
    private final List<BlockPos> startPoses = Lists.newArrayList();

    public CaveDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addTunnel(BlockPos blockPos, List<BlockPos> list, List<Float> list2) {
        for (int i = 0; i < list.size(); ++i) {
            this.tunnelsList.put(list.get(i), blockPos);
            this.thicknessMap.put(list.get(i), list2.get(i));
        }
        this.startPoses.add(blockPos);
    }

    @Override
    public void render(long l) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double f = camera.getPosition().z;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.disableTexture();
        BlockPos blockPos = new BlockPos(camera.getPosition().x, 0.0, camera.getPosition().z);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        for (Map.Entry<BlockPos, BlockPos> entry : this.tunnelsList.entrySet()) {
            BlockPos blockPos2 = entry.getKey();
            BlockPos blockPos3 = entry.getValue();
            float g = (float)(blockPos3.getX() * 128 % 256) / 256.0f;
            float h = (float)(blockPos3.getY() * 128 % 256) / 256.0f;
            float i = (float)(blockPos3.getZ() * 128 % 256) / 256.0f;
            float j = this.thicknessMap.get(blockPos2).floatValue();
            if (!blockPos.closerThan(blockPos2, 160.0)) continue;
            LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)blockPos2.getX() + 0.5f) - d - (double)j, (double)((float)blockPos2.getY() + 0.5f) - e - (double)j, (double)((float)blockPos2.getZ() + 0.5f) - f - (double)j, (double)((float)blockPos2.getX() + 0.5f) - d + (double)j, (double)((float)blockPos2.getY() + 0.5f) - e + (double)j, (double)((float)blockPos2.getZ() + 0.5f) - f + (double)j, g, h, i, 0.5f);
        }
        for (BlockPos blockPos4 : this.startPoses) {
            if (!blockPos.closerThan(blockPos4, 160.0)) continue;
            LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)blockPos4.getX() - d, (double)blockPos4.getY() - e, (double)blockPos4.getZ() - f, (double)((float)blockPos4.getX() + 1.0f) - d, (double)((float)blockPos4.getY() + 1.0f) - e, (double)((float)blockPos4.getZ() + 1.0f) - f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        tesselator.end();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}

