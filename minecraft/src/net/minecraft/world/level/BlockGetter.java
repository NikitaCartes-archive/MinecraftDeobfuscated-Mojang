package net.minecraft.world.level;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockGetter extends LevelHeightAccessor {
	@Nullable
	BlockEntity getBlockEntity(BlockPos blockPos);

	BlockState getBlockState(BlockPos blockPos);

	FluidState getFluidState(BlockPos blockPos);

	default int getLightEmission(BlockPos blockPos) {
		return this.getBlockState(blockPos).getLightEmission();
	}

	default int getMaxLightLevel() {
		return 15;
	}

	default Stream<BlockState> getBlockStates(AABB aABB) {
		return BlockPos.betweenClosedStream(aABB).map(this::getBlockState);
	}

	default BlockHitResult isBlockInLine(ClipBlockStateContext clipBlockStateContext) {
		return traverseBlocks(
			clipBlockStateContext.getFrom(),
			clipBlockStateContext.getTo(),
			clipBlockStateContext,
			(clipBlockStateContextx, blockPos) -> {
				BlockState blockState = this.getBlockState(blockPos);
				Vec3 vec3 = clipBlockStateContextx.getFrom().subtract(clipBlockStateContextx.getTo());
				return clipBlockStateContextx.isTargetBlock().test(blockState)
					? new BlockHitResult(clipBlockStateContextx.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipBlockStateContextx.getTo()), false)
					: null;
			},
			clipBlockStateContextx -> {
				Vec3 vec3 = clipBlockStateContextx.getFrom().subtract(clipBlockStateContextx.getTo());
				return BlockHitResult.miss(clipBlockStateContextx.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipBlockStateContextx.getTo()));
			}
		);
	}

	default BlockHitResult clip(ClipContext clipContext) {
		return traverseBlocks(clipContext.getFrom(), clipContext.getTo(), clipContext, (clipContextx, blockPos) -> {
			BlockState blockState = this.getBlockState(blockPos);
			FluidState fluidState = this.getFluidState(blockPos);
			Vec3 vec3 = clipContextx.getFrom();
			Vec3 vec32 = clipContextx.getTo();
			VoxelShape voxelShape = clipContextx.getBlockShape(blockState, this, blockPos);
			BlockHitResult blockHitResult = this.clipWithInteractionOverride(vec3, vec32, blockPos, voxelShape, blockState);
			VoxelShape voxelShape2 = clipContextx.getFluidShape(fluidState, this, blockPos);
			BlockHitResult blockHitResult2 = voxelShape2.clip(vec3, vec32, blockPos);
			double d = blockHitResult == null ? Double.MAX_VALUE : clipContextx.getFrom().distanceToSqr(blockHitResult.getLocation());
			double e = blockHitResult2 == null ? Double.MAX_VALUE : clipContextx.getFrom().distanceToSqr(blockHitResult2.getLocation());
			return d <= e ? blockHitResult : blockHitResult2;
		}, clipContextx -> {
			Vec3 vec3 = clipContextx.getFrom().subtract(clipContextx.getTo());
			return BlockHitResult.miss(clipContextx.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipContextx.getTo()));
		});
	}

	@Nullable
	default BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec32, BlockPos blockPos, VoxelShape voxelShape, BlockState blockState) {
		BlockHitResult blockHitResult = voxelShape.clip(vec3, vec32, blockPos);
		if (blockHitResult != null) {
			BlockHitResult blockHitResult2 = blockState.getInteractionShape(this, blockPos).clip(vec3, vec32, blockPos);
			if (blockHitResult2 != null && blockHitResult2.getLocation().subtract(vec3).lengthSqr() < blockHitResult.getLocation().subtract(vec3).lengthSqr()) {
				return blockHitResult.withDirection(blockHitResult2.getDirection());
			}
		}

		return blockHitResult;
	}

	default double getBlockFloorHeight(VoxelShape voxelShape, Supplier<VoxelShape> supplier) {
		if (!voxelShape.isEmpty()) {
			return voxelShape.max(Direction.Axis.Y);
		} else {
			double d = ((VoxelShape)supplier.get()).max(Direction.Axis.Y);
			return d >= 1.0 ? d - 1.0 : Double.NEGATIVE_INFINITY;
		}
	}

	default double getBlockFloorHeight(BlockPos blockPos) {
		return this.getBlockFloorHeight(this.getBlockState(blockPos).getCollisionShape(this, blockPos), () -> {
			BlockPos blockPos2 = blockPos.below();
			return this.getBlockState(blockPos2).getCollisionShape(this, blockPos2);
		});
	}

	static <T, C> T traverseBlocks(Vec3 vec3, Vec3 vec32, C object, BiFunction<C, BlockPos, T> biFunction, Function<C, T> function) {
		if (vec3.equals(vec32)) {
			return (T)function.apply(object);
		} else {
			double d = Mth.lerp(-1.0E-7, vec32.x, vec3.x);
			double e = Mth.lerp(-1.0E-7, vec32.y, vec3.y);
			double f = Mth.lerp(-1.0E-7, vec32.z, vec3.z);
			double g = Mth.lerp(-1.0E-7, vec3.x, vec32.x);
			double h = Mth.lerp(-1.0E-7, vec3.y, vec32.y);
			double i = Mth.lerp(-1.0E-7, vec3.z, vec32.z);
			int j = Mth.floor(g);
			int k = Mth.floor(h);
			int l = Mth.floor(i);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(j, k, l);
			T object2 = (T)biFunction.apply(object, mutableBlockPos);
			if (object2 != null) {
				return object2;
			} else {
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

					T object3 = (T)biFunction.apply(object, mutableBlockPos.set(j, k, l));
					if (object3 != null) {
						return object3;
					}
				}

				return (T)function.apply(object);
			}
		}
	}
}
