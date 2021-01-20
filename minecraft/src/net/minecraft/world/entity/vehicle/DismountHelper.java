package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DismountHelper {
	public static int[][] offsetsForDirection(Direction direction) {
		Direction direction2 = direction.getClockWise();
		Direction direction3 = direction2.getOpposite();
		Direction direction4 = direction.getOpposite();
		return new int[][]{
			{direction2.getStepX(), direction2.getStepZ()},
			{direction3.getStepX(), direction3.getStepZ()},
			{direction4.getStepX() + direction2.getStepX(), direction4.getStepZ() + direction2.getStepZ()},
			{direction4.getStepX() + direction3.getStepX(), direction4.getStepZ() + direction3.getStepZ()},
			{direction.getStepX() + direction2.getStepX(), direction.getStepZ() + direction2.getStepZ()},
			{direction.getStepX() + direction3.getStepX(), direction.getStepZ() + direction3.getStepZ()},
			{direction4.getStepX(), direction4.getStepZ()},
			{direction.getStepX(), direction.getStepZ()}
		};
	}

	public static boolean isBlockFloorValid(double d) {
		return !Double.isInfinite(d) && d < 1.0;
	}

	public static boolean canDismountTo(CollisionGetter collisionGetter, LivingEntity livingEntity, AABB aABB) {
		return collisionGetter.getBlockCollisions(livingEntity, aABB).allMatch(VoxelShape::isEmpty);
	}

	public static boolean canDismountTo(CollisionGetter collisionGetter, Vec3 vec3, LivingEntity livingEntity, Pose pose) {
		return canDismountTo(collisionGetter, livingEntity, livingEntity.getLocalBoundsForPose(pose).move(vec3));
	}

	public static VoxelShape nonClimbableShape(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		return !blockState.is(BlockTags.CLIMBABLE) && (!(blockState.getBlock() instanceof TrapDoorBlock) || !blockState.getValue(TrapDoorBlock.OPEN))
			? blockState.getCollisionShape(blockGetter, blockPos)
			: Shapes.empty();
	}

	public static double findCeilingFrom(BlockPos blockPos, int i, Function<BlockPos, VoxelShape> function) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		int j = 0;

		while (j < i) {
			VoxelShape voxelShape = (VoxelShape)function.apply(mutableBlockPos);
			if (!voxelShape.isEmpty()) {
				return (double)(blockPos.getY() + j) + voxelShape.min(Direction.Axis.Y);
			}

			j++;
			mutableBlockPos.move(Direction.UP);
		}

		return Double.POSITIVE_INFINITY;
	}

	@Nullable
	public static Vec3 findSafeDismountLocation(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean bl) {
		if (bl && entityType.isBlockDangerous(collisionGetter.getBlockState(blockPos))) {
			return null;
		} else {
			double d = collisionGetter.getBlockFloorHeight(nonClimbableShape(collisionGetter, blockPos), () -> nonClimbableShape(collisionGetter, blockPos.below()));
			if (!isBlockFloorValid(d)) {
				return null;
			} else if (bl && d <= 0.0 && entityType.isBlockDangerous(collisionGetter.getBlockState(blockPos.below()))) {
				return null;
			} else {
				Vec3 vec3 = Vec3.upFromBottomCenterOf(blockPos, d);
				return collisionGetter.getBlockCollisions(null, entityType.getDimensions().makeBoundingBox(vec3)).allMatch(VoxelShape::isEmpty) ? vec3 : null;
			}
		}
	}
}
