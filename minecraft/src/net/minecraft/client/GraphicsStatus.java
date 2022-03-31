package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

@Environment(EnvType.CLIENT)
public enum GraphicsStatus implements OptionEnum {
	FAST(0, "options.graphics.fast"),
	FANCY(1, "options.graphics.fancy"),
	FABULOUS(2, "options.graphics.fabulous");

	private static final GraphicsStatus[] BY_ID = (GraphicsStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(GraphicsStatus::getId))
		.toArray(GraphicsStatus[]::new);
	private final int id;
	private final String key;

	private GraphicsStatus(int j, String string2) {
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

	public String toString() {
		switch (this) {
			case FAST:
				return "fast";
			case FANCY:
				return "fancy";
			case FABULOUS:
				return "fabulous";
			default:
				throw new IllegalArgumentException();
		}
	}

	public static GraphicsStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
