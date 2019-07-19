package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public enum AmbientOcclusionStatus {
	OFF(0, "options.ao.off"),
	MIN(1, "options.ao.min"),
	MAX(2, "options.ao.max");

	private static final AmbientOcclusionStatus[] BY_ID = (AmbientOcclusionStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(AmbientOcclusionStatus::getId))
		.toArray(AmbientOcclusionStatus[]::new);
	private final int id;
	private final String key;

	private AmbientOcclusionStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	public String getKey() {
		return this.key;
	}

	public static AmbientOcclusionStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
