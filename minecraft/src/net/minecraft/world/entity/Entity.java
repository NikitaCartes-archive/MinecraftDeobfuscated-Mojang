package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Entity implements Nameable, CommandSource {
	protected static final Logger LOGGER = LogManager.getLogger();
	private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
	private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
	private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(
		Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1)
	);
	private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
	private static double viewScale = 1.0;
	private final EntityType<?> type;
	private int id = ENTITY_COUNTER.incrementAndGet();
	public boolean blocksBuilding;
	private final List<Entity> passengers = Lists.<Entity>newArrayList();
	protected int boardingCooldown;
	@Nullable
	private Entity vehicle;
	public boolean forcedLoading;
	public Level level;
	public double xo;
	public double yo;
	public double zo;
	private Vec3 position;
	private BlockPos blockPosition;
	private Vec3 deltaMovement = Vec3.ZERO;
	public float yRot;
	public float xRot;
	public float yRotO;
	public float xRotO;
	private AABB bb = INITIAL_AABB;
	protected boolean onGround;
	public boolean horizontalCollision;
	public boolean verticalCollision;
	public boolean collision;
	public boolean hurtMarked;
	protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
	public boolean removed;
	public float walkDistO;
	public float walkDist;
	public float moveDist;
	public float fallDistance;
	private float nextStep = 1.0F;
	private float nextFlap = 1.0F;
	public double xOld;
	public double yOld;
	public double zOld;
	public float maxUpStep;
	public boolean noPhysics;
	public float pushthrough;
	protected final Random random = new Random();
	public int tickCount;
	private int remainingFireTicks = -this.getFireImmuneTicks();
	protected boolean wasInWater;
	protected double fluidHeight;
	protected boolean wasUnderWater;
	protected boolean isInLava;
	public int invulnerableTime;
	protected boolean firstTick = true;
	protected final SynchedEntityData entityData;
	protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(
		Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT
	);
	private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
	protected static final EntityDataAccessor<Pose> DATA_POSE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.POSE);
	public boolean inChunk;
	public int xChunk;
	public int yChunk;
	public int zChunk;
	public long xp;
	public long yp;
	public long zp;
	public boolean noCulling;
	public boolean hasImpulse;
	public int changingDimensionDelay;
	protected boolean isInsidePortal;
	protected int portalTime;
	public DimensionType dimension;
	protected BlockPos portalEntranceBlock;
	protected Vec3 portalEntranceOffset;
	protected Direction portalEntranceForwards;
	private boolean invulnerable;
	protected UUID uuid = Mth.createInsecureUUID(this.random);
	protected String stringUUID = this.uuid.toString();
	protected boolean glowing;
	private final Set<String> tags = Sets.<String>newHashSet();
	private boolean teleported;
	private final double[] pistonDeltas = new double[]{0.0, 0.0, 0.0};
	private long pistonDeltasGameTime;
	private EntityDimensions dimensions;
	private float eyeHeight;

	public Entity(EntityType<?> entityType, Level level) {
		this.type = entityType;
		this.level = level;
		this.dimensions = entityType.getDimensions();
		this.position = Vec3.ZERO;
		this.blockPosition = BlockPos.ZERO;
		this.setPos(0.0, 0.0, 0.0);
		if (level != null) {
			this.dimension = level.dimension.getType();
		}

		this.entityData = new SynchedEntityData(this);
		this.entityData.define(DATA_SHARED_FLAGS_ID, (byte)0);
		this.entityData.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
		this.entityData.define(DATA_CUSTOM_NAME_VISIBLE, false);
		this.entityData.define(DATA_CUSTOM_NAME, Optional.empty());
		this.entityData.define(DATA_SILENT, false);
		this.entityData.define(DATA_NO_GRAVITY, false);
		this.entityData.define(DATA_POSE, Pose.STANDING);
		this.defineSynchedData();
		this.eyeHeight = this.getEyeHeight(Pose.STANDING, this.dimensions);
	}

	@Environment(EnvType.CLIENT)
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

	public void setPacketCoordinates(double d, double e, double f) {
		this.xp = ClientboundMoveEntityPacket.entityToPacket(d);
		this.yp = ClientboundMoveEntityPacket.entityToPacket(e);
		this.zp = ClientboundMoveEntityPacket.entityToPacket(f);
	}

	public EntityType<?> getType() {
		return this.type;
	}

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
		this.remove();
	}

	protected abstract void defineSynchedData();

	public SynchedEntityData getEntityData() {
		return this.entityData;
	}

	public boolean equals(Object object) {
		return object instanceof Entity ? ((Entity)object).id == this.id : false;
	}

	public int hashCode() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	protected void resetPos() {
		if (this.level != null) {
			for (double d = this.getY(); d > 0.0 && d < 256.0; d++) {
				this.setPos(this.getX(), d, this.getZ());
				if (this.level.noCollision(this)) {
					break;
				}
			}

			this.setDeltaMovement(Vec3.ZERO);
			this.xRot = 0.0F;
		}
	}

	public void remove() {
		this.removed = true;
	}

	protected void setPose(Pose pose) {
		this.entityData.set(DATA_POSE, pose);
	}

	public Pose getPose() {
		return this.entityData.get(DATA_POSE);
	}

	public boolean closerThan(Entity entity, double d) {
		double e = entity.position.x - this.position.x;
		double f = entity.position.y - this.position.y;
		double g = entity.position.z - this.position.z;
		return e * e + f * f + g * g < d * d;
	}

	protected void setRot(float f, float g) {
		this.yRot = f % 360.0F;
		this.xRot = g % 360.0F;
	}

	public void setPos(double d, double e, double f) {
		this.setPosRaw(d, e, f);
		float g = this.dimensions.width / 2.0F;
		float h = this.dimensions.height;
		this.setBoundingBox(new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)h, f + (double)g));
	}

	protected void reapplyPosition() {
		this.setPos(this.position.x, this.position.y, this.position.z);
	}

	@Environment(EnvType.CLIENT)
	public void turn(double d, double e) {
		double f = e * 0.15;
		double g = d * 0.15;
		this.xRot = (float)((double)this.xRot + f);
		this.yRot = (float)((double)this.yRot + g);
		this.xRot = Mth.clamp(this.xRot, -90.0F, 90.0F);
		this.xRotO = (float)((double)this.xRotO + f);
		this.yRotO = (float)((double)this.yRotO + g);
		this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
		if (this.vehicle != null) {
			this.vehicle.onPassengerTurned(this);
		}
	}

	public void tick() {
		if (!this.level.isClientSide) {
			this.setSharedFlag(6, this.isGlowing());
		}

		this.baseTick();
	}

	public void baseTick() {
		this.level.getProfiler().push("entityBaseTick");
		if (this.isPassenger() && this.getVehicle().removed) {
			this.stopRiding();
		}

		if (this.boardingCooldown > 0) {
			this.boardingCooldown--;
		}

		this.walkDistO = this.walkDist;
		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
		this.handleNetherPortal();
		this.updateSprintingState();
		this.updateInWaterStateAndDoFluidPushing();
		this.updateUnderWaterState();
		this.updateSwimming();
		if (this.level.isClientSide) {
			this.clearFire();
		} else if (this.remainingFireTicks > 0) {
			if (this.fireImmune()) {
				this.remainingFireTicks -= 4;
				if (this.remainingFireTicks < 0) {
					this.clearFire();
				}
			} else {
				if (this.remainingFireTicks % 20 == 0) {
					this.hurt(DamageSource.ON_FIRE, 1.0F);
				}

				this.remainingFireTicks--;
			}
		}

		if (this.isInLava()) {
			this.lavaHurt();
			this.fallDistance *= 0.5F;
		}

		if (this.getY() < -64.0) {
			this.outOfWorld();
		}

		if (!this.level.isClientSide) {
			this.setSharedFlag(0, this.remainingFireTicks > 0);
		}

		this.firstTick = false;
		this.level.getProfiler().pop();
	}

	protected void processDimensionDelay() {
		if (this.changingDimensionDelay > 0) {
			this.changingDimensionDelay--;
		}
	}

	public int getPortalWaitTime() {
		return 1;
	}

	protected void lavaHurt() {
		if (!this.fireImmune()) {
			this.setSecondsOnFire(15);
			this.hurt(DamageSource.LAVA, 4.0F);
		}
	}

	public void setSecondsOnFire(int i) {
		int j = i * 20;
		if (this instanceof LivingEntity) {
			j = ProtectionEnchantment.getFireAfterDampener((LivingEntity)this, j);
		}

		if (this.remainingFireTicks < j) {
			this.remainingFireTicks = j;
		}
	}

	public void setRemainingFireTicks(int i) {
		this.remainingFireTicks = i;
	}

	public int getRemainingFireTicks() {
		return this.remainingFireTicks;
	}

	public void clearFire() {
		this.remainingFireTicks = 0;
	}

	protected void outOfWorld() {
		this.remove();
	}

	public boolean isFree(double d, double e, double f) {
		return this.isFree(this.getBoundingBox().move(d, e, f));
	}

	private boolean isFree(AABB aABB) {
		return this.level.noCollision(this, aABB) && !this.level.containsAnyLiquid(aABB);
	}

	public void setOnGround(boolean bl) {
		this.onGround = bl;
	}

	public boolean isOnGround() {
		return this.onGround;
	}

	public void move(MoverType moverType, Vec3 vec3) {
		if (this.noPhysics) {
			this.setBoundingBox(this.getBoundingBox().move(vec3));
			this.setLocationFromBoundingbox();
		} else {
			if (moverType == MoverType.PISTON) {
				vec3 = this.limitPistonMovement(vec3);
				if (vec3.equals(Vec3.ZERO)) {
					return;
				}
			}

			this.level.getProfiler().push("move");
			if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
				vec3 = vec3.multiply(this.stuckSpeedMultiplier);
				this.stuckSpeedMultiplier = Vec3.ZERO;
				this.setDeltaMovement(Vec3.ZERO);
			}

			vec3 = this.maybeBackOffFromEdge(vec3, moverType);
			Vec3 vec32 = this.collide(vec3);
			if (vec32.lengthSqr() > 1.0E-7) {
				this.setBoundingBox(this.getBoundingBox().move(vec32));
				this.setLocationFromBoundingbox();
			}

			this.level.getProfiler().pop();
			this.level.getProfiler().push("rest");
			this.horizontalCollision = !Mth.equal(vec3.x, vec32.x) || !Mth.equal(vec3.z, vec32.z);
			this.verticalCollision = vec3.y != vec32.y;
			this.onGround = this.verticalCollision && vec3.y < 0.0;
			this.collision = this.horizontalCollision || this.verticalCollision;
			BlockPos blockPos = this.getOnPos();
			BlockState blockState = this.level.getBlockState(blockPos);
			this.checkFallDamage(vec32.y, this.onGround, blockState, blockPos);
			Vec3 vec33 = this.getDeltaMovement();
			if (vec3.x != vec32.x) {
				this.setDeltaMovement(0.0, vec33.y, vec33.z);
			}

			if (vec3.z != vec32.z) {
				this.setDeltaMovement(vec33.x, vec33.y, 0.0);
			}

			Block block = blockState.getBlock();
			if (vec3.y != vec32.y) {
				block.updateEntityAfterFallOn(this.level, this);
			}

			if (this.onGround && !this.isSteppingCarefully()) {
				block.stepOn(this.level, blockPos, this);
			}

			if (this.isMovementNoisy() && !this.isPassenger()) {
				double d = vec32.x;
				double e = vec32.y;
				double f = vec32.z;
				if (!block.is(BlockTags.CLIMBABLE)) {
					e = 0.0;
				}

				this.walkDist = (float)((double)this.walkDist + (double)Mth.sqrt(getHorizontalDistanceSqr(vec32)) * 0.6);
				this.moveDist = (float)((double)this.moveDist + (double)Mth.sqrt(d * d + e * e + f * f) * 0.6);
				if (this.moveDist > this.nextStep && !blockState.isAir()) {
					this.nextStep = this.nextStep();
					if (this.isInWater()) {
						Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
						float g = entity == this ? 0.35F : 0.4F;
						Vec3 vec34 = entity.getDeltaMovement();
						float h = Mth.sqrt(vec34.x * vec34.x * 0.2F + vec34.y * vec34.y + vec34.z * vec34.z * 0.2F) * g;
						if (h > 1.0F) {
							h = 1.0F;
						}

						this.playSwimSound(h);
					} else {
						this.playStepSound(blockPos, blockState);
					}
				} else if (this.moveDist > this.nextFlap && this.makeFlySound() && blockState.isAir()) {
					this.nextFlap = this.playFlySound(this.moveDist);
				}
			}

			try {
				this.isInLava = false;
				this.checkInsideBlocks();
			} catch (Throwable var18) {
				CrashReport crashReport = CrashReport.forThrowable(var18, "Checking entity block collision");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being checked for collision");
				this.fillCrashReportCategory(crashReportCategory);
				throw new ReportedException(crashReport);
			}

			this.setDeltaMovement(this.getDeltaMovement().multiply((double)this.getBlockSpeedFactor(), 1.0, (double)this.getBlockSpeedFactor()));
			if (!this.level.containsFireBlock(this.getBoundingBox().deflate(0.001)) && this.remainingFireTicks <= 0) {
				this.remainingFireTicks = -this.getFireImmuneTicks();
			}

			if (this.isInWaterRainOrBubble() && this.isOnFire()) {
				this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
				this.remainingFireTicks = -this.getFireImmuneTicks();
			}

			this.level.getProfiler().pop();
		}
	}

	protected BlockPos getOnPos() {
		int i = Mth.floor(this.position.x);
		int j = Mth.floor(this.position.y - 0.2F);
		int k = Mth.floor(this.position.z);
		BlockPos blockPos = new BlockPos(i, j, k);
		if (this.level.getBlockState(blockPos).isAir()) {
			BlockPos blockPos2 = blockPos.below();
			BlockState blockState = this.level.getBlockState(blockPos2);
			Block block = blockState.getBlock();
			if (block.is(BlockTags.FENCES) || block.is(BlockTags.WALLS) || block instanceof FenceGateBlock) {
				return blockPos2;
			}
		}

		return blockPos;
	}

	protected float getBlockJumpFactor() {
		float f = this.level.getBlockState(this.blockPosition()).getBlock().getJumpFactor();
		float g = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
		return (double)f == 1.0 ? g : f;
	}

	protected float getBlockSpeedFactor() {
		Block block = this.level.getBlockState(this.blockPosition()).getBlock();
		float f = block.getSpeedFactor();
		if (block != Blocks.WATER && block != Blocks.BUBBLE_COLUMN) {
			return (double)f == 1.0 ? this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : f;
		} else {
			return f;
		}
	}

	protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
		return new BlockPos(this.position.x, this.getBoundingBox().minY - 0.5000001, this.position.z);
	}

	protected Vec3 maybeBackOffFromEdge(Vec3 vec3, MoverType moverType) {
		return vec3;
	}

	protected Vec3 limitPistonMovement(Vec3 vec3) {
		if (vec3.lengthSqr() <= 1.0E-7) {
			return vec3;
		} else {
			long l = this.level.getGameTime();
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
		CollisionContext collisionContext = CollisionContext.of(this);
		VoxelShape voxelShape = this.level.getWorldBorder().getCollisionShape();
		Stream<VoxelShape> stream = Shapes.joinIsNotEmpty(voxelShape, Shapes.create(aABB.deflate(1.0E-7)), BooleanOp.AND) ? Stream.empty() : Stream.of(voxelShape);
		Stream<VoxelShape> stream2 = this.level.getEntityCollisions(this, aABB.expandTowards(vec3), ImmutableSet.of());
		RewindableStream<VoxelShape> rewindableStream = new RewindableStream<>(Stream.concat(stream2, stream));
		Vec3 vec32 = vec3.lengthSqr() == 0.0 ? vec3 : collideBoundingBoxHeuristically(this, vec3, aABB, this.level, collisionContext, rewindableStream);
		boolean bl = vec3.x != vec32.x;
		boolean bl2 = vec3.y != vec32.y;
		boolean bl3 = vec3.z != vec32.z;
		boolean bl4 = this.onGround || bl2 && vec3.y < 0.0;
		if (this.maxUpStep > 0.0F && bl4 && (bl || bl3)) {
			Vec3 vec33 = collideBoundingBoxHeuristically(this, new Vec3(vec3.x, (double)this.maxUpStep, vec3.z), aABB, this.level, collisionContext, rewindableStream);
			Vec3 vec34 = collideBoundingBoxHeuristically(
				this, new Vec3(0.0, (double)this.maxUpStep, 0.0), aABB.expandTowards(vec3.x, 0.0, vec3.z), this.level, collisionContext, rewindableStream
			);
			if (vec34.y < (double)this.maxUpStep) {
				Vec3 vec35 = collideBoundingBoxHeuristically(this, new Vec3(vec3.x, 0.0, vec3.z), aABB.move(vec34), this.level, collisionContext, rewindableStream)
					.add(vec34);
				if (getHorizontalDistanceSqr(vec35) > getHorizontalDistanceSqr(vec33)) {
					vec33 = vec35;
				}
			}

			if (getHorizontalDistanceSqr(vec33) > getHorizontalDistanceSqr(vec32)) {
				return vec33.add(
					collideBoundingBoxHeuristically(this, new Vec3(0.0, -vec33.y + vec3.y, 0.0), aABB.move(vec33), this.level, collisionContext, rewindableStream)
				);
			}
		}

		return vec32;
	}

	public static double getHorizontalDistanceSqr(Vec3 vec3) {
		return vec3.x * vec3.x + vec3.z * vec3.z;
	}

	public static Vec3 collideBoundingBoxHeuristically(
		@Nullable Entity entity, Vec3 vec3, AABB aABB, Level level, CollisionContext collisionContext, RewindableStream<VoxelShape> rewindableStream
	) {
		boolean bl = vec3.x == 0.0;
		boolean bl2 = vec3.y == 0.0;
		boolean bl3 = vec3.z == 0.0;
		if ((!bl || !bl2) && (!bl || !bl3) && (!bl2 || !bl3)) {
			RewindableStream<VoxelShape> rewindableStream2 = new RewindableStream<>(
				Stream.concat(rewindableStream.getStream(), level.getBlockCollisions(entity, aABB.expandTowards(vec3)))
			);
			return collideBoundingBoxLegacy(vec3, aABB, rewindableStream2);
		} else {
			return collideBoundingBox(vec3, aABB, level, collisionContext, rewindableStream);
		}
	}

	public static Vec3 collideBoundingBoxLegacy(Vec3 vec3, AABB aABB, RewindableStream<VoxelShape> rewindableStream) {
		double d = vec3.x;
		double e = vec3.y;
		double f = vec3.z;
		if (e != 0.0) {
			e = Shapes.collide(Direction.Axis.Y, aABB, rewindableStream.getStream(), e);
			if (e != 0.0) {
				aABB = aABB.move(0.0, e, 0.0);
			}
		}

		boolean bl = Math.abs(d) < Math.abs(f);
		if (bl && f != 0.0) {
			f = Shapes.collide(Direction.Axis.Z, aABB, rewindableStream.getStream(), f);
			if (f != 0.0) {
				aABB = aABB.move(0.0, 0.0, f);
			}
		}

		if (d != 0.0) {
			d = Shapes.collide(Direction.Axis.X, aABB, rewindableStream.getStream(), d);
			if (!bl && d != 0.0) {
				aABB = aABB.move(d, 0.0, 0.0);
			}
		}

		if (!bl && f != 0.0) {
			f = Shapes.collide(Direction.Axis.Z, aABB, rewindableStream.getStream(), f);
		}

		return new Vec3(d, e, f);
	}

	public static Vec3 collideBoundingBox(
		Vec3 vec3, AABB aABB, LevelReader levelReader, CollisionContext collisionContext, RewindableStream<VoxelShape> rewindableStream
	) {
		double d = vec3.x;
		double e = vec3.y;
		double f = vec3.z;
		if (e != 0.0) {
			e = Shapes.collide(Direction.Axis.Y, aABB, levelReader, e, collisionContext, rewindableStream.getStream());
			if (e != 0.0) {
				aABB = aABB.move(0.0, e, 0.0);
			}
		}

		boolean bl = Math.abs(d) < Math.abs(f);
		if (bl && f != 0.0) {
			f = Shapes.collide(Direction.Axis.Z, aABB, levelReader, f, collisionContext, rewindableStream.getStream());
			if (f != 0.0) {
				aABB = aABB.move(0.0, 0.0, f);
			}
		}

		if (d != 0.0) {
			d = Shapes.collide(Direction.Axis.X, aABB, levelReader, d, collisionContext, rewindableStream.getStream());
			if (!bl && d != 0.0) {
				aABB = aABB.move(d, 0.0, 0.0);
			}
		}

		if (!bl && f != 0.0) {
			f = Shapes.collide(Direction.Axis.Z, aABB, levelReader, f, collisionContext, rewindableStream.getStream());
		}

		return new Vec3(d, e, f);
	}

	protected float nextStep() {
		return (float)((int)this.moveDist + 1);
	}

	public void setLocationFromBoundingbox() {
		AABB aABB = this.getBoundingBox();
		this.setPosRaw((aABB.minX + aABB.maxX) / 2.0, aABB.minY, (aABB.minZ + aABB.maxZ) / 2.0);
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

	protected void checkInsideBlocks() {
		AABB aABB = this.getBoundingBox();
		BlockPos blockPos = new BlockPos(aABB.minX + 0.001, aABB.minY + 0.001, aABB.minZ + 0.001);
		BlockPos blockPos2 = new BlockPos(aABB.maxX - 0.001, aABB.maxY - 0.001, aABB.maxZ - 0.001);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		if (this.level.hasChunksAt(blockPos, blockPos2)) {
			for (int i = blockPos.getX(); i <= blockPos2.getX(); i++) {
				for (int j = blockPos.getY(); j <= blockPos2.getY(); j++) {
					for (int k = blockPos.getZ(); k <= blockPos2.getZ(); k++) {
						mutableBlockPos.set(i, j, k);
						BlockState blockState = this.level.getBlockState(mutableBlockPos);

						try {
							blockState.entityInside(this.level, mutableBlockPos, this);
							this.onInsideBlock(blockState);
						} catch (Throwable var12) {
							CrashReport crashReport = CrashReport.forThrowable(var12, "Colliding entity with block");
							CrashReportCategory crashReportCategory = crashReport.addCategory("Block being collided with");
							CrashReportCategory.populateBlockDetails(crashReportCategory, mutableBlockPos, blockState);
							throw new ReportedException(crashReport);
						}
					}
				}
			}
		}
	}

	protected void onInsideBlock(BlockState blockState) {
	}

	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		if (!blockState.getMaterial().isLiquid()) {
			BlockState blockState2 = this.level.getBlockState(blockPos.above());
			SoundType soundType = blockState2.getBlock() == Blocks.SNOW ? blockState2.getSoundType() : blockState.getSoundType();
			this.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15F, soundType.getPitch());
		}
	}

	protected void playSwimSound(float f) {
		this.playSound(this.getSwimSound(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
	}

	protected float playFlySound(float f) {
		return 0.0F;
	}

	protected boolean makeFlySound() {
		return false;
	}

	public void playSound(SoundEvent soundEvent, float f, float g) {
		if (!this.isSilent()) {
			this.level.playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g);
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

	protected boolean isMovementNoisy() {
		return true;
	}

	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		if (bl) {
			if (this.fallDistance > 0.0F) {
				blockState.getBlock().fallOn(this.level, blockPos, this, this.fallDistance);
			}

			this.fallDistance = 0.0F;
		} else if (d < 0.0) {
			this.fallDistance = (float)((double)this.fallDistance - d);
		}
	}

	@Nullable
	public AABB getCollideBox() {
		return null;
	}

	public boolean fireImmune() {
		return this.getType().fireImmune();
	}

	public boolean causeFallDamage(float f, float g) {
		if (this.isVehicle()) {
			for (Entity entity : this.getPassengers()) {
				entity.causeFallDamage(f, g);
			}
		}

		return false;
	}

	public boolean isInWater() {
		return this.wasInWater;
	}

	private boolean isInRain() {
		BlockPos blockPos = this.blockPosition();
		return this.level.isRainingAt(blockPos) || this.level.isRainingAt(blockPos.offset(0.0, (double)this.dimensions.height, 0.0));
	}

	private boolean isInBubbleColumn() {
		return this.level.getBlockState(this.blockPosition()).getBlock() == Blocks.BUBBLE_COLUMN;
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

	public boolean isUnderWater() {
		return this.wasUnderWater && this.isInWater();
	}

	public void updateSwimming() {
		if (this.isSwimming()) {
			this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
		} else {
			this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger());
		}
	}

	protected boolean updateInWaterStateAndDoFluidPushing() {
		this.updateInWaterStateAndDoWaterCurrentPushing();
		if (this.isInWater()) {
			return true;
		} else {
			double d = this.level.getDimension().isHasCeiling() ? 0.007 : 0.0023333333333333335;
			return this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, d);
		}
	}

	void updateInWaterStateAndDoWaterCurrentPushing() {
		if (this.getVehicle() instanceof Boat) {
			this.wasInWater = false;
		} else if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014)) {
			if (!this.wasInWater && !this.firstTick) {
				this.doWaterSplashEffect();
			}

			this.fallDistance = 0.0F;
			this.wasInWater = true;
			this.clearFire();
		} else {
			this.wasInWater = false;
		}
	}

	private void updateUnderWaterState() {
		this.wasUnderWater = this.isUnderLiquid(FluidTags.WATER, true);
	}

	protected void doWaterSplashEffect() {
		Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
		float f = entity == this ? 0.2F : 0.9F;
		Vec3 vec3 = entity.getDeltaMovement();
		float g = Mth.sqrt(vec3.x * vec3.x * 0.2F + vec3.y * vec3.y + vec3.z * vec3.z * 0.2F) * f;
		if (g > 1.0F) {
			g = 1.0F;
		}

		if ((double)g < 0.25) {
			this.playSound(this.getSwimSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
		} else {
			this.playSound(this.getSwimHighSpeedSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
		}

		float h = (float)Mth.floor(this.getY());

		for (int i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; i++) {
			float j = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
			float k = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
			this.level
				.addParticle(
					ParticleTypes.BUBBLE,
					this.getX() + (double)j,
					(double)(h + 1.0F),
					this.getZ() + (double)k,
					vec3.x,
					vec3.y - (double)(this.random.nextFloat() * 0.2F),
					vec3.z
				);
		}

		for (int i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; i++) {
			float j = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
			float k = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
			this.level.addParticle(ParticleTypes.SPLASH, this.getX() + (double)j, (double)(h + 1.0F), this.getZ() + (double)k, vec3.x, vec3.y, vec3.z);
		}
	}

	protected BlockState getBlockStateOn() {
		return this.level.getBlockState(this.getOnPos());
	}

	public void updateSprintingState() {
		if (this.isSprinting() && !this.isInWater()) {
			this.doSprintParticleEffect();
		}
	}

	protected void doSprintParticleEffect() {
		int i = Mth.floor(this.getX());
		int j = Mth.floor(this.getY() - 0.2F);
		int k = Mth.floor(this.getZ());
		BlockPos blockPos = new BlockPos(i, j, k);
		BlockState blockState = this.level.getBlockState(blockPos);
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			Vec3 vec3 = this.getDeltaMovement();
			this.level
				.addParticle(
					new BlockParticleOption(ParticleTypes.BLOCK, blockState),
					this.getX() + ((double)this.random.nextFloat() - 0.5) * (double)this.dimensions.width,
					this.getY() + 0.1,
					this.getZ() + ((double)this.random.nextFloat() - 0.5) * (double)this.dimensions.width,
					vec3.x * -4.0,
					1.5,
					vec3.z * -4.0
				);
		}
	}

	public boolean isUnderLiquid(Tag<Fluid> tag) {
		return this.isUnderLiquid(tag, false);
	}

	public boolean isUnderLiquid(Tag<Fluid> tag, boolean bl) {
		if (this.getVehicle() instanceof Boat) {
			return false;
		} else {
			double d = this.getEyeY();
			BlockPos blockPos = new BlockPos(this.getX(), d, this.getZ());
			if (bl && !this.level.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
				return false;
			} else {
				FluidState fluidState = this.level.getFluidState(blockPos);
				return fluidState.is(tag) && d < (double)((float)blockPos.getY() + fluidState.getHeight(this.level, blockPos) + 0.11111111F);
			}
		}
	}

	public void setInLava() {
		this.isInLava = true;
	}

	public boolean isInLava() {
		return this.isInLava;
	}

	public void moveRelative(float f, Vec3 vec3) {
		Vec3 vec32 = getInputVector(vec3, f, this.yRot);
		this.setDeltaMovement(this.getDeltaMovement().add(vec32));
	}

	private static Vec3 getInputVector(Vec3 vec3, float f, float g) {
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

	public float getBrightness() {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.getX(), 0.0, this.getZ());
		if (this.level.hasChunkAt(mutableBlockPos)) {
			mutableBlockPos.setY(Mth.floor(this.getEyeY()));
			return this.level.getBrightness(mutableBlockPos);
		} else {
			return 0.0F;
		}
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public void absMoveTo(double d, double e, double f, float g, float h) {
		double i = Mth.clamp(d, -3.0E7, 3.0E7);
		double j = Mth.clamp(f, -3.0E7, 3.0E7);
		this.xo = i;
		this.yo = e;
		this.zo = j;
		this.setPos(i, e, j);
		this.yRot = g % 360.0F;
		this.xRot = Mth.clamp(h, -90.0F, 90.0F) % 360.0F;
		this.yRotO = this.yRot;
		this.xRotO = this.xRot;
	}

	public void moveTo(BlockPos blockPos, float f, float g) {
		this.moveTo((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, f, g);
	}

	public void moveTo(double d, double e, double f, float g, float h) {
		this.setPosAndOldPos(d, e, f);
		this.yRot = g;
		this.xRot = h;
		this.reapplyPosition();
	}

	public void setPosAndOldPos(double d, double e, double f) {
		this.setPosRaw(d, e, f);
		this.xo = d;
		this.yo = e;
		this.zo = f;
		this.xOld = d;
		this.yOld = e;
		this.zOld = f;
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
					f = (double)Mth.sqrt(f);
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
					d *= (double)(1.0F - this.pushthrough);
					e *= (double)(1.0F - this.pushthrough);
					if (!this.isVehicle()) {
						this.push(-d, 0.0, -e);
					}

					if (!entity.isVehicle()) {
						entity.push(d, 0.0, e);
					}
				}
			}
		}
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

	public float getViewXRot(float f) {
		return f == 1.0F ? this.xRot : Mth.lerp(f, this.xRotO, this.xRot);
	}

	public float getViewYRot(float f) {
		return f == 1.0F ? this.yRot : Mth.lerp(f, this.yRotO, this.yRot);
	}

	protected final Vec3 calculateViewVector(float f, float g) {
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

	public final Vec3 getEyePosition(float f) {
		if (f == 1.0F) {
			return new Vec3(this.getX(), this.getEyeY(), this.getZ());
		} else {
			double d = Mth.lerp((double)f, this.xo, this.getX());
			double e = Mth.lerp((double)f, this.yo, this.getY()) + (double)this.getEyeHeight();
			double g = Mth.lerp((double)f, this.zo, this.getZ());
			return new Vec3(d, e, g);
		}
	}

	public HitResult pick(double d, float f, boolean bl) {
		Vec3 vec3 = this.getEyePosition(f);
		Vec3 vec32 = this.getViewVector(f);
		Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
		return this.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
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

	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double d, double e, double f) {
		double g = this.getX() - d;
		double h = this.getY() - e;
		double i = this.getZ() - f;
		double j = g * g + h * h + i * i;
		return this.shouldRenderAtSqrDistance(j);
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize();
		if (Double.isNaN(e)) {
			e = 1.0;
		}

		e *= 64.0 * viewScale;
		return d < e * e;
	}

	public boolean saveAsPassenger(CompoundTag compoundTag) {
		String string = this.getEncodeId();
		if (!this.removed && string != null) {
			compoundTag.putString("id", string);
			this.saveWithoutId(compoundTag);
			return true;
		} else {
			return false;
		}
	}

	public boolean save(CompoundTag compoundTag) {
		return this.isPassenger() ? false : this.saveAsPassenger(compoundTag);
	}

	public CompoundTag saveWithoutId(CompoundTag compoundTag) {
		try {
			compoundTag.put("Pos", this.newDoubleList(this.getX(), this.getY(), this.getZ()));
			Vec3 vec3 = this.getDeltaMovement();
			compoundTag.put("Motion", this.newDoubleList(vec3.x, vec3.y, vec3.z));
			compoundTag.put("Rotation", this.newFloatList(this.yRot, this.xRot));
			compoundTag.putFloat("FallDistance", this.fallDistance);
			compoundTag.putShort("Fire", (short)this.remainingFireTicks);
			compoundTag.putShort("Air", (short)this.getAirSupply());
			compoundTag.putBoolean("OnGround", this.onGround);
			compoundTag.putInt("Dimension", this.dimension.getId());
			compoundTag.putBoolean("Invulnerable", this.invulnerable);
			compoundTag.putInt("PortalCooldown", this.changingDimensionDelay);
			compoundTag.putUUID("UUID", this.getUUID());
			Component component = this.getCustomName();
			if (component != null) {
				compoundTag.putString("CustomName", Component.Serializer.toJson(component));
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

			if (this.glowing) {
				compoundTag.putBoolean("Glowing", this.glowing);
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
		} catch (Throwable var8) {
			CrashReport crashReport = CrashReport.forThrowable(var8, "Saving entity NBT");
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
			this.setPosAndOldPos(listTag.getDouble(0), listTag.getDouble(1), listTag.getDouble(2));
			this.yRot = listTag3.getFloat(0);
			this.xRot = listTag3.getFloat(1);
			this.yRotO = this.yRot;
			this.xRotO = this.xRot;
			this.setYHeadRot(this.yRot);
			this.setYBodyRot(this.yRot);
			this.fallDistance = compoundTag.getFloat("FallDistance");
			this.remainingFireTicks = compoundTag.getShort("Fire");
			this.setAirSupply(compoundTag.getShort("Air"));
			this.onGround = compoundTag.getBoolean("OnGround");
			if (compoundTag.contains("Dimension")) {
				this.dimension = DimensionType.getById(compoundTag.getInt("Dimension"));
			}

			this.invulnerable = compoundTag.getBoolean("Invulnerable");
			this.changingDimensionDelay = compoundTag.getInt("PortalCooldown");
			if (compoundTag.hasUUID("UUID")) {
				this.uuid = compoundTag.getUUID("UUID");
				this.stringUUID = this.uuid.toString();
			}

			if (!Double.isFinite(this.getX()) || !Double.isFinite(this.getY()) || !Double.isFinite(this.getZ())) {
				throw new IllegalStateException("Entity has invalid position");
			} else if (Double.isFinite((double)this.yRot) && Double.isFinite((double)this.xRot)) {
				this.reapplyPosition();
				this.setRot(this.yRot, this.xRot);
				if (compoundTag.contains("CustomName", 8)) {
					this.setCustomName(Component.Serializer.fromJson(compoundTag.getString("CustomName")));
				}

				this.setCustomNameVisible(compoundTag.getBoolean("CustomNameVisible"));
				this.setSilent(compoundTag.getBoolean("Silent"));
				this.setNoGravity(compoundTag.getBoolean("NoGravity"));
				this.setGlowing(compoundTag.getBoolean("Glowing"));
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
		} catch (Throwable var14) {
			CrashReport crashReport = CrashReport.forThrowable(var14, "Loading entity NBT");
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
		} else if (this.level.isClientSide) {
			return null;
		} else {
			ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), this.getY() + (double)f, this.getZ(), itemStack);
			itemEntity.setDefaultPickUpDelay();
			this.level.addFreshEntity(itemEntity);
			return itemEntity;
		}
	}

	public boolean isAlive() {
		return !this.removed;
	}

	public boolean isInWall() {
		if (this.noPhysics) {
			return false;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 8; i++) {
				int j = Mth.floor(this.getY() + (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)this.eyeHeight);
				int k = Mth.floor(this.getX() + (double)(((float)((i >> 1) % 2) - 0.5F) * this.dimensions.width * 0.8F));
				int l = Mth.floor(this.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * this.dimensions.width * 0.8F));
				if (mutableBlockPos.getX() != k || mutableBlockPos.getY() != j || mutableBlockPos.getZ() != l) {
					mutableBlockPos.set(k, j, l);
					if (this.level.getBlockState(mutableBlockPos).isSuffocating(this.level, mutableBlockPos)) {
						return true;
					}
				}
			}

			return false;
		}
	}

	public boolean interact(Player player, InteractionHand interactionHand) {
		return false;
	}

	@Nullable
	public AABB getCollideAgainstBox(Entity entity) {
		return null;
	}

	public void rideTick() {
		this.setDeltaMovement(Vec3.ZERO);
		this.tick();
		if (this.isPassenger()) {
			this.getVehicle().positionRider(this);
		}
	}

	public void positionRider(Entity entity) {
		this.positionRider(entity, Entity::setPos);
	}

	public void positionRider(Entity entity, Entity.MoveCallback moveCallback) {
		if (this.hasPassenger(entity)) {
			moveCallback.accept(entity, this.getX(), this.getY() + this.getRideHeight() + entity.getRidingHeight(), this.getZ());
		}
	}

	@Environment(EnvType.CLIENT)
	public void onPassengerTurned(Entity entity) {
	}

	public double getRidingHeight() {
		return 0.0;
	}

	public double getRideHeight() {
		return (double)this.dimensions.height * 0.75;
	}

	public boolean startRiding(Entity entity) {
		return this.startRiding(entity, false);
	}

	@Environment(EnvType.CLIENT)
	public boolean showVehicleHealth() {
		return this instanceof LivingEntity;
	}

	public boolean startRiding(Entity entity, boolean bl) {
		for (Entity entity2 = entity; entity2.vehicle != null; entity2 = entity2.vehicle) {
			if (entity2.vehicle == this) {
				return false;
			}
		}

		if (bl || this.canRide(entity) && entity.canAddPassenger(this)) {
			if (this.isPassenger()) {
				this.stopRiding();
			}

			this.vehicle = entity;
			this.vehicle.addPassenger(this);
			return true;
		} else {
			return false;
		}
	}

	protected boolean canRide(Entity entity) {
		return this.boardingCooldown <= 0;
	}

	protected boolean canEnterPose(Pose pose) {
		return this.level.noCollision(this, this.getBoundingBoxForPose(pose));
	}

	public void ejectPassengers() {
		for (int i = this.passengers.size() - 1; i >= 0; i--) {
			((Entity)this.passengers.get(i)).stopRiding();
		}
	}

	public void stopRiding() {
		if (this.vehicle != null) {
			Entity entity = this.vehicle;
			this.vehicle = null;
			entity.removePassenger(this);
		}
	}

	protected void addPassenger(Entity entity) {
		if (entity.getVehicle() != this) {
			throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
		} else {
			if (!this.level.isClientSide && entity instanceof Player && !(this.getControllingPassenger() instanceof Player)) {
				this.passengers.add(0, entity);
			} else {
				this.passengers.add(entity);
			}
		}
	}

	protected void removePassenger(Entity entity) {
		if (entity.getVehicle() == this) {
			throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
		} else {
			this.passengers.remove(entity);
			entity.boardingCooldown = 60;
		}
	}

	protected boolean canAddPassenger(Entity entity) {
		return this.getPassengers().size() < 1;
	}

	@Environment(EnvType.CLIENT)
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		this.setPos(d, e, f);
		this.setRot(g, h);
	}

	@Environment(EnvType.CLIENT)
	public void lerpHeadTo(float f, int i) {
		this.setYHeadRot(f);
	}

	public float getPickRadius() {
		return 0.0F;
	}

	public Vec3 getLookAngle() {
		return this.calculateViewVector(this.xRot, this.yRot);
	}

	public Vec2 getRotationVector() {
		return new Vec2(this.xRot, this.yRot);
	}

	@Environment(EnvType.CLIENT)
	public Vec3 getForward() {
		return Vec3.directionFromRotation(this.getRotationVector());
	}

	public void handleInsidePortal(BlockPos blockPos) {
		if (this.changingDimensionDelay > 0) {
			this.changingDimensionDelay = this.getDimensionChangingDelay();
		} else {
			if (!this.level.isClientSide && !blockPos.equals(this.portalEntranceBlock)) {
				this.portalEntranceBlock = new BlockPos(blockPos);
				BlockPattern.BlockPatternMatch blockPatternMatch = NetherPortalBlock.getPortalShape(this.level, this.portalEntranceBlock);
				double d = blockPatternMatch.getForwards().getAxis() == Direction.Axis.X
					? (double)blockPatternMatch.getFrontTopLeft().getZ()
					: (double)blockPatternMatch.getFrontTopLeft().getX();
				double e = Math.abs(
					Mth.pct(
						(blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? this.getZ() : this.getX())
							- (double)(blockPatternMatch.getForwards().getClockWise().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 1 : 0),
						d,
						d - (double)blockPatternMatch.getWidth()
					)
				);
				double f = Mth.pct(
					this.getY() - 1.0,
					(double)blockPatternMatch.getFrontTopLeft().getY(),
					(double)(blockPatternMatch.getFrontTopLeft().getY() - blockPatternMatch.getHeight())
				);
				this.portalEntranceOffset = new Vec3(e, f, 0.0);
				this.portalEntranceForwards = blockPatternMatch.getForwards();
			}

			this.isInsidePortal = true;
		}
	}

	protected void handleNetherPortal() {
		if (this.level instanceof ServerLevel) {
			int i = this.getPortalWaitTime();
			if (this.isInsidePortal) {
				if (this.level.getServer().isNetherEnabled() && !this.isPassenger() && this.portalTime++ >= i) {
					this.level.getProfiler().push("portal");
					this.portalTime = i;
					this.changingDimensionDelay = this.getDimensionChangingDelay();
					this.changeDimension(this.level.dimension.getType() == DimensionType.NETHER ? DimensionType.OVERWORLD : DimensionType.NETHER);
					this.level.getProfiler().pop();
				}

				this.isInsidePortal = false;
			} else {
				if (this.portalTime > 0) {
					this.portalTime -= 4;
				}

				if (this.portalTime < 0) {
					this.portalTime = 0;
				}
			}

			this.processDimensionDelay();
		}
	}

	public int getDimensionChangingDelay() {
		return 300;
	}

	@Environment(EnvType.CLIENT)
	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
	}

	@Environment(EnvType.CLIENT)
	public void handleEntityEvent(byte b) {
		switch (b) {
			case 53:
				HoneyBlock.showSlideParticles(this);
		}
	}

	@Environment(EnvType.CLIENT)
	public void animateHurt() {
	}

	public Iterable<ItemStack> getHandSlots() {
		return EMPTY_LIST;
	}

	public Iterable<ItemStack> getArmorSlots() {
		return EMPTY_LIST;
	}

	public Iterable<ItemStack> getAllSlots() {
		return Iterables.concat(this.getHandSlots(), this.getArmorSlots());
	}

	public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
	}

	public boolean isOnFire() {
		boolean bl = this.level != null && this.level.isClientSide;
		return !this.fireImmune() && (this.remainingFireTicks > 0 || bl && this.getSharedFlag(0));
	}

	public boolean isPassenger() {
		return this.getVehicle() != null;
	}

	public boolean isVehicle() {
		return !this.getPassengers().isEmpty();
	}

	public boolean rideableUnderWater() {
		return true;
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
		return this.getPose() == Pose.CROUCHING;
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
		return this.getPose() == Pose.SWIMMING;
	}

	@Environment(EnvType.CLIENT)
	public boolean isVisuallyCrawling() {
		return this.isVisuallySwimming() && !this.isInWater();
	}

	public void setSwimming(boolean bl) {
		this.setSharedFlag(4, bl);
	}

	public boolean isGlowing() {
		return this.glowing || this.level.isClientSide && this.getSharedFlag(6);
	}

	public void setGlowing(boolean bl) {
		this.glowing = bl;
		if (!this.level.isClientSide) {
			this.setSharedFlag(6, this.glowing);
		}
	}

	public boolean isInvisible() {
		return this.getSharedFlag(5);
	}

	@Environment(EnvType.CLIENT)
	public boolean isInvisibleTo(Player player) {
		if (player.isSpectator()) {
			return false;
		} else {
			Team team = this.getTeam();
			return team != null && player != null && player.getTeam() == team && team.canSeeFriendlyInvisibles() ? false : this.isInvisible();
		}
	}

	@Nullable
	public Team getTeam() {
		return this.level.getScoreboard().getPlayersTeam(this.getScoreboardName());
	}

	public boolean isAlliedTo(Entity entity) {
		return this.isAlliedTo(entity.getTeam());
	}

	public boolean isAlliedTo(Team team) {
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

	public void thunderHit(LightningBolt lightningBolt) {
		this.remainingFireTicks++;
		if (this.remainingFireTicks == 0) {
			this.setSecondsOnFire(8);
		}

		this.hurt(DamageSource.LIGHTNING_BOLT, 5.0F);
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
		this.fallDistance = 0.0F;
	}

	public void killed(LivingEntity livingEntity) {
	}

	protected void checkInBlock(double d, double e, double f) {
		BlockPos blockPos = new BlockPos(d, e, f);
		Vec3 vec3 = new Vec3(d - (double)blockPos.getX(), e - (double)blockPos.getY(), f - (double)blockPos.getZ());
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Direction direction = Direction.UP;
		double g = Double.MAX_VALUE;

		for (Direction direction2 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
			mutableBlockPos.setWithOffset(blockPos, direction2);
			if (!this.level.getBlockState(mutableBlockPos).isCollisionShapeFullBlock(this.level, mutableBlockPos)) {
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
		this.fallDistance = 0.0F;
		this.stuckSpeedMultiplier = vec3;
	}

	private static void removeAction(Component component) {
		component.withStyle(style -> style.setClickEvent(null)).getSiblings().forEach(Entity::removeAction);
	}

	@Override
	public Component getName() {
		Component component = this.getCustomName();
		if (component != null) {
			Component component2 = component.deepCopy();
			removeAction(component2);
			return component2;
		} else {
			return this.getTypeName();
		}
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
		return String.format(
			Locale.ROOT,
			"%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]",
			this.getClass().getSimpleName(),
			this.getName().getContents(),
			this.id,
			this.level == null ? "~NULL~" : this.level.getLevelData().getLevelName(),
			this.getX(),
			this.getY(),
			this.getZ()
		);
	}

	public boolean isInvulnerableTo(DamageSource damageSource) {
		return this.invulnerable && damageSource != DamageSource.OUT_OF_WORLD && !damageSource.isCreativePlayer();
	}

	public boolean isInvulnerable() {
		return this.invulnerable;
	}

	public void setInvulnerable(boolean bl) {
		this.invulnerable = bl;
	}

	public void copyPosition(Entity entity) {
		this.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
	}

	public void restoreFrom(Entity entity) {
		CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
		compoundTag.remove("Dimension");
		this.load(compoundTag);
		this.changingDimensionDelay = entity.changingDimensionDelay;
		this.portalEntranceBlock = entity.portalEntranceBlock;
		this.portalEntranceOffset = entity.portalEntranceOffset;
		this.portalEntranceForwards = entity.portalEntranceForwards;
	}

	@Nullable
	public Entity changeDimension(DimensionType dimensionType) {
		if (!this.level.isClientSide && !this.removed) {
			this.level.getProfiler().push("changeDimension");
			MinecraftServer minecraftServer = this.getServer();
			DimensionType dimensionType2 = this.dimension;
			ServerLevel serverLevel = minecraftServer.getLevel(dimensionType2);
			ServerLevel serverLevel2 = minecraftServer.getLevel(dimensionType);
			this.dimension = dimensionType;
			this.unRide();
			this.level.getProfiler().push("reposition");
			Vec3 vec3 = this.getDeltaMovement();
			float f = 0.0F;
			BlockPos blockPos;
			if (dimensionType2 == DimensionType.THE_END && dimensionType == DimensionType.OVERWORLD) {
				blockPos = serverLevel2.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, serverLevel2.getSharedSpawnPos());
			} else if (dimensionType == DimensionType.THE_END) {
				blockPos = serverLevel2.getDimensionSpecificSpawn();
			} else {
				double d = this.getX();
				double e = this.getZ();
				double g = 8.0;
				if (dimensionType2 == DimensionType.OVERWORLD && dimensionType == DimensionType.NETHER) {
					d /= 8.0;
					e /= 8.0;
				} else if (dimensionType2 == DimensionType.NETHER && dimensionType == DimensionType.OVERWORLD) {
					d *= 8.0;
					e *= 8.0;
				}

				double h = Math.min(-2.9999872E7, serverLevel2.getWorldBorder().getMinX() + 16.0);
				double i = Math.min(-2.9999872E7, serverLevel2.getWorldBorder().getMinZ() + 16.0);
				double j = Math.min(2.9999872E7, serverLevel2.getWorldBorder().getMaxX() - 16.0);
				double k = Math.min(2.9999872E7, serverLevel2.getWorldBorder().getMaxZ() - 16.0);
				d = Mth.clamp(d, h, j);
				e = Mth.clamp(e, i, k);
				Vec3 vec32 = this.getPortalEntranceOffset();
				blockPos = new BlockPos(d, this.getY(), e);
				BlockPattern.PortalInfo portalInfo = serverLevel2.getPortalForcer()
					.findPortal(blockPos, vec3, this.getPortalEntranceForwards(), vec32.x, vec32.y, this instanceof Player);
				if (portalInfo == null) {
					return null;
				}

				blockPos = new BlockPos(portalInfo.pos);
				vec3 = portalInfo.speed;
				f = (float)portalInfo.angle;
			}

			this.level.getProfiler().popPush("reloading");
			Entity entity = this.getType().create(serverLevel2);
			if (entity != null) {
				entity.restoreFrom(this);
				entity.moveTo(blockPos, entity.yRot + f, entity.xRot);
				entity.setDeltaMovement(vec3);
				serverLevel2.addFromAnotherDimension(entity);
			}

			this.removed = true;
			this.level.getProfiler().pop();
			serverLevel.resetEmptyTime();
			serverLevel2.resetEmptyTime();
			this.level.getProfiler().pop();
			return entity;
		} else {
			return null;
		}
	}

	public boolean canChangeDimensions() {
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

	public Vec3 getPortalEntranceOffset() {
		return this.portalEntranceOffset;
	}

	public Direction getPortalEntranceForwards() {
		return this.portalEntranceForwards;
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
			"Entity's Block location", CrashReportCategory.formatLocation(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()))
		);
		Vec3 vec3 = this.getDeltaMovement();
		crashReportCategory.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vec3.x, vec3.y, vec3.z));
		crashReportCategory.setDetail("Entity's Passengers", (CrashReportDetail<String>)(() -> this.getPassengers().toString()));
		crashReportCategory.setDetail("Entity's Vehicle", (CrashReportDetail<String>)(() -> this.getVehicle().toString()));
	}

	@Environment(EnvType.CLIENT)
	public boolean displayFireAnimation() {
		return this.isOnFire() && !this.isSpectator();
	}

	public void setUUID(UUID uUID) {
		this.uuid = uUID;
		this.stringUUID = this.uuid.toString();
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getStringUUID() {
		return this.stringUUID;
	}

	public String getScoreboardName() {
		return this.stringUUID;
	}

	public boolean isPushedByFluid() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public static double getViewScale() {
		return viewScale;
	}

	@Environment(EnvType.CLIENT)
	public static void setViewScale(double d) {
		viewScale = d;
	}

	@Override
	public Component getDisplayName() {
		return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName())
			.withStyle(style -> style.setHoverEvent(this.createHoverEvent()).setInsertion(this.getStringUUID()));
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

	public final void teleportToWithTicket(double d, double e, double f) {
		if (this.level instanceof ServerLevel) {
			ChunkPos chunkPos = new ChunkPos(new BlockPos(d, e, f));
			((ServerLevel)this.level).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 0, this.getId());
			this.level.getChunk(chunkPos.x, chunkPos.z);
			this.teleportTo(d, e, f);
		}
	}

	public void teleportTo(double d, double e, double f) {
		if (this.level instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel)this.level;
			this.moveTo(d, e, f, this.yRot, this.xRot);
			this.getSelfAndPassengers().forEach(entity -> {
				serverLevel.updateChunkPos(entity);
				entity.teleported = true;
				entity.repositionDirectPassengers(Entity::forceMove);
			});
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldShowName() {
		return this.isCustomNameVisible();
	}

	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_POSE.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}
	}

	public void refreshDimensions() {
		EntityDimensions entityDimensions = this.dimensions;
		Pose pose = this.getPose();
		EntityDimensions entityDimensions2 = this.getDimensions(pose);
		this.dimensions = entityDimensions2;
		this.eyeHeight = this.getEyeHeight(pose, entityDimensions2);
		if (entityDimensions2.width < entityDimensions.width) {
			double d = (double)entityDimensions2.width / 2.0;
			this.setBoundingBox(
				new AABB(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + (double)entityDimensions2.height, this.getZ() + d)
			);
		} else {
			AABB aABB = this.getBoundingBox();
			this.setBoundingBox(
				new AABB(
					aABB.minX,
					aABB.minY,
					aABB.minZ,
					aABB.minX + (double)entityDimensions2.width,
					aABB.minY + (double)entityDimensions2.height,
					aABB.minZ + (double)entityDimensions2.width
				)
			);
			if (entityDimensions2.width > entityDimensions.width && !this.firstTick && !this.level.isClientSide) {
				float f = entityDimensions.width - entityDimensions2.width;
				this.move(MoverType.SELF, new Vec3((double)f, 0.0, (double)f));
			}
		}
	}

	public Direction getDirection() {
		return Direction.fromYRot((double)this.yRot);
	}

	public Direction getMotionDirection() {
		return this.getDirection();
	}

	protected HoverEvent createHoverEvent() {
		CompoundTag compoundTag = new CompoundTag();
		ResourceLocation resourceLocation = EntityType.getKey(this.getType());
		compoundTag.putString("id", this.getStringUUID());
		if (resourceLocation != null) {
			compoundTag.putString("type", resourceLocation.toString());
		}

		compoundTag.putString("name", Component.Serializer.toJson(this.getName()));
		return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponent(compoundTag.toString()));
	}

	public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
		return true;
	}

	public AABB getBoundingBox() {
		return this.bb;
	}

	@Environment(EnvType.CLIENT)
	public AABB getBoundingBoxForCulling() {
		return this.getBoundingBox();
	}

	protected AABB getBoundingBoxForPose(Pose pose) {
		EntityDimensions entityDimensions = this.getDimensions(pose);
		float f = entityDimensions.width / 2.0F;
		Vec3 vec3 = new Vec3(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
		Vec3 vec32 = new Vec3(this.getX() + (double)f, this.getY() + (double)entityDimensions.height, this.getZ() + (double)f);
		return new AABB(vec3, vec32);
	}

	public void setBoundingBox(AABB aABB) {
		this.bb = aABB;
	}

	protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.85F;
	}

	@Environment(EnvType.CLIENT)
	public float getEyeHeight(Pose pose) {
		return this.getEyeHeight(pose, this.getDimensions(pose));
	}

	public final float getEyeHeight() {
		return this.eyeHeight;
	}

	public boolean setSlot(int i, ItemStack itemStack) {
		return false;
	}

	@Override
	public void sendMessage(Component component) {
	}

	public Level getCommandSenderWorld() {
		return this.level;
	}

	@Nullable
	public MinecraftServer getServer() {
		return this.level.getServer();
	}

	public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand) {
		return InteractionResult.PASS;
	}

	public boolean ignoreExplosion() {
		return false;
	}

	protected void doEnchantDamageEffects(LivingEntity livingEntity, Entity entity) {
		if (entity instanceof LivingEntity) {
			EnchantmentHelper.doPostHurtEffects((LivingEntity)entity, livingEntity);
		}

		EnchantmentHelper.doPostDamageEffects(livingEntity, entity);
	}

	public void startSeenByPlayer(ServerPlayer serverPlayer) {
	}

	public void stopSeenByPlayer(ServerPlayer serverPlayer) {
	}

	public float rotate(Rotation rotation) {
		float f = Mth.wrapDegrees(this.yRot);
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
		float f = Mth.wrapDegrees(this.yRot);
		switch (mirror) {
			case LEFT_RIGHT:
				return -f;
			case FRONT_BACK:
				return 180.0F - f;
			default:
				return f;
		}
	}

	public boolean onlyOpCanSetNbt() {
		return false;
	}

	public boolean checkAndResetTeleportedFlag() {
		boolean bl = this.teleported;
		this.teleported = false;
		return bl;
	}

	@Nullable
	public Entity getControllingPassenger() {
		return null;
	}

	public List<Entity> getPassengers() {
		return (List<Entity>)(this.passengers.isEmpty() ? Collections.emptyList() : Lists.<Entity>newArrayList(this.passengers));
	}

	public boolean hasPassenger(Entity entity) {
		for (Entity entity2 : this.getPassengers()) {
			if (entity2.equals(entity)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasPassenger(Class<? extends Entity> class_) {
		for (Entity entity : this.getPassengers()) {
			if (class_.isAssignableFrom(entity.getClass())) {
				return true;
			}
		}

		return false;
	}

	public Collection<Entity> getIndirectPassengers() {
		Set<Entity> set = Sets.<Entity>newHashSet();

		for (Entity entity : this.getPassengers()) {
			set.add(entity);
			entity.fillIndirectPassengers(false, set);
		}

		return set;
	}

	public Stream<Entity> getSelfAndPassengers() {
		return Stream.concat(Stream.of(this), this.passengers.stream().flatMap(Entity::getSelfAndPassengers));
	}

	public boolean hasOnePlayerPassenger() {
		Set<Entity> set = Sets.<Entity>newHashSet();
		this.fillIndirectPassengers(true, set);
		return set.size() == 1;
	}

	private void fillIndirectPassengers(boolean bl, Set<Entity> set) {
		for (Entity entity : this.getPassengers()) {
			if (!bl || ServerPlayer.class.isAssignableFrom(entity.getClass())) {
				set.add(entity);
			}

			entity.fillIndirectPassengers(bl, set);
		}
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
		for (Entity entity2 : this.getPassengers()) {
			if (entity2.equals(entity)) {
				return true;
			}

			if (entity2.hasIndirectPassenger(entity)) {
				return true;
			}
		}

		return false;
	}

	public void repositionDirectPassengers(Entity.MoveCallback moveCallback) {
		for (Entity entity : this.passengers) {
			this.positionRider(entity, moveCallback);
		}
	}

	public boolean isControlledByLocalInstance() {
		Entity entity = this.getControllingPassenger();
		return entity instanceof Player ? ((Player)entity).isLocalPlayer() : !this.level.isClientSide;
	}

	protected static Vec3 getCollisionHorizontalEscapeVector(double d, double e, float f) {
		double g = (d + e + 1.0E-5F) / 2.0;
		float h = -Mth.sin(f * (float) (Math.PI / 180.0));
		float i = Mth.cos(f * (float) (Math.PI / 180.0));
		float j = Math.max(Math.abs(h), Math.abs(i));
		return new Vec3((double)h * g / (double)j, 0.0, (double)i * g / (double)j);
	}

	protected static double getDismountTargetFloorHeight(Level level, BlockPos blockPos, CollisionContext collisionContext) {
		VoxelShape voxelShape = level.getBlockState(blockPos).getCollisionShape(level, blockPos, collisionContext);
		if (voxelShape.isEmpty()) {
			BlockPos blockPos2 = blockPos.below();
			VoxelShape voxelShape2 = level.getBlockState(blockPos2).getCollisionShape(level, blockPos2, collisionContext);
			double d = voxelShape2.max(Direction.Axis.Y);
			return d >= 1.0 ? d - 1.0 : Double.NEGATIVE_INFINITY;
		} else {
			return voxelShape.max(Direction.Axis.Y);
		}
	}

	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Direction direction = this.getMotionDirection();
		if (direction.getAxis() == Direction.Axis.Y) {
			return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
		} else {
			Direction direction2 = direction.getClockWise();
			int[][] is = new int[][]{
				{direction2.getStepX(), direction2.getStepZ()},
				{-direction2.getStepX(), -direction2.getStepZ()},
				{-direction.getStepX() + direction2.getStepX(), -direction.getStepZ() + direction2.getStepZ()},
				{-direction.getStepX() - direction2.getStepX(), -direction.getStepZ() - direction2.getStepZ()},
				{direction.getStepX() + direction2.getStepX(), direction.getStepZ() + direction2.getStepZ()},
				{direction.getStepX() - direction2.getStepX(), direction.getStepZ() - direction2.getStepZ()},
				{-direction.getStepX(), -direction.getStepZ()},
				{direction.getStepX(), direction.getStepZ()}
			};
			BlockPos blockPos = new BlockPos(this.getX(), this.getY(), this.getZ());
			CollisionContext collisionContext = CollisionContext.of(livingEntity);

			for (Pose pose : livingEntity.getDismountPoses()) {
				for (int i : POSE_DISMOUNT_HEIGHTS.get(pose)) {
					for (int[] js : is) {
						BlockPos blockPos2 = blockPos.offset(js[0], i, js[1]);
						double d = getDismountTargetFloorHeight(this.level, blockPos2, collisionContext);
						if (!Double.isInfinite(d) && !(d >= 1.0)) {
							double e = (double)blockPos2.getY() + d;
							AABB aABB = new AABB(
								(double)blockPos2.getX(),
								e,
								(double)blockPos2.getZ(),
								(double)blockPos2.getX() + 1.0,
								e + (double)livingEntity.getDimensions(pose).height,
								(double)blockPos2.getZ() + 1.0
							);
							if (this.level.getBlockCollisions(livingEntity, aABB).allMatch(VoxelShape::isEmpty)) {
								return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + d, (double)blockPos2.getZ() + 0.5);
							}
						}
					}
				}
			}

			return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
		}
	}

	@Nullable
	public Entity getVehicle() {
		return this.vehicle;
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
			this.level instanceof ServerLevel ? (ServerLevel)this.level : null,
			this.getPermissionLevel(),
			this.getName().getString(),
			this.getDisplayName(),
			this.level.getServer(),
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
		return this.level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
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
		double g = (double)Mth.sqrt(d * d + f * f);
		this.xRot = Mth.wrapDegrees((float)(-(Mth.atan2(e, g) * 180.0F / (float)Math.PI)));
		this.yRot = Mth.wrapDegrees((float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F);
		this.setYHeadRot(this.yRot);
		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
	}

	public boolean updateFluidHeightAndDoFluidPushing(Tag<Fluid> tag, double d) {
		AABB aABB = this.getBoundingBox().deflate(0.001);
		int i = Mth.floor(aABB.minX);
		int j = Mth.ceil(aABB.maxX);
		int k = Mth.floor(aABB.minY);
		int l = Mth.ceil(aABB.maxY);
		int m = Mth.floor(aABB.minZ);
		int n = Mth.ceil(aABB.maxZ);
		if (!this.level.hasChunksAt(i, k, m, j, l, n)) {
			return false;
		} else {
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
						FluidState fluidState = this.level.getFluidState(mutableBlockPos);
						if (fluidState.is(tag)) {
							double f = (double)((float)q + fluidState.getHeight(this.level, mutableBlockPos));
							if (f >= aABB.minY) {
								bl2 = true;
								e = Math.max(f - aABB.minY, e);
								if (bl) {
									Vec3 vec32 = fluidState.getFlow(this.level, mutableBlockPos);
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

				this.setDeltaMovement(this.getDeltaMovement().add(vec3.scale(d)));
			}

			this.fluidHeight = e;
			return bl2;
		}
	}

	public double getFluidHeight() {
		return this.fluidHeight;
	}

	public final float getBbWidth() {
		return this.dimensions.width;
	}

	public final float getBbHeight() {
		return this.dimensions.height;
	}

	public abstract Packet<?> getAddEntityPacket();

	public EntityDimensions getDimensions(Pose pose) {
		return this.type.getDimensions();
	}

	public Vec3 position() {
		return this.position;
	}

	public BlockPos blockPosition() {
		return this.blockPosition;
	}

	public Vec3 getDeltaMovement() {
		return this.deltaMovement;
	}

	public void setDeltaMovement(Vec3 vec3) {
		this.deltaMovement = vec3;
	}

	public void setDeltaMovement(double d, double e, double f) {
		this.setDeltaMovement(new Vec3(d, e, f));
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

	public final double getZ() {
		return this.position.z;
	}

	public double getZ(double d) {
		return this.position.z + (double)this.getBbWidth() * d;
	}

	public double getRandomZ(double d) {
		return this.getZ((2.0 * this.random.nextDouble() - 1.0) * d);
	}

	public void setPosRaw(double d, double e, double f) {
		if (this.position.x != d || this.position.y != e || this.position.z != f) {
			this.position = new Vec3(d, e, f);
			int i = Mth.floor(d);
			int j = Mth.floor(e);
			int k = Mth.floor(f);
			if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
				this.blockPosition = new BlockPos(i, j, k);
			}
		}
	}

	public void checkDespawn() {
	}

	public void forceMove(double d, double e, double f) {
		this.moveTo(d, e, f, this.yRot, this.xRot);
	}

	@FunctionalInterface
	public interface MoveCallback {
		void accept(Entity entity, double d, double e, double f);
	}
}
