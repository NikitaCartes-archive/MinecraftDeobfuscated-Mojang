package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
	private final Function<TagKey<Block>, TagBuilder> blockTags;

	public ItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, TagsProvider<Block> tagsProvider) {
		super(packOutput, Registries.ITEM, completableFuture, item -> item.builtInRegistryHolder().key());
		this.blockTags = tagsProvider::getOrCreateRawBuilder;
	}

	protected void copy(TagKey<Block> tagKey, TagKey<Item> tagKey2) {
		TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey2);
		TagBuilder tagBuilder2 = (TagBuilder)this.blockTags.apply(tagKey);
		tagBuilder2.build().forEach(tagBuilder::add);
	}
}
