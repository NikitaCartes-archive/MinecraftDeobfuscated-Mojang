package net.minecraft.world.entity.ai.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BlockPosWrapper;
import net.minecraft.world.entity.ai.behavior.PositionWrapper;
import net.minecraft.world.phys.Vec3;

public class WalkTarget {
	private final PositionWrapper target;
	private final float speedModifier;
	private final int closeEnoughDist;

	public WalkTarget(BlockPos blockPos, float f, int i) {
		this(new BlockPosWrapper(blockPos), f, i);
	}

	public WalkTarget(Vec3 vec3, float f, int i) {
		this(new BlockPosWrapper(new BlockPos(vec3)), f, i);
	}

	public WalkTarget(PositionWrapper positionWrapper, float f, int i) {
		this.target = positionWrapper;
		this.speedModifier = f;
		this.closeEnoughDist = i;
	}

	public PositionWrapper getTarget() {
		return this.target;
	}

	public float getSpeedModifier() {
		return this.speedModifier;
	}

	public int getCloseEnoughDist() {
		return this.closeEnoughDist;
	}
}
