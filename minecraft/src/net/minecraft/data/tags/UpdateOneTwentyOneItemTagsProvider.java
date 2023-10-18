package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class UpdateOneTwentyOneItemTagsProvider extends ItemTagsProvider {
	public UpdateOneTwentyOneItemTagsProvider(
		PackOutput packOutput,
		CompletableFuture<HolderLookup.Provider> completableFuture,
		CompletableFuture<TagsProvider.TagLookup<Item>> completableFuture2,
		CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture3
	) {
		super(packOutput, completableFuture, completableFuture2, completableFuture3);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
	}
}
