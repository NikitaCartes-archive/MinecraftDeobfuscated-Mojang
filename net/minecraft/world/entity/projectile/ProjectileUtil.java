/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ProjectileUtil {
    public static HitResult getHitResult(Entity entity, Predicate<Entity> predicate) {
        EntityHitResult hitResult2;
        Vec3 vec33;
        Vec3 vec3 = entity.getDeltaMovement();
        Level level = entity.level;
        Vec3 vec32 = entity.position();
        HitResult hitResult = level.clip(new ClipContext(vec32, vec33 = vec32.add(vec3), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
            vec33 = hitResult.getLocation();
        }
        if ((hitResult2 = ProjectileUtil.getEntityHitResult(level, entity, vec32, vec33, entity.getBoundingBox().expandTowards(entity.getDeltaMovement()).inflate(1.0), predicate)) != null) {
            hitResult = hitResult2;
        }
        return hitResult;
    }

    @Nullable
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
    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate) {
        double d = Double.MAX_VALUE;
        Entity entity2 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            double e;
            AABB aABB2 = entity3.getBoundingBox().inflate(0.3f);
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (!optional.isPresent() || !((e = vec3.distanceToSqr(optional.get())) < d)) continue;
            entity2 = entity3;
            d = e;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2);
    }

    public static void rotateTowardsMovement(Entity entity, float f) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.lengthSqr() == 0.0) {
            return;
        }
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
        return livingEntity.getMainHandItem().is(item) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity livingEntity, ItemStack itemStack, float f) {
        ArrowItem arrowItem = (ArrowItem)(itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);
        AbstractArrow abstractArrow = arrowItem.createArrow(livingEntity.level, itemStack, livingEntity);
        abstractArrow.setEnchantmentEffectsFromEntity(livingEntity, f);
        if (itemStack.is(Items.TIPPED_ARROW) && abstractArrow instanceof Arrow) {
            ((Arrow)abstractArrow).setEffectsFromItem(itemStack);
        }
        return abstractArrow;
    }
}

