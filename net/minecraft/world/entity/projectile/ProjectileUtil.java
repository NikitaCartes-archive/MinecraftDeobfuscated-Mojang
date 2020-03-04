/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ProjectileUtil {
    public static HitResult forwardsRaycast(Entity entity, boolean bl, boolean bl2, @Nullable Entity entity22, ClipContext.Block block) {
        return ProjectileUtil.forwardsRaycast(entity, bl, bl2, entity22, block, true, entity2 -> !entity2.isSpectator() && entity2.isPickable() && (bl2 || !entity2.is(entity22)) && !entity2.noPhysics, entity.getBoundingBox().expandTowards(entity.getDeltaMovement()).inflate(1.0));
    }

    public static HitResult getHitResult(Entity entity, AABB aABB, Predicate<Entity> predicate, ClipContext.Block block, boolean bl) {
        return ProjectileUtil.forwardsRaycast(entity, bl, false, null, block, false, predicate, aABB);
    }

    @Nullable
    public static EntityHitResult getHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate) {
        return ProjectileUtil.getHitResult(level, entity, vec3, vec32, aABB, predicate, Double.MAX_VALUE);
    }

    private static HitResult forwardsRaycast(Entity entity, boolean bl, boolean bl2, @Nullable Entity entity2, ClipContext.Block block, boolean bl3, Predicate<Entity> predicate, AABB aABB) {
        Vec3 vec3 = entity.getDeltaMovement();
        Level level = entity.level;
        Vec3 vec32 = entity.position();
        if (bl3 && !level.noCollision(entity, entity.getBoundingBox(), bl2 || entity2 == null ? ImmutableSet.of() : ProjectileUtil.getIgnoredEntities(entity2))) {
            return new BlockHitResult(vec32, Direction.getNearest(vec3.x, vec3.y, vec3.z), entity.blockPosition(), false);
        }
        Vec3 vec33 = vec32.add(vec3);
        HitResult hitResult = level.clip(new ClipContext(vec32, vec33, block, ClipContext.Fluid.NONE, entity));
        if (bl) {
            EntityHitResult hitResult2;
            if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
                vec33 = hitResult.getLocation();
            }
            if ((hitResult2 = ProjectileUtil.getHitResult(level, entity, vec32, vec33, aABB, predicate)) != null) {
                hitResult = hitResult2;
            }
        }
        return hitResult;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public static EntityHitResult getEntityHitResult(Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, double d) {
        Level level = entity.level;
        double e = d;
        Entity entity2 = null;
        Vec3 vec33 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            Vec3 vec34;
            double f;
            AABB aABB2 = entity3.getBoundingBox().inflate(entity3.getPickRadius());
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (aABB2.contains(vec3)) {
                if (!(e >= 0.0)) continue;
                entity2 = entity3;
                vec33 = optional.orElse(vec3);
                e = 0.0;
                continue;
            }
            if (!optional.isPresent() || !((f = vec3.distanceToSqr(vec34 = optional.get())) < e) && e != 0.0) continue;
            if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                if (e != 0.0) continue;
                entity2 = entity3;
                vec33 = vec34;
                continue;
            }
            entity2 = entity3;
            vec33 = vec34;
            e = f;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec33);
    }

    @Nullable
    public static EntityHitResult getHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, double d) {
        double e = d;
        Entity entity2 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            double f;
            AABB aABB2 = entity3.getBoundingBox().inflate(0.3f);
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (!optional.isPresent() || !((f = vec3.distanceToSqr(optional.get())) < e)) continue;
            entity2 = entity3;
            e = f;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2);
    }

    private static Set<Entity> getIgnoredEntities(Entity entity) {
        Entity entity2 = entity.getVehicle();
        return entity2 != null ? ImmutableSet.of(entity, entity2) : ImmutableSet.of(entity);
    }

    public static final void rotateTowardsMovement(Entity entity, float f) {
        Vec3 vec3 = entity.getDeltaMovement();
        float g = Mth.sqrt(Entity.getHorizontalDistanceSqr(vec3));
        entity.yRot = (float)(Mth.atan2(vec3.z, vec3.x) * 57.2957763671875) + 90.0f;
        entity.xRot = (float)(Mth.atan2(g, vec3.y) * 57.2957763671875) - 90.0f;
        while (entity.xRot - entity.xRotO < -180.0f) {
            entity.xRotO -= 360.0f;
        }
        while (entity.xRot - entity.xRotO >= 180.0f) {
            entity.xRotO += 360.0f;
        }
        while (entity.yRot - entity.yRotO < -180.0f) {
            entity.yRotO -= 360.0f;
        }
        while (entity.yRot - entity.yRotO >= 180.0f) {
            entity.yRotO += 360.0f;
        }
        entity.xRot = Mth.lerp(f, entity.xRotO, entity.xRot);
        entity.yRot = Mth.lerp(f, entity.yRotO, entity.yRot);
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity livingEntity, Item item) {
        return livingEntity.getMainHandItem().getItem() == item ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity livingEntity, ItemStack itemStack, float f) {
        ArrowItem arrowItem = (ArrowItem)(itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);
        AbstractArrow abstractArrow = arrowItem.createArrow(livingEntity.level, itemStack, livingEntity);
        abstractArrow.setEnchantmentEffectsFromEntity(livingEntity, f);
        if (itemStack.getItem() == Items.TIPPED_ARROW && abstractArrow instanceof Arrow) {
            ((Arrow)abstractArrow).setEffectsFromItem(itemStack);
        }
        return abstractArrow;
    }
}

