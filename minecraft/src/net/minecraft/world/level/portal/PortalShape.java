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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class PortalShape {
	private static final BlockBehaviour.StatePredicate FRAME = (blockState, blockGetter, blockPos) -> blockState.is(Blocks.OBSIDIAN);
	private final LevelAccessor level;
	private final Direction.Axis axis;
	private final Direction rightDir;
	private int numPortalBlocks;
	@Nullable
	private BlockPos bottomLeft;
	private int height;
	private int width;

	public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
		return findPortalShape(levelAccessor, blockPos, portalShape -> portalShape.isValid() && portalShape.numPortalBlocks == 0, axis);
	}

	public static Optional<PortalShape> findPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Predicate<PortalShape> predicate, Direction.Axis axis) {
		Optional<PortalShape> optional = Optional.of(new PortalShape(levelAccessor, blockPos, axis)).filter(predicate);
		if (optional.isPresent()) {
			return optional;
		} else {
			Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
			return Optional.of(new PortalShape(levelAccessor, blockPos, axis2)).filter(predicate);
		}
	}

	public PortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
		this.level = levelAccessor;
		this.axis = axis;
		this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
		this.bottomLeft = this.calculateBottomLeft(blockPos);
		if (this.bottomLeft == null) {
			this.bottomLeft = blockPos;
			this.width = 1;
			this.height = 1;
		} else {
			this.width = this.calculateWidth();
			if (this.width > 0) {
				this.height = this.calculateHeight();
			}
		}
	}

	@Nullable
	private BlockPos calculateBottomLeft(BlockPos blockPos) {
		int i = Math.max(0, blockPos.getY() - 21);

		while (blockPos.getY() > i && isEmpty(this.level.getBlockState(blockPos.below()))) {
			blockPos = blockPos.below();
		}

		Direction direction = this.rightDir.getOpposite();
		int j = this.getDistanceUntilEdgeAboveFrame(blockPos, direction) - 1;
		return j < 0 ? null : blockPos.relative(direction, j);
	}

	private int calculateWidth() {
		int i = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
		return i >= 2 && i <= 21 ? i : 0;
	}

	private int getDistanceUntilEdgeAboveFrame(BlockPos blockPos, Direction direction) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i <= 21; i++) {
			mutableBlockPos.set(blockPos).move(direction, i);
			BlockState blockState = this.level.getBlockState(mutableBlockPos);
			if (!isEmpty(blockState)) {
				if (FRAME.test(blockState, this.level, mutableBlockPos)) {
					return i;
				}
				break;
			}

			BlockState blockState2 = this.level.getBlockState(mutableBlockPos.move(Direction.DOWN));
			if (!FRAME.test(blockState2, this.level, mutableBlockPos)) {
				break;
			}
		}

		return 0;
	}

	private int calculateHeight() {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = this.getDistanceUntilTop(mutableBlockPos);
		return i >= 3 && i <= 21 && this.hasTopFrame(mutableBlockPos, i) ? i : 0;
	}

	private boolean hasTopFrame(BlockPos.MutableBlockPos mutableBlockPos, int i) {
		for (int j = 0; j < this.width; j++) {
			BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
			if (!FRAME.test(this.level.getBlockState(mutableBlockPos2), this.level, mutableBlockPos2)) {
				return false;
			}
		}

		return true;
	}

	private int getDistanceUntilTop(BlockPos.MutableBlockPos mutableBlockPos) {
		for (int i = 0; i < 21; i++) {
			mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
			if (!FRAME.test(this.level.getBlockState(mutableBlockPos), this.level, mutableBlockPos)) {
				return i;
			}

			mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
			if (!FRAME.test(this.level.getBlockState(mutableBlockPos), this.level, mutableBlockPos)) {
				return i;
			}

			for (int j = 0; j < this.width; j++) {
				mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
				BlockState blockState = this.level.getBlockState(mutableBlockPos);
				if (!isEmpty(blockState)) {
					return i;
				}

				if (blockState.is(Blocks.NETHER_PORTAL)) {
					this.numPortalBlocks++;
				}
			}
		}

		return 21;
	}

	private static boolean isEmpty(BlockState blockState) {
		return blockState.isAir() || blockState.is(BlockTags.FIRE) || blockState.is(Blocks.NETHER_PORTAL);
	}

	public boolean isValid() {
		return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
	}

	public void createPortalBlocks() {
		BlockState blockState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
		BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
			.forEach(blockPos -> this.level.setBlock(blockPos, blockState, 18));
	}

	public boolean isComplete() {
		return this.isValid() && this.numPortalBlocks == this.width * this.height;
	}

	public static Vec3 getRelativePosition(BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, EntityDimensions entityDimensions) {
		double d = (double)foundRectangle.axis1Size - (double)entityDimensions.width;
		double e = (double)foundRectangle.axis2Size - (double)entityDimensions.height;
		BlockPos blockPos = foundRectangle.minCorner;
		double g;
		if (d > 0.0) {
			float f = (float)blockPos.get(axis) + entityDimensions.width / 2.0F;
			g = Mth.clamp(Mth.inverseLerp(vec3.get(axis) - (double)f, 0.0, d), 0.0, 1.0);
		} else {
			g = 0.5;
		}

		double h;
		if (e > 0.0) {
			Direction.Axis axis2 = Direction.Axis.Y;
			h = Mth.clamp(Mth.inverseLerp(vec3.get(axis2) - (double)blockPos.get(axis2), 0.0, e), 0.0, 1.0);
		} else {
			h = 0.0;
		}

		Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		double i = vec3.get(axis2) - ((double)blockPos.get(axis2) + 0.5);
		return new Vec3(g, h, i);
	}

	public static PortalInfo createPortalInfo(
		ServerLevel serverLevel,
		BlockUtil.FoundRectangle foundRectangle,
		Direction.Axis axis,
		Vec3 vec3,
		EntityDimensions entityDimensions,
		Vec3 vec32,
		float f,
		float g
	) {
		BlockPos blockPos = foundRectangle.minCorner;
		BlockState blockState = serverLevel.getBlockState(blockPos);
		Direction.Axis axis2 = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
		double d = (double)foundRectangle.axis1Size;
		double e = (double)foundRectangle.axis2Size;
		int i = axis == axis2 ? 0 : 90;
		Vec3 vec33 = axis == axis2 ? vec32 : new Vec3(vec32.z, vec32.y, -vec32.x);
		double h = (double)entityDimensions.width / 2.0 + (d - (double)entityDimensions.width) * vec3.x();
		double j = (e - (double)entityDimensions.height) * vec3.y();
		double k = 0.5 + vec3.z();
		boolean bl = axis2 == Direction.Axis.X;
		Vec3 vec34 = new Vec3((double)blockPos.getX() + (bl ? h : k), (double)blockPos.getY() + j, (double)blockPos.getZ() + (bl ? k : h));
		return new PortalInfo(vec34, vec33, f + (float)i, g);
	}
}
