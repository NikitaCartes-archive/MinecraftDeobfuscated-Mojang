/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonDeathPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoverPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingApproachPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingAttackingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingFlamingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonTakeoffPhase;

public class EnderDragonPhase<T extends DragonPhaseInstance> {
    private static EnderDragonPhase<?>[] phases = new EnderDragonPhase[0];
    public static final EnderDragonPhase<DragonHoldingPatternPhase> HOLDING_PATTERN = EnderDragonPhase.create(DragonHoldingPatternPhase.class, "HoldingPattern");
    public static final EnderDragonPhase<DragonStrafePlayerPhase> STRAFE_PLAYER = EnderDragonPhase.create(DragonStrafePlayerPhase.class, "StrafePlayer");
    public static final EnderDragonPhase<DragonLandingApproachPhase> LANDING_APPROACH = EnderDragonPhase.create(DragonLandingApproachPhase.class, "LandingApproach");
    public static final EnderDragonPhase<DragonLandingPhase> LANDING = EnderDragonPhase.create(DragonLandingPhase.class, "Landing");
    public static final EnderDragonPhase<DragonTakeoffPhase> TAKEOFF = EnderDragonPhase.create(DragonTakeoffPhase.class, "Takeoff");
    public static final EnderDragonPhase<DragonSittingFlamingPhase> SITTING_FLAMING = EnderDragonPhase.create(DragonSittingFlamingPhase.class, "SittingFlaming");
    public static final EnderDragonPhase<DragonSittingScanningPhase> SITTING_SCANNING = EnderDragonPhase.create(DragonSittingScanningPhase.class, "SittingScanning");
    public static final EnderDragonPhase<DragonSittingAttackingPhase> SITTING_ATTACKING = EnderDragonPhase.create(DragonSittingAttackingPhase.class, "SittingAttacking");
    public static final EnderDragonPhase<DragonChargePlayerPhase> CHARGING_PLAYER = EnderDragonPhase.create(DragonChargePlayerPhase.class, "ChargingPlayer");
    public static final EnderDragonPhase<DragonDeathPhase> DYING = EnderDragonPhase.create(DragonDeathPhase.class, "Dying");
    public static final EnderDragonPhase<DragonHoverPhase> HOVERING = EnderDragonPhase.create(DragonHoverPhase.class, "Hover");
    private final Class<? extends DragonPhaseInstance> instanceClass;
    private final int id;
    private final String name;

    private EnderDragonPhase(int i, Class<? extends DragonPhaseInstance> class_, String string) {
        this.id = i;
        this.instanceClass = class_;
        this.name = string;
    }

    public DragonPhaseInstance createInstance(EnderDragon enderDragon) {
        try {
            Constructor<DragonPhaseInstance> constructor = this.getConstructor();
            return constructor.newInstance(enderDragon);
        } catch (Exception exception) {
            throw new Error(exception);
        }
    }

    protected Constructor<? extends DragonPhaseInstance> getConstructor() throws NoSuchMethodException {
        return this.instanceClass.getConstructor(EnderDragon.class);
    }

    public int getId() {
        return this.id;
    }

    public String toString() {
        return this.name + " (#" + this.id + ")";
    }

    public static EnderDragonPhase<?> getById(int i) {
        if (i < 0 || i >= phases.length) {
            return HOLDING_PATTERN;
        }
        return phases[i];
    }

    public static int getCount() {
        return phases.length;
    }

    private static <T extends DragonPhaseInstance> EnderDragonPhase<T> create(Class<T> class_, String string) {
        EnderDragonPhase<T> enderDragonPhase = new EnderDragonPhase<T>(phases.length, class_, string);
        phases = Arrays.copyOf(phases, phases.length + 1);
        EnderDragonPhase.phases[enderDragonPhase.getId()] = enderDragonPhase;
        return enderDragonPhase;
    }
}

