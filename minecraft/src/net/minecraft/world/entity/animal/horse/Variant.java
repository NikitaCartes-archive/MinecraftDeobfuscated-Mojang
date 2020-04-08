package net.minecraft.world.entity.animal.horse;

import java.util.Arrays;
import java.util.Comparator;

public enum Variant {
	WHITE(0),
	CREAMY(1),
	CHESTNUT(2),
	BROWN(3),
	BLACK(4),
	GRAY(5),
	DARKBROWN(6);

	private static final Variant[] BY_ID = (Variant[])Arrays.stream(values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(Variant[]::new);
	private final int id;

	private Variant(int j) {
		this.id = j;
	}

	public int getId() {
		return this.id;
	}

	public static Variant byId(int i) {
		return BY_ID[i % BY_ID.length];
	}
}
