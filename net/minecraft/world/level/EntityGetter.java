/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface EntityGetter {
    public List<Entity> getEntities(@Nullable Entity var1, AABB var2, @Nullable Predicate<? super Entity> var3);

    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> var1, AABB var2, @Nullable Predicate<? super T> var3);

    default public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> class_, AABB aABB, @Nullable Predicate<? super T> predicate) {
        return this.getEntitiesOfClass(class_, aABB, predicate);
    }

    public List<? extends Player> players();

    default public List<Entity> getEntities(@Nullable Entity entity, AABB aABB) {
        return this.getEntities(entity, aABB, EntitySelector.NO_SPECTATORS);
    }

    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        if (voxelShape.isEmpty()) {
            return true;
        }
        for (Entity entity2 : this.getEntities(entity, voxelShape.bounds())) {
            if (entity2.removed || !entity2.blocksBuilding || entity != null && entity2.isPassengerOfSameVehicle(entity) || !Shapes.joinIsNotEmpty(voxelShape, Shapes.create(entity2.getBoundingBox()), BooleanOp.AND)) continue;
            return false;
        }
        return true;
    }

    default public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> class_, AABB aABB) {
        return this.getEntitiesOfClass(class_, aABB, EntitySelector.NO_SPECTATORS);
    }

    default public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> class_, AABB aABB) {
        return this.getLoadedEntitiesOfClass(class_, aABB, EntitySelector.NO_SPECTATORS);
    }

    default public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
        if (aABB.getSize() < 1.0E-7) {
            return Stream.empty();
        }
        AABB aABB2 = aABB.inflate(1.0E-7);
        return this.getEntities(entity, aABB2, predicate.and(entity2 -> entity == null || !entity.isPassengerOfSameVehicle((Entity)entity2))).stream().flatMap(entity2 -> {
            AABB aABB2;
            if (entity != null && (aABB2 = entity.getCollideAgainstBox((Entity)entity2)) != null && aABB2.intersects(aABB2)) {
                return Stream.of(entity2.getCollideBox(), aABB2);
            }
            return Stream.of(entity2.getCollideBox());
        }).filter(Objects::nonNull).map(Shapes::create);
    }

    @Nullable
    default public Player getNearestPlayer(double d, double e, double f, double g, @Nullable Predicate<Entity> predicate) {
        double h = -1.0;
        Player player = null;
        for (Player player2 : this.players()) {
            if (predicate != null && !predicate.test(player2)) continue;
            double i = player2.distanceToSqr(d, e, f);
            if (!(g < 0.0) && !(i < g * g) || h != -1.0 && !(i < h)) continue;
            h = i;
            player = player2;
        }
        return player;
    }

    @Nullable
    default public Player getNearestPlayer(Entity entity, double d) {
        return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), d, false);
    }

    @Nullable
    default public Player getNearestPlayer(double d, double e, double f, double g, boolean bl) {
        Predicate<Entity> predicate = bl ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
        return this.getNearestPlayer(d, e, f, g, predicate);
    }

    default public boolean hasNearbyAlivePlayer(double d, double e, double f, double g) {
        for (Player player : this.players()) {
            if (!EntitySelector.NO_SPECTATORS.test(player) || !EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player)) continue;
            double h = player.distanceToSqr(d, e, f);
            if (!(g < 0.0) && !(h < g * g)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    default public Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity) {
        return this.getNearestEntity(this.players(), targetingConditions, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
    }

    @Nullable
    default public Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity livingEntity, double d, double e, double f) {
        return this.getNearestEntity(this.players(), targetingConditions, livingEntity, d, e, f);
    }

    @Nullable
    default public Player getNearestPlayer(TargetingConditions targetingConditions, double d, double e, double f) {
        return this.getNearestEntity(this.players(), targetingConditions, null, d, e, f);
    }

    @Nullable
    default public <T extends LivingEntity> T getNearestEntity(Class<? extends T> class_, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double e, double f, AABB aABB) {
        return this.getNearestEntity(this.getEntitiesOfClass(class_, aABB, null), targetingConditions, livingEntity, d, e, f);
    }

    @Nullable
    default public <T extends LivingEntity> T getNearestLoadedEntity(Class<? extends T> class_, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double e, double f, AABB aABB) {
        return this.getNearestEntity(this.getLoadedEntitiesOfClass(class_, aABB, null), targetingConditions, livingEntity, d, e, f);
    }

    @Nullable
    default public <T extends LivingEntity> T getNearestEntity(List<? extends T> list, TargetingConditions targetingConditions, @Nullable LivingEntity livingEntity, double d, double e, double f) {
        double g = -1.0;
        LivingEntity livingEntity2 = null;
        for (LivingEntity livingEntity3 : list) {
            if (!targetingConditions.test(livingEntity, livingEntity3)) continue;
            double h = livingEntity3.distanceToSqr(d, e, f);
            if (g != -1.0 && !(h < g)) continue;
            g = h;
            livingEntity2 = livingEntity3;
        }
        return (T)livingEntity2;
    }

    default public List<Player> getNearbyPlayers(TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aABB) {
        ArrayList<Player> list = Lists.newArrayList();
        for (Player player : this.players()) {
            if (!aABB.contains(player.getX(), player.getY(), player.getZ()) || !targetingConditions.test(livingEntity, player)) continue;
            list.add(player);
        }
        return list;
    }

    default public <T extends LivingEntity> List<T> getNearbyEntities(Class<? extends T> class_, TargetingConditions targetingConditions, LivingEntity livingEntity, AABB aABB) {
        List<T> list = this.getEntitiesOfClass(class_, aABB, null);
        ArrayList<LivingEntity> list2 = Lists.newArrayList();
        for (LivingEntity livingEntity2 : list) {
            if (!targetingConditions.test(livingEntity, livingEntity2)) continue;
            list2.add(livingEntity2);
        }
        return list2;
    }

    @Nullable
    default public Player getPlayerByUUID(UUID uUID) {
        for (int i = 0; i < this.players().size(); ++i) {
            Player player = this.players().get(i);
            if (!uUID.equals(player.getUUID())) continue;
            return player;
        }
        return null;
    }
}

