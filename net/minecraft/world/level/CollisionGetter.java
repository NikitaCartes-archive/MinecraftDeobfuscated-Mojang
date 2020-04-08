/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Streams;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface CollisionGetter
extends BlockGetter {
    public WorldBorder getWorldBorder();

    @Nullable
    public BlockGetter getChunkForCollisions(int var1, int var2);

    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        return true;
    }

    default public boolean isUnobstructed(BlockState blockState, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos, collisionContext);
        return voxelShape.isEmpty() || this.isUnobstructed(null, voxelShape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    default public boolean isUnobstructed(Entity entity) {
        return this.isUnobstructed(entity, Shapes.create(entity.getBoundingBox()));
    }

    default public boolean noCollision(AABB aABB) {
        return this.noCollision(null, aABB, entity -> true);
    }

    default public boolean noCollision(Entity entity2) {
        return this.noCollision(entity2, entity2.getBoundingBox(), entity -> true);
    }

    default public boolean noCollision(Entity entity2, AABB aABB) {
        return this.noCollision(entity2, aABB, entity -> true);
    }

    default public boolean noCollision(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
        return this.getCollisions(entity, aABB, predicate).allMatch(VoxelShape::isEmpty);
    }

    default public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
        return Stream.empty();
    }

    default public Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
        return Streams.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, predicate));
    }

    default public Stream<VoxelShape> getBlockCollisions(final @Nullable Entity entity, final AABB aABB) {
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
            boolean skipWorldBorderCheck;
            {
                super(l, i);
                this.skipWorldBorderCheck = entity == null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
                if (!this.skipWorldBorderCheck) {
                    boolean bl2;
                    this.skipWorldBorderCheck = true;
                    WorldBorder worldBorder = CollisionGetter.this.getWorldBorder();
                    boolean bl = CollisionGetter.isBoxFullyWithinWorldBorder(worldBorder, entity.getBoundingBox().deflate(1.0E-7));
                    boolean bl3 = bl2 = bl && !CollisionGetter.isBoxFullyWithinWorldBorder(worldBorder, entity.getBoundingBox().inflate(1.0E-7));
                    if (bl2) {
                        consumer.accept(worldBorder.getCollisionShape());
                        return true;
                    }
                }
                while (cursor3D.advance()) {
                    int n;
                    int m;
                    BlockGetter blockGetter;
                    int i = cursor3D.nextX();
                    int j = cursor3D.nextY();
                    int k = cursor3D.nextZ();
                    int l = cursor3D.getNextType();
                    if (l == 3 || (blockGetter = CollisionGetter.this.getChunkForCollisions(m = i >> 4, n = k >> 4)) == null) continue;
                    mutableBlockPos.set(i, j, k);
                    BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
                    if (l == 1 && !blockState.hasLargeCollisionShape() || l == 2 && blockState.getBlock() != Blocks.MOVING_PISTON) continue;
                    VoxelShape voxelShape3 = blockState.getCollisionShape(CollisionGetter.this, mutableBlockPos, collisionContext);
                    if (voxelShape3 == Shapes.block()) {
                        if (!aABB.intersects(i, j, k, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)) continue;
                        consumer.accept(voxelShape3.move(i, j, k));
                        return true;
                    }
                    VoxelShape voxelShape2 = voxelShape3.move(i, j, k);
                    if (!Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND)) continue;
                    consumer.accept(voxelShape2);
                    return true;
                }
                return false;
            }
        }, false);
    }

    public static boolean isBoxFullyWithinWorldBorder(WorldBorder worldBorder, AABB aABB) {
        double d = Mth.floor(worldBorder.getMinX());
        double e = Mth.floor(worldBorder.getMinZ());
        double f = Mth.ceil(worldBorder.getMaxX());
        double g = Mth.ceil(worldBorder.getMaxZ());
        return aABB.minX > d && aABB.minX < f && aABB.minZ > e && aABB.minZ < g && aABB.maxX > d && aABB.maxX < f && aABB.maxZ > e && aABB.maxZ < g;
    }
}

