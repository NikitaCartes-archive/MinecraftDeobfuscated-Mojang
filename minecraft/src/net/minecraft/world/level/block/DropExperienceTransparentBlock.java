package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropExperienceTransparentBlock extends DropExperienceBlock {
	public static final MapCodec<DropExperienceTransparentBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					IntProvider.codec(0, 10).fieldOf("experience").forGetter(dropExperienceTransparentBlock -> dropExperienceTransparentBlock.xpRange), propertiesCodec()
				)
				.apply(instance, DropExperienceTransparentBlock::new)
	);

	@Override
	public MapCodec<DropExperienceTransparentBlock> codec() {
		return CODEC;
	}

	public DropExperienceTransparentBlock(IntProvider intProvider, BlockBehaviour.Properties properties) {
		super(intProvider, properties);
	}

	@Override
	protected int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 2;
	}
}
