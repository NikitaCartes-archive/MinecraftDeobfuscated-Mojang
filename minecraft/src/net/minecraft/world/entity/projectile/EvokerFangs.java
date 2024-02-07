package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;

public class EvokerFangs extends Entity implements TraceableEntity {
	public static final int ATTACK_DURATION = 20;
	public static final int LIFE_OFFSET = 2;
	public static final int ATTACK_TRIGGER_TICKS = 14;
	private int warmupDelayTicks;
	private boolean sentSpikeEvent;
	private int lifeTicks = 22;
	private boolean clientSideAttackStarted;
	@Nullable
	private LivingEntity owner;
	@Nullable
	private UUID ownerUUID;

	public EvokerFangs(EntityType<? extends EvokerFangs> entityType, Level level) {
		super(entityType, level);
	}

	public EvokerFangs(Level level, double d, double e, double f, float g, int i, LivingEntity livingEntity) {
		this(EntityType.EVOKER_FANGS, level);
		this.warmupDelayTicks = i;
		this.setOwner(livingEntity);
		this.setYRot(g * (180.0F / (float)Math.PI));
		this.setPos(d, e, f);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	public void setOwner(@Nullable LivingEntity livingEntity) {
		this.owner = livingEntity;
		this.ownerUUID = livingEntity == null ? null : livingEntity.getUUID();
	}

	@Nullable
	public LivingEntity getOwner() {
		if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
			Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
			if (entity instanceof LivingEntity) {
				this.owner = (LivingEntity)entity;
			}
		}

		return this.owner;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.warmupDelayTicks = compoundTag.getInt("Warmup");
		if (compoundTag.hasUUID("Owner")) {
			this.ownerUUID = compoundTag.getUUID("Owner");
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putInt("Warmup", this.warmupDelayTicks);
		if (this.ownerUUID != null) {
			compoundTag.putUUID("Owner", this.ownerUUID);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			if (this.clientSideAttackStarted) {
				this.lifeTicks--;
				if (this.lifeTicks == 14) {
					for (int i = 0; i < 12; i++) {
						double d = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
						double e = this.getY() + 0.05 + this.random.nextDouble();
						double f = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
						double g = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
						double h = 0.3 + this.random.nextDouble() * 0.3;
						double j = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
						this.level().addParticle(ParticleTypes.CRIT, d, e + 1.0, f, g, h, j);
					}
				}
			}
		} else if (--this.warmupDelayTicks < 0) {
			if (this.warmupDelayTicks == -8) {
				for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2, 0.0, 0.2))) {
					this.dealDamageTo(livingEntity);
				}
			}

			if (!this.sentSpikeEvent) {
				this.level().broadcastEntityEvent(this, (byte)4);
				this.sentSpikeEvent = true;
			}

			if (--this.lifeTicks < 0) {
				this.discard();
			}
		}
	}

	private void dealDamageTo(LivingEntity livingEntity) {
		LivingEntity livingEntity2 = this.getOwner();
		if (livingEntity.isAlive() && !livingEntity.isInvulnerable() && livingEntity != livingEntity2) {
			if (livingEntity2 == null) {
				livingEntity.hurt(this.damageSources().magic(), 6.0F);
			} else {
				if (livingEntity2.isAlliedTo(livingEntity)) {
					return;
				}

				livingEntity.hurt(this.damageSources().indirectMagic(this, livingEntity2), 6.0F);
			}
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		super.handleEntityEvent(b);
		if (b == 4) {
			this.clientSideAttackStarted = true;
			if (!this.isSilent()) {
				this.level()
					.playLocalSound(
						this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false
					);
			}
		}
	}

	public float getAnimationProgress(float f) {
		if (!this.clientSideAttackStarted) {
			return 0.0F;
		} else {
			int i = this.lifeTicks - 2;
			return i <= 0 ? 1.0F : 1.0F - ((float)i - f) / 20.0F;
		}
	}
}
