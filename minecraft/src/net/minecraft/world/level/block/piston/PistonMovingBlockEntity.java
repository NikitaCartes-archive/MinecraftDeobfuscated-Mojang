package net.minecraft.world.level.block.piston;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
				.setValue(PistonHeadBlock.TYPE, this.movedState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT)
				.setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING))
			: this.movedState;
	}

	private void moveCollidedEntities(float f) {
		Direction direction = this.getMovementDirection();
		double d = (double)(f - this.progress);
		VoxelShape voxelShape = this.getCollisionRelatedBlockState().getCollisionShape(this.level, this.getBlockPos());
		if (!voxelShape.isEmpty()) {
			List<AABB> list = voxelShape.toAabbs();
			AABB aABB = this.moveByPositionAndProgress(this.getMinMaxPiecesAABB(list));
			List<Entity> list2 = this.level.getEntities(null, this.getMovementArea(aABB, direction, d).minmax(aABB));
			if (!list2.isEmpty()) {
				boolean bl = this.movedState.getBlock() == Blocks.SLIME_BLOCK;

				for (int i = 0; i < list2.size(); i++) {
					Entity entity = (Entity)list2.get(i);
					if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
						if (bl) {
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
						}

						double j = 0.0;

						for (int k = 0; k < list.size(); k++) {
							AABB aABB2 = this.getMovementArea(this.moveByPositionAndProgress((AABB)list.get(k)), direction, d);
							AABB aABB3 = entity.getBoundingBox();
							if (aABB2.intersects(aABB3)) {
								j = Math.max(j, this.getMovement(aABB2, direction, aABB3));
								if (j >= d) {
									break;
								}
							}
						}

						if (!(j <= 0.0)) {
							j = Math.min(j, d) + 0.01;
							NOCLIP.set(direction);
							entity.move(MoverType.PISTON, new Vec3(j * (double)direction.getStepX(), j * (double)direction.getStepY(), j * (double)direction.getStepZ()));
							NOCLIP.set(null);
							if (!this.extending && this.isSourcePiston) {
								this.fixEntityWithinPistonBase(entity, direction, d);
							}
						}
					}
				}
			}
		}
	}

	public Direction getMovementDirection() {
		return this.extending ? this.direction : this.direction.getOpposite();
	}

	private AABB getMinMaxPiecesAABB(List<AABB> list) {
		double d = 0.0;
		double e = 0.0;
		double f = 0.0;
		double g = 1.0;
		double h = 1.0;
		double i = 1.0;

		for (AABB aABB : list) {
			d = Math.min(aABB.minX, d);
			e = Math.min(aABB.minY, e);
			f = Math.min(aABB.minZ, f);
			g = Math.max(aABB.maxX, g);
			h = Math.max(aABB.maxY, h);
			i = Math.max(aABB.maxZ, i);
		}

		return new AABB(d, e, f, g, h, i);
	}

	private double getMovement(AABB aABB, Direction direction, AABB aABB2) {
		switch (direction.getAxis()) {
			case X:
				return getDeltaX(aABB, direction, aABB2);
			case Y:
			default:
				return getDeltaY(aABB, direction, aABB2);
			case Z:
				return getDeltaZ(aABB, direction, aABB2);
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

	private AABB getMovementArea(AABB aABB, Direction direction, double d) {
		double e = d * (double)direction.getAxisDirection().getStep();
		double f = Math.min(e, 0.0);
		double g = Math.max(e, 0.0);
		switch (direction) {
			case WEST:
				return new AABB(aABB.minX + f, aABB.minY, aABB.minZ, aABB.minX + g, aABB.maxY, aABB.maxZ);
			case EAST:
				return new AABB(aABB.maxX + f, aABB.minY, aABB.minZ, aABB.maxX + g, aABB.maxY, aABB.maxZ);
			case DOWN:
				return new AABB(aABB.minX, aABB.minY + f, aABB.minZ, aABB.maxX, aABB.minY + g, aABB.maxZ);
			case UP:
			default:
				return new AABB(aABB.minX, aABB.maxY + f, aABB.minZ, aABB.maxX, aABB.maxY + g, aABB.maxZ);
			case NORTH:
				return new AABB(aABB.minX, aABB.minY, aABB.minZ + f, aABB.maxX, aABB.maxY, aABB.minZ + g);
			case SOUTH:
				return new AABB(aABB.minX, aABB.minY, aABB.maxZ + f, aABB.maxX, aABB.maxY, aABB.maxZ + g);
		}
	}

	private void fixEntityWithinPistonBase(Entity entity, Direction direction, double d) {
		AABB aABB = entity.getBoundingBox();
		AABB aABB2 = Shapes.block().bounds().move(this.worldPosition);
		if (aABB.intersects(aABB2)) {
			Direction direction2 = direction.getOpposite();
			double e = this.getMovement(aABB2, direction2, aABB) + 0.01;
			double f = this.getMovement(aABB2, direction2, aABB.intersect(aABB2)) + 0.01;
			if (Math.abs(e - f) < 0.01) {
				e = Math.min(e, d) + 0.01;
				NOCLIP.set(direction);
				entity.move(MoverType.PISTON, new Vec3(e * (double)direction2.getStepX(), e * (double)direction2.getStepY(), e * (double)direction2.getStepZ()));
				NOCLIP.set(null);
			}
		}
	}

	private static double getDeltaX(AABB aABB, Direction direction, AABB aABB2) {
		return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? aABB.maxX - aABB2.minX : aABB2.maxX - aABB.minX;
	}

	private static double getDeltaY(AABB aABB, Direction direction, AABB aABB2) {
		return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? aABB.maxY - aABB2.minY : aABB2.maxY - aABB.minY;
	}

	private static double getDeltaZ(AABB aABB, Direction direction, AABB aABB2) {
		return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? aABB.maxZ - aABB2.minZ : aABB2.maxZ - aABB.minZ;
	}

	public BlockState getMovedState() {
		return this.movedState;
	}

	public void finalTick() {
		if (this.progressO < 1.0F && this.level != null) {
			this.progress = 1.0F;
			this.progressO = this.progress;
			this.level.removeBlockEntity(this.worldPosition);
			this.setRemoved();
			if (this.level.getBlockState(this.worldPosition).getBlock() == Blocks.MOVING_PISTON) {
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
			this.level.removeBlockEntity(this.worldPosition);
			this.setRemoved();
			if (this.movedState != null && this.level.getBlockState(this.worldPosition).getBlock() == Blocks.MOVING_PISTON) {
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
		} else {
			float f = this.progress + 0.5F;
			this.moveCollidedEntities(f);
			this.progress = f;
			if (this.progress >= 1.0F) {
				this.progress = 1.0F;
			}
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
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
					.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 4.0F));
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
}
