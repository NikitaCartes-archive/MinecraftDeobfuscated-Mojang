/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class PathfindingRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<Integer, Path> pathMap = Maps.newHashMap();
    private final Map<Integer, Float> pathMaxDist = Maps.newHashMap();
    private final Map<Integer, Long> creationMap = Maps.newHashMap();

    public PathfindingRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addPath(int i, Path path, float f) {
        this.pathMap.put(i, path);
        this.creationMap.put(i, Util.getMillis());
        this.pathMaxDist.put(i, Float.valueOf(f));
    }

    @Override
    public void render(long l) {
        if (this.pathMap.isEmpty()) {
            return;
        }
        long m = Util.getMillis();
        for (Integer integer : this.pathMap.keySet()) {
            Path path = this.pathMap.get(integer);
            float f = this.pathMaxDist.get(integer).floatValue();
            PathfindingRenderer.renderPath(this.getCamera(), path, f, true, true);
        }
        for (Integer integer2 : this.creationMap.keySet().toArray(new Integer[0])) {
            if (m - this.creationMap.get(integer2) <= 20000L) continue;
            this.pathMap.remove(integer2);
            this.creationMap.remove(integer2);
        }
    }

    public static void renderPath(Camera camera, Path path, float f, boolean bl, boolean bl2) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(0.0f, 1.0f, 0.0f, 0.75f);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0f);
        PathfindingRenderer.doRenderPath(camera, path, f, bl, bl2);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private static void doRenderPath(Camera camera, Path path, float f, boolean bl, boolean bl2) {
        int i;
        PathfindingRenderer.renderPathLine(camera, path);
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double g = camera.getPosition().z;
        BlockPos blockPos = path.getTarget();
        if (PathfindingRenderer.distanceToCamera(camera, blockPos) <= 40.0f) {
            DebugRenderer.renderFilledBox(new AABB((float)blockPos.getX() + 0.25f, (float)blockPos.getY() + 0.25f, (double)blockPos.getZ() + 0.25, (float)blockPos.getX() + 0.75f, (float)blockPos.getY() + 0.75f, (float)blockPos.getZ() + 0.75f).move(-d, -e, -g), 0.0f, 1.0f, 0.0f, 0.5f);
            for (i = 0; i < path.getSize(); ++i) {
                Node node = path.get(i);
                if (!(PathfindingRenderer.distanceToCamera(camera, node.asBlockPos()) <= 40.0f)) continue;
                float h = i == path.getIndex() ? 1.0f : 0.0f;
                float j = i == path.getIndex() ? 0.0f : 1.0f;
                DebugRenderer.renderFilledBox(new AABB((float)node.x + 0.5f - f, (float)node.y + 0.01f * (float)i, (float)node.z + 0.5f - f, (float)node.x + 0.5f + f, (float)node.y + 0.25f + 0.01f * (float)i, (float)node.z + 0.5f + f).move(-d, -e, -g), h, 0.0f, j, 0.5f);
            }
        }
        if (bl) {
            for (Node node2 : path.getClosedSet()) {
                if (!(PathfindingRenderer.distanceToCamera(camera, node2.asBlockPos()) <= 40.0f)) continue;
                DebugRenderer.renderFloatingText(String.format("%s", new Object[]{node2.type}), (double)node2.x + 0.5, (double)node2.y + 0.75, (double)node2.z + 0.5, -65536);
                DebugRenderer.renderFloatingText(String.format(Locale.ROOT, "%.2f", Float.valueOf(node2.costMalus)), (double)node2.x + 0.5, (double)node2.y + 0.25, (double)node2.z + 0.5, -65536);
            }
            for (Node node2 : path.getOpenSet()) {
                if (!(PathfindingRenderer.distanceToCamera(camera, node2.asBlockPos()) <= 40.0f)) continue;
                DebugRenderer.renderFloatingText(String.format("%s", new Object[]{node2.type}), (double)node2.x + 0.5, (double)node2.y + 0.75, (double)node2.z + 0.5, -16776961);
                DebugRenderer.renderFloatingText(String.format(Locale.ROOT, "%.2f", Float.valueOf(node2.costMalus)), (double)node2.x + 0.5, (double)node2.y + 0.25, (double)node2.z + 0.5, -16776961);
            }
        }
        if (bl2) {
            for (i = 0; i < path.getSize(); ++i) {
                Node node = path.get(i);
                if (!(PathfindingRenderer.distanceToCamera(camera, node.asBlockPos()) <= 40.0f)) continue;
                DebugRenderer.renderFloatingText(String.format("%s", new Object[]{node.type}), (double)node.x + 0.5, (double)node.y + 0.75, (double)node.z + 0.5, -1);
                DebugRenderer.renderFloatingText(String.format(Locale.ROOT, "%.2f", Float.valueOf(node.costMalus)), (double)node.x + 0.5, (double)node.y + 0.25, (double)node.z + 0.5, -1);
            }
        }
    }

    public static void renderPathLine(Camera camera, Path path) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double f = camera.getPosition().z;
        bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < path.getSize(); ++i) {
            Node node = path.get(i);
            if (PathfindingRenderer.distanceToCamera(camera, node.asBlockPos()) > 40.0f) continue;
            float g = (float)i / (float)path.getSize() * 0.33f;
            int j = i == 0 ? 0 : Mth.hsvToRgb(g, 0.9f, 0.9f);
            int k = j >> 16 & 0xFF;
            int l = j >> 8 & 0xFF;
            int m = j & 0xFF;
            bufferBuilder.vertex((double)node.x - d + 0.5, (double)node.y - e + 0.5, (double)node.z - f + 0.5).color(k, l, m, 255).endVertex();
        }
        tesselator.end();
    }

    private static float distanceToCamera(Camera camera, BlockPos blockPos) {
        return (float)(Math.abs((double)blockPos.getX() - camera.getPosition().x) + Math.abs((double)blockPos.getY() - camera.getPosition().y) + Math.abs((double)blockPos.getZ() - camera.getPosition().z));
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }
}

