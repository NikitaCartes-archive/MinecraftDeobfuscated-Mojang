package net.minecraft.world.level;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface LevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
	default float getMoonBrightness() {
		return DimensionType.MOON_BRIGHTNESS_PER_PHASE[this.dimensionType().moonPhase(this.getLevelData().getDayTime())];
	}

	default float getTimeOfDay(float f) {
		return this.dimensionType().timeOfDay(this.getLevelData().getDayTime());
	}

	@Environment(EnvType.CLIENT)
	default int getMoonPhase() {
		return this.dimensionType().moonPhase(this.getLevelData().getDayTime());
	}

	TickList<Block> getBlockTicks();

	TickList<Fluid> getLiquidTicks();

	Level getLevel();

	LevelData getLevelData();

	DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos);

	default Difficulty getDifficulty() {
		return this.getLevelData().getDifficulty();
	}

	ChunkSource getChunkSource();

	@Override
	default boolean hasChunk(int i, int j) {
		return this.getChunkSource().hasChunk(i, j);
	}

	Random getRandom();

	default void blockUpdated(BlockPos blockPos, Block block) {
	}

	void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g);

	void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i);

	void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j);

	default int getHeight() {
		return this.dimensionType().logicalHeight();
	}

	default void levelEvent(int i, BlockPos blockPos, int j) {
		this.levelEvent(null, i, blockPos, j);
	}

	@Override
	default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return EntityGetter.super.getEntityCollisions(entity, aABB, predicate);
	}

	@Override
	default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
		return EntityGetter.super.isUnobstructed(entity, voxelShape);
	}

	@Override
	default BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
		return LevelReader.super.getHeightmapPos(types, blockPos);
	}
}
