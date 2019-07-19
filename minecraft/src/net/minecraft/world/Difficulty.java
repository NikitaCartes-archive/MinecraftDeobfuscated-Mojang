package net.minecraft.world;

import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum Difficulty {
	PEACEFUL(0, "peaceful"),
	EASY(1, "easy"),
	NORMAL(2, "normal"),
	HARD(3, "hard");

	private static final Difficulty[] BY_ID = (Difficulty[])Arrays.stream(values()).sorted(Comparator.comparingInt(Difficulty::getId)).toArray(Difficulty[]::new);
	private final int id;
	private final String key;

	private Difficulty(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	public Component getDisplayName() {
		return new TranslatableComponent("options.difficulty." + this.key);
	}

	public static Difficulty byId(int i) {
		return BY_ID[i % BY_ID.length];
	}

	@Nullable
	public static Difficulty byName(String string) {
		for (Difficulty difficulty : values()) {
			if (difficulty.key.equals(string)) {
				return difficulty;
			}
		}

		return null;
	}

	public String getKey() {
		return this.key;
	}
}
