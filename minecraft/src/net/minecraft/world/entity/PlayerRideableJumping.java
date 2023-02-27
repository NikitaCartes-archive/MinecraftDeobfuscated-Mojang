package net.minecraft.world.entity;

public interface PlayerRideableJumping extends PlayerRideable {
	void onPlayerJump(int i);

	boolean canJump();

	void handleStartJump(int i);

	void handleStopJump();

	default int getJumpCooldown() {
		return 0;
	}
}
