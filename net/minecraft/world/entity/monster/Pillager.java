/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class Pillager
extends AbstractIllager
implements CrossbowAttackMob,
InventoryCarrier {
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Pillager.class, EntityDataSerializers.BOOLEAN);
    private static final int INVENTORY_SIZE = 5;
    private static final int SLOT_OFFSET = 300;
    private static final float CROSSBOW_POWER = 1.6f;
    private final SimpleContainer inventory = new SimpleContainer(5);

    public Pillager(EntityType<? extends Pillager> entityType, Level level) {
        super((EntityType<? extends AbstractIllager>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, this, 10.0f));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<Pillager>(this, 1.0, 8.0f));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.35f).add(Attributes.MAX_HEALTH, 24.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return projectileWeaponItem == Items.CROSSBOW;
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean bl) {
        this.entityData.set(IS_CHARGING_CROSSBOW, bl);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.writeInventoryToTag(compoundTag);
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isChargingCrossbow()) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
        }
        if (this.isHolding(Items.CROSSBOW)) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        }
        if (this.isAggressive()) {
            return AbstractIllager.IllagerArmPose.ATTACKING;
        }
        return AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.readInventoryFromTag(compoundTag);
        this.setCanPickUpLoot(true);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0f;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        this.populateDefaultEquipmentEnchantments(randomSource, difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    @Override
    protected void enchantSpawnedWeapon(RandomSource randomSource, float f) {
        ItemStack itemStack;
        super.enchantSpawnedWeapon(randomSource, f);
        if (randomSource.nextInt(300) == 0 && (itemStack = this.getMainHandItem()).is(Items.CROSSBOW)) {
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack);
            map.putIfAbsent(Enchantments.PIERCING, 1);
            EnchantmentHelper.setEnchantments(map, itemStack);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        }
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }
        if (entity instanceof LivingEntity && ((LivingEntity)entity).getMobType() == MobType.ILLAGER) {
            return this.getTeam() == null && entity.getTeam() == null;
        }
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.performCrossbowAttack(this, 1.6f);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f) {
        this.shootCrossbowProjectile(this, livingEntity, projectile, f, 1.6f);
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.getItem() instanceof BannerItem) {
            super.pickUpItem(itemEntity);
        } else if (this.wantsItem(itemStack)) {
            this.onItemPickup(itemEntity);
            ItemStack itemStack2 = this.inventory.addItem(itemStack);
            if (itemStack2.isEmpty()) {
                itemEntity.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    private boolean wantsItem(ItemStack itemStack) {
        return this.hasActiveRaid() && itemStack.is(Items.WHITE_BANNER);
    }

    @Override
    public SlotAccess getSlot(int i) {
        int j = i - 300;
        if (j >= 0 && j < this.inventory.getContainerSize()) {
            return SlotAccess.forContainer(this.inventory, j);
        }
        return super.getSlot(i);
    }

    @Override
    public void applyRaidBuffs(int i, boolean bl) {
        boolean bl2;
        Raid raid = this.getCurrentRaid();
        boolean bl3 = bl2 = this.random.nextFloat() <= raid.getEnchantOdds();
        if (bl2) {
            ItemStack itemStack = new ItemStack(Items.CROSSBOW);
            HashMap<Enchantment, Integer> map = Maps.newHashMap();
            if (i > raid.getNumGroups(Difficulty.NORMAL)) {
                map.put(Enchantments.QUICK_CHARGE, 2);
            } else if (i > raid.getNumGroups(Difficulty.EASY)) {
                map.put(Enchantments.QUICK_CHARGE, 1);
            }
            map.put(Enchantments.MULTISHOT, 1);
            EnchantmentHelper.setEnchantments(map, itemStack);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
}

