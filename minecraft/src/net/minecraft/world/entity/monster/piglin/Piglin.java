package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PigZombie;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Piglin extends Monster implements CrossbowAttackMob {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
	private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
	private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
		SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.2F, AttributeModifier.Operation.MULTIPLY_BASE
	);
	private int timeInOverworld = 0;
	private final SimpleContainer inventory = new SimpleContainer(8);
	private static int createCounter = 0;
	private static int dieCounter = 0;
	private static int killedByHoglinCounter = 0;
	private static int removeCounter = 0;
	protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES,
		SensorType.NEAREST_PLAYERS,
		SensorType.NEAREST_ITEMS,
		SensorType.HURT_BY,
		SensorType.INTERACTABLE_DOORS,
		SensorType.PIGLIN_SPECIFIC_SENSOR
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.INTERACTABLE_DOORS,
		MemoryModuleType.OPENED_DOORS,
		MemoryModuleType.LIVING_ENTITIES,
		MemoryModuleType.VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
		MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.INTERACTION_TARGET,
		MemoryModuleType.PATH,
		MemoryModuleType.ANGRY_AT,
		MemoryModuleType.AVOID_TARGET,
		MemoryModuleType.ADMIRING_ITEM,
		MemoryModuleType.WAS_HIT_BY_PLAYER,
		MemoryModuleType.CELEBRATE_LOCATION,
		MemoryModuleType.HUNTED_RECENTLY,
		MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
		MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN,
		MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN,
		MemoryModuleType.RIDE_TARGET,
		MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
		MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
		MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
		MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
		MemoryModuleType.ATE_RECENTLY,
		MemoryModuleType.NEAREST_SOUL_FIRE
	);

	public Piglin(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.setCanPickUpLoot(true);
		((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
		this.xpReward = 5;
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
	}

	@Override
	public void remove() {
		super.remove();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.isBaby()) {
			compoundTag.putBoolean("IsBaby", true);
		}

		compoundTag.put("Inventory", createInventoryTag(this.inventory));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setBaby(compoundTag.getBoolean("IsBaby"));
		updateInventoryFromTag(this.inventory, compoundTag.getList("Inventory", 10));
	}

	private static ListTag createInventoryTag(Container container) {
		ListTag listTag = new ListTag();

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack itemStack = container.getItem(i);
			if (!itemStack.isEmpty()) {
				listTag.add(itemStack.save(new CompoundTag()));
			}
		}

		return listTag;
	}

	private static void updateInventoryFromTag(SimpleContainer simpleContainer, ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			ItemStack itemStack = ItemStack.of(listTag.getCompound(i));
			if (!itemStack.isEmpty()) {
				simpleContainer.addItem(itemStack);
			}
		}
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		this.inventory.removeAllItems().forEach(this::spawnAtLocation);
		ItemStack itemStack = this.getItemInHand(InteractionHand.OFF_HAND);
		if (!itemStack.isEmpty()) {
			this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			this.spawnAtLocation(itemStack);
		}
	}

	protected ItemStack addToInventory(ItemStack itemStack) {
		return this.inventory.addItem(itemStack);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_BABY_ID, false);
		this.entityData.define(DATA_IS_CHARGING_CROSSBOW, false);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_BABY_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0);
	}

	public static boolean checkPiglinSpawnRules(
		EntityType<Piglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getBlockState(blockPos.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (levelAccessor.getRandom().nextFloat() < 0.2F) {
			this.setBaby(true);
		}

		this.populateDefaultEquipmentSlots(difficultyInstance);
		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
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
	protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
		if (this.isAdult()) {
			this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
			this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
			this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
			this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
			this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
		}
	}

	private void maybeWearArmor(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		if (this.level.random.nextFloat() < 0.1F) {
			this.setItemSlot(equipmentSlot, itemStack);
		}
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return PiglinAi.makeBrain(this, dynamic);
	}

	@Override
	public Brain<Piglin> getBrain() {
		return (Brain<Piglin>)super.getBrain();
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		if (super.mobInteract(player, interactionHand)) {
			return true;
		} else {
			return this.level.isClientSide ? false : PiglinAi.mobInteract(this, player, interactionHand);
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return this.isBaby() ? 0.93F : 1.74F;
	}

	@Override
	public void setBaby(boolean bl) {
		this.getEntityData().set(DATA_BABY_ID, bl);
		if (!this.level.isClientSide) {
			AttributeInstance attributeInstance = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			attributeInstance.removeModifier(SPEED_MODIFIER_BABY);
			if (bl) {
				attributeInstance.addModifier(SPEED_MODIFIER_BABY);
			}
		}
	}

	@Override
	public boolean isBaby() {
		return this.getEntityData().get(DATA_BABY_ID);
	}

	public boolean isAdult() {
		return !this.isBaby();
	}

	public boolean isConverting() {
		return this.level.getDimension().getType() == DimensionType.OVERWORLD;
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("piglinBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		PiglinAi.updateActivity(this);
		PiglinAi.maybePlayActivitySound(this);
		if (PiglinAi.seesPlayer(this)) {
			this.setPersistenceRequired();
		}

		if (this.level.dimension.getType() == DimensionType.OVERWORLD) {
			this.timeInOverworld++;
		} else {
			this.timeInOverworld = 0;
		}

		if (this.timeInOverworld > 300) {
			this.playConvertedSound();
			this.finishConversion((ServerLevel)this.level);
		}
	}

	@Override
	protected int getExperienceReward(Player player) {
		return this.xpReward;
	}

	private void finishConversion(ServerLevel serverLevel) {
		PigZombie pigZombie = EntityType.ZOMBIE_PIGMAN.create(serverLevel);
		pigZombie.copyPosition(this);
		pigZombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(new BlockPos(pigZombie)), MobSpawnType.CONVERSION, null, null);
		pigZombie.setBaby(this.isBaby());
		this.remove();
		pigZombie.setNoAi(this.isNoAi());
		if (this.hasCustomName()) {
			pigZombie.setCustomName(this.getCustomName());
			pigZombie.setCustomNameVisible(this.isCustomNameVisible());
		}

		serverLevel.addFreshEntity(pigZombie);
		pigZombie.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return (LivingEntity)this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
	}

	private ItemStack createSpawnWeapon() {
		return (double)this.random.nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD);
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	public Piglin.PiglinArmPose getArmPose() {
		if (this.swinging) {
			return Piglin.PiglinArmPose.DEFAULT;
		} else if (PiglinAi.isLovedItem(this.getOffhandItem().getItem())) {
			return Piglin.PiglinArmPose.ADMIRING_ITEM;
		} else if (this.isChargingCrossbow()) {
			return Piglin.PiglinArmPose.CROSSBOW_CHARGE;
		} else {
			return this.isHolding(Items.CROSSBOW) && this.isAggressive() ? Piglin.PiglinArmPose.CROSSBOW_HOLD : Piglin.PiglinArmPose.DEFAULT;
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		boolean bl = super.hurt(damageSource, f);
		if (this.level.isClientSide) {
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
	public void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f) {
		this.shootCrossbowProjectile(this, livingEntity, projectile, f, 1.6F);
	}

	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		return PiglinAi.wantsToPickUp(this, itemStack);
	}

	@Override
	protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2, EquipmentSlot equipmentSlot) {
		if (PiglinAi.isLovedItem(itemStack.getItem()) && !PiglinAi.isLovedItem(itemStack2.getItem())) {
			return true;
		} else {
			return !PiglinAi.isLovedItem(itemStack.getItem()) && PiglinAi.isLovedItem(itemStack2.getItem())
				? false
				: super.canReplaceCurrentItem(itemStack, itemStack2, equipmentSlot);
		}
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		PiglinAi.pickUpItem(this, itemEntity);
	}

	protected boolean isOffHandEmpty() {
		return this.getOffhandItem().isEmpty();
	}

	public boolean isRiding() {
		return this.getVehicle() != null;
	}

	protected float getMovementSpeed() {
		return (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PIGLIN_AMBIENT;
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

	void playAdmiringSound() {
		this.playSound(SoundEvents.PIGLIN_ADMIRING_ITEM, 1.0F, this.getVoicePitch());
	}

	@Override
	public void playAmbientSound() {
		if (PiglinAi.isIdle(this)) {
			super.playAmbientSound();
		}
	}

	void playAngrySound() {
		this.playSound(SoundEvents.PIGLIN_ANGRY, 1.0F, this.getVoicePitch());
	}

	void playCelebrateSound() {
		this.playSound(SoundEvents.PIGLIN_CELEBRATE, 1.0F, this.getVoicePitch());
	}

	void playHurtSound() {
		this.playRetreatSound();
	}

	void playRetreatSound() {
		this.playSound(SoundEvents.PIGLIN_RETREAT, 1.0F, this.getVoicePitch());
	}

	void playJealousSound() {
		this.playSound(SoundEvents.PIGLIN_JEALOUS, 1.0F, this.getVoicePitch());
	}

	void playConvertedSound() {
		this.playSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED, 1.0F, 1.0F);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Environment(EnvType.CLIENT)
	public static enum PiglinArmPose {
		CROSSBOW_HOLD,
		CROSSBOW_CHARGE,
		ADMIRING_ITEM,
		DEFAULT;
	}
}
