/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.world.entity.Mob;

public class JumpControl {
    private final Mob mob;
    protected boolean jump;

    public JumpControl(Mob mob) {
        this.mob = mob;
    }

    public void jump() {
        this.jump = true;
    }

    public void tick() {
        this.mob.setJumping(this.jump);
        this.jump = false;
    }
}

