/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndBiomeGetter;
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
import org.jetbrains.annotations.Nullable;

public interface LevelReader
extends BlockAndBiomeGetter {
    default public boolean isEmptyBlock(BlockPos blockPos) {
        return this.getBlockState(blockPos).isAir();
    }

    default public boolean canSeeSkyFromBelowWater(BlockPos blockPos) {
        if (blockPos.getY() >= this.getSeaLevel()) {
            return this.canSeeSky(blockPos);
        }
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), this.getSeaLevel(), blockPos.getZ());
        if (!this.canSeeSky(blockPos2)) {
            return false;
        }
        blockPos2 = blockPos2.below();
        while (blockPos2.getY() > blockPos.getY()) {
            BlockState blockState = this.getBlockState(blockPos2);
            if (blockState.getLightBlock(this, blockPos2) > 0 && !blockState.getMaterial().isLiquid()) {
                return false;
            }
            blockPos2 = blockPos2.below();
        }
        return true;
    }

    public int getRawBrightness(BlockPos var1, int var2);

    @Nullable
    public ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    public boolean hasChunk(int var1, int var2);

    public BlockPos getHeightmapPos(Heightmap.Types var1, BlockPos var2);

    public int getHeight(Heightmap.Types var1, int var2, int var3);

    default public float getBrightness(BlockPos blockPos) {
        return this.getDimension().getBrightnessRamp()[this.getMaxLocalRawBrightness(blockPos)];
    }

    public int getSkyDarken();

    public WorldBorder getWorldBorder();

    public boolean isUnobstructed(@Nullable Entity var1, VoxelShape var2);

    default public int getDirectSignal(BlockPos blockPos, Direction direction) {
        return this.getBlockState(blockPos).getDirectSignal(this, blockPos, direction);
    }

    public boolean isClientSide();

    public int getSeaLevel();

    default public ChunkAccess getChunk(BlockPos blockPos) {
        return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    default public ChunkAccess getChunk(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.FULL, true);
    }

    default public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus) {
        return this.getChunk(i, j, chunkStatus, true);
    }

    default public ChunkStatus statusForCollisions() {
        return ChunkStatus.EMPTY;
    }

    default public boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos, collisionContext);
        return voxelShape.isEmpty() || this.isUnobstructed(null, voxelShape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    default public boolean isUnobstructed(Entity entity) {
        return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
    }

    default public boolean noCollision(AABB aABB) {
        return this.noCollision(null, aABB, Collections.emptySet());
    }

    default public boolean noCollision(Entity entity) {
        return this.noCollision(entity, entity.getBoundingBox(), Collections.emptySet());
    }

    default public boolean noCollision(Entity entity, AABB aABB) {
        return this.noCollision(entity, aABB, Collections.emptySet());
    }

    default public boolean noCollision(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
        return this.getCollisions(entity, aABB, set).allMatch(VoxelShape::isEmpty);
    }

    default public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
        return Stream.empty();
    }

    default public Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Set<Entity> set) {
        return Streams.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, set));
    }

    default public Stream<VoxelShape> getBlockCollisions(final @Nullable Entity entity, AABB aABB) {
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
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280){
            boolean checkedBorder;
            {
                super(l, i);
                this.checkedBorder = entity == null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
                if (!this.checkedBorder) {
                    this.checkedBorder = true;
                    VoxelShape voxelShape4 = LevelReader.this.getWorldBorder().getCollisionShape();
                    boolean bl = Shapes.joinIsNotEmpty(voxelShape4, Shapes.create(entity.getBoundingBox().deflate(1.0E-7)), BooleanOp.AND);
                    boolean bl2 = Shapes.joinIsNotEmpty(voxelShape4, Shapes.create(entity.getBoundingBox().inflate(1.0E-7)), BooleanOp.AND);
                    if (!bl && bl2) {
                        consumer.accept(voxelShape4);
                        return true;
                    }
                }
                while (cursor3D.advance()) {
                    VoxelShape voxelShape2;
                    VoxelShape voxelShape3;
                    int n;
                    int m;
                    ChunkAccess chunkAccess;
                    int i = cursor3D.nextX();
                    int j = cursor3D.nextY();
                    int k = cursor3D.nextZ();
                    int l = cursor3D.getNextType();
                    if (l == 3 || (chunkAccess = LevelReader.this.getChunk(m = i >> 4, n = k >> 4, LevelReader.this.statusForCollisions(), false)) == null) continue;
                    mutableBlockPos.set(i, j, k);
                    BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
                    if (l == 1 && !blockState.hasLargeCollisionShape() || l == 2 && blockState.getBlock() != Blocks.MOVING_PISTON || !Shapes.joinIsNotEmpty(voxelShape, voxelShape3 = (voxelShape2 = blockState.getCollisionShape(LevelReader.this, mutableBlockPos, collisionContext)).move(i, j, k), BooleanOp.AND)) continue;
                    consumer.accept(voxelShape3);
                    return true;
                }
                return false;
            }
        }, false);
    }

    default public boolean isWaterAt(BlockPos blockPos) {
        return this.getFluidState(blockPos).is(FluidTags.WATER);
    }

    default public boolean containsAnyLiquid(AABB aABB) {
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire();){
            for (int o = i; o < j; ++o) {
                for (int p = k; p < l; ++p) {
                    for (int q = m; q < n; ++q) {
                        BlockState blockState = this.getBlockState(pooledMutableBlockPos.set(o, p, q));
                        if (blockState.getFluidState().isEmpty()) continue;
                        boolean bl = true;
                        return bl;
                    }
                }
            }
        }
        return false;
    }

    default public int getMaxLocalRawBrightness(BlockPos blockPos) {
        return this.getMaxLocalRawBrightness(blockPos, this.getSkyDarken());
    }

    default public int getMaxLocalRawBrightness(BlockPos blockPos, int i) {
        if (blockPos.getX() < -30000000 || blockPos.getZ() < -30000000 || blockPos.getX() >= 30000000 || blockPos.getZ() >= 30000000) {
            return 15;
        }
        return this.getRawBrightness(blockPos, i);
    }

    @Deprecated
    default public boolean hasChunkAt(BlockPos blockPos) {
        return this.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    @Deprecated
    default public boolean hasChunksAt(BlockPos blockPos, BlockPos blockPos2) {
        return this.hasChunksAt(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
    }

    @Deprecated
    default public boolean hasChunksAt(int i, int j, int k, int l, int m, int n) {
        if (m < 0 || j >= 256) {
            return false;
        }
        k >>= 4;
        l >>= 4;
        n >>= 4;
        for (int o = i >>= 4; o <= l; ++o) {
            for (int p = k; p <= n; ++p) {
                if (this.hasChunk(o, p)) continue;
                return false;
            }
        }
        return true;
    }

    public Dimension getDimension();
}

