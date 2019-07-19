/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.lineWidth(2.0f);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        int i = 200;
        double g = 0.0025;
        HashSet<BlockPos> set = Sets.newHashSet();
        HashMap<BlockPos, Integer> map = Maps.newHashMap();
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
                LevelRenderer.renderLineBox(new AABB(BlockPos.ZERO).inflate(0.002).deflate(0.0025 * (double)n).move(blockPos.getX(), blockPos.getY(), blockPos.getZ()).move(-d, -e, -f), 1.0f, 1.0f, 1.0f, 1.0f);
                map.put(blockPos, integer);
            }
        }
        for (Map.Entry entry : map.entrySet()) {
            BlockPos blockPos2 = (BlockPos)entry.getKey();
            Integer integer2 = (Integer)entry.getValue();
            DebugRenderer.renderFloatingText(String.valueOf(integer2), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), -1);
        }
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
}

