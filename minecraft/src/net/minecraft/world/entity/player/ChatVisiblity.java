package net.minecraft.world.entity.player;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

public enum ChatVisiblity {
	FULL(0, "options.chat.visibility.full"),
	SYSTEM(1, "options.chat.visibility.system"),
	HIDDEN(2, "options.chat.visibility.hidden");

	private static final ChatVisiblity[] BY_ID = (ChatVisiblity[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(ChatVisiblity::getId))
		.toArray(ChatVisiblity[]::new);
	private final int id;
	private final String key;

	private ChatVisiblity(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public String getKey() {
		return this.key;
	}

	@Environment(EnvType.CLIENT)
	public static ChatVisiblity byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
