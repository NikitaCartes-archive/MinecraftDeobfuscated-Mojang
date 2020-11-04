/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal
extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public HurtByTargetGoal(PathfinderMob pathfinderMob, Class<?> ... classs) {
        super(pathfinderMob, true);
        this.toIgnoreDamage = classs;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingEntity = this.mob.getLastHurtByMob();
        if (i == this.timestamp || livingEntity == null) {
            return false;
        }
        if (livingEntity.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            return false;
        }
        for (Class<?> class_ : this.toIgnoreDamage) {
            if (!class_.isAssignableFrom(livingEntity.getClass())) continue;
            return false;
        }
        return this.canAttack(livingEntity, HURT_BY_TARGETING);
    }

    public HurtByTargetGoal setAlertOthers(Class<?> ... classs) {
        this.alertSameType = true;
        this.toIgnoreAlert = classs;
        return this;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double d = this.getFollowDistance();
        AABB aABB = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d, 10.0, d);
        List<Entity> list = this.mob.level.getEntitiesOfClass(this.mob.getClass(), aABB, EntitySelector.NO_SPECTATORS);
        for (Mob mob : list) {
            if (this.mob == mob || mob.getTarget() != null || this.mob instanceof TamableAnimal && ((TamableAnimal)this.mob).getOwner() != ((TamableAnimal)mob).getOwner() || mob.isAlliedTo(this.mob.getLastHurtByMob())) continue;
            if (this.toIgnoreAlert != null) {
                boolean bl = false;
                for (Class<?> class_ : this.toIgnoreAlert) {
                    if (mob.getClass() != class_) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
            }
            this.alertOther(mob, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob mob, LivingEntity livingEntity) {
        mob.setTarget(livingEntity);
    }
}

