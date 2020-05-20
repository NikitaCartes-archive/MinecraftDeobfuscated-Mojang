package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState extends BlockBehaviour.BlockStateBase {
	public static final Codec<BlockState> CODEC = codec(Registry.BLOCK, Block::defaultBlockState).stable();

	public BlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
		super(block, immutableMap, mapCodec);
	}

	@Override
	protected BlockState asState() {
		return this;
	}
}
