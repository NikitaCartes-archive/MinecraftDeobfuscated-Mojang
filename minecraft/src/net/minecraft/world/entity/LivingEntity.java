package net.minecraft.world.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

public abstract class LivingEntity extends Entity {
	private static final UUID SPEED_MODIFIER_SPRINTING_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
	private static final UUID SPEED_MODIFIER_SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
	private static final UUID SPEED_MODIFIER_POWDER_SNOW_UUID = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce");
	private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(
		SPEED_MODIFIER_SPRINTING_UUID, "Sprinting speed boost", 0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL
	);
	public static final int HAND_SLOTS = 2;
	public static final int ARMOR_SLOTS = 4;
	public static final int EQUIPMENT_SLOT_OFFSET = 98;
	public static final int ARMOR_SLOT_OFFSET = 100;
	public static final int SWING_DURATION = 6;
	public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
	private static final int DAMAGE_SOURCE_TIMEOUT = 40;
	public static final double MIN_MOVEMENT_DISTANCE = 0.003;
	public static final double DEFAULT_BASE_GRAVITY = 0.08;
	public static final int DEATH_DURATION = 20;
	private static final int WAIT_TICKS_BEFORE_ITEM_USE_EFFECTS = 7;
	private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
	private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
	public static final int USE_ITEM_INTERVAL = 4;
	protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
	protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
	protected static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
	protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_EFFECT_COLOR_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(
		LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
	);
	protected static final float DEFAULT_EYE_HEIGHT = 1.74F;
	protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2F, 0.2F);
	public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5F;
	private final AttributeMap attributes;
	private final CombatTracker combatTracker = new CombatTracker(this);
	private final Map<MobEffect, MobEffectInstance> activeEffects = Maps.<MobEffect, MobEffectInstance>newHashMap();
	private final NonNullList<ItemStack> lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
	private final NonNullList<ItemStack> lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
	public boolean swinging;
	private boolean discardFriction = false;
	public InteractionHand swingingArm;
	public int swingTime;
	public int removeArrowTime;
	public int removeStingerTime;
	public int hurtTime;
	public int hurtDuration;
	public float hurtDir;
	public int deathTime;
	public float oAttackAnim;
	public float attackAnim;
	protected int attackStrengthTicker;
	public float animationSpeedOld;
	public float animationSpeed;
	public float animationPosition;
	public final int invulnerableDuration = 20;
	public final float timeOffs;
	public final float rotA;
	public float yBodyRot;
	public float yBodyRotO;
	public float yHeadRot;
	public float yHeadRotO;
	public float flyingSpeed = 0.02F;
	@Nullable
	protected Player lastHurtByPlayer;
	protected int lastHurtByPlayerTime;
	protected boolean dead;
	protected int noActionTime;
	protected float oRun;
	protected float run;
	protected float animStep;
	protected float animStepO;
	protected float rotOffs;
	protected int deathScore;
	protected float lastHurt;
	protected boolean jumping;
	public float xxa;
	public float yya;
	public float zza;
	protected int lerpSteps;
	protected double lerpX;
	protected double lerpY;
	protected double lerpZ;
	protected double lerpYRot;
	protected double lerpXRot;
	protected double lyHeadRot;
	protected int lerpHeadSteps;
	private boolean effectsDirty = true;
	@Nullable
	private LivingEntity lastHurtByMob;
	private int lastHurtByMobTimestamp;
	private LivingEntity lastHurtMob;
	private int lastHurtMobTimestamp;
	private float speed;
	private int noJumpDelay;
	private float absorptionAmount;
	protected ItemStack useItem = ItemStack.EMPTY;
	protected int useItemRemaining;
	protected int fallFlyTicks;
	private BlockPos lastPos;
	private Optional<BlockPos> lastClimbablePos = Optional.empty();
	@Nullable
	private DamageSource lastDamageSource;
	private long lastDamageStamp;
	protected int autoSpinAttackTicks;
	private float swimAmount;
	private float swimAmountO;
	protected Brain<?> brain;

	protected LivingEntity(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
		this.attributes = new AttributeMap(DefaultAttributes.getSupplier(entityType));
		this.setHealth(this.getMaxHealth());
		this.blocksBuilding = true;
		this.rotA = (float)((Math.random() + 1.0) * 0.01F);
		this.reapplyPosition();
		this.timeOffs = (float)Math.random() * 12398.0F;
		this.yRot = (float)(Math.random() * (float) (Math.PI * 2));
		this.yHeadRot = this.yRot;
		this.maxUpStep = 0.6F;
		NbtOps nbtOps = NbtOps.INSTANCE;
		this.brain = this.makeBrain(new Dynamic<>(nbtOps, nbtOps.createMap(ImmutableMap.of(nbtOps.createString("memories"), nbtOps.emptyMap()))));
	}

	public Brain<?> getBrain() {
		return this.brain;
	}

	protected Brain.Provider<?> brainProvider() {
		return Brain.provider(ImmutableList.of(), ImmutableList.of());
	}

	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return this.brainProvider().makeBrain(dynamic);
	}

	@Override
	public void kill() {
		this.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
	}

	public boolean canAttackType(EntityType<?> entityType) {
		return true;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
		this.entityData.define(DATA_EFFECT_COLOR_ID, 0);
		this.entityData.define(DATA_EFFECT_AMBIENCE_ID, false);
		this.entityData.define(DATA_ARROW_COUNT_ID, 0);
		this.entityData.define(DATA_STINGER_COUNT_ID, 0);
		this.entityData.define(DATA_HEALTH_ID, 1.0F);
		this.entityData.define(SLEEPING_POS_ID, Optional.empty());
	}

	public static AttributeSupplier.Builder createLivingAttributes() {
		return AttributeSupplier.builder()
			.add(Attributes.MAX_HEALTH)
			.add(Attributes.KNOCKBACK_RESISTANCE)
			.add(Attributes.MOVEMENT_SPEED)
			.add(Attributes.ARMOR)
			.add(Attributes.ARMOR_TOUGHNESS);
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		if (!this.isInWater()) {
			this.updateInWaterStateAndDoWaterCurrentPushing();
		}

		if (!this.level.isClientSide && bl && this.fallDistance > 0.0F) {
			this.removeSoulSpeed();
			this.tryAddSoulSpeed();
		}

		if (!this.level.isClientSide && this.fallDistance > 3.0F && bl) {
			float f = (float)Mth.ceil(this.fallDistance - 3.0F);
			if (!blockState.isAir()) {
				double e = Math.min((double)(0.2F + f / 15.0F), 2.5);
				int i = (int)(150.0 * e);
				((ServerLevel)this.level)
					.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), this.getX(), this.getY(), this.getZ(), i, 0.0, 0.0, 0.0, 0.15F);
			}
		}

		super.checkFallDamage(d, bl, blockState, blockPos);
	}

	public boolean canBreatheUnderwater() {
		return this.getMobType() == MobType.UNDEAD;
	}

	public float getSwimAmount(float f) {
		return Mth.lerp(f, this.swimAmountO, this.swimAmount);
	}

	@Override
	public void baseTick() {
		this.oAttackAnim = this.attackAnim;
		if (this.firstTick) {
			this.getSleepingPos().ifPresent(this::setPosToBed);
		}

		if (this.canSpawnSoulSpeedParticle()) {
			this.spawnSoulSpeedParticle();
		}

		super.baseTick();
		this.level.getProfiler().push("livingEntityBaseTick");
		boolean bl = this instanceof Player;
		if (this.isAlive()) {
			if (this.isInWall()) {
				this.hurt(DamageSource.IN_WALL, 1.0F);
			} else if (bl && !this.level.getWorldBorder().isWithinBounds(this.getBoundingBox())) {
				double d = this.level.getWorldBorder().getDistanceToBorder(this) + this.level.getWorldBorder().getDamageSafeZone();
				if (d < 0.0) {
					double e = this.level.getWorldBorder().getDamagePerBlock();
					if (e > 0.0) {
						this.hurt(DamageSource.IN_WALL, (float)Math.max(1, Mth.floor(-d * e)));
					}
				}
			}
		}

		if (this.fireImmune() || this.level.isClientSide) {
			this.clearFire();
		}

		boolean bl2 = bl && ((Player)this).getAbilities().invulnerable;
		if (this.isAlive()) {
			if (this.isEyeInFluid(FluidTags.WATER) && !this.level.getBlockState(new BlockPos(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
				if (!this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && !bl2) {
					this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
					if (this.getAirSupply() == -20) {
						this.setAirSupply(0);
						Vec3 vec3 = this.getDeltaMovement();

						for (int i = 0; i < 8; i++) {
							double f = this.random.nextDouble() - this.random.nextDouble();
							double g = this.random.nextDouble() - this.random.nextDouble();
							double h = this.random.nextDouble() - this.random.nextDouble();
							this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + f, this.getY() + g, this.getZ() + h, vec3.x, vec3.y, vec3.z);
						}

						this.hurt(DamageSource.DROWN, 2.0F);
					}
				}

				if (!this.level.isClientSide && this.isPassenger() && this.getVehicle() != null && !this.getVehicle().rideableUnderWater()) {
					this.stopRiding();
				}
			} else if (this.getAirSupply() < this.getMaxAirSupply()) {
				this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
			}

			if (!this.level.isClientSide) {
				BlockPos blockPos = this.blockPosition();
				if (!Objects.equal(this.lastPos, blockPos)) {
					this.lastPos = blockPos;
					this.onChangedBlock(blockPos);
				}
			}
		}

		if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow)) {
			this.clearFire();
		}

		if (this.hurtTime > 0) {
			this.hurtTime--;
		}

		if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
			this.invulnerableTime--;
		}

		if (this.isDeadOrDying()) {
			this.tickDeath();
		}

		if (this.lastHurtByPlayerTime > 0) {
			this.lastHurtByPlayerTime--;
		} else {
			this.lastHurtByPlayer = null;
		}

		if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
			this.lastHurtMob = null;
		}

		if (this.lastHurtByMob != null) {
			if (!this.lastHurtByMob.isAlive()) {
				this.setLastHurtByMob(null);
			} else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
				this.setLastHurtByMob(null);
			}
		}

		this.tickEffects();
		this.animStepO = this.animStep;
		this.yBodyRotO = this.yBodyRot;
		this.yHeadRotO = this.yHeadRot;
		this.yRotO = this.yRot;
		this.xRotO = this.xRot;
		this.level.getProfiler().pop();
	}

	public boolean canSpawnSoulSpeedParticle() {
		return this.tickCount % 5 == 0
			&& this.getDeltaMovement().x != 0.0
			&& this.getDeltaMovement().z != 0.0
			&& !this.isSpectator()
			&& EnchantmentHelper.hasSoulSpeed(this)
			&& this.onSoulSpeedBlock();
	}

	protected void spawnSoulSpeedParticle() {
		Vec3 vec3 = this.getDeltaMovement();
		this.level
			.addParticle(
				ParticleTypes.SOUL,
				this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
				this.getY() + 0.1,
				this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
				vec3.x * -0.2,
				0.1,
				vec3.z * -0.2
			);
		float f = this.random.nextFloat() * 0.4F + this.random.nextFloat() > 0.9F ? 0.6F : 0.0F;
		this.playSound(SoundEvents.SOUL_ESCAPE, f, 0.6F + this.random.nextFloat() * 0.4F);
	}

	protected boolean onSoulSpeedBlock() {
		return this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.SOUL_SPEED_BLOCKS);
	}

	@Override
	protected float getBlockSpeedFactor() {
		return this.onSoulSpeedBlock() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, this) > 0 ? 1.0F : super.getBlockSpeedFactor();
	}

	protected boolean shouldRemoveSoulSpeed(BlockState blockState) {
		return !blockState.isAir() || this.isFallFlying();
	}

	protected void removeSoulSpeed() {
		AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attributeInstance != null) {
			if (attributeInstance.getModifier(SPEED_MODIFIER_SOUL_SPEED_UUID) != null) {
				attributeInstance.removeModifier(SPEED_MODIFIER_SOUL_SPEED_UUID);
			}
		}
	}

	protected void tryAddSoulSpeed() {
		if (!this.getBlockStateOn().isAir()) {
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED, this);
			if (i > 0 && this.onSoulSpeedBlock()) {
				AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
				if (attributeInstance == null) {
					return;
				}

				attributeInstance.addTransientModifier(
					new AttributeModifier(
						SPEED_MODIFIER_SOUL_SPEED_UUID, "Soul speed boost", (double)(0.03F * (1.0F + (float)i * 0.35F)), AttributeModifier.Operation.ADDITION
					)
				);
				if (this.getRandom().nextFloat() < 0.04F) {
					ItemStack itemStack = this.getItemBySlot(EquipmentSlot.FEET);
					itemStack.hurtAndBreak(1, this, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.FEET));
				}
			}
		}
	}

	protected void removeFrost() {
		AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attributeInstance != null) {
			if (attributeInstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_UUID) != null) {
				attributeInstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_UUID);
			}
		}
	}

	protected void tryAddFrost() {
		if (!this.getBlockStateOn().isAir()) {
			int i = this.getTicksFrozen();
			if (i > 0) {
				AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
				if (attributeInstance == null) {
					return;
				}

				float f = -0.05F * this.getPercentFrozen();
				attributeInstance.addTransientModifier(
					new AttributeModifier(SPEED_MODIFIER_POWDER_SNOW_UUID, "Powder snow slow", (double)f, AttributeModifier.Operation.ADDITION)
				);
			}
		}
	}

	protected void onChangedBlock(BlockPos blockPos) {
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, this);
		if (i > 0) {
			FrostWalkerEnchantment.onEntityMoved(this, this.level, blockPos, i);
		}

		if (this.shouldRemoveSoulSpeed(this.getBlockStateOn())) {
			this.removeSoulSpeed();
		}

		this.tryAddSoulSpeed();
	}

	public boolean isBaby() {
		return false;
	}

	public float getScale() {
		return this.isBaby() ? 0.5F : 1.0F;
	}

	protected boolean isAffectedByFluids() {
		return true;
	}

	@Override
	public boolean rideableUnderWater() {
		return false;
	}

	protected void tickDeath() {
		this.deathTime++;
		if (this.deathTime == 20) {
			this.remove(Entity.RemovalReason.KILLED);

			for (int i = 0; i < 20; i++) {
				double d = this.random.nextGaussian() * 0.02;
				double e = this.random.nextGaussian() * 0.02;
				double f = this.random.nextGaussian() * 0.02;
				this.level.addParticle(ParticleTypes.POOF, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), d, e, f);
			}
		}
	}

	protected boolean shouldDropExperience() {
		return !this.isBaby();
	}

	protected boolean shouldDropLoot() {
		return !this.isBaby();
	}

	protected int decreaseAirSupply(int i) {
		int j = EnchantmentHelper.getRespiration(this);
		return j > 0 && this.random.nextInt(j + 1) > 0 ? i : i - 1;
	}

	protected int increaseAirSupply(int i) {
		return Math.min(i + 4, this.getMaxAirSupply());
	}

	protected int getExperienceReward(Player player) {
		return 0;
	}

	protected boolean isAlwaysExperienceDropper() {
		return false;
	}

	public Random getRandom() {
		return this.random;
	}

	@Nullable
	public LivingEntity getLastHurtByMob() {
		return this.lastHurtByMob;
	}

	public int getLastHurtByMobTimestamp() {
		return this.lastHurtByMobTimestamp;
	}

	public void setLastHurtByPlayer(@Nullable Player player) {
		this.lastHurtByPlayer = player;
		this.lastHurtByPlayerTime = this.tickCount;
	}

	public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
		this.lastHurtByMob = livingEntity;
		this.lastHurtByMobTimestamp = this.tickCount;
	}

	@Nullable
	public LivingEntity getLastHurtMob() {
		return this.lastHurtMob;
	}

	public int getLastHurtMobTimestamp() {
		return this.lastHurtMobTimestamp;
	}

	public void setLastHurtMob(Entity entity) {
		if (entity instanceof LivingEntity) {
			this.lastHurtMob = (LivingEntity)entity;
		} else {
			this.lastHurtMob = null;
		}

		this.lastHurtMobTimestamp = this.tickCount;
	}

	public int getNoActionTime() {
		return this.noActionTime;
	}

	public void setNoActionTime(int i) {
		this.noActionTime = i;
	}

	public boolean shouldDiscardFriction() {
		return this.discardFriction;
	}

	public void setDiscardFriction(boolean bl) {
		this.discardFriction = bl;
	}

	protected void equipEventAndSound(ItemStack itemStack) {
		SoundEvent soundEvent = itemStack.getEquipSound();
		if (!itemStack.isEmpty() && soundEvent != null && !this.isSpectator()) {
			this.gameEvent(GameEvent.EQUIP);
			this.playSound(soundEvent, 1.0F, 1.0F);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putFloat("Health", this.getHealth());
		compoundTag.putShort("HurtTime", (short)this.hurtTime);
		compoundTag.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
		compoundTag.putShort("DeathTime", (short)this.deathTime);
		compoundTag.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
		compoundTag.put("Attributes", this.getAttributes().save());
		if (!this.activeEffects.isEmpty()) {
			ListTag listTag = new ListTag();

			for (MobEffectInstance mobEffectInstance : this.activeEffects.values()) {
				listTag.add(mobEffectInstance.save(new CompoundTag()));
			}

			compoundTag.put("ActiveEffects", listTag);
		}

		compoundTag.putBoolean("FallFlying", this.isFallFlying());
		this.getSleepingPos().ifPresent(blockPos -> {
			compoundTag.putInt("SleepingX", blockPos.getX());
			compoundTag.putInt("SleepingY", blockPos.getY());
			compoundTag.putInt("SleepingZ", blockPos.getZ());
		});
		DataResult<Tag> dataResult = this.brain.serializeStart(NbtOps.INSTANCE);
		dataResult.resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("Brain", tag));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setAbsorptionAmount(compoundTag.getFloat("AbsorptionAmount"));
		if (compoundTag.contains("Attributes", 9) && this.level != null && !this.level.isClientSide) {
			this.getAttributes().load(compoundTag.getList("Attributes", 10));
		}

		if (compoundTag.contains("ActiveEffects", 9)) {
			ListTag listTag = compoundTag.getList("ActiveEffects", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				MobEffectInstance mobEffectInstance = MobEffectInstance.load(compoundTag2);
				if (mobEffectInstance != null) {
					this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
				}
			}
		}

		if (compoundTag.contains("Health", 99)) {
			this.setHealth(compoundTag.getFloat("Health"));
		}

		this.hurtTime = compoundTag.getShort("HurtTime");
		this.deathTime = compoundTag.getShort("DeathTime");
		this.lastHurtByMobTimestamp = compoundTag.getInt("HurtByTimestamp");
		if (compoundTag.contains("Team", 8)) {
			String string = compoundTag.getString("Team");
			PlayerTeam playerTeam = this.level.getScoreboard().getPlayerTeam(string);
			boolean bl = playerTeam != null && this.level.getScoreboard().addPlayerToTeam(this.getStringUUID(), playerTeam);
			if (!bl) {
				LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", string);
			}
		}

		if (compoundTag.getBoolean("FallFlying")) {
			this.setSharedFlag(7, true);
		}

		if (compoundTag.contains("SleepingX", 99) && compoundTag.contains("SleepingY", 99) && compoundTag.contains("SleepingZ", 99)) {
			BlockPos blockPos = new BlockPos(compoundTag.getInt("SleepingX"), compoundTag.getInt("SleepingY"), compoundTag.getInt("SleepingZ"));
			this.setSleepingPos(blockPos);
			this.entityData.set(DATA_POSE, Pose.SLEEPING);
			if (!this.firstTick) {
				this.setPosToBed(blockPos);
			}
		}

		if (compoundTag.contains("Brain", 10)) {
			this.brain = this.makeBrain(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("Brain")));
		}
	}

	protected void tickEffects() {
		Iterator<MobEffect> iterator = this.activeEffects.keySet().iterator();

		try {
			while (iterator.hasNext()) {
				MobEffect mobEffect = (MobEffect)iterator.next();
				MobEffectInstance mobEffectInstance = (MobEffectInstance)this.activeEffects.get(mobEffect);
				if (!mobEffectInstance.tick(this, () -> this.onEffectUpdated(mobEffectInstance, true))) {
					if (!this.level.isClientSide) {
						iterator.remove();
						this.onEffectRemoved(mobEffectInstance);
					}
				} else if (mobEffectInstance.getDuration() % 600 == 0) {
					this.onEffectUpdated(mobEffectInstance, false);
				}
			}
		} catch (ConcurrentModificationException var11) {
		}

		if (this.effectsDirty) {
			if (!this.level.isClientSide) {
				this.updateInvisibilityStatus();
			}

			this.effectsDirty = false;
		}

		int i = this.entityData.get(DATA_EFFECT_COLOR_ID);
		boolean bl = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
		if (i > 0) {
			boolean bl2;
			if (this.isInvisible()) {
				bl2 = this.random.nextInt(15) == 0;
			} else {
				bl2 = this.random.nextBoolean();
			}

			if (bl) {
				bl2 &= this.random.nextInt(5) == 0;
			}

			if (bl2 && i > 0) {
				double d = (double)(i >> 16 & 0xFF) / 255.0;
				double e = (double)(i >> 8 & 0xFF) / 255.0;
				double f = (double)(i >> 0 & 0xFF) / 255.0;
				this.level
					.addParticle(
						bl ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), d, e, f
					);
			}
		}
	}

	protected void updateInvisibilityStatus() {
		if (this.activeEffects.isEmpty()) {
			this.removeEffectParticles();
			this.setInvisible(false);
		} else {
			Collection<MobEffectInstance> collection = this.activeEffects.values();
			this.entityData.set(DATA_EFFECT_AMBIENCE_ID, areAllEffectsAmbient(collection));
			this.entityData.set(DATA_EFFECT_COLOR_ID, PotionUtils.getColor(collection));
			this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
		}
	}

	public double getVisibilityPercent(@Nullable Entity entity) {
		double d = 1.0;
		if (this.isDiscrete()) {
			d *= 0.8;
		}

		if (this.isInvisible()) {
			float f = this.getArmorCoverPercentage();
			if (f < 0.1F) {
				f = 0.1F;
			}

			d *= 0.7 * (double)f;
		}

		if (entity != null) {
			ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
			EntityType<?> entityType = entity.getType();
			if (entityType == EntityType.SKELETON && itemStack.is(Items.SKELETON_SKULL)
				|| entityType == EntityType.ZOMBIE && itemStack.is(Items.ZOMBIE_HEAD)
				|| entityType == EntityType.CREEPER && itemStack.is(Items.CREEPER_HEAD)) {
				d *= 0.5;
			}
		}

		return d;
	}

	public boolean canAttack(LivingEntity livingEntity) {
		return true;
	}

	public boolean canAttack(LivingEntity livingEntity, TargetingConditions targetingConditions) {
		return targetingConditions.test(this, livingEntity);
	}

	public boolean canBeTargeted() {
		return true;
	}

	public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> collection) {
		for (MobEffectInstance mobEffectInstance : collection) {
			if (!mobEffectInstance.isAmbient()) {
				return false;
			}
		}

		return true;
	}

	protected void removeEffectParticles() {
		this.entityData.set(DATA_EFFECT_AMBIENCE_ID, false);
		this.entityData.set(DATA_EFFECT_COLOR_ID, 0);
	}

	public boolean removeAllEffects() {
		if (this.level.isClientSide) {
			return false;
		} else {
			Iterator<MobEffectInstance> iterator = this.activeEffects.values().iterator();

			boolean bl;
			for (bl = false; iterator.hasNext(); bl = true) {
				this.onEffectRemoved((MobEffectInstance)iterator.next());
				iterator.remove();
			}

			return bl;
		}
	}

	public Collection<MobEffectInstance> getActiveEffects() {
		return this.activeEffects.values();
	}

	public Map<MobEffect, MobEffectInstance> getActiveEffectsMap() {
		return this.activeEffects;
	}

	public boolean hasEffect(MobEffect mobEffect) {
		return this.activeEffects.containsKey(mobEffect);
	}

	@Nullable
	public MobEffectInstance getEffect(MobEffect mobEffect) {
		return (MobEffectInstance)this.activeEffects.get(mobEffect);
	}

	public boolean addEffect(MobEffectInstance mobEffectInstance) {
		if (!this.canBeAffected(mobEffectInstance)) {
			return false;
		} else {
			MobEffectInstance mobEffectInstance2 = (MobEffectInstance)this.activeEffects.get(mobEffectInstance.getEffect());
			if (mobEffectInstance2 == null) {
				this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
				this.onEffectAdded(mobEffectInstance);
				return true;
			} else if (mobEffectInstance2.update(mobEffectInstance)) {
				this.onEffectUpdated(mobEffectInstance2, true);
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
		if (this.getMobType() == MobType.UNDEAD) {
			MobEffect mobEffect = mobEffectInstance.getEffect();
			if (mobEffect == MobEffects.REGENERATION || mobEffect == MobEffects.POISON) {
				return false;
			}
		}

		return true;
	}

	public void forceAddEffect(MobEffectInstance mobEffectInstance) {
		if (this.canBeAffected(mobEffectInstance)) {
			MobEffectInstance mobEffectInstance2 = (MobEffectInstance)this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
			if (mobEffectInstance2 == null) {
				this.onEffectAdded(mobEffectInstance);
			} else {
				this.onEffectUpdated(mobEffectInstance, true);
			}
		}
	}

	public boolean isInvertedHealAndHarm() {
		return this.getMobType() == MobType.UNDEAD;
	}

	@Nullable
	public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect mobEffect) {
		return (MobEffectInstance)this.activeEffects.remove(mobEffect);
	}

	public boolean removeEffect(MobEffect mobEffect) {
		MobEffectInstance mobEffectInstance = this.removeEffectNoUpdate(mobEffect);
		if (mobEffectInstance != null) {
			this.onEffectRemoved(mobEffectInstance);
			return true;
		} else {
			return false;
		}
	}

	protected void onEffectAdded(MobEffectInstance mobEffectInstance) {
		this.effectsDirty = true;
		if (!this.level.isClientSide) {
			mobEffectInstance.getEffect().addAttributeModifiers(this, this.getAttributes(), mobEffectInstance.getAmplifier());
		}
	}

	protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl) {
		this.effectsDirty = true;
		if (bl && !this.level.isClientSide) {
			MobEffect mobEffect = mobEffectInstance.getEffect();
			mobEffect.removeAttributeModifiers(this, this.getAttributes(), mobEffectInstance.getAmplifier());
			mobEffect.addAttributeModifiers(this, this.getAttributes(), mobEffectInstance.getAmplifier());
		}
	}

	protected void onEffectRemoved(MobEffectInstance mobEffectInstance) {
		this.effectsDirty = true;
		if (!this.level.isClientSide) {
			mobEffectInstance.getEffect().removeAttributeModifiers(this, this.getAttributes(), mobEffectInstance.getAmplifier());
		}
	}

	public void heal(float f) {
		float g = this.getHealth();
		if (g > 0.0F) {
			this.setHealth(g + f);
		}
	}

	public float getHealth() {
		return this.entityData.get(DATA_HEALTH_ID);
	}

	public void setHealth(float f) {
		this.entityData.set(DATA_HEALTH_ID, Mth.clamp(f, 0.0F, this.getMaxHealth()));
	}

	public boolean isDeadOrDying() {
		return this.getHealth() <= 0.0F;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (this.level.isClientSide) {
			return false;
		} else if (this.isDeadOrDying()) {
			return false;
		} else if (damageSource.isFire() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			return false;
		} else {
			if (this.isSleeping() && !this.level.isClientSide) {
				this.stopSleeping();
			}

			this.noActionTime = 0;
			float g = f;
			if (damageSource.isDamageHelmet() && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				this.getItemBySlot(EquipmentSlot.HEAD)
					.hurtAndBreak((int)(f * 4.0F + this.random.nextFloat() * f * 2.0F), this, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.HEAD));
				f *= 0.75F;
			}

			boolean bl = false;
			float h = 0.0F;
			if (f > 0.0F && this.isDamageSourceBlocked(damageSource)) {
				this.hurtCurrentlyUsedShield(f);
				h = f;
				f = 0.0F;
				if (!damageSource.isProjectile()) {
					Entity entity = damageSource.getDirectEntity();
					if (entity instanceof LivingEntity) {
						this.blockUsingShield((LivingEntity)entity);
					}
				}

				bl = true;
			}

			this.animationSpeed = 1.5F;
			boolean bl2 = true;
			if ((float)this.invulnerableTime > 10.0F) {
				if (f <= this.lastHurt) {
					return false;
				}

				this.actuallyHurt(damageSource, f - this.lastHurt);
				this.lastHurt = f;
				bl2 = false;
			} else {
				this.lastHurt = f;
				this.invulnerableTime = 20;
				this.actuallyHurt(damageSource, f);
				this.hurtDuration = 10;
				this.hurtTime = this.hurtDuration;
			}

			this.hurtDir = 0.0F;
			Entity entity2 = damageSource.getEntity();
			if (entity2 != null) {
				if (entity2 instanceof LivingEntity) {
					this.setLastHurtByMob((LivingEntity)entity2);
				}

				if (entity2 instanceof Player) {
					this.lastHurtByPlayerTime = 100;
					this.lastHurtByPlayer = (Player)entity2;
				} else if (entity2 instanceof Wolf) {
					Wolf wolf = (Wolf)entity2;
					if (wolf.isTame()) {
						this.lastHurtByPlayerTime = 100;
						LivingEntity livingEntity = wolf.getOwner();
						if (livingEntity != null && livingEntity.getType() == EntityType.PLAYER) {
							this.lastHurtByPlayer = (Player)livingEntity;
						} else {
							this.lastHurtByPlayer = null;
						}
					}
				}
			}

			if (bl2) {
				if (bl) {
					this.level.broadcastEntityEvent(this, (byte)29);
				} else if (damageSource instanceof EntityDamageSource && ((EntityDamageSource)damageSource).isThorns()) {
					this.level.broadcastEntityEvent(this, (byte)33);
				} else {
					byte b;
					if (damageSource == DamageSource.DROWN) {
						b = 36;
					} else if (damageSource.isFire()) {
						b = 37;
					} else if (damageSource == DamageSource.SWEET_BERRY_BUSH) {
						b = 44;
					} else if (damageSource == DamageSource.FREEZE) {
						b = 57;
					} else {
						b = 2;
					}

					this.level.broadcastEntityEvent(this, b);
				}

				if (damageSource != DamageSource.DROWN && (!bl || f > 0.0F)) {
					this.markHurt();
				}

				if (entity2 != null) {
					double d = entity2.getX() - this.getX();

					double e;
					for (e = entity2.getZ() - this.getZ(); d * d + e * e < 1.0E-4; e = (Math.random() - Math.random()) * 0.01) {
						d = (Math.random() - Math.random()) * 0.01;
					}

					this.hurtDir = (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI - (double)this.yRot);
					this.knockback(0.4F, d, e);
				} else {
					this.hurtDir = (float)((int)(Math.random() * 2.0) * 180);
				}
			}

			if (this.isDeadOrDying()) {
				if (!this.checkTotemDeathProtection(damageSource)) {
					SoundEvent soundEvent = this.getDeathSound();
					if (bl2 && soundEvent != null) {
						this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
					}

					this.die(damageSource);
				}
			} else if (bl2) {
				this.playHurtSound(damageSource);
			}

			boolean bl3 = !bl || f > 0.0F;
			if (bl3) {
				this.lastDamageSource = damageSource;
				this.lastDamageStamp = this.level.getGameTime();
			}

			if (this instanceof ServerPlayer) {
				CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)this, damageSource, g, f, bl);
				if (h > 0.0F && h < 3.4028235E37F) {
					((ServerPlayer)this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(h * 10.0F));
				}
			}

			if (entity2 instanceof ServerPlayer) {
				CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity2, this, damageSource, g, f, bl);
			}

			return bl3;
		}
	}

	protected void blockUsingShield(LivingEntity livingEntity) {
		livingEntity.blockedByShield(this);
	}

	protected void blockedByShield(LivingEntity livingEntity) {
		livingEntity.knockback(0.5F, livingEntity.getX() - this.getX(), livingEntity.getZ() - this.getZ());
	}

	private boolean checkTotemDeathProtection(DamageSource damageSource) {
		if (damageSource.isBypassInvul()) {
			return false;
		} else {
			ItemStack itemStack = null;

			for (InteractionHand interactionHand : InteractionHand.values()) {
				ItemStack itemStack2 = this.getItemInHand(interactionHand);
				if (itemStack2.is(Items.TOTEM_OF_UNDYING)) {
					itemStack = itemStack2.copy();
					itemStack2.shrink(1);
					break;
				}
			}

			if (itemStack != null) {
				if (this instanceof ServerPlayer) {
					ServerPlayer serverPlayer = (ServerPlayer)this;
					serverPlayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
					CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, itemStack);
				}

				this.setHealth(1.0F);
				this.removeAllEffects();
				this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
				this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
				this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
				this.level.broadcastEntityEvent(this, (byte)35);
			}

			return itemStack != null;
		}
	}

	@Nullable
	public DamageSource getLastDamageSource() {
		if (this.level.getGameTime() - this.lastDamageStamp > 40L) {
			this.lastDamageSource = null;
		}

		return this.lastDamageSource;
	}

	protected void playHurtSound(DamageSource damageSource) {
		SoundEvent soundEvent = this.getHurtSound(damageSource);
		if (soundEvent != null) {
			this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
		}
	}

	public boolean isDamageSourceBlocked(DamageSource damageSource) {
		Entity entity = damageSource.getDirectEntity();
		boolean bl = false;
		if (entity instanceof AbstractArrow) {
			AbstractArrow abstractArrow = (AbstractArrow)entity;
			if (abstractArrow.getPierceLevel() > 0) {
				bl = true;
			}
		}

		if (!damageSource.isBypassArmor() && this.isBlocking() && !bl) {
			Vec3 vec3 = damageSource.getSourcePosition();
			if (vec3 != null) {
				Vec3 vec32 = this.getViewVector(1.0F);
				Vec3 vec33 = vec3.vectorTo(this.position()).normalize();
				vec33 = new Vec3(vec33.x, 0.0, vec33.z);
				if (vec33.dot(vec32) < 0.0) {
					return true;
				}
			}
		}

		return false;
	}

	private void breakItem(ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			if (!this.isSilent()) {
				this.level
					.playLocalSound(
						this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, this.getSoundSource(), 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F, false
					);
			}

			this.spawnItemParticles(itemStack, 5);
		}
	}

	public void die(DamageSource damageSource) {
		if (!this.isRemoved() && !this.dead) {
			Entity entity = damageSource.getEntity();
			LivingEntity livingEntity = this.getKillCredit();
			if (this.deathScore >= 0 && livingEntity != null) {
				livingEntity.awardKillScore(this, this.deathScore, damageSource);
			}

			if (this.isSleeping()) {
				this.stopSleeping();
			}

			this.dead = true;
			this.getCombatTracker().recheckStatus();
			if (this.level instanceof ServerLevel) {
				if (entity != null) {
					entity.killed((ServerLevel)this.level, this);
				}

				this.dropAllDeathLoot(damageSource);
				this.createWitherRose(livingEntity);
			}

			this.level.broadcastEntityEvent(this, (byte)3);
			this.setPose(Pose.DYING);
		}
	}

	protected void createWitherRose(@Nullable LivingEntity livingEntity) {
		if (!this.level.isClientSide) {
			boolean bl = false;
			if (livingEntity instanceof WitherBoss) {
				if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
					BlockPos blockPos = this.blockPosition();
					BlockState blockState = Blocks.WITHER_ROSE.defaultBlockState();
					if (this.level.getBlockState(blockPos).isAir() && blockState.canSurvive(this.level, blockPos)) {
						this.level.setBlock(blockPos, blockState, 3);
						bl = true;
					}
				}

				if (!bl) {
					ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
					this.level.addFreshEntity(itemEntity);
				}
			}
		}
	}

	protected void dropAllDeathLoot(DamageSource damageSource) {
		Entity entity = damageSource.getEntity();
		int i;
		if (entity instanceof Player) {
			i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
		} else {
			i = 0;
		}

		boolean bl = this.lastHurtByPlayerTime > 0;
		if (this.shouldDropLoot() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			this.dropFromLootTable(damageSource, bl);
			this.dropCustomDeathLoot(damageSource, i, bl);
		}

		this.dropEquipment();
		this.dropExperience();
	}

	protected void dropEquipment() {
	}

	protected void dropExperience() {
		if (this.level instanceof ServerLevel
			&& (
				this.isAlwaysExperienceDropper()
					|| this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
			)) {
			ExperienceOrb.award((ServerLevel)this.level, this.position(), this.getExperienceReward(this.lastHurtByPlayer));
		}
	}

	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
	}

	public ResourceLocation getLootTable() {
		return this.getType().getDefaultLootTable();
	}

	protected void dropFromLootTable(DamageSource damageSource, boolean bl) {
		ResourceLocation resourceLocation = this.getLootTable();
		LootTable lootTable = this.level.getServer().getLootTables().get(resourceLocation);
		LootContext.Builder builder = this.createLootContext(bl, damageSource);
		lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY), this::spawnAtLocation);
	}

	protected LootContext.Builder createLootContext(boolean bl, DamageSource damageSource) {
		LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.level)
			.withRandom(this.random)
			.withParameter(LootContextParams.THIS_ENTITY, this)
			.withParameter(LootContextParams.ORIGIN, this.position())
			.withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
			.withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
			.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());
		if (bl && this.lastHurtByPlayer != null) {
			builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
		}

		return builder;
	}

	public void knockback(float f, double d, double e) {
		f = (float)((double)f * (1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
		if (!(f <= 0.0F)) {
			this.hasImpulse = true;
			Vec3 vec3 = this.getDeltaMovement();
			Vec3 vec32 = new Vec3(d, 0.0, e).normalize().scale((double)f);
			this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround ? Math.min(0.4, vec3.y / 2.0 + (double)f) : vec3.y, vec3.z / 2.0 - vec32.z);
		}
	}

	@Nullable
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.GENERIC_HURT;
	}

	@Nullable
	protected SoundEvent getDeathSound() {
		return SoundEvents.GENERIC_DEATH;
	}

	protected SoundEvent getFallDamageSound(int i) {
		return i > 4 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL;
	}

	protected SoundEvent getDrinkingSound(ItemStack itemStack) {
		return itemStack.getDrinkingSound();
	}

	public SoundEvent getEatingSound(ItemStack itemStack) {
		return itemStack.getEatingSound();
	}

	@Override
	public void setOnGround(boolean bl) {
		super.setOnGround(bl);
		if (bl) {
			this.lastClimbablePos = Optional.empty();
		}
	}

	public Optional<BlockPos> getLastClimbablePos() {
		return this.lastClimbablePos;
	}

	public boolean onClimbable() {
		if (this.isSpectator()) {
			return false;
		} else {
			BlockPos blockPos = this.blockPosition();
			BlockState blockState = this.getFeetBlockState();
			if (blockState.is(BlockTags.CLIMBABLE)) {
				this.lastClimbablePos = Optional.of(blockPos);
				return true;
			} else if (blockState.getBlock() instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(blockPos, blockState)) {
				this.lastClimbablePos = Optional.of(blockPos);
				return true;
			} else {
				return false;
			}
		}
	}

	public BlockState getFeetBlockState() {
		return this.level.getBlockState(this.blockPosition());
	}

	private boolean trapdoorUsableAsLadder(BlockPos blockPos, BlockState blockState) {
		if ((Boolean)blockState.getValue(TrapDoorBlock.OPEN)) {
			BlockState blockState2 = this.level.getBlockState(blockPos.below());
			if (blockState2.is(Blocks.LADDER) && blockState2.getValue(LadderBlock.FACING) == blockState.getValue(TrapDoorBlock.FACING)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isAlive() {
		return !this.isRemoved() && this.getHealth() > 0.0F;
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		boolean bl = super.causeFallDamage(f, g, damageSource);
		int i = this.calculateFallDamage(f, g);
		if (i > 0) {
			this.playSound(this.getFallDamageSound(i), 1.0F, 1.0F);
			this.playBlockFallSound();
			this.hurt(damageSource, (float)i);
			return true;
		} else {
			return bl;
		}
	}

	protected int calculateFallDamage(float f, float g) {
		MobEffectInstance mobEffectInstance = this.getEffect(MobEffects.JUMP);
		float h = mobEffectInstance == null ? 0.0F : (float)(mobEffectInstance.getAmplifier() + 1);
		return Mth.ceil((f - 3.0F - h) * g);
	}

	protected void playBlockFallSound() {
		if (!this.isSilent()) {
			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY() - 0.2F);
			int k = Mth.floor(this.getZ());
			BlockState blockState = this.level.getBlockState(new BlockPos(i, j, k));
			if (!blockState.isAir()) {
				SoundType soundType = blockState.getSoundType();
				this.playSound(soundType.getFallSound(), soundType.getVolume() * 0.5F, soundType.getPitch() * 0.75F);
			}
		}
	}

	@Override
	public void animateHurt() {
		this.hurtDuration = 10;
		this.hurtTime = this.hurtDuration;
		this.hurtDir = 0.0F;
	}

	public int getArmorValue() {
		return Mth.floor(this.getAttributeValue(Attributes.ARMOR));
	}

	protected void hurtArmor(DamageSource damageSource, float f) {
	}

	protected void hurtCurrentlyUsedShield(float f) {
	}

	protected float getDamageAfterArmorAbsorb(DamageSource damageSource, float f) {
		if (!damageSource.isBypassArmor()) {
			this.hurtArmor(damageSource, f);
			f = CombatRules.getDamageAfterAbsorb(f, (float)this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
		}

		return f;
	}

	protected float getDamageAfterMagicAbsorb(DamageSource damageSource, float f) {
		if (damageSource.isBypassMagic()) {
			return f;
		} else {
			if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
				int i = (this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
				int j = 25 - i;
				float g = f * (float)j;
				float h = f;
				f = Math.max(g / 25.0F, 0.0F);
				float k = h - f;
				if (k > 0.0F && k < 3.4028235E37F) {
					if (this instanceof ServerPlayer) {
						((ServerPlayer)this).awardStat(Stats.DAMAGE_RESISTED, Math.round(k * 10.0F));
					} else if (damageSource.getEntity() instanceof ServerPlayer) {
						((ServerPlayer)damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(k * 10.0F));
					}
				}
			}

			if (f <= 0.0F) {
				return 0.0F;
			} else {
				int i = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), damageSource);
				if (i > 0) {
					f = CombatRules.getDamageAfterMagicAbsorb(f, (float)i);
				}

				return f;
			}
		}
	}

	protected void actuallyHurt(DamageSource damageSource, float f) {
		if (!this.isInvulnerableTo(damageSource)) {
			f = this.getDamageAfterArmorAbsorb(damageSource, f);
			f = this.getDamageAfterMagicAbsorb(damageSource, f);
			float var8 = Math.max(f - this.getAbsorptionAmount(), 0.0F);
			this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - var8));
			float h = f - var8;
			if (h > 0.0F && h < 3.4028235E37F && damageSource.getEntity() instanceof ServerPlayer) {
				((ServerPlayer)damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(h * 10.0F));
			}

			if (var8 != 0.0F) {
				float i = this.getHealth();
				this.setHealth(i - var8);
				this.getCombatTracker().recordDamage(damageSource, i, var8);
				this.setAbsorptionAmount(this.getAbsorptionAmount() - var8);
				this.gameEvent(GameEvent.ENTITY_DAMAGED, damageSource.getEntity());
			}
		}
	}

	public CombatTracker getCombatTracker() {
		return this.combatTracker;
	}

	@Nullable
	public LivingEntity getKillCredit() {
		if (this.combatTracker.getKiller() != null) {
			return this.combatTracker.getKiller();
		} else if (this.lastHurtByPlayer != null) {
			return this.lastHurtByPlayer;
		} else {
			return this.lastHurtByMob != null ? this.lastHurtByMob : null;
		}
	}

	public final float getMaxHealth() {
		return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
	}

	public final int getArrowCount() {
		return this.entityData.get(DATA_ARROW_COUNT_ID);
	}

	public final void setArrowCount(int i) {
		this.entityData.set(DATA_ARROW_COUNT_ID, i);
	}

	public final int getStingerCount() {
		return this.entityData.get(DATA_STINGER_COUNT_ID);
	}

	public final void setStingerCount(int i) {
		this.entityData.set(DATA_STINGER_COUNT_ID, i);
	}

	private int getCurrentSwingDuration() {
		if (MobEffectUtil.hasDigSpeed(this)) {
			return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
		} else {
			return this.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
		}
	}

	public void swing(InteractionHand interactionHand) {
		this.swing(interactionHand, false);
	}

	public void swing(InteractionHand interactionHand, boolean bl) {
		if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
			this.swingTime = -1;
			this.swinging = true;
			this.swingingArm = interactionHand;
			if (this.level instanceof ServerLevel) {
				ClientboundAnimatePacket clientboundAnimatePacket = new ClientboundAnimatePacket(this, interactionHand == InteractionHand.MAIN_HAND ? 0 : 3);
				ServerChunkCache serverChunkCache = ((ServerLevel)this.level).getChunkSource();
				if (bl) {
					serverChunkCache.broadcastAndSend(this, clientboundAnimatePacket);
				} else {
					serverChunkCache.broadcast(this, clientboundAnimatePacket);
				}
			}
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		switch (b) {
			case 2:
			case 33:
			case 36:
			case 37:
			case 44:
			case 57:
				this.animationSpeed = 1.5F;
				this.invulnerableTime = 20;
				this.hurtDuration = 10;
				this.hurtTime = this.hurtDuration;
				this.hurtDir = 0.0F;
				if (b == 33) {
					this.playSound(SoundEvents.THORNS_HIT, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				}

				DamageSource damageSource;
				if (b == 37) {
					damageSource = DamageSource.ON_FIRE;
				} else if (b == 36) {
					damageSource = DamageSource.DROWN;
				} else if (b == 44) {
					damageSource = DamageSource.SWEET_BERRY_BUSH;
				} else if (b == 57) {
					damageSource = DamageSource.FREEZE;
				} else {
					damageSource = DamageSource.GENERIC;
				}

				SoundEvent soundEvent = this.getHurtSound(damageSource);
				if (soundEvent != null) {
					this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				}

				this.hurt(DamageSource.GENERIC, 0.0F);
				this.lastDamageSource = damageSource;
				this.lastDamageStamp = this.level.getGameTime();
				break;
			case 3:
				SoundEvent soundEvent2 = this.getDeathSound();
				if (soundEvent2 != null) {
					this.playSound(soundEvent2, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				}

				if (!(this instanceof Player)) {
					this.setHealth(0.0F);
					this.die(DamageSource.GENERIC);
				}
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
			case 26:
			case 27:
			case 28:
			case 31:
			case 32:
			case 34:
			case 35:
			case 38:
			case 39:
			case 40:
			case 41:
			case 42:
			case 43:
			case 45:
			case 53:
			case 56:
			default:
				super.handleEntityEvent(b);
				break;
			case 29:
				this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.level.random.nextFloat() * 0.4F);
				break;
			case 30:
				this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level.random.nextFloat() * 0.4F);
				break;
			case 46:
				int i = 128;

				for (int j = 0; j < 128; j++) {
					double d = (double)j / 127.0;
					float f = (this.random.nextFloat() - 0.5F) * 0.2F;
					float g = (this.random.nextFloat() - 0.5F) * 0.2F;
					float h = (this.random.nextFloat() - 0.5F) * 0.2F;
					double e = Mth.lerp(d, this.xo, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
					double k = Mth.lerp(d, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
					double l = Mth.lerp(d, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
					this.level.addParticle(ParticleTypes.PORTAL, e, k, l, (double)f, (double)g, (double)h);
				}
				break;
			case 47:
				this.breakItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
				break;
			case 48:
				this.breakItem(this.getItemBySlot(EquipmentSlot.OFFHAND));
				break;
			case 49:
				this.breakItem(this.getItemBySlot(EquipmentSlot.HEAD));
				break;
			case 50:
				this.breakItem(this.getItemBySlot(EquipmentSlot.CHEST));
				break;
			case 51:
				this.breakItem(this.getItemBySlot(EquipmentSlot.LEGS));
				break;
			case 52:
				this.breakItem(this.getItemBySlot(EquipmentSlot.FEET));
				break;
			case 54:
				HoneyBlock.showJumpParticles(this);
				break;
			case 55:
				this.swapHandItems();
		}
	}

	private void swapHandItems() {
		ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
		this.setItemSlot(EquipmentSlot.OFFHAND, this.getItemBySlot(EquipmentSlot.MAINHAND));
		this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
	}

	@Override
	protected void outOfWorld() {
		this.hurt(DamageSource.OUT_OF_WORLD, 4.0F);
	}

	protected void updateSwingTime() {
		int i = this.getCurrentSwingDuration();
		if (this.swinging) {
			this.swingTime++;
			if (this.swingTime >= i) {
				this.swingTime = 0;
				this.swinging = false;
			}
		} else {
			this.swingTime = 0;
		}

		this.attackAnim = (float)this.swingTime / (float)i;
	}

	@Nullable
	public AttributeInstance getAttribute(Attribute attribute) {
		return this.getAttributes().getInstance(attribute);
	}

	public double getAttributeValue(Attribute attribute) {
		return this.getAttributes().getValue(attribute);
	}

	public double getAttributeBaseValue(Attribute attribute) {
		return this.getAttributes().getBaseValue(attribute);
	}

	public AttributeMap getAttributes() {
		return this.attributes;
	}

	public MobType getMobType() {
		return MobType.UNDEFINED;
	}

	public ItemStack getMainHandItem() {
		return this.getItemBySlot(EquipmentSlot.MAINHAND);
	}

	public ItemStack getOffhandItem() {
		return this.getItemBySlot(EquipmentSlot.OFFHAND);
	}

	public boolean isHolding(Item item) {
		return this.isHolding(itemStack -> itemStack.is(item));
	}

	public boolean isHolding(Predicate<ItemStack> predicate) {
		return predicate.test(this.getMainHandItem()) || predicate.test(this.getOffhandItem());
	}

	public ItemStack getItemInHand(InteractionHand interactionHand) {
		if (interactionHand == InteractionHand.MAIN_HAND) {
			return this.getItemBySlot(EquipmentSlot.MAINHAND);
		} else if (interactionHand == InteractionHand.OFF_HAND) {
			return this.getItemBySlot(EquipmentSlot.OFFHAND);
		} else {
			throw new IllegalArgumentException("Invalid hand " + interactionHand);
		}
	}

	public void setItemInHand(InteractionHand interactionHand, ItemStack itemStack) {
		if (interactionHand == InteractionHand.MAIN_HAND) {
			this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
		} else {
			if (interactionHand != InteractionHand.OFF_HAND) {
				throw new IllegalArgumentException("Invalid hand " + interactionHand);
			}

			this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
		}
	}

	public boolean hasItemInSlot(EquipmentSlot equipmentSlot) {
		return !this.getItemBySlot(equipmentSlot).isEmpty();
	}

	@Override
	public abstract Iterable<ItemStack> getArmorSlots();

	public abstract ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

	@Override
	public abstract void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack);

	public float getArmorCoverPercentage() {
		Iterable<ItemStack> iterable = this.getArmorSlots();
		int i = 0;
		int j = 0;

		for (ItemStack itemStack : iterable) {
			if (!itemStack.isEmpty()) {
				j++;
			}

			i++;
		}

		return i > 0 ? (float)j / (float)i : 0.0F;
	}

	@Override
	public void setSprinting(boolean bl) {
		super.setSprinting(bl);
		AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attributeInstance.getModifier(SPEED_MODIFIER_SPRINTING_UUID) != null) {
			attributeInstance.removeModifier(SPEED_MODIFIER_SPRINTING);
		}

		if (bl) {
			attributeInstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
		}
	}

	protected float getSoundVolume() {
		return 1.0F;
	}

	protected float getVoicePitch() {
		return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
	}

	protected boolean isImmobile() {
		return this.isDeadOrDying();
	}

	@Override
	public void push(Entity entity) {
		if (!this.isSleeping()) {
			super.push(entity);
		}
	}

	private void dismountVehicle(Entity entity) {
		Vec3 vec3;
		if (!entity.isRemoved() && !this.level.getBlockState(entity.blockPosition()).is(BlockTags.PORTALS)) {
			vec3 = entity.getDismountLocationForPassenger(this);
		} else {
			vec3 = new Vec3(entity.getX(), entity.getY() + (double)entity.getBbHeight(), entity.getZ());
		}

		this.dismountTo(vec3.x, vec3.y, vec3.z);
	}

	@Override
	public boolean shouldShowName() {
		return this.isCustomNameVisible();
	}

	protected float getJumpPower() {
		return 0.42F * this.getBlockJumpFactor();
	}

	protected void jumpFromGround() {
		float f = this.getJumpPower();
		if (this.hasEffect(MobEffects.JUMP)) {
			f += 0.1F * (float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1);
		}

		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.x, (double)f, vec3.z);
		if (this.isSprinting()) {
			float g = this.yRot * (float) (Math.PI / 180.0);
			this.setDeltaMovement(this.getDeltaMovement().add((double)(-Mth.sin(g) * 0.2F), 0.0, (double)(Mth.cos(g) * 0.2F)));
		}

		this.hasImpulse = true;
	}

	protected void goDownInWater() {
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04F, 0.0));
	}

	protected void jumpInLiquid(net.minecraft.tags.Tag<Fluid> tag) {
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04F, 0.0));
	}

	protected float getWaterSlowDown() {
		return 0.8F;
	}

	public boolean canStandOnFluid(Fluid fluid) {
		return false;
	}

	public void travel(Vec3 vec3) {
		if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
			double d = 0.08;
			boolean bl = this.getDeltaMovement().y <= 0.0;
			if (bl && this.hasEffect(MobEffects.SLOW_FALLING)) {
				d = 0.01;
				this.fallDistance = 0.0F;
			}

			FluidState fluidState = this.level.getFluidState(this.blockPosition());
			if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState.getType())) {
				double e = this.getY();
				float f = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
				float g = 0.02F;
				float h = (float)EnchantmentHelper.getDepthStrider(this);
				if (h > 3.0F) {
					h = 3.0F;
				}

				if (!this.onGround) {
					h *= 0.5F;
				}

				if (h > 0.0F) {
					f += (0.54600006F - f) * h / 3.0F;
					g += (this.getSpeed() - g) * h / 3.0F;
				}

				if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
					f = 0.96F;
				}

				this.moveRelative(g, vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				Vec3 vec32 = this.getDeltaMovement();
				if (this.horizontalCollision && this.onClimbable()) {
					vec32 = new Vec3(vec32.x, 0.2, vec32.z);
				}

				this.setDeltaMovement(vec32.multiply((double)f, 0.8F, (double)f));
				Vec3 vec33 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
				this.setDeltaMovement(vec33);
				if (this.horizontalCollision && this.isFree(vec33.x, vec33.y + 0.6F - this.getY() + e, vec33.z)) {
					this.setDeltaMovement(vec33.x, 0.3F, vec33.z);
				}
			} else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState.getType())) {
				double ex = this.getY();
				this.moveRelative(0.02F, vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
					this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
					Vec3 vec34 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
					this.setDeltaMovement(vec34);
				} else {
					this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
				}

				if (!this.isNoGravity()) {
					this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d / 4.0, 0.0));
				}

				Vec3 vec34 = this.getDeltaMovement();
				if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6F - this.getY() + ex, vec34.z)) {
					this.setDeltaMovement(vec34.x, 0.3F, vec34.z);
				}
			} else if (this.isFallFlying()) {
				Vec3 vec35 = this.getDeltaMovement();
				if (vec35.y > -0.5) {
					this.fallDistance = 1.0F;
				}

				Vec3 vec36 = this.getLookAngle();
				float fx = this.xRot * (float) (Math.PI / 180.0);
				double i = Math.sqrt(vec36.x * vec36.x + vec36.z * vec36.z);
				double j = Math.sqrt(getHorizontalDistanceSqr(vec35));
				double k = vec36.length();
				float l = Mth.cos(fx);
				l = (float)((double)l * (double)l * Math.min(1.0, k / 0.4));
				vec35 = this.getDeltaMovement().add(0.0, d * (-1.0 + (double)l * 0.75), 0.0);
				if (vec35.y < 0.0 && i > 0.0) {
					double m = vec35.y * -0.1 * (double)l;
					vec35 = vec35.add(vec36.x * m / i, m, vec36.z * m / i);
				}

				if (fx < 0.0F && i > 0.0) {
					double m = j * (double)(-Mth.sin(fx)) * 0.04;
					vec35 = vec35.add(-vec36.x * m / i, m * 3.2, -vec36.z * m / i);
				}

				if (i > 0.0) {
					vec35 = vec35.add((vec36.x / i * j - vec35.x) * 0.1, 0.0, (vec36.z / i * j - vec35.z) * 0.1);
				}

				this.setDeltaMovement(vec35.multiply(0.99F, 0.98F, 0.99F));
				this.move(MoverType.SELF, this.getDeltaMovement());
				if (this.horizontalCollision && !this.level.isClientSide) {
					double m = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
					double n = j - m;
					float o = (float)(n * 10.0 - 3.0);
					if (o > 0.0F) {
						this.playSound(this.getFallDamageSound((int)o), 1.0F, 1.0F);
						this.hurt(DamageSource.FLY_INTO_WALL, o);
					}
				}

				if (this.onGround && !this.level.isClientSide) {
					this.setSharedFlag(7, false);
				}
			} else {
				BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
				float p = this.level.getBlockState(blockPos).getBlock().getFriction();
				float fxx = this.onGround ? p * 0.91F : 0.91F;
				Vec3 vec37 = this.handleRelativeFrictionAndCalculateMovement(vec3, p);
				double q = vec37.y;
				if (this.hasEffect(MobEffects.LEVITATION)) {
					q += (0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec37.y) * 0.2;
					this.fallDistance = 0.0F;
				} else if (this.level.isClientSide && !this.level.hasChunkAt(blockPos)) {
					if (this.getY() > (double)this.level.getMinBuildHeight()) {
						q = -0.1;
					} else {
						q = 0.0;
					}
				} else if (!this.isNoGravity()) {
					q -= d;
				}

				if (this.shouldDiscardFriction()) {
					this.setDeltaMovement(vec37.x, q, vec37.z);
				} else {
					this.setDeltaMovement(vec37.x * (double)fxx, q * 0.98F, vec37.z * (double)fxx);
				}
			}
		}

		this.calculateEntityAnimation(this, this instanceof FlyingAnimal);
	}

	public void calculateEntityAnimation(LivingEntity livingEntity, boolean bl) {
		livingEntity.animationSpeedOld = livingEntity.animationSpeed;
		double d = livingEntity.getX() - livingEntity.xo;
		double e = bl ? livingEntity.getY() - livingEntity.yo : 0.0;
		double f = livingEntity.getZ() - livingEntity.zo;
		float g = Mth.sqrt(d * d + e * e + f * f) * 4.0F;
		if (g > 1.0F) {
			g = 1.0F;
		}

		livingEntity.animationSpeed = livingEntity.animationSpeed + (g - livingEntity.animationSpeed) * 0.4F;
		livingEntity.animationPosition = livingEntity.animationPosition + livingEntity.animationSpeed;
	}

	public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 vec3, float f) {
		this.moveRelative(this.getFrictionInfluencedSpeed(f), vec3);
		this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
		this.move(MoverType.SELF, this.getDeltaMovement());
		Vec3 vec32 = this.getDeltaMovement();
		if ((this.horizontalCollision || this.jumping)
			&& (this.onClimbable() || this.getFeetBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
			vec32 = new Vec3(vec32.x, 0.2, vec32.z);
		}

		return vec32;
	}

	public Vec3 getFluidFallingAdjustedMovement(double d, boolean bl, Vec3 vec3) {
		if (!this.isNoGravity() && !this.isSprinting()) {
			double e;
			if (bl && Math.abs(vec3.y - 0.005) >= 0.003 && Math.abs(vec3.y - d / 16.0) < 0.003) {
				e = -0.003;
			} else {
				e = vec3.y - d / 16.0;
			}

			return new Vec3(vec3.x, e, vec3.z);
		} else {
			return vec3;
		}
	}

	private Vec3 handleOnClimbable(Vec3 vec3) {
		if (this.onClimbable()) {
			this.fallDistance = 0.0F;
			float f = 0.15F;
			double d = Mth.clamp(vec3.x, -0.15F, 0.15F);
			double e = Mth.clamp(vec3.z, -0.15F, 0.15F);
			double g = Math.max(vec3.y, -0.15F);
			if (g < 0.0 && !this.getFeetBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
				g = 0.0;
			}

			vec3 = new Vec3(d, g, e);
		}

		return vec3;
	}

	private float getFrictionInfluencedSpeed(float f) {
		return this.onGround ? this.getSpeed() * (0.21600002F / (f * f * f)) : this.flyingSpeed;
	}

	public float getSpeed() {
		return this.speed;
	}

	public void setSpeed(float f) {
		this.speed = f;
	}

	public boolean doHurtTarget(Entity entity) {
		this.setLastHurtMob(entity);
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.updatingUsingItem();
		this.updateSwimAmount();
		if (!this.level.isClientSide) {
			int i = this.getArrowCount();
			if (i > 0) {
				if (this.removeArrowTime <= 0) {
					this.removeArrowTime = 20 * (30 - i);
				}

				this.removeArrowTime--;
				if (this.removeArrowTime <= 0) {
					this.setArrowCount(i - 1);
				}
			}

			int j = this.getStingerCount();
			if (j > 0) {
				if (this.removeStingerTime <= 0) {
					this.removeStingerTime = 20 * (30 - j);
				}

				this.removeStingerTime--;
				if (this.removeStingerTime <= 0) {
					this.setStingerCount(j - 1);
				}
			}

			this.detectEquipmentUpdates();
			if (this.tickCount % 20 == 0) {
				this.getCombatTracker().recheckStatus();
			}

			if (!this.glowing) {
				boolean bl = this.hasEffect(MobEffects.GLOWING);
				if (this.getSharedFlag(6) != bl) {
					this.setSharedFlag(6, bl);
				}

				this.glowing = bl;
			}

			if (this.isSleeping() && !this.checkBedExists()) {
				this.stopSleeping();
			}
		}

		this.aiStep();
		double d = this.getX() - this.xo;
		double e = this.getZ() - this.zo;
		float f = (float)(d * d + e * e);
		float g = this.yBodyRot;
		float h = 0.0F;
		this.oRun = this.run;
		float k = 0.0F;
		if (f > 0.0025000002F) {
			k = 1.0F;
			h = (float)Math.sqrt((double)f) * 3.0F;
			float l = (float)Mth.atan2(e, d) * (180.0F / (float)Math.PI) - 90.0F;
			float m = Mth.abs(Mth.wrapDegrees(this.yRot) - l);
			if (95.0F < m && m < 265.0F) {
				g = l - 180.0F;
			} else {
				g = l;
			}
		}

		if (this.attackAnim > 0.0F) {
			g = this.yRot;
		}

		if (!this.onGround) {
			k = 0.0F;
		}

		this.run = this.run + (k - this.run) * 0.3F;
		this.level.getProfiler().push("headTurn");
		h = this.tickHeadTurn(g, h);
		this.level.getProfiler().pop();
		this.level.getProfiler().push("rangeChecks");

		while (this.yRot - this.yRotO < -180.0F) {
			this.yRotO -= 360.0F;
		}

		while (this.yRot - this.yRotO >= 180.0F) {
			this.yRotO += 360.0F;
		}

		while (this.yBodyRot - this.yBodyRotO < -180.0F) {
			this.yBodyRotO -= 360.0F;
		}

		while (this.yBodyRot - this.yBodyRotO >= 180.0F) {
			this.yBodyRotO += 360.0F;
		}

		while (this.xRot - this.xRotO < -180.0F) {
			this.xRotO -= 360.0F;
		}

		while (this.xRot - this.xRotO >= 180.0F) {
			this.xRotO += 360.0F;
		}

		while (this.yHeadRot - this.yHeadRotO < -180.0F) {
			this.yHeadRotO -= 360.0F;
		}

		while (this.yHeadRot - this.yHeadRotO >= 180.0F) {
			this.yHeadRotO += 360.0F;
		}

		this.level.getProfiler().pop();
		this.animStep += h;
		if (this.isFallFlying()) {
			this.fallFlyTicks++;
		} else {
			this.fallFlyTicks = 0;
		}

		if (this.isSleeping()) {
			this.xRot = 0.0F;
		}
	}

	private void detectEquipmentUpdates() {
		Map<EquipmentSlot, ItemStack> map = this.collectEquipmentChanges();
		if (map != null) {
			this.handleHandSwap(map);
			if (!map.isEmpty()) {
				this.handleEquipmentChanges(map);
			}
		}
	}

	@Nullable
	private Map<EquipmentSlot, ItemStack> collectEquipmentChanges() {
		Map<EquipmentSlot, ItemStack> map = null;

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			ItemStack itemStack;
			switch (equipmentSlot.getType()) {
				case HAND:
					itemStack = this.getLastHandItem(equipmentSlot);
					break;
				case ARMOR:
					itemStack = this.getLastArmorItem(equipmentSlot);
					break;
				default:
					continue;
			}

			ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
			if (!ItemStack.matches(itemStack2, itemStack)) {
				if (map == null) {
					map = Maps.newEnumMap(EquipmentSlot.class);
				}

				map.put(equipmentSlot, itemStack2);
				if (!itemStack.isEmpty()) {
					this.getAttributes().removeAttributeModifiers(itemStack.getAttributeModifiers(equipmentSlot));
				}

				if (!itemStack2.isEmpty()) {
					this.getAttributes().addTransientAttributeModifiers(itemStack2.getAttributeModifiers(equipmentSlot));
				}
			}
		}

		return map;
	}

	private void handleHandSwap(Map<EquipmentSlot, ItemStack> map) {
		ItemStack itemStack = (ItemStack)map.get(EquipmentSlot.MAINHAND);
		ItemStack itemStack2 = (ItemStack)map.get(EquipmentSlot.OFFHAND);
		if (itemStack != null
			&& itemStack2 != null
			&& ItemStack.matches(itemStack, this.getLastHandItem(EquipmentSlot.OFFHAND))
			&& ItemStack.matches(itemStack2, this.getLastHandItem(EquipmentSlot.MAINHAND))) {
			((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundEntityEventPacket(this, (byte)55));
			map.remove(EquipmentSlot.MAINHAND);
			map.remove(EquipmentSlot.OFFHAND);
			this.setLastHandItem(EquipmentSlot.MAINHAND, itemStack.copy());
			this.setLastHandItem(EquipmentSlot.OFFHAND, itemStack2.copy());
		}
	}

	private void handleEquipmentChanges(Map<EquipmentSlot, ItemStack> map) {
		List<Pair<EquipmentSlot, ItemStack>> list = Lists.<Pair<EquipmentSlot, ItemStack>>newArrayListWithCapacity(map.size());
		map.forEach((equipmentSlot, itemStack) -> {
			ItemStack itemStack2 = itemStack.copy();
			list.add(Pair.of(equipmentSlot, itemStack2));
			switch (equipmentSlot.getType()) {
				case HAND:
					this.setLastHandItem(equipmentSlot, itemStack2);
					break;
				case ARMOR:
					this.setLastArmorItem(equipmentSlot, itemStack2);
			}
		});
		((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundSetEquipmentPacket(this.getId(), list));
	}

	private ItemStack getLastArmorItem(EquipmentSlot equipmentSlot) {
		return this.lastArmorItemStacks.get(equipmentSlot.getIndex());
	}

	private void setLastArmorItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		this.lastArmorItemStacks.set(equipmentSlot.getIndex(), itemStack);
	}

	private ItemStack getLastHandItem(EquipmentSlot equipmentSlot) {
		return this.lastHandItemStacks.get(equipmentSlot.getIndex());
	}

	private void setLastHandItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
		this.lastHandItemStacks.set(equipmentSlot.getIndex(), itemStack);
	}

	protected float tickHeadTurn(float f, float g) {
		float h = Mth.wrapDegrees(f - this.yBodyRot);
		this.yBodyRot += h * 0.3F;
		float i = Mth.wrapDegrees(this.yRot - this.yBodyRot);
		boolean bl = i < -90.0F || i >= 90.0F;
		if (i < -75.0F) {
			i = -75.0F;
		}

		if (i >= 75.0F) {
			i = 75.0F;
		}

		this.yBodyRot = this.yRot - i;
		if (i * i > 2500.0F) {
			this.yBodyRot += i * 0.2F;
		}

		if (bl) {
			g *= -1.0F;
		}

		return g;
	}

	public void aiStep() {
		if (this.noJumpDelay > 0) {
			this.noJumpDelay--;
		}

		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
			this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpSteps > 0) {
			double d = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
			double e = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
			double f = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
			double g = Mth.wrapDegrees(this.lerpYRot - (double)this.yRot);
			this.yRot = (float)((double)this.yRot + g / (double)this.lerpSteps);
			this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
			this.lerpSteps--;
			this.setPos(d, e, f);
			this.setRot(this.yRot, this.xRot);
		} else if (!this.isEffectiveAi()) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
		}

		if (this.lerpHeadSteps > 0) {
			this.yHeadRot = (float)((double)this.yHeadRot + Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
			this.lerpHeadSteps--;
		}

		Vec3 vec3 = this.getDeltaMovement();
		double h = vec3.x;
		double i = vec3.y;
		double j = vec3.z;
		if (Math.abs(vec3.x) < 0.003) {
			h = 0.0;
		}

		if (Math.abs(vec3.y) < 0.003) {
			i = 0.0;
		}

		if (Math.abs(vec3.z) < 0.003) {
			j = 0.0;
		}

		this.setDeltaMovement(h, i, j);
		this.level.getProfiler().push("ai");
		if (this.isImmobile()) {
			this.jumping = false;
			this.xxa = 0.0F;
			this.zza = 0.0F;
		} else if (this.isEffectiveAi()) {
			this.level.getProfiler().push("newAi");
			this.serverAiStep();
			this.level.getProfiler().pop();
		}

		this.level.getProfiler().pop();
		this.level.getProfiler().push("jump");
		if (this.jumping && this.isAffectedByFluids()) {
			double k;
			if (this.isInLava()) {
				k = this.getFluidHeight(FluidTags.LAVA);
			} else {
				k = this.getFluidHeight(FluidTags.WATER);
			}

			boolean bl = this.isInWater() && k > 0.0;
			double l = this.getFluidJumpThreshold();
			if (!bl || this.onGround && !(k > l)) {
				if (!this.isInLava() || this.onGround && !(k > l)) {
					if ((this.onGround || bl && k <= l) && this.noJumpDelay == 0) {
						this.jumpFromGround();
						this.noJumpDelay = 10;
					}
				} else {
					this.jumpInLiquid(FluidTags.LAVA);
				}
			} else {
				this.jumpInLiquid(FluidTags.WATER);
			}
		} else {
			this.noJumpDelay = 0;
		}

		this.level.getProfiler().pop();
		this.level.getProfiler().push("travel");
		this.xxa *= 0.98F;
		this.zza *= 0.98F;
		this.updateFallFlying();
		AABB aABB = this.getBoundingBox();
		this.travel(new Vec3((double)this.xxa, (double)this.yya, (double)this.zza));
		this.level.getProfiler().pop();
		this.level.getProfiler().push("freezing");
		boolean bl2 = this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES);
		if (!this.level.isClientSide) {
			int m = this.getTicksFrozen();
			if (this.isInPowderSnow && this.canFreeze()) {
				this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), m + 1));
			} else {
				this.setTicksFrozen(Math.max(0, m - 2));
			}
		}

		this.removeFrost();
		this.tryAddFrost();
		if (!this.level.isClientSide && this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
			int m = bl2 ? 5 : 1;
			this.hurt(DamageSource.FREEZE, (float)m);
		}

		this.level.getProfiler().pop();
		this.level.getProfiler().push("push");
		if (this.autoSpinAttackTicks > 0) {
			this.autoSpinAttackTicks--;
			this.checkAutoSpinAttack(aABB, this.getBoundingBox());
		}

		this.pushEntities();
		this.level.getProfiler().pop();
		if (!this.level.isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
			this.hurt(DamageSource.DROWN, 1.0F);
		}
	}

	public boolean isSensitiveToWater() {
		return false;
	}

	private void updateFallFlying() {
		boolean bl = this.getSharedFlag(7);
		if (bl && !this.onGround && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
			ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
			if (itemStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemStack)) {
				bl = true;
				int i = this.fallFlyTicks + 1;
				if (!this.level.isClientSide && i % 10 == 0) {
					int j = i / 10;
					if (j % 2 == 0) {
						itemStack.hurtAndBreak(1, this, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.CHEST));
					}

					this.gameEvent(GameEvent.ELYTRA_FREE_FALL);
				}
			} else {
				bl = false;
			}
		} else {
			bl = false;
		}

		if (!this.level.isClientSide) {
			this.setSharedFlag(7, bl);
		}
	}

	protected void serverAiStep() {
	}

	protected void pushEntities() {
		List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
		if (!list.isEmpty()) {
			int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
			if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
				int j = 0;

				for (int k = 0; k < list.size(); k++) {
					if (!((Entity)list.get(k)).isPassenger()) {
						j++;
					}
				}

				if (j > i - 1) {
					this.hurt(DamageSource.CRAMMING, 6.0F);
				}
			}

			for (int j = 0; j < list.size(); j++) {
				Entity entity = (Entity)list.get(j);
				this.doPush(entity);
			}
		}
	}

	protected void checkAutoSpinAttack(AABB aABB, AABB aABB2) {
		AABB aABB3 = aABB.minmax(aABB2);
		List<Entity> list = this.level.getEntities(this, aABB3);
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Entity entity = (Entity)list.get(i);
				if (entity instanceof LivingEntity) {
					this.doAutoAttackOnTouch((LivingEntity)entity);
					this.autoSpinAttackTicks = 0;
					this.setDeltaMovement(this.getDeltaMovement().scale(-0.2));
					break;
				}
			}
		} else if (this.horizontalCollision) {
			this.autoSpinAttackTicks = 0;
		}

		if (!this.level.isClientSide && this.autoSpinAttackTicks <= 0) {
			this.setLivingEntityFlag(4, false);
		}
	}

	protected void doPush(Entity entity) {
		entity.push(this);
	}

	protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
	}

	public void startAutoSpinAttack(int i) {
		this.autoSpinAttackTicks = i;
		if (!this.level.isClientSide) {
			this.setLivingEntityFlag(4, true);
		}
	}

	public boolean isAutoSpinAttack() {
		return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
	}

	@Override
	public void stopRiding() {
		Entity entity = this.getVehicle();
		super.stopRiding();
		if (entity != null && entity != this.getVehicle() && !this.level.isClientSide) {
			this.dismountVehicle(entity);
		}
	}

	@Override
	public void rideTick() {
		super.rideTick();
		this.oRun = this.run;
		this.run = 0.0F;
		this.fallDistance = 0.0F;
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		this.lerpX = d;
		this.lerpY = e;
		this.lerpZ = f;
		this.lerpYRot = (double)g;
		this.lerpXRot = (double)h;
		this.lerpSteps = i;
	}

	@Override
	public void lerpHeadTo(float f, int i) {
		this.lyHeadRot = (double)f;
		this.lerpHeadSteps = i;
	}

	public void setJumping(boolean bl) {
		this.jumping = bl;
	}

	public void onItemPickup(ItemEntity itemEntity) {
		Player player = itemEntity.getThrower() != null ? this.level.getPlayerByUUID(itemEntity.getThrower()) : null;
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayer)player, itemEntity.getItem(), this);
		}
	}

	public void take(Entity entity, int i) {
		if (!entity.isRemoved() && !this.level.isClientSide && (entity instanceof ItemEntity || entity instanceof AbstractArrow || entity instanceof ExperienceOrb)) {
			((ServerLevel)this.level).getChunkSource().broadcast(entity, new ClientboundTakeItemEntityPacket(entity.getId(), this.getId(), i));
		}
	}

	public boolean canSee(Entity entity) {
		Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
		Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
		return this.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	@Override
	public float getViewYRot(float f) {
		return f == 1.0F ? this.yHeadRot : Mth.lerp(f, this.yHeadRotO, this.yHeadRot);
	}

	public float getAttackAnim(float f) {
		float g = this.attackAnim - this.oAttackAnim;
		if (g < 0.0F) {
			g++;
		}

		return this.oAttackAnim + g * f;
	}

	public boolean isEffectiveAi() {
		return !this.level.isClientSide;
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean isPushable() {
		return this.isAlive() && !this.isSpectator() && !this.onClimbable();
	}

	@Override
	protected void markHurt() {
		this.hurtMarked = this.random.nextDouble() >= this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
	}

	@Override
	public float getYHeadRot() {
		return this.yHeadRot;
	}

	@Override
	public void setYHeadRot(float f) {
		this.yHeadRot = f;
	}

	@Override
	public void setYBodyRot(float f) {
		this.yBodyRot = f;
	}

	@Override
	protected Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
		return resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
	}

	public static Vec3 resetForwardDirectionOfRelativePortalPosition(Vec3 vec3) {
		return new Vec3(vec3.x, vec3.y, 0.0);
	}

	public float getAbsorptionAmount() {
		return this.absorptionAmount;
	}

	public void setAbsorptionAmount(float f) {
		if (f < 0.0F) {
			f = 0.0F;
		}

		this.absorptionAmount = f;
	}

	public void onEnterCombat() {
	}

	public void onLeaveCombat() {
	}

	protected void updateEffectVisibility() {
		this.effectsDirty = true;
	}

	public abstract HumanoidArm getMainArm();

	public boolean isUsingItem() {
		return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
	}

	public InteractionHand getUsedItemHand() {
		return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	private void updatingUsingItem() {
		if (this.isUsingItem()) {
			if (ItemStack.isSameIgnoreDurability(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
				this.useItem = this.getItemInHand(this.getUsedItemHand());
				this.useItem.onUseTick(this.level, this, this.getUseItemRemainingTicks());
				if (this.shouldTriggerItemUseEffects()) {
					this.triggerItemUseEffects(this.useItem, 5);
				}

				if (--this.useItemRemaining == 0 && !this.level.isClientSide && !this.useItem.useOnRelease()) {
					this.completeUsingItem();
				}
			} else {
				this.stopUsingItem();
			}
		}
	}

	private boolean shouldTriggerItemUseEffects() {
		int i = this.getUseItemRemainingTicks();
		FoodProperties foodProperties = this.useItem.getItem().getFoodProperties();
		boolean bl = foodProperties != null && foodProperties.isFastFood();
		bl |= i <= this.useItem.getUseDuration() - 7;
		return bl && i % 4 == 0;
	}

	private void updateSwimAmount() {
		this.swimAmountO = this.swimAmount;
		if (this.isVisuallySwimming()) {
			this.swimAmount = Math.min(1.0F, this.swimAmount + 0.09F);
		} else {
			this.swimAmount = Math.max(0.0F, this.swimAmount - 0.09F);
		}
	}

	protected void setLivingEntityFlag(int i, boolean bl) {
		int j = this.entityData.get(DATA_LIVING_ENTITY_FLAGS);
		if (bl) {
			j |= i;
		} else {
			j &= ~i;
		}

		this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)j);
	}

	public void startUsingItem(InteractionHand interactionHand) {
		ItemStack itemStack = this.getItemInHand(interactionHand);
		if (!itemStack.isEmpty() && !this.isUsingItem()) {
			this.useItem = itemStack;
			this.useItemRemaining = itemStack.getUseDuration();
			if (!this.level.isClientSide) {
				this.setLivingEntityFlag(1, true);
				this.setLivingEntityFlag(2, interactionHand == InteractionHand.OFF_HAND);
			}
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (SLEEPING_POS_ID.equals(entityDataAccessor)) {
			if (this.level.isClientSide) {
				this.getSleepingPos().ifPresent(this::setPosToBed);
			}
		} else if (DATA_LIVING_ENTITY_FLAGS.equals(entityDataAccessor) && this.level.isClientSide) {
			if (this.isUsingItem() && this.useItem.isEmpty()) {
				this.useItem = this.getItemInHand(this.getUsedItemHand());
				if (!this.useItem.isEmpty()) {
					this.useItemRemaining = this.useItem.getUseDuration();
				}
			} else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
				this.useItem = ItemStack.EMPTY;
				this.useItemRemaining = 0;
			}
		}
	}

	@Override
	public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
		super.lookAt(anchor, vec3);
		this.yHeadRotO = this.yHeadRot;
		this.yBodyRot = this.yHeadRot;
		this.yBodyRotO = this.yBodyRot;
	}

	protected void triggerItemUseEffects(ItemStack itemStack, int i) {
		if (!itemStack.isEmpty() && this.isUsingItem()) {
			if (itemStack.getUseAnimation() == UseAnim.DRINK) {
				this.playSound(this.getDrinkingSound(itemStack), 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
			}

			if (itemStack.getUseAnimation() == UseAnim.EAT) {
				this.spawnItemParticles(itemStack, i);
				this.playSound(
					this.getEatingSound(itemStack), 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
				);
			}
		}
	}

	private void spawnItemParticles(ItemStack itemStack, int i) {
		for (int j = 0; j < i; j++) {
			Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
			vec3 = vec3.xRot(-this.xRot * (float) (Math.PI / 180.0));
			vec3 = vec3.yRot(-this.yRot * (float) (Math.PI / 180.0));
			double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
			Vec3 vec32 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
			vec32 = vec32.xRot(-this.xRot * (float) (Math.PI / 180.0));
			vec32 = vec32.yRot(-this.yRot * (float) (Math.PI / 180.0));
			vec32 = vec32.add(this.getX(), this.getEyeY(), this.getZ());
			this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
		}
	}

	protected void completeUsingItem() {
		InteractionHand interactionHand = this.getUsedItemHand();
		if (!this.useItem.equals(this.getItemInHand(interactionHand))) {
			this.releaseUsingItem();
		} else {
			if (!this.useItem.isEmpty() && this.isUsingItem()) {
				this.triggerItemUseEffects(this.useItem, 16);
				ItemStack itemStack = this.useItem.finishUsingItem(this.level, this);
				if (itemStack != this.useItem) {
					this.setItemInHand(interactionHand, itemStack);
				}

				this.stopUsingItem();
			}
		}
	}

	public ItemStack getUseItem() {
		return this.useItem;
	}

	public int getUseItemRemainingTicks() {
		return this.useItemRemaining;
	}

	public int getTicksUsingItem() {
		return this.isUsingItem() ? this.useItem.getUseDuration() - this.getUseItemRemainingTicks() : 0;
	}

	public void releaseUsingItem() {
		if (!this.useItem.isEmpty()) {
			this.useItem.releaseUsing(this.level, this, this.getUseItemRemainingTicks());
			if (this.useItem.useOnRelease()) {
				this.updatingUsingItem();
			}
		}

		this.stopUsingItem();
	}

	public void stopUsingItem() {
		if (!this.level.isClientSide) {
			this.setLivingEntityFlag(1, false);
		}

		this.useItem = ItemStack.EMPTY;
		this.useItemRemaining = 0;
	}

	public boolean isBlocking() {
		if (this.isUsingItem() && !this.useItem.isEmpty()) {
			Item item = this.useItem.getItem();
			return item.getUseAnimation(this.useItem) != UseAnim.BLOCK ? false : item.getUseDuration(this.useItem) - this.useItemRemaining >= 5;
		} else {
			return false;
		}
	}

	public boolean isSuppressingSlidingDownLadder() {
		return this.isShiftKeyDown();
	}

	public boolean isFallFlying() {
		return this.getSharedFlag(7);
	}

	@Override
	public boolean isVisuallySwimming() {
		return super.isVisuallySwimming() || !this.isFallFlying() && this.getPose() == Pose.FALL_FLYING;
	}

	public int getFallFlyingTicks() {
		return this.fallFlyTicks;
	}

	public boolean randomTeleport(double d, double e, double f, boolean bl) {
		double g = this.getX();
		double h = this.getY();
		double i = this.getZ();
		double j = e;
		boolean bl2 = false;
		BlockPos blockPos = new BlockPos(d, e, f);
		Level level = this.level;
		if (level.hasChunkAt(blockPos)) {
			boolean bl3 = false;

			while (!bl3 && blockPos.getY() > level.getMinBuildHeight()) {
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState = level.getBlockState(blockPos2);
				if (blockState.getMaterial().blocksMotion()) {
					bl3 = true;
				} else {
					j--;
					blockPos = blockPos2;
				}
			}

			if (bl3) {
				this.teleportTo(d, j, f);
				if (level.noCollision(this) && !level.containsAnyLiquid(this.getBoundingBox())) {
					bl2 = true;
				}
			}
		}

		if (!bl2) {
			this.teleportTo(g, h, i);
			return false;
		} else {
			if (bl) {
				level.broadcastEntityEvent(this, (byte)46);
			}

			if (this instanceof PathfinderMob) {
				((PathfinderMob)this).getNavigation().stop();
			}

			return true;
		}
	}

	public boolean isAffectedByPotions() {
		return true;
	}

	public boolean attackable() {
		return true;
	}

	public void setRecordPlayingNearby(BlockPos blockPos, boolean bl) {
	}

	public boolean canTakeItem(ItemStack itemStack) {
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddMobPacket(this);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return pose == Pose.SLEEPING ? SLEEPING_DIMENSIONS : super.getDimensions(pose).scale(this.getScale());
	}

	public ImmutableList<Pose> getDismountPoses() {
		return ImmutableList.of(Pose.STANDING);
	}

	public AABB getLocalBoundsForPose(Pose pose) {
		EntityDimensions entityDimensions = this.getDimensions(pose);
		return new AABB(
			(double)(-entityDimensions.width / 2.0F),
			0.0,
			(double)(-entityDimensions.width / 2.0F),
			(double)(entityDimensions.width / 2.0F),
			(double)entityDimensions.height,
			(double)(entityDimensions.width / 2.0F)
		);
	}

	public Optional<BlockPos> getSleepingPos() {
		return this.entityData.get(SLEEPING_POS_ID);
	}

	public void setSleepingPos(BlockPos blockPos) {
		this.entityData.set(SLEEPING_POS_ID, Optional.of(blockPos));
	}

	public void clearSleepingPos() {
		this.entityData.set(SLEEPING_POS_ID, Optional.empty());
	}

	public boolean isSleeping() {
		return this.getSleepingPos().isPresent();
	}

	public void startSleeping(BlockPos blockPos) {
		if (this.isPassenger()) {
			this.stopRiding();
		}

		BlockState blockState = this.level.getBlockState(blockPos);
		if (blockState.getBlock() instanceof BedBlock) {
			this.level.setBlock(blockPos, blockState.setValue(BedBlock.OCCUPIED, Boolean.valueOf(true)), 3);
		}

		this.setPose(Pose.SLEEPING);
		this.setPosToBed(blockPos);
		this.setSleepingPos(blockPos);
		this.setDeltaMovement(Vec3.ZERO);
		this.hasImpulse = true;
	}

	private void setPosToBed(BlockPos blockPos) {
		this.setPos((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.6875, (double)blockPos.getZ() + 0.5);
	}

	private boolean checkBedExists() {
		return (Boolean)this.getSleepingPos().map(blockPos -> this.level.getBlockState(blockPos).getBlock() instanceof BedBlock).orElse(false);
	}

	public void stopSleeping() {
		this.getSleepingPos().filter(this.level::hasChunkAt).ifPresent(blockPos -> {
			BlockState blockState = this.level.getBlockState(blockPos);
			if (blockState.getBlock() instanceof BedBlock) {
				this.level.setBlock(blockPos, blockState.setValue(BedBlock.OCCUPIED, Boolean.valueOf(false)), 3);
				Vec3 vec3x = (Vec3)BedBlock.findStandUpPosition(this.getType(), this.level, blockPos, this.yRot).orElseGet(() -> {
					BlockPos blockPos2 = blockPos.above();
					return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.1, (double)blockPos2.getZ() + 0.5);
				});
				Vec3 vec32 = Vec3.atBottomCenterOf(blockPos).subtract(vec3x).normalize();
				float f = (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 180.0F / (float)Math.PI - 90.0);
				this.setPos(vec3x.x, vec3x.y, vec3x.z);
				this.yRot = f;
				this.xRot = 0.0F;
			}
		});
		Vec3 vec3 = this.position();
		this.setPose(Pose.STANDING);
		this.setPos(vec3.x, vec3.y, vec3.z);
		this.clearSleepingPos();
	}

	@Nullable
	public Direction getBedOrientation() {
		BlockPos blockPos = (BlockPos)this.getSleepingPos().orElse(null);
		return blockPos != null ? BedBlock.getBedOrientation(this.level, blockPos) : null;
	}

	@Override
	public boolean isInWall() {
		return !this.isSleeping() && super.isInWall();
	}

	@Override
	protected final float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return pose == Pose.SLEEPING ? 0.2F : this.getStandingEyeHeight(pose, entityDimensions);
	}

	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return super.getEyeHeight(pose, entityDimensions);
	}

	public ItemStack getProjectile(ItemStack itemStack) {
		return ItemStack.EMPTY;
	}

	public ItemStack eat(Level level, ItemStack itemStack) {
		if (itemStack.isEdible()) {
			level.gameEvent(this, GameEvent.EAT, this.eyeBlockPosition());
			level.playSound(
				null,
				this.getX(),
				this.getY(),
				this.getZ(),
				this.getEatingSound(itemStack),
				SoundSource.NEUTRAL,
				1.0F,
				1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
			);
			this.addEatEffect(itemStack, level, this);
			if (!(this instanceof Player) || !((Player)this).getAbilities().instabuild) {
				itemStack.shrink(1);
			}

			this.gameEvent(GameEvent.EAT);
		}

		return itemStack;
	}

	private void addEatEffect(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		Item item = itemStack.getItem();
		if (item.isEdible()) {
			for (Pair<MobEffectInstance, Float> pair : item.getFoodProperties().getEffects()) {
				if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
					livingEntity.addEffect(new MobEffectInstance(pair.getFirst()));
				}
			}
		}
	}

	private static byte entityEventForEquipmentBreak(EquipmentSlot equipmentSlot) {
		switch (equipmentSlot) {
			case MAINHAND:
				return 47;
			case OFFHAND:
				return 48;
			case HEAD:
				return 49;
			case CHEST:
				return 50;
			case FEET:
				return 52;
			case LEGS:
				return 51;
			default:
				return 47;
		}
	}

	public void broadcastBreakEvent(EquipmentSlot equipmentSlot) {
		this.level.broadcastEntityEvent(this, entityEventForEquipmentBreak(equipmentSlot));
	}

	public void broadcastBreakEvent(InteractionHand interactionHand) {
		this.broadcastBreakEvent(interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		if (this.getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD)) {
			float f = 0.5F;
			return this.getBoundingBox().inflate(0.5, 0.5, 0.5);
		} else {
			return super.getBoundingBoxForCulling();
		}
	}

	public static EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (!itemStack.is(Items.CARVED_PUMPKIN) && (!(item instanceof BlockItem) || !(((BlockItem)item).getBlock() instanceof AbstractSkullBlock))) {
			if (item instanceof ArmorItem) {
				return ((ArmorItem)item).getSlot();
			} else if (itemStack.is(Items.ELYTRA)) {
				return EquipmentSlot.CHEST;
			} else {
				return itemStack.is(Items.SHIELD) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
			}
		} else {
			return EquipmentSlot.HEAD;
		}
	}

	private static SlotAccess createEquipmentSlotAccess(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		return equipmentSlot != EquipmentSlot.HEAD && equipmentSlot != EquipmentSlot.MAINHAND && equipmentSlot != EquipmentSlot.OFFHAND
			? SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot, itemStack -> itemStack.isEmpty() || Mob.getEquipmentSlotForItem(itemStack) == equipmentSlot)
			: SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot);
	}

	@Nullable
	private static EquipmentSlot getEquipmentSlot(int i) {
		if (i == 100 + EquipmentSlot.HEAD.getIndex()) {
			return EquipmentSlot.HEAD;
		} else if (i == 100 + EquipmentSlot.CHEST.getIndex()) {
			return EquipmentSlot.CHEST;
		} else if (i == 100 + EquipmentSlot.LEGS.getIndex()) {
			return EquipmentSlot.LEGS;
		} else if (i == 100 + EquipmentSlot.FEET.getIndex()) {
			return EquipmentSlot.FEET;
		} else if (i == 98) {
			return EquipmentSlot.MAINHAND;
		} else {
			return i == 99 ? EquipmentSlot.OFFHAND : null;
		}
	}

	@Override
	public SlotAccess getSlot(int i) {
		EquipmentSlot equipmentSlot = getEquipmentSlot(i);
		return equipmentSlot != null ? createEquipmentSlotAccess(this, equipmentSlot) : super.getSlot(i);
	}

	@Override
	public boolean canFreeze() {
		if (this.isSpectator()) {
			return false;
		} else {
			boolean bl = !this.getItemBySlot(EquipmentSlot.HEAD).is(ItemTags.FREEZE_IMMUNE_WEARABLES)
				&& !this.getItemBySlot(EquipmentSlot.CHEST).is(ItemTags.FREEZE_IMMUNE_WEARABLES)
				&& !this.getItemBySlot(EquipmentSlot.LEGS).is(ItemTags.FREEZE_IMMUNE_WEARABLES)
				&& !this.getItemBySlot(EquipmentSlot.FEET).is(ItemTags.FREEZE_IMMUNE_WEARABLES);
			return bl && super.canFreeze();
		}
	}

	public void recreateFromPacket(ClientboundAddMobPacket clientboundAddMobPacket) {
		double d = clientboundAddMobPacket.getX();
		double e = clientboundAddMobPacket.getY();
		double f = clientboundAddMobPacket.getZ();
		float g = (float)(clientboundAddMobPacket.getyRot() * 360) / 256.0F;
		float h = (float)(clientboundAddMobPacket.getxRot() * 360) / 256.0F;
		this.setPacketCoordinates(d, e, f);
		this.yBodyRot = (float)(clientboundAddMobPacket.getyHeadRot() * 360) / 256.0F;
		this.yHeadRot = (float)(clientboundAddMobPacket.getyHeadRot() * 360) / 256.0F;
		this.setId(clientboundAddMobPacket.getId());
		this.setUUID(clientboundAddMobPacket.getUUID());
		this.absMoveTo(d, e, f, g, h);
		this.setDeltaMovement(
			(double)((float)clientboundAddMobPacket.getXd() / 8000.0F),
			(double)((float)clientboundAddMobPacket.getYd() / 8000.0F),
			(double)((float)clientboundAddMobPacket.getZd() / 8000.0F)
		);
	}
}
