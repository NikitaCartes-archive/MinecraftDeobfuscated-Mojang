/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class WallClimberNavigation
extends GroundPathNavigation {
    private BlockPos pathToPosition;

    public WallClimberNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public Path createPath(BlockPos blockPos, int i) {
        this.pathToPosition = blockPos;
        return super.createPath(blockPos, i);
    }

    @Override
    public Path createPath(Entity entity, int i) {
        this.pathToPosition = new BlockPos(entity);
        return super.createPath(entity, i);
    }

    @Override
    public boolean moveTo(Entity entity, double d) {
        Path path = this.createPath(entity, 0);
        if (path != null) {
            return this.moveTo(path, d);
        }
        this.pathToPosition = new BlockPos(entity);
        this.speedModifier = d;
        return true;
    }

    @Override
    public void tick() {
        if (this.isDone()) {
            if (this.pathToPosition != null) {
                if (this.pathToPosition.closerThan(this.mob.position(), (double)this.mob.getBbWidth()) || this.mob.y > (double)this.pathToPosition.getY() && new BlockPos((double)this.pathToPosition.getX(), this.mob.y, (double)this.pathToPosition.getZ()).closerThan(this.mob.position(), (double)this.mob.getBbWidth())) {
                    this.pathToPosition = null;
                } else {
                    this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
                }
            }
            return;
        }
        super.tick();
    }
}

