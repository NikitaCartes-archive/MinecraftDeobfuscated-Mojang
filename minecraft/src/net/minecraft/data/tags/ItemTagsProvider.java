package net.minecraft.data.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
	private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
	private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap();

	public ItemTagsProvider(
		PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture2
	) {
		super(packOutput, Registries.ITEM, completableFuture, item -> item.builtInRegistryHolder().key());
		this.blockTags = completableFuture2;
	}

	public ItemTagsProvider(
		PackOutput packOutput,
		CompletableFuture<HolderLookup.Provider> completableFuture,
		CompletableFuture<TagsProvider.TagLookup<Item>> completableFuture2,
		CompletableFuture<TagsProvider.TagLookup<Block>> completableFuture3
	) {
		super(packOutput, Registries.ITEM, completableFuture, completableFuture2, item -> item.builtInRegistryHolder().key());
		this.blockTags = completableFuture3;
	}

	protected void copy(TagKey<Block> tagKey, TagKey<Item> tagKey2) {
		this.tagsToCopy.put(tagKey, tagKey2);
	}

	@Override
	protected CompletableFuture<HolderLookup.Provider> contentsProvider() {
		return super.contentsProvider().thenCombineAsync(this.blockTags, (provider, tagLookup) -> {
			this.tagsToCopy.forEach((tagKey, tagKey2) -> {
				TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey2);
				Optional<TagBuilder> optional = (Optional<TagBuilder>)tagLookup.apply(tagKey);
				((TagBuilder)optional.orElseThrow(() -> new IllegalStateException("Missing block tag " + tagKey2.location()))).build().forEach(tagBuilder::add);
			});
			return provider;
		});
	}
}
