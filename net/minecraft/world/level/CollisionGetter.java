/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionSpliterator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public Stream<VoxelShape> getEntityCollisions(@Nullable Entity var1, AABB var2, Predicate<Entity> var3);

    default public Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
        return Stream.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, predicate));
    }

    default public Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
        return StreamSupport.stream(new CollisionSpliterator(this, entity, aABB), false);
    }

    @Environment(value=EnvType.CLIENT)
    default public boolean hasBlockCollision(@Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
        return !this.getBlockCollisions(entity, aABB, biPredicate).allMatch(VoxelShape::isEmpty);
    }

    default public Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
        return StreamSupport.stream(new CollisionSpliterator(this, entity, aABB, biPredicate), false);
    }

    default public Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape voxelShape2, Vec3 vec3, double d, double e, double f) {
        if (voxelShape2.isEmpty()) {
            return Optional.empty();
        }
        AABB aABB2 = voxelShape2.bounds().inflate(d, e, f);
        VoxelShape voxelShape22 = this.getBlockCollisions(entity, aABB2).flatMap(voxelShape -> voxelShape.toAabbs().stream()).map(aABB -> aABB.inflate(d / 2.0, e / 2.0, f / 2.0)).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
        VoxelShape voxelShape3 = Shapes.join(voxelShape2, voxelShape22, BooleanOp.ONLY_FIRST);
        return voxelShape3.closestPointTo(vec3);
    }
}

