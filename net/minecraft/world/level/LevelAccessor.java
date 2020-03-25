/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface LevelAccessor
extends EntityGetter,
LevelReader,
LevelSimulatedRW {
    public long getSeed();

    default public float getMoonBrightness() {
        return Dimension.MOON_BRIGHTNESS_PER_PHASE[this.getDimension().getMoonPhase(this.getLevelData().getDayTime())];
    }

    default public float getTimeOfDay(float f) {
        return this.getDimension().getTimeOfDay(this.getLevelData().getDayTime(), f);
    }

    @Environment(value=EnvType.CLIENT)
    default public int getMoonPhase() {
        return this.getDimension().getMoonPhase(this.getLevelData().getDayTime());
    }

    public TickList<Block> getBlockTicks();

    public TickList<Fluid> getLiquidTicks();

    public Level getLevel();

    public LevelData getLevelData();

    public DifficultyInstance getCurrentDifficultyAt(BlockPos var1);

    default public Difficulty getDifficulty() {
        return this.getLevelData().getDifficulty();
    }

    public ChunkSource getChunkSource();

    @Override
    default public boolean hasChunk(int i, int j) {
        return this.getChunkSource().hasChunk(i, j);
    }

    public Random getRandom();

    default public void blockUpdated(BlockPos blockPos, Block block) {
    }

    public void playSound(@Nullable Player var1, BlockPos var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    public void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12);

    public void levelEvent(@Nullable Player var1, int var2, BlockPos var3, int var4);

    default public int getHeight() {
        return this.getDimension().isHasCeiling() ? 128 : 256;
    }

    default public void levelEvent(int i, BlockPos blockPos, int j) {
        this.levelEvent(null, i, blockPos, j);
    }

    @Override
    default public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
        return EntityGetter.super.getEntityCollisions(entity, aABB, set);
    }

    @Override
    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        return EntityGetter.super.isUnobstructed(entity, voxelShape);
    }

    @Override
    default public BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
        return LevelReader.super.getHeightmapPos(types, blockPos);
    }
}

