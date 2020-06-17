package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class Mob extends LivingEntity {
	private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
	public int ambientSoundTime;
	protected int xpReward;
	protected LookControl lookControl;
	protected MoveControl moveControl;
	protected JumpControl jumpControl;
	private final BodyRotationControl bodyRotationControl;
	protected PathNavigation navigation;
	protected final GoalSelector goalSelector;
	protected final GoalSelector targetSelector;
	private LivingEntity target;
	private final Sensing sensing;
	private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
	protected final float[] handDropChances = new float[2];
	private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
	protected final float[] armorDropChances = new float[4];
	private boolean canPickUpLoot;
	private boolean persistenceRequired;
	private final Map<BlockPathTypes, Float> pathfindingMalus = Maps.newEnumMap(BlockPathTypes.class);
	private ResourceLocation lootTable;
	private long lootTableSeed;
	@Nullable
	private Entity leashHolder;
	private int delayedLeashHolderId;
	@Nullable
	private CompoundTag leashInfoTag;
	private BlockPos restrictCenter = BlockPos.ZERO;
	private float restrictRadius = -1.0F;

	protected Mob(EntityType<? extends Mob> entityType, Level level) {
		super(entityType, level);
		this.goalSelector = new GoalSelector(level.getProfilerSupplier());
		this.targetSelector = new GoalSelector(level.getProfilerSupplier());
		this.lookControl = new LookControl(this);
		this.moveControl = new MoveControl(this);
		this.jumpControl = new JumpControl(this);
		this.bodyRotationControl = this.createBodyControl();
		this.navigation = this.createNavigation(level);
		this.sensing = new Sensing(this);
		Arrays.fill(this.armorDropChances, 0.085F);
		Arrays.fill(this.handDropChances, 0.085F);
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
		Mob mob;
		if (this.getVehicle() instanceof Mob && ((Mob)this.getVehicle()).shouldPassengersInheritMalus()) {
			mob = (Mob)this.getVehicle();
		} else {
			mob = this;
		}

		Float float_ = (Float)mob.pathfindingMalus.get(blockPathTypes);
		return float_ == null ? blockPathTypes.getMalus() : float_;
	}

	public void setPathfindingMalus(BlockPathTypes blockPathTypes, float f) {
		this.pathfindingMalus.put(blockPathTypes, f);
	}

	public boolean canCutCorner(BlockPathTypes blockPathTypes) {
		return blockPathTypes != BlockPathTypes.DANGER_FIRE && blockPathTypes != BlockPathTypes.DANGER_CACTUS && blockPathTypes != BlockPathTypes.DANGER_OTHER;
	}

	protected BodyRotationControl createBodyControl() {
		return new BodyRotationControl(this);
	}

	public LookControl getLookControl() {
		return this.lookControl;
	}

	public MoveControl getMoveControl() {
		if (this.isPassenger() && this.getVehicle() instanceof Mob) {
			Mob mob = (Mob)this.getVehicle();
			return mob.getMoveControl();
		} else {
			return this.moveControl;
		}
	}

	public JumpControl getJumpControl() {
		return this.jumpControl;
	}

	public PathNavigation getNavigation() {
		if (this.isPassenger() && this.getVehicle() instanceof Mob) {
			Mob mob = (Mob)this.getVehicle();
			return mob.getNavigation();
		} else {
			return this.navigation;
		}
	}

	public Sensing getSensing() {
		return this.sensing;
	}

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
	protected int getExperienceReward(Player player) {
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

			return i;
		} else {
			return this.xpReward;
		}
	}

	public void spawnAnim() {
		if (this.level.isClientSide) {
			for (int i = 0; i < 20; i++) {
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

	@Environment(EnvType.CLIENT)
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

		for (ItemStack itemStack2 : this.handItems) {
			CompoundTag compoundTag3 = new CompoundTag();
			if (!itemStack2.isEmpty()) {
				itemStack2.save(compoundTag3);
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
			CompoundTag compoundTag3 = new CompoundTag();
			if (this.leashHolder instanceof LivingEntity) {
				UUID uUID = this.leashHolder.getUUID();
				compoundTag3.putUUID("UUID", uUID);
			} else if (this.leashHolder instanceof HangingEntity) {
				BlockPos blockPos = ((HangingEntity)this.leashHolder).getPos();
				compoundTag3.putInt("X", blockPos.getX());
				compoundTag3.putInt("Y", blockPos.getY());
				compoundTag3.putInt("Z", blockPos.getZ());
			}

			compoundTag.put("Leash", compoundTag3);
		} else if (this.leashInfoTag != null) {
			compoundTag.put("Leash", this.leashInfoTag.copy());
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
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("CanPickUpLoot", 1)) {
			this.setCanPickUpLoot(compoundTag.getBoolean("CanPickUpLoot"));
		}

		this.persistenceRequired = compoundTag.getBoolean("PersistenceRequired");
		if (compoundTag.contains("ArmorItems", 9)) {
			ListTag listTag = compoundTag.getList("ArmorItems", 10);

			for (int i = 0; i < this.armorItems.size(); i++) {
				this.armorItems.set(i, ItemStack.of(listTag.getCompound(i)));
			}
		}

		if (compoundTag.contains("HandItems", 9)) {
			ListTag listTag = compoundTag.getList("HandItems", 10);

			for (int i = 0; i < this.handItems.size(); i++) {
				this.handItems.set(i, ItemStack.of(listTag.getCompound(i)));
			}
		}

		if (compoundTag.contains("ArmorDropChances", 9)) {
			ListTag listTag = compoundTag.getList("ArmorDropChances", 5);

			for (int i = 0; i < listTag.size(); i++) {
				this.armorDropChances[i] = listTag.getFloat(i);
			}
		}

		if (compoundTag.contains("HandDropChances", 9)) {
			ListTag listTag = compoundTag.getList("HandDropChances", 5);

			for (int i = 0; i < listTag.size(); i++) {
				this.handDropChances[i] = listTag.getFloat(i);
			}
		}

		if (compoundTag.contains("Leash", 10)) {
			this.leashInfoTag = compoundTag.getCompound("Leash");
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
			for (ItemEntity itemEntity : this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0, 0.0, 1.0))) {
				if (!itemEntity.removed && !itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay() && this.wantsToPickUp(itemEntity.getItem())) {
					this.pickUpItem(itemEntity);
				}
			}
		}

		this.level.getProfiler().pop();
	}

	protected void pickUpItem(ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		if (this.equipItemIfPossible(itemStack)) {
			this.onItemPickup(itemEntity);
			this.take(itemEntity, itemStack.getCount());
			itemEntity.remove();
		}
	}

	public boolean equipItemIfPossible(ItemStack itemStack) {
		EquipmentSlot equipmentSlot = getEquipmentSlotForItem(itemStack);
		ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
		boolean bl = this.canReplaceCurrentItem(itemStack, itemStack2);
		if (bl && this.canHoldItem(itemStack)) {
			double d = (double)this.getEquipmentDropChance(equipmentSlot);
			if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d) {
				this.spawnAtLocation(itemStack2);
			}

			this.setItemSlotAndDropWhenKilled(equipmentSlot, itemStack);
			this.playEquipSound(itemStack);
			return true;
		} else {
			return false;
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
			case ARMOR:
				this.armorDropChances[equipmentSlot.getIndex()] = 2.0F;
		}
	}

	protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack2.isEmpty()) {
			return true;
		} else if (itemStack.getItem() instanceof SwordItem) {
			if (!(itemStack2.getItem() instanceof SwordItem)) {
				return true;
			} else {
				SwordItem swordItem = (SwordItem)itemStack.getItem();
				SwordItem swordItem2 = (SwordItem)itemStack2.getItem();
				return swordItem.getDamage() != swordItem2.getDamage() ? swordItem.getDamage() > swordItem2.getDamage() : this.canReplaceEqualItem(itemStack, itemStack2);
			}
		} else if (itemStack.getItem() instanceof BowItem && itemStack2.getItem() instanceof BowItem) {
			return this.canReplaceEqualItem(itemStack, itemStack2);
		} else if (itemStack.getItem() instanceof CrossbowItem && itemStack2.getItem() instanceof CrossbowItem) {
			return this.canReplaceEqualItem(itemStack, itemStack2);
		} else if (itemStack.getItem() instanceof ArmorItem) {
			if (EnchantmentHelper.hasBindingCurse(itemStack2)) {
				return false;
			} else if (!(itemStack2.getItem() instanceof ArmorItem)) {
				return true;
			} else {
				ArmorItem armorItem = (ArmorItem)itemStack.getItem();
				ArmorItem armorItem2 = (ArmorItem)itemStack2.getItem();
				if (armorItem.getDefense() != armorItem2.getDefense()) {
					return armorItem.getDefense() > armorItem2.getDefense();
				} else {
					return armorItem.getToughness() != armorItem2.getToughness()
						? armorItem.getToughness() > armorItem2.getToughness()
						: this.canReplaceEqualItem(itemStack, itemStack2);
				}
			}
		} else {
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
	}

	public boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack.getDamageValue() >= itemStack2.getDamageValue() && (!itemStack.hasTag() || itemStack2.hasTag())) {
			return itemStack.hasTag() && itemStack2.hasTag()
				? itemStack.getTag().getAllKeys().stream().anyMatch(string -> !string.equals("Damage"))
					&& !itemStack2.getTag().getAllKeys().stream().anyMatch(string -> !string.equals("Damage"))
				: false;
		} else {
			return true;
		}
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
			this.remove();
		} else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
			Entity entity = this.level.getNearestPlayer(this, -1.0);
			if (entity != null) {
				double d = entity.distanceToSqr(this);
				int i = this.getType().getCategory().getDespawnDistance();
				int j = i * i;
				if (d > (double)j && this.removeWhenFarAway(d)) {
					this.remove();
				}

				int k = this.getType().getCategory().getNoDespawnDistance();
				int l = k * k;
				if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.removeWhenFarAway(d)) {
					this.remove();
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
		this.level.getProfiler().push("sensing");
		this.sensing.tick();
		this.level.getProfiler().pop();
		this.level.getProfiler().push("targetSelector");
		this.targetSelector.tick();
		this.level.getProfiler().pop();
		this.level.getProfiler().push("goalSelector");
		this.goalSelector.tick();
		this.level.getProfiler().pop();
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
		double d = entity.getX() - this.getX();
		double e = entity.getZ() - this.getZ();
		double h;
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)entity;
			h = livingEntity.getEyeY() - this.getEyeY();
		} else {
			h = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
		}

		double i = (double)Mth.sqrt(d * d + e * e);
		float j = (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI) - 90.0F;
		float k = (float)(-(Mth.atan2(h, i) * 180.0F / (float)Math.PI));
		this.xRot = this.rotlerp(this.xRot, k, g);
		this.yRot = this.rotlerp(this.yRot, j, f);
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
		EntityType<? extends Mob> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
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
		} else {
			int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
			i -= (3 - this.level.getDifficulty().getId()) * 4;
			if (i < 0) {
				i = 0;
			}

			return i + 3;
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

	@Override
	public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
		switch (equipmentSlot.getType()) {
			case HAND:
				return this.handItems.get(equipmentSlot.getIndex());
			case ARMOR:
				return this.armorItems.get(equipmentSlot.getIndex());
			default:
				return ItemStack.EMPTY;
		}
	}

	@Override
	public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		switch (equipmentSlot.getType()) {
			case HAND:
				this.handItems.set(equipmentSlot.getIndex(), itemStack);
				break;
			case ARMOR:
				this.armorItems.set(equipmentSlot.getIndex(), itemStack);
		}
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			ItemStack itemStack = this.getItemBySlot(equipmentSlot);
			float f = this.getEquipmentDropChance(equipmentSlot);
			boolean bl2 = f > 1.0F;
			if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack) && (bl || bl2) && Math.max(this.random.nextFloat() - (float)i * 0.01F, 0.0F) < f
				)
			 {
				if (!bl2 && itemStack.isDamageableItem()) {
					itemStack.setDamageValue(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
				}

				this.spawnAtLocation(itemStack);
			}
		}
	}

	protected float getEquipmentDropChance(EquipmentSlot equipmentSlot) {
		float f;
		switch (equipmentSlot.getType()) {
			case HAND:
				f = this.handDropChances[equipmentSlot.getIndex()];
				break;
			case ARMOR:
				f = this.armorDropChances[equipmentSlot.getIndex()];
				break;
			default:
				f = 0.0F;
		}

		return f;
	}

	protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
		if (this.random.nextFloat() < 0.15F * difficultyInstance.getSpecialMultiplier()) {
			int i = this.random.nextInt(2);
			float f = this.level.getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
			if (this.random.nextFloat() < 0.095F) {
				i++;
			}

			if (this.random.nextFloat() < 0.095F) {
				i++;
			}

			if (this.random.nextFloat() < 0.095F) {
				i++;
			}

			boolean bl = true;

			for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
				if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
					ItemStack itemStack = this.getItemBySlot(equipmentSlot);
					if (!bl && this.random.nextFloat() < f) {
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
	}

	public static EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item != Blocks.CARVED_PUMPKIN.asItem() && (!(item instanceof BlockItem) || !(((BlockItem)item).getBlock() instanceof AbstractSkullBlock))) {
			if (item instanceof ArmorItem) {
				return ((ArmorItem)item).getSlot();
			} else if (item == Items.ELYTRA) {
				return EquipmentSlot.CHEST;
			} else {
				return item == Items.SHIELD ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
			}
		} else {
			return EquipmentSlot.HEAD;
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

	protected void populateDefaultEquipmentEnchantments(DifficultyInstance difficultyInstance) {
		float f = difficultyInstance.getSpecialMultiplier();
		if (!this.getMainHandItem().isEmpty() && this.random.nextFloat() < 0.25F * f) {
			this.setItemSlot(
				EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(this.random, this.getMainHandItem(), (int)(5.0F + f * (float)this.random.nextInt(18)), false)
			);
		}

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
				ItemStack itemStack = this.getItemBySlot(equipmentSlot);
				if (!itemStack.isEmpty() && this.random.nextFloat() < 0.5F * f) {
					this.setItemSlot(equipmentSlot, EnchantmentHelper.enchantItem(this.random, itemStack, (int)(5.0F + f * (float)this.random.nextInt(18)), false));
				}
			}
		}
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		this.getAttribute(Attributes.FOLLOW_RANGE)
			.addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
		if (this.random.nextFloat() < 0.05F) {
			this.setLeftHanded(true);
		} else {
			this.setLeftHanded(false);
		}

		return spawnGroupData;
	}

	public boolean canBeControlledByRider() {
		return false;
	}

	public void setPersistenceRequired() {
		this.persistenceRequired = true;
	}

	public void setDropChance(EquipmentSlot equipmentSlot, float f) {
		switch (equipmentSlot.getType()) {
			case HAND:
				this.handDropChances[equipmentSlot.getIndex()] = f;
				break;
			case ARMOR:
				this.armorDropChances[equipmentSlot.getIndex()] = f;
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
		EquipmentSlot equipmentSlot = getEquipmentSlotForItem(itemStack);
		return this.getItemBySlot(equipmentSlot).isEmpty() && this.canPickUpLoot();
	}

	public boolean isPersistenceRequired() {
		return this.persistenceRequired;
	}

	@Override
	public final InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (!this.isAlive()) {
			return InteractionResult.PASS;
		} else if (this.getLeashHolder() == player) {
			this.dropLeash(true, !player.abilities.instabuild);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			InteractionResult interactionResult = this.checkAndHandleImportantInteractions(player, interactionHand);
			if (interactionResult.consumesAction()) {
				return interactionResult;
			} else {
				interactionResult = this.mobInteract(player, interactionHand);
				return interactionResult.consumesAction() ? interactionResult : super.interact(player, interactionHand);
			}
		}
	}

	private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() == Items.LEAD && this.canBeLeashed(player)) {
			this.setLeashedTo(player, true);
			itemStack.shrink(1);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			if (itemStack.getItem() == Items.NAME_TAG) {
				InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
				if (interactionResult.consumesAction()) {
					return interactionResult;
				}
			}

			if (itemStack.getItem() instanceof SpawnEggItem) {
				if (!this.level.isClientSide) {
					SpawnEggItem spawnEggItem = (SpawnEggItem)itemStack.getItem();
					Optional<Mob> optional = spawnEggItem.spawnOffspringFromSpawnEgg(
						player, this, (EntityType<? extends Mob>)this.getType(), this.level, this.position(), itemStack
					);
					optional.ifPresent(mob -> this.onOffspringSpawnedFromEgg(player, mob));
					return optional.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
				} else {
					return InteractionResult.CONSUME;
				}
			} else {
				return InteractionResult.PASS;
			}
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

	public boolean hasRestriction() {
		return this.restrictRadius != -1.0F;
	}

	@Nullable
	protected <T extends Mob> T convertTo(EntityType<T> entityType) {
		if (this.removed) {
			return null;
		} else {
			T mob = (T)entityType.create(this.level);
			mob.copyPosition(this);
			mob.setCanPickUpLoot(this.canPickUpLoot());
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

			for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
				ItemStack itemStack = this.getItemBySlot(equipmentSlot);
				if (!itemStack.isEmpty()) {
					mob.setItemSlot(equipmentSlot, itemStack.copy());
					mob.setDropChance(equipmentSlot, this.getEquipmentDropChance(equipmentSlot));
					itemStack.setCount(0);
				}
			}

			this.level.addFreshEntity(mob);
			this.remove();
			return mob;
		}
	}

	protected void tickLeash() {
		if (this.leashInfoTag != null) {
			this.restoreLeashFromSave();
		}

		if (this.leashHolder != null) {
			if (!this.isAlive() || !this.leashHolder.isAlive()) {
				this.dropLeash(true, true);
			}
		}
	}

	public void dropLeash(boolean bl, boolean bl2) {
		if (this.leashHolder != null) {
			this.forcedLoading = false;
			if (!(this.leashHolder instanceof Player)) {
				this.leashHolder.forcedLoading = false;
			}

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
		this.forcedLoading = true;
		if (!(this.leashHolder instanceof Player)) {
			this.leashHolder.forcedLoading = true;
		}

		if (!this.level.isClientSide && bl && this.level instanceof ServerLevel) {
			((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, this.leashHolder));
		}

		if (this.isPassenger()) {
			this.stopRiding();
		}
	}

	@Environment(EnvType.CLIENT)
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
				BlockPos blockPos = new BlockPos(this.leashInfoTag.getInt("X"), this.leashInfoTag.getInt("Y"), this.leashInfoTag.getInt("Z"));
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
	public boolean setSlot(int i, ItemStack itemStack) {
		EquipmentSlot equipmentSlot;
		if (i == 98) {
			equipmentSlot = EquipmentSlot.MAINHAND;
		} else if (i == 99) {
			equipmentSlot = EquipmentSlot.OFFHAND;
		} else if (i == 100 + EquipmentSlot.HEAD.getIndex()) {
			equipmentSlot = EquipmentSlot.HEAD;
		} else if (i == 100 + EquipmentSlot.CHEST.getIndex()) {
			equipmentSlot = EquipmentSlot.CHEST;
		} else if (i == 100 + EquipmentSlot.LEGS.getIndex()) {
			equipmentSlot = EquipmentSlot.LEGS;
		} else {
			if (i != 100 + EquipmentSlot.FEET.getIndex()) {
				return false;
			}

			equipmentSlot = EquipmentSlot.FEET;
		}

		if (!itemStack.isEmpty() && !isValidSlotForItem(equipmentSlot, itemStack) && equipmentSlot != EquipmentSlot.HEAD) {
			return false;
		} else {
			this.setItemSlot(equipmentSlot, itemStack);
			return true;
		}
	}

	@Override
	public boolean isControlledByLocalInstance() {
		return this.canBeControlledByRider() && super.isControlledByLocalInstance();
	}

	public static boolean isValidSlotForItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		EquipmentSlot equipmentSlot2 = getEquipmentSlotForItem(itemStack);
		return equipmentSlot2 == equipmentSlot
			|| equipmentSlot2 == EquipmentSlot.MAINHAND && equipmentSlot == EquipmentSlot.OFFHAND
			|| equipmentSlot2 == EquipmentSlot.OFFHAND && equipmentSlot == EquipmentSlot.MAINHAND;
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

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return livingEntity.getType() == EntityType.PLAYER && ((Player)livingEntity).abilities.invulnerable ? false : super.canAttack(livingEntity);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float g = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		if (entity instanceof LivingEntity) {
			f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entity).getMobType());
			g += (float)EnchantmentHelper.getKnockbackBonus(this);
		}

		int i = EnchantmentHelper.getFireAspect(this);
		if (i > 0) {
			entity.setSecondsOnFire(i * 4);
		}

		boolean bl = entity.hurt(DamageSource.mobAttack(this), f);
		if (bl) {
			if (g > 0.0F && entity instanceof LivingEntity) {
				((LivingEntity)entity)
					.knockback(g * 0.5F, (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0))));
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
		if (!itemStack.isEmpty() && !itemStack2.isEmpty() && itemStack.getItem() instanceof AxeItem && itemStack2.getItem() == Items.SHIELD) {
			float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
			if (this.random.nextFloat() < f) {
				player.getCooldowns().addCooldown(Items.SHIELD, 100);
				this.level.broadcastEntityEvent(player, (byte)30);
			}
		}
	}

	protected boolean isSunBurnTick() {
		if (this.level.isDay() && !this.level.isClientSide) {
			float f = this.getBrightness();
			BlockPos blockPos = this.getVehicle() instanceof Boat
				? new BlockPos(this.getX(), (double)Math.round(this.getY()), this.getZ()).above()
				: new BlockPos(this.getX(), (double)Math.round(this.getY()), this.getZ());
			if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.level.canSeeSky(blockPos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void jumpInLiquid(Tag<Fluid> tag) {
		if (this.getNavigation().canFloat()) {
			super.jumpInLiquid(tag);
		} else {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
		}
	}

	@Override
	protected void removeAfterChangingDimensions() {
		super.removeAfterChangingDimensions();
		this.dropLeash(true, false);
	}
}
