/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@EnvironmentInterfaces(value={@EnvironmentInterface(value=EnvType.CLIENT, itf=PowerableMob.class)})
public class WitherBoss
extends Monster
implements PowerableMob,
RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private final float[] xRotHeads = new float[2];
    private final float[] yRotHeads = new float[2];
    private final float[] xRotOHeads = new float[2];
    private final float[] yRotOHeads = new float[2];
    private final int[] nextHeadUpdate = new int[2];
    private final int[] idleHeadUpdates = new int[2];
    private int destroyBlocksTick;
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(true);
    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = livingEntity -> livingEntity.getMobType() != MobType.UNDEAD && livingEntity.attackable();
    private static final TargetingConditions TARGETING_CONDITIONS = new TargetingConditions().range(20.0).selector(LIVING_ENTITY_SELECTOR);

    public WitherBoss(EntityType<? extends WitherBoss> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.setHealth(this.getMaxHealth());
        this.getNavigation().setCanFloat(true);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WitherDoNothingGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Mob>(this, Mob.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_A, 0);
        this.entityData.define(DATA_TARGET_B, 0);
        this.entityData.define(DATA_TARGET_C, 0);
        this.entityData.define(DATA_ID_INV, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Invul", this.getInvulnerableTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setInvulnerableTicks(compoundTag.getInt("Invul"));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        super.setCustomName(component);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void aiStep() {
        int j;
        int i;
        Entity entity;
        Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.6, 1.0);
        if (!this.level.isClientSide && this.getAlternativeTarget(0) > 0 && (entity = this.level.getEntity(this.getAlternativeTarget(0))) != null) {
            double d = vec3.y;
            if (this.getY() < entity.getY() || !this.isPowered() && this.getY() < entity.getY() + 5.0) {
                d = Math.max(0.0, d);
                d += 0.3 - d * (double)0.6f;
            }
            vec3 = new Vec3(vec3.x, d, vec3.z);
            Vec3 vec32 = new Vec3(entity.getX() - this.getX(), 0.0, entity.getZ() - this.getZ());
            if (WitherBoss.getHorizontalDistanceSqr(vec32) > 9.0) {
                Vec3 vec33 = vec32.normalize();
                vec3 = vec3.add(vec33.x * 0.3 - vec3.x * 0.6, 0.0, vec33.z * 0.3 - vec3.z * 0.6);
            }
        }
        this.setDeltaMovement(vec3);
        if (WitherBoss.getHorizontalDistanceSqr(vec3) > 0.05) {
            this.yRot = (float)Mth.atan2(vec3.z, vec3.x) * 57.295776f - 90.0f;
        }
        super.aiStep();
        for (i = 0; i < 2; ++i) {
            this.yRotOHeads[i] = this.yRotHeads[i];
            this.xRotOHeads[i] = this.xRotHeads[i];
        }
        for (i = 0; i < 2; ++i) {
            int j2 = this.getAlternativeTarget(i + 1);
            Entity entity2 = null;
            if (j2 > 0) {
                entity2 = this.level.getEntity(j2);
            }
            if (entity2 != null) {
                double e = this.getHeadX(i + 1);
                double f = this.getHeadY(i + 1);
                double g = this.getHeadZ(i + 1);
                double h = entity2.getX() - e;
                double k = entity2.getEyeY() - f;
                double l = entity2.getZ() - g;
                double m = Mth.sqrt(h * h + l * l);
                float n = (float)(Mth.atan2(l, h) * 57.2957763671875) - 90.0f;
                float o = (float)(-(Mth.atan2(k, m) * 57.2957763671875));
                this.xRotHeads[i] = this.rotlerp(this.xRotHeads[i], o, 40.0f);
                this.yRotHeads[i] = this.rotlerp(this.yRotHeads[i], n, 10.0f);
                continue;
            }
            this.yRotHeads[i] = this.rotlerp(this.yRotHeads[i], this.yBodyRot, 10.0f);
        }
        boolean bl = this.isPowered();
        for (j = 0; j < 3; ++j) {
            double p = this.getHeadX(j);
            double q = this.getHeadY(j);
            double r = this.getHeadZ(j);
            this.level.addParticle(ParticleTypes.SMOKE, p + this.random.nextGaussian() * (double)0.3f, q + this.random.nextGaussian() * (double)0.3f, r + this.random.nextGaussian() * (double)0.3f, 0.0, 0.0, 0.0);
            if (!bl || this.level.random.nextInt(4) != 0) continue;
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, p + this.random.nextGaussian() * (double)0.3f, q + this.random.nextGaussian() * (double)0.3f, r + this.random.nextGaussian() * (double)0.3f, 0.7f, 0.7f, 0.5);
        }
        if (this.getInvulnerableTicks() > 0) {
            for (j = 0; j < 3; ++j) {
                this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * 3.3f), this.getZ() + this.random.nextGaussian(), 0.7f, 0.7f, 0.9f);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        int j;
        int i;
        if (this.getInvulnerableTicks() > 0) {
            int i2 = this.getInvulnerableTicks() - 1;
            if (i2 <= 0) {
                Explosion.BlockInteraction blockInteraction = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
                this.level.explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0f, false, blockInteraction);
                if (!this.isSilent()) {
                    this.level.globalLevelEvent(1023, this.blockPosition(), 0);
                }
            }
            this.setInvulnerableTicks(i2);
            if (this.tickCount % 10 == 0) {
                this.heal(10.0f);
            }
            return;
        }
        super.customServerAiStep();
        block0: for (i = 1; i < 3; ++i) {
            if (this.tickCount < this.nextHeadUpdate[i - 1]) continue;
            this.nextHeadUpdate[i - 1] = this.tickCount + 10 + this.random.nextInt(10);
            if (this.level.getDifficulty() == Difficulty.NORMAL || this.level.getDifficulty() == Difficulty.HARD) {
                int n = i - 1;
                int n2 = this.idleHeadUpdates[n];
                this.idleHeadUpdates[n] = n2 + 1;
                if (n2 > 15) {
                    float f = 10.0f;
                    float g = 5.0f;
                    double d = Mth.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
                    double e = Mth.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
                    double h = Mth.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
                    this.performRangedAttack(i + 1, d, e, h, true);
                    this.idleHeadUpdates[i - 1] = 0;
                }
            }
            if ((j = this.getAlternativeTarget(i)) > 0) {
                Entity entity = this.level.getEntity(j);
                if (entity == null || !entity.isAlive() || this.distanceToSqr(entity) > 900.0 || !this.canSee(entity)) {
                    this.setAlternativeTarget(i, 0);
                    continue;
                }
                if (entity instanceof Player && ((Player)entity).abilities.invulnerable) {
                    this.setAlternativeTarget(i, 0);
                    continue;
                }
                this.performRangedAttack(i + 1, (LivingEntity)entity);
                this.nextHeadUpdate[i - 1] = this.tickCount + 40 + this.random.nextInt(20);
                this.idleHeadUpdates[i - 1] = 0;
                continue;
            }
            List<LivingEntity> list = this.level.getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
            for (int k = 0; k < 10 && !list.isEmpty(); ++k) {
                LivingEntity livingEntity = list.get(this.random.nextInt(list.size()));
                if (livingEntity != this && livingEntity.isAlive() && this.canSee(livingEntity)) {
                    if (livingEntity instanceof Player) {
                        if (((Player)livingEntity).abilities.invulnerable) continue block0;
                        this.setAlternativeTarget(i, livingEntity.getId());
                        continue block0;
                    }
                    this.setAlternativeTarget(i, livingEntity.getId());
                    continue block0;
                }
                list.remove(livingEntity);
            }
        }
        if (this.getTarget() != null) {
            this.setAlternativeTarget(0, this.getTarget().getId());
        } else {
            this.setAlternativeTarget(0, 0);
        }
        if (this.destroyBlocksTick > 0) {
            --this.destroyBlocksTick;
            if (this.destroyBlocksTick == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                i = Mth.floor(this.getY());
                j = Mth.floor(this.getX());
                int l = Mth.floor(this.getZ());
                boolean bl = false;
                for (int m = -1; m <= 1; ++m) {
                    for (int n = -1; n <= 1; ++n) {
                        for (int o = 0; o <= 3; ++o) {
                            int p = j + m;
                            int q = i + o;
                            int r = l + n;
                            BlockPos blockPos = new BlockPos(p, q, r);
                            BlockState blockState = this.level.getBlockState(blockPos);
                            if (!WitherBoss.canDestroy(blockState)) continue;
                            bl = this.level.destroyBlock(blockPos, true, this) || bl;
                        }
                    }
                }
                if (bl) {
                    this.level.levelEvent(null, 1022, this.blockPosition(), 0);
                }
            }
        }
        if (this.tickCount % 20 == 0) {
            this.heal(1.0f);
        }
        this.bossEvent.setPercent(this.getHealth() / this.getMaxHealth());
    }

    public static boolean canDestroy(BlockState blockState) {
        return !blockState.isAir() && !BlockTags.WITHER_IMMUNE.contains(blockState.getBlock());
    }

    public void makeInvulnerable() {
        this.setInvulnerableTicks(220);
        this.setHealth(this.getMaxHealth() / 3.0f);
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    private double getHeadX(int i) {
        if (i <= 0) {
            return this.getX();
        }
        float f = (this.yBodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180);
        float g = Mth.cos(f);
        return this.getX() + (double)g * 1.3;
    }

    private double getHeadY(int i) {
        if (i <= 0) {
            return this.getY() + 3.0;
        }
        return this.getY() + 2.2;
    }

    private double getHeadZ(int i) {
        if (i <= 0) {
            return this.getZ();
        }
        float f = (this.yBodyRot + (float)(180 * (i - 1))) * ((float)Math.PI / 180);
        float g = Mth.sin(f);
        return this.getZ() + (double)g * 1.3;
    }

    private float rotlerp(float f, float g, float h) {
        float i = Mth.wrapDegrees(g - f);
        if (i > h) {
            i = h;
        }
        if (i < -h) {
            i = -h;
        }
        return f + i;
    }

    private void performRangedAttack(int i, LivingEntity livingEntity) {
        this.performRangedAttack(i, livingEntity.getX(), livingEntity.getY() + (double)livingEntity.getEyeHeight() * 0.5, livingEntity.getZ(), i == 0 && this.random.nextFloat() < 0.001f);
    }

    private void performRangedAttack(int i, double d, double e, double f, boolean bl) {
        if (!this.isSilent()) {
            this.level.levelEvent(null, 1024, this.blockPosition(), 0);
        }
        double g = this.getHeadX(i);
        double h = this.getHeadY(i);
        double j = this.getHeadZ(i);
        double k = d - g;
        double l = e - h;
        double m = f - j;
        WitherSkull witherSkull = new WitherSkull(this.level, this, k, l, m);
        witherSkull.setOwner(this);
        if (bl) {
            witherSkull.setDangerous(true);
        }
        witherSkull.setPosRaw(g, h, j);
        this.level.addFreshEntity(witherSkull);
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.performRangedAttack(0, livingEntity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        Entity entity;
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (damageSource == DamageSource.DROWN || damageSource.getEntity() instanceof WitherBoss) {
            return false;
        }
        if (this.getInvulnerableTicks() > 0 && damageSource != DamageSource.OUT_OF_WORLD) {
            return false;
        }
        if (this.isPowered() && (entity = damageSource.getDirectEntity()) instanceof AbstractArrow) {
            return false;
        }
        entity = damageSource.getEntity();
        if (entity != null && !(entity instanceof Player) && entity instanceof LivingEntity && ((LivingEntity)entity).getMobType() == this.getMobType()) {
            return false;
        }
        if (this.destroyBlocksTick <= 0) {
            this.destroyBlocksTick = 20;
        }
        int i = 0;
        while (i < this.idleHeadUpdates.length) {
            int n = i++;
            this.idleHeadUpdates[n] = this.idleHeadUpdates[n] + 3;
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
        super.dropCustomDeathLoot(damageSource, i, bl);
        ItemEntity itemEntity = this.spawnAtLocation(Items.NETHER_STAR);
        if (itemEntity != null) {
            itemEntity.setExtendedLifetime();
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.remove();
            return;
        }
        this.noActionTime = 0;
    }

    @Override
    public boolean causeFallDamage(float f, float g) {
        return false;
    }

    @Override
    public boolean addEffect(MobEffectInstance mobEffectInstance) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.6f).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.ARMOR, 4.0);
    }

    @Environment(value=EnvType.CLIENT)
    public float getHeadYRot(int i) {
        return this.yRotHeads[i];
    }

    @Environment(value=EnvType.CLIENT)
    public float getHeadXRot(int i) {
        return this.xRotHeads[i];
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(DATA_ID_INV);
    }

    public void setInvulnerableTicks(int i) {
        this.entityData.set(DATA_ID_INV, i);
    }

    public int getAlternativeTarget(int i) {
        return this.entityData.get(DATA_TARGETS.get(i));
    }

    public void setAlternativeTarget(int i, int j) {
        this.entityData.set(DATA_TARGETS.get(i), j);
    }

    @Override
    public boolean isPowered() {
        return this.getHealth() <= this.getMaxHealth() / 2.0f;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(mobEffectInstance);
    }

    class WitherDoNothingGoal
    extends Goal {
        public WitherDoNothingGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return WitherBoss.this.getInvulnerableTicks() > 0;
        }
    }
}

