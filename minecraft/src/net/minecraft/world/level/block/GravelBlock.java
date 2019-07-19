package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;

public class GravelBlock extends FallingBlock {
	public GravelBlock(Block.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getDustColor(BlockState blockState) {
		return -8356741;
	}
}
