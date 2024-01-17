package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class WeatheringCopperTrapDoorBlock extends TrapDoorBlock implements WeatheringCopper {
	public static final MapCodec<WeatheringCopperTrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BlockSetType.CODEC.fieldOf("block_set_type").forGetter(TrapDoorBlock::getType),
					WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperTrapDoorBlock::getAge),
					propertiesCodec()
				)
				.apply(instance, WeatheringCopperTrapDoorBlock::new)
	);
	private final WeatheringCopper.WeatherState weatherState;

	@Override
	public MapCodec<WeatheringCopperTrapDoorBlock> codec() {
		return CODEC;
	}

	protected WeatheringCopperTrapDoorBlock(BlockSetType blockSetType, WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
		super(blockSetType, properties);
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
