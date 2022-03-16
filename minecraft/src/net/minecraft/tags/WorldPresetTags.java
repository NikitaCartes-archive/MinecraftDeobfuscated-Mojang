package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class WorldPresetTags {
	public static final TagKey<WorldPreset> NORMAL = create("normal");
	public static final TagKey<WorldPreset> EXTENDED = create("extended");

	private WorldPresetTags() {
	}

	private static TagKey<WorldPreset> create(String string) {
		return TagKey.create(Registry.WORLD_PRESET_REGISTRY, new ResourceLocation(string));
	}
}
