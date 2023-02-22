package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlockPosTracker implements PositionTracker {
	private final BlockPos blockPos;
	private final Vec3 centerPosition;

	public BlockPosTracker(BlockPos blockPos) {
		this.blockPos = blockPos.immutable();
		this.centerPosition = Vec3.atCenterOf(blockPos);
	}

	public BlockPosTracker(Vec3 vec3) {
		this.blockPos = BlockPos.containing(vec3);
		this.centerPosition = vec3;
	}

	@Override
	public Vec3 currentPosition() {
		return this.centerPosition;
	}

	@Override
	public BlockPos currentBlockPosition() {
		return this.blockPos;
	}

	@Override
	public boolean isVisibleBy(LivingEntity livingEntity) {
		return true;
	}

	public String toString() {
		return "BlockPosTracker{blockPos=" + this.blockPos + ", centerPosition=" + this.centerPosition + "}";
	}
}
