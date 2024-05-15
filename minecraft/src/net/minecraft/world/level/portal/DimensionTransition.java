package net.minecraft.world.level.portal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public record DimensionTransition(ServerLevel newDimension, Vec3 pos, Vec3 speed, float yRot, float xRot, boolean missingRespawnBlock) {
	public DimensionTransition(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, float f, float g) {
		this(serverLevel, vec3, vec32, f, g, false);
	}

	public DimensionTransition(ServerLevel serverLevel) {
		this(serverLevel, serverLevel.getSharedSpawnPos().getCenter(), Vec3.ZERO, 0.0F, 0.0F, false);
	}

	public static DimensionTransition missingRespawnBlock(ServerLevel serverLevel) {
		return new DimensionTransition(serverLevel, serverLevel.getSharedSpawnPos().getCenter(), Vec3.ZERO, 0.0F, 0.0F, true);
	}
}
