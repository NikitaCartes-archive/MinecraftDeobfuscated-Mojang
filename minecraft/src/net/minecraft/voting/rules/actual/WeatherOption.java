package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum WeatherOption implements StringRepresentable {
	DEFAULT("default"),
	NEVER("never"),
	ALWAYS("always");

	public static final Codec<WeatherOption> CODEC = StringRepresentable.fromEnum(WeatherOption::values);
	private final String id;
	private final Component thunderDescription;
	private final Component rainDescription;

	private WeatherOption(String string2) {
		this.id = string2;
		this.rainDescription = Component.translatable("rule.weather.rain." + string2);
		this.thunderDescription = Component.translatable("rule.weather.thunder." + string2);
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public Component rainDescription() {
		return this.rainDescription;
	}

	public Component thunderDescription() {
		return this.thunderDescription;
	}
}
