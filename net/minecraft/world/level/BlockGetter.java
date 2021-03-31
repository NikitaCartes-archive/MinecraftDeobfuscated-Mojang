/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface BlockGetter
extends LevelHeightAccessor {
    @Nullable
    public BlockEntity getBlockEntity(BlockPos var1);

    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity == null || blockEntity.getType() != blockEntityType) {
            return Optional.empty();
        }
        return Optional.of(blockEntity);
    }

    public BlockState getBlockState(BlockPos var1);

    public FluidState getFluidState(BlockPos var1);

    default public int getLightEmission(BlockPos blockPos) {
        return this.getBlockState(blockPos).getLightEmission();
    }

    default public int getMaxLightLevel() {
        return 15;
    }

    default public Stream<BlockState> getBlockStates(AABB aABB) {
        return BlockPos.betweenClosedStream(aABB).map(this::getBlockState);
    }

    default public BlockHitResult isBlockInLine(ClipBlockStateContext clipBlockStateContext2) {
        return BlockGetter.traverseBlocks(clipBlockStateContext2.getFrom(), clipBlockStateContext2.getTo(), clipBlockStateContext2, (clipBlockStateContext, blockPos) -> {
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            Vec3 vec3 = clipBlockStateContext.getFrom().subtract(clipBlockStateContext.getTo());
            return clipBlockStateContext.isTargetBlock().test(blockState) ? new BlockHitResult(clipBlockStateContext.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipBlockStateContext.getTo()), false) : null;
        }, clipBlockStateContext -> {
            Vec3 vec3 = clipBlockStateContext.getFrom().subtract(clipBlockStateContext.getTo());
            return BlockHitResult.miss(clipBlockStateContext.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipBlockStateContext.getTo()));
        });
    }

    default public BlockHitResult clip(ClipContext clipContext2) {
        return BlockGetter.traverseBlocks(clipContext2.getFrom(), clipContext2.getTo(), clipContext2, (clipContext, blockPos) -> {
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            FluidState fluidState = this.getFluidState((BlockPos)blockPos);
            Vec3 vec3 = clipContext.getFrom();
            Vec3 vec32 = clipContext.getTo();
            VoxelShape voxelShape = clipContext.getBlockShape(blockState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult = this.clipWithInteractionOverride(vec3, vec32, (BlockPos)blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = clipContext.getFluidShape(fluidState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult2 = voxelShape2.clip(vec3, vec32, (BlockPos)blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult.getLocation());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult2.getLocation());
            return d <= e ? blockHitResult : blockHitResult2;
        }, clipContext -> {
            Vec3 vec3 = clipContext.getFrom().subtract(clipContext.getTo());
            return BlockHitResult.miss(clipContext.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipContext.getTo()));
        });
    }

    @Nullable
    default public BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec32, BlockPos blockPos, VoxelShape voxelShape, BlockState blockState) {
        BlockHitResult blockHitResult2;
        BlockHitResult blockHitResult = voxelShape.clip(vec3, vec32, blockPos);
        if (blockHitResult != null && (blockHitResult2 = blockState.getInteractionShape(this, blockPos).clip(vec3, vec32, blockPos)) != null && blockHitResult2.getLocation().subtract(vec3).lengthSqr() < blockHitResult.getLocation().subtract(vec3).lengthSqr()) {
            return blockHitResult.withDirection(blockHitResult2.getDirection());
        }
        return blockHitResult;
    }

    default public double getBlockFloorHeight(VoxelShape voxelShape, Supplier<VoxelShape> supplier) {
        if (!voxelShape.isEmpty()) {
            return voxelShape.max(Direction.Axis.Y);
        }
        double d = supplier.get().max(Direction.Axis.Y);
        if (d >= 1.0) {
            return d - 1.0;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default public double getBlockFloorHeight(BlockPos blockPos) {
        return this.getBlockFloorHeight(this.getBlockState(blockPos).getCollisionShape(this, blockPos), () -> {
            BlockPos blockPos2 = blockPos.below();
            return this.getBlockState(blockPos2).getCollisionShape(this, blockPos2);
        });
    }

    public static <T, C> T traverseBlocks(Vec3 vec3, Vec3 vec32, C object, BiFunction<C, BlockPos, T> biFunction, Function<C, T> function) {
        int l;
        int k;
        if (vec3.equals(vec32)) {
            return function.apply(object);
        }
        double d = Mth.lerp(-1.0E-7, vec32.x, vec3.x);
        double e = Mth.lerp(-1.0E-7, vec32.y, vec3.y);
        double f = Mth.lerp(-1.0E-7, vec32.z, vec3.z);
        double g = Mth.lerp(-1.0E-7, vec3.x, vec32.x);
        double h = Mth.lerp(-1.0E-7, vec3.y, vec32.y);
        double i = Mth.lerp(-1.0E-7, vec3.z, vec32.z);
        int j = Mth.floor(g);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(j, k = Mth.floor(h), l = Mth.floor(i));
        T object2 = biFunction.apply(object, mutableBlockPos);
        if (object2 != null) {
            return object2;
        }
        double m = d - g;
        double n = e - h;
        double o = f - i;
        int p = Mth.sign(m);
        int q = Mth.sign(n);
        int r = Mth.sign(o);
        double s = p == 0 ? Double.MAX_VALUE : (double)p / m;
        double t = q == 0 ? Double.MAX_VALUE : (double)q / n;
        double u = r == 0 ? Double.MAX_VALUE : (double)r / o;
        double v = s * (p > 0 ? 1.0 - Mth.frac(g) : Mth.frac(g));
        double w = t * (q > 0 ? 1.0 - Mth.frac(h) : Mth.frac(h));
        double x = u * (r > 0 ? 1.0 - Mth.frac(i) : Mth.frac(i));
        while (v <= 1.0 || w <= 1.0 || x <= 1.0) {
            T object3;
            if (v < w) {
                if (v < x) {
                    j += p;
                    v += s;
                } else {
                    l += r;
                    x += u;
                }
            } else if (w < x) {
                k += q;
                w += t;
            } else {
                l += r;
                x += u;
            }
            if ((object3 = biFunction.apply(object, mutableBlockPos.set(j, k, l))) == null) continue;
            return object3;
        }
        return function.apply(object);
    }
}

