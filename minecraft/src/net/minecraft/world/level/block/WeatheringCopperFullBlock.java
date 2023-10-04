package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperFullBlock extends Block implements WeatheringCopper {
	public static final MapCodec<WeatheringCopperFullBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge), propertiesCodec())
				.apply(instance, WeatheringCopperFullBlock::new)
	);
	private final WeatheringCopper.WeatherState weatherState;

	@Override
	public MapCodec<WeatheringCopperFullBlock> codec() {
		return CODEC;
	}

	public WeatheringCopperFullBlock(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
		super(properties);
		this.weatherState = weatherState;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.onRandomTick(blockState, serverLevel, blockPos, randomSource);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return WeatheringCopper.getNext(blockState.getBlock()).isPresent();
	}

	public WeatheringCopper.WeatherState getAge() {
		return this.weatherState;
	}
}
