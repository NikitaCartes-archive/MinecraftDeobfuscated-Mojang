package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
	@Override
	default long dayTime() {
		return this.getLevelData().getDayTime();
	}

	long nextSubTickCount();

	LevelTickAccess<Block> getBlockTicks();

	private <T> ScheduledTick<T> createTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
		return new ScheduledTick<>(object, blockPos, this.getLevelData().getGameTime() + (long)i, tickPriority, this.nextSubTickCount());
	}

	private <T> ScheduledTick<T> createTick(BlockPos blockPos, T object, int i) {
		return new ScheduledTick<>(object, blockPos, this.getLevelData().getGameTime() + (long)i, this.nextSubTickCount());
	}

	default void scheduleTick(BlockPos blockPos, Block block, int i, TickPriority tickPriority) {
		this.getBlockTicks().schedule(this.createTick(blockPos, block, i, tickPriority));
	}

	default void scheduleTick(BlockPos blockPos, Block block, int i) {
		this.getBlockTicks().schedule(this.createTick(blockPos, block, i));
	}

	LevelTickAccess<Fluid> getFluidTicks();

	default void scheduleTick(BlockPos blockPos, Fluid fluid, int i, TickPriority tickPriority) {
		this.getFluidTicks().schedule(this.createTick(blockPos, fluid, i, tickPriority));
	}

	default void scheduleTick(BlockPos blockPos, Fluid fluid, int i) {
		this.getFluidTicks().schedule(this.createTick(blockPos, fluid, i));
	}

	LevelData getLevelData();

	DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos);

	@Nullable
	MinecraftServer getServer();

	default Difficulty getDifficulty() {
		return this.getLevelData().getDifficulty();
	}

	ChunkSource getChunkSource();

	@Override
	default boolean hasChunk(int i, int j) {
		return this.getChunkSource().hasChunk(i, j);
	}

	RandomSource getRandom();

	default void blockUpdated(BlockPos blockPos, Block block) {
	}

	default void neighborShapeChanged(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int i, int j) {
		NeighborUpdater.executeShapeUpdate(this, direction, blockState, blockPos, blockPos2, i, j - 1);
	}

	default void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource) {
		this.playSound(player, blockPos, soundEvent, soundSource, 1.0F, 1.0F);
	}

	void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g);

	void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i);

	void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j);

	default void levelEvent(int i, BlockPos blockPos, int j) {
		this.levelEvent(null, i, blockPos, j);
	}

	void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context);

	default void gameEvent(@Nullable Entity entity, Holder<GameEvent> holder, Vec3 vec3) {
		this.gameEvent(holder, vec3, new GameEvent.Context(entity, null));
	}

	default void gameEvent(@Nullable Entity entity, Holder<GameEvent> holder, BlockPos blockPos) {
		this.gameEvent(holder, blockPos, new GameEvent.Context(entity, null));
	}

	default void gameEvent(Holder<GameEvent> holder, BlockPos blockPos, GameEvent.Context context) {
		this.gameEvent(holder, Vec3.atCenterOf(blockPos), context);
	}

	default void gameEvent(ResourceKey<GameEvent> resourceKey, BlockPos blockPos, GameEvent.Context context) {
		this.gameEvent(this.registryAccess().registryOrThrow(Registries.GAME_EVENT).getHolderOrThrow(resourceKey), blockPos, context);
	}
}
