package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class Player extends LivingEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final HumanoidArm DEFAULT_MAIN_HAND = HumanoidArm.RIGHT;
	public static final int DEFAULT_MODEL_CUSTOMIZATION = 0;
	public static final int MAX_HEALTH = 20;
	public static final int SLEEP_DURATION = 100;
	public static final int WAKE_UP_DURATION = 10;
	public static final int ENDER_SLOT_OFFSET = 200;
	public static final int HELD_ITEM_SLOT = 499;
	public static final int CRAFTING_SLOT_OFFSET = 500;
	public static final float DEFAULT_BLOCK_INTERACTION_RANGE = 4.5F;
	public static final float DEFAULT_ENTITY_INTERACTION_RANGE = 3.0F;
	public static final float CROUCH_BB_HEIGHT = 1.5F;
	public static final float SWIMMING_BB_WIDTH = 0.6F;
	public static final float SWIMMING_BB_HEIGHT = 0.6F;
	public static final float DEFAULT_EYE_HEIGHT = 1.62F;
	private static final int CURRENT_IMPULSE_CONTEXT_RESET_GRACE_TIME_TICKS = 40;
	public static final Vec3 DEFAULT_VEHICLE_ATTACHMENT = new Vec3(0.0, 0.6, 0.0);
	public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F)
		.withEyeHeight(1.62F)
		.withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT));
	private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder()
		.put(Pose.STANDING, STANDING_DIMENSIONS)
		.put(Pose.SLEEPING, SLEEPING_DIMENSIONS)
		.put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
		.put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
		.put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F))
		.put(
			Pose.CROUCHING,
			EntityDimensions.scalable(0.6F, 1.5F)
				.withEyeHeight(1.27F)
				.withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, DEFAULT_VEHICLE_ATTACHMENT))
		)
		.put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(1.62F))
		.build();
	private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_SCORE_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	private long timeEntitySatOnShoulder;
	final Inventory inventory = new Inventory(this);
	protected PlayerEnderChestContainer enderChestInventory = new PlayerEnderChestContainer();
	public final InventoryMenu inventoryMenu;
	public AbstractContainerMenu containerMenu;
	protected FoodData foodData = new FoodData();
	protected int jumpTriggerTime;
	public float oBob;
	public float bob;
	public int takeXpDelay;
	public double xCloakO;
	public double yCloakO;
	public double zCloakO;
	public double xCloak;
	public double yCloak;
	public double zCloak;
	private int sleepCounter;
	protected boolean wasUnderwater;
	private final Abilities abilities = new Abilities();
	public int experienceLevel;
	public int totalExperience;
	public float experienceProgress;
	protected int enchantmentSeed;
	protected final float defaultFlySpeed = 0.02F;
	private int lastLevelUpTime;
	private final GameProfile gameProfile;
	private boolean reducedDebugInfo;
	private ItemStack lastItemInMainHand = ItemStack.EMPTY;
	private final ItemCooldowns cooldowns = this.createItemCooldowns();
	private Optional<GlobalPos> lastDeathLocation = Optional.empty();
	@Nullable
	public FishingHook fishing;
	protected float hurtDir;
	@Nullable
	public Vec3 currentImpulseImpactPos;
	@Nullable
	public Entity currentExplosionCause;
	private boolean ignoreFallDamageFromCurrentImpulse;
	private int currentImpulseContextResetGraceTime;

	public Player(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(EntityType.PLAYER, level);
		this.setUUID(gameProfile.getId());
		this.gameProfile = gameProfile;
		this.inventoryMenu = new InventoryMenu(this.inventory, !level.isClientSide, this);
		this.containerMenu = this.inventoryMenu;
		this.moveTo((double)blockPos.getX() + 0.5, (double)(blockPos.getY() + 1), (double)blockPos.getZ() + 0.5, f, 0.0F);
		this.rotOffs = 180.0F;
	}

	public boolean blockActionRestricted(Level level, BlockPos blockPos, GameType gameType) {
		if (!gameType.isBlockPlacingRestricted()) {
			return false;
		} else if (gameType == GameType.SPECTATOR) {
			return true;
		} else if (this.mayBuild()) {
			return false;
		} else {
			ItemStack itemStack = this.getMainHandItem();
			return itemStack.isEmpty() || !itemStack.canBreakBlockInAdventureMode(new BlockInWorld(level, blockPos, false));
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.MOVEMENT_SPEED, 0.1F)
			.add(Attributes.ATTACK_SPEED)
			.add(Attributes.LUCK)
			.add(Attributes.BLOCK_INTERACTION_RANGE, 4.5)
			.add(Attributes.ENTITY_INTERACTION_RANGE, 3.0)
			.add(Attributes.BLOCK_BREAK_SPEED)
			.add(Attributes.SUBMERGED_MINING_SPEED)
			.add(Attributes.SNEAKING_SPEED)
			.add(Attributes.MINING_EFFICIENCY)
			.add(Attributes.SWEEPING_DAMAGE_RATIO);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_PLAYER_ABSORPTION_ID, 0.0F);
		builder.define(DATA_SCORE_ID, 0);
		builder.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
		builder.define(DATA_PLAYER_MAIN_HAND, (byte)DEFAULT_MAIN_HAND.getId());
		builder.define(DATA_SHOULDER_LEFT, new CompoundTag());
		builder.define(DATA_SHOULDER_RIGHT, new CompoundTag());
	}

	@Override
	public void tick() {
		this.noPhysics = this.isSpectator();
		if (this.isSpectator()) {
			this.setOnGround(false);
		}

		if (this.takeXpDelay > 0) {
			this.takeXpDelay--;
		}

		if (this.isSleeping()) {
			this.sleepCounter++;
			if (this.sleepCounter > 100) {
				this.sleepCounter = 100;
			}

			if (!this.level().isClientSide && this.level().isDay()) {
				this.stopSleepInBed(false, true);
			}
		} else if (this.sleepCounter > 0) {
			this.sleepCounter++;
			if (this.sleepCounter >= 110) {
				this.sleepCounter = 0;
			}
		}

		this.updateIsUnderwater();
		super.tick();
		if (!this.level().isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
			this.closeContainer();
			this.containerMenu = this.inventoryMenu;
		}

		this.moveCloak();
		if (!this.level().isClientSide) {
			this.foodData.tick(this);
			this.awardStat(Stats.PLAY_TIME);
			this.awardStat(Stats.TOTAL_WORLD_TIME);
			if (this.isAlive()) {
				this.awardStat(Stats.TIME_SINCE_DEATH);
			}

			if (this.isDiscrete()) {
				this.awardStat(Stats.CROUCH_TIME);
			}

			if (!this.isSleeping()) {
				this.awardStat(Stats.TIME_SINCE_REST);
			}
		}

		int i = 29999999;
		double d = Mth.clamp(this.getX(), -2.9999999E7, 2.9999999E7);
		double e = Mth.clamp(this.getZ(), -2.9999999E7, 2.9999999E7);
		if (d != this.getX() || e != this.getZ()) {
			this.setPos(d, this.getY(), e);
		}

		this.attackStrengthTicker++;
		ItemStack itemStack = this.getMainHandItem();
		if (!ItemStack.matches(this.lastItemInMainHand, itemStack)) {
			if (!ItemStack.isSameItem(this.lastItemInMainHand, itemStack)) {
				this.resetAttackStrengthTicker();
			}

			this.lastItemInMainHand = itemStack.copy();
		}

		if (!this.isEyeInFluid(FluidTags.WATER) && this.isEquipped(Items.TURTLE_HELMET)) {
			this.turtleHelmetTick();
		}

		this.cooldowns.tick();
		this.updatePlayerPose();
		if (this.currentImpulseContextResetGraceTime > 0) {
			this.currentImpulseContextResetGraceTime--;
		}
	}

	@Override
	protected float getMaxHeadRotationRelativeToBody() {
		return this.isBlocking() ? 15.0F : super.getMaxHeadRotationRelativeToBody();
	}

	public boolean isSecondaryUseActive() {
		return this.isShiftKeyDown();
	}

	protected boolean wantsToStopRiding() {
		return this.isShiftKeyDown();
	}

	protected boolean isStayingOnGroundSurface() {
		return this.isShiftKeyDown();
	}

	protected boolean updateIsUnderwater() {
		this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
		return this.wasUnderwater;
	}

	@Override
	public void onAboveBubbleCol(boolean bl) {
		if (!this.getAbilities().flying) {
			super.onAboveBubbleCol(bl);
		}
	}

	@Override
	public void onInsideBubbleColumn(boolean bl) {
		if (!this.getAbilities().flying) {
			super.onInsideBubbleColumn(bl);
		}
	}

	private void turtleHelmetTick() {
		this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
	}

	private boolean isEquipped(Item item) {
		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
			ItemStack itemStack = this.getItemBySlot(equipmentSlot);
			Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
			if (itemStack.is(item) && equippable != null && equippable.slot() == equipmentSlot) {
				return true;
			}
		}

		return false;
	}

	protected ItemCooldowns createItemCooldowns() {
		return new ItemCooldowns();
	}

	private void moveCloak() {
		this.xCloakO = this.xCloak;
		this.yCloakO = this.yCloak;
		this.zCloakO = this.zCloak;
		double d = this.getX() - this.xCloak;
		double e = this.getY() - this.yCloak;
		double f = this.getZ() - this.zCloak;
		double g = 10.0;
		if (d > 10.0) {
			this.xCloak = this.getX();
			this.xCloakO = this.xCloak;
		}

		if (f > 10.0) {
			this.zCloak = this.getZ();
			this.zCloakO = this.zCloak;
		}

		if (e > 10.0) {
			this.yCloak = this.getY();
			this.yCloakO = this.yCloak;
		}

		if (d < -10.0) {
			this.xCloak = this.getX();
			this.xCloakO = this.xCloak;
		}

		if (f < -10.0) {
			this.zCloak = this.getZ();
			this.zCloakO = this.zCloak;
		}

		if (e < -10.0) {
			this.yCloak = this.getY();
			this.yCloakO = this.yCloak;
		}

		this.xCloak += d * 0.25;
		this.zCloak += f * 0.25;
		this.yCloak += e * 0.25;
	}

	protected void updatePlayerPose() {
		if (this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.SWIMMING)) {
			Pose pose;
			if (this.isFallFlying()) {
				pose = Pose.FALL_FLYING;
			} else if (this.isSleeping()) {
				pose = Pose.SLEEPING;
			} else if (this.isSwimming()) {
				pose = Pose.SWIMMING;
			} else if (this.isAutoSpinAttack()) {
				pose = Pose.SPIN_ATTACK;
			} else if (this.isShiftKeyDown() && !this.abilities.flying) {
				pose = Pose.CROUCHING;
			} else {
				pose = Pose.STANDING;
			}

			Pose pose2;
			if (this.isSpectator() || this.isPassenger() || this.canPlayerFitWithinBlocksAndEntitiesWhen(pose)) {
				pose2 = pose;
			} else if (this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.CROUCHING)) {
				pose2 = Pose.CROUCHING;
			} else {
				pose2 = Pose.SWIMMING;
			}

			this.setPose(pose2);
		}
	}

	protected boolean canPlayerFitWithinBlocksAndEntitiesWhen(Pose pose) {
		return this.level().noCollision(this, this.getDimensions(pose).makeBoundingBox(this.position()).deflate(1.0E-7));
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.PLAYER_SWIM;
	}

	@Override
	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.PLAYER_SPLASH;
	}

	@Override
	protected SoundEvent getSwimHighSpeedSplashSound() {
		return SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
	}

	@Override
	public int getDimensionChangingDelay() {
		return 10;
	}

	@Override
	public void playSound(SoundEvent soundEvent, float f, float g) {
		this.level().playSound(this, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
	}

	public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.PLAYERS;
	}

	@Override
	protected int getFireImmuneTicks() {
		return 20;
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 9) {
			this.completeUsingItem();
		} else if (b == 23) {
			this.reducedDebugInfo = false;
		} else if (b == 22) {
			this.reducedDebugInfo = true;
		} else {
			super.handleEntityEvent(b);
		}
	}

	protected void closeContainer() {
		this.containerMenu = this.inventoryMenu;
	}

	protected void doCloseContainer() {
	}

	@Override
	public void rideTick() {
		if (!this.level().isClientSide && this.wantsToStopRiding() && this.isPassenger()) {
			this.stopRiding();
			this.setShiftKeyDown(false);
		} else {
			super.rideTick();
			this.oBob = this.bob;
			this.bob = 0.0F;
		}
	}

	@Override
	protected void serverAiStep() {
		super.serverAiStep();
		this.updateSwingTime();
		this.yHeadRot = this.getYRot();
	}

	@Override
	public void aiStep() {
		if (this.jumpTriggerTime > 0) {
			this.jumpTriggerTime--;
		}

		if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
			if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
				this.heal(1.0F);
			}

			if (this.foodData.getSaturationLevel() < 20.0F && this.tickCount % 20 == 0) {
				this.foodData.setSaturation(this.foodData.getSaturationLevel() + 1.0F);
			}

			if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
				this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
			}
		}

		this.inventory.tick();
		this.oBob = this.bob;
		if (this.abilities.flying && !this.isPassenger()) {
			this.resetFallDistance();
		}

		super.aiStep();
		this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
		float f;
		if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming()) {
			f = Math.min(0.1F, (float)this.getDeltaMovement().horizontalDistance());
		} else {
			f = 0.0F;
		}

		this.bob = this.bob + (f - this.bob) * 0.4F;
		if (this.getHealth() > 0.0F && !this.isSpectator()) {
			AABB aABB;
			if (this.isPassenger() && !this.getVehicle().isRemoved()) {
				aABB = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
			} else {
				aABB = this.getBoundingBox().inflate(1.0, 0.5, 1.0);
			}

			List<Entity> list = this.level().getEntities(this, aABB);
			List<Entity> list2 = Lists.<Entity>newArrayList();

			for (Entity entity : list) {
				if (entity.getType() == EntityType.EXPERIENCE_ORB) {
					list2.add(entity);
				} else if (!entity.isRemoved()) {
					this.touch(entity);
				}
			}

			if (!list2.isEmpty()) {
				this.touch(Util.getRandom(list2, this.random));
			}
		}

		this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
		this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
		if (!this.level().isClientSide && (this.fallDistance > 0.5F || this.isInWater()) || this.abilities.flying || this.isSleeping() || this.isInPowderSnow) {
			this.removeEntitiesOnShoulder();
		}
	}

	private void playShoulderEntityAmbientSound(@Nullable CompoundTag compoundTag) {
		if (compoundTag != null && (!compoundTag.contains("Silent") || !compoundTag.getBoolean("Silent")) && this.level().random.nextInt(200) == 0) {
			String string = compoundTag.getString("id");
			EntityType.byString(string)
				.filter(entityType -> entityType == EntityType.PARROT)
				.ifPresent(
					entityType -> {
						if (!Parrot.imitateNearbyMobs(this.level(), this)) {
							this.level()
								.playSound(
									null,
									this.getX(),
									this.getY(),
									this.getZ(),
									Parrot.getAmbient(this.level(), this.level().random),
									this.getSoundSource(),
									1.0F,
									Parrot.getPitch(this.level().random)
								);
						}
					}
				);
		}
	}

	private void touch(Entity entity) {
		entity.playerTouch(this);
	}

	public int getScore() {
		return this.entityData.get(DATA_SCORE_ID);
	}

	public void setScore(int i) {
		this.entityData.set(DATA_SCORE_ID, i);
	}

	public void increaseScore(int i) {
		int j = this.getScore();
		this.entityData.set(DATA_SCORE_ID, j + i);
	}

	public void startAutoSpinAttack(int i, float f, ItemStack itemStack) {
		this.autoSpinAttackTicks = i;
		this.autoSpinAttackDmg = f;
		this.autoSpinAttackItemStack = itemStack;
		if (!this.level().isClientSide) {
			this.removeEntitiesOnShoulder();
			this.setLivingEntityFlag(4, true);
		}
	}

	@Nonnull
	@Override
	public ItemStack getWeaponItem() {
		return this.isAutoSpinAttack() && this.autoSpinAttackItemStack != null ? this.autoSpinAttackItemStack : super.getWeaponItem();
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
		this.reapplyPosition();
		if (!this.isSpectator() && this.level() instanceof ServerLevel serverLevel) {
			this.dropAllDeathLoot(serverLevel, damageSource);
		}

		if (damageSource != null) {
			this.setDeltaMovement(
				(double)(-Mth.cos((this.getHurtDir() + this.getYRot()) * (float) (Math.PI / 180.0)) * 0.1F),
				0.1F,
				(double)(-Mth.sin((this.getHurtDir() + this.getYRot()) * (float) (Math.PI / 180.0)) * 0.1F)
			);
		} else {
			this.setDeltaMovement(0.0, 0.1, 0.0);
		}

		this.awardStat(Stats.DEATHS);
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
		this.clearFire();
		this.setSharedFlagOnFire(false);
		this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (!this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			this.destroyVanishingCursedItems();
			this.inventory.dropAll();
		}
	}

	protected void destroyVanishingCursedItems() {
		for (int i = 0; i < this.inventory.getContainerSize(); i++) {
			ItemStack itemStack = this.inventory.getItem(i);
			if (!itemStack.isEmpty() && EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				this.inventory.removeItemNoUpdate(i);
			}
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return damageSource.type().effects().sound();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	public void handleCreativeModeItemDrop(ItemStack itemStack) {
	}

	@Nullable
	public ItemEntity drop(ItemStack itemStack, boolean bl) {
		return this.drop(itemStack, false, bl);
	}

	@Nullable
	public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
		if (!itemStack.isEmpty() && this.level().isClientSide) {
			this.swing(InteractionHand.MAIN_HAND);
		}

		return null;
	}

	public float getDestroySpeed(BlockState blockState) {
		float f = this.inventory.getDestroySpeed(blockState);
		if (f > 1.0F) {
			f += (float)this.getAttributeValue(Attributes.MINING_EFFICIENCY);
		}

		if (MobEffectUtil.hasDigSpeed(this)) {
			f *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2F;
		}

		if (this.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float g = switch (this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
				case 0 -> 0.3F;
				case 1 -> 0.09F;
				case 2 -> 0.0027F;
				default -> 8.1E-4F;
			};
			f *= g;
		}

		f *= (float)this.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
		if (this.isEyeInFluid(FluidTags.WATER)) {
			f *= (float)this.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
		}

		if (!this.onGround()) {
			f /= 5.0F;
		}

		return f;
	}

	public boolean hasCorrectToolForDrops(BlockState blockState) {
		return !blockState.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(blockState);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setUUID(this.gameProfile.getId());
		ListTag listTag = compoundTag.getList("Inventory", 10);
		this.inventory.load(listTag);
		this.inventory.selected = compoundTag.getInt("SelectedItemSlot");
		this.sleepCounter = compoundTag.getShort("SleepTimer");
		this.experienceProgress = compoundTag.getFloat("XpP");
		this.experienceLevel = compoundTag.getInt("XpLevel");
		this.totalExperience = compoundTag.getInt("XpTotal");
		this.enchantmentSeed = compoundTag.getInt("XpSeed");
		if (this.enchantmentSeed == 0) {
			this.enchantmentSeed = this.random.nextInt();
		}

		this.setScore(compoundTag.getInt("Score"));
		this.foodData.readAdditionalSaveData(compoundTag);
		this.abilities.loadSaveData(compoundTag);
		this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)this.abilities.getWalkingSpeed());
		if (compoundTag.contains("EnderItems", 9)) {
			this.enderChestInventory.fromTag(compoundTag.getList("EnderItems", 10), this.registryAccess());
		}

		if (compoundTag.contains("ShoulderEntityLeft", 10)) {
			this.setShoulderEntityLeft(compoundTag.getCompound("ShoulderEntityLeft"));
		}

		if (compoundTag.contains("ShoulderEntityRight", 10)) {
			this.setShoulderEntityRight(compoundTag.getCompound("ShoulderEntityRight"));
		}

		if (compoundTag.contains("LastDeathLocation", 10)) {
			this.setLastDeathLocation(GlobalPos.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("LastDeathLocation")).resultOrPartial(LOGGER::error));
		}

		if (compoundTag.contains("current_explosion_impact_pos", 9)) {
			Vec3.CODEC
				.parse(NbtOps.INSTANCE, compoundTag.get("current_explosion_impact_pos"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(vec3 -> this.currentImpulseImpactPos = vec3);
		}

		this.ignoreFallDamageFromCurrentImpulse = compoundTag.getBoolean("ignore_fall_damage_from_current_explosion");
		this.currentImpulseContextResetGraceTime = compoundTag.getInt("current_impulse_context_reset_grace_time");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		NbtUtils.addCurrentDataVersion(compoundTag);
		compoundTag.put("Inventory", this.inventory.save(new ListTag()));
		compoundTag.putInt("SelectedItemSlot", this.inventory.selected);
		compoundTag.putShort("SleepTimer", (short)this.sleepCounter);
		compoundTag.putFloat("XpP", this.experienceProgress);
		compoundTag.putInt("XpLevel", this.experienceLevel);
		compoundTag.putInt("XpTotal", this.totalExperience);
		compoundTag.putInt("XpSeed", this.enchantmentSeed);
		compoundTag.putInt("Score", this.getScore());
		this.foodData.addAdditionalSaveData(compoundTag);
		this.abilities.addSaveData(compoundTag);
		compoundTag.put("EnderItems", this.enderChestInventory.createTag(this.registryAccess()));
		if (!this.getShoulderEntityLeft().isEmpty()) {
			compoundTag.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
		}

		if (!this.getShoulderEntityRight().isEmpty()) {
			compoundTag.put("ShoulderEntityRight", this.getShoulderEntityRight());
		}

		this.getLastDeathLocation()
			.flatMap(globalPos -> GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos).resultOrPartial(LOGGER::error))
			.ifPresent(tag -> compoundTag.put("LastDeathLocation", tag));
		if (this.currentImpulseImpactPos != null) {
			compoundTag.put("current_explosion_impact_pos", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.currentImpulseImpactPos).getOrThrow());
		}

		compoundTag.putBoolean("ignore_fall_damage_from_current_explosion", this.ignoreFallDamageFromCurrentImpulse);
		compoundTag.putInt("current_impulse_context_reset_grace_time", this.currentImpulseContextResetGraceTime);
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		if (super.isInvulnerableTo(damageSource)) {
			return true;
		} else if (damageSource.is(DamageTypeTags.IS_DROWNING)) {
			return !this.level().getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE);
		} else if (damageSource.is(DamageTypeTags.IS_FALL)) {
			return !this.level().getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE);
		} else if (damageSource.is(DamageTypeTags.IS_FIRE)) {
			return !this.level().getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE);
		} else {
			return damageSource.is(DamageTypeTags.IS_FREEZING) ? !this.level().getGameRules().getBoolean(GameRules.RULE_FREEZE_DAMAGE) : false;
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (this.abilities.invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		} else {
			this.noActionTime = 0;
			if (this.isDeadOrDying()) {
				return false;
			} else {
				if (!this.level().isClientSide) {
					this.removeEntitiesOnShoulder();
				}

				if (damageSource.scalesWithDifficulty()) {
					if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
						f = 0.0F;
					}

					if (this.level().getDifficulty() == Difficulty.EASY) {
						f = Math.min(f / 2.0F + 1.0F, f);
					}

					if (this.level().getDifficulty() == Difficulty.HARD) {
						f = f * 3.0F / 2.0F;
					}
				}

				return f == 0.0F ? false : super.hurt(damageSource, f);
			}
		}
	}

	@Override
	protected void blockUsingShield(LivingEntity livingEntity) {
		super.blockUsingShield(livingEntity);
		ItemStack itemStack = this.getItemBlockingWith();
		if (livingEntity.canDisableShield() && itemStack != null) {
			this.disableShield(itemStack);
		}
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		return !this.getAbilities().invulnerable && super.canBeSeenAsEnemy();
	}

	public boolean canHarmPlayer(Player player) {
		Team team = this.getTeam();
		Team team2 = player.getTeam();
		if (team == null) {
			return true;
		} else {
			return !team.isAlliedTo(team2) ? true : team.isAllowFriendlyFire();
		}
	}

	@Override
	protected void hurtArmor(DamageSource damageSource, float f) {
		this.doHurtEquipment(damageSource, f, new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD});
	}

	@Override
	protected void hurtHelmet(DamageSource damageSource, float f) {
		this.doHurtEquipment(damageSource, f, new EquipmentSlot[]{EquipmentSlot.HEAD});
	}

	@Override
	protected void hurtCurrentlyUsedShield(float f) {
		if (this.useItem.is(Items.SHIELD)) {
			if (!this.level().isClientSide) {
				this.awardStat(Stats.ITEM_USED.get(this.useItem.getItem()));
			}

			if (f >= 3.0F) {
				int i = 1 + Mth.floor(f);
				InteractionHand interactionHand = this.getUsedItemHand();
				this.useItem.hurtAndBreak(i, this, getSlotForHand(interactionHand));
				if (this.useItem.isEmpty()) {
					if (interactionHand == InteractionHand.MAIN_HAND) {
						this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					} else {
						this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
					}

					this.useItem = ItemStack.EMPTY;
					this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
				}
			}
		}
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float f) {
		if (!this.isInvulnerableTo(damageSource)) {
			f = this.getDamageAfterArmorAbsorb(damageSource, f);
			f = this.getDamageAfterMagicAbsorb(damageSource, f);
			float var7 = Math.max(f - this.getAbsorptionAmount(), 0.0F);
			this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - var7));
			float h = f - var7;
			if (h > 0.0F && h < 3.4028235E37F) {
				this.awardStat(Stats.DAMAGE_ABSORBED, Math.round(h * 10.0F));
			}

			if (var7 != 0.0F) {
				this.causeFoodExhaustion(damageSource.getFoodExhaustion());
				this.getCombatTracker().recordDamage(damageSource, var7);
				this.setHealth(this.getHealth() - var7);
				if (var7 < 3.4028235E37F) {
					this.awardStat(Stats.DAMAGE_TAKEN, Math.round(var7 * 10.0F));
				}

				this.gameEvent(GameEvent.ENTITY_DAMAGE);
			}
		}
	}

	public boolean isTextFilteringEnabled() {
		return false;
	}

	public void openTextEdit(SignBlockEntity signBlockEntity, boolean bl) {
	}

	public void openMinecartCommandBlock(BaseCommandBlock baseCommandBlock) {
	}

	public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
	}

	public void openStructureBlock(StructureBlockEntity structureBlockEntity) {
	}

	public void openJigsawBlock(JigsawBlockEntity jigsawBlockEntity) {
	}

	public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
	}

	public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
		return OptionalInt.empty();
	}

	public void sendMerchantOffers(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
	}

	public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
	}

	public InteractionResult interactOn(Entity entity, InteractionHand interactionHand) {
		if (this.isSpectator()) {
			if (entity instanceof MenuProvider) {
				this.openMenu((MenuProvider)entity);
			}

			return InteractionResult.PASS;
		} else {
			ItemStack itemStack = this.getItemInHand(interactionHand);
			ItemStack itemStack2 = itemStack.copy();
			InteractionResult interactionResult = entity.interact(this, interactionHand);
			if (interactionResult.consumesAction()) {
				if (this.abilities.instabuild && itemStack == this.getItemInHand(interactionHand) && itemStack.getCount() < itemStack2.getCount()) {
					itemStack.setCount(itemStack2.getCount());
				}

				return interactionResult;
			} else {
				if (!itemStack.isEmpty() && entity instanceof LivingEntity) {
					if (this.abilities.instabuild) {
						itemStack = itemStack2;
					}

					InteractionResult interactionResult2 = itemStack.interactLivingEntity(this, (LivingEntity)entity, interactionHand);
					if (interactionResult2.consumesAction()) {
						this.level().gameEvent(GameEvent.ENTITY_INTERACT, entity.position(), GameEvent.Context.of(this));
						if (itemStack.isEmpty() && !this.abilities.instabuild) {
							this.setItemInHand(interactionHand, ItemStack.EMPTY);
						}

						return interactionResult2;
					}
				}

				return InteractionResult.PASS;
			}
		}
	}

	@Override
	public void removeVehicle() {
		super.removeVehicle();
		this.boardingCooldown = 0;
	}

	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || this.isSleeping();
	}

	@Override
	public boolean isAffectedByFluids() {
		return !this.abilities.flying;
	}

	@Override
	protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType moverType) {
		float f = this.maxUpStep();
		if (!this.abilities.flying
			&& !(vec3.y > 0.0)
			&& (moverType == MoverType.SELF || moverType == MoverType.PLAYER)
			&& this.isStayingOnGroundSurface()
			&& this.isAboveGround(f)) {
			double d = vec3.x;
			double e = vec3.z;
			double g = 0.05;
			double h = Math.signum(d) * 0.05;

			double i;
			for (i = Math.signum(e) * 0.05; d != 0.0 && this.canFallAtLeast(d, 0.0, f); d -= h) {
				if (Math.abs(d) <= 0.05) {
					d = 0.0;
					break;
				}
			}

			while (e != 0.0 && this.canFallAtLeast(0.0, e, f)) {
				if (Math.abs(e) <= 0.05) {
					e = 0.0;
					break;
				}

				e -= i;
			}

			while (d != 0.0 && e != 0.0 && this.canFallAtLeast(d, e, f)) {
				if (Math.abs(d) <= 0.05) {
					d = 0.0;
				} else {
					d -= h;
				}

				if (Math.abs(e) <= 0.05) {
					e = 0.0;
				} else {
					e -= i;
				}
			}

			return new Vec3(d, vec3.y, e);
		} else {
			return vec3;
		}
	}

	private boolean isAboveGround(float f) {
		return this.onGround() || this.fallDistance < f && !this.canFallAtLeast(0.0, 0.0, f - this.fallDistance);
	}

	private boolean canFallAtLeast(double d, double e, float f) {
		AABB aABB = this.getBoundingBox();
		return this.level().noCollision(this, new AABB(aABB.minX + d, aABB.minY - (double)f - 1.0E-5F, aABB.minZ + e, aABB.maxX + d, aABB.minY, aABB.maxZ + e));
	}

	public void attack(Entity entity) {
		if (entity.isAttackable()) {
			if (!entity.skipAttackInteraction(this)) {
				float f = this.isAutoSpinAttack() ? this.autoSpinAttackDmg : (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
				ItemStack itemStack = this.getWeaponItem();
				DamageSource damageSource = (DamageSource)Optional.ofNullable(itemStack.getItem().getDamageSource(this)).orElse(this.damageSources().playerAttack(this));
				float g = this.getEnchantedDamage(entity, f, damageSource) - f;
				float h = this.getAttackStrengthScale(0.5F);
				f *= 0.2F + h * h * 0.8F;
				g *= h;
				this.resetAttackStrengthTicker();
				if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
					&& entity instanceof Projectile projectile
					&& projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this, this, true)) {
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource());
					return;
				}

				if (f > 0.0F || g > 0.0F) {
					boolean bl = h > 0.9F;
					boolean bl2;
					if (this.isSprinting() && bl) {
						this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
						bl2 = true;
					} else {
						bl2 = false;
					}

					f += itemStack.getItem().getAttackDamageBonus(entity, f, damageSource);
					boolean bl3 = bl
						&& this.fallDistance > 0.0F
						&& !this.onGround()
						&& !this.onClimbable()
						&& !this.isInWater()
						&& !this.hasEffect(MobEffects.BLINDNESS)
						&& !this.isPassenger()
						&& entity instanceof LivingEntity
						&& !this.isSprinting();
					if (bl3) {
						f *= 1.5F;
					}

					float i = f + g;
					boolean bl4 = false;
					if (bl && !bl3 && !bl2 && this.onGround()) {
						double d = this.getKnownMovement().horizontalDistanceSqr();
						double e = (double)this.getSpeed() * 2.5;
						if (d < Mth.square(e) && this.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.SWORDS)) {
							bl4 = true;
						}
					}

					float j = 0.0F;
					if (entity instanceof LivingEntity livingEntity) {
						j = livingEntity.getHealth();
					}

					Vec3 vec3 = entity.getDeltaMovement();
					boolean bl5 = entity.hurt(damageSource, i);
					if (bl5) {
						float k = this.getKnockback(entity, damageSource) + (bl2 ? 1.0F : 0.0F);
						if (k > 0.0F) {
							if (entity instanceof LivingEntity livingEntity2) {
								livingEntity2.knockback(
									(double)(k * 0.5F), (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)))
								);
							} else {
								entity.push(
									(double)(-Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)) * k * 0.5F),
									0.1,
									(double)(Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * k * 0.5F)
								);
							}

							this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
							this.setSprinting(false);
						}

						if (bl4) {
							float l = 1.0F + (float)this.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * f;

							for (LivingEntity livingEntity3 : this.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.0, 0.25, 1.0))) {
								if (livingEntity3 != this
									&& livingEntity3 != entity
									&& !this.isAlliedTo(livingEntity3)
									&& (!(livingEntity3 instanceof ArmorStand) || !((ArmorStand)livingEntity3).isMarker())
									&& this.distanceToSqr(livingEntity3) < 9.0) {
									float m = this.getEnchantedDamage(livingEntity3, l, damageSource) * h;
									livingEntity3.knockback(
										0.4F, (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)))
									);
									livingEntity3.hurt(damageSource, m);
									if (this.level() instanceof ServerLevel serverLevel) {
										EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity3, damageSource);
									}
								}
							}

							this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
							this.sweepAttack();
						}

						if (entity instanceof ServerPlayer && entity.hurtMarked) {
							((ServerPlayer)entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
							entity.hurtMarked = false;
							entity.setDeltaMovement(vec3);
						}

						if (bl3) {
							this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
							this.crit(entity);
						}

						if (!bl3 && !bl4) {
							if (bl) {
								this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
							} else {
								this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
							}
						}

						if (g > 0.0F) {
							this.magicCrit(entity);
						}

						this.setLastHurtMob(entity);
						Entity entity2 = entity;
						if (entity instanceof EnderDragonPart) {
							entity2 = ((EnderDragonPart)entity).parentMob;
						}

						boolean bl6 = false;
						if (this.level() instanceof ServerLevel serverLevel2) {
							if (entity2 instanceof LivingEntity livingEntity3x) {
								bl6 = itemStack.hurtEnemy(livingEntity3x, this);
							}

							EnchantmentHelper.doPostAttackEffects(serverLevel2, entity, damageSource);
						}

						if (!this.level().isClientSide && !itemStack.isEmpty() && entity2 instanceof LivingEntity) {
							if (bl6) {
								itemStack.postHurtEnemy((LivingEntity)entity2, this);
							}

							if (itemStack.isEmpty()) {
								if (itemStack == this.getMainHandItem()) {
									this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
								} else {
									this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
								}
							}
						}

						if (entity instanceof LivingEntity) {
							float n = j - ((LivingEntity)entity).getHealth();
							this.awardStat(Stats.DAMAGE_DEALT, Math.round(n * 10.0F));
							if (this.level() instanceof ServerLevel && n > 2.0F) {
								int o = (int)((double)n * 0.5);
								((ServerLevel)this.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5), entity.getZ(), o, 0.1, 0.0, 0.1, 0.2);
							}
						}

						this.causeFoodExhaustion(0.1F);
					} else {
						this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
					}
				}
			}
		}
	}

	protected float getEnchantedDamage(Entity entity, float f, DamageSource damageSource) {
		return f;
	}

	@Override
	protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
		this.attack(livingEntity);
	}

	public void disableShield(ItemStack itemStack) {
		this.getCooldowns().addCooldown(itemStack, 100);
		this.stopUsingItem();
		this.level().broadcastEntityEvent(this, (byte)30);
	}

	public void crit(Entity entity) {
	}

	public void magicCrit(Entity entity) {
	}

	public void sweepAttack() {
		double d = (double)(-Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)));
		double e = (double)Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
		if (this.level() instanceof ServerLevel) {
			((ServerLevel)this.level()).sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
		}
	}

	public void respawn() {
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		super.remove(removalReason);
		this.inventoryMenu.removed(this);
		if (this.containerMenu != null && this.hasContainerOpen()) {
			this.doCloseContainer();
		}
	}

	public boolean isLocalPlayer() {
		return false;
	}

	public GameProfile getGameProfile() {
		return this.gameProfile;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public Abilities getAbilities() {
		return this.abilities;
	}

	@Override
	public boolean hasInfiniteMaterials() {
		return this.abilities.instabuild;
	}

	public void updateTutorialInventoryAction(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction) {
	}

	public boolean hasContainerOpen() {
		return this.containerMenu != this.inventoryMenu;
	}

	public boolean canDropItems() {
		return true;
	}

	public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
		this.startSleeping(blockPos);
		this.sleepCounter = 0;
		return Either.right(Unit.INSTANCE);
	}

	public void stopSleepInBed(boolean bl, boolean bl2) {
		super.stopSleeping();
		if (this.level() instanceof ServerLevel && bl2) {
			((ServerLevel)this.level()).updateSleepingPlayerList();
		}

		this.sleepCounter = bl ? 0 : 100;
	}

	@Override
	public void stopSleeping() {
		this.stopSleepInBed(true, true);
	}

	public boolean isSleepingLongEnough() {
		return this.isSleeping() && this.sleepCounter >= 100;
	}

	public int getSleepTimer() {
		return this.sleepCounter;
	}

	public void displayClientMessage(Component component, boolean bl) {
	}

	public void awardStat(ResourceLocation resourceLocation) {
		this.awardStat(Stats.CUSTOM.get(resourceLocation));
	}

	public void awardStat(ResourceLocation resourceLocation, int i) {
		this.awardStat(Stats.CUSTOM.get(resourceLocation), i);
	}

	public void awardStat(Stat<?> stat) {
		this.awardStat(stat, 1);
	}

	public void awardStat(Stat<?> stat, int i) {
	}

	public void resetStat(Stat<?> stat) {
	}

	public int awardRecipes(Collection<RecipeHolder<?>> collection) {
		return 0;
	}

	public void triggerRecipeCrafted(RecipeHolder<?> recipeHolder, List<ItemStack> list) {
	}

	public void awardRecipesByKey(List<ResourceLocation> list) {
	}

	public int resetRecipes(Collection<RecipeHolder<?>> collection) {
		return 0;
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isPassenger()) {
			super.travel(vec3);
		} else {
			if (this.isSwimming()) {
				double d = this.getLookAngle().y;
				double e = d < -0.2 ? 0.085 : 0.06;
				if (d <= 0.0 || this.jumping || !this.level().getFluidState(BlockPos.containing(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).isEmpty()) {
					Vec3 vec32 = this.getDeltaMovement();
					this.setDeltaMovement(vec32.add(0.0, (d - vec32.y) * e, 0.0));
				}
			}

			if (this.getAbilities().flying) {
				double d = this.getDeltaMovement().y;
				super.travel(vec3);
				this.setDeltaMovement(this.getDeltaMovement().with(Direction.Axis.Y, d * 0.6));
			} else {
				super.travel(vec3);
			}
		}
	}

	@Override
	protected boolean canGlide() {
		return !this.abilities.flying && super.canGlide();
	}

	@Override
	public void updateSwimming() {
		if (this.abilities.flying) {
			this.setSwimming(false);
		} else {
			super.updateSwimming();
		}
	}

	protected boolean freeAt(BlockPos blockPos) {
		return !this.level().getBlockState(blockPos).isSuffocating(this.level(), blockPos);
	}

	@Override
	public float getSpeed() {
		return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		if (this.abilities.mayfly) {
			return false;
		} else {
			if (f >= 2.0F) {
				this.awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)f * 100.0));
			}

			boolean bl = this.currentImpulseImpactPos != null && this.ignoreFallDamageFromCurrentImpulse;
			float h;
			if (bl) {
				h = Math.min(f, (float)(this.currentImpulseImpactPos.y - this.getY()));
				boolean bl2 = h <= 0.0F;
				if (bl2) {
					this.resetCurrentImpulseContext();
				} else {
					this.tryResetCurrentImpulseContext();
				}
			} else {
				h = f;
			}

			if (h > 0.0F && super.causeFallDamage(h, g, damageSource)) {
				this.resetCurrentImpulseContext();
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean tryToStartFallFlying() {
		if (!this.isFallFlying() && this.canGlide() && !this.isInWater()) {
			this.startFallFlying();
			return true;
		} else {
			return false;
		}
	}

	public void startFallFlying() {
		this.setSharedFlag(7, true);
	}

	public void stopFallFlying() {
		this.setSharedFlag(7, true);
		this.setSharedFlag(7, false);
	}

	@Override
	protected void doWaterSplashEffect() {
		if (!this.isSpectator()) {
			super.doWaterSplashEffect();
		}
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		if (this.isInWater()) {
			this.waterSwimSound();
			this.playMuffledStepSound(blockState);
		} else {
			BlockPos blockPos2 = this.getPrimaryStepSoundBlockPos(blockPos);
			if (!blockPos.equals(blockPos2)) {
				BlockState blockState2 = this.level().getBlockState(blockPos2);
				if (blockState2.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
					this.playCombinationStepSounds(blockState2, blockState);
				} else {
					super.playStepSound(blockPos2, blockState2);
				}
			} else {
				super.playStepSound(blockPos, blockState);
			}
		}
	}

	@Override
	public LivingEntity.Fallsounds getFallSounds() {
		return new LivingEntity.Fallsounds(SoundEvents.PLAYER_SMALL_FALL, SoundEvents.PLAYER_BIG_FALL);
	}

	@Override
	public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
		this.awardStat(Stats.ENTITY_KILLED.get(livingEntity.getType()));
		return true;
	}

	@Override
	public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
		if (!this.abilities.flying) {
			super.makeStuckInBlock(blockState, vec3);
		}

		this.tryResetCurrentImpulseContext();
	}

	public void giveExperiencePoints(int i) {
		this.increaseScore(i);
		this.experienceProgress = this.experienceProgress + (float)i / (float)this.getXpNeededForNextLevel();
		this.totalExperience = Mth.clamp(this.totalExperience + i, 0, Integer.MAX_VALUE);

		while (this.experienceProgress < 0.0F) {
			float f = this.experienceProgress * (float)this.getXpNeededForNextLevel();
			if (this.experienceLevel > 0) {
				this.giveExperienceLevels(-1);
				this.experienceProgress = 1.0F + f / (float)this.getXpNeededForNextLevel();
			} else {
				this.giveExperienceLevels(-1);
				this.experienceProgress = 0.0F;
			}
		}

		while (this.experienceProgress >= 1.0F) {
			this.experienceProgress = (this.experienceProgress - 1.0F) * (float)this.getXpNeededForNextLevel();
			this.giveExperienceLevels(1);
			this.experienceProgress = this.experienceProgress / (float)this.getXpNeededForNextLevel();
		}
	}

	public int getEnchantmentSeed() {
		return this.enchantmentSeed;
	}

	public void onEnchantmentPerformed(ItemStack itemStack, int i) {
		this.experienceLevel -= i;
		if (this.experienceLevel < 0) {
			this.experienceLevel = 0;
			this.experienceProgress = 0.0F;
			this.totalExperience = 0;
		}

		this.enchantmentSeed = this.random.nextInt();
	}

	public void giveExperienceLevels(int i) {
		this.experienceLevel = IntMath.saturatedAdd(this.experienceLevel, i);
		if (this.experienceLevel < 0) {
			this.experienceLevel = 0;
			this.experienceProgress = 0.0F;
			this.totalExperience = 0;
		}

		if (i > 0 && this.experienceLevel % 5 == 0 && (float)this.lastLevelUpTime < (float)this.tickCount - 100.0F) {
			float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), f * 0.75F, 1.0F);
			this.lastLevelUpTime = this.tickCount;
		}
	}

	public int getXpNeededForNextLevel() {
		if (this.experienceLevel >= 30) {
			return 112 + (this.experienceLevel - 30) * 9;
		} else {
			return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
		}
	}

	public void causeFoodExhaustion(float f) {
		if (!this.abilities.invulnerable) {
			if (!this.level().isClientSide) {
				this.foodData.addExhaustion(f);
			}
		}
	}

	public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
		return Optional.empty();
	}

	public FoodData getFoodData() {
		return this.foodData;
	}

	public boolean canEat(boolean bl) {
		return this.abilities.invulnerable || bl || this.foodData.needsFood();
	}

	public boolean isHurt() {
		return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
	}

	public boolean mayBuild() {
		return this.abilities.mayBuild;
	}

	public boolean mayUseItemAt(BlockPos blockPos, Direction direction, ItemStack itemStack) {
		if (this.abilities.mayBuild) {
			return true;
		} else {
			BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
			BlockInWorld blockInWorld = new BlockInWorld(this.level(), blockPos2, false);
			return itemStack.canPlaceOnBlockInAdventureMode(blockInWorld);
		}
	}

	@Override
	protected int getBaseExperienceReward() {
		return !this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !this.isSpectator() ? Math.min(this.experienceLevel * 7, 100) : 0;
	}

	@Override
	protected boolean isAlwaysExperienceDropper() {
		return true;
	}

	@Override
	public boolean shouldShowName() {
		return true;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return this.abilities.flying || this.onGround() && this.isDiscrete() ? Entity.MovementEmission.NONE : Entity.MovementEmission.ALL;
	}

	public void onUpdateAbilities() {
	}

	@Override
	public Component getName() {
		return Component.literal(this.gameProfile.getName());
	}

	public PlayerEnderChestContainer getEnderChestInventory() {
		return this.enderChestInventory;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			return this.inventory.getSelected();
		} else if (equipmentSlot == EquipmentSlot.OFFHAND) {
			return (ItemStack)this.inventory.offhand.getFirst();
		} else {
			return equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? this.inventory.armor.get(equipmentSlot.getIndex()) : ItemStack.EMPTY;
		}
	}

	@Override
	protected boolean doesEmitEquipEvent(EquipmentSlot equipmentSlot) {
		return equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
	}

	@Override
	public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		this.verifyEquippedItem(itemStack);
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			this.onEquipItem(equipmentSlot, this.inventory.items.set(this.inventory.selected, itemStack), itemStack);
		} else if (equipmentSlot == EquipmentSlot.OFFHAND) {
			this.onEquipItem(equipmentSlot, this.inventory.offhand.set(0, itemStack), itemStack);
		} else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
			this.onEquipItem(equipmentSlot, this.inventory.armor.set(equipmentSlot.getIndex(), itemStack), itemStack);
		}
	}

	public boolean addItem(ItemStack itemStack) {
		return this.inventory.add(itemStack);
	}

	@Override
	public Iterable<ItemStack> getHandSlots() {
		return Lists.<ItemStack>newArrayList(this.getMainHandItem(), this.getOffhandItem());
	}

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return this.inventory.armor;
	}

	@Override
	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return equipmentSlot != EquipmentSlot.BODY;
	}

	public boolean setEntityOnShoulder(CompoundTag compoundTag) {
		if (this.isPassenger() || !this.onGround() || this.isInWater() || this.isInPowderSnow) {
			return false;
		} else if (this.getShoulderEntityLeft().isEmpty()) {
			this.setShoulderEntityLeft(compoundTag);
			this.timeEntitySatOnShoulder = this.level().getGameTime();
			return true;
		} else if (this.getShoulderEntityRight().isEmpty()) {
			this.setShoulderEntityRight(compoundTag);
			this.timeEntitySatOnShoulder = this.level().getGameTime();
			return true;
		} else {
			return false;
		}
	}

	protected void removeEntitiesOnShoulder() {
		if (this.timeEntitySatOnShoulder + 20L < this.level().getGameTime()) {
			this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
			this.setShoulderEntityLeft(new CompoundTag());
			this.respawnEntityOnShoulder(this.getShoulderEntityRight());
			this.setShoulderEntityRight(new CompoundTag());
		}
	}

	private void respawnEntityOnShoulder(CompoundTag compoundTag) {
		if (!this.level().isClientSide && !compoundTag.isEmpty()) {
			EntityType.create(compoundTag, this.level(), EntitySpawnReason.LOAD).ifPresent(entity -> {
				if (entity instanceof TamableAnimal) {
					((TamableAnimal)entity).setOwnerUUID(this.uuid);
				}

				entity.setPos(this.getX(), this.getY() + 0.7F, this.getZ());
				((ServerLevel)this.level()).addWithUUID(entity);
			});
		}
	}

	@Override
	public abstract boolean isSpectator();

	@Override
	public boolean canBeHitByProjectile() {
		return !this.isSpectator() && super.canBeHitByProjectile();
	}

	@Override
	public boolean isSwimming() {
		return !this.abilities.flying && !this.isSpectator() && super.isSwimming();
	}

	public abstract boolean isCreative();

	@Override
	public boolean isPushedByFluid() {
		return !this.abilities.flying;
	}

	public Scoreboard getScoreboard() {
		return this.level().getScoreboard();
	}

	@Override
	public Component getDisplayName() {
		MutableComponent mutableComponent = PlayerTeam.formatNameForTeam(this.getTeam(), this.getName());
		return this.decorateDisplayNameComponent(mutableComponent);
	}

	private MutableComponent decorateDisplayNameComponent(MutableComponent mutableComponent) {
		String string = this.getGameProfile().getName();
		return mutableComponent.withStyle(
			style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " "))
					.withHoverEvent(this.createHoverEvent())
					.withInsertion(string)
		);
	}

	@Override
	public String getScoreboardName() {
		return this.getGameProfile().getName();
	}

	@Override
	protected void internalSetAbsorptionAmount(float f) {
		this.getEntityData().set(DATA_PLAYER_ABSORPTION_ID, f);
	}

	@Override
	public float getAbsorptionAmount() {
		return this.getEntityData().get(DATA_PLAYER_ABSORPTION_ID);
	}

	public boolean isModelPartShown(PlayerModelPart playerModelPart) {
		return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & playerModelPart.getMask()) == playerModelPart.getMask();
	}

	@Override
	public SlotAccess getSlot(int i) {
		if (i == 499) {
			return new SlotAccess() {
				@Override
				public ItemStack get() {
					return Player.this.containerMenu.getCarried();
				}

				@Override
				public boolean set(ItemStack itemStack) {
					Player.this.containerMenu.setCarried(itemStack);
					return true;
				}
			};
		} else {
			final int j = i - 500;
			if (j >= 0 && j < 4) {
				return new SlotAccess() {
					@Override
					public ItemStack get() {
						return Player.this.inventoryMenu.getCraftSlots().getItem(j);
					}

					@Override
					public boolean set(ItemStack itemStack) {
						Player.this.inventoryMenu.getCraftSlots().setItem(j, itemStack);
						Player.this.inventoryMenu.slotsChanged(Player.this.inventory);
						return true;
					}
				};
			} else if (i >= 0 && i < this.inventory.items.size()) {
				return SlotAccess.forContainer(this.inventory, i);
			} else {
				int k = i - 200;
				return k >= 0 && k < this.enderChestInventory.getContainerSize() ? SlotAccess.forContainer(this.enderChestInventory, k) : super.getSlot(i);
			}
		}
	}

	public boolean isReducedDebugInfo() {
		return this.reducedDebugInfo;
	}

	public void setReducedDebugInfo(boolean bl) {
		this.reducedDebugInfo = bl;
	}

	@Override
	public void setRemainingFireTicks(int i) {
		super.setRemainingFireTicks(this.abilities.invulnerable ? Math.min(i, 1) : i);
	}

	@Override
	public HumanoidArm getMainArm() {
		return this.entityData.get(DATA_PLAYER_MAIN_HAND) == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
	}

	public void setMainArm(HumanoidArm humanoidArm) {
		this.entityData.set(DATA_PLAYER_MAIN_HAND, (byte)(humanoidArm == HumanoidArm.LEFT ? 0 : 1));
	}

	public CompoundTag getShoulderEntityLeft() {
		return this.entityData.get(DATA_SHOULDER_LEFT);
	}

	protected void setShoulderEntityLeft(CompoundTag compoundTag) {
		this.entityData.set(DATA_SHOULDER_LEFT, compoundTag);
	}

	public CompoundTag getShoulderEntityRight() {
		return this.entityData.get(DATA_SHOULDER_RIGHT);
	}

	protected void setShoulderEntityRight(CompoundTag compoundTag) {
		this.entityData.set(DATA_SHOULDER_RIGHT, compoundTag);
	}

	public float getCurrentItemAttackStrengthDelay() {
		return (float)(1.0 / this.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0);
	}

	public float getAttackStrengthScale(float f) {
		return Mth.clamp(((float)this.attackStrengthTicker + f) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
	}

	public void resetAttackStrengthTicker() {
		this.attackStrengthTicker = 0;
	}

	public ItemCooldowns getCooldowns() {
		return this.cooldowns;
	}

	@Override
	protected float getBlockSpeedFactor() {
		return !this.abilities.flying && !this.isFallFlying() ? super.getBlockSpeedFactor() : 1.0F;
	}

	public float getLuck() {
		return (float)this.getAttributeValue(Attributes.LUCK);
	}

	public boolean canUseGameMasterBlocks() {
		return this.abilities.instabuild && this.getPermissionLevel() >= 2;
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return (EntityDimensions)POSES.getOrDefault(pose, STANDING_DIMENSIONS);
	}

	@Override
	public ImmutableList<Pose> getDismountPoses() {
		return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
	}

	@Override
	public ItemStack getProjectile(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ProjectileWeaponItem)) {
			return ItemStack.EMPTY;
		} else {
			Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemStack.getItem()).getSupportedHeldProjectiles();
			ItemStack itemStack2 = ProjectileWeaponItem.getHeldProjectile(this, predicate);
			if (!itemStack2.isEmpty()) {
				return itemStack2;
			} else {
				predicate = ((ProjectileWeaponItem)itemStack.getItem()).getAllSupportedProjectiles();

				for (int i = 0; i < this.inventory.getContainerSize(); i++) {
					ItemStack itemStack3 = this.inventory.getItem(i);
					if (predicate.test(itemStack3)) {
						return itemStack3;
					}
				}

				return this.abilities.instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
			}
		}
	}

	@Override
	public Vec3 getRopeHoldPosition(float f) {
		double d = 0.22 * (this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0);
		float g = Mth.lerp(f * 0.5F, this.getXRot(), this.xRotO) * (float) (Math.PI / 180.0);
		float h = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * (float) (Math.PI / 180.0);
		if (this.isFallFlying() || this.isAutoSpinAttack()) {
			Vec3 vec3 = this.getViewVector(f);
			Vec3 vec32 = this.getDeltaMovement();
			double e = vec32.horizontalDistanceSqr();
			double i = vec3.horizontalDistanceSqr();
			float l;
			if (e > 0.0 && i > 0.0) {
				double j = (vec32.x * vec3.x + vec32.z * vec3.z) / Math.sqrt(e * i);
				double k = vec32.x * vec3.z - vec32.z * vec3.x;
				l = (float)(Math.signum(k) * Math.acos(j));
			} else {
				l = 0.0F;
			}

			return this.getPosition(f).add(new Vec3(d, -0.11, 0.85).zRot(-l).xRot(-g).yRot(-h));
		} else if (this.isVisuallySwimming()) {
			return this.getPosition(f).add(new Vec3(d, 0.2, -0.15).xRot(-g).yRot(-h));
		} else {
			double m = this.getBoundingBox().getYsize() - 1.0;
			double e = this.isCrouching() ? -0.2 : 0.07;
			return this.getPosition(f).add(new Vec3(d, m, e).yRot(-h));
		}
	}

	@Override
	public boolean isAlwaysTicking() {
		return true;
	}

	public boolean isScoping() {
		return this.isUsingItem() && this.getUseItem().is(Items.SPYGLASS);
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	public Optional<GlobalPos> getLastDeathLocation() {
		return this.lastDeathLocation;
	}

	public void setLastDeathLocation(Optional<GlobalPos> optional) {
		this.lastDeathLocation = optional;
	}

	@Override
	public float getHurtDir() {
		return this.hurtDir;
	}

	@Override
	public void animateHurt(float f) {
		super.animateHurt(f);
		this.hurtDir = f;
	}

	@Override
	public boolean canSprint() {
		return true;
	}

	@Override
	protected float getFlyingSpeed() {
		if (this.abilities.flying && !this.isPassenger()) {
			return this.isSprinting() ? this.abilities.getFlyingSpeed() * 2.0F : this.abilities.getFlyingSpeed();
		} else {
			return this.isSprinting() ? 0.025999999F : 0.02F;
		}
	}

	public double blockInteractionRange() {
		return this.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
	}

	public double entityInteractionRange() {
		return this.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
	}

	public boolean canInteractWithEntity(Entity entity, double d) {
		return entity.isRemoved() ? false : this.canInteractWithEntity(entity.getBoundingBox(), d);
	}

	public boolean canInteractWithEntity(AABB aABB, double d) {
		double e = this.entityInteractionRange() + d;
		return aABB.distanceToSqr(this.getEyePosition()) < e * e;
	}

	public boolean canInteractWithBlock(BlockPos blockPos, double d) {
		double e = this.blockInteractionRange() + d;
		return new AABB(blockPos).distanceToSqr(this.getEyePosition()) < e * e;
	}

	public void setIgnoreFallDamageFromCurrentImpulse(boolean bl) {
		this.ignoreFallDamageFromCurrentImpulse = bl;
		if (bl) {
			this.currentImpulseContextResetGraceTime = 40;
		} else {
			this.currentImpulseContextResetGraceTime = 0;
		}
	}

	public boolean isIgnoringFallDamageFromCurrentImpulse() {
		return this.ignoreFallDamageFromCurrentImpulse;
	}

	public void tryResetCurrentImpulseContext() {
		if (this.currentImpulseContextResetGraceTime == 0) {
			this.resetCurrentImpulseContext();
		}
	}

	public void resetCurrentImpulseContext() {
		this.currentImpulseContextResetGraceTime = 0;
		this.currentExplosionCause = null;
		this.currentImpulseImpactPos = null;
		this.ignoreFallDamageFromCurrentImpulse = false;
	}

	public boolean shouldRotateWithMinecart() {
		return false;
	}

	public static enum BedSleepingProblem {
		NOT_POSSIBLE_HERE,
		NOT_POSSIBLE_NOW(Component.translatable("block.minecraft.bed.no_sleep")),
		TOO_FAR_AWAY(Component.translatable("block.minecraft.bed.too_far_away")),
		OBSTRUCTED(Component.translatable("block.minecraft.bed.obstructed")),
		OTHER_PROBLEM,
		NOT_SAFE(Component.translatable("block.minecraft.bed.not_safe"));

		@Nullable
		private final Component message;

		private BedSleepingProblem() {
			this.message = null;
		}

		private BedSleepingProblem(final Component component) {
			this.message = component;
		}

		@Nullable
		public Component getMessage() {
			return this.message;
		}
	}
}
