/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Panda
extends Animal {
    private static final EntityDataAccessor<Integer> UNHAPPY_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SNEEZE_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> MAIN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> HIDDEN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private boolean gotBamboo;
    private boolean didBite;
    public int rollCounter;
    private Vec3 rollDelta;
    private float sitAmount;
    private float sitAmountO;
    private float onBackAmount;
    private float onBackAmountO;
    private float rollAmount;
    private float rollAmountO;
    private static final Predicate<ItemEntity> PANDA_ITEMS = itemEntity -> {
        Item item = itemEntity.getItem().getItem();
        return (item == Blocks.BAMBOO.asItem() || item == Blocks.CAKE.asItem()) && itemEntity.isAlive() && !itemEntity.hasPickUpDelay();
    };

    public Panda(EntityType<? extends Panda> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.moveControl = new PandaMoveControl(this);
        if (!this.isBaby()) {
            this.setCanPickUpLoot(true);
        }
    }

    @Override
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
        if (!this.getItemBySlot(equipmentSlot).isEmpty()) {
            return false;
        }
        return equipmentSlot == EquipmentSlot.MAINHAND && super.canTakeItem(itemStack);
    }

    public int getUnhappyCounter() {
        return this.entityData.get(UNHAPPY_COUNTER);
    }

    public void setUnhappyCounter(int i) {
        this.entityData.set(UNHAPPY_COUNTER, i);
    }

    public boolean isSneezing() {
        return this.getFlag(2);
    }

    public boolean isSitting() {
        return this.getFlag(8);
    }

    public void sit(boolean bl) {
        this.setFlag(8, bl);
    }

    public boolean isOnBack() {
        return this.getFlag(16);
    }

    public void setOnBack(boolean bl) {
        this.setFlag(16, bl);
    }

    public boolean isEating() {
        return this.entityData.get(EAT_COUNTER) > 0;
    }

    public void eat(boolean bl) {
        this.entityData.set(EAT_COUNTER, bl ? 1 : 0);
    }

    private int getEatCounter() {
        return this.entityData.get(EAT_COUNTER);
    }

    private void setEatCounter(int i) {
        this.entityData.set(EAT_COUNTER, i);
    }

    public void sneeze(boolean bl) {
        this.setFlag(2, bl);
        if (!bl) {
            this.setSneezeCounter(0);
        }
    }

    public int getSneezeCounter() {
        return this.entityData.get(SNEEZE_COUNTER);
    }

    public void setSneezeCounter(int i) {
        this.entityData.set(SNEEZE_COUNTER, i);
    }

    public Gene getMainGene() {
        return Gene.byId(this.entityData.get(MAIN_GENE_ID).byteValue());
    }

    public void setMainGene(Gene gene) {
        if (gene.getId() > 6) {
            gene = Gene.getRandom(this.random);
        }
        this.entityData.set(MAIN_GENE_ID, (byte)gene.getId());
    }

    public Gene getHiddenGene() {
        return Gene.byId(this.entityData.get(HIDDEN_GENE_ID).byteValue());
    }

    public void setHiddenGene(Gene gene) {
        if (gene.getId() > 6) {
            gene = Gene.getRandom(this.random);
        }
        this.entityData.set(HIDDEN_GENE_ID, (byte)gene.getId());
    }

    public boolean isRolling() {
        return this.getFlag(4);
    }

    public void roll(boolean bl) {
        this.setFlag(4, bl);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(UNHAPPY_COUNTER, 0);
        this.entityData.define(SNEEZE_COUNTER, 0);
        this.entityData.define(MAIN_GENE_ID, (byte)0);
        this.entityData.define(HIDDEN_GENE_ID, (byte)0);
        this.entityData.define(DATA_ID_FLAGS, (byte)0);
        this.entityData.define(EAT_COUNTER, 0);
    }

    private boolean getFlag(int i) {
        return (this.entityData.get(DATA_ID_FLAGS) & i) != 0;
    }

    private void setFlag(int i, boolean bl) {
        byte b = this.entityData.get(DATA_ID_FLAGS);
        if (bl) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b | i));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b & ~i));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("MainGene", this.getMainGene().getName());
        compoundTag.putString("HiddenGene", this.getHiddenGene().getName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setMainGene(Gene.byName(compoundTag.getString("MainGene")));
        this.setHiddenGene(Gene.byName(compoundTag.getString("HiddenGene")));
    }

    @Override
    @Nullable
    public AgableMob getBreedOffspring(AgableMob agableMob) {
        Panda panda = EntityType.PANDA.create(this.level);
        if (agableMob instanceof Panda) {
            panda.setGeneFromParents(this, (Panda)agableMob);
        }
        panda.setAttributes();
        return panda;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PandaPanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new PandaBreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new PandaAttackGoal(this, (double)1.2f, true));
        this.goalSelector.addGoal(4, new TemptGoal((PathfinderMob)this, 1.0, Ingredient.of(Blocks.BAMBOO.asItem()), false));
        this.goalSelector.addGoal(6, new PandaAvoidGoal<Player>(this, Player.class, 8.0f, 2.0, 2.0));
        this.goalSelector.addGoal(6, new PandaAvoidGoal<Monster>(this, Monster.class, 4.0f, 2.0, 2.0));
        this.goalSelector.addGoal(7, new PandaSitGoal());
        this.goalSelector.addGoal(8, new PandaLieOnBackGoal(this));
        this.goalSelector.addGoal(8, new PandaSneezeGoal(this));
        this.goalSelector.addGoal(9, new PandaLookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(12, new PandaRollGoal(this));
        this.goalSelector.addGoal(13, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(14, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new PandaHurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15f);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
    }

    public Gene getVariant() {
        return Gene.getVariantFromGenes(this.getMainGene(), this.getHiddenGene());
    }

    public boolean isLazy() {
        return this.getVariant() == Gene.LAZY;
    }

    public boolean isWorried() {
        return this.getVariant() == Gene.WORRIED;
    }

    public boolean isPlayful() {
        return this.getVariant() == Gene.PLAYFUL;
    }

    public boolean isWeak() {
        return this.getVariant() == Gene.WEAK;
    }

    @Override
    public boolean isAggressive() {
        return this.getVariant() == Gene.AGGRESSIVE;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        this.playSound(SoundEvents.PANDA_BITE, 1.0f, 1.0f);
        if (!this.isAggressive()) {
            this.didBite = true;
        }
        return super.doHurtTarget(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isWorried()) {
            if (this.level.isThundering() && !this.isInWater()) {
                this.sit(true);
                this.eat(false);
            } else if (!this.isEating()) {
                this.sit(false);
            }
        }
        if (this.getTarget() == null) {
            this.gotBamboo = false;
            this.didBite = false;
        }
        if (this.getUnhappyCounter() > 0) {
            if (this.getTarget() != null) {
                this.lookAt(this.getTarget(), 90.0f, 90.0f);
            }
            if (this.getUnhappyCounter() == 29 || this.getUnhappyCounter() == 14) {
                this.playSound(SoundEvents.PANDA_CANT_BREED, 1.0f, 1.0f);
            }
            this.setUnhappyCounter(this.getUnhappyCounter() - 1);
        }
        if (this.isSneezing()) {
            this.setSneezeCounter(this.getSneezeCounter() + 1);
            if (this.getSneezeCounter() > 20) {
                this.sneeze(false);
                this.afterSneeze();
            } else if (this.getSneezeCounter() == 1) {
                this.playSound(SoundEvents.PANDA_PRE_SNEEZE, 1.0f, 1.0f);
            }
        }
        if (this.isRolling()) {
            this.handleRoll();
        } else {
            this.rollCounter = 0;
        }
        if (this.isSitting()) {
            this.xRot = 0.0f;
        }
        this.updateSitAmount();
        this.handleEating();
        this.updateOnBackAnimation();
        this.updateRollAmount();
    }

    public boolean isScared() {
        return this.isWorried() && this.level.isThundering();
    }

    private void handleEating() {
        if (!this.isEating() && this.isSitting() && !this.isScared() && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
            this.eat(true);
        } else if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !this.isSitting()) {
            this.eat(false);
        }
        if (this.isEating()) {
            this.addEatingParticles();
            if (!this.level.isClientSide && this.getEatCounter() > 80 && this.random.nextInt(20) == 1) {
                if (this.getEatCounter() > 100 && this.isFoodOrCake(this.getItemBySlot(EquipmentSlot.MAINHAND))) {
                    if (!this.level.isClientSide) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }
                    this.sit(false);
                }
                this.eat(false);
                return;
            }
            this.setEatCounter(this.getEatCounter() + 1);
        }
    }

    private void addEatingParticles() {
        if (this.getEatCounter() % 5 == 0) {
            this.playSound(SoundEvents.PANDA_EAT, 0.5f + 0.5f * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            for (int i = 0; i < 6; ++i) {
                Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, ((double)this.random.nextFloat() - 0.5) * 0.1);
                vec3 = vec3.xRot(-this.xRot * ((float)Math.PI / 180));
                vec3 = vec3.yRot(-this.yRot * ((float)Math.PI / 180));
                double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
                Vec3 vec32 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.8, d, 1.0 + ((double)this.random.nextFloat() - 0.5) * 0.4);
                vec32 = vec32.yRot(-this.yBodyRot * ((float)Math.PI / 180));
                vec32 = vec32.add(this.getX(), this.getEyeY() + 1.0, this.getZ());
                this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlot.MAINHAND)), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
            }
        }
    }

    private void updateSitAmount() {
        this.sitAmountO = this.sitAmount;
        this.sitAmount = this.isSitting() ? Math.min(1.0f, this.sitAmount + 0.15f) : Math.max(0.0f, this.sitAmount - 0.19f);
    }

    private void updateOnBackAnimation() {
        this.onBackAmountO = this.onBackAmount;
        this.onBackAmount = this.isOnBack() ? Math.min(1.0f, this.onBackAmount + 0.15f) : Math.max(0.0f, this.onBackAmount - 0.19f);
    }

    private void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        this.rollAmount = this.isRolling() ? Math.min(1.0f, this.rollAmount + 0.15f) : Math.max(0.0f, this.rollAmount - 0.19f);
    }

    @Environment(value=EnvType.CLIENT)
    public float getSitAmount(float f) {
        return Mth.lerp(f, this.sitAmountO, this.sitAmount);
    }

    @Environment(value=EnvType.CLIENT)
    public float getLieOnBackAmount(float f) {
        return Mth.lerp(f, this.onBackAmountO, this.onBackAmount);
    }

    @Environment(value=EnvType.CLIENT)
    public float getRollAmount(float f) {
        return Mth.lerp(f, this.rollAmountO, this.rollAmount);
    }

    private void handleRoll() {
        ++this.rollCounter;
        if (this.rollCounter > 32) {
            this.roll(false);
            return;
        }
        if (!this.level.isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            if (this.rollCounter == 1) {
                float f = this.yRot * ((float)Math.PI / 180);
                float g = this.isBaby() ? 0.1f : 0.2f;
                this.rollDelta = new Vec3(vec3.x + (double)(-Mth.sin(f) * g), 0.0, vec3.z + (double)(Mth.cos(f) * g));
                this.setDeltaMovement(this.rollDelta.add(0.0, 0.27, 0.0));
            } else if ((float)this.rollCounter == 7.0f || (float)this.rollCounter == 15.0f || (float)this.rollCounter == 23.0f) {
                this.setDeltaMovement(0.0, this.onGround ? 0.27 : vec3.y, 0.0);
            } else {
                this.setDeltaMovement(this.rollDelta.x, vec3.y, this.rollDelta.z);
            }
        }
    }

    private void afterSneeze() {
        Vec3 vec3 = this.getDeltaMovement();
        this.level.addParticle(ParticleTypes.SNEEZE, this.getX() - (double)(this.getBbWidth() + 1.0f) * 0.5 * (double)Mth.sin(this.yBodyRot * ((float)Math.PI / 180)), this.getEyeY() - (double)0.1f, this.getZ() + (double)(this.getBbWidth() + 1.0f) * 0.5 * (double)Mth.cos(this.yBodyRot * ((float)Math.PI / 180)), vec3.x, 0.0, vec3.z);
        this.playSound(SoundEvents.PANDA_SNEEZE, 1.0f, 1.0f);
        List<Panda> list = this.level.getEntitiesOfClass(Panda.class, this.getBoundingBox().inflate(10.0));
        for (Panda panda : list) {
            if (panda.isBaby() || !panda.onGround || panda.isInWater() || !panda.canPerformAction()) continue;
            panda.jumpFromGround();
        }
        if (!this.level.isClientSide() && this.random.nextInt(700) == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(Items.SLIME_BALL);
        }
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && PANDA_ITEMS.test(itemEntity)) {
            ItemStack itemStack = itemEntity.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0f;
            this.take(itemEntity, itemStack.getCount());
            itemEntity.remove();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        this.sit(false);
        return super.hurt(damageSource, f);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(LevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        this.setMainGene(Gene.getRandom(this.random));
        this.setHiddenGene(Gene.getRandom(this.random));
        this.setAttributes();
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData();
            ((AgableMob.AgableMobGroupData)spawnGroupData).setBabySpawnChance(0.2f);
        }
        return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public void setGeneFromParents(Panda panda, @Nullable Panda panda2) {
        if (panda2 == null) {
            if (this.random.nextBoolean()) {
                this.setMainGene(panda.getOneOfGenesRandomly());
                this.setHiddenGene(Gene.getRandom(this.random));
            } else {
                this.setMainGene(Gene.getRandom(this.random));
                this.setHiddenGene(panda.getOneOfGenesRandomly());
            }
        } else if (this.random.nextBoolean()) {
            this.setMainGene(panda.getOneOfGenesRandomly());
            this.setHiddenGene(panda2.getOneOfGenesRandomly());
        } else {
            this.setMainGene(panda2.getOneOfGenesRandomly());
            this.setHiddenGene(panda.getOneOfGenesRandomly());
        }
        if (this.random.nextInt(32) == 0) {
            this.setMainGene(Gene.getRandom(this.random));
        }
        if (this.random.nextInt(32) == 0) {
            this.setHiddenGene(Gene.getRandom(this.random));
        }
    }

    private Gene getOneOfGenesRandomly() {
        if (this.random.nextBoolean()) {
            return this.getMainGene();
        }
        return this.getHiddenGene();
    }

    public void setAttributes() {
        if (this.isWeak()) {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        }
        if (this.isLazy()) {
            this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.07f);
        }
    }

    private void tryToSit() {
        if (!this.isInWater()) {
            this.setZza(0.0f);
            this.getNavigation().stop();
            this.sit(true);
        }
    }

    @Override
    public boolean mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(player, interactionHand);
        }
        if (this.isScared()) {
            return false;
        }
        if (this.isOnBack()) {
            this.setOnBack(false);
            return true;
        }
        if (this.isFood(itemStack)) {
            if (this.getTarget() != null) {
                this.gotBamboo = true;
            }
            if (this.isBaby()) {
                this.usePlayerItem(player, itemStack);
                this.ageUp((int)((float)(-this.getAge() / 20) * 0.1f), true);
            } else if (!this.level.isClientSide && this.getAge() == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, itemStack);
                this.setInLove(player);
            } else if (!(this.level.isClientSide || this.isSitting() || this.isInWater())) {
                this.tryToSit();
                this.eat(true);
                ItemStack itemStack2 = this.getItemBySlot(EquipmentSlot.MAINHAND);
                if (!itemStack2.isEmpty() && !player.abilities.instabuild) {
                    this.spawnAtLocation(itemStack2);
                }
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(itemStack.getItem(), 1));
                this.usePlayerItem(player, itemStack);
            } else {
                return false;
            }
            player.swing(interactionHand, true);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isAggressive()) {
            return SoundEvents.PANDA_AGGRESSIVE_AMBIENT;
        }
        if (this.isWorried()) {
            return SoundEvents.PANDA_WORRIED_AMBIENT;
        }
        return SoundEvents.PANDA_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.PANDA_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.getItem() == Blocks.BAMBOO.asItem();
    }

    private boolean isFoodOrCake(ItemStack itemStack) {
        return this.isFood(itemStack) || itemStack.getItem() == Blocks.CAKE.asItem();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PANDA_HURT;
    }

    public boolean canPerformAction() {
        return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
    }

    static class PandaPanicGoal
    extends PanicGoal {
        private final Panda panda;

        public PandaPanicGoal(Panda panda, double d) {
            super(panda, d);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            if (!this.panda.isOnFire()) {
                return false;
            }
            BlockPos blockPos = this.lookForWater(this.mob.level, this.mob, 5, 4);
            if (blockPos != null) {
                this.posX = blockPos.getX();
                this.posY = blockPos.getY();
                this.posZ = blockPos.getZ();
                return true;
            }
            return this.findRandomPosition();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.panda.isSitting()) {
                this.panda.getNavigation().stop();
                return false;
            }
            return super.canContinueToUse();
        }
    }

    static class PandaHurtByTargetGoal
    extends HurtByTargetGoal {
        private final Panda panda;

        public PandaHurtByTargetGoal(Panda panda, Class<?> ... classs) {
            super(panda, classs);
            this.panda = panda;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.panda.gotBamboo || this.panda.didBite) {
                this.panda.setTarget(null);
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity livingEntity) {
            if (mob instanceof Panda && ((Panda)mob).isAggressive()) {
                mob.setTarget(livingEntity);
            }
        }
    }

    static class PandaLieOnBackGoal
    extends Goal {
        private final Panda panda;
        private int cooldown;

        public PandaLieOnBackGoal(Panda panda) {
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.cooldown < this.panda.tickCount && this.panda.isLazy() && this.panda.canPerformAction() && this.panda.random.nextInt(400) == 1;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.panda.isInWater() || !this.panda.isLazy() && this.panda.random.nextInt(600) == 1) {
                return false;
            }
            return this.panda.random.nextInt(2000) != 1;
        }

        @Override
        public void start() {
            this.panda.setOnBack(true);
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            this.panda.setOnBack(false);
            this.cooldown = this.panda.tickCount + 200;
        }
    }

    class PandaSitGoal
    extends Goal {
        private int cooldown;

        public PandaSitGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > Panda.this.tickCount || Panda.this.isBaby() || Panda.this.isInWater() || !Panda.this.canPerformAction() || Panda.this.getUnhappyCounter() > 0) {
                return false;
            }
            List<ItemEntity> list = Panda.this.level.getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(6.0, 6.0, 6.0), PANDA_ITEMS);
            return !list.isEmpty() || !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            if (Panda.this.isInWater() || !Panda.this.isLazy() && Panda.this.random.nextInt(600) == 1) {
                return false;
            }
            return Panda.this.random.nextInt(2000) != 1;
        }

        @Override
        public void tick() {
            if (!Panda.this.isSitting() && !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.tryToSit();
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = Panda.this.level.getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(8.0, 8.0, 8.0), PANDA_ITEMS);
            if (!list.isEmpty() && Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.getNavigation().moveTo(list.get(0), (double)1.2f);
            } else if (!Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.tryToSit();
            }
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            ItemStack itemStack = Panda.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                Panda.this.spawnAtLocation(itemStack);
                Panda.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                int i = Panda.this.isLazy() ? Panda.this.random.nextInt(50) + 10 : Panda.this.random.nextInt(150) + 10;
                this.cooldown = Panda.this.tickCount + i * 20;
            }
            Panda.this.sit(false);
        }
    }

    static class PandaAvoidGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Panda panda;

        public PandaAvoidGoal(Panda panda, Class<T> class_, float f, double d, double e) {
            super(panda, class_, f, d, e, EntitySelector.NO_SPECTATORS::test);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.panda.isWorried() && this.panda.canPerformAction() && super.canUse();
        }
    }

    static class PandaBreedGoal
    extends BreedGoal {
        private static final TargetingConditions BREED_TARGETING = new TargetingConditions().range(8.0).allowSameTeam().allowInvulnerable();
        private final Panda panda;
        private int unhappyCooldown;

        public PandaBreedGoal(Panda panda, double d) {
            super(panda, d);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            if (super.canUse() && this.panda.getUnhappyCounter() == 0) {
                if (!this.canFindBamboo()) {
                    if (this.unhappyCooldown <= this.panda.tickCount) {
                        this.panda.setUnhappyCounter(32);
                        this.unhappyCooldown = this.panda.tickCount + 600;
                        if (this.panda.isEffectiveAi()) {
                            Player player = this.level.getNearestPlayer(BREED_TARGETING, this.panda);
                            this.panda.setTarget(player);
                        }
                    }
                    return false;
                }
                return true;
            }
            return false;
        }

        private boolean canFindBamboo() {
            BlockPos blockPos = new BlockPos(this.panda);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 8; ++j) {
                    int k = 0;
                    while (k <= j) {
                        int l;
                        int n = l = k < j && k > -j ? j : 0;
                        while (l <= j) {
                            mutableBlockPos.set(blockPos).move(k, i, l);
                            if (this.level.getBlockState(mutableBlockPos).getBlock() == Blocks.BAMBOO) {
                                return true;
                            }
                            l = l > 0 ? -l : 1 - l;
                        }
                        k = k > 0 ? -k : 1 - k;
                    }
                }
            }
            return false;
        }
    }

    static class PandaSneezeGoal
    extends Goal {
        private final Panda panda;

        public PandaSneezeGoal(Panda panda) {
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            if (!this.panda.isBaby() || !this.panda.canPerformAction()) {
                return false;
            }
            if (this.panda.isWeak() && this.panda.random.nextInt(500) == 1) {
                return true;
            }
            return this.panda.random.nextInt(6000) == 1;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.panda.sneeze(true);
        }
    }

    static class PandaRollGoal
    extends Goal {
        private final Panda panda;

        public PandaRollGoal(Panda panda) {
            this.panda = panda;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!this.panda.isBaby() && !this.panda.isPlayful() || !this.panda.onGround) {
                return false;
            }
            if (!this.panda.canPerformAction()) {
                return false;
            }
            float f = this.panda.yRot * ((float)Math.PI / 180);
            int i = 0;
            int j = 0;
            float g = -Mth.sin(f);
            float h = Mth.cos(f);
            if ((double)Math.abs(g) > 0.5) {
                i = (int)((float)i + g / Math.abs(g));
            }
            if ((double)Math.abs(h) > 0.5) {
                j = (int)((float)j + h / Math.abs(h));
            }
            if (this.panda.level.getBlockState(new BlockPos(this.panda).offset(i, -1, j)).isAir()) {
                return true;
            }
            if (this.panda.isPlayful() && this.panda.random.nextInt(60) == 1) {
                return true;
            }
            return this.panda.random.nextInt(500) == 1;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.panda.roll(true);
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }
    }

    static class PandaLookAtPlayerGoal
    extends LookAtPlayerGoal {
        private final Panda panda;

        public PandaLookAtPlayerGoal(Panda panda, Class<? extends LivingEntity> class_, float f) {
            super(panda, class_, f);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.panda.canPerformAction() && super.canUse();
        }
    }

    static class PandaAttackGoal
    extends MeleeAttackGoal {
        private final Panda panda;

        public PandaAttackGoal(Panda panda, double d, boolean bl) {
            super(panda, d, bl);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.panda.canPerformAction() && super.canUse();
        }
    }

    static class PandaMoveControl
    extends MoveControl {
        private final Panda panda;

        public PandaMoveControl(Panda panda) {
            super(panda);
            this.panda = panda;
        }

        @Override
        public void tick() {
            if (!this.panda.canPerformAction()) {
                return;
            }
            super.tick();
        }
    }

    public static enum Gene {
        NORMAL(0, "normal", false),
        LAZY(1, "lazy", false),
        WORRIED(2, "worried", false),
        PLAYFUL(3, "playful", false),
        BROWN(4, "brown", true),
        WEAK(5, "weak", true),
        AGGRESSIVE(6, "aggressive", false);

        private static final Gene[] BY_ID;
        private final int id;
        private final String name;
        private final boolean isRecessive;

        private Gene(int j, String string2, boolean bl) {
            this.id = j;
            this.name = string2;
            this.isRecessive = bl;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public boolean isRecessive() {
            return this.isRecessive;
        }

        private static Gene getVariantFromGenes(Gene gene, Gene gene2) {
            if (gene.isRecessive()) {
                if (gene == gene2) {
                    return gene;
                }
                return NORMAL;
            }
            return gene;
        }

        public static Gene byId(int i) {
            if (i < 0 || i >= BY_ID.length) {
                i = 0;
            }
            return BY_ID[i];
        }

        public static Gene byName(String string) {
            for (Gene gene : Gene.values()) {
                if (!gene.name.equals(string)) continue;
                return gene;
            }
            return NORMAL;
        }

        public static Gene getRandom(Random random) {
            int i = random.nextInt(16);
            if (i == 0) {
                return LAZY;
            }
            if (i == 1) {
                return WORRIED;
            }
            if (i == 2) {
                return PLAYFUL;
            }
            if (i == 4) {
                return AGGRESSIVE;
            }
            if (i < 9) {
                return WEAK;
            }
            if (i < 11) {
                return BROWN;
            }
            return NORMAL;
        }

        static {
            BY_ID = (Gene[])Arrays.stream(Gene.values()).sorted(Comparator.comparingInt(Gene::getId)).toArray(Gene[]::new);
        }
    }
}

