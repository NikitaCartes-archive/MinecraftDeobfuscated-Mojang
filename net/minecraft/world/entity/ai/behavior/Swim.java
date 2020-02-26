/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class Swim
extends Behavior<Mob> {
    private final float height;
    private final float chance;

    public Swim(float f, float g) {
        super(ImmutableMap.of());
        this.height = f;
        this.chance = g;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        return mob.isInWater() && mob.getFluidHeight() > (double)this.height || mob.isInLava();
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        return this.checkExtraStartConditions(serverLevel, mob);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Mob mob, long l) {
        if (mob.getRandom().nextFloat() < this.chance) {
            mob.getJumpControl().jump();
        }
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Mob)livingEntity, l);
    }
}

