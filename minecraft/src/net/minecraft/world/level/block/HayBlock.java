package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HayBlock extends RotatedPillarBlock {
	public static final MapCodec<HayBlock> CODEC = simpleCodec(HayBlock::new);

	@Override
	public MapCodec<HayBlock> codec() {
		return CODEC;
	}

	public HayBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		entity.causeFallDamage(f, 0.2F, level.damageSources().fall());
	}
}
