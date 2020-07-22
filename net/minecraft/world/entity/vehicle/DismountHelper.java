/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
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
import org.jetbrains.annotations.Nullable;

public class DismountHelper {
    public static int[][] offsetsForDirection(Direction direction) {
        Direction direction2 = direction.getClockWise();
        Direction direction3 = direction2.getOpposite();
        Direction direction4 = direction.getOpposite();
        return new int[][]{{direction2.getStepX(), direction2.getStepZ()}, {direction3.getStepX(), direction3.getStepZ()}, {direction4.getStepX() + direction2.getStepX(), direction4.getStepZ() + direction2.getStepZ()}, {direction4.getStepX() + direction3.getStepX(), direction4.getStepZ() + direction3.getStepZ()}, {direction.getStepX() + direction2.getStepX(), direction.getStepZ() + direction2.getStepZ()}, {direction.getStepX() + direction3.getStepX(), direction.getStepZ() + direction3.getStepZ()}, {direction4.getStepX(), direction4.getStepZ()}, {direction.getStepX(), direction.getStepZ()}};
    }

    public static boolean isBlockFloorValid(double d) {
        return !Double.isInfinite(d) && d < 1.0;
    }

    public static boolean canDismountTo(CollisionGetter collisionGetter, LivingEntity livingEntity, AABB aABB) {
        return collisionGetter.getBlockCollisions(livingEntity, aABB).allMatch(VoxelShape::isEmpty);
    }

    @Nullable
    public static Vec3 findDismountLocation(CollisionGetter collisionGetter, double d, double e, double f, LivingEntity livingEntity, Pose pose) {
        if (DismountHelper.isBlockFloorValid(e)) {
            Vec3 vec3 = new Vec3(d, e, f);
            if (DismountHelper.canDismountTo(collisionGetter, livingEntity, livingEntity.getLocalBoundsForPose(pose).move(vec3))) {
                return vec3;
            }
        }
        return null;
    }

    public static VoxelShape nonClimbableShape(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockState.is(BlockTags.CLIMBABLE) || blockState.getBlock() instanceof TrapDoorBlock && blockState.getValue(TrapDoorBlock.OPEN).booleanValue()) {
            return Shapes.empty();
        }
        return blockState.getCollisionShape(blockGetter, blockPos);
    }

    public static double findCeilingFrom(BlockPos blockPos, int i, Function<BlockPos, VoxelShape> function) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int j = 0; j < i; ++j) {
            VoxelShape voxelShape = function.apply(mutableBlockPos);
            if (!voxelShape.isEmpty()) {
                return (double)(blockPos.getY() + j) + voxelShape.min(Direction.Axis.Y);
            }
            mutableBlockPos.move(Direction.UP);
        }
        return Double.POSITIVE_INFINITY;
    }

    @Nullable
    public static Vec3 findSafeDismountLocation(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean bl) {
        if (bl && entityType.isBlockDangerous(collisionGetter.getBlockState(blockPos))) {
            return null;
        }
        double d = collisionGetter.getBlockFloorHeight(DismountHelper.nonClimbableShape(collisionGetter, blockPos), () -> DismountHelper.nonClimbableShape(collisionGetter, blockPos.below()));
        if (!DismountHelper.isBlockFloorValid(d)) {
            return null;
        }
        if (bl && d <= 0.0 && entityType.isBlockDangerous(collisionGetter.getBlockState(blockPos.below()))) {
            return null;
        }
        Vec3 vec3 = Vec3.upFromBottomCenterOf(blockPos, d);
        if (collisionGetter.getBlockCollisions(null, entityType.getDimensions().makeBoundingBox(vec3)).allMatch(VoxelShape::isEmpty)) {
            return vec3;
        }
        return null;
    }
}

