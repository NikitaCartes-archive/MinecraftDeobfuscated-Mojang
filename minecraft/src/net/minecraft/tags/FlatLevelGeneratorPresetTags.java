package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

public class FlatLevelGeneratorPresetTags {
	public static final TagKey<FlatLevelGeneratorPreset> VISIBLE = create("visible");

	private FlatLevelGeneratorPresetTags() {
	}

	private static TagKey<FlatLevelGeneratorPreset> create(String string) {
		return TagKey.create(Registries.FLAT_LEVEL_GENERATOR_PRESET, ResourceLocation.withDefaultNamespace(string));
	}
}
