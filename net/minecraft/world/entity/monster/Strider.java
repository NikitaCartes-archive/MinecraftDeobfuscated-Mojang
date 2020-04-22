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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Strider
extends Animal
implements ItemSteerable,
Saddleable {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WARPED_FUNGUS);
    private static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering;
    private TemptGoal temptGoal;
    private PanicGoal panicGoal;

    public Strider(EntityType<? extends Strider> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
        this.blocksBuilding = true;
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0f);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0f);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0f);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getBlockState(blockPos.above()).isAir();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BOOST_TIME.equals(entityDataAccessor) && this.level.isClientSide) {
            this.steering.onSynced();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BOOST_TIME, 0);
        this.entityData.define(DATA_SUFFOCATING, false);
        this.entityData.define(DATA_SADDLE_ID, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.steering.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.steering.readAdditionalSaveData(compoundTag);
    }

    @Override
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource soundSource) {
        this.steering.setSaddle(true);
        if (soundSource != null) {
            this.level.playSound(null, this, SoundEvents.STRIDER_SADDLE, soundSource, 0.5f, 1.0f);
        }
    }

    @Override
    protected void registerGoals() {
        this.panicGoal = new PanicGoal(this, 1.65);
        this.goalSelector.addGoal(1, this.panicGoal);
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.temptGoal = new TemptGoal((PathfinderMob)this, 1.4, false, TEMPT_ITEMS);
        this.goalSelector.addGoal(4, this.temptGoal);
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0f));
    }

    public void setSuffocating(boolean bl) {
        this.entityData.set(DATA_SUFFOCATING, bl);
    }

    public boolean isSuffocating() {
        return this.entityData.get(DATA_SUFFOCATING);
    }

    @Override
    public boolean canFloatInLava() {
        return true;
    }

    @Override
    @Nullable
    public AABB getCollideAgainstBox(Entity entity) {
        if (entity.isPushable()) {
            return entity.getBoundingBox();
        }
        return null;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public double getRideHeight() {
        float f = Math.min(0.25f, this.animationSpeed);
        float g = this.animationPosition;
        return 1.4 + (double)(0.12f * Mth.cos(g * 1.5f) * 2.0f * f);
    }

    @Override
    public boolean canBeControlledByRider() {
        Entity entity = this.getControllingPassenger();
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = (Player)entity;
        return player.getMainHandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK || player.getOffhandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        if (this.getPassengers().isEmpty()) {
            return null;
        }
        return this.getPassengers().get(0);
    }

    @Override
    public void travel(Vec3 vec3) {
        this.setSpeed(this.getMoveSpeed());
        this.travel(this, this.steering, vec3);
    }

    public float getMoveSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.66f : 1.0f);
    }

    @Override
    public float getSteeringSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.23f : 0.55f);
    }

    @Override
    public void travelWithInput(Vec3 vec3) {
        super.travel(vec3);
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.6f;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0f, 1.0f);
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Override
    public void tick() {
        if (this.temptGoal != null && this.temptGoal.isRunning() && this.random.nextInt(100) == 0) {
            this.playSound(SoundEvents.STRIDER_HAPPY, 1.0f, this.getVoicePitch());
        }
        if (this.panicGoal != null && this.panicGoal.isRunning() && this.random.nextInt(60) == 0) {
            this.playSound(SoundEvents.STRIDER_RETREAT, 1.0f, this.getVoicePitch());
        }
        BlockState blockState = this.level.getBlockState(this.blockPosition());
        BlockState blockState2 = this.getBlockStateOn();
        boolean bl = blockState.is(BlockTags.STRIDER_WARM_BLOCKS) || blockState2.is(BlockTags.STRIDER_WARM_BLOCKS);
        this.setSuffocating(!bl && !this.isPassenger());
        if (this.isInLava()) {
            this.onGround = true;
        }
        super.tick();
        this.floatStrider();
        this.checkInsideBlocks();
    }

    @Override
    protected boolean shouldPassengersInheritMalus() {
        return true;
    }

    public float getLavaLevel() {
        AABB aABB = this.getBoundingBox();
        float f = -1.0f;
        float g = 0.0f;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(aABB.getCenter().x, aABB.minY + 0.5, aABB.getCenter().z);
        FluidState fluidState = this.level.getFluidState(mutableBlockPos);
        while (fluidState.is(FluidTags.LAVA)) {
            f = mutableBlockPos.getY();
            g = fluidState.getHeight(this.level, mutableBlockPos);
            mutableBlockPos.move(0, 1, 0);
            fluidState = this.level.getFluidState(mutableBlockPos);
        }
        return f + g;
    }

    private void floatStrider() {
        Vec3 vec3 = this.getDeltaMovement();
        AABB aABB = this.getBoundingBox();
        if (this.isInLava()) {
            boolean bl = aABB.minY <= (double)this.getLavaLevel() - (this.isBaby() ? 0.0 : 0.25);
            this.setDeltaMovement(vec3.x, bl ? vec3.y + 0.01 : -0.01, vec3.z);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.15f).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().isEmpty() && !this.isUnderLiquid(FluidTags.LAVA);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isInWaterRainOrBubble()) {
            this.hurt(DamageSource.DROWN, 1.0f);
        }
        super.customServerAiStep();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return this.isInLava() || super.isNoGravity();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new StriderPathNavigation(this, level);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos).getFluidState().is(FluidTags.LAVA)) {
            return 10.0f;
        }
        return 0.0f;
    }

    @Override
    public Strider getBreedOffspring(AgableMob agableMob) {
        return EntityType.STRIDER.create(this.level);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }
    }

    @Override
    public boolean mobInteract(Player player, InteractionHand interactionHand) {
        boolean bl = this.isFood(player.getItemInHand(interactionHand));
        if (!super.mobInteract(player, interactionHand)) {
            if (this.isSaddled() && !this.isVehicle() && !this.isBaby()) {
                if (!this.level.isClientSide) {
                    player.startRiding(this);
                }
                return true;
            }
            ItemStack itemStack = player.getItemInHand(interactionHand);
            return itemStack.getItem() == Items.SADDLE && itemStack.interactEnemy(player, this, interactionHand);
        }
        if (bl && !this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STRIDER_EAT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(LevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        ZombifiedPiglin zombifiedPiglin;
        StriderGroupData.Rider rider;
        if (spawnGroupData instanceof StriderGroupData) {
            rider = ((StriderGroupData)spawnGroupData).rider;
        } else {
            rider = this.random.nextInt(30) == 0 ? StriderGroupData.Rider.PIGLIN_RIDER : (this.random.nextInt(10) == 0 ? StriderGroupData.Rider.BABY_RIDER : StriderGroupData.Rider.NO_RIDER);
            spawnGroupData = new StriderGroupData(rider);
            ((AgableMob.AgableMobGroupData)spawnGroupData).setBabySpawnChance(rider == StriderGroupData.Rider.NO_RIDER ? 0.5f : 0.0f);
        }
        PathfinderMob mob = null;
        if (rider == StriderGroupData.Rider.BABY_RIDER) {
            Strider strider = EntityType.STRIDER.create(levelAccessor.getLevel());
            if (strider != null) {
                mob = strider;
                strider.setAge(-24000);
            }
        } else if (rider == StriderGroupData.Rider.PIGLIN_RIDER && (zombifiedPiglin = EntityType.ZOMBIFIED_PIGLIN.create(levelAccessor.getLevel())) != null) {
            mob = zombifiedPiglin;
            this.equipSaddle(null);
        }
        if (mob != null) {
            mob.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0f);
            mob.finalizeSpawn(levelAccessor, difficultyInstance, MobSpawnType.JOCKEY, null, null);
            mob.startRiding(this, true);
            levelAccessor.addFreshEntity(mob);
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public /* synthetic */ AgableMob getBreedOffspring(AgableMob agableMob) {
        return this.getBreedOffspring(agableMob);
    }

    static class StriderPathNavigation
    extends GroundPathNavigation {
        StriderPathNavigation(Strider strider, Level level) {
            super(strider, level);
        }

        @Override
        protected PathFinder createPathFinder(int i) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, i);
        }

        @Override
        protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
            if (blockPathTypes == BlockPathTypes.LAVA || blockPathTypes == BlockPathTypes.DAMAGE_FIRE || blockPathTypes == BlockPathTypes.DANGER_FIRE) {
                return true;
            }
            return super.hasValidPathType(blockPathTypes);
        }

        @Override
        public boolean isStableDestination(BlockPos blockPos) {
            return this.level.getBlockState(blockPos).getBlock() == Blocks.LAVA || super.isStableDestination(blockPos);
        }
    }

    public static class StriderGroupData
    extends AgableMob.AgableMobGroupData {
        public final Rider rider;

        public StriderGroupData(Rider rider) {
            this.rider = rider;
        }

        public static enum Rider {
            NO_RIDER,
            BABY_RIDER,
            PIGLIN_RIDER;

        }
    }
}

