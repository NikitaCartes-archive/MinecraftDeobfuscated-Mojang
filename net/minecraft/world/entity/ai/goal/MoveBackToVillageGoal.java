/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MoveBackToVillageGoal
extends RandomStrollGoal {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public MoveBackToVillageGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
        super(pathfinderMob, d, 10, bl);
    }

    @Override
    public boolean canUse() {
        ServerLevel serverLevel = (ServerLevel)this.mob.level;
        BlockPos blockPos = this.mob.blockPosition();
        if (serverLevel.isVillage(blockPos)) {
            return false;
        }
        return super.canUse();
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        ServerLevel serverLevel = (ServerLevel)this.mob.level;
        BlockPos blockPos = this.mob.blockPosition();
        SectionPos sectionPos = SectionPos.of(blockPos);
        SectionPos sectionPos2 = BehaviorUtils.findSectionClosestToVillage(serverLevel, sectionPos, 2);
        if (sectionPos2 != sectionPos) {
            return DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(sectionPos2.center()), 1.5707963705062866);
        }
        return null;
    }
}

