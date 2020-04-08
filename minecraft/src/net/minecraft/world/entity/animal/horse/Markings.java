package net.minecraft.world.entity.animal.horse;

import java.util.Arrays;
import java.util.Comparator;

public enum Markings {
	NONE(0),
	WHITE(1),
	WHITE_FIELD(2),
	WHITE_DOTS(3),
	BLACK_DOTS(4);

	private static final Markings[] BY_ID = (Markings[])Arrays.stream(values()).sorted(Comparator.comparingInt(Markings::getId)).toArray(Markings[]::new);
	private final int id;

	private Markings(int j) {
		this.id = j;
	}

	public int getId() {
		return this.id;
	}

	public static Markings byId(int i) {
		return BY_ID[i % BY_ID.length];
	}
}
