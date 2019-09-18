/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

@Environment(value=EnvType.CLIENT)
public class GameTestDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Map<BlockPos, Marker> markers = Maps.newHashMap();

    public void addMarker(BlockPos blockPos, int i, String string, int j) {
        this.markers.put(blockPos, new Marker(i, string, Util.getMillis() + (long)j));
    }

    @Override
    public void clear() {
        this.markers.clear();
    }

    @Environment(value=EnvType.CLIENT)
    static class Marker {
        public int color;
        public String text;
        public long removeAtTime;

        public Marker(int i, String string, long l) {
            this.color = i;
            this.text = string;
            this.removeAtTime = l;
        }
    }
}

