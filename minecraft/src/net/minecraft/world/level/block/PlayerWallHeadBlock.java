package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PlayerWallHeadBlock extends WallSkullBlock {
	public static final MapCodec<PlayerWallHeadBlock> CODEC = simpleCodec(PlayerWallHeadBlock::new);

	@Override
	public MapCodec<PlayerWallHeadBlock> codec() {
		return CODEC;
	}

	protected PlayerWallHeadBlock(BlockBehaviour.Properties properties) {
		super(SkullBlock.Types.PLAYER, properties);
	}
}
