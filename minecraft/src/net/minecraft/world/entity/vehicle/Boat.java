package net.minecraft.world.entity.vehicle;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Boat extends Entity implements VariantHolder<Boat.Type> {
	private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	public static final int PADDLE_LEFT = 0;
	public static final int PADDLE_RIGHT = 1;
	private static final int TIME_TO_EJECT = 60;
	private static final float PADDLE_SPEED = (float) (Math.PI / 8);
	public static final double PADDLE_SOUND_TIME = (float) (Math.PI / 4);
	public static final int BUBBLE_TIME = 60;
	private final float[] paddlePositions = new float[2];
	private float invFriction;
	private float outOfControlTicks;
	private float deltaRotation;
	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;
	private boolean inputLeft;
	private boolean inputRight;
	private boolean inputUp;
	private boolean inputDown;
	private double waterLevel;
	private float landFriction;
	private Boat.Status status;
	private Boat.Status oldStatus;
	private double lastYd;
	private boolean isAboveBubbleColumn;
	private boolean bubbleColumnDirectionIsDown;
	private float bubbleMultiplier;
	private float bubbleAngle;
	private float bubbleAngleO;

	public Boat(EntityType<? extends Boat> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
	}

	public Boat(Level level, double d, double e, double f) {
		this(EntityType.BOAT, level);
		this.setPos(d, e, f);
		this.xo = d;
		this.yo = e;
		this.zo = f;
	}

	@Override
	protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.EVENTS;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_ID_HURT, 0);
		this.entityData.define(DATA_ID_HURTDIR, 1);
		this.entityData.define(DATA_ID_DAMAGE, 0.0F);
		this.entityData.define(DATA_ID_TYPE, Boat.Type.OAK.ordinal());
		this.entityData.define(DATA_ID_PADDLE_LEFT, false);
		this.entityData.define(DATA_ID_PADDLE_RIGHT, false);
		this.entityData.define(DATA_ID_BUBBLE_TIME, 0);
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return canVehicleCollide(this, entity);
	}

	public static boolean canVehicleCollide(Entity entity, Entity entity2) {
		return (entity2.canBeCollidedWith() || entity2.isPushable()) && !entity.isPassengerOfSameVehicle(entity2);
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
		return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
	}

	@Override
	public double getPassengersRidingOffset() {
		return this.getVariant() == Boat.Type.BAMBOO ? 0.3 : -0.1;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (!this.level.isClientSide && !this.isRemoved()) {
			this.setHurtDir(-this.getHurtDir());
			this.setHurtTime(10);
			this.setDamage(this.getDamage() + f * 10.0F);
			this.markHurt();
			this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
			boolean bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
			if (bl || this.getDamage() > 40.0F) {
				if (!bl && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.destroy(damageSource);
				}

				this.discard();
			}

			return true;
		} else {
			return true;
		}
	}

	protected void destroy(DamageSource damageSource) {
		this.spawnAtLocation(this.getDropItem());
	}

	@Override
	public void onAboveBubbleCol(boolean bl) {
		if (!this.level.isClientSide) {
			this.isAboveBubbleColumn = true;
			this.bubbleColumnDirectionIsDown = bl;
			if (this.getBubbleTime() == 0) {
				this.setBubbleTime(60);
			}
		}

		this.level
			.addParticle(
				ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0
			);
		if (this.random.nextInt(20) == 0) {
			this.level
				.playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
			this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
		}
	}

	@Override
	public void push(Entity entity) {
		if (entity instanceof Boat) {
			if (entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
				super.push(entity);
			}
		} else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
			super.push(entity);
		}
	}

	public Item getDropItem() {
		return switch (this.getVariant()) {
			case SPRUCE -> Items.SPRUCE_BOAT;
			case BIRCH -> Items.BIRCH_BOAT;
			case JUNGLE -> Items.JUNGLE_BOAT;
			case ACACIA -> Items.ACACIA_BOAT;
			case CHERRY -> Items.CHERRY_BOAT;
			case DARK_OAK -> Items.DARK_OAK_BOAT;
			case MANGROVE -> Items.MANGROVE_BOAT;
			case BAMBOO -> Items.BAMBOO_RAFT;
			default -> Items.OAK_BOAT;
		};
	}

	@Override
	public void animateHurt(float f) {
		this.setHurtDir(-this.getHurtDir());
		this.setHurtTime(10);
		this.setDamage(this.getDamage() * 11.0F);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		this.lerpX = d;
		this.lerpY = e;
		this.lerpZ = f;
		this.lerpYRot = (double)g;
		this.lerpXRot = (double)h;
		this.lerpSteps = 10;
	}

	@Override
	public Direction getMotionDirection() {
		return this.getDirection().getClockWise();
	}

	@Override
	public void tick() {
		this.oldStatus = this.status;
		this.status = this.getStatus();
		if (this.status != Boat.Status.UNDER_WATER && this.status != Boat.Status.UNDER_FLOWING_WATER) {
			this.outOfControlTicks = 0.0F;
		} else {
			this.outOfControlTicks++;
		}

		if (!this.level.isClientSide && this.outOfControlTicks >= 60.0F) {
			this.ejectPassengers();
		}

		if (this.getHurtTime() > 0) {
			this.setHurtTime(this.getHurtTime() - 1);
		}

		if (this.getDamage() > 0.0F) {
			this.setDamage(this.getDamage() - 1.0F);
		}

		super.tick();
		this.tickLerp();
		if (this.isControlledByLocalInstance()) {
			if (!(this.getFirstPassenger() instanceof Player)) {
				this.setPaddleState(false, false);
			}

			this.floatBoat();
			if (this.level.isClientSide) {
				this.controlBoat();
				this.level.sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
		} else {
			this.setDeltaMovement(Vec3.ZERO);
		}

		this.tickBubbleColumn();

		for (int i = 0; i <= 1; i++) {
			if (this.getPaddleState(i)) {
				if (!this.isSilent()
					&& (double)(this.paddlePositions[i] % (float) (Math.PI * 2)) <= (float) (Math.PI / 4)
					&& (double)((this.paddlePositions[i] + (float) (Math.PI / 8)) % (float) (Math.PI * 2)) >= (float) (Math.PI / 4)) {
					SoundEvent soundEvent = this.getPaddleSound();
					if (soundEvent != null) {
						Vec3 vec3 = this.getViewVector(1.0F);
						double d = i == 1 ? -vec3.z : vec3.z;
						double e = i == 1 ? vec3.x : -vec3.x;
						this.level.playSound(null, this.getX() + d, this.getY(), this.getZ() + e, soundEvent, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
					}
				}

				this.paddlePositions[i] = this.paddlePositions[i] + (float) (Math.PI / 8);
			} else {
				this.paddlePositions[i] = 0.0F;
			}
		}

		this.checkInsideBlocks();
		List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
		if (!list.isEmpty()) {
			boolean bl = !this.level.isClientSide && !(this.getControllingPassenger() instanceof Player);

			for (int j = 0; j < list.size(); j++) {
				Entity entity = (Entity)list.get(j);
				if (!entity.hasPassenger(this)) {
					if (bl
						&& this.getPassengers().size() < this.getMaxPassengers()
						&& !entity.isPassenger()
						&& this.hasEnoughSpaceFor(entity)
						&& entity instanceof LivingEntity
						&& !(entity instanceof WaterAnimal)
						&& !(entity instanceof Player)) {
						entity.startRiding(this);
					} else {
						this.push(entity);
					}
				}
			}
		}
	}

	private void tickBubbleColumn() {
		if (this.level.isClientSide) {
			int i = this.getBubbleTime();
			if (i > 0) {
				this.bubbleMultiplier += 0.05F;
			} else {
				this.bubbleMultiplier -= 0.1F;
			}

			this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0F, 1.0F);
			this.bubbleAngleO = this.bubbleAngle;
			this.bubbleAngle = 10.0F * (float)Math.sin((double)(0.5F * (float)this.level.getGameTime())) * this.bubbleMultiplier;
		} else {
			if (!this.isAboveBubbleColumn) {
				this.setBubbleTime(0);
			}

			int i = this.getBubbleTime();
			if (i > 0) {
				this.setBubbleTime(--i);
				int j = 60 - i - 1;
				if (j > 0 && i == 0) {
					this.setBubbleTime(0);
					Vec3 vec3 = this.getDeltaMovement();
					if (this.bubbleColumnDirectionIsDown) {
						this.setDeltaMovement(vec3.add(0.0, -0.7, 0.0));
						this.ejectPassengers();
					} else {
						this.setDeltaMovement(vec3.x, this.hasPassenger(entity -> entity instanceof Player) ? 2.7 : 0.6, vec3.z);
					}
				}

				this.isAboveBubbleColumn = false;
			}
		}
	}

	@Nullable
	protected SoundEvent getPaddleSound() {
		switch (this.getStatus()) {
			case IN_WATER:
			case UNDER_WATER:
			case UNDER_FLOWING_WATER:
				return SoundEvents.BOAT_PADDLE_WATER;
			case ON_LAND:
				return SoundEvents.BOAT_PADDLE_LAND;
			case IN_AIR:
			default:
				return null;
		}
	}

	private void tickLerp() {
		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
			this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpSteps > 0) {
			double d = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
			double e = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
			double f = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
			double g = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
			this.setYRot(this.getYRot() + (float)g / (float)this.lerpSteps);
			this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
			this.lerpSteps--;
			this.setPos(d, e, f);
			this.setRot(this.getYRot(), this.getXRot());
		}
	}

	public void setPaddleState(boolean bl, boolean bl2) {
		this.entityData.set(DATA_ID_PADDLE_LEFT, bl);
		this.entityData.set(DATA_ID_PADDLE_RIGHT, bl2);
	}

	public float getRowingTime(int i, float f) {
		return this.getPaddleState(i) ? Mth.clampedLerp(this.paddlePositions[i] - (float) (Math.PI / 8), this.paddlePositions[i], f) : 0.0F;
	}

	private Boat.Status getStatus() {
		Boat.Status status = this.isUnderwater();
		if (status != null) {
			this.waterLevel = this.getBoundingBox().maxY;
			return status;
		} else if (this.checkInWater()) {
			return Boat.Status.IN_WATER;
		} else {
			float f = this.getGroundFriction();
			if (f > 0.0F) {
				this.landFriction = f;
				return Boat.Status.ON_LAND;
			} else {
				return Boat.Status.IN_AIR;
			}
		}
	}

	public float getWaterLevelAbove() {
		AABB aABB = this.getBoundingBox();
		int i = Mth.floor(aABB.minX);
		int j = Mth.ceil(aABB.maxX);
		int k = Mth.floor(aABB.maxY);
		int l = Mth.ceil(aABB.maxY - this.lastYd);
		int m = Mth.floor(aABB.minZ);
		int n = Mth.ceil(aABB.maxZ);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		label39:
		for (int o = k; o < l; o++) {
			float f = 0.0F;

			for (int p = i; p < j; p++) {
				for (int q = m; q < n; q++) {
					mutableBlockPos.set(p, o, q);
					FluidState fluidState = this.level.getFluidState(mutableBlockPos);
					if (fluidState.is(FluidTags.WATER)) {
						f = Math.max(f, fluidState.getHeight(this.level, mutableBlockPos));
					}

					if (f >= 1.0F) {
						continue label39;
					}
				}
			}

			if (f < 1.0F) {
				return (float)mutableBlockPos.getY() + f;
			}
		}

		return (float)(l + 1);
	}

	public float getGroundFriction() {
		AABB aABB = this.getBoundingBox();
		AABB aABB2 = new AABB(aABB.minX, aABB.minY - 0.001, aABB.minZ, aABB.maxX, aABB.minY, aABB.maxZ);
		int i = Mth.floor(aABB2.minX) - 1;
		int j = Mth.ceil(aABB2.maxX) + 1;
		int k = Mth.floor(aABB2.minY) - 1;
		int l = Mth.ceil(aABB2.maxY) + 1;
		int m = Mth.floor(aABB2.minZ) - 1;
		int n = Mth.ceil(aABB2.maxZ) + 1;
		VoxelShape voxelShape = Shapes.create(aABB2);
		float f = 0.0F;
		int o = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int p = i; p < j; p++) {
			for (int q = m; q < n; q++) {
				int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
				if (r != 2) {
					for (int s = k; s < l; s++) {
						if (r <= 0 || s != k && s != l - 1) {
							mutableBlockPos.set(p, s, q);
							BlockState blockState = this.level.getBlockState(mutableBlockPos);
							if (!(blockState.getBlock() instanceof WaterlilyBlock)
								&& Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level, mutableBlockPos).move((double)p, (double)s, (double)q), voxelShape, BooleanOp.AND)) {
								f += blockState.getBlock().getFriction();
								o++;
							}
						}
					}
				}
			}
		}

		return f / (float)o;
	}

	private boolean checkInWater() {
		AABB aABB = this.getBoundingBox();
		int i = Mth.floor(aABB.minX);
		int j = Mth.ceil(aABB.maxX);
		int k = Mth.floor(aABB.minY);
		int l = Mth.ceil(aABB.minY + 0.001);
		int m = Mth.floor(aABB.minZ);
		int n = Mth.ceil(aABB.maxZ);
		boolean bl = false;
		this.waterLevel = -Double.MAX_VALUE;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int o = i; o < j; o++) {
			for (int p = k; p < l; p++) {
				for (int q = m; q < n; q++) {
					mutableBlockPos.set(o, p, q);
					FluidState fluidState = this.level.getFluidState(mutableBlockPos);
					if (fluidState.is(FluidTags.WATER)) {
						float f = (float)p + fluidState.getHeight(this.level, mutableBlockPos);
						this.waterLevel = Math.max((double)f, this.waterLevel);
						bl |= aABB.minY < (double)f;
					}
				}
			}
		}

		return bl;
	}

	@Nullable
	private Boat.Status isUnderwater() {
		AABB aABB = this.getBoundingBox();
		double d = aABB.maxY + 0.001;
		int i = Mth.floor(aABB.minX);
		int j = Mth.ceil(aABB.maxX);
		int k = Mth.floor(aABB.maxY);
		int l = Mth.ceil(d);
		int m = Mth.floor(aABB.minZ);
		int n = Mth.ceil(aABB.maxZ);
		boolean bl = false;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int o = i; o < j; o++) {
			for (int p = k; p < l; p++) {
				for (int q = m; q < n; q++) {
					mutableBlockPos.set(o, p, q);
					FluidState fluidState = this.level.getFluidState(mutableBlockPos);
					if (fluidState.is(FluidTags.WATER) && d < (double)((float)mutableBlockPos.getY() + fluidState.getHeight(this.level, mutableBlockPos))) {
						if (!fluidState.isSource()) {
							return Boat.Status.UNDER_FLOWING_WATER;
						}

						bl = true;
					}
				}
			}
		}

		return bl ? Boat.Status.UNDER_WATER : null;
	}

	private void floatBoat() {
		double d = -0.04F;
		double e = this.isNoGravity() ? 0.0 : -0.04F;
		double f = 0.0;
		this.invFriction = 0.05F;
		if (this.oldStatus == Boat.Status.IN_AIR && this.status != Boat.Status.IN_AIR && this.status != Boat.Status.ON_LAND) {
			this.waterLevel = this.getY(1.0);
			this.setPos(this.getX(), (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101, this.getZ());
			this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
			this.lastYd = 0.0;
			this.status = Boat.Status.IN_WATER;
		} else {
			if (this.status == Boat.Status.IN_WATER) {
				f = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
				this.invFriction = 0.9F;
			} else if (this.status == Boat.Status.UNDER_FLOWING_WATER) {
				e = -7.0E-4;
				this.invFriction = 0.9F;
			} else if (this.status == Boat.Status.UNDER_WATER) {
				f = 0.01F;
				this.invFriction = 0.45F;
			} else if (this.status == Boat.Status.IN_AIR) {
				this.invFriction = 0.9F;
			} else if (this.status == Boat.Status.ON_LAND) {
				this.invFriction = this.landFriction;
				if (this.getControllingPassenger() instanceof Player) {
					this.landFriction /= 2.0F;
				}
			}

			Vec3 vec3 = this.getDeltaMovement();
			this.setDeltaMovement(vec3.x * (double)this.invFriction, vec3.y + e, vec3.z * (double)this.invFriction);
			this.deltaRotation = this.deltaRotation * this.invFriction;
			if (f > 0.0) {
				Vec3 vec32 = this.getDeltaMovement();
				this.setDeltaMovement(vec32.x, (vec32.y + f * 0.06153846016296973) * 0.75, vec32.z);
			}
		}
	}

	private void controlBoat() {
		if (this.isVehicle()) {
			float f = 0.0F;
			if (this.inputLeft) {
				this.deltaRotation--;
			}

			if (this.inputRight) {
				this.deltaRotation++;
			}

			if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
				f += 0.005F;
			}

			this.setYRot(this.getYRot() + this.deltaRotation);
			if (this.inputUp) {
				f += 0.04F;
			}

			if (this.inputDown) {
				f -= 0.005F;
			}

			this.setDeltaMovement(
				this.getDeltaMovement()
					.add((double)(Mth.sin(-this.getYRot() * (float) (Math.PI / 180.0)) * f), 0.0, (double)(Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * f))
			);
			this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
		}
	}

	protected float getSinglePassengerXOffset() {
		return 0.0F;
	}

	public boolean hasEnoughSpaceFor(Entity entity) {
		return entity.getBbWidth() < this.getBbWidth();
	}

	@Override
	public void positionRider(Entity entity) {
		if (this.hasPassenger(entity)) {
			float f = this.getSinglePassengerXOffset();
			float g = (float)((this.isRemoved() ? 0.01F : this.getPassengersRidingOffset()) + entity.getMyRidingOffset());
			if (this.getPassengers().size() > 1) {
				int i = this.getPassengers().indexOf(entity);
				if (i == 0) {
					f = 0.2F;
				} else {
					f = -0.6F;
				}

				if (entity instanceof Animal) {
					f += 0.2F;
				}
			}

			Vec3 vec3 = new Vec3((double)f, 0.0, 0.0).yRot(-this.getYRot() * (float) (Math.PI / 180.0) - (float) (Math.PI / 2));
			entity.setPos(this.getX() + vec3.x, this.getY() + (double)g, this.getZ() + vec3.z);
			entity.setYRot(entity.getYRot() + this.deltaRotation);
			entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
			this.clampRotation(entity);
			if (entity instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
				int j = entity.getId() % 2 == 0 ? 90 : 270;
				entity.setYBodyRot(((Animal)entity).yBodyRot + (float)j);
				entity.setYHeadRot(entity.getYHeadRot() + (float)j);
			}
		}
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Vec3 vec3 = getCollisionHorizontalEscapeVector((double)(this.getBbWidth() * Mth.SQRT_OF_TWO), (double)livingEntity.getBbWidth(), livingEntity.getYRot());
		double d = this.getX() + vec3.x;
		double e = this.getZ() + vec3.z;
		BlockPos blockPos = new BlockPos(d, this.getBoundingBox().maxY, e);
		BlockPos blockPos2 = blockPos.below();
		if (!this.level.isWaterAt(blockPos2)) {
			List<Vec3> list = Lists.<Vec3>newArrayList();
			double f = this.level.getBlockFloorHeight(blockPos);
			if (DismountHelper.isBlockFloorValid(f)) {
				list.add(new Vec3(d, (double)blockPos.getY() + f, e));
			}

			double g = this.level.getBlockFloorHeight(blockPos2);
			if (DismountHelper.isBlockFloorValid(g)) {
				list.add(new Vec3(d, (double)blockPos2.getY() + g, e));
			}

			for (Pose pose : livingEntity.getDismountPoses()) {
				for (Vec3 vec32 : list) {
					if (DismountHelper.canDismountTo(this.level, vec32, livingEntity, pose)) {
						livingEntity.setPose(pose);
						return vec32;
					}
				}
			}
		}

		return super.getDismountLocationForPassenger(livingEntity);
	}

	protected void clampRotation(Entity entity) {
		entity.setYBodyRot(this.getYRot());
		float f = Mth.wrapDegrees(entity.getYRot() - this.getYRot());
		float g = Mth.clamp(f, -105.0F, 105.0F);
		entity.yRotO += g - f;
		entity.setYRot(entity.getYRot() + g - f);
		entity.setYHeadRot(entity.getYRot());
	}

	@Override
	public void onPassengerTurned(Entity entity) {
		this.clampRotation(entity);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putString("Type", this.getVariant().getSerializedName());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("Type", 8)) {
			this.setVariant(Boat.Type.byName(compoundTag.getString("Type")));
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		} else if (this.outOfControlTicks < 60.0F) {
			if (!this.level.isClientSide) {
				return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
			} else {
				return InteractionResult.SUCCESS;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		this.lastYd = this.getDeltaMovement().y;
		if (!this.isPassenger()) {
			if (bl) {
				if (this.fallDistance > 3.0F) {
					if (this.status != Boat.Status.ON_LAND) {
						this.resetFallDistance();
						return;
					}

					this.causeFallDamage(this.fallDistance, 1.0F, this.damageSources().fall());
					if (!this.level.isClientSide && !this.isRemoved()) {
						this.kill();
						if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
							for (int i = 0; i < 3; i++) {
								this.spawnAtLocation(this.getVariant().getPlanks());
							}

							for (int i = 0; i < 2; i++) {
								this.spawnAtLocation(Items.STICK);
							}
						}
					}
				}

				this.resetFallDistance();
			} else if (!this.level.getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && d < 0.0) {
				this.fallDistance -= (float)d;
			}
		}
	}

	public boolean getPaddleState(int i) {
		return this.entityData.get(i == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) && this.getControllingPassenger() != null;
	}

	public void setDamage(float f) {
		this.entityData.set(DATA_ID_DAMAGE, f);
	}

	public float getDamage() {
		return this.entityData.get(DATA_ID_DAMAGE);
	}

	public void setHurtTime(int i) {
		this.entityData.set(DATA_ID_HURT, i);
	}

	public int getHurtTime() {
		return this.entityData.get(DATA_ID_HURT);
	}

	private void setBubbleTime(int i) {
		this.entityData.set(DATA_ID_BUBBLE_TIME, i);
	}

	private int getBubbleTime() {
		return this.entityData.get(DATA_ID_BUBBLE_TIME);
	}

	public float getBubbleAngle(float f) {
		return Mth.lerp(f, this.bubbleAngleO, this.bubbleAngle);
	}

	public void setHurtDir(int i) {
		this.entityData.set(DATA_ID_HURTDIR, i);
	}

	public int getHurtDir() {
		return this.entityData.get(DATA_ID_HURTDIR);
	}

	public void setVariant(Boat.Type type) {
		this.entityData.set(DATA_ID_TYPE, type.ordinal());
	}

	public Boat.Type getVariant() {
		return Boat.Type.byId(this.entityData.get(DATA_ID_TYPE));
	}

	@Override
	protected boolean canAddPassenger(Entity entity) {
		return this.getPassengers().size() < this.getMaxPassengers() && !this.isEyeInFluid(FluidTags.WATER);
	}

	protected int getMaxPassengers() {
		return 2;
	}

	@Nullable
	@Override
	public Entity getControllingPassenger() {
		return this.getFirstPassenger();
	}

	public void setInput(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		this.inputLeft = bl;
		this.inputRight = bl2;
		this.inputUp = bl3;
		this.inputDown = bl4;
	}

	@Override
	public boolean isUnderWater() {
		return this.status == Boat.Status.UNDER_WATER || this.status == Boat.Status.UNDER_FLOWING_WATER;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(this.getDropItem());
	}

	public static enum Status {
		IN_WATER,
		UNDER_WATER,
		UNDER_FLOWING_WATER,
		ON_LAND,
		IN_AIR;
	}

	public static enum Type implements StringRepresentable {
		OAK(Blocks.OAK_PLANKS, "oak"),
		SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
		BIRCH(Blocks.BIRCH_PLANKS, "birch"),
		JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
		ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
		CHERRY(Blocks.CHERRY_PLANKS, "cherry"),
		DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak"),
		MANGROVE(Blocks.MANGROVE_PLANKS, "mangrove"),
		BAMBOO(Blocks.BAMBOO_PLANKS, "bamboo");

		private final String name;
		private final Block planks;
		public static final StringRepresentable.EnumCodec<Boat.Type> CODEC = StringRepresentable.fromEnum(Boat.Type::values);
		private static final IntFunction<Boat.Type> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

		private Type(Block block, String string2) {
			this.name = string2;
			this.planks = block;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public String getName() {
			return this.name;
		}

		public Block getPlanks() {
			return this.planks;
		}

		public String toString() {
			return this.name;
		}

		public static Boat.Type byId(int i) {
			return (Boat.Type)BY_ID.apply(i);
		}

		public static Boat.Type byName(String string) {
			return (Boat.Type)CODEC.byName(string, OAK);
		}
	}
}
