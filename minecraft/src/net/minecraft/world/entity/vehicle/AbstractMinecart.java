package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecart extends VehicleEntity {
	private static final Vec3 LOWERED_PASSENGER_ATTACHMENT = new Vec3(0.0, 0.0, 0.0);
	private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
	private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(
		Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1)
	);
	protected static final float WATER_SLOWDOWN_FACTOR = 0.95F;
	private boolean onRails;
	private boolean flipped;
	private Vec3 passengerMoveIntent = Vec3.ZERO;
	private final MinecartBehavior behavior;
	private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), enumMap -> {
		Vec3i vec3i = Direction.WEST.getUnitVec3i();
		Vec3i vec3i2 = Direction.EAST.getUnitVec3i();
		Vec3i vec3i3 = Direction.NORTH.getUnitVec3i();
		Vec3i vec3i4 = Direction.SOUTH.getUnitVec3i();
		Vec3i vec3i5 = vec3i.below();
		Vec3i vec3i6 = vec3i2.below();
		Vec3i vec3i7 = vec3i3.below();
		Vec3i vec3i8 = vec3i4.below();
		enumMap.put(RailShape.NORTH_SOUTH, Pair.of(vec3i3, vec3i4));
		enumMap.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i2));
		enumMap.put(RailShape.ASCENDING_EAST, Pair.of(vec3i5, vec3i2));
		enumMap.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i6));
		enumMap.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i3, vec3i8));
		enumMap.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i7, vec3i4));
		enumMap.put(RailShape.SOUTH_EAST, Pair.of(vec3i4, vec3i2));
		enumMap.put(RailShape.SOUTH_WEST, Pair.of(vec3i4, vec3i));
		enumMap.put(RailShape.NORTH_WEST, Pair.of(vec3i3, vec3i));
		enumMap.put(RailShape.NORTH_EAST, Pair.of(vec3i3, vec3i2));
	});

	protected AbstractMinecart(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		if (useExperimentalMovement(level)) {
			this.behavior = new NewMinecartBehavior(this);
		} else {
			this.behavior = new OldMinecartBehavior(this);
		}
	}

	protected AbstractMinecart(EntityType<?> entityType, Level level, double d, double e, double f) {
		this(entityType, level);
		this.setPos(d, e, f);
		this.xo = d;
		this.yo = e;
		this.zo = f;
	}

	public static AbstractMinecart createMinecart(
		ServerLevel serverLevel, double d, double e, double f, AbstractMinecart.Type type, ItemStack itemStack, @Nullable Player player
	) {
		AbstractMinecart abstractMinecart = (AbstractMinecart)(switch (type) {
			case CHEST -> new MinecartChest(serverLevel, d, e, f);
			case FURNACE -> new MinecartFurnace(serverLevel, d, e, f);
			case TNT -> new MinecartTNT(serverLevel, d, e, f);
			case SPAWNER -> new MinecartSpawner(serverLevel, d, e, f);
			case HOPPER -> new MinecartHopper(serverLevel, d, e, f);
			case COMMAND_BLOCK -> new MinecartCommandBlock(serverLevel, d, e, f);
			default -> new Minecart(serverLevel, d, e, f);
		});
		EntityType.createDefaultStackConfig(serverLevel, itemStack, player).accept(abstractMinecart);
		return abstractMinecart;
	}

	public MinecartBehavior getBehavior() {
		return this.behavior;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.EVENTS;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ID_DISPLAY_BLOCK, Block.getId(Blocks.AIR.defaultBlockState()));
		builder.define(DATA_ID_DISPLAY_OFFSET, 6);
		builder.define(DATA_ID_CUSTOM_DISPLAY, false);
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return Boat.canVehicleCollide(this, entity);
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
		return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		boolean bl = entity instanceof Villager || entity instanceof WanderingTrader;
		return bl ? LOWERED_PASSENGER_ATTACHMENT : super.getPassengerAttachmentPoint(entity, entityDimensions, f);
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Direction direction = this.getMotionDirection();
		if (direction.getAxis() == Direction.Axis.Y) {
			return super.getDismountLocationForPassenger(livingEntity);
		} else {
			int[][] is = DismountHelper.offsetsForDirection(direction);
			BlockPos blockPos = this.blockPosition();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			ImmutableList<Pose> immutableList = livingEntity.getDismountPoses();

			for (Pose pose : immutableList) {
				EntityDimensions entityDimensions = livingEntity.getDimensions(pose);
				float f = Math.min(entityDimensions.width(), 1.0F) / 2.0F;

				for (int i : POSE_DISMOUNT_HEIGHTS.get(pose)) {
					for (int[] js : is) {
						mutableBlockPos.set(blockPos.getX() + js[0], blockPos.getY() + i, blockPos.getZ() + js[1]);
						double d = this.level()
							.getBlockFloorHeight(
								DismountHelper.nonClimbableShape(this.level(), mutableBlockPos), () -> DismountHelper.nonClimbableShape(this.level(), mutableBlockPos.below())
							);
						if (DismountHelper.isBlockFloorValid(d)) {
							AABB aABB = new AABB((double)(-f), 0.0, (double)(-f), (double)f, (double)entityDimensions.height(), (double)f);
							Vec3 vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, d);
							if (DismountHelper.canDismountTo(this.level(), livingEntity, aABB.move(vec3))) {
								livingEntity.setPose(pose);
								return vec3;
							}
						}
					}
				}
			}

			double e = this.getBoundingBox().maxY;
			mutableBlockPos.set((double)blockPos.getX(), e, (double)blockPos.getZ());

			for (Pose pose2 : immutableList) {
				double g = (double)livingEntity.getDimensions(pose2).height();
				int j = Mth.ceil(e - (double)mutableBlockPos.getY() + g);
				double h = DismountHelper.findCeilingFrom(mutableBlockPos, j, blockPosx -> this.level().getBlockState(blockPosx).getCollisionShape(this.level(), blockPosx));
				if (e + g <= h) {
					livingEntity.setPose(pose2);
					break;
				}
			}

			return super.getDismountLocationForPassenger(livingEntity);
		}
	}

	@Override
	protected float getBlockSpeedFactor() {
		BlockState blockState = this.level().getBlockState(this.blockPosition());
		return blockState.is(BlockTags.RAILS) ? 1.0F : super.getBlockSpeedFactor();
	}

	@Override
	public void animateHurt(float f) {
		this.setHurtDir(-this.getHurtDir());
		this.setHurtTime(10);
		this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	public static Pair<Vec3i, Vec3i> exits(RailShape railShape) {
		return (Pair<Vec3i, Vec3i>)EXITS.get(railShape);
	}

	@Override
	public Direction getMotionDirection() {
		return this.behavior.getMotionDirection();
	}

	@Override
	protected double getDefaultGravity() {
		return this.isInWater() ? 0.005 : 0.04;
	}

	@Override
	public void tick() {
		if (this.getHurtTime() > 0) {
			this.setHurtTime(this.getHurtTime() - 1);
		}

		if (this.getDamage() > 0.0F) {
			this.setDamage(this.getDamage() - 1.0F);
		}

		this.checkBelowWorld();
		this.handlePortal();
		this.behavior.tick();
		this.updateInWaterStateAndDoFluidPushing();
		if (this.isInLava()) {
			this.lavaHurt();
			this.fallDistance *= 0.5F;
		}

		this.firstTick = false;
	}

	public BlockPos getCurrentBlockPos() {
		int i = Mth.floor(this.getX());
		int j = Mth.floor(this.getY());
		int k = Mth.floor(this.getZ());
		if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
			j--;
		}

		return new BlockPos(i, j, k);
	}

	public boolean pushOrPickUpEntities(AABB aABB, double d) {
		boolean bl = false;
		if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && this.getDeltaMovement().horizontalDistanceSqr() >= d) {
			List<Entity> list = this.level().getEntities(this, aABB, EntitySelector.pushableBy(this));
			if (!list.isEmpty()) {
				for (Entity entity : list) {
					if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.isVehicle() && !entity.isPassenger()) {
						entity.startRiding(this);
						bl = true;
					} else {
						entity.push(this);
					}
				}
			}
		} else {
			for (Entity entity2 : this.level().getEntities(this, aABB)) {
				if (!this.hasPassenger(entity2) && entity2.isPushable() && entity2 instanceof AbstractMinecart) {
					entity2.push(this);
				}
			}
		}

		return bl;
	}

	protected double getMaxSpeed() {
		return this.behavior.getMaxSpeed();
	}

	public void activateMinecart(int i, int j, int k, boolean bl) {
	}

	@Override
	public void lerpPositionAndRotationStep(int i, double d, double e, double f, double g, double h) {
		super.lerpPositionAndRotationStep(i, d, e, f, g, h);
	}

	@Override
	public void applyGravity() {
		super.applyGravity();
	}

	@Override
	public void reapplyPosition() {
		super.reapplyPosition();
	}

	@Override
	public boolean updateInWaterStateAndDoFluidPushing() {
		return super.updateInWaterStateAndDoFluidPushing();
	}

	@Override
	public Vec3 getKnownMovement() {
		return this.behavior.getKnownMovement(super.getKnownMovement());
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.behavior.lerpTo(d, e, f, g, h, i);
	}

	@Override
	public double lerpTargetX() {
		return this.behavior.lerpTargetX();
	}

	@Override
	public double lerpTargetY() {
		return this.behavior.lerpTargetY();
	}

	@Override
	public double lerpTargetZ() {
		return this.behavior.lerpTargetZ();
	}

	@Override
	public float lerpTargetXRot() {
		return this.behavior.lerpTargetXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return this.behavior.lerpTargetYRot();
	}

	@Override
	public void lerpMotion(double d, double e, double f) {
		this.behavior.lerpMotion(d, e, f);
	}

	protected void moveAlongTrack() {
		this.behavior.moveAlongTrack();
	}

	protected void comeOffTrack() {
		double d = this.getMaxSpeed();
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(Mth.clamp(vec3.x, -d, d), vec3.y, Mth.clamp(vec3.z, -d, d));
		if (this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		if (!this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
		}
	}

	protected double makeStepAlongTrack(BlockPos blockPos, RailShape railShape, double d) {
		return this.behavior.stepAlongTrack(blockPos, railShape, d);
	}

	@Override
	public void move(MoverType moverType, Vec3 vec3) {
		if (useExperimentalMovement(this.level())) {
			Vec3 vec32 = this.position().add(vec3);
			super.move(moverType, vec3);
			if (this.horizontalCollision || this.verticalCollision) {
				boolean bl = this.pushOrPickUpEntities(this.getBoundingBox().inflate(1.0E-7), 0.0);
				if (bl) {
					super.move(moverType, vec32.subtract(this.position()));
				}
			}
		} else {
			super.move(moverType, vec3);
		}
	}

	@Override
	public boolean isOnRails() {
		return this.onRails;
	}

	public void setOnRails(boolean bl) {
		this.onRails = bl;
	}

	public boolean isflipped() {
		return this.flipped;
	}

	public void setFlipped(boolean bl) {
		this.flipped = bl;
	}

	public Vec3 getRedstoneDirection(BlockPos blockPos) {
		BlockState blockState = this.level().getBlockState(blockPos);
		if (blockState.is(Blocks.POWERED_RAIL) && (Boolean)blockState.getValue(PoweredRailBlock.POWERED)) {
			RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
			if (railShape == RailShape.EAST_WEST) {
				if (this.isRedstoneConductor(blockPos.west())) {
					return new Vec3(1.0, 0.0, 0.0);
				}

				if (this.isRedstoneConductor(blockPos.east())) {
					return new Vec3(-1.0, 0.0, 0.0);
				}
			} else if (railShape == RailShape.NORTH_SOUTH) {
				if (this.isRedstoneConductor(blockPos.north())) {
					return new Vec3(0.0, 0.0, 1.0);
				}

				if (this.isRedstoneConductor(blockPos.south())) {
					return new Vec3(0.0, 0.0, -1.0);
				}
			}

			return Vec3.ZERO;
		} else {
			return Vec3.ZERO;
		}
	}

	public boolean isRedstoneConductor(BlockPos blockPos) {
		return this.level().getBlockState(blockPos).isRedstoneConductor(this.level(), blockPos);
	}

	protected Vec3 applyNaturalSlowdown(Vec3 vec3) {
		double d = this.behavior.getSlowdownFactor();
		Vec3 vec32 = vec3.multiply(d, 0.0, d);
		if (this.isInWater()) {
			vec32 = vec32.scale(0.95F);
		}

		return vec32;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.getBoolean("CustomDisplayTile")) {
			this.setDisplayBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound("DisplayState")));
			this.setDisplayOffset(compoundTag.getInt("DisplayOffset"));
		}

		this.flipped = compoundTag.getBoolean("FlippedRotation");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		if (this.hasCustomDisplay()) {
			compoundTag.putBoolean("CustomDisplayTile", true);
			compoundTag.put("DisplayState", NbtUtils.writeBlockState(this.getDisplayBlockState()));
			compoundTag.putInt("DisplayOffset", this.getDisplayOffset());
		}

		compoundTag.putBoolean("FlippedRotation", this.flipped);
	}

	@Override
	public void push(Entity entity) {
		if (!this.level().isClientSide) {
			if (!entity.noPhysics && !this.noPhysics) {
				if (!this.hasPassenger(entity)) {
					double d = entity.getX() - this.getX();
					double e = entity.getZ() - this.getZ();
					double f = d * d + e * e;
					if (f >= 1.0E-4F) {
						f = Math.sqrt(f);
						d /= f;
						e /= f;
						double g = 1.0 / f;
						if (g > 1.0) {
							g = 1.0;
						}

						d *= g;
						e *= g;
						d *= 0.1F;
						e *= 0.1F;
						d *= 0.5;
						e *= 0.5;
						if (entity instanceof AbstractMinecart) {
							double h = entity.getX() - this.getX();
							double i = entity.getZ() - this.getZ();
							Vec3 vec3 = new Vec3(h, 0.0, i).normalize();
							Vec3 vec32 = new Vec3((double)Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)), 0.0, (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)))
								.normalize();
							double j = Math.abs(vec3.dot(vec32));
							if (j < 0.8F) {
								return;
							}

							Vec3 vec33 = this.getDeltaMovement();
							Vec3 vec34 = entity.getDeltaMovement();
							if (((AbstractMinecart)entity).getMinecartType() == AbstractMinecart.Type.FURNACE && this.getMinecartType() != AbstractMinecart.Type.FURNACE) {
								this.setDeltaMovement(vec33.multiply(0.2, 1.0, 0.2));
								this.push(vec34.x - d, 0.0, vec34.z - e);
								entity.setDeltaMovement(vec34.multiply(0.95, 1.0, 0.95));
							} else if (((AbstractMinecart)entity).getMinecartType() != AbstractMinecart.Type.FURNACE && this.getMinecartType() == AbstractMinecart.Type.FURNACE) {
								entity.setDeltaMovement(vec34.multiply(0.2, 1.0, 0.2));
								entity.push(vec33.x + d, 0.0, vec33.z + e);
								this.setDeltaMovement(vec33.multiply(0.95, 1.0, 0.95));
							} else {
								double k = (vec34.x + vec33.x) / 2.0;
								double l = (vec34.z + vec33.z) / 2.0;
								this.setDeltaMovement(vec33.multiply(0.2, 1.0, 0.2));
								this.push(k - d, 0.0, l - e);
								entity.setDeltaMovement(vec34.multiply(0.2, 1.0, 0.2));
								entity.push(k + d, 0.0, l + e);
							}
						} else {
							this.push(-d, 0.0, -e);
							entity.push(d / 4.0, 0.0, e / 4.0);
						}
					}
				}
			}
		}
	}

	public abstract AbstractMinecart.Type getMinecartType();

	public BlockState getDisplayBlockState() {
		return !this.hasCustomDisplay() ? this.getDefaultDisplayBlockState() : Block.stateById(this.getEntityData().get(DATA_ID_DISPLAY_BLOCK));
	}

	public BlockState getDefaultDisplayBlockState() {
		return Blocks.AIR.defaultBlockState();
	}

	public int getDisplayOffset() {
		return !this.hasCustomDisplay() ? this.getDefaultDisplayOffset() : this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
	}

	public int getDefaultDisplayOffset() {
		return 6;
	}

	public void setDisplayBlockState(BlockState blockState) {
		this.getEntityData().set(DATA_ID_DISPLAY_BLOCK, Block.getId(blockState));
		this.setCustomDisplay(true);
	}

	public void setDisplayOffset(int i) {
		this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, i);
		this.setCustomDisplay(true);
	}

	public boolean hasCustomDisplay() {
		return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY);
	}

	public void setCustomDisplay(boolean bl) {
		this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY, bl);
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(switch (this.getMinecartType()) {
			case CHEST -> Items.CHEST_MINECART;
			case FURNACE -> Items.FURNACE_MINECART;
			case TNT -> Items.TNT_MINECART;
			default -> Items.MINECART;
			case HOPPER -> Items.HOPPER_MINECART;
			case COMMAND_BLOCK -> Items.COMMAND_BLOCK_MINECART;
		});
	}

	public void setPassengerMoveIntentFromInput(LivingEntity livingEntity, Vec3 vec3) {
		Vec3 vec32 = getInputVector(vec3, 1.0F, livingEntity.getYRot());
		this.setPassengerMoveIntent(vec32);
	}

	public void setPassengerMoveIntent(Vec3 vec3) {
		this.passengerMoveIntent = vec3;
	}

	public Vec3 getPassengerMoveIntent() {
		return this.passengerMoveIntent;
	}

	public static boolean useExperimentalMovement(Level level) {
		return level.enabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS);
	}

	public static enum Type {
		RIDEABLE,
		CHEST,
		FURNACE,
		TNT,
		SPAWNER,
		HOPPER,
		COMMAND_BLOCK;
	}
}
