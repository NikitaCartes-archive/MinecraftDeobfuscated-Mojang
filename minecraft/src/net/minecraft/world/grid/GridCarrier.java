package net.minecraft.world.grid;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddSubGridPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GridCarrier extends Entity {
	public static final int LERP_STEPS = 2;
	private static final EntityDataAccessor<Direction> MOVEMENT_DIRECTION = SynchedEntityData.defineId(GridCarrier.class, EntityDataSerializers.DIRECTION);
	private static final EntityDataAccessor<Float> MOVEMENT_SPEED = SynchedEntityData.defineId(GridCarrier.class, EntityDataSerializers.FLOAT);
	private final SubGrid grid;
	@Nullable
	private SubGridMovementCollider movementCollider;
	@Nullable
	private GridCarrier.PosInterpolationTarget posInterpolationTarget;
	private int placeInTicks;

	public GridCarrier(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.grid = level.createSubGrid(this);
		this.noCulling = true;
	}

	public void setMovement(Direction direction, float f) {
		this.getEntityData().set(MOVEMENT_DIRECTION, direction);
		this.getEntityData().set(MOVEMENT_SPEED, f);
		this.movementCollider = SubGridMovementCollider.generate(this.grid.getBlocks(), direction);
	}

	public void clearMovement() {
		this.getEntityData().set(MOVEMENT_SPEED, 0.0F);
		this.movementCollider = null;
	}

	public SubGrid grid() {
		return this.grid;
	}

	@Override
	public void setPos(double d, double e, double f) {
		super.setPos(d, e, f);
		if (this.grid != null) {
			this.grid.updatePosition(d, e, f);
		}
	}

	@Override
	public void tick() {
		super.tick();
		Direction direction = this.getMovementDirection();
		this.grid.getBlocks().tick(this.level(), this.position(), direction);
		if (this.level().isClientSide()) {
			this.tickClient();
		} else {
			this.tickServer();
		}
	}

	private void tickClient() {
		if (this.posInterpolationTarget != null) {
			this.posInterpolationTarget.applyLerpStep(this);
			if (--this.posInterpolationTarget.steps == 0) {
				this.posInterpolationTarget = null;
			}
		}
	}

	private void tickServer() {
		Direction direction = this.getMovementDirection();
		float f = this.getMovementSpeed();
		if (this.placeInTicks == 0 && f == 0.0F) {
			this.placeInTicks = 2;
		}

		if (this.placeInTicks > 0) {
			this.placeInTicks--;
			if (this.placeInTicks == 1) {
				this.grid.getBlocks().place(this.blockPosition(), this.level());
			} else if (this.placeInTicks == 0) {
				this.discard();
			}
		} else if (this.movementCollider != null) {
			this.tickMovement(this.movementCollider, direction, f);
		}
	}

	private void tickMovement(SubGridMovementCollider subGridMovementCollider, Direction direction, float f) {
		Vec3 vec3 = this.position();
		Vec3 vec32 = vec3.add((double)((float)direction.getStepX() * f), (double)((float)direction.getStepY() * f), (double)((float)direction.getStepZ() * f));
		BlockPos blockPos = this.getCollidingPos(vec3, direction);
		BlockPos blockPos2 = this.getCollidingPos(vec32, direction);
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		while (!mutableBlockPos.equals(blockPos2)) {
			mutableBlockPos.move(direction);
			if (subGridMovementCollider.checkCollision(this.level(), mutableBlockPos)) {
				BlockPos blockPos3 = mutableBlockPos.relative(direction, -1);
				this.setPos(Vec3.atLowerCornerOf(blockPos3));
				this.clearMovement();
				this.placeInTicks = 5;
				return;
			}
		}

		this.setPos(vec32);
	}

	private BlockPos getCollidingPos(Vec3 vec3, Direction direction) {
		BlockPos blockPos = BlockPos.containing(vec3);
		return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? blockPos.relative(direction) : blockPos;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(MOVEMENT_DIRECTION, Direction.NORTH);
		builder.define(MOVEMENT_SPEED, 0.0F);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.grid.setBlocks(SubGridBlocks.decode(this.registryAccess().lookupOrThrow(Registries.BLOCK), compoundTag.getCompound("blocks")));
		if (compoundTag.contains("biome", 8)) {
			this.registryAccess().registryOrThrow(Registries.BIOME).getHolder(new ResourceLocation(compoundTag.getString("biome"))).ifPresent(this.grid::setBiome);
		}

		if (compoundTag.contains("movement_direction", 8)) {
			Direction direction = Direction.byName(compoundTag.getString("movement_direction"));
			if (direction != null) {
				this.setMovement(direction, compoundTag.getFloat("movement_speed"));
			}
		} else {
			this.clearMovement();
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.put("blocks", this.grid.getBlocks().encode());
		this.grid.getBiome().unwrapKey().ifPresent(resourceKey -> compoundTag.putString("biome", resourceKey.location().toString()));
		compoundTag.putString("movement_direction", this.getMovementDirection().getSerializedName());
		compoundTag.putFloat("movement_speed", this.getMovementSpeed());
	}

	private float getMovementSpeed() {
		return this.getEntityData().get(MOVEMENT_SPEED);
	}

	private Direction getMovementDirection() {
		return this.getEntityData().get(MOVEMENT_DIRECTION);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddSubGridPacket(this);
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.posInterpolationTarget = new GridCarrier.PosInterpolationTarget(2, d, e, f);
	}

	@Override
	public double lerpTargetX() {
		return this.posInterpolationTarget != null ? this.posInterpolationTarget.targetX : this.getX();
	}

	@Override
	public double lerpTargetY() {
		return this.posInterpolationTarget != null ? this.posInterpolationTarget.targetY : this.getY();
	}

	@Override
	public double lerpTargetZ() {
		return this.posInterpolationTarget != null ? this.posInterpolationTarget.targetZ : this.getZ();
	}

	static class PosInterpolationTarget {
		int steps;
		final double targetX;
		final double targetY;
		final double targetZ;

		PosInterpolationTarget(int i, double d, double e, double f) {
			this.steps = i;
			this.targetX = d;
			this.targetY = e;
			this.targetZ = f;
		}

		void applyLerpStep(Entity entity) {
			entity.lerpPositionAndRotationStep(this.steps, this.targetX, this.targetY, this.targetZ, 0.0, 0.0);
		}
	}
}
