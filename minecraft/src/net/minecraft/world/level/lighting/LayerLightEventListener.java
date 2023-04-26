package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.DataLayer;

public interface LayerLightEventListener extends LightEventListener {
	@Nullable
	DataLayer getDataLayerData(SectionPos sectionPos);

	int getLightValue(BlockPos blockPos);

	public static enum DummyLightLayerEventListener implements LayerLightEventListener {
		INSTANCE;

		@Nullable
		@Override
		public DataLayer getDataLayerData(SectionPos sectionPos) {
			return null;
		}

		@Override
		public int getLightValue(BlockPos blockPos) {
			return 0;
		}

		@Override
		public void checkBlock(BlockPos blockPos) {
		}

		@Override
		public boolean hasLightWork() {
			return false;
		}

		@Override
		public int runLightUpdates() {
			return 0;
		}

		@Override
		public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
		}

		@Override
		public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
		}

		@Override
		public void propagateLightSources(ChunkPos chunkPos) {
		}
	}
}
