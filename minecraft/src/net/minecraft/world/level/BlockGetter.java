package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockGetter extends LevelHeightAccessor {
	int MAX_BLOCK_ITERATIONS_ALONG_TRAVEL = 16;

	@Nullable
	BlockEntity getBlockEntity(BlockPos blockPos);

	default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
		BlockEntity blockEntity = this.getBlockEntity(blockPos);
		return blockEntity != null && blockEntity.getType() == blockEntityType ? Optional.of(blockEntity) : Optional.empty();
	}

	BlockState getBlockState(BlockPos blockPos);

	FluidState getFluidState(BlockPos blockPos);

	default int getLightEmission(BlockPos blockPos) {
		return this.getBlockState(blockPos).getLightEmission();
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
					? new BlockHitResult(
						clipBlockStateContextx.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipBlockStateContextx.getTo()), false
					)
					: null;
			},
			clipBlockStateContextx -> {
				Vec3 vec3 = clipBlockStateContextx.getFrom().subtract(clipBlockStateContextx.getTo());
				return BlockHitResult.miss(
					clipBlockStateContextx.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipBlockStateContextx.getTo())
				);
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
			return BlockHitResult.miss(clipContextx.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipContextx.getTo()));
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

	static Iterable<BlockPos> boxTraverseBlocks(Vec3 vec3, Vec3 vec32, AABB aABB) {
		Vec3 vec33 = vec32.subtract(vec3);
		Iterable<BlockPos> iterable = BlockPos.betweenClosed(aABB);
		if (vec33.lengthSqr() < (double)Mth.square(0.99999F)) {
			return iterable;
		} else {
			Set<BlockPos> set = new ObjectLinkedOpenHashSet<>();
			Vec3 vec34 = vec33.normalize().scale(1.0E-7);
			Vec3 vec35 = aABB.getMinPosition().add(vec34);
			Vec3 vec36 = aABB.getMinPosition().subtract(vec33).subtract(vec34);
			addCollisionsAlongTravel(set, vec36, vec35, aABB);

			for (BlockPos blockPos : iterable) {
				set.add(blockPos.immutable());
			}

			return set;
		}
	}

	private static void addCollisionsAlongTravel(Set<BlockPos> set, Vec3 vec3, Vec3 vec32, AABB aABB) {
		Vec3 vec33 = vec32.subtract(vec3);
		int i = Mth.floor(vec3.x);
		int j = Mth.floor(vec3.y);
		int k = Mth.floor(vec3.z);
		int l = Mth.sign(vec33.x);
		int m = Mth.sign(vec33.y);
		int n = Mth.sign(vec33.z);
		double d = l == 0 ? Double.MAX_VALUE : (double)l / vec33.x;
		double e = m == 0 ? Double.MAX_VALUE : (double)m / vec33.y;
		double f = n == 0 ? Double.MAX_VALUE : (double)n / vec33.z;
		double g = d * (l > 0 ? 1.0 - Mth.frac(vec3.x) : Mth.frac(vec3.x));
		double h = e * (m > 0 ? 1.0 - Mth.frac(vec3.y) : Mth.frac(vec3.y));
		double o = f * (n > 0 ? 1.0 - Mth.frac(vec3.z) : Mth.frac(vec3.z));
		int p = 0;

		while (g <= 1.0 || h <= 1.0 || o <= 1.0) {
			if (g < h) {
				if (g < o) {
					i += l;
					g += d;
				} else {
					k += n;
					o += f;
				}
			} else if (h < o) {
				j += m;
				h += e;
			} else {
				k += n;
				o += f;
			}

			if (p++ > 16) {
				break;
			}

			Optional<Vec3> optional = AABB.clip((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1), vec3, vec32);
			if (!optional.isEmpty()) {
				Vec3 vec34 = (Vec3)optional.get();
				double q = Mth.clamp(vec34.x, (double)i + 1.0E-5F, (double)i + 1.0 - 1.0E-5F);
				double r = Mth.clamp(vec34.y, (double)j + 1.0E-5F, (double)j + 1.0 - 1.0E-5F);
				double s = Mth.clamp(vec34.z, (double)k + 1.0E-5F, (double)k + 1.0 - 1.0E-5F);
				int t = Mth.floor(q + aABB.getXsize());
				int u = Mth.floor(r + aABB.getYsize());
				int v = Mth.floor(s + aABB.getZsize());

				for (int w = i; w <= t; w++) {
					for (int x = j; x <= u; x++) {
						for (int y = k; y <= v; y++) {
							set.add(new BlockPos(w, x, y));
						}
					}
				}
			}
		}
	}
}
