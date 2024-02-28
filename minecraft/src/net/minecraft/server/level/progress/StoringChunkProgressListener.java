package net.minecraft.server.level.progress;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class StoringChunkProgressListener implements ChunkProgressListener {
	private final LoggerChunkProgressListener delegate;
	private final Long2ObjectOpenHashMap<ChunkStatus> statuses = new Long2ObjectOpenHashMap<>();
	private ChunkPos spawnPos = new ChunkPos(0, 0);
	private final int fullDiameter;
	private final int radius;
	private final int diameter;
	private boolean started;

	private StoringChunkProgressListener(LoggerChunkProgressListener loggerChunkProgressListener, int i, int j, int k) {
		this.delegate = loggerChunkProgressListener;
		this.fullDiameter = i;
		this.radius = j;
		this.diameter = k;
	}

	public static StoringChunkProgressListener createFromGameruleRadius(int i) {
		return i > 0 ? create(i + 1) : createCompleted();
	}

	public static StoringChunkProgressListener create(int i) {
		LoggerChunkProgressListener loggerChunkProgressListener = LoggerChunkProgressListener.create(i);
		int j = ChunkProgressListener.calculateDiameter(i);
		int k = i + ChunkStatus.maxDistance();
		int l = ChunkProgressListener.calculateDiameter(k);
		return new StoringChunkProgressListener(loggerChunkProgressListener, j, k, l);
	}

	public static StoringChunkProgressListener createCompleted() {
		return new StoringChunkProgressListener(LoggerChunkProgressListener.createCompleted(), 0, 0, 0);
	}

	@Override
	public void updateSpawnPos(ChunkPos chunkPos) {
		if (this.started) {
			this.delegate.updateSpawnPos(chunkPos);
			this.spawnPos = chunkPos;
		}
	}

	@Override
	public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
		if (this.started) {
			this.delegate.onStatusChange(chunkPos, chunkStatus);
			if (chunkStatus == null) {
				this.statuses.remove(chunkPos.toLong());
			} else {
				this.statuses.put(chunkPos.toLong(), chunkStatus);
			}
		}
	}

	@Override
	public void start() {
		this.started = true;
		this.statuses.clear();
		this.delegate.start();
	}

	@Override
	public void stop() {
		this.started = false;
		this.delegate.stop();
	}

	public int getFullDiameter() {
		return this.fullDiameter;
	}

	public int getDiameter() {
		return this.diameter;
	}

	public int getProgress() {
		return this.delegate.getProgress();
	}

	@Nullable
	public ChunkStatus getStatus(int i, int j) {
		return this.statuses.get(ChunkPos.asLong(i + this.spawnPos.x - this.radius, j + this.spawnPos.z - this.radius));
	}
}
