package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldPresetTagsProvider extends TagsProvider<WorldPreset> {
	public WorldPresetTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registry.WORLD_PRESET_REGISTRY, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(WorldPresetTags.NORMAL)
			.add(WorldPresets.NORMAL)
			.add(WorldPresets.FLAT)
			.add(WorldPresets.LARGE_BIOMES)
			.add(WorldPresets.AMPLIFIED)
			.add(WorldPresets.SINGLE_BIOME_SURFACE);
		this.tag(WorldPresetTags.EXTENDED).addTag(WorldPresetTags.NORMAL).add(WorldPresets.DEBUG);
	}
}
