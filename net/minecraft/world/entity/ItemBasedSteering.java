/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ItemBasedSteering {
    private static final int MIN_BOOST_TIME = 140;
    private static final int MAX_BOOST_TIME = 700;
    private final SynchedEntityData entityData;
    private final EntityDataAccessor<Integer> boostTimeAccessor;
    private final EntityDataAccessor<Boolean> hasSaddleAccessor;
    private boolean boosting;
    private int boostTime;

    public ItemBasedSteering(SynchedEntityData synchedEntityData, EntityDataAccessor<Integer> entityDataAccessor, EntityDataAccessor<Boolean> entityDataAccessor2) {
        this.entityData = synchedEntityData;
        this.boostTimeAccessor = entityDataAccessor;
        this.hasSaddleAccessor = entityDataAccessor2;
    }

    public void onSynced() {
        this.boosting = true;
        this.boostTime = 0;
    }

    public boolean boost(RandomSource randomSource) {
        if (this.boosting) {
            return false;
        }
        this.boosting = true;
        this.boostTime = 0;
        this.entityData.set(this.boostTimeAccessor, randomSource.nextInt(841) + 140);
        return true;
    }

    public void tickBoost() {
        if (this.boosting && this.boostTime++ > this.boostTimeTotal()) {
            this.boosting = false;
        }
    }

    public float boostFactor() {
        if (this.boosting) {
            return 1.0f + 1.15f * Mth.sin((float)this.boostTime / (float)this.boostTimeTotal() * (float)Math.PI);
        }
        return 1.0f;
    }

    private int boostTimeTotal() {
        return this.entityData.get(this.boostTimeAccessor);
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("Saddle", this.hasSaddle());
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setSaddle(compoundTag.getBoolean("Saddle"));
    }

    public void setSaddle(boolean bl) {
        this.entityData.set(this.hasSaddleAccessor, bl);
    }

    public boolean hasSaddle() {
        return this.entityData.get(this.hasSaddleAccessor);
    }
}

