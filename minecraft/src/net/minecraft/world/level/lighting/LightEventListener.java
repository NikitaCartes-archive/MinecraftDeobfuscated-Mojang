package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface LightEventListener {
	void checkBlock(BlockPos blockPos);

	void onBlockEmissionIncrease(BlockPos blockPos, int i);

	boolean hasLightWork();

	int runUpdates(int i, boolean bl, boolean bl2);

	default void updateSectionStatus(BlockPos blockPos, boolean bl) {
		this.updateSectionStatus(SectionPos.of(blockPos), bl);
	}

	void updateSectionStatus(SectionPos sectionPos, boolean bl);

	void enableLightSources(ChunkPos chunkPos, boolean bl);
}
