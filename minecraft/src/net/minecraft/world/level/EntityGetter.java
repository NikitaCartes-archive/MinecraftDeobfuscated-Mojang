package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter {
	List<Entity> getEntities(@Nullable Entity entity, AABB aABB, Predicate<? super Entity> predicate);

	<T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate);

	default <T extends Entity> List<T> getEntitiesOfClass(Class<T> class_, AABB aABB, Predicate<? super T> predicate) {
		return this.getEntities(EntityTypeTest.forClass(class_), aABB, predicate);
	}

	List<? extends Player> players();

	default List<Entity> getEntities(@Nullable Entity entity, AABB aABB) {
		return this.getEntities(entity, aABB, EntitySelector.NO_SPECTATORS);
	}

	default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
		if (voxelShape.isEmpty()) {
			return true;
		} else {
			for (Entity entity2 : this.getEntities(entity, voxelShape.bounds())) {
				if (!entity2.isRemoved()
					&& entity2.blocksBuilding
					&& (entity == null || !entity2.isPassengerOfSameVehicle(entity))
					&& Shapes.joinIsNotEmpty(voxelShape, Shapes.create(entity2.getBoundingBox()), BooleanOp.AND)) {
					return false;
				}
			}

			return true;
		}
	}

	default <T extends Entity> List<T> getEntitiesOfClass(Class<T> class_, AABB aABB) {
		return this.getEntitiesOfClass(class_, aABB, EntitySelector.NO_SPECTATORS);
	}

	default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB) {
		if (aABB.getSize() < 1.0E-7) {
			return List.of();
		} else {
			Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
			List<Entity> list = this.getEntities(entity, aABB.inflate(1.0E-7), predicate);
			if (list.isEmpty()) {
				return List.of();
			} else {
				Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size());

				for (Entity entity2 : list) {
					builder.add(Shapes.create(entity2.getBoundingBox()));
				}

				return builder.build();
			}
		}
	}

	@Nullable
	default Player getNearestPlayer(double d, double e, double f, double g, @Nullable Predicate<Entity> predicate) {
		double h = -1.0;
		Player player = null;

		for (Player player2 : this.players()) {
			if (predicate == null || predicate.test(player2)) {
				double i = player2.distanceToSqr(d, e, f);
				if ((g < 0.0 || i < g * g) && (h == -1.0 || i < h)) {
					h = i;
					player = player2;
				}
			}
		}

		return player;
	}

	@Nullable
	default Player getNearestPlayer(Entity entity, double d) {
		return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), d, false);
	}

	@Nullable
	default Player getNearestPlayer(double d, double e, double f, double g, boolean bl) {
		Predicate<Entity> predicate = bl ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
		return this.getNearestPlayer(d, e, f, g, predicate);
	}

	default boolean hasNearbyAlivePlayer(double d, double e, double f, double g) {
		for (Player player : this.players()) {
			if (EntitySelector.NO_SPECTATORS.test(player) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player)) {
				double h = player.distanceToSqr(d, e, f);
				if (g < 0.0 || h < g * g) {
					return true;
				}
			}
		}

		return false;
	}

	@Nullable
	default Player getPlayerByUUID(UUID uUID) {
		for (int i = 0; i < this.players().size(); i++) {
			Player player = (Player)this.players().get(i);
			if (uUID.equals(player.getUUID())) {
				return player;
			}
		}

		return null;
	}
}
