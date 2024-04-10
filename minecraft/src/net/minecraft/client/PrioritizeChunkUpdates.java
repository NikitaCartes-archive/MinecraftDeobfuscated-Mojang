package net.minecraft.client;

import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

@Environment(EnvType.CLIENT)
public enum PrioritizeChunkUpdates implements OptionEnum {
	NONE(0, "options.prioritizeChunkUpdates.none"),
	PLAYER_AFFECTED(1, "options.prioritizeChunkUpdates.byPlayer"),
	NEARBY(2, "options.prioritizeChunkUpdates.nearby");

	private static final IntFunction<PrioritizeChunkUpdates> BY_ID = ByIdMap.continuous(PrioritizeChunkUpdates::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	private final int id;
	private final String key;

	private PrioritizeChunkUpdates(final int j, final String string2) {
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

	public static PrioritizeChunkUpdates byId(int i) {
		return (PrioritizeChunkUpdates)BY_ID.apply(i);
	}
}
