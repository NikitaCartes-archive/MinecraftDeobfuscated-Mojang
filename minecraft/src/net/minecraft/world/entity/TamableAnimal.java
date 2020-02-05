package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

public abstract class TamableAnimal extends Animal {
	protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(
		TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID
	);
	private boolean orderedToSit;

	protected TamableAnimal(EntityType<? extends TamableAnimal> entityType, Level level) {
		super(entityType, level);
		this.reassessTameGoals();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte)0);
		this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.getOwnerUUID() == null) {
			compoundTag.putString("OwnerUUID", "");
		} else {
			compoundTag.putString("OwnerUUID", this.getOwnerUUID().toString());
		}

		compoundTag.putBoolean("Sitting", this.orderedToSit);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		String string;
		if (compoundTag.contains("OwnerUUID", 8)) {
			string = compoundTag.getString("OwnerUUID");
		} else {
			String string2 = compoundTag.getString("Owner");
			string = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string2);
		}

		if (!string.isEmpty()) {
			try {
				this.setOwnerUUID(UUID.fromString(string));
				this.setTame(true);
			} catch (Throwable var4) {
				this.setTame(false);
			}
		}

		this.orderedToSit = compoundTag.getBoolean("Sitting");
		this.setInSittingPose(this.orderedToSit);
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return !this.isLeashed();
	}

	@Environment(EnvType.CLIENT)
	protected void spawnTamingParticles(boolean bl) {
		ParticleOptions particleOptions = ParticleTypes.HEART;
		if (!bl) {
			particleOptions = ParticleTypes.SMOKE;
		}

		for (int i = 0; i < 7; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level.addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 7) {
			this.spawnTamingParticles(true);
		} else if (b == 6) {
			this.spawnTamingParticles(false);
		} else {
			super.handleEntityEvent(b);
		}
	}

	public boolean isTame() {
		return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
	}

	public void setTame(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 4));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -5));
		}

		this.reassessTameGoals();
	}

	protected void reassessTameGoals() {
	}

	public boolean isInSittingPose() {
		return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
	}

	public void setInSittingPose(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 1));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -2));
		}
	}

	@Nullable
	public UUID getOwnerUUID() {
		return (UUID)this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
	}

	public void setOwnerUUID(@Nullable UUID uUID) {
		this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uUID));
	}

	public void tame(Player player) {
		this.setTame(true);
		this.setOwnerUUID(player.getUUID());
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
		}
	}

	@Nullable
	public LivingEntity getOwner() {
		try {
			UUID uUID = this.getOwnerUUID();
			return uUID == null ? null : this.level.getPlayerByUUID(uUID);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return this.isOwnedBy(livingEntity) ? false : super.canAttack(livingEntity);
	}

	public boolean isOwnedBy(LivingEntity livingEntity) {
		return livingEntity == this.getOwner();
	}

	public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
		return true;
	}

	@Override
	public Team getTeam() {
		if (this.isTame()) {
			LivingEntity livingEntity = this.getOwner();
			if (livingEntity != null) {
				return livingEntity.getTeam();
			}
		}

		return super.getTeam();
	}

	@Override
	public boolean isAlliedTo(Entity entity) {
		if (this.isTame()) {
			LivingEntity livingEntity = this.getOwner();
			if (entity == livingEntity) {
				return true;
			}

			if (livingEntity != null) {
				return livingEntity.isAlliedTo(entity);
			}
		}

		return super.isAlliedTo(entity);
	}

	@Override
	public void die(DamageSource damageSource) {
		if (!this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
			this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
		}

		super.die(damageSource);
	}

	public boolean isOrderedToSit() {
		return this.orderedToSit;
	}

	public void setOrderedToSit(boolean bl) {
		this.orderedToSit = bl;
	}
}
