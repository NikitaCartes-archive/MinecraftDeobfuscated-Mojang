package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

public class RemoveBlockGoal extends MoveToBlockGoal {
	private final Block blockToRemove;
	private final Mob removerMob;
	private int ticksSinceReachedGoal;
	private static final int WAIT_AFTER_BLOCK_FOUND = 20;

	public RemoveBlockGoal(Block block, PathfinderMob pathfinderMob, double d, int i) {
		super(pathfinderMob, d, 24, i);
		this.blockToRemove = block;
		this.removerMob = pathfinderMob;
	}

	@Override
	public boolean canUse() {
		if (!this.removerMob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			return false;
		} else if (this.nextStartTick > 0) {
			this.nextStartTick--;
			return false;
		} else if (this.findNearestBlock()) {
			this.nextStartTick = reducedTickDelay(20);
			return true;
		} else {
			this.nextStartTick = this.nextStartTick(this.mob);
			return false;
		}
	}

	@Override
	public void stop() {
		super.stop();
		this.removerMob.fallDistance = 1.0F;
	}

	@Override
	public void start() {
		super.start();
		this.ticksSinceReachedGoal = 0;
	}

	public void playDestroyProgressSound(LevelAccessor levelAccessor, BlockPos blockPos) {
	}

	public void playBreakSound(Level level, BlockPos blockPos) {
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.removerMob.level();
		BlockPos blockPos = this.removerMob.blockPosition();
		BlockPos blockPos2 = this.getPosWithBlock(blockPos, level);
		RandomSource randomSource = this.removerMob.getRandom();
		if (this.isReachedTarget() && blockPos2 != null) {
			if (this.ticksSinceReachedGoal > 0) {
				Vec3 vec3 = this.removerMob.getDeltaMovement();
				this.removerMob.setDeltaMovement(vec3.x, 0.3, vec3.z);
				if (!level.isClientSide) {
					double d = 0.08;
					((ServerLevel)level)
						.sendParticles(
							new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)),
							(double)blockPos2.getX() + 0.5,
							(double)blockPos2.getY() + 0.7,
							(double)blockPos2.getZ() + 0.5,
							3,
							((double)randomSource.nextFloat() - 0.5) * 0.08,
							((double)randomSource.nextFloat() - 0.5) * 0.08,
							((double)randomSource.nextFloat() - 0.5) * 0.08,
							0.15F
						);
				}
			}

			if (this.ticksSinceReachedGoal % 2 == 0) {
				Vec3 vec3 = this.removerMob.getDeltaMovement();
				this.removerMob.setDeltaMovement(vec3.x, -0.3, vec3.z);
				if (this.ticksSinceReachedGoal % 6 == 0) {
					this.playDestroyProgressSound(level, this.blockPos);
				}
			}

			if (this.ticksSinceReachedGoal > 60) {
				level.removeBlock(blockPos2, false);
				if (!level.isClientSide) {
					for (int i = 0; i < 20; i++) {
						double d = randomSource.nextGaussian() * 0.02;
						double e = randomSource.nextGaussian() * 0.02;
						double f = randomSource.nextGaussian() * 0.02;
						((ServerLevel)level)
							.sendParticles(ParticleTypes.POOF, (double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5, 1, d, e, f, 0.15F);
					}

					this.playBreakSound(level, blockPos2);
				}
			}

			this.ticksSinceReachedGoal++;
		}
	}

	@Nullable
	private BlockPos getPosWithBlock(BlockPos blockPos, BlockGetter blockGetter) {
		if (blockGetter.getBlockState(blockPos).is(this.blockToRemove)) {
			return blockPos;
		} else {
			BlockPos[] blockPoss = new BlockPos[]{blockPos.below(), blockPos.west(), blockPos.east(), blockPos.north(), blockPos.south(), blockPos.below().below()};

			for (BlockPos blockPos2 : blockPoss) {
				if (blockGetter.getBlockState(blockPos2).is(this.blockToRemove)) {
					return blockPos2;
				}
			}

			return null;
		}
	}

	@Override
	protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
		ChunkAccess chunkAccess = levelReader.getChunk(
			SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), ChunkStatus.FULL, false
		);
		return chunkAccess == null
			? false
			: chunkAccess.getBlockState(blockPos).is(this.blockToRemove)
				&& chunkAccess.getBlockState(blockPos.above()).isAir()
				&& chunkAccess.getBlockState(blockPos.above(2)).isAir();
	}
}
