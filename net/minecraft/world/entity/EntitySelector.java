/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

public final class EntitySelector {
    public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
    public static final Predicate<LivingEntity> LIVING_ENTITY_STILL_ALIVE = LivingEntity::isAlive;
    public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = entity -> entity.isAlive() && !entity.isVehicle() && !entity.isPassenger();
    public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = entity -> entity instanceof Container && entity.isAlive();
    public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = entity -> !(entity instanceof Player) || !entity.isSpectator() && !((Player)entity).isCreative();
    public static final Predicate<Entity> ATTACK_ALLOWED = entity -> !(entity instanceof Player) || !entity.isSpectator() && !((Player)entity).isCreative() && entity.level.getDifficulty() != Difficulty.PEACEFUL;
    public static final Predicate<Entity> NO_SPECTATORS = entity -> !entity.isSpectator();

    public static Predicate<Entity> withinDistance(double d, double e, double f, double g) {
        double h = g * g;
        return entity -> entity != null && entity.distanceToSqr(d, e, f) <= h;
    }

    public static Predicate<Entity> pushableBy(Entity entity) {
        Team.CollisionRule collisionRule;
        Team team = entity.getTeam();
        Team.CollisionRule collisionRule2 = collisionRule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        if (collisionRule == Team.CollisionRule.NEVER) {
            return Predicates.alwaysFalse();
        }
        return NO_SPECTATORS.and(entity2 -> {
            boolean bl;
            Team.CollisionRule collisionRule2;
            if (!entity2.isPushable()) {
                return false;
            }
            if (!(!entity.level.isClientSide || entity2 instanceof Player && ((Player)entity2).isLocalPlayer())) {
                return false;
            }
            Team team2 = entity2.getTeam();
            Team.CollisionRule collisionRule3 = collisionRule2 = team2 == null ? Team.CollisionRule.ALWAYS : team2.getCollisionRule();
            if (collisionRule2 == Team.CollisionRule.NEVER) {
                return false;
            }
            boolean bl2 = bl = team != null && team.isAlliedTo(team2);
            if ((collisionRule == Team.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == Team.CollisionRule.PUSH_OWN_TEAM) && bl) {
                return false;
            }
            return collisionRule != Team.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != Team.CollisionRule.PUSH_OTHER_TEAMS || bl;
        });
    }

    public static Predicate<Entity> notRiding(Entity entity) {
        return entity2 -> {
            while (entity2.isPassenger()) {
                if ((entity2 = entity2.getVehicle()) != entity) continue;
                return false;
            }
            return true;
        };
    }

    public static class MobCanWearArmourEntitySelector
    implements Predicate<Entity> {
        private final ItemStack itemStack;

        public MobCanWearArmourEntitySelector(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public boolean test(@Nullable Entity entity) {
            if (!entity.isAlive()) {
                return false;
            }
            if (!(entity instanceof LivingEntity)) {
                return false;
            }
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity.canTakeItem(this.itemStack);
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Entity)object);
        }
    }
}

