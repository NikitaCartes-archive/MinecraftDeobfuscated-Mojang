/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHorse
extends Animal
implements ContainerListener,
HasCustomInventoryScreen,
OwnableEntity,
PlayerRideableJumping,
Saddleable {
    public static final int EQUIPMENT_SLOT_OFFSET = 400;
    public static final int CHEST_SLOT_OFFSET = 499;
    public static final int INVENTORY_SLOT_OFFSET = 500;
    public static final double BREEDING_CROSS_FACTOR = 0.15;
    private static final float MIN_MOVEMENT_SPEED = (float)AbstractHorse.generateSpeed(() -> 0.0);
    private static final float MAX_MOVEMENT_SPEED = (float)AbstractHorse.generateSpeed(() -> 1.0);
    private static final float MIN_JUMP_STRENGTH = (float)AbstractHorse.generateJumpStrength(() -> 0.0);
    private static final float MAX_JUMP_STRENGTH = (float)AbstractHorse.generateJumpStrength(() -> 1.0);
    private static final float MIN_HEALTH = AbstractHorse.generateMaxHealth(i -> 0);
    private static final float MAX_HEALTH = AbstractHorse.generateMaxHealth(i -> i - 1);
    private static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25f;
    private static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5f;
    private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = livingEntity -> livingEntity instanceof AbstractHorse && ((AbstractHorse)livingEntity).isBred();
    private static final TargetingConditions MOMMY_TARGETING = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().selector(PARENT_HORSE_SELECTOR);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
    private static final int FLAG_TAME = 2;
    private static final int FLAG_SADDLE = 4;
    private static final int FLAG_BRED = 8;
    private static final int FLAG_EATING = 16;
    private static final int FLAG_STANDING = 32;
    private static final int FLAG_OPEN_MOUTH = 64;
    public static final int INV_SLOT_SADDLE = 0;
    public static final int INV_SLOT_ARMOR = 1;
    public static final int INV_BASE_COUNT = 2;
    private int eatingCounter;
    private int mouthCounter;
    private int standCounter;
    public int tailCounter;
    public int sprintCounter;
    protected boolean isJumping;
    protected SimpleContainer inventory;
    protected int temper;
    protected float playerJumpPendingScale;
    protected boolean allowStandSliding;
    private float eatAnim;
    private float eatAnimO;
    private float standAnim;
    private float standAnimO;
    private float mouthAnim;
    private float mouthAnimO;
    protected boolean canGallop = true;
    protected int gallopSoundCounter;
    @Nullable
    private UUID owner;

    protected AbstractHorse(EntityType<? extends AbstractHorse> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.setMaxUpStep(1.0f);
        this.createInventory();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, AbstractHorse.class));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        if (this.canPerformRearing()) {
            this.goalSelector.addGoal(9, new RandomStandGoal(this));
        }
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE), false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FLAGS, (byte)0);
    }

    protected boolean getFlag(int i) {
        return (this.entityData.get(DATA_ID_FLAGS) & i) != 0;
    }

    protected void setFlag(int i, boolean bl) {
        byte b = this.entityData.get(DATA_ID_FLAGS);
        if (bl) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b | i));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b & ~i));
        }
    }

    public boolean isTamed() {
        return this.getFlag(2);
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.owner;
    }

    public void setOwnerUUID(@Nullable UUID uUID) {
        this.owner = uUID;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public void setTamed(boolean bl) {
        this.setFlag(2, bl);
    }

    public void setIsJumping(boolean bl) {
        this.isJumping = bl;
    }

    @Override
    protected void onLeashDistance(float f) {
        if (f > 6.0f && this.isEating()) {
            this.setEating(false);
        }
    }

    public boolean isEating() {
        return this.getFlag(16);
    }

    public boolean isStanding() {
        return this.getFlag(32);
    }

    public boolean isBred() {
        return this.getFlag(8);
    }

    public void setBred(boolean bl) {
        this.setFlag(8, bl);
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby() && this.isTamed();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource soundSource) {
        this.inventory.setItem(0, new ItemStack(Items.SADDLE));
        if (soundSource != null) {
            this.level.playSound(null, this, this.getSaddleSoundEvent(), soundSource, 0.5f, 1.0f);
        }
    }

    public void equipArmor(Player player, ItemStack itemStack) {
        if (this.isArmor(itemStack)) {
            this.inventory.setItem(1, new ItemStack(itemStack.getItem()));
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }
    }

    @Override
    public boolean isSaddled() {
        return this.getFlag(4);
    }

    public int getTemper() {
        return this.temper;
    }

    public void setTemper(int i) {
        this.temper = i;
    }

    public int modifyTemper(int i) {
        int j = Mth.clamp(this.getTemper() + i, 0, this.getMaxTemper());
        this.setTemper(j);
        return j;
    }

    @Override
    public boolean isPushable() {
        return !this.isVehicle();
    }

    private void eating() {
        SoundEvent soundEvent;
        this.openMouth();
        if (!this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
    }

    @Override
    public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
        int i;
        if (f > 1.0f) {
            this.playSound(SoundEvents.HORSE_LAND, 0.4f, 1.0f);
        }
        if ((i = this.calculateFallDamage(f, g)) <= 0) {
            return false;
        }
        this.hurt(damageSource, i);
        if (this.isVehicle()) {
            for (Entity entity : this.getIndirectPassengers()) {
                entity.hurt(damageSource, i);
            }
        }
        this.playBlockFallSound();
        return true;
    }

    @Override
    protected int calculateFallDamage(float f, float g) {
        return Mth.ceil((f * 0.5f - 3.0f) * g);
    }

    protected int getInventorySize() {
        return 2;
    }

    protected void createInventory() {
        SimpleContainer simpleContainer = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());
        if (simpleContainer != null) {
            simpleContainer.removeListener(this);
            int i = Math.min(simpleContainer.getContainerSize(), this.inventory.getContainerSize());
            for (int j = 0; j < i; ++j) {
                ItemStack itemStack = simpleContainer.getItem(j);
                if (itemStack.isEmpty()) continue;
                this.inventory.setItem(j, itemStack.copy());
            }
        }
        this.inventory.addListener(this);
        this.updateContainerEquipment();
    }

    protected void updateContainerEquipment() {
        if (this.level.isClientSide) {
            return;
        }
        this.setFlag(4, !this.inventory.getItem(0).isEmpty());
    }

    @Override
    public void containerChanged(Container container) {
        boolean bl = this.isSaddled();
        this.updateContainerEquipment();
        if (this.tickCount > 20 && !bl && this.isSaddled()) {
            this.playSound(SoundEvents.HORSE_SADDLE, 0.5f, 1.0f);
        }
    }

    public double getCustomJump() {
        return this.getAttributeValue(Attributes.JUMP_STRENGTH);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        boolean bl = super.hurt(damageSource, f);
        if (bl && this.random.nextInt(3) == 0) {
            this.standIfPossible();
        }
        return bl;
    }

    protected boolean canPerformRearing() {
        return true;
    }

    @Nullable
    protected SoundEvent getEatingSound() {
        return null;
    }

    @Nullable
    protected SoundEvent getAngrySound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (blockState.getMaterial().isLiquid()) {
            return;
        }
        BlockState blockState2 = this.level.getBlockState(blockPos.above());
        SoundType soundType = blockState.getSoundType();
        if (blockState2.is(Blocks.SNOW)) {
            soundType = blockState2.getSoundType();
        }
        if (this.isVehicle() && this.canGallop) {
            ++this.gallopSoundCounter;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                this.playGallopSound(soundType);
            } else if (this.gallopSoundCounter <= 5) {
                this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15f, soundType.getPitch());
            }
        } else if (soundType == SoundType.WOOD) {
            this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15f, soundType.getPitch());
        } else {
            this.playSound(SoundEvents.HORSE_STEP, soundType.getVolume() * 0.15f, soundType.getPitch());
        }
    }

    protected void playGallopSound(SoundType soundType) {
        this.playSound(SoundEvents.HORSE_GALLOP, soundType.getVolume() * 0.15f, soundType.getPitch());
    }

    public static AttributeSupplier.Builder createBaseHorseAttributes() {
        return Mob.createMobAttributes().add(Attributes.JUMP_STRENGTH).add(Attributes.MAX_HEALTH, 53.0).add(Attributes.MOVEMENT_SPEED, 0.225f);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 6;
    }

    public int getMaxTemper() {
        return 100;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level.isClientSide && (!this.isVehicle() || this.hasPassenger(player)) && this.isTamed()) {
            player.openHorseInventory(this, this.inventory);
        }
    }

    public InteractionResult fedFood(Player player, ItemStack itemStack) {
        boolean bl = this.handleEating(player, itemStack);
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        if (this.level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    protected boolean handleEating(Player player, ItemStack itemStack) {
        boolean bl = false;
        float f = 0.0f;
        int i = 0;
        int j = 0;
        if (itemStack.is(Items.WHEAT)) {
            f = 2.0f;
            i = 20;
            j = 3;
        } else if (itemStack.is(Items.SUGAR)) {
            f = 1.0f;
            i = 30;
            j = 3;
        } else if (itemStack.is(Blocks.HAY_BLOCK.asItem())) {
            f = 20.0f;
            i = 180;
        } else if (itemStack.is(Items.APPLE)) {
            f = 3.0f;
            i = 60;
            j = 3;
        } else if (itemStack.is(Items.GOLDEN_CARROT)) {
            f = 4.0f;
            i = 60;
            j = 5;
            if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                bl = true;
                this.setInLove(player);
            }
        } else if (itemStack.is(Items.GOLDEN_APPLE) || itemStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            f = 10.0f;
            i = 240;
            j = 10;
            if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                bl = true;
                this.setInLove(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && i > 0) {
            this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level.isClientSide) {
                this.ageUp(i);
            }
            bl = true;
        }
        if (j > 0 && (bl || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            bl = true;
            if (!this.level.isClientSide) {
                this.modifyTemper(j);
            }
        }
        if (bl) {
            this.eating();
            this.gameEvent(GameEvent.EAT);
        }
        return bl;
    }

    protected void doPlayerRide(Player player) {
        this.setEating(false);
        this.setStanding(false);
        if (!this.level.isClientSide) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        }
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.inventory == null) {
            return;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (itemStack.isEmpty() || EnchantmentHelper.hasVanishingCurse(itemStack)) continue;
            this.spawnAtLocation(itemStack);
        }
    }

    @Override
    public void aiStep() {
        if (this.random.nextInt(200) == 0) {
            this.moveTail();
        }
        super.aiStep();
        if (this.level.isClientSide || !this.isAlive()) {
            return;
        }
        if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0f);
        }
        if (this.canEatGrass()) {
            if (!this.isEating() && !this.isVehicle() && this.random.nextInt(300) == 0 && this.level.getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                this.setEating(true);
            }
            if (this.isEating() && ++this.eatingCounter > 50) {
                this.eatingCounter = 0;
                this.setEating(false);
            }
        }
        this.followMommy();
    }

    protected void followMommy() {
        AbstractHorse livingEntity;
        if (this.isBred() && this.isBaby() && !this.isEating() && (livingEntity = this.level.getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0))) != null && this.distanceToSqr(livingEntity) > 4.0) {
            this.navigation.createPath(livingEntity, 0);
        }
    }

    public boolean canEatGrass() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
            this.mouthCounter = 0;
            this.setFlag(64, false);
        }
        if (this.isEffectiveAi() && this.standCounter > 0 && ++this.standCounter > 20) {
            this.standCounter = 0;
            this.setStanding(false);
        }
        if (this.tailCounter > 0 && ++this.tailCounter > 8) {
            this.tailCounter = 0;
        }
        if (this.sprintCounter > 0) {
            ++this.sprintCounter;
            if (this.sprintCounter > 300) {
                this.sprintCounter = 0;
            }
        }
        this.eatAnimO = this.eatAnim;
        if (this.isEating()) {
            this.eatAnim += (1.0f - this.eatAnim) * 0.4f + 0.05f;
            if (this.eatAnim > 1.0f) {
                this.eatAnim = 1.0f;
            }
        } else {
            this.eatAnim += (0.0f - this.eatAnim) * 0.4f - 0.05f;
            if (this.eatAnim < 0.0f) {
                this.eatAnim = 0.0f;
            }
        }
        this.standAnimO = this.standAnim;
        if (this.isStanding()) {
            this.eatAnimO = this.eatAnim = 0.0f;
            this.standAnim += (1.0f - this.standAnim) * 0.4f + 0.05f;
            if (this.standAnim > 1.0f) {
                this.standAnim = 1.0f;
            }
        } else {
            this.allowStandSliding = false;
            this.standAnim += (0.8f * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6f - 0.05f;
            if (this.standAnim < 0.0f) {
                this.standAnim = 0.0f;
            }
        }
        this.mouthAnimO = this.mouthAnim;
        if (this.getFlag(64)) {
            this.mouthAnim += (1.0f - this.mouthAnim) * 0.7f + 0.05f;
            if (this.mouthAnim > 1.0f) {
                this.mouthAnim = 1.0f;
            }
        } else {
            this.mouthAnim += (0.0f - this.mouthAnim) * 0.7f - 0.05f;
            if (this.mouthAnim < 0.0f) {
                this.mouthAnim = 0.0f;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (this.isVehicle() || this.isBaby()) {
            return super.mobInteract(player, interactionHand);
        }
        if (this.isTamed() && player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty()) {
            InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            }
            if (this.canWearArmor() && this.isArmor(itemStack) && !this.isWearingArmor()) {
                this.equipArmor(player, itemStack);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
        this.doPlayerRide(player);
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    private void openMouth() {
        if (!this.level.isClientSide) {
            this.mouthCounter = 1;
            this.setFlag(64, true);
        }
    }

    public void setEating(boolean bl) {
        this.setFlag(16, bl);
    }

    public void setStanding(boolean bl) {
        if (bl) {
            this.setEating(false);
        }
        this.setFlag(32, bl);
    }

    @Nullable
    public SoundEvent getAmbientStandSound() {
        return this.getAmbientSound();
    }

    public void standIfPossible() {
        if (this.canPerformRearing() && this.isEffectiveAi()) {
            this.standCounter = 1;
            this.setStanding(true);
        }
    }

    public void makeMad() {
        if (!this.isStanding()) {
            this.standIfPossible();
            SoundEvent soundEvent = this.getAngrySound();
            if (soundEvent != null) {
                this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
            }
        }
    }

    public boolean tameWithName(Player player) {
        this.setOwnerUUID(player.getUUID());
        this.setTamed(true);
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
        }
        this.level.broadcastEntityEvent(this, (byte)7);
        return true;
    }

    @Override
    protected void tickRidden(LivingEntity livingEntity, Vec3 vec3) {
        super.tickRidden(livingEntity, vec3);
        Vec2 vec2 = this.getRiddenRotation(livingEntity);
        this.setRot(vec2.y, vec2.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        if (this.isControlledByLocalInstance()) {
            if (vec3.z <= 0.0) {
                this.gallopSoundCounter = 0;
            }
            if (this.onGround) {
                this.setIsJumping(false);
                if (this.playerJumpPendingScale > 0.0f && !this.isJumping()) {
                    this.executeRidersJump(this.playerJumpPendingScale, vec3);
                }
                this.playerJumpPendingScale = 0.0f;
            }
        }
    }

    protected Vec2 getRiddenRotation(LivingEntity livingEntity) {
        return new Vec2(livingEntity.getXRot() * 0.5f, livingEntity.getYRot());
    }

    @Override
    protected Vec3 getRiddenInput(LivingEntity livingEntity, Vec3 vec3) {
        if (this.onGround && this.playerJumpPendingScale == 0.0f && this.isStanding() && !this.allowStandSliding) {
            return Vec3.ZERO;
        }
        float f = livingEntity.xxa * 0.5f;
        float g = livingEntity.zza;
        if (g <= 0.0f) {
            g *= 0.25f;
        }
        return new Vec3(f, 0.0, g);
    }

    @Override
    protected float getRiddenSpeed(LivingEntity livingEntity) {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    protected void executeRidersJump(float f, Vec3 vec3) {
        double d = this.getCustomJump() * (double)f * (double)this.getBlockJumpFactor();
        double e = d + this.getJumpBoostPower();
        Vec3 vec32 = this.getDeltaMovement();
        this.setDeltaMovement(vec32.x, e, vec32.z);
        this.setIsJumping(true);
        this.hasImpulse = true;
        if (vec3.z > 0.0) {
            float g = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float h = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            this.setDeltaMovement(this.getDeltaMovement().add(-0.4f * g * f, 0.0, 0.4f * h * f));
        }
    }

    protected void playJumpSound() {
        this.playSound(SoundEvents.HORSE_JUMP, 0.4f, 1.0f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("EatingHaystack", this.isEating());
        compoundTag.putBoolean("Bred", this.isBred());
        compoundTag.putInt("Temper", this.getTemper());
        compoundTag.putBoolean("Tame", this.isTamed());
        if (this.getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", this.getOwnerUUID());
        }
        if (!this.inventory.getItem(0).isEmpty()) {
            compoundTag.put("SaddleItem", this.inventory.getItem(0).save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        ItemStack itemStack;
        UUID uUID;
        super.readAdditionalSaveData(compoundTag);
        this.setEating(compoundTag.getBoolean("EatingHaystack"));
        this.setBred(compoundTag.getBoolean("Bred"));
        this.setTemper(compoundTag.getInt("Temper"));
        this.setTamed(compoundTag.getBoolean("Tame"));
        if (compoundTag.hasUUID("Owner")) {
            uUID = compoundTag.getUUID("Owner");
        } else {
            String string = compoundTag.getString("Owner");
            uUID = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string);
        }
        if (uUID != null) {
            this.setOwnerUUID(uUID);
        }
        if (compoundTag.contains("SaddleItem", 10) && (itemStack = ItemStack.of(compoundTag.getCompound("SaddleItem"))).is(Items.SADDLE)) {
            this.inventory.setItem(0, itemStack);
        }
        this.updateContainerEquipment();
    }

    @Override
    public boolean canMate(Animal animal) {
        return false;
    }

    protected boolean canParent() {
        return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    protected void setOffspringAttributes(AgeableMob ageableMob, AbstractHorse abstractHorse) {
        this.setOffspringAttribute(ageableMob, abstractHorse, Attributes.MAX_HEALTH, MIN_HEALTH, MAX_HEALTH);
        this.setOffspringAttribute(ageableMob, abstractHorse, Attributes.JUMP_STRENGTH, MIN_JUMP_STRENGTH, MAX_JUMP_STRENGTH);
        this.setOffspringAttribute(ageableMob, abstractHorse, Attributes.MOVEMENT_SPEED, MIN_MOVEMENT_SPEED, MAX_MOVEMENT_SPEED);
    }

    private void setOffspringAttribute(AgeableMob ageableMob, AbstractHorse abstractHorse, Attribute attribute, double d, double e) {
        double f = AbstractHorse.createOffspringAttribute(this.getAttributeBaseValue(attribute), ageableMob.getAttributeBaseValue(attribute), d, e, this.random);
        abstractHorse.getAttribute(attribute).setBaseValue(f);
    }

    static double createOffspringAttribute(double d, double e, double f, double g, RandomSource randomSource) {
        double k;
        if (g <= f) {
            throw new IllegalArgumentException("Incorrect range for an attribute");
        }
        d = Mth.clamp(d, f, g);
        e = Mth.clamp(e, f, g);
        double h = 0.15 * (g - f);
        double j = (d + e) / 2.0;
        double i = Math.abs(d - e) + h * 2.0;
        double l = j + i * (k = (randomSource.nextDouble() + randomSource.nextDouble() + randomSource.nextDouble()) / 3.0 - 0.5);
        if (l > g) {
            double m = l - g;
            return g - m;
        }
        if (l < f) {
            double m = f - l;
            return f + m;
        }
        return l;
    }

    public float getEatAnim(float f) {
        return Mth.lerp(f, this.eatAnimO, this.eatAnim);
    }

    public float getStandAnim(float f) {
        return Mth.lerp(f, this.standAnimO, this.standAnim);
    }

    public float getMouthAnim(float f) {
        return Mth.lerp(f, this.mouthAnimO, this.mouthAnim);
    }

    @Override
    public void onPlayerJump(int i) {
        if (!this.isSaddled()) {
            return;
        }
        if (i < 0) {
            i = 0;
        } else {
            this.allowStandSliding = true;
            this.standIfPossible();
        }
        this.playerJumpPendingScale = i >= 90 ? 1.0f : 0.4f + 0.4f * (float)i / 90.0f;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void handleStartJump(int i) {
        this.allowStandSliding = true;
        this.standIfPossible();
        this.playJumpSound();
    }

    @Override
    public void handleStopJump() {
    }

    protected void spawnTamingParticles(boolean bl) {
        SimpleParticleType particleOptions = bl ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.level.addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
        }
    }

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

    @Override
    public void positionRider(Entity entity) {
        super.positionRider(entity);
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            this.yBodyRot = mob.yBodyRot;
        }
        if (this.standAnimO > 0.0f) {
            float f = Mth.sin(this.yBodyRot * ((float)Math.PI / 180));
            float g = Mth.cos(this.yBodyRot * ((float)Math.PI / 180));
            float h = 0.7f * this.standAnimO;
            float i = 0.15f * this.standAnimO;
            entity.setPos(this.getX() + (double)(h * f), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset() + (double)i, this.getZ() - (double)(h * g));
            if (entity instanceof LivingEntity) {
                ((LivingEntity)entity).yBodyRot = this.yBodyRot;
            }
        }
    }

    protected static float generateMaxHealth(IntUnaryOperator intUnaryOperator) {
        return 15.0f + (float)intUnaryOperator.applyAsInt(8) + (float)intUnaryOperator.applyAsInt(9);
    }

    protected static double generateJumpStrength(DoubleSupplier doubleSupplier) {
        return (double)0.4f + doubleSupplier.getAsDouble() * 0.2 + doubleSupplier.getAsDouble() * 0.2 + doubleSupplier.getAsDouble() * 0.2;
    }

    protected static double generateSpeed(DoubleSupplier doubleSupplier) {
        return ((double)0.45f + doubleSupplier.getAsDouble() * 0.3 + doubleSupplier.getAsDouble() * 0.3 + doubleSupplier.getAsDouble() * 0.3) * 0.25;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.95f;
    }

    public boolean canWearArmor() {
        return false;
    }

    public boolean isWearingArmor() {
        return !this.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
    }

    public boolean isArmor(ItemStack itemStack) {
        return false;
    }

    private SlotAccess createEquipmentSlotAccess(final int i, final Predicate<ItemStack> predicate) {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return AbstractHorse.this.inventory.getItem(i);
            }

            @Override
            public boolean set(ItemStack itemStack) {
                if (!predicate.test(itemStack)) {
                    return false;
                }
                AbstractHorse.this.inventory.setItem(i, itemStack);
                AbstractHorse.this.updateContainerEquipment();
                return true;
            }
        };
    }

    @Override
    public SlotAccess getSlot(int i) {
        int k;
        int j = i - 400;
        if (j >= 0 && j < 2 && j < this.inventory.getContainerSize()) {
            if (j == 0) {
                return this.createEquipmentSlotAccess(j, itemStack -> itemStack.isEmpty() || itemStack.is(Items.SADDLE));
            }
            if (j == 1) {
                if (!this.canWearArmor()) {
                    return SlotAccess.NULL;
                }
                return this.createEquipmentSlotAccess(j, itemStack -> itemStack.isEmpty() || this.isArmor((ItemStack)itemStack));
            }
        }
        if ((k = i - 500 + 2) >= 2 && k < this.inventory.getContainerSize()) {
            return SlotAccess.forContainer(this.inventory, k);
        }
        return super.getSlot(i);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity;
        }
        return null;
    }

    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 vec3, LivingEntity livingEntity) {
        double d = this.getX() + vec3.x;
        double e = this.getBoundingBox().minY;
        double f = this.getZ() + vec3.z;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (Pose pose : livingEntity.getDismountPoses()) {
            mutableBlockPos.set(d, e, f);
            double g = this.getBoundingBox().maxY + 0.75;
            do {
                Vec3 vec32;
                AABB aABB;
                double h = this.level.getBlockFloorHeight(mutableBlockPos);
                if ((double)mutableBlockPos.getY() + h > g) continue block0;
                if (DismountHelper.isBlockFloorValid(h) && DismountHelper.canDismountTo(this.level, livingEntity, (aABB = livingEntity.getLocalBoundsForPose(pose)).move(vec32 = new Vec3(d, (double)mutableBlockPos.getY() + h, f)))) {
                    livingEntity.setPose(pose);
                    return vec32;
                }
                mutableBlockPos.move(Direction.UP);
            } while ((double)mutableBlockPos.getY() < g);
        }
        return null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 vec3 = AbstractHorse.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), this.getYRot() + (livingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0f : -90.0f));
        Vec3 vec32 = this.getDismountLocationInDirection(vec3, livingEntity);
        if (vec32 != null) {
            return vec32;
        }
        Vec3 vec33 = AbstractHorse.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), this.getYRot() + (livingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0f : -90.0f));
        Vec3 vec34 = this.getDismountLocationInDirection(vec33, livingEntity);
        if (vec34 != null) {
            return vec34;
        }
        return this.position();
    }

    protected void randomizeAttributes(RandomSource randomSource) {
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.2f);
        }
        this.randomizeAttributes(serverLevelAccessor.getRandom());
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public boolean hasInventoryChanged(Container container) {
        return this.inventory != container;
    }

    public int getAmbientStandInterval() {
        return this.getAmbientSoundInterval();
    }

    @Override
    public /* synthetic */ EntityGetter getLevel() {
        return super.getLevel();
    }
}

