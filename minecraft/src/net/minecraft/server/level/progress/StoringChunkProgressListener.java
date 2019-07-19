package net.minecraft.server.level.progress;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

@Environment(EnvType.CLIENT)
public class StoringChunkProgressListener implements ChunkProgressListener {
	private final LoggerChunkProgressListener delegate;
	private final Long2ObjectOpenHashMap<ChunkStatus> statuses;
	private ChunkPos spawnPos = new ChunkPos(0, 0);
	private final int fullDiameter;
	private final int radius;
	private final int diameter;
	private boolean started;

	public StoringChunkProgressListener(int i) {
		this.delegate = new LoggerChunkProgressListener(i);
		this.fullDiameter = i * 2 + 1;
		this.radius = i + ChunkStatus.maxDistance();
		this.diameter = this.radius * 2 + 1;
		this.statuses = new Long2ObjectOpenHashMap<>();
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

	public void start() {
		this.started = true;
		this.statuses.clear();
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
