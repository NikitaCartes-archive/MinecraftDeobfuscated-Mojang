/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DragonSittingScanningPhase
extends AbstractDragonSittingPhase {
    private static final TargetingConditions CHARGE_TARGETING = new TargetingConditions().range(150.0);
    private final TargetingConditions scanTargeting = new TargetingConditions().range(20.0).selector(livingEntity -> Math.abs(livingEntity.y - enderDragon.y) <= 10.0);
    private int scanningTime;

    public DragonSittingScanningPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick() {
        ++this.scanningTime;
        Player livingEntity = this.dragon.level.getNearestPlayer(this.scanTargeting, this.dragon, this.dragon.x, this.dragon.y, this.dragon.z);
        if (livingEntity != null) {
            if (this.scanningTime > 25) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
            } else {
                Vec3 vec3 = new Vec3(livingEntity.x - this.dragon.x, 0.0, livingEntity.z - this.dragon.z).normalize();
                Vec3 vec32 = new Vec3(Mth.sin(this.dragon.yRot * ((float)Math.PI / 180)), 0.0, -Mth.cos(this.dragon.yRot * ((float)Math.PI / 180))).normalize();
                float f = (float)vec32.dot(vec3);
                float g = (float)(Math.acos(f) * 57.2957763671875) + 0.5f;
                if (g < 0.0f || g > 10.0f) {
                    float i;
                    double d = livingEntity.x - this.dragon.head.x;
                    double e = livingEntity.z - this.dragon.head.z;
                    double h = Mth.clamp(Mth.wrapDegrees(180.0 - Mth.atan2(d, e) * 57.2957763671875 - (double)this.dragon.yRot), -100.0, 100.0);
                    this.dragon.yRotA *= 0.8f;
                    float j = i = Mth.sqrt(d * d + e * e) + 1.0f;
                    if (i > 40.0f) {
                        i = 40.0f;
                    }
                    this.dragon.yRotA = (float)((double)this.dragon.yRotA + h * (double)(0.7f / i / j));
                    this.dragon.yRot += this.dragon.yRotA;
                }
            }
        } else if (this.scanningTime >= 100) {
            livingEntity = this.dragon.level.getNearestPlayer(CHARGE_TARGETING, this.dragon, this.dragon.x, this.dragon.y, this.dragon.z);
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            if (livingEntity != null) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(new Vec3(livingEntity.x, livingEntity.y, livingEntity.z));
            }
        }
    }

    @Override
    public void begin() {
        this.scanningTime = 0;
    }

    public EnderDragonPhase<DragonSittingScanningPhase> getPhase() {
        return EnderDragonPhase.SITTING_SCANNING;
    }
}

