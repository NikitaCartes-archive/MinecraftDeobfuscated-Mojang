package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends FallingBlock {
	private final int dustColor;

	public SandBlock(int i, Block.Properties properties) {
		super(properties);
		this.dustColor = i;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getDustColor(BlockState blockState) {
		return this.dustColor;
	}
}
