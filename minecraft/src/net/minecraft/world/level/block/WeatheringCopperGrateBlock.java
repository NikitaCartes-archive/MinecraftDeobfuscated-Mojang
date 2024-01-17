package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperGrateBlock extends WaterloggedTransparentBlock implements WeatheringCopper {
	public static final MapCodec<WeatheringCopperGrateBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperGrateBlock::getAge), propertiesCodec())
				.apply(instance, WeatheringCopperGrateBlock::new)
	);
	private final WeatheringCopper.WeatherState weatherState;

	@Override
	protected MapCodec<WeatheringCopperGrateBlock> codec() {
		return CODEC;
	}

	protected WeatheringCopperGrateBlock(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
		super(properties);
		this.weatherState = weatherState;
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.changeOverTime(blockState, serverLevel, blockPos, randomSource);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		return WeatheringCopper.getNext(blockState.getBlock()).isPresent();
	}

	public WeatheringCopper.WeatherState getAge() {
		return this.weatherState;
	}
}
