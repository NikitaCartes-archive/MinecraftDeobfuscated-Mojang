package net.minecraft.world.level.block;

import net.minecraft.world.level.BlockLayer;

public class GlassBlock extends AbstractGlassBlock {
	public GlassBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public BlockLayer getRenderLayer() {
		return BlockLayer.CUTOUT;
	}
}
