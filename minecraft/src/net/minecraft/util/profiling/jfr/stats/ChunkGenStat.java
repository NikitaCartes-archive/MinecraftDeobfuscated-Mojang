package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public record ChunkGenStat() implements TimedStat {
	private final Duration duration;
	private final ChunkPos chunkPos;
	private final ColumnPos worldPos;
	private final ChunkStatus status;
	private final boolean success;
	private final String level;

	public ChunkGenStat(Duration duration, ChunkPos chunkPos, ColumnPos columnPos, ChunkStatus chunkStatus, boolean bl, String string) {
		this.duration = duration;
		this.chunkPos = chunkPos;
		this.worldPos = columnPos;
		this.status = chunkStatus;
		this.success = bl;
		this.level = string;
	}

	public static ChunkGenStat from(RecordedEvent recordedEvent) {
		return new ChunkGenStat(
			recordedEvent.getDuration(),
			new ChunkPos(recordedEvent.getInt("chunkPosX"), recordedEvent.getInt("chunkPosX")),
			new ColumnPos(recordedEvent.getInt("worldPosX"), recordedEvent.getInt("worldPosZ")),
			ChunkStatus.byName(recordedEvent.getString("status")),
			recordedEvent.getBoolean("success"),
			recordedEvent.getString("level")
		);
	}
}
