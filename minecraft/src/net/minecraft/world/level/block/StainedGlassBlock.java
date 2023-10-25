package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StainedGlassBlock extends TransparentBlock implements BeaconBeamBlock {
	public static final MapCodec<StainedGlassBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(DyeColor.CODEC.fieldOf("color").forGetter(StainedGlassBlock::getColor), propertiesCodec()).apply(instance, StainedGlassBlock::new)
	);
	private final DyeColor color;

	@Override
	public MapCodec<StainedGlassBlock> codec() {
		return CODEC;
	}

	public StainedGlassBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
	}

	@Override
	public DyeColor getColor() {
		return this.color;
	}
}
