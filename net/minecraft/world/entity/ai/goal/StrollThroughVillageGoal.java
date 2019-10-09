/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StrollThroughVillageGoal
extends Goal {
    private final PathfinderMob mob;
    private final int interval;
    @Nullable
    private BlockPos wantedPos;

    public StrollThroughVillageGoal(PathfinderMob pathfinderMob, int i) {
        this.mob = pathfinderMob;
        this.interval = i;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        if (this.mob.level.isDay()) {
            return false;
        }
        if (this.mob.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)this.mob.level;
        BlockPos blockPos2 = new BlockPos(this.mob);
        if (!serverLevel.closeToVillage(blockPos2, 6)) {
            return false;
        }
        Vec3 vec3 = RandomPos.getLandPos(this.mob, 15, 7, blockPos -> -serverLevel.sectionsToVillage(SectionPos.of(blockPos)));
        this.wantedPos = vec3 == null ? null : new BlockPos(vec3);
        return this.wantedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.wantedPos != null && !this.mob.getNavigation().isDone() && this.mob.getNavigation().getTargetPos().equals(this.wantedPos);
    }

    @Override
    public void tick() {
        if (this.wantedPos == null) {
            return;
        }
        PathNavigation pathNavigation = this.mob.getNavigation();
        if (pathNavigation.isDone() && !this.wantedPos.closerThan(this.mob.position(), 10.0)) {
            Vec3 vec3 = new Vec3(this.wantedPos);
            Vec3 vec32 = this.mob.position();
            Vec3 vec33 = vec32.subtract(vec3);
            vec3 = vec33.scale(0.4).add(vec3);
            Vec3 vec34 = vec3.subtract(vec32).normalize().scale(10.0).add(vec32);
            BlockPos blockPos = new BlockPos(vec34);
            if (!pathNavigation.moveTo((blockPos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos)).getX(), blockPos.getY(), blockPos.getZ(), 1.0)) {
                this.moveRandomly();
            }
        }
    }

    private void moveRandomly() {
        Random random = this.mob.getRandom();
        BlockPos blockPos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(this.mob).offset(-8 + random.nextInt(16), 0, -8 + random.nextInt(16)));
        this.mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
    }
}

