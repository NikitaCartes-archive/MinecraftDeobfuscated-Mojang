/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.horse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Llama
extends AbstractChestedHorse
implements RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final int VARIANTS = 4;
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Blocks.HAY_BLOCK.asItem());
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SWAG_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private boolean didSpit;
    @Nullable
    private Llama caravanHead;
    @Nullable
    private Llama caravanTail;

    public Llama(EntityType<? extends Llama> entityType, Level level) {
        super((EntityType<? extends AbstractChestedHorse>)entityType, level);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int i) {
        this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, i)));
    }

    private void setRandomStrength() {
        int i = this.random.nextFloat() < 0.04f ? 5 : 3;
        this.setStrength(1 + this.random.nextInt(i));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", this.getVariant());
        compoundTag.putInt("Strength", this.getStrength());
        if (!this.inventory.getItem(1).isEmpty()) {
            compoundTag.put("DecorItem", this.inventory.getItem(1).save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setStrength(compoundTag.getInt("Strength"));
        super.readAdditionalSaveData(compoundTag);
        this.setVariant(compoundTag.getInt("Variant"));
        if (compoundTag.contains("DecorItem", 10)) {
            this.inventory.setItem(1, ItemStack.of(compoundTag.getCompound("DecorItem")));
        }
        this.updateContainerEquipment();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1f));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0f));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Llama.createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STRENGTH_ID, 0);
        this.entityData.define(DATA_SWAG_ID, -1);
        this.entityData.define(DATA_VARIANT_ID, 0);
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT_ID), 0, 3);
    }

    public void setVariant(int i) {
        this.entityData.set(DATA_VARIANT_ID, i);
    }

    @Override
    protected int getInventorySize() {
        if (this.hasChest()) {
            return 2 + 3 * this.getInventoryColumns();
        }
        return super.getInventorySize();
    }

    @Override
    public void positionRider(Entity entity) {
        if (!this.hasPassenger(entity)) {
            return;
        }
        float f = Mth.cos(this.yBodyRot * ((float)Math.PI / 180));
        float g = Mth.sin(this.yBodyRot * ((float)Math.PI / 180));
        float h = 0.3f;
        entity.setPos(this.getX() + (double)(0.3f * g), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset(), this.getZ() - (double)(0.3f * f));
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getBbHeight() * 0.67;
    }

    @Override
    public boolean canBeControlledByRider() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override
    protected boolean handleEating(Player player, ItemStack itemStack) {
        int i = 0;
        int j = 0;
        float f = 0.0f;
        boolean bl = false;
        if (itemStack.is(Items.WHEAT)) {
            i = 10;
            j = 3;
            f = 2.0f;
        } else if (itemStack.is(Blocks.HAY_BLOCK.asItem())) {
            i = 90;
            j = 6;
            f = 10.0f;
            if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
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
            SoundEvent soundEvent;
            this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
            if (!this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(), this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
        }
        return bl;
    }

    @Override
    protected boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        int i;
        this.setRandomStrength();
        if (spawnGroupData instanceof LlamaGroupData) {
            i = ((LlamaGroupData)spawnGroupData).variant;
        } else {
            i = this.random.nextInt(4);
            spawnGroupData = new LlamaGroupData(i);
        }
        this.setVariant(i);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.LLAMA_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public void makeMad() {
        SoundEvent soundEvent = this.getAngrySound();
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public int getInventoryColumns() {
        return this.getStrength();
    }

    @Override
    public boolean canWearArmor() {
        return true;
    }

    @Override
    public boolean isWearingArmor() {
        return !this.inventory.getItem(1).isEmpty();
    }

    @Override
    public boolean isArmor(ItemStack itemStack) {
        return itemStack.is(ItemTags.CARPETS);
    }

    @Override
    public boolean isSaddleable() {
        return false;
    }

    @Override
    public void containerChanged(Container container) {
        DyeColor dyeColor = this.getSwag();
        super.containerChanged(container);
        DyeColor dyeColor2 = this.getSwag();
        if (this.tickCount > 20 && dyeColor2 != null && dyeColor2 != dyeColor) {
            this.playSound(SoundEvents.LLAMA_SWAG, 0.5f, 1.0f);
        }
    }

    @Override
    protected void updateContainerEquipment() {
        if (this.level.isClientSide) {
            return;
        }
        super.updateContainerEquipment();
        this.setSwag(Llama.getDyeColor(this.inventory.getItem(1)));
    }

    private void setSwag(@Nullable DyeColor dyeColor) {
        this.entityData.set(DATA_SWAG_ID, dyeColor == null ? -1 : dyeColor.getId());
    }

    @Nullable
    private static DyeColor getDyeColor(ItemStack itemStack) {
        Block block = Block.byItem(itemStack.getItem());
        if (block instanceof WoolCarpetBlock) {
            return ((WoolCarpetBlock)block).getColor();
        }
        return null;
    }

    @Nullable
    public DyeColor getSwag() {
        int i = this.entityData.get(DATA_SWAG_ID);
        return i == -1 ? null : DyeColor.byId(i);
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canMate(Animal animal) {
        return animal != this && animal instanceof Llama && this.canParent() && ((Llama)animal).canParent();
    }

    @Override
    public Llama getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Llama llama = this.makeBabyLlama();
        this.setOffspringAttributes(ageableMob, llama);
        Llama llama2 = (Llama)ageableMob;
        int i = this.random.nextInt(Math.max(this.getStrength(), llama2.getStrength())) + 1;
        if (this.random.nextFloat() < 0.03f) {
            ++i;
        }
        llama.setStrength(i);
        llama.setVariant(this.random.nextBoolean() ? this.getVariant() : llama2.getVariant());
        return llama;
    }

    protected Llama makeBabyLlama() {
        return EntityType.LLAMA.create(this.level);
    }

    private void spit(LivingEntity livingEntity) {
        LlamaSpit llamaSpit = new LlamaSpit(this.level, this);
        double d = livingEntity.getX() - this.getX();
        double e = livingEntity.getY(0.3333333333333333) - llamaSpit.getY();
        double f = livingEntity.getZ() - this.getZ();
        float g = Mth.sqrt(d * d + f * f) * 0.2f;
        llamaSpit.shoot(d, e + (double)g, f, 1.5f, 10.0f);
        if (!this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        this.level.addFreshEntity(llamaSpit);
        this.didSpit = true;
    }

    private void setDidSpit(boolean bl) {
        this.didSpit = bl;
    }

    @Override
    public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
        int i = this.calculateFallDamage(f, g);
        if (i <= 0) {
            return false;
        }
        if (f >= 6.0f) {
            this.hurt(damageSource, i);
            if (this.isVehicle()) {
                for (Entity entity : this.getIndirectPassengers()) {
                    entity.hurt(damageSource, i);
                }
            }
        }
        this.playBlockFallSound();
        return true;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(Llama llama) {
        this.caravanHead = llama;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    protected double followLeashSpeed() {
        return 2.0;
    }

    @Override
    protected void followMommy() {
        if (!this.inCaravan() && this.isBaby()) {
            super.followMommy();
        }
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.spit(livingEntity);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
    }

    @Override
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class LlamaAttackWolfGoal
    extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama llama) {
            super(llama, Wolf.class, 16, false, true, livingEntity -> !((Wolf)livingEntity).isTame());
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25;
        }
    }

    static class LlamaHurtByTargetGoal
    extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama llama) {
            super(llama, new Class[0]);
        }

        @Override
        public boolean canContinueToUse() {
            Llama llama;
            if (this.mob instanceof Llama && (llama = (Llama)this.mob).didSpit) {
                llama.setDidSpit(false);
                return false;
            }
            return super.canContinueToUse();
        }
    }

    static class LlamaGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final int variant;

        private LlamaGroupData(int i) {
            super(true);
            this.variant = i;
        }
    }
}

