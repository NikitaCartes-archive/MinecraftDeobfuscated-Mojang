package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends FallingBlock {
	private final int dustColor;

	public SandBlock(int i, BlockBehaviour.Properties properties) {
		super(properties);
		this.dustColor = i;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.dustColor;
	}
}
