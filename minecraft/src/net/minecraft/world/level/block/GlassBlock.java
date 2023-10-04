package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class GlassBlock extends AbstractGlassBlock {
	public static final MapCodec<GlassBlock> CODEC = simpleCodec(GlassBlock::new);

	@Override
	public MapCodec<GlassBlock> codec() {
		return CODEC;
	}

	public GlassBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
}
