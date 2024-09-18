package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public class PortalShape {
	private static final int MIN_WIDTH = 2;
	public static final int MAX_WIDTH = 21;
	private static final int MIN_HEIGHT = 3;
	public static final int MAX_HEIGHT = 21;
	private static final BlockBehaviour.StatePredicate FRAME = (blockState, blockGetter, blockPos) -> blockState.is(Blocks.OBSIDIAN);
	private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
	private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0;
	private final Direction.Axis axis;
	private final Direction rightDir;
	private final int numPortalBlocks;
	private final BlockPos bottomLeft;
	private final int height;
	private final int width;

	private PortalShape(Direction.Axis axis, int i, Direction direction, BlockPos blockPos, int j, int k) {
		this.axis = axis;
		this.numPortalBlocks = i;
		this.rightDir = direction;
		this.bottomLeft = blockPos;
		this.width = j;
		this.height = k;
	}

	public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
		return findPortalShape(levelAccessor, blockPos, portalShape -> portalShape.isValid() && portalShape.numPortalBlocks == 0, axis);
	}

	public static Optional<PortalShape> findPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Predicate<PortalShape> predicate, Direction.Axis axis) {
		Optional<PortalShape> optional = Optional.of(findAnyShape(levelAccessor, blockPos, axis)).filter(predicate);
		if (optional.isPresent()) {
			return optional;
		} else {
			Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
			return Optional.of(findAnyShape(levelAccessor, blockPos, axis2)).filter(predicate);
		}
	}

	public static PortalShape findAnyShape(BlockGetter blockGetter, BlockPos blockPos, Direction.Axis axis) {
		Direction direction = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
		BlockPos blockPos2 = calculateBottomLeft(blockGetter, direction, blockPos);
		if (blockPos2 == null) {
			return new PortalShape(axis, 0, direction, blockPos, 0, 0);
		} else {
			int i = calculateWidth(blockGetter, blockPos2, direction);
			if (i == 0) {
				return new PortalShape(axis, 0, direction, blockPos2, 0, 0);
			} else {
				MutableInt mutableInt = new MutableInt();
				int j = calculateHeight(blockGetter, blockPos2, direction, i, mutableInt);
				return new PortalShape(axis, mutableInt.getValue(), direction, blockPos2, i, j);
			}
		}
	}

	@Nullable
	private static BlockPos calculateBottomLeft(BlockGetter blockGetter, Direction direction, BlockPos blockPos) {
		int i = Math.max(blockGetter.getMinY(), blockPos.getY() - 21);

		while (blockPos.getY() > i && isEmpty(blockGetter.getBlockState(blockPos.below()))) {
			blockPos = blockPos.below();
		}

		Direction direction2 = direction.getOpposite();
		int j = getDistanceUntilEdgeAboveFrame(blockGetter, blockPos, direction2) - 1;
		return j < 0 ? null : blockPos.relative(direction2, j);
	}

	private static int calculateWidth(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		int i = getDistanceUntilEdgeAboveFrame(blockGetter, blockPos, direction);
		return i >= 2 && i <= 21 ? i : 0;
	}

	private static int getDistanceUntilEdgeAboveFrame(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i <= 21; i++) {
			mutableBlockPos.set(blockPos).move(direction, i);
			BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
			if (!isEmpty(blockState)) {
				if (FRAME.test(blockState, blockGetter, mutableBlockPos)) {
					return i;
				}
				break;
			}

			BlockState blockState2 = blockGetter.getBlockState(mutableBlockPos.move(Direction.DOWN));
			if (!FRAME.test(blockState2, blockGetter, mutableBlockPos)) {
				break;
			}
		}

		return 0;
	}

	private static int calculateHeight(BlockGetter blockGetter, BlockPos blockPos, Direction direction, int i, MutableInt mutableInt) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int j = getDistanceUntilTop(blockGetter, blockPos, direction, mutableBlockPos, i, mutableInt);
		return j >= 3 && j <= 21 && hasTopFrame(blockGetter, blockPos, direction, mutableBlockPos, i, j) ? j : 0;
	}

	private static boolean hasTopFrame(BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos.MutableBlockPos mutableBlockPos, int i, int j) {
		for (int k = 0; k < i; k++) {
			BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.set(blockPos).move(Direction.UP, j).move(direction, k);
			if (!FRAME.test(blockGetter.getBlockState(mutableBlockPos2), blockGetter, mutableBlockPos2)) {
				return false;
			}
		}

		return true;
	}

	private static int getDistanceUntilTop(
		BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos.MutableBlockPos mutableBlockPos, int i, MutableInt mutableInt
	) {
		for (int j = 0; j < 21; j++) {
			mutableBlockPos.set(blockPos).move(Direction.UP, j).move(direction, -1);
			if (!FRAME.test(blockGetter.getBlockState(mutableBlockPos), blockGetter, mutableBlockPos)) {
				return j;
			}

			mutableBlockPos.set(blockPos).move(Direction.UP, j).move(direction, i);
			if (!FRAME.test(blockGetter.getBlockState(mutableBlockPos), blockGetter, mutableBlockPos)) {
				return j;
			}

			for (int k = 0; k < i; k++) {
				mutableBlockPos.set(blockPos).move(Direction.UP, j).move(direction, k);
				BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
				if (!isEmpty(blockState)) {
					return j;
				}

				if (blockState.is(Blocks.NETHER_PORTAL)) {
					mutableInt.increment();
				}
			}
		}

		return 21;
	}

	private static boolean isEmpty(BlockState blockState) {
		return blockState.isAir() || blockState.is(BlockTags.FIRE) || blockState.is(Blocks.NETHER_PORTAL);
	}

	public boolean isValid() {
		return this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
	}

	public void createPortalBlocks(LevelAccessor levelAccessor) {
		BlockState blockState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
		BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
			.forEach(blockPos -> levelAccessor.setBlock(blockPos, blockState, 18));
	}

	public boolean isComplete() {
		return this.isValid() && this.numPortalBlocks == this.width * this.height;
	}

	public static Vec3 getRelativePosition(BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, EntityDimensions entityDimensions) {
		double d = (double)foundRectangle.axis1Size - (double)entityDimensions.width();
		double e = (double)foundRectangle.axis2Size - (double)entityDimensions.height();
		BlockPos blockPos = foundRectangle.minCorner;
		double g;
		if (d > 0.0) {
			double f = (double)blockPos.get(axis) + (double)entityDimensions.width() / 2.0;
			g = Mth.clamp(Mth.inverseLerp(vec3.get(axis) - f, 0.0, d), 0.0, 1.0);
		} else {
			g = 0.5;
		}

		double f;
		if (e > 0.0) {
			Direction.Axis axis2 = Direction.Axis.Y;
			f = Mth.clamp(Mth.inverseLerp(vec3.get(axis2) - (double)blockPos.get(axis2), 0.0, e), 0.0, 1.0);
		} else {
			f = 0.0;
		}

		Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		double h = vec3.get(axis2) - ((double)blockPos.get(axis2) + 0.5);
		return new Vec3(g, f, h);
	}

	public static Vec3 findCollisionFreePosition(Vec3 vec3, ServerLevel serverLevel, Entity entity, EntityDimensions entityDimensions) {
		if (!(entityDimensions.width() > 4.0F) && !(entityDimensions.height() > 4.0F)) {
			double d = (double)entityDimensions.height() / 2.0;
			Vec3 vec32 = vec3.add(0.0, d, 0.0);
			VoxelShape voxelShape = Shapes.create(
				AABB.ofSize(vec32, (double)entityDimensions.width(), 0.0, (double)entityDimensions.width()).expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6)
			);
			Optional<Vec3> optional = serverLevel.findFreePosition(
				entity, voxelShape, vec32, (double)entityDimensions.width(), (double)entityDimensions.height(), (double)entityDimensions.width()
			);
			Optional<Vec3> optional2 = optional.map(vec3x -> vec3x.subtract(0.0, d, 0.0));
			return (Vec3)optional2.orElse(vec3);
		} else {
			return vec3;
		}
	}
}
