package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerChunkProgressListener implements ChunkProgressListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private final int maxCount;
	private int count;
	private long startTime;
	private long nextTickTime = Long.MAX_VALUE;

	public LoggerChunkProgressListener(int i) {
		int j = i * 2 + 1;
		this.maxCount = j * j;
	}

	@Override
	public void updateSpawnPos(ChunkPos chunkPos) {
		this.nextTickTime = Util.getMillis();
		this.startTime = this.nextTickTime;
	}

	@Override
	public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
		if (chunkStatus == ChunkStatus.FULL) {
			this.count++;
		}

		int i = this.getProgress();
		if (Util.getMillis() > this.nextTickTime) {
			this.nextTickTime += 500L;
			LOGGER.info(new TranslatableComponent("menu.preparingSpawn", Mth.clamp(i, 0, 100)).getString());
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void start() {
	}

	@Override
	public void stop() {
		LOGGER.info("Time elapsed: {} ms", Util.getMillis() - this.startTime);
		this.nextTickTime = Long.MAX_VALUE;
	}

	public int getProgress() {
		return Mth.floor((float)this.count * 100.0F / (float)this.maxCount);
	}
}
