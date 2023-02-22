/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public abstract class Mob
extends LivingEntity
implements Targeting {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    private static final int MOB_FLAG_NO_AI = 1;
    private static final int MOB_FLAG_LEFTHANDED = 2;
    private static final int MOB_FLAG_AGGRESSIVE = 4;
    protected static final int PICKUP_REACH = 1;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
    public static final float MAX_WEARING_ARMOR_CHANCE = 0.15f;
    public static final float MAX_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25f;
    public static final String LEASH_TAG = "Leash";
    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085f;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    @Nullable
    private LivingEntity target;
    private final Sensing sensing;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    protected final float[] handDropChances = new float[2];
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    protected final float[] armorDropChances = new float[4];
    private boolean canPickUpLoot;
    private boolean persistenceRequired;
    private final Map<BlockPathTypes, Float> pathfindingMalus = Maps.newEnumMap(BlockPathTypes.class);
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;
    @Nullable
    private Entity leashHolder;
    private int delayedLeashHolderId;
    @Nullable
    private CompoundTag leashInfoTag;
    private BlockPos restrictCenter = BlockPos.ZERO;
    private float restrictRadius = -1.0f;

    protected Mob(EntityType<? extends Mob> entityType, Level level) {
        super((EntityType<? extends LivingEntity>)entityType, level);
        this.goalSelector = new GoalSelector(level.getProfilerSupplier());
        this.targetSelector = new GoalSelector(level.getProfilerSupplier());
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = this.createBodyControl();
        this.navigation = this.createNavigation(level);
        this.sensing = new Sensing(this);
        Arrays.fill(this.armorDropChances, 0.085f);
        Arrays.fill(this.handDropChances, 0.085f);
        if (level != null && !level.isClientSide) {
            this.registerGoals();
        }
    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.ATTACK_KNOCKBACK);
    }

    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(BlockPathTypes blockPathTypes) {
        Mob mob = this.getVehicle() instanceof Mob && ((Mob)this.getVehicle()).shouldPassengersInheritMalus() ? (Mob)this.getVehicle() : this;
        Float float_ = mob.pathfindingMalus.get((Object)blockPathTypes);
        return float_ == null ? blockPathTypes.getMalus() : float_.floatValue();
    }

    public void setPathfindingMalus(BlockPathTypes blockPathTypes, float f) {
        this.pathfindingMalus.put(blockPathTypes, Float.valueOf(f));
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getVehicle();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return mob.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        if (this.isPassenger() && this.getVehicle() instanceof Mob) {
            Mob mob = (Mob)this.getVehicle();
            return mob.getNavigation();
        }
        return this.navigation;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.target;
    }

    public void setTarget(@Nullable LivingEntity livingEntity) {
        this.target = livingEntity;
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return entityType != EntityType.GHAST;
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return false;
    }

    public void ate() {
        this.gameEvent(GameEvent.EAT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MOB_FLAGS_ID, (byte)0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        SoundEvent soundEvent = this.getAmbientSound();
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.level.getProfiler().push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.resetAmbientSoundTime();
            this.playAmbientSound();
        }
        this.level.getProfiler().pop();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.resetAmbientSoundTime();
        super.playHurtSound(damageSource);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    public int getExperienceReward() {
        if (this.xpReward > 0) {
            int j;
            int i = this.xpReward;
            for (j = 0; j < this.armorItems.size(); ++j) {
                if (this.armorItems.get(j).isEmpty() || !(this.armorDropChances[j] <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            for (j = 0; j < this.handItems.size(); ++j) {
                if (this.handItems.get(j).isEmpty() || !(this.handDropChances[j] <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            return i;
        }
        return this.xpReward;
    }

    public void spawnAnim() {
        if (this.level.isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double e = this.random.nextGaussian() * 0.02;
                double f = this.random.nextGaussian() * 0.02;
                double g = 10.0;
                this.level.addParticle(ParticleTypes.POOF, this.getX(1.0) - d * 10.0, this.getRandomY() - e * 10.0, this.getRandomZ(1.0) - f * 10.0, d, e, f);
            }
        } else {
            this.level.broadcastEntityEvent(this, (byte)20);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 20) {
            this.spawnAnim();
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.tickLeash();
            if (this.tickCount % 5 == 0) {
                this.updateControlFlags();
            }
        }
    }

    protected void updateControlFlags() {
        boolean bl = !(this.getControllingPassenger() instanceof Mob);
        boolean bl2 = !(this.getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, bl);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, bl && bl2);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, bl);
    }

    @Override
    protected float tickHeadTurn(float f, float g) {
        this.bodyRotationControl.clientTick();
        return g;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("CanPickUpLoot", this.canPickUpLoot());
        compoundTag.putBoolean("PersistenceRequired", this.persistenceRequired);
        ListTag listTag = new ListTag();
        for (ItemStack itemStack : this.armorItems) {
            CompoundTag compoundTag2 = new CompoundTag();
            if (!itemStack.isEmpty()) {
                itemStack.save(compoundTag2);
            }
            listTag.add(compoundTag2);
        }
        compoundTag.put("ArmorItems", listTag);
        ListTag listTag2 = new ListTag();
        for (ItemStack itemStack : this.handItems) {
            CompoundTag compoundTag3 = new CompoundTag();
            if (!itemStack.isEmpty()) {
                itemStack.save(compoundTag3);
            }
            listTag2.add(compoundTag3);
        }
        compoundTag.put("HandItems", listTag2);
        ListTag listTag3 = new ListTag();
        for (float f : this.armorDropChances) {
            listTag3.add(FloatTag.valueOf(f));
        }
        compoundTag.put("ArmorDropChances", listTag3);
        ListTag listTag4 = new ListTag();
        for (float g : this.handDropChances) {
            listTag4.add(FloatTag.valueOf(g));
        }
        compoundTag.put("HandDropChances", listTag4);
        if (this.leashHolder != null) {
            Object compoundTag3 = new CompoundTag();
            if (this.leashHolder instanceof LivingEntity) {
                UUID uUID = this.leashHolder.getUUID();
                ((CompoundTag)compoundTag3).putUUID("UUID", uUID);
            } else if (this.leashHolder instanceof HangingEntity) {
                BlockPos blockPos = ((HangingEntity)this.leashHolder).getPos();
                ((CompoundTag)compoundTag3).putInt("X", blockPos.getX());
                ((CompoundTag)compoundTag3).putInt("Y", blockPos.getY());
                ((CompoundTag)compoundTag3).putInt("Z", blockPos.getZ());
            }
            compoundTag.put(LEASH_TAG, (Tag)compoundTag3);
        } else if (this.leashInfoTag != null) {
            compoundTag.put(LEASH_TAG, this.leashInfoTag.copy());
        }
        compoundTag.putBoolean("LeftHanded", this.isLeftHanded());
        if (this.lootTable != null) {
            compoundTag.putString("DeathLootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                compoundTag.putLong("DeathLootTableSeed", this.lootTableSeed);
            }
        }
        if (this.isNoAi()) {
            compoundTag.putBoolean("NoAI", this.isNoAi());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        int i;
        ListTag listTag;
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("CanPickUpLoot", 1)) {
            this.setCanPickUpLoot(compoundTag.getBoolean("CanPickUpLoot"));
        }
        this.persistenceRequired = compoundTag.getBoolean("PersistenceRequired");
        if (compoundTag.contains("ArmorItems", 9)) {
            listTag = compoundTag.getList("ArmorItems", 10);
            for (i = 0; i < this.armorItems.size(); ++i) {
                this.armorItems.set(i, ItemStack.of(listTag.getCompound(i)));
            }
        }
        if (compoundTag.contains("HandItems", 9)) {
            listTag = compoundTag.getList("HandItems", 10);
            for (i = 0; i < this.handItems.size(); ++i) {
                this.handItems.set(i, ItemStack.of(listTag.getCompound(i)));
            }
        }
        if (compoundTag.contains("ArmorDropChances", 9)) {
            listTag = compoundTag.getList("ArmorDropChances", 5);
            for (i = 0; i < listTag.size(); ++i) {
                this.armorDropChances[i] = listTag.getFloat(i);
            }
        }
        if (compoundTag.contains("HandDropChances", 9)) {
            listTag = compoundTag.getList("HandDropChances", 5);
            for (i = 0; i < listTag.size(); ++i) {
                this.handDropChances[i] = listTag.getFloat(i);
            }
        }
        if (compoundTag.contains(LEASH_TAG, 10)) {
            this.leashInfoTag = compoundTag.getCompound(LEASH_TAG);
        }
        this.setLeftHanded(compoundTag.getBoolean("LeftHanded"));
        if (compoundTag.contains("DeathLootTable", 8)) {
            this.lootTable = new ResourceLocation(compoundTag.getString("DeathLootTable"));
            this.lootTableSeed = compoundTag.getLong("DeathLootTableSeed");
        }
        this.setNoAi(compoundTag.getBoolean("NoAI"));
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean bl) {
        super.dropFromLootTable(damageSource, bl);
        this.lootTable = null;
    }

    @Override
    protected LootContext.Builder createLootContext(boolean bl, DamageSource damageSource) {
        return super.createLootContext(bl, damageSource).withOptionalRandomSeed(this.lootTableSeed, this.random);
    }

    @Override
    public final ResourceLocation getLootTable() {
        return this.lootTable == null ? this.getDefaultLootTable() : this.lootTable;
    }

    protected ResourceLocation getDefaultLootTable() {
        return super.getLootTable();
    }

    public void setZza(float f) {
        this.zza = f;
    }

    public void setYya(float f) {
        this.yya = f;
    }

    public void setXxa(float f) {
        this.xxa = f;
    }

    @Override
    public void setSpeed(float f) {
        super.setSpeed(f);
        this.setZza(f);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.level.getProfiler().push("looting");
        if (!this.level.isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            Vec3i vec3i = this.getPickupReach();
            List<ItemEntity> list = this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
            for (ItemEntity itemEntity : list) {
                if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty() || itemEntity.hasPickUpDelay() || !this.wantsToPickUp(itemEntity.getItem())) continue;
                this.pickUpItem(itemEntity);
            }
        }
        this.level.getProfiler().pop();
    }

    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack itemStack2 = this.equipItemIfPossible(itemStack.copy());
        if (!itemStack2.isEmpty()) {
            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemStack2.getCount());
            itemStack.shrink(itemStack2.getCount());
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public ItemStack equipItemIfPossible(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItemStack(itemStack);
        ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
        boolean bl = this.canReplaceCurrentItem(itemStack, itemStack2);
        if (bl && this.canHoldItem(itemStack)) {
            double d = this.getEquipmentDropChance(equipmentSlot);
            if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.spawnAtLocation(itemStack2);
            }
            if (equipmentSlot.isArmor() && itemStack.getCount() > 1) {
                ItemStack itemStack3 = itemStack.copyWithCount(1);
                this.setItemSlotAndDropWhenKilled(equipmentSlot, itemStack3);
                return itemStack3;
            }
            this.setItemSlotAndDropWhenKilled(equipmentSlot, itemStack);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    private EquipmentSlot getEquipmentSlotForItemStack(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
        boolean bl = this.getItemBySlot(equipmentSlot).isEmpty();
        return equipmentSlot.isArmor() && !bl ? EquipmentSlot.MAINHAND : equipmentSlot;
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.setItemSlot(equipmentSlot, itemStack);
        this.setGuaranteedDrop(equipmentSlot);
        this.persistenceRequired = true;
    }

    public void setGuaranteedDrop(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case HAND: {
                this.handDropChances[equipmentSlot.getIndex()] = 2.0f;
                break;
            }
            case ARMOR: {
                this.armorDropChances[equipmentSlot.getIndex()] = 2.0f;
            }
        }
    }

    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.getItem() instanceof SwordItem) {
            if (!(itemStack2.getItem() instanceof SwordItem)) {
                return true;
            }
            SwordItem swordItem = (SwordItem)itemStack.getItem();
            SwordItem swordItem2 = (SwordItem)itemStack2.getItem();
            if (swordItem.getDamage() != swordItem2.getDamage()) {
                return swordItem.getDamage() > swordItem2.getDamage();
            }
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof BowItem && itemStack2.getItem() instanceof BowItem) {
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof CrossbowItem && itemStack2.getItem() instanceof CrossbowItem) {
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof ArmorItem) {
            if (EnchantmentHelper.hasBindingCurse(itemStack2)) {
                return false;
            }
            if (!(itemStack2.getItem() instanceof ArmorItem)) {
                return true;
            }
            ArmorItem armorItem = (ArmorItem)itemStack.getItem();
            ArmorItem armorItem2 = (ArmorItem)itemStack2.getItem();
            if (armorItem.getDefense() != armorItem2.getDefense()) {
                return armorItem.getDefense() > armorItem2.getDefense();
            }
            if (armorItem.getToughness() != armorItem2.getToughness()) {
                return armorItem.getToughness() > armorItem2.getToughness();
            }
            return this.canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof DiggerItem) {
            if (itemStack2.getItem() instanceof BlockItem) {
                return true;
            }
            if (itemStack2.getItem() instanceof DiggerItem) {
                DiggerItem diggerItem = (DiggerItem)itemStack.getItem();
                DiggerItem diggerItem2 = (DiggerItem)itemStack2.getItem();
                if (diggerItem.getAttackDamage() != diggerItem2.getAttackDamage()) {
                    return diggerItem.getAttackDamage() > diggerItem2.getAttackDamage();
                }
                return this.canReplaceEqualItem(itemStack, itemStack2);
            }
        }
        return false;
    }

    public boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.getDamageValue() < itemStack2.getDamageValue() || itemStack.hasTag() && !itemStack2.hasTag()) {
            return true;
        }
        if (itemStack.hasTag() && itemStack2.hasTag()) {
            return itemStack.getTag().getAllKeys().stream().anyMatch(string -> !string.equals("Damage")) && !itemStack2.getTag().getAllKeys().stream().anyMatch(string -> !string.equals("Damage"));
        }
        return false;
    }

    public boolean canHoldItem(ItemStack itemStack) {
        return true;
    }

    public boolean wantsToPickUp(ItemStack itemStack) {
        return this.canHoldItem(itemStack);
    }

    public boolean removeWhenFarAway(double d) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return this.isPassenger();
    }

    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
            this.noActionTime = 0;
            return;
        }
        Player entity = this.level.getNearestPlayer(this, -1.0);
        if (entity != null) {
            int i;
            int j;
            double d = entity.distanceToSqr(this);
            if (d > (double)(j = (i = this.getType().getCategory().getDespawnDistance()) * i) && this.removeWhenFarAway(d)) {
                this.discard();
            }
            int k = this.getType().getCategory().getNoDespawnDistance();
            int l = k * k;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.removeWhenFarAway(d)) {
                this.discard();
            } else if (d < (double)l) {
                this.noActionTime = 0;
            }
        }
    }

    @Override
    protected final void serverAiStep() {
        ++this.noActionTime;
        this.level.getProfiler().push("sensing");
        this.sensing.tick();
        this.level.getProfiler().pop();
        int i = this.level.getServer().getTickCount() + this.getId();
        if (i % 2 == 0 || this.tickCount <= 1) {
            this.level.getProfiler().push("targetSelector");
            this.targetSelector.tick();
            this.level.getProfiler().pop();
            this.level.getProfiler().push("goalSelector");
            this.goalSelector.tick();
            this.level.getProfiler().pop();
        } else {
            this.level.getProfiler().push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            this.level.getProfiler().pop();
            this.level.getProfiler().push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            this.level.getProfiler().pop();
        }
        this.level.getProfiler().push("navigation");
        this.navigation.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("mob tick");
        this.customServerAiStep();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("controls");
        this.level.getProfiler().push("move");
        this.moveControl.tick();
        this.level.getProfiler().popPush("look");
        this.lookControl.tick();
        this.level.getProfiler().popPush("jump");
        this.jumpControl.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().pop();
        this.sendDebugPackets();
    }

    protected void sendDebugPackets() {
        DebugPackets.sendGoalSelector(this.level, this, this.goalSelector);
    }

    protected void customServerAiStep() {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity entity, float f, float g) {
        double h;
        double d = entity.getX() - this.getX();
        double e = entity.getZ() - this.getZ();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            h = livingEntity.getEyeY() - this.getEyeY();
        } else {
            h = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double i = Math.sqrt(d * d + e * e);
        float j = (float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0f;
        float k = (float)(-(Mth.atan2(h, i) * 57.2957763671875));
        this.setXRot(this.rotlerp(this.getXRot(), k, g));
        this.setYRot(this.rotlerp(this.getYRot(), j, f));
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

    public static boolean checkMobSpawnRules(EntityType<? extends Mob> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2 = blockPos.below();
        return mobSpawnType == MobSpawnType.SPAWNER || levelAccessor.getBlockState(blockPos2).isValidSpawn(levelAccessor, blockPos2, entityType);
    }

    public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return !levelReader.containsAnyLiquid(this.getBoundingBox()) && levelReader.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int i) {
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return 3;
        }
        int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if ((i -= (3 - this.level.getDifficulty().getId()) * 4) < 0) {
            i = 0;
        }
        return i + 3;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case HAND: {
                return this.handItems.get(equipmentSlot.getIndex());
            }
            case ARMOR: {
                return this.armorItems.get(equipmentSlot.getIndex());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.verifyEquippedItem(itemStack);
        switch (equipmentSlot.getType()) {
            case HAND: {
                this.onEquipItem(equipmentSlot, this.handItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
                break;
            }
            case ARMOR: {
                this.onEquipItem(equipmentSlot, this.armorItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
        super.dropCustomDeathLoot(damageSource, i, bl);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            boolean bl2;
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            float f = this.getEquipmentDropChance(equipmentSlot);
            boolean bl3 = bl2 = f > 1.0f;
            if (itemStack.isEmpty() || EnchantmentHelper.hasVanishingCurse(itemStack) || !bl && !bl2 || !(Math.max(this.random.nextFloat() - (float)i * 0.01f, 0.0f) < f)) continue;
            if (!bl2 && itemStack.isDamageableItem()) {
                itemStack.setDamageValue(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
            }
            this.spawnAtLocation(itemStack);
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
    }

    protected float getEquipmentDropChance(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot.getType()) {
            case EquipmentSlot.Type.HAND -> this.handDropChances[equipmentSlot.getIndex()];
            case EquipmentSlot.Type.ARMOR -> this.armorDropChances[equipmentSlot.getIndex()];
            default -> 0.0f;
        };
    }

    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        if (randomSource.nextFloat() < 0.15f * difficultyInstance.getSpecialMultiplier()) {
            float f;
            int i = randomSource.nextInt(2);
            float f2 = f = this.level.getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            if (randomSource.nextFloat() < 0.095f) {
                ++i;
            }
            if (randomSource.nextFloat() < 0.095f) {
                ++i;
            }
            if (randomSource.nextFloat() < 0.095f) {
                ++i;
            }
            boolean bl = true;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Item item;
                if (equipmentSlot.getType() != EquipmentSlot.Type.ARMOR) continue;
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (!bl && randomSource.nextFloat() < f) break;
                bl = false;
                if (!itemStack.isEmpty() || (item = Mob.getEquipmentForSlot(equipmentSlot, i)) == null) continue;
                this.setItemSlot(equipmentSlot, new ItemStack(item));
            }
        }
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int i) {
        switch (equipmentSlot) {
            case HEAD: {
                if (i == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (i == 1) {
                    return Items.GOLDEN_HELMET;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (i == 3) {
                    return Items.IRON_HELMET;
                }
                if (i == 4) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if (i == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (i == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (i == 3) {
                    return Items.IRON_CHESTPLATE;
                }
                if (i == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if (i == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (i == 1) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (i == 3) {
                    return Items.IRON_LEGGINGS;
                }
                if (i == 4) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if (i == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (i == 1) {
                    return Items.GOLDEN_BOOTS;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (i == 3) {
                    return Items.IRON_BOOTS;
                }
                if (i != 4) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void populateDefaultEquipmentEnchantments(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        float f = difficultyInstance.getSpecialMultiplier();
        this.enchantSpawnedWeapon(randomSource, f);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.ARMOR) continue;
            this.enchantSpawnedArmor(randomSource, f, equipmentSlot);
        }
    }

    protected void enchantSpawnedWeapon(RandomSource randomSource, float f) {
        if (!this.getMainHandItem().isEmpty() && randomSource.nextFloat() < 0.25f * f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(randomSource, this.getMainHandItem(), (int)(5.0f + f * (float)randomSource.nextInt(18)), false));
        }
    }

    protected void enchantSpawnedArmor(RandomSource randomSource, float f, EquipmentSlot equipmentSlot) {
        ItemStack itemStack = this.getItemBySlot(equipmentSlot);
        if (!itemStack.isEmpty() && randomSource.nextFloat() < 0.5f * f) {
            this.setItemSlot(equipmentSlot, EnchantmentHelper.enchantItem(randomSource, itemStack, (int)(5.0f + f * (float)randomSource.nextInt(18)), false));
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random spawn bonus", randomSource.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.MULTIPLY_BASE));
        if (randomSource.nextFloat() < 0.05f) {
            this.setLeftHanded(true);
        } else {
            this.setLeftHanded(false);
        }
        return spawnGroupData;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    public void setDropChance(EquipmentSlot equipmentSlot, float f) {
        switch (equipmentSlot.getType()) {
            case HAND: {
                this.handDropChances[equipmentSlot.getIndex()] = f;
                break;
            }
            case ARMOR: {
                this.armorDropChances[equipmentSlot.getIndex()] = f;
            }
        }
    }

    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean bl) {
        this.canPickUpLoot = bl;
    }

    @Override
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
        return this.getItemBySlot(equipmentSlot).isEmpty() && this.canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override
    public final InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (!this.isAlive()) {
            return InteractionResult.PASS;
        }
        if (this.getLeashHolder() == player) {
            this.dropLeash(true, !player.getAbilities().instabuild);
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        InteractionResult interactionResult = this.checkAndHandleImportantInteractions(player, interactionHand);
        if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionResult;
        }
        interactionResult = this.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionResult;
        }
        return super.interact(player, interactionHand);
    }

    private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.LEAD) && this.canBeLeashed(player)) {
            this.setLeashedTo(player, true);
            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (itemStack.is(Items.NAME_TAG) && (interactionResult = itemStack.interactLivingEntity(player, this, interactionHand)).consumesAction()) {
            return interactionResult;
        }
        if (itemStack.getItem() instanceof SpawnEggItem) {
            if (this.level instanceof ServerLevel) {
                SpawnEggItem spawnEggItem = (SpawnEggItem)itemStack.getItem();
                Optional<Mob> optional = spawnEggItem.spawnOffspringFromSpawnEgg(player, this, this.getType(), (ServerLevel)this.level, this.position(), itemStack);
                optional.ifPresent(mob -> this.onOffspringSpawnedFromEgg(player, (Mob)mob));
                return optional.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
    }

    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    public boolean isWithinRestriction() {
        return this.isWithinRestriction(this.blockPosition());
    }

    public boolean isWithinRestriction(BlockPos blockPos) {
        if (this.restrictRadius == -1.0f) {
            return true;
        }
        return this.restrictCenter.distSqr(blockPos) < (double)(this.restrictRadius * this.restrictRadius);
    }

    public void restrictTo(BlockPos blockPos, int i) {
        this.restrictCenter = blockPos;
        this.restrictRadius = i;
    }

    public BlockPos getRestrictCenter() {
        return this.restrictCenter;
    }

    public float getRestrictRadius() {
        return this.restrictRadius;
    }

    public void clearRestriction() {
        this.restrictRadius = -1.0f;
    }

    public boolean hasRestriction() {
        return this.restrictRadius != -1.0f;
    }

    @Nullable
    public <T extends Mob> T convertTo(EntityType<T> entityType, boolean bl) {
        if (this.isRemoved()) {
            return null;
        }
        Mob mob = (Mob)entityType.create(this.level);
        if (mob == null) {
            return null;
        }
        mob.copyPosition(this);
        mob.setBaby(this.isBaby());
        mob.setNoAi(this.isNoAi());
        if (this.hasCustomName()) {
            mob.setCustomName(this.getCustomName());
            mob.setCustomNameVisible(this.isCustomNameVisible());
        }
        if (this.isPersistenceRequired()) {
            mob.setPersistenceRequired();
        }
        mob.setInvulnerable(this.isInvulnerable());
        if (bl) {
            mob.setCanPickUpLoot(this.canPickUpLoot());
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                mob.setItemSlot(equipmentSlot, itemStack.copy());
                mob.setDropChance(equipmentSlot, this.getEquipmentDropChance(equipmentSlot));
                itemStack.setCount(0);
            }
        }
        this.level.addFreshEntity(mob);
        if (this.isPassenger()) {
            Entity entity = this.getVehicle();
            this.stopRiding();
            mob.startRiding(entity, true);
        }
        this.discard();
        return (T)mob;
    }

    protected void tickLeash() {
        if (this.leashInfoTag != null) {
            this.restoreLeashFromSave();
        }
        if (this.leashHolder == null) {
            return;
        }
        if (!this.isAlive() || !this.leashHolder.isAlive()) {
            this.dropLeash(true, true);
        }
    }

    public void dropLeash(boolean bl, boolean bl2) {
        if (this.leashHolder != null) {
            this.leashHolder = null;
            this.leashInfoTag = null;
            if (!this.level.isClientSide && bl2) {
                this.spawnAtLocation(Items.LEAD);
            }
            if (!this.level.isClientSide && bl && this.level instanceof ServerLevel) {
                ((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, null));
            }
        }
    }

    public boolean canBeLeashed(Player player) {
        return !this.isLeashed() && !(this instanceof Enemy);
    }

    public boolean isLeashed() {
        return this.leashHolder != null;
    }

    @Nullable
    public Entity getLeashHolder() {
        if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.level.isClientSide) {
            this.leashHolder = this.level.getEntity(this.delayedLeashHolderId);
        }
        return this.leashHolder;
    }

    public void setLeashedTo(Entity entity, boolean bl) {
        this.leashHolder = entity;
        this.leashInfoTag = null;
        if (!this.level.isClientSide && bl && this.level instanceof ServerLevel) {
            ((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, this.leashHolder));
        }
        if (this.isPassenger()) {
            this.stopRiding();
        }
    }

    public void setDelayedLeashHolderId(int i) {
        this.delayedLeashHolderId = i;
        this.dropLeash(false, false);
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl) {
        boolean bl2 = super.startRiding(entity, bl);
        if (bl2 && this.isLeashed()) {
            this.dropLeash(true, true);
        }
        return bl2;
    }

    private void restoreLeashFromSave() {
        if (this.leashInfoTag != null && this.level instanceof ServerLevel) {
            if (this.leashInfoTag.hasUUID("UUID")) {
                UUID uUID = this.leashInfoTag.getUUID("UUID");
                Entity entity = ((ServerLevel)this.level).getEntity(uUID);
                if (entity != null) {
                    this.setLeashedTo(entity, true);
                    return;
                }
            } else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
                BlockPos blockPos = NbtUtils.readBlockPos(this.leashInfoTag);
                this.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(this.level, blockPos), true);
                return;
            }
            if (this.tickCount > 100) {
                this.spawnAtLocation(Items.LEAD);
                this.leashInfoTag = null;
            }
        }
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isNoAi();
    }

    public void setNoAi(boolean bl) {
        byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean bl) {
        byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 2) : (byte)(b & 0xFFFFFFFD));
    }

    public void setAggressive(boolean bl) {
        byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 4) : (byte)(b & 0xFFFFFFFB));
    }

    public boolean isNoAi() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
    }

    public boolean isAggressive() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
    }

    public void setBaby(boolean bl) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public double getMeleeAttackRangeSqr(LivingEntity livingEntity) {
        return this.getBbWidth() * 2.0f * (this.getBbWidth() * 2.0f) + livingEntity.getBbWidth();
    }

    public double getPerceivedTargetDistanceSquareForMeleeAttack(LivingEntity livingEntity) {
        return Math.max(this.distanceToSqr(livingEntity.getMeleeAttackReferencePosition()), this.distanceToSqr(livingEntity.position()));
    }

    public boolean isWithinMeleeAttackRange(LivingEntity livingEntity) {
        double d = this.getPerceivedTargetDistanceSquareForMeleeAttack(livingEntity);
        return d <= this.getMeleeAttackRangeSqr(livingEntity);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean bl;
        int i;
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float g = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entity instanceof LivingEntity) {
            f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entity).getMobType());
            g += (float)EnchantmentHelper.getKnockbackBonus(this);
        }
        if ((i = EnchantmentHelper.getFireAspect(this)) > 0) {
            entity.setSecondsOnFire(i * 4);
        }
        if (bl = entity.hurt(this.damageSources().mobAttack(this), f)) {
            if (g > 0.0f && entity instanceof LivingEntity) {
                ((LivingEntity)entity).knockback(g * 0.5f, Mth.sin(this.getYRot() * ((float)Math.PI / 180)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180)));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (entity instanceof Player) {
                Player player = (Player)entity;
                this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
            }
            this.doEnchantDamageEffects(this, entity);
            this.setLastHurtMob(entity);
        }
        return bl;
    }

    private void maybeDisableShield(Player player, ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack.isEmpty() && !itemStack2.isEmpty() && itemStack.getItem() instanceof AxeItem && itemStack2.is(Items.SHIELD)) {
            float f = 0.25f + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05f;
            if (this.random.nextFloat() < f) {
                player.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level.broadcastEntityEvent(player, (byte)30);
            }
        }
    }

    protected boolean isSunBurnTick() {
        if (this.level.isDay() && !this.level.isClientSide) {
            boolean bl;
            float f = this.getLightLevelDependentMagicValue();
            BlockPos blockPos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean bl2 = bl = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
            if (f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.level.canSeeSky(blockPos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid(tagKey);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
    }

    public void removeFreeWill() {
        this.removeAllGoals(goal -> true);
        this.getBrain().removeAllBehaviors();
    }

    public void removeAllGoals(Predicate<Goal> predicate) {
        this.goalSelector.removeAllGoals(predicate);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        this.dropLeash(true, false);
        this.getAllSlots().forEach(itemStack -> itemStack.setCount(0));
    }

    @Override
    @Nullable
    public ItemStack getPickResult() {
        SpawnEggItem spawnEggItem = SpawnEggItem.byId(this.getType());
        if (spawnEggItem == null) {
            return null;
        }
        return new ItemStack(spawnEggItem);
    }
}

