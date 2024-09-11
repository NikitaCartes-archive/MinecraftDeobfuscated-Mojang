package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class Entity implements SyncedDataHolder, Nameable, EntityAccess, CommandSource, ScoreHolder {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String ID_TAG = "id";
	public static final String PASSENGERS_TAG = "Passengers";
	private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
	public static final int CONTENTS_SLOT_INDEX = 0;
	public static final int BOARDING_COOLDOWN = 60;
	public static final int TOTAL_AIR_SUPPLY = 300;
	public static final int MAX_ENTITY_TAG_COUNT = 1024;
	public static final float DELTA_AFFECTED_BY_BLOCKS_BELOW_0_2 = 0.2F;
	public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_0_5 = 0.500001;
	public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_1_0 = 0.999999;
	public static final int BASE_TICKS_REQUIRED_TO_FREEZE = 140;
	public static final int FREEZE_HURT_FREQUENCY = 40;
	public static final int BASE_SAFE_FALL_DISTANCE = 3;
	private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	private static final double WATER_FLOW_SCALE = 0.014;
	private static final double LAVA_FAST_FLOW_SCALE = 0.007;
	private static final double LAVA_SLOW_FLOW_SCALE = 0.0023333333333333335;
	public static final String UUID_TAG = "UUID";
	private static double viewScale = 1.0;
	private final EntityType<?> type;
	private int id = ENTITY_COUNTER.incrementAndGet();
	public boolean blocksBuilding;
	private ImmutableList<Entity> passengers = ImmutableList.of();
	protected int boardingCooldown;
	@Nullable
	private Entity vehicle;
	private Level level;
	public double xo;
	public double yo;
	public double zo;
	private Vec3 position;
	private BlockPos blockPosition;
	private ChunkPos chunkPosition;
	private Vec3 deltaMovement = Vec3.ZERO;
	private float yRot;
	private float xRot;
	public float yRotO;
	public float xRotO;
	private AABB bb = INITIAL_AABB;
	private boolean onGround;
	public boolean horizontalCollision;
	public boolean verticalCollision;
	public boolean verticalCollisionBelow;
	public boolean minorHorizontalCollision;
	public boolean hurtMarked;
	protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
	@Nullable
	private Entity.RemovalReason removalReason;
	public static final float DEFAULT_BB_WIDTH = 0.6F;
	public static final float DEFAULT_BB_HEIGHT = 1.8F;
	public float moveDist;
	public float flyDist;
	public float fallDistance;
	private float nextStep = 1.0F;
	public double xOld;
	public double yOld;
	public double zOld;
	public boolean noPhysics;
	protected final RandomSource random = RandomSource.create();
	public int tickCount;
	private int remainingFireTicks = -this.getFireImmuneTicks();
	protected boolean wasTouchingWater;
	protected Object2DoubleMap<TagKey<Fluid>> fluidHeight = new Object2DoubleArrayMap<>(2);
	protected boolean wasEyeInWater;
	private final Set<TagKey<Fluid>> fluidOnEyes = new HashSet();
	public int invulnerableTime;
	protected boolean firstTick = true;
	protected final SynchedEntityData entityData;
	protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
	protected static final int FLAG_ONFIRE = 0;
	private static final int FLAG_SHIFT_KEY_DOWN = 1;
	private static final int FLAG_SPRINTING = 3;
	private static final int FLAG_SWIMMING = 4;
	private static final int FLAG_INVISIBLE = 5;
	protected static final int FLAG_GLOWING = 6;
	protected static final int FLAG_FALL_FLYING = 7;
	private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(
		Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT
	);
	private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Pose> DATA_POSE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.POSE);
	private static final EntityDataAccessor<Integer> DATA_TICKS_FROZEN = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
	private EntityInLevelCallback levelCallback = EntityInLevelCallback.NULL;
	private final VecDeltaCodec packetPositionCodec = new VecDeltaCodec();
	public boolean hasImpulse;
	@Nullable
	public PortalProcessor portalProcess;
	private int portalCooldown;
	private boolean invulnerable;
	protected UUID uuid = Mth.createInsecureUUID(this.random);
	protected String stringUUID = this.uuid.toString();
	private boolean hasGlowingTag;
	private final Set<String> tags = Sets.<String>newHashSet();
	private final double[] pistonDeltas = new double[]{0.0, 0.0, 0.0};
	private long pistonDeltasGameTime;
	private EntityDimensions dimensions;
	private float eyeHeight;
	public boolean isInPowderSnow;
	public boolean wasInPowderSnow;
	public Optional<BlockPos> mainSupportingBlockPos = Optional.empty();
	private boolean onGroundNoBlocks = false;
	private float crystalSoundIntensity;
	private int lastCrystalSoundPlayTick;
	private boolean hasVisualFire;
	@Nullable
	private BlockState inBlockState = null;
	private final Map<BlockPos, BlockState> blocksInside = new HashMap();

	public Entity(EntityType<?> entityType, Level level) {
		this.type = entityType;
		this.level = level;
		this.dimensions = entityType.getDimensions();
		this.position = Vec3.ZERO;
		this.blockPosition = BlockPos.ZERO;
		this.chunkPosition = ChunkPos.ZERO;
		SynchedEntityData.Builder builder = new SynchedEntityData.Builder(this);
		builder.define(DATA_SHARED_FLAGS_ID, (byte)0);
		builder.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
		builder.define(DATA_CUSTOM_NAME_VISIBLE, false);
		builder.define(DATA_CUSTOM_NAME, Optional.empty());
		builder.define(DATA_SILENT, false);
		builder.define(DATA_NO_GRAVITY, false);
		builder.define(DATA_POSE, Pose.STANDING);
		builder.define(DATA_TICKS_FROZEN, 0);
		this.defineSynchedData(builder);
		this.entityData = builder.build();
		this.setPos(0.0, 0.0, 0.0);
		this.eyeHeight = this.dimensions.eyeHeight();
	}

	public boolean isColliding(BlockPos blockPos, BlockState blockState) {
		VoxelShape voxelShape = blockState.getCollisionShape(this.level(), blockPos, CollisionContext.of(this));
		VoxelShape voxelShape2 = voxelShape.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
		return Shapes.joinIsNotEmpty(voxelShape2, Shapes.create(this.getBoundingBox()), BooleanOp.AND);
	}

	public int getTeamColor() {
		Team team = this.getTeam();
		return team != null && team.getColor().getColor() != null ? team.getColor().getColor() : 16777215;
	}

	public boolean isSpectator() {
		return false;
	}

	public final void unRide() {
		if (this.isVehicle()) {
			this.ejectPassengers();
		}

		if (this.isPassenger()) {
			this.stopRiding();
		}
	}

	public void syncPacketPositionCodec(double d, double e, double f) {
		this.packetPositionCodec.setBase(new Vec3(d, e, f));
	}

	public VecDeltaCodec getPositionCodec() {
		return this.packetPositionCodec;
	}

	public EntityType<?> getType() {
		return this.type;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public void setId(int i) {
		this.id = i;
	}

	public Set<String> getTags() {
		return this.tags;
	}

	public boolean addTag(String string) {
		return this.tags.size() >= 1024 ? false : this.tags.add(string);
	}

	public boolean removeTag(String string) {
		return this.tags.remove(string);
	}

	public void kill() {
		this.remove(Entity.RemovalReason.KILLED);
		this.gameEvent(GameEvent.ENTITY_DIE);
	}

	public final void discard() {
		this.remove(Entity.RemovalReason.DISCARDED);
	}

	protected abstract void defineSynchedData(SynchedEntityData.Builder builder);

	public SynchedEntityData getEntityData() {
		return this.entityData;
	}

	public boolean equals(Object object) {
		return object instanceof Entity ? ((Entity)object).id == this.id : false;
	}

	public int hashCode() {
		return this.id;
	}

	public void remove(Entity.RemovalReason removalReason) {
		this.setRemoved(removalReason);
	}

	public void onClientRemoval() {
	}

	public void setPose(Pose pose) {
		this.entityData.set(DATA_POSE, pose);
	}

	public Pose getPose() {
		return this.entityData.get(DATA_POSE);
	}

	public boolean hasPose(Pose pose) {
		return this.getPose() == pose;
	}

	public boolean closerThan(Entity entity, double d) {
		return this.position().closerThan(entity.position(), d);
	}

	public boolean closerThan(Entity entity, double d, double e) {
		double f = entity.getX() - this.getX();
		double g = entity.getY() - this.getY();
		double h = entity.getZ() - this.getZ();
		return Mth.lengthSquared(f, h) < Mth.square(d) && Mth.square(g) < Mth.square(e);
	}

	protected void setRot(float f, float g) {
		this.setYRot(f % 360.0F);
		this.setXRot(g % 360.0F);
	}

	public final void setPos(Vec3 vec3) {
		this.setPos(vec3.x(), vec3.y(), vec3.z());
	}

	public void setPos(double d, double e, double f) {
		this.setPosRaw(d, e, f);
		this.setBoundingBox(this.makeBoundingBox());
	}

	protected AABB makeBoundingBox() {
		return this.dimensions.makeBoundingBox(this.position);
	}

	protected void reapplyPosition() {
		this.setPos(this.position.x, this.position.y, this.position.z);
	}

	public void turn(double d, double e) {
		float f = (float)e * 0.15F;
		float g = (float)d * 0.15F;
		this.setXRot(this.getXRot() + f);
		this.setYRot(this.getYRot() + g);
		this.setXRot(Mth.clamp(this.getXRot(), -90.0F, 90.0F));
		this.xRotO += f;
		this.yRotO += g;
		this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
		if (this.vehicle != null) {
			this.vehicle.onPassengerTurned(this);
		}
	}

	public void tick() {
		this.baseTick();
	}

	public void baseTick() {
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("entityBaseTick");
		this.inBlockState = null;
		if (this.isPassenger() && this.getVehicle().isRemoved()) {
			this.stopRiding();
		}

		if (this.boardingCooldown > 0) {
			this.boardingCooldown--;
		}

		this.xRotO = this.getXRot();
		this.yRotO = this.getYRot();
		this.handlePortal();
		if (this.canSpawnSprintParticle()) {
			this.spawnSprintParticle();
		}

		this.wasInPowderSnow = this.isInPowderSnow;
		this.isInPowderSnow = false;
		this.updateInWaterStateAndDoFluidPushing();
		this.updateFluidOnEyes();
		this.updateSwimming();
		if (this.level().isClientSide) {
			this.clearFire();
		} else if (this.remainingFireTicks > 0) {
			if (this.fireImmune()) {
				this.setRemainingFireTicks(this.remainingFireTicks - 4);
				if (this.remainingFireTicks < 0) {
					this.clearFire();
				}
			} else {
				if (this.remainingFireTicks % 20 == 0 && !this.isInLava()) {
					this.hurt(this.damageSources().onFire(), 1.0F);
				}

				this.setRemainingFireTicks(this.remainingFireTicks - 1);
			}

			if (this.getTicksFrozen() > 0) {
				this.setTicksFrozen(0);
				this.level().levelEvent(null, 1009, this.blockPosition, 1);
			}
		}

		if (this.isInLava()) {
			this.lavaHurt();
			this.fallDistance *= 0.5F;
		}

		this.checkBelowWorld();
		if (!this.level().isClientSide) {
			this.setSharedFlagOnFire(this.remainingFireTicks > 0);
		}

		this.firstTick = false;
		if (!this.level().isClientSide && this instanceof Leashable) {
			Leashable.tickLeash((Entity)((Leashable)this));
		}

		profilerFiller.pop();
	}

	public void setSharedFlagOnFire(boolean bl) {
		this.setSharedFlag(0, bl || this.hasVisualFire);
	}

	public void checkBelowWorld() {
		if (this.getY() < (double)(this.level().getMinY() - 64)) {
			this.onBelowWorld();
		}
	}

	public void setPortalCooldown() {
		this.portalCooldown = this.getDimensionChangingDelay();
	}

	public void setPortalCooldown(int i) {
		this.portalCooldown = i;
	}

	public int getPortalCooldown() {
		return this.portalCooldown;
	}

	public boolean isOnPortalCooldown() {
		return this.portalCooldown > 0;
	}

	protected void processPortalCooldown() {
		if (this.isOnPortalCooldown()) {
			this.portalCooldown--;
		}
	}

	public void lavaHurt() {
		if (!this.fireImmune()) {
			this.igniteForSeconds(15.0F);
			if (this.hurt(this.damageSources().lava(), 4.0F)) {
				this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
			}
		}
	}

	public final void igniteForSeconds(float f) {
		this.igniteForTicks(Mth.floor(f * 20.0F));
	}

	public void igniteForTicks(int i) {
		if (this.remainingFireTicks < i) {
			this.setRemainingFireTicks(i);
		}
	}

	public void setRemainingFireTicks(int i) {
		this.remainingFireTicks = i;
	}

	public int getRemainingFireTicks() {
		return this.remainingFireTicks;
	}

	public void clearFire() {
		this.setRemainingFireTicks(0);
	}

	protected void onBelowWorld() {
		this.discard();
	}

	public boolean isFree(double d, double e, double f) {
		return this.isFree(this.getBoundingBox().move(d, e, f));
	}

	private boolean isFree(AABB aABB) {
		return this.level().noCollision(this, aABB) && !this.level().containsAnyLiquid(aABB);
	}

	public void setOnGround(boolean bl) {
		this.onGround = bl;
		this.checkSupportingBlock(bl, null);
	}

	public void setOnGroundWithMovement(boolean bl, boolean bl2, Vec3 vec3) {
		this.onGround = bl;
		this.horizontalCollision = bl2;
		this.checkSupportingBlock(bl, vec3);
	}

	public boolean isSupportedBy(BlockPos blockPos) {
		return this.mainSupportingBlockPos.isPresent() && ((BlockPos)this.mainSupportingBlockPos.get()).equals(blockPos);
	}

	protected void checkSupportingBlock(boolean bl, @Nullable Vec3 vec3) {
		if (bl) {
			AABB aABB = this.getBoundingBox();
			AABB aABB2 = new AABB(aABB.minX, aABB.minY - 1.0E-6, aABB.minZ, aABB.maxX, aABB.minY, aABB.maxZ);
			Optional<BlockPos> optional = this.level.findSupportingBlock(this, aABB2);
			if (optional.isPresent() || this.onGroundNoBlocks) {
				this.mainSupportingBlockPos = optional;
			} else if (vec3 != null) {
				AABB aABB3 = aABB2.move(-vec3.x, 0.0, -vec3.z);
				optional = this.level.findSupportingBlock(this, aABB3);
				this.mainSupportingBlockPos = optional;
			}

			this.onGroundNoBlocks = optional.isEmpty();
		} else {
			this.onGroundNoBlocks = false;
			if (this.mainSupportingBlockPos.isPresent()) {
				this.mainSupportingBlockPos = Optional.empty();
			}
		}
	}

	public boolean onGround() {
		return this.onGround;
	}

	public void move(MoverType moverType, Vec3 vec3) {
		if (this.noPhysics) {
			this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
		} else {
			if (moverType == MoverType.PISTON) {
				vec3 = this.limitPistonMovement(vec3);
				if (vec3.equals(Vec3.ZERO)) {
					return;
				}
			}

			ProfilerFiller profilerFiller = Profiler.get();
			profilerFiller.push("move");
			if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
				vec3 = vec3.multiply(this.stuckSpeedMultiplier);
				this.stuckSpeedMultiplier = Vec3.ZERO;
				this.setDeltaMovement(Vec3.ZERO);
			}

			vec3 = this.maybeBackOffFromEdge(vec3, moverType);
			Vec3 vec32 = this.collide(vec3);
			double d = vec32.lengthSqr();
			if (d > 1.0E-7 || vec3.lengthSqr() - d < 1.0E-7) {
				if (this.fallDistance != 0.0F && d >= 1.0) {
					BlockHitResult blockHitResult = this.level()
						.clip(new ClipContext(this.position(), this.position().add(vec32), ClipContext.Block.FALLDAMAGE_RESETTING, ClipContext.Fluid.WATER, this));
					if (blockHitResult.getType() != HitResult.Type.MISS) {
						this.resetFallDistance();
					}
				}

				this.setPos(this.getX() + vec32.x, this.getY() + vec32.y, this.getZ() + vec32.z);
			}

			profilerFiller.pop();
			profilerFiller.push("rest");
			boolean bl = !Mth.equal(vec3.x, vec32.x);
			boolean bl2 = !Mth.equal(vec3.z, vec32.z);
			this.horizontalCollision = bl || bl2;
			if (Math.abs(vec3.y) > 0.0 || this.isControlledByOrIsLocalPlayer()) {
				this.verticalCollision = vec3.y != vec32.y;
				this.verticalCollisionBelow = this.verticalCollision && vec3.y < 0.0;
				this.setOnGroundWithMovement(this.verticalCollisionBelow, this.horizontalCollision, vec32);
			}

			if (this.horizontalCollision) {
				this.minorHorizontalCollision = this.isHorizontalCollisionMinor(vec32);
			} else {
				this.minorHorizontalCollision = false;
			}

			BlockPos blockPos = this.getOnPosLegacy();
			BlockState blockState = this.level().getBlockState(blockPos);
			if (!this.level().isClientSide() || this.isControlledByLocalInstance()) {
				this.checkFallDamage(vec32.y, this.onGround(), blockState, blockPos);
			}

			if (this.isRemoved()) {
				profilerFiller.pop();
			} else {
				if (this.horizontalCollision) {
					Vec3 vec33 = this.getDeltaMovement();
					this.setDeltaMovement(bl ? 0.0 : vec33.x, vec33.y, bl2 ? 0.0 : vec33.z);
				}

				if (this.isControlledByLocalInstance()) {
					Block block = blockState.getBlock();
					if (vec3.y != vec32.y) {
						block.updateEntityMovementAfterFallOn(this.level(), this);
					}
				}

				if (!this.level().isClientSide() || this.isControlledByLocalInstance()) {
					Entity.MovementEmission movementEmission = this.getMovementEmission();
					if (movementEmission.emitsAnything() && !this.isPassenger()) {
						this.applyMovementEmissionAndPlaySound(movementEmission, vec32, blockPos, blockState);
					}
				}

				float f = this.getBlockSpeedFactor();
				this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 1.0, (double)f));
				profilerFiller.pop();
			}
		}
	}

	private void applyMovementEmissionAndPlaySound(Entity.MovementEmission movementEmission, Vec3 vec3, BlockPos blockPos, BlockState blockState) {
		float f = 0.6F;
		float g = (float)(vec3.length() * 0.6F);
		float h = (float)(vec3.horizontalDistance() * 0.6F);
		BlockPos blockPos2 = this.getOnPos();
		BlockState blockState2 = this.level().getBlockState(blockPos2);
		boolean bl = this.isStateClimbable(blockState2);
		this.moveDist += bl ? g : h;
		this.flyDist += g;
		if (this.moveDist > this.nextStep && !blockState2.isAir()) {
			boolean bl2 = blockPos2.equals(blockPos);
			boolean bl3 = this.vibrationAndSoundEffectsFromBlock(blockPos, blockState, movementEmission.emitsSounds(), bl2, vec3);
			if (!bl2) {
				bl3 |= this.vibrationAndSoundEffectsFromBlock(blockPos2, blockState2, false, movementEmission.emitsEvents(), vec3);
			}

			if (bl3) {
				this.nextStep = this.nextStep();
			} else if (this.isInWater()) {
				this.nextStep = this.nextStep();
				if (movementEmission.emitsSounds()) {
					this.waterSwimSound();
				}

				if (movementEmission.emitsEvents()) {
					this.gameEvent(GameEvent.SWIM);
				}
			}
		} else if (blockState2.isAir()) {
			this.processFlappingMovement();
		}
	}

	public void applyEffectsFromBlocks() {
		this.applyEffectsFromBlocks(this.oldPosition(), this.position);
	}

	public void applyEffectsFromBlocks(Vec3 vec3, Vec3 vec32) {
		if (this.isAffectedByBlocks()) {
			boolean bl = this.isOnFire();
			if (this.onGround()) {
				BlockPos blockPos = this.getOnPosLegacy();
				BlockState blockState = this.level().getBlockState(blockPos);
				blockState.getBlock().stepOn(this.level(), blockPos, blockState, this);
			}

			this.collectBlockCollidedWith(this.blocksInside, vec3, vec32);
			boolean bl2 = false;

			for (Entry<BlockPos, BlockState> entry : this.blocksInside.entrySet()) {
				((BlockState)entry.getValue()).entityInside(this.level(), (BlockPos)entry.getKey(), this);
				this.onInsideBlock((BlockState)entry.getValue());
				if (((BlockState)entry.getValue()).is(BlockTags.FIRE) || ((BlockState)entry.getValue()).is(Blocks.LAVA)) {
					bl2 = true;
				}
			}

			this.blocksInside.clear();
			if (!bl2) {
				if (this.remainingFireTicks <= 0) {
					this.setRemainingFireTicks(-this.getFireImmuneTicks());
				}

				if (bl && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
					this.playEntityOnFireExtinguishedSound();
				}
			}

			if (this.isOnFire() && (this.isInPowderSnow || this.isInWaterRainOrBubble())) {
				this.setRemainingFireTicks(-this.getFireImmuneTicks());
			}
		}
	}

	protected boolean isAffectedByBlocks() {
		return !this.isRemoved() && !this.noPhysics;
	}

	private boolean isStateClimbable(BlockState blockState) {
		return blockState.is(BlockTags.CLIMBABLE) || blockState.is(Blocks.POWDER_SNOW);
	}

	private boolean vibrationAndSoundEffectsFromBlock(BlockPos blockPos, BlockState blockState, boolean bl, boolean bl2, Vec3 vec3) {
		if (blockState.isAir()) {
			return false;
		} else {
			boolean bl3 = this.isStateClimbable(blockState);
			if ((this.onGround() || bl3 || this.isCrouching() && vec3.y == 0.0 || this.isOnRails()) && !this.isSwimming()) {
				if (bl) {
					this.walkingStepSound(blockPos, blockState);
				}

				if (bl2) {
					this.level().gameEvent(GameEvent.STEP, this.position(), GameEvent.Context.of(this, blockState));
				}

				return true;
			} else {
				return false;
			}
		}
	}

	protected boolean isHorizontalCollisionMinor(Vec3 vec3) {
		return false;
	}

	protected void playEntityOnFireExtinguishedSound() {
		this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
	}

	public void extinguishFire() {
		if (!this.level().isClientSide && this.isOnFire()) {
			this.playEntityOnFireExtinguishedSound();
		}

		this.clearFire();
	}

	protected void processFlappingMovement() {
		if (this.isFlapping()) {
			this.onFlap();
			if (this.getMovementEmission().emitsEvents()) {
				this.gameEvent(GameEvent.FLAP);
			}
		}
	}

	@Deprecated
	public BlockPos getOnPosLegacy() {
		return this.getOnPos(0.2F);
	}

	public BlockPos getBlockPosBelowThatAffectsMyMovement() {
		return this.getOnPos(0.500001F);
	}

	public BlockPos getOnPos() {
		return this.getOnPos(1.0E-5F);
	}

	protected BlockPos getOnPos(float f) {
		if (this.mainSupportingBlockPos.isPresent()) {
			BlockPos blockPos = (BlockPos)this.mainSupportingBlockPos.get();
			if (!(f > 1.0E-5F)) {
				return blockPos;
			} else {
				BlockState blockState = this.level().getBlockState(blockPos);
				return (!((double)f <= 0.5) || !blockState.is(BlockTags.FENCES)) && !blockState.is(BlockTags.WALLS) && !(blockState.getBlock() instanceof FenceGateBlock)
					? blockPos.atY(Mth.floor(this.position.y - (double)f))
					: blockPos;
			}
		} else {
			int i = Mth.floor(this.position.x);
			int j = Mth.floor(this.position.y - (double)f);
			int k = Mth.floor(this.position.z);
			return new BlockPos(i, j, k);
		}
	}

	protected float getBlockJumpFactor() {
		float f = this.level().getBlockState(this.blockPosition()).getBlock().getJumpFactor();
		float g = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
		return (double)f == 1.0 ? g : f;
	}

	protected float getBlockSpeedFactor() {
		BlockState blockState = this.level().getBlockState(this.blockPosition());
		float f = blockState.getBlock().getSpeedFactor();
		if (!blockState.is(Blocks.WATER) && !blockState.is(Blocks.BUBBLE_COLUMN)) {
			return (double)f == 1.0 ? this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : f;
		} else {
			return f;
		}
	}

	protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType moverType) {
		return vec3;
	}

	protected Vec3 limitPistonMovement(Vec3 vec3) {
		if (vec3.lengthSqr() <= 1.0E-7) {
			return vec3;
		} else {
			long l = this.level().getGameTime();
			if (l != this.pistonDeltasGameTime) {
				Arrays.fill(this.pistonDeltas, 0.0);
				this.pistonDeltasGameTime = l;
			}

			if (vec3.x != 0.0) {
				double d = this.applyPistonMovementRestriction(Direction.Axis.X, vec3.x);
				return Math.abs(d) <= 1.0E-5F ? Vec3.ZERO : new Vec3(d, 0.0, 0.0);
			} else if (vec3.y != 0.0) {
				double d = this.applyPistonMovementRestriction(Direction.Axis.Y, vec3.y);
				return Math.abs(d) <= 1.0E-5F ? Vec3.ZERO : new Vec3(0.0, d, 0.0);
			} else if (vec3.z != 0.0) {
				double d = this.applyPistonMovementRestriction(Direction.Axis.Z, vec3.z);
				return Math.abs(d) <= 1.0E-5F ? Vec3.ZERO : new Vec3(0.0, 0.0, d);
			} else {
				return Vec3.ZERO;
			}
		}
	}

	private double applyPistonMovementRestriction(Direction.Axis axis, double d) {
		int i = axis.ordinal();
		double e = Mth.clamp(d + this.pistonDeltas[i], -0.51, 0.51);
		d = e - this.pistonDeltas[i];
		this.pistonDeltas[i] = e;
		return d;
	}

	private Vec3 collide(Vec3 vec3) {
		AABB aABB = this.getBoundingBox();
		List<VoxelShape> list = this.level().getEntityCollisions(this, aABB.expandTowards(vec3));
		Vec3 vec32 = vec3.lengthSqr() == 0.0 ? vec3 : collideBoundingBox(this, vec3, aABB, this.level(), list);
		boolean bl = vec3.x != vec32.x;
		boolean bl2 = vec3.y != vec32.y;
		boolean bl3 = vec3.z != vec32.z;
		boolean bl4 = bl2 && vec3.y < 0.0;
		if (this.maxUpStep() > 0.0F && (bl4 || this.onGround()) && (bl || bl3)) {
			AABB aABB2 = bl4 ? aABB.move(0.0, vec32.y, 0.0) : aABB;
			AABB aABB3 = aABB2.expandTowards(vec3.x, (double)this.maxUpStep(), vec3.z);
			if (!bl4) {
				aABB3 = aABB3.expandTowards(0.0, -1.0E-5F, 0.0);
			}

			List<VoxelShape> list2 = collectColliders(this, this.level, list, aABB3);
			float f = (float)vec32.y;
			float[] fs = collectCandidateStepUpHeights(aABB2, list2, this.maxUpStep(), f);

			for (float g : fs) {
				Vec3 vec33 = collideWithShapes(new Vec3(vec3.x, (double)g, vec3.z), aABB2, list2);
				if (vec33.horizontalDistanceSqr() > vec32.horizontalDistanceSqr()) {
					double d = aABB.minY - aABB2.minY;
					return vec33.add(0.0, -d, 0.0);
				}
			}
		}

		return vec32;
	}

	private static float[] collectCandidateStepUpHeights(AABB aABB, List<VoxelShape> list, float f, float g) {
		FloatSet floatSet = new FloatArraySet(4);

		for (VoxelShape voxelShape : list) {
			for (double d : voxelShape.getCoords(Direction.Axis.Y)) {
				float h = (float)(d - aABB.minY);
				if (!(h < 0.0F) && h != g) {
					if (h > f) {
						break;
					}

					floatSet.add(h);
				}
			}
		}

		float[] fs = floatSet.toFloatArray();
		FloatArrays.unstableSort(fs);
		return fs;
	}

	public static Vec3 collideBoundingBox(@Nullable Entity entity, Vec3 vec3, AABB aABB, Level level, List<VoxelShape> list) {
		List<VoxelShape> list2 = collectColliders(entity, level, list, aABB.expandTowards(vec3));
		return collideWithShapes(vec3, aABB, list2);
	}

	private static List<VoxelShape> collectColliders(@Nullable Entity entity, Level level, List<VoxelShape> list, AABB aABB) {
		Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size() + 1);
		if (!list.isEmpty()) {
			builder.addAll(list);
		}

		WorldBorder worldBorder = level.getWorldBorder();
		boolean bl = entity != null && worldBorder.isInsideCloseToBorder(entity, aABB);
		if (bl) {
			builder.add(worldBorder.getCollisionShape());
		}

		builder.addAll(level.getBlockCollisions(entity, aABB));
		return builder.build();
	}

	private static Vec3 collideWithShapes(Vec3 vec3, AABB aABB, List<VoxelShape> list) {
		if (list.isEmpty()) {
			return vec3;
		} else {
			double d = vec3.x;
			double e = vec3.y;
			double f = vec3.z;
			if (e != 0.0) {
				e = Shapes.collide(Direction.Axis.Y, aABB, list, e);
				if (e != 0.0) {
					aABB = aABB.move(0.0, e, 0.0);
				}
			}

			boolean bl = Math.abs(d) < Math.abs(f);
			if (bl && f != 0.0) {
				f = Shapes.collide(Direction.Axis.Z, aABB, list, f);
				if (f != 0.0) {
					aABB = aABB.move(0.0, 0.0, f);
				}
			}

			if (d != 0.0) {
				d = Shapes.collide(Direction.Axis.X, aABB, list, d);
				if (!bl && d != 0.0) {
					aABB = aABB.move(d, 0.0, 0.0);
				}
			}

			if (!bl && f != 0.0) {
				f = Shapes.collide(Direction.Axis.Z, aABB, list, f);
			}

			return new Vec3(d, e, f);
		}
	}

	protected float nextStep() {
		return (float)((int)this.moveDist + 1);
	}

	protected SoundEvent getSwimSound() {
		return SoundEvents.GENERIC_SWIM;
	}

	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.GENERIC_SPLASH;
	}

	protected SoundEvent getSwimHighSpeedSplashSound() {
		return SoundEvents.GENERIC_SPLASH;
	}

	public void recordMovementThroughBlocks(Vec3 vec3, Vec3 vec32) {
		this.collectBlockCollidedWith(this.blocksInside, vec3, vec32);
	}

	private void collectBlockCollidedWith(Map<BlockPos, BlockState> map, Vec3 vec3, Vec3 vec32) {
		AABB aABB = this.getBoundingBox().deflate(1.0E-5F);

		for (BlockPos blockPos : BlockGetter.boxTraverseBlocks(vec3, vec32, aABB)) {
			if (!this.isAlive()) {
				return;
			}

			BlockState blockState = this.level().getBlockState(blockPos);
			if (!blockState.isAir() && !map.containsKey(blockPos)) {
				try {
					VoxelShape voxelShape = blockState.getEntityInsideCollisionShape(this.level(), blockPos);
					if (voxelShape == Shapes.block() || this.collidedWithShapeMovingFrom(vec3, vec32, blockPos, voxelShape)) {
						map.put(blockPos.immutable(), blockState);
					}
				} catch (Throwable var12) {
					CrashReport crashReport = CrashReport.forThrowable(var12, "Colliding entity with block");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Block being collided with");
					CrashReportCategory.populateBlockDetails(crashReportCategory, this.level(), blockPos, blockState);
					CrashReportCategory crashReportCategory2 = crashReport.addCategory("Entity being checked for collision");
					this.fillCrashReportCategory(crashReportCategory2);
					throw new ReportedException(crashReport);
				}
			}
		}
	}

	private boolean collidedWithShapeMovingFrom(Vec3 vec3, Vec3 vec32, BlockPos blockPos, VoxelShape voxelShape) {
		AABB aABB = this.getBoundingBox().move(this.getBoundingBox().getCenter().scale(-1.0)).move(vec32);
		Vec3 vec33 = vec3.subtract(aABB.getBottomCenter());
		return this.getBoundingBox().collidedAlongVector(vec33, voxelShape.move(new Vec3(blockPos)).toAabbs());
	}

	protected void onInsideBlock(BlockState blockState) {
	}

	public BlockPos adjustSpawnLocation(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos blockPos2 = serverLevel.getSharedSpawnPos();
		Vec3 vec3 = blockPos2.getCenter();
		int i = serverLevel.getChunkAt(blockPos2).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos2.getX(), blockPos2.getZ()) + 1;
		return BlockPos.containing(vec3.x, (double)i, vec3.z);
	}

	public void gameEvent(Holder<GameEvent> holder, @Nullable Entity entity) {
		this.level().gameEvent(entity, holder, this.position);
	}

	public void gameEvent(Holder<GameEvent> holder) {
		this.gameEvent(holder, this);
	}

	private void walkingStepSound(BlockPos blockPos, BlockState blockState) {
		this.playStepSound(blockPos, blockState);
		if (this.shouldPlayAmethystStepSound(blockState)) {
			this.playAmethystStepSound();
		}
	}

	protected void waterSwimSound() {
		Entity entity = (Entity)Objects.requireNonNullElse(this.getControllingPassenger(), this);
		float f = entity == this ? 0.35F : 0.4F;
		Vec3 vec3 = entity.getDeltaMovement();
		float g = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * 0.2F + vec3.y * vec3.y + vec3.z * vec3.z * 0.2F) * f);
		this.playSwimSound(g);
	}

	protected BlockPos getPrimaryStepSoundBlockPos(BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState = this.level().getBlockState(blockPos2);
		return !blockState.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) && !blockState.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS) ? blockPos : blockPos2;
	}

	protected void playCombinationStepSounds(BlockState blockState, BlockState blockState2) {
		SoundType soundType = blockState.getSoundType();
		this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());
		this.playMuffledStepSound(blockState2);
	}

	protected void playMuffledStepSound(BlockState blockState) {
		SoundType soundType = blockState.getSoundType();
		this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.05F, soundType.getPitch() * 0.8F);
	}

	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		SoundType soundType = blockState.getSoundType();
		this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());
	}

	private boolean shouldPlayAmethystStepSound(BlockState blockState) {
		return blockState.is(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.tickCount >= this.lastCrystalSoundPlayTick + 20;
	}

	private void playAmethystStepSound() {
		this.crystalSoundIntensity = this.crystalSoundIntensity * (float)Math.pow(0.997, (double)(this.tickCount - this.lastCrystalSoundPlayTick));
		this.crystalSoundIntensity = Math.min(1.0F, this.crystalSoundIntensity + 0.07F);
		float f = 0.5F + this.crystalSoundIntensity * this.random.nextFloat() * 1.2F;
		float g = 0.1F + this.crystalSoundIntensity * 1.2F;
		this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, g, f);
		this.lastCrystalSoundPlayTick = this.tickCount;
	}

	protected void playSwimSound(float f) {
		this.playSound(this.getSwimSound(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
	}

	protected void onFlap() {
	}

	protected boolean isFlapping() {
		return false;
	}

	public void playSound(SoundEvent soundEvent, float f, float g) {
		if (!this.isSilent()) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
		}
	}

	public void playSound(SoundEvent soundEvent) {
		if (!this.isSilent()) {
			this.playSound(soundEvent, 1.0F, 1.0F);
		}
	}

	public boolean isSilent() {
		return this.entityData.get(DATA_SILENT);
	}

	public void setSilent(boolean bl) {
		this.entityData.set(DATA_SILENT, bl);
	}

	public boolean isNoGravity() {
		return this.entityData.get(DATA_NO_GRAVITY);
	}

	public void setNoGravity(boolean bl) {
		this.entityData.set(DATA_NO_GRAVITY, bl);
	}

	protected double getDefaultGravity() {
		return 0.0;
	}

	public final double getGravity() {
		return this.isNoGravity() ? 0.0 : this.getDefaultGravity();
	}

	protected void applyGravity() {
		double d = this.getGravity();
		if (d != 0.0) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d, 0.0));
		}
	}

	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.ALL;
	}

	public boolean dampensVibrations() {
		return false;
	}

	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		if (bl) {
			if (this.fallDistance > 0.0F) {
				blockState.getBlock().fallOn(this.level(), blockState, blockPos, this, this.fallDistance);
				this.level()
					.gameEvent(
						GameEvent.HIT_GROUND,
						this.position,
						GameEvent.Context.of(this, (BlockState)this.mainSupportingBlockPos.map(blockPosx -> this.level().getBlockState(blockPosx)).orElse(blockState))
					);
			}

			this.resetFallDistance();
		} else if (d < 0.0) {
			this.fallDistance -= (float)d;
		}
	}

	public boolean fireImmune() {
		return this.getType().fireImmune();
	}

	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		if (this.type.is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
			return false;
		} else {
			if (this.isVehicle()) {
				for (Entity entity : this.getPassengers()) {
					entity.causeFallDamage(f, g, damageSource);
				}
			}

			return false;
		}
	}

	public boolean isInWater() {
		return this.wasTouchingWater;
	}

	private boolean isInRain() {
		BlockPos blockPos = this.blockPosition();
		return this.level().isRainingAt(blockPos)
			|| this.level().isRainingAt(BlockPos.containing((double)blockPos.getX(), this.getBoundingBox().maxY, (double)blockPos.getZ()));
	}

	private boolean isInBubbleColumn() {
		return this.getInBlockState().is(Blocks.BUBBLE_COLUMN);
	}

	public boolean isInWaterOrRain() {
		return this.isInWater() || this.isInRain();
	}

	public boolean isInWaterRainOrBubble() {
		return this.isInWater() || this.isInRain() || this.isInBubbleColumn();
	}

	public boolean isInWaterOrBubble() {
		return this.isInWater() || this.isInBubbleColumn();
	}

	public boolean isInLiquid() {
		return this.isInWaterOrBubble() || this.isInLava();
	}

	public boolean isUnderWater() {
		return this.wasEyeInWater && this.isInWater();
	}

	public void updateSwimming() {
		if (this.isSwimming()) {
			this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
		} else {
			this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger() && this.level().getFluidState(this.blockPosition).is(FluidTags.WATER));
		}
	}

	protected boolean updateInWaterStateAndDoFluidPushing() {
		this.fluidHeight.clear();
		this.updateInWaterStateAndDoWaterCurrentPushing();
		double d = this.level().dimensionType().ultraWarm() ? 0.007 : 0.0023333333333333335;
		boolean bl = this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, d);
		return this.isInWater() || bl;
	}

	void updateInWaterStateAndDoWaterCurrentPushing() {
		if (this.getVehicle() instanceof Boat boat && !boat.isUnderWater()) {
			this.wasTouchingWater = false;
			return;
		}

		if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014)) {
			if (!this.wasTouchingWater && !this.firstTick) {
				this.doWaterSplashEffect();
			}

			this.resetFallDistance();
			this.wasTouchingWater = true;
			this.clearFire();
		} else {
			this.wasTouchingWater = false;
		}
	}

	private void updateFluidOnEyes() {
		this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
		this.fluidOnEyes.clear();
		double d = this.getEyeY();
		if (this.getVehicle() instanceof Boat boat && !boat.isUnderWater() && boat.getBoundingBox().maxY >= d && boat.getBoundingBox().minY <= d) {
			return;
		}

		BlockPos blockPos = BlockPos.containing(this.getX(), d, this.getZ());
		FluidState fluidState = this.level().getFluidState(blockPos);
		double e = (double)((float)blockPos.getY() + fluidState.getHeight(this.level(), blockPos));
		if (e > d) {
			fluidState.getTags().forEach(this.fluidOnEyes::add);
		}
	}

	protected void doWaterSplashEffect() {
		Entity entity = (Entity)Objects.requireNonNullElse(this.getControllingPassenger(), this);
		float f = entity == this ? 0.2F : 0.9F;
		Vec3 vec3 = entity.getDeltaMovement();
		float g = Math.min(1.0F, (float)Math.sqrt(vec3.x * vec3.x * 0.2F + vec3.y * vec3.y + vec3.z * vec3.z * 0.2F) * f);
		if (g < 0.25F) {
			this.playSound(this.getSwimSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
		} else {
			this.playSound(this.getSwimHighSpeedSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
		}

		float h = (float)Mth.floor(this.getY());

		for (int i = 0; (float)i < 1.0F + this.dimensions.width() * 20.0F; i++) {
			double d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
			double e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
			this.level()
				.addParticle(ParticleTypes.BUBBLE, this.getX() + d, (double)(h + 1.0F), this.getZ() + e, vec3.x, vec3.y - this.random.nextDouble() * 0.2F, vec3.z);
		}

		for (int i = 0; (float)i < 1.0F + this.dimensions.width() * 20.0F; i++) {
			double d = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
			double e = (this.random.nextDouble() * 2.0 - 1.0) * (double)this.dimensions.width();
			this.level().addParticle(ParticleTypes.SPLASH, this.getX() + d, (double)(h + 1.0F), this.getZ() + e, vec3.x, vec3.y, vec3.z);
		}

		this.gameEvent(GameEvent.SPLASH);
	}

	@Deprecated
	protected BlockState getBlockStateOnLegacy() {
		return this.level().getBlockState(this.getOnPosLegacy());
	}

	public BlockState getBlockStateOn() {
		return this.level().getBlockState(this.getOnPos());
	}

	public boolean canSpawnSprintParticle() {
		return this.isSprinting() && !this.isInWater() && !this.isSpectator() && !this.isCrouching() && !this.isInLava() && this.isAlive();
	}

	protected void spawnSprintParticle() {
		BlockPos blockPos = this.getOnPosLegacy();
		BlockState blockState = this.level().getBlockState(blockPos);
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			Vec3 vec3 = this.getDeltaMovement();
			BlockPos blockPos2 = this.blockPosition();
			double d = this.getX() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
			double e = this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.dimensions.width();
			if (blockPos2.getX() != blockPos.getX()) {
				d = Mth.clamp(d, (double)blockPos.getX(), (double)blockPos.getX() + 1.0);
			}

			if (blockPos2.getZ() != blockPos.getZ()) {
				e = Mth.clamp(e, (double)blockPos.getZ(), (double)blockPos.getZ() + 1.0);
			}

			this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d, this.getY() + 0.1, e, vec3.x * -4.0, 1.5, vec3.z * -4.0);
		}
	}

	public boolean isEyeInFluid(TagKey<Fluid> tagKey) {
		return this.fluidOnEyes.contains(tagKey);
	}

	public boolean isInLava() {
		return !this.firstTick && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
	}

	public void moveRelative(float f, Vec3 vec3) {
		Vec3 vec32 = getInputVector(vec3, f, this.getYRot());
		this.setDeltaMovement(this.getDeltaMovement().add(vec32));
	}

	protected static Vec3 getInputVector(Vec3 vec3, float f, float g) {
		double d = vec3.lengthSqr();
		if (d < 1.0E-7) {
			return Vec3.ZERO;
		} else {
			Vec3 vec32 = (d > 1.0 ? vec3.normalize() : vec3).scale((double)f);
			float h = Mth.sin(g * (float) (Math.PI / 180.0));
			float i = Mth.cos(g * (float) (Math.PI / 180.0));
			return new Vec3(vec32.x * (double)i - vec32.z * (double)h, vec32.y, vec32.z * (double)i + vec32.x * (double)h);
		}
	}

	@Deprecated
	public float getLightLevelDependentMagicValue() {
		return this.level().hasChunkAt(this.getBlockX(), this.getBlockZ())
			? this.level().getLightLevelDependentMagicValue(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()))
			: 0.0F;
	}

	public void absMoveTo(double d, double e, double f, float g, float h) {
		this.absMoveTo(d, e, f);
		this.absRotateTo(g, h);
	}

	public void absRotateTo(float f, float g) {
		this.setYRot(f % 360.0F);
		this.setXRot(Mth.clamp(g, -90.0F, 90.0F) % 360.0F);
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}

	public void absMoveTo(double d, double e, double f) {
		double g = Mth.clamp(d, -3.0E7, 3.0E7);
		double h = Mth.clamp(f, -3.0E7, 3.0E7);
		this.xo = g;
		this.yo = e;
		this.zo = h;
		this.setPos(g, e, h);
	}

	public void moveTo(Vec3 vec3) {
		this.moveTo(vec3.x, vec3.y, vec3.z);
	}

	public void moveTo(double d, double e, double f) {
		this.moveTo(d, e, f, this.getYRot(), this.getXRot());
	}

	public void moveTo(BlockPos blockPos, float f, float g) {
		this.moveTo(blockPos.getBottomCenter(), f, g);
	}

	public void moveTo(Vec3 vec3, float f, float g) {
		this.moveTo(vec3.x, vec3.y, vec3.z, f, g);
	}

	public void moveTo(double d, double e, double f, float g, float h) {
		this.setPosRaw(d, e, f);
		this.setYRot(g);
		this.setXRot(h);
		this.setOldPosAndRot();
		this.reapplyPosition();
	}

	public final void setOldPosAndRot() {
		this.setOldPos();
		this.setOldRot();
	}

	public final void setOldPosAndRot(Vec3 vec3, float f, float g) {
		this.setOldPos(vec3);
		this.setOldRot(f, g);
	}

	protected void setOldPos() {
		this.setOldPos(this.position);
	}

	protected void setOldRot() {
		this.setOldRot(this.getYRot(), this.getXRot());
	}

	private void setOldPos(Vec3 vec3) {
		this.xo = this.xOld = vec3.x;
		this.yo = this.yOld = vec3.y;
		this.zo = this.zOld = vec3.z;
	}

	private void setOldRot(float f, float g) {
		this.yRotO = f;
		this.xRotO = g;
	}

	public final Vec3 oldPosition() {
		return new Vec3(this.xOld, this.yOld, this.zOld);
	}

	public float distanceTo(Entity entity) {
		float f = (float)(this.getX() - entity.getX());
		float g = (float)(this.getY() - entity.getY());
		float h = (float)(this.getZ() - entity.getZ());
		return Mth.sqrt(f * f + g * g + h * h);
	}

	public double distanceToSqr(double d, double e, double f) {
		double g = this.getX() - d;
		double h = this.getY() - e;
		double i = this.getZ() - f;
		return g * g + h * h + i * i;
	}

	public double distanceToSqr(Entity entity) {
		return this.distanceToSqr(entity.position());
	}

	public double distanceToSqr(Vec3 vec3) {
		double d = this.getX() - vec3.x;
		double e = this.getY() - vec3.y;
		double f = this.getZ() - vec3.z;
		return d * d + e * e + f * f;
	}

	public void playerTouch(Player player) {
	}

	public void push(Entity entity) {
		if (!this.isPassengerOfSameVehicle(entity)) {
			if (!entity.noPhysics && !this.noPhysics) {
				double d = entity.getX() - this.getX();
				double e = entity.getZ() - this.getZ();
				double f = Mth.absMax(d, e);
				if (f >= 0.01F) {
					f = Math.sqrt(f);
					d /= f;
					e /= f;
					double g = 1.0 / f;
					if (g > 1.0) {
						g = 1.0;
					}

					d *= g;
					e *= g;
					d *= 0.05F;
					e *= 0.05F;
					if (!this.isVehicle() && this.isPushable()) {
						this.push(-d, 0.0, -e);
					}

					if (!entity.isVehicle() && entity.isPushable()) {
						entity.push(d, 0.0, e);
					}
				}
			}
		}
	}

	public void push(Vec3 vec3) {
		this.push(vec3.x, vec3.y, vec3.z);
	}

	public void push(double d, double e, double f) {
		this.setDeltaMovement(this.getDeltaMovement().add(d, e, f));
		this.hasImpulse = true;
	}

	protected void markHurt() {
		this.hurtMarked = true;
	}

	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.markHurt();
			return false;
		}
	}

	public final Vec3 getViewVector(float f) {
		return this.calculateViewVector(this.getViewXRot(f), this.getViewYRot(f));
	}

	public Direction getNearestViewDirection() {
		return Direction.getApproximateNearest(this.getViewVector(1.0F));
	}

	public float getViewXRot(float f) {
		return f == 1.0F ? this.getXRot() : Mth.lerp(f, this.xRotO, this.getXRot());
	}

	public float getViewYRot(float f) {
		return f == 1.0F ? this.getYRot() : Mth.lerp(f, this.yRotO, this.getYRot());
	}

	public float getXRot(float f) {
		return Mth.lerp(f, this.xRotO, this.getXRot());
	}

	public float getYRot(float f) {
		return Mth.rotLerp(f, this.yRotO, this.getYRot());
	}

	public final Vec3 calculateViewVector(float f, float g) {
		float h = f * (float) (Math.PI / 180.0);
		float i = -g * (float) (Math.PI / 180.0);
		float j = Mth.cos(i);
		float k = Mth.sin(i);
		float l = Mth.cos(h);
		float m = Mth.sin(h);
		return new Vec3((double)(k * l), (double)(-m), (double)(j * l));
	}

	public final Vec3 getUpVector(float f) {
		return this.calculateUpVector(this.getViewXRot(f), this.getViewYRot(f));
	}

	protected final Vec3 calculateUpVector(float f, float g) {
		return this.calculateViewVector(f - 90.0F, g);
	}

	public final Vec3 getEyePosition() {
		return new Vec3(this.getX(), this.getEyeY(), this.getZ());
	}

	public final Vec3 getEyePosition(float f) {
		double d = Mth.lerp((double)f, this.xo, this.getX());
		double e = Mth.lerp((double)f, this.yo, this.getY()) + (double)this.getEyeHeight();
		double g = Mth.lerp((double)f, this.zo, this.getZ());
		return new Vec3(d, e, g);
	}

	public Vec3 getLightProbePosition(float f) {
		return this.getEyePosition(f);
	}

	public final Vec3 getPosition(float f) {
		double d = Mth.lerp((double)f, this.xo, this.getX());
		double e = Mth.lerp((double)f, this.yo, this.getY());
		double g = Mth.lerp((double)f, this.zo, this.getZ());
		return new Vec3(d, e, g);
	}

	public HitResult pick(double d, float f, boolean bl) {
		Vec3 vec3 = this.getEyePosition(f);
		Vec3 vec32 = this.getViewVector(f);
		Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
		return this.level().clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
	}

	public boolean canBeHitByProjectile() {
		return this.isAlive() && this.isPickable();
	}

	public boolean isPickable() {
		return false;
	}

	public boolean isPushable() {
		return false;
	}

	public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
		if (entity instanceof ServerPlayer) {
			CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayer)entity, this, damageSource);
		}
	}

	public boolean shouldRender(double d, double e, double f) {
		double g = this.getX() - d;
		double h = this.getY() - e;
		double i = this.getZ() - f;
		double j = g * g + h * h + i * i;
		return this.shouldRenderAtSqrDistance(j);
	}

	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize();
		if (Double.isNaN(e)) {
			e = 1.0;
		}

		e *= 64.0 * viewScale;
		return d < e * e;
	}

	public boolean saveAsPassenger(CompoundTag compoundTag) {
		if (this.removalReason != null && !this.removalReason.shouldSave()) {
			return false;
		} else {
			String string = this.getEncodeId();
			if (string == null) {
				return false;
			} else {
				compoundTag.putString("id", string);
				this.saveWithoutId(compoundTag);
				return true;
			}
		}
	}

	public boolean save(CompoundTag compoundTag) {
		return this.isPassenger() ? false : this.saveAsPassenger(compoundTag);
	}

	public CompoundTag saveWithoutId(CompoundTag compoundTag) {
		try {
			if (this.vehicle != null) {
				compoundTag.put("Pos", this.newDoubleList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
			} else {
				compoundTag.put("Pos", this.newDoubleList(this.getX(), this.getY(), this.getZ()));
			}

			Vec3 vec3 = this.getDeltaMovement();
			compoundTag.put("Motion", this.newDoubleList(vec3.x, vec3.y, vec3.z));
			compoundTag.put("Rotation", this.newFloatList(this.getYRot(), this.getXRot()));
			compoundTag.putFloat("FallDistance", this.fallDistance);
			compoundTag.putShort("Fire", (short)this.remainingFireTicks);
			compoundTag.putShort("Air", (short)this.getAirSupply());
			compoundTag.putBoolean("OnGround", this.onGround());
			compoundTag.putBoolean("Invulnerable", this.invulnerable);
			compoundTag.putInt("PortalCooldown", this.portalCooldown);
			compoundTag.putUUID("UUID", this.getUUID());
			Component component = this.getCustomName();
			if (component != null) {
				compoundTag.putString("CustomName", Component.Serializer.toJson(component, this.registryAccess()));
			}

			if (this.isCustomNameVisible()) {
				compoundTag.putBoolean("CustomNameVisible", this.isCustomNameVisible());
			}

			if (this.isSilent()) {
				compoundTag.putBoolean("Silent", this.isSilent());
			}

			if (this.isNoGravity()) {
				compoundTag.putBoolean("NoGravity", this.isNoGravity());
			}

			if (this.hasGlowingTag) {
				compoundTag.putBoolean("Glowing", true);
			}

			int i = this.getTicksFrozen();
			if (i > 0) {
				compoundTag.putInt("TicksFrozen", this.getTicksFrozen());
			}

			if (this.hasVisualFire) {
				compoundTag.putBoolean("HasVisualFire", this.hasVisualFire);
			}

			if (!this.tags.isEmpty()) {
				ListTag listTag = new ListTag();

				for (String string : this.tags) {
					listTag.add(StringTag.valueOf(string));
				}

				compoundTag.put("Tags", listTag);
			}

			this.addAdditionalSaveData(compoundTag);
			if (this.isVehicle()) {
				ListTag listTag = new ListTag();

				for (Entity entity : this.getPassengers()) {
					CompoundTag compoundTag2 = new CompoundTag();
					if (entity.saveAsPassenger(compoundTag2)) {
						listTag.add(compoundTag2);
					}
				}

				if (!listTag.isEmpty()) {
					compoundTag.put("Passengers", listTag);
				}
			}

			return compoundTag;
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Saving entity NBT");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being saved");
			this.fillCrashReportCategory(crashReportCategory);
			throw new ReportedException(crashReport);
		}
	}

	public void load(CompoundTag compoundTag) {
		try {
			ListTag listTag = compoundTag.getList("Pos", 6);
			ListTag listTag2 = compoundTag.getList("Motion", 6);
			ListTag listTag3 = compoundTag.getList("Rotation", 5);
			double d = listTag2.getDouble(0);
			double e = listTag2.getDouble(1);
			double f = listTag2.getDouble(2);
			this.setDeltaMovement(Math.abs(d) > 10.0 ? 0.0 : d, Math.abs(e) > 10.0 ? 0.0 : e, Math.abs(f) > 10.0 ? 0.0 : f);
			this.hasImpulse = true;
			double g = 3.0000512E7;
			this.setPosRaw(
				Mth.clamp(listTag.getDouble(0), -3.0000512E7, 3.0000512E7),
				Mth.clamp(listTag.getDouble(1), -2.0E7, 2.0E7),
				Mth.clamp(listTag.getDouble(2), -3.0000512E7, 3.0000512E7)
			);
			this.setYRot(listTag3.getFloat(0));
			this.setXRot(listTag3.getFloat(1));
			this.setOldPosAndRot();
			this.setYHeadRot(this.getYRot());
			this.setYBodyRot(this.getYRot());
			this.fallDistance = compoundTag.getFloat("FallDistance");
			this.remainingFireTicks = compoundTag.getShort("Fire");
			if (compoundTag.contains("Air")) {
				this.setAirSupply(compoundTag.getShort("Air"));
			}

			this.onGround = compoundTag.getBoolean("OnGround");
			this.invulnerable = compoundTag.getBoolean("Invulnerable");
			this.portalCooldown = compoundTag.getInt("PortalCooldown");
			if (compoundTag.hasUUID("UUID")) {
				this.uuid = compoundTag.getUUID("UUID");
				this.stringUUID = this.uuid.toString();
			}

			if (!Double.isFinite(this.getX()) || !Double.isFinite(this.getY()) || !Double.isFinite(this.getZ())) {
				throw new IllegalStateException("Entity has invalid position");
			} else if (Double.isFinite((double)this.getYRot()) && Double.isFinite((double)this.getXRot())) {
				this.reapplyPosition();
				this.setRot(this.getYRot(), this.getXRot());
				if (compoundTag.contains("CustomName", 8)) {
					String string = compoundTag.getString("CustomName");

					try {
						this.setCustomName(Component.Serializer.fromJson(string, this.registryAccess()));
					} catch (Exception var16) {
						LOGGER.warn("Failed to parse entity custom name {}", string, var16);
					}
				}

				this.setCustomNameVisible(compoundTag.getBoolean("CustomNameVisible"));
				this.setSilent(compoundTag.getBoolean("Silent"));
				this.setNoGravity(compoundTag.getBoolean("NoGravity"));
				this.setGlowingTag(compoundTag.getBoolean("Glowing"));
				this.setTicksFrozen(compoundTag.getInt("TicksFrozen"));
				this.hasVisualFire = compoundTag.getBoolean("HasVisualFire");
				if (compoundTag.contains("Tags", 9)) {
					this.tags.clear();
					ListTag listTag4 = compoundTag.getList("Tags", 8);
					int i = Math.min(listTag4.size(), 1024);

					for (int j = 0; j < i; j++) {
						this.tags.add(listTag4.getString(j));
					}
				}

				this.readAdditionalSaveData(compoundTag);
				if (this.repositionEntityAfterLoad()) {
					this.reapplyPosition();
				}
			} else {
				throw new IllegalStateException("Entity has invalid rotation");
			}
		} catch (Throwable var17) {
			CrashReport crashReport = CrashReport.forThrowable(var17, "Loading entity NBT");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being loaded");
			this.fillCrashReportCategory(crashReportCategory);
			throw new ReportedException(crashReport);
		}
	}

	protected boolean repositionEntityAfterLoad() {
		return true;
	}

	@Nullable
	protected final String getEncodeId() {
		EntityType<?> entityType = this.getType();
		ResourceLocation resourceLocation = EntityType.getKey(entityType);
		return entityType.canSerialize() && resourceLocation != null ? resourceLocation.toString() : null;
	}

	protected abstract void readAdditionalSaveData(CompoundTag compoundTag);

	protected abstract void addAdditionalSaveData(CompoundTag compoundTag);

	protected ListTag newDoubleList(double... ds) {
		ListTag listTag = new ListTag();

		for (double d : ds) {
			listTag.add(DoubleTag.valueOf(d));
		}

		return listTag;
	}

	protected ListTag newFloatList(float... fs) {
		ListTag listTag = new ListTag();

		for (float f : fs) {
			listTag.add(FloatTag.valueOf(f));
		}

		return listTag;
	}

	@Nullable
	public ItemEntity spawnAtLocation(ItemLike itemLike) {
		return this.spawnAtLocation(itemLike, 0);
	}

	@Nullable
	public ItemEntity spawnAtLocation(ItemLike itemLike, int i) {
		return this.spawnAtLocation(new ItemStack(itemLike), (float)i);
	}

	@Nullable
	public ItemEntity spawnAtLocation(ItemStack itemStack) {
		return this.spawnAtLocation(itemStack, 0.0F);
	}

	@Nullable
	public ItemEntity spawnAtLocation(ItemStack itemStack, float f) {
		if (itemStack.isEmpty()) {
			return null;
		} else if (this.level().isClientSide) {
			return null;
		} else {
			ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + (double)f, this.getZ(), itemStack);
			itemEntity.setDefaultPickUpDelay();
			this.level().addFreshEntity(itemEntity);
			return itemEntity;
		}
	}

	public boolean isAlive() {
		return !this.isRemoved();
	}

	public boolean isInWall() {
		if (this.noPhysics) {
			return false;
		} else {
			float f = this.dimensions.width() * 0.8F;
			AABB aABB = AABB.ofSize(this.getEyePosition(), (double)f, 1.0E-6, (double)f);
			return BlockPos.betweenClosedStream(aABB)
				.anyMatch(
					blockPos -> {
						BlockState blockState = this.level().getBlockState(blockPos);
						return !blockState.isAir()
							&& blockState.isSuffocating(this.level(), blockPos)
							&& Shapes.joinIsNotEmpty(
								blockState.getCollisionShape(this.level(), blockPos).move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()),
								Shapes.create(aABB),
								BooleanOp.AND
							);
					}
				);
		}
	}

	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (this.isAlive() && this instanceof Leashable leashable) {
			if (leashable.getLeashHolder() == player) {
				if (!this.level().isClientSide()) {
					leashable.dropLeash(true, !player.hasInfiniteMaterials());
					this.gameEvent(GameEvent.ENTITY_INTERACT, player);
				}

				return InteractionResult.SUCCESS;
			}

			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (itemStack.is(Items.LEAD) && leashable.canHaveALeashAttachedToIt()) {
				if (!this.level().isClientSide()) {
					leashable.setLeashedTo(player, true);
				}

				itemStack.shrink(1);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	public boolean canCollideWith(Entity entity) {
		return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
	}

	public boolean canBeCollidedWith() {
		return false;
	}

	public void rideTick() {
		this.setDeltaMovement(Vec3.ZERO);
		this.tick();
		if (this.isPassenger()) {
			this.getVehicle().positionRider(this);
		}
	}

	public final void positionRider(Entity entity) {
		if (this.hasPassenger(entity)) {
			this.positionRider(entity, Entity::setPos);
		}
	}

	protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
		Vec3 vec3 = this.getPassengerRidingPosition(entity);
		Vec3 vec32 = entity.getVehicleAttachmentPoint(this);
		moveFunction.accept(entity, vec3.x - vec32.x, vec3.y - vec32.y, vec3.z - vec32.z);
	}

	public void onPassengerTurned(Entity entity) {
	}

	public Vec3 getVehicleAttachmentPoint(Entity entity) {
		return this.getAttachments().get(EntityAttachment.VEHICLE, 0, this.yRot);
	}

	public Vec3 getPassengerRidingPosition(Entity entity) {
		return this.position().add(this.getPassengerAttachmentPoint(entity, this.dimensions, 1.0F));
	}

	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return getDefaultPassengerAttachmentPoint(this, entity, entityDimensions.attachments());
	}

	protected static Vec3 getDefaultPassengerAttachmentPoint(Entity entity, Entity entity2, EntityAttachments entityAttachments) {
		int i = entity.getPassengers().indexOf(entity2);
		return entityAttachments.getClamped(EntityAttachment.PASSENGER, i, entity.yRot);
	}

	public boolean startRiding(Entity entity) {
		return this.startRiding(entity, false);
	}

	public boolean showVehicleHealth() {
		return this instanceof LivingEntity;
	}

	public boolean startRiding(Entity entity, boolean bl) {
		if (entity == this.vehicle) {
			return false;
		} else if (!entity.couldAcceptPassenger()) {
			return false;
		} else {
			for (Entity entity2 = entity; entity2.vehicle != null; entity2 = entity2.vehicle) {
				if (entity2.vehicle == this) {
					return false;
				}
			}

			if (bl || this.canRide(entity) && entity.canAddPassenger(this)) {
				if (this.isPassenger()) {
					this.stopRiding();
				}

				this.setPose(Pose.STANDING);
				this.vehicle = entity;
				this.vehicle.addPassenger(this);
				entity.getIndirectPassengersStream()
					.filter(entityx -> entityx instanceof ServerPlayer)
					.forEach(entityx -> CriteriaTriggers.START_RIDING_TRIGGER.trigger((ServerPlayer)entityx));
				return true;
			} else {
				return false;
			}
		}
	}

	protected boolean canRide(Entity entity) {
		return !this.isShiftKeyDown() && this.boardingCooldown <= 0;
	}

	public void ejectPassengers() {
		for (int i = this.passengers.size() - 1; i >= 0; i--) {
			((Entity)this.passengers.get(i)).stopRiding();
		}
	}

	public void removeVehicle() {
		if (this.vehicle != null) {
			Entity entity = this.vehicle;
			this.vehicle = null;
			entity.removePassenger(this);
		}
	}

	public void stopRiding() {
		this.removeVehicle();
	}

	protected void addPassenger(Entity entity) {
		if (entity.getVehicle() != this) {
			throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
		} else {
			if (this.passengers.isEmpty()) {
				this.passengers = ImmutableList.of(entity);
			} else {
				List<Entity> list = Lists.<Entity>newArrayList(this.passengers);
				if (!this.level().isClientSide && entity instanceof Player && !(this.getFirstPassenger() instanceof Player)) {
					list.add(0, entity);
				} else {
					list.add(entity);
				}

				this.passengers = ImmutableList.copyOf(list);
			}

			this.gameEvent(GameEvent.ENTITY_MOUNT, entity);
		}
	}

	protected void removePassenger(Entity entity) {
		if (entity.getVehicle() == this) {
			throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
		} else {
			if (this.passengers.size() == 1 && this.passengers.get(0) == entity) {
				this.passengers = ImmutableList.of();
			} else {
				this.passengers = (ImmutableList<Entity>)this.passengers.stream().filter(entity2 -> entity2 != entity).collect(ImmutableList.toImmutableList());
			}

			entity.boardingCooldown = 60;
			this.gameEvent(GameEvent.ENTITY_DISMOUNT, entity);
		}
	}

	protected boolean canAddPassenger(Entity entity) {
		return this.passengers.isEmpty();
	}

	protected boolean couldAcceptPassenger() {
		return true;
	}

	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.setPos(d, e, f);
		this.setRot(g, h);
	}

	public double lerpTargetX() {
		return this.getX();
	}

	public double lerpTargetY() {
		return this.getY();
	}

	public double lerpTargetZ() {
		return this.getZ();
	}

	public float lerpTargetXRot() {
		return this.getXRot();
	}

	public float lerpTargetYRot() {
		return this.getYRot();
	}

	public void lerpHeadTo(float f, int i) {
		this.setYHeadRot(f);
	}

	public float getPickRadius() {
		return 0.0F;
	}

	public Vec3 getLookAngle() {
		return this.calculateViewVector(this.getXRot(), this.getYRot());
	}

	public Vec3 getHandHoldingItemAngle(Item item) {
		if (!(this instanceof Player player)) {
			return Vec3.ZERO;
		} else {
			boolean bl = player.getOffhandItem().is(item) && !player.getMainHandItem().is(item);
			HumanoidArm humanoidArm = bl ? player.getMainArm().getOpposite() : player.getMainArm();
			return this.calculateViewVector(0.0F, this.getYRot() + (float)(humanoidArm == HumanoidArm.RIGHT ? 80 : -80)).scale(0.5);
		}
	}

	public Vec2 getRotationVector() {
		return new Vec2(this.getXRot(), this.getYRot());
	}

	public Vec3 getForward() {
		return Vec3.directionFromRotation(this.getRotationVector());
	}

	public void setAsInsidePortal(Portal portal, BlockPos blockPos) {
		if (this.isOnPortalCooldown()) {
			this.setPortalCooldown();
		} else {
			if (this.portalProcess == null || !this.portalProcess.isSamePortal(portal)) {
				this.portalProcess = new PortalProcessor(portal, blockPos.immutable());
			} else if (!this.portalProcess.isInsidePortalThisTick()) {
				this.portalProcess.updateEntryPosition(blockPos.immutable());
				this.portalProcess.setAsInsidePortalThisTick(true);
			}
		}
	}

	protected void handlePortal() {
		if (this.level() instanceof ServerLevel serverLevel) {
			this.processPortalCooldown();
			if (this.portalProcess != null) {
				if (this.portalProcess.processPortalTeleportation(serverLevel, this, this.canUsePortal(false))) {
					ProfilerFiller profilerFiller = Profiler.get();
					profilerFiller.push("portal");
					this.setPortalCooldown();
					DimensionTransition dimensionTransition = this.portalProcess.getPortalDestination(serverLevel, this);
					if (dimensionTransition != null) {
						ServerLevel serverLevel2 = dimensionTransition.newLevel();
						if (serverLevel.getServer().isLevelEnabled(serverLevel2)
							&& (serverLevel2.dimension() == serverLevel.dimension() || this.canChangeDimensions(serverLevel, serverLevel2))) {
							this.changeDimension(dimensionTransition);
						}
					}

					profilerFiller.pop();
				} else if (this.portalProcess.hasExpired()) {
					this.portalProcess = null;
				}
			}
		}
	}

	public int getDimensionChangingDelay() {
		Entity entity = this.getFirstPassenger();
		return entity instanceof ServerPlayer ? entity.getDimensionChangingDelay() : 300;
	}

	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
	}

	public void handleDamageEvent(DamageSource damageSource) {
	}

	public void handleEntityEvent(byte b) {
		switch (b) {
			case 53:
				HoneyBlock.showSlideParticles(this);
		}
	}

	public void animateHurt(float f) {
	}

	public boolean isOnFire() {
		boolean bl = this.level() != null && this.level().isClientSide;
		return !this.fireImmune() && (this.remainingFireTicks > 0 || bl && this.getSharedFlag(0));
	}

	public boolean isPassenger() {
		return this.getVehicle() != null;
	}

	public boolean isVehicle() {
		return !this.passengers.isEmpty();
	}

	public boolean dismountsUnderwater() {
		return this.getType().is(EntityTypeTags.DISMOUNTS_UNDERWATER);
	}

	public boolean canControlVehicle() {
		return !this.getType().is(EntityTypeTags.NON_CONTROLLING_RIDER);
	}

	public void setShiftKeyDown(boolean bl) {
		this.setSharedFlag(1, bl);
	}

	public boolean isShiftKeyDown() {
		return this.getSharedFlag(1);
	}

	public boolean isSteppingCarefully() {
		return this.isShiftKeyDown();
	}

	public boolean isSuppressingBounce() {
		return this.isShiftKeyDown();
	}

	public boolean isDiscrete() {
		return this.isShiftKeyDown();
	}

	public boolean isDescending() {
		return this.isShiftKeyDown();
	}

	public boolean isCrouching() {
		return this.hasPose(Pose.CROUCHING);
	}

	public boolean isSprinting() {
		return this.getSharedFlag(3);
	}

	public void setSprinting(boolean bl) {
		this.setSharedFlag(3, bl);
	}

	public boolean isSwimming() {
		return this.getSharedFlag(4);
	}

	public boolean isVisuallySwimming() {
		return this.hasPose(Pose.SWIMMING);
	}

	public boolean isVisuallyCrawling() {
		return this.isVisuallySwimming() && !this.isInWater();
	}

	public void setSwimming(boolean bl) {
		this.setSharedFlag(4, bl);
	}

	public final boolean hasGlowingTag() {
		return this.hasGlowingTag;
	}

	public final void setGlowingTag(boolean bl) {
		this.hasGlowingTag = bl;
		this.setSharedFlag(6, this.isCurrentlyGlowing());
	}

	public boolean isCurrentlyGlowing() {
		return this.level().isClientSide() ? this.getSharedFlag(6) : this.hasGlowingTag;
	}

	public boolean isInvisible() {
		return this.getSharedFlag(5);
	}

	public boolean isInvisibleTo(Player player) {
		if (player.isSpectator()) {
			return false;
		} else {
			Team team = this.getTeam();
			return team != null && player != null && player.getTeam() == team && team.canSeeFriendlyInvisibles() ? false : this.isInvisible();
		}
	}

	public boolean isOnRails() {
		return false;
	}

	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
	}

	@Nullable
	public PlayerTeam getTeam() {
		return this.level().getScoreboard().getPlayersTeam(this.getScoreboardName());
	}

	public final boolean isAlliedTo(@Nullable Entity entity) {
		return entity == null ? false : this == entity || this.considersEntityAsAlly(entity) || entity.considersEntityAsAlly(this);
	}

	protected boolean considersEntityAsAlly(Entity entity) {
		return this.isAlliedTo(entity.getTeam());
	}

	public boolean isAlliedTo(@Nullable Team team) {
		return this.getTeam() != null ? this.getTeam().isAlliedTo(team) : false;
	}

	public void setInvisible(boolean bl) {
		this.setSharedFlag(5, bl);
	}

	protected boolean getSharedFlag(int i) {
		return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << i) != 0;
	}

	protected void setSharedFlag(int i, boolean bl) {
		byte b = this.entityData.get(DATA_SHARED_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b | 1 << i));
		} else {
			this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b & ~(1 << i)));
		}
	}

	public int getMaxAirSupply() {
		return 300;
	}

	public int getAirSupply() {
		return this.entityData.get(DATA_AIR_SUPPLY_ID);
	}

	public void setAirSupply(int i) {
		this.entityData.set(DATA_AIR_SUPPLY_ID, i);
	}

	public int getTicksFrozen() {
		return this.entityData.get(DATA_TICKS_FROZEN);
	}

	public void setTicksFrozen(int i) {
		this.entityData.set(DATA_TICKS_FROZEN, i);
	}

	public float getPercentFrozen() {
		int i = this.getTicksRequiredToFreeze();
		return (float)Math.min(this.getTicksFrozen(), i) / (float)i;
	}

	public boolean isFullyFrozen() {
		return this.getTicksFrozen() >= this.getTicksRequiredToFreeze();
	}

	public int getTicksRequiredToFreeze() {
		return 140;
	}

	public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
		this.setRemainingFireTicks(this.remainingFireTicks + 1);
		if (this.remainingFireTicks == 0) {
			this.igniteForSeconds(8.0F);
		}

		this.hurt(this.damageSources().lightningBolt(), 5.0F);
	}

	public void onAboveBubbleCol(boolean bl) {
		Vec3 vec3 = this.getDeltaMovement();
		double d;
		if (bl) {
			d = Math.max(-0.9, vec3.y - 0.03);
		} else {
			d = Math.min(1.8, vec3.y + 0.1);
		}

		this.setDeltaMovement(vec3.x, d, vec3.z);
	}

	public void onInsideBubbleColumn(boolean bl) {
		Vec3 vec3 = this.getDeltaMovement();
		double d;
		if (bl) {
			d = Math.max(-0.3, vec3.y - 0.03);
		} else {
			d = Math.min(0.7, vec3.y + 0.06);
		}

		this.setDeltaMovement(vec3.x, d, vec3.z);
		this.resetFallDistance();
	}

	public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
		return true;
	}

	public void checkSlowFallDistance() {
		if (this.getDeltaMovement().y() > -0.5 && this.fallDistance > 1.0F) {
			this.fallDistance = 1.0F;
		}
	}

	public void resetFallDistance() {
		this.fallDistance = 0.0F;
	}

	protected void moveTowardsClosestSpace(double d, double e, double f) {
		BlockPos blockPos = BlockPos.containing(d, e, f);
		Vec3 vec3 = new Vec3(d - (double)blockPos.getX(), e - (double)blockPos.getY(), f - (double)blockPos.getZ());
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Direction direction = Direction.UP;
		double g = Double.MAX_VALUE;

		for (Direction direction2 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
			mutableBlockPos.setWithOffset(blockPos, direction2);
			if (!this.level().getBlockState(mutableBlockPos).isCollisionShapeFullBlock(this.level(), mutableBlockPos)) {
				double h = vec3.get(direction2.getAxis());
				double i = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - h : h;
				if (i < g) {
					g = i;
					direction = direction2;
				}
			}
		}

		float j = this.random.nextFloat() * 0.2F + 0.1F;
		float k = (float)direction.getAxisDirection().getStep();
		Vec3 vec32 = this.getDeltaMovement().scale(0.75);
		if (direction.getAxis() == Direction.Axis.X) {
			this.setDeltaMovement((double)(k * j), vec32.y, vec32.z);
		} else if (direction.getAxis() == Direction.Axis.Y) {
			this.setDeltaMovement(vec32.x, (double)(k * j), vec32.z);
		} else if (direction.getAxis() == Direction.Axis.Z) {
			this.setDeltaMovement(vec32.x, vec32.y, (double)(k * j));
		}
	}

	public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
		this.resetFallDistance();
		this.stuckSpeedMultiplier = vec3;
	}

	private static Component removeAction(Component component) {
		MutableComponent mutableComponent = component.plainCopy().setStyle(component.getStyle().withClickEvent(null));

		for (Component component2 : component.getSiblings()) {
			mutableComponent.append(removeAction(component2));
		}

		return mutableComponent;
	}

	@Override
	public Component getName() {
		Component component = this.getCustomName();
		return component != null ? removeAction(component) : this.getTypeName();
	}

	protected Component getTypeName() {
		return this.type.getDescription();
	}

	public boolean is(Entity entity) {
		return this == entity;
	}

	public float getYHeadRot() {
		return 0.0F;
	}

	public void setYHeadRot(float f) {
	}

	public void setYBodyRot(float f) {
	}

	public boolean isAttackable() {
		return true;
	}

	public boolean skipAttackInteraction(Entity entity) {
		return false;
	}

	public String toString() {
		String string = this.level() == null ? "~NULL~" : this.level().toString();
		return this.removalReason != null
			? String.format(
				Locale.ROOT,
				"%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]",
				this.getClass().getSimpleName(),
				this.getName().getString(),
				this.id,
				string,
				this.getX(),
				this.getY(),
				this.getZ(),
				this.removalReason
			)
			: String.format(
				Locale.ROOT,
				"%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]",
				this.getClass().getSimpleName(),
				this.getName().getString(),
				this.id,
				string,
				this.getX(),
				this.getY(),
				this.getZ()
			);
	}

	public boolean isInvulnerableTo(DamageSource damageSource) {
		return this.isRemoved()
			|| this.invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.isCreativePlayer()
			|| damageSource.is(DamageTypeTags.IS_FIRE) && this.fireImmune()
			|| damageSource.is(DamageTypeTags.IS_FALL) && this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE);
	}

	public boolean isInvulnerable() {
		return this.invulnerable;
	}

	public void setInvulnerable(boolean bl) {
		this.invulnerable = bl;
	}

	public void copyPosition(Entity entity) {
		this.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
	}

	public void restoreFrom(Entity entity) {
		CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
		compoundTag.remove("Dimension");
		this.load(compoundTag);
		this.portalCooldown = entity.portalCooldown;
		this.portalProcess = entity.portalProcess;
	}

	@Nullable
	public Entity changeDimension(DimensionTransition dimensionTransition) {
		if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved()) {
			ServerLevel serverLevel2 = dimensionTransition.newLevel();
			List<Entity> list = this.getPassengers();
			this.unRide();
			List<Entity> list2 = new ArrayList();

			for (Entity entity : list) {
				float f = entity.getYRot() - this.getYRot();
				float g = entity.getXRot() - this.getXRot();
				float h = dimensionTransition.yRot() + (dimensionTransition.relatives().contains(Relative.Y_ROT) ? 0.0F : f);
				float i = dimensionTransition.xRot() + (dimensionTransition.relatives().contains(Relative.X_ROT) ? 0.0F : g);
				Entity entity2 = entity.changeDimension(dimensionTransition.withRotation(h, i));
				if (entity2 != null) {
					list2.add(entity2);
				}
			}

			ProfilerFiller profilerFiller = Profiler.get();
			profilerFiller.push("changeDimension");
			Entity entityx = serverLevel2.dimension() == serverLevel.dimension() ? this : this.getType().create(serverLevel2, EntitySpawnReason.DIMENSION_TRAVEL);
			if (entityx != null) {
				if (this != entityx) {
					entityx.restoreFrom(this);
					this.removeAfterChangingDimensions();
				}

				entityx.teleportSetPosition(dimensionTransition);
				if (this != entityx) {
					serverLevel2.addDuringTeleport(entityx);
				}

				for (Entity entity3 : list2) {
					entity3.startRiding(entityx, true);
				}

				serverLevel.resetEmptyTime();
				serverLevel2.resetEmptyTime();
				dimensionTransition.postDimensionTransition().onTransition(entityx);
			}

			profilerFiller.pop();
			return entityx;
		}

		return null;
	}

	protected void teleportSetPosition(DimensionTransition dimensionTransition) {
		PositionMoveRotation positionMoveRotation = PositionMoveRotation.of(dimensionTransition);
		PositionMoveRotation positionMoveRotation2 = PositionMoveRotation.calculateAbsolute(
			PositionMoveRotation.of(this), positionMoveRotation, dimensionTransition.relatives()
		);
		this.setPosRaw(positionMoveRotation2.position().x, positionMoveRotation2.position().y, positionMoveRotation2.position().z);
		this.setYRot(positionMoveRotation2.yRot());
		this.setYHeadRot(positionMoveRotation2.yRot());
		this.setXRot(positionMoveRotation2.xRot());
		this.reapplyPosition();
		this.setOldPosAndRot();
		this.setDeltaMovement(positionMoveRotation2.deltaMovement());
		this.blocksInside.clear();
	}

	public void placePortalTicket(BlockPos blockPos) {
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
		}
	}

	protected void removeAfterChangingDimensions() {
		this.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
		if (this instanceof Leashable leashable) {
			leashable.dropLeash(true, false);
		}
	}

	public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
		return PortalShape.getRelativePosition(foundRectangle, axis, this.position(), this.getDimensions(this.getPose()));
	}

	public boolean canUsePortal(boolean bl) {
		return (bl || !this.isPassenger()) && this.isAlive();
	}

	public boolean canChangeDimensions(Level level, Level level2) {
		if (level.dimension() == Level.END && level2.dimension() == Level.OVERWORLD) {
			for (Entity entity : this.getPassengers()) {
				if (entity instanceof ServerPlayer serverPlayer && !serverPlayer.seenCredits) {
					return false;
				}
			}
		}

		return true;
	}

	public float getBlockExplosionResistance(
		Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f
	) {
		return f;
	}

	public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
		return true;
	}

	public int getMaxFallDistance() {
		return 3;
	}

	public boolean isIgnoringBlockTriggers() {
		return false;
	}

	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail(
			"Entity Type", (CrashReportDetail<String>)(() -> EntityType.getKey(this.getType()) + " (" + this.getClass().getCanonicalName() + ")")
		);
		crashReportCategory.setDetail("Entity ID", this.id);
		crashReportCategory.setDetail("Entity Name", (CrashReportDetail<String>)(() -> this.getName().getString()));
		crashReportCategory.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
		crashReportCategory.setDetail(
			"Entity's Block location", CrashReportCategory.formatLocation(this.level(), Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()))
		);
		Vec3 vec3 = this.getDeltaMovement();
		crashReportCategory.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vec3.x, vec3.y, vec3.z));
		crashReportCategory.setDetail("Entity's Passengers", (CrashReportDetail<String>)(() -> this.getPassengers().toString()));
		crashReportCategory.setDetail("Entity's Vehicle", (CrashReportDetail<String>)(() -> String.valueOf(this.getVehicle())));
	}

	public boolean displayFireAnimation() {
		return this.isOnFire() && !this.isSpectator();
	}

	public void setUUID(UUID uUID) {
		this.uuid = uUID;
		this.stringUUID = this.uuid.toString();
	}

	@Override
	public UUID getUUID() {
		return this.uuid;
	}

	public String getStringUUID() {
		return this.stringUUID;
	}

	@Override
	public String getScoreboardName() {
		return this.stringUUID;
	}

	public boolean isPushedByFluid() {
		return true;
	}

	public static double getViewScale() {
		return viewScale;
	}

	public static void setViewScale(double d) {
		viewScale = d;
	}

	@Override
	public Component getDisplayName() {
		return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName())
			.withStyle(style -> style.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID()));
	}

	public void setCustomName(@Nullable Component component) {
		this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(component));
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return (Component)this.entityData.get(DATA_CUSTOM_NAME).orElse(null);
	}

	@Override
	public boolean hasCustomName() {
		return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
	}

	public void setCustomNameVisible(boolean bl) {
		this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, bl);
	}

	public boolean isCustomNameVisible() {
		return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
	}

	public boolean teleportTo(ServerLevel serverLevel, double d, double e, double f, Set<Relative> set, float g, float h, boolean bl) {
		float i = Mth.clamp(h, -90.0F, 90.0F);
		Entity entity = this.changeDimension(new DimensionTransition(serverLevel, new Vec3(d, e, f), Vec3.ZERO, g, i, set, DimensionTransition.DO_NOTHING));
		return entity != null;
	}

	public void dismountTo(double d, double e, double f) {
		this.teleportTo(d, e, f);
	}

	public void teleportTo(double d, double e, double f) {
		if (this.level() instanceof ServerLevel) {
			this.moveTo(d, e, f, this.getYRot(), this.getXRot());
			this.teleportPassengers();
		}
	}

	private void teleportPassengers() {
		this.getSelfAndPassengers().forEach(entity -> {
			for (Entity entity2 : entity.passengers) {
				entity.positionRider(entity2, Entity::moveTo);
			}
		});
	}

	public void teleportRelative(double d, double e, double f) {
		this.teleportTo(this.getX() + d, this.getY() + e, this.getZ() + f);
	}

	public boolean shouldShowName() {
		return this.isCustomNameVisible();
	}

	@Override
	public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> list) {
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_POSE.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}
	}

	@Deprecated
	protected void fixupDimensions() {
		Pose pose = this.getPose();
		EntityDimensions entityDimensions = this.getDimensions(pose);
		this.dimensions = entityDimensions;
		this.eyeHeight = entityDimensions.eyeHeight();
	}

	public void refreshDimensions() {
		EntityDimensions entityDimensions = this.dimensions;
		Pose pose = this.getPose();
		EntityDimensions entityDimensions2 = this.getDimensions(pose);
		this.dimensions = entityDimensions2;
		this.eyeHeight = entityDimensions2.eyeHeight();
		this.reapplyPosition();
		boolean bl = entityDimensions2.width() <= 4.0F && entityDimensions2.height() <= 4.0F;
		if (!this.level.isClientSide
			&& !this.firstTick
			&& !this.noPhysics
			&& bl
			&& (entityDimensions2.width() > entityDimensions.width() || entityDimensions2.height() > entityDimensions.height())
			&& !(this instanceof Player)) {
			this.fudgePositionAfterSizeChange(entityDimensions);
		}
	}

	public boolean fudgePositionAfterSizeChange(EntityDimensions entityDimensions) {
		EntityDimensions entityDimensions2 = this.getDimensions(this.getPose());
		Vec3 vec3 = this.position().add(0.0, (double)entityDimensions.height() / 2.0, 0.0);
		double d = (double)Math.max(0.0F, entityDimensions2.width() - entityDimensions.width()) + 1.0E-6;
		double e = (double)Math.max(0.0F, entityDimensions2.height() - entityDimensions.height()) + 1.0E-6;
		VoxelShape voxelShape = Shapes.create(AABB.ofSize(vec3, d, e, d));
		Optional<Vec3> optional = this.level
			.findFreePosition(this, voxelShape, vec3, (double)entityDimensions2.width(), (double)entityDimensions2.height(), (double)entityDimensions2.width());
		if (optional.isPresent()) {
			this.setPos(((Vec3)optional.get()).add(0.0, (double)(-entityDimensions2.height()) / 2.0, 0.0));
			return true;
		} else {
			if (entityDimensions2.width() > entityDimensions.width() && entityDimensions2.height() > entityDimensions.height()) {
				VoxelShape voxelShape2 = Shapes.create(AABB.ofSize(vec3, d, 1.0E-6, d));
				Optional<Vec3> optional2 = this.level
					.findFreePosition(this, voxelShape2, vec3, (double)entityDimensions2.width(), (double)entityDimensions.height(), (double)entityDimensions2.width());
				if (optional2.isPresent()) {
					this.setPos(((Vec3)optional2.get()).add(0.0, (double)(-entityDimensions.height()) / 2.0 + 1.0E-6, 0.0));
					return true;
				}
			}

			return false;
		}
	}

	public Direction getDirection() {
		return Direction.fromYRot((double)this.getYRot());
	}

	public Direction getMotionDirection() {
		return this.getDirection();
	}

	protected HoverEvent createHoverEvent() {
		return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(this.getType(), this.getUUID(), this.getName()));
	}

	public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
		return true;
	}

	@Override
	public final AABB getBoundingBox() {
		return this.bb;
	}

	public final void setBoundingBox(AABB aABB) {
		this.bb = aABB;
	}

	public final float getEyeHeight(Pose pose) {
		return this.getDimensions(pose).eyeHeight();
	}

	public final float getEyeHeight() {
		return this.eyeHeight;
	}

	public Vec3 getLeashOffset(float f) {
		return this.getLeashOffset();
	}

	protected Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)this.getEyeHeight(), (double)(this.getBbWidth() * 0.4F));
	}

	public SlotAccess getSlot(int i) {
		return SlotAccess.NULL;
	}

	@Override
	public void sendSystemMessage(Component component) {
	}

	public Level getCommandSenderWorld() {
		return this.level();
	}

	@Nullable
	public MinecraftServer getServer() {
		return this.level().getServer();
	}

	public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand) {
		return InteractionResult.PASS;
	}

	public boolean ignoreExplosion(Explosion explosion) {
		return false;
	}

	public void startSeenByPlayer(ServerPlayer serverPlayer) {
	}

	public void stopSeenByPlayer(ServerPlayer serverPlayer) {
	}

	public float rotate(Rotation rotation) {
		float f = Mth.wrapDegrees(this.getYRot());
		switch (rotation) {
			case CLOCKWISE_180:
				return f + 180.0F;
			case COUNTERCLOCKWISE_90:
				return f + 270.0F;
			case CLOCKWISE_90:
				return f + 90.0F;
			default:
				return f;
		}
	}

	public float mirror(Mirror mirror) {
		float f = Mth.wrapDegrees(this.getYRot());
		switch (mirror) {
			case FRONT_BACK:
				return -f;
			case LEFT_RIGHT:
				return 180.0F - f;
			default:
				return f;
		}
	}

	public boolean onlyOpCanSetNbt() {
		return false;
	}

	public ProjectileDeflection deflection(Projectile projectile) {
		return this.getType().is(EntityTypeTags.DEFLECTS_PROJECTILES) ? ProjectileDeflection.REVERSE : ProjectileDeflection.NONE;
	}

	@Nullable
	public LivingEntity getControllingPassenger() {
		return null;
	}

	public final boolean hasControllingPassenger() {
		return this.getControllingPassenger() != null;
	}

	public final List<Entity> getPassengers() {
		return this.passengers;
	}

	@Nullable
	public Entity getFirstPassenger() {
		return this.passengers.isEmpty() ? null : (Entity)this.passengers.get(0);
	}

	public boolean hasPassenger(Entity entity) {
		return this.passengers.contains(entity);
	}

	public boolean hasPassenger(Predicate<Entity> predicate) {
		for (Entity entity : this.passengers) {
			if (predicate.test(entity)) {
				return true;
			}
		}

		return false;
	}

	private Stream<Entity> getIndirectPassengersStream() {
		return this.passengers.stream().flatMap(Entity::getSelfAndPassengers);
	}

	@Override
	public Stream<Entity> getSelfAndPassengers() {
		return Stream.concat(Stream.of(this), this.getIndirectPassengersStream());
	}

	@Override
	public Stream<Entity> getPassengersAndSelf() {
		return Stream.concat(this.passengers.stream().flatMap(Entity::getPassengersAndSelf), Stream.of(this));
	}

	public Iterable<Entity> getIndirectPassengers() {
		return () -> this.getIndirectPassengersStream().iterator();
	}

	public int countPlayerPassengers() {
		return (int)this.getIndirectPassengersStream().filter(entity -> entity instanceof Player).count();
	}

	public boolean hasExactlyOnePlayerPassenger() {
		return this.countPlayerPassengers() == 1;
	}

	public Entity getRootVehicle() {
		Entity entity = this;

		while (entity.isPassenger()) {
			entity = entity.getVehicle();
		}

		return entity;
	}

	public boolean isPassengerOfSameVehicle(Entity entity) {
		return this.getRootVehicle() == entity.getRootVehicle();
	}

	public boolean hasIndirectPassenger(Entity entity) {
		if (!entity.isPassenger()) {
			return false;
		} else {
			Entity entity2 = entity.getVehicle();
			return entity2 == this ? true : this.hasIndirectPassenger(entity2);
		}
	}

	public boolean isControlledByOrIsLocalPlayer() {
		return this instanceof Player player ? player.isLocalPlayer() : this.isControlledByLocalInstance();
	}

	public boolean isControlledByLocalInstance() {
		return this.getControllingPassenger() instanceof Player player ? player.isLocalPlayer() : this.isEffectiveAi();
	}

	public boolean isEffectiveAi() {
		return !this.level().isClientSide;
	}

	protected static Vec3 getCollisionHorizontalEscapeVector(double d, double e, float f) {
		double g = (d + e + 1.0E-5F) / 2.0;
		float h = -Mth.sin(f * (float) (Math.PI / 180.0));
		float i = Mth.cos(f * (float) (Math.PI / 180.0));
		float j = Math.max(Math.abs(h), Math.abs(i));
		return new Vec3((double)h * g / (double)j, 0.0, (double)i * g / (double)j);
	}

	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
	}

	@Nullable
	public Entity getVehicle() {
		return this.vehicle;
	}

	@Nullable
	public Entity getControlledVehicle() {
		return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
	}

	public PushReaction getPistonPushReaction() {
		return PushReaction.NORMAL;
	}

	public SoundSource getSoundSource() {
		return SoundSource.NEUTRAL;
	}

	protected int getFireImmuneTicks() {
		return 1;
	}

	public CommandSourceStack createCommandSourceStack() {
		return new CommandSourceStack(
			this,
			this.position(),
			this.getRotationVector(),
			this.level() instanceof ServerLevel ? (ServerLevel)this.level() : null,
			this.getPermissionLevel(),
			this.getName().getString(),
			this.getDisplayName(),
			this.level().getServer(),
			this
		);
	}

	protected int getPermissionLevel() {
		return 0;
	}

	public boolean hasPermissions(int i) {
		return this.getPermissionLevel() >= i;
	}

	@Override
	public boolean acceptsSuccess() {
		return this.level().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
	}

	@Override
	public boolean acceptsFailure() {
		return true;
	}

	@Override
	public boolean shouldInformAdmins() {
		return true;
	}

	public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
		Vec3 vec32 = anchor.apply(this);
		double d = vec3.x - vec32.x;
		double e = vec3.y - vec32.y;
		double f = vec3.z - vec32.z;
		double g = Math.sqrt(d * d + f * f);
		this.setXRot(Mth.wrapDegrees((float)(-(Mth.atan2(e, g) * 180.0F / (float)Math.PI))));
		this.setYRot(Mth.wrapDegrees((float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F));
		this.setYHeadRot(this.getYRot());
		this.xRotO = this.getXRot();
		this.yRotO = this.getYRot();
	}

	public float getPreciseBodyRotation(float f) {
		return Mth.lerp(f, this.yRotO, this.yRot);
	}

	public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagKey, double d) {
		if (this.touchingUnloadedChunk()) {
			return false;
		} else {
			AABB aABB = this.getBoundingBox().deflate(0.001);
			int i = Mth.floor(aABB.minX);
			int j = Mth.ceil(aABB.maxX);
			int k = Mth.floor(aABB.minY);
			int l = Mth.ceil(aABB.maxY);
			int m = Mth.floor(aABB.minZ);
			int n = Mth.ceil(aABB.maxZ);
			double e = 0.0;
			boolean bl = this.isPushedByFluid();
			boolean bl2 = false;
			Vec3 vec3 = Vec3.ZERO;
			int o = 0;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int p = i; p < j; p++) {
				for (int q = k; q < l; q++) {
					for (int r = m; r < n; r++) {
						mutableBlockPos.set(p, q, r);
						FluidState fluidState = this.level().getFluidState(mutableBlockPos);
						if (fluidState.is(tagKey)) {
							double f = (double)((float)q + fluidState.getHeight(this.level(), mutableBlockPos));
							if (f >= aABB.minY) {
								bl2 = true;
								e = Math.max(f - aABB.minY, e);
								if (bl) {
									Vec3 vec32 = fluidState.getFlow(this.level(), mutableBlockPos);
									if (e < 0.4) {
										vec32 = vec32.scale(e);
									}

									vec3 = vec3.add(vec32);
									o++;
								}
							}
						}
					}
				}
			}

			if (vec3.length() > 0.0) {
				if (o > 0) {
					vec3 = vec3.scale(1.0 / (double)o);
				}

				if (!(this instanceof Player)) {
					vec3 = vec3.normalize();
				}

				Vec3 vec33 = this.getDeltaMovement();
				vec3 = vec3.scale(d);
				double g = 0.003;
				if (Math.abs(vec33.x) < 0.003 && Math.abs(vec33.z) < 0.003 && vec3.length() < 0.0045000000000000005) {
					vec3 = vec3.normalize().scale(0.0045000000000000005);
				}

				this.setDeltaMovement(this.getDeltaMovement().add(vec3));
			}

			this.fluidHeight.put(tagKey, e);
			return bl2;
		}
	}

	public boolean touchingUnloadedChunk() {
		AABB aABB = this.getBoundingBox().inflate(1.0);
		int i = Mth.floor(aABB.minX);
		int j = Mth.ceil(aABB.maxX);
		int k = Mth.floor(aABB.minZ);
		int l = Mth.ceil(aABB.maxZ);
		return !this.level().hasChunksAt(i, k, j, l);
	}

	public double getFluidHeight(TagKey<Fluid> tagKey) {
		return this.fluidHeight.getDouble(tagKey);
	}

	public double getFluidJumpThreshold() {
		return (double)this.getEyeHeight() < 0.4 ? 0.0 : 0.4;
	}

	public final float getBbWidth() {
		return this.dimensions.width();
	}

	public final float getBbHeight() {
		return this.dimensions.height();
	}

	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(this, serverEntity);
	}

	public EntityDimensions getDimensions(Pose pose) {
		return this.type.getDimensions();
	}

	public final EntityAttachments getAttachments() {
		return this.dimensions.attachments();
	}

	public Vec3 position() {
		return this.position;
	}

	public Vec3 trackingPosition() {
		return this.position();
	}

	@Override
	public BlockPos blockPosition() {
		return this.blockPosition;
	}

	public BlockState getInBlockState() {
		if (this.inBlockState == null) {
			this.inBlockState = this.level().getBlockState(this.blockPosition());
		}

		return this.inBlockState;
	}

	public ChunkPos chunkPosition() {
		return this.chunkPosition;
	}

	public Vec3 getDeltaMovement() {
		return this.deltaMovement;
	}

	public void setDeltaMovement(Vec3 vec3) {
		this.deltaMovement = vec3;
	}

	public void addDeltaMovement(Vec3 vec3) {
		this.setDeltaMovement(this.getDeltaMovement().add(vec3));
	}

	public void setDeltaMovement(double d, double e, double f) {
		this.setDeltaMovement(new Vec3(d, e, f));
	}

	public final int getBlockX() {
		return this.blockPosition.getX();
	}

	public final double getX() {
		return this.position.x;
	}

	public double getX(double d) {
		return this.position.x + (double)this.getBbWidth() * d;
	}

	public double getRandomX(double d) {
		return this.getX((2.0 * this.random.nextDouble() - 1.0) * d);
	}

	public final int getBlockY() {
		return this.blockPosition.getY();
	}

	public final double getY() {
		return this.position.y;
	}

	public double getY(double d) {
		return this.position.y + (double)this.getBbHeight() * d;
	}

	public double getRandomY() {
		return this.getY(this.random.nextDouble());
	}

	public double getEyeY() {
		return this.position.y + (double)this.eyeHeight;
	}

	public final int getBlockZ() {
		return this.blockPosition.getZ();
	}

	public final double getZ() {
		return this.position.z;
	}

	public double getZ(double d) {
		return this.position.z + (double)this.getBbWidth() * d;
	}

	public double getRandomZ(double d) {
		return this.getZ((2.0 * this.random.nextDouble() - 1.0) * d);
	}

	public final void setPosRaw(double d, double e, double f) {
		if (this.position.x != d || this.position.y != e || this.position.z != f) {
			this.position = new Vec3(d, e, f);
			int i = Mth.floor(d);
			int j = Mth.floor(e);
			int k = Mth.floor(f);
			if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
				this.blockPosition = new BlockPos(i, j, k);
				this.inBlockState = null;
				if (SectionPos.blockToSectionCoord(i) != this.chunkPosition.x || SectionPos.blockToSectionCoord(k) != this.chunkPosition.z) {
					this.chunkPosition = new ChunkPos(this.blockPosition);
				}
			}

			this.levelCallback.onMove();
		}
	}

	public void checkDespawn() {
	}

	public Vec3 getRopeHoldPosition(float f) {
		return this.getPosition(f).add(0.0, (double)this.eyeHeight * 0.7, 0.0);
	}

	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		int i = clientboundAddEntityPacket.getId();
		double d = clientboundAddEntityPacket.getX();
		double e = clientboundAddEntityPacket.getY();
		double f = clientboundAddEntityPacket.getZ();
		this.syncPacketPositionCodec(d, e, f);
		this.moveTo(d, e, f);
		this.setXRot(clientboundAddEntityPacket.getXRot());
		this.setYRot(clientboundAddEntityPacket.getYRot());
		this.setId(i);
		this.setUUID(clientboundAddEntityPacket.getUUID());
	}

	@Nullable
	public ItemStack getPickResult() {
		return null;
	}

	public void setIsInPowderSnow(boolean bl) {
		this.isInPowderSnow = bl;
	}

	public boolean canFreeze() {
		return !this.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
	}

	public boolean isFreezing() {
		return (this.isInPowderSnow || this.wasInPowderSnow) && this.canFreeze();
	}

	public float getYRot() {
		return this.yRot;
	}

	public float getVisualRotationYInDegrees() {
		return this.getYRot();
	}

	public void setYRot(float f) {
		if (!Float.isFinite(f)) {
			Util.logAndPauseIfInIde("Invalid entity rotation: " + f + ", discarding.");
		} else {
			this.yRot = f;
		}
	}

	public float getXRot() {
		return this.xRot;
	}

	public void setXRot(float f) {
		if (!Float.isFinite(f)) {
			Util.logAndPauseIfInIde("Invalid entity rotation: " + f + ", discarding.");
		} else {
			this.xRot = Math.clamp(f % 360.0F, -90.0F, 90.0F);
		}
	}

	public boolean canSprint() {
		return false;
	}

	public float maxUpStep() {
		return 0.0F;
	}

	public void onExplosionHit(@Nullable Entity entity) {
	}

	public final boolean isRemoved() {
		return this.removalReason != null;
	}

	@Nullable
	public Entity.RemovalReason getRemovalReason() {
		return this.removalReason;
	}

	@Override
	public final void setRemoved(Entity.RemovalReason removalReason) {
		if (this.removalReason == null) {
			this.removalReason = removalReason;
		}

		if (this.removalReason.shouldDestroy()) {
			this.stopRiding();
		}

		this.getPassengers().forEach(Entity::stopRiding);
		this.levelCallback.onRemove(removalReason);
	}

	protected void unsetRemoved() {
		this.removalReason = null;
	}

	@Override
	public void setLevelCallback(EntityInLevelCallback entityInLevelCallback) {
		this.levelCallback = entityInLevelCallback;
	}

	@Override
	public boolean shouldBeSaved() {
		if (this.removalReason != null && !this.removalReason.shouldSave()) {
			return false;
		} else {
			return this.isPassenger() ? false : !this.isVehicle() || !this.hasExactlyOnePlayerPassenger();
		}
	}

	@Override
	public boolean isAlwaysTicking() {
		return false;
	}

	public boolean mayInteract(Level level, BlockPos blockPos) {
		return true;
	}

	public Level level() {
		return this.level;
	}

	protected void setLevel(Level level) {
		this.level = level;
	}

	public DamageSources damageSources() {
		return this.level().damageSources();
	}

	public RegistryAccess registryAccess() {
		return this.level().registryAccess();
	}

	protected void lerpPositionAndRotationStep(int i, double d, double e, double f, double g, double h) {
		double j = 1.0 / (double)i;
		double k = Mth.lerp(j, this.getX(), d);
		double l = Mth.lerp(j, this.getY(), e);
		double m = Mth.lerp(j, this.getZ(), f);
		float n = (float)Mth.rotLerp(j, (double)this.getYRot(), g);
		float o = (float)Mth.lerp(j, (double)this.getXRot(), h);
		this.setPos(k, l, m);
		this.setRot(n, o);
	}

	public RandomSource getRandom() {
		return this.random;
	}

	public Vec3 getKnownMovement() {
		if (this.getControllingPassenger() instanceof Player player && this.isAlive()) {
			return player.getKnownMovement();
		}

		return this.getDeltaMovement();
	}

	@Nullable
	public ItemStack getWeaponItem() {
		return null;
	}

	public Optional<ResourceKey<LootTable>> getLootTable() {
		return this.type.getDefaultLootTable();
	}

	@FunctionalInterface
	public interface MoveFunction {
		void accept(Entity entity, double d, double e, double f);
	}

	public static enum MovementEmission {
		NONE(false, false),
		SOUNDS(true, false),
		EVENTS(false, true),
		ALL(true, true);

		final boolean sounds;
		final boolean events;

		private MovementEmission(final boolean bl, final boolean bl2) {
			this.sounds = bl;
			this.events = bl2;
		}

		public boolean emitsAnything() {
			return this.events || this.sounds;
		}

		public boolean emitsEvents() {
			return this.events;
		}

		public boolean emitsSounds() {
			return this.sounds;
		}
	}

	public static enum RemovalReason {
		KILLED(true, false),
		DISCARDED(true, false),
		UNLOADED_TO_CHUNK(false, true),
		UNLOADED_WITH_PLAYER(false, false),
		CHANGED_DIMENSION(false, false);

		private final boolean destroy;
		private final boolean save;

		private RemovalReason(final boolean bl, final boolean bl2) {
			this.destroy = bl;
			this.save = bl2;
		}

		public boolean shouldDestroy() {
			return this.destroy;
		}

		public boolean shouldSave() {
			return this.save;
		}
	}
}
