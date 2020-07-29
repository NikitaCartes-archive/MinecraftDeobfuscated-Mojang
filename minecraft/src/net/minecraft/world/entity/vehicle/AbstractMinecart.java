package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecart extends Entity {
	private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
	private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(
		Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1)
	);
	private boolean flipped;
	private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), enumMap -> {
		Vec3i vec3i = Direction.WEST.getNormal();
		Vec3i vec3i2 = Direction.EAST.getNormal();
		Vec3i vec3i3 = Direction.NORTH.getNormal();
		Vec3i vec3i4 = Direction.SOUTH.getNormal();
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
	private int lSteps;
	private double lx;
	private double ly;
	private double lz;
	private double lyr;
	private double lxr;
	@Environment(EnvType.CLIENT)
	private double lxd;
	@Environment(EnvType.CLIENT)
	private double lyd;
	@Environment(EnvType.CLIENT)
	private double lzd;

	protected AbstractMinecart(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
	}

	protected AbstractMinecart(EntityType<?> entityType, Level level, double d, double e, double f) {
		this(entityType, level);
		this.setPos(d, e, f);
		this.setDeltaMovement(Vec3.ZERO);
		this.xo = d;
		this.yo = e;
		this.zo = f;
	}

	public static AbstractMinecart createMinecart(Level level, double d, double e, double f, AbstractMinecart.Type type) {
		if (type == AbstractMinecart.Type.CHEST) {
			return new MinecartChest(level, d, e, f);
		} else if (type == AbstractMinecart.Type.FURNACE) {
			return new MinecartFurnace(level, d, e, f);
		} else if (type == AbstractMinecart.Type.TNT) {
			return new MinecartTNT(level, d, e, f);
		} else if (type == AbstractMinecart.Type.SPAWNER) {
			return new MinecartSpawner(level, d, e, f);
		} else if (type == AbstractMinecart.Type.HOPPER) {
			return new MinecartHopper(level, d, e, f);
		} else {
			return (AbstractMinecart)(type == AbstractMinecart.Type.COMMAND_BLOCK ? new MinecartCommandBlock(level, d, e, f) : new Minecart(level, d, e, f));
		}
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
		this.entityData.define(DATA_ID_DISPLAY_BLOCK, Block.getId(Blocks.AIR.defaultBlockState()));
		this.entityData.define(DATA_ID_DISPLAY_OFFSET, 6);
		this.entityData.define(DATA_ID_CUSTOM_DISPLAY, false);
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
	public double getPassengersRidingOffset() {
		return 0.0;
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
				float f = Math.min(entityDimensions.width, 1.0F) / 2.0F;

				for (int i : POSE_DISMOUNT_HEIGHTS.get(pose)) {
					for (int[] js : is) {
						mutableBlockPos.set(blockPos.getX() + js[0], blockPos.getY() + i, blockPos.getZ() + js[1]);
						double d = this.level
							.getBlockFloorHeight(
								DismountHelper.nonClimbableShape(this.level, mutableBlockPos), () -> DismountHelper.nonClimbableShape(this.level, mutableBlockPos.below())
							);
						if (DismountHelper.isBlockFloorValid(d)) {
							AABB aABB = new AABB((double)(-f), 0.0, (double)(-f), (double)f, (double)entityDimensions.height, (double)f);
							Vec3 vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, d);
							if (DismountHelper.canDismountTo(this.level, livingEntity, aABB.move(vec3))) {
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
				double g = (double)livingEntity.getDimensions(pose2).height;
				int j = Mth.ceil(e - (double)mutableBlockPos.getY() + g);
				double h = DismountHelper.findCeilingFrom(mutableBlockPos, j, blockPosx -> this.level.getBlockState(blockPosx).getCollisionShape(this.level, blockPosx));
				if (e + g <= h) {
					livingEntity.setPose(pose2);
					break;
				}
			}

			return super.getDismountLocationForPassenger(livingEntity);
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.level.isClientSide || this.removed) {
			return true;
		} else if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.setHurtDir(-this.getHurtDir());
			this.setHurtTime(10);
			this.markHurt();
			this.setDamage(this.getDamage() + f * 10.0F);
			boolean bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).abilities.instabuild;
			if (bl || this.getDamage() > 40.0F) {
				this.ejectPassengers();
				if (bl && !this.hasCustomName()) {
					this.remove();
				} else {
					this.destroy(damageSource);
				}
			}

			return true;
		}
	}

	@Override
	protected float getBlockSpeedFactor() {
		BlockState blockState = this.level.getBlockState(this.blockPosition());
		return blockState.is(BlockTags.RAILS) ? 1.0F : super.getBlockSpeedFactor();
	}

	public void destroy(DamageSource damageSource) {
		this.remove();
		if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			ItemStack itemStack = new ItemStack(Items.MINECART);
			if (this.hasCustomName()) {
				itemStack.setHoverName(this.getCustomName());
			}

			this.spawnAtLocation(itemStack);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateHurt() {
		this.setHurtDir(-this.getHurtDir());
		this.setHurtTime(10);
		this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
	}

	@Override
	public boolean isPickable() {
		return !this.removed;
	}

	private static Pair<Vec3i, Vec3i> exits(RailShape railShape) {
		return (Pair<Vec3i, Vec3i>)EXITS.get(railShape);
	}

	@Override
	public Direction getMotionDirection() {
		return this.flipped ? this.getDirection().getOpposite().getClockWise() : this.getDirection().getClockWise();
	}

	@Override
	public void tick() {
		if (this.getHurtTime() > 0) {
			this.setHurtTime(this.getHurtTime() - 1);
		}

		if (this.getDamage() > 0.0F) {
			this.setDamage(this.getDamage() - 1.0F);
		}

		if (this.getY() < -64.0) {
			this.outOfWorld();
		}

		this.handleNetherPortal();
		if (this.level.isClientSide) {
			if (this.lSteps > 0) {
				double d = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
				double e = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
				double f = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
				double g = Mth.wrapDegrees(this.lyr - (double)this.yRot);
				this.yRot = (float)((double)this.yRot + g / (double)this.lSteps);
				this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
				this.lSteps--;
				this.setPos(d, e, f);
				this.setRot(this.yRot, this.xRot);
			} else {
				this.reapplyPosition();
				this.setRot(this.yRot, this.xRot);
			}
		} else {
			if (!this.isNoGravity()) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
			}

			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY());
			int k = Mth.floor(this.getZ());
			if (this.level.getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
				j--;
			}

			BlockPos blockPos = new BlockPos(i, j, k);
			BlockState blockState = this.level.getBlockState(blockPos);
			if (BaseRailBlock.isRail(blockState)) {
				this.moveAlongTrack(blockPos, blockState);
				if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
					this.activateMinecart(i, j, k, (Boolean)blockState.getValue(PoweredRailBlock.POWERED));
				}
			} else {
				this.comeOffTrack();
			}

			this.checkInsideBlocks();
			this.xRot = 0.0F;
			double h = this.xo - this.getX();
			double l = this.zo - this.getZ();
			if (h * h + l * l > 0.001) {
				this.yRot = (float)(Mth.atan2(l, h) * 180.0 / Math.PI);
				if (this.flipped) {
					this.yRot += 180.0F;
				}
			}

			double m = (double)Mth.wrapDegrees(this.yRot - this.yRotO);
			if (m < -170.0 || m >= 170.0) {
				this.yRot += 180.0F;
				this.flipped = !this.flipped;
			}

			this.setRot(this.yRot, this.xRot);
			if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && getHorizontalDistanceSqr(this.getDeltaMovement()) > 0.01) {
				List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, 0.0, 0.2F), EntitySelector.pushableBy(this));
				if (!list.isEmpty()) {
					for (int n = 0; n < list.size(); n++) {
						Entity entity = (Entity)list.get(n);
						if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.isVehicle() && !entity.isPassenger()) {
							entity.startRiding(this);
						} else {
							entity.push(this);
						}
					}
				}
			} else {
				for (Entity entity2 : this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, 0.0, 0.2F))) {
					if (!this.hasPassenger(entity2) && entity2.isPushable() && entity2 instanceof AbstractMinecart) {
						entity2.push(this);
					}
				}
			}

			this.updateInWaterStateAndDoFluidPushing();
			if (this.isInLava()) {
				this.lavaHurt();
				this.fallDistance *= 0.5F;
			}

			this.firstTick = false;
		}
	}

	protected double getMaxSpeed() {
		return 0.4;
	}

	public void activateMinecart(int i, int j, int k, boolean bl) {
	}

	protected void comeOffTrack() {
		double d = this.getMaxSpeed();
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(Mth.clamp(vec3.x, -d, d), vec3.y, Mth.clamp(vec3.z, -d, d));
		if (this.onGround) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		if (!this.onGround) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
		}
	}

	protected void moveAlongTrack(BlockPos blockPos, BlockState blockState) {
		this.fallDistance = 0.0F;
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		Vec3 vec3 = this.getPos(d, e, f);
		e = (double)blockPos.getY();
		boolean bl = false;
		boolean bl2 = false;
		BaseRailBlock baseRailBlock = (BaseRailBlock)blockState.getBlock();
		if (baseRailBlock == Blocks.POWERED_RAIL) {
			bl = (Boolean)blockState.getValue(PoweredRailBlock.POWERED);
			bl2 = !bl;
		}

		double g = 0.0078125;
		Vec3 vec32 = this.getDeltaMovement();
		RailShape railShape = blockState.getValue(baseRailBlock.getShapeProperty());
		switch (railShape) {
			case ASCENDING_EAST:
				this.setDeltaMovement(vec32.add(-0.0078125, 0.0, 0.0));
				e++;
				break;
			case ASCENDING_WEST:
				this.setDeltaMovement(vec32.add(0.0078125, 0.0, 0.0));
				e++;
				break;
			case ASCENDING_NORTH:
				this.setDeltaMovement(vec32.add(0.0, 0.0, 0.0078125));
				e++;
				break;
			case ASCENDING_SOUTH:
				this.setDeltaMovement(vec32.add(0.0, 0.0, -0.0078125));
				e++;
		}

		vec32 = this.getDeltaMovement();
		Pair<Vec3i, Vec3i> pair = exits(railShape);
		Vec3i vec3i = pair.getFirst();
		Vec3i vec3i2 = pair.getSecond();
		double h = (double)(vec3i2.getX() - vec3i.getX());
		double i = (double)(vec3i2.getZ() - vec3i.getZ());
		double j = Math.sqrt(h * h + i * i);
		double k = vec32.x * h + vec32.z * i;
		if (k < 0.0) {
			h = -h;
			i = -i;
		}

		double l = Math.min(2.0, Math.sqrt(getHorizontalDistanceSqr(vec32)));
		vec32 = new Vec3(l * h / j, vec32.y, l * i / j);
		this.setDeltaMovement(vec32);
		Entity entity = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
		if (entity instanceof Player) {
			Vec3 vec33 = entity.getDeltaMovement();
			double m = getHorizontalDistanceSqr(vec33);
			double n = getHorizontalDistanceSqr(this.getDeltaMovement());
			if (m > 1.0E-4 && n < 0.01) {
				this.setDeltaMovement(this.getDeltaMovement().add(vec33.x * 0.1, 0.0, vec33.z * 0.1));
				bl2 = false;
			}
		}

		if (bl2) {
			double o = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
			if (o < 0.03) {
				this.setDeltaMovement(Vec3.ZERO);
			} else {
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
			}
		}

		double o = (double)blockPos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
		double p = (double)blockPos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
		double q = (double)blockPos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
		double r = (double)blockPos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
		h = q - o;
		i = r - p;
		double s;
		if (h == 0.0) {
			s = f - (double)blockPos.getZ();
		} else if (i == 0.0) {
			s = d - (double)blockPos.getX();
		} else {
			double t = d - o;
			double u = f - p;
			s = (t * h + u * i) * 2.0;
		}

		d = o + h * s;
		f = p + i * s;
		this.setPos(d, e, f);
		double t = this.isVehicle() ? 0.75 : 1.0;
		double u = this.getMaxSpeed();
		vec32 = this.getDeltaMovement();
		this.move(MoverType.SELF, new Vec3(Mth.clamp(t * vec32.x, -u, u), 0.0, Mth.clamp(t * vec32.z, -u, u)));
		if (vec3i.getY() != 0 && Mth.floor(this.getX()) - blockPos.getX() == vec3i.getX() && Mth.floor(this.getZ()) - blockPos.getZ() == vec3i.getZ()) {
			this.setPos(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
		} else if (vec3i2.getY() != 0 && Mth.floor(this.getX()) - blockPos.getX() == vec3i2.getX() && Mth.floor(this.getZ()) - blockPos.getZ() == vec3i2.getZ()) {
			this.setPos(this.getX(), this.getY() + (double)vec3i2.getY(), this.getZ());
		}

		this.applyNaturalSlowdown();
		Vec3 vec34 = this.getPos(this.getX(), this.getY(), this.getZ());
		if (vec34 != null && vec3 != null) {
			double v = (vec3.y - vec34.y) * 0.05;
			Vec3 vec35 = this.getDeltaMovement();
			double w = Math.sqrt(getHorizontalDistanceSqr(vec35));
			if (w > 0.0) {
				this.setDeltaMovement(vec35.multiply((w + v) / w, 1.0, (w + v) / w));
			}

			this.setPos(this.getX(), vec34.y, this.getZ());
		}

		int x = Mth.floor(this.getX());
		int y = Mth.floor(this.getZ());
		if (x != blockPos.getX() || y != blockPos.getZ()) {
			Vec3 vec35 = this.getDeltaMovement();
			double w = Math.sqrt(getHorizontalDistanceSqr(vec35));
			this.setDeltaMovement(w * (double)(x - blockPos.getX()), vec35.y, w * (double)(y - blockPos.getZ()));
		}

		if (bl) {
			Vec3 vec35 = this.getDeltaMovement();
			double w = Math.sqrt(getHorizontalDistanceSqr(vec35));
			if (w > 0.01) {
				double z = 0.06;
				this.setDeltaMovement(vec35.add(vec35.x / w * 0.06, 0.0, vec35.z / w * 0.06));
			} else {
				Vec3 vec36 = this.getDeltaMovement();
				double aa = vec36.x;
				double ab = vec36.z;
				if (railShape == RailShape.EAST_WEST) {
					if (this.isRedstoneConductor(blockPos.west())) {
						aa = 0.02;
					} else if (this.isRedstoneConductor(blockPos.east())) {
						aa = -0.02;
					}
				} else {
					if (railShape != RailShape.NORTH_SOUTH) {
						return;
					}

					if (this.isRedstoneConductor(blockPos.north())) {
						ab = 0.02;
					} else if (this.isRedstoneConductor(blockPos.south())) {
						ab = -0.02;
					}
				}

				this.setDeltaMovement(aa, vec36.y, ab);
			}
		}
	}

	private boolean isRedstoneConductor(BlockPos blockPos) {
		return this.level.getBlockState(blockPos).isRedstoneConductor(this.level, blockPos);
	}

	protected void applyNaturalSlowdown() {
		double d = this.isVehicle() ? 0.997 : 0.96;
		this.setDeltaMovement(this.getDeltaMovement().multiply(d, 0.0, d));
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Vec3 getPosOffs(double d, double e, double f, double g) {
		int i = Mth.floor(d);
		int j = Mth.floor(e);
		int k = Mth.floor(f);
		if (this.level.getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
			j--;
		}

		BlockState blockState = this.level.getBlockState(new BlockPos(i, j, k));
		if (BaseRailBlock.isRail(blockState)) {
			RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
			e = (double)j;
			if (railShape.isAscending()) {
				e = (double)(j + 1);
			}

			Pair<Vec3i, Vec3i> pair = exits(railShape);
			Vec3i vec3i = pair.getFirst();
			Vec3i vec3i2 = pair.getSecond();
			double h = (double)(vec3i2.getX() - vec3i.getX());
			double l = (double)(vec3i2.getZ() - vec3i.getZ());
			double m = Math.sqrt(h * h + l * l);
			h /= m;
			l /= m;
			d += h * g;
			f += l * g;
			if (vec3i.getY() != 0 && Mth.floor(d) - i == vec3i.getX() && Mth.floor(f) - k == vec3i.getZ()) {
				e += (double)vec3i.getY();
			} else if (vec3i2.getY() != 0 && Mth.floor(d) - i == vec3i2.getX() && Mth.floor(f) - k == vec3i2.getZ()) {
				e += (double)vec3i2.getY();
			}

			return this.getPos(d, e, f);
		} else {
			return null;
		}
	}

	@Nullable
	public Vec3 getPos(double d, double e, double f) {
		int i = Mth.floor(d);
		int j = Mth.floor(e);
		int k = Mth.floor(f);
		if (this.level.getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
			j--;
		}

		BlockState blockState = this.level.getBlockState(new BlockPos(i, j, k));
		if (BaseRailBlock.isRail(blockState)) {
			RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
			Pair<Vec3i, Vec3i> pair = exits(railShape);
			Vec3i vec3i = pair.getFirst();
			Vec3i vec3i2 = pair.getSecond();
			double g = (double)i + 0.5 + (double)vec3i.getX() * 0.5;
			double h = (double)j + 0.0625 + (double)vec3i.getY() * 0.5;
			double l = (double)k + 0.5 + (double)vec3i.getZ() * 0.5;
			double m = (double)i + 0.5 + (double)vec3i2.getX() * 0.5;
			double n = (double)j + 0.0625 + (double)vec3i2.getY() * 0.5;
			double o = (double)k + 0.5 + (double)vec3i2.getZ() * 0.5;
			double p = m - g;
			double q = (n - h) * 2.0;
			double r = o - l;
			double s;
			if (p == 0.0) {
				s = f - (double)k;
			} else if (r == 0.0) {
				s = d - (double)i;
			} else {
				double t = d - g;
				double u = f - l;
				s = (t * p + u * r) * 2.0;
			}

			d = g + p * s;
			e = h + q * s;
			f = l + r * s;
			if (q < 0.0) {
				e++;
			} else if (q > 0.0) {
				e += 0.5;
			}

			return new Vec3(d, e, f);
		} else {
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public AABB getBoundingBoxForCulling() {
		AABB aABB = this.getBoundingBox();
		return this.hasCustomDisplay() ? aABB.inflate((double)Math.abs(this.getDisplayOffset()) / 16.0) : aABB;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.getBoolean("CustomDisplayTile")) {
			this.setDisplayBlockState(NbtUtils.readBlockState(compoundTag.getCompound("DisplayState")));
			this.setDisplayOffset(compoundTag.getInt("DisplayOffset"));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		if (this.hasCustomDisplay()) {
			compoundTag.putBoolean("CustomDisplayTile", true);
			compoundTag.put("DisplayState", NbtUtils.writeBlockState(this.getDisplayBlockState()));
			compoundTag.putInt("DisplayOffset", this.getDisplayOffset());
		}
	}

	@Override
	public void push(Entity entity) {
		if (!this.level.isClientSide) {
			if (!entity.noPhysics && !this.noPhysics) {
				if (!this.hasPassenger(entity)) {
					double d = entity.getX() - this.getX();
					double e = entity.getZ() - this.getZ();
					double f = d * d + e * e;
					if (f >= 1.0E-4F) {
						f = (double)Mth.sqrt(f);
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
						d *= (double)(1.0F - this.pushthrough);
						e *= (double)(1.0F - this.pushthrough);
						d *= 0.5;
						e *= 0.5;
						if (entity instanceof AbstractMinecart) {
							double h = entity.getX() - this.getX();
							double i = entity.getZ() - this.getZ();
							Vec3 vec3 = new Vec3(h, 0.0, i).normalize();
							Vec3 vec32 = new Vec3((double)Mth.cos(this.yRot * (float) (Math.PI / 180.0)), 0.0, (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0))).normalize();
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

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
		this.lx = d;
		this.ly = e;
		this.lz = f;
		this.lyr = (double)g;
		this.lxr = (double)h;
		this.lSteps = i + 2;
		this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpMotion(double d, double e, double f) {
		this.lxd = d;
		this.lyd = e;
		this.lzd = f;
		this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
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

	public void setHurtDir(int i) {
		this.entityData.set(DATA_ID_HURTDIR, i);
	}

	public int getHurtDir() {
		return this.entityData.get(DATA_ID_HURTDIR);
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
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
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
