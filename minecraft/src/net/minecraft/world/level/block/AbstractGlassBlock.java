package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractGlassBlock extends HalfTransparentBlock {
	protected AbstractGlassBlock(Block.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	@Override
	public boolean isSuffocating(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	public boolean isRedstoneConductor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	public boolean isValidSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
		return false;
	}
}
