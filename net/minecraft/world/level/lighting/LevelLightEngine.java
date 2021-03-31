/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LightEventListener;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.jetbrains.annotations.Nullable;

public class LevelLightEngine
implements LightEventListener {
    public static final int MAX_SOURCE_LEVEL = 15;
    public static final int LIGHT_SECTION_PADDING = 1;
    protected final LevelHeightAccessor levelHeightAccessor;
    @Nullable
    private final LayerLightEngine<?, ?> blockEngine;
    @Nullable
    private final LayerLightEngine<?, ?> skyEngine;

    public LevelLightEngine(LightChunkGetter lightChunkGetter, boolean bl, boolean bl2) {
        this.levelHeightAccessor = lightChunkGetter.getLevel();
        this.blockEngine = bl ? new BlockLightEngine(lightChunkGetter) : null;
        this.skyEngine = bl2 ? new SkyLightEngine(lightChunkGetter) : null;
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
    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
        if (this.blockEngine != null) {
            this.blockEngine.onBlockEmissionIncrease(blockPos, i);
        }
    }

    @Override
    public boolean hasLightWork() {
        if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
            return true;
        }
        return this.blockEngine != null && this.blockEngine.hasLightWork();
    }

    @Override
    public int runUpdates(int i, boolean bl, boolean bl2) {
        if (this.blockEngine != null && this.skyEngine != null) {
            int j = i / 2;
            int k = this.blockEngine.runUpdates(j, bl, bl2);
            int l = i - j + k;
            int m = this.skyEngine.runUpdates(l, bl, bl2);
            if (k == 0 && m > 0) {
                return this.blockEngine.runUpdates(m, bl, bl2);
            }
            return m;
        }
        if (this.blockEngine != null) {
            return this.blockEngine.runUpdates(i, bl, bl2);
        }
        if (this.skyEngine != null) {
            return this.skyEngine.runUpdates(i, bl, bl2);
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
            if (this.blockEngine == null) {
                return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
            }
            return this.blockEngine;
        }
        if (this.skyEngine == null) {
            return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
        }
        return this.skyEngine;
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

    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer, boolean bl) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(sectionPos.asLong(), dataLayer, bl);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(sectionPos.asLong(), dataLayer, bl);
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

