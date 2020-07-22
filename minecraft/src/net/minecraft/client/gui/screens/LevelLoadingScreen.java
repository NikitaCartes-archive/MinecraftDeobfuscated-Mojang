package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkStatus;

@Environment(EnvType.CLIENT)
public class LevelLoadingScreen extends Screen {
	private final StoringChunkProgressListener progressListener;
	private long lastNarration = -1L;
	private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
		object2IntOpenHashMap.defaultReturnValue(0);
		object2IntOpenHashMap.put(ChunkStatus.EMPTY, 5526612);
		object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
		object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
		object2IntOpenHashMap.put(ChunkStatus.BIOMES, 8434258);
		object2IntOpenHashMap.put(ChunkStatus.NOISE, 13750737);
		object2IntOpenHashMap.put(ChunkStatus.SURFACE, 7497737);
		object2IntOpenHashMap.put(ChunkStatus.CARVERS, 7169628);
		object2IntOpenHashMap.put(ChunkStatus.LIQUID_CARVERS, 3159410);
		object2IntOpenHashMap.put(ChunkStatus.FEATURES, 2213376);
		object2IntOpenHashMap.put(ChunkStatus.LIGHT, 13421772);
		object2IntOpenHashMap.put(ChunkStatus.SPAWN, 15884384);
		object2IntOpenHashMap.put(ChunkStatus.HEIGHTMAPS, 15658734);
		object2IntOpenHashMap.put(ChunkStatus.FULL, 16777215);
	});

	public LevelLoadingScreen(StoringChunkProgressListener storingChunkProgressListener) {
		super(NarratorChatListener.NO_TITLE);
		this.progressListener = storingChunkProgressListener;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void removed() {
		NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.loading.done").getString());
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		String string = Mth.clamp(this.progressListener.getProgress(), 0, 100) + "%";
		long l = Util.getMillis();
		if (l - this.lastNarration > 2000L) {
			this.lastNarration = l;
			NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.loading", string).getString());
		}

		int k = this.width / 2;
		int m = this.height / 2;
		int n = 30;
		renderChunks(poseStack, this.progressListener, k, m + 30, 2, 0);
		drawCenteredString(poseStack, this.font, string, k, m - 9 / 2 - 30, 16777215);
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
			fill(poseStack, i - t, j - t, i - t + 1, j + t, -16772609);
			fill(poseStack, i + t - 1, j - t, i + t, j + t, -16772609);
			fill(poseStack, i - t, j - t, i + t, j - t + 1, -16772609);
			fill(poseStack, i - t, j + t - 1, i + t, j + t, -16772609);
		}

		for (int v = 0; v < p; v++) {
			for (int w = 0; w < p; w++) {
				ChunkStatus chunkStatus = storingChunkProgressListener.getStatus(v, w);
				int x = r + v * m;
				int y = s + w * m;
				fill(poseStack, x, y, x + k, y + k, COLORS.getInt(chunkStatus) | 0xFF000000);
			}
		}
	}
}
