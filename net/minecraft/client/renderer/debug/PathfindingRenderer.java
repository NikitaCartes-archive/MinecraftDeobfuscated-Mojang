/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class PathfindingRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Map<Integer, Path> pathMap = Maps.newHashMap();
    private final Map<Integer, Float> pathMaxDist = Maps.newHashMap();
    private final Map<Integer, Long> creationMap = Maps.newHashMap();
    private static final long TIMEOUT = 5000L;
    private static final float MAX_RENDER_DIST = 80.0f;
    private static final boolean SHOW_OPEN_CLOSED = true;
    private static final boolean SHOW_OPEN_CLOSED_COST_MALUS = false;
    private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_TEXT = false;
    private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_BOX = true;
    private static final boolean SHOW_GROUND_LABELS = true;
    private static final float TEXT_SCALE = 0.02f;

    public void addPath(int i, Path path, float f) {
        this.pathMap.put(i, path);
        this.creationMap.put(i, Util.getMillis());
        this.pathMaxDist.put(i, Float.valueOf(f));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        if (this.pathMap.isEmpty()) {
            return;
        }
        long l = Util.getMillis();
        for (Integer integer : this.pathMap.keySet()) {
            Path path = this.pathMap.get(integer);
            float g = this.pathMaxDist.get(integer).floatValue();
            PathfindingRenderer.renderPath(poseStack, multiBufferSource, path, g, true, true, d, e, f);
        }
        for (Integer integer2 : this.creationMap.keySet().toArray(new Integer[0])) {
            if (l - this.creationMap.get(integer2) <= 5000L) continue;
            this.pathMap.remove(integer2);
            this.creationMap.remove(integer2);
        }
    }

    public static void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, Path path, float f, boolean bl, boolean bl2, double d, double e, double g) {
        int i;
        PathfindingRenderer.renderPathLine(poseStack, multiBufferSource.getBuffer(RenderType.debugLineStrip(6.0)), path, d, e, g);
        BlockPos blockPos = path.getTarget();
        if (PathfindingRenderer.distanceToCamera(blockPos, d, e, g) <= 80.0f) {
            DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)blockPos.getX() + 0.25f, (float)blockPos.getY() + 0.25f, (double)blockPos.getZ() + 0.25, (float)blockPos.getX() + 0.75f, (float)blockPos.getY() + 0.75f, (float)blockPos.getZ() + 0.75f).move(-d, -e, -g), 0.0f, 1.0f, 0.0f, 0.5f);
            for (i = 0; i < path.getNodeCount(); ++i) {
                Node node = path.getNode(i);
                if (!(PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, e, g) <= 80.0f)) continue;
                float h = i == path.getNextNodeIndex() ? 1.0f : 0.0f;
                float j = i == path.getNextNodeIndex() ? 0.0f : 1.0f;
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)node.x + 0.5f - f, (float)node.y + 0.01f * (float)i, (float)node.z + 0.5f - f, (float)node.x + 0.5f + f, (float)node.y + 0.25f + 0.01f * (float)i, (float)node.z + 0.5f + f).move(-d, -e, -g), h, 0.0f, j, 0.5f);
            }
        }
        if (bl) {
            for (Node node2 : path.getClosedSet()) {
                if (!(PathfindingRenderer.distanceToCamera(node2.asBlockPos(), d, e, g) <= 80.0f)) continue;
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)node2.x + 0.5f - f / 2.0f, (float)node2.y + 0.01f, (float)node2.z + 0.5f - f / 2.0f, (float)node2.x + 0.5f + f / 2.0f, (double)node2.y + 0.1, (float)node2.z + 0.5f + f / 2.0f).move(-d, -e, -g), 1.0f, 0.8f, 0.8f, 0.5f);
            }
            for (Node node2 : path.getOpenSet()) {
                if (!(PathfindingRenderer.distanceToCamera(node2.asBlockPos(), d, e, g) <= 80.0f)) continue;
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, new AABB((float)node2.x + 0.5f - f / 2.0f, (float)node2.y + 0.01f, (float)node2.z + 0.5f - f / 2.0f, (float)node2.x + 0.5f + f / 2.0f, (double)node2.y + 0.1, (float)node2.z + 0.5f + f / 2.0f).move(-d, -e, -g), 0.8f, 1.0f, 1.0f, 0.5f);
            }
        }
        if (bl2) {
            for (i = 0; i < path.getNodeCount(); ++i) {
                Node node = path.getNode(i);
                if (!(PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, e, g) <= 80.0f)) continue;
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf((Object)node.type), (double)node.x + 0.5, (double)node.y + 0.75, (double)node.z + 0.5, -1, 0.02f, true, 0.0f, true);
                DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.format(Locale.ROOT, "%.2f", Float.valueOf(node.costMalus)), (double)node.x + 0.5, (double)node.y + 0.25, (double)node.z + 0.5, -1, 0.02f, true, 0.0f, true);
            }
        }
    }

    public static void renderPathLine(PoseStack poseStack, VertexConsumer vertexConsumer, Path path, double d, double e, double f) {
        for (int i = 0; i < path.getNodeCount(); ++i) {
            Node node = path.getNode(i);
            if (PathfindingRenderer.distanceToCamera(node.asBlockPos(), d, e, f) > 80.0f) continue;
            float g = (float)i / (float)path.getNodeCount() * 0.33f;
            int j = i == 0 ? 0 : Mth.hsvToRgb(g, 0.9f, 0.9f);
            int k = j >> 16 & 0xFF;
            int l = j >> 8 & 0xFF;
            int m = j & 0xFF;
            vertexConsumer.vertex(poseStack.last().pose(), (float)((double)node.x - d + 0.5), (float)((double)node.y - e + 0.5), (float)((double)node.z - f + 0.5)).color(k, l, m, 255).endVertex();
        }
    }

    private static float distanceToCamera(BlockPos blockPos, double d, double e, double f) {
        return (float)(Math.abs((double)blockPos.getX() - d) + Math.abs((double)blockPos.getY() - e) + Math.abs((double)blockPos.getZ() - f));
    }
}

