/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;

@Environment(value=EnvType.CLIENT)
public class GameTestDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final float PADDING = 0.02f;
    private final Map<BlockPos, Marker> markers = Maps.newHashMap();

    public void addMarker(BlockPos blockPos, int i, String string, int j) {
        this.markers.put(blockPos, new Marker(i, string, Util.getMillis() + (long)j));
    }

    @Override
    public void clear() {
        this.markers.clear();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        long l = Util.getMillis();
        this.markers.entrySet().removeIf(entry -> l > ((Marker)entry.getValue()).removeAtTime);
        this.markers.forEach((blockPos, marker) -> this.renderMarker(poseStack, multiBufferSource, (BlockPos)blockPos, (Marker)marker));
    }

    private void renderMarker(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, Marker marker) {
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.02f, marker.getR(), marker.getG(), marker.getB(), marker.getA() * 0.75f);
        if (!marker.text.isEmpty()) {
            double d = (double)blockPos.getX() + 0.5;
            double e = (double)blockPos.getY() + 1.2;
            double f = (double)blockPos.getZ() + 0.5;
            DebugRenderer.renderFloatingText(poseStack, multiBufferSource, marker.text, d, e, f, -1, 0.01f, true, 0.0f, true);
        }
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

        public float getR() {
            return (float)(this.color >> 16 & 0xFF) / 255.0f;
        }

        public float getG() {
            return (float)(this.color >> 8 & 0xFF) / 255.0f;
        }

        public float getB() {
            return (float)(this.color & 0xFF) / 255.0f;
        }

        public float getA() {
            return (float)(this.color >> 24 & 0xFF) / 255.0f;
        }
    }
}

