package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperStairBlock extends StairBlock implements WeatheringCopper {
	private final WeatheringCopper.WeatherState weatherState;
	private final Block changeTo;

	public WeatheringCopperStairBlock(BlockState blockState, BlockBehaviour.Properties properties) {
		super(blockState, properties);
		this.weatherState = WeatheringCopper.WeatherState.values()[WeatheringCopper.WeatherState.values().length - 1];
		this.changeTo = this;
	}

	public WeatheringCopperStairBlock(BlockState blockState, BlockBehaviour.Properties properties, WeatheringCopper.WeatherState weatherState, Block block) {
		super(blockState, properties);
		this.weatherState = weatherState;
		this.changeTo = block;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.onRandomTick(blockState, serverLevel, blockPos, random);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return this.changeTo != this;
	}

	public WeatheringCopper.WeatherState getAge() {
		return this.weatherState;
	}

	@Override
	public BlockState getChangeTo(BlockState blockState) {
		return this.changeTo
			.defaultBlockState()
			.setValue(FACING, blockState.getValue(FACING))
			.setValue(HALF, blockState.getValue(HALF))
			.setValue(SHAPE, blockState.getValue(SHAPE))
			.setValue(WATERLOGGED, blockState.getValue(WATERLOGGED));
	}
}
