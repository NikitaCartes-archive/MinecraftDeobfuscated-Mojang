package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.status.ChunkStatus;

@Environment(EnvType.CLIENT)
public class LevelLoadingScreen extends Screen {
	private static final long NARRATION_DELAY_MS = 2000L;
	private final StoringChunkProgressListener progressListener;
	private long lastNarration = -1L;
	private boolean done;
	private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
		object2IntOpenHashMap.defaultReturnValue(0);
		object2IntOpenHashMap.put(ChunkStatus.EMPTY, 5526612);
		object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
		object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
		object2IntOpenHashMap.put(ChunkStatus.BIOMES, 8434258);
		object2IntOpenHashMap.put(ChunkStatus.NOISE, 13750737);
		object2IntOpenHashMap.put(ChunkStatus.SURFACE, 7497737);
		object2IntOpenHashMap.put(ChunkStatus.CARVERS, 3159410);
		object2IntOpenHashMap.put(ChunkStatus.FEATURES, 2213376);
		object2IntOpenHashMap.put(ChunkStatus.INITIALIZE_LIGHT, 13421772);
		object2IntOpenHashMap.put(ChunkStatus.LIGHT, 16769184);
		object2IntOpenHashMap.put(ChunkStatus.SPAWN, 15884384);
		object2IntOpenHashMap.put(ChunkStatus.FULL, 16777215);
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
			narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narrator.loading.done"));
		} else {
			narrationElementOutput.add(NarratedElementType.TITLE, this.getFormattedProgress());
		}
	}

	private Component getFormattedProgress() {
		return Component.translatable("loading.progress", Mth.clamp(this.progressListener.getProgress(), 0, 100));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		long l = Util.getMillis();
		if (l - this.lastNarration > 2000L) {
			this.lastNarration = l;
			this.triggerImmediateNarration(true);
		}

		int k = this.width / 2;
		int m = this.height / 2;
		renderChunks(guiGraphics, this.progressListener, k, m, 2, 0);
		int n = this.progressListener.getDiameter() + 9 + 2;
		guiGraphics.drawCenteredString(this.font, this.getFormattedProgress(), k, m - n, 16777215);
	}

	public static void renderChunks(GuiGraphics guiGraphics, StoringChunkProgressListener storingChunkProgressListener, int i, int j, int k, int l) {
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
			guiGraphics.fill(i - t, j - t, i - t + 1, j + t, -16772609);
			guiGraphics.fill(i + t - 1, j - t, i + t, j + t, -16772609);
			guiGraphics.fill(i - t, j - t, i + t, j - t + 1, -16772609);
			guiGraphics.fill(i - t, j + t - 1, i + t, j + t, -16772609);
		}

		for (int v = 0; v < p; v++) {
			for (int w = 0; w < p; w++) {
				ChunkStatus chunkStatus = storingChunkProgressListener.getStatus(v, w);
				int x = r + v * m;
				int y = s + w * m;
				guiGraphics.fill(x, y, x + k, y + k, ARGB.opaque(COLORS.getInt(chunkStatus)));
			}
		}
	}
}
