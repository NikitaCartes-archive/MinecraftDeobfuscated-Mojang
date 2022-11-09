package net.minecraft.world.entity;

import net.minecraft.world.entity.player.Player;

public interface PlayerRideableJumping extends PlayerRideable {
	void onPlayerJump(int i);

	boolean canJump(Player player);

	void handleStartJump(int i);

	void handleStopJump();

	default int getJumpCooldown() {
		return 0;
	}
}
