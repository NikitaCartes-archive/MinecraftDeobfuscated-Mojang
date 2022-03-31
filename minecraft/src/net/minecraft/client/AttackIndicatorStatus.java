package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

@Environment(EnvType.CLIENT)
public enum AttackIndicatorStatus implements OptionEnum {
	OFF(0, "options.off"),
	CROSSHAIR(1, "options.attack.crosshair"),
	HOTBAR(2, "options.attack.hotbar");

	private static final AttackIndicatorStatus[] BY_ID = (AttackIndicatorStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(AttackIndicatorStatus::getId))
		.toArray(AttackIndicatorStatus[]::new);
	private final int id;
	private final String key;

	private AttackIndicatorStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	public static AttackIndicatorStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
