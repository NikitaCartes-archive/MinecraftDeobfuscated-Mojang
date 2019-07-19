package net.minecraft.world.level;

import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface LevelReader extends BlockAndBiomeGetter {
	default boolean isEmptyBlock(BlockPos blockPos) {
		return this.getBlockState(blockPos).isAir();
	}

	default boolean canSeeSkyFromBelowWater(BlockPos blockPos) {
		if (blockPos.getY() >= this.getSeaLevel()) {
			return this.canSeeSky(blockPos);
		} else {
			BlockPos blockPos2 = new BlockPos(blockPos.getX(), this.getSeaLevel(), blockPos.getZ());
			if (!this.canSeeSky(blockPos2)) {
				return false;
			} else {
				for (BlockPos var4 = blockPos2.below(); var4.getY() > blockPos.getY(); var4 = var4.below()) {
					BlockState blockState = this.getBlockState(var4);
					if (blockState.getLightBlock(this, var4) > 0 && !blockState.getMaterial().isLiquid()) {
						return false;
					}
				}

				return true;
			}
		}
	}

	int getRawBrightness(BlockPos blockPos, int i);

	@Nullable
	ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl);

	@Deprecated
	boolean hasChunk(int i, int j);

	BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos);

	int getHeight(Heightmap.Types types, int i, int j);

	default float getBrightness(BlockPos blockPos) {
		return this.getDimension().getBrightnessRamp()[this.getMaxLocalRawBrightness(blockPos)];
	}

	int getSkyDarken();

	WorldBorder getWorldBorder();

	boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape);

	default int getDirectSignal(BlockPos blockPos, Direction direction) {
		return this.getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
	}

	boolean isClientSide();

	int getSeaLevel();

	default ChunkAccess getChunk(BlockPos blockPos) {
		return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	default ChunkAccess getChunk(int i, int j) {
		return this.getChunk(i, j, ChunkStatus.FULL, true);
	}

	default ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus) {
		return this.getChunk(i, j, chunkStatus, true);
	}

	default ChunkStatus statusForCollisions() {
		return ChunkStatus.EMPTY;
	}

	default boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
		VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos, collisionContext);
		return voxelShape.isEmpty() || this.isUnobstructed(null, voxelShape.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()));
	}

	default boolean isUnobstructed(Entity entity) {
		return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
	}

	default boolean noCollision(AABB aABB) {
		return this.noCollision(null, aABB, Collections.emptySet());
	}

	default boolean noCollision(Entity entity) {
		return this.noCollision(entity, entity.getBoundingBox(), Collections.emptySet());
	}

	default boolean noCollision(Entity entity, AABB aABB) {
		return this.noCollision(entity, aABB, Collections.emptySet());
	}

	default boolean noCollision(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
		return this.getCollisions(entity, aABB, set).allMatch(VoxelShape::isEmpty);
	}

	default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
		return Stream.empty();
	}

	default Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
		return Streams.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, set));
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
			boolean checkedBorder = entity == null;

			public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
				if (!this.checkedBorder) {
					this.checkedBorder = true;
					VoxelShape voxelShape = LevelReader.this.getWorldBorder().getCollisionShape();
					boolean bl = Shapes.joinIsNotEmpty(voxelShape, Shapes.create(entity.getBoundingBox().deflate(1.0E-7)), BooleanOp.AND);
					boolean bl2 = Shapes.joinIsNotEmpty(voxelShape, Shapes.create(entity.getBoundingBox().inflate(1.0E-7)), BooleanOp.AND);
					if (!bl && bl2) {
						consumer.accept(voxelShape);
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
						ChunkAccess chunkAccess = LevelReader.this.getChunk(m, n, LevelReader.this.statusForCollisions(), false);
						if (chunkAccess != null) {
							mutableBlockPos.set(i, j, k);
							BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
							if ((l != 1 || blockState.hasLargeCollisionShape()) && (l != 2 || blockState.getBlock() == Blocks.MOVING_PISTON)) {
								VoxelShape voxelShape2 = blockState.getCollisionShape(LevelReader.this, mutableBlockPos, collisionContext);
								VoxelShape voxelShape3 = voxelShape2.move((double)i, (double)j, (double)k);
								if (Shapes.joinIsNotEmpty(voxelShape, voxelShape3, BooleanOp.AND)) {
									consumer.accept(voxelShape3);
									return true;
								}
							}
						}
					}
				}

				return false;
			}
		}, false);
	}

	default boolean isWaterAt(BlockPos blockPos) {
		return this.getFluidState(blockPos).is(FluidTags.WATER);
	}

	default boolean containsAnyLiquid(AABB aABB) {
		int i = Mth.floor(aABB.minX);
		int j = Mth.ceil(aABB.maxX);
		int k = Mth.floor(aABB.minY);
		int l = Mth.ceil(aABB.maxY);
		int m = Mth.floor(aABB.minZ);
		int n = Mth.ceil(aABB.maxZ);

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			for (int o = i; o < j; o++) {
				for (int p = k; p < l; p++) {
					for (int q = m; q < n; q++) {
						BlockState blockState = this.getBlockState(pooledMutableBlockPos.set(o, p, q));
						if (!blockState.getFluidState().isEmpty()) {
							return true;
						}
					}
				}
			}

			return false;
		}
	}

	default int getMaxLocalRawBrightness(BlockPos blockPos) {
		return this.getMaxLocalRawBrightness(blockPos, this.getSkyDarken());
	}

	default int getMaxLocalRawBrightness(BlockPos blockPos, int i) {
		return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 && blockPos.getX() < 30000000 && blockPos.getZ() < 30000000
			? this.getRawBrightness(blockPos, i)
			: 15;
	}

	@Deprecated
	default boolean hasChunkAt(BlockPos blockPos) {
		return this.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	@Deprecated
	default boolean hasChunksAt(BlockPos blockPos, BlockPos blockPos2) {
		return this.hasChunksAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
	}

	@Deprecated
	default boolean hasChunksAt(int i, int j, int k, int l, int m, int n) {
		if (m >= 0 && j < 256) {
			i >>= 4;
			k >>= 4;
			l >>= 4;
			n >>= 4;

			for (int o = i; o <= l; o++) {
				for (int p = k; p <= n; p++) {
					if (!this.hasChunk(o, p)) {
						return false;
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	Dimension getDimension();
}
