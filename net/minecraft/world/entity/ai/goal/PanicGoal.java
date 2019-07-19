/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PanicGoal
extends Goal {
    protected final PathfinderMob mob;
    protected final double speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;

    public PanicGoal(PathfinderMob pathfinderMob, double d) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos blockPos;
        if (this.mob.getLastHurtByMob() == null && !this.mob.isOnFire()) {
            return false;
        }
        if (this.mob.isOnFire() && (blockPos = this.lookForWater(this.mob.level, this.mob, 5, 4)) != null) {
            this.posX = blockPos.getX();
            this.posY = blockPos.getY();
            this.posZ = blockPos.getZ();
            return true;
        }
        return this.findRandomPosition();
    }

    protected boolean findRandomPosition() {
        Vec3 vec3 = RandomPos.getPos(this.mob, 5, 4);
        if (vec3 == null) {
            return false;
        }
        this.posX = vec3.x;
        this.posY = vec3.y;
        this.posZ = vec3.z;
        return true;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected BlockPos lookForWater(BlockGetter blockGetter, Entity entity, int i, int j) {
        BlockPos blockPos = new BlockPos(entity);
        int k = blockPos.getX();
        int l = blockPos.getY();
        int m = blockPos.getZ();
        float f = i * i * j * 2;
        BlockPos blockPos2 = null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int n = k - i; n <= k + i; ++n) {
            for (int o = l - j; o <= l + j; ++o) {
                for (int p = m - i; p <= m + i; ++p) {
                    float g;
                    mutableBlockPos.set(n, o, p);
                    if (!blockGetter.getFluidState(mutableBlockPos).is(FluidTags.WATER) || !((g = (float)((n - k) * (n - k) + (o - l) * (o - l) + (p - m) * (p - m))) < f)) continue;
                    f = g;
                    blockPos2 = new BlockPos(mutableBlockPos);
                }
            }
        }
        return blockPos2;
    }
}

