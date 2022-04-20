package com.mojang.realmsclient.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public enum LevelType {
	DEFAULT(0, Component.translatable("generator.default")),
	FLAT(1, Component.translatable("generator.flat")),
	LARGE_BIOMES(2, Component.translatable("generator.large_biomes")),
	AMPLIFIED(3, Component.translatable("generator.amplified"));

	private final int index;
	private final Component name;

	private LevelType(int j, Component component) {
		this.index = j;
		this.name = component;
	}

	public Component getName() {
		return this.name;
	}

	public int getDtoIndex() {
		return this.index;
	}
}
