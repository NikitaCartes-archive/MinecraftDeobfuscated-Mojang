package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public record ChunkGenStat(Duration duration, ChunkPos chunkPos, ColumnPos worldPos, ChunkStatus status, String level) implements TimedStat {
	public static ChunkGenStat from(RecordedEvent recordedEvent) {
		return new ChunkGenStat(
			recordedEvent.getDuration(),
			new ChunkPos(recordedEvent.getInt("chunkPosX"), recordedEvent.getInt("chunkPosX")),
			new ColumnPos(recordedEvent.getInt("worldPosX"), recordedEvent.getInt("worldPosZ")),
			ChunkStatus.byName(recordedEvent.getString("status")),
			recordedEvent.getString("level")
		);
	}
}
