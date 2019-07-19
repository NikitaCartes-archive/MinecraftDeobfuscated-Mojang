package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface PositionWrapper {
	BlockPos getPos();

	Vec3 getLookAtPos();

	boolean isVisible(LivingEntity livingEntity);
}
