package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record ChunkIdentification(String level, String dimension, int x, int z) {
	public static ChunkIdentification from(RecordedEvent recordedEvent) {
		return new ChunkIdentification(
			recordedEvent.getString("level"), recordedEvent.getString("dimension"), recordedEvent.getInt("chunkPosX"), recordedEvent.getInt("chunkPosZ")
		);
	}
}
