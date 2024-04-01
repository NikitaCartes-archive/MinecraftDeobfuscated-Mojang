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

	public static record ConstantLayer(int lightLevel) implements LayerLightEventListener {
		public static final LayerLightEventListener.ConstantLayer ZERO = new LayerLightEventListener.ConstantLayer(0);
		public static final LayerLightEventListener.ConstantLayer FULL_BRIGHT = new LayerLightEventListener.ConstantLayer(15);

		@Nullable
		@Override
		public DataLayer getDataLayerData(SectionPos sectionPos) {
			return null;
		}

		@Override
		public int getLightValue(BlockPos blockPos) {
			return this.lightLevel;
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
