/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Spider
extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);

    public Spider(EntityType<? extends Spider> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(4, new SpiderAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new SpiderTargetGoal<Player>(this, Player.class));
        this.targetSelector.addGoal(3, new SpiderTargetGoal<IronGolem>(this, IronGolem.class));
    }

    @Override
    public double getRideHeight() {
        return this.getBbHeight() * 0.5f;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.setClimbing(this.horizontalCollision);
        }
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean onLadder() {
        return this.isClimbing();
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
        if (blockState.getBlock() != Blocks.COBWEB) {
            super.makeStuckInBlock(blockState, vec3);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.POISON) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean bl) {
        byte b = this.entityData.get(DATA_FLAGS_ID);
        b = bl ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE);
        this.entityData.set(DATA_FLAGS_ID, b);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(LevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        MobEffect mobEffect;
        spawnGroupData = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        if (levelAccessor.getRandom().nextInt(100) == 0) {
            Skeleton skeleton = EntityType.SKELETON.create(this.level);
            skeleton.moveTo(this.x, this.y, this.z, this.yRot, 0.0f);
            skeleton.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, null, null);
            levelAccessor.addFreshEntity(skeleton);
            skeleton.startRiding(this);
        }
        if (spawnGroupData == null) {
            spawnGroupData = new SpiderEffectsGroupData();
            if (levelAccessor.getDifficulty() == Difficulty.HARD && levelAccessor.getRandom().nextFloat() < 0.1f * difficultyInstance.getSpecialMultiplier()) {
                ((SpiderEffectsGroupData)spawnGroupData).setRandomEffect(levelAccessor.getRandom());
            }
        }
        if (spawnGroupData instanceof SpiderEffectsGroupData && (mobEffect = ((SpiderEffectsGroupData)spawnGroupData).effect) != null) {
            this.addEffect(new MobEffectInstance(mobEffect, Integer.MAX_VALUE));
        }
        return spawnGroupData;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.65f;
    }

    static class SpiderTargetGoal<T extends LivingEntity>
    extends NearestAttackableTargetGoal<T> {
        public SpiderTargetGoal(Spider spider, Class<T> class_) {
            super((Mob)spider, class_, true);
        }

        @Override
        public boolean canUse() {
            float f = this.mob.getBrightness();
            if (f >= 0.5f) {
                return false;
            }
            return super.canUse();
        }
    }

    static class SpiderAttackGoal
    extends MeleeAttackGoal {
        public SpiderAttackGoal(Spider spider) {
            super(spider, 1.0, true);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        @Override
        public boolean canContinueToUse() {
            float f = this.mob.getBrightness();
            if (f >= 0.5f && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            return 4.0f + livingEntity.getBbWidth();
        }
    }

    public static class SpiderEffectsGroupData
    implements SpawnGroupData {
        public MobEffect effect;

        public void setRandomEffect(Random random) {
            int i = random.nextInt(5);
            if (i <= 1) {
                this.effect = MobEffects.MOVEMENT_SPEED;
            } else if (i <= 2) {
                this.effect = MobEffects.DAMAGE_BOOST;
            } else if (i <= 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (i <= 4) {
                this.effect = MobEffects.INVISIBILITY;
            }
        }
    }
}

