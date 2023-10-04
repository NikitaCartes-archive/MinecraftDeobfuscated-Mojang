package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredFallingBlock extends FallingBlock {
	public static final MapCodec<ColoredFallingBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter(coloredFallingBlock -> coloredFallingBlock.dustColor), propertiesCodec())
				.apply(instance, ColoredFallingBlock::new)
	);
	private final ColorRGBA dustColor;

	@Override
	public MapCodec<ColoredFallingBlock> codec() {
		return CODEC;
	}

	public ColoredFallingBlock(ColorRGBA colorRGBA, BlockBehaviour.Properties properties) {
		super(properties);
		this.dustColor = colorRGBA;
	}

	@Override
	public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return this.dustColor.rgba();
	}
}
