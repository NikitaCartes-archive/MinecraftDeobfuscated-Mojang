package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface NeutralMob {
	String TAG_ANGER_TIME = "AngerTime";
	String TAG_ANGRY_AT = "AngryAt";

	int getRemainingPersistentAngerTime();

	void setRemainingPersistentAngerTime(int i);

	@Nullable
	UUID getPersistentAngerTarget();

	void setPersistentAngerTarget(@Nullable UUID uUID);

	void startPersistentAngerTimer();

	default void addPersistentAngerSaveData(CompoundTag compoundTag) {
		compoundTag.putInt("AngerTime", this.getRemainingPersistentAngerTime());
		if (this.getPersistentAngerTarget() != null) {
			compoundTag.putUUID("AngryAt", this.getPersistentAngerTarget());
		}
	}

	default void readPersistentAngerSaveData(Level level, CompoundTag compoundTag) {
		this.setRemainingPersistentAngerTime(compoundTag.getInt("AngerTime"));
		if (level instanceof ServerLevel) {
			if (!compoundTag.hasUUID("AngryAt")) {
				this.setPersistentAngerTarget(null);
			} else {
				UUID uUID = compoundTag.getUUID("AngryAt");
				this.setPersistentAngerTarget(uUID);
				Entity entity = ((ServerLevel)level).getEntity(uUID);
				if (entity != null) {
					if (entity instanceof Mob) {
						this.setLastHurtByMob((Mob)entity);
					}

					if (entity.getType() == EntityType.PLAYER) {
						this.setLastHurtByPlayer((Player)entity);
					}
				}
			}
		}
	}

	default void updatePersistentAnger(ServerLevel serverLevel, boolean bl) {
		LivingEntity livingEntity = this.getTarget();
		UUID uUID = this.getPersistentAngerTarget();
		if ((livingEntity == null || livingEntity.isDeadOrDying()) && uUID != null && serverLevel.getEntity(uUID) instanceof Mob) {
			this.stopBeingAngry();
		} else {
			if (livingEntity != null && !Objects.equals(uUID, livingEntity.getUUID())) {
				this.setPersistentAngerTarget(livingEntity.getUUID());
				this.startPersistentAngerTimer();
			}

			if (this.getRemainingPersistentAngerTime() > 0 && (livingEntity == null || livingEntity.getType() != EntityType.PLAYER || !bl)) {
				this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
				if (this.getRemainingPersistentAngerTime() == 0) {
					this.stopBeingAngry();
				}
			}
		}
	}

	default boolean isAngryAt(LivingEntity livingEntity) {
		if (!this.canAttack(livingEntity)) {
			return false;
		} else {
			return livingEntity.getType() == EntityType.PLAYER && this.isAngryAtAllPlayers(livingEntity.level())
				? true
				: livingEntity.getUUID().equals(this.getPersistentAngerTarget());
		}
	}

	default boolean isAngryAtAllPlayers(Level level) {
		return level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
	}

	default boolean isAngry() {
		return this.getRemainingPersistentAngerTime() > 0;
	}

	default void playerDied(Player player) {
		if (player.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
			if (player.getUUID().equals(this.getPersistentAngerTarget())) {
				this.stopBeingAngry();
			}
		}
	}

	default void forgetCurrentTargetAndRefreshUniversalAnger() {
		this.stopBeingAngry();
		this.startPersistentAngerTimer();
	}

	default void stopBeingAngry() {
		this.setLastHurtByMob(null);
		this.setPersistentAngerTarget(null);
		this.setTarget(null);
		this.setRemainingPersistentAngerTime(0);
	}

	@Nullable
	LivingEntity getLastHurtByMob();

	void setLastHurtByMob(@Nullable LivingEntity livingEntity);

	void setLastHurtByPlayer(@Nullable Player player);

	void setTarget(@Nullable LivingEntity livingEntity);

	boolean canAttack(LivingEntity livingEntity);

	@Nullable
	LivingEntity getTarget();
}
