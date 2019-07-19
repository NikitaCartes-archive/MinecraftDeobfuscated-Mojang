package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class LevelLightEngine implements LightEventListener {
	@Nullable
	private final LayerLightEngine<?, ?> blockEngine;
	@Nullable
	private final LayerLightEngine<?, ?> skyEngine;

	public LevelLightEngine(LightChunkGetter lightChunkGetter, boolean bl, boolean bl2) {
		this.blockEngine = bl ? new BlockLightEngine(lightChunkGetter) : null;
		this.skyEngine = bl2 ? new SkyLightEngine(lightChunkGetter) : null;
	}

	public void checkBlock(BlockPos blockPos) {
		if (this.blockEngine != null) {
			this.blockEngine.checkBlock(blockPos);
		}

		if (this.skyEngine != null) {
			this.skyEngine.checkBlock(blockPos);
		}
	}

	public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
		if (this.blockEngine != null) {
			this.blockEngine.onBlockEmissionIncrease(blockPos, i);
		}
	}

	public boolean hasLightWork() {
		return this.skyEngine != null && this.skyEngine.hasLightWork() ? true : this.blockEngine != null && this.blockEngine.hasLightWork();
	}

	public int runUpdates(int i, boolean bl, boolean bl2) {
		if (this.blockEngine != null && this.skyEngine != null) {
			int j = i / 2;
			int k = this.blockEngine.runUpdates(j, bl, bl2);
			int l = i - j + k;
			int m = this.skyEngine.runUpdates(l, bl, bl2);
			return k == 0 && m > 0 ? this.blockEngine.runUpdates(m, bl, bl2) : m;
		} else if (this.blockEngine != null) {
			return this.blockEngine.runUpdates(i, bl, bl2);
		} else {
			return this.skyEngine != null ? this.skyEngine.runUpdates(i, bl, bl2) : i;
		}
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

	public void enableLightSources(ChunkPos chunkPos, boolean bl) {
		if (this.blockEngine != null) {
			this.blockEngine.enableLightSources(chunkPos, bl);
		}

		if (this.skyEngine != null) {
			this.skyEngine.enableLightSources(chunkPos, bl);
		}
	}

	public LayerLightEventListener getLayerListener(LightLayer lightLayer) {
		if (lightLayer == LightLayer.BLOCK) {
			return (LayerLightEventListener)(this.blockEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.blockEngine);
		} else {
			return (LayerLightEventListener)(this.skyEngine == null ? LayerLightEventListener.DummyLightLayerEventListener.INSTANCE : this.skyEngine);
		}
	}

	@Environment(EnvType.CLIENT)
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
}
