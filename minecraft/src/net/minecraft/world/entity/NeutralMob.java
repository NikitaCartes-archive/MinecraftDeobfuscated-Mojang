package net.minecraft.world.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface NeutralMob {
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
		if (compoundTag.hasUUID("AngryAt")) {
			this.setPersistentAngerTarget(compoundTag.getUUID("AngryAt"));
			UUID uUID = this.getPersistentAngerTarget();
			Player player = uUID == null ? null : level.getPlayerByUUID(uUID);
			if (player != null) {
				this.setLastHurtByMob(player);
				this.setLastHurtByPlayer(player);
			}
		}
	}

	default void updatePersistentAnger() {
		LivingEntity livingEntity = this.getTarget();
		if (livingEntity != null && livingEntity.getType() == EntityType.PLAYER) {
			this.setPersistentAngerTarget(livingEntity.getUUID());
			if (this.getRemainingPersistentAngerTime() <= 0) {
				this.startPersistentAngerTimer();
			}
		} else {
			int i = this.getRemainingPersistentAngerTime();
			if (i > 0) {
				this.setRemainingPersistentAngerTime(i - 1);
				if (this.getRemainingPersistentAngerTime() == 0) {
					this.setPersistentAngerTarget(null);
				}
			}
		}
	}

	default boolean isAngryAt(LivingEntity livingEntity) {
		if (livingEntity instanceof Player && EntitySelector.ATTACK_ALLOWED.test(livingEntity)) {
			boolean bl = livingEntity.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER);
			return bl ? this.isAngry() : livingEntity.getUUID().equals(this.getPersistentAngerTarget());
		} else {
			return false;
		}
	}

	default boolean isAngry() {
		return this.getRemainingPersistentAngerTime() > 0;
	}

	default void playerDied(Player player) {
		if (!player.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
		}

		if (player.getUUID().equals(this.getPersistentAngerTarget())) {
			this.setLastHurtByMob(null);
			this.setPersistentAngerTarget(null);
			this.setTarget(null);
			this.setRemainingPersistentAngerTime(0);
		}
	}

	void setLastHurtByMob(@Nullable LivingEntity livingEntity);

	void setLastHurtByPlayer(@Nullable Player player);

	void setTarget(@Nullable LivingEntity livingEntity);

	@Nullable
	LivingEntity getTarget();
}
