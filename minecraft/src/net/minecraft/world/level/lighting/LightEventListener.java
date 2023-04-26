package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface LightEventListener {
	void checkBlock(BlockPos blockPos);

	boolean hasLightWork();

	int runLightUpdates();

	default void updateSectionStatus(BlockPos blockPos, boolean bl) {
		this.updateSectionStatus(SectionPos.of(blockPos), bl);
	}

	void updateSectionStatus(SectionPos sectionPos, boolean bl);

	void setLightEnabled(ChunkPos chunkPos, boolean bl);

	void propagateLightSources(ChunkPos chunkPos);
}
