package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StainedGlassPaneBlock extends IronBarsBlock implements BeaconBeamBlock {
	public static final MapCodec<StainedGlassPaneBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(DyeColor.CODEC.fieldOf("color").forGetter(StainedGlassPaneBlock::getColor), propertiesCodec())
				.apply(instance, StainedGlassPaneBlock::new)
	);
	private final DyeColor color;

	@Override
	public MapCodec<StainedGlassPaneBlock> codec() {
		return CODEC;
	}

	public StainedGlassPaneBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	public DyeColor getColor() {
		return this.color;
	}
}
