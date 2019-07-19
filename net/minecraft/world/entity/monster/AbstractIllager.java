/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager
extends Raider {
    protected AbstractIllager(EntityType<? extends AbstractIllager> entityType, Level level) {
        super((EntityType<? extends Raider>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public MobType getMobType() {
        return MobType.ILLAGER;
    }

    @Environment(value=EnvType.CLIENT)
    public IllagerArmPose getArmPose() {
        return IllagerArmPose.CROSSED;
    }

    public class RaiderOpenDoorGoal
    extends OpenDoorGoal {
        public RaiderOpenDoorGoal(Raider raider) {
            super(raider, false);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && AbstractIllager.this.hasActiveRaid();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum IllagerArmPose {
        CROSSED,
        ATTACKING,
        SPELLCASTING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        CELEBRATING;

    }
}

