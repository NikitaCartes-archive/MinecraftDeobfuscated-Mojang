/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.raid.Raider;
import org.jetbrains.annotations.Nullable;

public class NearestHealableRaiderTargetGoal<T extends LivingEntity>
extends NearestAttackableTargetGoal<T> {
    private int cooldown = 0;

    public NearestHealableRaiderTargetGoal(Raider raider, Class<T> class_, boolean bl, @Nullable Predicate<LivingEntity> predicate) {
        super(raider, class_, 500, bl, false, predicate);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void decrementCooldown() {
        --this.cooldown;
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0 || !this.mob.getRandom().nextBoolean()) {
            return false;
        }
        if (!((Raider)this.mob).hasActiveRaid()) {
            return false;
        }
        this.findTarget();
        return this.target != null;
    }

    @Override
    public void start() {
        this.cooldown = 200;
        super.start();
    }
}

