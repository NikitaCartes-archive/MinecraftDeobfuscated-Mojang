/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.Nullable;

public class ImposterProtoChunk
extends ProtoChunk {
    private final LevelChunk wrapped;
    private final boolean allowWrites;

    public ImposterProtoChunk(LevelChunk levelChunk, boolean bl) {
        super(levelChunk.getPos(), UpgradeData.EMPTY, levelChunk.levelHeightAccessor, levelChunk.getLevel().registryAccess().registryOrThrow(Registries.BIOME), levelChunk.getBlendingData());
        this.wrapped = levelChunk;
        this.allowWrites = bl;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.wrapped.getBlockEntity(blockPos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return this.wrapped.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.wrapped.getFluidState(blockPos);
    }

    @Override
    public int getMaxLightLevel() {
        return this.wrapped.getMaxLightLevel();
    }

    @Override
    public LevelChunkSection getSection(int i) {
        if (this.allowWrites) {
            return this.wrapped.getSection(i);
        }
        return super.getSection(i);
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
        if (this.allowWrites) {
            return this.wrapped.setBlockState(blockPos, blockState, bl);
        }
        return null;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        if (this.allowWrites) {
            this.wrapped.setBlockEntity(blockEntity);
        }
    }

    @Override
    public void addEntity(Entity entity) {
        if (this.allowWrites) {
            this.wrapped.addEntity(entity);
        }
    }

    @Override
    public void setStatus(ChunkStatus chunkStatus) {
        if (this.allowWrites) {
            super.setStatus(chunkStatus);
        }
    }

    @Override
    public LevelChunkSection[] getSections() {
        return this.wrapped.getSections();
    }

    @Override
    public void setHeightmap(Heightmap.Types types, long[] ls) {
    }

    private Heightmap.Types fixType(Heightmap.Types types) {
        if (types == Heightmap.Types.WORLD_SURFACE_WG) {
            return Heightmap.Types.WORLD_SURFACE;
        }
        if (types == Heightmap.Types.OCEAN_FLOOR_WG) {
            return Heightmap.Types.OCEAN_FLOOR;
        }
        return types;
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
        return this.wrapped.getOrCreateHeightmapUnprimed(types);
    }

    @Override
    public int getHeight(Heightmap.Types types, int i, int j) {
        return this.wrapped.getHeight(this.fixType(types), i, j);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        return this.wrapped.getNoiseBiome(i, j, k);
    }

    @Override
    public ChunkPos getPos() {
        return this.wrapped.getPos();
    }

    @Override
    @Nullable
    public StructureStart getStartForStructure(Structure structure) {
        return this.wrapped.getStartForStructure(structure);
    }

    @Override
    public void setStartForStructure(Structure structure, StructureStart structureStart) {
    }

    @Override
    public Map<Structure, StructureStart> getAllStarts() {
        return this.wrapped.getAllStarts();
    }

    @Override
    public void setAllStarts(Map<Structure, StructureStart> map) {
    }

    @Override
    public LongSet getReferencesForStructure(Structure structure) {
        return this.wrapped.getReferencesForStructure(structure);
    }

    @Override
    public void addReferenceForStructure(Structure structure, long l) {
    }

    @Override
    public Map<Structure, LongSet> getAllReferences() {
        return this.wrapped.getAllReferences();
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> map) {
    }

    @Override
    public void setUnsaved(boolean bl) {
        this.wrapped.setUnsaved(bl);
    }

    @Override
    public boolean isUnsaved() {
        return false;
    }

    @Override
    public ChunkStatus getStatus() {
        return this.wrapped.getStatus();
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {
    }

    @Override
    public void markPosForPostprocessing(BlockPos blockPos) {
    }

    @Override
    public void setBlockEntityNbt(CompoundTag compoundTag) {
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
        return this.wrapped.getBlockEntityNbt(blockPos);
    }

    @Override
    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
        return this.wrapped.getBlockEntityNbtForSaving(blockPos);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return this.wrapped.getLights();
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        if (this.allowWrites) {
            return this.wrapped.getBlockTicks();
        }
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        if (this.allowWrites) {
            return this.wrapped.getFluidTicks();
        }
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public ChunkAccess.TicksToSave getTicksForSerialization() {
        return this.wrapped.getTicksForSerialization();
    }

    @Override
    @Nullable
    public BlendingData getBlendingData() {
        return this.wrapped.getBlendingData();
    }

    @Override
    public void setBlendingData(BlendingData blendingData) {
        this.wrapped.setBlendingData(blendingData);
    }

    @Override
    public CarvingMask getCarvingMask(GenerationStep.Carving carving) {
        if (this.allowWrites) {
            return super.getCarvingMask(carving);
        }
        throw Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    @Override
    public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving carving) {
        if (this.allowWrites) {
            return super.getOrCreateCarvingMask(carving);
        }
        throw Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    public LevelChunk getWrapped() {
        return this.wrapped;
    }

    @Override
    public boolean isLightCorrect() {
        return this.wrapped.isLightCorrect();
    }

    @Override
    public void setLightCorrect(boolean bl) {
        this.wrapped.setLightCorrect(bl);
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler) {
        if (this.allowWrites) {
            this.wrapped.fillBiomesFromNoise(biomeResolver, sampler);
        }
    }
}

