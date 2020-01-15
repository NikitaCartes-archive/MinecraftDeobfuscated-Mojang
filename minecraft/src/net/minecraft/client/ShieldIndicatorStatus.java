package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public enum ShieldIndicatorStatus {
	OFF(0, "options.off"),
	CROSSHAIR(1, "options.attack.crosshair"),
	HOTBAR(2, "options.attack.hotbar");

	private static final ShieldIndicatorStatus[] BY_ID = (ShieldIndicatorStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(ShieldIndicatorStatus::getId))
		.toArray(ShieldIndicatorStatus[]::new);
	private final int id;
	private final String key;

	private ShieldIndicatorStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	public String getKey() {
		return this.key;
	}

	public static ShieldIndicatorStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
