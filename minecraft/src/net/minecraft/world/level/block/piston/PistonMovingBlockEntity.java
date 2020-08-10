package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonMovingBlockEntity extends BlockEntity implements TickableBlockEntity {
	private BlockState movedState;
	private Direction direction;
	private boolean extending;
	private boolean isSourcePiston;
	private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);
	private float progress;
	private float progressO;
	private long lastTicked;
	private int deathTicks;

	public PistonMovingBlockEntity() {
		super(BlockEntityType.PISTON);
	}

	public PistonMovingBlockEntity(BlockState blockState, Direction direction, boolean bl, boolean bl2) {
		this();
		this.movedState = blockState;
		this.direction = direction;
		this.extending = bl;
		this.isSourcePiston = bl2;
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	public boolean isExtending() {
		return this.extending;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public boolean isSourcePiston() {
		return this.isSourcePiston;
	}

	public float getProgress(float f) {
		if (f > 1.0F) {
			f = 1.0F;
		}

		return Mth.lerp(f, this.progressO, this.progress);
	}

	@Environment(EnvType.CLIENT)
	public float getXOff(float f) {
		return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(f));
	}

	@Environment(EnvType.CLIENT)
	public float getYOff(float f) {
		return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(f));
	}

	@Environment(EnvType.CLIENT)
	public float getZOff(float f) {
		return (float)this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(f));
	}

	private float getExtendedProgress(float f) {
		return this.extending ? f - 1.0F : 1.0F - f;
	}

	private BlockState getCollisionRelatedBlockState() {
		return !this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof PistonBaseBlock
			? Blocks.PISTON_HEAD
				.defaultBlockState()
				.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.progress > 0.25F))
				.setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)
				.setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING))
			: this.movedState;
	}

	private void moveCollidedEntities(float f) {
		Direction direction = this.getMovementDirection();
		double d = (double)(f - this.progress);
		VoxelShape voxelShape = this.getCollisionRelatedBlockState().getCollisionShape(this.level, this.getBlockPos());
		if (!voxelShape.isEmpty()) {
			AABB aABB = this.moveByPositionAndProgress(voxelShape.bounds());
			List<Entity> list = this.level.getEntities(null, PistonMath.getMovementArea(aABB, direction, d).minmax(aABB));
			if (!list.isEmpty()) {
				List<AABB> list2 = voxelShape.toAabbs();
				boolean bl = this.movedState.is(Blocks.SLIME_BLOCK);
				Iterator var10 = list.iterator();

				while (true) {
					Entity entity;
					while (true) {
						if (!var10.hasNext()) {
							return;
						}

						entity = (Entity)var10.next();
						if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
							if (!bl) {
								break;
							}

							if (!(entity instanceof ServerPlayer)) {
								Vec3 vec3 = entity.getDeltaMovement();
								double e = vec3.x;
								double g = vec3.y;
								double h = vec3.z;
								switch (direction.getAxis()) {
									case X:
										e = (double)direction.getStepX();
										break;
									case Y:
										g = (double)direction.getStepY();
										break;
									case Z:
										h = (double)direction.getStepZ();
								}

								entity.setDeltaMovement(e, g, h);
								break;
							}
						}
					}

					double i = 0.0;

					for (AABB aABB2 : list2) {
						AABB aABB3 = PistonMath.getMovementArea(this.moveByPositionAndProgress(aABB2), direction, d);
						AABB aABB4 = entity.getBoundingBox();
						if (aABB3.intersects(aABB4)) {
							i = Math.max(i, getMovement(aABB3, direction, aABB4));
							if (i >= d) {
								break;
							}
						}
					}

					if (!(i <= 0.0)) {
						i = Math.min(i, d) + 0.01;
						moveEntityByPiston(direction, entity, i, direction);
						if (!this.extending && this.isSourcePiston) {
							this.fixEntityWithinPistonBase(entity, direction, d);
						}
					}
				}
			}
		}
	}

	private static void moveEntityByPiston(Direction direction, Entity entity, double d, Direction direction2) {
		NOCLIP.set(direction);
		entity.move(MoverType.PISTON, new Vec3(d * (double)direction2.getStepX(), d * (double)direction2.getStepY(), d * (double)direction2.getStepZ()));
		NOCLIP.set(null);
	}

	private void moveStuckEntities(float f) {
		if (this.isStickyForEntities()) {
			Direction direction = this.getMovementDirection();
			if (direction.getAxis().isHorizontal()) {
				double d = this.movedState.getCollisionShape(this.level, this.worldPosition).max(Direction.Axis.Y);
				AABB aABB = this.moveByPositionAndProgress(new AABB(0.0, d, 0.0, 1.0, 1.5000000999999998, 1.0));
				double e = (double)(f - this.progress);

				for (Entity entity : this.level.getEntities((Entity)null, aABB, entityx -> matchesStickyCritera(aABB, entityx))) {
					moveEntityByPiston(direction, entity, e, direction);
				}
			}
		}
	}

	private static boolean matchesStickyCritera(AABB aABB, Entity entity) {
		return entity.getPistonPushReaction() == PushReaction.NORMAL
			&& entity.isOnGround()
			&& entity.getX() >= aABB.minX
			&& entity.getX() <= aABB.maxX
			&& entity.getZ() >= aABB.minZ
			&& entity.getZ() <= aABB.maxZ;
	}

	private boolean isStickyForEntities() {
		return this.movedState.is(Blocks.HONEY_BLOCK);
	}

	public Direction getMovementDirection() {
		return this.extending ? this.direction : this.direction.getOpposite();
	}

	private static double getMovement(AABB aABB, Direction direction, AABB aABB2) {
		switch (direction) {
			case EAST:
				return aABB.maxX - aABB2.minX;
			case WEST:
				return aABB2.maxX - aABB.minX;
			case UP:
			default:
				return aABB.maxY - aABB2.minY;
			case DOWN:
				return aABB2.maxY - aABB.minY;
			case SOUTH:
				return aABB.maxZ - aABB2.minZ;
			case NORTH:
				return aABB2.maxZ - aABB.minZ;
		}
	}

	private AABB moveByPositionAndProgress(AABB aABB) {
		double d = (double)this.getExtendedProgress(this.progress);
		return aABB.move(
			(double)this.worldPosition.getX() + d * (double)this.direction.getStepX(),
			(double)this.worldPosition.getY() + d * (double)this.direction.getStepY(),
			(double)this.worldPosition.getZ() + d * (double)this.direction.getStepZ()
		);
	}

	private void fixEntityWithinPistonBase(Entity entity, Direction direction, double d) {
		AABB aABB = entity.getBoundingBox();
		AABB aABB2 = Shapes.block().bounds().move(this.worldPosition);
		if (aABB.intersects(aABB2)) {
			Direction direction2 = direction.getOpposite();
			double e = getMovement(aABB2, direction2, aABB) + 0.01;
			double f = getMovement(aABB2, direction2, aABB.intersect(aABB2)) + 0.01;
			if (Math.abs(e - f) < 0.01) {
				e = Math.min(e, d) + 0.01;
				moveEntityByPiston(direction, entity, e, direction2);
			}
		}
	}

	public BlockState getMovedState() {
		return this.movedState;
	}

	public void finalTick() {
		if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide)) {
			this.progress = 1.0F;
			this.progressO = this.progress;
			this.level.removeBlockEntity(this.worldPosition);
			this.setRemoved();
			if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
				BlockState blockState;
				if (this.isSourcePiston) {
					blockState = Blocks.AIR.defaultBlockState();
				} else {
					blockState = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
				}

				this.level.setBlock(this.worldPosition, blockState, 3);
				this.level.neighborChanged(this.worldPosition, blockState.getBlock(), this.worldPosition);
			}
		}
	}

	@Override
	public void tick() {
		this.lastTicked = this.level.getGameTime();
		this.progressO = this.progress;
		if (this.progressO >= 1.0F) {
			if (this.level.isClientSide && this.deathTicks < 5) {
				this.deathTicks++;
			} else {
				this.level.removeBlockEntity(this.worldPosition);
				this.setRemoved();
				if (this.movedState != null && this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
					BlockState blockState = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
					if (blockState.isAir()) {
						this.level.setBlock(this.worldPosition, this.movedState, 84);
						Block.updateOrDestroy(this.movedState, blockState, this.level, this.worldPosition, 3);
					} else {
						if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && (Boolean)blockState.getValue(BlockStateProperties.WATERLOGGED)) {
							blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
						}

						this.level.setBlock(this.worldPosition, blockState, 67);
						this.level.neighborChanged(this.worldPosition, blockState.getBlock(), this.worldPosition);
					}
				}
			}
		} else {
			float f = this.progress + 0.5F;
			this.moveCollidedEntities(f);
			this.moveStuckEntities(f);
			this.progress = f;
			if (this.progress >= 1.0F) {
				this.progress = 1.0F;
			}
		}
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
		this.movedState = NbtUtils.readBlockState(compoundTag.getCompound("blockState"));
		this.direction = Direction.from3DDataValue(compoundTag.getInt("facing"));
		this.progress = compoundTag.getFloat("progress");
		this.progressO = this.progress;
		this.extending = compoundTag.getBoolean("extending");
		this.isSourcePiston = compoundTag.getBoolean("source");
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.put("blockState", NbtUtils.writeBlockState(this.movedState));
		compoundTag.putInt("facing", this.direction.get3DDataValue());
		compoundTag.putFloat("progress", this.progressO);
		compoundTag.putBoolean("extending", this.extending);
		compoundTag.putBoolean("source", this.isSourcePiston);
		return compoundTag;
	}

	public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
		VoxelShape voxelShape;
		if (!this.extending && this.isSourcePiston) {
			voxelShape = this.movedState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true)).getCollisionShape(blockGetter, blockPos);
		} else {
			voxelShape = Shapes.empty();
		}

		Direction direction = (Direction)NOCLIP.get();
		if ((double)this.progress < 1.0 && direction == this.getMovementDirection()) {
			return voxelShape;
		} else {
			BlockState blockState;
			if (this.isSourcePiston()) {
				blockState = Blocks.PISTON_HEAD
					.defaultBlockState()
					.setValue(PistonHeadBlock.FACING, this.direction)
					.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 0.25F));
			} else {
				blockState = this.movedState;
			}

			float f = this.getExtendedProgress(this.progress);
			double d = (double)((float)this.direction.getStepX() * f);
			double e = (double)((float)this.direction.getStepY() * f);
			double g = (double)((float)this.direction.getStepZ() * f);
			return Shapes.or(voxelShape, blockState.getCollisionShape(blockGetter, blockPos).move(d, e, g));
		}
	}

	public long getLastTicked() {
		return this.lastTicked;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public double getViewDistance() {
		return 68.0;
	}
}
