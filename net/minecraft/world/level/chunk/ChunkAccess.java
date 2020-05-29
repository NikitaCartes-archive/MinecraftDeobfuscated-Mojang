/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public interface ChunkAccess
extends BlockGetter,
FeatureAccess {
    @Nullable
    public BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    public void setBlockEntity(BlockPos var1, BlockEntity var2);

    public void addEntity(Entity var1);

    @Nullable
    default public LevelChunkSection getHighestSection() {
        LevelChunkSection[] levelChunkSections = this.getSections();
        for (int i = levelChunkSections.length - 1; i >= 0; --i) {
            LevelChunkSection levelChunkSection = levelChunkSections[i];
            if (LevelChunkSection.isEmpty(levelChunkSection)) continue;
            return levelChunkSection;
        }
        return null;
    }

    default public int getHighestSectionPosition() {
        LevelChunkSection levelChunkSection = this.getHighestSection();
        return levelChunkSection == null ? 0 : levelChunkSection.bottomBlockY();
    }

    public Set<BlockPos> getBlockEntitiesPos();

    public LevelChunkSection[] getSections();

    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps();

    public void setHeightmap(Heightmap.Types var1, long[] var2);

    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types var1);

    public int getHeight(Heightmap.Types var1, int var2, int var3);

    public ChunkPos getPos();

    public void setLastSaveTime(long var1);

    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts();

    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> var1);

    default public boolean isYSpaceEmpty(int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (j >= 256) {
            j = 255;
        }
        for (int k = i; k <= j; k += 16) {
            if (LevelChunkSection.isEmpty(this.getSections()[k >> 4])) continue;
            return false;
        }
        return true;
    }

    @Nullable
    public ChunkBiomeContainer getBiomes();

    public void setUnsaved(boolean var1);

    public boolean isUnsaved();

    public ChunkStatus getStatus();

    public void removeBlockEntity(BlockPos var1);

    default public void markPosForPostprocessing(BlockPos blockPos) {
        LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)blockPos);
    }

    public ShortList[] getPostProcessing();

    default public void addPackedPostProcess(short s, int i) {
        ChunkAccess.getOrCreateOffsetList(this.getPostProcessing(), i).add(s);
    }

    default public void setBlockEntityNbt(CompoundTag compoundTag) {
        LogManager.getLogger().warn("Trying to set a BlockEntity, but this operation is not supported.");
    }

    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos var1);

    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos var1);

    public Stream<BlockPos> getLights();

    public TickList<Block> getBlockTicks();

    public TickList<Fluid> getLiquidTicks();

    public UpgradeData getUpgradeData();

    public void setInhabitedTime(long var1);

    public long getInhabitedTime();

    public static ShortList getOrCreateOffsetList(ShortList[] shortLists, int i) {
        if (shortLists[i] == null) {
            shortLists[i] = new ShortArrayList();
        }
        return shortLists[i];
    }

    public boolean isLightCorrect();

    public void setLightCorrect(boolean var1);
}

