package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

	Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate);

	default Stream<VoxelShape> getCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return Stream.concat(this.getBlockCollisions(entity, aABB), this.getEntityCollisions(entity, aABB, predicate));
	}

	default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB) {
		return StreamSupport.stream(new CollisionSpliterator(this, entity, aABB), false);
	}

	default boolean hasBlockCollision(@Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
		return !this.getBlockCollisions(entity, aABB, biPredicate).allMatch(VoxelShape::isEmpty);
	}

	default Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB aABB, BiPredicate<BlockState, BlockPos> biPredicate) {
		return StreamSupport.stream(new CollisionSpliterator(this, entity, aABB, biPredicate), false);
	}

	default Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape voxelShape, Vec3 vec3, double d, double e, double f) {
		if (voxelShape.isEmpty()) {
			return Optional.empty();
		} else {
			AABB aABB = voxelShape.bounds().inflate(d, e, f);
			VoxelShape voxelShape2 = (VoxelShape)this.getBlockCollisions(entity, aABB)
				.flatMap(voxelShapex -> voxelShapex.toAabbs().stream())
				.map(aABBx -> aABBx.inflate(d / 2.0, e / 2.0, f / 2.0))
				.map(Shapes::create)
				.reduce(Shapes.empty(), Shapes::or);
			VoxelShape voxelShape3 = Shapes.join(voxelShape, voxelShape2, BooleanOp.ONLY_FIRST);
			return voxelShape3.closestPointTo(vec3);
		}
	}
}
