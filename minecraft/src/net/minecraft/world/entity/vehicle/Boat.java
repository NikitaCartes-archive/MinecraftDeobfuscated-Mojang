package net.minecraft.world.entity.vehicle;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Boat extends Entity {
	private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
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
		this.setDeltaMovement(Vec3.ZERO);
		this.xo = d;
		this.yo = e;
		this.zo = f;
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
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

	@Nullable
	@Override
	public AABB getCollideAgainstBox(Entity entity) {
		return entity.isPushable() ? entity.getBoundingBox() : null;
	}

	@Nullable
	@Override
	public AABB getCollideBox() {
		return this.getBoundingBox();
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public double getRideHeight() {
		return -0.1;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (this.level.isClientSide || this.removed) {
			return true;
		} else if (damageSource instanceof IndirectEntityDamageSource && damageSource.getEntity() != null && this.hasPassenger(damageSource.getEntity())) {
			return false;
		} else {
			this.setHurtDir(-this.getHurtDir());
			this.setHurtTime(10);
			this.setDamage(this.getDamage() + f * 10.0F);
			this.markHurt();
			boolean bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).abilities.instabuild;
			if (bl || this.getDamage() > 40.0F) {
				if (!bl && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.spawnAtLocation(this.getDropItem());
				}

				this.remove();
			}

			return true;
		}
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

		this.level.addParticle(ParticleTypes.SPLASH, this.x + (double)this.random.nextFloat(), this.y + 0.7, this.z + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
		if (this.random.nextInt(20) == 0) {
			this.level.playLocalSound(this.x, this.y, this.z, this.getSwimSplashSound(), this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
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
		switch (this.getBoatType()) {
			case OAK:
			default:
				return Items.OAK_BOAT;
			case SPRUCE:
				return Items.SPRUCE_BOAT;
			case BIRCH:
				return Items.BIRCH_BOAT;
			case JUNGLE:
				return Items.JUNGLE_BOAT;
			case ACACIA:
				return Items.ACACIA_BOAT;
			case DARK_OAK:
				return Items.DARK_OAK_BOAT;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateHurt() {
		this.setHurtDir(-this.getHurtDir());
		this.setHurtTime(10);
		this.setDamage(this.getDamage() * 11.0F);
	}

	@Override
	public boolean isPickable() {
		return !this.removed;
	}

	@Environment(EnvType.CLIENT)
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
			if (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof Player)) {
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
					&& ((double)this.paddlePositions[i] + (float) (Math.PI / 8)) % (float) (Math.PI * 2) >= (float) (Math.PI / 4)) {
					SoundEvent soundEvent = this.getPaddleSound();
					if (soundEvent != null) {
						Vec3 vec3 = this.getViewVector(1.0F);
						double d = i == 1 ? -vec3.z : vec3.z;
						double e = i == 1 ? vec3.x : -vec3.x;
						this.level.playSound(null, this.x + d, this.y, this.z + e, soundEvent, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
					}
				}

				this.paddlePositions[i] = (float)((double)this.paddlePositions[i] + (float) (Math.PI / 8));
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
						&& this.getPassengers().size() < 2
						&& !entity.isPassenger()
						&& entity.getBbWidth() < this.getBbWidth()
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
						this.setDeltaMovement(vec3.x, this.hasPassenger(Player.class) ? 2.7 : 0.6, vec3.z);
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
			this.setPacketCoordinates(this.x, this.y, this.z);
		}

		if (this.lerpSteps > 0) {
			double d = this.x + (this.lerpX - this.x) / (double)this.lerpSteps;
			double e = this.y + (this.lerpY - this.y) / (double)this.lerpSteps;
			double f = this.z + (this.lerpZ - this.z) / (double)this.lerpSteps;
			double g = Mth.wrapDegrees(this.lerpYRot - (double)this.yRot);
			this.yRot = (float)((double)this.yRot + g / (double)this.lerpSteps);
			this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
			this.lerpSteps--;
			this.setPos(d, e, f);
			this.setRot(this.yRot, this.xRot);
		}
	}

	public void setPaddleState(boolean bl, boolean bl2) {
		this.entityData.set(DATA_ID_PADDLE_LEFT, bl);
		this.entityData.set(DATA_ID_PADDLE_RIGHT, bl2);
	}

	@Environment(EnvType.CLIENT)
	public float getRowingTime(int i, float f) {
		return this.getPaddleState(i)
			? (float)Mth.clampedLerp((double)this.paddlePositions[i] - (float) (Math.PI / 8), (double)this.paddlePositions[i], (double)f)
			: 0.0F;
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

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			label136:
			for (int o = k; o < l; o++) {
				float f = 0.0F;
				int p = i;

				while (true) {
					if (p < j) {
						for (int q = m; q < n; q++) {
							pooledMutableBlockPos.set(p, o, q);
							FluidState fluidState = this.level.getFluidState(pooledMutableBlockPos);
							if (fluidState.is(FluidTags.WATER)) {
								f = Math.max(f, fluidState.getHeight(this.level, pooledMutableBlockPos));
							}

							if (f >= 1.0F) {
								continue label136;
							}
						}

						p++;
					} else {
						if (f < 1.0F) {
							return (float)pooledMutableBlockPos.getY() + f;
						}
						break;
					}
				}
			}

			return (float)(l + 1);
		}
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

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			for (int p = i; p < j; p++) {
				for (int q = m; q < n; q++) {
					int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
					if (r != 2) {
						for (int s = k; s < l; s++) {
							if (r <= 0 || s != k && s != l - 1) {
								pooledMutableBlockPos.set(p, s, q);
								BlockState blockState = this.level.getBlockState(pooledMutableBlockPos);
								if (!(blockState.getBlock() instanceof WaterlilyBlock)
									&& Shapes.joinIsNotEmpty(
										blockState.getCollisionShape(this.level, pooledMutableBlockPos).move((double)p, (double)s, (double)q), voxelShape, BooleanOp.AND
									)) {
									f += blockState.getBlock().getFriction();
									o++;
								}
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
		this.waterLevel = Double.MIN_VALUE;

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			for (int o = i; o < j; o++) {
				for (int p = k; p < l; p++) {
					for (int q = m; q < n; q++) {
						pooledMutableBlockPos.set(o, p, q);
						FluidState fluidState = this.level.getFluidState(pooledMutableBlockPos);
						if (fluidState.is(FluidTags.WATER)) {
							float f = (float)p + fluidState.getHeight(this.level, pooledMutableBlockPos);
							this.waterLevel = Math.max((double)f, this.waterLevel);
							bl |= aABB.minY < (double)f;
						}
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

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			for (int o = i; o < j; o++) {
				for (int p = k; p < l; p++) {
					for (int q = m; q < n; q++) {
						pooledMutableBlockPos.set(o, p, q);
						FluidState fluidState = this.level.getFluidState(pooledMutableBlockPos);
						if (fluidState.is(FluidTags.WATER) && d < (double)((float)pooledMutableBlockPos.getY() + fluidState.getHeight(this.level, pooledMutableBlockPos))) {
							if (!fluidState.isSource()) {
								return Boat.Status.UNDER_FLOWING_WATER;
							}

							bl = true;
						}
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
			this.waterLevel = this.getBoundingBox().minY + (double)this.getBbHeight();
			this.setPos(this.x, (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101, this.z);
			this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
			this.lastYd = 0.0;
			this.status = Boat.Status.IN_WATER;
		} else {
			if (this.status == Boat.Status.IN_WATER) {
				f = (this.waterLevel - this.getBoundingBox().minY) / (double)this.getBbHeight();
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

			this.yRot = this.yRot + this.deltaRotation;
			if (this.inputUp) {
				f += 0.04F;
			}

			if (this.inputDown) {
				f -= 0.005F;
			}

			this.setDeltaMovement(
				this.getDeltaMovement()
					.add((double)(Mth.sin(-this.yRot * (float) (Math.PI / 180.0)) * f), 0.0, (double)(Mth.cos(this.yRot * (float) (Math.PI / 180.0)) * f))
			);
			this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
		}
	}

	@Override
	public void positionRider(Entity entity) {
		if (this.hasPassenger(entity)) {
			float f = 0.0F;
			float g = (float)((this.removed ? 0.01F : this.getRideHeight()) + entity.getRidingHeight());
			if (this.getPassengers().size() > 1) {
				int i = this.getPassengers().indexOf(entity);
				if (i == 0) {
					f = 0.2F;
				} else {
					f = -0.6F;
				}

				if (entity instanceof Animal) {
					f = (float)((double)f + 0.2);
				}
			}

			Vec3 vec3 = new Vec3((double)f, 0.0, 0.0).yRot(-this.yRot * (float) (Math.PI / 180.0) - (float) (Math.PI / 2));
			entity.setPos(this.x + vec3.x, this.y + (double)g, this.z + vec3.z);
			entity.yRot = entity.yRot + this.deltaRotation;
			entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
			this.clampRotation(entity);
			if (entity instanceof Animal && this.getPassengers().size() > 1) {
				int j = entity.getId() % 2 == 0 ? 90 : 270;
				entity.setYBodyRot(((Animal)entity).yBodyRot + (float)j);
				entity.setYHeadRot(entity.getYHeadRot() + (float)j);
			}
		}
	}

	protected void clampRotation(Entity entity) {
		entity.setYBodyRot(this.yRot);
		float f = Mth.wrapDegrees(entity.yRot - this.yRot);
		float g = Mth.clamp(f, -105.0F, 105.0F);
		entity.yRotO += g - f;
		entity.yRot += g - f;
		entity.setYHeadRot(entity.yRot);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onPassengerTurned(Entity entity) {
		this.clampRotation(entity);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putString("Type", this.getBoatType().getName());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("Type", 8)) {
			this.setType(Boat.Type.byName(compoundTag.getString("Type")));
		}
	}

	@Override
	public boolean interact(Player player, InteractionHand interactionHand) {
		if (player.isSecondaryUseActive()) {
			return false;
		} else {
			if (!this.level.isClientSide && this.outOfControlTicks < 60.0F) {
				player.startRiding(this);
			}

			return true;
		}
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		this.lastYd = this.getDeltaMovement().y;
		if (!this.isPassenger()) {
			if (bl) {
				if (this.fallDistance > 3.0F) {
					if (this.status != Boat.Status.ON_LAND) {
						this.fallDistance = 0.0F;
						return;
					}

					this.causeFallDamage(this.fallDistance, 1.0F);
					if (!this.level.isClientSide && !this.removed) {
						this.remove();
						if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
							for (int i = 0; i < 3; i++) {
								this.spawnAtLocation(this.getBoatType().getPlanks());
							}

							for (int i = 0; i < 2; i++) {
								this.spawnAtLocation(Items.STICK);
							}
						}
					}
				}

				this.fallDistance = 0.0F;
			} else if (!this.level.getFluidState(new BlockPos(this).below()).is(FluidTags.WATER) && d < 0.0) {
				this.fallDistance = (float)((double)this.fallDistance - d);
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

	@Environment(EnvType.CLIENT)
	public float getBubbleAngle(float f) {
		return Mth.lerp(f, this.bubbleAngleO, this.bubbleAngle);
	}

	public void setHurtDir(int i) {
		this.entityData.set(DATA_ID_HURTDIR, i);
	}

	public int getHurtDir() {
		return this.entityData.get(DATA_ID_HURTDIR);
	}

	public void setType(Boat.Type type) {
		this.entityData.set(DATA_ID_TYPE, type.ordinal());
	}

	public Boat.Type getBoatType() {
		return Boat.Type.byId(this.entityData.get(DATA_ID_TYPE));
	}

	@Override
	protected boolean canAddPassenger(Entity entity) {
		return this.getPassengers().size() < 2 && !this.isUnderLiquid(FluidTags.WATER);
	}

	@Nullable
	@Override
	public Entity getControllingPassenger() {
		List<Entity> list = this.getPassengers();
		return list.isEmpty() ? null : (Entity)list.get(0);
	}

	@Environment(EnvType.CLIENT)
	public void setInput(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		this.inputLeft = bl;
		this.inputRight = bl2;
		this.inputUp = bl3;
		this.inputDown = bl4;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	public static enum Status {
		IN_WATER,
		UNDER_WATER,
		UNDER_FLOWING_WATER,
		ON_LAND,
		IN_AIR;
	}

	public static enum Type {
		OAK(Blocks.OAK_PLANKS, "oak"),
		SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
		BIRCH(Blocks.BIRCH_PLANKS, "birch"),
		JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
		ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
		DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak");

		private final String name;
		private final Block planks;

		private Type(Block block, String string2) {
			this.name = string2;
			this.planks = block;
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
			Boat.Type[] types = values();
			if (i < 0 || i >= types.length) {
				i = 0;
			}

			return types[i];
		}

		public static Boat.Type byName(String string) {
			Boat.Type[] types = values();

			for (int i = 0; i < types.length; i++) {
				if (types[i].getName().equals(string)) {
					return types[i];
				}
			}

			return types[0];
		}
	}
}
