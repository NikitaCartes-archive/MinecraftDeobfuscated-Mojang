/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.HostilesSensor;

public class VillagerHostilesSensor
extends HostilesSensor {
    private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.builder().put(EntityType.DROWNED, Float.valueOf(8.0f)).put(EntityType.EVOKER, Float.valueOf(12.0f)).put(EntityType.HUSK, Float.valueOf(8.0f)).put(EntityType.ILLUSIONER, Float.valueOf(12.0f)).put(EntityType.PILLAGER, Float.valueOf(15.0f)).put(EntityType.RAVAGER, Float.valueOf(12.0f)).put(EntityType.VEX, Float.valueOf(8.0f)).put(EntityType.VINDICATOR, Float.valueOf(10.0f)).put(EntityType.ZOGLIN, Float.valueOf(10.0f)).put(EntityType.ZOMBIE, Float.valueOf(8.0f)).put(EntityType.ZOMBIE_VILLAGER, Float.valueOf(8.0f)).build();

    @Override
    protected Optional<LivingEntity> getNearestHostile(LivingEntity livingEntity) {
        return this.getVisibleEntities(livingEntity).flatMap(list -> list.stream().filter(this::isHostile).filter(livingEntity2 -> this.isClose(livingEntity, (LivingEntity)livingEntity2)).min(Comparator.comparingDouble(livingEntity::distanceToSqr)));
    }

    @Override
    protected boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
        float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingEntity2.getType()).floatValue();
        return livingEntity2.distanceToSqr(livingEntity) <= (double)(f * f);
    }

    private boolean isHostile(LivingEntity livingEntity) {
        return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingEntity.getType());
    }
}

