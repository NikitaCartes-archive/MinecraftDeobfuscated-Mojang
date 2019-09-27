/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class NeighborsUpdateRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

    NeighborsUpdateRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void addUpdate(long l, BlockPos blockPos) {
        Integer integer;
        Map<BlockPos, Integer> map = this.lastUpdate.get(l);
        if (map == null) {
            map = Maps.newHashMap();
            this.lastUpdate.put(l, map);
        }
        if ((integer = map.get(blockPos)) == null) {
            integer = 0;
        }
        map.put(blockPos, integer + 1);
    }

    @Override
    public void render(long l) {
        long m = this.minecraft.level.getGameTime();
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double f = camera.getPosition().z;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        int i = 200;
        double g = 0.0025;
        HashSet<BlockPos> set = Sets.newHashSet();
        HashMap<BlockPos, Integer> map = Maps.newHashMap();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.LINES);
        Iterator<Map.Entry<Long, Map<BlockPos, Integer>>> iterator = this.lastUpdate.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Map<BlockPos, Integer>> entry = iterator.next();
            Long long_ = entry.getKey();
            Map<BlockPos, Integer> map2 = entry.getValue();
            long n = m - long_;
            if (n > 200L) {
                iterator.remove();
                continue;
            }
            for (Map.Entry<BlockPos, Integer> entry2 : map2.entrySet()) {
                BlockPos blockPos = entry2.getKey();
                Integer integer = entry2.getValue();
                if (!set.add(blockPos)) continue;
                AABB aABB = new AABB(BlockPos.ZERO).inflate(0.002).deflate(0.0025 * (double)n).move(blockPos.getX(), blockPos.getY(), blockPos.getZ()).move(-d, -e, -f);
                LevelRenderer.renderLineBox(vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, 1.0f, 1.0f, 1.0f, 1.0f);
                map.put(blockPos, integer);
            }
        }
        bufferSource.endBatch();
        for (Map.Entry entry : map.entrySet()) {
            BlockPos blockPos2 = (BlockPos)entry.getKey();
            Integer integer2 = (Integer)entry.getValue();
            DebugRenderer.renderFloatingText(String.valueOf(integer2), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), -1);
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}

