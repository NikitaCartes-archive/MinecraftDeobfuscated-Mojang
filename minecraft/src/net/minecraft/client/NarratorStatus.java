package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public enum NarratorStatus {
	OFF(0, "options.narrator.off"),
	ALL(1, "options.narrator.all"),
	CHAT(2, "options.narrator.chat"),
	SYSTEM(3, "options.narrator.system");

	private static final NarratorStatus[] BY_ID = (NarratorStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(NarratorStatus::getId))
		.toArray(NarratorStatus[]::new);
	private final int id;
	private final Component name;

	private NarratorStatus(int j, String string2) {
		this.id = j;
		this.name = new TranslatableComponent(string2);
	}

	public int getId() {
		return this.id;
	}

	public Component getName() {
		return this.name;
	}

	public static NarratorStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}
