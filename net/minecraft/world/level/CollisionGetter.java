/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
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
        return this.noCollision(null, aABB);
    }

    default public boolean noCollision(Entity entity) {
        return this.noCollision(entity, entity.getBoundingBox());
    }

    default public boolean noCollision(@Nullable Entity entity, AABB aABB) {
        for (VoxelShape voxelShape : this.getBlockCollisions(entity, aABB)) {
            if (voxelShape.isEmpty()) continue;
            return false;
        }
        if (!this.getEntityCollisions(entity, aABB).isEmpty()) {
            return false;
        }
        if (entity != null) {
            VoxelShape voxelShape2 = this.borderCollision(entity, aABB);
            return voxelShape2 == null || !Shapes.joinIsNotEmpty(voxelShape2, Shapes.create(aABB), BooleanOp.AND);
        }
        return true;
    }

    public List<VoxelShape> getEntityCollisions(@Nullable Entity var1, AABB var2);

    default public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB) {
        List<VoxelShape> list = this.getEntityCollisions(entity, aABB);
        Iterable<VoxelShape> iterable = this.getBlockCollisions(entity, aABB);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default public Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
        return () -> new BlockCollisions(this, entity, aABB);
    }

    @Nullable
    private VoxelShape borderCollision(Entity entity, AABB aABB) {
        WorldBorder worldBorder = this.getWorldBorder();
        return worldBorder.isInsideCloseToBorder(entity, aABB) ? worldBorder.getCollisionShape() : null;
    }

    default public boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB aABB) {
        BlockCollisions blockCollisions = new BlockCollisions(this, entity, aABB, true);
        while (blockCollisions.hasNext()) {
            if (((VoxelShape)blockCollisions.next()).isEmpty()) continue;
            return true;
        }
        return false;
    }

    default public Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape voxelShape2, Vec3 vec3, double d, double e, double f) {
        if (voxelShape2.isEmpty()) {
            return Optional.empty();
        }
        AABB aABB2 = voxelShape2.bounds().inflate(d, e, f);
        VoxelShape voxelShape22 = StreamSupport.stream(this.getBlockCollisions(entity, aABB2).spliterator(), false).filter(voxelShape -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(voxelShape.bounds())).flatMap(voxelShape -> voxelShape.toAabbs().stream()).map(aABB -> aABB.inflate(d / 2.0, e / 2.0, f / 2.0)).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
        VoxelShape voxelShape3 = Shapes.join(voxelShape2, voxelShape22, BooleanOp.ONLY_FIRST);
        return voxelShape3.closestPointTo(vec3);
    }
}

