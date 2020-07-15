package net.minecraft.world.level;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
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

	Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate);

	default Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return Stream.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, predicate));
	}

	default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
		return StreamSupport.stream(new CollisionSpliterator(this, entity, aABB), false);
	}

	@Environment(EnvType.CLIENT)
	default boolean noBlockCollision(@Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
		return this.getBlockCollisions(entity, aABB, biPredicate).allMatch(VoxelShape::isEmpty);
	}

	default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
		return StreamSupport.stream(new CollisionSpliterator(this, entity, aABB, biPredicate), false);
	}
}
