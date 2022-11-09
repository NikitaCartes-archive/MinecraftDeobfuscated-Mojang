/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.player.Player;

public interface PlayerRideableJumping
extends PlayerRideable {
    public void onPlayerJump(int var1);

    public boolean canJump(Player var1);

    public void handleStartJump(int var1);

    public void handleStopJump();

    default public int getJumpCooldown() {
        return 0;
    }
}

