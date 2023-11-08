package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public class UpdateOneTwentyOneBiomeTagsProvider extends TagsProvider<Biome> {
	public UpdateOneTwentyOneBiomeTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Biome>> completableFuture2
	) {
		super(packOutput, Registries.BIOME, completableFuture, completableFuture2);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BiomeTags.HAS_TRIAL_CHAMBERS).addTag(BiomeTags.IS_OVERWORLD);
	}
}
