package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Piglin extends AbstractPiglin implements CrossbowAttackMob, InventoryCarrier {
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
	private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
	private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
		SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.2F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
	);
	private static final int MAX_HEALTH = 16;
	private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
	private static final int ATTACK_DAMAGE = 5;
	private static final float CHANCE_OF_WEARING_EACH_ARMOUR_ITEM = 0.1F;
	private static final int MAX_PASSENGERS_ON_ONE_HOGLIN = 3;
	private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2F;
	private static final EntityDimensions BABY_DIMENSIONS = EntityType.PIGLIN.getDimensions().scale(0.5F).withEyeHeight(0.97F);
	private static final double PROBABILITY_OF_SPAWNING_WITH_CROSSBOW_INSTEAD_OF_SWORD = 0.5;
	private final SimpleContainer inventory = new SimpleContainer(8);
	private boolean cannotHunt;
	protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.DOORS_TO_CLOSE,
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
		MemoryModuleType.NEARBY_ADULT_PIGLINS,
		MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
		MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN,
		MemoryModuleType.INTERACTION_TARGET,
		MemoryModuleType.PATH,
		MemoryModuleType.ANGRY_AT,
		MemoryModuleType.UNIVERSAL_ANGER,
		MemoryModuleType.AVOID_TARGET,
		MemoryModuleType.ADMIRING_ITEM,
		MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM,
		MemoryModuleType.ADMIRING_DISABLED,
		MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM,
		MemoryModuleType.CELEBRATE_LOCATION,
		MemoryModuleType.DANCING,
		MemoryModuleType.HUNTED_RECENTLY,
		MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
		MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
		MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED,
		MemoryModuleType.RIDE_TARGET,
		MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
		MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
		MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
		MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
		MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
		MemoryModuleType.ATE_RECENTLY,
		MemoryModuleType.NEAREST_REPELLENT
	);

	public Piglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.isBaby()) {
			compoundTag.putBoolean("IsBaby", true);
		}

		if (this.cannotHunt) {
			compoundTag.putBoolean("CannotHunt", true);
		}

		this.writeInventoryToTag(compoundTag, this.registryAccess());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setBaby(compoundTag.getBoolean("IsBaby"));
		this.setCannotHunt(compoundTag.getBoolean("CannotHunt"));
		this.readInventoryFromTag(compoundTag, this.registryAccess());
	}

	@VisibleForDebug
	@Override
	public SimpleContainer getInventory() {
		return this.inventory;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		if (damageSource.getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
			ItemStack itemStack = new ItemStack(Items.PIGLIN_HEAD);
			creeper.increaseDroppedSkulls();
			this.spawnAtLocation(itemStack);
		}

		this.inventory.removeAllItems().forEach(this::spawnAtLocation);
	}

	protected ItemStack addToInventory(ItemStack itemStack) {
		return this.inventory.addItem(itemStack);
	}

	protected boolean canAddToInventory(ItemStack itemStack) {
		return this.inventory.canAddItem(itemStack);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_BABY_ID, false);
		builder.define(DATA_IS_CHARGING_CROSSBOW, false);
		builder.define(DATA_IS_DANCING, false);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_BABY_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.35F).add(Attributes.ATTACK_DAMAGE, 5.0);
	}

	public static boolean checkPiglinSpawnRules(
		EntityType<Piglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		if (mobSpawnType != MobSpawnType.STRUCTURE) {
			if (randomSource.nextFloat() < 0.2F) {
				this.setBaby(true);
			} else if (this.isAdult()) {
				this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
			}
		}

		PiglinAi.initMemories(this, serverLevelAccessor.getRandom());
		this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
		this.populateDefaultEquipmentEnchantments(randomSource, difficultyInstance);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isPersistenceRequired();
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		if (this.isAdult()) {
			this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), randomSource);
			this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), randomSource);
			this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), randomSource);
			this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), randomSource);
		}
	}

	private void maybeWearArmor(EquipmentSlot equipmentSlot, ItemStack itemStack, RandomSource randomSource) {
		if (randomSource.nextFloat() < 0.1F) {
			this.setItemSlot(equipmentSlot, itemStack);
		}
	}

	@Override
	protected Brain.Provider<Piglin> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return PiglinAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Piglin> getBrain() {
		return (Brain<Piglin>)super.getBrain();
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		InteractionResult interactionResult = super.mobInteract(player, interactionHand);
		if (interactionResult.consumesAction()) {
			return interactionResult;
		} else if (!this.level().isClientSide) {
			return PiglinAi.mobInteract(this, player, interactionHand);
		} else {
			boolean bl = PiglinAi.canAdmire(this, player.getItemInHand(interactionHand)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
			return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
	}

	@Override
	public void setBaby(boolean bl) {
		this.getEntityData().set(DATA_BABY_ID, bl);
		if (!this.level().isClientSide) {
			AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
			attributeInstance.removeModifier(SPEED_MODIFIER_BABY.getId());
			if (bl) {
				attributeInstance.addTransientModifier(SPEED_MODIFIER_BABY);
			}
		}
	}

	@Override
	public boolean isBaby() {
		return this.getEntityData().get(DATA_BABY_ID);
	}

	private void setCannotHunt(boolean bl) {
		this.cannotHunt = bl;
	}

	@Override
	protected boolean canHunt() {
		return !this.cannotHunt;
	}

	@Override
	protected void customServerAiStep() {
		this.level().getProfiler().push("piglinBrain");
		this.getBrain().tick((ServerLevel)this.level(), this);
		this.level().getProfiler().pop();
		PiglinAi.updateActivity(this);
		super.customServerAiStep();
	}

	@Override
	public int getExperienceReward() {
		return this.xpReward;
	}

	@Override
	protected void finishConversion(ServerLevel serverLevel) {
		PiglinAi.cancelAdmiring(this);
		this.inventory.removeAllItems().forEach(this::spawnAtLocation);
		super.finishConversion(serverLevel);
	}

	private ItemStack createSpawnWeapon() {
		return (double)this.random.nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
	}

	private boolean isChargingCrossbow() {
		return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
	}

	@Override
	public void setChargingCrossbow(boolean bl) {
		this.entityData.set(DATA_IS_CHARGING_CROSSBOW, bl);
	}

	@Override
	public void onCrossbowAttackPerformed() {
		this.noActionTime = 0;
	}

	@Override
	public PiglinArmPose getArmPose() {
		if (this.isDancing()) {
			return PiglinArmPose.DANCING;
		} else if (PiglinAi.isLovedItem(this.getOffhandItem())) {
			return PiglinArmPose.ADMIRING_ITEM;
		} else if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
			return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
		} else if (this.isChargingCrossbow()) {
			return PiglinArmPose.CROSSBOW_CHARGE;
		} else {
			return this.isAggressive() && this.isHolding(Items.CROSSBOW) ? PiglinArmPose.CROSSBOW_HOLD : PiglinArmPose.DEFAULT;
		}
	}

	public boolean isDancing() {
		return this.entityData.get(DATA_IS_DANCING);
	}

	public void setDancing(boolean bl) {
		this.entityData.set(DATA_IS_DANCING, bl);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		boolean bl = super.hurt(damageSource, f);
		if (this.level().isClientSide) {
			return false;
		} else {
			if (bl && damageSource.getEntity() instanceof LivingEntity) {
				PiglinAi.wasHurtBy(this, (LivingEntity)damageSource.getEntity());
			}

			return bl;
		}
	}

	@Override
	public void performRangedAttack(LivingEntity livingEntity, float f) {
		this.performCrossbowAttack(this, 1.6F);
	}

	@Override
	public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
		return projectileWeaponItem == Items.CROSSBOW;
	}

	protected void holdInMainHand(ItemStack itemStack) {
		this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemStack);
	}

	protected void holdInOffHand(ItemStack itemStack) {
		if (itemStack.is(PiglinAi.BARTERING_ITEM)) {
			this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
			this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
		} else {
			this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, itemStack);
		}
	}

	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.canPickUpLoot() && PiglinAi.wantsToPickup(this, itemStack);
	}

	protected boolean canReplaceCurrentItem(ItemStack itemStack) {
		EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
		ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
		return this.canReplaceCurrentItem(itemStack, itemStack2);
	}

	@Override
	protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
		if (EnchantmentHelper.hasBindingCurse(itemStack2)) {
			return false;
		} else {
			boolean bl = PiglinAi.isLovedItem(itemStack) || itemStack.is(Items.CROSSBOW);
			boolean bl2 = PiglinAi.isLovedItem(itemStack2) || itemStack2.is(Items.CROSSBOW);
			if (bl && !bl2) {
				return true;
			} else if (!bl && bl2) {
				return false;
			} else {
				return this.isAdult() && !itemStack.is(Items.CROSSBOW) && itemStack2.is(Items.CROSSBOW) ? false : super.canReplaceCurrentItem(itemStack, itemStack2);
			}
		}
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		this.onItemPickup(itemEntity);
		PiglinAi.pickUpItem(this, itemEntity);
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		if (this.isBaby() && entity.getType() == EntityType.HOGLIN) {
			entity = this.getTopPassenger(entity, 3);
		}

		return super.startRiding(entity, bl);
	}

	private Entity getTopPassenger(Entity entity, int i) {
		List<Entity> list = entity.getPassengers();
		return i != 1 && !list.isEmpty() ? this.getTopPassenger((Entity)list.get(0), i - 1) : entity;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.level().isClientSide ? null : (SoundEvent)PiglinAi.getSoundForCurrentActivity(this).orElse(null);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PIGLIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PIGLIN_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.0F);
	}

	@Override
	protected void playConvertedSound() {
		this.makeSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
	}
}
