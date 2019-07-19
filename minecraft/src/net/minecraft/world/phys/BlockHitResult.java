package net.minecraft.world.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult {
	private final Direction direction;
	private final BlockPos blockPos;
	private final boolean miss;
	private final boolean inside;

	public static BlockHitResult miss(Vec3 vec3, Direction direction, BlockPos blockPos) {
		return new BlockHitResult(true, vec3, direction, blockPos, false);
	}

	public BlockHitResult(Vec3 vec3, Direction direction, BlockPos blockPos, boolean bl) {
		this(false, vec3, direction, blockPos, bl);
	}

	private BlockHitResult(boolean bl, Vec3 vec3, Direction direction, BlockPos blockPos, boolean bl2) {
		super(vec3);
		this.miss = bl;
		this.direction = direction;
		this.blockPos = blockPos;
		this.inside = bl2;
	}

	public BlockHitResult withDirection(Direction direction) {
		return new BlockHitResult(this.miss, this.location, direction, this.blockPos, this.inside);
	}

	public BlockPos getBlockPos() {
		return this.blockPos;
	}

	public Direction getDirection() {
		return this.direction;
	}

	@Override
	public HitResult.Type getType() {
		return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
	}

	public boolean isInside() {
		return this.inside;
	}
}
