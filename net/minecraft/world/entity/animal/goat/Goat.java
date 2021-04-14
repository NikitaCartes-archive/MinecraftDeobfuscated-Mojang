/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;

public class Goat
extends Animal {
    public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.scalable(0.9f, 1.3f).scale(0.7f);
    protected static final ImmutableList<SensorType<? extends Sensor<? super Goat>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, new MemoryModuleType[]{MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET});
    public static final int GOAT_FALL_DAMAGE_REDUCTION = 10;
    public static final double GOAT_SCREAMING_CHANCE = 0.02;
    private static final EntityDataAccessor<Boolean> DATA_IS_SCREAMING_GOAT = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
    private boolean isLoweringHead;
    private int lowerHeadTick;

    public Goat(EntityType<? extends Goat> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.getNavigation().setCanFloat(true);
    }

    protected Brain.Provider<Goat> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return GoatAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2f).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected int calculateFallDamage(float f, float g) {
        return super.calculateFallDamage(f, g) - 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_AMBIENT;
        }
        return SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_HURT;
        }
        return SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_DEATH;
        }
        return SoundEvents.GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.GOAT_STEP, 0.15f, 1.0f);
    }

    protected SoundEvent getMilkingSound() {
        if (this.isScreamingGoat()) {
            return SoundEvents.GOAT_SCREAMING_MILK;
        }
        return SoundEvents.GOAT_MILK;
    }

    @Override
    public Goat getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Goat goat = (Goat)ageableMob;
        Goat goat2 = EntityType.GOAT.create(serverLevel);
        if (goat2 != null && goat.isScreamingGoat()) {
            goat2.setScreamingGoat(true);
        }
        return goat2;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        if (this.isBaby()) {
            return entityDimensions.height * 0.95f;
        }
        return 1.3f;
    }

    public Brain<Goat> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("goatBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("goatActivityUpdate");
        GoatAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public int getMaxHeadYRot() {
        return 15;
    }

    @Override
    public SoundEvent getEatingSound(ItemStack itemStack) {
        return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_EAT : SoundEvents.GOAT_EAT;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.BUCKET) && !this.isBaby()) {
            player.playSound(this.getMilkingSound(), 1.0f, 1.0f);
            ItemStack itemStack2 = ItemUtils.createFilledResult(itemStack, player, Items.MILK_BUCKET.getDefaultInstance());
            player.setItemInHand(interactionHand, itemStack2);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        InteractionResult interactionResult = super.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction() && this.isFood(itemStack)) {
            this.level.playSound(null, this, this.getEatingSound(itemStack), SoundSource.NEUTRAL, 1.0f, Mth.randomBetween(this.level.random, 0.8f, 1.2f));
        }
        return interactionResult;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        GoatAi.initMemories(this);
        this.setScreamingGoat(serverLevelAccessor.getRandom().nextDouble() < 0.02);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(pose);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("IsScreamingGoat", this.isScreamingGoat());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setScreamingGoat(compoundTag.getBoolean("IsScreamingGoat"));
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 58) {
            this.isLoweringHead = true;
        } else if (b == 59) {
            this.isLoweringHead = false;
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public void aiStep() {
        this.lowerHeadTick = this.isLoweringHead ? ++this.lowerHeadTick : (this.lowerHeadTick -= 2);
        this.lowerHeadTick = Mth.clamp(this.lowerHeadTick, 0, 20);
        super.aiStep();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SCREAMING_GOAT, false);
    }

    public boolean isScreamingGoat() {
        return this.entityData.get(DATA_IS_SCREAMING_GOAT);
    }

    public void setScreamingGoat(boolean bl) {
        this.entityData.set(DATA_IS_SCREAMING_GOAT, bl);
    }

    public float getRammingXHeadRot() {
        return (float)this.lowerHeadTick / 20.0f * 30.0f * ((float)Math.PI / 180);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GoatPathNavigation(this, level);
    }

    @Override
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class GoatNodeEvaluator
    extends WalkNodeEvaluator {
        private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

        private GoatNodeEvaluator() {
        }

        @Override
        public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
            this.belowPos.set(i, j - 1, k);
            BlockPathTypes blockPathTypes = GoatNodeEvaluator.getBlockPathTypeRaw(blockGetter, this.belowPos);
            if (blockPathTypes == BlockPathTypes.POWDER_SNOW) {
                return BlockPathTypes.BLOCKED;
            }
            return GoatNodeEvaluator.getBlockPathTypeStatic(blockGetter, this.belowPos.move(Direction.UP));
        }
    }

    static class GoatPathNavigation
    extends GroundPathNavigation {
        GoatPathNavigation(Goat goat, Level level) {
            super(goat, level);
        }

        @Override
        protected PathFinder createPathFinder(int i) {
            this.nodeEvaluator = new GoatNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, i);
        }
    }
}

