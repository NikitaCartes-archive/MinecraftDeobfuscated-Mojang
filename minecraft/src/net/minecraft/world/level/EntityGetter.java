package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
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

	default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		if (aABB.getSize() < 1.0E-7) {
			return Stream.empty();
		} else {
			AABB aABB2 = aABB.inflate(1.0E-7);
			return this.getEntities(
					entity,
					aABB2,
					predicate.and(entity2 -> entity2.getBoundingBox().intersects(aABB2) && (entity == null ? entity2.canBeCollidedWith() : entity.canCollideWith(entity2)))
				)
				.stream()
				.map(Entity::getBoundingBox)
				.map(Shapes::create);
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
	default Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity) {
		return this.getNearestEntity(this.players(), targetingConditions, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
	}

	@Nullable
	default Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity, double d, double e, double f) {
		return this.getNearestEntity(this.players(), targetingConditions, livingEntity, d, e, f);
	}

	@Nullable
	default Player getNearestPlayer(TargetingConditions targetingConditions, double d, double e, double f) {
		return this.getNearestEntity(this.players(), targetingConditions, null, d, e, f);
	}

	@Nullable
	default <T extends LivingEntity> T getNearestEntity(
		Class<? extends T> class_, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double e, double f, AABB aABB
	) {
		return this.getNearestEntity(this.getEntitiesOfClass(class_, aABB, livingEntityx -> true), targetingConditions, livingEntity, d, e, f);
	}

	@Nullable
	default <T extends LivingEntity> T getNearestEntity(
		List<? extends T> list, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double e, double f
	) {
		double g = -1.0;
		T livingEntity2 = null;

		for (T livingEntity3 : list) {
			if (targetingConditions.test(livingEntity, livingEntity3)) {
				double h = livingEntity3.distanceToSqr(d, e, f);
				if (g == -1.0 || h < g) {
					g = h;
					livingEntity2 = livingEntity3;
				}
			}
		}

		return livingEntity2;
	}

	default List<Player> getNearbyPlayers(TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aABB) {
		List<Player> list = Lists.<Player>newArrayList();

		for (Player player : this.players()) {
			if (aABB.contains(player.getX(), player.getY(), player.getZ()) && targetingConditions.test(livingEntity, player)) {
				list.add(player);
			}
		}

		return list;
	}

	default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> class_, TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aABB) {
		List<T> list = this.getEntitiesOfClass(class_, aABB, livingEntityx -> true);
		List<T> list2 = Lists.<T>newArrayList();

		for (T livingEntity2 : list) {
			if (targetingConditions.test(livingEntity, livingEntity2)) {
				list2.add(livingEntity2);
			}
		}

		return list2;
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
