package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;

public abstract class LivingEntity extends Entity implements Attackable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String TAG_ACTIVE_EFFECTS = "active_effects";
	private static final ResourceLocation SPEED_MODIFIER_POWDER_SNOW_ID = ResourceLocation.withDefaultNamespace("powder_snow");
	private static final ResourceLocation SPRINTING_MODIFIER_ID = ResourceLocation.withDefaultNamespace("sprinting");
	private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(
		SPRINTING_MODIFIER_ID, 0.3F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);
	public static final int HAND_SLOTS = 2;
	public static final int ARMOR_SLOTS = 4;
	public static final int EQUIPMENT_SLOT_OFFSET = 98;
	public static final int ARMOR_SLOT_OFFSET = 100;
	public static final int BODY_ARMOR_OFFSET = 105;
	public static final int SWING_DURATION = 6;
	public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
	private static final int DAMAGE_SOURCE_TIMEOUT = 40;
	public static final double MIN_MOVEMENT_DISTANCE = 0.003;
	public static final double DEFAULT_BASE_GRAVITY = 0.08;
	public static final int DEATH_DURATION = 20;
	private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
	private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
	public static final float BASE_JUMP_POWER = 0.42F;
	private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 128.0;
	protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
	protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
	protected static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
	protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES = SynchedEntityData.defineId(
		LivingEntity.class, EntityDataSerializers.PARTICLES
	);
	private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(
		LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS
	);
	private static final int PARTICLE_FREQUENCY_WHEN_INVISIBLE = 15;
	protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(0.2F);
	public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5F;
	public static final float DEFAULT_BABY_SCALE = 0.5F;
	public static final String ATTRIBUTES_FIELD = "attributes";
	private final AttributeMap attributes;
	private final CombatTracker combatTracker = new CombatTracker(this);
	private final Map<Holder<MobEffect>, MobEffectInstance> activeEffects = Maps.<Holder<MobEffect>, MobEffectInstance>newHashMap();
	private final NonNullList<ItemStack> lastHandItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
	private final NonNullList<ItemStack> lastArmorItemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
	private ItemStack lastBodyItemStack = ItemStack.EMPTY;
	public boolean swinging;
	private boolean discardFriction = false;
	public InteractionHand swingingArm;
	public int swingTime;
	public int removeArrowTime;
	public int removeStingerTime;
	public int hurtTime;
	public int hurtDuration;
	public int deathTime;
	public float oAttackAnim;
	public float attackAnim;
	protected int attackStrengthTicker;
	public final WalkAnimationState walkAnimation = new WalkAnimationState();
	public final int invulnerableDuration = 20;
	public final float timeOffs;
	public final float rotA;
	public float yBodyRot;
	public float yBodyRotO;
	public float yHeadRot;
	public float yHeadRotO;
	public final ElytraAnimationState elytraAnimationState = new ElytraAnimationState(this);
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
	protected double lerpYHeadRot;
	protected int lerpHeadSteps;
	private boolean effectsDirty = true;
	@Nullable
	private LivingEntity lastHurtByMob;
	private int lastHurtByMobTimestamp;
	@Nullable
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
	protected float autoSpinAttackDmg;
	@Nullable
	protected ItemStack autoSpinAttackItemStack;
	private float swimAmount;
	private float swimAmountO;
	protected Brain<?> brain;
	private boolean skipDropExperience;
	private final EnumMap<EquipmentSlot, Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>>> activeLocationDependentEnchantments = new EnumMap(
		EquipmentSlot.class
	);
	protected float appliedScale = 1.0F;

	protected LivingEntity(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
		this.attributes = new AttributeMap(DefaultAttributes.getSupplier(entityType));
		this.setHealth(this.getMaxHealth());
		this.blocksBuilding = true;
		this.rotA = (float)((Math.random() + 1.0) * 0.01F);
		this.reapplyPosition();
		this.timeOffs = (float)Math.random() * 12398.0F;
		this.setYRot((float)(Math.random() * (float) (Math.PI * 2)));
		this.yHeadRot = this.getYRot();
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
		this.hurt(this.damageSources().genericKill(), Float.MAX_VALUE);
	}

	public boolean canAttackType(EntityType<?> entityType) {
		return true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
		builder.define(DATA_EFFECT_PARTICLES, List.of());
		builder.define(DATA_EFFECT_AMBIENCE_ID, false);
		builder.define(DATA_ARROW_COUNT_ID, 0);
		builder.define(DATA_STINGER_COUNT_ID, 0);
		builder.define(DATA_HEALTH_ID, 1.0F);
		builder.define(SLEEPING_POS_ID, Optional.empty());
	}

	public static AttributeSupplier.Builder createLivingAttributes() {
		return AttributeSupplier.builder()
			.add(Attributes.MAX_HEALTH)
			.add(Attributes.KNOCKBACK_RESISTANCE)
			.add(Attributes.MOVEMENT_SPEED)
			.add(Attributes.ARMOR)
			.add(Attributes.ARMOR_TOUGHNESS)
			.add(Attributes.MAX_ABSORPTION)
			.add(Attributes.STEP_HEIGHT)
			.add(Attributes.SCALE)
			.add(Attributes.GRAVITY)
			.add(Attributes.SAFE_FALL_DISTANCE)
			.add(Attributes.FALL_DAMAGE_MULTIPLIER)
			.add(Attributes.JUMP_STRENGTH)
			.add(Attributes.OXYGEN_BONUS)
			.add(Attributes.BURNING_TIME)
			.add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE)
			.add(Attributes.WATER_MOVEMENT_EFFICIENCY)
			.add(Attributes.MOVEMENT_EFFICIENCY)
			.add(Attributes.ATTACK_KNOCKBACK);
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		if (!this.isInWater()) {
			this.updateInWaterStateAndDoWaterCurrentPushing();
		}

		if (this.level() instanceof ServerLevel serverLevel && bl && this.fallDistance > 0.0F) {
			this.onChangedBlock(serverLevel, blockPos);
			double e = this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
			if ((double)this.fallDistance > e && !blockState.isAir()) {
				double f = this.getX();
				double g = this.getY();
				double h = this.getZ();
				BlockPos blockPos2 = this.blockPosition();
				if (blockPos.getX() != blockPos2.getX() || blockPos.getZ() != blockPos2.getZ()) {
					double i = f - (double)blockPos.getX() - 0.5;
					double j = h - (double)blockPos.getZ() - 0.5;
					double k = Math.max(Math.abs(i), Math.abs(j));
					f = (double)blockPos.getX() + 0.5 + i / k * 0.5;
					h = (double)blockPos.getZ() + 0.5 + j / k * 0.5;
				}

				float l = (float)Mth.ceil((double)this.fallDistance - e);
				double m = Math.min((double)(0.2F + l / 15.0F), 2.5);
				int n = (int)(150.0 * m);
				((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), f, g, h, n, 0.0, 0.0, 0.0, 0.15F);
			}
		}

		super.checkFallDamage(d, bl, blockState, blockPos);
		if (bl) {
			this.lastClimbablePos = Optional.empty();
		}
	}

	public final boolean canBreatheUnderwater() {
		return this.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
	}

	public float getSwimAmount(float f) {
		return Mth.lerp(f, this.swimAmountO, this.swimAmount);
	}

	public boolean hasLandedInLiquid() {
		return this.getDeltaMovement().y() < 1.0E-5F && this.isInLiquid();
	}

	@Override
	public void baseTick() {
		this.oAttackAnim = this.attackAnim;
		if (this.firstTick) {
			this.getSleepingPos().ifPresent(this::setPosToBed);
		}

		if (this.level() instanceof ServerLevel serverLevel) {
			EnchantmentHelper.tickEffects(serverLevel, this);
		}

		super.baseTick();
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("livingEntityBaseTick");
		if (this.fireImmune() || this.level().isClientSide) {
			this.clearFire();
		}

		if (this.isAlive()) {
			boolean bl = this instanceof Player;
			if (!this.level().isClientSide) {
				if (this.isInWall()) {
					this.hurt(this.damageSources().inWall(), 1.0F);
				} else if (bl && !this.level().getWorldBorder().isWithinBounds(this.getBoundingBox())) {
					double d = this.level().getWorldBorder().getDistanceToBorder(this) + this.level().getWorldBorder().getDamageSafeZone();
					if (d < 0.0) {
						double e = this.level().getWorldBorder().getDamagePerBlock();
						if (e > 0.0) {
							this.hurt(this.damageSources().outOfBorder(), (float)Math.max(1, Mth.floor(-d * e)));
						}
					}
				}
			}

			if (this.isEyeInFluid(FluidTags.WATER)
				&& !this.level().getBlockState(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
				boolean bl2 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && (!bl || !((Player)this).getAbilities().invulnerable);
				if (bl2) {
					this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
					if (this.getAirSupply() == -20) {
						this.setAirSupply(0);
						Vec3 vec3 = this.getDeltaMovement();

						for (int i = 0; i < 8; i++) {
							double f = this.random.nextDouble() - this.random.nextDouble();
							double g = this.random.nextDouble() - this.random.nextDouble();
							double h = this.random.nextDouble() - this.random.nextDouble();
							this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + f, this.getY() + g, this.getZ() + h, vec3.x, vec3.y, vec3.z);
						}

						this.hurt(this.damageSources().drown(), 2.0F);
					}
				}

				if (!this.level().isClientSide && this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
					this.stopRiding();
				}
			} else if (this.getAirSupply() < this.getMaxAirSupply()) {
				this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
			}

			if (this.level() instanceof ServerLevel serverLevel2) {
				BlockPos blockPos = this.blockPosition();
				if (!Objects.equal(this.lastPos, blockPos)) {
					this.lastPos = blockPos;
					this.onChangedBlock(serverLevel2, blockPos);
				}
			}
		}

		if (this.isAlive() && (this.isInWaterRainOrBubble() || this.isInPowderSnow)) {
			this.extinguishFire();
		}

		if (this.hurtTime > 0) {
			this.hurtTime--;
		}

		if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
			this.invulnerableTime--;
		}

		if (this.isDeadOrDying() && this.level().shouldTickDeath(this)) {
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
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
		profilerFiller.pop();
	}

	@Override
	protected float getBlockSpeedFactor() {
		return Mth.lerp((float)this.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), super.getBlockSpeedFactor(), 1.0F);
	}

	protected void removeFrost() {
		AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attributeInstance != null) {
			if (attributeInstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
				attributeInstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_ID);
			}
		}
	}

	protected void tryAddFrost() {
		if (!this.getBlockStateOnLegacy().isAir()) {
			int i = this.getTicksFrozen();
			if (i > 0) {
				AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
				if (attributeInstance == null) {
					return;
				}

				float f = -0.05F * this.getPercentFrozen();
				attributeInstance.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_POWDER_SNOW_ID, (double)f, AttributeModifier.Operation.ADD_VALUE));
			}
		}
	}

	protected void onChangedBlock(ServerLevel serverLevel, BlockPos blockPos) {
		EnchantmentHelper.runLocationChangedEffects(serverLevel, this);
	}

	public boolean isBaby() {
		return false;
	}

	public float getAgeScale() {
		return this.isBaby() ? 0.5F : 1.0F;
	}

	public final float getScale() {
		AttributeMap attributeMap = this.getAttributes();
		return attributeMap == null ? 1.0F : this.sanitizeScale((float)attributeMap.getValue(Attributes.SCALE));
	}

	protected float sanitizeScale(float f) {
		return f;
	}

	protected boolean isAffectedByFluids() {
		return true;
	}

	protected void tickDeath() {
		this.deathTime++;
		if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
			this.level().broadcastEntityEvent(this, (byte)60);
			this.remove(Entity.RemovalReason.KILLED);
		}
	}

	public boolean shouldDropExperience() {
		return !this.isBaby();
	}

	protected boolean shouldDropLoot() {
		return !this.isBaby();
	}

	protected int decreaseAirSupply(int i) {
		AttributeInstance attributeInstance = this.getAttribute(Attributes.OXYGEN_BONUS);
		double d;
		if (attributeInstance != null) {
			d = attributeInstance.getValue();
		} else {
			d = 0.0;
		}

		return d > 0.0 && this.random.nextDouble() >= 1.0 / (d + 1.0) ? i : i - 1;
	}

	protected int increaseAirSupply(int i) {
		return Math.min(i + 4, this.getMaxAirSupply());
	}

	public final int getExperienceReward(ServerLevel serverLevel, @Nullable Entity entity) {
		return EnchantmentHelper.processMobExperience(serverLevel, entity, this, this.getBaseExperienceReward());
	}

	protected int getBaseExperienceReward() {
		return 0;
	}

	protected boolean isAlwaysExperienceDropper() {
		return false;
	}

	@Nullable
	public LivingEntity getLastHurtByMob() {
		return this.lastHurtByMob;
	}

	@Override
	public LivingEntity getLastAttacker() {
		return this.getLastHurtByMob();
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

	protected boolean doesEmitEquipEvent(EquipmentSlot equipmentSlot) {
		return true;
	}

	public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
		if (!this.level().isClientSide() && !this.isSpectator()) {
			boolean bl = itemStack2.isEmpty() && itemStack.isEmpty();
			if (!bl && !ItemStack.isSameItemSameComponents(itemStack, itemStack2) && !this.firstTick) {
				Equippable equippable = itemStack2.get(DataComponents.EQUIPPABLE);
				if (!this.isSilent() && equippable != null && equipmentSlot == equippable.slot()) {
					this.level()
						.playSeededSound(null, this.getX(), this.getY(), this.getZ(), equippable.equipSound(), this.getSoundSource(), 1.0F, 1.0F, this.random.nextLong());
				}

				if (this.doesEmitEquipEvent(equipmentSlot)) {
					this.gameEvent(equippable != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
				}
			}
		}
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED || removalReason == Entity.RemovalReason.DISCARDED) {
			this.triggerOnDeathMobEffects(removalReason);
		}

		super.remove(removalReason);
		this.brain.clearMemories();
	}

	protected void triggerOnDeathMobEffects(Entity.RemovalReason removalReason) {
		for (MobEffectInstance mobEffectInstance : this.getActiveEffects()) {
			mobEffectInstance.onMobRemoved(this, removalReason);
		}

		this.activeEffects.clear();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putFloat("Health", this.getHealth());
		compoundTag.putShort("HurtTime", (short)this.hurtTime);
		compoundTag.putInt("HurtByTimestamp", this.lastHurtByMobTimestamp);
		compoundTag.putShort("DeathTime", (short)this.deathTime);
		compoundTag.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
		compoundTag.put("attributes", this.getAttributes().save());
		if (!this.activeEffects.isEmpty()) {
			ListTag listTag = new ListTag();

			for (MobEffectInstance mobEffectInstance : this.activeEffects.values()) {
				listTag.add(mobEffectInstance.save());
			}

			compoundTag.put("active_effects", listTag);
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
		this.internalSetAbsorptionAmount(compoundTag.getFloat("AbsorptionAmount"));
		if (compoundTag.contains("attributes", 9) && this.level() != null && !this.level().isClientSide) {
			this.getAttributes().load(compoundTag.getList("attributes", 10));
		}

		if (compoundTag.contains("active_effects", 9)) {
			ListTag listTag = compoundTag.getList("active_effects", 10);

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
			Scoreboard scoreboard = this.level().getScoreboard();
			PlayerTeam playerTeam = scoreboard.getPlayerTeam(string);
			boolean bl = playerTeam != null && scoreboard.addPlayerToTeam(this.getStringUUID(), playerTeam);
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
		Iterator<Holder<MobEffect>> iterator = this.activeEffects.keySet().iterator();

		try {
			while (iterator.hasNext()) {
				Holder<MobEffect> holder = (Holder<MobEffect>)iterator.next();
				MobEffectInstance mobEffectInstance = (MobEffectInstance)this.activeEffects.get(holder);
				if (!mobEffectInstance.tick(this, () -> this.onEffectUpdated(mobEffectInstance, true, null))) {
					if (!this.level().isClientSide) {
						iterator.remove();
						this.onEffectsRemoved(List.of(mobEffectInstance));
					}
				} else if (mobEffectInstance.getDuration() % 600 == 0) {
					this.onEffectUpdated(mobEffectInstance, false, null);
				}
			}
		} catch (ConcurrentModificationException var6) {
		}

		if (this.effectsDirty) {
			if (!this.level().isClientSide) {
				this.updateInvisibilityStatus();
				this.updateGlowingStatus();
			}

			this.effectsDirty = false;
		}

		List<ParticleOptions> list = this.entityData.get(DATA_EFFECT_PARTICLES);
		if (!list.isEmpty()) {
			boolean bl = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
			int i = this.isInvisible() ? 15 : 4;
			int j = bl ? 5 : 1;
			if (this.random.nextInt(i * j) == 0) {
				this.level().addParticle(Util.getRandom(list, this.random), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 1.0, 1.0, 1.0);
			}
		}
	}

	protected void updateInvisibilityStatus() {
		if (this.activeEffects.isEmpty()) {
			this.removeEffectParticles();
			this.setInvisible(false);
		} else {
			this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
			this.updateSynchronizedMobEffectParticles();
		}
	}

	private void updateSynchronizedMobEffectParticles() {
		List<ParticleOptions> list = this.activeEffects.values().stream().filter(MobEffectInstance::isVisible).map(MobEffectInstance::getParticleOptions).toList();
		this.entityData.set(DATA_EFFECT_PARTICLES, list);
		this.entityData.set(DATA_EFFECT_AMBIENCE_ID, areAllEffectsAmbient(this.activeEffects.values()));
	}

	private void updateGlowingStatus() {
		boolean bl = this.isCurrentlyGlowing();
		if (this.getSharedFlag(6) != bl) {
			this.setSharedFlag(6, bl);
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
				|| entityType == EntityType.PIGLIN && itemStack.is(Items.PIGLIN_HEAD)
				|| entityType == EntityType.PIGLIN_BRUTE && itemStack.is(Items.PIGLIN_HEAD)
				|| entityType == EntityType.CREEPER && itemStack.is(Items.CREEPER_HEAD)) {
				d *= 0.5;
			}
		}

		return d;
	}

	public boolean canAttack(LivingEntity livingEntity) {
		return livingEntity instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL ? false : livingEntity.canBeSeenAsEnemy();
	}

	public boolean canAttack(LivingEntity livingEntity, TargetingConditions targetingConditions) {
		return targetingConditions.test(this, livingEntity);
	}

	public boolean canBeSeenAsEnemy() {
		return !this.isInvulnerable() && this.canBeSeenByAnyone();
	}

	public boolean canBeSeenByAnyone() {
		return !this.isSpectator() && this.isAlive();
	}

	public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> collection) {
		for (MobEffectInstance mobEffectInstance : collection) {
			if (mobEffectInstance.isVisible() && !mobEffectInstance.isAmbient()) {
				return false;
			}
		}

		return true;
	}

	protected void removeEffectParticles() {
		this.entityData.set(DATA_EFFECT_PARTICLES, List.of());
	}

	public boolean removeAllEffects() {
		if (this.level().isClientSide) {
			return false;
		} else if (this.activeEffects.isEmpty()) {
			return false;
		} else {
			Map<Holder<MobEffect>, MobEffectInstance> map = Maps.<Holder<MobEffect>, MobEffectInstance>newHashMap(this.activeEffects);
			this.activeEffects.clear();
			this.onEffectsRemoved(map.values());
			return true;
		}
	}

	public Collection<MobEffectInstance> getActiveEffects() {
		return this.activeEffects.values();
	}

	public Map<Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
		return this.activeEffects;
	}

	public boolean hasEffect(Holder<MobEffect> holder) {
		return this.activeEffects.containsKey(holder);
	}

	@Nullable
	public MobEffectInstance getEffect(Holder<MobEffect> holder) {
		return (MobEffectInstance)this.activeEffects.get(holder);
	}

	public final boolean addEffect(MobEffectInstance mobEffectInstance) {
		return this.addEffect(mobEffectInstance, null);
	}

	public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
		if (!this.canBeAffected(mobEffectInstance)) {
			return false;
		} else {
			MobEffectInstance mobEffectInstance2 = (MobEffectInstance)this.activeEffects.get(mobEffectInstance.getEffect());
			boolean bl = false;
			if (mobEffectInstance2 == null) {
				this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
				this.onEffectAdded(mobEffectInstance, entity);
				bl = true;
				mobEffectInstance.onEffectAdded(this);
			} else if (mobEffectInstance2.update(mobEffectInstance)) {
				this.onEffectUpdated(mobEffectInstance2, true, entity);
				bl = true;
			}

			mobEffectInstance.onEffectStarted(this);
			return bl;
		}
	}

	public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
		if (this.getType().is(EntityTypeTags.IMMUNE_TO_INFESTED)) {
			return !mobEffectInstance.is(MobEffects.INFESTED);
		} else if (this.getType().is(EntityTypeTags.IMMUNE_TO_OOZING)) {
			return !mobEffectInstance.is(MobEffects.OOZING);
		} else {
			return !this.getType().is(EntityTypeTags.IGNORES_POISON_AND_REGEN)
				? true
				: !mobEffectInstance.is(MobEffects.REGENERATION) && !mobEffectInstance.is(MobEffects.POISON);
		}
	}

	public void forceAddEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
		if (this.canBeAffected(mobEffectInstance)) {
			MobEffectInstance mobEffectInstance2 = (MobEffectInstance)this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
			if (mobEffectInstance2 == null) {
				this.onEffectAdded(mobEffectInstance, entity);
			} else {
				mobEffectInstance.copyBlendState(mobEffectInstance2);
				this.onEffectUpdated(mobEffectInstance, true, entity);
			}
		}
	}

	public boolean isInvertedHealAndHarm() {
		return this.getType().is(EntityTypeTags.INVERTED_HEALING_AND_HARM);
	}

	@Nullable
	public MobEffectInstance removeEffectNoUpdate(Holder<MobEffect> holder) {
		return (MobEffectInstance)this.activeEffects.remove(holder);
	}

	public boolean removeEffect(Holder<MobEffect> holder) {
		MobEffectInstance mobEffectInstance = this.removeEffectNoUpdate(holder);
		if (mobEffectInstance != null) {
			this.onEffectsRemoved(List.of(mobEffectInstance));
			return true;
		} else {
			return false;
		}
	}

	protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
		this.effectsDirty = true;
		if (!this.level().isClientSide) {
			mobEffectInstance.getEffect().value().addAttributeModifiers(this.getAttributes(), mobEffectInstance.getAmplifier());
			this.sendEffectToPassengers(mobEffectInstance);
		}
	}

	public void sendEffectToPassengers(MobEffectInstance mobEffectInstance) {
		for (Entity entity : this.getPassengers()) {
			if (entity instanceof ServerPlayer serverPlayer) {
				serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, false));
			}
		}
	}

	protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl, @Nullable Entity entity) {
		this.effectsDirty = true;
		if (bl && !this.level().isClientSide) {
			MobEffect mobEffect = mobEffectInstance.getEffect().value();
			mobEffect.removeAttributeModifiers(this.getAttributes());
			mobEffect.addAttributeModifiers(this.getAttributes(), mobEffectInstance.getAmplifier());
			this.refreshDirtyAttributes();
		}

		if (!this.level().isClientSide) {
			this.sendEffectToPassengers(mobEffectInstance);
		}
	}

	protected void onEffectsRemoved(Collection<MobEffectInstance> collection) {
		this.effectsDirty = true;
		if (!this.level().isClientSide) {
			for (MobEffectInstance mobEffectInstance : collection) {
				mobEffectInstance.getEffect().value().removeAttributeModifiers(this.getAttributes());

				for (Entity entity : this.getPassengers()) {
					if (entity instanceof ServerPlayer serverPlayer) {
						serverPlayer.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
					}
				}
			}

			this.refreshDirtyAttributes();
		}
	}

	private void refreshDirtyAttributes() {
		Set<AttributeInstance> set = this.getAttributes().getAttributesToUpdate();

		for (AttributeInstance attributeInstance : set) {
			this.onAttributeUpdated(attributeInstance.getAttribute());
		}

		set.clear();
	}

	protected void onAttributeUpdated(Holder<Attribute> holder) {
		if (holder.is(Attributes.MAX_HEALTH)) {
			float f = this.getMaxHealth();
			if (this.getHealth() > f) {
				this.setHealth(f);
			}
		} else if (holder.is(Attributes.MAX_ABSORPTION)) {
			float f = this.getMaxAbsorption();
			if (this.getAbsorptionAmount() > f) {
				this.setAbsorptionAmount(f);
			}
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
		} else if (this.level().isClientSide) {
			return false;
		} else if (this.isDeadOrDying()) {
			return false;
		} else if (damageSource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			return false;
		} else {
			if (this.isSleeping() && !this.level().isClientSide) {
				this.stopSleeping();
			}

			this.noActionTime = 0;
			if (f < 0.0F) {
				f = 0.0F;
			}

			float g = f;
			boolean bl = false;
			float h = 0.0F;
			if (f > 0.0F && this.isDamageSourceBlocked(damageSource)) {
				this.hurtCurrentlyUsedShield(f);
				h = f;
				f = 0.0F;
				if (!damageSource.is(DamageTypeTags.IS_PROJECTILE) && damageSource.getDirectEntity() instanceof LivingEntity livingEntity) {
					this.blockUsingShield(livingEntity);
				}

				bl = true;
			}

			if (damageSource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
				f *= 5.0F;
			}

			if (damageSource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				this.hurtHelmet(damageSource, f);
				f *= 0.75F;
			}

			this.walkAnimation.setSpeed(1.5F);
			if (Float.isNaN(f) || Float.isInfinite(f)) {
				f = Float.MAX_VALUE;
			}

			boolean bl2 = true;
			if ((float)this.invulnerableTime > 10.0F && !damageSource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
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

			Entity entity2 = damageSource.getEntity();
			if (entity2 != null) {
				if (entity2 instanceof LivingEntity livingEntity2
					&& !damageSource.is(DamageTypeTags.NO_ANGER)
					&& (!damageSource.is(DamageTypes.WIND_CHARGE) || !this.getType().is(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
					this.setLastHurtByMob(livingEntity2);
				}

				if (entity2 instanceof Player player) {
					this.lastHurtByPlayerTime = 100;
					this.lastHurtByPlayer = player;
				} else if (entity2 instanceof Wolf wolf && wolf.isTame()) {
					this.lastHurtByPlayerTime = 100;
					if (wolf.getOwner() instanceof Player player2) {
						this.lastHurtByPlayer = player2;
					} else {
						this.lastHurtByPlayer = null;
					}
				}
			}

			if (bl2) {
				if (bl) {
					this.level().broadcastEntityEvent(this, (byte)29);
				} else {
					this.level().broadcastDamageEvent(this, damageSource);
				}

				if (!damageSource.is(DamageTypeTags.NO_IMPACT) && (!bl || f > 0.0F)) {
					this.markHurt();
				}

				if (!damageSource.is(DamageTypeTags.NO_KNOCKBACK)) {
					double d = 0.0;
					double e = 0.0;
					if (damageSource.getDirectEntity() instanceof Projectile projectile) {
						DoubleDoubleImmutablePair doubleDoubleImmutablePair = projectile.calculateHorizontalHurtKnockbackDirection(this, damageSource);
						d = -doubleDoubleImmutablePair.leftDouble();
						e = -doubleDoubleImmutablePair.rightDouble();
					} else if (damageSource.getSourcePosition() != null) {
						d = damageSource.getSourcePosition().x() - this.getX();
						e = damageSource.getSourcePosition().z() - this.getZ();
					}

					this.knockback(0.4F, d, e);
					if (!bl) {
						this.indicateDamage(d, e);
					}
				}
			}

			if (this.isDeadOrDying()) {
				if (!this.checkTotemDeathProtection(damageSource)) {
					if (bl2) {
						this.makeSound(this.getDeathSound());
					}

					this.die(damageSource);
				}
			} else if (bl2) {
				this.playHurtSound(damageSource);
			}

			boolean bl3 = !bl || f > 0.0F;
			if (bl3) {
				this.lastDamageSource = damageSource;
				this.lastDamageStamp = this.level().getGameTime();

				for (MobEffectInstance mobEffectInstance : this.getActiveEffects()) {
					mobEffectInstance.onMobHurt(this, damageSource, f);
				}
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
		livingEntity.knockback(0.5, livingEntity.getX() - this.getX(), livingEntity.getZ() - this.getZ());
	}

	private boolean checkTotemDeathProtection(DamageSource damageSource) {
		if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		} else {
			ItemStack itemStack = null;
			DeathProtection deathProtection = null;

			for (InteractionHand interactionHand : InteractionHand.values()) {
				ItemStack itemStack2 = this.getItemInHand(interactionHand);
				deathProtection = itemStack2.get(DataComponents.DEATH_PROTECTION);
				if (deathProtection != null) {
					itemStack = itemStack2.copy();
					itemStack2.shrink(1);
					break;
				}
			}

			if (itemStack != null) {
				if (this instanceof ServerPlayer serverPlayer) {
					serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
					CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, itemStack);
					this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
				}

				this.setHealth(1.0F);
				deathProtection.applyEffects(itemStack, this);
				this.level().broadcastEntityEvent(this, (byte)35);
			}

			return deathProtection != null;
		}
	}

	@Nullable
	public DamageSource getLastDamageSource() {
		if (this.level().getGameTime() - this.lastDamageStamp > 40L) {
			this.lastDamageSource = null;
		}

		return this.lastDamageSource;
	}

	protected void playHurtSound(DamageSource damageSource) {
		this.makeSound(this.getHurtSound(damageSource));
	}

	public void makeSound(@Nullable SoundEvent soundEvent) {
		if (soundEvent != null) {
			this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
		}
	}

	public boolean isDamageSourceBlocked(DamageSource damageSource) {
		Entity entity = damageSource.getDirectEntity();
		boolean bl = false;
		if (entity instanceof AbstractArrow abstractArrow && abstractArrow.getPierceLevel() > 0) {
			bl = true;
		}

		if (!damageSource.is(DamageTypeTags.BYPASSES_SHIELD) && this.isBlocking() && !bl) {
			Vec3 vec3 = damageSource.getSourcePosition();
			if (vec3 != null) {
				Vec3 vec32 = this.calculateViewVector(0.0F, this.getYHeadRot());
				Vec3 vec33 = vec3.vectorTo(this.position());
				vec33 = new Vec3(vec33.x, 0.0, vec33.z).normalize();
				return vec33.dot(vec32) < 0.0;
			}
		}

		return false;
	}

	private void breakItem(ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			if (!this.isSilent()) {
				this.level()
					.playLocalSound(
						this.getX(), this.getY(), this.getZ(), itemStack.getBreakingSound(), this.getSoundSource(), 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F, false
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

			if (!this.level().isClientSide && this.hasCustomName()) {
				LOGGER.info("Named entity {} died: {}", this, this.getCombatTracker().getDeathMessage().getString());
			}

			this.dead = true;
			this.getCombatTracker().recheckStatus();
			if (this.level() instanceof ServerLevel serverLevel) {
				if (entity == null || entity.killedEntity(serverLevel, this)) {
					this.gameEvent(GameEvent.ENTITY_DIE);
					this.dropAllDeathLoot(serverLevel, damageSource);
					this.createWitherRose(livingEntity);
				}

				this.level().broadcastEntityEvent(this, (byte)3);
			}

			this.setPose(Pose.DYING);
		}
	}

	protected void createWitherRose(@Nullable LivingEntity livingEntity) {
		if (!this.level().isClientSide) {
			boolean bl = false;
			if (livingEntity instanceof WitherBoss) {
				if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
					BlockPos blockPos = this.blockPosition();
					BlockState blockState = Blocks.WITHER_ROSE.defaultBlockState();
					if (this.level().getBlockState(blockPos).isAir() && blockState.canSurvive(this.level(), blockPos)) {
						this.level().setBlock(blockPos, blockState, 3);
						bl = true;
					}
				}

				if (!bl) {
					ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
					this.level().addFreshEntity(itemEntity);
				}
			}
		}
	}

	protected void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource) {
		boolean bl = this.lastHurtByPlayerTime > 0;
		if (this.shouldDropLoot() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			this.dropFromLootTable(damageSource, bl);
			this.dropCustomDeathLoot(serverLevel, damageSource, bl);
		}

		this.dropEquipment();
		this.dropExperience(damageSource.getEntity());
	}

	protected void dropEquipment() {
	}

	protected void dropExperience(@Nullable Entity entity) {
		if (this.level() instanceof ServerLevel serverLevel
			&& !this.wasExperienceConsumed()
			&& (
				this.isAlwaysExperienceDropper()
					|| this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
			)) {
			ExperienceOrb.award(serverLevel, this.position(), this.getExperienceReward(serverLevel, entity));
		}
	}

	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
	}

	public long getLootTableSeed() {
		return 0L;
	}

	protected float getKnockback(Entity entity, DamageSource damageSource) {
		float f = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		return this.level() instanceof ServerLevel serverLevel ? EnchantmentHelper.modifyKnockback(serverLevel, this.getWeaponItem(), entity, damageSource, f) : f;
	}

	protected void dropFromLootTable(DamageSource damageSource, boolean bl) {
		Optional<ResourceKey<LootTable>> optional = this.getLootTable();
		if (!optional.isEmpty()) {
			LootTable lootTable = this.level().getServer().reloadableRegistries().getLootTable((ResourceKey<LootTable>)optional.get());
			LootParams.Builder builder = new LootParams.Builder((ServerLevel)this.level())
				.withParameter(LootContextParams.THIS_ENTITY, this)
				.withParameter(LootContextParams.ORIGIN, this.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());
			if (bl && this.lastHurtByPlayer != null) {
				builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
			}

			LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
			lootTable.getRandomItems(lootParams, this.getLootTableSeed(), this::spawnAtLocation);
		}
	}

	public boolean dropFromGiftLootTable(ResourceKey<LootTable> resourceKey, Consumer<ItemStack> consumer) {
		return this.dropFromLootTable(
			resourceKey,
			builder -> builder.withParameter(LootContextParams.ORIGIN, this.position())
					.withParameter(LootContextParams.THIS_ENTITY, this)
					.create(LootContextParamSets.GIFT),
			consumer
		);
	}

	protected void dropFromShearingLootTable(ResourceKey<LootTable> resourceKey, ItemStack itemStack, Consumer<ItemStack> consumer) {
		this.dropFromLootTable(
			resourceKey,
			builder -> builder.withParameter(LootContextParams.ORIGIN, this.position())
					.withParameter(LootContextParams.THIS_ENTITY, this)
					.withParameter(LootContextParams.TOOL, itemStack)
					.create(LootContextParamSets.SHEARING),
			consumer
		);
	}

	protected boolean dropFromLootTable(ResourceKey<LootTable> resourceKey, Function<LootParams.Builder, LootParams> function, Consumer<ItemStack> consumer) {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return false;
		} else {
			LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
			LootParams lootParams = (LootParams)function.apply(new LootParams.Builder(serverLevel));
			boolean bl = false;

			for (ItemStack itemStack : lootTable.getRandomItems(lootParams)) {
				consumer.accept(itemStack);
				bl = true;
			}

			return bl;
		}
	}

	public void knockback(double d, double e, double f) {
		d *= 1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		if (!(d <= 0.0)) {
			this.hasImpulse = true;
			Vec3 vec3 = this.getDeltaMovement();

			while (e * e + f * f < 1.0E-5F) {
				e = (Math.random() - Math.random()) * 0.01;
				f = (Math.random() - Math.random()) * 0.01;
			}

			Vec3 vec32 = new Vec3(e, 0.0, f).normalize().scale(d);
			this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround() ? Math.min(0.4, vec3.y / 2.0 + d) : vec3.y, vec3.z / 2.0 - vec32.z);
		}
	}

	public void indicateDamage(double d, double e) {
	}

	@Nullable
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.GENERIC_HURT;
	}

	@Nullable
	protected SoundEvent getDeathSound() {
		return SoundEvents.GENERIC_DEATH;
	}

	private SoundEvent getFallDamageSound(int i) {
		return i > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
	}

	public void skipDropExperience() {
		this.skipDropExperience = true;
	}

	public boolean wasExperienceConsumed() {
		return this.skipDropExperience;
	}

	public float getHurtDir() {
		return 0.0F;
	}

	protected AABB getHitbox() {
		AABB aABB = this.getBoundingBox();
		Entity entity = this.getVehicle();
		if (entity != null) {
			Vec3 vec3 = entity.getPassengerRidingPosition(this);
			return aABB.setMinY(Math.max(vec3.y, aABB.minY));
		} else {
			return aABB;
		}
	}

	public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantments(EquipmentSlot equipmentSlot) {
		return (Map<Enchantment, Set<EnchantmentLocationBasedEffect>>)this.activeLocationDependentEnchantments
			.computeIfAbsent(equipmentSlot, equipmentSlotx -> new Reference2ObjectArrayMap());
	}

	public LivingEntity.Fallsounds getFallSounds() {
		return new LivingEntity.Fallsounds(SoundEvents.GENERIC_SMALL_FALL, SoundEvents.GENERIC_BIG_FALL);
	}

	public Optional<BlockPos> getLastClimbablePos() {
		return this.lastClimbablePos;
	}

	public boolean onClimbable() {
		if (this.isSpectator()) {
			return false;
		} else {
			BlockPos blockPos = this.blockPosition();
			BlockState blockState = this.getInBlockState();
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

	private boolean trapdoorUsableAsLadder(BlockPos blockPos, BlockState blockState) {
		if (!(Boolean)blockState.getValue(TrapDoorBlock.OPEN)) {
			return false;
		} else {
			BlockState blockState2 = this.level().getBlockState(blockPos.below());
			return blockState2.is(Blocks.LADDER) && blockState2.getValue(LadderBlock.FACING) == blockState.getValue(TrapDoorBlock.FACING);
		}
	}

	@Override
	public boolean isAlive() {
		return !this.isRemoved() && this.getHealth() > 0.0F;
	}

	@Override
	public int getMaxFallDistance() {
		return this.getComfortableFallDistance(0.0F);
	}

	protected final int getComfortableFallDistance(float f) {
		return Mth.floor(f + 3.0F);
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
		if (this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
			return 0;
		} else {
			float h = (float)this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
			float i = f - h;
			return Mth.ceil((double)(i * g) * this.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
		}
	}

	protected void playBlockFallSound() {
		if (!this.isSilent()) {
			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY() - 0.2F);
			int k = Mth.floor(this.getZ());
			BlockState blockState = this.level().getBlockState(new BlockPos(i, j, k));
			if (!blockState.isAir()) {
				SoundType soundType = blockState.getSoundType();
				this.playSound(soundType.getFallSound(), soundType.getVolume() * 0.5F, soundType.getPitch() * 0.75F);
			}
		}
	}

	@Override
	public void animateHurt(float f) {
		this.hurtDuration = 10;
		this.hurtTime = this.hurtDuration;
	}

	public int getArmorValue() {
		return Mth.floor(this.getAttributeValue(Attributes.ARMOR));
	}

	protected void hurtArmor(DamageSource damageSource, float f) {
	}

	protected void hurtHelmet(DamageSource damageSource, float f) {
	}

	protected void hurtCurrentlyUsedShield(float f) {
	}

	protected void doHurtEquipment(DamageSource damageSource, float f, EquipmentSlot... equipmentSlots) {
		if (!(f <= 0.0F)) {
			int i = (int)Math.max(1.0F, f / 4.0F);

			for (EquipmentSlot equipmentSlot : equipmentSlots) {
				ItemStack itemStack = this.getItemBySlot(equipmentSlot);
				Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
				if (equippable != null && equippable.damageOnHurt() && itemStack.isDamageableItem() && itemStack.canBeHurtBy(damageSource)) {
					itemStack.hurtAndBreak(i, this, equipmentSlot);
				}
			}
		}
	}

	protected float getDamageAfterArmorAbsorb(DamageSource damageSource, float f) {
		if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
			this.hurtArmor(damageSource, f);
			f = CombatRules.getDamageAfterAbsorb(this, f, damageSource, (float)this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
		}

		return f;
	}

	protected float getDamageAfterMagicAbsorb(DamageSource damageSource, float f) {
		if (damageSource.is(DamageTypeTags.BYPASSES_EFFECTS)) {
			return f;
		} else {
			if (this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !damageSource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
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
			} else if (damageSource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
				return f;
			} else {
				float l;
				if (this.level() instanceof ServerLevel serverLevel) {
					l = EnchantmentHelper.getDamageProtection(serverLevel, this, damageSource);
				} else {
					l = 0.0F;
				}

				if (l > 0.0F) {
					f = CombatRules.getDamageAfterMagicAbsorb(f, l);
				}

				return f;
			}
		}
	}

	protected void actuallyHurt(DamageSource damageSource, float f) {
		if (!this.isInvulnerableTo(damageSource)) {
			f = this.getDamageAfterArmorAbsorb(damageSource, f);
			f = this.getDamageAfterMagicAbsorb(damageSource, f);
			float var9 = Math.max(f - this.getAbsorptionAmount(), 0.0F);
			this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - var9));
			float h = f - var9;
			if (h > 0.0F && h < 3.4028235E37F && damageSource.getEntity() instanceof ServerPlayer serverPlayer) {
				serverPlayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(h * 10.0F));
			}

			if (var9 != 0.0F) {
				this.getCombatTracker().recordDamage(damageSource, var9);
				this.setHealth(this.getHealth() - var9);
				this.setAbsorptionAmount(this.getAbsorptionAmount() - var9);
				this.gameEvent(GameEvent.ENTITY_DAMAGE);
			}
		}
	}

	public CombatTracker getCombatTracker() {
		return this.combatTracker;
	}

	@Nullable
	public LivingEntity getKillCredit() {
		if (this.lastHurtByPlayer != null) {
			return this.lastHurtByPlayer;
		} else {
			return this.lastHurtByMob != null ? this.lastHurtByMob : null;
		}
	}

	public final float getMaxHealth() {
		return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
	}

	public final float getMaxAbsorption() {
		return (float)this.getAttributeValue(Attributes.MAX_ABSORPTION);
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
			if (this.level() instanceof ServerLevel) {
				ClientboundAnimatePacket clientboundAnimatePacket = new ClientboundAnimatePacket(this, interactionHand == InteractionHand.MAIN_HAND ? 0 : 3);
				ServerChunkCache serverChunkCache = ((ServerLevel)this.level()).getChunkSource();
				if (bl) {
					serverChunkCache.broadcastAndSend(this, clientboundAnimatePacket);
				} else {
					serverChunkCache.broadcast(this, clientboundAnimatePacket);
				}
			}
		}
	}

	@Override
	public void handleDamageEvent(DamageSource damageSource) {
		this.walkAnimation.setSpeed(1.5F);
		this.invulnerableTime = 20;
		this.hurtDuration = 10;
		this.hurtTime = this.hurtDuration;
		SoundEvent soundEvent = this.getHurtSound(damageSource);
		if (soundEvent != null) {
			this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
		}

		this.hurt(this.damageSources().generic(), 0.0F);
		this.lastDamageSource = damageSource;
		this.lastDamageStamp = this.level().getGameTime();
	}

	@Override
	public void handleEntityEvent(byte b) {
		switch (b) {
			case 3:
				SoundEvent soundEvent = this.getDeathSound();
				if (soundEvent != null) {
					this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				}

				if (!(this instanceof Player)) {
					this.setHealth(0.0F);
					this.die(this.damageSources().generic());
				}
				break;
			case 29:
				this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.level().random.nextFloat() * 0.4F);
				break;
			case 30:
				this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
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
					this.level().addParticle(ParticleTypes.PORTAL, e, k, l, (double)f, (double)g, (double)h);
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
				break;
			case 60:
				this.makePoofParticles();
				break;
			case 65:
				this.breakItem(this.getItemBySlot(EquipmentSlot.BODY));
				break;
			default:
				super.handleEntityEvent(b);
		}
	}

	private void makePoofParticles() {
		for (int i = 0; i < 20; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level().addParticle(ParticleTypes.POOF, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), d, e, f);
		}
	}

	private void swapHandItems() {
		ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
		this.setItemSlot(EquipmentSlot.OFFHAND, this.getItemBySlot(EquipmentSlot.MAINHAND));
		this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
	}

	@Override
	protected void onBelowWorld() {
		this.hurt(this.damageSources().fellOutOfWorld(), 4.0F);
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
	public AttributeInstance getAttribute(Holder<Attribute> holder) {
		return this.getAttributes().getInstance(holder);
	}

	public double getAttributeValue(Holder<Attribute> holder) {
		return this.getAttributes().getValue(holder);
	}

	public double getAttributeBaseValue(Holder<Attribute> holder) {
		return this.getAttributes().getBaseValue(holder);
	}

	public AttributeMap getAttributes() {
		return this.attributes;
	}

	public ItemStack getMainHandItem() {
		return this.getItemBySlot(EquipmentSlot.MAINHAND);
	}

	public ItemStack getOffhandItem() {
		return this.getItemBySlot(EquipmentSlot.OFFHAND);
	}

	public ItemStack getItemHeldByArm(HumanoidArm humanoidArm) {
		return this.getMainArm() == humanoidArm ? this.getMainHandItem() : this.getOffhandItem();
	}

	@Nonnull
	@Override
	public ItemStack getWeaponItem() {
		return this.getMainHandItem();
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

	public boolean canUseSlot(EquipmentSlot equipmentSlot) {
		return false;
	}

	public abstract Iterable<ItemStack> getArmorSlots();

	public abstract ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

	public abstract void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack);

	public Iterable<ItemStack> getHandSlots() {
		return List.of();
	}

	public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
		return this.getArmorSlots();
	}

	public Iterable<ItemStack> getAllSlots() {
		return Iterables.concat(this.getHandSlots(), this.getArmorAndBodyArmorSlots());
	}

	protected void verifyEquippedItem(ItemStack itemStack) {
		itemStack.getItem().verifyComponentsAfterLoad(itemStack);
	}

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
		attributeInstance.removeModifier(SPEED_MODIFIER_SPRINTING.id());
		if (bl) {
			attributeInstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
		}
	}

	protected float getSoundVolume() {
		return 1.0F;
	}

	public float getVoicePitch() {
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
		if (this.isRemoved()) {
			vec3 = this.position();
		} else if (!entity.isRemoved() && !this.level().getBlockState(entity.blockPosition()).is(BlockTags.PORTALS)) {
			vec3 = entity.getDismountLocationForPassenger(this);
		} else {
			double d = Math.max(this.getY(), entity.getY());
			vec3 = new Vec3(this.getX(), d, this.getZ());
			boolean bl = this.getBbWidth() <= 4.0F && this.getBbHeight() <= 4.0F;
			if (bl) {
				double e = (double)this.getBbHeight() / 2.0;
				Vec3 vec32 = vec3.add(0.0, e, 0.0);
				VoxelShape voxelShape = Shapes.create(AABB.ofSize(vec32, (double)this.getBbWidth(), (double)this.getBbHeight(), (double)this.getBbWidth()));
				vec3 = (Vec3)this.level()
					.findFreePosition(this, voxelShape, vec32, (double)this.getBbWidth(), (double)this.getBbHeight(), (double)this.getBbWidth())
					.map(vec3x -> vec3x.add(0.0, -e, 0.0))
					.orElse(vec3);
			}
		}

		this.dismountTo(vec3.x, vec3.y, vec3.z);
	}

	@Override
	public boolean shouldShowName() {
		return this.isCustomNameVisible();
	}

	protected float getJumpPower() {
		return this.getJumpPower(1.0F);
	}

	protected float getJumpPower(float f) {
		return (float)this.getAttributeValue(Attributes.JUMP_STRENGTH) * f * this.getBlockJumpFactor() + this.getJumpBoostPower();
	}

	public float getJumpBoostPower() {
		return this.hasEffect(MobEffects.JUMP) ? 0.1F * ((float)this.getEffect(MobEffects.JUMP).getAmplifier() + 1.0F) : 0.0F;
	}

	@VisibleForTesting
	public void jumpFromGround() {
		float f = this.getJumpPower();
		if (!(f <= 1.0E-5F)) {
			Vec3 vec3 = this.getDeltaMovement();
			this.setDeltaMovement(vec3.x, Math.max((double)f, vec3.y), vec3.z);
			if (this.isSprinting()) {
				float g = this.getYRot() * (float) (Math.PI / 180.0);
				this.addDeltaMovement(new Vec3((double)(-Mth.sin(g)) * 0.2, 0.0, (double)Mth.cos(g) * 0.2));
			}

			this.hasImpulse = true;
		}
	}

	protected void goDownInWater() {
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04F, 0.0));
	}

	protected void jumpInLiquid(TagKey<Fluid> tagKey) {
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04F, 0.0));
	}

	protected float getWaterSlowDown() {
		return 0.8F;
	}

	public boolean canStandOnFluid(FluidState fluidState) {
		return false;
	}

	@Override
	protected double getDefaultGravity() {
		return this.getAttributeValue(Attributes.GRAVITY);
	}

	protected double getEffectiveGravity() {
		boolean bl = this.getDeltaMovement().y <= 0.0;
		return bl && this.hasEffect(MobEffects.SLOW_FALLING) ? Math.min(this.getGravity(), 0.01) : this.getGravity();
	}

	public void travel(Vec3 vec3) {
		if (this.isControlledByLocalInstance()) {
			FluidState fluidState = this.level().getFluidState(this.blockPosition());
			if ((this.isInWater() || this.isInLava()) && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
				this.travelInFluid(vec3);
			} else if (this.isFallFlying()) {
				this.travelFallFlying();
			} else {
				this.travelInAir(vec3);
			}
		}
	}

	private void travelInAir(Vec3 vec3) {
		BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
		float f = this.onGround() ? this.level().getBlockState(blockPos).getBlock().getFriction() : 1.0F;
		float g = f * 0.91F;
		Vec3 vec32 = this.handleRelativeFrictionAndCalculateMovement(vec3, f);
		double d = vec32.y;
		MobEffectInstance mobEffectInstance = this.getEffect(MobEffects.LEVITATION);
		if (mobEffectInstance != null) {
			d += (0.05 * (double)(mobEffectInstance.getAmplifier() + 1) - vec32.y) * 0.2;
		} else if (!this.level().isClientSide || this.level().hasChunkAt(blockPos)) {
			d -= this.getEffectiveGravity();
		} else if (this.getY() > (double)this.level().getMinY()) {
			d = -0.1;
		} else {
			d = 0.0;
		}

		if (this.shouldDiscardFriction()) {
			this.setDeltaMovement(vec32.x, d, vec32.z);
		} else {
			float h = this instanceof FlyingAnimal ? g : 0.98F;
			this.setDeltaMovement(vec32.x * (double)g, d * (double)h, vec32.z * (double)g);
		}
	}

	private void travelInFluid(Vec3 vec3) {
		boolean bl = this.getDeltaMovement().y <= 0.0;
		double d = this.getY();
		double e = this.getEffectiveGravity();
		if (this.isInWater()) {
			float f = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
			float g = 0.02F;
			float h = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
			if (!this.onGround()) {
				h *= 0.5F;
			}

			if (h > 0.0F) {
				f += (0.54600006F - f) * h;
				g += (this.getSpeed() - g) * h;
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

			vec32 = vec32.multiply((double)f, 0.8F, (double)f);
			this.setDeltaMovement(this.getFluidFallingAdjustedMovement(e, bl, vec32));
		} else {
			this.moveRelative(0.02F, vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
				Vec3 vec33 = this.getFluidFallingAdjustedMovement(e, bl, this.getDeltaMovement());
				this.setDeltaMovement(vec33);
			} else {
				this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
			}

			if (e != 0.0) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -e / 4.0, 0.0));
			}
		}

		Vec3 vec33 = this.getDeltaMovement();
		if (this.horizontalCollision && this.isFree(vec33.x, vec33.y + 0.6F - this.getY() + d, vec33.z)) {
			this.setDeltaMovement(vec33.x, 0.3F, vec33.z);
		}
	}

	private void travelFallFlying() {
		Vec3 vec3 = this.getDeltaMovement();
		double d = vec3.horizontalDistance();
		this.setDeltaMovement(this.updateFallFlyingMovement(vec3));
		this.move(MoverType.SELF, this.getDeltaMovement());
		if (!this.level().isClientSide) {
			double e = this.getDeltaMovement().horizontalDistance();
			this.handleFallFlyingCollisions(d, e);
		}
	}

	private Vec3 updateFallFlyingMovement(Vec3 vec3) {
		Vec3 vec32 = this.getLookAngle();
		float f = this.getXRot() * (float) (Math.PI / 180.0);
		double d = Math.sqrt(vec32.x * vec32.x + vec32.z * vec32.z);
		double e = vec3.horizontalDistance();
		double g = this.getEffectiveGravity();
		double h = Mth.square(Math.cos((double)f));
		vec3 = vec3.add(0.0, g * (-1.0 + h * 0.75), 0.0);
		if (vec3.y < 0.0 && d > 0.0) {
			double i = vec3.y * -0.1 * h;
			vec3 = vec3.add(vec32.x * i / d, i, vec32.z * i / d);
		}

		if (f < 0.0F && d > 0.0) {
			double i = e * (double)(-Mth.sin(f)) * 0.04;
			vec3 = vec3.add(-vec32.x * i / d, i * 3.2, -vec32.z * i / d);
		}

		if (d > 0.0) {
			vec3 = vec3.add((vec32.x / d * e - vec3.x) * 0.1, 0.0, (vec32.z / d * e - vec3.z) * 0.1);
		}

		return vec3.multiply(0.99F, 0.98F, 0.99F);
	}

	private void handleFallFlyingCollisions(double d, double e) {
		if (this.horizontalCollision) {
			double f = d - e;
			float g = (float)(f * 10.0 - 3.0);
			if (g > 0.0F) {
				this.playSound(this.getFallDamageSound((int)g), 1.0F, 1.0F);
				this.hurt(this.damageSources().flyIntoWall(), g);
			}
		}
	}

	private void travelRidden(Player player, Vec3 vec3) {
		Vec3 vec32 = this.getRiddenInput(player, vec3);
		this.tickRidden(player, vec32);
		if (this.isControlledByLocalInstance()) {
			this.setSpeed(this.getRiddenSpeed(player));
			this.travel(vec32);
		} else {
			this.setDeltaMovement(Vec3.ZERO);
		}
	}

	protected void tickRidden(Player player, Vec3 vec3) {
	}

	protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
		return vec3;
	}

	protected float getRiddenSpeed(Player player) {
		return this.getSpeed();
	}

	public void calculateEntityAnimation(boolean bl) {
		float f = (float)Mth.length(this.getX() - this.xo, bl ? this.getY() - this.yo : 0.0, this.getZ() - this.zo);
		if (!this.isPassenger() && this.isAlive()) {
			this.updateWalkAnimation(f);
		} else {
			this.walkAnimation.stop();
		}
	}

	protected void updateWalkAnimation(float f) {
		float g = Math.min(f * 4.0F, 1.0F);
		this.walkAnimation.update(g, 0.4F, this.isBaby() ? 3.0F : 1.0F);
	}

	private Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 vec3, float f) {
		this.moveRelative(this.getFrictionInfluencedSpeed(f), vec3);
		this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
		this.move(MoverType.SELF, this.getDeltaMovement());
		Vec3 vec32 = this.getDeltaMovement();
		if ((this.horizontalCollision || this.jumping)
			&& (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
			vec32 = new Vec3(vec32.x, 0.2, vec32.z);
		}

		return vec32;
	}

	public Vec3 getFluidFallingAdjustedMovement(double d, boolean bl, Vec3 vec3) {
		if (d != 0.0 && !this.isSprinting()) {
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
			this.resetFallDistance();
			float f = 0.15F;
			double d = Mth.clamp(vec3.x, -0.15F, 0.15F);
			double e = Mth.clamp(vec3.z, -0.15F, 0.15F);
			double g = Math.max(vec3.y, -0.15F);
			if (g < 0.0 && !this.getInBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
				g = 0.0;
			}

			vec3 = new Vec3(d, g, e);
		}

		return vec3;
	}

	private float getFrictionInfluencedSpeed(float f) {
		return this.onGround() ? this.getSpeed() * (0.21600002F / (f * f * f)) : this.getFlyingSpeed();
	}

	protected float getFlyingSpeed() {
		return this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1F : 0.02F;
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
		if (!this.level().isClientSide) {
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

			if (this.isSleeping() && !this.checkBedExists()) {
				this.stopSleeping();
			}
		}

		if (!this.isRemoved()) {
			this.aiStep();
		}

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
			float m = Mth.abs(Mth.wrapDegrees(this.getYRot()) - l);
			if (95.0F < m && m < 265.0F) {
				g = l - 180.0F;
			} else {
				g = l;
			}
		}

		if (this.attackAnim > 0.0F) {
			g = this.getYRot();
		}

		if (!this.onGround()) {
			k = 0.0F;
		}

		this.run = this.run + (k - this.run) * 0.3F;
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("headTurn");
		h = this.tickHeadTurn(g, h);
		profilerFiller.pop();
		profilerFiller.push("rangeChecks");

		while (this.getYRot() - this.yRotO < -180.0F) {
			this.yRotO -= 360.0F;
		}

		while (this.getYRot() - this.yRotO >= 180.0F) {
			this.yRotO += 360.0F;
		}

		while (this.yBodyRot - this.yBodyRotO < -180.0F) {
			this.yBodyRotO -= 360.0F;
		}

		while (this.yBodyRot - this.yBodyRotO >= 180.0F) {
			this.yBodyRotO += 360.0F;
		}

		while (this.getXRot() - this.xRotO < -180.0F) {
			this.xRotO -= 360.0F;
		}

		while (this.getXRot() - this.xRotO >= 180.0F) {
			this.xRotO += 360.0F;
		}

		while (this.yHeadRot - this.yHeadRotO < -180.0F) {
			this.yHeadRotO -= 360.0F;
		}

		while (this.yHeadRot - this.yHeadRotO >= 180.0F) {
			this.yHeadRotO += 360.0F;
		}

		profilerFiller.pop();
		this.animStep += h;
		if (this.isFallFlying()) {
			this.fallFlyTicks++;
		} else {
			this.fallFlyTicks = 0;
		}

		if (this.isSleeping()) {
			this.setXRot(0.0F);
		}

		this.refreshDirtyAttributes();
		float m = this.getScale();
		if (m != this.appliedScale) {
			this.appliedScale = m;
			this.refreshDimensions();
		}

		this.elytraAnimationState.tick();
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

		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
			ItemStack itemStack = switch (equipmentSlot.getType()) {
				case HAND -> this.getLastHandItem(equipmentSlot);
				case HUMANOID_ARMOR -> this.getLastArmorItem(equipmentSlot);
				case ANIMAL_ARMOR -> this.lastBodyItemStack;
			};
			ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
			if (this.equipmentHasChanged(itemStack, itemStack2)) {
				if (map == null) {
					map = Maps.newEnumMap(EquipmentSlot.class);
				}

				map.put(equipmentSlot, itemStack2);
				AttributeMap attributeMap = this.getAttributes();
				if (!itemStack.isEmpty()) {
					this.stopLocationBasedEffects(itemStack, equipmentSlot, attributeMap);
				}
			}
		}

		if (map != null) {
			for (Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
				EquipmentSlot equipmentSlot2 = (EquipmentSlot)entry.getKey();
				ItemStack itemStack2 = (ItemStack)entry.getValue();
				if (!itemStack2.isEmpty() && !itemStack2.isBroken()) {
					itemStack2.forEachModifier(equipmentSlot2, (holder, attributeModifier) -> {
						AttributeInstance attributeInstance = this.attributes.getInstance(holder);
						if (attributeInstance != null) {
							attributeInstance.removeModifier(attributeModifier.id());
							attributeInstance.addTransientModifier(attributeModifier);
						}
					});
					if (this.level() instanceof ServerLevel serverLevel) {
						EnchantmentHelper.runLocationChangedEffects(serverLevel, itemStack2, this, equipmentSlot2);
					}
				}
			}
		}

		return map;
	}

	public boolean equipmentHasChanged(ItemStack itemStack, ItemStack itemStack2) {
		return !ItemStack.matches(itemStack2, itemStack);
	}

	private void handleHandSwap(Map<EquipmentSlot, ItemStack> map) {
		ItemStack itemStack = (ItemStack)map.get(EquipmentSlot.MAINHAND);
		ItemStack itemStack2 = (ItemStack)map.get(EquipmentSlot.OFFHAND);
		if (itemStack != null
			&& itemStack2 != null
			&& ItemStack.matches(itemStack, this.getLastHandItem(EquipmentSlot.OFFHAND))
			&& ItemStack.matches(itemStack2, this.getLastHandItem(EquipmentSlot.MAINHAND))) {
			((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundEntityEventPacket(this, (byte)55));
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
				case HUMANOID_ARMOR:
					this.setLastArmorItem(equipmentSlot, itemStack2);
					break;
				case ANIMAL_ARMOR:
					this.lastBodyItemStack = itemStack2;
			}
		});
		((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEquipmentPacket(this.getId(), list));
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
		float i = Mth.wrapDegrees(this.getYRot() - this.yBodyRot);
		float j = this.getMaxHeadRotationRelativeToBody();
		if (Math.abs(i) > j) {
			this.yBodyRot = this.yBodyRot + (i - (float)Mth.sign((double)i) * j);
		}

		boolean bl = i < -90.0F || i >= 90.0F;
		if (bl) {
			g *= -1.0F;
		}

		return g;
	}

	protected float getMaxHeadRotationRelativeToBody() {
		return 50.0F;
	}

	public void aiStep() {
		if (this.noJumpDelay > 0) {
			this.noJumpDelay--;
		}

		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
			this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpSteps > 0) {
			this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
			this.lerpSteps--;
		} else if (!this.isEffectiveAi()) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
		}

		if (this.lerpHeadSteps > 0) {
			this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
			this.lerpHeadSteps--;
		}

		Vec3 vec3 = this.getDeltaMovement();
		double d = vec3.x;
		double e = vec3.y;
		double f = vec3.z;
		if (Math.abs(vec3.x) < 0.003) {
			d = 0.0;
		}

		if (Math.abs(vec3.y) < 0.003) {
			e = 0.0;
		}

		if (Math.abs(vec3.z) < 0.003) {
			f = 0.0;
		}

		this.setDeltaMovement(d, e, f);
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("ai");
		if (this.isImmobile()) {
			this.jumping = false;
			this.xxa = 0.0F;
			this.zza = 0.0F;
		} else if (this.isEffectiveAi()) {
			profilerFiller.push("newAi");
			this.serverAiStep();
			profilerFiller.pop();
		}

		profilerFiller.pop();
		profilerFiller.push("jump");
		if (this.jumping && this.isAffectedByFluids()) {
			double g;
			if (this.isInLava()) {
				g = this.getFluidHeight(FluidTags.LAVA);
			} else {
				g = this.getFluidHeight(FluidTags.WATER);
			}

			boolean bl = this.isInWater() && g > 0.0;
			double h = this.getFluidJumpThreshold();
			if (!bl || this.onGround() && !(g > h)) {
				if (!this.isInLava() || this.onGround() && !(g > h)) {
					if ((this.onGround() || bl && g <= h) && this.noJumpDelay == 0) {
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

		profilerFiller.pop();
		profilerFiller.push("travel");
		this.xxa *= 0.98F;
		this.zza *= 0.98F;
		if (this.isFallFlying()) {
			this.updateFallFlying();
		}

		AABB aABB = this.getBoundingBox();
		Vec3 vec32 = new Vec3((double)this.xxa, (double)this.yya, (double)this.zza);
		if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
			this.resetFallDistance();
		}

		label115: {
			if (this.getControllingPassenger() instanceof Player player && this.isAlive()) {
				this.travelRidden(player, vec32);
				break label115;
			}

			this.travel(vec32);
		}

		if (!this.level().isClientSide() || this.isControlledByLocalInstance()) {
			this.applyEffectsFromBlocks();
		}

		this.calculateEntityAnimation(this instanceof FlyingAnimal);
		profilerFiller.pop();
		profilerFiller.push("freezing");
		if (!this.level().isClientSide && !this.isDeadOrDying()) {
			int i = this.getTicksFrozen();
			if (this.isInPowderSnow && this.canFreeze()) {
				this.setTicksFrozen(Math.min(this.getTicksRequiredToFreeze(), i + 1));
			} else {
				this.setTicksFrozen(Math.max(0, i - 2));
			}
		}

		this.removeFrost();
		this.tryAddFrost();
		if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
			this.hurt(this.damageSources().freeze(), 1.0F);
		}

		profilerFiller.pop();
		profilerFiller.push("push");
		if (this.autoSpinAttackTicks > 0) {
			this.autoSpinAttackTicks--;
			this.checkAutoSpinAttack(aABB, this.getBoundingBox());
		}

		this.pushEntities();
		profilerFiller.pop();
		if (!this.level().isClientSide && this.isSensitiveToWater() && this.isInWaterRainOrBubble()) {
			this.hurt(this.damageSources().drown(), 1.0F);
		}
	}

	public boolean isSensitiveToWater() {
		return false;
	}

	protected void updateFallFlying() {
		this.checkSlowFallDistance();
		if (!this.level().isClientSide) {
			if (!this.canGlide()) {
				this.setSharedFlag(7, false);
				return;
			}

			int i = this.fallFlyTicks + 1;
			if (i % 10 == 0) {
				int j = i / 10;
				if (j % 2 == 0) {
					List<EquipmentSlot> list = EquipmentSlot.VALUES
						.stream()
						.filter(equipmentSlotx -> canGlideUsing(this.getItemBySlot(equipmentSlotx), equipmentSlotx))
						.toList();
					EquipmentSlot equipmentSlot = Util.getRandom(list, this.random);
					this.getItemBySlot(equipmentSlot).hurtAndBreak(1, this, equipmentSlot);
				}

				this.gameEvent(GameEvent.ELYTRA_GLIDE);
			}
		}
	}

	protected boolean canGlide() {
		if (!this.onGround() && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
			for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
				if (canGlideUsing(this.getItemBySlot(equipmentSlot), equipmentSlot)) {
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}

	protected void serverAiStep() {
	}

	protected void pushEntities() {
		if (this.level().isClientSide()) {
			this.level().getEntities(EntityTypeTest.forClass(Player.class), this.getBoundingBox(), EntitySelector.pushableBy(this)).forEach(this::doPush);
		} else {
			List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this));
			if (!list.isEmpty()) {
				int i = this.level().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
				if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
					int j = 0;

					for (Entity entity : list) {
						if (!entity.isPassenger()) {
							j++;
						}
					}

					if (j > i - 1) {
						this.hurt(this.damageSources().cramming(), 6.0F);
					}
				}

				for (Entity entity2 : list) {
					this.doPush(entity2);
				}
			}
		}
	}

	protected void checkAutoSpinAttack(AABB aABB, AABB aABB2) {
		AABB aABB3 = aABB.minmax(aABB2);
		List<Entity> list = this.level().getEntities(this, aABB3);
		if (!list.isEmpty()) {
			for (Entity entity : list) {
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

		if (!this.level().isClientSide && this.autoSpinAttackTicks <= 0) {
			this.setLivingEntityFlag(4, false);
			this.autoSpinAttackDmg = 0.0F;
			this.autoSpinAttackItemStack = null;
		}
	}

	protected void doPush(Entity entity) {
		entity.push(this);
	}

	protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
	}

	public boolean isAutoSpinAttack() {
		return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
	}

	@Override
	public void stopRiding() {
		Entity entity = this.getVehicle();
		super.stopRiding();
		if (entity != null && entity != this.getVehicle() && !this.level().isClientSide) {
			this.dismountVehicle(entity);
		}
	}

	@Override
	public void rideTick() {
		super.rideTick();
		this.oRun = this.run;
		this.run = 0.0F;
		this.resetFallDistance();
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.lerpX = d;
		this.lerpY = e;
		this.lerpZ = f;
		this.lerpYRot = (double)g;
		this.lerpXRot = (double)h;
		this.lerpSteps = i;
	}

	@Override
	public double lerpTargetX() {
		return this.lerpSteps > 0 ? this.lerpX : this.getX();
	}

	@Override
	public double lerpTargetY() {
		return this.lerpSteps > 0 ? this.lerpY : this.getY();
	}

	@Override
	public double lerpTargetZ() {
		return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return this.lerpSteps > 0 ? (float)this.lerpXRot : this.getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return this.lerpSteps > 0 ? (float)this.lerpYRot : this.getYRot();
	}

	@Override
	public void lerpHeadTo(float f, int i) {
		this.lerpYHeadRot = (double)f;
		this.lerpHeadSteps = i;
	}

	public void setJumping(boolean bl) {
		this.jumping = bl;
	}

	public void onItemPickup(ItemEntity itemEntity) {
		Entity entity = itemEntity.getOwner();
		if (entity instanceof ServerPlayer) {
			CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayer)entity, itemEntity.getItem(), this);
		}
	}

	public void take(Entity entity, int i) {
		if (!entity.isRemoved() && !this.level().isClientSide && (entity instanceof ItemEntity || entity instanceof AbstractArrow || entity instanceof ExperienceOrb)
			)
		 {
			((ServerLevel)this.level()).getChunkSource().broadcast(entity, new ClientboundTakeItemEntityPacket(entity.getId(), this.getId(), i));
		}
	}

	public boolean hasLineOfSight(Entity entity) {
		if (entity.level() != this.level()) {
			return false;
		} else {
			Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
			Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
			return vec32.distanceTo(vec3) > 128.0
				? false
				: this.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
		}
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

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean isPushable() {
		return this.isAlive() && !this.isSpectator() && !this.onClimbable();
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
	public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
		return resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
	}

	public static Vec3 resetForwardDirectionOfRelativePortalPosition(Vec3 vec3) {
		return new Vec3(vec3.x, vec3.y, 0.0);
	}

	public float getAbsorptionAmount() {
		return this.absorptionAmount;
	}

	public final void setAbsorptionAmount(float f) {
		this.internalSetAbsorptionAmount(Mth.clamp(f, 0.0F, this.getMaxAbsorption()));
	}

	protected void internalSetAbsorptionAmount(float f) {
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
			if (ItemStack.isSameItem(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
				this.useItem = this.getItemInHand(this.getUsedItemHand());
				this.updateUsingItem(this.useItem);
			} else {
				this.stopUsingItem();
			}
		}
	}

	protected void updateUsingItem(ItemStack itemStack) {
		itemStack.onUseTick(this.level(), this, this.getUseItemRemainingTicks());
		if (--this.useItemRemaining == 0 && !this.level().isClientSide && !itemStack.useOnRelease()) {
			this.completeUsingItem();
		}
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
			this.useItemRemaining = itemStack.getUseDuration(this);
			if (!this.level().isClientSide) {
				this.setLivingEntityFlag(1, true);
				this.setLivingEntityFlag(2, interactionHand == InteractionHand.OFF_HAND);
				this.gameEvent(GameEvent.ITEM_INTERACT_START);
			}
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (SLEEPING_POS_ID.equals(entityDataAccessor)) {
			if (this.level().isClientSide) {
				this.getSleepingPos().ifPresent(this::setPosToBed);
			}
		} else if (DATA_LIVING_ENTITY_FLAGS.equals(entityDataAccessor) && this.level().isClientSide) {
			if (this.isUsingItem() && this.useItem.isEmpty()) {
				this.useItem = this.getItemInHand(this.getUsedItemHand());
				if (!this.useItem.isEmpty()) {
					this.useItemRemaining = this.useItem.getUseDuration(this);
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

	@Override
	public float getPreciseBodyRotation(float f) {
		return Mth.lerp(f, this.yBodyRotO, this.yBodyRot);
	}

	public void spawnItemParticles(ItemStack itemStack, int i) {
		for (int j = 0; j < i; j++) {
			Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
			vec3 = vec3.xRot(-this.getXRot() * (float) (Math.PI / 180.0));
			vec3 = vec3.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
			double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
			Vec3 vec32 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
			vec32 = vec32.xRot(-this.getXRot() * (float) (Math.PI / 180.0));
			vec32 = vec32.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
			vec32 = vec32.add(this.getX(), this.getEyeY(), this.getZ());
			this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
		}
	}

	protected void completeUsingItem() {
		if (!this.level().isClientSide || this.isUsingItem()) {
			InteractionHand interactionHand = this.getUsedItemHand();
			if (!this.useItem.equals(this.getItemInHand(interactionHand))) {
				this.releaseUsingItem();
			} else {
				if (!this.useItem.isEmpty() && this.isUsingItem()) {
					ItemStack itemStack = this.useItem.finishUsingItem(this.level(), this);
					if (itemStack != this.useItem) {
						this.setItemInHand(interactionHand, itemStack);
					}

					this.stopUsingItem();
				}
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
		return this.isUsingItem() ? this.useItem.getUseDuration(this) - this.getUseItemRemainingTicks() : 0;
	}

	public void releaseUsingItem() {
		if (!this.useItem.isEmpty()) {
			this.useItem.releaseUsing(this.level(), this, this.getUseItemRemainingTicks());
			if (this.useItem.useOnRelease()) {
				this.updatingUsingItem();
			}
		}

		this.stopUsingItem();
	}

	public void stopUsingItem() {
		if (!this.level().isClientSide) {
			boolean bl = this.isUsingItem();
			this.setLivingEntityFlag(1, false);
			if (bl) {
				this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
			}
		}

		this.useItem = ItemStack.EMPTY;
		this.useItemRemaining = 0;
	}

	public boolean isBlocking() {
		return this.getItemBlockingWith() != null;
	}

	@Nullable
	public ItemStack getItemBlockingWith() {
		if (this.isUsingItem() && !this.useItem.isEmpty()) {
			Item item = this.useItem.getItem();
			if (item.getUseAnimation(this.useItem) != ItemUseAnimation.BLOCK) {
				return null;
			} else {
				return item.getUseDuration(this.useItem, this) - this.useItemRemaining < 5 ? null : this.useItem;
			}
		} else {
			return null;
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
		return super.isVisuallySwimming() || !this.isFallFlying() && this.hasPose(Pose.FALL_FLYING);
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
		BlockPos blockPos = BlockPos.containing(d, e, f);
		Level level = this.level();
		if (level.hasChunkAt(blockPos)) {
			boolean bl3 = false;

			while (!bl3 && blockPos.getY() > level.getMinY()) {
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState = level.getBlockState(blockPos2);
				if (blockState.blocksMotion()) {
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

			if (this instanceof PathfinderMob pathfinderMob) {
				pathfinderMob.getNavigation().stop();
			}

			return true;
		}
	}

	public boolean isAffectedByPotions() {
		return !this.isDeadOrDying();
	}

	public boolean attackable() {
		return true;
	}

	public void setRecordPlayingNearby(BlockPos blockPos, boolean bl) {
	}

	public boolean canPickUpLoot() {
		return false;
	}

	@Override
	public final EntityDimensions getDimensions(Pose pose) {
		return pose == Pose.SLEEPING ? SLEEPING_DIMENSIONS : this.getDefaultDimensions(pose).scale(this.getScale());
	}

	protected EntityDimensions getDefaultDimensions(Pose pose) {
		return this.getType().getDimensions().scale(this.getAgeScale());
	}

	public ImmutableList<Pose> getDismountPoses() {
		return ImmutableList.of(Pose.STANDING);
	}

	public AABB getLocalBoundsForPose(Pose pose) {
		EntityDimensions entityDimensions = this.getDimensions(pose);
		return new AABB(
			(double)(-entityDimensions.width() / 2.0F),
			0.0,
			(double)(-entityDimensions.width() / 2.0F),
			(double)(entityDimensions.width() / 2.0F),
			(double)entityDimensions.height(),
			(double)(entityDimensions.width() / 2.0F)
		);
	}

	protected boolean wouldNotSuffocateAtTargetPose(Pose pose) {
		AABB aABB = this.getDimensions(pose).makeBoundingBox(this.position());
		return this.level().noBlockCollision(this, aABB);
	}

	@Override
	public boolean canUsePortal(boolean bl) {
		return super.canUsePortal(bl) && !this.isSleeping();
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

		BlockState blockState = this.level().getBlockState(blockPos);
		if (blockState.getBlock() instanceof BedBlock) {
			this.level().setBlock(blockPos, blockState.setValue(BedBlock.OCCUPIED, Boolean.valueOf(true)), 3);
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
		return (Boolean)this.getSleepingPos().map(blockPos -> this.level().getBlockState(blockPos).getBlock() instanceof BedBlock).orElse(false);
	}

	public void stopSleeping() {
		this.getSleepingPos().filter(this.level()::hasChunkAt).ifPresent(blockPos -> {
			BlockState blockState = this.level().getBlockState(blockPos);
			if (blockState.getBlock() instanceof BedBlock) {
				Direction direction = blockState.getValue(BedBlock.FACING);
				this.level().setBlock(blockPos, blockState.setValue(BedBlock.OCCUPIED, Boolean.valueOf(false)), 3);
				Vec3 vec3x = (Vec3)BedBlock.findStandUpPosition(this.getType(), this.level(), blockPos, direction, this.getYRot()).orElseGet(() -> {
					BlockPos blockPos2 = blockPos.above();
					return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.1, (double)blockPos2.getZ() + 0.5);
				});
				Vec3 vec32 = Vec3.atBottomCenterOf(blockPos).subtract(vec3x).normalize();
				float f = (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 180.0F / (float)Math.PI - 90.0);
				this.setPos(vec3x.x, vec3x.y, vec3x.z);
				this.setYRot(f);
				this.setXRot(0.0F);
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
		return blockPos != null ? BedBlock.getBedOrientation(this.level(), blockPos) : null;
	}

	@Override
	public boolean isInWall() {
		return !this.isSleeping() && super.isInWall();
	}

	public ItemStack getProjectile(ItemStack itemStack) {
		return ItemStack.EMPTY;
	}

	private static byte entityEventForEquipmentBreak(EquipmentSlot equipmentSlot) {
		return switch (equipmentSlot) {
			case MAINHAND -> 47;
			case OFFHAND -> 48;
			case HEAD -> 49;
			case CHEST -> 50;
			case FEET -> 52;
			case LEGS -> 51;
			case BODY -> 65;
		};
	}

	public void onEquippedItemBroken(Item item, EquipmentSlot equipmentSlot) {
		this.level().broadcastEntityEvent(this, entityEventForEquipmentBreak(equipmentSlot));
		this.stopLocationBasedEffects(this.getItemBySlot(equipmentSlot), equipmentSlot, this.attributes);
	}

	private void stopLocationBasedEffects(ItemStack itemStack, EquipmentSlot equipmentSlot, AttributeMap attributeMap) {
		itemStack.forEachModifier(equipmentSlot, (holder, attributeModifier) -> {
			AttributeInstance attributeInstance = attributeMap.getInstance(holder);
			if (attributeInstance != null) {
				attributeInstance.removeModifier(attributeModifier);
			}
		});
		EnchantmentHelper.stopLocationBasedEffects(itemStack, this, equipmentSlot);
	}

	public static EquipmentSlot getSlotForHand(InteractionHand interactionHand) {
		return interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
	}

	public final boolean canEquipWithDispenser(ItemStack itemStack) {
		if (this.isAlive() && !this.isSpectator()) {
			Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
			if (equippable != null && equippable.dispensable()) {
				EquipmentSlot equipmentSlot = equippable.slot();
				return this.canUseSlot(equipmentSlot) && equippable.canBeEquippedBy(this.getType())
					? this.getItemBySlot(equipmentSlot).isEmpty() && this.canDispenserEquipIntoSlot(equipmentSlot)
					: false;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
		return true;
	}

	public final EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		return equippable != null && this.canUseSlot(equippable.slot()) ? equippable.slot() : EquipmentSlot.MAINHAND;
	}

	public final boolean isEquippableInSlot(ItemStack itemStack, EquipmentSlot equipmentSlot) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		return equippable == null
			? equipmentSlot == EquipmentSlot.MAINHAND && this.canUseSlot(EquipmentSlot.MAINHAND)
			: equipmentSlot == equippable.slot() && this.canUseSlot(equippable.slot()) && equippable.canBeEquippedBy(this.getType());
	}

	private static SlotAccess createEquipmentSlotAccess(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		return equipmentSlot != EquipmentSlot.HEAD && equipmentSlot != EquipmentSlot.MAINHAND && equipmentSlot != EquipmentSlot.OFFHAND
			? SlotAccess.forEquipmentSlot(
				livingEntity, equipmentSlot, itemStack -> itemStack.isEmpty() || livingEntity.getEquipmentSlotForItem(itemStack) == equipmentSlot
			)
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
		} else if (i == 99) {
			return EquipmentSlot.OFFHAND;
		} else {
			return i == 105 ? EquipmentSlot.BODY : null;
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
				&& !this.getItemBySlot(EquipmentSlot.FEET).is(ItemTags.FREEZE_IMMUNE_WEARABLES)
				&& !this.getItemBySlot(EquipmentSlot.BODY).is(ItemTags.FREEZE_IMMUNE_WEARABLES);
			return bl && super.canFreeze();
		}
	}

	@Override
	public boolean isCurrentlyGlowing() {
		return !this.level().isClientSide() && this.hasEffect(MobEffects.GLOWING) || super.isCurrentlyGlowing();
	}

	@Override
	public float getVisualRotationYInDegrees() {
		return this.yBodyRot;
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		double d = clientboundAddEntityPacket.getX();
		double e = clientboundAddEntityPacket.getY();
		double f = clientboundAddEntityPacket.getZ();
		float g = clientboundAddEntityPacket.getYRot();
		float h = clientboundAddEntityPacket.getXRot();
		this.syncPacketPositionCodec(d, e, f);
		this.yBodyRot = clientboundAddEntityPacket.getYHeadRot();
		this.yHeadRot = clientboundAddEntityPacket.getYHeadRot();
		this.yBodyRotO = this.yBodyRot;
		this.yHeadRotO = this.yHeadRot;
		this.setId(clientboundAddEntityPacket.getId());
		this.setUUID(clientboundAddEntityPacket.getUUID());
		this.absMoveTo(d, e, f, g, h);
		this.setDeltaMovement(clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
	}

	public boolean canDisableShield() {
		return this.getWeaponItem().getItem() instanceof AxeItem;
	}

	@Override
	public float maxUpStep() {
		float f = (float)this.getAttributeValue(Attributes.STEP_HEIGHT);
		return this.getControllingPassenger() instanceof Player ? Math.max(f, 1.0F) : f;
	}

	@Override
	public Vec3 getPassengerRidingPosition(Entity entity) {
		return this.position().add(this.getPassengerAttachmentPoint(entity, this.getDimensions(this.getPose()), this.getScale() * this.getAgeScale()));
	}

	protected void lerpHeadRotationStep(int i, double d) {
		this.yHeadRot = (float)Mth.rotLerp(1.0 / (double)i, (double)this.yHeadRot, d);
	}

	@Override
	public void igniteForTicks(int i) {
		super.igniteForTicks(Mth.ceil((double)i * this.getAttributeValue(Attributes.BURNING_TIME)));
	}

	public boolean hasInfiniteMaterials() {
		return false;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		if (super.isInvulnerableTo(damageSource)) {
			return true;
		} else {
			if (this.level() instanceof ServerLevel serverLevel && EnchantmentHelper.isImmuneToDamage(serverLevel, this, damageSource)) {
				return true;
			}

			return false;
		}
	}

	public static boolean canGlideUsing(ItemStack itemStack, EquipmentSlot equipmentSlot) {
		if (!itemStack.has(DataComponents.GLIDER)) {
			return false;
		} else {
			Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
			return equippable != null && equipmentSlot == equippable.slot() && !itemStack.nextDamageWillBreak();
		}
	}

	@VisibleForTesting
	public int getLastHurtByPlayerTime() {
		return this.lastHurtByPlayerTime;
	}

	public static record Fallsounds(SoundEvent small, SoundEvent big) {
	}
}
