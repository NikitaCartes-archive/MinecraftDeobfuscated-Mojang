package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.WinterDropBiomes;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public class WinterDropBiomeTagsProvider extends TagsProvider<Biome> {
	public WinterDropBiomeTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Biome>> completableFuture2
	) {
		super(packOutput, Registries.BIOME, completableFuture, completableFuture2);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BiomeTags.IS_FOREST).add(WinterDropBiomes.PALE_GARDEN);
		this.tag(BiomeTags.STRONGHOLD_BIASED_TO).add(WinterDropBiomes.PALE_GARDEN);
		this.tag(BiomeTags.IS_OVERWORLD).add(WinterDropBiomes.PALE_GARDEN);
		this.tag(BiomeTags.HAS_TRIAL_CHAMBERS).add(WinterDropBiomes.PALE_GARDEN);
	}
}
