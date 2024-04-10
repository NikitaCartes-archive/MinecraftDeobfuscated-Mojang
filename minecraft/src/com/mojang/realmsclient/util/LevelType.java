package com.mojang.realmsclient.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

@Environment(EnvType.CLIENT)
public enum LevelType {
	DEFAULT(0, WorldPresets.NORMAL),
	FLAT(1, WorldPresets.FLAT),
	LARGE_BIOMES(2, WorldPresets.LARGE_BIOMES),
	AMPLIFIED(3, WorldPresets.AMPLIFIED);

	private final int index;
	private final Component name;

	private LevelType(final int j, final ResourceKey<WorldPreset> resourceKey) {
		this.index = j;
		this.name = Component.translatable(resourceKey.location().toLanguageKey("generator"));
	}

	public Component getName() {
		return this.name;
	}

	public int getDtoIndex() {
		return this.index;
	}
}
