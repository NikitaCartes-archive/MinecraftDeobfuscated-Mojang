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
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

public class BellBlockEntity extends BlockEntity implements TickableBlockEntity {
	private long lastRingTimestamp;
	public int ticks;
	public boolean shaking;
	public Direction clickDirection;
	private List<LivingEntity> nearbyEntities;
	private boolean resonating;
	private int resonationTicks;

	public BellBlockEntity() {
		super(BlockEntityType.BELL);
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

	@Override
	public void tick() {
		if (this.shaking) {
			this.ticks++;
		}

		if (this.ticks >= 50) {
			this.shaking = false;
			this.ticks = 0;
		}

		if (this.ticks >= 5 && this.resonationTicks == 0 && this.areRaidersNearby()) {
			this.resonating = true;
			this.playResonateSound();
		}

		if (this.resonating) {
			if (this.resonationTicks < 40) {
				this.resonationTicks++;
			} else {
				this.makeRaidersGlow(this.level);
				this.showBellParticles(this.level);
				this.resonating = false;
			}
		}
	}

	private void playResonateSound() {
		this.level.playSound(null, this.getBlockPos(), SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
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
				if (livingEntity.isAlive() && !livingEntity.removed && blockPos.closerThan(livingEntity.position(), 32.0)) {
					livingEntity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, this.level.getGameTime());
				}
			}
		}
	}

	private boolean areRaidersNearby() {
		BlockPos blockPos = this.getBlockPos();

		for (LivingEntity livingEntity : this.nearbyEntities) {
			if (livingEntity.isAlive()
				&& !livingEntity.removed
				&& blockPos.closerThan(livingEntity.position(), 32.0)
				&& livingEntity.getType().is(EntityTypeTags.RAIDERS)) {
				return true;
			}
		}

		return false;
	}

	private void makeRaidersGlow(Level level) {
		if (!level.isClientSide) {
			this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach(this::glow);
		}
	}

	private void showBellParticles(Level level) {
		if (level.isClientSide) {
			BlockPos blockPos = this.getBlockPos();
			MutableInt mutableInt = new MutableInt(16700985);
			int i = (int)this.nearbyEntities.stream().filter(livingEntity -> blockPos.closerThan(livingEntity.position(), 48.0)).count();
			this.nearbyEntities
				.stream()
				.filter(this::isRaiderWithinRange)
				.forEach(
					livingEntity -> {
						float f = 1.0F;
						float g = Mth.sqrt(
							(livingEntity.getX() - (double)blockPos.getX()) * (livingEntity.getX() - (double)blockPos.getX())
								+ (livingEntity.getZ() - (double)blockPos.getZ()) * (livingEntity.getZ() - (double)blockPos.getZ())
						);
						double d = (double)((float)blockPos.getX() + 0.5F) + (double)(1.0F / g) * (livingEntity.getX() - (double)blockPos.getX());
						double e = (double)((float)blockPos.getZ() + 0.5F) + (double)(1.0F / g) * (livingEntity.getZ() - (double)blockPos.getZ());
						int j = Mth.clamp((i - 21) / -2, 3, 15);

						for (int k = 0; k < j; k++) {
							int l = mutableInt.addAndGet(5);
							double h = (double)FastColor.ARGB32.red(l) / 255.0;
							double m = (double)FastColor.ARGB32.green(l) / 255.0;
							double n = (double)FastColor.ARGB32.blue(l) / 255.0;
							level.addParticle(ParticleTypes.ENTITY_EFFECT, d, (double)((float)blockPos.getY() + 0.5F), e, h, m, n);
						}
					}
				);
		}
	}

	private boolean isRaiderWithinRange(LivingEntity livingEntity) {
		return livingEntity.isAlive()
			&& !livingEntity.removed
			&& this.getBlockPos().closerThan(livingEntity.position(), 48.0)
			&& livingEntity.getType().is(EntityTypeTags.RAIDERS);
	}

	private void glow(LivingEntity livingEntity) {
		livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
	}
}
