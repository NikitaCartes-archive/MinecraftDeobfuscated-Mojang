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
                    VoxelShape voxelShape4 = CollisionGetter.this.getWorldBorder().getCollisionShape();
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
                    BlockGetter blockGetter;
                    int i = cursor3D.nextX();
                    int j = cursor3D.nextY();
                    int k = cursor3D.nextZ();
                    int l = cursor3D.getNextType();
                    if (l == 3 || (blockGetter = CollisionGetter.this.getChunkForCollisions(m = i >> 4, n = k >> 4)) == null) continue;
                    mutableBlockPos.set(i, j, k);
                    BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
                    if (l == 1 && !blockState.hasLargeCollisionShape() || l == 2 && blockState.getBlock() != Blocks.MOVING_PISTON || !Shapes.joinIsNotEmpty(voxelShape, voxelShape3 = (voxelShape2 = blockState.getCollisionShape(CollisionGetter.this, mutableBlockPos, collisionContext)).move(i, j, k), BooleanOp.AND)) continue;
                    consumer.accept(voxelShape3);
                    return true;
                }
                return false;
            }
        }, false);
    }
}

