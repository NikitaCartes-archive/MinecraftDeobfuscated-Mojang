/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface PlayerRideableJumping {
    @Environment(value=EnvType.CLIENT)
    public void onPlayerJump(int var1);

    public boolean canJump();

    public void handleStartJump(int var1);

    public void handleStopJump();
}

