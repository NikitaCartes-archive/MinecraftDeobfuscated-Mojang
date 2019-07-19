package net.minecraft.world.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface PlayerRideableJumping {
	@Environment(EnvType.CLIENT)
	void onPlayerJump(int i);

	boolean canJump();

	void handleStartJump(int i);

	void handleStopJump();
}
