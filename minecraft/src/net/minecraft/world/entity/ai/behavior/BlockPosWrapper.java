package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlockPosWrapper implements PositionWrapper {
	private final BlockPos pos;
	private final Vec3 lookAt;

	public BlockPosWrapper(BlockPos blockPos) {
		this.pos = blockPos;
		this.lookAt = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
	}

	@Override
	public BlockPos getPos() {
		return this.pos;
	}

	@Override
	public Vec3 getLookAtPos() {
		return this.lookAt;
	}

	@Override
	public boolean isVisible(LivingEntity livingEntity) {
		return true;
	}

	public String toString() {
		return "BlockPosWrapper{pos=" + this.pos + ", lookAt=" + this.lookAt + '}';
	}
}
