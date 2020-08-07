package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public abstract class Player extends LivingEntity {
	public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F);
	private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder()
		.put(Pose.STANDING, STANDING_DIMENSIONS)
		.put(Pose.SLEEPING, SLEEPING_DIMENSIONS)
		.put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F))
		.put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F))
		.put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F))
		.put(Pose.CROUCHING, EntityDimensions.scalable(0.6F, 1.5F))
		.put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F))
		.build();
	private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_SCORE_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
	protected static final EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Byte> DATA_PLAYER_MAIN_HAND = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	protected static final EntityDataAccessor<CompoundTag> DATA_SHOULDER_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	private long timeEntitySatOnShoulder;
	public final Inventory inventory = new Inventory(this);
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
	public final Abilities abilities = new Abilities();
	public int experienceLevel;
	public int totalExperience;
	public float experienceProgress;
	protected int enchantmentSeed;
	protected final float defaultFlySpeed = 0.02F;
	private int lastLevelUpTime;
	private final GameProfile gameProfile;
	@Environment(EnvType.CLIENT)
	private boolean reducedDebugInfo;
	private ItemStack lastItemInMainHand = ItemStack.EMPTY;
	private final ItemCooldowns cooldowns = this.createItemCooldowns();
	@Nullable
	public FishingHook fishing;
	protected boolean enableShieldOnCrouch = true;

	public Player(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(EntityType.PLAYER, level);
		this.setUUID(createPlayerUUID(gameProfile));
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
			return itemStack.isEmpty() || !itemStack.hasAdventureModeBreakTagForBlock(level.getTagManager(), new BlockInWorld(level, blockPos, false));
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.MOVEMENT_SPEED, 0.1F)
			.add(Attributes.ATTACK_SPEED)
			.add(Attributes.ATTACK_REACH)
			.add(Attributes.LUCK);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_PLAYER_ABSORPTION_ID, 0.0F);
		this.entityData.define(DATA_SCORE_ID, 0);
		this.entityData.define(DATA_PLAYER_MODE_CUSTOMISATION, (byte)0);
		this.entityData.define(DATA_PLAYER_MAIN_HAND, (byte)1);
		this.entityData.define(DATA_SHOULDER_LEFT, new CompoundTag());
		this.entityData.define(DATA_SHOULDER_RIGHT, new CompoundTag());
	}

	@Override
	public void tick() {
		this.noPhysics = this.isSpectator();
		if (this.isSpectator()) {
			this.onGround = false;
		}

		if (this.takeXpDelay > 0) {
			this.takeXpDelay--;
		}

		if (this.isSleeping()) {
			this.sleepCounter++;
			if (this.sleepCounter > 100) {
				this.sleepCounter = 100;
			}

			if (!this.level.isClientSide && this.level.isDay()) {
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
		if (!this.level.isClientSide && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
			this.closeContainer();
			this.containerMenu = this.inventoryMenu;
		}

		this.moveCloak();
		if (!this.level.isClientSide) {
			this.foodData.tick(this);
			this.awardStat(Stats.PLAY_ONE_MINUTE);
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

		this.attackStrengthTicker--;
		ItemStack itemStack = this.getMainHandItem();
		if (!ItemStack.matches(this.lastItemInMainHand, itemStack)) {
			this.lastItemInMainHand = itemStack.copy();
		}

		this.turtleHelmetTick();
		this.cooldowns.tick();
		this.updatePlayerPose();
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

	private void turtleHelmetTick() {
		ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
		if (itemStack.getItem() == Items.TURTLE_HELMET && !this.isEyeInFluid(FluidTags.WATER)) {
			this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false, true));
		}
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
		if (this.canEnterPose(Pose.SWIMMING)) {
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
			if (this.isSpectator() || this.isPassenger() || this.canEnterPose(pose)) {
				pose2 = pose;
			} else if (this.canEnterPose(Pose.CROUCHING)) {
				pose2 = Pose.CROUCHING;
			} else {
				pose2 = Pose.SWIMMING;
			}

			this.setPose(pose2);
		}
	}

	@Override
	public int getPortalWaitTime() {
		return this.abilities.invulnerable ? 1 : 80;
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
		this.level.playSound(this, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
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

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 9) {
			this.completeUsingItem();
		} else if (b == 23) {
			this.reducedDebugInfo = false;
		} else if (b == 22) {
			this.reducedDebugInfo = true;
		} else if (b == 43) {
			this.addParticlesAroundSelf(ParticleTypes.CLOUD);
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Environment(EnvType.CLIENT)
	private void addParticlesAroundSelf(ParticleOptions particleOptions) {
		for (int i = 0; i < 5; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level.addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d, e, f);
		}
	}

	protected void closeContainer() {
		this.containerMenu = this.inventoryMenu;
	}

	@Override
	public void rideTick() {
		if (this.wantsToStopRiding() && this.isPassenger()) {
			this.stopRiding();
			this.setShiftKeyDown(false);
		} else {
			double d = this.getX();
			double e = this.getY();
			double f = this.getZ();
			super.rideTick();
			this.oBob = this.bob;
			this.bob = 0.0F;
			this.checkRidingStatistics(this.getX() - d, this.getY() - e, this.getZ() - f);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void resetPos() {
		this.setPose(Pose.STANDING);
		super.resetPos();
		this.setHealth(this.getMaxHealth());
		this.deathTime = 0;
	}

	@Override
	protected void serverAiStep() {
		super.serverAiStep();
		this.updateSwingTime();
		this.yHeadRot = this.yRot;
	}

	@Override
	public void aiStep() {
		if (this.jumpTriggerTime > 0) {
			this.jumpTriggerTime--;
		}

		if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
			if (this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
				this.heal(1.0F);
			}

			if (this.foodData.needsFood() && this.tickCount % 10 == 0) {
				this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
			}
		}

		this.inventory.tick();
		this.oBob = this.bob;
		super.aiStep();
		this.flyingSpeed = 0.02F;
		if (this.isSprinting()) {
			this.flyingSpeed = (float)((double)this.flyingSpeed + 0.005999999865889549);
		}

		this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
		float f;
		if (this.onGround && !this.isDeadOrDying() && !this.isSwimming()) {
			f = Math.min(0.1F, Mth.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement())));
		} else {
			f = 0.0F;
		}

		this.bob = this.bob + (f - this.bob) * 0.4F;
		if (this.getHealth() > 0.0F && !this.isSpectator()) {
			AABB aABB;
			if (this.isPassenger() && !this.getVehicle().removed) {
				aABB = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
			} else {
				aABB = this.getBoundingBox().inflate(1.0, 0.5, 1.0);
			}

			List<Entity> list = this.level.getEntities(this, aABB);

			for (int i = 0; i < list.size(); i++) {
				Entity entity = (Entity)list.get(i);
				if (!entity.removed) {
					this.touch(entity);
				}
			}
		}

		this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
		this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
		if (!this.level.isClientSide && (this.fallDistance > 0.5F || this.isInWater()) || this.abilities.flying || this.isSleeping()) {
			this.removeEntitiesOnShoulder();
		}
	}

	private void playShoulderEntityAmbientSound(@Nullable CompoundTag compoundTag) {
		if (compoundTag != null && (!compoundTag.contains("Silent") || !compoundTag.getBoolean("Silent")) && this.level.random.nextInt(200) == 0) {
			String string = compoundTag.getString("id");
			EntityType.byString(string)
				.filter(entityType -> entityType == EntityType.PARROT)
				.ifPresent(
					entityType -> {
						if (!Parrot.imitateNearbyMobs(this.level, this)) {
							this.level
								.playSound(
									null,
									this.getX(),
									this.getY(),
									this.getZ(),
									Parrot.getAmbient(this.level, this.level.random),
									this.getSoundSource(),
									1.0F,
									Parrot.getPitch(this.level.random)
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

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
		this.reapplyPosition();
		if (!this.isSpectator()) {
			this.dropAllDeathLoot(damageSource);
		}

		if (damageSource != null) {
			this.setDeltaMovement(
				(double)(-Mth.cos((this.hurtDir + this.yRot) * (float) (Math.PI / 180.0)) * 0.1F),
				0.1F,
				(double)(-Mth.sin((this.hurtDir + this.yRot) * (float) (Math.PI / 180.0)) * 0.1F)
			);
		} else {
			this.setDeltaMovement(0.0, 0.1, 0.0);
		}

		this.awardStat(Stats.DEATHS);
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
		this.clearFire();
		this.setSharedFlag(0, false);
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			this.destroyVanishingCursedItems();
			this.inventory.dropAll();
		}
	}

	protected void destroyVanishingCursedItems() {
		for (int i = 0; i < this.inventory.getContainerSize(); i++) {
			ItemStack itemStack = this.inventory.getItem(i);
			if (!itemStack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemStack)) {
				this.inventory.removeItemNoUpdate(i);
			}
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		if (damageSource == DamageSource.ON_FIRE) {
			return SoundEvents.PLAYER_HURT_ON_FIRE;
		} else if (damageSource == DamageSource.DROWN) {
			return SoundEvents.PLAYER_HURT_DROWN;
		} else {
			return damageSource == DamageSource.SWEET_BERRY_BUSH ? SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH : SoundEvents.PLAYER_HURT;
		}
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	public boolean drop(boolean bl) {
		return this.drop(
				this.inventory.removeItem(this.inventory.selected, bl && !this.inventory.getSelected().isEmpty() ? this.inventory.getSelected().getCount() : 1),
				false,
				true
			)
			!= null;
	}

	@Nullable
	public ItemEntity drop(ItemStack itemStack, boolean bl) {
		return this.drop(itemStack, false, bl);
	}

	@Nullable
	public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
		if (itemStack.isEmpty()) {
			return null;
		} else {
			if (this.level.isClientSide) {
				this.swing(InteractionHand.MAIN_HAND);
			}

			double d = this.getEyeY() - 0.3F;
			ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), d, this.getZ(), itemStack);
			itemEntity.setPickUpDelay(40);
			if (bl2) {
				itemEntity.setThrower(this.getUUID());
			}

			if (bl) {
				float f = this.random.nextFloat() * 0.5F;
				float g = this.random.nextFloat() * (float) (Math.PI * 2);
				itemEntity.setDeltaMovement((double)(-Mth.sin(g) * f), 0.2F, (double)(Mth.cos(g) * f));
			} else {
				float f = 0.3F;
				float g = Mth.sin(this.xRot * (float) (Math.PI / 180.0));
				float h = Mth.cos(this.xRot * (float) (Math.PI / 180.0));
				float i = Mth.sin(this.yRot * (float) (Math.PI / 180.0));
				float j = Mth.cos(this.yRot * (float) (Math.PI / 180.0));
				float k = this.random.nextFloat() * (float) (Math.PI * 2);
				float l = 0.02F * this.random.nextFloat();
				itemEntity.setDeltaMovement(
					(double)(-i * h * 0.3F) + Math.cos((double)k) * (double)l,
					(double)(-g * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
					(double)(j * h * 0.3F) + Math.sin((double)k) * (double)l
				);
			}

			return itemEntity;
		}
	}

	public float getDestroySpeed(BlockState blockState) {
		float f = this.inventory.getDestroySpeed(blockState);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getDiggingEfficiency(this);
			ItemStack itemStack = this.getMainHandItem();
			if (i > 0 && !itemStack.isEmpty()) {
				f += (float)(i * i + 1);
			}
		}

		if (MobEffectUtil.hasDigSpeed(this)) {
			f *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2F;
		}

		if (this.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float g;
			switch (this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
				case 0:
					g = 0.3F;
					break;
				case 1:
					g = 0.09F;
					break;
				case 2:
					g = 0.0027F;
					break;
				case 3:
				default:
					g = 8.1E-4F;
			}

			f *= g;
		}

		if (this.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
			f /= 5.0F;
		}

		if (!this.onGround) {
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
		this.setUUID(createPlayerUUID(this.gameProfile));
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
			this.enderChestInventory.fromTag(compoundTag.getList("EnderItems", 10));
		}

		if (compoundTag.contains("ShoulderEntityLeft", 10)) {
			this.setShoulderEntityLeft(compoundTag.getCompound("ShoulderEntityLeft"));
		}

		if (compoundTag.contains("ShoulderEntityRight", 10)) {
			this.setShoulderEntityRight(compoundTag.getCompound("ShoulderEntityRight"));
		}

		this.getAttribute(Attributes.ATTACK_REACH).setBaseValue(3.0);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
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
		compoundTag.put("EnderItems", this.enderChestInventory.createTag());
		if (!this.getShoulderEntityLeft().isEmpty()) {
			compoundTag.put("ShoulderEntityLeft", this.getShoulderEntityLeft());
		}

		if (!this.getShoulderEntityRight().isEmpty()) {
			compoundTag.put("ShoulderEntityRight", this.getShoulderEntityRight());
		}
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		if (super.isInvulnerableTo(damageSource)) {
			return true;
		} else if (damageSource == DamageSource.DROWN) {
			return !this.level.getGameRules().getBoolean(GameRules.RULE_DROWNING_DAMAGE);
		} else if (damageSource == DamageSource.FALL) {
			return !this.level.getGameRules().getBoolean(GameRules.RULE_FALL_DAMAGE);
		} else {
			return damageSource.isFire() ? !this.level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE) : false;
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (this.abilities.invulnerable && !damageSource.isBypassInvul()) {
			return false;
		} else {
			this.noActionTime = 0;
			if (this.isDeadOrDying()) {
				return false;
			} else {
				this.removeEntitiesOnShoulder();
				float g = f;
				if (damageSource.scalesWithDifficulty()) {
					if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
						f = 0.0F;
					}

					if (this.level.getDifficulty() == Difficulty.EASY) {
						f = Math.min(f / 2.0F + 1.0F, f);
					}

					if (this.level.getDifficulty() == Difficulty.HARD) {
						f = f * 3.0F / 2.0F;
					}
				}

				return f == 0.0F && g > 0.0F ? false : super.hurt(damageSource, f);
			}
		}
	}

	@Override
	protected void blockUsingShield(LivingEntity livingEntity) {
		super.blockUsingShield(livingEntity);
	}

	@Override
	protected void blockedByShield(LivingEntity livingEntity) {
		super.blockedByShield(livingEntity);
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
		this.inventory.hurtArmor(damageSource, f);
	}

	@Override
	protected void hurtCurrentlyUsedShield(float f) {
		ItemStack itemStack = this.useItem;
		InteractionHand interactionHand = this.getUsedItemHand();
		if (itemStack.getItem() != Items.SHIELD) {
			itemStack = this.getOffhandItem();
			interactionHand = InteractionHand.OFF_HAND;
		}

		if (itemStack.getItem() == Items.SHIELD) {
			if (!this.level.isClientSide) {
				this.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
			}

			if (f >= 3.0F) {
				int i = 1 + Mth.floor(f);
				InteractionHand interactionHand2 = interactionHand;
				itemStack.hurtAndBreak(i, this, player -> player.broadcastBreakEvent(interactionHand2));
				if (itemStack.isEmpty()) {
					if (interactionHand == InteractionHand.MAIN_HAND) {
						this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					} else {
						this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
					}

					this.useItem = ItemStack.EMPTY;
					this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
				}
			}
		}
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float f) {
		if (!this.isInvulnerableTo(damageSource)) {
			f = this.getDamageAfterArmorAbsorb(damageSource, f);
			f = this.getDamageAfterMagicAbsorb(damageSource, f);
			float var8 = Math.max(f - this.getAbsorptionAmount(), 0.0F);
			this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - var8));
			float h = f - var8;
			if (h > 0.0F && h < 3.4028235E37F) {
				this.awardStat(Stats.DAMAGE_ABSORBED, Math.round(h * 10.0F));
			}

			if (var8 != 0.0F) {
				this.causeFoodExhaustion(damageSource.getFoodExhaustion());
				float i = this.getHealth();
				this.setHealth(this.getHealth() - var8);
				this.getCombatTracker().recordDamage(damageSource, i, var8);
				if (var8 < 3.4028235E37F) {
					this.awardStat(Stats.DAMAGE_TAKEN, Math.round(var8 * 10.0F));
				}
			}
		}
	}

	@Override
	protected boolean onSoulSpeedBlock() {
		return !this.abilities.flying && super.onSoulSpeedBlock();
	}

	public void openTextEdit(SignBlockEntity signBlockEntity) {
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
	public double getMyRidingOffset() {
		return -0.35;
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
		if (!this.abilities.flying && (moverType == MoverType.SELF || moverType == MoverType.PLAYER) && this.isStayingOnGroundSurface() && this.isAboveGround()) {
			double d = vec3.x;
			double e = vec3.z;
			double f = 0.05;

			while (d != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(d, (double)(-this.maxUpStep), 0.0))) {
				if (d < 0.05 && d >= -0.05) {
					d = 0.0;
				} else if (d > 0.0) {
					d -= 0.05;
				} else {
					d += 0.05;
				}
			}

			while (e != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(0.0, (double)(-this.maxUpStep), e))) {
				if (e < 0.05 && e >= -0.05) {
					e = 0.0;
				} else if (e > 0.0) {
					e -= 0.05;
				} else {
					e += 0.05;
				}
			}

			while (d != 0.0 && e != 0.0 && this.level.noCollision(this, this.getBoundingBox().move(d, (double)(-this.maxUpStep), e))) {
				if (d < 0.05 && d >= -0.05) {
					d = 0.0;
				} else if (d > 0.0) {
					d -= 0.05;
				} else {
					d += 0.05;
				}

				if (e < 0.05 && e >= -0.05) {
					e = 0.0;
				} else if (e > 0.0) {
					e -= 0.05;
				} else {
					e += 0.05;
				}
			}

			vec3 = new Vec3(d, vec3.y, e);
		}

		return vec3;
	}

	private boolean isAboveGround() {
		return this.onGround
			|| this.fallDistance < this.maxUpStep && !this.level.noCollision(this, this.getBoundingBox().move(0.0, (double)(this.fallDistance - this.maxUpStep), 0.0));
	}

	public void attack(Entity entity) {
		if (entity.isAttackable()) {
			if (!entity.skipAttackInteraction(this)) {
				float f = this.getAttackStrengthScale(1.0F);
				if (!(f < 1.0F)) {
					float g = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
					float h;
					if (entity instanceof LivingEntity) {
						h = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), (LivingEntity)entity);
					} else {
						h = EnchantmentHelper.getDamageBonus(this.getMainHandItem(), null);
					}

					float i = this.getCurrentAttackReach(1.0F);
					this.resetAttackStrengthTicker(true);
					if (g > 0.0F || h > 0.0F) {
						boolean bl = !this.onClimbable() && !this.isInWater() && !this.hasEffect(MobEffects.BLINDNESS) && !this.isPassenger() && entity instanceof LivingEntity;
						boolean bl2 = false;
						int j = 0;
						j += EnchantmentHelper.getKnockbackBonus(this);
						if (this.isSprinting() && bl) {
							this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, this.getSoundSource(), 1.0F, 1.0F);
							j++;
							bl2 = true;
						}

						boolean bl3 = bl && this.fallDistance > 0.0F && !this.onGround;
						if (bl3) {
							g *= 1.5F;
						}

						g += h;
						boolean bl4 = !bl3 && !bl2 && this.checkSweepAttack();
						float k = 0.0F;
						boolean bl5 = false;
						int l = EnchantmentHelper.getFireAspect(this);
						if (entity instanceof LivingEntity) {
							k = ((LivingEntity)entity).getHealth();
							if (l > 0 && !entity.isOnFire()) {
								bl5 = true;
								entity.setSecondsOnFire(1);
							}
						}

						Vec3 vec3 = entity.getDeltaMovement();
						boolean bl6 = entity.hurt(DamageSource.playerAttack(this).setCritSpecial(bl3), g);
						if (bl6) {
							if (j > 0) {
								if (entity instanceof LivingEntity) {
									((LivingEntity)entity)
										.knockback((float)j * 0.5F, (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0))));
								} else {
									entity.push(
										(double)(-Mth.sin(this.yRot * (float) (Math.PI / 180.0)) * (float)j * 0.5F),
										0.1,
										(double)(Mth.cos(this.yRot * (float) (Math.PI / 180.0)) * (float)j * 0.5F)
									);
								}

								this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
								this.setSprinting(false);
							}

							if (bl4) {
								AABB aABB = entity.getBoundingBox().inflate(1.0, 0.25, 1.0);
								this.sweepAttack(aABB, i, g, entity);
							}

							if (entity instanceof ServerPlayer && entity.hurtMarked) {
								((ServerPlayer)entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
								entity.hurtMarked = false;
								entity.setDeltaMovement(vec3);
							}

							if (bl3) {
								this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.getSoundSource(), 1.0F, 1.0F);
								this.crit(entity);
							}

							if (!bl3 && !bl4) {
								if (bl) {
									this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
								} else {
									this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 1.0F, 1.0F);
								}
							}

							if (h > 0.0F) {
								this.magicCrit(entity);
							}

							this.setLastHurtMob(entity);
							if (entity instanceof LivingEntity) {
								EnchantmentHelper.doPostHurtEffects((LivingEntity)entity, this);
							}

							EnchantmentHelper.doPostDamageEffects(this, entity);
							ItemStack itemStack = this.getMainHandItem();
							Entity entity2 = entity;
							if (entity instanceof EnderDragonPart) {
								entity2 = ((EnderDragonPart)entity).parentMob;
							}

							if (!this.level.isClientSide && !itemStack.isEmpty() && entity2 instanceof LivingEntity) {
								itemStack.hurtEnemy((LivingEntity)entity2, this);
								if (itemStack.isEmpty()) {
									this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
								}
							}

							if (entity instanceof LivingEntity) {
								float m = k - ((LivingEntity)entity).getHealth();
								this.awardStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));
								if (l > 0) {
									entity.setSecondsOnFire(l * 4);
								}

								if (this.level instanceof ServerLevel && m > 2.0F) {
									int n = (int)((double)m * 0.5);
									((ServerLevel)this.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5), entity.getZ(), n, 0.1, 0.0, 0.1, 0.2);
								}
							}

							this.causeFoodExhaustion(0.1F);
						} else {
							this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
							if (bl5) {
								entity.clearFire();
							}
						}
					}
				}
			}
		}
	}

	public void attackAir() {
		float f = this.getAttackStrengthScale(1.0F);
		if (!(f < 1.0F)) {
			this.swing(InteractionHand.MAIN_HAND);
			this.resetAttackStrengthTicker(false);
			float g = (float)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
			if (g > 0.0F && this.checkSweepAttack()) {
				float h = this.getCurrentAttackReach(1.0F);
				double d = 2.0;
				double e = (double)(-Mth.sin(this.yRot * (float) (Math.PI / 180.0))) * 2.0;
				double i = (double)Mth.cos(this.yRot * (float) (Math.PI / 180.0)) * 2.0;
				AABB aABB = this.getBoundingBox().inflate(1.0, 0.25, 1.0).move(e, 0.0, i);
				this.sweepAttack(aABB, h, g, null);
			}
		}
	}

	@Override
	protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
		this.attack(livingEntity);
	}

	@Override
	public boolean disableShield(float f) {
		this.getCooldowns().addCooldown(Items.SHIELD, (int)(f * 20.0F));
		this.stopUsingItem();
		this.level.broadcastEntityEvent(this, (byte)30);
		return true;
	}

	public void crit(Entity entity) {
	}

	public void magicCrit(Entity entity) {
	}

	protected boolean checkSweepAttack() {
		return this.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwordItem || EnchantmentHelper.getSweepingDamageRatio(this) > 0.0F;
	}

	public void sweepAttack(AABB aABB, float f, float g, Entity entity) {
		float h = 1.0F + EnchantmentHelper.getSweepingDamageRatio(this) * g;

		for (LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, aABB)) {
			if (livingEntity != this
				&& livingEntity != entity
				&& !this.isAlliedTo(livingEntity)
				&& (!(livingEntity instanceof ArmorStand) || !((ArmorStand)livingEntity).isMarker())) {
				float i = f + livingEntity.getBbWidth() * 0.5F;
				if (this.distanceToSqr(livingEntity) < (double)(i * i)) {
					livingEntity.knockback(0.4F, (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0)), (double)(-Mth.cos(this.yRot * (float) (Math.PI / 180.0))));
					livingEntity.hurt(DamageSource.playerAttack(this), h);
				}
			}
		}

		this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
		if (this.level instanceof ServerLevel) {
			double d = (double)(-Mth.sin(this.yRot * (float) (Math.PI / 180.0)));
			double e = (double)Mth.cos(this.yRot * (float) (Math.PI / 180.0));
			((ServerLevel)this.level)
				.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getY() + (double)this.getBbHeight() * 0.5, this.getZ() + e, 0, d, 0.0, e, 0.0);
		}
	}

	@Environment(EnvType.CLIENT)
	public void respawn() {
	}

	@Override
	public void remove() {
		super.remove();
		this.inventoryMenu.removed(this);
		if (this.containerMenu != null) {
			this.containerMenu.removed(this);
		}
	}

	public boolean isLocalPlayer() {
		return false;
	}

	public GameProfile getGameProfile() {
		return this.gameProfile;
	}

	public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
		this.startSleeping(blockPos);
		this.sleepCounter = 0;
		return Either.right(Unit.INSTANCE);
	}

	public void stopSleepInBed(boolean bl, boolean bl2) {
		super.stopSleeping();
		if (this.level instanceof ServerLevel && bl2) {
			((ServerLevel)this.level).updateSleepingPlayerList();
		}

		this.sleepCounter = bl ? 0 : 100;
	}

	@Override
	public void stopSleeping() {
		this.stopSleepInBed(true, true);
	}

	public static Optional<Vec3> findRespawnPositionAndUseSpawnBlock(ServerLevel serverLevel, BlockPos blockPos, float f, boolean bl, boolean bl2) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof RespawnAnchorBlock && (Integer)blockState.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(serverLevel)) {
			Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos);
			if (!bl2 && optional.isPresent()) {
				serverLevel.setBlock(
					blockPos, blockState.setValue(RespawnAnchorBlock.CHARGE, Integer.valueOf((Integer)blockState.getValue(RespawnAnchorBlock.CHARGE) - 1)), 3
				);
			}

			return optional;
		} else if (block instanceof BedBlock && BedBlock.canSetSpawn(serverLevel)) {
			return BedBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos, f);
		} else if (!bl) {
			return Optional.empty();
		} else {
			boolean bl3 = block.isPossibleToRespawnInThis();
			boolean bl4 = serverLevel.getBlockState(blockPos.above()).getBlock().isPossibleToRespawnInThis();
			return bl3 && bl4 ? Optional.of(new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.1, (double)blockPos.getZ() + 0.5)) : Optional.empty();
		}
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

	public int awardRecipes(Collection<Recipe<?>> collection) {
		return 0;
	}

	public void awardRecipesByKey(ResourceLocation[] resourceLocations) {
	}

	public int resetRecipes(Collection<Recipe<?>> collection) {
		return 0;
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		this.awardStat(Stats.JUMP);
		if (this.isSprinting()) {
			this.causeFoodExhaustion(0.2F);
		} else {
			this.causeFoodExhaustion(0.05F);
		}
	}

	@Override
	public void travel(Vec3 vec3) {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		if (this.isSwimming() && !this.isPassenger()) {
			double g = this.getLookAngle().y;
			double h = g < -0.2 ? 0.085 : 0.06;
			if (g <= 0.0 || this.jumping || !this.level.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0 - 0.1, this.getZ())).getFluidState().isEmpty()) {
				Vec3 vec32 = this.getDeltaMovement();
				this.setDeltaMovement(vec32.add(0.0, (g - vec32.y) * h, 0.0));
			}
		}

		if (this.abilities.flying && !this.isPassenger()) {
			double g = this.getDeltaMovement().y;
			float i = this.flyingSpeed;
			this.flyingSpeed = this.abilities.getFlyingSpeed() * (float)(this.isSprinting() ? 2 : 1);
			super.travel(vec3);
			Vec3 vec33 = this.getDeltaMovement();
			this.setDeltaMovement(vec33.x, g * 0.6, vec33.z);
			this.flyingSpeed = i;
			this.fallDistance = 0.0F;
			this.setSharedFlag(7, false);
		} else {
			super.travel(vec3);
		}

		this.checkMovementStatistics(this.getX() - d, this.getY() - e, this.getZ() - f);
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
		return !this.level.getBlockState(blockPos).isSuffocating(this.level, blockPos);
	}

	@Override
	public float getSpeed() {
		return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
	}

	public void checkMovementStatistics(double d, double e, double f) {
		if (!this.isPassenger()) {
			if (this.isSwimming()) {
				int i = Math.round(Mth.sqrt(d * d + e * e + f * f) * 100.0F);
				if (i > 0) {
					this.awardStat(Stats.SWIM_ONE_CM, i);
					this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
				}
			} else if (this.isEyeInFluid(FluidTags.WATER)) {
				int i = Math.round(Mth.sqrt(d * d + e * e + f * f) * 100.0F);
				if (i > 0) {
					this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, i);
					this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
				}
			} else if (this.isInWater()) {
				int i = Math.round(Mth.sqrt(d * d + f * f) * 100.0F);
				if (i > 0) {
					this.awardStat(Stats.WALK_ON_WATER_ONE_CM, i);
					this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
				}
			} else if (this.onClimbable()) {
				if (e > 0.0) {
					this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(e * 100.0));
				}
			} else if (this.onGround) {
				int i = Math.round(Mth.sqrt(d * d + f * f) * 100.0F);
				if (i > 0) {
					if (this.isSprinting()) {
						this.awardStat(Stats.SPRINT_ONE_CM, i);
						this.causeFoodExhaustion(0.1F * (float)i * 0.01F);
					} else if (this.isCrouching()) {
						this.awardStat(Stats.CROUCH_ONE_CM, i);
						this.causeFoodExhaustion(0.0F * (float)i * 0.01F);
					} else {
						this.awardStat(Stats.WALK_ONE_CM, i);
						this.causeFoodExhaustion(0.0F * (float)i * 0.01F);
					}
				}
			} else if (this.isFallFlying()) {
				int i = Math.round(Mth.sqrt(d * d + e * e + f * f) * 100.0F);
				this.awardStat(Stats.AVIATE_ONE_CM, i);
			} else {
				int i = Math.round(Mth.sqrt(d * d + f * f) * 100.0F);
				if (i > 25) {
					this.awardStat(Stats.FLY_ONE_CM, i);
				}
			}
		}
	}

	private void checkRidingStatistics(double d, double e, double f) {
		if (this.isPassenger()) {
			int i = Math.round(Mth.sqrt(d * d + e * e + f * f) * 100.0F);
			if (i > 0) {
				Entity entity = this.getVehicle();
				if (entity instanceof AbstractMinecart) {
					this.awardStat(Stats.MINECART_ONE_CM, i);
				} else if (entity instanceof Boat) {
					this.awardStat(Stats.BOAT_ONE_CM, i);
				} else if (entity instanceof Pig) {
					this.awardStat(Stats.PIG_ONE_CM, i);
				} else if (entity instanceof AbstractHorse) {
					this.awardStat(Stats.HORSE_ONE_CM, i);
				} else if (entity instanceof Strider) {
					this.awardStat(Stats.STRIDER_ONE_CM, i);
				}
			}
		}
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		if (this.abilities.mayfly) {
			return false;
		} else {
			if (f >= 2.0F) {
				this.awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)f * 100.0));
			}

			return super.causeFallDamage(f, g);
		}
	}

	public boolean tryToStartFallFlying() {
		if (!this.onGround && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION)) {
			ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
			if (itemStack.getItem() == Items.ELYTRA && ElytraItem.isFlyEnabled(itemStack)) {
				this.startFallFlying();
				return true;
			}
		}

		return false;
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
	protected SoundEvent getFallDamageSound(int i) {
		return i > 4 ? SoundEvents.PLAYER_BIG_FALL : SoundEvents.PLAYER_SMALL_FALL;
	}

	@Override
	public void killed(ServerLevel serverLevel, LivingEntity livingEntity) {
		this.awardStat(Stats.ENTITY_KILLED.get(livingEntity.getType()));
	}

	@Override
	public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
		if (!this.abilities.flying) {
			super.makeStuckInBlock(blockState, vec3);
		}
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
		this.experienceLevel += i;
		if (this.experienceLevel < 0) {
			this.experienceLevel = 0;
			this.experienceProgress = 0.0F;
			this.totalExperience = 0;
		}

		if (i > 0 && this.experienceLevel % 5 == 0 && (float)this.lastLevelUpTime < (float)this.tickCount - 100.0F) {
			float f = this.experienceLevel > 30 ? 1.0F : (float)this.experienceLevel / 30.0F;
			this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), f * 0.75F, 1.0F);
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
			if (!this.level.isClientSide) {
				this.foodData.addExhaustion(f);
			}
		}
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
			BlockInWorld blockInWorld = new BlockInWorld(this.level, blockPos2, false);
			return itemStack.hasAdventureModePlaceTagForBlock(this.level.getTagManager(), blockInWorld);
		}
	}

	@Override
	protected int getExperienceReward(Player player) {
		if (!this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !this.isSpectator()) {
			int i = this.experienceLevel * 7;
			return i > 100 ? 100 : i;
		} else {
			return 0;
		}
	}

	@Override
	protected boolean isAlwaysExperienceDropper() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldShowName() {
		return true;
	}

	@Override
	protected boolean isMovementNoisy() {
		return !this.abilities.flying && (!this.onGround || !this.isDiscrete());
	}

	public void onUpdateAbilities() {
	}

	public void setGameMode(GameType gameType) {
	}

	@Override
	public Component getName() {
		return new TextComponent(this.gameProfile.getName());
	}

	public PlayerEnderChestContainer getEnderChestInventory() {
		return this.enderChestInventory;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			return this.inventory.getSelected();
		} else if (equipmentSlot == EquipmentSlot.OFFHAND) {
			return this.inventory.offhand.get(0);
		} else {
			return equipmentSlot.getType() == EquipmentSlot.Type.ARMOR ? this.inventory.armor.get(equipmentSlot.getIndex()) : ItemStack.EMPTY;
		}
	}

	@Override
	public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			this.playEquipSound(itemStack);
			this.inventory.items.set(this.inventory.selected, itemStack);
		} else if (equipmentSlot == EquipmentSlot.OFFHAND) {
			this.playEquipSound(itemStack);
			this.inventory.offhand.set(0, itemStack);
		} else if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
			this.playEquipSound(itemStack);
			this.inventory.armor.set(equipmentSlot.getIndex(), itemStack);
		}
	}

	public boolean addItem(ItemStack itemStack) {
		this.playEquipSound(itemStack);
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

	public boolean setEntityOnShoulder(CompoundTag compoundTag) {
		if (this.isPassenger() || !this.onGround || this.isInWater()) {
			return false;
		} else if (this.getShoulderEntityLeft().isEmpty()) {
			this.setShoulderEntityLeft(compoundTag);
			this.timeEntitySatOnShoulder = this.level.getGameTime();
			return true;
		} else if (this.getShoulderEntityRight().isEmpty()) {
			this.setShoulderEntityRight(compoundTag);
			this.timeEntitySatOnShoulder = this.level.getGameTime();
			return true;
		} else {
			return false;
		}
	}

	protected void removeEntitiesOnShoulder() {
		if (this.timeEntitySatOnShoulder + 20L < this.level.getGameTime()) {
			this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
			this.setShoulderEntityLeft(new CompoundTag());
			this.respawnEntityOnShoulder(this.getShoulderEntityRight());
			this.setShoulderEntityRight(new CompoundTag());
		}
	}

	private void respawnEntityOnShoulder(CompoundTag compoundTag) {
		if (!this.level.isClientSide && !compoundTag.isEmpty()) {
			EntityType.create(compoundTag, this.level).ifPresent(entity -> {
				if (entity instanceof TamableAnimal) {
					((TamableAnimal)entity).setOwnerUUID(this.uuid);
				}

				entity.setPos(this.getX(), this.getY() + 0.7F, this.getZ());
				((ServerLevel)this.level).addWithUUID(entity);
			});
		}
	}

	@Override
	public abstract boolean isSpectator();

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
		return this.level.getScoreboard();
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
	public float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		switch (pose) {
			case SWIMMING:
			case FALL_FLYING:
			case SPIN_ATTACK:
				return 0.4F;
			case CROUCHING:
				return 1.27F;
			default:
				return 1.62F;
		}
	}

	@Override
	public void setAbsorptionAmount(float f) {
		if (f < 0.0F) {
			f = 0.0F;
		}

		this.getEntityData().set(DATA_PLAYER_ABSORPTION_ID, f);
	}

	@Override
	public float getAbsorptionAmount() {
		return this.getEntityData().get(DATA_PLAYER_ABSORPTION_ID);
	}

	public static UUID createPlayerUUID(GameProfile gameProfile) {
		UUID uUID = gameProfile.getId();
		if (uUID == null) {
			uUID = createPlayerUUID(gameProfile.getName());
		}

		return uUID;
	}

	public static UUID createPlayerUUID(String string) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + string).getBytes(StandardCharsets.UTF_8));
	}

	@Environment(EnvType.CLIENT)
	public boolean isModelPartShown(PlayerModelPart playerModelPart) {
		return (this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) & playerModelPart.getMask()) == playerModelPart.getMask();
	}

	@Override
	public boolean setSlot(int i, ItemStack itemStack) {
		if (i >= 0 && i < this.inventory.items.size()) {
			this.inventory.setItem(i, itemStack);
			return true;
		} else {
			EquipmentSlot equipmentSlot;
			if (i == 100 + EquipmentSlot.HEAD.getIndex()) {
				equipmentSlot = EquipmentSlot.HEAD;
			} else if (i == 100 + EquipmentSlot.CHEST.getIndex()) {
				equipmentSlot = EquipmentSlot.CHEST;
			} else if (i == 100 + EquipmentSlot.LEGS.getIndex()) {
				equipmentSlot = EquipmentSlot.LEGS;
			} else if (i == 100 + EquipmentSlot.FEET.getIndex()) {
				equipmentSlot = EquipmentSlot.FEET;
			} else {
				equipmentSlot = null;
			}

			if (i == 98) {
				this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
				return true;
			} else if (i == 99) {
				this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
				return true;
			} else if (equipmentSlot == null) {
				int j = i - 200;
				if (j >= 0 && j < this.enderChestInventory.getContainerSize()) {
					this.enderChestInventory.setItem(j, itemStack);
					return true;
				} else {
					return false;
				}
			} else {
				if (!itemStack.isEmpty()) {
					if (!(itemStack.getItem() instanceof ArmorItem) && !(itemStack.getItem() instanceof ElytraItem)) {
						if (equipmentSlot != EquipmentSlot.HEAD) {
							return false;
						}
					} else if (Mob.getEquipmentSlotForItem(itemStack) != equipmentSlot) {
						return false;
					}
				}

				this.inventory.setItem(equipmentSlot.getIndex() + this.inventory.items.size(), itemStack);
				return true;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean isReducedDebugInfo() {
		return this.reducedDebugInfo;
	}

	@Environment(EnvType.CLIENT)
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

	public int getAttackDelay() {
		float f = (float)this.getAttribute(Attributes.ATTACK_SPEED).getValue() - 1.5F;
		f = Mth.clamp(f, 0.1F, 1024.0F);
		return (int)(1.0F / f * 20.0F + 0.5F);
	}

	@Override
	public float getAttackStrengthScale(float f) {
		return this.attackStrengthStartValue == 0
			? 2.0F
			: Mth.clamp(2.0F * (1.0F - ((float)this.attackStrengthTicker - f) / (float)this.attackStrengthStartValue), 0.0F, 2.0F);
	}

	public void resetAttackStrengthTicker(boolean bl) {
		int i = 8;
		if (bl) {
			i = this.getAttackDelay() * 2;
		}

		if (i > this.attackStrengthTicker) {
			this.attackStrengthStartValue = i;
			this.attackStrengthTicker = this.attackStrengthStartValue;
		}
	}

	public float getCurrentAttackReach(float f) {
		return (float)this.getAttribute(Attributes.ATTACK_REACH).getValue();
	}

	public ItemCooldowns getCooldowns() {
		return this.cooldowns;
	}

	@Override
	protected float getBlockSpeedFactor() {
		return !this.abilities.flying && !this.isFallFlying() ? super.getBlockSpeedFactor() : 1.0F;
	}

	@Override
	public boolean isItemOnCooldown(ItemStack itemStack) {
		return this.cooldowns.isOnCooldown(itemStack.getItem());
	}

	public float getLuck() {
		return (float)this.getAttributeValue(Attributes.LUCK);
	}

	public boolean canUseGameMasterBlocks() {
		return this.abilities.instabuild && this.getPermissionLevel() >= 2;
	}

	@Override
	public boolean canTakeItem(ItemStack itemStack) {
		EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
		return this.getItemBySlot(equipmentSlot).isEmpty();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
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
	public ItemStack eat(Level level, ItemStack itemStack) {
		this.getFoodData().eat(itemStack.getItem(), itemStack);
		this.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
		if (this instanceof ServerPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)this, itemStack);
		}

		return super.eat(level, itemStack);
	}

	@Override
	protected boolean shouldRemoveSoulSpeed(BlockState blockState) {
		return this.abilities.flying || super.shouldRemoveSoulSpeed(blockState);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getRopeHoldPosition(float f) {
		double d = 0.22 * (this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0);
		float g = Mth.lerp(f * 0.5F, this.xRot, this.xRotO) * (float) (Math.PI / 180.0);
		float h = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * (float) (Math.PI / 180.0);
		if (this.isFallFlying() || this.isAutoSpinAttack()) {
			Vec3 vec3 = this.getViewVector(f);
			Vec3 vec32 = this.getDeltaMovement();
			double e = Entity.getHorizontalDistanceSqr(vec32);
			double i = Entity.getHorizontalDistanceSqr(vec3);
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
	public boolean hasEnabledShieldOnCrouch() {
		return this.enableShieldOnCrouch;
	}

	public static enum BedSleepingProblem {
		NOT_POSSIBLE_HERE,
		NOT_POSSIBLE_NOW(new TranslatableComponent("block.minecraft.bed.no_sleep")),
		TOO_FAR_AWAY(new TranslatableComponent("block.minecraft.bed.too_far_away")),
		OBSTRUCTED(new TranslatableComponent("block.minecraft.bed.obstructed")),
		OTHER_PROBLEM,
		NOT_SAFE(new TranslatableComponent("block.minecraft.bed.not_safe"));

		@Nullable
		private final Component message;

		private BedSleepingProblem() {
			this.message = null;
		}

		private BedSleepingProblem(Component component) {
			this.message = component;
		}

		@Nullable
		public Component getMessage() {
			return this.message;
		}
	}
}
