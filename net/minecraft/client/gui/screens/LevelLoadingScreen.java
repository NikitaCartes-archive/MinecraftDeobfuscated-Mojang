/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkStatus;

@Environment(value=EnvType.CLIENT)
public class LevelLoadingScreen
extends Screen {
    private static final long NARRATION_DELAY_MS = 2000L;
    private final StoringChunkProgressListener progressListener;
    private long lastNarration = -1L;
    private boolean done;
    private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.defaultReturnValue(0);
        object2IntOpenHashMap.put(ChunkStatus.EMPTY, 0x545454);
        object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_STARTS, 0x999999);
        object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        object2IntOpenHashMap.put(ChunkStatus.BIOMES, 8434258);
        object2IntOpenHashMap.put(ChunkStatus.NOISE, 0xD1D1D1);
        object2IntOpenHashMap.put(ChunkStatus.SURFACE, 7497737);
        object2IntOpenHashMap.put(ChunkStatus.CARVERS, 7169628);
        object2IntOpenHashMap.put(ChunkStatus.LIQUID_CARVERS, 3159410);
        object2IntOpenHashMap.put(ChunkStatus.FEATURES, 2213376);
        object2IntOpenHashMap.put(ChunkStatus.LIGHT, 0xCCCCCC);
        object2IntOpenHashMap.put(ChunkStatus.SPAWN, 15884384);
        object2IntOpenHashMap.put(ChunkStatus.HEIGHTMAPS, 0xEEEEEE);
        object2IntOpenHashMap.put(ChunkStatus.FULL, 0xFFFFFF);
    });

    public LevelLoadingScreen(StoringChunkProgressListener storingChunkProgressListener) {
        super(GameNarrator.NO_TITLE);
        this.progressListener = storingChunkProgressListener;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void removed() {
        this.done = true;
        this.triggerImmediateNarration(true);
    }

    @Override
    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        if (this.done) {
            narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narrator.loading.done"));
        } else {
            String string = this.getFormattedProgress();
            narrationElementOutput.add(NarratedElementType.TITLE, string);
        }
    }

    private String getFormattedProgress() {
        return Mth.clamp(this.progressListener.getProgress(), 0, 100) + "%";
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        long l = Util.getMillis();
        if (l - this.lastNarration > 2000L) {
            this.lastNarration = l;
            this.triggerImmediateNarration(true);
        }
        int k = this.width / 2;
        int m = this.height / 2;
        int n = 30;
        LevelLoadingScreen.renderChunks(poseStack, this.progressListener, k, m + 30, 2, 0);
        LevelLoadingScreen.drawCenteredString(poseStack, this.font, this.getFormattedProgress(), k, m - this.font.lineHeight / 2 - 30, 0xFFFFFF);
    }

    public static void renderChunks(PoseStack poseStack, StoringChunkProgressListener storingChunkProgressListener, int i, int j, int k, int l) {
        int m = k + l;
        int n = storingChunkProgressListener.getFullDiameter();
        int o = n * m - l;
        int p = storingChunkProgressListener.getDiameter();
        int q = p * m - l;
        int r = i - q / 2;
        int s = j - q / 2;
        int t = o / 2 + 1;
        int u = -16772609;
        if (l != 0) {
            LevelLoadingScreen.fill(poseStack, i - t, j - t, i - t + 1, j + t, -16772609);
            LevelLoadingScreen.fill(poseStack, i + t - 1, j - t, i + t, j + t, -16772609);
            LevelLoadingScreen.fill(poseStack, i - t, j - t, i + t, j - t + 1, -16772609);
            LevelLoadingScreen.fill(poseStack, i - t, j + t - 1, i + t, j + t, -16772609);
        }
        for (int v = 0; v < p; ++v) {
            for (int w = 0; w < p; ++w) {
                ChunkStatus chunkStatus = storingChunkProgressListener.getStatus(v, w);
                int x = r + v * m;
                int y = s + w * m;
                LevelLoadingScreen.fill(poseStack, x, y, x + k, y + k, COLORS.getInt(chunkStatus) | 0xFF000000);
            }
        }
    }
}

