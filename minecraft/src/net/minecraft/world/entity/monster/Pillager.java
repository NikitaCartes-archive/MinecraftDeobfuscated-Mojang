package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public class Pillager extends AbstractIllager implements CrossbowAttackMob, InventoryCarrier {
	private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Pillager.class, EntityDataSerializers.BOOLEAN);
	private static final int INVENTORY_SIZE = 5;
	private static final int SLOT_OFFSET = 300;
	private final SimpleContainer inventory = new SimpleContainer(5);

	public Pillager(EntityType<? extends Pillager> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
		this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
		this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.35F)
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.ATTACK_DAMAGE, 5.0)
			.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(IS_CHARGING_CROSSBOW, false);
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
		this.writeInventoryToTag(compoundTag, this.registryAccess());
	}

	@Override
	public AbstractIllager.IllagerArmPose getArmPose() {
		if (this.isChargingCrossbow()) {
			return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
		} else if (this.isHolding(Items.CROSSBOW)) {
			return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
		} else {
			return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.readInventoryFromTag(compoundTag, this.registryAccess());
		this.setCanPickUpLoot(true);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return 0.0F;
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 1;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
		this.populateDefaultEquipmentEnchantments(serverLevelAccessor, randomSource, difficultyInstance);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
	}

	@Override
	protected void enchantSpawnedWeapon(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
		super.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
		if (randomSource.nextInt(300) == 0) {
			ItemStack itemStack = this.getMainHandItem();
			if (itemStack.is(Items.CROSSBOW)) {
				EnchantmentHelper.enchantItemFromProvider(
					itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.PILLAGER_SPAWN_CROSSBOW, difficultyInstance, randomSource
				);
			}
		}
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
		this.performCrossbowAttack(this, 1.6F);
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
		return j >= 0 && j < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, j) : super.getSlot(i);
	}

	@Override
	public void applyRaidBuffs(ServerLevel serverLevel, int i, boolean bl) {
		Raid raid = this.getCurrentRaid();
		boolean bl2 = this.random.nextFloat() <= raid.getEnchantOdds();
		if (bl2) {
			ItemStack itemStack = new ItemStack(Items.CROSSBOW);
			ResourceKey<EnchantmentProvider> resourceKey;
			if (i > raid.getNumGroups(Difficulty.NORMAL)) {
				resourceKey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_5;
			} else if (i > raid.getNumGroups(Difficulty.EASY)) {
				resourceKey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_3;
			} else {
				resourceKey = null;
			}

			if (resourceKey != null) {
				EnchantmentHelper.enchantItemFromProvider(
					itemStack, serverLevel.registryAccess(), resourceKey, serverLevel.getCurrentDifficultyAt(this.blockPosition()), this.getRandom()
				);
				this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
			}
		}
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.PILLAGER_CELEBRATE;
	}
}
