/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl {
    protected final Mob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected Operation operation = Operation.WAIT;

    public MoveControl(Mob mob) {
        this.mob = mob;
    }

    public boolean hasWanted() {
        return this.operation == Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public void setWantedPosition(double d, double e, double f, double g) {
        this.wantedX = d;
        this.wantedY = e;
        this.wantedZ = f;
        this.speedModifier = g;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }
    }

    public void strafe(float f, float g) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = f;
        this.strafeRight = g;
        this.speedModifier = 0.25;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float n;
            float f = (float)this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
            float g = (float)this.speedModifier * f;
            float h = this.strafeForwards;
            float i = this.strafeRight;
            float j = Mth.sqrt(h * h + i * i);
            if (j < 1.0f) {
                j = 1.0f;
            }
            j = g / j;
            float k = Mth.sin(this.mob.yRot * ((float)Math.PI / 180));
            float l = Mth.cos(this.mob.yRot * ((float)Math.PI / 180));
            float m = (h *= j) * l - (i *= j) * k;
            if (!this.isWalkable(m, n = i * l + h * k)) {
                this.strafeForwards = 1.0f;
                this.strafeRight = 0.0f;
            }
            this.mob.setSpeed(g);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double d = this.wantedX - this.mob.getX();
            double e = this.wantedZ - this.mob.getZ();
            double o = this.wantedY - this.mob.getY();
            double p = d * d + o * o + e * e;
            if (p < 2.500000277905201E-7) {
                this.mob.setZza(0.0f);
                return;
            }
            float n = (float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0f;
            this.mob.yRot = this.rotlerp(this.mob.yRot, n, 90.0f);
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
            BlockPos blockPos = this.mob.blockPosition();
            BlockState blockState = this.mob.level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            VoxelShape voxelShape = blockState.getCollisionShape(this.mob.level, blockPos);
            if (o > (double)this.mob.maxUpStep && d * d + e * e < (double)Math.max(1.0f, this.mob.getBbWidth()) || !voxelShape.isEmpty() && this.mob.getY() < voxelShape.max(Direction.Axis.Y) + (double)blockPos.getY() && !block.is(BlockTags.DOORS) && !block.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
            if (this.mob.isOnGround()) {
                this.operation = Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0f);
        }
    }

    private boolean isWalkable(float f, float g) {
        NodeEvaluator nodeEvaluator;
        PathNavigation pathNavigation = this.mob.getNavigation();
        return pathNavigation == null || (nodeEvaluator = pathNavigation.getNodeEvaluator()) == null || nodeEvaluator.getBlockPathType(this.mob.level, Mth.floor(this.mob.getX() + (double)f), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() + (double)g)) == BlockPathTypes.WALKABLE;
    }

    protected float rotlerp(float f, float g, float h) {
        float j;
        float i = Mth.wrapDegrees(g - f);
        if (i > h) {
            i = h;
        }
        if (i < -h) {
            i = -h;
        }
        if ((j = f + i) < 0.0f) {
            j += 360.0f;
        } else if (j > 360.0f) {
            j -= 360.0f;
        }
        return j;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    public static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;

    }
}

