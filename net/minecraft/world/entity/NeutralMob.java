/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface NeutralMob {
    public int getRemainingPersistentAngerTime();

    public void setRemainingPersistentAngerTime(int var1);

    @Nullable
    public UUID getPersistentAngerTarget();

    public void setPersistentAngerTarget(@Nullable UUID var1);

    public void startPersistentAngerTimer();

    default public void addPersistentAngerSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("AngerTime", this.getRemainingPersistentAngerTime());
        if (this.getPersistentAngerTarget() != null) {
            compoundTag.putUUID("AngryAt", this.getPersistentAngerTarget());
        }
    }

    default public void readPersistentAngerSaveData(Level level, CompoundTag compoundTag) {
        this.setRemainingPersistentAngerTime(compoundTag.getInt("AngerTime"));
        if (compoundTag.hasUUID("AngryAt")) {
            Player player;
            this.setPersistentAngerTarget(compoundTag.getUUID("AngryAt"));
            UUID uUID = this.getPersistentAngerTarget();
            Player player2 = player = uUID == null ? null : level.getPlayerByUUID(uUID);
            if (player != null) {
                this.setLastHurtByMob(player);
                this.setLastHurtByPlayer(player);
            }
        }
    }

    default public void updatePersistentAnger() {
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

    default public boolean isAngryAt(LivingEntity livingEntity) {
        if (livingEntity instanceof Player && EntitySelector.ATTACK_ALLOWED.test(livingEntity)) {
            boolean bl = livingEntity.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER);
            return bl ? this.isAngry() : livingEntity.getUUID().equals(this.getPersistentAngerTarget());
        }
        return false;
    }

    default public boolean isAngry() {
        return this.getRemainingPersistentAngerTime() > 0;
    }

    default public void playerDied(Player player) {
        if (!player.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            // empty if block
        }
        if (!player.getUUID().equals(this.getPersistentAngerTarget())) {
            return;
        }
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
        this.setRemainingPersistentAngerTime(0);
    }

    public void setLastHurtByMob(@Nullable LivingEntity var1);

    public void setLastHurtByPlayer(@Nullable Player var1);

    public void setTarget(@Nullable LivingEntity var1);

    @Nullable
    public LivingEntity getTarget();
}

