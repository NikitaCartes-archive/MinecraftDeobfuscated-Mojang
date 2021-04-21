package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
	public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(
		false,
		Blocks.ACACIA_BUTTON.defaultBlockState(),
		Blocks.CANDLE.defaultBlockState(),
		Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
		Blocks.GLASS.defaultBlockState()
	);
	public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.BOOL.optionalFieldOf("debug_mode", Boolean.valueOf(false)).forGetter(CarverDebugSettings::isDebugMode),
					BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState),
					BlockState.CODEC.optionalFieldOf("water_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getWaterState),
					BlockState.CODEC.optionalFieldOf("lava_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getLavaState),
					BlockState.CODEC.optionalFieldOf("barrier_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getBarrierState)
				)
				.apply(instance, CarverDebugSettings::new)
	);
	private boolean debugMode;
	private final BlockState airState;
	private final BlockState waterState;
	private final BlockState lavaState;
	private final BlockState barrierState;

	public static CarverDebugSettings of(boolean bl, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4) {
		return new CarverDebugSettings(bl, blockState, blockState2, blockState3, blockState4);
	}

	public static CarverDebugSettings of(BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4) {
		return new CarverDebugSettings(false, blockState, blockState2, blockState3, blockState4);
	}

	public static CarverDebugSettings of(boolean bl, BlockState blockState) {
		return new CarverDebugSettings(bl, blockState, DEFAULT.getWaterState(), DEFAULT.getLavaState(), DEFAULT.getBarrierState());
	}

	private CarverDebugSettings(boolean bl, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4) {
		this.debugMode = bl;
		this.airState = blockState;
		this.waterState = blockState2;
		this.lavaState = blockState3;
		this.barrierState = blockState4;
	}

	public boolean isDebugMode() {
		return this.debugMode;
	}

	public BlockState getAirState() {
		return this.airState;
	}

	public BlockState getWaterState() {
		return this.waterState;
	}

	public BlockState getLavaState() {
		return this.lavaState;
	}

	public BlockState getBarrierState() {
		return this.barrierState;
	}
}
