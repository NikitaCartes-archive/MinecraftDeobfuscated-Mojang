/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DragonStrafePlayerPhase
extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int FIREBALL_CHARGE_AMOUNT = 5;
    private int fireballCharge;
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;
    @Nullable
    private LivingEntity attackTarget;
    private boolean holdingPatternClockwise;

    public DragonStrafePlayerPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick() {
        double h;
        double e;
        double d;
        if (this.attackTarget == null) {
            LOGGER.warn("Skipping player strafe phase because no player was found");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }
        if (this.currentPath != null && this.currentPath.isDone()) {
            d = this.attackTarget.getX();
            e = this.attackTarget.getZ();
            double f = d - this.dragon.getX();
            double g = e - this.dragon.getZ();
            h = Math.sqrt(f * f + g * g);
            double i = Math.min((double)0.4f + h / 80.0 - 1.0, 10.0);
            this.targetLocation = new Vec3(d, this.attackTarget.getY() + i, e);
        }
        double d2 = d = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0) {
            this.findNewTarget();
        }
        e = 64.0;
        if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0) {
            if (this.dragon.hasLineOfSight(this.attackTarget)) {
                ++this.fireballCharge;
                Vec3 vec3 = new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0, this.attackTarget.getZ() - this.dragon.getZ()).normalize();
                Vec3 vec32 = new Vec3(Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180)), 0.0, -Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180))).normalize();
                float j = (float)vec32.dot(vec3);
                float k = (float)(Math.acos(j) * 57.2957763671875);
                k += 0.5f;
                if (this.fireballCharge >= 5 && k >= 0.0f && k < 10.0f) {
                    h = 1.0;
                    Vec3 vec33 = this.dragon.getViewVector(1.0f);
                    double l = this.dragon.head.getX() - vec33.x * 1.0;
                    double m = this.dragon.head.getY(0.5) + 0.5;
                    double n = this.dragon.head.getZ() - vec33.z * 1.0;
                    double o = this.attackTarget.getX() - l;
                    double p = this.attackTarget.getY(0.5) - m;
                    double q = this.attackTarget.getZ() - n;
                    if (!this.dragon.isSilent()) {
                        this.dragon.level.levelEvent(null, 1017, this.dragon.blockPosition(), 0);
                    }
                    DragonFireball dragonFireball = new DragonFireball(this.dragon.level, this.dragon, o, p, q);
                    dragonFireball.moveTo(l, m, n, 0.0f, 0.0f);
                    this.dragon.level.addFreshEntity(dragonFireball);
                    this.fireballCharge = 0;
                    if (this.currentPath != null) {
                        while (!this.currentPath.isDone()) {
                            this.currentPath.advance();
                        }
                    }
                    this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
                }
            } else if (this.fireballCharge > 0) {
                --this.fireballCharge;
            }
        } else if (this.fireballCharge > 0) {
            --this.fireballCharge;
        }
    }

    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int i;
            int j = i = this.dragon.findClosestNode();
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.holdingPatternClockwise = !this.holdingPatternClockwise;
                j += 6;
            }
            j = this.holdingPatternClockwise ? ++j : --j;
            if (this.dragon.getDragonFight() == null || this.dragon.getDragonFight().getCrystalsAlive() <= 0) {
                j -= 12;
                j &= 7;
                j += 12;
            } else if ((j %= 12) < 0) {
                j += 12;
            }
            this.currentPath = this.dragon.findPath(i, j, null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }
        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            double f;
            BlockPos vec3i = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double d = vec3i.getX();
            double e = vec3i.getZ();
            while ((f = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)vec3i.getY()) {
            }
            this.targetLocation = new Vec3(d, f, e);
        }
    }

    @Override
    public void begin() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
    }

    public void setTarget(LivingEntity livingEntity) {
        this.attackTarget = livingEntity;
        int i = this.dragon.findClosestNode();
        int j = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
        int k = this.attackTarget.getBlockX();
        int l = this.attackTarget.getBlockZ();
        double d = (double)k - this.dragon.getX();
        double e = (double)l - this.dragon.getZ();
        double f = Math.sqrt(d * d + e * e);
        double g = Math.min((double)0.4f + f / 80.0 - 1.0, 10.0);
        int m = Mth.floor(this.attackTarget.getY() + g);
        Node node = new Node(k, m, l);
        this.currentPath = this.dragon.findPath(i, j, node);
        if (this.currentPath != null) {
            this.currentPath.advance();
            this.navigateToNextPathNode();
        }
    }

    @Override
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public EnderDragonPhase<DragonStrafePlayerPhase> getPhase() {
        return EnderDragonPhase.STRAFE_PLAYER;
    }
}

