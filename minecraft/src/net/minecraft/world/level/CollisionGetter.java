package net.minecraft.world.level;

import com.google.common.collect.Streams;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter extends BlockGetter {
	WorldBorder getWorldBorder();

	@Nullable
	BlockGetter getChunkForCollisions(int i, int j);

	default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
		return true;
	}

	default boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
		VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos, collisionContext);
		return voxelShape.isEmpty() || this.isUnobstructed(null, voxelShape.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()));
	}

	default boolean isUnobstructed(Entity entity) {
		return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
	}

	default boolean noCollision(AABB aABB) {
		return this.noCollision(null, aABB, entity -> true);
	}

	default boolean noCollision(Entity entity) {
		return this.noCollision(entity, entity.getBoundingBox(), entityx -> true);
	}

	default boolean noCollision(Entity entity, AABB aABB) {
		return this.noCollision(entity, aABB, entityx -> true);
	}

	default boolean noCollision(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return this.getCollisions(entity, aABB, predicate).allMatch(VoxelShape::isEmpty);
	}

	default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return Stream.empty();
	}

	default Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return Streams.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, predicate));
	}

	default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
		int i = Mth.floor(aABB.minX - 1.0E-7) - 1;
		int j = Mth.floor(aABB.maxX + 1.0E-7) + 1;
		int k = Mth.floor(aABB.minY - 1.0E-7) - 1;
		int l = Mth.floor(aABB.maxY + 1.0E-7) + 1;
		int m = Mth.floor(aABB.minZ - 1.0E-7) - 1;
		int n = Mth.floor(aABB.maxZ + 1.0E-7) + 1;
		final CollisionContext collisionContext = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
		final Cursor3D cursor3D = new Cursor3D(i, k, m, j, l, n);
		final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		final VoxelShape voxelShape = Shapes.create(aABB);
		return StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280) {
			boolean skipWorldBorderCheck = entity == null;

			public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
				if (!this.skipWorldBorderCheck) {
					this.skipWorldBorderCheck = true;
					WorldBorder worldBorder = CollisionGetter.this.getWorldBorder();
					boolean bl = CollisionGetter.isBoxFullyWithinWorldBorder(worldBorder, entity.getBoundingBox().deflate(1.0E-7));
					boolean bl2 = bl && !CollisionGetter.isBoxFullyWithinWorldBorder(worldBorder, entity.getBoundingBox().inflate(1.0E-7));
					if (bl2) {
						consumer.accept(worldBorder.getCollisionShape());
						return true;
					}
				}

				while (cursor3D.advance()) {
					int i = cursor3D.nextX();
					int j = cursor3D.nextY();
					int k = cursor3D.nextZ();
					int l = cursor3D.getNextType();
					if (l != 3) {
						int m = i >> 4;
						int n = k >> 4;
						BlockGetter blockGetter = CollisionGetter.this.getChunkForCollisions(m, n);
						if (blockGetter != null) {
							mutableBlockPos.set(i, j, k);
							BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
							if ((l != 1 || blockState.hasLargeCollisionShape()) && (l != 2 || blockState.getBlock() == Blocks.MOVING_PISTON)) {
								VoxelShape voxelShape = blockState.getCollisionShape(CollisionGetter.this, mutableBlockPos, collisionContext);
								if (voxelShape == Shapes.block()) {
									if (aABB.intersects((double)i, (double)j, (double)k, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)) {
										consumer.accept(voxelShape.move((double)i, (double)j, (double)k));
										return true;
									}
								} else {
									VoxelShape voxelShape2 = voxelShape.move((double)i, (double)j, (double)k);
									if (Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND)) {
										consumer.accept(voxelShape2);
										return true;
									}
								}
							}
						}
					}
				}

				return false;
			}
		}, false);
	}

	static boolean isBoxFullyWithinWorldBorder(WorldBorder worldBorder, AABB aABB) {
		double d = (double)Mth.floor(worldBorder.getMinX());
		double e = (double)Mth.floor(worldBorder.getMinZ());
		double f = (double)Mth.ceil(worldBorder.getMaxX());
		double g = (double)Mth.ceil(worldBorder.getMaxZ());
		return aABB.minX > d && aABB.minX < f && aABB.minZ > e && aABB.minZ < g && aABB.maxX > d && aABB.maxX < f && aABB.maxZ > e && aABB.maxZ < g;
	}
}
