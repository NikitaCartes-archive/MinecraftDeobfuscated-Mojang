package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldPresetTagsProvider extends TagsProvider<WorldPreset> {
	public WorldPresetTagsProvider(PackOutput packOutput) {
		super(packOutput, BuiltinRegistries.WORLD_PRESET);
	}

	@Override
	protected void addTags() {
		this.tag(WorldPresetTags.NORMAL)
			.add(WorldPresets.NORMAL)
			.add(WorldPresets.FLAT)
			.add(WorldPresets.LARGE_BIOMES)
			.add(WorldPresets.AMPLIFIED)
			.add(WorldPresets.SINGLE_BIOME_SURFACE);
		this.tag(WorldPresetTags.EXTENDED).addTag(WorldPresetTags.NORMAL).add(WorldPresets.DEBUG);
	}
}
