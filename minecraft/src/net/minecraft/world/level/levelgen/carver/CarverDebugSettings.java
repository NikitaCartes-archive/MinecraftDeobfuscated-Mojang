package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
	public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(false, Blocks.ACACIA_BUTTON.defaultBlockState());
	public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.BOOL.optionalFieldOf("debug_mode", Boolean.valueOf(false)).forGetter(CarverDebugSettings::isDebugMode),
					BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState)
				)
				.apply(instance, CarverDebugSettings::new)
	);
	private boolean debugMode;
	private final BlockState airState;

	public static CarverDebugSettings of(boolean bl, BlockState blockState) {
		return new CarverDebugSettings(bl, blockState);
	}

	private CarverDebugSettings(boolean bl, BlockState blockState) {
		this.debugMode = bl;
		this.airState = blockState;
	}

	public boolean isDebugMode() {
		return this.debugMode;
	}

	public BlockState getAirState() {
		return this.airState;
	}
}
