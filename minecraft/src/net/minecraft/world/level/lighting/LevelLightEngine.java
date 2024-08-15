package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class LevelLightEngine implements LightEventListener {
	public static final int LIGHT_SECTION_PADDING = 1;
	public static final LevelLightEngine EMPTY = new LevelLightEngine();
	protected final LevelHeightAccessor levelHeightAccessor;
	@Nullable
	private final LightEngine<?, ?> blockEngine;
	@Nullable
	private final LightEngine<?, ?> skyEngine;

	public LevelLightEngine(LightChunkGetter lightChunkGetter, boolean bl, boolean bl2) {
		this.levelHeightAccessor = lightChunkGetter.getLevel();
		this.blockEngine = bl ? new BlockLightEngine(lightChunkGetter) : null;
		this.skyEngine = bl2 ? new SkyLightEngine(lightChunkGetter) : null;
	}

	private LevelLightEngine() {
		this.levelHeightAccessor = LevelHeightAccessor.create(0, 0);
		this.blockEngine = null;
		this.skyEngine = null;
	}

	@Override
	public void checkBlock(BlockPos blockPos) {
		if (this.blockEngine != null) {
			this.blockEngine.checkBlock(blockPos);
		}

		if (this.skyEngine != null) {
			this.skyEngine.checkBlock(blockPos);
		}
	}

	@Override
	public boolean hasLightWork() {
		return this.skyEngine != null && this.skyEngine.hasLightWork() ? true : this.blockEngine != null && this.blockEngine.hasLightWork();
	}

	@Override
	public int runLightUpdates() {
		int i = 0;
		if (this.blockEngine != null) {
			i += this.blockEngine.runLightUpdates();
		}

		if (this.skyEngine != null) {
			i += this.skyEngine.runLightUpdates();
		}

		return i;
	}

	@Override
	public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
		if (this.blockEngine != null) {
			this.blockEngine.updateSectionStatus(sectionPos, bl);
		}

		if (this.skyEngine != null) {
			this.skyEngine.updateSectionStatus(sectionPos, bl);
		}
	}

	@Override
	public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
		if (this.blockEngine != null) {
			this.blockEngine.setLightEnabled(chunkPos, bl);
		}

		if (this.skyEngine != null) {
			this.skyEngine.setLightEnabled(chunkPos, bl);
		}
	}

	@Override
	public void propagateLightSources(ChunkPos chunkPos) {
		if (this.blockEngine != null) {
			this.blockEngine.propagateLightSources(chunkPos);
		}

		if (this.skyEngine != null) {
			this.skyEngine.propagateLightSources(chunkPos);
		}
	}

	public LayerLightEventListener getLayerListener(LightLayer lightLayer) {
		if (lightLayer == LightLayer.BLOCK) {
			return (LayerLightEventListener)(this.blockEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.blockEngine);
		} else {
			return (LayerLightEventListener)(this.skyEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.skyEngine);
		}
	}

	public String getDebugData(LightLayer lightLayer, SectionPos sectionPos) {
		if (lightLayer == LightLayer.BLOCK) {
			if (this.blockEngine != null) {
				return this.blockEngine.getDebugData(sectionPos.asLong());
			}
		} else if (this.skyEngine != null) {
			return this.skyEngine.getDebugData(sectionPos.asLong());
		}

		return "n/a";
	}

	public LayerLightSectionStorage.SectionType getDebugSectionType(LightLayer lightLayer, SectionPos sectionPos) {
		if (lightLayer == LightLayer.BLOCK) {
			if (this.blockEngine != null) {
				return this.blockEngine.getDebugSectionType(sectionPos.asLong());
			}
		} else if (this.skyEngine != null) {
			return this.skyEngine.getDebugSectionType(sectionPos.asLong());
		}

		return LayerLightSectionStorage.SectionType.EMPTY;
	}

	public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer) {
		if (lightLayer == LightLayer.BLOCK) {
			if (this.blockEngine != null) {
				this.blockEngine.queueSectionData(sectionPos.asLong(), dataLayer);
			}
		} else if (this.skyEngine != null) {
			this.skyEngine.queueSectionData(sectionPos.asLong(), dataLayer);
		}
	}

	public void retainData(ChunkPos chunkPos, boolean bl) {
		if (this.blockEngine != null) {
			this.blockEngine.retainData(chunkPos, bl);
		}

		if (this.skyEngine != null) {
			this.skyEngine.retainData(chunkPos, bl);
		}
	}

	public int getRawBrightness(BlockPos blockPos, int i) {
		int j = this.skyEngine == null ? 0 : this.skyEngine.getLightValue(blockPos) - i;
		int k = this.blockEngine == null ? 0 : this.blockEngine.getLightValue(blockPos);
		return Math.max(k, j);
	}

	public boolean lightOnInSection(SectionPos sectionPos) {
		long l = sectionPos.asLong();
		return this.blockEngine == null || this.blockEngine.storage.lightOnInSection(l) && (this.skyEngine == null || this.skyEngine.storage.lightOnInSection(l));
	}

	public int getLightSectionCount() {
		return this.levelHeightAccessor.getSectionsCount() + 2;
	}

	public int getMinLightSection() {
		return this.levelHeightAccessor.getMinSection() - 1;
	}

	public int getMaxLightSection() {
		return this.getMinLightSection() + this.getLightSectionCount();
	}
}
