package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkGenStat implements TimedStat {
	public final Duration duration;
	public final ChunkPos chunkPos;
	public final BlockPos blockPos;
	public final ChunkStatus status;
	public final boolean success;
	public final String level;

	public ChunkGenStat(RecordedEvent recordedEvent) {
		this.duration = recordedEvent.getDuration();
		this.chunkPos = new ChunkPos(recordedEvent.getInt("chunkPosX"), recordedEvent.getInt("chunkPosZ"));
		this.blockPos = new BlockPos(recordedEvent.getInt("centerBlockPosX"), 100, recordedEvent.getInt("centerBlockPosZ"));
		this.status = ChunkStatus.byName(recordedEvent.getString("targetStatus"));
		this.success = recordedEvent.getBoolean("success");
		this.level = recordedEvent.getString("level");
	}

	@Override
	public Duration duration() {
		return this.duration;
	}
}
