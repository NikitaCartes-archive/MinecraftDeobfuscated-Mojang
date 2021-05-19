package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

public class BellBlockEntity extends BlockEntity {
	private static final int DURATION = 50;
	private static final int GLOW_DURATION = 60;
	private static final int MIN_TICKS_BETWEEN_SEARCHES = 60;
	private static final int MAX_RESONATION_TICKS = 40;
	private static final int TICKS_BEFORE_RESONATION = 5;
	private static final int SEARCH_RADIUS = 48;
	private static final int HEAR_BELL_RADIUS = 32;
	private static final int HIGHLIGHT_RAIDERS_RADIUS = 48;
	private long lastRingTimestamp;
	public int ticks;
	public boolean shaking;
	public Direction clickDirection;
	private List<LivingEntity> nearbyEntities;
	private boolean resonating;
	private int resonationTicks;

	public BellBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BELL, blockPos, blockState);
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		if (i == 1) {
			this.updateEntities();
			this.resonationTicks = 0;
			this.clickDirection = Direction.from3DDataValue(j);
			this.ticks = 0;
			this.shaking = true;
			return true;
		} else {
			return super.triggerEvent(i, j);
		}
	}

	private static void tick(
		Level level, BlockPos blockPos, BlockState blockState, BellBlockEntity bellBlockEntity, BellBlockEntity.ResonationEndAction resonationEndAction
	) {
		if (bellBlockEntity.shaking) {
			bellBlockEntity.ticks++;
		}

		if (bellBlockEntity.ticks >= 50) {
			bellBlockEntity.shaking = false;
			bellBlockEntity.ticks = 0;
		}

		if (bellBlockEntity.ticks >= 5 && bellBlockEntity.resonationTicks == 0 && areRaidersNearby(blockPos, bellBlockEntity.nearbyEntities)) {
			bellBlockEntity.resonating = true;
			level.playSound(null, blockPos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		if (bellBlockEntity.resonating) {
			if (bellBlockEntity.resonationTicks < 40) {
				bellBlockEntity.resonationTicks++;
			} else {
				resonationEndAction.run(level, blockPos, bellBlockEntity.nearbyEntities);
				bellBlockEntity.resonating = false;
			}
		}
	}

	public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, BellBlockEntity bellBlockEntity) {
		tick(level, blockPos, blockState, bellBlockEntity, BellBlockEntity::showBellParticles);
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BellBlockEntity bellBlockEntity) {
		tick(level, blockPos, blockState, bellBlockEntity, BellBlockEntity::makeRaidersGlow);
	}

	public void onHit(Direction direction) {
		BlockPos blockPos = this.getBlockPos();
		this.clickDirection = direction;
		if (this.shaking) {
			this.ticks = 0;
		} else {
			this.shaking = true;
		}

		this.level.blockEvent(blockPos, this.getBlockState().getBlock(), 1, direction.get3DDataValue());
	}

	private void updateEntities() {
		BlockPos blockPos = this.getBlockPos();
		if (this.level.getGameTime() > this.lastRingTimestamp + 60L || this.nearbyEntities == null) {
			this.lastRingTimestamp = this.level.getGameTime();
			AABB aABB = new AABB(blockPos).inflate(48.0);
			this.nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, aABB);
		}

		if (!this.level.isClientSide) {
			for (LivingEntity livingEntity : this.nearbyEntities) {
				if (livingEntity.isAlive() && !livingEntity.isRemoved() && blockPos.closerThan(livingEntity.position(), 32.0)) {
					livingEntity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, this.level.getGameTime());
				}
			}
		}
	}

	private static boolean areRaidersNearby(BlockPos blockPos, List<LivingEntity> list) {
		for (LivingEntity livingEntity : list) {
			if (livingEntity.isAlive()
				&& !livingEntity.isRemoved()
				&& blockPos.closerThan(livingEntity.position(), 32.0)
				&& livingEntity.getType().is(EntityTypeTags.RAIDERS)) {
				return true;
			}
		}

		return false;
	}

	private static void makeRaidersGlow(Level level, BlockPos blockPos, List<LivingEntity> list) {
		list.stream().filter(livingEntity -> isRaiderWithinRange(blockPos, livingEntity)).forEach(BellBlockEntity::glow);
	}

	private static void showBellParticles(Level level, BlockPos blockPos, List<LivingEntity> list) {
		MutableInt mutableInt = new MutableInt(16700985);
		int i = (int)list.stream().filter(livingEntity -> blockPos.closerThan(livingEntity.position(), 48.0)).count();
		list.stream()
			.filter(livingEntity -> isRaiderWithinRange(blockPos, livingEntity))
			.forEach(
				livingEntity -> {
					float f = 1.0F;
					double d = Math.sqrt(
						(livingEntity.getX() - (double)blockPos.getX()) * (livingEntity.getX() - (double)blockPos.getX())
							+ (livingEntity.getZ() - (double)blockPos.getZ()) * (livingEntity.getZ() - (double)blockPos.getZ())
					);
					double e = (double)((float)blockPos.getX() + 0.5F) + 1.0 / d * (livingEntity.getX() - (double)blockPos.getX());
					double g = (double)((float)blockPos.getZ() + 0.5F) + 1.0 / d * (livingEntity.getZ() - (double)blockPos.getZ());
					int j = Mth.clamp((i - 21) / -2, 3, 15);

					for (int k = 0; k < j; k++) {
						int l = mutableInt.addAndGet(5);
						double h = (double)FastColor.ARGB32.red(l) / 255.0;
						double m = (double)FastColor.ARGB32.green(l) / 255.0;
						double n = (double)FastColor.ARGB32.blue(l) / 255.0;
						level.addParticle(ParticleTypes.ENTITY_EFFECT, e, (double)((float)blockPos.getY() + 0.5F), g, h, m, n);
					}
				}
			);
	}

	private static boolean isRaiderWithinRange(BlockPos blockPos, LivingEntity livingEntity) {
		return livingEntity.isAlive()
			&& !livingEntity.isRemoved()
			&& blockPos.closerThan(livingEntity.position(), 48.0)
			&& livingEntity.getType().is(EntityTypeTags.RAIDERS);
	}

	private static void glow(LivingEntity livingEntity) {
		livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
	}

	@FunctionalInterface
	interface ResonationEndAction {
		void run(Level level, BlockPos blockPos, List<LivingEntity> list);
	}
}
