package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AgeableMob extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgeableMob.class, EntityDataSerializers.BOOLEAN);
	public static final int BABY_START_AGE = -24000;
	private static final int FORCED_AGE_PARTICLE_TICKS = 40;
	protected int age;
	protected int forcedAge;
	protected int forcedAgeTimer;

	protected AgeableMob(EntityType<? extends AgeableMob> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		if (spawnGroupData == null) {
			spawnGroupData = new AgeableMob.AgeableMobGroupData(true);
		}

		AgeableMob.AgeableMobGroupData ageableMobGroupData = (AgeableMob.AgeableMobGroupData)spawnGroupData;
		if (ageableMobGroupData.isShouldSpawnBaby()
			&& ageableMobGroupData.getGroupSize() > 0
			&& serverLevelAccessor.getRandom().nextFloat() <= ageableMobGroupData.getBabySpawnChance()) {
			this.setAge(-24000);
		}

		ageableMobGroupData.increaseGroupSizeByOne();
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	@Nullable
	public abstract AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob);

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_BABY_ID, false);
	}

	public boolean canBreed() {
		return false;
	}

	public int getAge() {
		if (this.level().isClientSide) {
			return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
		} else {
			return this.age;
		}
	}

	public void ageUp(int i, boolean bl) {
		int j = this.getAge();
		j += i * 20;
		if (j > 0) {
			j = 0;
		}

		int l = j - j;
		this.setAge(j);
		if (bl) {
			this.forcedAge += l;
			if (this.forcedAgeTimer == 0) {
				this.forcedAgeTimer = 40;
			}
		}

		if (this.getAge() == 0) {
			this.setAge(this.forcedAge);
		}
	}

	public void ageUp(int i) {
		this.ageUp(i, false);
	}

	public void setAge(int i) {
		int j = this.getAge();
		this.age = i;
		if (j < 0 && i >= 0 || j >= 0 && i < 0) {
			this.entityData.set(DATA_BABY_ID, i < 0);
			this.ageBoundaryReached();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Age", this.getAge());
		compoundTag.putInt("ForcedAge", this.forcedAge);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setAge(compoundTag.getInt("Age"));
		this.forcedAge = compoundTag.getInt("ForcedAge");
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_BABY_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide) {
			if (this.forcedAgeTimer > 0) {
				if (this.forcedAgeTimer % 4 == 0) {
					this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
				}

				this.forcedAgeTimer--;
			}
		} else if (this.isAlive()) {
			int i = this.getAge();
			if (i < 0) {
				this.setAge(++i);
			} else if (i > 0) {
				this.setAge(--i);
			}
		}
	}

	protected void ageBoundaryReached() {
		if (!this.isBaby() && this.isPassenger() && this.getVehicle() instanceof AbstractBoat abstractBoat && !abstractBoat.hasEnoughSpaceFor(this)) {
			this.stopRiding();
		}
	}

	@Override
	public boolean isBaby() {
		return this.getAge() < 0;
	}

	@Override
	public void setBaby(boolean bl) {
		this.setAge(bl ? -24000 : 0);
	}

	public static int getSpeedUpSecondsWhenFeeding(int i) {
		return (int)((float)(i / 20) * 0.1F);
	}

	@VisibleForTesting
	public int getForcedAge() {
		return this.forcedAge;
	}

	@VisibleForTesting
	public int getForcedAgeTimer() {
		return this.forcedAgeTimer;
	}

	public static class AgeableMobGroupData implements SpawnGroupData {
		private int groupSize;
		private final boolean shouldSpawnBaby;
		private final float babySpawnChance;

		public AgeableMobGroupData(boolean bl, float f) {
			this.shouldSpawnBaby = bl;
			this.babySpawnChance = f;
		}

		public AgeableMobGroupData(boolean bl) {
			this(bl, 0.05F);
		}

		public AgeableMobGroupData(float f) {
			this(true, f);
		}

		public int getGroupSize() {
			return this.groupSize;
		}

		public void increaseGroupSizeByOne() {
			this.groupSize++;
		}

		public boolean isShouldSpawnBaby() {
			return this.shouldSpawnBaby;
		}

		public float getBabySpawnChance() {
			return this.babySpawnChance;
		}
	}
}
