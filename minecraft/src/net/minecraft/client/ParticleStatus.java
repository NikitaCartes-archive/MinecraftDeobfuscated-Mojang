package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

@Environment(EnvType.CLIENT)
public enum ParticleStatus implements OptionEnum {
	ALL(0, "options.particles.all"),
	DECREASED(1, "options.particles.decreased"),
	MINIMAL(2, "options.particles.minimal");

	private static final ParticleStatus[] BY_ID = (ParticleStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(ParticleStatus::getId))
		.toArray(ParticleStatus[]::new);
	private final int id;
	private final String key;

	private ParticleStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public static ParticleStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
