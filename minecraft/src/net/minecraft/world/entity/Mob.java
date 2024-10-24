package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.ArmorItem;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public abstract class Mob extends LivingEntity implements EquipmentUser, Leashable, Targeting {
	private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
	private static final int MOB_FLAG_NO_AI = 1;
	private static final int MOB_FLAG_LEFTHANDED = 2;
	private static final int MOB_FLAG_AGGRESSIVE = 4;
	protected static final int PICKUP_REACH = 1;
	private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
	private static final List<EquipmentSlot> EQUIPMENT_POPULATION_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
	public static final float MAX_WEARING_ARMOR_CHANCE = 0.15F;
	public static final float MAX_PICKUP_LOOT_CHANCE = 0.55F;
	public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5F;
	public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25F;
	public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
	public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0F;
	public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
	public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
	private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;
	protected static final ResourceLocation RANDOM_SPAWN_BONUS_ID = ResourceLocation.withDefaultNamespace("random_spawn_bonus");
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
	private ItemStack bodyArmorItem = ItemStack.EMPTY;
	protected float bodyArmorDropChance;
	private boolean canPickUpLoot;
	private boolean persistenceRequired;
	private final Map<PathType, Float> pathfindingMalus = Maps.newEnumMap(PathType.class);
	private Optional<ResourceKey<LootTable>> lootTable = Optional.empty();
	private long lootTableSeed;
	@Nullable
	private Leashable.LeashData leashData;
	private BlockPos restrictCenter = BlockPos.ZERO;
	private float restrictRadius = -1.0F;

	protected Mob(EntityType<? extends Mob> entityType, Level level) {
		super(entityType, level);
		this.goalSelector = new GoalSelector();
		this.targetSelector = new GoalSelector();
		this.lookControl = new LookControl(this);
		this.moveControl = new MoveControl(this);
		this.jumpControl = new JumpControl(this);
		this.bodyRotationControl = this.createBodyControl();
		this.navigation = this.createNavigation(level);
		this.sensing = new Sensing(this);
		Arrays.fill(this.armorDropChances, 0.085F);
		Arrays.fill(this.handDropChances, 0.085F);
		this.bodyArmorDropChance = 0.085F;
		if (level instanceof ServerLevel) {
			this.registerGoals();
		}
	}

	protected void registerGoals() {
	}

	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
	}

	protected PathNavigation createNavigation(Level level) {
		return new GroundPathNavigation(this, level);
	}

	protected boolean shouldPassengersInheritMalus() {
		return false;
	}

	public float getPathfindingMalus(PathType pathType) {
		Mob mob2;
		label17: {
			if (this.getControlledVehicle() instanceof Mob mob && mob.shouldPassengersInheritMalus()) {
				mob2 = mob;
				break label17;
			}

			mob2 = this;
		}

		Float float_ = (Float)mob2.pathfindingMalus.get(pathType);
		return float_ == null ? pathType.getMalus() : float_;
	}

	public void setPathfindingMalus(PathType pathType, float f) {
		this.pathfindingMalus.put(pathType, f);
	}

	public void onPathfindingStart() {
	}

	public void onPathfindingDone() {
	}

	protected BodyRotationControl createBodyControl() {
		return new BodyRotationControl(this);
	}

	public LookControl getLookControl() {
		return this.lookControl;
	}

	public MoveControl getMoveControl() {
		return this.getControlledVehicle() instanceof Mob mob ? mob.getMoveControl() : this.moveControl;
	}

	public JumpControl getJumpControl() {
		return this.jumpControl;
	}

	public PathNavigation getNavigation() {
		return this.getControlledVehicle() instanceof Mob mob ? mob.getNavigation() : this.navigation;
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		Entity entity = this.getFirstPassenger();
		if (!this.isNoAi() && entity instanceof Mob mob && entity.canControlVehicle()) {
			return mob;
		}

		return null;
	}

	public Sensing getSensing() {
		return this.sensing;
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.target;
	}

	@Nullable
	protected final LivingEntity getTargetFromBrain() {
		return (LivingEntity)this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_MOB_FLAGS_ID, (byte)0);
	}

	public int getAmbientSoundInterval() {
		return 80;
	}

	public void playAmbientSound() {
		this.makeSound(this.getAmbientSound());
	}

	@Override
	public void baseTick() {
		super.baseTick();
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("mobBaseTick");
		if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
			this.resetAmbientSoundTime();
			this.playAmbientSound();
		}

		profilerFiller.pop();
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
	protected int getBaseExperienceReward(ServerLevel serverLevel) {
		if (this.xpReward > 0) {
			int i = this.xpReward;

			for (int j = 0; j < this.armorItems.size(); j++) {
				if (!this.armorItems.get(j).isEmpty() && this.armorDropChances[j] <= 1.0F) {
					i += 1 + this.random.nextInt(3);
				}
			}

			for (int jx = 0; jx < this.handItems.size(); jx++) {
				if (!this.handItems.get(jx).isEmpty() && this.handDropChances[jx] <= 1.0F) {
					i += 1 + this.random.nextInt(3);
				}
			}

			if (!this.bodyArmorItem.isEmpty() && this.bodyArmorDropChance <= 1.0F) {
				i += 1 + this.random.nextInt(3);
			}

			return i;
		} else {
			return this.xpReward;
		}
	}

	public void spawnAnim() {
		if (this.level().isClientSide) {
			this.makePoofParticles();
		} else {
			this.level().broadcastEntityEvent(this, (byte)20);
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
		if (!this.level().isClientSide && this.tickCount % 5 == 0) {
			this.updateControlFlags();
		}
	}

	protected void updateControlFlags() {
		boolean bl = !(this.getControllingPassenger() instanceof Mob);
		boolean bl2 = !(this.getVehicle() instanceof AbstractBoat);
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
			if (!itemStack.isEmpty()) {
				listTag.add(itemStack.save(this.registryAccess()));
			} else {
				listTag.add(new CompoundTag());
			}
		}

		compoundTag.put("ArmorItems", listTag);
		ListTag listTag2 = new ListTag();

		for (float f : this.armorDropChances) {
			listTag2.add(FloatTag.valueOf(f));
		}

		compoundTag.put("ArmorDropChances", listTag2);
		ListTag listTag3 = new ListTag();

		for (ItemStack itemStack2 : this.handItems) {
			if (!itemStack2.isEmpty()) {
				listTag3.add(itemStack2.save(this.registryAccess()));
			} else {
				listTag3.add(new CompoundTag());
			}
		}

		compoundTag.put("HandItems", listTag3);
		ListTag listTag4 = new ListTag();

		for (float g : this.handDropChances) {
			listTag4.add(FloatTag.valueOf(g));
		}

		compoundTag.put("HandDropChances", listTag4);
		if (!this.bodyArmorItem.isEmpty()) {
			compoundTag.put("body_armor_item", this.bodyArmorItem.save(this.registryAccess()));
			compoundTag.putFloat("body_armor_drop_chance", this.bodyArmorDropChance);
		}

		this.writeLeashData(compoundTag, this.leashData);
		compoundTag.putBoolean("LeftHanded", this.isLeftHanded());
		if (this.lootTable.isPresent()) {
			compoundTag.putString("DeathLootTable", ((ResourceKey)this.lootTable.get()).location().toString());
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
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("CanPickUpLoot", 99)) {
			this.setCanPickUpLoot(compoundTag.getBoolean("CanPickUpLoot"));
		}

		this.persistenceRequired = compoundTag.getBoolean("PersistenceRequired");
		if (compoundTag.contains("ArmorItems", 9)) {
			ListTag listTag = compoundTag.getList("ArmorItems", 10);

			for (int i = 0; i < this.armorItems.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				this.armorItems.set(i, ItemStack.parseOptional(this.registryAccess(), compoundTag2));
			}
		}

		if (compoundTag.contains("ArmorDropChances", 9)) {
			ListTag listTag = compoundTag.getList("ArmorDropChances", 5);

			for (int i = 0; i < listTag.size(); i++) {
				this.armorDropChances[i] = listTag.getFloat(i);
			}
		}

		if (compoundTag.contains("HandItems", 9)) {
			ListTag listTag = compoundTag.getList("HandItems", 10);

			for (int i = 0; i < this.handItems.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				this.handItems.set(i, ItemStack.parseOptional(this.registryAccess(), compoundTag2));
			}
		}

		if (compoundTag.contains("HandDropChances", 9)) {
			ListTag listTag = compoundTag.getList("HandDropChances", 5);

			for (int i = 0; i < listTag.size(); i++) {
				this.handDropChances[i] = listTag.getFloat(i);
			}
		}

		if (compoundTag.contains("body_armor_item", 10)) {
			this.bodyArmorItem = (ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("body_armor_item")).orElse(ItemStack.EMPTY);
			this.bodyArmorDropChance = compoundTag.getFloat("body_armor_drop_chance");
		} else {
			this.bodyArmorItem = ItemStack.EMPTY;
		}

		this.leashData = this.readLeashData(compoundTag);
		this.setLeftHanded(compoundTag.getBoolean("LeftHanded"));
		if (compoundTag.contains("DeathLootTable", 8)) {
			this.lootTable = Optional.of(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(compoundTag.getString("DeathLootTable"))));
			this.lootTableSeed = compoundTag.getLong("DeathLootTableSeed");
		}

		this.setNoAi(compoundTag.getBoolean("NoAI"));
	}

	@Override
	protected void dropFromLootTable(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
		super.dropFromLootTable(serverLevel, damageSource, bl);
		this.lootTable = Optional.empty();
	}

	@Override
	public final Optional<ResourceKey<LootTable>> getLootTable() {
		return this.lootTable.isPresent() ? this.lootTable : super.getLootTable();
	}

	@Override
	public long getLootTableSeed() {
		return this.lootTableSeed;
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

	public void stopInPlace() {
		this.getNavigation().stop();
		this.setXxa(0.0F);
		this.setYya(0.0F);
		this.setSpeed(0.0F);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("looting");
		if (this.level() instanceof ServerLevel serverLevel
			&& this.canPickUpLoot()
			&& this.isAlive()
			&& !this.dead
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			Vec3i vec3i = this.getPickupReach();

			for (ItemEntity itemEntity : this.level()
				.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ()))) {
				if (!itemEntity.isRemoved() && !itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay() && this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
					this.pickUpItem(serverLevel, itemEntity);
				}
			}
		}

		profilerFiller.pop();
	}

	protected Vec3i getPickupReach() {
		return ITEM_PICKUP_REACH;
	}

	protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		ItemStack itemStack2 = this.equipItemIfPossible(serverLevel, itemStack.copy());
		if (!itemStack2.isEmpty()) {
			this.onItemPickup(itemEntity);
			this.take(itemEntity, itemStack2.getCount());
			itemStack.shrink(itemStack2.getCount());
			if (itemStack.isEmpty()) {
				itemEntity.discard();
			}
		}
	}

	public ItemStack equipItemIfPossible(ServerLevel serverLevel, ItemStack itemStack) {
		EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
		ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
		boolean bl = this.canReplaceCurrentItem(itemStack, itemStack2, equipmentSlot);
		if (equipmentSlot.isArmor() && !bl) {
			equipmentSlot = EquipmentSlot.MAINHAND;
			itemStack2 = this.getItemBySlot(equipmentSlot);
			bl = itemStack2.isEmpty();
		}

		if (bl && this.canHoldItem(itemStack)) {
			double d = (double)this.getEquipmentDropChance(equipmentSlot);
			if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d) {
				this.spawnAtLocation(serverLevel, itemStack2);
			}

			ItemStack itemStack3 = equipmentSlot.limit(itemStack);
			this.setItemSlotAndDropWhenKilled(equipmentSlot, itemStack3);
			return itemStack3;
		} else {
			return ItemStack.EMPTY;
		}
	}

	protected void setItemSlotAndDropWhenKilled(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		this.setItemSlot(equipmentSlot, itemStack);
		this.setGuaranteedDrop(equipmentSlot);
		this.persistenceRequired = true;
	}

	public void setGuaranteedDrop(EquipmentSlot equipmentSlot) {
		switch (equipmentSlot.getType()) {
			case HAND:
				this.handDropChances[equipmentSlot.getIndex()] = 2.0F;
				break;
			case HUMANOID_ARMOR:
				this.armorDropChances[equipmentSlot.getIndex()] = 2.0F;
				break;
			case ANIMAL_ARMOR:
				this.bodyArmorDropChance = 2.0F;
		}
	}

	protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2, EquipmentSlot equipmentSlot) {
		if (itemStack2.isEmpty()) {
			return true;
		} else if (itemStack.getItem() instanceof SwordItem) {
			if (!(itemStack2.getItem() instanceof SwordItem)) {
				return true;
			} else {
				double d = this.getApproximateAttributeWith(itemStack, Attributes.ATTACK_DAMAGE, equipmentSlot);
				double e = this.getApproximateAttributeWith(itemStack2, Attributes.ATTACK_DAMAGE, equipmentSlot);
				return d != e ? d > e : this.canReplaceEqualItem(itemStack, itemStack2);
			}
		} else if (itemStack.getItem() instanceof BowItem && itemStack2.getItem() instanceof BowItem) {
			return this.canReplaceEqualItem(itemStack, itemStack2);
		} else if (itemStack.getItem() instanceof CrossbowItem && itemStack2.getItem() instanceof CrossbowItem) {
			return this.canReplaceEqualItem(itemStack, itemStack2);
		} else if (itemStack.getItem() instanceof ArmorItem) {
			if (EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
				return false;
			} else if (!(itemStack2.getItem() instanceof ArmorItem)) {
				return true;
			} else {
				double d = this.getApproximateAttributeWith(itemStack, Attributes.ARMOR, equipmentSlot);
				double e = this.getApproximateAttributeWith(itemStack2, Attributes.ARMOR, equipmentSlot);
				double f = this.getApproximateAttributeWith(itemStack, Attributes.ARMOR_TOUGHNESS, equipmentSlot);
				double g = this.getApproximateAttributeWith(itemStack2, Attributes.ARMOR_TOUGHNESS, equipmentSlot);
				if (d != e) {
					return d > e;
				} else {
					return f != g ? f > g : this.canReplaceEqualItem(itemStack, itemStack2);
				}
			}
		} else {
			if (itemStack.getItem() instanceof DiggerItem) {
				if (itemStack2.getItem() instanceof BlockItem) {
					return true;
				}

				if (itemStack2.getItem() instanceof DiggerItem) {
					double d = this.getApproximateAttributeWith(itemStack, Attributes.ATTACK_DAMAGE, equipmentSlot);
					double e = this.getApproximateAttributeWith(itemStack2, Attributes.ATTACK_DAMAGE, equipmentSlot);
					if (d != e) {
						return d > e;
					}

					return this.canReplaceEqualItem(itemStack, itemStack2);
				}
			}

			return false;
		}
	}

	private double getApproximateAttributeWith(ItemStack itemStack, Holder<Attribute> holder, EquipmentSlot equipmentSlot) {
		double d = this.getAttributes().hasAttribute(holder) ? this.getAttributeBaseValue(holder) : 0.0;
		ItemAttributeModifiers itemAttributeModifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		return itemAttributeModifiers.compute(d, equipmentSlot);
	}

	public boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.getDamageValue() < itemStack2.getDamageValue() ? true : hasAnyComponentExceptDamage(itemStack) && !hasAnyComponentExceptDamage(itemStack2);
	}

	private static boolean hasAnyComponentExceptDamage(ItemStack itemStack) {
		DataComponentMap dataComponentMap = itemStack.getComponents();
		int i = dataComponentMap.size();
		return i > 1 || i == 1 && !dataComponentMap.has(DataComponents.DAMAGE);
	}

	public boolean canHoldItem(ItemStack itemStack) {
		return true;
	}

	public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack itemStack) {
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
		if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
			this.discard();
		} else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
			Entity entity = this.level().getNearestPlayer(this, -1.0);
			if (entity != null) {
				double d = entity.distanceToSqr(this);
				int i = this.getType().getCategory().getDespawnDistance();
				int j = i * i;
				if (d > (double)j && this.removeWhenFarAway(d)) {
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
		} else {
			this.noActionTime = 0;
		}
	}

	@Override
	protected final void serverAiStep() {
		this.noActionTime++;
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("sensing");
		this.sensing.tick();
		profilerFiller.pop();
		int i = this.tickCount + this.getId();
		if (i % 2 != 0 && this.tickCount > 1) {
			profilerFiller.push("targetSelector");
			this.targetSelector.tickRunningGoals(false);
			profilerFiller.pop();
			profilerFiller.push("goalSelector");
			this.goalSelector.tickRunningGoals(false);
			profilerFiller.pop();
		} else {
			profilerFiller.push("targetSelector");
			this.targetSelector.tick();
			profilerFiller.pop();
			profilerFiller.push("goalSelector");
			this.goalSelector.tick();
			profilerFiller.pop();
		}

		profilerFiller.push("navigation");
		this.navigation.tick();
		profilerFiller.pop();
		profilerFiller.push("mob tick");
		this.customServerAiStep((ServerLevel)this.level());
		profilerFiller.pop();
		profilerFiller.push("controls");
		profilerFiller.push("move");
		this.moveControl.tick();
		profilerFiller.popPush("look");
		this.lookControl.tick();
		profilerFiller.popPush("jump");
		this.jumpControl.tick();
		profilerFiller.pop();
		profilerFiller.pop();
		this.sendDebugPackets();
	}

	protected void sendDebugPackets() {
		DebugPackets.sendGoalSelector(this.level(), this, this.goalSelector);
	}

	protected void customServerAiStep(ServerLevel serverLevel) {
	}

	public int getMaxHeadXRot() {
		return 40;
	}

	public int getMaxHeadYRot() {
		return 75;
	}

	protected void clampHeadRotationToBody() {
		float f = (float)this.getMaxHeadYRot();
		float g = this.getYHeadRot();
		float h = Mth.wrapDegrees(this.yBodyRot - g);
		float i = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - g), -f, f);
		float j = g + h - i;
		this.setYHeadRot(j);
	}

	public int getHeadRotSpeed() {
		return 10;
	}

	public void lookAt(Entity entity, float f, float g) {
		double d = entity.getX() - this.getX();
		double e = entity.getZ() - this.getZ();
		double h;
		if (entity instanceof LivingEntity livingEntity) {
			h = livingEntity.getEyeY() - this.getEyeY();
		} else {
			h = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
		}

		double i = Math.sqrt(d * d + e * e);
		float j = (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI) - 90.0F;
		float k = (float)(-(Mth.atan2(h, i) * 180.0F / (float)Math.PI));
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

	public static boolean checkMobSpawnRules(
		EntityType<? extends Mob> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource
	) {
		BlockPos blockPos2 = blockPos.below();
		return EntitySpawnReason.isSpawner(entitySpawnReason) || levelAccessor.getBlockState(blockPos2).isValidSpawn(levelAccessor, blockPos2, entityType);
	}

	public boolean checkSpawnRules(LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason) {
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
			return this.getComfortableFallDistance(0.0F);
		} else {
			int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
			i -= (3 - this.level().getDifficulty().getId()) * 4;
			if (i < 0) {
				i = 0;
			}

			return this.getComfortableFallDistance((float)i);
		}
	}

	@Override
	public Iterable<ItemStack> getHandSlots() {
		return this.handItems;
	}

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return this.armorItems;
	}

	public ItemStack getBodyArmorItem() {
		return this.bodyArmorItem;
	}

	@Override
	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return equipmentSlot != EquipmentSlot.BODY;
	}

	public boolean isWearingBodyArmor() {
		return !this.getItemBySlot(EquipmentSlot.BODY).isEmpty();
	}

	public void setBodyArmorItem(ItemStack itemStack) {
		this.setItemSlotAndDropWhenKilled(EquipmentSlot.BODY, itemStack);
	}

	@Override
	public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
		return (Iterable<ItemStack>)(this.bodyArmorItem.isEmpty() ? this.armorItems : Iterables.concat(this.armorItems, List.of(this.bodyArmorItem)));
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot.getType()) {
			case HAND -> (ItemStack)this.handItems.get(equipmentSlot.getIndex());
			case HUMANOID_ARMOR -> (ItemStack)this.armorItems.get(equipmentSlot.getIndex());
			case ANIMAL_ARMOR -> this.bodyArmorItem;
		};
	}

	@Override
	public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		this.verifyEquippedItem(itemStack);
		switch (equipmentSlot.getType()) {
			case HAND:
				this.onEquipItem(equipmentSlot, this.handItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
				break;
			case HUMANOID_ARMOR:
				this.onEquipItem(equipmentSlot, this.armorItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
				break;
			case ANIMAL_ARMOR:
				ItemStack itemStack2 = this.bodyArmorItem;
				this.bodyArmorItem = itemStack;
				this.onEquipItem(equipmentSlot, itemStack2, itemStack);
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
		super.dropCustomDeathLoot(serverLevel, damageSource, bl);

		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
			ItemStack itemStack = this.getItemBySlot(equipmentSlot);
			float f = this.getEquipmentDropChance(equipmentSlot);
			if (f != 0.0F) {
				boolean bl2 = f > 1.0F;
				Entity var11 = damageSource.getEntity();
				if (var11 instanceof LivingEntity) {
					LivingEntity livingEntity = (LivingEntity)var11;
					if (this.level() instanceof ServerLevel serverLevel2) {
						f = EnchantmentHelper.processEquipmentDropChance(serverLevel2, livingEntity, damageSource, f);
					}
				}

				if (!itemStack.isEmpty()
					&& !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
					&& (bl || bl2)
					&& this.random.nextFloat() < f) {
					if (!bl2 && itemStack.isDamageableItem()) {
						itemStack.setDamageValue(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
					}

					this.spawnAtLocation(serverLevel, itemStack);
					this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
				}
			}
		}
	}

	protected float getEquipmentDropChance(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot.getType()) {
			case HAND -> this.handDropChances[equipmentSlot.getIndex()];
			case HUMANOID_ARMOR -> this.armorDropChances[equipmentSlot.getIndex()];
			case ANIMAL_ARMOR -> this.bodyArmorDropChance;
		};
	}

	public void dropPreservedEquipment(ServerLevel serverLevel) {
		this.dropPreservedEquipment(serverLevel, itemStack -> true);
	}

	public Set<EquipmentSlot> dropPreservedEquipment(ServerLevel serverLevel, Predicate<ItemStack> predicate) {
		Set<EquipmentSlot> set = new HashSet();

		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
			ItemStack itemStack = this.getItemBySlot(equipmentSlot);
			if (!itemStack.isEmpty()) {
				if (!predicate.test(itemStack)) {
					set.add(equipmentSlot);
				} else {
					double d = (double)this.getEquipmentDropChance(equipmentSlot);
					if (d > 1.0) {
						this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
						this.spawnAtLocation(serverLevel, itemStack);
					}
				}
			}
		}

		return set;
	}

	private LootParams createEquipmentParams(ServerLevel serverLevel) {
		return new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, this.position())
			.withParameter(LootContextParams.THIS_ENTITY, this)
			.create(LootContextParamSets.EQUIPMENT);
	}

	public void equip(EquipmentTable equipmentTable) {
		this.equip(equipmentTable.lootTable(), equipmentTable.slotDropChances());
	}

	public void equip(ResourceKey<LootTable> resourceKey, Map<EquipmentSlot, Float> map) {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.equip(resourceKey, this.createEquipmentParams(serverLevel), map);
		}
	}

	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		if (randomSource.nextFloat() < 0.15F * difficultyInstance.getSpecialMultiplier()) {
			int i = randomSource.nextInt(2);
			float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
			if (randomSource.nextFloat() < 0.095F) {
				i++;
			}

			if (randomSource.nextFloat() < 0.095F) {
				i++;
			}

			if (randomSource.nextFloat() < 0.095F) {
				i++;
			}

			boolean bl = true;

			for (EquipmentSlot equipmentSlot : EQUIPMENT_POPULATION_ORDER) {
				ItemStack itemStack = this.getItemBySlot(equipmentSlot);
				if (!bl && randomSource.nextFloat() < f) {
					break;
				}

				bl = false;
				if (itemStack.isEmpty()) {
					Item item = getEquipmentForSlot(equipmentSlot, i);
					if (item != null) {
						this.setItemSlot(equipmentSlot, new ItemStack(item));
					}
				}
			}
		}
	}

	@Nullable
	public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int i) {
		switch (equipmentSlot) {
			case HEAD:
				if (i == 0) {
					return Items.LEATHER_HELMET;
				} else if (i == 1) {
					return Items.GOLDEN_HELMET;
				} else if (i == 2) {
					return Items.CHAINMAIL_HELMET;
				} else if (i == 3) {
					return Items.IRON_HELMET;
				} else if (i == 4) {
					return Items.DIAMOND_HELMET;
				}
			case CHEST:
				if (i == 0) {
					return Items.LEATHER_CHESTPLATE;
				} else if (i == 1) {
					return Items.GOLDEN_CHESTPLATE;
				} else if (i == 2) {
					return Items.CHAINMAIL_CHESTPLATE;
				} else if (i == 3) {
					return Items.IRON_CHESTPLATE;
				} else if (i == 4) {
					return Items.DIAMOND_CHESTPLATE;
				}
			case LEGS:
				if (i == 0) {
					return Items.LEATHER_LEGGINGS;
				} else if (i == 1) {
					return Items.GOLDEN_LEGGINGS;
				} else if (i == 2) {
					return Items.CHAINMAIL_LEGGINGS;
				} else if (i == 3) {
					return Items.IRON_LEGGINGS;
				} else if (i == 4) {
					return Items.DIAMOND_LEGGINGS;
				}
			case FEET:
				if (i == 0) {
					return Items.LEATHER_BOOTS;
				} else if (i == 1) {
					return Items.GOLDEN_BOOTS;
				} else if (i == 2) {
					return Items.CHAINMAIL_BOOTS;
				} else if (i == 3) {
					return Items.IRON_BOOTS;
				} else if (i == 4) {
					return Items.DIAMOND_BOOTS;
				}
			default:
				return null;
		}
	}

	protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
		this.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);

		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
			if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				this.enchantSpawnedArmor(serverLevelAccessor, randomSource, equipmentSlot, difficultyInstance);
			}
		}
	}

	protected void enchantSpawnedWeapon(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
		this.enchantSpawnedEquipment(serverLevelAccessor, EquipmentSlot.MAINHAND, randomSource, 0.25F, difficultyInstance);
	}

	protected void enchantSpawnedArmor(
		ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, EquipmentSlot equipmentSlot, DifficultyInstance difficultyInstance
	) {
		this.enchantSpawnedEquipment(serverLevelAccessor, equipmentSlot, randomSource, 0.5F, difficultyInstance);
	}

	private void enchantSpawnedEquipment(
		ServerLevelAccessor serverLevelAccessor, EquipmentSlot equipmentSlot, RandomSource randomSource, float f, DifficultyInstance difficultyInstance
	) {
		ItemStack itemStack = this.getItemBySlot(equipmentSlot);
		if (!itemStack.isEmpty() && randomSource.nextFloat() < f * difficultyInstance.getSpecialMultiplier()) {
			EnchantmentHelper.enchantItemFromProvider(
				itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource
			);
			this.setItemSlot(equipmentSlot, itemStack);
		}
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		AttributeInstance attributeInstance = (AttributeInstance)Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE));
		if (!attributeInstance.hasModifier(RANDOM_SPAWN_BONUS_ID)) {
			attributeInstance.addPermanentModifier(
				new AttributeModifier(RANDOM_SPAWN_BONUS_ID, randomSource.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
			);
		}

		this.setLeftHanded(randomSource.nextFloat() < 0.05F);
		return spawnGroupData;
	}

	public void setPersistenceRequired() {
		this.persistenceRequired = true;
	}

	@Override
	public void setDropChance(EquipmentSlot equipmentSlot, float f) {
		switch (equipmentSlot.getType()) {
			case HAND:
				this.handDropChances[equipmentSlot.getIndex()] = f;
				break;
			case HUMANOID_ARMOR:
				this.armorDropChances[equipmentSlot.getIndex()] = f;
				break;
			case ANIMAL_ARMOR:
				this.bodyArmorDropChance = f;
		}
	}

	@Override
	public boolean canPickUpLoot() {
		return this.canPickUpLoot;
	}

	public void setCanPickUpLoot(boolean bl) {
		this.canPickUpLoot = bl;
	}

	@Override
	protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
		return this.canPickUpLoot();
	}

	public boolean isPersistenceRequired() {
		return this.persistenceRequired;
	}

	@Override
	public final InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (!this.isAlive()) {
			return InteractionResult.PASS;
		} else {
			InteractionResult interactionResult = this.checkAndHandleImportantInteractions(player, interactionHand);
			if (interactionResult.consumesAction()) {
				this.gameEvent(GameEvent.ENTITY_INTERACT, player);
				return interactionResult;
			} else {
				InteractionResult interactionResult2 = super.interact(player, interactionHand);
				if (interactionResult2 != InteractionResult.PASS) {
					return interactionResult2;
				} else {
					interactionResult = this.mobInteract(player, interactionHand);
					if (interactionResult.consumesAction()) {
						this.gameEvent(GameEvent.ENTITY_INTERACT, player);
						return interactionResult;
					} else {
						return InteractionResult.PASS;
					}
				}
			}
		}
	}

	private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.is(Items.NAME_TAG)) {
			InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
			if (interactionResult.consumesAction()) {
				return interactionResult;
			}
		}

		if (itemStack.getItem() instanceof SpawnEggItem) {
			if (this.level() instanceof ServerLevel) {
				SpawnEggItem spawnEggItem = (SpawnEggItem)itemStack.getItem();
				Optional<Mob> optional = spawnEggItem.spawnOffspringFromSpawnEgg(
					player, this, (EntityType<? extends Mob>)this.getType(), (ServerLevel)this.level(), this.position(), itemStack
				);
				optional.ifPresent(mob -> this.onOffspringSpawnedFromEgg(player, mob));
				if (optional.isEmpty()) {
					return InteractionResult.PASS;
				}
			}

			return InteractionResult.SUCCESS_SERVER;
		} else {
			return InteractionResult.PASS;
		}
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
		return this.restrictRadius == -1.0F ? true : this.restrictCenter.distSqr(blockPos) < (double)(this.restrictRadius * this.restrictRadius);
	}

	public void restrictTo(BlockPos blockPos, int i) {
		this.restrictCenter = blockPos;
		this.restrictRadius = (float)i;
	}

	public BlockPos getRestrictCenter() {
		return this.restrictCenter;
	}

	public float getRestrictRadius() {
		return this.restrictRadius;
	}

	public void clearRestriction() {
		this.restrictRadius = -1.0F;
	}

	public boolean hasRestriction() {
		return this.restrictRadius != -1.0F;
	}

	@Nullable
	public <T extends Mob> T convertTo(
		EntityType<T> entityType, ConversionParams conversionParams, EntitySpawnReason entitySpawnReason, ConversionParams.AfterConversion<T> afterConversion
	) {
		if (this.isRemoved()) {
			return null;
		} else {
			T mob = (T)entityType.create(this.level(), entitySpawnReason);
			if (mob == null) {
				return null;
			} else {
				conversionParams.type().convert(this, mob, conversionParams);
				afterConversion.finalizeConversion(mob);
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.addFreshEntity(mob);
				}

				if (conversionParams.type().shouldDiscardAfterConversion()) {
					this.discard();
				}

				return mob;
			}
		}
	}

	@Nullable
	public <T extends Mob> T convertTo(EntityType<T> entityType, ConversionParams conversionParams, ConversionParams.AfterConversion<T> afterConversion) {
		return this.convertTo(entityType, conversionParams, EntitySpawnReason.CONVERSION, afterConversion);
	}

	@Nullable
	@Override
	public Leashable.LeashData getLeashData() {
		return this.leashData;
	}

	@Override
	public void setLeashData(@Nullable Leashable.LeashData leashData) {
		this.leashData = leashData;
	}

	@Override
	public void dropLeash(boolean bl, boolean bl2) {
		Leashable.super.dropLeash(bl, bl2);
		if (this.getLeashData() == null) {
			this.clearRestriction();
		}
	}

	@Override
	public void leashTooFarBehaviour() {
		Leashable.super.leashTooFarBehaviour();
		this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
	}

	@Override
	public boolean canBeLeashed() {
		return !(this instanceof Enemy);
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		boolean bl2 = super.startRiding(entity, bl);
		if (bl2 && this.isLeashed()) {
			this.dropLeash(true, true);
		}

		return bl2;
	}

	@Override
	public boolean isEffectiveAi() {
		return super.isEffectiveAi() && !this.isNoAi();
	}

	public void setNoAi(boolean bl) {
		byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
		this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 1) : (byte)(b & -2));
	}

	public void setLeftHanded(boolean bl) {
		byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
		this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 2) : (byte)(b & -3));
	}

	public void setAggressive(boolean bl) {
		byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
		this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 4) : (byte)(b & -5));
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

	public boolean isWithinMeleeAttackRange(LivingEntity livingEntity) {
		return this.getAttackBoundingBox().intersects(livingEntity.getHitbox());
	}

	protected AABB getAttackBoundingBox() {
		Entity entity = this.getVehicle();
		AABB aABB3;
		if (entity != null) {
			AABB aABB = entity.getBoundingBox();
			AABB aABB2 = this.getBoundingBox();
			aABB3 = new AABB(
				Math.min(aABB2.minX, aABB.minX), aABB2.minY, Math.min(aABB2.minZ, aABB.minZ), Math.max(aABB2.maxX, aABB.maxX), aABB2.maxY, Math.max(aABB2.maxZ, aABB.maxZ)
			);
		} else {
			aABB3 = this.getBoundingBox();
		}

		return aABB3.inflate(DEFAULT_ATTACK_REACH, 0.0, DEFAULT_ATTACK_REACH);
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
		float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
		ItemStack itemStack = this.getWeaponItem();
		DamageSource damageSource = (DamageSource)Optional.ofNullable(itemStack.getItem().getDamageSource(this)).orElse(this.damageSources().mobAttack(this));
		f = EnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, f);
		f += itemStack.getItem().getAttackDamageBonus(entity, f, damageSource);
		boolean bl = entity.hurtServer(serverLevel, damageSource, f);
		if (bl) {
			float g = this.getKnockback(entity, damageSource);
			if (g > 0.0F && entity instanceof LivingEntity livingEntity) {
				livingEntity.knockback(
					(double)(g * 0.5F), (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)))
				);
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
			}

			if (entity instanceof LivingEntity livingEntity) {
				itemStack.hurtEnemy(livingEntity, this);
			}

			EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
			this.setLastHurtMob(entity);
			this.playAttackSound();
		}

		return bl;
	}

	protected void playAttackSound() {
	}

	protected boolean isSunBurnTick() {
		if (this.level().isDay() && !this.level().isClientSide) {
			float f = this.getLightLevelDependentMagicValue();
			BlockPos blockPos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
			boolean bl = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
			if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !bl && this.level().canSeeSky(blockPos)) {
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

	@VisibleForTesting
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
		this.getAllSlots().forEach(itemStack -> {
			if (!itemStack.isEmpty()) {
				itemStack.setCount(0);
			}
		});
	}

	@Nullable
	@Override
	public ItemStack getPickResult() {
		SpawnEggItem spawnEggItem = SpawnEggItem.byId(this.getType());
		return spawnEggItem == null ? null : new ItemStack(spawnEggItem);
	}

	@Override
	protected void onAttributeUpdated(Holder<Attribute> holder) {
		super.onAttributeUpdated(holder);
		if (holder.is(Attributes.FOLLOW_RANGE) || holder.is(Attributes.TEMPT_RANGE)) {
			this.getNavigation().updatePathfinderMaxVisitedNodes();
		}
	}

	@VisibleForTesting
	public float[] getHandDropChances() {
		return this.handDropChances;
	}

	@VisibleForTesting
	public float[] getArmorDropChances() {
		return this.armorDropChances;
	}
}
