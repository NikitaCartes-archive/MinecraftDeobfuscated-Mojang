/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

public class PathfindToRaidGoal<T extends Raider>
extends Goal {
    private final T mob;

    public PathfindToRaidGoal(T raider) {
        this.mob = raider;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return ((Mob)this.mob).getTarget() == null && !((Entity)this.mob).isVehicle() && ((Raider)this.mob).hasActiveRaid() && !((Raider)this.mob).getCurrentRaid().isOver() && !((ServerLevel)((Raider)this.mob).level).isVillage(new BlockPos((Entity)this.mob));
    }

    @Override
    public boolean canContinueToUse() {
        return ((Raider)this.mob).hasActiveRaid() && !((Raider)this.mob).getCurrentRaid().isOver() && ((Raider)this.mob).level instanceof ServerLevel && !((ServerLevel)((Raider)this.mob).level).isVillage(new BlockPos((Entity)this.mob));
    }

    @Override
    public void tick() {
        if (((Raider)this.mob).hasActiveRaid()) {
            Vec3 vec3;
            Raid raid = ((Raider)this.mob).getCurrentRaid();
            if (((Raider)this.mob).tickCount % 20 == 0) {
                this.recruitNearby(raid);
            }
            if (!((PathfinderMob)this.mob).isPathFinding() && (vec3 = RandomPos.getPosTowards(this.mob, 15, 4, new Vec3(raid.getCenter()))) != null) {
                ((Mob)this.mob).getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0);
            }
        }
    }

    private void recruitNearby(Raid raid) {
        if (raid.isActive()) {
            HashSet<Raider> set = Sets.newHashSet();
            List<Raider> list = ((Raider)this.mob).level.getEntitiesOfClass(Raider.class, ((Entity)this.mob).getBoundingBox().inflate(16.0), raider -> !raider.hasActiveRaid() && Raids.canJoinRaid(raider, raid));
            set.addAll(list);
            for (Raider raider2 : set) {
                raid.joinRaid(raid.getGroupsSpawned(), raider2, null, true);
            }
        }
    }
}

