package net.minecraft.world.level.block;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
	@Override
	default float getChanceModifier() {
		return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
	}

	public static enum WeatherState {
		UNAFFECTED,
		EXPOSED,
		WEATHERED,
		OXIDIZED;
	}
}
