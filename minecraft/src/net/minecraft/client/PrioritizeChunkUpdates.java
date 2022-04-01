package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public enum PrioritizeChunkUpdates {
	NONE(0, "options.prioritizeChunkUpdates.none"),
	PLAYER_AFFECTED(1, "options.prioritizeChunkUpdates.byPlayer"),
	NEARBY(2, "options.prioritizeChunkUpdates.nearby");

	private static final PrioritizeChunkUpdates[] BY_ID = (PrioritizeChunkUpdates[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(PrioritizeChunkUpdates::getId))
		.toArray(PrioritizeChunkUpdates[]::new);
	private final int id;
	private final String key;

	private PrioritizeChunkUpdates(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	public String getKey() {
		return this.key;
	}

	public static PrioritizeChunkUpdates byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
