package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock {
	protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
	private final MobEffect suspiciousStewEffect;
	private final int effectDuration;

	public FlowerBlock(MobEffect mobEffect, int i, Block.Properties properties) {
		super(properties);
		this.suspiciousStewEffect = mobEffect;
		if (mobEffect.isInstantenous()) {
			this.effectDuration = i;
		} else {
			this.effectDuration = i * 20;
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		return SHAPE.move(vec3.x, vec3.y, vec3.z);
	}

	@Override
	public Block.OffsetType getOffsetType() {
		return Block.OffsetType.XZ;
	}

	public MobEffect getSuspiciousStewEffect() {
		return this.suspiciousStewEffect;
	}

	public int getEffectDuration() {
		return this.effectDuration;
	}
}
