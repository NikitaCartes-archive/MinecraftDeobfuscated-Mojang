package net.minecraft.world.level;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
	@Override
	default long dayTime() {
		return this.getLevelData().getDayTime();
	}

	TickList<Block> getBlockTicks();

	TickList<Fluid> getLiquidTicks();

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
}
