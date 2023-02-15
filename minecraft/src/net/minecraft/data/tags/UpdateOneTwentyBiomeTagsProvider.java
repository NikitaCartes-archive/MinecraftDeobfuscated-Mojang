package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class UpdateOneTwentyBiomeTagsProvider extends TagsProvider<Biome> {
	public UpdateOneTwentyBiomeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.BIOME, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BiomeTags.IS_MOUNTAIN).add(Biomes.CHERRY_GROVE);
	}
}
