package net.minecraft.sounds;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SoundSource {
	MASTER("master"),
	MUSIC("music"),
	RECORDS("record"),
	WEATHER("weather"),
	BLOCKS("block"),
	HOSTILE("hostile"),
	NEUTRAL("neutral"),
	PLAYERS("player"),
	AMBIENT("ambient"),
	VOICE("voice");

	private static final Map<String, SoundSource> BY_NAME = (Map<String, SoundSource>)Arrays.stream(values())
		.collect(Collectors.toMap(SoundSource::getName, Function.identity()));
	private final String name;

	private SoundSource(String string2) {
		this.name = string2;
	}

	public String getName() {
		return this.name;
	}
}
