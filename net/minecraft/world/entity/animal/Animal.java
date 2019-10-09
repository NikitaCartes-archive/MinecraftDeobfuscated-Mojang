/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public abstract class Animal
extends AgableMob {
    private int inLove;
    private UUID loveCause;

    protected Animal(EntityType<? extends Animal> entityType, Level level) {
        super((EntityType<? extends AgableMob>)entityType, level);
    }

    @Override
    protected void customServerAiStep() {
        if (this.getAge() != 0) {
            this.inLove = 0;
        }
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getAge() != 0) {
            this.inLove = 0;
        }
        if (this.inLove > 0) {
            --this.inLove;
            if (this.inLove % 10 == 0) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        this.inLove = 0;
        return super.hurt(damageSource, f);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos.below()).getBlock() == Blocks.GRASS_BLOCK) {
            return 10.0f;
        }
        return levelReader.getBrightness(blockPos) - 0.5f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("InLove", this.inLove);
        if (this.loveCause != null) {
            compoundTag.putUUID("LoveCause", this.loveCause);
        }
    }

    @Override
    public double getRidingHeight() {
        return 0.14;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.inLove = compoundTag.getInt("InLove");
        this.loveCause = compoundTag.hasUUID("LoveCause") ? compoundTag.getUUID("LoveCause") : null;
    }

    public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getBlockState(blockPos.below()).getBlock() == Blocks.GRASS_BLOCK && levelAccessor.getRawBrightness(blockPos, 0) > 8;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected int getExperienceReward(Player player) {
        return 1 + this.level.random.nextInt(3);
    }

    public boolean isFood(ItemStack itemStack) {
        return itemStack.getItem() == Items.WHEAT;
    }

    @Override
    public boolean mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (this.isFood(itemStack)) {
            if (this.getAge() == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, itemStack);
                this.setInLove(player);
                return true;
            }
            if (this.isBaby()) {
                this.usePlayerItem(player, itemStack);
                this.ageUp((int)((float)(-this.getAge() / 20) * 0.1f), true);
                return true;
            }
        }
        return super.mobInteract(player, interactionHand);
    }

    protected void usePlayerItem(Player player, ItemStack itemStack) {
        if (!player.abilities.instabuild) {
            itemStack.shrink(1);
        }
    }

    public boolean canFallInLove() {
        return this.inLove <= 0;
    }

    public void setInLove(@Nullable Player player) {
        this.inLove = 600;
        if (player != null) {
            this.loveCause = player.getUUID();
        }
        this.level.broadcastEntityEvent(this, (byte)18);
    }

    public void setInLoveTime(int i) {
        this.inLove = i;
    }

    @Nullable
    public ServerPlayer getLoveCause() {
        if (this.loveCause == null) {
            return null;
        }
        Player player = this.level.getPlayerByUUID(this.loveCause);
        if (player instanceof ServerPlayer) {
            return (ServerPlayer)player;
        }
        return null;
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public void resetLove() {
        this.inLove = 0;
    }

    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (animal.getClass() != this.getClass()) {
            return false;
        }
        return this.isInLove() && animal.isInLove();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void handleEntityEvent(byte b) {
        if (b == 18) {
            for (int i = 0; i < 7; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
            }
        } else {
            super.handleEntityEvent(b);
        }
    }
}

